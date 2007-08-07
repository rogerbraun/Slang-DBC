/**
 * Abstrakte Klasse für die Speicherung in die Datenbank. Nur für die DBC
 * wichtig.
 * 
 * @author Volker Klöbb
 */

package data;

import java.io.Serializable;

import connection.DBC_Key;

public abstract class DB_Element
      implements
         Serializable,
         IDOwner {

   protected static final int NORMAL = 0;
   protected static final int CHANGE = 1;
   protected static final int REMOVE = 2;

   private int                id;
   private int                state;

   protected DB_Element(int id) {
      this.id = id;
      state = NORMAL;
   }

   /**
    * Die Datenbank-ID
    */
   public int getDB_ID() {
      return id;
   }

   /**
    * Wird vom DBC benötigt
    */
   public final void setDB_ID(DBC_Key key, int id) {
      key.unlock();
      this.id = id;
   }

   protected final void setDB_ID(int id) {
      this.id = id;
   }

   /**
    * Gibt den Zustand des DB-Elements zurück.
    */
   public String getState() {
      switch (state) {
         case NORMAL :
            return "normal";
         case CHANGE :
            return "change";
         case REMOVE :
            return "remove";
         default :
            return "unknown";
      }
   }

   protected int getStateAsInt() {
      return state;
   }

   public abstract boolean remove();

   /**
    * Der Index des Elements, beginnend bei 0.
    */
   public abstract int getIndex();

   /**
    * Wird vom DBC benötigt
    */
   public abstract void setChapter(DBC_Key key, Chapter chapter);

   /**
    * Wird vom DBC benötigt
    */
   public void updateIDs(DBC_Key key, DB_Element answer) {
      key.unlock();
      id = answer.id;
      if (state != REMOVE)
         state = NORMAL;
   }

   protected final void changeState(int state) {
      if (this.state != REMOVE)
         this.state = state;
   }

   /**
    * Wird vom DBC benötigt
    */
   public final void resetState(DBC_Key key) {
      key.unlock();
      state = NORMAL;
   }

   protected final void resetStateIntern() {
      state = NORMAL;
   }

   /**
    * Prüft, ob dieses Element geändert wurde
    */
   public final boolean hasChanged() {
      return state == CHANGE;
   }

   /**
    * Prüft, ob dieses Element gelöscht wurde (oder noch gelöscht werden soll)
    */
   public final boolean isRemoved() {
      return state == REMOVE;
   }

   /**
    * Prüft, ob dieses Element nicht verändert wurde
    */
   public final boolean isUnchanged() {
      return state == NORMAL && id >= 0;
   }

   void resetIDs() {
      id = -1;
      state = CHANGE;
   }
}