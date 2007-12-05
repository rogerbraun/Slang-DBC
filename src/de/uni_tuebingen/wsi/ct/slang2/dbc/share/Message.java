/*
 * Erstellt: 29.08.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.share;

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


   public Message(DBC_Key key, String method, Object ... arguments) throws NullPointerException
   {
	   if(key == null || method == null)
		   throw new NullPointerException("key or method is null");
	   key.unlock();
	   this.method = method;
	   this.arguments = arguments;
	   if(arguments != null) {
		   for (Object object : arguments) {
			   if( object == null)
				   /*
				    *  getParameterTypes() will treat a null's type as null
				    *  Method.invoke() will be unable to find a method with any ParameterType == null
				    */
				   throw new NullPointerException("At least one of arguments elements is null");
		   }
	   }
   }

   /**
    * @return the method
    */
   public String getMethod() {
	   return method;
   }

   /**
    * @return the arguments
    */
   public Object[] getArguments() {
	   return arguments;
   }

   /**
    * @return the classes of the arguments. If an argument is <code>null</code>, it's class is <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public Class[] getParameterTypes()
   {
	   Class[] types = null;
	   if(arguments != null) {
		   types = new Class[arguments.length];
		   for (int i = 0; i < types.length; ++i) {
			   types[i] = (arguments[i] == null) ? null : arguments[i].getClass();
		   }
	   }
	   return types;
   }

   public String toString() {
      StringBuffer res = new StringBuffer(method);
      res.append("(");
      for (Object o : arguments) {
         res.append(o);
         res.append(", ");
      }
      res.delete(res.length()-2, res.length());
      res.append(")");
      return res.toString();
   }

}