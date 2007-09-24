/*
 * Erstellt: 29.08.2004
 */

package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

import connection.Message;

/**
 * @author Volker Kl�bb
 */
public class Connection extends Thread {

   private boolean            run = true;
   private Socket             socket;
   private ObjectOutputStream out;
   private ObjectInputStream  in;
   private DBC_Server         dbc;

   Connection(Socket socket, DBC_Server dbc) throws IOException {
      this.socket = socket;
      this.dbc = dbc;
      try {
         out = new ObjectOutputStream(socket.getOutputStream());
         in = new ObjectInputStream(socket.getInputStream());
      }
      catch (NullPointerException e) {
         System.err.println("Str�me konnten nicht ge�ffnet werden.");
      }
   }

   public void run() {
      while (run) {
         try {
            Message input = (Message) in.readObject();
            System.out.println(socket.getInetAddress().getHostName()
                  + " => "
                  + input.getMethod());

            dbc.resetCounter();
            Message output = decode(input);

            if (output != null)
               out.writeObject(output);
            out.flush();
         }
         catch (Exception e) {
            e.printStackTrace();
            run = false;
         }

         try {
            Thread.sleep(500);
         }
         catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   private synchronized Message decode(Message input) {
      if (input.getMethod().equals("close")) {
         close();
         return null;
      }

      try {
         Method method = dbc.getClass().getMethod(input.getMethod(),
               input.getParameterTypes());
         Object res = method.invoke(dbc, input.getArguments());
         return new Message(DBC_Server.key, "OK", res);
      }
      catch (Exception e) {
         e.printStackTrace();
         return new Message(DBC_Server.key, "ERROR", e);
      }
   }

   public void close() {
      run = false;
      try {
         out.close();
         in.close();
         socket.close();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
   }

}