package com.wplex.wservices;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.wordnik.sample.JavaRestResourceUtil;
import com.wordnik.sample.data.StoreData;
import com.wordnik.sample.exception.NotFoundException;
import com.wordnik.sample.model.Order;
import com.wplex.services.common.annotation.PathParam;
import com.wplex.services.common.annotation.Route;
import com.wplex.services.common.annotation.RouteResource;
import com.wplex.services.common.resource.Request.Method;
import com.wplex.services.common.resource.Response.Status;
import com.wplex.services.common.resource.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Pet Store Resource.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
@Api(value = "/store")
@RouteResource(path = "/store")
public class PetStoreResource {

	private static StoreData storeData = new StoreData();
	private static JavaRestResourceUtil ru = new JavaRestResourceUtil();

	@ApiOperation(value = "Find purchase order by ID", notes = "For valid response try integer ID with value <= 5 or > 10. Other values will generated excetions.", response = Order.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
			@ApiResponse(code = 404, message = "Order not found") })
	@Route(path = "/order/{orderId}", method = Method.GET)
	public Order getOrderById(
			@ApiParam(value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]", required = true) @PathParam(key = "orderId") String orderId)
			throws NotFoundException {
		Order order = storeData.findOrderById(ru.getLong(0, 10000, 0, orderId));
		if (order != null) {
			return order;
		} else {
			throw new NotFoundException(404, "Order not found");
		}
	}

	@ApiOperation(value = "Find multiple purchase orders by Ids", notes = "For valid response try integer IDs with value <= 5 or > 10. Other values will generated exceptions", response = Order.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
			@ApiResponse(code = 404, message = "Order not found") })
	@Route(path = "/orders/{orderIds}", method = Method.GET)
	public List<Order> getOrdersById(
			@ApiParam(value = "IDs of pets that needs to be fetched", required = true) @PathParam(key = "orderIds") List<String> orderIds)
			throws NotFoundException {
		List<Order> orders = Lists.newArrayList();
		for (String orderId : orderIds) {
			Order order = storeData.findOrderById(ru.getLong(0, 10000, 0, orderId));
			if (order != null) {
				orders.add(order);
			} else {
				throw new NotFoundException(404, "Order #" + orderId + " not found");
			}
		}

		return orders;
	}

	@ApiOperation(value = "Place an order for a pet")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid Order") })
	@Route(path = "/order", method = Method.POST)
	public Order placeOrder(@ApiParam(value = "Order placed for purchasing the pet", required = true) Order order) {
		Order placeOrder = storeData.placeOrder(order);
		return placeOrder;
	}

	@ApiOperation(value = "Delete purchase order by ID", notes = "For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
			@ApiResponse(code = 404, message = "Order not found") })
	@Route(path = "/order/{orderId}", method = Method.DELETE)
	public Response deleteOrder(
			@ApiParam(value = "Id of the order that needs to be deleted", allowableValues = "range[1,infinity]", required = true) @PathParam(key = "orderId") String orderId) {
		storeData.deleteOrder(ru.getLong(0, 10000, 0, orderId));

		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

	@ApiOperation(value = "ping")
	@Route(path = "/ping", method = Method.GET)
	public String storePing() {
		return new String("Store pong");
	}
}
