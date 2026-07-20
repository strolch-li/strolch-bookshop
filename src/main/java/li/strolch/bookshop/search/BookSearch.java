package li.strolch.bookshop.search;

import static li.strolch.bookshop.BookShopConstants.*;

import li.strolch.search.ResourceSearch;
import li.strolch.utils.helper.StringHelper;

public class BookSearch extends ResourceSearch {

	public BookSearch() {
		types(TYPE_BOOK);
	}

	public BookSearch stringQuery(String value) {
		if (StringHelper.isEmpty(value))
			return this;

		// split by spaces
		value = value.trim();
		String[] values = value.split(" ");

		// add where clauses for id, name and description
		where(id()
				.containsIgnoreCase(values)
				.or(name().containsIgnoreCase(values))
				.or(param(BAG_PARAMETERS, PARAM_DESCRIPTION).containsIgnoreCase(values)));

		return this;
	}
}
