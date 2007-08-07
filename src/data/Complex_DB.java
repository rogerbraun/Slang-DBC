/*
 * Erstellt: 11.12.2005
 */

package data;

import java.util.Vector;

import connection.DBC_Key;

public class Complex_DB extends DB_Element {

   private static final long serialVersionUID = -5124223975220890767L;

   /**
    * Ist das ein 0 Komplex? 0=Kein Nullkomplex - VollstÃ¤ndiger Komplex aus
    * Nomen und Deictica (mindestens ein Deicticon) -1 Nullkomplex der nur aus
    * Deicticon besteht 1 Nullkomplex der nur aus Nomen besteht
    */
   private int               type;

   /**
    * Die IDs der CWs, die für die Nomen stehen
    */
   private Vector            nounsID;

   /**
    * Ein Vektor mit Integer, die für die ID der CWs stehen, die Deiktikas sind.
    */
   private Vector            deicticasID;

   public Complex_DB(Complex_DB complex) {
      super(complex.getDB_ID());
      type = complex.type;
      nounsID = new Vector();
      deicticasID = new Vector();
   }

   public Complex_DB(DBC_Key key, int id, int type) {
      super(id);
      key.unlock();
      this.type = type;
      nounsID = new Vector();
      deicticasID = new Vector();
   }

   protected Complex_DB() {
      super(-1);
      deicticasID = new Vector();
      nounsID = new Vector();
   }

   public final Complex_DB cloneComplex() {
      Complex_DB c = new Complex_DB();
      c.setDB_ID(getDB_ID());
      c.changeState(getStateAsInt());
      c.type = type;
      c.nounsID = (Vector) nounsID.clone();
      c.deicticasID = (Vector) deicticasID.clone();
      return c;
   }
   
   public int hashCode() {
      return nounsID.hashCode() ^ deicticasID.hashCode();
   }

   /**
    * Liefert Complextype
    * 
    */
   public int getComplexType() {
      return type;
   }

   public void setComplexType(int type) {
      this.type = type;
      changeState(CHANGE);
   }

   protected void addDeicticonID(int deicticaID) {
      Integer id = new Integer(deicticaID);
      if (!deicticasID.contains(id)) {
         deicticasID.add(id);
         changeState(CHANGE);
      }
   }

   public void addDeicticonID(DBC_Key key, int deicticaID) {
      key.unlock();
      addDeicticonID(deicticaID);
   }

   public Vector getDeicticasID() {
      return deicticasID;
   }

   protected boolean removeDeicticaID(int deicticaID) {
      Integer id = new Integer(deicticaID);
      if (deicticasID.remove(id)) {
         changeState(CHANGE);
         return true;
      }
      return false;
   }

   protected void removeAllDeicticasID() {
      deicticasID.clear();
   }
   
   protected void addNounID(int nounID) {
      Integer id = new Integer(nounID);
      if (!nounsID.contains(id)) {
         nounsID.add(id);
         changeState(CHANGE);
      }
   }

   public void addNounID(DBC_Key key, int nounID) {
      key.unlock();
      addNounID(nounID);
   }

   public Vector getNounsID() {
      return nounsID;
   }

   protected boolean removeNounID(int nounID) {
      Integer id = new Integer(nounID);
      if (nounsID.remove(id)) {
         changeState(CHANGE);
         return true;
      }
      return false;
   }

   protected void removeAllNounsID() {
      nounsID.clear();
   }

   public boolean remove() {
      changeState(REMOVE);
      return false;
   }

   /**
    * Ohne Funktion
    */
   public int getIndex() {
      return 0;
   }

   public void setChapter(DBC_Key key, Chapter chapter) {
   // TODO Auto-generated method stub

   }

   public String toString() {
      return nounsID + ": " + deicticasID;
   }

}
