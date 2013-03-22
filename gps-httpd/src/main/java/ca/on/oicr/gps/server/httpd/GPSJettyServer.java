package ca.on.oicr.gps.server.httpd;

import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPSJettyServer {

	private static final Logger log = LoggerFactory.getLogger(GPSJettyServer.class);

	private final Server jettyServer;

	public static void main(String[] args) throws Exception {
		GPSJettyServer server = new GPSJettyServer();
		server.start();
		server.stop();
	}

	public GPSJettyServer() throws Exception {
		
		String home = System.getProperty("GPS_HOME");
		
		URL configXMLURL = new URL("file", "", home + "/conf/gps.xml");
		log.info("Configuring web server from: " + configXMLURL);
		Resource configXML = new FileResource(configXMLURL);
	
	    XmlConfiguration configuration = new XmlConfiguration(configXML.getInputStream());
	    Server server = (Server)configuration.configure();
	    
	    this.jettyServer = server;
	}

	public boolean isRunning() {
		return this.jettyServer.isRunning();
	}

	public void start() {
		try {
			log.info("Starting GPS server on port {}",
					this.jettyServer.getConnectors()[0].getPort());
			this.jettyServer.start();
			this.jettyServer.join();
		} catch (Exception e) {
			log.error("Error starting Jetty", e);
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		try {
			this.jettyServer.stop();
		} catch (Exception e) {
			// log and ignore
			log.warn("Exception during GPS server shutdown", e);
		}

	}

}