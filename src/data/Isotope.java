/*
 * Erstellt: 21.12.2004
 */

package data;

import connection.DBC_Key;

/**
 * Repräsentiert die Verbindung von einem Wort zu einer Isotopiekategorie.
 * 
 * @author Volker Klöbb
 */
public class Isotope extends DB_Element {

   /**
    * 
    */
   private static final long serialVersionUID = -8098356250731727256L;
   private transient Chapter chapter;
   private transient Word    word;
   private String            category;
   private int               wordIndex;

   Isotope(int id, Chapter chapter, String category, Word word) {
      super(id);
      this.chapter = chapter;
      this.category = category;
      this.word = word;
      wordIndex = word.getIndex();
   }

   /**
    * Die Kategorie dieser Isotopie
    */
   public String getCategory() {
      return category;
   }

   /**
    * Setzt die Kategorie neu
    */
   public void setCategory(String category) {
      this.category = category;
      changeState(CHANGE);
   }

   /**
    * Das Kapitel, in dem diese Isotopie vorkommt.
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Das Word, auf das sich diese Kategorie bezieht.
    */
   public Word getWord() {
      return word;
   }

   public boolean equals(Object o) {
      if (o instanceof Isotope) {
         Isotope i = (Isotope) o;
         return category.equals(i.category) && word == i.word;
      }
      return false;
   }

   public String toString() {
      return category + ": " + word.getContent();
   }

   public int getIndex() {
      return 0;
   }

   /**
    * Wird vom DBC benötigt.
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      this.chapter = chapter;
      this.word = (Word) chapter.getTokenAtIndex(wordIndex);
   }

   public boolean remove() {
      changeState(REMOVE);
      return true;
   }
}