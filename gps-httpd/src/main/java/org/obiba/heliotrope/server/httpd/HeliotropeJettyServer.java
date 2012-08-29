/*******************************************************************************
 * Copyright 2011(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.heliotrope.server.httpd;

import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeliotropeJettyServer {

	private static final Logger log = LoggerFactory.getLogger(HeliotropeJettyServer.class);

	private final Server jettyServer;

	public static void main(String[] args) throws Exception {
		HeliotropeJettyServer server = new HeliotropeJettyServer();
		server.start();
		server.stop();
	}

	public HeliotropeJettyServer() throws Exception {
		
		String home = System.getProperty("HELIOTROPE_HOME");
		
		URL configXMLURL = new URL("file", "", home + "/conf/heliotrope.xml");
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
			log.info("Starting Heliotrope server on port {}",
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
			log.warn("Exception during Heliotrope server shutdown", e);
		}

	}

}