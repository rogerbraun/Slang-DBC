/*
 * Erstellt: 04.05.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import de.uni_tuebingen.wsi.ct.slang2.dbc.client.DBC;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;

/**
 * The Slang2Server listens on a socket for incoming connections from a {@link DBC} Client and delegates it to a created {@link Connection} Thread
 * @see DBC
 * @see Connection
 */
public class Slang2Server implements Daemon, Runnable {

	private ServerSocket socket;	// the listening socket
	private DBC_Server   dbc_server;	// the DBC_Server instance
	private Properties	properties;
	static Logger logger;
	private ExecutorService conn_thread_pool;	// the tread pool for Connections
	private ExecutorService server_executor;	// Slang2Server thread executor; only used if server is started using commons-daemon
	private Vector<Connection> connections;	// to keep track of opened connections

	public Slang2Server() {    
	}

	public Slang2Server(Properties properties) {
		init(properties);
	}

	private void init(Properties p) {
		this.properties = new Properties();
		this.properties.setProperty("slang2server.host", "localhost");
		this.properties.setProperty("slang2server.port", "9998");
		this.properties.setProperty("slang2server.max_connections", "10");
		this.properties.setProperty("slang2server.dbc.host", "localhost");
		this.properties.setProperty("slang2server.dbc.port", "3306");
		this.properties.setProperty("slang2server.dbc.name", "slang2_v2");
		this.properties.setProperty("slang2server.dbc.user", "slang2");
		this.properties.setProperty("slang2server.dbc.password", "kauderwelch");
		this.properties.setProperty("slang2server.log.file", "");
		this.properties.setProperty("slang2server.log.level", "ALL");
		this.properties.putAll(p);

		server_executor = Executors.newSingleThreadExecutor();
		conn_thread_pool = Executors.newFixedThreadPool( Integer.parseInt(this.properties.getProperty("slang2server.max_connections")));

		connections = new Vector<Connection>();
		logger = Logger.getLogger(this.getClass().getName());
		Handler handler = null;
		if(this.properties.getProperty("slang2server.log.file").length() == 0)
			handler = new ConsoleHandler();
		else {
			try {
				handler = new FileHandler(this.properties.getProperty("slang2server.log.file"));
			} catch (Exception e) {
				handler = new ConsoleHandler();
			}
		}
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
		logger.setLevel(Level.parse(this.properties.getProperty("slang2server.log.level")));
		logger.info("Starting DBC-Server...");
		try {
			dbc_server = new DBC_Server(
					this.properties.getProperty("slang2server.dbc.host"),
					Integer.parseInt(this.properties.getProperty("slang2server.dbc.port")),
					this.properties.getProperty("slang2server.dbc.name"),
					this.properties.getProperty("slang2server.dbc.user"),
					this.properties.getProperty("slang2server.dbc.password"));
		} catch (DBC_ConnectionException e) {
			logger.severe("Could not start DBC-Server: "+e.getMessage());
			System.exit(1);
		}    
		Executors.newSingleThreadExecutor().execute(dbc_server);
	}

	public void listen() throws IOException {
		int iPortNumber = Integer.parseInt(this.properties.getProperty("slang2server.port"));
		try {
			logger.info( "Open the listening socket at: " + iPortNumber );
			socket = new ServerSocket(iPortNumber);
		} catch (IOException e1) {
			logger.severe("Could not listen on port: " + iPortNumber );
			System.exit(1);
		}

		try {
			// run until stopped by InterruptedIOException
			for (;;) {
				// listen
				Connection connection = new Connection(socket.accept(), dbc_server);
				
				logger.fine("New connection attempt");
				cleanup_connections();
				
				// start a new connection thread
				try {
					conn_thread_pool.execute(connection);
					connections.add(connection);
					logger.fine("Connection accepted");
				}
				catch(RejectedExecutionException e) {
					logger.warning("Connection rejected: "+e.getMessage());
				}
				
			} 
		}
		catch (InterruptedIOException iie) {
			logger.info("Server interrupted: Initializing shutdown.");
			conn_thread_pool.shutdown();
			try {
				conn_thread_pool.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// Ignore
			}
			conn_thread_pool.shutdownNow();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			listen();
		} catch (IOException e) {
			// Ignore
		}
	}


	/**
	 * Wipe all connections that are <code>null</code>
	 */
	private void cleanup_connections() {
		if(connections.remove(null))
			cleanup_connections();
	}

	public static void main(String[] args) throws IOException {
		/*
		 * Try loading Server-Properties from file
		 */
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("slang2server.properties"));
		} catch (Exception e) {
			System.err.println("Failed loading Properties (Using default values): "+e.getMessage());
		}

		new Slang2Server(properties).listen();
	}
	
	
	/**********************************************************************************************
	 * The following methods are only used if the server is controlled by apache's commons-daemon *
	 * Read http://commons.apache.org/daemon/ for explanation                                     *
	 **********************************************************************************************/
	

	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#destroy()
	 */
	public void destroy() {
		this.connections.clear();
		this.connections = null;
		this.dbc_server = null;
		this.properties.clear();
		this.properties = null;
		this.socket = null;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#init(org.apache.commons.daemon.DaemonContext)
	 */
	public void init(DaemonContext arg0) throws Exception {	   
		/*
		 * Try loading Server-Properties from file
		 */
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("slang2server.properties"));
		} catch (Exception e) {
			System.err.println("Failed loading Properties (Using default values): "+e.getMessage());
		}
		this.init(properties);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#start()
	 */
	public void start() throws Exception {
		server_executor.execute(this);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#stop()
	 */
	public void stop() throws Exception {		
		logger.fine("Closing active connections");
		cleanup_connections();
		for (Connection c : connections) {
			c.close();
		}
		
		logger.fine("Shuting down server");
		server_executor.shutdownNow();
		
		logger.fine("Closing socket");
		if(socket != null)
			socket.close();
		
		logger.fine("Closing DBC_Server");
		if(dbc_server != null)
			dbc_server.close();
	}
}
