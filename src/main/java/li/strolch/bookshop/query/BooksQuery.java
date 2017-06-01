package li.strolch.bookshop.query;

import li.strolch.bookshop.BookShopConstants;
import li.strolch.model.query.ResourceQuery;
import li.strolch.model.query.StrolchTypeNavigation;

public class BooksQuery<U> extends ResourceQuery<U> {

	public BooksQuery() {
		super(new StrolchTypeNavigation(BookShopConstants.TYPE_BOOK));
	}
}
