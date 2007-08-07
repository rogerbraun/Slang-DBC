/*
 * Erstellt: 18.06.2005
 */

package data;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Entspricht einem Tupel aus der datenbank. Kommt dann zum Einsatz, wenn daten
 * abgefragt werden, die in keinem direkten Zusammenhang zu einem speziellen
 * Kapitel stehen.
 * 
 * @author Volker Klöbb
 */
public class DB_Tupel
      implements
         Serializable {

   public static final int   UNKNOWN          = 0;
   public static final int   SAVE             = 1;
   public static final int   CHANGE           = 2;
   public static final int   DELETE           = 3;

   /**
    * 
    */
   private static final long serialVersionUID = 7557113770734924575L;
   private Hashtable         data;
   private int               state;

   public DB_Tupel() {
      data = new Hashtable();
      state = UNKNOWN;
   }

   public void setState(int state) {
      this.state = state;
   }

   public void setStateSave() {
      setState(SAVE);
   }

   public void setStateChange() {
      setState(CHANGE);
   }

   public void setStateDelete() {
      setState(DELETE);
   }

   public int getState() {
      return state;
   }

   public void put(String key, Object value) {
      data.put(key.toLowerCase(), value);
   }

   /**
    * Fügt ein neues Wert-Schlüssel-Paar ein
    * 
    * @param key
    *        Der Schlüssel
    * @param value
    *        Der Wert, hier ein String
    */
   public void put(String key, String value) {
      data.put(key.toLowerCase(), value);
   }

   public void put(String key, boolean value) {
      data.put(key.toLowerCase(), new Boolean(value));
   }

   /**
    * Fügt ein neues Wert-Schlüssel-Paar ein
    * 
    * @param key
    *        Der Schlüssel
    * @param value
    *        Der Wert, hier ein int
    */
   public void put(String key, int value) {
      data.put(key.toLowerCase(), new Integer(value));
   }

   /**
    * Fügt ein neues Wert-Schlüssel-Paar ein
    * 
    * @param key
    *        Der Schlüssel
    * @param value
    *        Der Wert, hier ein byte
    */
   public void put(String key, byte value) {
      data.put(key.toLowerCase(), new Byte(value));
   }

   public String getString(String key) {
      key = key.toLowerCase();
      if (data.containsKey(key)) {
         return data.get(key).toString();
      }
      throw new NoSuchElementException();
   }

   /**
    * Ließt ein int aus, das unter dem Schlüssel gespeichert wurde.
    */
   public int getInt(String key) {
      key = key.toLowerCase();
      if (data.containsKey(key)) {
         return ((Integer) data.get(key)).intValue();
      }
      throw new NoSuchElementException();
   }

   /**
    * Ließt ein byte aus, das unter dem Schlüssel gespeichert wurde.
    */
   public byte getByte(String key) {
      key = key.toLowerCase();
      if (data.containsKey(key)) {
         return ((Byte) data.get(key)).byteValue();
      }
      throw new NoSuchElementException();
   }

   public boolean getBoolean(String key) {
      key = key.toLowerCase();
      if (data.containsKey(key)) {
         return ((Boolean) data.get(key)).booleanValue();
      }
      throw new NoSuchElementException();
   }

   /**
    * Ließt einen String aus, der unter dem Schlüssel gespeichert wurde
    */
   public Object get(String key) {
      key = key.toLowerCase();
      if (data.containsKey(key)) {
         return data.get(key);
      }
      throw new NoSuchElementException();
   }

   public boolean delete(String key) {
      return data.remove(key.toLowerCase()) != null;
   }

   public String[] getKeys() {
      String[] keys = new String[data.size()];
      int index = 0;
      Enumeration ks = data.keys();
      while (ks.hasMoreElements())
         keys[index++] = (String) ks.nextElement();
      return keys;
   }

   public boolean containsKey(String key) {
      return data.containsKey(key.toLowerCase());
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      String[] keys = getKeys();
      for (int i = 0; i < keys.length; i++) {
         sb.append(keys[i]);
         sb.append(':');
         sb.append(data.get(keys[i]));
         sb.append(' ');
      }
      return sb.toString();
   }

   /**
    * Nimmt die durch key bestimmten Werte und erstellt aus diesen einen neuen
    * Vektor
    * 
    * @param tupels
    *        Der vektor mit DB_Tupeln, der gefiltert werden soll
    * @param key
    *        der Schlüssel, der den zu filternden Wert bestimmt
    */
   public static Vector filter(Vector tupels, String key) {
      key = key.toLowerCase();
      Vector res = new Vector();
      for (int i = 0; i < tupels.size(); i++) {
         DB_Tupel tupel = (DB_Tupel) tupels.get(i);
         res.add(tupel.get(key));
      }
      return res;
   }

   public static Vector filter(Vector tupels, String key, int value) {
      return filter(tupels, key, new Integer(value));
   }

   public static Vector filter(Vector tupels, String key, byte value) {
      return filter(tupels, key, new Byte(value));
   }

   public static Vector filter(Vector tupels, String key, boolean value) {
      return filter(tupels, key, new Boolean(value));
   }

   /**
    * Filtert alle Tupel aus, bei denen der Wert, der durch den Schlüssel key
    * bestimmt wird, gleich value ist
    * 
    */
   public static Vector filter(Vector tupels, String key, Object value) {
      key = key.toLowerCase();
      Vector res = new Vector();
      for (int i = 0; i < tupels.size(); i++) {
         DB_Tupel tupel = (DB_Tupel) tupels.get(i);
         if (tupel.get(key).equals(value))
            res.add(tupel);
      }
      return res;
   }

   public static Vector filterDistinct(Vector tupels, String key) {
      Vector res = new Vector();
      key = key.toLowerCase();

      for (int i = 0; i < tupels.size(); i++) {
         DB_Tupel tupel = (DB_Tupel) tupels.get(i);
         Object o = tupel.get(key);

         if (o instanceof String) {
            String s = (String) o;
            boolean found = false;
            for (int j = 0; j < res.size(); j++) {
               String t = (String) res.get(j);
               if (s.equalsIgnoreCase(t)) {
                  found = true;
                  break;
               }
            }
            if (!found)
               res.add(s);
         }
         else {
            if (!res.contains(o))
               res.add(o);
         }
      }

      return res;
   }
}
