package com.wplex.wservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;

import com.wordnik.sample.TestVendorExtension.TestVendorAnnotation;
import com.wordnik.sample.data.PetData;
import com.wordnik.sample.exception.NotFoundException;
import com.wordnik.sample.model.PaginationHelper;
import com.wordnik.sample.model.Pet;
import com.wordnik.springmvc.UpdatePetRequest;
import com.wplex.services.common.annotation.Body;
import com.wplex.services.common.annotation.PathParam;
import com.wplex.services.common.annotation.QueryParam;
import com.wplex.services.common.annotation.Route;
import com.wplex.services.common.annotation.RouteResource;
import com.wplex.services.common.resource.Request.Method;
import com.wplex.services.common.resource.Response;
import com.wplex.services.common.resource.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;

/**
 * The Pet Resource.
 *
 * @author Ryan Padilha <ryan.padilha@wplex.com.br>
 * @since 3.1.6
 *
 */
@Api(value = "/pet")
@RouteResource(path = "/pet")
public class PetResource {

	private static PetData petData = new PetData();

	@ApiOperation(value = "Find pet by ID", notes = "Returns a pet when ID < 10. ID > 10 or nonintegers will simulate API error conditions")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pet data found", response = Pet.class),
			@ApiResponse(code = 400, message = "Invalid ID supplied"),
			@ApiResponse(code = 404, message = "Pet not found") })
	@Route(path = "/{petId}", method = Method.GET)
	public Pet getPetById(
			@ApiParam(value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]", required = true) @PathParam(key = "petId") Long petId)
			throws NotFoundException {
		Pet pet = petData.getPetbyId(petId);

		if (pet != null) {
			return pet;
		} else {
			throw new NotFoundException(404, "Pet not found");
		}
	}

	@ApiOperation(value = "Deletes a pet", nickname = "removePet")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid pet value") })
	@Route(path = "/{petId}", method = Method.DELETE)
	public Response deletePet(
			@ApiParam(value = "Pet id to delete", required = true) @PathParam(key = "petId") @Size(min = 0, max = Integer.MAX_VALUE) Long petId) {
		petData.deletePet(petId);

		final Response response = new Response();
		response.setStatus(Status.OK);
		return response;
	}

	@ApiOperation(value = "Add a new pet to the store")
	@ApiResponses(value = { @ApiResponse(code = 405, message = "Invalid Input") })
	@Route(path = "", method = Method.POST)
	public Pet addPet(
			@ApiParam(value = "Pet object that needs to be added to the store", required = true) @Body Pet pet) {
		Pet updatedPet = petData.addPet(pet);
		return updatedPet;
	}

	@ApiOperation(value = "Add multiple pets to the store")
	@ApiResponses(value = { @ApiResponse(code = 405, message = "Invalid Input", response = List.class) })
	@Route(path = "/pets", method = Method.POST)
	public List<Pet> addMultiplePets(
			@ApiParam(value = "A list of pet objects that need to be added to the store", required = true) @Body Set<Pet> pets) {

		List<Pet> createdPets = new ArrayList<Pet>();
		for (Pet pet : pets) {
			createdPets.add(petData.addPet(pet));
		}

		return createdPets;
	}

	@ApiOperation(value = "Update an existing pet")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
			@ApiResponse(code = 404, message = "Pet not found"),
			@ApiResponse(code = 405, message = "Invalid exception") })
	@Route(path = "", method = Method.PUT)
	public Pet updatePet(
			@ApiParam(value = "Pet object that needs to be added to the store", required = true) @Body Pet pet) {
		Pet updatedPet = petData.addPet(pet);
		return updatedPet;
	}

	@ApiOperation(value = "Finds pets by status", notes = "Multiple status values can be provided with comma separated strings", response = Pet.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid status value") })
	@Route(path = "/findByStatus", method = Method.GET)
	public List<Pet> findPetsByStatus(
			@ApiParam(value = "Status values that need to be considered for filter", required = true, defaultValue = "available", allowableValues = "available,pending,sold", allowMultiple = true) @QueryParam(key = "status") String status) {
		List<Pet> pets = petData.findPetByStatus(status);
		return pets;
	}

	@ApiOperation(value = "Find pets by status", notes = "Multiple status values can be provided with multiple query parameters. Example: ?status=sold&status=pending", response = Pet.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Operation successful, and items were found matching the query. Data in response body."),
			@ApiResponse(code = 400, message = "Invalid status value") })
	@Route(path = "/findByStatuses", method = Method.GET)
	public List<Pet> findPetsByStatuses(
			@ApiParam(value = "Status values that need to be considered for filter", required = true, defaultValue = "available", allowableValues = "available,pending,sold") @QueryParam(key = "status") List<String> statuses) {
		List<Pet> pets = petData.findPetByStatus(StringUtils.join(statuses, ","));
		return pets;
	}

	@ApiOperation(value = "Finds pets by tags", notes = "Muliple tags can be provided with comma seperated strings. Use tags=tag1,tag2,tag3 for testing.", response = Pet.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid tag value") })
	@Route(path = "/findByTags", method = Method.GET)
	public List<Pet> findPetsByTags(
			@ApiParam(value = "Tags to filter by", required = true, allowMultiple = true) @QueryParam(key = "tags") String tags) {
		List<Pet> pets = petData.findPetByTags(tags);
		return pets;
	}

	@ApiOperation(value = "Retrieve all pets. Pagination supported", notes = "If you wish to paginate the results of this API, supply offset and limit query parameters.", response = Pet.class, responseContainer = "List")
	@Route(path = "/pets", method = Method.GET)
	public Map<String, Object> getAllPetsPaginated(PaginationHelper paginationHelper) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("limit", paginationHelper.getLimit());
		map.put("offset", paginationHelper.getOffset());

		// TODO: implement paginated getter for petData
		map.put("results", petData.findPetByStatus("available,sold,pending"));
		return map;
	}

	@ApiOperation(value = "Updates a pet in the store with form data")
	@ApiResponses(value = { @ApiResponse(code = 405, message = "Invalid input") })
	@Route(path = "/{petId}", method = Method.POST)
	public com.wordnik.sample.model.ApiResponse updatePetWithForm(UpdatePetRequest updatePetRequest) {
		System.out.println(updatePetRequest.getName());
		System.out.println(updatePetRequest.getStatus());
		return new com.wordnik.sample.model.ApiResponse(200, "SUCCESS");
	}

	@ApiOperation(value = "Returns pet", response = Pet.class)
	@Route(path = "", method = Method.GET)
	public Pet get() {
		return new Pet();
	}

	@ApiOperation(value = "Ping the service")
	@Route(path = "/petPing", method = Method.GET)
	public String petPing(
			@ApiParam(hidden = true, name = "thisShouldBeHidden") @QueryParam(key = "hidden") Map<String, String> hiddenParameter) {
		return new String("Pet pong");
	}

	@ApiOperation(value = "testExtensions", extensions = {
			@Extension(name = "firstExtension", properties = {
					@ExtensionProperty(name = "extensionName1", value = "extensionValue1"),
					@ExtensionProperty(name = "extensionName2", value = "extensionValue2") }),
			@Extension(properties = { @ExtensionProperty(name = "extensionName3", value = "extensionValue3") }) })
	@Route(path = "/test/extensions", method = Method.GET)
	public Pet testingExtensions() {
		return new Pet();
	}

	@ApiOperation(value = "testingHiddenApiOperation", hidden = true)
	@Route(path = "/testingHiddenApiOperation", method = Method.GET)
	public String testingHiddenApiOperation() {
		return new String("testingHiddenApiOperation");
	}

	@ApiOperation(value = "testing")
	@Route(path = "/testing", method = Method.GET)
	public Object testing(@ApiParam(name = "items") @QueryParam(key = "items") String[] items) {
		return new Object();
	}

	@ApiOperation(value = "testingApiImplicitParams")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "header-test-name", value = "header-test-value", required = true, dataType = "string", paramType = "header", defaultValue = "z"),
			@ApiImplicitParam(name = "path-test-name", value = "path-test-value", required = true, dataType = "string", paramType = "path", defaultValue = "path-test-defaultValue"),
			@ApiImplicitParam(name = "body-test-name", value = "body-test-value", required = true, dataType = "com.wordnik.sample.model.Pet", paramType = "body") })
	@Route(path = "/testingApiImplicitParams", method = Method.GET)
	public String testingApiImplicitParams() {
		return new String("testingImplicitParams");
	}

	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "form-test-name", value = "form-test-value", allowMultiple = true, required = true, dataType = "string", paramType = "form", defaultValue = "form-test-defaultValue") })
	@Route(path = "/testingFormApiImplicitParam", method = Method.GET)
	public String testingFormApiImplicitParam() {
		return new String("testingFormApiImplicitParam");
	}

	@ApiOperation(value = "testingBasicAuth", authorizations = @Authorization(value = "basicAuth"))
	@Route(path = "/testingBasicAuth", method = Method.GET)
	public String testingBasicAuth() {
		return new String("testingBasicAuth");
	}

	@ApiOperation(value = "testingVendorExtensions")
	@TestVendorAnnotation
	@Route(path = "/testingVendorExtensions", method = Method.GET)
	public String testingVendorExtensions() {
		return null;
	}

	@ApiOperation(value = "testingMergedAnnotations")
	@Route(path = "/testingMergedAnnotations", method = Method.GET)
	public String testingMergedAnnotations() {
		return new String("it works");
	}
}
