/*
 * Erstellt: 04.05.2005
 */

package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * @author Volker Klöbb
 */
public class Slang2Server {

   private ServerSocket socket;
   private boolean      listen = true;
   private DBC_Server   dbc;
   private Properties	properties;

   public Slang2Server() {
	   initProperties(new Properties());
	   init();
      
   }

   public Slang2Server(Properties properties) {
	   initProperties(properties);
	   init();
   }
   
   private void initProperties(Properties p) {
	   this.properties = new Properties();
	   this.properties.setProperty("slang2server.host", "localhost");
	   this.properties.setProperty("slang2server.port", "9998");
	   this.properties.setProperty("slang2server.dbc.host", "localhost");
	   this.properties.setProperty("slang2server.dbc.port", "3306");
	   this.properties.setProperty("slang2server.dbc.name", "slang2_v2");
	   this.properties.setProperty("slang2server.dbc.user", "slang2");
	   this.properties.setProperty("slang2server.dbc.password", "kauderwelch");
	   this.properties.putAll(p);
   }
   
   private void init() {
	   
	   try {
	         socket = new ServerSocket(Integer.parseInt(this.properties.getProperty("slang2server.port")));
	         dbc = new DBC_Server(
	        		 this.properties.getProperty("slang2server.dbc.host"),
	        		 Integer.parseInt(this.properties.getProperty("slang2server.dbc.port")),
	        		 this.properties.getProperty("slang2server.dbc.name"),
	        		 this.properties.getProperty("slang2server.dbc.user"),
	        		 this.properties.getProperty("slang2server.dbc.password"));
	         System.out.println("SLANG2-Server gestartet...");
	         dbc.start();
	      }
	      catch (Exception e) {
	         e.printStackTrace();
	         System.exit(0);
	      }
   }

public void listen() {
      while (listen) {
         try {
            Socket s = socket.accept();
            Connection c = new Connection(s, dbc);
            c.start();
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public static void main(String[] args) {
	   /*
	    * Load Properties from file if exists
	    */
	   Properties properties = new Properties();
	   try {
			properties.load(new FileInputStream("slang2server.properties"));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {			
		}
	   
      Slang2Server server = new Slang2Server(properties);
      server.listen();
   }
}
