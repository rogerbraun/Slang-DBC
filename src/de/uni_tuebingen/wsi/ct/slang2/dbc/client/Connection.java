/*
 * Erstellt: 29.08.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.Message;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;

/**
 * @author Volker Klöbb
 */
class Connection {

   private ObjectOutputStream out;
   private ObjectInputStream  in;

   Connection(Socket socket) throws IOException {
      if(socket != null) {
    	  try {
    	      this.out = new ObjectOutputStream(socket.getOutputStream());
    	      this.in = new ObjectInputStream(socket.getInputStream());
    	  }
    	  catch( IOException e) {
    		  close();
    		  throw e;
    	  }
      }
      else
    	  throw new IOException("Socket is null");
   }

   synchronized Message call(Message output) throws Exception 
   {
	   if(in != null && out != null && output != null) {

		   try {
			   out.writeObject(output);
			   out.flush();
		   } catch (IOException e1) {
			   throw new DBC_ConnectionException("Could not write to ostream");
		   }

		   // warte bei der Close-Nachricht nicht auf eine Antwort
		   if (output.getMethod().equals("close"))
			   return null;

		   try {
			   Object answer_object = in.readObject();
			   if(answer_object != null && answer_object instanceof Message) {
				   Message answer = (Message) answer_object;
				   if (answer.getMethod().equals("OK"))
					   return answer;
				   if (answer.getMethod().equals("ERROR")
						   && answer.getArguments().length > 0
						   && answer.getArguments()[0] instanceof Exception)
					   throw (Exception) answer.getArguments()[0];
			   }
			   else
				   throw new DBC_ConnectionException("");
		   } catch (IOException e) {
			   e.printStackTrace();
			   throw new DBC_ConnectionException("Could not read from istream: "+e.getMessage());
		   } catch (ClassNotFoundException e) {
			   throw new DBC_ConnectionException("Returned message is not usable. This a server error!");
		   }
		   
	   }
	   return null;
   }

   void close() throws IOException {
	   try {
		   call(new Message(DBC.key, "close"));
	   } catch (IOException e) {
		   throw e;
	   }
	   catch (Exception e1) {
		   // Ignore
	   }
	   if(out != null)
		   out.close();
	   if(in != null)
		   in.close();
   }
}