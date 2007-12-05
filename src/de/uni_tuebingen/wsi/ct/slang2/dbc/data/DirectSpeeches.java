/*
 * Created on 18.06.2004 To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * hier sind alle direkten Reden in einem Vektor gespeichert, damit wird die
 * Verwaltung der direkten Reden vereinfacht
 * 
 * @author Katrin-Shanthy Raff
 */
public class DirectSpeeches
      implements
         Serializable {

   private Vector directSpeeches;
   private Vector deletedDirectSpeeches;

   /**
    * Vektor für das Speichern von allen direkten Reden erzeugen
    */
   public DirectSpeeches() {
      directSpeeches = new Vector();
      deletedDirectSpeeches = new Vector();
   }

   /**
    * clont directSpeeches
    * 
    * @return Object
    */
   public Object clone() {
      System.out.println("DirectSpeeches wird geclont");
      try {
         DirectSpeeches directSpeechesClone = new DirectSpeeches();
         directSpeechesClone.directSpeeches = clone(directSpeeches);
         directSpeechesClone.deletedDirectSpeeches = clone(deletedDirectSpeeches);
         return directSpeechesClone;
      }
      catch (Exception e) {
         System.out.println("Fehler beim Clonen von DirectSpeeches");
      }
      return null;
   }

   private static Vector clone(Vector v) {
      if (v == null)
         return null;

      Vector vc = new Vector(v.capacity());
      for (int i = 0; i < v.size(); i++) {
         Object o = v.get(i);
         if (o instanceof DirectSpeech)
            vc.add(i, ((DirectSpeech) o).clone());
      }
      return vc;
   }

   /**
    * Anzahl der direkten Reden
    * 
    * @return int size
    */
   public int size() {
      return directSpeeches.size();
   }

   /**
    * die i-te direkte Rede zurückgeben
    * 
    * @param i
    *        int
    * @return DirectSpeech
    */
   public DirectSpeech get(int i) {
      return (DirectSpeech) directSpeeches.get(i);
   }

   /**
    * Gibt alle direkte Reden zurück, die in diesem Dialog stehen
    * 
    * @param dialog
    *        Der Dialog, dessen Direkte Reden zurückgegeben werden sollen
    * @return ein Vektor mit Direkten Reden
    */
   public Vector get(Dialog dialog) {
      Vector res = new Vector();
      Vector ius = dialog.getIllocutionUnits();

      for (int i = 0; i < directSpeeches.size(); i++) {
         DirectSpeech ds = (DirectSpeech) directSpeeches.get(i);

         for (int j = 0; j < ius.size(); j++) {
            IllocutionUnit iu = (IllocutionUnit) ius.get(j);
            if (ds.contains(iu)) {
               res.add(ds);
               break;
            }
         }
      }

      return res;
   }

   /**
    * eine direkte Rede hinten im Vector hinzufügen
    * 
    * @param directSpeech
    *        DirectSpeech
    */
   public void add(DirectSpeech directSpeech) {
      add(directSpeech, directSpeeches.size());
   }

   /**
    * direkte Rede an der i-ten Stelle einfügen
    * 
    * @param directSpeech
    *        DirectSpeech
    * @param i
    *        int
    */
   public void add(DirectSpeech directSpeech, int i) {
      if (i < 0)
         directSpeeches.insertElementAt(directSpeech, 0);
      else
         directSpeeches.insertElementAt(directSpeech, i);

      // damit die Positionen der direkten Reden wieder stimmen
      updatePositions();
   }

   /**
    * eine direkte Rede löschen und danach die Positionen neu festlegen
    * 
    * @param directSpeech
    *        DirectSpeech
    */
   public void remove(DirectSpeech directSpeech) {
      if (directSpeech != null) {
         for (int i = 0; i < directSpeeches.size(); i++) {
            DirectSpeech ds = (DirectSpeech) directSpeeches.get(i);
            if (ds.equals(directSpeech)) {
               ds.remove();
               directSpeeches.remove(i);
               deletedDirectSpeeches.add(ds);
               updatePositions();
               return;
            }
         }
      }
   }

   /**
    * wenn eine direkte Rede gelöscht oder hinzugefügt wurde müssen die
    * Positionen der anderen direkten Rede angepasst werden, damit die
    * Reihenfolge bestehen bleibt
    */
   public void updatePositions() {
      int counter = 0;
      for (int i = 0; i < directSpeeches.size(); i++) {
         DirectSpeech ds = (DirectSpeech) directSpeeches.get(i);
         ds.setIndex(++counter);
      }
   }

   /**
    * gibt den Vektor mit den direkten Reden zurück
    * 
    * @return directSpeeches
    */

   public Vector getVector() {
      return directSpeeches;
   }

   public Vector getAllDirectSpeeches(DBC_Key key) {
      key.unlock();
      Vector res = new Vector();
      res.addAll(deletedDirectSpeeches);
      res.addAll(directSpeeches);
      return res;
   }

   /**
    * Direkte Rede zurückgeben, die die wordPosition enthält
    * 
    * @param wordPosition
    *        int
    * @return DirectSpeech
    */
   public DirectSpeech getDirectSpeech(int wordPosition) {
      for (int i = 0; i < directSpeeches.size(); i++) {
         DirectSpeech ds = (DirectSpeech) directSpeeches.get(i);
         if (ds.containsIndex(wordPosition))
            return ds;
      }
      return null;
   }

   /**
    * testet, ob zwei direkte Reden die gleichen sind
    * 
    * @param o
    *        Object
    * @return boolean
    */
   public boolean equals(Object o) {
      if (o instanceof DirectSpeeches) {
         DirectSpeeches ds = (DirectSpeeches) o;
         for (int i = 0; i < directSpeeches.size(); i++) {
            if (!(ds.get(i)).equals(directSpeeches.get(i)))
               return false;
         }
      }
      return false;
   }

   /**
    * 
    */
   public String toString() {
      String s = new String();
      for (int i = 0; i < directSpeeches.size(); i++) {
         s += directSpeeches.get(i);
         s += "\n";
      }
      return s;
   }

   public void setChapter(DBC_Key key, Chapter chapter) {
      for (int i = 0; i < directSpeeches.size(); i++) {
         DirectSpeech ds = (DirectSpeech) directSpeeches.get(i);
         ds.setChapter(key, chapter);
      }
   }

   public void updateIDs(DBC_Key key, DirectSpeeches dss) {
      for (int i = 0; i < directSpeeches.size(); i++) {
         DirectSpeech ds1 = (DirectSpeech) directSpeeches.get(i);
         DirectSpeech ds2 = (DirectSpeech) dss.directSpeeches.get(i);
         ds1.updateIDs(key, ds2);
      }
   }

   public DirectSpeech getDirectSpeechWithID(int id) {
      for (int i = 0; i < directSpeeches.size(); i++) {
         DirectSpeech ds = (DirectSpeech) directSpeeches.get(i);
         if (ds.getDB_ID() == id)
            return ds;
      }
      return null;
   }

   void resetIDs() {
      for(int i = 0; i < directSpeeches.size(); i++)
         ((DirectSpeech)directSpeeches.get(i)).resetIDs();
   }
}