/*
 * Erstellt: 14.11.2004
 */

package connection;

/**
 * Wird geworfen, wenn w�hrend der Speicherung in die Datenbank ein Fehler
 * auftritt.
 * 
 * @author Volker Kl�bb
 */
public class DBC_SaveException extends Exception {

   /**
    * 
    */
   private static final long serialVersionUID = 1675003923582081646L;

   public DBC_SaveException(String message) {
      super(message);
   }

}