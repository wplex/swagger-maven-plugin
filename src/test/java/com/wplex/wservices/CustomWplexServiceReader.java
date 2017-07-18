package com.wplex.wservices;

import java.util.Set;

import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.GenerateException;

import io.swagger.models.Swagger;

/**
 * The Custom Wplex Services Reader.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
public class CustomWplexServiceReader extends VendorExtensionsWplexServicesReader {

	public CustomWplexServiceReader(Swagger swagger, Log log) {
		super(swagger, log);
	}

	@Override
	public Swagger read(Set<Class<?>> classes) throws GenerateException {
		Swagger swagger = super.read(classes);
		swagger.getInfo().setDescription("Processed with CustomWplexServiceReader");
		return swagger;
	}

}
