/*
 * Erstellt: 13.05.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.tools;

import javax.swing.JOptionPane;

import de.uni_tuebingen.wsi.ct.slang2.dbc.client.DBC;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Book;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Chapter;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_KeyAcceptor;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_SaveException;

/**
 * Eine Klasse, mit der man Bücher und Kapitel erstellen und diese in die
 * Datenbank übertragen kann. <br>
 * Die normale Reihenfolge ist: <br>
 * <ul>
 * <li>Writer erstellen</li>
 * <li>Buch erstellen</li>
 * <li>Kapitel erstellen</li>
 * <li>In beliebiger Folge Wörter, Satzzeichen, Zeilenumbrüche und
 * Äußerungseinheiten zu dem Kapitel hinzufügen</li>
 * <li>abspeichern</li>
 * </ul>
 * 
 * @author Volker Klöbb
 */
public class Writer implements DBC_KeyAcceptor {

   private DBC_Key key;
   private DBC     dbc;
   private Book    book;
   private Chapter chapter;

   /**
    * Erstellt einen neuen Writer, der eine Verbindung zu dem angegebenen
    * Slang2-Server aufbaut.
    * 
    * @param server
    *        Der Server, normlaerweise "kloebb.dyndns.org"
    * @throws DBC_ConnectionException
    */
   public Writer(String server) throws DBC_ConnectionException {
      DBC_Key.makeKey(this);
      dbc = new DBC(server);
      dbc.close();
   }

   /**
    * Legt ein neues Buch an. Existiert in der Datenbank schon ein solches Buch,
    * wird das schon gespeicherte verwendet. Doppeleinträge sind nicht möglich.
    * 
    * @param title
    *        Der Titel des Buches
    * @param author
    *        Der Autor des Buches
    * @param year
    *        Der Erscheinungsjahr
    */
   public void makeBook(String title, String author, int year) {
      book = new Book(key, -1, title, author, year);
   }

   /**
    * Legt ein neues Kapitel an. Dazu muss zuvor mit makeBook() ein Buch
    * erstellt worden sein.
    * 
    * @param title
    *        Der Titel von dem Kapitel
    * @param index
    *        Der Index vom dem Kapitel. Das erste Kapitel steht an Stelle 0
    * @see #makeBook(String, String, int)
    */
   public void makeChapter(String title, int index, String date) {
      chapter = new Chapter(key, -1, -1, index, title, date);
   }
   
   public void makeChapter( Book book , String title , int index , String date )
   {
	   this.book = book;
	   chapter = new Chapter(key, -1, book.getDB_ID(), index, title, date);
   }
   
   public void setChapter(Chapter chapter) {
      this.chapter = chapter;
      chapter.setBookID(key, -1);
      chapter.setDB_ID(key, -1);
   }

   /**
    * Fügt in ein zuvor erstelltes Kapitel ein deutsches Wort ein.
    * 
    * @param content
    *        Das Wort
    * @param startPosition
    *        Die Zeichenposition des Wortes (Die Stelle des ersten Buchstabens)
    * @see #makeChapter(String, int)
    */
   public void addWord(String content, int startPosition) {
      chapter.addWord(key, content, startPosition);
   }

   /**
    * Fügt in ein zuvor erstelltes Kapitel ein Wort ein.
    * 
    * @param content
    *        Das Wort
    * @param language
    *        Die Sprache des Wortes (DE, EN, ...)
    * @param startPosition
    *        Die Zeichenposition des Wortes (Die Stelle des ersten Buchstabens)
    * @see #makeChapter(String, int)
    */
   public void addWord(String content, String language, int startPosition) {
      chapter.addWord(key, content, language, startPosition);
   }

   /**
    * Fügt ein Satzzeichen in ein zuvor erstelltes Kapitel ein.
    * 
    * @param sign
    *        Das Satzzeichen
    * @param position
    *        Die zeichenposition des Satzzeichens
    * @see #makeChapter(String, int)
    */
   public void addSign(char sign, int position) {
      chapter.addSign(key, sign, position);
   }

   /**
    * Fügt ein Satzzeichen in ein zuvor erstelltes Kapitel ein.
    * 
    * @param sign
    *        Das Satzzeichen
    * @param position
    *        Die zeichenposition des Satzzeichens
    * @see #makeChapter(String, int)
    */
   public void addSign(String sign, int position) {
      addSign(sign.charAt(0), position);
   }

   /**
    * Fügt einen Zeilenumbruch ein.
    * 
    * @param position
    *        Die Zeichenposition des Zeilenumbruches
    */
   public void addNewline(int position) {
      chapter.addNewline(key, position);
   }

   /**
    * Fügt eine Äußerungseinheit in ein bestehendes Kapitel ein.
    * 
    * @param startPosition
    *        Die Zeichenposition des Anfangsbuchstabens der Äußerungseinheit
    * @param endPosition
    *        Die Zeichenposition des Endbuchstabens der Äußerungseinheit
    */
/*   public void addIllocutionUnit(int startPosition, int endPosition) {
      chapter.addIllocutionUnit(key, startPosition, endPosition);
   }
*/
   /**
    * Fügt eine Äußerungseinheit in ein bestehendes Kapitel ein.
    * 
    * @param startPosition
    *        Die Zeichenposition des Anfangsbuchstabens der Äußerungseinheit
    * @param endPosition
    *        Die Zeichenposition des Endbuchstabens der Äußerungseinheit
    * @param kriterium
    * 		 Grund warum diese Äußerungseinheit definiert wurde
    */
   public void addIllocutionUnit(int startPosition, int endPosition, String kriterium, boolean paragraph, boolean newline) {
      chapter.addIllocutionUnit(key, startPosition, endPosition, kriterium,paragraph,newline);
   }
   
   /**
    * Setzt den Writer zurück, alle Daten gehen verloren.
    * 
    */
   public void reset() {
      book = null;
      chapter = null;
   }

   /**
    * Speicher das zuvor angelegte Buch und Kapitel
    * 
    * @throws DBC_SaveException
    */
   public void save()
         throws DBC_SaveException {
      if (book == null)
         throw new DBC_SaveException("Kein Buch angegeben");
      if (chapter == null)
         throw new DBC_SaveException("Kein Kapitel erstellt");

      chapter.calculateIndicies(key);

		try
		{
			dbc.open();
			dbc.saveBook(key, book);
			chapter.setBookID(key, book.getDB_ID());
			dbc.saveChapter(key, chapter);
			dbc.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Fehler: " + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
		}
	}

   public void setKey(DBC_Key key) {
       this.key = key;
   }
   
}
