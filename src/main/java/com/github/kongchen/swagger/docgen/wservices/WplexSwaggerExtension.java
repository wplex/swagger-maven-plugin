package com.github.kongchen.swagger.docgen.wservices;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.github.kongchen.swagger.docgen.reader.WplexApiReader;
import com.wplex.services.common.annotation.Body;
import com.wplex.services.common.annotation.PathParam;
import com.wplex.services.common.annotation.QueryParam;

import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;

/**
 * The Wplex Swagger Extension for {@link WplexApiReader}
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
public class WplexSwaggerExtension extends AbstractSwaggerExtension {

	@Override
	public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip,
			Iterator<SwaggerExtension> chain) {
		if (this.shouldIgnoreType(type, typesToSkip)) {
			return new ArrayList<Parameter>();
		}

		String defaultValue = "";
		List<Parameter> parameters = new ArrayList<>();
		Parameter parameter = null;

		for (Annotation annotation : annotations) {

			/*
			if (annotation instanceof Body) {
				parameters.addAll(extractParametersFromBodyAnnotation(annotation, type));
			} else {
				parameter = extractParameterFromAnnotation(annotation, defaultValue, type);
			}
			*/
			parameter = extractParameterFromAnnotation(annotation, defaultValue, type);

			if (parameter != null) {
				parameters.add(parameter);
			}
		}

		return parameters;
	}

	private Parameter extractParameterFromAnnotation(Annotation annotation, String defaultValue, Type type) {
		Parameter parameter = null;

		if (annotation instanceof QueryParam) {
			QueryParam queryParam = (QueryParam) annotation;
			QueryParameter queryParameter = new QueryParameter().name(queryParam.key());

			Property schema = ModelConverters.getInstance().readAsProperty(type);
			if (schema != null) {
				queryParameter.setProperty(schema);
			}

			parameter = queryParameter;
		} else if (annotation instanceof PathParam) {
			PathParam pathParam = (PathParam) annotation;
			PathParameter pathParameter = new PathParameter().name(pathParam.key());

			Property schema = ModelConverters.getInstance().readAsProperty(type);
			if (schema != null) {
				pathParameter.setProperty(schema);
			}

			parameter = pathParameter;
		}

		return parameter;
	}

	private List<Parameter> extractParametersFromBodyAnnotation(Annotation annotation, Type type) {
		if (!(annotation instanceof Body)) {
			return null;
		}

		Class<?> cls = TypeUtils.getRawType(type, type);
		List<Parameter> parameters = new ArrayList<>();


		return null;
	}
}
