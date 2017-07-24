package li.strolch.bookshop.service;

import li.strolch.bookshop.BookShopConstants;
import li.strolch.model.Resource;
import li.strolch.persistence.api.RemoveResourceCommand;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.service.StringServiceArgument;
import li.strolch.service.api.AbstractService;
import li.strolch.service.api.ServiceResult;

public class RemoveBookService extends AbstractService<StringServiceArgument, ServiceResult> {

	private static final long serialVersionUID = 1L;

	@Override
	protected ServiceResult getResultInstance() {
		return new ServiceResult();
	}

	@Override
	public StringServiceArgument getArgumentInstance() {
		return new StringServiceArgument();
	}

	@Override
	protected ServiceResult internalDoService(StringServiceArgument arg) throws Exception {

		// open a new transaction, using the realm from the argument, or the certificate
		try (StrolchTransaction tx = openArgOrUserTx(arg)) {

			// get the existing book
			Resource book = tx.getResourceBy(BookShopConstants.TYPE_BOOK, arg.value, true);

			// add command to remove the resource
			RemoveResourceCommand cmd = new RemoveResourceCommand(getContainer(), tx);
			cmd.setResource(book);
			tx.addCommand(cmd);

			// notify the TX that it should commit on close
			tx.commitOnClose();
		}

		// and return the result
		return ServiceResult.success();
	}
}
