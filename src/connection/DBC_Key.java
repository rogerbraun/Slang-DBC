
package connection;

/**
 * Ein Schlüssel, um bestimmte Funktionen freizuschalten, die nur vom DBC
 * verwendet werden dürfen.
 * 
 * @author Volker Klöbb
 */
public final class DBC_Key {

   private DBC_Key() {}

   /**
    * Testet den Schlüsel auf Gültigkeit
    * 
    */
   public void unlock() {}

   /**
    * Erstellt einen Schlüssel für eine registrierte Anwendung
    */
   public static DBC_Key makeKey(Object o) {
      // TODO null entfernen
      if (o == null
            || o.getClass().getName().equals("data.CompleteAnalyse")
            || o.getClass().getName().equals("connection.DBC")
            || o.getClass().getName().equals("connection.ChapterReader")
            || o.getClass().getName().equals("connection.Writer")
            || o.getClass().getName().equals("server.DBC_Server"))
         return new DBC_Key();
      return null;
   }
}