/*
 * Erstellt: 1.3.2006
 */

package data;

import connection.DBC_Key;

/**
 * Repräsentiert die Verbindung von einem konstitutiven Wort zu einer
 * Renominalisierungskategorie.
 * 
 * author: Martin Schaefer
 */
public class Renominalisation extends DB_Element {

   /**
    * 
    */
   private static final long          serialVersionUID = -8098356250731727256L;
   private transient Chapter          chapter;
   private transient ConstitutiveWord consititutiveWord;
   private String                     category;
   private int                        wordIndex;

   Renominalisation(int id,
         Chapter chapter,
         String category,
         ConstitutiveWord consititutiveWord) {
      super(id);
      this.chapter = chapter;
      this.category = category;
      this.consititutiveWord = consititutiveWord;
      wordIndex = consititutiveWord.getDB_ID();

   }

   /**
    * Die Kategorie dieser Renominalisierung
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
    * Das Kapitel, in dem diese Renominalisierung vorkommt.
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Das Word, auf das sich diese Kategorie bezieht.
    */
   public Word getWord() {

      return consititutiveWord.getWord();
   }

   public ConstitutiveWord getConstitutiveWord() {

      return consititutiveWord;
   }

   public boolean equals(Object o) {
      if (o instanceof Renominalisation) {
         Renominalisation i = (Renominalisation) o;
         return category.equals(i.category)
               && consititutiveWord == i.getConstitutiveWord();
      }
      return false;
   }

   public String toString() {
      return category + ": " + consititutiveWord.getContent();
   }

   public boolean isValid() {
      return true;
   }

   public int getIndex() {
      return wordIndex;
   }

   /**
    * Wird vom DBC benötigt.
    */
   public void setChapter(DBC_Key key, Chapter chapter) { // todo: word ->
                                                            // constitutiveWord

      key.unlock();
      this.chapter = chapter;

   }

   public void setConstitutiveWord(DBC_Key key, IllocutionUnitRoots iur) {
      key.unlock();
      this.consititutiveWord = iur.getConstitutiveWordWithID(wordIndex);
   }

   public boolean remove() {
      changeState(REMOVE);
      return true;
   }
}
