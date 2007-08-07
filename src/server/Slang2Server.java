/*
 * Erstellt: 04.05.2005
 */

package server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Volker Klöbb
 */
public class Slang2Server {

   private ServerSocket socket;
   private boolean      listen = true;
   private DBC_Server   dbc;

   public Slang2Server(int port, String dbServer) {
      try {
         socket = new ServerSocket(port);
         dbc = new DBC_Server(dbServer, 3000);
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
      Slang2Server server = new Slang2Server(9998, "127.0.0.1");
	  //Slang2Server server = new Slang2Server(9998, "hosea");
      server.listen();
   }
}
