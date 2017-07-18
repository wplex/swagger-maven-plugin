package com.wplex.wservices;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.reader.WplexApiReader;
import com.wordnik.sample.TestVendorExtension;

import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.Swagger;

/**
 * The Vendor Extensions WPLEX Services Reader.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
public class VendorExtensionsWplexServicesReader extends WplexApiReader {

	public VendorExtensionsWplexServicesReader(Swagger swagger, Log log) {
		super(swagger, log);

		List<SwaggerExtension> extensions = new LinkedList<SwaggerExtension>(SwaggerExtensions.getExtensions());
		extensions.add(new TestVendorExtension());
		SwaggerExtensions.setExtensions(extensions);
	}

}
