package com.wplex.wservices;

import java.util.List;

import com.wordnik.sample.data.UserData;
import com.wordnik.sample.exception.ApiException;
import com.wordnik.sample.exception.NotFoundException;
import com.wordnik.sample.model.User;
import com.wplex.services.common.annotation.PathParam;
import com.wplex.services.common.annotation.QueryParam;
import com.wplex.services.common.annotation.Route;
import com.wplex.services.common.annotation.RouteResource;
import com.wplex.services.common.resource.Request.Method;
import com.wplex.services.common.resource.Response;
import com.wplex.services.common.resource.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The User Resource class for Wplex Services.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
@Api(value = "/user")
@RouteResource(path = "/user")
public class UserResource {

	private static UserData userData = new UserData();

	@ApiOperation(value = "Create user", notes = "This can only be done by the logged in user.")
	@Route(path = "", method = Method.POST)
	public Response createUser(@ApiParam(value = "Created user object", required = true) User user,
			String arbitraryString) {

		userData.addUser(user);

		// TODO we can have a response's constructor with status param
		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

	@ApiOperation(value = "Create list of users with given input array")
	@Route(path = "/createWithArray", method = Method.POST)
	public Response createUsersWithArrayInput(@ApiParam(value = "List of user object", required = true) User[] users) {
		for (User user : users) {
			userData.addUser(user);
		}

		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

	@ApiOperation(value = "Create list of users with given input array")
	@Route(path = "/createWithList", method = Method.POST)
	public Response createUsersWithListInput(
			@ApiParam(value = "List of user object", required = true) List<User> users) {
		for (User user : users) {
			userData.addUser(user);
		}

		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

	// TODO Route method is an unique object, must be array
	@ApiOperation(value = "Updated user", notes = "This can only be done by the logged in user.")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid username supplied"),
			@ApiResponse(code = 404, message = "User not found") })
	@Route(path = "/{username}", method = Method.PUT)
	public Response updateUser(
			@ApiParam(value = "name that need to be deleted", required = true) @PathParam(key = "username") String username,
			@ApiParam(value = "Updated user object", required = true) User user) {
		userData.addUser(user);

		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

	@ApiOperation(value = "Delete user", notes = "This can only be done by the logged in user.")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid username supplied"),
			@ApiResponse(code = 404, message = "User not found") })
	@Route(path = "/{username}", method = Method.DELETE)
	public Response deleteUser(
			@ApiParam(value = "The name that needs to be deleted", required = true) @PathParam(key = "username") String username) {
		userData.removeUser(username);

		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

	@ApiOperation(value = "Get user by username", response = User.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid username supplied"),
			@ApiResponse(code = 404, message = "User not found") })
	@Route(path = "/{username}", method = Method.GET)
	public User getUserByName(
			@ApiParam(value = "The name that needs to be fetched. Use user1 for testing.", required = true) @PathParam(key = "username") String username)
			throws ApiException {
		User user = userData.findUserByName(username);

		if (user != null) {
			return user;
		} else {
			throw new NotFoundException(404, "User not found");
		}
	}

	@ApiOperation(value = "Login user into the system", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid username/password supplied") })
	@Route(path = "/login", method = Method.GET)
	public String loginUser(
			@ApiParam(value = "The username for login", required = true) @QueryParam(key = "username") String username,
			@ApiParam(value = "The password for login in clear text", required = true) @QueryParam(key = "password") String password) {

		return new String("logged in user session: " + System.currentTimeMillis());
	}

	@ApiOperation(value = "Logout current logged in user session")
	@Route(path = "/logout", method = Method.GET)
	public Response logoutUser() {
		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

}
