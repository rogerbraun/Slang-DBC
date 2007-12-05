/*
 * Erstellt: 22.05.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * @author Volker Klöbb
 */
public class Thema_DB
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 3328792469040815043L;
   private transient Chapter chapter;
    int               id;
   private int               firstOccurrenceID;
   private Thema_DB          firstOccurrence;
   private String            lemma;
   private String            description;
   private boolean           isRhema;
   private Vector            occurrences;

   /**
    * Wird vom DBC benötigt
    */
   public Thema_DB(DBC_Key key,
         Chapter chapter,
         int id,
         int firstOccurrenceID,
         String lemma,
         String description,
         boolean isRhema) {
      key.unlock();
      this.chapter = chapter;
      this.id = id;
      this.firstOccurrenceID = firstOccurrenceID;
      this.lemma = lemma;
      this.description = description;
      this.isRhema = isRhema;
      occurrences = new Vector();
   }

   /**
    * Konstruktor für ein neues Thema
    * 
    * @param chapter
    *        Das Kapitel, dem dieses Thema angehört
    * @param firstOccurrence
    *        Das erste Vorkommen des Themas
    * @param lemma
    *        Das Lemma
    * @param description
    *        Die nähere beschreinung
    * @param isRhema
    *        Ob der Datensatz ein Thema oder Rhema ist
    */
   public Thema_DB(Chapter chapter,
         Thema_DB firstOccurrence,
         String lemma,
         String description,
         boolean isRhema) {
      this.chapter = chapter;
      this.id = -1;
      this.firstOccurrence = firstOccurrence;
      this.lemma = lemma;
      this.description = description;
      this.isRhema = isRhema;
      occurrences = new Vector();
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setFirstOccurrence(DBC_Key key, Thema_DB firstOccurrence) {
      key.unlock();
      this.firstOccurrence = firstOccurrence;
   }

   /**
    * Die Datenbank-ID
    */
   public int getDB_ID() {
      return id;
   }

   /**
    * Die Datenbank-ID
    */
   public void setDB_ID(DBC_Key key, int id) {
      key.unlock();
      this.id = id;
   }

   /**
    * Die ID von dem ersten Vorkommen
    */
   public int getFirstOccurrenceID() {
      return firstOccurrenceID;
   }

   /**
    * Das Kapitel
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      this.chapter = chapter;
   }

   /**
    * Die Beschreibung
    */
   public String getDescription() {
      return description;
   }

   /**
    * Das erste Vorkommen von dem Thema
    */
   public Thema_DB getFirstOccurrence() {
      return firstOccurrence;
   }

   /**
    * Prüft, ob es sich um ein Rhema handelt.
    */
   public boolean isRhema() {
      return isRhema;
   }

   /**
    * Das Lemma
    */
   public String getLemma() {
      return lemma;
   }

   /**
    * Alle Vorkommen des Themas
    */
   public Vector getOccurrences() {
      return occurrences;
   }

   /**
    * Fügt ein neues Vorkommen zu dem Thema hinzu.
    * 
    * @param occurruence
    */
   public void addOccurrunce(Occurrence_DB occurruence) {
      occurrences.add(occurruence);
   }

   /**
    * Fügt ein neues Vorkommen zu dem Thema hinzu.
    * 
    * @param start
    *        Der Index von dem ersten Token des Vorkommens
    * @param end
    *        Der Index von dem letzten Token des Vorkommens
    */
   public void addOccurrence(int start, int end) {
      occurrences.add(new Occurrence_DB(this, start, end));
   }

   public String toString() {
      return lemma + ":\t" + occurrences;
   }
}
