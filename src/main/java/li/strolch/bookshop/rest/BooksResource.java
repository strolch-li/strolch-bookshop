package li.strolch.bookshop.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import li.strolch.bookshop.BookShopConstants;
import li.strolch.bookshop.query.BooksQuery;
import li.strolch.bookshop.service.CreateBookService;
import li.strolch.bookshop.service.RemoveBookService;
import li.strolch.bookshop.service.UpdateBookService;
import li.strolch.model.Resource;
import li.strolch.model.json.StrolchRootElementToJsonVisitor;
import li.strolch.model.query.NameSelection;
import li.strolch.model.query.OrSelection;
import li.strolch.model.query.ParameterSelection;
import li.strolch.model.query.ResourceQuery;
import li.strolch.model.visitor.ResourceVisitor;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.privilege.model.Certificate;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.StrolchRestfulConstants;
import li.strolch.rest.helper.ResponseUtil;
import li.strolch.service.JsonServiceArgument;
import li.strolch.service.JsonServiceResult;
import li.strolch.service.StringServiceArgument;
import li.strolch.service.api.ServiceHandler;
import li.strolch.service.api.ServiceResult;
import li.strolch.utils.StringMatchMode;
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

		int offset = StringHelper.isNotEmpty(offsetS) ? Integer.valueOf(offsetS) : 0;
		int limit = StringHelper.isNotEmpty(limitS) ? Integer.valueOf(limitS) : 20;

		// open the TX with the certificate, using this class as context
		try (StrolchTransaction tx = RestfulStrolchComponent.getInstance().openTx(cert, getClass())) {

			// prepare the query
			ResourceQuery<Resource> query = new BooksQuery<>();

			// prepare selections
			if (StringHelper.isEmpty(queryS)) {
				query.withAny();
			} else {
				OrSelection or = new OrSelection();
				or.with(ParameterSelection
						.stringSelection(BookShopConstants.BAG_PARAMETERS, BookShopConstants.PARAM_DESCRIPTION, queryS,
								StringMatchMode.ci()));
				or.with(new NameSelection(queryS, StringMatchMode.ci()));

				// add selections
				query.with(or);
			}

			// perform the query
			List<Resource> books = tx.doQuery(query);

			// perform paging
			Paging<Resource> paging = Paging.asPage(books, offset, limit);
			List<Resource> page = paging.getPage();

			// return result, transforming to JSON
			ResourceVisitor<JsonObject> visitor = new StrolchRootElementToJsonVisitor().flat().asResourceVisitor();
			return ResponseUtil.listToResponse(StrolchRestfulConstants.DATA, page, a -> a.accept(visitor));
		}
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
		JsonObject jsonData = new JsonParser().parse(data).getAsJsonObject();

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
		JsonObject jsonData = new JsonParser().parse(data).getAsJsonObject();

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
	public Response update(@Context HttpServletRequest request, @PathParam("id") String id) {

		// this is an authenticated method call, thus we can get the certificate from the request:
		Certificate cert = (Certificate) request.getAttribute(StrolchRestfulConstants.STROLCH_CERTIFICATE);

		// instantiate the service with the argument
		RemoveBookService svc = new RemoveBookService();
		StringServiceArgument arg = svc.getArgumentInstance();
		arg.value = id;

		// perform the service
		ServiceHandler serviceHandler = RestfulStrolchComponent.getInstance().getServiceHandler();
		ServiceResult result = serviceHandler.doService(cert, svc, arg);

		// return depending on the result state
		return ResponseUtil.toResponse(result);
	}
}
