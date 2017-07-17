package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.WplexApiReader;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.wplex.services.common.annotation.RouteResource;

import io.swagger.annotations.Api;
import io.swagger.config.FilterFactory;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.models.auth.SecuritySchemeDefinition;

/**
 * The Wplex Maven Document Source for wplex-services.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
public class WplexMavenDocumentSource extends AbstractDocumentSource {

	public WplexMavenDocumentSource(ApiSource apiSource, Log log, String encoding) throws MojoFailureException {
		super(log, apiSource);
		if (encoding != null) {
			this.encoding = encoding;
		}
	}

	@Override
	public void loadDocuments() throws GenerateException {
		if (apiSource.getSwaggerInternalFilter() != null) {
			try {
				LOG.info("[wplex] Setting filter configuration: " + apiSource.getSwaggerInternalFilter());
				FilterFactory.setFilter(
						(SwaggerSpecFilter) Class.forName(apiSource.getSwaggerInternalFilter()).newInstance());
			} catch (Exception e) {
				throw new GenerateException("[wplex] Cannot load: " + apiSource.getSwaggerInternalFilter(), e);
			}
		}

		SetView<Class<?>> validClasses = getValidClasses();
		swagger = resolveApiReader().read(validClasses);

		if (apiSource.getSecurityDefinitions() != null) {
			for (SecurityDefinition sd : apiSource.getSecurityDefinitions()) {
				for (Map.Entry<String, SecuritySchemeDefinition> entry : sd.getDefinitions().entrySet()) {
					swagger.addSecurityDefinition(entry.getKey(), entry.getValue());
				}
			}

			// sort security defs to make output consistent
			Map<String, SecuritySchemeDefinition> defs = swagger.getSecurityDefinitions();
			Map<String, SecuritySchemeDefinition> sortedDefs = new TreeMap<String, SecuritySchemeDefinition>();

			sortedDefs.putAll(defs);
			swagger.setSecurityDefinitions(sortedDefs);
		}

		if (FilterFactory.getFilter() != null) {
			new SpecFilter().filter(swagger, FilterFactory.getFilter(), new HashMap<String, List<String>>(),
					new HashMap<String, String>(), new HashMap<String, List<String>>());
		}
	}

	private Sets.SetView<Class<?>> getValidClasses() {
		return Sets.union(apiSource.getValidClasses(Api.class), apiSource.getValidClasses(RouteResource.class));
	}

	private ClassSwaggerReader resolveApiReader() throws GenerateException {
		String customReaderClassName = apiSource.getSwaggerApiReader();
		if (customReaderClassName == null) {
			WplexApiReader reader = new WplexApiReader(swagger, LOG);
			reader.setTypesToSkip(this.typesToSkip);
			return reader;
		} else {
			return getCustomApiReader(customReaderClassName);
		}
	}

}
