/*
 * Erstellt: 23.10.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Book;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Chapter;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Comments;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DB_Element;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DB_Tupel;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Dialog;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Dialogs;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeech;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeeches;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnitRoots;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Isotopes;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Pattern;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.PronounComplex;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Relation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Renominalisations;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Thema_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Word;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WordListElement;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.PronounComplex.PronounComplex_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Relation.Relation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_KeyAcceptor;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.Message;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_SaveException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.chapterloader.ChapterLoader;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.pathselector.PathNode;

/**
 * Die Schnittstelle zur Slang2-Datenbank, mit der M�glichkeit, ganze Kapitel
 * und dazugeh�rige Zusatzinformationen auszulesen und zu speichern. <br>
 * Codebeispiel: <br>
 * 
 * <pre>
 * DBC dbc = new DBC(&quot;kloebb.dyndns.org&quot;);
 * Chapter c = dbc.loadChapter(loader.getChapterID());
 * dbc.close();
 * System.out.println(c);
 * </pre>
 * 
 * @author Volker Kl�bb
 * @see ChapterLoader
 */
public class DBC implements DBC_KeyAcceptor {

	static DBC_Key     key;
	private Connection connection;
	private String     server;

	public static final String VERSION = "3.1.3";

	/**
	 * �ffnet eine Verbindung zu der Datenbank. Sollte am Ende immer mit <code>close()</code>
	 * geschlossen werden.
	 * 
	 * @param server
	 *        Die URL des Servers als String
	 * @throws DBC_ConnectionException
	 * @see #close()
	 */
	public DBC(String server) throws DBC_ConnectionException {
		this.server = server;
		DBC_Key.makeKey(this);
		open();
	}

	/**
	 * �ffnet eine geschlossene Verbindung.
	 * 
	 * @throws DBC_ConnectionException
	 * @see #close()
	 */
	public void open() throws DBC_ConnectionException {
		if (connection == null) {
			Socket socket = null;
			try {
				InetAddress server = InetAddress.getByName(this.server);
				socket = new Socket(server, 9998);
				connection = new Connection(socket);
				sendHello();
			}
			catch (Exception e) {
				// close socket

				try {
					// close connection
					if(connection != null)
						connection.close();
					if(socket != null)
						socket.close();				   
				} catch (IOException e1) {
					// Ignore
				}

				// throw message
				String message = e.getMessage();
				// The "real" exception might be wraped in an InvocationTargetException
				Throwable t = e.getCause();
				if (t!=null)
					message = t.getMessage();			   
				throw new DBC_ConnectionException("Verbindungsaufbau fehlgeschlagen: "+message);
			}
		}
	}

	/**
	 * Send a "hello" message to the server to check if client and server can continue to communicate
	 * @throws Exception
	 */
	private void sendHello() throws Exception {
		connection.call(new Message(key, "hello", DBC.VERSION));
	}

	/**
	 * Beendet eine Verbindung
	 * 
	 * @see #open()
	 */
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				// Ignore
			}
			connection = null;
		}
	}

	/**
	 * L�dt alle B�cher aus der Datenbank.
	 * 
	 * @return ein Vektor mit allen gespeicherten B�chern
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
	 * L�dt ein Buch aus der Datenbank
	 * 
	 * @param id
	 *        Die ID des Buches
	 * @return Das Buch aus der Datenbank mit ID id
	 */
	public Book loadBook(int id)
	throws Exception {
		Message answer = connection.call(new Message(key, "loadBook", new Integer(id)));
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
		Book answer = (Book) connection.call(new Message(key, "saveBook", book)).getArguments()[0];
		book.setDB_ID(key, answer.getDB_ID());
	}
	
	/**
	 * L�dt alle Pattern aus der Datenbank.
	 * 
	 * @return Vektor<Pattern>
	 */
	public Vector<Pattern> loadPatterns() throws Exception {
	    Message answer = connection.call(new Message(key, "loadPatterns"));
	    return (Vector<Pattern>) answer.getArguments()[0];	   
	}

	/**
	 * L�dt alle Pattern mit tdTyp aus der Datenbank.
	 * 
	 * @param String tdType
	 * @return Vektor<Pattern>
	 */
	public Vector<Pattern> loadPatterns(String tdType) throws Exception {
	    Message answer = connection.call(new Message(key, "loadPatterns", tdType));
	    return (Vector<Pattern>) answer.getArguments()[0];
	}

	/**
	 * Speichert ein Pattern in der Datenbank
	 * 
	 * @param Pattern pattern
	 */
	public void savePattern(Pattern pattern) throws Exception {
	    Message answer = connection.call(new Message(key, "savePattern", pattern));
	    Pattern pat = (Pattern) answer.getArguments()[0];
	    pattern.setDB_ID(pat.getDB_ID());
	}

	/**
	 * L�dt ein Kapitel aus der Datenbank.
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
	 * L�scht ein Kapitel aus der Datenbank
	 * 
	 * @see #deleteChapter(int)
	 */
	public void deleteChapter(int id)
	throws Exception {
		Message answer = connection.call(new Message(key, "deleteChapter", new Integer(id)));
	}

	/**
	 * L�dt alle Direkten Reden aus der Datenbank, die zu diesem Kapitel
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
	 * L�dt alle Dialoge des Kapitels aus der Datenbank.
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
	 * L�dt eine Unterart der �u�erungseinheit, der Sememegruppen, Semantische
	 * Einheiten und isolierte Funktionsw�rter usw. untergeordnet sind.
	 * 
	 * @param chapter
	 *        Das Kapitel, dessen Daten geladen werden sollen
	 * @return Ein Vektor mit allen Wurzeln zu den �u�erungseinheiten. Wurden zu
	 *         einer �u�erungseinheit keine Sememegruppen o.�. angegeben, wird
	 *         eine leere Wurzel erzeugt.
	 * @see #saveIllocutionUnitRoots(IllocutionUnitRoots)
	 */
	public IllocutionUnitRoots loadIllocutionUnitRoots(Chapter chapter)
	throws Exception {
		Message answer = connection.call(new Message(key,"loadIllocutionUnitRoots", new Integer(chapter.getDB_ID())));
		IllocutionUnitRoots roots = (IllocutionUnitRoots) answer.getArguments()[0];
		roots.setChapter(key, chapter);
		roots.fillCaches(key);
		return roots;
	}

	/**
	 * Speichert die Wurzeln der �u�erungseinheiten in der Datenbank
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
	 * Alle Funktionsw�rter, ohne Zusammenhang zu dem Text. Deswegen nur Strings
	 */
	public Vector loadFunctionWords()
	throws Exception {
		Vector answer = (Vector) connection.call(new Message(key,
		"loadFunctionWords")).getArguments()[0];
		return answer;
	}

	/**
	 * Alle Kategorien der Funktionsw�rter
	 */
	public Vector loadFunctionWordsCategories()
	throws Exception {
		Vector answer = (Vector) connection.call(new Message(key,
		"loadFunctionWordsCategories")).getArguments()[0];
		return answer;
	}

	/**
	 * Alle semantisch konstitutiven W�rter ohne Zusammenhang zum Text. Deswegen
	 * nur Strings.
	 */
	public Vector loadConstitutiveWords()
	throws Exception {
		Vector answer = (Vector) connection.call(new Message(key,
		"loadConstitutiveWords")).getArguments()[0];
		return answer;
	}

	/**
	 * Die extrem wichtigen, �ber alles ben�tigten Pfade
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
	 * �berpr�ft, ob zu diesem Wort in einem Kapitel ein Funktionswort
	 * gespeichert wurde. Dabei werden keine Teilw�rter beachtet. Bei dem Wort
	 * ist nur der Content wichtig, die Position im Kapitel spielt keine Rolle.
	 * 
	 * @param word
	 *        Das zu �berpr�fende Wort
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
	 * �berpr�ft, ob zu diesem Wort in einem Kapitel ein Konstitutives Wort
	 * gespeichert wurde. Dabei werden keine Teilw�rter beachtet. Bei dem Wort
	 * ist nur der Content wichtig, die Position im Kapitel spielt keine Rolle.
	 * 
	 * @param word
	 *        Das zu �berpr�fende Wort
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
	 * Gibt alle CWs zur�ck, die zu dieser Sprache gespeichert wurden
	 * 
	 * @param language
	 *        Die Sprache, z.B. DE oder EN
	 * @return Ein Vektor mit DB_Tupeln, welche das CW plus Zusatzinformationen
	 *         speichern. Das Tupel besitzt folgende Eintr�ge:
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
	 * Gibt alle FWs zur�ck, die zu dieser Sprache gespeichert wurden
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
	 * Gibt eine Liste mit Konstitutiven W�rtern zur�ck, die entweder aus dem
	 * �bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
	 * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
	 * Kapitel durchsucht. <br>
	 * Bei dem Wort "K�nigshaus" bekommt man zum Beispiel folgendes Ergebnis
	 * (sofern die CWs zu diesem Wort schon gespeichert wurden) [K�nig, haus].
	 * Die Gro�- und Kleinschreibung wird ignoriert.
	 * 
	 * @param word
	 *        Das Wort, auf welchem die gefundenen CWs aufbauen.
	 * @return Ein Vektor mit DB_Tupeln, welche das CW plus Zusatzinformationen
	 *         speichern. Das Tupel besitzt folgende Eintr�ge:
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
	 * Gibt eine Liste mit Konstitutiven W�rtern zur�ck, die entweder aus dem
	 * �bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
	 * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
	 * Kapitel durchsucht. <br>
	 * Bei dem Wort "K�nigshaus" bekommt man zum Beispiel folgendes Ergebnis
	 * (sofern die CWs zu diesem Wort schon gespeichert wurden) [K�nig, haus].
	 * Die Gro�- und Kleinschreibung wird ignoriert.
	 * 
	 * @param content
	 *        Der Inhalt des zu suchenden Wortes
	 * @param language
	 *        Die Sprache des Wortes
	 * @return Ein Vektor mit DB_Tupeln, welche das CW plus Zusatzinformationen
	 *         speichern. Das Tupel besitzt folgende Eintr�ge:
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
	public Vector<DB_Tupel> getConstitutiveWords(String content, String language)
	throws Exception {
		Message answer = connection.call(new Message(key, "getConstitutiveWords",
				content, language));
		return (Vector<DB_Tupel>) answer.getArguments()[0];
	}

	/**
	 * Gibt eine Liste mit Funktionsw�rtern zur�ck, die entweder aus dem
	 * �bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
	 * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
	 * Kapitel durchsucht. <br>
	 * Bei dem Wort "K�nigshaus" bekommt man zum Beispiel folgendes Ergebnis
	 * (sofern die FWs zu diesem Wort schon gespeichert wurden) [s]. Die Gro�-
	 * und Kleinschreibung wird ignoriert.
	 * 
	 * @param word
	 *        Das Wort, auf welchem die gefundenen FWs aufbauen.
	 * @return Ein Vektor mit DB_Tupeln, welche das FW plus Zusatzinformationen
	 *         speichern. Das Tupel besitzt folgende Eintr�ge:
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
	 * Gibt eine Liste mit Funktionsw�rtern zur�ck, die entweder aus dem
	 * �bergebenen Wort bestehen oder ein Teilwort von diesem sind und aus der
	 * gleichen Sprache kommen. Dabei werden alle in der Datenbank gespeicherten
	 * Kapitel durchsucht. <br>
	 * Bei dem Wort "K�nigshaus" bekommt man zum Beispiel folgendes Ergebnis
	 * (sofern die FWs zu diesem Wort schon gespeichert wurden) [s]. Die Gro�-
	 * und Kleinschreibung wird ignoriert.
	 * 
	 * @param content
	 *        Der Inhalt des zu suchenden Wortes
	 * @param language
	 *        Die Sprache des Wortes
	 * @return Ein Vektor mit DB_Tupeln, welche das FW plus Zusatzinformationen
	 *         speichern. Das Tupel besitzt folgende Eintr�ge:
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
	 * alten Eintr�ge zu dem Kapitel gel�scht. Es m�ssen also alle Themas von
	 * diesem Kapitel �bergeben werden.
	 * 
	 * @param chapter
	 *        Das Kapitel, zu dem die Themas geh�ren
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
	 * L�dt alle Themas von einem Kapitel und gibt sie in einem Vektor zur�ck.
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
	 * Erstellt eine Isotopie-Sammlung �ber alle in diesem Kapitel vorkommenden
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
	 *        Ein Vektor, der die Hierachie repr�sentiert. Der n�here Aufbau des
	 *        Vektors ist egal, er wird serialisiert in der Datenbank
	 *        gespeichert. Die maximale Gr��e des serialisierten vektors darf
	 *        65535 Zeichen nicht �berschreiten.
	 */
	public void saveIsotopeHierachy(Chapter chapter, Vector hierachy)
	throws Exception {
		connection.call(new Message(key, "saveIsotopeHierachy", new Integer(
				chapter.getDB_ID()), hierachy));
	}

	/**
	 * L�dt die Hierachien von Isotopien eines Kapitels.
	 * 
	 * @param chapter
	 *        Das Kapitel, dessen Isotopien-Hierachie geladen werden soll.
	 * @return Ein Vektor, der die Hierachie repr�sentiert.
	 */
	public Vector loadIsotopeHierachy(Chapter chapter)
	throws Exception {
		return (Vector) connection.call(new Message(key, "loadIsotopeHierachy",
				new Integer(chapter.getDB_ID()))).getArguments()[0];
	}

	/**
	 * Alle in der Datenbank gespeicherten Sprachen
	 * 
	 * @return Ein Vektor mit L�nderk�rzeln wie "DE" oder "EN"
	 */
	public Vector getLanguages()
	throws Exception {
		return (Vector) connection.call(new Message(key, "getLanguages"))
		.getArguments()[0];
	}

	/**
	 * L�dt alle Kommentare, die zu �u�erungseinheiten eines Kapitels
	 * abgespeichert wurden.
	 * 
	 * @param comments
	 *        Die Kommentarsammlung, zu der die neuen Kommentare hinzugef�gt
	 *        werden sollen
	 * @param chapter
	 *        Das Kapitel, in dem die �u�erungseinheiten stehen
	 * @throws Exception
	 */
	public void loadIllocutionUnitComments(Comments comments, Chapter chapter)
	throws Exception {
		comments.add(loadComments(chapter, Comments.CLASS_CODE_ILLOCUTION_UNIT));
	}

	/**
	 * L�dt alle Kommentare, die zu direkten Reden eines Kapitels abgespeichert
	 * wurden.
	 * 
	 * @param comments
	 *        Die Kommentarsammlung, zu der die neuen Kommentare hinzugef�gt
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
	 * L�dt alle Kommentare, die zu Dialogen eines Kapitels abgespeichert wurden.
	 * Dazu geh�ren auch Vor- und Nachfeld der Dialoge.
	 * 
	 * @param comments
	 *        Die Kommentarsammlung, zu der die neuen Kommentare hinzugef�gt
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
	 * Gibt alle Eintr�ge aus der Wortliste zur�ck, die sich auf das Wort
	 * beziehen
	 * 
	 * @param word
	 *        Das Wort (Gro�/Kleinschreibung wird ignoriert)
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
	 * @deprecated Ohne Funktion! Wortliste ist nun �ber WordListElements erreichbar.
	 */
	public Vector getWordList(String word, String language) throws Exception
	{
//		return (Vector) connection.call(new Message(key, "getWordList", word,
//		language)).getArguments()[0];
		return null;
	}

	/**
	 * Gibt alle Eintr�ge aus der Wortliste zur�ck, deren Werte mit denen des
	 * Query-DB-Tupel �bereinstimmen
	 * 
	 * @param query
	 *        ein DB_Tupel, um die Auswahl aus der Wortliste einzugrenzen.
	 *        Erlaubte Eintr�ge sind content, language (aber nur mit content
	 *        zusammen), tr_genus, tr_numerus, tr_determination, tr_case,
	 *        tr_person, tr_wordclass, tr_wordsubclass, tr_conjugation,
	 *        tr_pronoun, tr_tempus, tr_diathese, type und multiple. Alle
	 *        Eintr�ge sind optional.
	 * @return ein Vektor mit DB-Tupel, die diese Einschr�nkungen erf�llen
	 * @throws Exception
	 * @deprecated Ohne Funktion! Wortliste ist nun �ber WordListElements erreichbar.
	 */
	public Vector getWordList(DB_Tupel query) throws Exception
	{
//		return (Vector) connection.call(new Message(key, "getWordList", query))
//		.getArguments()[0];
		return null;
	}

	/**
	 * Speichert eine Liste von Tupel in die Wortliste der Datenbank. Dabei
	 * k�nnen auch mehrere Eintr�ge zu einem Content gespeichert werden,
	 * vorrausgesetzt die Bestimmung ist unterschiedlich (z.B. bei Bank). �ber
	 * die Funktion setStateSave(), setStateChange() und setStateDelete() von
	 * DB-Tupel kann entschieden werden, ob dieses Tupel gespeichert, ge�ndert
	 * oder gel�scht werden soll. Beim L�schen eines Tupels wird der Eintrag "id"
	 * (int) ben�tigt, der beim Auslesen der Wortliste gesetzt wird.
	 * 
	 * @param list
	 *        ein Vector mit DB_Tupeln
	 *        <ul>
	 *        <li><b>content (String) </b>: Das Wort, muss angegeben werden</li>
	 *        <li><b>language (String) </b>: Die Sprache des Wortes, muss
	 *        angegeben werden</li>
	 *        <li><b>id (int) </b>: Die ID des Tupels, wird zum �ndern und
	 *        l�schen ben�tigt</li>
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
	 * @deprecated Ohne Funktion! Wortliste ist nun �ber WordListElements erreichbar.
	 */
	public void saveWordList(Vector list)
	throws Exception {
		connection.call(new Message(key, "saveWordList", list));
	}

	
	/**
	 * Save all of <code>complexes</code> that are out of sync with DB
	 * @param complexes
	 * @throws Exception
	 */
	public void saveComplexes(Collection<? extends PronounComplex> complexes) throws Exception
	{	
		// get all complexes that have changed as DB Version
		ArrayList<PronounComplex> complexes_oos = new ArrayList<PronounComplex>();
		ArrayList<PronounComplex_DB> complexes_db = new ArrayList<PronounComplex_DB>();
		for (PronounComplex pronounComplex : complexes) {
			if( pronounComplex.isOutOfSync() ) {
				complexes_oos.add(pronounComplex);
				complexes_db.add(pronounComplex.new PronounComplex_DB(key));
			//TODO: check for unsaved references and ...?
			}
		}
		
		// save
		ArrayList<PronounComplex_DB> answer = (ArrayList<PronounComplex_DB>) connection.call(
				new Message(key, "saveComplexes", complexes_db)).getArguments()[0];
		
		// update
		if(answer.size() != complexes_db.size()) {
			for (PronounComplex_DB pronounComplex_DB : complexes_db) {
				pronounComplex_DB.changeState(key, DB_Element.ERROR);
			}
			throw new DBC_SaveException("Complexes could (probably) be saved but server did not return all of them");
		}
		else
			for (int i = 0; i < answer.size(); i++) {
				complexes_oos.get(i).setDB_ID(key, answer.toArray(new PronounComplex_DB[0])[i].getDB_ID());
				complexes_oos.get(i).changeState(key, DB_Element.NORMAL);
			}
	}

	/**
	 * @param roots
	 * @return
	 * @throws Exception
	 */
	public Vector<PronounComplex> loadComplexes(IllocutionUnitRoots roots) throws Exception
	{
		Message answer = connection.call(new Message(key, "loadComplexes", new Integer(roots.getChapter().getDB_ID())));
		
		Vector<PronounComplex> complexes = new Vector<PronounComplex>();
		
		// materialize complexes
		for (PronounComplex_DB pronounComplex : (Vector<PronounComplex_DB>) answer.getArguments()[0]) {
			try {
				complexes.add(new PronounComplex(key, roots, pronounComplex));
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		return complexes;
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
	 * Erstellt eine Isotopie-Sammlung über alle in diesem Kapitel vorkommenden
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

	public void saveWordListElements(WordListElement ... element) throws Exception {
		//key.unlock();
		WordListElement[] answer = (WordListElement[]) connection.call(new Message(key, "saveWordListElement", (Object[]) element))
		.getArguments()[0];

		for(int i = 0; i != answer.length; i++){
			element[i].updateIDs(key, answer[i]);
			if(element[i].getAssignation() != null)
				element[i].getAssignation().updateIDs(key, answer[i].getAssignation());
		}
	}


	/**
	 * Load from DB all WordListElements with word = <code>content</code>
	 * @param content
	 * @return all WordListElements that exist in the DB with word = <code>content</code>
	 * @throws Exception
	 */
	public WordListElement[] loadWordListElement(String content) throws Exception {
		//key.unlock();
		return (WordListElement[]) connection.call(new Message(key, "loadWordListElement", content)) .getArguments()[0];
	}
	
	/**
	 * Load from DB all WordListElements with word = <code>content</code> and language = <code>language</code>
	 * @param content
	 * @return all WordListElements that exist in the DB with word = <code>content</code> and language = <code>language</code>
	 * @throws Exception
	 */
	public WordListElement[] loadWordListElement(String content, String language) throws Exception {
		//key.unlock();
		return (WordListElement[]) connection.call(new Message(key, "loadWordListElement", content, language)) .getArguments()[0];
	}
	
	/**
	 * Load from DB the WordListElement with DB_ID = id 
	 * @param id the id of the WordListElement to load
	 * @return a WordListElement with DB_ID = id
	 * @throws NullPointerException
	 * @throws Exception
	 */
	private WordListElement loadWordListElement(int id) throws NullPointerException, Exception {
		return (WordListElement) connection.call(new Message(key, "loadWordListElement", new Integer(id))).getArguments()[0];
	}

	public Vector loadWordClasses(Vector contents)
	throws Exception {
		//key.unlock();
		System.out.println(contents);
		Vector answer = (Vector) connection.call(new Message(key, "loadWordClasses", contents))
		.getArguments()[0];
		return answer;
	}

//	public WordListElement loadWordListElement(TR_Assignation assignation)
//	throws Exception {
//		//key.unlock();
//		WordListElement answer = (WordListElement) connection.call(new Message(key, "loadWordListElement", assignation))
//		.getArguments()[0];
//		return answer;
//	}

	public void saveRelations(Relation ... relations) throws Exception {
		// get all complexes that have changed as DB Version
		ArrayList<Relation> relations_oos = new ArrayList<Relation>();
		ArrayList<Relation_DB> relations_db = new ArrayList<Relation_DB>();
		for (Relation relation : relations) {
			if( relation.isOutOfSync() ) {
				relations_oos.add(relation);
				relations_db.add(relation.new Relation_DB(key));
			}
			//TODO: check for unsaved references and ...?
		}
		//key.unlock();
		Relation[] answer = (Relation[]) connection.call(new Message(key, "saveRelation", relations_db.toArray())).getArguments()[0];
		
		// update
		if(answer.length != relations_db.size()) {
			for (Relation_DB relation : relations_db) {
				relation.changeState(key, DB_Element.ERROR);
			}
			throw new DBC_SaveException("Complexes could (probably) be saved but server did not return all of them");
		}
		else
			for (int i = 0; i < answer.length; i++) {
				relations_oos.get(i).setDB_ID(key, answer[i].getDB_ID());
				relations_oos.get(i).changeState(key, DB_Element.NORMAL);
			}
	}
	
	/**
	 * Delegates to {@link loadRelations(WordListElement wle, Collection<WordListElement> possibleTargets)} with <code>possibleTargets</code> as an empty Collection.
	 * @param wle
	 * @return
	 * @throws Exception
	 */
	public Relation[] loadRelations(WordListElement wle) throws Exception {
		return loadRelations(wle, new ArrayList<WordListElement>());
	}

	/**
	 * Load all relations with <code>wle</code> as origin.
	 * Target WordListElement is an element of <code>possibleTargets</code> if one has target's DB_ID or a newly loaded one.
	 * @param wle the origin of all relations
	 * @param possibleTargets a collection of WordListElements that contain possible targets for the realtions
	 * @return all relations with <code>wle</code> as origin
	 * @throws Exception
	 */
	public Relation[] loadRelations(WordListElement wle, Collection<WordListElement> possibleTargets)
	throws Exception {
		//key.unlock();
		Relation_DB[] answer = (Relation_DB[]) connection.call(new Message(key, "loadRelations", wle)).getArguments()[0];
		Relation[] ret = new Relation[answer.length];
		int i=0;
		for (Relation_DB relation : answer) {
			for (WordListElement possibleTarget : possibleTargets) {
				if(relation.target_cw_id == possibleTarget.getDB_ID()) {
					ret[i] = new Relation(wle, possibleTarget, relation.getType());
					break;
				}
			}
			if(ret[i] == null)
				ret[i] = new Relation(wle, loadWordListElement(relation.target_cw_id), relation.getType());
			++i;
		}
		return ret;
	}

//	public Vector loadRelations(Vector assignations)
//	throws Exception {
//		//key.unlock();
//		Vector answer = (Vector) connection.call(new Message(key, "loadRelations", assignations))
//		.getArguments()[0];
//		return answer;
//	}

	public boolean isEdited(Chapter c, int category) throws Exception{
		boolean answer = (Boolean)connection.call(new Message(key,"isEdited",c,category)).getArguments()[0];
		return answer;
	}

	public void setKey(DBC_Key key) {
	    DBC.key = key;
	}
}