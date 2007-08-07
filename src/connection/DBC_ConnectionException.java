/*
 * Erstellt: 03.12.2004
 */

package connection;

/**
 * Ausnahme, die geworfen wird, wenn kein Verbindungsaufbau zur Datenbank
 * möglich ist.
 * 
 * @author Volker Klöbb
 * @see DBC#open()
 */
public class DBC_ConnectionException extends Exception {

   /**
    * 
    */
   private static final long serialVersionUID = -116234630428275750L;

   public DBC_ConnectionException(String message) {
      super(message);
   }

}