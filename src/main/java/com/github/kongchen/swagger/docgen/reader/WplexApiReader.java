package com.github.kongchen.swagger.docgen.reader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.github.kongchen.swagger.docgen.GenerateException;

import com.wplex.services.common.annotation.Route;
import com.wplex.services.common.annotation.RouteResource;
import com.wplex.services.common.resource.Response;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.ReflectionUtils;

/**
 * The Wplex API reader for wplex-services.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
public class WplexApiReader extends AbstractReader implements ClassSwaggerReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(WplexApiReader.class);
	private static final ResponseContainerConverter RESPONSE_CONTAINER_CONVERTER = new ResponseContainerConverter();

	public WplexApiReader(Swagger swagger, Log log) {
		super(swagger, log);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Swagger read(Set<Class<?>> classes) throws GenerateException {
		for (Class cls : classes) {
			read(cls);
		}

		return swagger;
	}

	@SuppressWarnings("rawtypes")
	public Swagger read(Class cls) {
		return read(cls, "", null, false, new String[0], new String[0], new HashMap<String, Tag>(), new ArrayList<Parameter>());
	}

	protected Swagger read(Class<?> cls, String parentPath, String parentMethod, boolean readHidden,
			String[] parentConsumes, String[] parentProduces, Map<String, Tag> parentTags,
			List<Parameter> parentParameters) {

		if (swagger == null) {
			swagger = new Swagger();
		}

		Api api = AnnotationUtils.findAnnotation(cls, Api.class);
		RouteResource apiRouteResource = AnnotationUtils.findAnnotation(cls, RouteResource.class);
		Route apiRoute = AnnotationUtils.findAnnotation(cls, Route.class);

		// only read if allowing hidden apis OR api is not marked as hidden
		if (!canReadApi(readHidden, api)) {
			return swagger;
		}

		Map<String, Tag> tags = updateTagsForApi(parentTags, api);
		List<SecurityRequirement> securities = getSecurityRequirements(api);

		// parse the method
		for (Method method : cls.getMethods()) {
			ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
			if (apiOperation == null || apiOperation.hidden()) {
				continue;
			}

			Route methodRoute = AnnotationUtils.findAnnotation(method, Route.class);

			String operationRoute = getRoute(apiRouteResource, apiRoute, parentPath);
			if (operationRoute != null) {
				Map<String, String> regexMap = new HashMap<String, String>();
				operationRoute = parseOperationPath(operationRoute, regexMap);

				// http method-verb
				String httpMethod = methodRoute.method().toString().toLowerCase();
				Operation operation = parseMethod(method);
				updateOperationParameters(parentParameters, regexMap, operation);
				updateOperationProtocols(apiOperation, operation);

				// TODO consumes, produces

				//

			}
			updateTagDescriptions();
		}

		return swagger;
	}

	private String getRoute(RouteResource classLevelRoute, Route methodLevelRoute, String parentRoute) {
		if (classLevelRoute == null && methodLevelRoute == null) {
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();
		if (parentRoute != null && !parentRoute.isEmpty() && !parentRoute.equals("/")) {
			if (!parentRoute.startsWith("/")) {
				parentRoute = "/" + parentRoute;
			}

			if (parentRoute.endsWith("/")) {
				parentRoute = parentRoute.substring(0, parentRoute.length() - 1);
			}

			stringBuilder.append(parentRoute);
		}

		if (classLevelRoute != null) {
			stringBuilder.append(classLevelRoute.path());
		}

		if (methodLevelRoute != null && !methodLevelRoute.path().equals("/")) {
			String methodRoute = methodLevelRoute.path();
			if (!methodRoute.startsWith("/") && !stringBuilder.toString().endsWith("/")) {
				stringBuilder.append("/");
			}
			if (methodRoute.endsWith("/")) {
				methodRoute = methodRoute.substring(0, methodRoute.length() - 1);
			}
			stringBuilder.append(methodRoute);
		}

		String output = stringBuilder.toString();
		if (!output.startsWith("/")) {
			output = "/" + output;
		}

		if (output.endsWith("/") && output.length() > 1) {
			return output.substring(0, output.length() - 1);
		} else {
			return output;
		}
	}

	private void updateTagDescriptions() {
		Map<String, Tag> tags = new HashMap<String, Tag>();
		for (Class<?> aClass : new Reflections("").getTypesAnnotatedWith(SwaggerDefinition.class)) {
			SwaggerDefinition swaggerDefinition = AnnotationUtils.findAnnotation(aClass, SwaggerDefinition.class);

			for (io.swagger.annotations.Tag tag : swaggerDefinition.tags()) {
				String tagName = tag.name();
				if (!tagName.isEmpty()) {
					tags.put(tag.name(), new Tag().name(tag.name()).description(tag.description()));
				}
			}
		}

		if (swagger.getTags() != null) {
			for (Tag tag : swagger.getTags()) {
				Tag rightTag = tags.get(tag.getName());
				if (rightTag != null && rightTag.getDescription() != null) {
					tag.setDescription(rightTag.getDescription());
				}
			}
		}
	}

	private Operation parseMethod(Method method) {
		Operation operation = new Operation();
		ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);

		String operationId = method.getName();
		String responseContainer = null;

		Class<?> responseClass = null;
		Map<String, Property> defaultResponseHeaders = null;

		if (apiOperation != null) {
			if (apiOperation.hidden()) {
				return null;
			}
			if (!apiOperation.nickname().isEmpty()) {
				operationId = apiOperation.nickname();
			}

			defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders());
			operation.summary(apiOperation.value()).description(apiOperation.notes());

			Set<Map<String, Object>> customExtensions = parseCustomExtensions(apiOperation.extensions());
			if (customExtensions != null) {
				for (Map<String, Object> extension : customExtensions) {
					if (extension == null) {
						continue;
					}
					for (Map.Entry<String, Object> map : extension.entrySet()) {
						operation.setVendorExtension(map.getKey().startsWith("x-") ? map.getKey() : "x-" + map.getKey(),
								map.getValue());
					}
				}
			}

			if (!apiOperation.response().equals(Void.class)) {
				responseClass = apiOperation.response();
			}
			if (!apiOperation.responseContainer().isEmpty()) {
				responseContainer = apiOperation.responseContainer();
			}

			List<SecurityRequirement> securities = new ArrayList<SecurityRequirement>();
			for (Authorization auth : apiOperation.authorizations()) {
				if (!auth.value().isEmpty()) {
					SecurityRequirement security = new SecurityRequirement();
					security.setName(auth.value());
					for (AuthorizationScope scope : auth.scopes()) {
						if (!scope.scope().isEmpty()) {
							security.addScope(scope.scope());
						}
					}
					securities.add(security);
				}
			}

			for (SecurityRequirement sec : securities) {
				operation.security(sec);
			}
		}

		operation.operationId(operationId);

		if (responseClass == null) {
			// pick out response from method declaration
			LOGGER.debug("[wplex] picking up response class from method: " + method);
			Type t = method.getGenericReturnType();
			responseClass = method.getReturnType();

			if (!responseClass.equals(Void.class) && !responseClass.equals(void.class)
					&& (AnnotationUtils.findAnnotation(responseClass, Api.class) == null)) {
				LOGGER.debug("[wplex] reading model: " + responseClass);
				Map<String, Model> models = ModelConverters.getInstance().readAll(t);
			}
		}

		if (responseClass != null
				&& !responseClass.equals(Void.class)
				&& !responseClass.equals(Response.class)
				&& (AnnotationUtils.findAnnotation(responseClass, Api.class) == null)) {
			if (isPrimitive(responseClass)) {
				Property property = ModelConverters.getInstance().readAsProperty(responseClass);
				if (property != null) {
					Property responseProperty = RESPONSE_CONTAINER_CONVERTER.withResponseContainer(responseContainer, property);

					operation.response(apiOperation.code(), new io.swagger.models.Response()
							.description("sucessful operation")
							.schema(responseProperty)
							.headers(defaultResponseHeaders));
				}
			} else if (!responseClass.equals(Void.class) && !responseClass.equals(void.class)) {
				Map<String, Model> models = ModelConverters.getInstance().read(responseClass);
				if (models.isEmpty()) {
					Property p = ModelConverters.getInstance().readAsProperty(responseClass);
					operation.response(apiOperation.code(), new io.swagger.models.Response()
							.description("sucessfull operation")
							.schema(p)
							.headers(defaultResponseHeaders));
				}

				for (String key : models.keySet()) {
					Property responseProperty = RESPONSE_CONTAINER_CONVERTER.withResponseContainer(responseContainer, new RefProperty().asDefault(key));

					operation.response(apiOperation.code(), new io.swagger.models.Response()
							.description("sucessfull operation")
							.schema(responseProperty)
							.headers(defaultResponseHeaders));
					swagger.model(key, models.get(key));
				}

				models = ModelConverters.getInstance().readAll(responseClass);
				for (Map.Entry<String, Model> entry : models.entrySet()) {
					swagger.model(entry.getKey(), entry.getValue());
				}
			}
		}

		// TODO consumes e produces - JSONCodec

		//
		ApiResponses responseAnnotation = AnnotationUtils.findAnnotation(method, ApiResponses.class);
		if (responseAnnotation != null) {
			updateApiResponse(operation, responseAnnotation);
		}

		if (AnnotationUtils.findAnnotation(method, Deprecated.class) != null) {
			operation.deprecated(true);
		}

		// process parameters
		Class[] parameterTypes = method.getParameterTypes();
		Type[] genericParameterTypes = method.getGenericParameterTypes();
		Annotation[][] paramAnnotations = findParamAnnotations(method);

		for (int i = 0; i < parameterTypes.length; i++) {
			Type type = genericParameterTypes[i];
			List<Annotation> annotations = Arrays.asList(paramAnnotations[i]);
			List<Parameter> parameters = getParameters(type, annotations);

			for (Parameter parameter : parameters) {
				operation.parameter(parameter);
			}
		}

		if (operation.getResponses() == null) {
			operation.defaultResponse(new io.swagger.models.Response().description("sucessful operation"));
		}

		// process @ApiImplicitParams
		this.readImplicitParameters(method, operation);
		processOperationDecorator(operation, method);

		return operation;
	}

	private Annotation[][] findParamAnnotations(Method method) {
		Annotation[][] paramAnnotation = method.getParameterAnnotations();
		method = ReflectionUtils.getOverriddenMethod(method);

		while (method != null) {
			paramAnnotation = merge(paramAnnotation, method.getParameterAnnotations());
			method = ReflectionUtils.getOverriddenMethod(method);
		}

		return paramAnnotation;
	}

	private Annotation[][] merge(Annotation[][] paramAnnotation, Annotation[][] superMethodParamAnnotations) {
		Annotation[][] mergedAnnotations = new Annotation[paramAnnotation.length][];

		for (int i = 0; i < paramAnnotation.length; i++) {
			mergedAnnotations[i] = merge(paramAnnotation[i], superMethodParamAnnotations[i]);
		}

		return mergedAnnotations;
	}

	private Annotation[] merge(Annotation[] annotations, Annotation[] annotations2) {
		Set<Annotation> mergedAnnotations = new HashSet<Annotation>();
		mergedAnnotations.addAll(Arrays.asList(annotations));
		mergedAnnotations.addAll(Arrays.asList(annotations2));
		return mergedAnnotations.toArray(new Annotation[0]);
	}

}
