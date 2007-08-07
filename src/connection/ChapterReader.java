
package connection;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Vector;

import data.Chapter;

/**
 * @author shanthy
 */
public class ChapterReader {

   private Chapter chapter;

   private Vector  signs   = new Vector();

   private int     counter = 0;

   private DBC_Key key;

   /**
    * Ließt eine Textdatei ein und wandelt sie in ein Kapitel um.
    * 
    * @param bookID
    *        Die ID des Buches, zu der dieses Kapitel gehört
    * @param index
    *        Der Index des Kapitels. Das erste Kapitel steht an Stelle 0
    * @param title
    *        Der Titel von dem Kapitel
    * @param file
    *        Der Pfad zu der Datei
    */
   public ChapterReader(int bookID, int index, String title, String file, String date) {
      key = DBC_Key.makeKey(this);
      chapter = new Chapter(key, -1, bookID, index, title ,date);
      readSignFile();
      read(file);
   }

   public ChapterReader(String title, String file, String date) {
      this(-1, -1, title, file, date);
   }

   // private void writeSignFile(){
   // try
   // {
   // FileWriter fw = new FileWriter( "signs.txt" );
   // for(int i=0;i<signs.length;i++){
   // fw.write(signs[i]);
   // fw.write('\n');
   // }
   // fw.close();
   // }
   // catch ( IOException e ) { System.err.println( e ); }
   // }

   private void readSignFile() {
      try {
         StreamTokenizer st = new StreamTokenizer(new FileReader("connection"
               + File.separatorChar
               + "signs.txt"));
         // damit �berhaupt ein Zeilenende gefunden wird
         st.eolIsSignificant(true);
         // damit kein Zeichen eine Sonderbehandlung geniest
         st.resetSyntax();
         char c = ' ';
         for (int tval; (tval = st.nextToken()) != StreamTokenizer.TT_EOF;) {
            if (tval == StreamTokenizer.TT_EOL) {
               signs.add(String.valueOf(c));
            }
            else
               c = (char) st.ttype;
         }
      }
      catch (IOException io) {
         io.printStackTrace();
      }
   }

   /**
    * liest das txt-file ein
    * 
    * @param file
    */
   private void read(String file) {
      try {
         StreamTokenizer st = new StreamTokenizer(new FileReader(file));
         // damit überhaupt ein Zeilenende gefunden wird
         st.eolIsSignificant(true);
         // damit kein Zeichen eine Sonderbehandlung geniest
         st.resetSyntax();
         String line = new String();
         for (int tval; (tval = st.nextToken()) != StreamTokenizer.TT_EOF;) {
            if (tval == StreamTokenizer.TT_EOL) {
               splitLine(line.trim());
               line = "";
            }
            else
               line += (char) st.ttype;
         }

         if (line.length() > 0) {
            splitLine(line.trim());
         }
      }
      catch (IOException io) {
         io.printStackTrace();
      }
   }

   /**
    * den String einer Zeile in W�rter und Zeichen unterteilen
    * 
    * @param line
    */
   private void splitLine(String line) {
      // in oldCounter wird die Startposition einer IU gespeichert
      int oldCounter = -1;
      // falls die Zeile nicht leer ist, die Startposition merken
      if (line.length() > 0) {
         oldCounter = counter;
      }
      // zum Speichern der Buchstaben eines Wortes
      String word = new String();
      for (int i = 0; i < line.length(); i++) {
         char c = line.charAt(i);
         // falls das Zeichen ein Leerzeichen ist
         if (Character.isWhitespace(c)) {
            // falls der word-String nicht leer ist, ein neues Wort
            // erstellen
            if (word.length() > 0) {
               word = createWord(word);
               counter++;
            }
            // sonst nur den Counter erh�hen
            else
               counter++;
         }
         // falls der Character ein Zeichen ist
         else if (isSign(c)) {
            // falls das Ende der Zeile erreicht ist entweder Zeichen oder
            // Wort und Zeichen erstellen
            if (i >= (line.length() - 1)) {
               if (word.length() > 0) {
                  word = createWord(word);
               }
               createSign(c);
               counter++;
            }
            // falls der n�chste Character wieder ein Zeichen ist, entweder
            // Zeichen oder Wort und Zeichen erstellen
            else if (isSign(line.charAt(i + 1))) {
               if (word.length() > 0) {
                  word = createWord(word);
               }
               createSign(c);
            }
            // falls der n�chste Character ein Leerzeichen ist, entweder
            // Zeichen oder Wort und Zeichen erstellen
            else if (line.charAt(i + 1) == ' ') {
               if (word.length() > 0) {
                  word = createWord(word);
               }
               createSign(c);
            }
            // falls der n�chste Character ein Buchstabe ist
            else if (!isSign(line.charAt(i + 1)) && line.charAt(i + 1) != ' ') {
               if (word.length() > 0)
                  word += c;
               else {
                  createSign(c);
               }
            }
         }
         // Character muss ein Wort sein
         else {
            word += c;
         }
      }
      // eventuell das letzte Wort erstellen
      if (word.length() > 0) {
         word = createWord(word);
         counter++;
      }
      createIllocutionUnit(oldCounter);
   }

   private String createWord(String word) {
      chapter.addWord(key, word, counter);
      counter += word.length() - 1;
      word = "";
      counter++;
      return word;
   }

   private void createSign(char c) {
      chapter.addSign(key, c, counter);
      counter++;
   }

   /**
    * am Ende der Zeile �u�erungseinheit erstellen, falls die Zeile leer
    * war, newline einf�gen
    * 
    * @param oldCounter
    */
   private void createIllocutionUnit(int oldCounter) {
      if (oldCounter != -1 && oldCounter != counter) {
         chapter.addIllocutionUnit(key, oldCounter, counter - 2);
      }
      else {
         chapter.addNewline(key, counter);
      }
   }

   /**
    * true, falls der Character ein Zeichen ist, sonst falsch
    * 
    * @param c
    * @return boolean
    */
   private boolean isSign(char c) {
      for (int i = 0; i < signs.size(); i++) {
         char sign = (signs.get(i).toString()).charAt(0);
         if (sign == c) {
            return true;
         }
      }
      return false;
   }

   /**
    * Das eingelesene Kapitel
    */
   public Chapter getChapter() {
      return chapter;
   }

   public void save(String server) {
      try {
         DBC dbc = new DBC(server);
         dbc.saveChapter(key, chapter);
         dbc.close();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

}