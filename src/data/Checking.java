/*
 * Erstellt: 17.10.2005
 */

package data;

import connection.DBC_Key;

public class Checking extends DB_Element {

   /**
    * 
    */
   private static final long serialVersionUID = 6718606469348044669L;
   private MeaningUnit       meaningUnit;
   private int               path;
   private boolean           accepted;

   public Checking(DBC_Key key,
         int id,
         IllocutionUnitRoots roots,
         MeaningUnit meaningUnit,
         int path,
         boolean accepted) {
      super(id);
      key.unlock();
      this.meaningUnit = meaningUnit;
      this.path = path;
      this.accepted = accepted;
      roots.addChecking(this);
      meaningUnit.getRoot().addChecking(this);
   }

   public Checking(IllocutionUnitRoots roots, MeaningUnit meaningUnit) {
      super(-1);
      this.meaningUnit = meaningUnit;

      roots.addChecking(this);
      meaningUnit.getRoot().addChecking(this);
   }

   public MeaningUnit getMeaningUnit() {
      return meaningUnit;
   }

   public IllocutionUnitRoot getRoot() {
      return meaningUnit.getRoot();
   }

   public IllocutionUnit getIllocutionUnit() {
      return meaningUnit.getRoot().getIllocutionUnit();
   }

   public void setPath(int pathID) {
      changeState(CHANGE);
      path = pathID;
   }

   /**
    * Die ID des Pfades.
    */
   public int getPath() {
      return path;
   }

   public boolean isAccepted() {
      return accepted;
   }

   public void setAccepted(boolean accepted) {
      changeState(CHANGE);
      this.accepted = accepted;
   }

   public boolean remove() {
      changeState(REMOVE);
      meaningUnit.getRoot().removeChecking(this);
      return true;
   }

   public int getIndex() {
      return getRoot().getIndex();
   }

   public void setChapter(DBC_Key key, Chapter chapter) {

   }

   public String toString() {
      return "{CH " + meaningUnit.toString() + "}";
   }

   public boolean equals(Object o) {
      if (o instanceof Checking) {
         Checking c = (Checking) o;
         return meaningUnit.equals(c.getMeaningUnit());
      }
      return false;
   }

   public boolean containsIndex(int index) {
      return getIllocutionUnit().containsIndex(index);
   }

   public boolean containsPosition(int position) {
      return meaningUnit.containsPosition(position);
   }
}
