/*
 * Erstellt: 12.06.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.util.Vector;


import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.pathselector.PathSelector;


/**
 * @author Volker Klöbb
 */
public class MacroSentence extends DB_Element {

   /**
    * 
    */
   private static final long  serialVersionUID = -3660883261302165976L;
   private IllocutionUnitRoot head;
   private Vector             dependencies;
   private int                path;
   private boolean            accepted;

   public MacroSentence(DBC_Key key,
         int id,
         IllocutionUnitRoots roots,
         IllocutionUnitRoot head,
         int path,
         boolean accepted) {
      super(id);
      key.unlock();
      this.head = head;
      this.path = path;
      this.accepted = accepted;
      dependencies = new Vector();
      roots.addMacroSentence(this);
   }

   public MacroSentence(IllocutionUnitRoots roots, IllocutionUnitRoot head) {
      super(-1);
      this.head = head;
      dependencies = new Vector();
      roots.addMacroSentence(this);
   }

   /**
    * Prüft, ob dieser Makrosatz schon akzeptiert wurde.
    * 
    */
   public boolean isAccepted() {
      return accepted;
   }

   /**
    * Legt fest, ob dieser Makrosatz vom Benutzer abgesegnet wurde oder nicht.
    * 
    */
   public void setAccepted(boolean accepted) {
      changeState(CHANGE);
      this.accepted = accepted;
   }

   public boolean addDependency(IllocutionUnitRoot iur) {
      for (int i = 0; i < dependencies.size(); i++) {
         IllocutionUnitRoot x = (IllocutionUnitRoot) dependencies.get(i);
         if (x.getIndex() > iur.getIndex()) {
            dependencies.add(i, iur);
            changeState(CHANGE);
            return true;
         }
         if (x.getIndex() == iur.getIndex())
            return false;
      }
      dependencies.add(iur);
      changeState(CHANGE);
      return true;
   }

   public boolean removeDependency(IllocutionUnitRoot iur) {
      if (dependencies.remove(iur)) {
         changeState(CHANGE);
         return true;
      }
      return false;
   }

   public boolean containsDependency(IllocutionUnitRoot iur) {
      return dependencies.contains(iur);
   }

   public boolean containsIndex(int index) {
      if (head.getIndex() == index)
         return true;

      for (int i = 0; i < dependencies.size(); i++) {
         IllocutionUnitRoot iur = (IllocutionUnitRoot) dependencies.get(i);
         if (iur.getIndex() == index)
            return true;
      }

      return false;
   }

   public boolean containsPosition(int position) {
      if (head.getIllocutionUnit().containsPosition(position))
         return true;

      for (int i = 0; i < dependencies.size(); i++) {
         IllocutionUnitRoot iur = (IllocutionUnitRoot) dependencies.get(i);
         if (iur.getIllocutionUnit().containsPosition(position))
            return true;
      }

      return false;
   }

   public Vector getDependencies() {
      return dependencies;
   }

   public IllocutionUnitRoot getHead() {
      return head;
   }

   public int getIndex() {
      return head.getIndex();
   }

   public void setChapter(DBC_Key key, Chapter chapter) {}

   /**
    * Die ID des Pfades.
    */
   public int getPath() {
      return path;
   }

   /**
    * Setzt den Pafd von diesem Makrosatz
    * 
    * @param pathID
    *        die ID des Pfades
    * @see PathSelector
    */
   public void setPath(int pathID) {
      changeState(CHANGE);
      path = pathID;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer("head: " + head);
      for (int i = 0; i < dependencies.size(); i++) {
         IllocutionUnitRoot iur = (IllocutionUnitRoot) dependencies.get(i);
         sb.append("\n-> " + iur.getIllocutionUnit());
      }
      return sb.toString();
   }

   public boolean equals(Object o) {
      if (o instanceof MacroSentence) {
         return getIndex() == ((MacroSentence) o).getIndex();
      }
      return false;
   }

   public boolean remove() {
      changeState(REMOVE);
      return true;
   }

}
