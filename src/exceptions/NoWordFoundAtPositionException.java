/*
 * Erstellt: 17.10.2005
 */

package exceptions;

public class NoWordFoundAtPositionException extends Exception {

   /**
    * 
    */
   private static final long serialVersionUID = 8579011687373112511L;

   public NoWordFoundAtPositionException() {
      super();
   }

   public NoWordFoundAtPositionException(String message) {
      super(message);
   }

   public NoWordFoundAtPositionException(String message, Throwable cause) {
      super(message, cause);
   }

   public NoWordFoundAtPositionException(Throwable cause) {
      super(cause);
   }

}
