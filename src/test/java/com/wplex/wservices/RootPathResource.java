package com.wplex.wservices;

import com.wplex.services.common.annotation.Route;
import com.wplex.services.common.annotation.RouteResource;
import com.wplex.services.common.resource.Request.Method;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * The root path resource.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
@Api(value = "/")
@RouteResource(path = "/")
public class RootPathResource {

	@ApiOperation(value = "testingRootPathResource")
	@Route(path = "", method = Method.GET)
	public String testingRootPathResource() {
		return new String("testingRootPathResource");
	}
}
