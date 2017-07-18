package com.wplex.swagger.docgen.mavenplugin;

import java.util.Collections;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.mavenplugin.WplexMavenDocumentSource;
import com.google.common.collect.Sets.SetView;
import com.wplex.services.common.annotation.RouteResource;


import io.swagger.annotations.Api;

/**
 * The Wplex Maven Document Source Test.
 *
 * @author Ryan Padilha <ryan.padilah@wplex.com.br>
 * @since 3.1.6
 *
 */
public class WplexMavenDocumentSourceTest {

	@Test
	public void testGetValidClasses() throws MojoFailureException {
		SystemStreamLog log = new SystemStreamLog();

		ApiSource apiSource = new ApiSource();
		apiSource.setLocations(Collections.singletonList(this.getClass().getPackage().getName()));
		apiSource.setSwaggerDirectory("./");

		WplexMavenDocumentSource wplexMavenDocumentSource = new WplexMavenDocumentSource(apiSource, log, "UTF-8");
		SetView<Class<?>> validClasses = wplexMavenDocumentSource.getValidClasses();

		Assert.assertEquals(validClasses.size(), 2);
		Assert.assertTrue(validClasses.contains(ExampleRouterController1.class));
		Assert.assertTrue(validClasses.contains(ExampleRouterController2.class));
	}

	@RouteResource(path = "/controller1")
	private static class ExampleRouterController1 {
	}

	@Api
	@RouteResource(path="/controller2")
	private static class ExampleRouterController2 {
	}
}
