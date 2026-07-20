package li.strolch.bookshop.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import li.strolch.bookshop.BookShopConstants;
import li.strolch.bookshop.search.BookSearch;
import li.strolch.bookshop.service.CreateBookService;
import li.strolch.bookshop.service.RemoveBookService;
import li.strolch.bookshop.service.UpdateBookService;
import li.strolch.model.Resource;
import li.strolch.model.json.StrolchRootElementToJsonVisitor;
import li.strolch.model.visitor.ResourceVisitor;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.privilege.model.Certificate;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.StrolchRestfulConstants;
import li.strolch.rest.helper.ResponseUtil;
import li.strolch.service.JsonServiceArgument;
import li.strolch.service.JsonServiceResult;
import li.strolch.service.StringArgument;
import li.strolch.service.api.ServiceHandler;
import li.strolch.service.api.ServiceResult;
import li.strolch.utils.collections.Paging;
import li.strolch.utils.helper.StringHelper;

@Path("books")
public class BooksResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response query(@Context HttpServletRequest request, @QueryParam("query") String queryS,
			@QueryParam("offset") String offsetS, @QueryParam("limit") String limitS) {

		// this is an authenticated method call, thus we can get the certificate from the request:
		Certificate cert = (Certificate) request.getAttribute(StrolchRestfulConstants.STROLCH_CERTIFICATE);

		int offset = StringHelper.isNotEmpty(offsetS) ? Integer.parseInt(offsetS) : 0;
		int limit = StringHelper.isNotEmpty(limitS) ? Integer.parseInt(limitS) : 20;

		// open the TX with the certificate, using this class as context
		Paging<Resource> paging;
		try (StrolchTransaction tx = RestfulStrolchComponent.getInstance().openTx(cert, getClass())) {

			// get the total count of books (before filtering)
			long dataSetSize = tx.getResourceCount(BookShopConstants.TYPE_BOOK);

			// perform a book search
			paging = new BookSearch()
					.stringQuery(queryS)
					.search(tx)
					.orderByName(false)
					.toPaging(offset, limit, dataSetSize);
		}

		ResourceVisitor<JsonObject> visitor = new StrolchRootElementToJsonVisitor().flat().asResourceVisitor();
		return ResponseUtil.toResponse(paging, e -> e.accept(visitor));
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpServletRequest request, @PathParam("id") String id) {

		// this is an authenticated method call, thus we can get the certificate from the request:
		Certificate cert = (Certificate) request.getAttribute(StrolchRestfulConstants.STROLCH_CERTIFICATE);

		// open the TX with the certificate, using this class as context
		try (StrolchTransaction tx = RestfulStrolchComponent.getInstance().openTx(cert, getClass())) {

			// get the book
			Resource book = tx.getResourceBy(BookShopConstants.TYPE_BOOK, id);
			if (book == null)
				return ResponseUtil.toResponse(Status.NOT_FOUND, "Book " + id + " does not exist!");

			// transform to JSON
			JsonObject bookJ = book.accept(new StrolchRootElementToJsonVisitor().flat());

			// return
			return ResponseUtil.toResponse(StrolchRestfulConstants.DATA, bookJ);
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@Context HttpServletRequest request, String data) {

		// this is an authenticated method call, thus we can get the certificate from the request:
		Certificate cert = (Certificate) request.getAttribute(StrolchRestfulConstants.STROLCH_CERTIFICATE);

		// parse data to JSON
		JsonObject jsonData = JsonParser.parseString(data).getAsJsonObject();

		// instantiate the service with the argument
		CreateBookService svc = new CreateBookService();
		JsonServiceArgument arg = svc.getArgumentInstance();
		arg.jsonElement = jsonData;

		// perform the service
		ServiceHandler serviceHandler = RestfulStrolchComponent.getInstance().getServiceHandler();
		JsonServiceResult result = serviceHandler.doService(cert, svc, arg);

		// return depending on the result state
		if (result.isOk())
			return ResponseUtil.toResponse(StrolchRestfulConstants.DATA, result.getResult());
		return ResponseUtil.toResponse(result);
	}

	@PUT
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@Context HttpServletRequest request, @PathParam("id") String id, String data) {

		// this is an authenticated method call, thus we can get the certificate from the request:
		Certificate cert = (Certificate) request.getAttribute(StrolchRestfulConstants.STROLCH_CERTIFICATE);

		// parse data to JSON
		JsonObject jsonData = JsonParser.parseString(data).getAsJsonObject();

		// instantiate the service with the argument
		UpdateBookService svc = new UpdateBookService();
		JsonServiceArgument arg = svc.getArgumentInstance();
		arg.objectId = id;
		arg.jsonElement = jsonData;

		// perform the service
		ServiceHandler serviceHandler = RestfulStrolchComponent.getInstance().getServiceHandler();
		JsonServiceResult result = serviceHandler.doService(cert, svc, arg);

		// return depending on the result state
		if (result.isOk())
			return ResponseUtil.toResponse(StrolchRestfulConstants.DATA, result.getResult());
		return ResponseUtil.toResponse(result);
	}

	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response remove(@Context HttpServletRequest request, @PathParam("id") String id) {

		// this is an authenticated method call, thus we can get the certificate from the request:
		Certificate cert = (Certificate) request.getAttribute(StrolchRestfulConstants.STROLCH_CERTIFICATE);

		// instantiate the service with the argument
		RemoveBookService svc = new RemoveBookService();
		StringArgument arg = svc.getArgumentInstance();
		arg.value = id;

		// perform the service
		ServiceHandler serviceHandler = RestfulStrolchComponent.getInstance().getServiceHandler();
		ServiceResult result = serviceHandler.doService(cert, svc, arg);

		// return depending on the result state
		return ResponseUtil.toResponse(result);
	}
}
