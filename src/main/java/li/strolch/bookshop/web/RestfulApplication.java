package li.strolch.bookshop.web;

import jakarta.ws.rs.ApplicationPath;
import java.util.logging.Level;

import li.strolch.bookshop.rest.BooksResource;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.StrolchRestfulClasses;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationPath("rest")
public class RestfulApplication extends ResourceConfig {

	public RestfulApplication() {

		// add strolch resources
		registerClasses(StrolchRestfulClasses.getRestfulClasses());
		registerClasses(StrolchRestfulClasses.getProviderClasses());

		// add project resources by package name
		packages(BooksResource.class.getPackage().getName());

		RestfulStrolchComponent restfulComponent = RestfulStrolchComponent.getInstance();
		if (restfulComponent.isRestLogging()) {
			register(new LoggingFeature(java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
					Level.SEVERE, LoggingFeature.Verbosity.PAYLOAD_ANY, Integer.MAX_VALUE));

			property(ServerProperties.TRACING, "ALL");
			property(ServerProperties.TRACING_THRESHOLD, "TRACE");
		}
	}
}
