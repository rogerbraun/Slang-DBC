/*
 * Erstellt: 29.08.2004
 */

package connection;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Volker Kl�bb
 */
class Connection {

   private Socket             socket;
   private ObjectOutputStream out;
   private ObjectInputStream  in;

   Connection(Socket socket) throws IOException {
      this.socket = socket;
      try {
         out = new ObjectOutputStream(socket.getOutputStream());
         in = new ObjectInputStream(socket.getInputStream());
      }
      catch (NullPointerException e) {
         System.err.println("Str�me konnten nicht ge�ffnet werden.");
      }
   }

   synchronized Message call(Message output)
         throws Exception {
      try {
         out.writeObject(output);
         out.flush();

         // warte bei der Close-Nachricht nicht auf eine Antwort
         if (output.getMethod().equals("close"))
            return null;
         // System.out.println(in.readObject());
         Message answer = (Message) in.readObject();
         if (answer.getMethod().equals("OK"))
        	return answer;
         if (answer.getMethod().equals("ERROR"))
            throw (Exception) answer.getArguments()[0];
      }
      catch (InvalidClassException e) {
         System.err.println("Versionskonflikt! Das DBC ist nicht mehr aktuell...");
         e.printStackTrace();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   void close() {
      try {
         call(new Message(DBC.key, "close"));
         out.close();
         in.close();
         socket.close();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
}