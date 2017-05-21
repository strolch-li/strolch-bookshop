package li.strolch.bookshop.web;

import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.strolch.agent.api.StrolchAgent;
import li.strolch.agent.api.StrolchBootstrapper;

@WebListener
public class StartupListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);

	private StrolchAgent agent;

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		logger.info("Starting Bookshop...");
		try {
			// we load the configuration by reading the boot strap file:
			String boostrapFileName = "/WEB-INF/" + StrolchBootstrapper.FILE_BOOTSTRAP;
			InputStream bootstrapFile = sce.getServletContext().getResourceAsStream(boostrapFileName);
			StrolchBootstrapper bootstrapper = new StrolchBootstrapper(StartupListener.class);

			// now setup, initialize and start Strolch:
			this.agent = bootstrapper.setupByBoostrapFile(StartupListener.class, bootstrapFile);
			this.agent.initialize();
			this.agent.start();

		} catch (Exception e) {
			logger.error("Failed to start Bookshop due to: " + e.getMessage(), e);
			throw e;
		}

		logger.info("Started Bookshop.");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (this.agent != null) {
			this.agent.stop();
			this.agent.destroy();
		}
		logger.info("Destroyed Bookshop.");
	}
}
