/*
 * Erstellt: 1.3.2006
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.text.DefaultStyledDocument;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Eine Sammlung aller Renominalisierungen, die in einem Kapitel vorkommen.
 * 
 * author: Martin Schaefer
 */
public final class Renominalisations
      implements
         Serializable {

   /**
    * 
    */
   private static final long     serialVersionUID = -2342338995879462113L;
   private transient Chapter     chapter;
   private Hashtable             renominalisations;
   private DefaultStyledDocument interpretation;

   public Renominalisations(Chapter chapter) {
      this.chapter = chapter;
      renominalisations = new Hashtable();
   }

   /**
    * Wird zum laden aus der Datenbank benötigt. Nicht verwenden, sondern
    * addRenominalisation()
    */
   public boolean setRenominalisation(DBC_Key key,
         int id,
         ConstitutiveWord cw,
         String category) {
      key.unlock();
      add(id, cw, category);
      return true;
   }

   public void addRenominalisation(ConstitutiveWord cw, String category) {
      add(-1, cw, category);
   }

   private void add(int id, ConstitutiveWord cword, String category) {
      Renominalisation i = new Renominalisation(id, chapter, category, cword);
      Integer index = new Integer(cword.getWord().getIndex());

      if (renominalisations.containsKey(index)) {
         Vector v = (Vector) renominalisations.get(index);
         if (!v.contains(i))
            v.add(i);
         else
             v.setElementAt(i,index);
      }
      else {
         Vector v = new Vector();
         v.add(i);
         renominalisations.put(index, v);
      }
   }

   /**
    * Löscht eine Renominalisierung aus der Sammlung.
    * 
    * @param cw
    *        Das Wort, dessen Renominalisierung gelöscht werden soll.
    * @param category
    *        Der Name der zu löschenden Renominalisierung.
    * @return true, falls es eine zu löschende Renominalisierung gab, sonst
    *         false.
    */
   public boolean removeRenominalisation(ConstitutiveWord cw, String category) {
      Vector v = getRenominalisations(cw);
      if (v == null)
         return false;

      for (int i = 0; i < v.size(); i++) {
         Renominalisation renom = (Renominalisation) v.get(i);
         if (renom.getCategory().equalsIgnoreCase(category)) {
            renom.remove();
            return true;
         }
      }
      return false;

   }

   /**
    * Löscht alle Renominalisierungen dieser Sammlung, die dieser Kategorie
    * zugeordnet wurden.
    * 
    * @param category
    *        die zu löschen Kategorie
    */
   public void removeCategory(String category) {
      Vector v = getRenominalisations();
      for (int i = 0; i < v.size(); i++) {
         Renominalisation renom = (Renominalisation) v.get(i);
         if (renom.getCategory().equalsIgnoreCase(category))
            renom.changeState(Renominalisation.REMOVE);
      }
   }

   /**
    * Ändert von allen Renominalisierungen dieser Kategorie den Namen der
    * Kategorie.
    * 
    * @param category
    *        der alte Name der Kategorie
    * @param newCategory
    *        der neue Name der Kategorie
    */
   public void renameCategory(String category, String newCategory) {
      Vector v = getRenominalisations();
      for (int i = 0; i < v.size(); i++) {
         Renominalisation renom = (Renominalisation) v.get(i);
         if (renom.getCategory().equalsIgnoreCase(category))
            renom.setCategory(newCategory);
      }
   }

   public Vector getRenominalisationsByCategory(String category) {
      Vector res = new Vector();
      Collection e = renominalisations.values();
      Iterator iter = e.iterator();
      while (iter.hasNext()) {
         Vector v = (Vector) iter.next();
         for (int i = 0; i != v.size(); i++) {
            Renominalisation renom = (Renominalisation) v.get(i);
            if (renom.getCategory().equalsIgnoreCase(category)) {
               res.add(renom);
            }
         }
      }
      return res;
   }

   /**
    * Gibt alle Renominalisierungen zu diesem konstitutivem Wort in einem Vektor
    * zurück.
    */
   public Vector getRenominalisations(ConstitutiveWord cw) {
      Integer index = new Integer(cw.getWord().getIndex());
      Vector res = new Vector();
      Vector v = (Vector) renominalisations.get(index);
      if (v == null)
         return null;

      for (int i = 0; i < v.size(); i++) {
         Renominalisation renom = (Renominalisation) v.get(i);
         if (!renom.isRemoved())
            res.add(renom);
      }
      return res;

   }

   /**
    * Gibt alle Kategorien dieser Sammlung zurück.
    */
   public Vector getCategories() {
      Vector res = new Vector();
      Enumeration e = renominalisations.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Renominalisation renom = (Renominalisation) v.get(i);
               if (!renom.isRemoved() && !res.contains(renom.getCategory()))
                  res.add(renom.getCategory());
            }
         }
      }
      return res;
   }

   /**
    * Gibt alle Renominalisierung zurück, die in dieser Sammlung vorkommen.
    */
   public Vector getRenominalisations() {
      Vector res = new Vector();
      Enumeration e = renominalisations.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Renominalisation renom = (Renominalisation) v.get(i);
               if (!renom.isRemoved() && !res.contains(renom))
                  res.add(renom);
            }
         }
      }
      return res;
   }

   /**
    * Gibt alle Renominalisierung zurück, die in dieser Sammlung vorkommen,
    * auch die Renominalisierung, die als gelöscht markiert wurden. Wird zum
    * Speichern in die Datenbank benötigt.
    */
   public Vector getAllRenominalisations(DBC_Key key) {
      key.unlock();
      Vector res = new Vector();
      Enumeration e = renominalisations.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Renominalisation renom = (Renominalisation) v.get(i);
               if (!res.contains(renom))
                  res.add(renom);
            }
         }
      }
      return res;
   }

   /**
    * Gibt einen Vector mit Wörter zurück, die alle zu dieser Kategorie
    * gespeichert wurden.
    */
   public Vector getWords(String category) {
      Vector res = new Vector();
      Enumeration e = renominalisations.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Renominalisation renom = (Renominalisation) v.get(i);
               if (!renom.isRemoved()
                     && renom.getCategory().equalsIgnoreCase(category)
                     && !res.contains(renom))
                  res.add(renom.getWord());
            }
         }
      }
      return res;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      Enumeration k = renominalisations.keys();
      while (k.hasMoreElements()) {
         Integer index = (Integer) k.nextElement();
         Word word = (Word) chapter.getTokenAtIndex(index.intValue());
         sb.append(word.getContent());
         sb.append(": ");
         Vector v = (Vector) renominalisations.get(index);
         for (int i = 0; i < v.size(); i++) {
            Renominalisation renom = (Renominalisation) v.get(i);
            if (!renom.isRemoved()) {
               sb.append(renom.getCategory());
               sb.append(", ");
            }
         }
         sb.append('\n');
      }
      return sb.toString();
   }

   /**
    * Wird vom DBC benötigt.
    */
   public void setChapter(DBC_Key key,
         Chapter chapter,
         IllocutionUnitRoots roots) {
      key.unlock();
      this.chapter = chapter;

      Enumeration elements = renominalisations.elements();
      while (elements.hasMoreElements()) {
         Vector renoms = (Vector) elements.nextElement();
         for (int i = 0; i < renoms.size(); i++) {
            Renominalisation renom = (Renominalisation) renoms.get(i);
            renom.setChapter(key, chapter);
            renom.setConstitutiveWord(key, roots);
         }
      }
   }

   public Chapter getChapter() {
      return chapter;
   }

   public void updateIDs(DBC_Key key, Renominalisations answer) {
      key.unlock();

      Enumeration keys = renominalisations.keys();
      while (keys.hasMoreElements()) {
         Object k = keys.nextElement();
         Vector renoms1 = (Vector) renominalisations.get(k);
         Vector renoms2 = (Vector) answer.renominalisations.get(k);
         for (int i = 0; i < Math.min(renoms1.size(), renoms2.size()); i++) {
            Renominalisation renom1 = (Renominalisation) renoms1.get(i);
            Renominalisation renom2 = (Renominalisation) renoms2.get(i);
            renom1.updateIDs(key, renom2);
         }
      }
   }

   public boolean containsWord(ConstitutiveWord cw) {
      Vector v = (Vector) renominalisations.get(new Integer(cw.getWord()
            .getIndex()));
      if (v == null) {
         return false;
      }
      Iterator i = v.iterator();
      while (i.hasNext()) {
         Renominalisation r = (Renominalisation) i.next();
         if (r.getConstitutiveWord().equals(cw)) {
            return true;
         }
      }
      return false;

   }

   public String getCategory(ConstitutiveWord cw) {
      Vector v = getRenominalisations();
      for (int i = 0; i < v.size(); i++) {
         Renominalisation renom = (Renominalisation) v.get(i);
         if (renom.getConstitutiveWord().equals(cw)) {
            return renom.getCategory();
         }
      }
      return null;
   }

   public void removeRenominalisation(String category) {
      Vector v = getRenominalisations();
      for (int i = 0; i < v.size(); i++) {
         Renominalisation renom = (Renominalisation) v.get(i);
         if (renom.getCategory().equalsIgnoreCase(category))
            renom.remove();
      }
   }

   public DefaultStyledDocument getInterpretation() {
      return interpretation;
   }

   public void setInterpretation(DefaultStyledDocument intDoc) {
      interpretation = intDoc;
   }

}