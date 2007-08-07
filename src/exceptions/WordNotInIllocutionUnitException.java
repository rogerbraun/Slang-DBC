/*
 * Erstellt: 17.10.2005
 */

package exceptions;

public class WordNotInIllocutionUnitException extends Exception {

   /**
    * 
    */
   private static final long serialVersionUID = -5989074297042641573L;

   public WordNotInIllocutionUnitException() {
      super();
   }

   public WordNotInIllocutionUnitException(String message) {
      super(message);
   }

   public WordNotInIllocutionUnitException(String message, Throwable cause) {
      super(message, cause);
   }

   public WordNotInIllocutionUnitException(Throwable cause) {
      super(cause);
   }

}
