/*
 * Erstellt: 29.08.2004
 */

package connection;

import java.io.Serializable;

/**
 * Eine Nachricht, die zur Kommunikation zwischen Client und Server verwendet
 * wird
 * 
 * @author Volker Klöbb
 */
public class Message
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 7100867585648061128L;
   private String            method;
   private Object[]          arguments;
   private int               nextIndex;

   public Message(DBC_Key key, String method, int args) {
      key.unlock();
      this.method = method;
      arguments = new Object[args];
      nextIndex = 0;
   }

   public Message(DBC_Key key, String method) {
      this(key, method, 0);
   }

   public Message(DBC_Key key, String method, Object arg1) {
      this(key, method, 1);
      arguments[0] = arg1;
   }

   public Message(DBC_Key key, String method, Object arg1, Object arg2) {
      this(key, method, 2);
      arguments[0] = arg1;
      arguments[1] = arg2;
   }

   public Message(DBC_Key key,
         String method,
         Object arg1,
         Object arg2,
         Object arg3) {
      this(key, method, 3);
      arguments[0] = arg1;
      arguments[1] = arg2;
      arguments[2] = arg3;
   }

   public void setNextArgument(Object argument) {
      arguments[nextIndex++] = argument;
   }

   public void setArgument(Object argument, int index) {
      arguments[index] = argument;
   }

   public String getMethod() {
      return method;
   }

   public Object[] getArguments() {
      return arguments;
   }

   public Class[] getParameterTypes() {
      Class[] types = new Class[arguments.length];
      for (int i = 0; i < types.length; i++)
         types[i] = arguments[i].getClass();
      return types;
   }

   public String toString() {
      StringBuffer res = new StringBuffer(method);
      res.append("(");
      for (int i = 0; i < arguments.length; i++) {
         res.append(arguments[i]);
         if (i < arguments.length - 1)
            res.append(", ");
      }
      res.append(")");
      return res.toString();
   }
}