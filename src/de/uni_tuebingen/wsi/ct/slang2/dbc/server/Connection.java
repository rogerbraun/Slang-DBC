/*
 * Erstellt: 29.08.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.Message;


/**
 * A connection object is created and run as a thread if the Slang2Server accepts an incoming connection on its listening socket.
 * This Class is the mediatior between the DBC_Client and the DBC_Server.
 * It also defines the communication protocol.
 * @author Volker Klöbb
 */
public class Connection implements Runnable {

   private static final int STATUS_WAIT_FOR_HELLO = 0;
   private static final int STATUS_CONNECTED = 1;
   private static final int STATUS_CLOSE = 2;
   
   private Integer         	  connection_status;
   private ObjectOutputStream out;
   private ObjectInputStream  in;  
   
   private Socket             socket;
   private DBC_Server         dbc_server;


   /**
    * @param socket
    * @param dbc
    * @throws IOException
    * @throws NullPointerException
    */
   Connection(Socket socket, DBC_Server dbc)
   throws IOException, NullPointerException {    
	   connection_status = STATUS_WAIT_FOR_HELLO;
	   if(socket != null && dbc != null ) {
    	  this.socket = socket;
		  this.dbc_server = dbc;
    	  try {    		  
    		  this.in = new ObjectInputStream(socket.getInputStream());
    	      this.out = new ObjectOutputStream(socket.getOutputStream());
    	  }
    	  catch( IOException e) {
    		  if(Slang2Server.logger != null)
              	Slang2Server.logger.severe("Initialisazion failed: "+e.getMessage());
    		  close();
    		  throw e;
    	  }
      }
      else
    	  throw new NullPointerException("Unable to initialize Connection: socket or dbc is `null'");
   }

   /* (non-Javadoc)
    * @see java.lang.Thread#run()
    */
   public void run() {
      while ( connection_status != STATUS_CLOSE ) {
         try {
            Message input = (Message) in.readObject();
            
            if(Slang2Server.logger != null)
            	Slang2Server.logger.info(socket.getInetAddress().getHostName() + " => " + input.getMethod());

            dbc_server.resetCounter();
            Message output = getAnswer(input);
            
            if(Slang2Server.logger != null)
            	Slang2Server.logger.info(socket.getInetAddress().getHostName() + " <= " + output.getMethod() + ": " + output.getArguments()[0]);

            if (output != null)
               out.writeObject(output);
            out.flush();
         }
         catch (Exception e) {
        	 System.err.println(e.getMessage());
        	 connection_status = STATUS_CLOSE;
         }
         if( connection_status != STATUS_CLOSE ) {
	         try {
	            Thread.sleep(500);
	         }
	         catch (Exception e) {
	        	 connection_status = STATUS_CLOSE;
	         }
         }
      }
      if(Slang2Server.logger != null)
      	Slang2Server.logger.info("Connection closed");
   }

   /**
    * Generate an answer message dependent on the status of this connection.
    * @param input A Message from a client
    * @return An answer message which has either method 'OK' and the object returned by the called DBC_server method named equal to <code>input.getMethod()</code> and accepting arguments equal to <code>input.getArguments()</code>,<br>
    * or method 'ERROR' and an Exception object as argument.
    */
   private synchronized Message getAnswer(Message input) {
	   if ( connection_status == STATUS_WAIT_FOR_HELLO ) {
		   if(input.getMethod().equals("hello")) {
			   connection_status = STATUS_CONNECTED;
		   }
		   else {
			   connection_status = STATUS_CLOSE;
			   return new Message(DBC_Server.key, "ERROR", new Exception("Client did not sent a 'hello' message: closing connection"));
		   }		   
	   }
	   if ( connection_status == STATUS_CONNECTED ) {
		   if(input.getMethod().equals("close")) {
			   connection_status = STATUS_CLOSE;			   
			   return null;
		   }
		   try {
			   Object o = dbc_server.getClass().getMethod(input.getMethod(),
					   input.getParameterTypes()).invoke(dbc_server, input.getArguments());
			   // if invoked method is a void
			   if(o == null)
				   o = new Object[0];
			   return new Message(DBC_Server.key, "OK", o);
		   }
		   // input.getMethod() is null or input.getArguments() contains a null value
		   catch (NullPointerException e ) {
			   // report that code at client must have a bug. Any other possibilities?
			   return new Message(DBC_Server.key, "ERROR", e);
		   }
		   // invoked method returned an exception
		   catch (InvocationTargetException e) {
			   // After an exception at the 'hello' message no further calls are accepted
			   if(input.getMethod().equals("hello")) {
				   connection_status = STATUS_CLOSE;
			   }
			   return new Message(DBC_Server.key, "ERROR", e.getCause());
		   }
		   catch (Exception e) {
			   if(Slang2Server.logger != null)
				   Slang2Server.logger.info(e.toString());
			   return new Message(DBC_Server.key, "ERROR", new Exception("Server Error"));
		   }
	   }
	   return new Message(DBC_Server.key, "ERROR", new Exception("Connection Status Failure"));
   }
   
   /**
    * Set the connection status to <code>STATUS_CLOSE</code> and try to close the opened Object-Streams.
    */
   synchronized void close() {
	   connection_status = STATUS_CLOSE;
	   try {
		   if(out != null)
			   out.close();
		   if(in != null)
			   in.close();
	   } catch (IOException e) {
		   if(Slang2Server.logger != null)
			   Slang2Server.logger.info("Failed closing Connection"+e.getMessage());
	   }
   }
}