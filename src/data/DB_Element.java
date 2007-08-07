/**
 * Abstrakte Klasse f�r die Speicherung in die Datenbank. Nur f�r die DBC
 * wichtig.
 * 
 * @author Volker Kl�bb
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
    * Wird vom DBC ben�tigt
    */
   public final void setDB_ID(DBC_Key key, int id) {
      key.unlock();
      this.id = id;
   }

   protected final void setDB_ID(int id) {
      this.id = id;
   }

   /**
    * Gibt den Zustand des DB-Elements zur�ck.
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
    * Wird vom DBC ben�tigt
    */
   public abstract void setChapter(DBC_Key key, Chapter chapter);

   /**
    * Wird vom DBC ben�tigt
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
    * Wird vom DBC ben�tigt
    */
   public final void resetState(DBC_Key key) {
      key.unlock();
      state = NORMAL;
   }

   protected final void resetStateIntern() {
      state = NORMAL;
   }

   /**
    * Pr�ft, ob dieses Element ge�ndert wurde
    */
   public final boolean hasChanged() {
      return state == CHANGE;
   }

   /**
    * Pr�ft, ob dieses Element gel�scht wurde (oder noch gel�scht werden soll)
    */
   public final boolean isRemoved() {
      return state == REMOVE;
   }

   /**
    * Pr�ft, ob dieses Element nicht ver�ndert wurde
    */
   public final boolean isUnchanged() {
      return state == NORMAL && id >= 0;
   }

   void resetIDs() {
      id = -1;
      state = CHANGE;
   }
}