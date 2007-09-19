/*
 * Erstellt: 23.10.2004
 */

package connection;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

import connection.Message;

import pathselector.PathNode;
import data.Book;
import data.Chapter;
import data.Comments;
import data.Complex_DB;
import data.DB_Tupel;
import data.Dialog;
import data.Dialogs;
import data.DirectSpeech;
import data.DirectSpeeches;
import data.IllocutionUnitRoots;
import data.Isotopes;
import data.Relation;
import data.Renominalisations;
import data.TR_Assignation;
import data.Thema_DB;
import data.Word;
import data.WordListElement;
import dialogs.chapterloader.ChapterLoader;

/**
 * Die Schnittstelle zur Slang2-Datenbank, mit der Mï¿½glichkeit, ganze Kapitel
 * und dazugehï¿½rige Zusatzinformationen auszulesen und zu speichern. <br>
 * Codebeispiel: <br>
 * 
 * <pre>
 * DBC dbc = new DBC(&quot;kloebb.dyndns.org&quot;);
 * Chapter c = dbc.loadChapter(loader.getChapterID());
 * dbc.close();
 * System.out.println(c);
 * </pre>
 * 
 * @author Volker Klï¿½bb
 * @see ChapterLoader
 */
public class DBC {

   static DBC_Key     key;
   private Connection connection;
   private String     server;

   /**
    * ï¿½ffnet eine Verbindung zu der Datenbank. Am Ende wieder mit close()
    * schlieï¿½en.
    * 
    * @param server
    *        Die URL des Servers als String
    * @throws DBC_ConnectionException
    * @see #close()
    */
   public DBC(String server) throws DBC_ConnectionException {
      this.server = server;
      key = DBC_Key.makeKey(this);
      open();
   }

   /**
    * ï¿½ffnet eine geschlossene Verbindung.
    * 
    * @throws DBC_ConnectionException
    * @see #close()
    */
   public void open()
         throws DBC_ConnectionException {
      if (connection == null) {
         try {
            InetAddress server = InetAddress.getByName(this.server);
            Socket socket = new Socket(server, 9998);//3306 wenn local?
            connection = new Connection(socket);
         }
         catch (Exception e) {
            throw new DBC_ConnectionException(
                  "Verbindungsaufbau fehlgeschlagen");
         }
      }
   }

   /**
    * Beendet eine Verbindung
    * 
    * @see #open()
    */
   public void close() {
      if (connection != null) {
         connection.close();
         connection = null;
      }
   }

   /**
    * Lï¿½dt alle Bï¿½cher aus der Datenbank.
    * 
    * @return ein Vektor mit allen gespeicherten Bï¿½chern
    * 
    * @see Book
    * @see DBC#loadBook(int)
    */
   public Vector loadBooks()
         throws Exception {
      Message answer = connection.call(new Message(key, "loadBooks"));
      return (Vector) answer.getArguments()[0];
   }

   /**
    * Lï¿½dt ein Buch aus der Datenbank
    * 
    * @param id
    *        Die ID des Buches
    * @return Das Buch aus der Datenbank mit ID id
    */
   public Book loadBook(int id)
         throws Exception {
      Message answer = connection.call(new Message(key, "loadBook",
            new Integer(id)));
      return (Book) answer.getArguments()[0];
   }

   /**
    * Speichert ein Buch in der Datenbank
    * 
    * @param book
    * @see DBC#loadBook(int)
    */
   public void saveBook(DBC_Key key, Book book)
         throws Exception {
      key.unlock();
      Book answer = (Book) connection.call(new Message(key, "saveBook", book))
            .getArguments()[0];
      book.setDB_ID(key, answer.getDB_ID());
   }

   /**
    * Lï¿½dt ein Kapitel aus der Datenbank.
    * 
    * @param id
    *        Die ID des zu ladenden Kapitels
    * @see ChapterLoader
    */
   public Chapter loadChapter(int id)
         throws Exception {
      Message answer = connection.call(new Message(key, "loadChapter",
            new Integer(id)));
      return (Chapter) answer.getArguments()[0];
   }

   /**
    * Speichert ein Kapitel in der Datenbank
    * 
    * @see #loadChapter(int)
    */
   public void saveChapter(DBC_Key key, Chapter chapter)
         throws Exception {
      key.unlock();
      Chapter answer = (Chapter) connection.call(new Message(key,
            "saveChapter", chapter)).getArguments()[0];
      chapter.updateIDs(key, answer);
   }
   
   /*
    * Löscht ein Kapitel aus der Datenbank
    * 
    * @see #deleteChapter(int)
    */
   public void deleteChapter(int id)
   		throws Exception {
	   Message answer = connection.call(new Message(key, "deleteChapter",
			   new Integer(id)));
}

   /**
    * Lï¿½dt alle Direkten Reden aus der Datenbank, die zu diesem Kapitel
    * gespeichert wurden.
    * 
    * @param chapter
    *        das Kapitel
    * @see DirectSpeech
    */
   public DirectSpeeches loadDirectSpeeches(Chapter chapter)
         throws Exception {
      Message answer = connection.call(new Message(key, "loadDirectSpeeches",
            new Integer(chapter.getDB_ID())));
      DirectSpeeches dss = (DirectSpeeches) answer.getArguments()[0];
      dss.setChapter(key, chapter);
      return dss;
   }

/* 
   public void saveDirectSpeeches(Chapter chapter, DirectSpeeches directSpeeches)
         throws Exception {
      DirectSpeeches answer = (DirectSpeeches) connection.call(new Message(key,
            "saveDirectSpeeches", new Integer(chapter.getDB_ID()),
            directSpeeches)).getArguments()[0];
      directSpeeches.updateIDs(key, answer);
   }
*/
   
   public void saveDirectSpeeches(Chapter chapter, DirectSpeeches newdirectSpeeches, DirectSpeeches olddirectSpeeches)
   throws Exception {
	   DirectSpeeches answer = (DirectSpeeches) connection.call(new Message(key,
			   "saveDirectSpeeches", new Integer(chapter.getDB_ID()),
			   newdirectSpeeches, olddirectSpeeches)).getArguments()[0];
	   newdirectSpeeches.updateIDs(key, answer);
	   newdirectSpeeches.updateIDs(key, answer);
   }

   public void saveDialogs(Chapter chapter, Dialogs dialogs)
         throws Exception {
      Dialogs answer = (Dialogs) connection.call(new Message(key,
            "saveDialogs", new Integer(chapter.getDB_ID()), dialogs))
            .getArguments()[0];
      dialogs.updateIDs(key, answer);
   }

   /**
    * Lï¿½dt alle Dialoge des Kapitels aus der Datenbank.
    * 
    * @param chapter
    *        Das Kapitel, dessen Dialoge geladen werden sollen
    * @see Dialog
    */
   public Dialogs loadDialogs(Chapter chapter)
         throws Exception {
      Message answer = connection.call(new Message(key, "loadDialogs",
            new Integer(chapter.getDB_ID())));
      Dialogs ds = (Dialogs) answer.getArguments()[0];
      ds.setChapter(key, chapter);
      return ds;
   }

   /**
    * Lï¿½dt eine Unterart der ï¿½uï¿½erungseinheit, der Sememegruppen, Semantische
    * Einheiten und isolierte Funktionswï¿½rter usw. untergeordnet sind.
    * 
    * @param chapter
    *        Das Kapitel, dessen Daten geladen werden sollen
    * @return Ein Vektor mit allen Wurzeln zu den ï¿½uï¿½erungseinheiten. Wurden zu
    *         einer ï¿½uï¿½erungseinheit keine Sememegruppen o.ï¿½. angegeben, wird
    *         eine leere Wurzel erzeugt.
    * @see #saveIllocutionUnitRoots(IllocutionUnitRoots)
    */
   public IllocutionUnitRoots loadIllocutionUnitRoots(Chapter chapter)
         throws Exception {
      Message answer = connection.call(new Message(key,
            "loadIllocutionUnitRoots", new Integer(chapter.getDB_ID())));
      IllocutionUnitRoots roots = (IllocutionUnitRoots) answer.getArguments()[0];
      roots.setChapter(key, chapter);
      roots.fillCaches(key);
      return roots;
   }

   /**
    * Speichert die Wurzeln der ï¿½uï¿½erungseinheiten in der Datenbank
    * 
    * @see #loadIllocutionUnitRoots(Chapter)
    */
   public void saveIllocutionUnitRoots(IllocutionUnitRoots iurs)
         throws Exception {
      IllocutionUnitRoots answer = (IllocutionUnitRoots) connection
            .call(new Message(key, "saveIllocutionUnitRoots", new Integer(iurs
                  .getChapter().getDB_ID()), iurs)).getArguments()[0];
      iurs.updateIDs(key, answer);
   }

   /**
    * Alle Funktionswï¿½rter, ohne Zusammenhang zu dem Text. Deswegen nur Strings
    */
   public Vector loadFunctionWords()
         throws Exception {
      Vector answer = (Vector) connection.call(new Message(key,
            "loadFunctionWords")).getArguments()[0];
      return answer;
   }

   /**
    * Alle Kategorien der Funktionswï¿½rter
    */
   public Vector loadFunctionWordsCategories()
         throws Exception {
      Vector answer = (Vector) connection.call(new Message(key,
            "loadFunctionWordsCategories")).getArguments()[0];
      return answer;
   }

   /**
    * Alle semantisch konstitutiven Wï¿½rter ohne Zusammenhang zum Text. Deswegen
    * nur Strings.
    */
   public Vector loadConstitutiveWords()
         throws Exception {
      Vector answer = (Vector) connection.call(new Message(key,
            "loadConstitutiveWords")).getArguments()[0];
      return answer;
   }

   /**
    * Die extrem wichtigen, ï¿½ber alles benï¿½tigten Pfade
    */
   public PathNode getPaths()
         throws Exception {
      return (PathNode) connection.call(new Message(key, "getPaths"))
            .getArguments()[0];
   }

   /**
    * Die Numerus-Pfade
    */
   public PathNode getNumerusPaths()
         throws Exception {
      return (PathNode) connection.call(new Message(key, "getNumerusPaths"))
            .getArguments()[0];
   }
   
   /**
    * ï¿½berprï¿½ft, ob zu diesem Wort in einem Kapitel ein Funktionswort
    * gespeichert wurde. Dabei werden keine Teilwï¿½rter beachtet. Bei dem Wort
    * ist nur der Content wichtig, die Position im Kapitel spielt keine Rolle.
    * 
    * @param word
    *        Das zu ï¿½berprï¿½fende Wort
    * @return true, falls ein Funktionswort zu diesem Wort angelegt wurde.
    */
   public boolean existsFunctionWord(Word word)
         throws Exception {
      Message answer = connection.call(new Message(key, "existsFunctionWord",
            new Integer(word.getDB_ID()), new Integer(word.getEndPosition()
                  - word.getStartPosition())));
      return ((Boolean) answer.getArguments()[0]).booleanValue();
   }

   /**
    * ï¿½berprï¿½ft, ob zu diesem Wort in einem Kapitel ein Konstitutives Wort
    * gespeichert wurde. Dabei werden keine Teilwï¿½rter beachtet. Bei dem Wort
    * ist nur der Content wichtig, die Position im Kapitel spielt keine Rolle.
    * 
    * @param word
    *        Das zu ï¿½berprï¿½fende Wort
    * @return true, falls ein Konstitutives Wort zu diesem Wort angelegt wurde.
    */
   public boolean existsConstitutiveWord(Word word)
         throws Exception {
      Message answer = connection.call(new Message(key,
            "existsConstitutiveWord", new Integer(word.getDB_ID()),
            new Integer(word.getEndPosition() - word.getStartPosition())));
      return ((Boolean) answer.getArguments()[0]).booleanValue();
   }

   /**
    * Gibt alle CWs zurï¿½ck, die zu dieser Sprache gespeichert wurden
    * 
    * @param language
    *        Die Sprache, z.B. DE oder EN
    * @return Ein Vektor mit DB_Tupeln, welche das CW plus Zusatzinformationen
    *         speichern. Das Tupel besitzt folgende Eintrï¿½ge:
    *         <ul>
    *         <li><b>content (String) </b>: Der Inhalt des CW</li>
    *         <li><b>tr_genus (byte)</b>Genus</li>
    *         <li><b>tr_numerus (byte)</b>Numers</li>
    *         <li><b>tr_case (int)</b>Kasus</li>
    *         <li><b>tr_determination (byte)</b>Determination</li>
    *         <li><b>tr_person (byte)</b>Person</li>
    *         <li><b>tr_wordclass (byte)</b>Wortart</li>
    *         <li><b>tr_conjunction (byte)</b>Konjunktion</li>
    *         <li><b>tr_pronoun (byte)</b>Pronomen</li>
    *         <li><b>tr_tempus (byte)</b>Tempus</li>
    *         <li><b>tr_diathese (byte)</b>Diathese oder Genus Verbi</li>
    *         </ul>
    */
   public Vector getAllConstitutiveWords(String language)
         throws Exception {
      Message answer = connection.call(new Message(key,
            "getAllConstitutiveWords", language));
      return (Vector) answer.getArguments()[0];
   }

   /**
    * Gibt alle FWs zurï¿½ck, die zu dieser Sprache gespeichert wurden
    * 
    * @param language
    *        Die Sprache, z.B. DE oder EN
    * @return Ein Vector mit Strings
    * @throws Exception
    */
   public Vector getAllFunctionWords(String language)
         throws Exception {
      Message answer = connection.call(new Message(key, "getAllFunctionWords",
            language));
      return (Vector) answer.getArguments()[0];
   }

   /**
    * Gibt eine Liste mit Konstitutiven Wï¿½rtern zurï¿½ck, die entweder aus dem
    * ï¿½bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
    * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
    * Kapitel durchsucht. <br>
    * Bei dem Wort "Kï¿½nigshaus" bekommt man zum Beispiel folgendes Ergebnis
    * (sofern die CWs zu diesem Wort schon gespeichert wurden) [Kï¿½nig, haus].
    * Die Groï¿½- und Kleinschreibung wird ignoriert.
    * 
    * @param word
    *        Das Wort, auf welchem die gefundenen CWs aufbauen.
    * @return Ein Vektor mit DB_Tupeln, welche das CW plus Zusatzinformationen
    *         speichern. Das Tupel besitzt folgende Eintrï¿½ge:
    *         <ul>
    *         <li><b>content (String) </b>: Der Inhalt des CW</li>
    *         <li><b>start (int) </b>: Die Startposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>end (int) </b>: Die Endposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>chapter (int) </b>: Die ID des Kapitels, in dem dieses
    *         Wort vorkommt</li>
    *         <li><b>position (int) </b>: Die Position des Wortes innerhalb des
    *         Kapitels</li>
    *         <li><b>tr_genus (byte)</b>Genus</li>
    *         <li><b>tr_numerus (byte)</b>Numers</li>
    *         <li><b>tr_case (int)</b>Kasus</li>
    *         <li><b>tr_determination (byte)</b>Determination</li>
    *         <li><b>tr_person (byte)</b>Person</li>
    *         <li><b>tr_wordclass (byte)</b>Wortart</li>
    *         <li><b>tr_conjunction (byte)</b>Konjunktion</li>
    *         <li><b>tr_pronoun (byte)</b>Pronomen</li>
    *         <li><b>tr_tempus (byte)</b>Tempus</li>
    *         <li><b>tr_diathese (byte)</b>Diathese oder Genus Verbi</li>*
    *         </ul>
    * @throws Exception
    */
   public Vector getConstitutiveWords(Word word)
         throws Exception {
      Message answer = connection.call(new Message(key, "getConstitutiveWords",
            word.getContent(), word.getLanguage()));
      return (Vector) answer.getArguments()[0];
   }

   /**
    * Gibt eine Liste mit Konstitutiven Wï¿½rtern zurï¿½ck, die entweder aus dem
    * ï¿½bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
    * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
    * Kapitel durchsucht. <br>
    * Bei dem Wort "Kï¿½nigshaus" bekommt man zum Beispiel folgendes Ergebnis
    * (sofern die CWs zu diesem Wort schon gespeichert wurden) [Kï¿½nig, haus].
    * Die Groï¿½- und Kleinschreibung wird ignoriert.
    * 
    * @param content
    *        Der Inhalt des zu suchenden Wortes
    * @param language
    *        Die Sprache des Wortes
    * @return Ein Vektor mit DB_Tupeln, welche das CW plus Zusatzinformationen
    *         speichern. Das Tupel besitzt folgende Eintrï¿½ge:
    *         <ul>
    *         <li><b>content (String) </b>: Der Inhalt des CW</li>
    *         <li><b>start (int) </b>: Die Startposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>end (int) </b>: Die Endposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>chapter (int) </b>: Die ID des Kapitels, in dem dieses
    *         Wort vorkommt</li>
    *         <li><b>position (int) </b>: Die Position des Wortes innerhalb des
    *         Kapitels</li>
    *         <li><b>tr_genus (byte)</b>Genus</li>
    *         <li><b>tr_numerus (byte)</b>Numers</li>
    *         <li><b>tr_case (int)</b>Kasus</li>
    *         <li><b>tr_determination (byte)</b>Determination</li>
    *         <li><b>tr_person (byte)</b>Person</li>
    *         <li><b>tr_wordclass (byte)</b>Wortart</li>
    *         <li><b>tr_conjunction (byte)</b>Konjunktion</li>
    *         <li><b>tr_pronoun (byte)</b>Pronomen</li>
    *         <li><b>tr_tempus (byte)</b>Tempus</li>
    *         <li><b>tr_diathese (byte)</b>Diathese oder Genus Verbi</li>
    *         <li><b>lexprag_path (int)</b></li>
    *         <li><b>lexprag_level (int)</b></li>
    *         <li><b>text_gr_path (int)</b></li>
    *         <li><b>sem_path (int)</b></li>
    *         </ul>
    * @throws Exception
    */
   public Vector getConstitutiveWords(String content, String language)
         throws Exception {
      Message answer = connection.call(new Message(key, "getConstitutiveWords",
            content, language));
      return (Vector) answer.getArguments()[0];
   }

   /**
    * Gibt eine Liste mit Funktionswï¿½rtern zurï¿½ck, die entweder aus dem
    * ï¿½bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
    * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
    * Kapitel durchsucht. <br>
    * Bei dem Wort "Kï¿½nigshaus" bekommt man zum Beispiel folgendes Ergebnis
    * (sofern die FWs zu diesem Wort schon gespeichert wurden) [s]. Die Groï¿½-
    * und Kleinschreibung wird ignoriert.
    * 
    * @param word
    *        Das Wort, auf welchem die gefundenen FWs aufbauen.
    * @return Ein Vektor mit DB_Tupeln, welche das FW plus Zusatzinformationen
    *         speichern. Das Tupel besitzt folgende Eintrï¿½ge:
    *         <ul>
    *         <li><b>content (String) </b>: Der Inhalt des CW</li>
    *         <li><b>start (int) </b>: Die Startposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>end (int) </b>: Die Endposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>chapter (int) </b>: Die ID des Kapitels, in dem dieses
    *         Wort vorkommt</li>
    *         <li><b>position (int) </b>: Die Position des Wortes innerhalb des
    *         Kapitels</li>
    *         </ul>
    * @throws Exception
    */
   public Vector getFunctionWords(Word word)
         throws Exception {
      Message answer = connection.call(new Message(key, "getFunctionWords",
            word.getContent(), word.getLanguage()));
      return (Vector) answer.getArguments()[0];
   }

   /**
    * Gibt eine Liste mit Funktionswï¿½rtern zurï¿½ck, die entweder aus dem
    * ï¿½bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
    * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
    * Kapitel durchsucht. <br>
    * Bei dem Wort "Kï¿½nigshaus" bekommt man zum Beispiel folgendes Ergebnis
    * (sofern die FWs zu diesem Wort schon gespeichert wurden) [s]. Die Groï¿½-
    * und Kleinschreibung wird ignoriert.
    * 
    * @param content
    *        Der Inhalt des zu suchenden Wortes
    * @param language
    *        Die Sprache des Wortes
    * @return Ein Vektor mit DB_Tupeln, welche das FW plus Zusatzinformationen
    *         speichern. Das Tupel besitzt folgende Eintrï¿½ge:
    *         <ul>
    *         <li><b>content (String) </b>: Der Inhalt des CW</li>
    *         <li><b>start (int) </b>: Die Startposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>end (int) </b>: Die Endposition des CW, ausgehend vom
    *         Begin des Wortes (also relative Position bezogen auf das Wort)</li>
    *         <li><b>chapter (int) </b>: Die ID des Kapitels, in dem dieses
    *         Wort vorkommt</li>
    *         <li><b>position (int) </b>: Die Position des Wortes innerhalb des
    *         Kapitels</li>
    *         </ul>
    * @throws Exception
    */
   public Vector getFunctionWords(String content, String language)
         throws Exception {
      Message answer = connection.call(new Message(key, "getFunctionWords",
            content, language));
      return (Vector) answer.getArguments()[0];
   }

   /**
    * Speichert Themas (Themen, Themata..?) in die Datenbank. Dabei werden alle
    * alten Eintrï¿½ge zu dem Kapitel gelï¿½scht. Es mï¿½ssen also alle Themas von
    * diesem Kapitel ï¿½bergeben werden.
    * 
    * @param chapter
    *        Das Kapitel, zu dem die Themas gehï¿½ren
    * @param themas
    *        Ein Vektor mit Themas
    * @throws Exception
    */
   public void saveThemas(Chapter chapter, Vector themas)
         throws Exception {
      connection.call(new Message(key, "saveThemas", new Integer(chapter
            .getDB_ID()), themas));
   }

   /**
    * Lï¿½dt alle Themas von einem Kapitel und gibt sie in einem Vektor zurï¿½ck.
    * 
    * @param chapter
    *        Das Kapitel, vondem die Themas geladen werden sollen.
    * @throws Exception
    */
   public Vector loadThemas(Chapter chapter)
         throws Exception {
      Message answer = connection.call(new Message(key, "loadThemas",
            new Integer(chapter.getDB_ID())));
      Vector themas = (Vector) answer.getArguments()[0];
      for (int i = 0; i < themas.size(); i++) {
         Thema_DB thema = (Thema_DB) themas.get(i);
         thema.setChapter(key, chapter);
      }
      return themas;
   }

   /**
    * Erstellt eine Isotopie-Sammlung ï¿½ber alle in diesem Kapitel vorkommenden
    * Isotopien.
    */
   public Isotopes loadIsotopes(Chapter chapter)
         throws Exception {
      Message answer = connection.call(new Message(key, "loadIsotopes",
            new Integer(chapter.getDB_ID())));
      Isotopes isotopes = (Isotopes) answer.getArguments()[0];
      isotopes.setChapter(key, chapter);
      return isotopes;
   }

   /**
    * Speichert die Isotopien zu diesem Kapitel.
    * 
    * @throws DBC_SaveException
    */
   public void saveIsotopes(Isotopes isotopes)
         throws Exception {
      Isotopes answer = (Isotopes) connection.call(new Message(key,
            "saveIsotopes", isotopes, new Integer(isotopes.getChapter()
                  .getDB_ID()))).getArguments()[0];
      isotopes.updateIDs(key, answer);
   }

   /**
    * Speichert die Hierachie von Isotopien eines Kapitels in der Datenbank.
    * 
    * @param chapter
    *        Das Kapitel, in dem die Isotopien vorkommen.
    * @param hierachy
    *        Ein Vektor, der die Hierachie reprï¿½sentiert. Der nï¿½here Aufbau des
    *        Vektors ist egal, er wird serialisiert in der Datenbank
    *        gespeichert. Die maximale Grï¿½ï¿½e des serialisierten vektors darf
    *        65535 Zeichen nicht ï¿½berschreiten.
    */
   public void saveIsotopeHierachy(Chapter chapter, Vector hierachy)
         throws Exception {
      connection.call(new Message(key, "saveIsotopeHierachy", new Integer(
            chapter.getDB_ID()), hierachy));
   }

   /**
    * Lï¿½dt die Hierachien von Isotopien eines Kapitels.
    * 
    * @param chapter
    *        Das Kapitel, dessen Isotopien-Hierachie geladen werden soll.
    * @return Ein Vektor, der die Hierachie reprï¿½sentiert.
    */
   public Vector loadIsotopeHierachy(Chapter chapter)
         throws Exception {
      return (Vector) connection.call(new Message(key, "loadIsotopeHierachy",
            new Integer(chapter.getDB_ID()))).getArguments()[0];
   }

   /**
    * Alle in der Datenbank gespeicherten Sprachen
    * 
    * @return Ein Vektor mit Lï¿½nderkï¿½rzeln wie "DE" oder "EN"
    */
   public Vector getLanguages()
         throws Exception {
      return (Vector) connection.call(new Message(key, "getLanguages"))
            .getArguments()[0];
   }

   /**
    * Lï¿½dt alle Kommentare, die zu ï¿½uï¿½erungseinheiten eines Kapitels
    * abgespeichert wurden.
    * 
    * @param comments
    *        Die Kommentarsammlung, zu der die neuen Kommentare hinzugefï¿½gt
    *        werden sollen
    * @param chapter
    *        Das Kapitel, in dem die ï¿½uï¿½erungseinheiten stehen
    * @throws Exception
    */
   public void loadIllocutionUnitComments(Comments comments, Chapter chapter)
         throws Exception {
      comments.add(loadComments(chapter, Comments.CLASS_CODE_ILLOCUTION_UNIT));
   }

   /**
    * Lï¿½dt alle Kommentare, die zu direkten Reden eines Kapitels abgespeichert
    * wurden.
    * 
    * @param comments
    *        Die Kommentarsammlung, zu der die neuen Kommentare hinzugefï¿½gt
    *        werden sollen
    * @param chapter
    *        Das Kapitel, in dem die direkten Reden stehen
    * @throws Exception
    */
   public void loadDirectSpeechComments(Comments comments, Chapter chapter)
         throws Exception {
      comments.add(loadComments(chapter, Comments.CLASS_CODE_DIRECT_SPEECH));
   }

   /**
    * Lï¿½dt alle Kommentare, die zu Dialogen eines Kapitels abgespeichert wurden.
    * Dazu gehï¿½ren auch Vor- und Nachfeld der Dialoge.
    * 
    * @param comments
    *        Die Kommentarsammlung, zu der die neuen Kommentare hinzugefï¿½gt
    *        werden sollen
    * @param chapter
    *        Das Kapitel, in dem die Dialoge stehen
    * @throws Exception
    */
   public void loadDialogComments(Comments comments, Chapter chapter)
         throws Exception {
      comments.add(loadComments(chapter, Comments.CLASS_CODE_DIALOG));
      comments.add(loadComments(chapter, Comments.CLASS_CODE_DIALOG_FOLLOWUP));
      comments.add(loadComments(chapter, Comments.CLASS_CODE_DIALOG_RUNUP));
   }

   private Comments loadComments(Chapter chapter, int ownerClassCode)
         throws Exception {
      return (Comments) connection.call(new Message(key, "loadComments",
            new Integer(chapter.getDB_ID()), new Integer(ownerClassCode)))
            .getArguments()[0];
   }

   /**
    * Speichert die Kommentarsammlung in der DB ab.
    * 
    * @param comments
    * @throws Exception
    */
   public void saveComments(Comments comments)
         throws Exception {
      connection.call(new Message(key, "saveComments", comments));
      comments.resetChange(key);
   }

   /**
    * Gibt alle Eintrï¿½ge aus der Wortliste zurï¿½ck, die sich auf das Wort
    * beziehen
    * 
    * @param word
    *        Das Wort (Groï¿½/Kleinschreibung wird ignoriert)
    * @param language
    *        Die Sprache des Wortes (DE, EN, usw.)
    * @return Ein Vektor mit DB_Tupeln
    *         <ul>
    *         <li><b>content (String) </b>: Das Wort</li>
    *         <li><b>language (String) </b>: Die Sprache des Wortes</li>
    *         <li><b>id (int) </b>: Die ID des Eintrages in der Wortliste</li>
    *         <li><b>tr_genus (byte) </b>: Genus. Beliebiger Wert, muss aber
    *         nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_numerus (byte) </b>: Numerus. Beliebiger Wert, muss
    *         aber nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_case (byte) </b>: Fall. Beliebiger Wert, muss aber
    *         nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_determination (byte) </b>: Determination. Beliebiger
    *         Wert, muss aber nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_person (byte) </b>: Person. Beliebiger Wert, muss aber
    *         nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_wordclass (byte) </b>: Wordclass. Beliebiger Wert, muss
    *         aber nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_wordsubclass (byte) </b>: Wordsubclass. Beliebiger
    *         Wert, muss aber nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_conjugation (byte) </b>: Konjunktion. Beliebiger Wert,
    *         muss aber nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_pronoun (byte) </b>: Pronomen. Beliebiger Wert, muss
    *         aber nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_tempus (byte) </b>: Zeit. Beliebiger Wert, muss aber
    *         nicht unbedingt gesetzt sein</li>
    *         <li><b>tr_diathese (byte) </b>: Diathese. Beliebiger Wert, muss
    *         aber nicht unbedingt gesetzt sein</li>
    *         <li><b>type (byte) </b>: Type des Wortes, also FW (1), CW (2)
    *         oder unbekannt (0). Muss aber nicht unbedingt gesetzt sein</li>
    *         <li><b>multiple (int) </b>: Beliebiger Wert, muss aber nicht
    *         unbedingt gesetzt sein</li>
    *         </ul>
    * @throws Exception
    */
   public Vector getWordList(String word, String language)
         throws Exception {
      return (Vector) connection.call(new Message(key, "getWordList", word,
            language)).getArguments()[0];
   }

   /**
    * Gibt alle Eintrï¿½ge aus der Wortliste zurï¿½ck, deren Werte mit denen des
    * Query-DB-Tupel ï¿½bereinstimmen
    * 
    * @param query
    *        ein DB_Tupel, um die Auswahl aus der Wortliste einzugrenzen.
    *        Erlaubte Eintrï¿½ge sind content, language (aber nur mit content
    *        zusammen), tr_genus, tr_numerus, tr_determination, tr_case,
    *        tr_person, tr_wordclass, tr_wordsubclass, tr_conjugation,
    *        tr_pronoun, tr_tempus, tr_diathese, type und multiple. Alle
    *        Eintrï¿½ge sind optional.
    * @return ein Vektor mit DB-Tupel, die diese Einschrï¿½nkungen erfï¿½llen
    * @throws Exception
    */
   public Vector getWordList(DB_Tupel query)
         throws Exception {
      return (Vector) connection.call(new Message(key, "getWordList", query))
            .getArguments()[0];
   }

   /**
    * Speichert eine Liste von Tupel in die Wortliste der Datenbank. Dabei
    * kï¿½nnen auch mehrere Eintrï¿½ge zu einem Content gespeichert werden,
    * vorrausgesetzt die Bestimmung ist unterschiedlich (z.B. bei Bank). ï¿½ber
    * die Funktion setStateSave(), setStateChange() und setStateDelete() von
    * DB-Tupel kann entschieden werden, ob dieses Tupel gespeichert, geï¿½ndert
    * oder gelï¿½scht werden soll. Beim Lï¿½schen eines Tupels wird der Eintrag "id"
    * (int) benï¿½tigt, der beim Auslesen der Wortliste gesetzt wird.
    * 
    * @param list
    *        ein Vector mit DB_Tupeln
    *        <ul>
    *        <li><b>content (String) </b>: Das Wort, muss angegeben werden</li>
    *        <li><b>language (String) </b>: Die Sprache des Wortes, muss
    *        angegeben werden</li>
    *        <li><b>id (int) </b>: Die ID des Tupels, wird zum ï¿½ndern und
    *        lï¿½schen benï¿½tigt</li>
    *        <li><b>tr_genus (byte) </b>: Genus. Beliebiger Wert, muss aber
    *        nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_numerus (byte) </b>: Numerus. Beliebiger Wert, muss aber
    *        nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_case (byte) </b>: Fall. Beliebiger Wert, muss aber nicht
    *        unbedingt gesetzt sein</li>
    *        <li><b>tr_determination (byte) </b>: Determination. Beliebiger
    *        Wert, muss aber nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_person (byte) </b>: Person. Beliebiger Wert, muss aber
    *        nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_wordclass (byte) </b>: Wordclass. Beliebiger Wert, muss
    *        aber nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_wordsubclass (byte) </b>: Wordsubclass. Beliebiger Wert,
    *        muss aber nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_conjugation (byte) </b>: Konjunktion. Beliebiger Wert,
    *        muss aber nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_pronoun (byte) </b>: Pronomen. Beliebiger Wert, muss
    *        aber nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_tempus (byte) </b>: Zeit. Beliebiger Wert, muss aber
    *        nicht unbedingt gesetzt sein</li>
    *        <li><b>tr_diathese (byte) </b>: Diathese. Beliebiger Wert, muss
    *        aber nicht unbedingt gesetzt sein</li>
    *        <li><b>type (byte) </b>: Type des Wortes, also FW (1), CW (2) oder
    *        unbekannt (0). Muss aber nicht unbedingt gesetzt sein</li>
    *        <li><b>multiple (Object) </b>: Beliebiger Wert, muss aber nicht
    *        unbedingt gesetzt sein</li>
    *        </ul>
    * @throws Exception
    */
   public void saveWordList(Vector list)
         throws Exception {
      connection.call(new Message(key, "saveWordList", list));
   }

   public void saveComplexes(Chapter chapter, Vector complexes)
         throws Exception {
      Vector cs = new Vector(complexes.size());
      for (int i = 0; i < complexes.size(); i++) {
         Complex_DB c = ((Complex_DB) complexes.get(i)).cloneComplex();
         cs.add(c);
      }

      Vector answer = (Vector) connection.call(new Message(key,
            "saveComplexes", cs, new Integer(chapter.getDB_ID())))
            .getArguments()[0];

      for (int i = 0; i < Math.min(complexes.size(), answer.size()); i++) {
         Complex_DB a = (Complex_DB) complexes.get(i);
         Complex_DB b = (Complex_DB) answer.get(i);

         a.updateIDs(key, b);
      }

      for (int i = 0; i < complexes.size(); i++) {
         Complex_DB c = (Complex_DB) complexes.get(i);
         if (c.isRemoved())
            complexes.remove(i--);
      }
   }

   public Vector loadComplexes(Chapter chapter)
         throws Exception {
      Message answer = connection.call(new Message(key, "loadComplexes",
            new Integer(chapter.getDB_ID())));
      return (Vector) answer.getArguments()[0];
   }

   public void saveRenominalisations(Renominalisations renominalisations)
         throws Exception {
      Renominalisations answer = (Renominalisations) connection
            .call(new Message(key, "saveRenominalisations", renominalisations,
                  new Integer(renominalisations.getChapter().getDB_ID())))
            .getArguments()[0];
      renominalisations.updateIDs(key, answer);
   }

   /**
    * Erstellt eine Isotopie-Sammlung Ã¼ber alle in diesem Kapitel vorkommenden
    * Isotopien.
    */
   public Renominalisations loadRenominalisations(Chapter chapter)
         throws Exception {
      Message answer = connection.call(new Message(key,
            "loadRenominalisations", new Integer(chapter.getDB_ID())));
      Renominalisations renominalisations = (Renominalisations) answer
            .getArguments()[0];
      IllocutionUnitRoots iur = loadIllocutionUnitRoots(chapter);
      renominalisations.setChapter(key, chapter, iur);
      return renominalisations;
   }
   
   public void saveWordListElement(WordListElement element)
   throws Exception {
	   //key.unlock();
	   WordListElement answer = (WordListElement) connection.call(new Message(key, "saveWordListElement", element))
	            .getArguments()[0];
	    // element.setDB_ID(key, answer.getDB_ID());
	   for(int i = 0; i != answer.getAssignations().size();i++){
		   TR_Assignation assignation = (TR_Assignation)element.getAssignations().get(i);
		   assignation.DB_ID = ((TR_Assignation)answer.getAssignations().get(i)).DB_ID;
	   }
   }
   
   public WordListElement loadWordListElement(String content)
   throws Exception {
	   //key.unlock();
	   WordListElement answer = (WordListElement) connection.call(new Message(key, "loadWordListElement", content))
	            .getArguments()[0];
	   return answer;
   }
   
   public Vector loadWordClasses(Vector contents)
   throws Exception {
	   //key.unlock();
	   System.out.println(contents);
	   Vector answer = (Vector) connection.call(new Message(key, "loadWordClasses", contents))
	            .getArguments()[0];
	   return answer;
   }
   
   public WordListElement loadWordListElement(TR_Assignation assignation)
   throws Exception {
	   //key.unlock();
	   WordListElement answer = (WordListElement) connection.call(new Message(key, "loadWordListElement", assignation))
	            .getArguments()[0];
	   return answer;
   }
   
   public void saveRelation(Relation r)
   throws Exception {
	   //key.unlock();
	   Relation answer = (Relation) connection.call(new Message(key, "saveRelation", r))
	            .getArguments()[0];
	   r.setDB_ID(answer.getDB_ID());
	      
   }
   
   public Vector loadRelations(TR_Assignation assignation)
   throws Exception {
	   //key.unlock();
	   Vector answer = (Vector) connection.call(new Message(key, "loadRelations", assignation))
	            .getArguments()[0];
	   return answer;
   }
   
   public Vector loadRelations(Vector assignations)
   throws Exception {
	   //key.unlock();
	   Vector answer = (Vector) connection.call(new Message(key, "loadRelations", assignations))
	            .getArguments()[0];
	   return answer;
   }

   public boolean isEdited(Chapter c, int category) throws Exception{
	   boolean answer = (Boolean)connection.call(new Message(key,"isEdited",c,category)).getArguments()[0];
	   return answer;
   }
}