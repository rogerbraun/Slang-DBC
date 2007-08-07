/*
 * Erstellt: 29.08.2004
 */

package server;

import java.io.Serializable;

/**
 * @author Volker Klöbb
 */
public class Message
      implements
         Serializable {

   private static final long serialVersionUID = -9116827981984262529L;
   private String            method;
   private Object[]          arguments;
   private int               nextIndex;
   private int               key;

   public Message(String method, int args) {
      this.method = method;
      arguments = new Object[args];
      nextIndex = 0;
      key = (int) (method.hashCode() * System.currentTimeMillis());
   }

   public Message(String method) {
      this(method, 0);
   }

   public Message(String method, Object arg1) {
      this(method, 1);
      arguments[0] = arg1;
   }

   public Message(String method, Object arg1, Object arg2) {
      this(method, 2);
      arguments[0] = arg1;
      arguments[1] = arg2;
   }

   public Message(String method, Object arg1, Object arg2, Object arg3) {
      this(method, 3);
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

   public int getKey() {
      return key;
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