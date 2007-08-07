/*
 * Erstellt: 23.10.2004
 */

package data;

import java.io.Serializable;
import java.util.Vector;

import connection.DBC_Key;

/**
 * Das Buch ist eine Rahmenklasse, um den Titel, Autor u.ä. zu speichern. Die
 * einzelnen Kapitel werden seperat geladen und stehen in keinem direkten
 * Zusammenhang zu dem Buch.
 * 
 * @author Volker Klöbb
 * @see Chapter
 */
public class Book
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 2362353228084193549L;
   private int               id;
   private String            title;
   private String            author;
   private int               year;
   private Vector            chapters;

   /**
    * Wird vom DBC benötigt
    */
   public Book(DBC_Key key, int id, String title, String author, int year) {
      key.unlock();
      this.id = id;
      this.title = title;
      this.author = author;
      this.year = year;

      chapters = new Vector();
   }

   /**
    * Wird vom DBC benötigt
    */
   public void add(DBC_Key key, Chapter chapter) {
      key.unlock();
      chapters.add(chapter);
   }

   /**
    * Gibt alle Kapitel des Buches in einem Vektor zurück. Diese Kapitel sind
    * aber nicht komplett, haben zum Beispiel keine Tokens. Bei Bedarfsfall muss
    * das Kapitel seperat geladen werden.
    * 
    * @return ein Vektor mit Kapiteln
    * @see Chapter
    */
   public Vector getChapters() {
      return chapters;
   }

   /**
    * Der Autor des Buches
    * 
    * @return Der Autor als String
    */
   public String getAuthor() {
      return author;
   }

   /**
    * Die ID, die von der Datenbank vergeben wurde
    * 
    * @return die Datenbank-ID
    */
   public int getDB_ID() {
      return id;
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setDB_ID(DBC_Key key, int id) {
      key.unlock();
      this.id = id;
   }

   /**
    * Der Title des Buches
    * 
    * @return den Buchtitel als String
    */
   public String getTitle() {
      return title;
   }

   /**
    * Das Erscheinungsjahr des Buches
    * 
    * @return Erscheinungsjahr
    */
   public int getYear() {
      return year;
   }

   public String toString() {
      return "Buch " + title;
   }
}