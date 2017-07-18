package com.github.kongchen.smp.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;

import io.swagger.jackson.SwaggerAnnotationIntrospector;

/**
 * The Wplex Maven Plugin for WPLEX Services.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
public class WplexMavenPluginTest extends AbstractMojoTestCase {

	private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui-wplex-services");
	private File docOutput = new File(getBasedir(), "generated/document-wplex-services.html");
	private ApiDocumentMojo mojo;

	@BeforeMethod
	protected void setUp() throws Exception {
		super.setUp();

		try {
			FileUtils.deleteDirectory(swaggerOutputDir);
			FileUtils.forceDelete(docOutput);
		} catch (Exception e) {
			// ignore
		}

		File testPom = new File(getBasedir(), "target/test-classes/plugin-config-wplex-services.xml");
		mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
	}

	@Test
	public void testGeneratedSwaggerSpecJson() {
		assertGeneratedSwaggerSpecJson("This is a sample JSON for wplex services.");
	}

	@Test
	public void testGeneratedSwaggerSpecYaml() {
		assertGeneratedSwaggerSpecYaml("This is a sample YAML for wplex services.");
	}

	@Test
	public void testSwaggerCustomReaderJson() {
		setCustomReader(mojo, "");
		assertGeneratedSwaggerSpecJson("Processed with CustomWplexReader");
	}

	@Test
	public void testSwaggerCustomReaderYaml() {
		setCustomReader(mojo, "");
		assertGeneratedSwaggerSpecYaml("Processed with CustomWplexReader");
	}

	@Test
	public void testInvalidCustomReaderJson() {
		String className = "";

		setCustomReader(mojo, className);
		testGeneratedSwaggerSpecJson();
	}

	@Test
	public void testInvalidCusomReaderYaml() {
		String className = "";

		setCustomReader(mojo, className);
		testGeneratedSwaggerSpecYaml();
	}

	@Test
	public void testGeneratedDoc() throws MojoExecutionException, MojoFailureException {
		mojo.execute();

		BufferedReader actualReader = null;
		BufferedReader expectReader = null;
		FileInputStream swaggerJson = null;
		BufferedReader swaggerReader = null;

		try {

		} finally {
			if (actualReader != null) {
				actualReader.close();
			}
			if (expectReader != null) {
				expectReader.close();
			}
			if (swaggerJson != null) {
				swaggerJson.close();
			}
			if (swaggerReader != null) {
				swaggerReader.close();
			}
		}

	}


	private void assertGeneratedSwaggerSpecJson(String string) {
		// TODO Auto-generated method stub

	}

	private void assertGeneratedSwaggerSpecYaml(String string) {
		// TODO Auto-generated method stub

	}

	private void setCustomReader(ApiDocumentMojo mojo2, String string) {
		// TODO Auto-generated method stub

	}

}
