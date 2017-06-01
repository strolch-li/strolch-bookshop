package li.strolch.bookshop.web;

import java.util.logging.Level;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Priorities;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import li.strolch.bookshop.rest.BooksResource;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.StrolchRestfulExceptionMapper;
import li.strolch.rest.endpoint.AuthenticationService;
import li.strolch.rest.filters.AccessControlResponseFilter;
import li.strolch.rest.filters.AuthenticationRequestFilter;
import li.strolch.rest.filters.AuthenticationResponseFilter;
import li.strolch.rest.filters.CharsetResponseFilter;
import li.strolch.rest.filters.HttpCacheResponseFilter;

@ApplicationPath("rest")
public class RestfulApplication extends ResourceConfig {

	public RestfulApplication() {

		// add strolch resources
		register(AuthenticationService.class);

		// add project resources by package name
		packages(BooksResource.class.getPackage().getName());

		// filters
		register(AuthenticationRequestFilter.class, Priorities.AUTHENTICATION);
		register(AccessControlResponseFilter.class);
		register(AuthenticationResponseFilter.class);
		register(HttpCacheResponseFilter.class);

		// log exceptions and return them as plain text to the caller
		register(StrolchRestfulExceptionMapper.class);

		// the JSON generated is in UTF-8
		register(CharsetResponseFilter.class);

		RestfulStrolchComponent restfulComponent = RestfulStrolchComponent.getInstance();
		if (restfulComponent.isRestLogging()) {
			register(new LoggingFeature(java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
					Level.SEVERE, LoggingFeature.Verbosity.PAYLOAD_ANY, Integer.MAX_VALUE));

			property(ServerProperties.TRACING, "ALL");
			property(ServerProperties.TRACING_THRESHOLD, "TRACE");
		}
	}
}
