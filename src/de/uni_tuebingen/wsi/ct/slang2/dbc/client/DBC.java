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

import com.mysql.jdbc.MysqlDataTruncation;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Book;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Chapter;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Comments;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DB_Element;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DB_Tupel;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Dialog;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogComment;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogD_Themat;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogFaces;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogISignal;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogSpeaker;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogSpeakerChange;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogTarget;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Dialogs;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeech;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeeches;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnitRoots;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Isotopes;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IU_Comment;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism1;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism2;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Pattern;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.PronounComplex;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Relation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Renominalisations;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Thema_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Word;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WordListElement;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WorkingTranslation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism1.LiteraryCriticism1_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism2.LiteraryCriticism2_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.PronounComplex.PronounComplex_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Relation.Relation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WorkingTranslation.WorkingTranslation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_KeyAcceptor;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.Message;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_SaveException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.chapterloader.ChapterLoader;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.pathselector.PathNode;

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
public class DBC implements DBC_KeyAcceptor {

	static DBC_Key     key;
	private Connection connection;
	private String     server;

	public static final String VERSION = "3.1.3";

	/**
	 * Öffnet eine Verbindung zu der Datenbank. Sollte am Ende immer mit <code>close()</code>
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

	public DBC_Key getKey() {
		return key;
	}
	/**
	 * Öffnet eine geschlossene Verbindung.
	 * 
	 * @throws DBC_ConnectionException
	 * @see #close()
	 */
	public void open() throws DBC_ConnectionException {
		if (connection == null) {
			Socket socket = null;
			try {
				InetAddress server = InetAddress.getByName(this.server);
				socket = new Socket(server, 9999);
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
	 * Lädt alle Bücher aus der Datenbank.
	 * 
	 * @return ein Vektor mit allen gespeicherten Büchern
	 * 
	 * @see Book
	 * @see DBC#loadBook(int)
	 */
	public Vector<Book> loadBooks()
	throws Exception {
		Message answer = connection.call(new Message(key, "loadBooks"));
		return (Vector<Book>) answer.getArguments()[0];
	}

	/**
	 * Lädt ein Buch aus der Datenbank
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
	public void saveBook(DBC_Key key, Book book) throws Exception {
		try {
			key.unlock();
			Book answer = (Book) connection.call(new Message(key, "saveBook", book)).getArguments()[0];
			book.setDB_ID(key, answer.getDB_ID());
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	/**
	 * Lädt alle Pattern aus der Datenbank.
	 * 
	 * @return Vektor<Pattern>
	 */
	public Vector<Pattern> loadPatterns() throws Exception {
	    Message answer = connection.call(new Message(key, "loadPatterns"));
	    return (Vector<Pattern>) answer.getArguments()[0];	   
	}

	/**
	 * Lädt alle Pattern mit tdTyp aus der Datenbank.
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
		try {
		    Message answer = connection.call(new Message(key, "savePattern", pattern));
		    Pattern pat = (Pattern) answer.getArguments()[0];
		    pattern.setDB_ID(pat.getDB_ID());
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
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
	public void saveChapter(DBC_Key key, Chapter chapter) throws Exception {
		try {
			key.unlock();
			Chapter answer = (Chapter) connection.call(new Message(key,	"saveChapter", chapter)).getArguments()[0];
			chapter.updateIDs(key, answer);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	/**
	 * Loescht ein Kapitel aus der Datenbank
	 * 
	 * @see #deleteChapter(int)
	 */
	public void deleteChapter(int id)
	throws Exception {
		Message answer = connection.call(new Message(key, "deleteChapter", new Integer(id)));
	}
	
	/**
	 *  delete wle and the coressponding assignation and set the assignation in cw to null
	 */
	public void deleteWLECW(int wleID, int assigID)
	throws Exception {
		Message answer = connection.call(new Message(key, "deleteWLECW", new Integer(wleID), new Integer(assigID)));
	}
	
	/**
	 *  delete wle and the coressponding assignation and set the assignation in cw to null
	 */
	public void deleteWLEFW(int wleID, int assigID)
	throws Exception {
		Message answer = connection.call(new Message(key, "deleteWLEFW", new Integer(wleID), new Integer(assigID)));
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
		try {
			DirectSpeeches answer = (DirectSpeeches) connection.call(new Message(key,
					"saveDirectSpeeches", new Integer(chapter.getDB_ID()),
					newdirectSpeeches, olddirectSpeeches)).getArguments()[0];
			//newdirectSpeeches.updateIDs(key, answer);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	/*public void saveDialogs(Chapter chapter, Dialogs dialogs)
	throws Exception {
		Dialogs answer = (Dialogs) connection.call(new Message(key,
				"saveDialogs", new Integer(chapter.getDB_ID()), dialogs))
				.getArguments()[0];
		dialogs.updateIDs(key, answer);
	}*/
	
	public void saveDialogs(Chapter chapter, Dialogs oldDialogs, Dialogs newDialogs )
	throws Exception {
		try {
			Dialogs answer = (Dialogs) connection.call(new Message(key,
					"saveDialogs", new Integer(chapter.getDB_ID()), oldDialogs, newDialogs)).getArguments()[0];
			newDialogs.updateIDs(key, answer);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
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

	public void saveSpeakers(Chapter chapter, ArrayList<DialogSpeaker> speakers)
	throws Exception {
		try {
			ArrayList<DialogSpeaker> answer = (ArrayList<DialogSpeaker>) connection.call(new Message(key,
					"saveSpeakers", new Integer(chapter.getDB_ID()), speakers)).getArguments()[0];
			//speaker.updateIDs(key, answer);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	public void saveSpeakerChanges(Chapter chapter, ArrayList<DialogSpeakerChange> changes) throws Exception {
		try {
			connection.call(new Message(key, "saveSpeakerChanges", new Integer(chapter.getDB_ID()), changes));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	public void saveD_Themat(Chapter chapter, ArrayList<DialogD_Themat> themats) throws Exception {
		try {
			connection.call(new Message(key, "saveD_Themat", new Integer(chapter.getDB_ID()), themats));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	public void saveFaces(Chapter chapter, ArrayList<DialogFaces> faces) throws Exception {
		try {
			connection.call(new Message(key, "saveFaces", new Integer(chapter.getDB_ID()), faces));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	public void saveTargets(Chapter chapter, ArrayList<DialogTarget> targets) throws Exception {
		try {
			connection.call(new Message(key, "saveTargets", new Integer(chapter.getDB_ID()), targets));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	public void saveISignals(Chapter chapter, ArrayList<DialogISignal> signals) throws Exception {
		try {
			connection.call(new Message(key, "saveISignals", new Integer(chapter.getDB_ID()), signals));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	public void saveComments(Chapter chapter, ArrayList<DialogComment> comments) throws Exception {
		try {
			connection.call(new Message(key, "saveComments", new Integer(chapter.getDB_ID()), comments));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}
	
	public ArrayList<DialogSpeaker> loadSpeakers(Chapter chapter, String typ) throws Exception {
		Message answer = connection.call(new Message(key, "loadSpeakers", new Integer(chapter.getDB_ID()), typ));
		ArrayList<DialogSpeaker> speakers = (ArrayList<DialogSpeaker>) answer.getArguments()[0];
		for (int i=0; i != speakers.size(); ++i)
		{
			DialogSpeaker speaker = speakers.get(i);
			speaker.setChapter(key, chapter);
		}
		return speakers;
	}
	
	public synchronized ArrayList<DialogSpeakerChange> loadSpeakerChanges (Chapter chapter, String typ) throws Exception {
		Message answer = connection.call(new Message(key, "loadSpeakerChanges", new Integer(chapter.getDB_ID()), typ));
		ArrayList<DialogSpeakerChange> changes = (ArrayList<DialogSpeakerChange>) answer.getArguments()[0];
		for (int i=0; i != changes.size(); ++i)
		{
			DialogSpeakerChange change = changes.get(i);
			change.setChapter(key, chapter);
		}
		return changes;
	}
	
	public synchronized ArrayList<DialogD_Themat> loadD_Themat (Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key, "loadD_Themat", new Integer(chapter.getDB_ID())));
		ArrayList<DialogD_Themat> d_themas = (ArrayList<DialogD_Themat>) answer.getArguments()[0];
		for (int i=0; i != d_themas.size(); ++i)
		{
			DialogD_Themat d_thema = d_themas.get(i);
			d_thema.setChapter(key, chapter);
		}
		return d_themas;
	}
	
	public synchronized ArrayList<DialogFaces> loadFaces (Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key, "loadFaces", new Integer(chapter.getDB_ID())));
		ArrayList<DialogFaces> faces = (ArrayList<DialogFaces>) answer.getArguments()[0];
		for (int i=0; i != faces.size(); ++i)
		{
			DialogFaces face = faces.get(i);
			face.setChapter(key, chapter);
		}
		return faces;
	}
	
	public synchronized ArrayList<DialogTarget> loadTargets (Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key, "loadTargets", new Integer(chapter.getDB_ID())));
		ArrayList<DialogTarget> targets = (ArrayList<DialogTarget>) answer.getArguments()[0];
		for (int i=0; i != targets.size(); ++i)
		{
			DialogTarget target = targets.get(i);
			target.setChapter(key, chapter);
		}
		return targets;
	}
	
	public synchronized ArrayList<DialogISignal> loadISignals (Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key, "loadISignals", new Integer(chapter.getDB_ID())));
		ArrayList<DialogISignal> signals = (ArrayList<DialogISignal>) answer.getArguments()[0];
		for (int i=0; i != signals.size(); ++i)
		{
			DialogISignal signal = signals.get(i);
			signal.setChapter(key, chapter);
		}
		return signals;
	}
	
	public synchronized ArrayList<DialogComment> loadComments (Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key, "loadComments", new Integer(chapter.getDB_ID())));
		ArrayList<DialogComment> comments = (ArrayList<DialogComment>) answer.getArguments()[0];
		for (int i=0; i != comments.size(); ++i)
		{
			DialogComment comment = comments.get(i);
			comment.setChapter(key, chapter);
		}
		return comments;
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
	public IllocutionUnitRoots loadIllocutionUnitRoots(Chapter chapter)	throws Exception {
		Message answer = connection.call(new Message(key,"loadIllocutionUnitRoots", new Integer(chapter.getDB_ID())));
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
	public void saveIllocutionUnitRoots(IllocutionUnitRoots iurs) throws Exception {
		try {
			IllocutionUnitRoots answer = (IllocutionUnitRoots) connection.call(new Message(key, "saveIllocutionUnitRoots", new Integer(iurs
					.getChapter().getDB_ID()), iurs)).getArguments()[0];
			iurs.updateIDs(key, answer);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	/**
	 * Alle Funktionswï¿½rter, ohne Zusammenhang zu dem Text. Deswegen nur Strings
	 */
	public Vector loadFunctionWords() throws Exception {
		Vector answer = (Vector) connection.call(new Message(key,
		"loadFunctionWords")).getArguments()[0];
		return answer;
	}

	/**
	 * Alle Kategorien der Funktionswörter
	 */
	public Vector loadFunctionWordsCategories()	throws Exception {
		Vector answer = (Vector) connection.call(new Message(key,
		"loadFunctionWordsCategories")).getArguments()[0];
		return answer;
	}

	/**
	 * Alle semantisch konstitutiven Wörter ohne Zusammenhang zum Text. Deswegen
	 * nur Strings.
	 */
	public Vector loadConstitutiveWords() throws Exception {
		Vector answer = (Vector) connection.call(new Message(key, "loadConstitutiveWords")).getArguments()[0];
		return answer;
	}

	/**
	 * Die extrem wichtigen, über alles benötigten Pfade
	 */
	public PathNode getPaths() throws Exception {
		return (PathNode) connection.call(new Message(key, "getPaths")).getArguments()[0];
	}

	/**
	 * Die Numerus-Pfade
	 */
	public PathNode getNumerusPaths() throws Exception {
	    return (PathNode) connection.call(new Message(key, "getNumerusPaths")).getArguments()[0];
	}

	/**
	 * überprüft, ob zu diesem Wort in einem Kapitel ein Funktionswort
	 * gespeichert wurde. Dabei werden keine Teilwörter beachtet. Bei dem Wort
	 * ist nur der Content wichtig, die Position im Kapitel spielt keine Rolle.
	 * 
	 * @param word
	 *        Das zu ï¿½berprï¿½fende Wort
	 * @return true, falls ein Funktionswort zu diesem Wort angelegt wurde.
	 */
	public boolean existsFunctionWord(Word word) throws Exception {
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
	public boolean existsConstitutiveWord(Word word) throws Exception {
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
	public Vector getAllConstitutiveWords(String language) throws Exception {
		Message answer = connection.call(new Message(key, "getAllConstitutiveWords", language));
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
	public Vector getAllFunctionWords(String language) throws Exception {
		Message answer = connection.call(new Message(key, "getAllFunctionWords", language));
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
	public Vector getConstitutiveWords(Word word) throws Exception {
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
	public Vector<DB_Tupel> getConstitutiveWords(String content, String language) throws Exception {
		Message answer = connection.call(new Message(key, "getConstitutiveWords",
				content, language));
		return (Vector<DB_Tupel>) answer.getArguments()[0];
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
	public Vector getFunctionWords(Word word) throws Exception {
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
	public Vector getFunctionWords(String content, String language) throws Exception {
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
	public void saveThemas(Chapter chapter, Vector themas) throws Exception {
		try {
			connection.call(new Message(key, "saveThemas", new Integer(chapter.getDB_ID()), themas));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	/**
	 * Lï¿½dt alle Themas von einem Kapitel und gibt sie in einem Vektor zurï¿½ck.
	 * 
	 * @param chapter
	 *        Das Kapitel, vondem die Themas geladen werden sollen.
	 * @throws Exception
	 */
	public Vector loadThemas(Chapter chapter) throws Exception {
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
	public Isotopes loadIsotopes(Chapter chapter) throws Exception {
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
	public void saveIsotopes(Isotopes isotopes) throws Exception {
		try {
			Isotopes answer = (Isotopes) connection.call(new Message(key,
					"saveIsotopes", isotopes, new Integer(isotopes.getChapter()
							.getDB_ID()))).getArguments()[0];
			isotopes.updateIDs(key, answer);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
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
	public void saveIsotopeHierachy(Chapter chapter, Vector hierachy) throws Exception {
		try {
			connection.call(new Message(key, "saveIsotopeHierachy", new Integer(chapter.getDB_ID()), hierachy));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	/**
	 * Lï¿½dt die Hierachien von Isotopien eines Kapitels.
	 * 
	 * @param chapter
	 *        Das Kapitel, dessen Isotopien-Hierachie geladen werden soll.
	 * @return Ein Vektor, der die Hierachie reprï¿½sentiert.
	 */
	public Vector loadIsotopeHierachy(Chapter chapter) throws Exception {
		return (Vector) connection.call(new Message(key, "loadIsotopeHierachy",
				new Integer(chapter.getDB_ID()))).getArguments()[0];
	}

	/**
	 * Alle in der Datenbank gespeicherten Sprachen
	 * 
	 * @return Ein Vektor mit Lï¿½nderkï¿½rzeln wie "DE" oder "EN"
	 */
	public Vector getLanguages() throws Exception {
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
	public void loadIllocutionUnitComments(Comments comments, Chapter chapter) throws Exception {
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
	public void loadDirectSpeechComments(Comments comments, Chapter chapter) throws Exception {
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
	public void loadDialogComments(Comments comments, Chapter chapter) throws Exception {
		comments.add(loadComments(chapter, Comments.CLASS_CODE_DIALOG));
		comments.add(loadComments(chapter, Comments.CLASS_CODE_DIALOG_COSMOLOGIES));
		//comments.add(loadComments(chapter, Comments.CLASS_CODE_DIALOG_FOLLOWUP));
		//comments.add(loadComments(chapter, Comments.CLASS_CODE_DIALOG_RUNUP));
	}

	private Comments loadComments(Chapter chapter, int ownerClassCode) throws Exception {
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
	public void saveComments(Comments comments)	throws Exception {
		try {
			connection.call(new Message(key, "saveComments", comments));
			comments.resetChange(key);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	/**
	 * Save all of <code>complexes</code> that are out of sync with DB
	 * @param complexes
	 * @throws Exception
	 */
	public void saveComplexes(Collection<? extends PronounComplex> complexes) throws Exception {	
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
		ArrayList<PronounComplex_DB> answer;
		// save
		try {
			answer = (ArrayList<PronounComplex_DB>) connection.call(
				new Message(key, "saveComplexes", complexes_db)).getArguments()[0];
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}		
		// update
		if(answer.size() != complexes_db.size()) {
			for (PronounComplex_DB pronounComplex_DB : complexes_db) {
				pronounComplex_DB.changeState(key, DB_Element.ERROR);
			}
			throw new DBC_SaveException("Complexes could (probably) be saved but server did not return all of them");
		}
		else {
			for (int i = 0; i < answer.size(); i++) {
				complexes_oos.get(i).setDB_ID(key, answer.toArray(new PronounComplex_DB[0])[i].getDB_ID());
				complexes_oos.get(i).changeState(key, DB_Element.NORMAL);
			}
		}
	}

	/**
	 * @param roots
	 * @return
	 * @throws Exception
	 */
	public Vector<PronounComplex> loadComplexes(IllocutionUnitRoots roots) throws Exception {
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

	public void saveRenominalisations(Renominalisations renominalisations) throws Exception {
		try {
			Renominalisations answer = (Renominalisations) connection.call(new Message(key, "saveRenominalisations", renominalisations,
					new Integer(renominalisations.getChapter().getDB_ID())))
					.getArguments()[0];
			renominalisations.updateIDs(key, answer);
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	/**
	 * Erstellt eine Isotopie-Sammlung Ã¼ber alle in diesem Kapitel vorkommenden
	 * Isotopien.
	 */
	public Renominalisations loadRenominalisations(Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key,
				"loadRenominalisations", new Integer(chapter.getDB_ID())));
		Renominalisations renominalisations = (Renominalisations) answer
		.getArguments()[0];
		IllocutionUnitRoots iur = loadIllocutionUnitRoots(chapter);
		renominalisations.setChapter(key, chapter, iur);
		return renominalisations;
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
		 * @deprecated Ohne Funktion! Wortliste ist nun über WordListElements erreichbar.
		 */
		public Vector getWordList(String word, String language) throws Exception
		{
	//		return (Vector) connection.call(new Message(key, "getWordList", word,
	//		language)).getArguments()[0];
			return null;
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
		 * @deprecated Ohne Funktion! Wortliste ist nun über WordListElements erreichbar.
		 */
		public Vector getWordList(DB_Tupel query) throws Exception
		{
	//		return (Vector) connection.call(new Message(key, "getWordList", query))
	//		.getArguments()[0];
			return null;
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
	 * @deprecated Ohne Funktion! Wortliste ist nun über WordListElements erreichbar.
	 */
	public void saveWordList(Vector list)
	throws Exception {
		connection.call(new Message(key, "saveWordList", list));
	}

	/**
	 * @param element
	 * @throws Exception
	 */
	public void saveWordListElements(WordListElement ... element) throws Exception {
		//key.unlock();
		WordListElement[] answer;
		try {
			answer = (WordListElement[]) connection.call(new Message(key, "saveWordListElements", new Object[] {(Object[]) element}))
			.getArguments()[0];
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
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

	/**
	 * Load from DB WordListElement with assignation id = assigID
	 * @param AssigID
	 * @return
	 */
	public WordListElement loadWordListElementWithAssigID(int assigID) throws Exception {
		Message m = new Message(key, "loadWordListElementWithAssigID", assigID);
		Message oa = connection.call(m);
		Object o = oa.getArguments()[0];
		WordListElement wle = (WordListElement) o;
	
		return wle;//(WordListElement) connection.call(new Message(key, "loadWordListElementWithAssigID", assigID)).getArguments()[0];
	}

	/**
	 * Load the Words appropriated to the given Wortart
	 * @param wortArt, Enum from TR_Assignation
	 * @return 
	 * @throws Exception
	 */
	public Vector<String> loadWordsWithWortArt1(TR_Assignation.Wortart1 wortArt) throws Exception {
		Message message = new Message(key, "loadWordsWithWortArt1", wortArt);
		Message mes = connection.call(message);
		return (Vector<String>)mes.getArguments()[0];
    }
	
	/**
	 * Load the Words appropriated to the given abbreviation.
	 * @param wortArt, Enum from TR_Assignation
	 * @return 
	 * @throws Exception
	 */
	public Vector<String> loadWordsWithAbbreviation(String abbr) throws Exception {
		Message message = new Message(key, "loadWordsWithAbbreviation", abbr);
		Message mes = connection.call(message);
		
		return (Vector<String>)mes.getArguments()[0];
    }
	
	/**
	 * Load the Words appropriated to the given Conjugation.
	 * @param wortArt, Enum from TR_Assignation
	 * @return 
	 * @throws Exception
	 */
	public Vector<String> loadWordsWithConjugation(TR_Assignation.Conjugation conjug) throws Exception {
		Message message = new Message(key, "loadWordsWithConjugation", conjug);
		Message mes = connection.call(message);
			
		return (Vector<String>)mes.getArguments()[0];
	}
	
	/**
	 * Load the Words appropriated to the given Pronoun.
	 * @param wortArt, Enum from TR_Assignation
	 * @return 
	 * @throws Exception
	 */
	public Vector<String> loadWordsWithPronoun(TR_Assignation.WordsubclassPronoun pron) throws Exception {
		 Message message = new Message(key, "loadWordsWithPronoun", pron);
		 Message mes = connection.call(message);
				
		 return (Vector<String>)mes.getArguments()[0];
	}
	
	public Vector loadWordClasses(Vector contents) throws Exception {
		//key.unlock();
		Vector answer = (Vector) connection.call(new Message(key, "loadWordClasses", contents))
		.getArguments()[0];
		return answer;
	}

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
		Relation[] answer;
		try {
			answer = (Relation[]) connection.call(new Message(key, "saveRelation", relations_db.toArray())).getArguments()[0];		
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
		// update
		if(answer.length != relations_db.size()) {
			for (Relation_DB relation : relations_db) {
				relation.changeState(key, DB_Element.ERROR);
			}
			throw new DBC_SaveException("Complexes could (probably) be saved but server did not return all of them");
		}
		else {
			for (int i = 0; i < answer.length; i++) {
				relations_oos.get(i).setDB_ID(key, answer[i].getDB_ID());
				relations_oos.get(i).changeState(key, DB_Element.NORMAL);
			}
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

	public boolean isEdited(Chapter c, int category) throws Exception {
		boolean answer = (Boolean)connection.call(new Message(key,"isEdited",c,category)).getArguments()[0];
		return answer;
	}
	
	public void setKey(DBC_Key key) {
	    DBC.key = key;
	}
	
	public Vector<Vector<String>> loadText_Raw (String strTitle, String strId, String strCreator, String strLang, String strDate) throws Exception {
	    Message answer = connection.call(new Message(key, "loadText_Raw", strTitle, strId, strCreator, strLang, strDate));
	    return (Vector<Vector<String>>) answer.getArguments()[0];

	}
	
	public void saveWorkingTranslations(WorkingTranslation ... translations) throws Exception {
	    // get all complexes that have changed as DB Version
	    ArrayList<WorkingTranslation> translations_oos = new ArrayList<WorkingTranslation>();
	    ArrayList<WorkingTranslation_DB> translations_db = new ArrayList<WorkingTranslation_DB>();
	    for (WorkingTranslation translation : translations) {
			if( translation.isOutOfSync() ) {
			    translations_oos.add(translation);
			    translations_db.add(translation.new WorkingTranslation_DB(key));
			    //TODO: check for unsaved references and ...?
			}
	    }

	    // save
	    ArrayList<WorkingTranslation_DB> answer;
	    try {
			answer = (ArrayList<WorkingTranslation_DB>) connection.call(
				    new Message(key, "saveWorkingTranslations", translations_db)).getArguments()[0];
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	    // update
	    if(answer.size() != translations_db.size()) {
			for (WorkingTranslation_DB workingTranslation_DB : translations_db) {
			    workingTranslation_DB.changeState(key, DB_Element.ERROR);
			}
			throw new DBC_SaveException("WorkingTranslation could (probably) be saved but server did not return all of them");
	    }
	    else {
			for (int i = 0; i < answer.size(); i++) {
			    translations_oos.get(i).setDB_ID(key, answer.toArray(new WorkingTranslation_DB[0])[i].getDB_ID());
			    translations_oos.get(i).changeState(key, DB_Element.NORMAL);
			}
	    }	
	}
	
	/**
	 * @param roots
	 * @return
	 * @throws Exception
	 */
	public Vector<WorkingTranslation> loadWorkingTranslations(String pLg, String pOriginal) throws Exception
	{
		Message answer = connection.call(new Message(key, "loadWorkingTranslations", pLg, pOriginal));
		
		Vector<WorkingTranslation> translations = new Vector<WorkingTranslation>();
		
		// materialize complexes
		for (WorkingTranslation_DB pronounComplex : (Vector<WorkingTranslation_DB>) answer.getArguments()[0]) {
			try {
				translations.add(new WorkingTranslation(key, pronounComplex));
			}
			catch (Exception e) {
				
			}
		}
		return translations;
	}
	
	//@SuppressWarnings("unchecked")
    public Vector<String> loadWorkingTranslationsLanguage() throws Exception
    {
        Message answer = connection.call(new Message(key, "loadWorkingTranslationsLanguage"));
        Vector<String> lgs = (Vector<String>)answer.getArguments()[0];
        return lgs;   
    }

	
	public void saveLiteraryCriticism1(LiteraryCriticism1 ... criticisms) throws Exception {
	    // get all complexes that have changed as DB Version
	    ArrayList<LiteraryCriticism1> criticisms_oos = new ArrayList<LiteraryCriticism1>();
	    ArrayList<LiteraryCriticism1_DB> criticisms_db = new ArrayList<LiteraryCriticism1_DB>();
	    for (LiteraryCriticism1 criticism : criticisms) {
			if( criticism.isOutOfSync() ) {
			    criticisms_oos.add(criticism);
			    criticisms_db.add(criticism.new LiteraryCriticism1_DB(key));
			    //TODO: check for unsaved references and ...?
			}
	    }

	    // save
	    ArrayList<LiteraryCriticism1_DB> answer;
	    try {
			answer = (ArrayList<LiteraryCriticism1_DB>) connection.call(
				    new Message(key, "saveLiteraryCriticism1", criticisms_db)).getArguments()[0];	
		} catch (MysqlDataTruncation e){
	    	throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	    // update
	    if(answer.size() != criticisms_db.size()) {
		for (LiteraryCriticism1_DB criticism_DB : criticisms_db) {
		    criticism_DB.changeState(key, DB_Element.ERROR);
		}
		throw new DBC_SaveException("LiteraryCriticism1 could (probably) be saved but server did not return all of them");
	    }
	    else {
			for (int i = 0; i < answer.size(); i++) {
			    criticisms_oos.get(i).setDB_ID(key, answer.toArray(new LiteraryCriticism1_DB[0])[i].getDB_ID());
			    criticisms_oos.get(i).changeState(key, DB_Element.NORMAL);
			}
	    }
	}
	
	/**
	 * @param roots
	 * @return
	 * @throws Exception
	 */
	public Vector<LiteraryCriticism1> loadLiteraryCriticism1(Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key, "loadLiteraryCriticism1", new Integer(chapter.getDB_ID())));
		Vector<LiteraryCriticism1> criticisms = new Vector<LiteraryCriticism1>();
		
		// materialize complexes
		for (LiteraryCriticism1_DB criticism : (Vector<LiteraryCriticism1_DB>) answer.getArguments()[0]) {
			try {
				criticisms.add(new LiteraryCriticism1(key, chapter, criticism));
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		return criticisms;
	}

	public void saveLiteraryCriticism2(LiteraryCriticism2 ... criticisms) throws Exception {
	    // get all complexes that have changed as DB Version
	    ArrayList<LiteraryCriticism2> criticisms_oos = new ArrayList<LiteraryCriticism2>();
	    ArrayList<LiteraryCriticism2_DB> criticisms_db = new ArrayList<LiteraryCriticism2_DB>();
	    for (LiteraryCriticism2 criticism : criticisms) {
			if( criticism.isOutOfSync() ) {
			    criticisms_oos.add(criticism);
			    criticisms_db.add(criticism.new LiteraryCriticism2_DB(key));
			    //TODO: check for unsaved references and ...?
			}
	    }

	    // save
	    ArrayList<LiteraryCriticism2_DB> answer;
		try {
			answer = (ArrayList<LiteraryCriticism2_DB>) connection.call(
				    new Message(key, "saveLiteraryCriticism2", criticisms_db)).getArguments()[0];
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}	    
	   
	    // update
	    if(answer.size() != criticisms_db.size()) {
			for (LiteraryCriticism2_DB criticism_DB : criticisms_db) {
			    criticism_DB.changeState(key, DB_Element.ERROR);
			}
			throw new DBC_SaveException("LiteraryCriticism2 could (probably) be saved but server did not return all of them");
	    }
	    else {
			for (int i = 0; i < answer.size(); i++) {
			    criticisms_oos.get(i).setDB_ID(key, answer.toArray(new LiteraryCriticism2_DB[0])[i].getDB_ID());
			    criticisms_oos.get(i).changeState(key, DB_Element.NORMAL);
			}
	    }
	}
	
	/**
	 * @param roots
	 * @return
	 * @throws Exception
	 */
	public Vector<LiteraryCriticism2> loadLiteraryCriticism2(Chapter chapter) throws Exception {
		Message answer = connection.call(new Message(key, "loadLiteraryCriticism2", new Integer(chapter.getDB_ID())));
		Vector<LiteraryCriticism2> criticisms = new Vector<LiteraryCriticism2>();
		
		// materialize complexes
		for (LiteraryCriticism2_DB pronounComplex : (Vector<LiteraryCriticism2_DB>) answer.getArguments()[0]) {
			try {
				criticisms.add(new LiteraryCriticism2(key, chapter, pronounComplex));
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		return criticisms;
	}
	
	/* 
	 * ==============================================
	 * ================= IU_Comment =================
	 * ==============================================
	 */
	
	public ArrayList<IU_Comment> loadIUComments(int id) throws Exception {
		Message answer = connection.call(new Message(key, "loadIUComments", new Integer(id)));
		return (ArrayList<IU_Comment>) answer.getArguments()[0];
	}

	public void saveIUComment(IU_Comment comment) throws Exception {
		try {
			connection.call(new Message(key, "saveIUComment", comment));
		} catch (MysqlDataTruncation e){
			throw new DBC_SaveException(e.getMessage());
		} catch (Exception e){
			throw new DBC_SaveException(e.getMessage());
		}
	}

	public void deleteIUComment(Integer IU_ID) throws Exception {
		connection.call(new Message(key, "deleteIUComment", IU_ID));
	}

	public void editIUComment(Integer IU_ID, String text) throws Exception {
		connection.call(new Message(key, "editIUComment", IU_ID, text));
	}
}