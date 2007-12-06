/*
 * Erstellt: 23.10.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Book;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Chapter;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.ChapterEditingTester;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Checking;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Comment;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.CommentKey;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Comments;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.ConstitutiveWord;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DB_Tupel;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Dialog;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Dialogs;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeech;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeeches;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.FunctionWord;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IDOwner;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnit;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnitRoot;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnitRoots;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Isotope;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Isotopes;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.MacroSentence;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.MeaningUnit;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Occurrence_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Pattern;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.PronounComplex;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Relation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Renominalisation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Renominalisations;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.SememeGroup;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Sign;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.SpeakerChange;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Thema_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Token;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Word;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WordListElement;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.PronounComplex.PronounComplex_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Relation.Relation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.TR_Assignation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_KeyAcceptor;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_SaveException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.pathselector.PathNode;

/**
 * The DBC_Server communicates to the mySQL server.
 */
public class DBC_Server implements Runnable, DBC_KeyAcceptor {

    	static DBC_Key	key;
	private DBC_Cache  chapterCache;
	private PathNode   root;
	private PathNode numerusRoot;
	private Connection connection;
	private String     db_host;
	private int     db_port;
	private String     db_name;
	private String     db_user;
	private String     db_password;
	private int        counter;
	private int        keepAlive;
	public static Logger logger;

	public static final String minimalClientVersion = "3.1";

	DBC_Server(String host, int port, String name, String user, String password)
	throws DBC_ConnectionException {

		this.db_host = host;
		this.db_port = port;
		this.db_name = name;
		this.db_user = user;
		this.db_password = password;   

		this.keepAlive = 3000;
		counter = this.keepAlive;

		logger = (Slang2Server.logger != null) ? Slang2Server.logger : Logger.global;

		DBC_Key.makeKey(this);
		chapterCache = new DBC_Cache(5);
		open();
		root = loadPaths();
		numerusRoot = loadNumerusPaths();
	}

	/**
	 * Beendet die Verbindung zu dem Datenbank-Server
	 * 
	 */
	 void close() {
		 if (connection != null) {
			 try {
				 logger.info("Closing the connection to the mySQL server...");
				 connection.close();
				 connection = null;
			 }
			 catch (SQLException e) {
				 e.printStackTrace();
			 }
		 }
	 }

	 /**
	  * Öffnet die Verbindung zur Datenbank. Wird vom Konstruktor aufgerufen. Auch
	  * nützlich, wenn die Verbindung mit <code>close()</code> beendet wurde und nun wieder
	  * hergestellt werden soll.
	  * 
	  * @throws DBC_ConnectionException falls kein Verbindungsaufbau möglich ist.
	  * 
	  * @see #close()
	  */
	 void open() throws DBC_ConnectionException {	   
		 if (connection == null) {
			 logger.info("Connecting to the mySQL server...");
			 try {

				 Class.forName("com.mysql.jdbc.Driver");
				 connection = DriverManager.getConnection(String.format("jdbc:mysql://%1$s:%2$d/%3$s?useUnicode=true&characterEncoding=ISO-8859-1"
						 ,this.db_host, this.db_port, this.db_name), this.db_user, this.db_password);
			 }
			 catch (SQLException e) {
				 throw new DBC_ConnectionException("Could not connect to the mySQL server: " + e.getLocalizedMessage());
			 }
			 catch (ClassNotFoundException e) {
				 throw new DBC_ConnectionException("JDBC driver not found");
			 }
			 logger.info("Connection is up.");
		 }
	 }

	 public void run() {
		 // run until stopped by InterruptedException
		 for (;;) {
			 if (counter == 0)
				 close();
			 else
				 counter--;

			 try {
				 Thread.sleep(1000);
			 }
			 catch (InterruptedException e) {
				 logger.finer("DBC_Server Thread was interrupted");
				 close();
				 return;
			 }
		 }
	 }

	 public synchronized void resetCounter() throws DBC_ConnectionException {
		 logger.finest("Resetting keep alive counter.");
		 counter = keepAlive;
		 open();
	 }

	 private static IDOwner getElement(int id, Vector elements) {
		 for (int i = 0; i < elements.size(); i++) {
			 IDOwner e = (IDOwner) elements.get(i);
			 if (e.getDB_ID() == id)
				 return e;
		 }
		 return null;
	 }


	 /**
	  * First call by DBC to prevent conflicts
	  * @param clientVersion
	  * @throws Exception 
	  */
	 public void hello(String version) throws Exception {
		 // Check for version conflict
		 String[] clientVersionNumber = version.split("\\.");
		 String[] requiredMinimalVersionNumber = minimalClientVersion.split("\\.");

		 for (int i = 0; i < requiredMinimalVersionNumber.length; i++) {
			 if(clientVersionNumber.length >= i+1
					 && Integer.parseInt(requiredMinimalVersionNumber[i]) <= Integer.parseInt(clientVersionNumber[i]))
				 continue;
			 else
				 throw new Exception("DBC Client is too old. Please update to a version >= " + minimalClientVersion);
		 }
	 }


	 /**
	  * Lï¿½dt alle Bï¿½cher aus der Datenbank.
	  * 
	  * @return ein Vektor mit allen gespeicherten Bï¿½chern
	  * 
	  * @see Book
	  */
	 public synchronized Vector loadBooks()
	 throws Exception {
		 Vector books = new Vector();

		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * FROM books");

		 while (res.next()) {
			 books.add(new Book(key, res.getInt("id"), res.getString("title"), res
					 .getString("author"), res.getInt("year")));
		 }

		 for (int i = 0; i < books.size(); i++) {
			 Book book = (Book) books.get(i);
			 res = stmt.executeQuery("SELECT * FROM chapters WHERE book = "
					 + book.getDB_ID());

			 while (res.next()) {
				 Timestamp ts = res.getTimestamp("date");
				 if(ts == null){ //noch keine Zeit gesetzt, wird neu initialisiert
					 ts = new Timestamp(0);
				 }
				 ts.setNanos(0);
				 book.add(key, new Chapter(key, res.getInt("id"), book.getDB_ID(),
						 res.getInt("index"), res.getString("title"), ts.toString().substring(0,ts.toString().length()-2))); //dirty, aber sehe keine andere Möglichkeit, den nano-
				 //Anteil loszuwerden
			 }
		 }

		 stmt.close();

		 return books;
	 }

	 /**
	  * Lï¿½dt ein Buch aus der Datenbank
	  * 
	  * @param id
	  *        Die ID des Buches
	  * @return Das Buch aus der Datenbank mit ID id
	  */
	 public synchronized Book loadBook(Integer id)
	 throws Exception {
		 Book book = null;
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * "
				 + "FROM books WHERE id = "
				 + id.intValue());

		 if (res.next())
			 book = new Book(key, id.intValue(), res.getString("title"), res
					 .getString("author"), res.getInt("year"));

		 stmt.close();
		 return book;
	 }

	 public synchronized Book saveBook(Book book)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);

		 ResultSet res = stmt.executeQuery("SELECT * FROM books "
				 + "WHERE title like binary '"
				 + book.getTitle()
				 + "' and author like binary '"
				 + book.getAuthor()
				 + "' and year = "
				 + book.getYear());

		 if (!res.next()) {
			 res.moveToInsertRow();
			 res.updateString("title", book.getTitle());
			 res.updateString("author", book.getAuthor());
			 res.updateInt("year", book.getYear());
			 res.insertRow();
			 res.close();
		 }

		 res = stmt.executeQuery("SELECT * FROM books "
				 + "WHERE title like binary '"
				 + book.getTitle()
				 + "' and author like binary '"
				 + book.getAuthor()
				 + "' and year = "
				 + book.getYear());
		 if (res.next())
			 book.setDB_ID(key, res.getInt("id"));
		 else {
			 connection.rollback();
			 stmt.close();
			 connection.setAutoCommit(true);
			 throw new DBC_SaveException("Buch "
					 + book.getTitle()
					 + "konnte nicht in der "
					 + "DB gespeichert werden!");
		 }

		 connection.commit();
		 stmt.close();
		 connection.setAutoCommit(true);
		 return book;
	 }
		
		/**
		    * LÃ¤dt alle Pattern aus der Datenbank.
		    * 
		    * @return Vektor<Pattern>
		    */
		public synchronized Vector<Pattern> loadPatterns() throws Exception {
			Vector<Pattern> ret = new Vector<Pattern>();
			
			Statement stmt = connection.createStatement();
			ResultSet res = stmt.executeQuery("SELECT * FROM pattern");

			while (res.next()) {
				ret.add(new Pattern(res.getInt("id"), res.getString("name"), 
						res.getString("description"), res.getString("tdtype"), 
						res.getInt("level"), res.getInt("mu"), res.getInt("path")));
			}
			res.close();
			stmt.close();
			return ret;
		}
		
		 /**
		    * LÃ¤dt alle Pattern mit tdTyp aus der Datenbank.
		    * 
		    * @param String tdType
		    * @return Vektor<Pattern>
		    */
		public synchronized Vector<Pattern> loadPatterns(String tdType) throws Exception {
			Vector<Pattern> ret = new Vector<Pattern>();

			Statement stmt = connection.createStatement();
			
			ResultSet res = stmt.executeQuery("SELECT * "
					+ "FROM pattern WHERE tdtype = '" + tdType + "'");

			while (res.next()) {
				ret.add(new Pattern(res.getInt("id"), res.getString("name"),
						res.getString("description"), res.getString("tdtype"), 
						res.getInt("level"), res.getInt("mu"), res.getInt("path")));
			}
			res.close();
			stmt.close();
			return ret;
		}
		
		 /**
		    * Speichert ein Pattern in der Datenbank
		    * 
		    * @param Pattern pattern
		    */
		public synchronized Pattern savePattern(Pattern pattern) throws Exception {
			connection.setAutoCommit(false);
			Statement stmt = connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			ResultSet res = stmt.executeQuery("SELECT * FROM pattern "  
					+ "WHERE id = " + pattern.getDB_ID());
			
			// schon in der DB vorhanden
			if (res.next()) {
				System.out.println("schon in der DB vorhanden");
				res.updateString("name", pattern.getName());
				res.updateString("description", pattern.getDescription());
				res.updateString("tdtype", pattern.gettdType());
				res.updateInt("level", pattern.getLevel());
				res.updateInt("mu", pattern.getMu());
				res.updateInt("path", pattern.getPath());
				res.updateRow();
			}
			
			// muss gespeichert werden
			else {
				System.out.println("muss gespeichert werden");
				res.moveToInsertRow();
				res.updateString("name", pattern.getName());
				res.updateString("description", pattern.getDescription());
				res.updateString("tdtype", pattern.gettdType());
				res.updateInt("level", pattern.getLevel());
				res.updateInt("mu", pattern.getMu());
				res.updateInt("path", pattern.getPath());
				res.insertRow();
				res.close();
				
				res = stmt.executeQuery("SELECT * FROM pattern "
						+ "WHERE name = '"        + pattern.getName()
						+ "' and description = '" + pattern.getDescription()
						+ "' and tdtype = '"      + pattern.gettdType()
						+ "' and level = '"       + pattern.getLevel()
						+ "' and mu = '"          + pattern.getMu()
						+ "' and path = '"        + pattern.getPath() + "'");

				if (res.next())
					pattern.setDB_ID(res.getInt("id"));

				else
					throw new DBC_SaveException("Pattern " + pattern.getName()
							+ "konnte nicht in der " + "DB gespeichert werden!");
				
				res.close();
				connection.commit();
			}
			connection.setAutoCommit(true);
			stmt.close();
			return pattern;
		}

	 private void setChapter(Chapter chapter) {
		 chapterCache.set(chapter.getDB_ID(), chapter);
	 }

	 private synchronized Chapter getChapter(int id) {
		 Chapter chapter = (Chapter) chapterCache.get(id);
		 if (chapter == null) {
			 try {
				 return loadChapter(id);
			 }
			 catch (Exception e) {
				 e.printStackTrace();
				 return null;
			 }
		 }
		 return chapter;

	 }

	 private Chapter loadChapter(int chapterID) throws SQLException
	 {
		 Statement stmt = null;
		 ResultSet res = null;
		 Chapter chapter = null;

		 try {
			 stmt = connection.createStatement();
			 res = stmt.executeQuery("SELECT * "
					 + "FROM chapters WHERE id = "
					 + chapterID);

			 if (res.next()) {
				 // Kapiteldaten
				 Timestamp ts = res.getTimestamp("date");
				 if(ts == null){ //noch keine Zeit gesetzt, wird neu initialisiert
					 ts = new Timestamp(0);
				 }
				 ts.setNanos(0);
				 chapter = new Chapter(key, res.getInt("id"), res
						 .getInt("book"), res.getInt("index"), res.getString("title"), ts.toString().substring(0,ts.toString().length()-2));

				 // Wörter des Kapitels
				 res = stmt.executeQuery("SELECT words.id AS id, "
						 + "words_in_chapter.position, "
						 + "words.content, words.language "
						 + "FROM words_in_chapter, words "
						 + "WHERE words_in_chapter.chapter = "
						 + chapter.getDB_ID()
						 + " AND words_in_chapter.word = words.id "
						 + "ORDER BY words_in_chapter.position");
				 while (res.next())
					 chapter.addWord(key,
							 res.getInt("id"),
							 new String(res.getBytes("content")),
							 res.getString("language"),
							 res.getInt("position"));

				 // Satzzeichen des Kapitels
				 res = stmt.executeQuery("SELECT signs.id as id, "
						 + "signs_in_chapter.position, "
						 + "signs.sign "
						 + "FROM signs_in_chapter, signs "
						 + "WHERE signs_in_chapter.chapter = "
						 + chapter.getDB_ID()
						 + " and signs_in_chapter.sign = signs.id "
						 + "ORDER BY signs_in_chapter.position");
				 while (res.next())
					 chapter.addSign(key, res.getInt("id"), res.getString("sign")
							 .charAt(0), res.getInt("position"));

				 // Absätze des Kapitels
				 res = stmt.executeQuery("SELECT position "
						 + "FROM paragraphs_in_chapter "
						 + "WHERE chapter = "
						 + chapter.getDB_ID()
						 + " ORDER BY position");
				 while (res.next())
					 chapter.addNewline(key, res.getInt("position"));

				 // Äußerungseinheiten des Kapitels
				 res = stmt.executeQuery("SELECT * FROM illocution_units "
						 + "WHERE chapter = "
						 + chapter.getDB_ID());
				 while (res.next())
					 chapter.addIllocutionUnit(key, res.getInt("id"), res
							 .getInt("start"), res.getInt("end"));

				 chapter.calculateIndicies(key);
				 setChapter(chapter);

			 }
		 }
		 catch ( SQLException e ){
			 throw e;
		 }
		 finally {
			 try
			 {
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 // ignore
			 }
		 }

		 return chapter;
	 }

	 public synchronized Chapter loadChapter(Integer chapterID) throws Exception
	 {
		 return getChapter(chapterID.intValue());
	 }

	 public synchronized Chapter saveChapter(Chapter chapter) throws DBC_SaveException, SQLException
	 {
		 logger.entering(DBC_Server.class.getName(), "saveChapter", chapter);

		 PreparedStatement stmt = null;
		 ResultSet res = null;

		 chapter.calculateIndicies(key);

		 try {
			 connection.setAutoCommit(false);

			 stmt = connection.prepareStatement(

					 "SELECT * FROM chapters WHERE book = ? AND index = ?",

					 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE
			 );

			 stmt.setInt(1, chapter.getBookID());
			 stmt.setInt(2, chapter.getIndex());

			 res = stmt.executeQuery();

			 // Kapitel wurde schon gespeichert -> lösche es
			 if ( res.next() ) {
				 //System.out.println("l\u00f6sche altes Kapitel");
				 logger.info("L\u00f6sche altes Kapitel");
				 res.deleteRow();
				 connection.commit();
			 }

			 res.moveToInsertRow();
			 res.updateInt("book", chapter.getBookID());
			 res.updateInt("index", chapter.getIndex());
			 res.updateString("title", chapter.getTitle());
			 res.updateTimestamp("date", new Timestamp(System.currentTimeMillis())); //todo: don't instantiate with currentMills
			 res.insertRow();

			 // Wiederhole die Anfrage
			 res = stmt.executeQuery();

			 if (res.next())
				 chapter.setDB_ID(key, res.getInt("id"));
			 else {
				 connection.rollback();
				 throw new DBC_SaveException("Kapitel "
						 + chapter.getTitle()
						 + "konnte nicht in der "
						 + "DB gespeichert werden!");
			 }
			 //System.out.println("lege neues Kapitel an:");
			 logger.info("Lege neues Kapitel an");

			 Vector words = chapter.getWords();
			 //System.out.println("\nspeichere " + words.size() + " W\u00f6rter:");
			 logger.info("Speichere " + words.size() + " W\u00f6rter");

			 for (int i = 0; i < words.size(); i++) {
				 Word word = (Word) words.get(i);

				 if (word.getDB_ID() == -1) {
					 // Insert word first if not exists
					 stmt = connection.prepareStatement(

							 "INSERT IGNORE INTO words " +
							 "(content, language) " +
							 "VALUES(?, ?)",

							 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);	   

					 stmt.setBytes(1, word.getContent().getBytes());
					 stmt.setString(2, word.getLanguage());  
					 stmt.executeUpdate();
					 res = stmt.getGeneratedKeys();

					 int wordId = 0;
					 if( res.next() ) {
						 wordId = res.getInt(1);
					 }
					 else {
						 stmt = connection.prepareStatement(

								 "SELECT id FROM words WHERE content = ? AND language = ?",

								 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

						 stmt.setBytes(1, word.getContent().getBytes());
						 stmt.setString(2, word.getLanguage());

						 res = stmt.executeQuery();
						 if( res.next() ) {
							 wordId = res.getInt(1);
						 }
					 }

					 if( wordId > 0 ) {
						 word.setDB_ID(key, res.getInt("id"));
					 }
					 else {
						 connection.rollback();
						 throw new DBC_SaveException("Wort "
								 + word
								 + " konnte nicht in der "
								 + "DB gespeichert werden!");
					 }

					 // speichere Wort im Kapitel
					 stmt = connection.prepareStatement(

							 "INSERT INTO words_in_chapter (chapter, word, position) VALUES(?, ?, ?)",

							 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

					 stmt.setInt(1, word.getChapter().getDB_ID());
					 stmt.setInt(2, word.getDB_ID());
					 stmt.setInt(3, word.getStartPosition());
					 int rowCount = stmt.executeUpdate();

					 if( rowCount != 0 ) {
						 //System.out.print('+');
						 logger.finer("+");
					 }
				 }
				 else {
					 //System.out.print('-');
					 logger.finer("-");
				 }
			 }

			 Vector signs = chapter.getSigns();

			 //System.out.println("\n\nspeichere " + signs.size() + " Satzzeichen:");
			 logger.info("Speichere " + signs.size() + " Satzzeichen");

			 stmt = connection.prepareStatement("INSERT IGNORE INTO signs (sign) VALUES(?)");

			 for (int i = 0; i < signs.size(); i++) {
				 Sign sign = (Sign) signs.get(i);

				 if (sign.getDB_ID() == -1) {

					 stmt.setString(1, sign.getContent());
					 stmt.addBatch();
				 }
			 }
			 int [] counts = stmt.executeBatch();

			 // setze entsprechende DB Id im Objekt
			 stmt = connection.prepareStatement("SELECT id FROM signs WHERE sign = ?");

			 for (int i = 0; i < signs.size(); i++) {
				 Sign sign = (Sign) signs.get(i);

				 if (sign.getDB_ID() == -1) {

					 stmt.setString(1, sign.getContent());
					 res = stmt.executeQuery();

					 if (res.next()) {
						 sign.setDB_ID(key, res.getInt(1));
						 logger.finer("-");
					 }
					 else {
						 connection.rollback();
						 throw new DBC_SaveException("Satzzeichen "
								 + sign
								 + " konnte nicht in der "
								 + "DB gespeichert werden!");
					 }
				 }
			 }

			 // speichere Satzzeichen im Kapitel
			 stmt = connection.prepareStatement("INSERT INTO signs_in_chapter (chapter, sign, position) VALUES(?, ?, ?)");

			 for (int i = 0; i < signs.size(); i++) {	  
				 Sign sign = (Sign) signs.get(i);

				 stmt.setInt(1, sign.getChapter().getDB_ID());
				 stmt.setInt(2, sign.getDB_ID());
				 stmt.setInt(3, sign.getStartPosition());
				 stmt.addBatch();
			 }
			 counts = stmt.executeBatch();

			 // Speichere Absätze
			 Vector paragraphs = chapter.getParagraphs();
//			 System.out.println("\n\nspeichere "
//			 + paragraphs.size()
//			 + " Abs\u00e4tze:");
			 logger.info("Speichere " + paragraphs.size() + " Abs\u00e4tze");

			 stmt = connection.prepareStatement(

					 "INSERT INTO paragraphs_in_chapter (chapter, position) VALUES (?, ?)"
			 );		   
			 for (int i = 0; i < paragraphs.size(); i++) {
				 Integer p = (Integer) paragraphs.get(i);
				 stmt.setInt(1, chapter.getDB_ID());
				 stmt.setInt(2, p.intValue());
				 stmt.addBatch();
			 }
			 int [] updateCounts = stmt.executeBatch();

			 String logMessage = "";
			 for(int i = 0; i < paragraphs.size(); i++) {
				 logMessage += (updateCounts[i]!=0)?"+":"-";
			 }
			 logger.finer(logMessage);

			 // Speichere Äußerungseinheiten
			 Vector ius = chapter.getIllocutionUnits();
//			 System.out.println("\n\nspeichere "
//			 + ius.size()
//			 + " Ã„uÃŸerungseinheiten:");
			 logger.info("Speichere " + ius.size() + " Äußerungseinheiten");

			 stmt = connection.prepareStatement(

					 "INSERT INTO illocution_units (chapter, start, end) VALUES (?, ?, ?)"
			 );		   
			 for (int i = 0; i < ius.size(); i++) {
				 IllocutionUnit iu = (IllocutionUnit) ius.get(i);
				 stmt.setInt(1, chapter.getDB_ID());
				 stmt.setInt(2, iu.getStartPosition());
				 stmt.setInt(3, iu.getStartPosition());
				 stmt.addBatch();
			 }
			 updateCounts = stmt.executeBatch();

			 stmt = connection.prepareStatement(

					 "SELECT id FROM illocution_units WHERE chapter = ? AND start = ? AND end = ?"
			 );
			 for (int i = 0; i < ius.size(); i++) {
				 IllocutionUnit iu = (IllocutionUnit) ius.get(i);			   
				 stmt.setInt(1, chapter.getDB_ID());
				 stmt.setInt(2, iu.getStartPosition());
				 stmt.setInt(3, iu.getStartPosition());
				 res = stmt.executeQuery();

				 if ( res.next() )
					 iu.setDB_ID(key, res.getInt(1));
				 else {
					 logger.severe("Fehler beim Speichern einer Äußerungseinheit => Rollback");
					 connection.rollback();
					 throw new DBC_SaveException("Äußerungseinheit "
							 + iu
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");
				 }

				 if( ! res.isLast() ) {
					 logger.warning("Doppelte Einträge in der illocution_units Tabelle gefunden");
				 }

//				 System.out.print('+');
				 logger.finer("+");
			 }
//			 System.out.println("\nfertig");
			 logger.info("Fertig");

			 connection.commit();
		 }
		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 connection.setAutoCommit(true);
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return chapter;
	 }

	 public synchronized void deleteChapter(Integer chapterID)
	 throws Exception {
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * "
				 + "FROM chapters WHERE id = "
				 + chapterID);
		 if (res.next()) {
			 int bookID = res.getInt("book");
			 res = stmt.executeQuery("SELECT * "
					 + "FROM chapters WHERE book = "
					 + bookID);
			 res.next(); //zeigt auf zu löschendes, auf jeden Fall vorhandene Chapter
			 if(!res.next()){ //Buch kann gelöscht werden, da kein weiteres Kapitel von diesem Buch vorhanden ist
				 stmt.execute("delete FROM books WHERE id = "+bookID);
			 }
			 stmt.execute("delete FROM chapters WHERE id = "+chapterID);
		 }
	 }

	 /**
	  * Lï¿½dt alle Direkten Reden aus der Datenbank, die zu diesem Kapitel
	  * gespeichert wurden.
	  * 
	  * @param chapterID
	  *        das Kapitel
	  * @return ein Vektor mit den entsprechenden Direkten Reden.
	  * @see DirectSpeech
	  */
	 public synchronized DirectSpeeches loadDirectSpeeches(Integer chapterID)
	 throws Exception {

		 System.out.println("***********saveDirectSpeeches********");
		 Chapter chapter = getChapter(chapterID.intValue());

		 DirectSpeeches directSpeeches = new DirectSpeeches();
		 Vector speeches = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res;

		 // Grunddaten der direkten Reden einlesen.
		 res = stmt.executeQuery("SELECT * "
				 + "FROM direct_speeches WHERE chapter = "
				 + chapter.getDB_ID()
				 + " ORDER BY `index`");

		 while (res.next())
			 speeches.add(new DirectSpeech(key, res.getInt("id"), chapter, res
					 .getInt("index"), res.getInt("depth"), res
					 .getBoolean("accepted"), res.getInt("possible_question")));

		 // ï¿½uï¿½erungseinheiten zu den direkten Reeden einlesen.
		 for (int i = 0; i < speeches.size(); i++) {
			 DirectSpeech ds = (DirectSpeech) speeches.get(i);
			 res = stmt.executeQuery("SELECT illocution_unit "
					 + "FROM ius_FROM_direct_speeches "
					 + "WHERE direct_speech = "
					 + ds.getDB_ID()
					 + " ORDER BY illocution_unit");

			 while (res.next())
				 ds.setIllocutionUnit(key, res.getInt("illocution_unit"));

			 directSpeeches.add(ds);
		 }

		 for (int i = 0; i < speeches.size(); i++) {
			 DirectSpeech ds = (DirectSpeech) speeches.get(i);
			 int id = ds.getPossibleQuestionID();
			 if (id > 0)
				 ds.setPossibleQuestion(directSpeeches.getDirectSpeechWithID(id));
		 }

		 res.close();
		 stmt.close();
		 return directSpeeches;
	 }

	 public synchronized DirectSpeeches saveDirectSpeeches (Integer chapterID,
			 DirectSpeeches oldDirectSpeeches,
			 DirectSpeeches newDirectSpeeches)
	 throws Exception 
	 {
		 Chapter chapter = getChapter(chapterID.intValue());

		 Vector oldDss = oldDirectSpeeches.getAllDirectSpeeches(key);
		 Vector newDss = newDirectSpeeches.getAllDirectSpeeches(key);

		 oldDirectSpeeches.setChapter(key, chapter);
		 newDirectSpeeches.setChapter(key, chapter);

		 connection.setAutoCommit(false);
		 Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);

		 ResultSet res;

		 //lösche alle Direct Speeches des chapters in der Datenbank
		 for (int i = 0; i < oldDss.size(); i++) 
		 {
			 DirectSpeech ds = (DirectSpeech) oldDss.get(i);

			 //lösche alle direct_speeches Einträge aus oldDss die bereits in der Datenbank gespeichert sind
			 res = stmt.executeQuery("SELECT * "
					 + "FROM direct_speeches "
					 + "WHERE id = "
					 + ds.getDB_ID());

			 if(res.next())
				 res.deleteRow();

			 //lösche alle ius_FROM_direct_speeches Einträge aus oldDss die bereits in der Datenbank gespeichert
			 res = stmt.executeQuery("SELECT * "
					 + "FROM ius_FROM_direct_speeches "
					 + "WHERE direct_speech = "
					 + ds.getDB_ID());

			 if(res.next())
				 res.deleteRow();

		 }

		 // speichere alle aktuellen Direct Speeches des chapters
		 for (int i = 0; i < newDss.size(); i++)	
		 {
			 DirectSpeech ds = (DirectSpeech) newDss.get(i);

			 res = stmt.executeQuery("SELECT * "
					 + "FROM direct_speeches "
					 + "WHERE id = "
					 + ds.getDB_ID());

			 //wenn Eintrag nicht in Datenbank vorhanden
			 if (ds.getDB_ID() == -1) 
			 {
				 res.moveToInsertRow();
				 res.updateInt("chapter", chapter.getDB_ID());
				 res.updateInt("index", ds.getIndex());
				 res.updateInt("depth", ds.getDepth());
				 res.updateBoolean("accepted", ds.isAccepted());

				 if (ds.getPossibleQuestionID() > 0)
					 res.updateInt("possible_question", ds.getPossibleQuestionID());
				 else
					 res.updateNull("possible_question");

				 res.insertRow();
				 res.close();
				 ds.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM direct_speeches WHERE chapter = "
						 + chapter.getDB_ID()
						 + " and `index` = "
						 + ds.getIndex()
						 + " and depth = "
						 + ds.getDepth());

				 if (res.next())
					 ds.setDB_ID(key, res.getInt("id"));
				 else
					 throw new DBC_SaveException("Direkte Rede "
							 + ds
							 + "konnte nicht angelegt werden");

				 res.close();

				 res = stmt.executeQuery("SELECT * FROM ius_FROM_direct_speeches");
				 Vector ius = ds.getIllocutionUnits();

				 for (int j = 0; j < ius.size(); j++) 
				 {
					 IllocutionUnit iu = (IllocutionUnit) ius.get(j);

					 res.moveToInsertRow();
					 res.updateInt("direct_speech", ds.getDB_ID());
					 res.updateInt("illocution_unit", iu.getDB_ID());
					 res.insertRow();
				 }
				 res.close();
			 }
		 }

		 connection.commit();
		 connection.setAutoCommit(true);
		 stmt.close();

		 return newDirectSpeeches;
	 }


	 /*   public synchronized DirectSpeeches saveDirectSpeeches(Integer chapterID,
         DirectSpeeches directSpeeches)
         throws Exception {

      Chapter chapter = getChapter(chapterID.intValue());
      Vector dss = directSpeeches.getAllDirectSpeeches(key);
      directSpeeches.setChapter(key, chapter);

      connection.setAutoCommit(false);
      Statement stmt = connection
            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            				 ResultSet.CONCUR_UPDATABLE);
      ResultSet res;

      for (int i = 0; i < dss.size(); i++) {
         DirectSpeech ds = (DirectSpeech) dss.get(i);

         res = stmt.executeQuery("SELECT * "
               + "FROM direct_speeches "
               + "WHERE id = "
               + ds.getDB_ID());

         if (res.next() && ds.getDB_ID() != -1) {
            if (ds.hasChanged()) {
               res.updateInt("chapter", chapter.getDB_ID());
               res.updateInt("index", ds.getIndex());
               res.updateInt("depth", ds.getDepth());
               res.updateBoolean("accepted", ds.isAccepted());
               if (ds.getPossibleQuestionID() > 0)
                  res
                        .updateInt("possible_question", ds
                              .getPossibleQuestionID());
               else
                  res.updateNull("possible_question");
               res.updateRow();
               ds.resetState(key);

               Vector ius = ds.getIllocutionUnits();
               Vector existingIUs = new Vector();
               res = stmt
                     .executeQuery("SELECT * FROM ius_FROM_direct_speeches "
                           + "WHERE direct_speech = "
                           + ds.getDB_ID());

               while (res.next()) {
                  int iuID = res.getInt("illocution_unit");
                  IllocutionUnit iu = (IllocutionUnit) getElement(iuID, ius);
                  if (iu == null)
                     res.deleteRow();
                  else
                     existingIUs.add(iu);
               }

               for (int j = 0; j < ius.size(); j++) {
                  IllocutionUnit iu = (IllocutionUnit) ius.get(j);
                  if (!existingIUs.contains(iu)) {
                     res.moveToInsertRow();
                     res.updateInt("direct_speech", ds.getDB_ID());
                     res.updateInt("illocution_unit", iu.getDB_ID());
                     res.insertRow();
                  }
               }
            }
            else if (ds.isRemoved()) {
               res.deleteRow();
            }
         }

         else if (!ds.isRemoved() && ds.getDB_ID() == -1) {
            res.moveToInsertRow();
            res.updateInt("chapter", chapter.getDB_ID());
            res.updateInt("index", ds.getIndex());
            res.updateInt("depth", ds.getDepth());
            res.updateBoolean("accepted", ds.isAccepted());
            if (ds.getPossibleQuestionID() > 0)
               res.updateInt("possible_question", ds.getPossibleQuestionID());
            else
               res.updateNull("possible_question");
            res.insertRow();
            res.close();
            ds.resetState(key);

            res = stmt.executeQuery("SELECT id "
                  + "FROM direct_speeches WHERE chapter = "
                  + chapter.getDB_ID()
                  + " and `index` = "
                  + ds.getIndex()
                  + " and depth = "
                  + ds.getDepth());

            if (res.next())
               ds.setDB_ID(key, res.getInt("id"));
            else
               throw new DBC_SaveException("Direkte Rede "
                     + ds
                     + "konnte nicht angelegt werden");

            res.close();
            res = stmt.executeQuery("SELECT * FROM ius_FROM_direct_speeches");
            Vector ius = ds.getIllocutionUnits();
            for (int j = 0; j < ius.size(); j++) {
               IllocutionUnit iu = (IllocutionUnit) ius.get(j);
               res.moveToInsertRow();
               res.updateInt("direct_speech", ds.getDB_ID());
               res.updateInt("illocution_unit", iu.getDB_ID());
               res.insertRow();
            }
         }
         res.close();
         connection.commit();
      }
      connection.setAutoCommit(true);
      stmt.close();
      return directSpeeches;
   }
	  */

	 public synchronized Dialogs saveDialogs(Integer chapterID, Dialogs dialogs)
	 throws Exception {

		 System.out.println("******* SAVE *******");
		 Chapter chapter = getChapter(chapterID.intValue());
		 Vector ds = dialogs.getAllDialogs(key);
		 dialogs.setChapter(key, chapter);

		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < ds.size(); i++) {
			 Dialog d = (Dialog) ds.get(i);

			 res = stmt.executeQuery("SELECT * "
					 + "FROM dialogs "
					 + "WHERE id = "
					 + d.getDB_ID());

			 if (res.next() && d.getDB_ID() != -1) {
				 if (d.hasChanged()) {
					 res.updateInt("chapter", chapter.getDB_ID());
					 res.updateInt("index", d.getIndex());
					 res.updateInt("depth", d.getDepth());
					 res.updateInt("start", d.getDialogStart().getDB_ID());
					 res.updateInt("end", d.getDialogEnd().getDB_ID());
					 res.updateBoolean("accepted", d.isAccepted());
					 res.updateRow();
					 d.resetState(key);
					 res.close();

					 res = stmt.executeQuery("SELECT * FROM run_up WHERE dialog = "
							 + d.getDB_ID());
					 if (res.next()) {
						 if (d.hasRunUp()) {
							 res.updateInt("start", d.getRunUpStart().getDB_ID());
							 res.updateInt("end", d.getRunUpEnd().getDB_ID());
							 res.updateRow();
						 }
						 else
							 res.deleteRow();
					 }
					 res.close();

					 res = stmt
					 .executeQuery("SELECT * FROM follow_up WHERE dialog = "
							 + d.getDB_ID());
					 if (res.next()) {
						 if (d.hasFollowUp()) {
							 res.updateInt("start", d.getFollowUpStart().getDB_ID());
							 res.updateInt("end", d.getFollowUpEnd().getDB_ID());
							 res.updateRow();
						 }
						 else
							 res.deleteRow();
					 }
					 res.close();
				 }
				 else if (d.isRemoved()) {
					 res.deleteRow();
				 }
			 }

			 else if (!d.isRemoved() && d.getDB_ID() == -1) {
				 res.moveToInsertRow();
				 res.updateInt("chapter", chapter.getDB_ID());
				 res.updateInt("index", d.getIndex());
				 res.updateInt("depth", d.getDepth());
				 res.updateInt("start", d.getDialogStart().getDB_ID());
				 res.updateInt("end", d.getDialogEnd().getDB_ID());
				 res.updateBoolean("accepted", d.isAccepted());
				 res.insertRow();
				 res.close();
				 d.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM dialogs WHERE chapter = "
						 + chapter.getDB_ID()
						 + " and `index` = "
						 + d.getIndex()
						 + " and depth = "
						 + d.getDepth());

				 if (res.next())
					 d.setDB_ID(key, res.getInt("id"));
				 else
					 throw new DBC_SaveException("Dialog "
							 + d
							 + "konnte nicht angelegt werden");
				 res.close();

				 if (d.hasFollowUp()) {
					 res = stmt.executeQuery("SELECT * FROM follow_up");
					 res.moveToInsertRow();
					 res.updateInt("dialog", d.getDB_ID());
					 res.updateInt("start", d.getFollowUpStart().getDB_ID());
					 res.updateInt("end", d.getFollowUpEnd().getDB_ID());
					 res.insertRow();
					 res.close();
				 }

				 if (d.hasRunUp()) {
					 res = stmt.executeQuery("SELECT * FROM run_up");
					 res.moveToInsertRow();
					 res.updateInt("dialog", d.getDB_ID());
					 res.updateInt("start", d.getRunUpStart().getDB_ID());
					 res.updateInt("end", d.getRunUpEnd().getDB_ID());
					 res.insertRow();
					 res.close();
				 }
			 }
			 res.close();
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();

		 saveSpeakerChanges(dialogs);

		 return dialogs;
	 }

	 private void saveSpeakerChanges(Dialogs dialogs)
	 throws Exception {
		 Vector scs = dialogs.getAllSpeakerChanges(key);
		 // Kapitel muss nicht gesetzt werden, da dies schon durch saveDialogs
		 // erledigt wurde;

		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < scs.size(); i++) {
			 SpeakerChange sc = (SpeakerChange) scs.get(i);

			 res = stmt.executeQuery("SELECT * "
					 + "FROM speaker_changes "
					 + "WHERE id = "
					 + sc.getDB_ID());

			 if (res.next() && sc.getDB_ID() != -1) {
				 if (sc.hasChanged()) {
					 res.updateInt("dialog", sc.getDialog().getDB_ID());
					 res.updateInt("speaker_change", sc.getSpeakerChange());
					 res.updateInt("index", sc.getIndex());
					 res.updateBoolean("accepted", sc.isAccepted());
					 res.updateRow();
					 sc.resetState(key);

					 Vector words = sc.getWords();
					 Vector existingWords = new Vector();
					 res = stmt.executeQuery("SELECT * FROM words_in_speaker_change "
							 + "WHERE speaker_change = "
							 + sc.getDB_ID());

					 // lï¿½sche die Wï¿½rter, die nicht mehr im Sprecherwechsel vorkommen
					 while (res.next()) {
						 int wordID = res.getInt("word");
						 Word w = (Word) getElement(wordID, words);
						 if (w == null)
							 res.deleteRow();
						 else
							 existingWords.add(w);
					 }

					 for (int j = 0; j < words.size(); j++) {
						 Word w = (Word) words.get(j);
						 if (!existingWords.contains(w)) {
							 res.moveToInsertRow();
							 res.updateInt("speaker_change", sc.getDB_ID());
							 res.updateInt("word", w.getDB_ID());
							 res.insertRow();
						 }
					 }
				 }
				 else if (sc.isRemoved()) {
					 res.deleteRow();
				 }
			 }

			 else if (!sc.isRemoved() && sc.getDB_ID() == -1) {
				 res.moveToInsertRow();
				 res.updateInt("dialog", sc.getDialog().getDB_ID());
				 res.updateInt("speaker_change", sc.getSpeakerChange());
				 res.updateInt("index", sc.getIndex());
				 res.updateBoolean("accepted", sc.isAccepted());
				 res.insertRow();
				 res.close();
				 sc.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM speaker_changes WHERE dialog = "
						 + sc.getDialog().getDB_ID()
						 + " and `index` = "
						 + sc.getIndex());

				 if (res.next())
					 sc.setDB_ID(key, res.getInt("id"));
				 else
					 throw new DBC_SaveException("Sprecherwechsel "
							 + sc
							 + "konnte nicht angelegt werden");

				 res.close();
				 res = stmt.executeQuery("SELECT * FROM words_in_speaker_change");
				 Vector words = sc.getWords();
				 for (int j = 0; j < words.size(); j++) {
					 Word w = (Word) words.get(j);
					 res.moveToInsertRow();
					 res.updateInt("speaker_change", sc.getDB_ID());
					 res.updateInt("word", w.getDB_ID());
					 res.insertRow();
				 }
			 }
			 res.close();
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();
	 }

	 public synchronized Dialogs loadDialogs(Integer chapterID)
	 throws Exception {
		 Chapter chapter = getChapter(chapterID.intValue());

		 Dialogs dialogs = new Dialogs();
		 Vector ds = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res;

		 // Grunddaten der Dialoge einlesen.
		 res = stmt
		 .executeQuery("SELECT id, `index`, depth, start, end, accepted "
				 + "FROM dialogs WHERE chapter = "
				 + chapter.getDB_ID()
				 + " ORDER BY `index`");

		 while (res.next()) {
			 Dialog dialog = new Dialog(key, res.getInt("id"), chapter, res
					 .getInt("index"), res.getInt("depth"), res
					 .getBoolean("accepted"));
			 dialog.setDialogStart(chapter.getIllocutionUnitWithID(res
					 .getInt("start")));
			 dialog
			 .setDialogEnd(chapter.getIllocutionUnitWithID(res.getInt("end")));
			 ds.add(dialog);
		 }

		 // ï¿½uï¿½erungseinheiten zu dem Vorfeld einlesen.
		 for (int i = 0; i < ds.size(); i++) {
			 Dialog dialog = (Dialog) ds.get(i);
			 res = stmt.executeQuery("SELECT start, end "
					 + "FROM run_up WHERE dialog = "
					 + dialog.getDB_ID());

			 if (res.next()) {
				 dialog.setRunUpStart(chapter.getIllocutionUnitWithID(res
						 .getInt("start")));
				 dialog.setRunUpEnd(chapter.getIllocutionUnitWithID(res
						 .getInt("end")));
			 }
		 }

		 // ï¿½uï¿½erungseinheiten zu dem Nachfeld einlesen.
		 for (int i = 0; i < ds.size(); i++) {
			 Dialog dialog = (Dialog) ds.get(i);
			 res = stmt.executeQuery("SELECT start, end "
					 + "FROM follow_up WHERE dialog = "
					 + dialog.getDB_ID());

			 if (res.next()) {
				 dialog.setFollowUpStart(chapter.getIllocutionUnitWithID(res
						 .getInt("start")));
				 dialog.setFollowUpEnd(chapter.getIllocutionUnitWithID(res
						 .getInt("end")));
			 }
		 }

		 for (int i = 0; i < ds.size(); i++) {
			 Dialog dialog = (Dialog) ds.get(i);
			 res = stmt.executeQuery("SELECT * "
					 + "FROM speaker_changes WHERE dialog = "
					 + dialog.getDB_ID());

			 if (res.next()) {
				 SpeakerChange sc = new SpeakerChange(key, res.getInt("id"), dialog,
						 res.getInt("speaker_change"), res.getInt("index"), res
						 .getBoolean("accepted"));

				 res = stmt.executeQuery("SELECT word FROM words_in_speaker_change "
						 + "WHERE speaker_change = "
						 + sc.getDB_ID());
				 while (res.next()) {
					 Word w = chapter.getWordWithID(res.getInt("word"));
					 sc.addWord(w);
				 }
				 sc.resetState(key);
			 }
		 }

		 for (int i = 0; i < ds.size(); i++) {
			 Dialog dialog = (Dialog) ds.get(i);
			 dialogs.add(dialog);
		 }

		 stmt.close();
		 return dialogs;
	 }

	 public synchronized IllocutionUnitRoots loadIllocutionUnitRoots(Integer chapterID)
	 throws Exception {
		 Chapter chapter = getChapter(chapterID.intValue());
		 Vector roots = makeIllocutionUnitRoots(chapter);

		 Vector fws = loadFunctionWords(chapter, roots);
		 Vector cws = loadConstitutiveWords(chapter, roots);
		 Vector mus = loadMeaningUnits(chapter, roots, fws, cws);
		 loadSememeGroups(chapter, roots, mus, fws);

		 IllocutionUnitRoots iurs = new IllocutionUnitRoots(key, chapter, roots);

		 loadCheckings(chapter, iurs, mus);
		 loadMacroSentences(chapter, iurs);

		 return iurs;
	 }

	 private Vector makeIllocutionUnitRoots(Chapter chapter)
	 throws Exception {
		 Vector ius = chapter.getIllocutionUnits();
		 Vector roots = new Vector(ius.size());
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * FROM illocution_units "
				 + "WHERE chapter = "
				 + chapter.getDB_ID());
		 while (res.next()) {
			 int start = res.getInt("start");
			 int path = res.getInt("path");
			 IllocutionUnit iu = chapter.getIllocutionUnitAtPosition(start);
			 roots.add(new IllocutionUnitRoot(key, iu, path));
		 }
		 stmt.close();
		 return roots;
	 }

	 private Vector<FunctionWord> loadFunctionWords(Chapter chapter, Vector<IllocutionUnitRoot> roots)
	 throws Exception {
		 Statement stmt = connection.createStatement();
		 Vector<FunctionWord> fwords = new Vector<FunctionWord>();

		 ResultSet res = stmt.executeQuery("SELECT function_words.id, "
				 + "word, start, end, accepted "
				 + "FROM function_words "
				 + "WHERE chapter = "
				 + chapter.getDB_ID());
		 while (res.next()) {
			 Token token = chapter.getTokenAtPosition(res.getInt("start"));
			 IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(token
					 .getIllocutionUnit().getDB_ID(), roots);
			 if (token instanceof Word) {
				 fwords.add(new FunctionWord(key, root, res.getInt("id"),
						 (Word) token, res.getInt("start"), res.getInt("end"), res
						 .getBoolean("accepted")));
			 }
		 }

		 stmt.close();
		 return fwords;
	 }

	 private TR_Assignation loadAssignation(int id) throws SQLException
	 {
		 PreparedStatement stmt = null;
		 ResultSet res = null;
		 TR_Assignation_DB assignation = null;
		 try {
			 stmt = connection.prepareStatement(

					 "SELECT * FROM assignations WHERE id = ?"
			 );
			 stmt.setInt(1, id);
			 res = stmt.executeQuery();
			 if(res.next()) {
				 assignation = new TR_Assignation().new TR_Assignation_DB();
				 assignation.setCasesBinary( res.getBytes("tr_case"));
				 assignation.setConjugationsBinary(res.getBytes("tr_conjugation"));
				 assignation.setDeterminationsBinary(res.getBytes("tr_determination"));
				 assignation.setDiathesesBinary(res.getBytes("tr_diathese"));
				 assignation.setNumeriBinary(res.getBytes("tr_numerus"));
				 assignation.setPersonsBinary(res.getBytes("tr_person"));
				 assignation.setTemporaBinary(res.getBytes("tr_tempus"));
				 assignation.setWordclassesBinary(res.getBytes("tr_wordclass"));
				 assignation.setDB_ID(key, res.getInt("id"));
				 assignation.resetState(key);
			 }
		 }

		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return assignation;
	 }

	 private Vector<ConstitutiveWord> loadConstitutiveWords(Chapter chapter, Vector<IllocutionUnitRoot> roots) throws Exception
	 {
		 PreparedStatement stmt = null;
		 ResultSet res = null;
		 Vector<ConstitutiveWord> cwords = new Vector<ConstitutiveWord>();
		 try {
			 stmt = connection.prepareStatement(

					 "SELECT * FROM constitutive_words WHERE chapter = ?"
			 );
			 stmt.setInt(1, chapter.getDB_ID());
			 res = stmt.executeQuery();
			 while (res.next()) {
				 Token token = chapter.getTokenAtPosition(res.getInt("start"));
				 IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(token
						 .getIllocutionUnit().getDB_ID(), roots);
				 if (token instanceof Word) {
					 ConstitutiveWord cw = new ConstitutiveWord(key, root, res.getInt("id"),
							 (Word) token, res.getInt("start"), res.getInt("end"), res.getBoolean("accepted"), res.getInt("lexprag_path"),
							 res.getInt("lexprag_level"), res.getInt("text_gr_path"), res.getInt("sem_path"));

					 TR_Assignation assi = loadAssignation(res.getInt("assignation_id"));
					 if(assi != null) {
//						 assi.setContent(cw.getContent());
						 cw.setAssignation(assi);
					 }

					 cwords.add(cw);
				 }
			 }
		 }

		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return cwords;
	 }

	 private Vector loadMeaningUnits(Chapter chapter,
			 Vector roots,
			 Vector funtionWords,
			 Vector constetutiveWords)
	 throws Exception {
		 Vector mus = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * FROM meaning_units "
				 + "WHERE illocution_unit in"
				 + "(SELECT id FROM illocution_units WHERE chapter = "
				 + chapter.getDB_ID()
				 + ")");

		 while (res.next()) {
			 IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(res
					 .getInt("illocution_unit"), roots);
			 FunctionWord fw = (FunctionWord) getElement(res
					 .getInt("function_word"), funtionWords);
			 ConstitutiveWord cw = (ConstitutiveWord) getElement(res
					 .getInt("constitutive_word"), constetutiveWords);
			 MeaningUnit mu = new MeaningUnit(key, root, res.getInt("id"), fw, cw,
					 res.getInt("path"), res.getBoolean("accepted"));
			 mus.add(mu);
		 }

		 stmt.close();
		 return mus;
	 }

	 private Vector loadSememeGroups(Chapter chapter,
			 Vector roots,
			 Vector meaningUnits,
			 Vector funtionWords)
	 throws Exception {

		 Vector sgs = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * FROM sememe_groups "
				 + "WHERE illocution_unit in"
				 + "(SELECT id FROM illocution_units WHERE chapter = "
				 + chapter.getDB_ID()
				 + ")");

		 while (res.next()) {
			 IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(res
					 .getInt("illocution_unit"), roots);
			 FunctionWord fw = (FunctionWord) getElement(res
					 .getInt("function_word"), funtionWords);
			 MeaningUnit mu1 = (MeaningUnit) getElement(res
					 .getInt("meaning_unit_1"), meaningUnits);
			 MeaningUnit mu2 = (MeaningUnit) getElement(res
					 .getInt("meaning_unit_2"), meaningUnits);
			 SememeGroup sg = new SememeGroup(key, root, res.getInt("id"), fw, mu1,
					 mu2, res.getInt("path"), res.getBoolean("accepted"));
			 sgs.add(sg);
		 }

		 stmt.close();

		 return sgs;
	 }

	 private Vector loadCheckings(Chapter chapter,
			 IllocutionUnitRoots iurs,
			 Vector mus)
	 throws Exception {
		 Vector chs = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * "
				 + "FROM checkings "
				 + "WHERE chapter = "
				 + chapter.getDB_ID());

		 while (res.next())
			 chs.add(new Checking(key, res.getInt("id"), iurs,
					 (MeaningUnit) getElement(res.getInt("meaning_unit"), mus), res
					 .getInt("path"), res.getBoolean("accepted")));
		 res.close();
		 stmt.close();
		 return chs;
	 }

	 private Vector loadMacroSentences(Chapter chapter, IllocutionUnitRoots iurs)
	 throws Exception {
		 Vector mss = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * "
				 + "FROM macro_sentences "
				 + "WHERE chapter = "
				 + chapter.getDB_ID());

		 while (res.next())
			 mss.add(new MacroSentence(key, res.getInt("id"), iurs, iurs
					 .getRootWithID(res.getInt("head")), res.getInt("path"), res
					 .getBoolean("accepted")));
		 res.close();

		 for (int i = 0; i < mss.size(); i++) {
			 MacroSentence ms = (MacroSentence) mss.get(i);
			 res = stmt.executeQuery("SELECT * FROM macro_sentences_dependencies "
					 + "WHERE macro_sentence = "
					 + ms.getDB_ID());
			 while (res.next())
				 ms.addDependency(iurs.getRoot(chapter.getIllocutionUnitWithID(res
						 .getInt("depending_illocution_unit"))));
			 res.close();
		 }

		 stmt.close();
		 return mss;
	 }

	 public synchronized IllocutionUnitRoots saveIllocutionUnitRoots(Integer chapterID,
			 IllocutionUnitRoots iurs)
	 throws Exception {

		 iurs.setChapter(key, getChapter(chapterID.intValue()));
		 saveIllocutionUnitRoots(iurs.getRoots());
		 saveFunctionWords(iurs.getFunctionWords(key));
		 saveConstitutiveWords(iurs.getConstitutiveWords(key));
		 saveMeaningUnits(iurs.getMeaningUnits(key));
		 saveSememeGroups(iurs.getSememeGroups(key));
		 saveCheckings(iurs.getCheckings(key));
		 saveMacroSentences(iurs.getMacroSentences(key));

		 return iurs;
	 }

	 private void saveIllocutionUnitRoots(Vector roots)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < roots.size(); i++) {
			 IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);

			 if (root.isUnchanged())
				 continue;

			 res = stmt.executeQuery("SELECT * "
					 + "FROM illocution_units "
					 + "WHERE id = "
					 + root.getIllocutionUnit().getDB_ID());
			 if (res.next() && root.hasChanged()) {
				 res.updateInt("path", root.getPath());
				 res.updateInt("numerus_paths", root.getNumerusPath());
				 res.updateInt("phrastic", root.getPhrastic());
				 res.updateRow();
				 res.close();
				 root.resetState(key);
			 }

			 res.close();
			 connection.commit();
		 }

		 stmt.close();
		 connection.setAutoCommit(true);
	 }

	 private void saveFunctionWords(Vector words)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < words.size(); i++) {

			 if (!(words.get(i) instanceof FunctionWord))
				 continue;

			 FunctionWord word = (FunctionWord) words.get(i);

			 if (word.getWord() == null) {
				 System.err.println("Funktionswort "
						 + word
						 + "("
						 + word.getDB_ID()
						 + ", "
						 + word.getState()
						 + ")"
						 + " ist keinem Wort zugeordnet!");
				 continue;
			 }

			 if (word.isUnchanged())
				 continue;

			 res = stmt.executeQuery("SELECT * "
					 + "FROM function_words "
					 + "WHERE id = "
					 + word.getDB_ID());

			 if (res.next() && word.getDB_ID() != -1) {
				 if (word.hasChanged()) {
					 res.updateInt("chapter", word.getWord().getChapter().getDB_ID());
					 res.updateInt("word", word.getWord().getDB_ID());
					 res.updateInt("start", word.getStartPosition());
					 res.updateInt("end", word.getEndPosition());
					 res.updateBoolean("accepted", word.isAccepted());
					 res.updateRow();
					 res.close();
					 word.resetState(key);
				 }
				 else if (word.isRemoved())
					 res.deleteRow();
			 }

			 else if (!word.isRemoved() && word.getDB_ID() == -1) {
				 res.moveToInsertRow();
				 res.updateInt("chapter", word.getWord().getChapter().getDB_ID());
				 res.updateInt("word", word.getWord().getDB_ID());
				 res.updateInt("start", word.getStartPosition());
				 res.updateInt("end", word.getEndPosition());
				 res.updateBoolean("accepted", word.isAccepted());
				 res.insertRow();
				 res.close();
				 word.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM function_words WHERE word = "
						 + word.getWord().getDB_ID()
						 + " and chapter = "
						 + word.getWord().getChapter().getDB_ID()
						 + " and start = "
						 + word.getStartPosition()
						 + " and end = "
						 + word.getEndPosition());

				 // hole die DB-ID zu dem neu angelegten FW
				 if (res.next())
					 word.setDB_ID(key, res.getInt("id"));

				 else
					 throw new DBC_SaveException("Funktionswort "
							 + word
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");
			 }

			 else
				 System.err.println("Funktionswort "
						 + word
						 + "("
						 + word.getDB_ID()
						 + ", "
						 + word.getState()
						 + ")"
						 + " hat beim speichern einen Fehler verursacht!");

			 res.close();
			 connection.commit();
		 }

		 stmt.close();
		 connection.setAutoCommit(true);

	 }

	 private void saveConstitutiveWords(Vector<ConstitutiveWord> words) throws SQLException
	 {	  
		 logger.entering(this.getClass().getName(), "saveConstitutiveWords", words);
		 if(words == null) {
			 logger.warning("unexpected null pointer as method parameter");
			 return;
		 }

		 PreparedStatement stmt = null;
		 ResultSet res = null;

		 try {
			 connection.setAutoCommit(false);
			 stmt = connection.prepareStatement(

					 "SELECT * FROM constitutive_words WHERE id = ?",

					 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE
			 );
			 for (ConstitutiveWord constitutiveWord : words) {

				 // Check for exceptions
				 if(constitutiveWord == null) {
					 logger.warning("constitutiveWord is unexpectedly a null pointer");
					 continue;
				 }

				 if (constitutiveWord.getWord() == null) {
					 logger.warning("constitutiveWord has unexpectedly no associated word");
					 continue;
				 }

				 // Get assignation
				 TR_Assignation assi = constitutiveWord.getAssignation();              

				 stmt.setInt(1, constitutiveWord.getDB_ID());			   
				 res = stmt.executeQuery();

				 if (constitutiveWord.hasChanged()) {
					 if(assi != null)
						 saveAssignations(assi);
					 if(! res.next()) {
						 res.moveToInsertRow();
					 }
					 res.updateInt("chapter", constitutiveWord.getWord().getChapter().getDB_ID());
					 res.updateInt("word", constitutiveWord.getWord().getDB_ID());
					 res.updateInt("start", constitutiveWord.getStartPosition());
					 res.updateInt("end", constitutiveWord.getEndPosition());
					 res.updateBoolean("accepted", constitutiveWord.isAccepted());
					 res.updateInt("lexprag_path", constitutiveWord.getLexpragPath());
					 res.updateInt("lexprag_level", constitutiveWord.getLexpragLevel());
					 res.updateInt("text_gr_path", constitutiveWord.getTextGrPath());
					 res.updateInt("sem_path", constitutiveWord.getSemPath());
					 if(assi != null)
						 res.updateInt("assignation_id", constitutiveWord.getAssignation().getDB_ID());
					 if(res.isFirst()) {
						 res.updateRow();
					 }
					 else {
						 res.insertRow();
						 res.last();
						 constitutiveWord.setDB_ID(key, res.getInt("id"));
					 }

					 constitutiveWord.resetState(key);
				 }
				 else if(constitutiveWord.isRemoved() && res.next()) {
					 res.deleteRow();
					 if(assi != null) {
						 assi.remove();
						 saveAssignations(assi);
					 }
				 }			
				 connection.commit();
			 }
		 }
		 catch ( SQLException e ) {
			 if(connection != null)
				 connection.rollback();
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 if(connection != null)
					 connection.setAutoCommit(true);
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }


	 }

	 private void saveMeaningUnits(Vector meaningUnits)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < meaningUnits.size(); i++) {
			 MeaningUnit mu = (MeaningUnit) meaningUnits.get(i);

			 if (mu.isUnchanged())
				 continue;

			 res = stmt.executeQuery("SELECT * FROM "
					 + "meaning_units WHERE id = "
					 + mu.getDB_ID());

			 if (res.next()) {
				 if (mu.hasChanged()) {
					 res.updateInt("illocution_unit", mu.getRoot()
							 .getIllocutionUnit().getDB_ID());
					 if (mu.getFunctionWord() != null)
						 res.updateInt("function_word", mu.getFunctionWord()
								 .getDB_ID());
					 else
						 res.updateNull("function_word");
					 res.updateInt("constitutive_word", mu.getConstitutiveWord()
							 .getDB_ID());
					 res.updateInt("path", mu.getPath());
					 res.updateBoolean("accepted", mu.isAccepted());
					 res.updateRow();
					 mu.resetState(key);
				 }
				 else if (mu.isRemoved())
					 res.deleteRow();
			 }
			 else if (!mu.isRemoved()) {
				 res.moveToInsertRow();
				 res.updateInt("illocution_unit", mu.getRoot().getIllocutionUnit()
						 .getDB_ID());
				 if (mu.getFunctionWord() != null)
					 res.updateInt("function_word", mu.getFunctionWord().getDB_ID());
				 res.updateInt("constitutive_word", mu.getConstitutiveWord()
						 .getDB_ID());
				 res.updateInt("path", mu.getPath());
				 res.updateBoolean("accepted", mu.isAccepted());
				 res.insertRow();
				 res.close();
				 mu.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM meaning_units WHERE illocution_unit = "
						 + mu.getRoot().getIllocutionUnit().getDB_ID()
						 + " and constitutive_word = "
						 + mu.getConstitutiveWord().getDB_ID());

				 if (res.next())
					 mu.setDB_ID(key, res.getInt("id"));

				 else
					 throw new DBC_SaveException("Semantische Einheit "
							 + mu
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");
			 }
			 res.close();
			 // schreibe ï¿½nderung in die DB
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();
	 }

	 private void saveSememeGroups(Vector sememeGroups)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < sememeGroups.size(); i++) {
			 SememeGroup sg = (SememeGroup) sememeGroups.get(i);

			 if (sg.isUnchanged())
				 continue;

			 res = stmt.executeQuery("SELECT * FROM "
					 + "sememe_groups WHERE id = "
					 + sg.getDB_ID());

			 // schon in der DB vorhanden
			 if (res.next()) {
				 if (sg.hasChanged()) {
					 res.updateInt("illocution_unit", sg.getRoot()
							 .getIllocutionUnit().getDB_ID());
					 if (sg.getFunctionWord() != null)
						 res.updateInt("function_word", sg.getFunctionWord()
								 .getDB_ID());
					 else
						 res.updateNull("function_word");
					 res.updateInt("meaning_unit_1", sg.getFirst().getDB_ID());
					 res.updateInt("meaning_unit_2", sg.getSecond().getDB_ID());
					 res.updateInt("path", sg.getPath());
					 res.updateBoolean("accepted", sg.isAccepted());
					 res.updateRow();
					 sg.resetState(key);
				 }
				 else if (sg.isRemoved())
					 res.deleteRow();
			 }
			 // muss gespeichert werden
			 else if (!sg.isRemoved()) {
				 res.moveToInsertRow();
				 res.updateInt("illocution_unit", sg.getRoot().getIllocutionUnit()
						 .getDB_ID());
				 if (sg.getFunctionWord() != null)
					 res.updateInt("function_word", sg.getFunctionWord().getDB_ID());
				 else
					 res.updateNull("function_word");
				 res.updateInt("meaning_unit_1", sg.getFirst().getDB_ID());
				 res.updateInt("meaning_unit_2", sg.getSecond().getDB_ID());
				 res.updateInt("path", sg.getPath());
				 res.updateBoolean("accepted", sg.isAccepted());
				 res.insertRow();
				 res.close();
				 sg.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM sememe_groups WHERE meaning_unit_1 = "
						 + sg.getFirst().getDB_ID()
						 + " and meaning_unit_2 = "
						 + sg.getSecond().getDB_ID());

				 if (res.next())
					 sg.setDB_ID(key, res.getInt("id"));

				 else
					 throw new DBC_SaveException("Sememegruppe "
							 + sg
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");
			 }
			 res.close();
			 // schreibe ï¿½nderung in die DB
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();

	 }

	 private void saveCheckings(Vector checkings)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < checkings.size(); i++) {
			 Checking ch = (Checking) checkings.get(i);

			 if (ch.isUnchanged())
				 continue;

			 res = stmt.executeQuery("SELECT * FROM "
					 + "checkings WHERE id = "
					 + ch.getDB_ID());

			 // schon in der DB vorhanden
			 if (res.next()) {
				 if (ch.hasChanged()) {
					 res.updateInt("meaning_unit", ch.getMeaningUnit().getDB_ID());
					 res.updateInt("chapter", ch.getRoot().getChapter().getDB_ID());
					 res.updateInt("path", ch.getPath());
					 res.updateBoolean("accepted", ch.isAccepted());
					 res.updateRow();
					 ch.resetState(key);
				 }
				 else if (ch.isRemoved())
					 res.deleteRow();
			 }
			 // muss gespeichert werden
			 else if (!ch.isRemoved()) {
				 res.moveToInsertRow();
				 res.updateInt("meaning_unit", ch.getMeaningUnit().getDB_ID());
				 res.updateInt("chapter", ch.getRoot().getChapter().getDB_ID());
				 res.updateInt("path", ch.getPath());
				 res.updateBoolean("accepted", ch.isAccepted());
				 res.insertRow();
				 res.close();
				 ch.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM checkings WHERE meaning_unit = "
						 + ch.getMeaningUnit().getDB_ID());

				 if (res.next())
					 ch.setDB_ID(key, res.getInt("id"));

				 else
					 throw new DBC_SaveException("Checking "
							 + ch
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");
			 }
			 res.close();
			 // schreibe ï¿½nderung in die DB
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();
	 }

	 private void saveMacroSentences(Vector macroSentences)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < macroSentences.size(); i++) {
			 MacroSentence ms = (MacroSentence) macroSentences.get(i);

			 if (ms.isUnchanged())
				 continue;

			 res = stmt.executeQuery("SELECT * FROM "
					 + "macro_sentences WHERE id = "
					 + ms.getDB_ID());

			 // schon in der DB vorhanden
			 if (res.next()) {
				 if (ms.hasChanged()) {
					 res.updateInt("head", ms.getHead().getDB_ID());
					 res.updateInt("chapter", ms.getHead().getChapter().getDB_ID());
					 res.updateInt("path", ms.getPath());
					 res.updateBoolean("accepted", ms.isAccepted());
					 res.updateRow();
					 ms.resetState(key);

					 res = stmt.executeQuery("SELECT * FROM "
							 + "macro_sentences_dependencies "
							 + "WHERE macro_sentence = "
							 + ms.getDB_ID());
					 Vector dep = ms.getDependencies();
					 for (int j = 0; j < dep.size(); j++) {
						 IllocutionUnitRoot iur = (IllocutionUnitRoot) dep.get(j);
						 try {
							 res.moveToInsertRow();
							 res.updateInt("macro_sentence", ms.getDB_ID());
							 res.updateInt("depending_illocution_unit", iur.getDB_ID());
							 res.insertRow();
						 }
						 catch (Exception e) {
							 continue;
						 }
					 }
				 }
				 else if (ms.isRemoved())
					 res.deleteRow();
			 }
			 // muss gespeichert werden
			 else if (!ms.isRemoved()) {
				 res.moveToInsertRow();
				 res.updateInt("head", ms.getHead().getDB_ID());
				 res.updateInt("chapter", ms.getHead().getChapter().getDB_ID());
				 res.updateInt("path", ms.getPath());
				 res.updateBoolean("accepted", ms.isAccepted());
				 res.insertRow();
				 res.close();
				 ms.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM macro_sentences WHERE checking = "
						 + ms.getHead().getDB_ID());

				 if (res.next())
					 ms.setDB_ID(key, res.getInt("id"));

				 else
					 throw new DBC_SaveException("Makrosatz "
							 + ms
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");

				 res = stmt.executeQuery("SELECT * FROM "
						 + "macro_sentences_dependencies "
						 + "WHERE macro_sentence = "
						 + ms.getDB_ID());
				 Vector dep = ms.getDependencies();
				 for (int j = 0; j < dep.size(); j++) {
					 IllocutionUnitRoot iur = (IllocutionUnitRoot) dep.get(j);
					 res.moveToInsertRow();
					 res.updateInt("macro_sentence", ms.getDB_ID());
					 res.updateInt("depending_illocution_unit", iur.getDB_ID());
					 res.insertRow();
				 }
			 }
			 res.close();
			 // schreibe ï¿½nderung in die DB
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();
	 }

	 public synchronized Vector loadFunctionWords()
	 throws Exception {
		 Statement stmt = connection.createStatement();
		 Vector fwords = new Vector();

		 ResultSet res = stmt.executeQuery("SELECT content "
				 + "FROM words WHERE id IN "
				 + "(SELECT distinct word "
				 + "FROM function_words)");
		 while (res.next())
			 fwords.add(new String(res.getBytes("content")));

		 stmt.close();
		 return fwords;
	 }

	 public synchronized Vector loadFunctionWordsCategories()
	 throws Exception {
		 Statement stmt = connection.createStatement();

		 // Anzahl der Kategorien bestimmen
		 ResultSet res = stmt.executeQuery("SELECT count(*) "
				 + "FROM function_words_categories");
		 res.next();
		 Vector cats = new Vector(res.getInt(1));

		 // Kategorien auslesen.
		 res = stmt.executeQuery("SELECT category FROM "
				 + "function_words_categories "
				 + "ORDER BY category");
		 while (res.next())
			 cats.add(res.getString("category"));

		 stmt.close();
		 return cats;
	 }

	 public synchronized Vector loadConstitutiveWords()
	 throws Exception {
		 Statement stmt = connection.createStatement();
		 Vector cwords = new Vector();

		 ResultSet res = stmt.executeQuery("SELECT content "
				 + "FROM words WHERE id in "
				 + "(SELECT distinct word "
				 + "FROM constitutive_words)");
		 while (res.next())
			 cwords.add(new String(res.getBytes("content")));

		 stmt.close();
		 return cwords;
	 }

	 private PathNode loadPaths() {
		 PathNode root = new PathNode(key, 0, null, "pathroot", "---");
		 try {
			 Statement stmt = connection.createStatement();
			 ResultSet res = stmt
			 .executeQuery("SELECT * FROM paths WHERE id > 0 ORDER BY parent");

			 while (res.next()) {
				 PathNode parent = root.getNode(res.getInt("parent"));
				 new PathNode(key, res.getInt("id"), parent, res.getString("name"),
						 res.getString("description"));
			 }

			 stmt.close();
		 }
		 catch (SQLException e) {
			 e.printStackTrace();
		 }
		 return root;
	 }
	 
	 private PathNode loadNumerusPaths() {
		PathNode root = new PathNode(key, 0, null, "numerusPathRoot", "---");
		try {
			Statement stmt = connection.createStatement();
			ResultSet res = stmt
					.executeQuery("select * from numerus_paths where id > 0 order by parent");

			while (res.next()) {
				PathNode parent = root.getNode(res.getInt("parent"));
				new PathNode(key, res.getInt("id"), parent, res
						.getString("name"), res.getString("description"));
			}

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return root;
	}

	 public PathNode getPaths() {
		 return root;
	 }

	 public PathNode getNumerusPaths() {
	     return numerusRoot;
	 }

	 public synchronized Boolean existsFunctionWord(Integer wordID, Integer length)
	 throws Exception {
		 Boolean result = new Boolean(false);
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * FROM function_words "
				 + "WHERE word = "
				 + wordID.intValue());

		 while (res.next()) {
			 int start = res.getInt("start");
			 int end = res.getInt("end");
			 if (end - start == length.intValue()) {
				 result = new Boolean(true);
				 break;
			 }
		 }

		 stmt.close();
		 return result;
	 }

	 public synchronized Boolean existsConstitutiveWord(Integer wordID,
			 Integer length)
	 throws Exception {
		 Boolean result = new Boolean(false);
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * FROM constitutive_words "
				 + "WHERE word = "
				 + wordID.intValue());

		 while (res.next()) {
			 int start = res.getInt("start");
			 int end = res.getInt("end");
			 if (end - start == length.intValue()) {
				 result = new Boolean(true);
				 break;
			 }
		 }

		 stmt.close();
		 return result;
	 }

	 public synchronized Vector getAllConstitutiveWords(String language)
	 throws Exception {
		 Vector result = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt
		 .executeQuery("SELECT DISTINCT "
				 + "SUBSTRING(CAST(content AS CHAR), start-position+1, end-position+1) AS content "
				 + "FROM constitutive_words AS cws, words, words_in_chapter AS winc "
				 + "WHERE words.id = cws.word "
				 + "AND words.id = winc.word "
				 + "AND winc.chapter = cws.chapter "
				 + "AND winc.position <= cws.start "
				 + "AND cws.end <= winc.position + length(content) "
				 + "AND words.language = '"
				 + language
				 + "' ORDER BY content");

		 while (res.next()) {
			 DB_Tupel tupel = new DB_Tupel();
			 tupel.put("content", res.getString("content"));
			 result.add(tupel);
		 }

		 stmt.close();
		 return result;
	 }

	 public synchronized Vector getAllFunctionWords(String language)
	 throws Exception {
		 Vector result = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT DISTINCT "
				 + "SUBSTRING(CAST(content AS CHAR), start-position+1, end-position+1) AS string "
				 + "FROM function_words AS fws, words, words_in_chapter AS winc "
				 + "WHERE words.id = fws.word "
				 + "AND words.id = winc.word "
				 + "AND winc.chapter = fws.chapter "
				 + "AND winc.position <= fws.start "
				 + "AND fws.end <= winc.position + length(content) "
				 + "AND words.language = '"
				 + language
				 + "' ORDER BY string");

		 while (res.next())
			 result.add(res.getString("string"));

		 stmt.close();
		 return result;
	 }

	 public synchronized Vector<DB_Tupel> getConstitutiveWords(String word, String language)
	 throws Exception {
		 Vector<DB_Tupel> result = new Vector<DB_Tupel>();
		 Statement stmt = connection.createStatement();
		 String query = word;

		 if (word.length() > 6)
			 query = "%" + word.substring(0, word.length() - 2) + "%";
		 else if (word.length() > 3)
			 query = "%" + word + "%";

		 ResultSet res = stmt
		 .executeQuery("SELECT cws.chapter, content, position, start, end, "
				 + "lexprag_path, lexprag_level, "
				 + "text_gr_path, sem_path "
				 + "FROM constitutive_words AS cws, words, words_in_chapter AS winc "
				 + "WHERE words.id = cws.word "
				 + "AND words.id = winc.word "
				 + "AND winc.chapter = cws.chapter "
				 + "AND winc.position <= cws.start "
				 + "AND cws.end <= winc.position+length(content) "
				 + "AND words.id IN " + String.format("(SELECT id FROM words WHERE content LIKE '%1$s' AND language = '%2$s')"
						 , query, language)
		 );

		 while (res.next()) {
			 String content = new String(res.getBytes("content"));
			 int position = res.getInt("position");
			 int start = res.getInt("start");
			 int end = res.getInt("end");

			 content = content.substring(start - position, Math.min(end
					 - position
					 + 1, content.length()));

			 if (content.toLowerCase().matches(".*" + word.toLowerCase() + ".*")
					 || word.toLowerCase().matches(".*"
							 + content.toLowerCase()
							 + ".*")) {
				 DB_Tupel tupel = new DB_Tupel();
				 tupel.put("content", content);
				 tupel.put("start", start - position);
				 tupel.put("end", end - position);
				 tupel.put("chapter", res.getInt("cws.chapter"));
				 tupel.put("position", position);
				 tupel.put("lexprag_path", res.getInt("lexprag_path"));
				 tupel.put("lexprag_level", res.getInt("lexprag_level"));
				 tupel.put("text_gr_path", res.getInt("text_gr_path"));
				 tupel.put("sem_path", res.getInt("sem_path"));
				 result.add(tupel);
			 }
		 }

		 stmt.close();
		 return result;
	 }

	 public synchronized Vector getFunctionWords(String word, String language)
	 throws Exception {
		 Vector result = new Vector();
		 Statement stmt = connection.createStatement();

		 ResultSet res = stmt
		 .executeQuery("SELECT fws.chapter, content, position, start, end "
				 + "FROM function_words AS fws, words, words_in_chapter AS winc "
				 + "WHERE words.id = fws.word "
				 + "AND words.id = winc.word "
				 + "AND winc.chapter = fws.chapter "
				 + "AND winc.position <= fws.start "
				 + "AND fws.end <= winc.position + LENGTH(content) "
				 + "AND words.id IN " + String.format("(SELECT id FROM words WHERE content LIKE '%1$s' AND language = '%2$s')"
						 , word, language)
		 );

		 while (res.next()) {
			 String content = new String(res.getBytes("content"));
			 int position = res.getInt("position");
			 int start = res.getInt("start");
			 int end = res.getInt("end");

			 DB_Tupel tupel = new DB_Tupel();
			 tupel.put("content", content.substring(start - position, Math.min(end
					 - position
					 + 1, content.length())));
			 tupel.put("start", start - position);
			 tupel.put("end", end - position);
			 tupel.put("chapter", res.getInt("fws.chapter"));
			 tupel.put("position", position);
			 result.add(tupel);
		 }

		 stmt.close();
		 return result;
	 }

	 public synchronized Vector loadThemas(Integer chapterID)
	 throws Exception {
		 Vector themas = new Vector();
		 Chapter chapter = getChapter(chapterID.intValue());

		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT * FROM themas "
				 + "WHERE chapter = "
				 + chapter.getDB_ID());

		 // erst alle Themas einlesen
		 while (res.next()) {
			 themas.add(new Thema_DB(key, chapter, res.getInt("id"), res
					 .getInt("first_occurrence"), res.getString("lemma"), res
					 .getString("description"), res.getBoolean("is_rhema")));
		 }

		 // Referenzen setzen und Vorkommen einlesen
		 for (int i = 0; i < themas.size(); i++) {
			 Thema_DB thema = ((Thema_DB) themas.get(i));

			 Thema_DB firstOccurrence = null;
			 for (int j = 0; j < themas.size(); j++) {
				 Thema_DB t = ((Thema_DB) themas.get(j));
				 if (t.getDB_ID() == thema.getFirstOccurrenceID()) {
					 firstOccurrence = t;
					 break;
				 }
			 }
			 thema.setFirstOccurrence(key, firstOccurrence);

			 res = stmt.executeQuery("SELECT * FROM thema_occurrences "
					 + "WHERE thema = "
					 + thema.getDB_ID());
			 while (res.next())
				 thema.addOccurrence(res.getInt("start"), res.getInt("end"));
		 }

		 return themas;
	 }

	 public synchronized void saveThemas(Integer chapterID, Vector themas)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 stmt.executeUpdate("delete FROM themas WHERE chapter = " + chapterID);
		 ResultSet res = stmt.executeQuery("SELECT * FROM themas WHERE chapter = "
				 + chapterID);

		 // alle Themas speichern
		 for (int i = 0; i < themas.size(); i++) {
			 Thema_DB thema = (Thema_DB) themas.get(i);
			 res.moveToInsertRow();
			 res.updateInt("chapter", chapterID.intValue());
			 res.updateInt("index", i);
			 res.updateString("lemma", thema.getLemma());
			 res.updateString("description", thema.getDescription());
			 res.updateBoolean("is_rhema", thema.isRhema());
			 res.insertRow();
		 }

		 // IDs auslesen und setzen
		 res = stmt.executeQuery("SELECT * FROM themas WHERE chapter = "
				 + chapterID);
		 while (res.next()) {
			 Thema_DB thema = (Thema_DB) themas.get(res.getInt("index"));
			 thema.setDB_ID(key, res.getInt("id"));
		 }

		 // Referenzen speichern
		 res = stmt.executeQuery("SELECT * FROM themas WHERE chapter = "
				 + chapterID);
		 while (res.next()) {
			 Thema_DB thema = (Thema_DB) themas.get(res.getInt("index"));
			 if (thema.getFirstOccurrence() != null)
				 res.updateInt("first_occurrence", thema.getFirstOccurrence()
						 .getDB_ID());
			 res.updateRow();
		 }

		 // Vorkommen speichern
		 res = stmt.executeQuery("SELECT * FROM thema_occurrences");
		 for (int i = 0; i < themas.size(); i++) {
			 Thema_DB thema = (Thema_DB) themas.get(i);
			 Vector ocs = thema.getOccurrences();
			 for (int j = 0; j < ocs.size(); j++) {
				 Occurrence_DB oc = (Occurrence_DB) ocs.get(j);
				 res.moveToInsertRow();
				 res.updateInt("thema", thema.getDB_ID());
				 res.updateInt("start", oc.getStartIndex());
				 res.updateInt("end", oc.getEndIndex());
				 res.insertRow();
			 }
		 }

		 stmt.close();
		 connection.commit();
		 connection.setAutoCommit(true);
	 }

	 /**
	  * Speichert die Isotopien zu diesem Kapitel.
	  * 
	  * @throws DBC_SaveException
	  */
	 public Isotopes saveIsotopes(Isotopes isotopes, Integer chapterID)
	 throws Exception {
		 Chapter chapter = getChapter(chapterID.intValue());
		 isotopes.setChapter(key, chapter);
		 Vector isos = isotopes.getAllIsotopes(key);
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < isos.size(); i++) {
			 Isotope isotope = (Isotope) isos.get(i);

			 // Suche die ID zu der Kategorie
			 int categoryID = -1;
			 res = stmt.executeQuery("SELECT id "
					 + "FROM isotope_categories "
					 + "WHERE category like binary '"
					 + isotope.getCategory()
					 + "'");
			 if (res.next()) {
				 categoryID = res.getInt("id");
				 res.close();
			 }
			 else {
				 res.close();
				 stmt.executeUpdate("insert into "
						 + "isotope_categories (category) "
						 + "values('"
						 + isotope.getCategory()
						 + "')");
				 i--;
				 continue;
			 }

			 // Prï¿½fe, ob schon ein Tupel mit der ID in der DB
			 // gespeichert ist
			 res = stmt.executeQuery("SELECT * "
					 + "FROM isotopes WHERE id = "
					 + isotope.getDB_ID());

			 // Tupel ist vorhanden...
			 if (res.next()) {
				 // ...und wird aktualisiert
				 if (isotope.hasChanged()) {
					 res.updateInt("category", categoryID);
					 res.updateInt("word", isotope.getWord().getDB_ID());
					 res.updateInt("chapter", isotope.getChapter().getDB_ID());
					 res.updateInt("index", isotope.getWord().getIndex());
					 res.updateRow();
					 isotope.resetState(key);
				 }
				 // ...und wird gelï¿½scht
				 else if (isotope.isRemoved())
					 res.deleteRow();
			 }
			 // Tupel wird neu angelegt, wenn es nicht schon
			 // wieder in der Java-Datenstruktur gelï¿½scht wurde
			 else if (!isotope.isRemoved()) {
				 res.moveToInsertRow();
				 res.updateInt("category", categoryID);
				 res.updateInt("word", isotope.getWord().getDB_ID());
				 res.updateInt("chapter", isotope.getChapter().getDB_ID());
				 res.updateInt("index", isotope.getWord().getIndex());
				 res.insertRow();
				 res.close();
				 isotope.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM isotopes WHERE category = "
						 + categoryID
						 + " and word = "
						 + isotope.getWord().getDB_ID()
						 + " and chapter = "
						 + isotope.getChapter().getDB_ID()
						 + " and `index` = "
						 + isotope.getWord().getIndex());

				 // hole die DB-ID zu der neu angelegten Isotopie
				 if (res.next())
					 isotope.setDB_ID(key, res.getInt("id"));

				 else
					 throw new DBC_SaveException("Isotopie "
							 + isotope
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");
			 }
			 res.close();
			 // schreibe ï¿½nderung in die DB
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();
		 return isotopes;
	 }

	 /**
	  * Erstellt eine Isotopie-Sammlung ï¿½ber alle in diesem Kapitel vorkommenden
	  * Isotopien.
	  */
	 public Isotopes loadIsotopes(Integer chapterID)
	 throws Exception {
		 Chapter chapter = getChapter(chapterID.intValue());
		 Isotopes isotopes = new Isotopes(chapter);

		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT isotopes.id, isotopes.index, "
				 + "isotope_categories.category "
				 + "FROM isotopes, isotope_categories "
				 + "WHERE isotopes.chapter = "
				 + chapter.getDB_ID()
				 + " and isotope_categories.id = isotopes.category");

		 while (res.next()) {
			 isotopes.setIsotope(key, res.getInt("id"), chapter.getTokenAtIndex(res
					 .getInt("index")), res.getString("category"));
		 }

		 stmt.close();
		 return isotopes;
	 }

	 public Vector loadIsotopeHierachy(Integer chapterID)
	 throws Exception {
		 Vector hierachy = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT hierachy "
				 + "FROM isotope_hierachies "
				 + "WHERE chapter = "
				 + chapterID);

		 if (res.next()) {
			 Object o = res.getObject(1);
			 if (o instanceof Vector)
				 hierachy = (Vector) o;
		 }

		 stmt.close();
		 return hierachy;
	 }

	 public void saveIsotopeHierachy(Integer chapterID, Vector hierachy)
	 throws Exception {
		 boolean doAgain = false;
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res = stmt.executeQuery("SELECT chapter, hierachy "
				 + "FROM isotope_hierachies "
				 + "WHERE chapter = "
				 + chapterID);

		 if (res.next()) {
			 res.updateObject("hierachy", hierachy);
			 res.updateRow();
		 }
		 else {
			 res.moveToInsertRow();
			 res.updateInt("chapter", chapterID.intValue());
			 res.updateString("hierachy", "blï¿½des Java will "
					 + "nicht aufs erste mal Objekte schreiben...");
			 res.insertRow();
			 doAgain = true;
		 }

		 stmt.close();
		 connection.commit();
		 connection.setAutoCommit(true);

		 // Es ist nicht mï¿½glich, Objects sofort in eine neue Zeile
		 // zu schreiben, deswegen wird die Zeile neu angelegt und
		 // die Zelle, die eigentlich das Object speichern soll,
		 // wird mit Nonsens gefï¿½llt, erst beim wiederholten Aufruf
		 // kann das Object gespeichert werden.
		 if (doAgain)
			 saveIsotopeHierachy(chapterID, hierachy);
	 }

	 public Vector getLanguages()
	 throws Exception {
		 Vector languages = new Vector();
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT count(*) AS c, language "
				 + "FROM words GROUP BY language ORDER BY c DESC");

		 while (res.next())
			 languages.add(res.getString("language"));

		 stmt.close();
		 return languages;
	 }

	 public Comments loadComments(Integer ChapterID, Integer ownerClassCode)
	 throws Exception {
		 Comments c = new Comments();
		 String subquery;

		 switch (ownerClassCode.intValue()) {
		 case Comments.CLASS_CODE_ILLOCUTION_UNIT :
			 subquery = "SELECT id FROM illocution_units WHERE chapter = "
				 + ChapterID;
			 break;
		 case Comments.CLASS_CODE_DIRECT_SPEECH :
			 subquery = "SELECT id FROM direct_speeches WHERE chapter = "
				 + ChapterID;
			 break;
		 case Comments.CLASS_CODE_DIALOG :
			 subquery = "SELECT id FROM dialogs WHERE chapter = " + ChapterID;
			 break;
		 case Comments.CLASS_CODE_DIALOG_FOLLOWUP :
			 subquery = "SELECT id FROM dialogs WHERE chapter = " + ChapterID;
			 break;
		 case Comments.CLASS_CODE_DIALOG_RUNUP :
			 subquery = "SELECT id FROM dialogs WHERE chapter = " + ChapterID;
			 break;
		 default :
			 subquery = "";
		 break;
		 }

		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt.executeQuery("SELECT owner_id, owner_class_code, "
				 + "program, comment FROM comments "
				 + "WHERE owner_class_code = "
				 + ownerClassCode
				 + " and owner_id in ("
				 + subquery
				 + ")");

		 while (res.next()) {
			 CommentKey ck = new CommentKey(key, res.getInt("owner_id"), res
					 .getInt("owner_class_code"), res.getString("program"));
			 c.setComment(key, ck, res.getString("comment"));
		 }

		 return c;
	 }

	 public void saveComments(Comments comments)
	 throws Exception {
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);

		 ResultSet res;
		 Enumeration cks = comments.getKeys();

		 while (cks.hasMoreElements()) {
			 CommentKey ck = (CommentKey) cks.nextElement();
			 Comment c = comments.getComment(ck);

			 res = stmt.executeQuery("SELECT * FROM comments WHERE owner_id = "
					 + ck.getOwnerID()
					 + " and owner_class_code = "
					 + ck.getOwnerClassCode()
					 + " and program = '"
					 + ck.getProgramID()
					 + "'");

			 if (res.next()) {
				 if (c.isRemoved())
					 res.deleteRow();
				 else if (c.hasChanged()) {
					 res.updateString("comment", c.getComment());
					 res.updateRow();
				 }
			 }
			 else {
				 if (c.isNew() || c.hasChanged()) {
					 res.moveToInsertRow();
					 res.updateInt("owner_id", ck.getOwnerID());
					 res.updateInt("owner_class_code", ck.getOwnerClassCode());
					 res.updateString("program", ck.getProgramID());
					 res.updateString("comment", c.getComment());
					 res.insertRow();
				 }
			 }
			 connection.commit();
		 }

		 stmt.close();
		 connection.setAutoCommit(true);
	 }

	 /**
	  * Das gleiche wie die Wortlisten, bloï¿½, dass die Ausgabe in der Klasse
	  * LonleyConstitutiveWord gekapselt ist.
	  * 
	  * @param content
	  * @param language
	  * @deprecated Ohne Funktion! Wortliste ist nun über WordListElements erreichbar.
	  */



	 public void saveWordList(Vector list) throws Exception
	 {
		 return; // SQL Statements sind ungültig

//		 connection.setAutoCommit(false);
//		 Statement stmt = connection
//		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
//		 ResultSet.CONCUR_UPDATABLE);

//		 ResultSet res;

//		 for (int i = 0; i < list.size(); i++) {
//		 DB_Tupel tupel = (DB_Tupel) list.get(i);

//		 // Tupel soll gelï¿½scht werden
//		 if (tupel.getState() == DB_Tupel.DELETE) {
//		 res = stmt.executeQuery("SELECT * FROM word_list WHERE id = "
//		 + tupel.getInt("id"));
//		 if (res.next()) {
//		 // System.out.println("lï¿½sche "+ tupel);
//		 res.deleteRow();
//		 connection.commit();
//		 }
//		 }

//		 // Tupel wird in der DB neu angelegt oder geï¿½ndert
//		 else {
//		 // Fï¿½ge erst eventuell das Wort in die Wort-DB ein


//		 int rowCount = stmt.executeUpdate(String.format(

//		 "INSERT IGNORE INTO words " +
//		 "(content, language, cont_lang_checksum) " +
//		 "VALUES('%1$s', '%2$s', UNHEX(MD5(CONCAT('%1$s', '%2$s'))))",

//		 tupel.getString("content"), tupel.getString("language")));

//		 res = stmt.executeQuery(String.format(

//		 "SELECT id, language, content " +
//		 "FROM words " +
//		 "AND words.cont_lang_checksum = UNHEX(MD5(CONCAT('%1$s', '%2$s')))",

//		 mask(tupel.getString("content")), tupel.getString("language")));

//		 // hole die Wort-ID aud der Wort-DB
//		 int wordID = res.getInt("id");
//		 res.close();
//		 // System.out.println("Wort-ID: "+ wordID);

//		 // Tupel soll geï¿½ndert werden
//		 if (tupel.getState() == DB_Tupel.CHANGE) {
//		 res = stmt.executeQuery("SELECT * FROM word_list WHERE id = "
//		 + tupel.getInt("id"));
//		 if (res.next()) {
//		 // System.out.println("ï¿½ndere "+ tupel);
//		 res.updateInt("word", wordID);

//		 if (tupel.containsKey("tr_genus"))
//		 res.updateByte("tr_genus", tupel.getByte("tr_genus"));

//		 if (tupel.containsKey("tr_numerus"))
//		 res.updateByte("tr_numerus", tupel.getByte("tr_numerus"));

//		 if (tupel.containsKey("tr_case"))
//		 res.updateByte("tr_case", tupel.getByte("tr_case"));

//		 if (tupel.containsKey("tr_determination"))
//		 res.updateByte("tr_determination", tupel
//		 .getByte("tr_determination"));

//		 if (tupel.containsKey("tr_person"))
//		 res.updateByte("tr_person", tupel.getByte("tr_person"));

//		 if (tupel.containsKey("tr_wordclass"))
//		 res.updateByte("tr_wordclass", tupel
//		 .getByte("tr_wordclass"));

//		 if (tupel.containsKey("tr_wordsubclass"))
//		 res.updateByte("tr_wordsubclass", tupel
//		 .getByte("tr_wordsubclass"));

//		 if (tupel.containsKey("tr_conjugation"))
//		 res.updateByte("tr_conjugation", tupel
//		 .getByte("tr_conjugation"));

//		 if (tupel.containsKey("tr_pronoun"))
//		 res.updateByte("tr_pronoun", tupel.getByte("tr_pronoun"));

//		 if (tupel.containsKey("tr_tempus"))
//		 res.updateByte("tr_tempus", tupel.getByte("tr_tempus"));

//		 if (tupel.containsKey("tr_diathese"))
//		 res.updateByte("tr_diathese", tupel
//		 .getByte("tr_diathese"));

//		 if (tupel.containsKey("type"))
//		 res.updateByte("type", tupel.getByte("type"));

//		 if (tupel.containsKey("multiple"))
//		 res.updateInt("multiple", tupel.getInt("multiple"));
//		 res.updateRow();
//		 connection.commit();
//		 }
//		 else {
//		 // System.out.println(tupel +" konnte nicht geï¿½ndert
//		 // werden.\n" +
//		 // "Lege es neu an...");
//		 tupel.setState(DB_Tupel.SAVE);
//		 i--;
//		 continue;
//		 }
//		 }

//		 // Tupel wird neu angelegt
//		 else {
//		 String query = "SELECT * FROM word_list WHERE word = " + wordID;

//		 if (tupel.containsKey("tr_genus"))
//		 query += " and tr_genus = " + tupel.getByte("tr_genus");

//		 if (tupel.containsKey("tr_numerus"))
//		 query += " and tr_numerus = " + tupel.getByte("tr_numerus");

//		 if (tupel.containsKey("tr_case"))
//		 query += " and tr_case = " + tupel.getByte("tr_case");

//		 if (tupel.containsKey("tr_determination"))
//		 query += " and tr_determination = "
//		 + tupel.getByte("tr_determination");

//		 if (tupel.containsKey("tr_person"))
//		 query += " and tr_person = " + tupel.getByte("tr_person");

//		 if (tupel.containsKey("tr_wordclass"))
//		 query += " and tr_wordclass = "
//		 + tupel.getByte("tr_wordclass");

//		 if (tupel.containsKey("tr_wordsubclass"))
//		 query += " and tr_wordsubclass = "
//		 + tupel.getByte("tr_wordsubclass");

//		 if (tupel.containsKey("tr_conjugation"))
//		 query += " and tr_conjugation = "
//		 + tupel.getByte("tr_conjugation");

//		 if (tupel.containsKey("tr_pronoun"))
//		 query += " and tr_pronoun = " + tupel.getByte("tr_pronoun");

//		 if (tupel.containsKey("tr_tempus"))
//		 query += " and tr_tempus = " + tupel.getByte("tr_tempus");

//		 if (tupel.containsKey("tr_diathese"))
//		 query += " and tr_diathese = " + tupel.getByte("tr_diathese");

//		 if (tupel.containsKey("type"))
//		 query += " and type = " + tupel.getByte("type");

//		 res = stmt.executeQuery(query);

//		 // Tupel in der Wortliste noch nicht vorhanden,
//		 // wird also neu angelegt
//		 if (!res.next()) {
//		 // System.out.println("speichere "+ tupel);
//		 res.moveToInsertRow();
//		 res.updateInt("word", wordID);

//		 if (tupel.containsKey("tr_genus"))
//		 res.updateByte("tr_genus", tupel.getByte("tr_genus"));

//		 if (tupel.containsKey("tr_numerus"))
//		 res.updateByte("tr_numerus", tupel.getByte("tr_numerus"));

//		 if (tupel.containsKey("tr_case"))
//		 res.updateByte("tr_case", tupel.getByte("tr_case"));

//		 if (tupel.containsKey("tr_determination"))
//		 res.updateByte("tr_determination", tupel
//		 .getByte("tr_determination"));

//		 if (tupel.containsKey("tr_person"))
//		 res.updateByte("tr_person", tupel.getByte("tr_person"));

//		 if (tupel.containsKey("tr_wordclass"))
//		 res.updateByte("tr_wordclass", tupel
//		 .getByte("tr_wordclass"));

//		 if (tupel.containsKey("tr_wordsubclass"))
//		 res.updateByte("tr_wordsubclass", tupel
//		 .getByte("tr_wordsubclass"));

//		 if (tupel.containsKey("tr_conjugation"))
//		 res.updateByte("tr_conjugation", tupel
//		 .getByte("tr_conjugation"));

//		 if (tupel.containsKey("tr_pronoun"))
//		 res.updateByte("tr_pronoun", tupel.getByte("tr_pronoun"));

//		 if (tupel.containsKey("tr_tempus"))
//		 res.updateByte("tr_tempus", tupel.getByte("tr_tempus"));

//		 if (tupel.containsKey("tr_diathese"))
//		 res
//		 .updateByte("tr_diathese", tupel
//		 .getByte("tr_diathese"));

//		 if (tupel.containsKey("type"))
//		 res.updateByte("type", tupel.getByte("type"));

//		 if (tupel.containsKey("multiple"))
//		 res.updateInt("multiple", tupel.getInt("multiple"));

//		 res.insertRow();
//		 connection.commit();
//		 }
//		 else {
//		 // System.out.println(tupel +" schon vorhanden");
//		 }
//		 }
//		 }
//		 }
//		 stmt.close();
//		 connection.setAutoCommit(true);
	 }

	 /**
	  * save all of <code>complexes</code> whose state indicates an "out of sync with DB" status.
	 * @param complexes
	 * @return <code>complexes</code> with updated states and DB_IDs
	 * @throws SQLException
	 */
	public synchronized ArrayList<PronounComplex_DB> saveComplexes( ArrayList<PronounComplex_DB> complexes) throws SQLException
	 {
		 connection.setAutoCommit(false);
		 PreparedStatement stmt_insert = null, stmt_delete = null, stmt_max_id = null;
		 ResultSet res = null;
		 int max_id = 0;

		 try {
			 // compute free id value for insertion
			 stmt_max_id = connection.prepareStatement("SELECT MAX(id) FROM complexes");
			 res = stmt_max_id.executeQuery();
			 connection.commit();
			 if( res.next()) {
				 max_id = res.getInt(1);
			 }

			 stmt_insert = connection.prepareStatement(

					 "INSERT INTO complexes (id, consitutive_word_id, chapter, member_type) VALUES (?, ?, ?, ?)"
			 );
			 stmt_delete = connection.prepareStatement(

					 "DELETE FROM complexes WHERE id = ?"
			 );

			 for (PronounComplex_DB complex_DB : complexes) {
				 
				 /*
				  * TODO: first check if referenced elements got saved
				  */
				 
				 
				 if(complex_DB.getStateAsInt() == PronounComplex_DB.NEW) {
					 complex_DB.setDB_ID(key, ++max_id);
				 }
				 // remove existing entries
				 if(complex_DB.getStateAsInt() == PronounComplex_DB.REMOVE
						 || complex_DB.getStateAsInt() == PronounComplex_DB.CHANGE) {
					 stmt_delete.setInt(1, complex_DB.getDB_ID());
					 stmt_delete.addBatch();
				 }
				 if(complex_DB.getStateAsInt() == PronounComplex_DB.NEW
						 || complex_DB.getStateAsInt() == PronounComplex_DB.CHANGE) {
					 // add noun
					 stmt_insert.setInt(1, complex_DB.getDB_ID());
					 stmt_insert.setInt(2, complex_DB.nomenID);
					 stmt_insert.setInt(3, complex_DB.chapterID);
					 stmt_insert.setString(4, "noun");
					 stmt_insert.addBatch();
					 // add deictica
					 for (Integer deictica_id : complex_DB.deicticaID) {
						 stmt_insert.setInt(1, complex_DB.getDB_ID());
						 stmt_insert.setInt(2, deictica_id);
						 stmt_insert.setInt(3, complex_DB.chapterID);
						 stmt_insert.setString(4, "deicticon");
						 stmt_insert.addBatch();
					 }
					 complex_DB.resetState(key);
				 }
			 }
			 stmt_delete.executeBatch();
			 stmt_insert.executeBatch();
			 connection.commit();
		 }
		 catch ( SQLException e ) {
			 connection.rollback();
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 connection.setAutoCommit(true);
				 if (res  != null)
					 res.close();
				 if (stmt_insert  != null)
					 stmt_insert.close();
				 if (stmt_delete  != null)
					 stmt_delete.close();
				 if (stmt_max_id  != null)
					 stmt_max_id.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return complexes;
	 }

	 /**
	  * Load all complexes in chapter with ID <code>chapterID</code>
	 * @param chapterID
	 * @return a Vector of PronounComplex_DB. All elements are expected to get converted into PronounComplex on the client side.
	 * @throws Exception
	 */
	public Vector<PronounComplex_DB> loadComplexes(Integer chapterID) throws Exception
	 {
		 Vector<PronounComplex_DB> result = new Vector<PronounComplex_DB>();
		 PreparedStatement stmt = null;
		 ResultSet res = null;

		 try {
			 stmt = connection.prepareStatement(

					 "SELECT * FROM complexes WHERE chapter = ? ORDER BY id"
			 );
			 stmt.setInt(1, chapterID);
			 res = stmt.executeQuery();
			 int id = PronounComplex_DB.DEFAULT_ID;
			 PronounComplex_DB complex = null;
			 while(res.next()) {
				 if(res.getInt("id") > id) {
					 id = res.getInt("id");
					 if(complex != null) {
						 complex.resetState(key);
						 result.add(complex);
					 }
					 complex = new PronounComplex().new PronounComplex_DB(key);
					 complex.setDB_ID(key, res.getInt("id"));					 
				 }
				 if( res.getString("member_type").compareToIgnoreCase("NOUN") == 0 ) {
					 complex.nomenID = res.getInt("consitutive_word_id");
				 }
				 else if( res.getString("member_type").compareToIgnoreCase("DEICTICON") == 0 ) {
					 //TODO: make this more efficient
					 int[] tmp = new int[complex.deicticaID.length + 1];
					 System.arraycopy(complex.deicticaID, 0, tmp, 0, complex.deicticaID.length);
					 tmp[complex.deicticaID.length] = res.getInt("consitutive_word_id");
					 complex.deicticaID = tmp;
				 }
			 }
			 if(complex != null) {
				 complex.resetState(key);
				 result.add(complex);
			 }
		 }
		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 connection.setAutoCommit(true);
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return result;
	 }

	 public Renominalisations saveRenominalisations(Renominalisations renominalisations,
			 Integer chapterID)
	 throws Exception {
		 Chapter chapter = getChapter(chapterID.intValue());
		 IllocutionUnitRoots iur = loadIllocutionUnitRoots(chapterID);
		 renominalisations.setChapter(key, chapter, iur);
		 Vector renoms = renominalisations.getAllRenominalisations(key);
		 connection.setAutoCommit(false);
		 Statement stmt = connection
		 .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				 ResultSet.CONCUR_UPDATABLE);
		 ResultSet res;

		 for (int i = 0; i < renoms.size(); i++) {
			 Renominalisation renominalisation = (Renominalisation) renoms.get(i);
			 // Suche die ID zu der Kategorie
			 int categoryID = -1;
			 res = stmt.executeQuery("SELECT id "
					 + "FROM renominalisation_categories "
					 + "WHERE category like binary '"
					 + renominalisation.getCategory()
					 + "'");
			 if (res.next()) {
				 categoryID = res.getInt("id");
				 res.close();
			 } //didn't find category
			 else {
				 res.close();
				 stmt.executeUpdate("insert into "
						 + "renominalisation_categories (category) "
						 + "values('"
						 + renominalisation.getCategory()
						 + "')");
				 i--;
				 continue;
			 }

			 // PrÃ¼fe, ob schon ein Tupel mit der ID in der DB
			 // gespeichert ist
			 res = stmt.executeQuery("SELECT * "
					 + "FROM renominalisations WHERE id = "
					 + renominalisation.getDB_ID());

			 // Tupel ist vorhanden...
			 if (res.next()) {
				 // ...und wird aktualisiert
				 if (renominalisation.hasChanged()) {
					 res.updateInt("category", categoryID);
					 res.updateInt("constitutive_word", renominalisation
							 .getConstitutiveWord().getDB_ID());
					 res.updateInt("chapter", renominalisation.getChapter()
							 .getDB_ID());
					 // res.updateInt("index",
					 // renominalisation.getConstitutiveWord().getIndex());
					 res.updateRow();
					 renominalisation.resetState(key);
				 }
				 // ...und wird gelï¿½scht
				 else if (renominalisation.isRemoved())
					 res.deleteRow();
			 }
			 // Tupel wird neu angelegt, wenn es nicht schon
			 // wieder in der Java-Datenstruktur gelÃ¶scht wurde
			 else if (!renominalisation.isRemoved()) {
				 res.moveToInsertRow();
				 res.updateInt("category", categoryID);
				 res.updateInt("constitutive_word", renominalisation
						 .getConstitutiveWord().getDB_ID());
				 res.updateInt("chapter", renominalisation.getChapter().getDB_ID());
				 // res.updateInt("index",
				 // renominalisation.getConstitutiveWord().getIndex());
				 res.insertRow();
				 res.close();
				 renominalisation.resetState(key);

				 res = stmt.executeQuery("SELECT id "
						 + "FROM renominalisations WHERE category = "
						 + categoryID
						 + " and constitutive_word = "
						 + renominalisation.getConstitutiveWord().getDB_ID()
						 + " and chapter = "
						 + renominalisation.getChapter().getDB_ID());
				 /*
				  * + " and `index` = " +
				  * renominalisation.getConstitutiveWord().getIndex());
				  */

				 // hole die DB-ID zu der neu angelegten Renominalisation
				 if (res.next())
					 renominalisation.setDB_ID(key, res.getInt("id"));

				 else
					 throw new DBC_SaveException("Renominalisation "
							 + renominalisation
							 + " konnte nicht in der "
							 + "DB gespeichert werden!");
			 }
			 res.close();
			 // schreibe Ã„nderung in die DB
			 connection.commit();
		 }
		 connection.setAutoCommit(true);
		 stmt.close();
		 return renominalisations;
	 }

	 /**
	  * Erstellt eine Renominalisation-Sammlung Ã¼ber alle in diesem Kapitel
	  * vorkommenden Renominalisation.
	  */
	 public Renominalisations loadRenominalisations(Integer chapterID)
	 throws Exception {
		 Chapter chapter = getChapter(chapterID.intValue());
		 Renominalisations renominalisations = new Renominalisations(chapter);
		 Statement stmt = connection.createStatement();
		 ResultSet res = stmt
		 .executeQuery("SELECT renominalisations.id, renominalisations.constitutive_word,  "
				 + "renominalisation_categories.category "
				 + "FROM renominalisations, renominalisation_categories "
				 + "WHERE renominalisations.chapter = "
				 + chapter.getDB_ID()
				 + " and renominalisation_categories.id = renominalisations.category");
		 IllocutionUnitRoots iur = loadIllocutionUnitRoots(chapterID);// Integer.valueOf(res.getInt("chapter")
		 while (res.next()) {
			 renominalisations.setRenominalisation(key, res.getInt("id"), iur
					 .getConstitutiveWordWithID(res.getInt("constitutive_word")), res
					 .getString("category"));
		 }
		 stmt.close();
		 return renominalisations;
	 }


	 /**
	  * Inserts, updates or removes <code>elements</code> data including their {@link TR_Assignation} and Word data in the Database dependent on their state.
	 * @param elements
	 * @throws SQLException
	 */
	public synchronized WordListElement[] saveWordListElements(WordListElement ... elements) throws SQLException
	 {
		 PreparedStatement stmt = null, stmt2 = null;
		 ResultSet res = null, res2 = null;

		 try {
			 // save all words and assignations
			 stmt = connection.prepareStatement(

					 "INSERT IGNORE INTO words " +
					 "(content, language) " +
					 "VALUES(?, ?)"
			 );
			 
			 TR_Assignation[] assignations = new TR_Assignation[elements.length];
			 int i=0;
			 
			 for (WordListElement wordListElement : elements) {
				 
				 if(wordListElement == null) {
					 logger.info("wordListElement is null");
					 continue;
				 }
				 
				 stmt.setBytes(1, wordListElement.getContent().getBytes());
				 stmt.setString(2, wordListElement.getLanguage());	   
				 stmt.addBatch();

				 assignations[i] = wordListElement.getAssignation();
			 }			 
			 stmt.executeBatch();
			 saveAssignations(assignations);
			
			 // save WLEs
			 stmt = connection.prepareStatement(
						
					 "SELECT * " +
					 "FROM word_list_elements " +
					 "WHERE id = ?"
			 );
			 
			 for (WordListElement wordListElement : elements) {
				 	 
				 if(wordListElement == null) {
					 logger.info("wordListElement is null");
					 continue;
				 }

				 if(wordListElement.isUnchanged()) {
					 logger.finest("wordListElement unchanged");
					 continue;
				 }
				 
				 stmt.setInt(1, wordListElement.getDB_ID());
				 res = stmt.executeQuery();
				 
				 // INSERT or UPDATE
				 if (wordListElement.hasChanged()) {
					 if(! res.next()) {
						 res.moveToInsertRow();
					 }
					 
					 // get word id
					 stmt2 = connection.prepareStatement(

							 "SELECT id FROM words " +
							 "WHERE content = ? AND language = ?"
					 );
					 stmt.setBytes(1, wordListElement.getContent().getBytes());
					 stmt.setString(2, wordListElement.getLanguage());
					 res2 = stmt.executeQuery();
					 
					 // if a word ID could be found
					 if( res2.next() ) {
						 
						 // write WLE to DB
						 res.updateInt("word_id", res2.getInt(1));
						 res.updateInt("assignation_id", wordListElement.getAssignation().getDB_ID());
						 
						 if(res.isFirst()) {
							 res.updateRow();
						 }
						 else {
							 res.insertRow();
							 res.last();
							 wordListElement.setDB_ID(key, res.getInt("id"));
						 }
						 wordListElement.resetState(key);
					 }
					 else {
						 logger.warning("word id not found");
						 continue;
					 }
				 }
				 // DELETE
				 else if(wordListElement.isRemoved() && res.next()) {
					 res.deleteRow();
				 }
			 }
		 }
		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 connection.setAutoCommit(true);
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
				 if (res2  != null)
					 res2.close();
				 if (stmt2  != null)
					 stmt2.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return elements;
	 }

	 public synchronized WordListElement[] loadWordListElement(String content) throws SQLException
	 {
		 //TODO: change to all possible languages
		 return this.loadWordListElement(content, "DE");
	 }

	 public synchronized WordListElement[] loadWordListElement(String content, String language) throws SQLException
	 {
		 PreparedStatement stmt = null;
		 ResultSet res = null;
		 WordListElement[] elements = null;

		 try {

			 stmt = connection.prepareStatement(

					 "SELECT assignations.*, word_list_elements.id AS wle_id " +
					 "FROM assignations, word_list_elements, words " +
					 "WHERE assignations.id = word_list_elements.assignation_id " +
					 "AND word_list_elements.word_id = words.id " +
			 "AND words.content = ? AND language = ?");

			 stmt.setBytes(1, content.getBytes());
			 stmt.setString(2, language);
			 res = stmt.executeQuery();

			 res.last();
			 int rows = res.getRow();
			 res.beforeFirst();

			 elements = new WordListElement[rows];

			 int i=0;
			 while( res.next() ) {

				 elements[i] = new WordListElement(content, language);

				 TR_Assignation_DB assignation = new TR_Assignation().new TR_Assignation_DB();
				 assignation.setCasesBinary(res.getBytes("tr_case"));
				 assignation.setConjugationsBinary(res.getBytes("tr_conjugation"));
				 assignation.setDeterminationsBinary(res.getBytes("tr_determination"));
				 assignation.setDiathesesBinary(res.getBytes("tr_diathese"));
				 assignation.setNumeriBinary(res.getBytes("tr_numerus"));
				 assignation.setPersonsBinary(res.getBytes("tr_person"));
				 assignation.setTemporaBinary(res.getBytes("tr_tempus"));
				 assignation.setWordclassesBinary(res.getBytes("tr_wordclass"));
				 assignation.setDB_ID(key, res.getInt("id"));
				 assignation.resetState(key);

				 elements[i].setAssignation(assignation);
				 elements[i].setDB_ID(key, res.getInt("wle_id"));
				 i++;
			 }

			 stmt.close();
		 }
		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return elements;
	 }

	 //gibt für jede Assigantion eines Strings die Wortklasse und Subklasse zurück
//	 public Vector loadWordClasses(Vector contents) throws Exception {
//	 Vector<Vector> resultSet = new Vector<Vector>();
//	 for(int i=0; i != contents.size(); ++i)
//	 {
//	 String content = (String)contents.get(i);
//	 Vector assignations = loadWordListElement(content).getAssignations();
//	 Vector<Long> wordClasses = new Vector<Long>();
//	 Vector<Long> wordSubClasses = new Vector<Long>();
//	 for (int j=0; j != assignations.size(); ++j)
//	 {
//	 TR_Assignation assi = (TR_Assignation)assignations.get(j); 
//	 wordClasses.add(assi.getWordclassesBinary());
//	 if (assi.getWordsubclassAdjectivesBinary()        != 0)
//	 wordSubClasses.add(assi.getWordsubclassAdjectivesBinary());
//	 else if (assi.getWordsubclassConnectorsBinary()   != 0)
//	 wordSubClasses.add(assi.getWordsubclassConnectorsBinary());
//	 else if (assi.getWordsubclassPrepositionsBinary() != 0)
//	 wordSubClasses.add(assi.getWordsubclassPrepositionsBinary());
//	 else if (assi.getWordsubclassPronounsBinary()     != 0)
//	 wordSubClasses.add(assi.getWordsubclassPronounsBinary());
//	 else if (assi.getWordsubclassSignsBinary()        != 0)
//	 wordSubClasses.add(assi.getWordsubclassSignsBinary());
//	 else if (assi.getWordsubclassVerbsBinary()        != 0)
//	 wordSubClasses.add(assi.getWordsubclassVerbsBinary());
//	 else 
//	 wordSubClasses.add(new Long(0));
//	 }
//	 resultSet.add(wordClasses);
//	 resultSet.add(wordSubClasses);
//	 }
//	 return resultSet;	  
//	 }


	 /**
	  * @param assignation
	  * @return the WordListElement characterized by the assignation, <code>null</code> if none exists
	  * @throws SQLException
	  * @throws NullPointerException if the assignation is <code>null</code>
	  */
//	 public synchronized WordListElement loadWordListElement(TR_Assignation assignation) throws SQLException, NullPointerException
//	 {	
//	 if(assignation == null) {
//	 throw new NullPointerException("");
//	 }
//	 if(assignation.getDB_ID() == -1) {
//	 return null;
//	 }

//	 WordListElement element = null;
//	 PreparedStatement stmt = null;
//	 ResultSet res = null;

//	 try {	
//	 stmt = connection.prepareStatement(

//	 "SELECT words.content " +
//	 "FROM word_list_elements, words " +
//	 "WHERE assignation_id = ? " +
//	 "AND words.id = assignation_id.word_id");

//	 stmt.setInt(1, assignation.getDB_ID());

//	 res = stmt.executeQuery();

//	 if( res.next() ) {
//	 element = loadWordListElement(res.getString(1));
//	 }
//	 }
//	 catch ( SQLException e ) {
//	 logger.severe(e.getLocalizedMessage());
//	 throw e;
//	 }
//	 finally {
//	 try
//	 {
//	 if (res  != null)
//	 res.close();
//	 if (stmt  != null)
//	 stmt.close();
//	 }
//	 catch (SQLException e)
//	 {
//	 logger.warning(e.getLocalizedMessage());
//	 }
//	 }

//	 return element;
//	 }

	 /**
	  * Inserts, updates or removes <code>assignations</code> in the Database dependent on their state.
	  * @param assignations
	  * @return assignations with updates DB_IDs
	  * @throws SQLException
	  * @throws NullPointerException if <code>assignations</code> is null
	  */
	 private synchronized TR_Assignation[] saveAssignations(TR_Assignation ... assignations) throws SQLException, NullPointerException
	 {
		 logger.entering(this.getClass().getName(), "saveAssignation", assignations);
		 if(assignations == null) {
			 throw new NullPointerException("");
		 }

		 PreparedStatement stmt = null;
		 ResultSet res = null;
		 try {
			 for (int i = 0; i < assignations.length; i++) {
			     
			     	TR_Assignation_DB assignation = assignations[i].new TR_Assignation_DB();

				 if(assignation == null) {
					 logger.info("assignation is null");
					 continue;
				 }

				 if(assignation.isUnchanged()) {
					 logger.finest("assignation unchanged");
					 continue;
				 }

				 stmt = connection.prepareStatement(

						 "SELECT * FROM assignations WHERE id = ?",

						 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE
				 );
				 stmt.setInt(1, assignation.getDB_ID());
				 res = stmt.executeQuery();

				 if (assignation.hasChanged()) {
					 if(! res.next()) {
						 res.moveToInsertRow();
					 }
//					 res.updateLong("tr_type", assignation[i].getTypesBinary());
//					 res.updateLong("tr_genus", assignation[i].getGenusBinary());
//					 res.updateLong("tr_numerus", assignation[i].getNumerusBinary());
//					 res.updateLong("tr_determination", assignation[i].getDeterminationBinary());
//					 res.updateLong("tr_case", assignation[i].getCasesBinary());
//					 res.updateLong("tr_person", assignation[i].getPersonsBinary());
//					 res.updateLong("tr_conjugation", assignation[i].getConjugationsBinary());
//					 res.updateLong("tr_tempus", assignation[i].getTempusBinary());
//					 res.updateLong("tr_diathese", assignation[i].getDiathesesBinary());
//					 res.updateLong("tr_wordclass", assignation[i].getWordclassesBinary());
//					 res.updateLong("tr_subclass_connector", assignation[i].getWordsubclassConnectorsBinary());
//					 res.updateLong("tr_subclass_verb", assignation[i].getWordsubclassVerbsBinary());
//					 res.updateLong("tr_subclass_adjective", assignation[i].getWordsubclassAdjectivesBinary());
//					 res.updateLong("tr_subclass_preposition", assignation[i].getWordsubclassPrepositionsBinary());
//					 res.updateLong("tr_subclass_pronoun", assignation[i].getWordsubclassPronounsBinary());
//					 res.updateLong("tr_subclass_sign", assignation[i].getWordsubclassSignsBinary());
//					 res.updateLong("tr_wortart1", assignation[i].getWortarten1Binary());
//					 res.updateLong("tr_wortart2", assignation[i].getWortarten2Binary());
//					 res.updateLong("tr_wortart3", assignation[i].getWortarten3Binary());
//					 res.updateLong("tr_wortart4", assignation[i].getWortarten4Binary());
					 res.updateBytes("tr_type", assignation.getTypesBinary());
					 res.updateBytes("tr_genus", assignation.getGeneraBinary());
					 res.updateBytes("tr_numerus", assignation.getNumeriBinary());
					 res.updateBytes("tr_determination", assignation.getDeterminationsBinary());
					 res.updateBytes("tr_numerus", assignation.getNumeriBinary());
					 res.updateBytes("tr_case", assignation.getCasesBinary());
					 res.updateBytes("tr_person", assignation.getPersonsBinary());
					 res.updateBytes("tr_conjugation", assignation.getConjugationsBinary());
					 res.updateBytes("tr_tempus", assignation.getTemporaBinary());
					 res.updateBytes("tr_diathese", assignation.getDiathesesBinary());
					 res.updateBytes("tr_wordclass", assignation.getWordclassesBinary());
					 res.updateString("description", assignation.getDescription());
					 res.updateString("etymol", assignation.getEtymol());
					 if(res.isFirst()) {
						 res.updateRow();
					 }
					 else {
						 res.insertRow();
						 res.last();
						 assignations[i].setDB_ID(key, res.getInt("id"));
					 }
					 assignations[i].resetState(key);
				 }
				 else if(assignation.isRemoved() && res.next()) {
					 res.deleteRow();
				 }
			 }
		 }
		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return assignations;
	 }

	 /**
	  * Inserts, updates or removes <code>relations</code> in the Database dependent on their state.
	  * @param relations
	  * @throws SQLException
	  * @throws DBC_SaveException
	  */
	 public synchronized Relation[] saveRelations(Relation ... relations) throws SQLException, DBC_SaveException
	 {
		 if(relations == null) {
			 throw new NullPointerException("");
		 }

		 PreparedStatement stmt = null;
		 ResultSet res = null;
		 connection.setAutoCommit(false);

		 try {
			 for (Relation relation : relations) {


				 if(relation.getOrigin().getDB_ID() == -1 || relation.getTarget().getDB_ID() == -1) {
					 logger.finest("origin or target not in DB. Skipping");
					 continue;
					 //throw new DBC_SaveException("Speichern der relation fehlgeschlagen: Assignation muss vorher gespeichert werden");
				 }

				 if(relation.isUnchanged()) {
					 logger.finest("relation unchanged");
					 continue;
				 }

				 stmt = connection.prepareStatement(

						 "SELECT * FROM word_list_relations WHERE id = ?",

						 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE
				 );
				 stmt.setInt(1, relation.getDB_ID());
				 res = stmt.executeQuery();

				 if (relation.hasChanged()) {
					 if(! res.next()) {
						 res.moveToInsertRow();
					 }	

					 res.updateInt("origin", relation.getOrigin().getDB_ID());
					 res.updateInt("target", relation.getTarget().getDB_ID());
					 res.updateString("type", relation.getType().name());

					 if(res.isFirst()) {
						 res.updateRow();
					 }
					 else {
						 res.insertRow();
						 res.last();
						 relation.setDB_ID(key, res.getInt("id"));
					 }
					 relation.resetState(key);
				 }
				 else if(relation.isRemoved() && res.next()) {
					 res.deleteRow();
				 }
			 }
			 connection.commit();
		 }
		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 connection.setAutoCommit(true);
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return relations;
	 }

	 public Relation_DB[] loadRelations(WordListElement wordlistelement) throws SQLException
	 {
		 if(wordlistelement == null) {
			 throw new NullPointerException("");
		 }


		 if(wordlistelement.getDB_ID() == -1){
			 System.out.println("WordListElement ist noch nicht gespeichert, daher können keine Relations geladen werden.");
			 return new Relation_DB[0];
		 }
		 ResultSet res = null;
		 PreparedStatement stmt = null;

		 stmt = connection.prepareStatement(

				 "SELECT word_list_elements.id, word_list_relations.id AS relationID, word_list_relations.type AS relationType " +
				 "FROM word_list_elements, word_list_relations " +
				 "WHERE word_list_elements.id = word_list_relations.target " +
		 "AND word_list_relations.origin = ?");

		 stmt.setInt(1, wordlistelement.getDB_ID());
		 res = stmt.executeQuery();

		 res.last();
		 Relation_DB[] resultSet = new Relation_DB[res.getRow()];
		 res.beforeFirst();
		 int i = 0;

		 while ( res.next() ) {

			 Relation_DB r = new Relation(key).new Relation_DB(key);
			 r.origin_cw_id = wordlistelement.getDB_ID();
			 r.target_cw_id = res.getInt("id");
			 r.setType(Relation.Types.valueOf(res.getString("relationType")));
			 r.setDB_ID(key, res.getInt("relationID"));
			 r.resetState(key);
			 resultSet[i] =  r;

			 ++i;
		 }

		 return resultSet;
	 }

	 public WordListElement loadWordListElement(Integer id) throws SQLException {
		 PreparedStatement stmt = null;
		 ResultSet res = null;
		 WordListElement element = null;

		 try {

			 stmt = connection.prepareStatement("SELECT * FROM word_list_elements, words WHERE word_list_elements.id = ? AND word_list_elements.word_id = words.id");
			 stmt.setInt(1, id);

			 res = stmt.executeQuery();
			 if(res.next()) {
				 element = new WordListElement(new String(res.getBytes("content")), res.getString("language"));
				 if(res.getInt("assignation_id") != 0)
					 element.setAssignation(loadAssignation(res.getInt("assignation_id")));
				 element.setDB_ID(key, id);
				 element.resetState(key);
			 }
		 }
		 catch ( SQLException e ) {
			 logger.severe(e.getLocalizedMessage());
			 throw e;
		 }
		 finally {
			 try
			 {
				 if (res  != null)
					 res.close();
				 if (stmt  != null)
					 stmt.close();
			 }
			 catch (SQLException e)
			 {
				 logger.warning(e.getLocalizedMessage());
			 }
		 }
		 return element;
	 }

//	 public Vector loadRelations(Vector assignations) throws SQLException
//	 {
//	 if(assignations == null) {
//	 throw new NullPointerException("");
//	 }
//	 Vector resultSet = new Vector<Relation>();

//	 for(int i = 0; i != assignations.size(); i++) {
//	 TR_Assignation assignation = (TR_Assignation)assignations.get(i);
//	 resultSet.addAll(loadRelations(assignation));
//	 }

//	 return resultSet;
//	 }

	 public boolean isEdited(Chapter c, int category) throws SQLException{
		 Vector resultSet = new Vector();
		 connection.setAutoCommit(false);
		 Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		 ResultSet res = null;
		 switch(category){

		 case ChapterEditingTester.CONSTITUTIVE_WORD:
			 res = stmt.executeQuery("SELECT * FROM constitutive_words WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.FUNCTION_WORD:
			 res = stmt.executeQuery("SELECT * FROM function_words WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.COMPLEX:
			 res = stmt.executeQuery("SELECT * FROM complexes WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.DIALOG:
			 res = stmt.executeQuery("SELECT * FROM dialogs WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.DIRECT_SPEECH:
			 res = stmt.executeQuery("SELECT * FROM direct_speeches WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.ILLOCUTION_UNIT:
			 res = stmt.executeQuery("SELECT * FROM illocution_units WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.ISOTOPE:
			 res = stmt.executeQuery("SELECT * FROM isotopes WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.MACRO_SENTENCE:
			 res = stmt.executeQuery("SELECT * FROM macro_sentences WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.RENOMINALISATION:
			 res = stmt.executeQuery("SELECT * FROM renominalisations WHERE chapter="+c.getDB_ID());
			 break;
		 case ChapterEditingTester.THEMA:
			 res = stmt.executeQuery("SELECT * FROM themas WHERE chapter="+c.getDB_ID());
			 break;
		 }

		 return res.next();
	 }

	 /**
	  * Bereitet den übergebenen String zur Verwendung in einer mySQL Abfrage vor, indem jedes Vorkommen von `'' mit `\'' ersetzt.
	  * @param s
	  * @return Den maskierten String
	  * @deprecated Bitte prepared statements benutzen! http://www.sdnshare.com/view.jsp?id=525
	  */
	 private static String mask(String s) {
		 char[] s1 = s.toCharArray();
		 int found = 0;

		 for (int i = 0; i < s1.length; i++)
			 if (s1[i] == '\'')
				 found++;

		 char[] s2 = new char[s1.length + found];
		 for (int i = 0, j = 0; i < s1.length; i++) {
			 if (s1[i] == '\'') {
				 s2[j++] = '\\';
			 }
			 s2[j++] = s1[i];
		 }
		 return new String(s2);
	 }

	public void setKey(DBC_Key key) {
	    this.key = key;
	}

}