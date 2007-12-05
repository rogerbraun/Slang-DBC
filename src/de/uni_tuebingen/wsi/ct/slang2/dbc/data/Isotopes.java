/*
 * Erstellt: 21.12.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Eine Sammlung aller Isotopien, die in einem Kapitel vorkommen.
 * 
 * @author Volker Kl�bb
 */
public final class Isotopes
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = -2342338995879462113L;
   private transient Chapter chapter;
   private Hashtable         isotopes;

   public Isotopes(Chapter chapter) {
      this.chapter = chapter;
      isotopes = new Hashtable();
   }

   /**
    * Wird zum laden aus der Datenbank ben�tigt. Nicht verwenden, sondern
    * addIsotope()
    */
   public boolean setIsotope(DBC_Key key, int id, Token token, String category) {
      key.unlock();
      if (token instanceof Word) {
         add(id, (Word) token, category);
         return true;
      }
      return false;
   }

   /**
    * F�gt eine neue Isotopie zu der Sammlung hinzu.
    * 
    * @param token
    *        Das Wort, auf das sich die Isotopie bezieht. Ein Satzzeichen kann
    *        keine Isotopie besitzen.
    * @param category
    *        Der Name der Kategorie. Muss min. ein Zeichen lang sein.
    * @return true, falls das einf�gen geklappt hat, false sonst.
    */
   public boolean addIsotope(Token token, String category) {
      if (token instanceof Word && category.trim().length() > 0) {
         add(-1, (Word) token, category);
         return true;
      }
      return false;
   }

   private void add(int id, Word word, String category) {
      Isotope i = new Isotope(id, chapter, category, word);
      Integer index = new Integer(word.getIndex());

      if (isotopes.containsKey(index)) {
         Vector v = (Vector) isotopes.get(index);
         if (!v.contains(i))
            v.add(i);
      }
      else {
         Vector v = new Vector();
         v.add(i);
         isotopes.put(index, v);
      }
   }

   /**
    * L�scht eine Isotopie aus der Sammlung.
    * 
    * @param token
    *        Das Wort, dessen Isotopie gel�scht werden soll.
    * @param category
    *        Der Name der zu l�schenden Isotopie.
    * @return true, falls es eine zu l�schende Isotopie gab, sonst false.
    */
   public boolean removeIsotope(Token token, String category) {
      if (token instanceof Word) {
         Vector v = getIsotopes(token);
         if (v == null)
            return false;

         for (int i = 0; i < v.size(); i++) {
            Isotope iso = (Isotope) v.get(i);
            if (iso.getCategory().equalsIgnoreCase(category)) {
               iso.remove();
               return true;
            }
         }
      }
      return false;
   }

   /**
    * L�scht alle Isotopien dieser Sammlung, die dieser Kategorie zugeorndet
    * wurden.
    * 
    * @param category
    *        die zu l�schen Kategorie
    */
   public void removeCategory(String category) {
      Vector v = getIsotopes();
      for (int i = 0; i < v.size(); i++) {
         Isotope iso = (Isotope) v.get(i);
         if (iso.getCategory().equalsIgnoreCase(category))
            iso.changeState(Isotope.REMOVE);
      }
   }

   /**
    * �ndert von allen Isotopien dieser Kategorie den Namen der Kategorie.
    * 
    * @param category
    *        der alte Name der Kategorie
    * @param newCategory
    *        der neue Name der Kategorie
    */
   public void renameCategory(String category, String newCategory) {
      Vector v = getIsotopes();
      for (int i = 0; i < v.size(); i++) {
         Isotope iso = (Isotope) v.get(i);
         if (iso.getCategory().equalsIgnoreCase(category))
            iso.setCategory(newCategory);
      }
   }

   /**
    * Gibt alle Isotopien zu diesem Token in einem Vektor zur�ck.
    */
   public Vector getIsotopes(Token token) {
      if (token instanceof Word) {
         Integer index = new Integer(token.getIndex());
         Vector res = new Vector();
         Vector v = (Vector) isotopes.get(index);
         if (v == null)
            return null;

         for (int i = 0; i < v.size(); i++) {
            Isotope iso = (Isotope) v.get(i);
            if (!iso.isRemoved())
               res.add(iso);
         }
         return res;
      }
      return null;
   }

   /**
    * Gibt alle Isotopien zu diesem String zur�ck. Es wird also nicht auf einen
    * konkreten Token eingegangen, sondern auf jeden Token, der den gleichen
    * Inhalt hat.
    */
   public Vector getIsotopes(String content) {
      Vector res = new Vector();
      Enumeration keys = isotopes.keys();
      while (keys.hasMoreElements()) {
         Integer index = (Integer) keys.nextElement();
         Word word = (Word) chapter.getTokenAtIndex(index.intValue());
         if (word.getContent().equals(content)) {
            Vector v = getIsotopes(word);
            if (v != null) {
               for (int i = 0; i < v.size(); i++) {
                  Object o = v.get(i);
                  if (!res.contains(o))
                     res.add(o);
               }
            }
         }
      }

      return res;
   }

   /**
    * Gibt alle Kategorien dieser Sammlung zur�ck.
    */
   public Vector getCategories() {
      Vector res = new Vector();
      Enumeration e = isotopes.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Isotope iso = (Isotope) v.get(i);
               if (!iso.isRemoved() && !res.contains(iso.getCategory()))
                  res.add(iso.getCategory());
            }
         }
      }
      return res;
   }

   /**
    * Gibt alle Isotopien zur�ck, die in dieser Sammlung vorkommen.
    */
   public Vector getIsotopes() {
      Vector res = new Vector();
      Enumeration e = isotopes.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Isotope iso = (Isotope) v.get(i);
               if (!iso.isRemoved() && !res.contains(iso))
                  res.add(iso);
            }
         }
      }
      return res;
   }

   /**
    * Gibt alle Isotopien zur�ck, die in dieser Sammlung vorkommen, auch die
    * Isotopien, die als gel�scht markiert wurden. Wird zum Speichern in die
    * Datenbank ben�tigt.
    */
   public Vector getAllIsotopes(DBC_Key key) {
      key.unlock();
      Vector res = new Vector();
      Enumeration e = isotopes.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Isotope iso = (Isotope) v.get(i);
               if (!res.contains(iso))
                  res.add(iso);
            }
         }
      }
      return res;
   }

   /**
    * Gibt einen Vector mit W�rter zur�ck, die alle zu dieser Kategorie
    * gespeichert wurden.
    */
   public Vector getWords(String category) {
      Vector res = new Vector();
      Enumeration e = isotopes.elements();
      while (e.hasMoreElements()) {
         Vector v = (Vector) e.nextElement();
         if (v != null) {
            for (int i = 0; i < v.size(); i++) {
               Isotope iso = (Isotope) v.get(i);
               if (!iso.isRemoved()
                     && iso.getCategory().equalsIgnoreCase(category)
                     && !res.contains(iso))
                  res.add(iso.getWord());
            }
         }
      }
      return res;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      Enumeration k = isotopes.keys();
      while (k.hasMoreElements()) {
         Integer index = (Integer) k.nextElement();
         Word word = (Word) chapter.getTokenAtIndex(index.intValue());
         sb.append(word.getContent());
         sb.append(": ");
         Vector v = (Vector) isotopes.get(index);
         for (int i = 0; i < v.size(); i++) {
            Isotope iso = (Isotope) v.get(i);
            if (!iso.isRemoved()) {
               sb.append(iso.getCategory());
               sb.append(", ");
            }
         }
         sb.append('\n');
      }
      return sb.toString();
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      this.chapter = chapter;

      Enumeration elements = isotopes.elements();
      while (elements.hasMoreElements()) {
         Vector isos = (Vector) elements.nextElement();
         for (int i = 0; i < isos.size(); i++) {
            Isotope iso = (Isotope) isos.get(i);
            iso.setChapter(key, chapter);
         }
      }
   }

   public Chapter getChapter() {
      return chapter;
   }

   public void updateIDs(DBC_Key key, Isotopes answer) {
      key.unlock();

      Enumeration keys = isotopes.keys();
      while (keys.hasMoreElements()) {
         Object k = keys.nextElement();
         Vector isos1 = (Vector) isotopes.get(k);
         Vector isos2 = (Vector) answer.isotopes.get(k);
         for (int i = 0; i < Math.min(isos1.size(), isos2.size()); i++) {
            Isotope iso1 = (Isotope) isos1.get(i);
            Isotope iso2 = (Isotope) isos2.get(i);
            iso1.updateIDs(key, iso2);
         }
      }
   }
   
   void resetIDs() {
      Vector isos = getIsotopes();
      for(int i = 0; i < isos.size(); i++)
         ((Isotope)isos.get(i)).resetIDs();
   }

}