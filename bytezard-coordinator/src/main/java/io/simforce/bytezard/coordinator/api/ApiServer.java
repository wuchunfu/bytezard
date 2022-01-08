package io.simforce.bytezard.coordinator.api;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;

/**
 * @author zixi0825
 */
public class ApiServer {

    private final Server server;

    public ApiServer(CoordinatorConfiguration configuration) {
        server = new Server(
                configuration.getInt(CoordinatorConfiguration.SERVLET_PORT,
                        CoordinatorConfiguration.SERVLET_PORT_DEFAULT));
        ServletHolder servlet = new ServletHolder(ServletContainer.class);
        servlet.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        servlet.setInitParameter("com.sun.jersey.config.property.packages", "io.simforce.bytezard.coordinator.api.rest");
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(servlet, "/*");
        server.setHandler(handler);
    }

    public void start() throws Exception {
        if (server!= null) {
            server.start();
        }
    }

    public void stop() throws Exception {
        if (server!= null) {
            server.stop();
        }
    }
}
