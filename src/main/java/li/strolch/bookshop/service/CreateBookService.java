package li.strolch.bookshop.service;

import com.google.gson.JsonObject;

import li.strolch.bookshop.BookShopConstants;
import li.strolch.model.Resource;
import li.strolch.model.json.FromFlatJsonVisitor;
import li.strolch.model.json.StrolchElementToJsonVisitor;
import li.strolch.persistence.api.AddResourceCommand;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.rest.util.JsonServiceArgument;
import li.strolch.rest.util.JsonServiceResult;
import li.strolch.service.api.AbstractService;

public class CreateBookService extends AbstractService<JsonServiceArgument, JsonServiceResult> {

	private static final long serialVersionUID = 1L;

	@Override
	protected JsonServiceResult getResultInstance() {
		return new JsonServiceResult();
	}

	@Override
	public JsonServiceArgument getArgumentInstance() {
		return new JsonServiceArgument();
	}

	@Override
	protected JsonServiceResult internalDoService(JsonServiceArgument arg) throws Exception {

		// open a new transaction, using the realm from the argument, or the certificate
		Resource book;
		try (StrolchTransaction tx = openArgOrUserTx(arg)) {

			// get a new book "instance" from the template
			book = tx.getResourceTemplate(BookShopConstants.TYPE_BOOK);

			// map all values from the JSON object into the new book element
			new FromFlatJsonVisitor().visit(book, arg.jsonElement.getAsJsonObject());

			// add command to store the resource
			AddResourceCommand cmd = new AddResourceCommand(getContainer(), tx);
			cmd.setResource(book);
			tx.addCommand(cmd);

			// notify the TX that it should commit on close
			tx.commitOnClose();
		}

		// map the return value to JSON
		JsonObject result = book.accept(new StrolchElementToJsonVisitor().flat());

		// and return the result
		return new JsonServiceResult(result);
	}
}
