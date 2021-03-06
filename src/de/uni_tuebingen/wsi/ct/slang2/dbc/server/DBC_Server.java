/*
 * Erstellt: 23.10.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.server;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
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
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogComment;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogCosmology;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogD_Themat;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogFaces;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogISignal;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogSpeaker;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogSpeakerChange;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DialogTarget;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Dialogs;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeech;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DirectSpeeches;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.FunctionWord;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IDOwner;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IU_Comment;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnit;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnitRoot;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnitRoots;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Isotope;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Isotopes;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism1;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism2;
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
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Thema_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Token;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Word;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WordListElement;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WorkingTranslation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism1.LiteraryCriticism1_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.LiteraryCriticism2.LiteraryCriticism2_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.PronounComplex.PronounComplex_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Relation.Relation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.TR_Assignation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WorkingTranslation.WorkingTranslation_DB;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_KeyAcceptor;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_LoadException;
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

	logger = Logger.getLogger(this.getClass().getName());
	if(Slang2Server.logger != null)
	    logger.setParent(Slang2Server.logger);
	
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
     * �ffnet die Verbindung zur Datenbank. Wird vom Konstruktor aufgerufen. Auch
     * n�tzlich, wenn die Verbindung mit <code>close()</code> beendet wurde und nun wieder
     * hergestellt werden soll.
     * 
     * @throws DBC_ConnectionException falls kein Verbindungsaufbau m�glich ist.
     * 
     * @see #close()
     */
    void open() throws DBC_ConnectionException {	   
	try {
	    if (connection == null || connection.isClosed()) {
		logger.info("Connecting to the mySQL server...");
		try {

		    Class.forName("com.mysql.jdbc.Driver");
		    connection = DriverManager.getConnection(String.format("jdbc:mysql://%1$s:%2$d/%3$s?useUnicode=true&characterEncoding=ISO-8859-1"
			    ,this.db_host, this.db_port, this.db_name), this.db_user, this.db_password);
		}
		catch (SQLException e) {
		    logger.log(Level.SEVERE, "JDBC Connection Failure", e);
		    throw new DBC_ConnectionException("The DBC-Server was unable to connect to the mySQL-Server");
		}
		catch (ClassNotFoundException e) {
		    logger.log(Level.SEVERE, "JDBC driver not found", e);
		    throw new DBC_ConnectionException("The DBC-Server was unable to connect to the mySQL-Server");
		}
		logger.info("Connection is up.");
	    }
	} catch (SQLException e) {
	    logger.log(Level.SEVERE, "Database Access error", e);
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

    /**
     * Search in <code>elements</code> for the one whose <code>getDB_ID()</code> Method returns <code>id</code>
     * @param id
     * @param elements
     * @return The element in <code>elements</code> whose <code>getDB_ID()</code> Method returns <code>id</code>, <code>null</code> if none does so.
     */
    private static IDOwner getElement(int id, Vector<? extends IDOwner> elements) {
	if(elements == null)
	    throw new NullPointerException();
	for (IDOwner owner : elements)
	    if (owner.getDB_ID() == id)
		return owner;
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
     * Laedt alle Buecher aus der Datenbank.
     * 
     * @return ein Vektor mit allen gespeicherten Buechern
     * 
     * @see Book
     */
    public synchronized Vector<Book> loadBooks()
    throws Exception {
	Vector<Book> books = new Vector<Book>();

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
			res.getInt("index"), res.getString("title"), ts.toString().substring(0,ts.toString().length()-2))); //dirty, aber sehe keine andere M�glichkeit, den nano-
		//Anteil loszuwerden
	    }
	}

	stmt.close();

	return books;
    }

    /**
     * Laedt ein Buch aus der Datenbank
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
     * Laedt alle Pattern aus der Datenbank.
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
     * Laedt alle Pattern mit tdTyp aus der Datenbank.
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
	    logger.info("schon in der DB vorhanden");
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
	    logger.info("muss gespeichert werden");
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

    /**
     * Query the chapter identified by <code>id</code> by doning the following in order:
     * <ol>
     * <li>looking if it is present in the chapterCache</li>
     * <li>trying to load it from the database</li>
     * </ol>
     * @param id
     * @return The chapter identified by <code>id</code>, <code>NULL</code> if no corresponding chapter could be found.
     * @throws SQLException 
     */
    private synchronized Chapter getChapter(int id) throws SQLException {
	Chapter chapter = (Chapter) chapterCache.get(id);
	if (chapter == null) {
	    chapter = loadChapter(id);
	}
	return chapter;

    }

    /**
     * @param chapterID
     * @return
     * @throws SQLException
     */
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

		// W�rter des Kapitels
		res = stmt.executeQuery("SELECT words.id AS id, "
			+ "words_in_chapter.position, "
			+ "words.content, words.language "
			+ "FROM words_in_chapter, words "
			+ "WHERE words_in_chapter.chapter = "
			+ chapter.getDB_ID()
			+ " AND words_in_chapter.word = words.id "
			+ "ORDER BY words_in_chapter.position");
		while (res.next())
		    try {
			chapter.addWord(key,
			    res.getInt("id"),
			    new String(res.getBytes("content"), "ISO-8859-1"),
			    res.getString("language"),
			    res.getInt("position"));
		    } catch (UnsupportedEncodingException e) {
			logger.warning(e.getMessage());
		    }

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

		// Abs�tze des Kapitels
		res = stmt.executeQuery("SELECT position "
			+ "FROM paragraphs_in_chapter "
			+ "WHERE chapter = "
			+ chapter.getDB_ID()
			+ " ORDER BY position");
		while (res.next())
		    chapter.addNewline(key, res.getInt("position"));

		// �u�erungseinheiten des Kapitels
		res = stmt.executeQuery("SELECT * FROM illocution_units "
			+ "WHERE chapter = "
			+ chapter.getDB_ID());
		while (res.next())
		  //  chapter.addIllocutionUnit(key, res.getInt("id"), res.getInt("start"), res.getInt("end"));
			  chapter.addIllocutionUnit(key, res.getInt("id"), res.getInt("start"), res.getInt("end"), res.getString("kriterium"),res.getBoolean("startsParagraph"),res.getBoolean("startsNewline"));

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

    /**
     * Saves Chaper in the database
     * @param chapter
     * @return
     * @throws DBC_SaveException
     * @throws SQLException
     */
    /*
     * TODO: Der R�ckgabewert sollte nur die Chapter ID sein, da alles andere gleich bleibt. (CAVE: Vermutung)
     */
    public synchronized Chapter saveChapter(Chapter chapter) throws DBC_SaveException, SQLException
	{

		long timeA = 0;
		long timeB = 0;
		
		Stopwatch a = new Stopwatch();
		Stopwatch b = new Stopwatch();
		
		if (chapter == null)
		{
			throw new NullPointerException();
		}

		logger.entering(DBC_Server.class.getName(), "saveChapter", chapter);

		PreparedStatement stmt = null;
		ResultSet res = null;

		chapter.calculateIndicies(key);

		try
		{
			connection.setAutoCommit(false);

			stmt = connection.prepareStatement(

			"SELECT * FROM chapters WHERE book = ? AND `index` = ?",

			ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			stmt.setInt(1, chapter.getBookID());
			stmt.setInt(2, chapter.getIndex());

			res = stmt.executeQuery();

			// Kapitel wurde schon gespeichert -> l�sche es
			if (res.next())
			{
				// logger.info("l\u00f6sche altes Kapitel");
				logger.info("L\u00f6sche altes Kapitel");
				res.deleteRow();
				connection.commit();
			}

			// res.beforeFirst(); // due to MySQL Bug #19451
			res.moveToInsertRow();
			res.updateInt("book", chapter.getBookID());
			res.updateInt("index", chapter.getIndex());
			res.updateString("title", chapter.getTitle());
			res.updateTimestamp("date", new Timestamp(System.currentTimeMillis())); // TODO
																					// don't
																					// instantiate
																					// with
																					// currentMills
			res.insertRow();

			// Wiederhole die Anfrage
			res = stmt.executeQuery();

			if (res.next())
				chapter.setDB_ID(key, res.getInt("id"));
			else
			{
				connection.rollback();
				throw new DBC_SaveException("Kapitel " + chapter.getTitle() + "konnte nicht in der "
						+ "DB gespeichert werden!");
			}
			logger.info("Lege neues Kapitel an");

			Vector<Word> words = chapter.getWords();
			Map<String,Integer> checkedWordsMap = new HashMap<String,Integer>();
			
			logger.info("Speichere " + words.size() + " W\u00f6rter");
			
			for (int i = 0; i < words.size(); i++)
			{
				Word word = (Word) words.get(i);

				
				if (word.getDB_ID() == -1 )
				{
					/*
					 * Wort schon gepr�ft?
					 */
					if( checkedWordsMap.containsKey( word.getContent()) )
					{
						int word_ID = checkedWordsMap.get( word.getContent() );
						word.setDB_ID(key, word_ID);
						
						// speichere Wort im Kapitel
						stmt = connection.prepareStatement(
								"INSERT INTO words_in_chapter (chapter, word, position) VALUES(?, ?, ?)",
								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
						stmt.setInt(1, word.getChapter().getDB_ID());
						stmt.setInt(2, word.getDB_ID());
						stmt.setInt(3, word.getStartPosition());
						int rowCount = stmt.executeUpdate();

						if (rowCount != 0)
						{
							logger.finer("+");
						}
					}
					
					/*
					 * Ermittle ob Wort in DB + Wort ID
					 */
					else
					{
						a.start();
						
						// Insert word first if not exists
						stmt = connection.prepareStatement("INSERT IGNORE INTO words " + "(content, language) "
								+ "VALUES(?, ?)", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE, Statement.RETURN_GENERATED_KEYS );
						stmt.setBytes(1, word.getContent().getBytes("ISO-8859-1"));
						stmt.setString(2, word.getLanguage());
						stmt.executeUpdate();
						res = stmt.getGeneratedKeys();

						a.stop();
						timeA += a.getElapsedTimeMilliSecs();
						
						int wordId = 0;
						if (res.next())
						{
							wordId = res.getInt(1);
						}
						else
						{
							b.start();
							stmt = connection.prepareStatement("SELECT id FROM words WHERE content = ? AND language = ?",
									ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
							stmt.setBytes(1, word.getContent().getBytes("ISO-8859-1"));
							stmt.setString(2, word.getLanguage());

							res = stmt.executeQuery();
							if (res.next())
							{
								wordId = res.getInt(1);
							}
							
							b.stop();
							timeB += b.getElapsedTimeMilliSecs();
							
						}

						if (wordId > 0)
						{
							word.setDB_ID(key, wordId);
						}
						else
						{
							connection.rollback();
							throw new DBC_SaveException("Wort " + word + " konnte nicht in der " + "DB gespeichert werden!");
						}

						// speichere Wort im Kapitel
						stmt = connection.prepareStatement(
								"INSERT INTO words_in_chapter (chapter, word, position) VALUES(?, ?, ?)",
								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
						stmt.setInt(1, word.getChapter().getDB_ID());
						stmt.setInt(2, word.getDB_ID());
						stmt.setInt(3, word.getStartPosition());
						int rowCount = stmt.executeUpdate();

						if (rowCount != 0)
						{
							logger.finer("+");
						}
						
						checkedWordsMap.put( word.getContent(), word.getDB_ID() );

					}
				}
				else
				{
					logger.finer("-");
				}
			}

			Vector signs = chapter.getSigns();

			logger.info("Speichere " + signs.size() + " Satzzeichen");

			stmt = connection.prepareStatement("INSERT IGNORE INTO signs (sign) VALUES(?)");

			for (int i = 0; i < signs.size(); i++)
			{
				Sign sign = (Sign) signs.get(i);
				if (sign.getDB_ID() == -1)
				{
					stmt.setString(1, sign.getContent());
					stmt.addBatch();
				}
			}
			int[] counts = stmt.executeBatch();

			// setze entsprechende DB Id im Objekt
			stmt = connection.prepareStatement("SELECT id FROM signs WHERE sign = ?");

			for (int i = 0; i < signs.size(); i++)
			{
				Sign sign = (Sign) signs.get(i);

				if (sign.getDB_ID() == -1)
				{
					stmt.setString(1, sign.getContent());
					res = stmt.executeQuery();

					if (res.next())
					{
						sign.setDB_ID(key, res.getInt(1));
						logger.finer("-");
					}
					else
					{
						connection.rollback();
						throw new DBC_SaveException("Satzzeichen " + sign + " konnte nicht in der "
								+ "DB gespeichert werden!");
					}
				}
			}

			// speichere Satzzeichen im Kapitel
			stmt = connection
					.prepareStatement("INSERT INTO signs_in_chapter (chapter, sign, position) VALUES(?, ?, ?)");

			for (int i = 0; i < signs.size(); i++)
			{
				Sign sign = (Sign) signs.get(i);

				stmt.setInt(1, sign.getChapter().getDB_ID());
				stmt.setInt(2, sign.getDB_ID());
				stmt.setInt(3, sign.getStartPosition());
				stmt.addBatch();
			}
			counts = stmt.executeBatch();

			// Speichere Abs�tze
			Vector paragraphs = chapter.getParagraphs();
			logger.info("Speichere " + paragraphs.size() + " Abs\u00e4tze");

			stmt = connection.prepareStatement("INSERT INTO paragraphs_in_chapter (chapter, position) VALUES (?, ?)");

			for (int i = 0; i < paragraphs.size(); i++)
			{
				Integer p = (Integer) paragraphs.get(i);
				stmt.setInt(1, chapter.getDB_ID());
				stmt.setInt(2, p.intValue());
				stmt.addBatch();
			}
			int[] updateCounts = stmt.executeBatch();

			String logMessage = "";
			for (int i = 0; i < paragraphs.size(); i++)
			{
				logMessage += (updateCounts[i] != 0) ? "+" : "-";
			}
			logger.finer(logMessage);

			// Speichere �u�erungseinheiten
			Vector ius = chapter.getIllocutionUnits();
			logger.info("Speichere " + ius.size() + " Aeu�erungseinheiten");

			stmt = connection
					.prepareStatement("INSERT INTO illocution_units (chapter, start, end, kriterium, startsParagraph, startsNewline) VALUES (?, ?, ?, ?, ?, ?)");

			for (int i = 0; i < ius.size(); i++)
			{
				IllocutionUnit iu = (IllocutionUnit) ius.get(i);
				stmt.setInt(1, chapter.getDB_ID());
				stmt.setInt(2, iu.getStartPosition());
				stmt.setInt(3, iu.getEndPosition());
				stmt.setString(4, iu.getKriterium());
				stmt.setBoolean(5, iu.isStartsParagraph());
				stmt.setBoolean(6, iu.isStartsNewline());
				stmt.addBatch();
			}
			updateCounts = stmt.executeBatch();

			stmt = connection
					.prepareStatement("SELECT id FROM illocution_units WHERE chapter = ? AND start = ? AND end = ? AND kriterium = ? AND startsParagraph = ? AND startsNewline = ?");
			for (int i = 0; i < ius.size(); i++)
			{
				IllocutionUnit iu = (IllocutionUnit) ius.get(i);
				stmt.setInt(1, chapter.getDB_ID());
				stmt.setInt(2, iu.getStartPosition());
				stmt.setInt(3, iu.getEndPosition());
				stmt.setString(4, iu.getKriterium());
				stmt.setBoolean(5, iu.isStartsParagraph());
				stmt.setBoolean(6, iu.isStartsNewline());
				res = stmt.executeQuery();

				if (res.next())
					iu.setDB_ID(key, res.getInt(1));
				else
				{
					logger.severe("Fehler beim Speichern einer �u�erungseinheit => Rollback");
					connection.rollback();
					throw new DBC_SaveException("�u�erungseinheit " + iu
							+ " konnte nicht in der DB gespeichert werden!");
				}

				if (!res.isLast())
				{
					logger.warning("Doppelte Eintr�ge in der illocution_units Tabelle gefunden");
				}
				logger.finer("+");
			}
			logger.info("Fertig");

			connection.commit();
		}
		catch (SQLException e)
		{
			logger.severe(e.getLocalizedMessage());
			throw e;
		}
		catch (UnsupportedEncodingException e)
		{
			logger.warning(e.getMessage());
		}

		finally
		{
			try
			{
				connection.setAutoCommit(true);
				if (res != null)
					res.close();
				if (stmt != null)
					stmt.close();
			}
			catch (SQLException e)
			{
				logger.warning(e.getLocalizedMessage());
			}
		}
	
		logger.info("Seconds\n"+
				"INSERT INTO IGNORE Duration: "+(timeA/1000)+"\n"+
				"SELECT ID Duartion: "+(timeB/1000)+"\n"
				);
		
		logger.info("Minutes\n"+
		"INSERT INTO IGNORE Duration: "+(timeA/1000/60)+"\n"+
		"SELECT ID Duartion: "+(timeB/1000/60)+"\n"
		);
		
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
		    res.next(); //zeigt auf zu l�schendes, auf jeden Fall vorhandene Chapter
		    if(!res.next()) //Buch kann gel�scht werden, da kein weiteres Kapitel von diesem Buch vorhanden ist
		    	stmt.execute("delete FROM books WHERE id = "+bookID);
		    
		    stmt.execute("delete FROM chapters WHERE id = "+chapterID);
		}
    }
    
	public synchronized void deleteBook( Integer bookID ) throws Exception
	{
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery("SELECT * " + "FROM books WHERE id = " + bookID);
		
		if( res.next() )
		{
			stmt.execute("delete FROM books WHERE id = " + bookID);
		}	
	}
    
	/**
	 * Laedt alle Direkten Reden aus der Datenbank, die zu diesem Kapitel
	 * gespeichert wurden.
	 * 
	 * @param chapterID
	 *        das Kapitel
	 * @return ein Vektor mit den entsprechenden Direkten Reden.
	 * @see DirectSpeech
	 */
    public synchronized DirectSpeeches loadDirectSpeeches(Integer chapterID, Integer level)
	throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		
		DirectSpeeches directSpeeches = new DirectSpeeches();
		Vector speeches = new Vector();
		Statement stmt = connection.createStatement();
		ResultSet res;
		
		// Grunddaten der direkten Reden einlesen.
		res = stmt.executeQuery("SELECT * "
				+ "FROM direct_speeches WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level
				+ " ORDER BY `index`");
		
		while (res.next())
			speeches.add(new DirectSpeech(key, res.getInt("id"), chapter, res
					.getInt("index"), res.getInt("depth"), res
					.getBoolean("accepted"), res.getInt("possible_question")));
		
		// Aeu�erungseinheiten zu den direkten Reden einlesen.
		for (int i = 0; i < speeches.size(); i++) {
			DirectSpeech ds = (DirectSpeech) speeches.get(i);
			res = stmt.executeQuery("SELECT illocution_unit "
					+ "FROM ius_from_direct_speeches "
					+ "WHERE direct_speech = " + ds.getDB_ID()
					+ " AND level = " + level
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
		
	public synchronized DirectSpeeches saveDirectSpeeches (Integer chapterID, DirectSpeeches oldDirectSpeeches, DirectSpeeches newDirectSpeeches, Integer level)
	throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		
		Vector oldDss = oldDirectSpeeches.getAllDirectSpeeches(key);
		Vector newDss = newDirectSpeeches.getAllDirectSpeeches(key);
		
		oldDirectSpeeches.setChapter(key, chapter);
		newDirectSpeeches.setChapter(key, chapter);
		
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		ResultSet.CONCUR_UPDATABLE);
		
		ResultSet res;
		
		//l�sche alle Direct Speeches des chapters in der Datenbank
		for (int i = 0; i < oldDss.size(); i++) {
			DirectSpeech ds = (DirectSpeech) oldDss.get(i);
			
			//l�sche alle direct_speeches Eintr�ge aus oldDss die bereits in der Datenbank gespeichert sind
			res = stmt.executeQuery("SELECT * "
				+ "FROM direct_speeches "
				+ "WHERE id = "
				+ ds.getDB_ID()
				+ " AND level = " + level);
			
			if(res.next())
				res.deleteRow();
			
			//l�sche alle ius_FROM_direct_speeches Eintr�ge aus oldDss die bereits in der Datenbank gespeichert
			res = stmt.executeQuery("SELECT * "
				+ "FROM ius_from_direct_speeches "
				+ "WHERE direct_speech = "
				+ ds.getDB_ID()
				+ " AND level = " + level);
			
			if(res.next())
				res.deleteRow();
		}
		
		//speichere alle aktuellen Direct Speeches des chapters
		for (int i = 0; i < newDss.size(); i++)	{
			DirectSpeech ds = (DirectSpeech) newDss.get(i);
			
			res = stmt.executeQuery("SELECT * "
				+ "FROM direct_speeches "
				+ "WHERE id = "
				+ ds.getDB_ID()
				+ " AND level = " + level);
			
			//wenn Eintrag nicht in Datenbank vorhanden
			if (ds.getDB_ID() == -1) 
			{
				res.moveToInsertRow();
				res.updateInt("chapter", chapter.getDB_ID());
				res.updateInt("index", ds.getIndex());
				res.updateInt("depth", ds.getDepth());
				res.updateBoolean("accepted", ds.isAccepted());
				res.updateInt("level", level);
				
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
					+ ds.getDepth()
					+ " AND level = " + level);
				
				if (res.next())
					ds.setDB_ID(key, res.getInt("id"));
				else
					throw new DBC_SaveException("Direkte Rede "
						+ ds
						+ "konnte nicht angelegt werden");
				
				res.close();
				
				res = stmt.executeQuery("SELECT * FROM ius_from_direct_speeches WHERE level = " + level);
				Vector ius = ds.getIllocutionUnits();
				
				for (int j = 0; j < ius.size(); j++) 
				{
					IllocutionUnit iu = (IllocutionUnit) ius.get(j);
				
					res.moveToInsertRow();
					res.updateInt("direct_speech", ds.getDB_ID());
					res.updateInt("illocution_unit", iu.getDB_ID());
					res.updateInt("level", level);
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
 
	public synchronized Dialogs saveDialogs(Integer chapterID, Dialogs oldDialogs, Dialogs newDialogs, Integer level )
	throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		Vector<Dialog> newDs = newDialogs.getAllDialogs(key);
		Vector<Dialog> oldDs = oldDialogs.getAllDialogs(key);
		
		newDialogs.setChapter(key, chapter);
		oldDialogs.setChapter(key, chapter);
		
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

		ResultSet res;
		
		// l�sche alle Dialoge (oldDs) die bereits in der Datenbank gespeichert sind
		for (int i = 0; i < oldDs.size(); i++) {
			Dialog ds = (Dialog) oldDs.get(i);
			
			res = stmt.executeQuery("SELECT * " + "FROM dialogs " + "WHERE id = " + ds.getDB_ID()
					+ " AND level = " + level);

			if (res.next())
				res.deleteRow();
		}
		
		// speichert alle neuen Dialoge (newDs) in der Datenbank
		for (int i = 0; i < newDs.size(); i++) 
		{
			Dialog d = (Dialog) newDs.get(i);

			res = stmt.executeQuery("SELECT * " + "FROM dialogs "
					+ "WHERE id = " + d.getDB_ID()
					+ " AND level = " + level);
		
			if (res.next() && d.getDB_ID() != -1) 
			{
				logger.info("-----UPDATE--------");
				res.updateInt("chapter", chapter.getDB_ID());
				res.updateInt("index", d.getIndex());
				res.updateInt("depth", d.getDepth());
				res.updateInt("start", d.getDialogStart().getDB_ID());
				res.updateInt("end", d.getDialogEnd().getDB_ID());
				res.updateBoolean("accepted", d.isAccepted());
				res.updateInt("level", level);
				res.updateRow();
				d.resetState(key);
				res.close();

				res = stmt.executeQuery("SELECT * FROM cosmologies WHERE dialog = " + d.getDB_ID()
						+ " AND level = " + level);
				int j = 0;
				while (res.next()) 
				{
					if (d.getCosmologies().size() != j ) 
					{
						res.updateInt("start", d.getCosmologies().get(j).getStartIndex());
						res.updateInt("end", d.getCosmologies().get(j).getEndIndex());
						res.updateString("description", d.getCosmologies().get(j).getDescription());
						res.updateInt("level", level);
						res.updateRow();
						++j;
					} else
						res.deleteRow();
				}
				res.close();
			}
			else if (d.getDB_ID() == -1) 
			{
				res.moveToInsertRow();
				res.updateInt("chapter", chapter.getDB_ID());
				res.updateInt("index", d.getIndex());
				res.updateInt("depth", d.getDepth());
				res.updateInt("start", d.getDialogStart().getDB_ID());
				res.updateInt("end", d.getDialogEnd().getDB_ID());
				res.updateBoolean("accepted", d.isAccepted());
				res.updateInt("level", level);
				res.insertRow();
				res.close();
				d.resetState(key);
		
				res = stmt.executeQuery("SELECT id "
						+ "FROM dialogs WHERE chapter = " + chapter.getDB_ID()
						+ " and `index` = " + d.getIndex() + " and depth = "
						+ d.getDepth()
						+ " AND level = " + level);
		
				if (res.next())
					d.setDB_ID(key, res.getInt("id"));
				else
					throw new DBC_SaveException("Dialog " + d
							+ "konnte nicht angelegt werden");
				res.close();
		
				for (int j=0; j != d.getCosmologies().size(); ++j) 
				{
					DialogCosmology cosmol = d.getCosmologies().get(j);
					res = stmt.executeQuery("SELECT * FROM cosmologies");
					res.moveToInsertRow();
					res.updateInt("dialog", d.getDB_ID());
					res.updateInt("start", cosmol.getStartIndex());
					res.updateInt("end", cosmol.getEndIndex());
					res.updateString("description", cosmol.getDescription());
					res.updateInt("level", level);
					res.insertRow();
					res.close();
				}
			}
			res.close();
			connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
		
		return newDialogs;
	}
    
    /**
     * Wird vom Dialogprogramm benutzt.
     * Es werden alle Speakers in der DB gespeichert.
     * Speaker k�nnen Akteure und Kp (Kommunikationspartner) sein, au�erdem werden speaker bei der Face-Bestimmung festgelegt. 
     */
	public synchronized ArrayList<DialogSpeaker> saveSpeakers(Integer chapterID, ArrayList<DialogSpeaker> speakers, Integer level)
	throws Exception 
	{
		Chapter chapter = getChapter(chapterID.intValue());

		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

		ResultSet res;
		
		// alle Speakers des Chapters werden gel�scht
		stmt.execute("DELETE FROM speakers WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level);
		
		for (int i = 0; i < speakers.size(); i++) 
		{
			DialogSpeaker speaker = (DialogSpeaker) speakers.get(i);

			res = stmt.executeQuery("SELECT * " + "FROM speakers "
					+ "WHERE id = " + speaker.getDB_ID()
					+ " AND level = " + level);

			for (int speakerNr : speaker.getSpeakerMap().keySet()) 
			{
				String speakerName = speaker.getSpeakerMap().get(speakerNr);
				res.moveToInsertRow();
				res.updateInt("chapter", chapter.getDB_ID());
				res.updateInt("speakerNr", speakerNr);
				res.updateString("speaker", speakerName);
				res.updateString("typ", speaker.getTyp());
				res.updateInt("location", speaker.getIUIndex());
				res.updateInt("level", level);
				res.insertRow();
			}
			res.close();
			speaker.resetState(key);
			
			for (int speakerNr : speaker.getSpeakerMap().keySet()) 
			{
				String speakerName = speaker.getSpeakerMap().get(speakerNr);
				 PreparedStatement preStmt = connection.prepareStatement("SELECT id "
							+ "FROM speakers WHERE " 
							+ "chapter = " + chapter.getDB_ID() 
							+ " and speakerNr = " + speakerNr 
							+ " and `speaker` = ?"
							+ " and typ = ?"
							+ " and location = " + speaker.getIUIndex()
							+ " AND level = " + level);
						   
				preStmt.setString(1, speakerName);
				preStmt.setString(2, speaker.getTyp());
				
				res = preStmt.executeQuery();
				
				if (res.next())
					speaker.setDB_ID(key, res.getInt("id"));
				else
					throw new DBC_SaveException("Speaker " + speaker
							+ "konnte nicht angelegt werden");
			}

			res.close();
		}
		connection.commit();
		connection.setAutoCommit(true);
		stmt.close();

		return speakers;
	}
      
	public void saveSpeakerChanges(Integer chapterID, ArrayList<DialogSpeakerChange> changes, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID);
	  
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			ResultSet.CONCUR_UPDATABLE);
		ResultSet res;
		
		// alle SpeakerChanges des Chapters werden gel�scht
		stmt.execute("DELETE FROM `speaker_changes` WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level);


		for (int i = 0; i < changes.size(); i++) 
		{
			DialogSpeakerChange sc = (DialogSpeakerChange) changes.get(i);

			res = stmt.executeQuery("SELECT * "
					+ "FROM speaker_changes "
					+ "WHERE id = " + sc.getDB_ID()
					+ " AND level = " + level);
			   
			res.moveToInsertRow();
			res.updateInt("chapter", chapter.getDB_ID());
			res.updateString("description", sc.getDescription());
			res.updateString("typ", sc.getTyp());
			res.updateInt("location", sc.getIUIndex());
			res.updateInt("level", level);
			res.insertRow();
			res.close();
			sc.resetState(key);	
			
			PreparedStatement preStmt = connection.prepareStatement("SELECT id "
					+ "FROM speaker_changes WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " and description =  ? "  
					+ " and typ =  ?"
					+ " and location = " + sc.getIUIndex()
					+ " AND level = " + level);
				   
			
			preStmt.setString(1, sc.getDescription());
			preStmt.setString(2, sc.getTyp());
			res = preStmt.executeQuery();
			
			if (res.next()) {
				sc.setDB_ID(key, res.getInt("id"));
			}
			else 
			{
				throw new DBC_SaveException("Sprecherwechsel "
					+ sc
					+ "konnte nicht angelegt werden");
			}
						   
			res.close();
			connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
	}
  
	public void saveTargets(Integer chapterID, ArrayList<DialogTarget> targets, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID);
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet res;
		
		// alle SpeakerChanges des Chapters werden gel�scht
		stmt.execute("DELETE FROM `targets` WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level);

		for (int i = 0; i < targets.size(); i++) 
		{
			DialogTarget dt = (DialogTarget) targets.get(i);

			res = stmt.executeQuery("SELECT * "
					+ "FROM targets "
					+ "WHERE id = " + dt.getDB_ID()
					+ " AND level = " + level);
			res.moveToInsertRow();
			res.updateInt("chapter", chapter.getDB_ID());
			res.updateString("description", dt.getDescription());
			res.updateInt("targetNr", dt.getTargetNr());
			res.updateString("target", dt.getTarget());
			res.updateInt("location", dt.getIUIndex());
			res.updateInt("level", level);
			res.insertRow();
			res.close();
			dt.resetState(key);	
			
			PreparedStatement preStmt = connection.prepareStatement("SELECT id "
					+ "FROM targets WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " and description =  ? "
					+ " and targetNr =  " + dt.getTargetNr() 
					+ " and target =  ? "
					+ " and location = " + dt.getIUIndex()
					+ " AND level = " + level);
			preStmt.setString(1, dt.getDescription());
			preStmt.setString(2, dt.getTarget());
			
			res = preStmt.executeQuery();
			if (res.next()) {
				dt.setDB_ID(key, res.getInt("id"));
			}
			else {
				throw new DBC_SaveException("Target " + dt + "konnte nicht angelegt werden");
			}
			res.close();
			connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
	}

	public void saveISignals(Integer chapterID, ArrayList<DialogISignal> signals, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID);
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet res;
		
		// alle SpeakerChanges des Chapters werden gel�scht
		stmt.execute("DELETE FROM `i_signals` WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level);

		for (int i = 0; i < signals.size(); i++) 
		{
			DialogISignal sig = (DialogISignal) signals.get(i);

			res = stmt.executeQuery("SELECT * "
					+ "FROM i_signals "
					+ "WHERE id = " + sig.getDB_ID()
					+ " AND level = " + level);
			res.moveToInsertRow();
			res.updateInt("chapter", chapter.getDB_ID());
			res.updateBoolean("signal", sig.getSignal());
			res.updateInt("location", sig.getIUIndex());
			res.updateInt("level", level);
			res.insertRow();
			res.close();
			sig.resetState(key);	
			
			PreparedStatement preStmt = connection.prepareStatement("SELECT id "
					+ "FROM i_signals WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " and signal =  " + sig.getSignal()
					+ " and location = " + sig.getIUIndex()
					+ " AND level = " + level);
			res = preStmt.executeQuery();
			
			if (res.next()) 
				sig.setDB_ID(key, res.getInt("id"));
			else 
				throw new DBC_SaveException("I_Signal "	+ sig + "konnte nicht angelegt werden");
			
			res.close();
			connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
	}

	public void saveComments(Integer chapterID, ArrayList<DialogComment> comments, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID);
	  
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			ResultSet.CONCUR_UPDATABLE);
		ResultSet res;
		
		// alle SpeakerChanges des Chapters werden gel�scht
		stmt.execute("DELETE FROM `comments` WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level);


		for (int i = 0; i < comments.size(); i++) 
		{
			DialogComment com = (DialogComment) comments.get(i);

			res = stmt.executeQuery("SELECT * "
					+ "FROM comments "
					+ "WHERE id = " + com.getDB_ID()
					+ " AND level = " + level);
			res.moveToInsertRow();
			res.updateInt("chapter", chapter.getDB_ID());
			res.updateString("comment", com.getComment());
			res.updateInt("commentNr", com.getCommentNr());
			res.updateInt("location", com.getIUIndex());
			res.updateInt("level", level);
			res.insertRow();
			res.close();
			com.resetState(key);	
			
			PreparedStatement preStmt = connection.prepareStatement("SELECT id "
					+ "FROM comments WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " and comment =  ?"
					+ " and commentNr =  " + com.getCommentNr()
					+ " and location = " + com.getIUIndex()
					+ " AND level = " + level);
			preStmt.setString(1, com.getComment());
			res = preStmt.executeQuery();
			
			if (res.next()) 
				com.setDB_ID(key, res.getInt("id"));
			else 
				throw new DBC_SaveException("Comment " + com + "konnte nicht angelegt werden");
			
			res.close();
			connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
	}

    public Vector<String> getDBUsers() throws Exception
    {
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery("SHOW FULL PROCESSLIST");

		Vector<String> user = new Vector<String>();
		
		while (res.next())
		{
			String s = "";
			
			s += res.getString("Host")+" ";
			s += res.getString("Command")+" ";
			s += res.getString("Time")+" ";
			s += res.getString("Info").replace(' ', '_')+" ";
			s += res.getString("DB")+";";
			
			user.add(s);
		}
		stmt.close();
		return user;
    }
    
	public synchronized Dialogs loadDialogs(Integer chapterID, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID.intValue());
		Dialogs dialogs = new Dialogs();
		Vector<Dialog> ds = new Vector<Dialog>();
		Statement stmt = connection.createStatement();
		ResultSet res;

		// Grunddaten der Dialoge einlesen.
		res = stmt.executeQuery("SELECT id, `index`, depth, start, end, accepted "
						+ "FROM dialogs WHERE chapter = "
						+ chapter.getDB_ID()
						+ " AND level = " + level
						+ " ORDER BY `index`");

		while (res.next()) {
			Dialog dialog = new Dialog(key, res.getInt("id"), chapter, res.getInt("index"), 
									   res.getInt("depth"), res.getBoolean("accepted"));
			dialog.setDialogStart(chapter.getIllocutionUnitWithID(res.getInt("start")));
			dialog.setDialogEnd(chapter.getIllocutionUnitWithID(res.getInt("end")));
			ds.add(dialog);
		}

		for (int i = 0; i < ds.size(); i++) {
			Dialog dialog = (Dialog)ds.get(i);
			res = stmt.executeQuery("SELECT start, end, description "
				+ "FROM cosmologies WHERE dialog = " + dialog.getDB_ID()
				+ " AND level = " + level);

			while (res.next()) {
				DialogCosmology cosmol = new DialogCosmology(dialog, 
				res.getInt("start"), 
				res.getInt("end"), 
				res.getString("description") );
				dialog.setCosmology(cosmol);
			}
		}
		for (int i = 0; i < ds.size(); i++) {
			Dialog dialog = (Dialog) ds.get(i);
			dialogs.add(dialog);
		}

		stmt.close();
		return dialogs;
	}
 
	public synchronized ArrayList<DialogSpeaker> loadSpeakers (Integer chapterID, String typ, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID.intValue());
		ArrayList<DialogSpeaker> speakers = new ArrayList<DialogSpeaker>();
		Statement stmt = connection.createStatement();
		ResultSet res;

		PreparedStatement preStmt = connection.prepareStatement("SELECT * "
				+ "FROM speakers WHERE " 
				+ "chapter = " + chapter.getDB_ID()
				+ " and typ = ?"
				+ " AND level = " + level
				+ " ORDER BY `location` ASC ");
		preStmt.setString(1, typ);
		res = preStmt.executeQuery();
			
		if ( res.next() ) {
			int location = res.getInt("location");
			Map<Integer, String> map = new HashMap<Integer, String>();
			map.put(res.getInt("speakerNr"), res.getString("speaker"));
			
			while ( res.next() ) {
				if ( location == res.getInt("location") )
					map.put(res.getInt("speakerNr"), res.getString("speaker"));
				else {
					DialogSpeaker speaker = new DialogSpeaker(chapter, map, typ, location);
					speakers.add(speaker);
					location = res.getInt("location");
					map = new HashMap<Integer, String>();
					map.put(res.getInt("speakerNr"), res.getString("speaker"));
				}
			}
			DialogSpeaker speaker = new DialogSpeaker(chapter, map, typ, location);
			speakers.add(speaker);
		}
		res.close();
		stmt.close();
		return speakers;
	}

	public synchronized ArrayList<DialogSpeakerChange> loadSpeakerChanges (Integer chapterID, String typ, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID.intValue());
		ArrayList<DialogSpeakerChange> changes = new ArrayList<DialogSpeakerChange>();
		Statement stmt = connection.createStatement();
		ResultSet res;

		PreparedStatement preStmt = connection.prepareStatement("SELECT * "
					+ "FROM speaker_changes WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " and typ = ?"
					+ " AND level = " + level);
		 preStmt.setString(1, typ);
		 res = preStmt.executeQuery();			
					
		 while ( res.next() ) {
			 DialogSpeakerChange change = new DialogSpeakerChange(chapter, res.getString("description"), res.getInt("location"), typ);
			 changes.add(change);
		 }
		 res.close();
		 stmt.close();
		 return changes;
	}

	public synchronized ArrayList<DialogD_Themat> loadD_Themat (Integer chapterID, Integer level)
	throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		ArrayList<DialogD_Themat> d_themats = new ArrayList<DialogD_Themat>();
		Statement stmt = connection.createStatement();
		ResultSet res, res2;

		PreparedStatement preStmt = connection.prepareStatement("SELECT * "
					+ "FROM d_themat WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " AND level = " + level);
		res = preStmt.executeQuery();
			
		while ( res.next() ) {
			PreparedStatement preStmt2 = connection.prepareStatement("SELECT * "
					+ "FROM options_of_d_themat WHERE " 
					+ "d_themat = " + res.getInt("id"));
			res2 = preStmt2.executeQuery();

			Vector<String> options = new Vector<String>();
			if (res2.next()) {
				if (res2.getBoolean("agree")) {
					options.add("agree");
				}
				if (res2.getBoolean("disagree")) {
					options.add("disagree");
				}
				if (res2.getBoolean("obey")) {
					options.add("obey");
				}
				if (res2.getBoolean("refuse")) {
					options.add("refuse");
				}
				if (res2.getBoolean("accept")) {
					options.add("accept");
				}
				if (res2.getBoolean("reject")) {
					options.add("reject");
				}
				if (res2.getBoolean("approve")) {
					options.add("approve");
				}
				if (res2.getBoolean("disapprove")) {
					options.add("disapprove");
				}		    	
			}	    	
			DialogD_Themat d_themat = new DialogD_Themat(chapter, res.getString("description"), res.getInt("location"), options);
			d_themats.add(d_themat);
		}
		res.close();
		stmt.close();
		return d_themats;
	}

	public void saveD_Themat(Integer chapterID, ArrayList<DialogD_Themat> themats, Integer level)
	throws Exception {
		Chapter chapter = getChapter(chapterID);
	  
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			ResultSet.CONCUR_UPDATABLE);
		ResultSet res;
		
		// alle SpeakerChanges des Chapters werden gel�scht
		stmt.execute("DELETE FROM `d_themat` WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level);

		for (int i = 0; i < themats.size(); i++) 
		{
			DialogD_Themat dt = (DialogD_Themat) themats.get(i);
			res = stmt.executeQuery("SELECT * "
					+ "FROM d_themat "
					+ "WHERE id = " + dt.getDB_ID()
					+ " AND level = " + level);
			res.moveToInsertRow();
			res.updateInt("chapter", chapter.getDB_ID());
			res.updateString("description", dt.getDescription());
			res.updateInt("location", dt.getIUIndex());
			res.updateInt("level", level);
			res.insertRow();
			res.close();
			dt.resetState(key);	
			
			PreparedStatement preStmt = connection.prepareStatement("SELECT id "
					+ "FROM d_themat WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " and description =  ? "  
					+ " and location = " + dt.getIUIndex()
					+ " AND level = " + level);
			preStmt.setString(1, dt.getDescription());
			res = preStmt.executeQuery();
			
			if (res.next()) 
			{
				dt.setDB_ID(key, res.getInt("id"));
				
				// save options
				if (dt.getOptions().size() != 0)
				{
					res = stmt.executeQuery("SELECT * "
							+ "FROM options_of_d_themat ");
					res.moveToInsertRow();
					res.updateInt("d_themat", dt.getDB_ID());	    	
					res.updateBoolean("agree", dt.isAgree());
					res.updateBoolean("disagree", dt.isDisagree());	    	
					res.updateBoolean("obey", dt.isObey());
					res.updateBoolean("refuse", dt.isRefuse());
					res.updateBoolean("accept", dt.isAccept());
					res.updateBoolean("reject", dt.isReject());	    	
					res.updateBoolean("approve", dt.isApprove());
					res.updateBoolean("disapprove", dt.isDisapprove());
					res.insertRow();
					res.close();
					dt.resetState(key);
				}
			}
			else 
			{
				throw new DBC_SaveException("d_thematisch Eintrag "
					+ dt
					+ "konnte nicht angelegt werden");
			}
			res.close();
			connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
	}

	public synchronized ArrayList<DialogFaces> loadFaces (Integer chapterID, Integer level)
	throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		ArrayList<DialogFaces> faces = new ArrayList<DialogFaces>();
		Statement stmt = connection.createStatement();
		ResultSet res;

		PreparedStatement preStmt = connection.prepareStatement("SELECT * "
					+ "FROM faces WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " AND level = " + level);
		res = preStmt.executeQuery();
			
		ArrayList<DialogSpeaker> speakers = loadSpeakers(chapterID, DialogSpeaker.FACE_TYP, level);
		while ( res.next() ) {
			DialogSpeaker speaker = null;
			for (DialogSpeaker dialogSpeaker : speakers) {
				if (dialogSpeaker.getIUIndex() == res.getInt("location")) {
					speaker = dialogSpeaker;
					break;
				}
			}
			res.getInt("location");
			DialogFaces face = new DialogFaces(chapter, res.getInt("location"), res.getString("description"), speaker);
			faces.add(face);
		}			
		res.close();
		stmt.close();
		return faces;
	}
	/**
	 * Wird vom Dialogprogramm benutzt
	 * Es werden die Faces in der DB gespeichert. 
	 * Die zu den Faces zugeh�rigen Speakers m�ssen separat mit saveSpeakers gespeichert werden.
	 * @param chapterID
	 * @param faces
	 * @return
	 * @throws Exception
	 */
	public synchronized void saveFaces (Integer chapterID, ArrayList<DialogFaces> faces, Integer level)
	throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet res;
		
		// alle Speakers des Chapters werden gel�scht
		stmt.execute("DELETE FROM faces WHERE chapter = " + chapter.getDB_ID()
				+ " AND level = " + level);
		
		for (int i = 0; i < faces.size(); i++) {
			DialogFaces face = (DialogFaces) faces.get(i);
			res = stmt.executeQuery("SELECT * " + "FROM faces "
					+ "WHERE id = " + face.getDB_ID()
					+ " AND level = " + level);
			res.moveToInsertRow();
			res.updateInt("chapter", chapter.getDB_ID());
			res.updateString("description", face.getDescription());
			res.updateInt("location", face.getIUIndex());
			res.updateInt("level", level);
			res.insertRow();
			res.close();
			face.resetState(key);
			
			PreparedStatement preStmt = connection.prepareStatement("SELECT id "
					+ "FROM faces WHERE " 
					+ "chapter = " + chapter.getDB_ID() 
					+ " and `description` = ?"
					+ " and location = " + face.getIUIndex()
					+ " AND level = " + level);
			preStmt.setString(1, face.getDescription());
			res = preStmt.executeQuery();
			
			if (res.next())
				face.setDB_ID(key, res.getInt("id"));
			else {
				throw new DBC_SaveException("Face " + face
						+ "konnte nicht angelegt werden");
			}
			res.close();
		}
		connection.commit();
		connection.setAutoCommit(true);
		stmt.close();
	}

	
	public synchronized ArrayList<DialogTarget> loadTargets (Integer chapterID, Integer level) throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		ArrayList<DialogTarget> targets = new ArrayList<DialogTarget>();
		Statement stmt = connection.createStatement();
		ResultSet res;

		PreparedStatement preStmt = connection.prepareStatement("SELECT * "
					+ "FROM targets WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " AND level = " + level);
		res = preStmt.executeQuery();
			
		while ( res.next() ) {
			DialogTarget target = new DialogTarget(chapter, res.getInt("location"), 
					res.getString("description"), res.getInt("targetNr"), res.getString("target"));
			targets.add(target);
		}			
		res.close();
		stmt.close();
		return targets;
	}

	public synchronized ArrayList<DialogISignal> loadISignals (Integer chapterID, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID.intValue());
		ArrayList<DialogISignal> signals = new ArrayList<DialogISignal>();
		Statement stmt = connection.createStatement();
		ResultSet res;

		PreparedStatement preStmt = connection.prepareStatement("SELECT * "
					+ "FROM i_signals WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " AND level = " + level);
		res = preStmt.executeQuery();
			
		while ( res.next() ) {
			DialogISignal signal = new DialogISignal(chapter, res.getBoolean("signal"), res.getInt("location"));
			signals.add(signal);
		}			
		res.close();
		stmt.close();
		return signals;
	}

	public synchronized ArrayList<DialogComment> loadComments (Integer chapterID, Integer level) throws Exception 
	{
		Chapter chapter = getChapter(chapterID.intValue());
		ArrayList<DialogComment> comments = new ArrayList<DialogComment>();
		Statement stmt = connection.createStatement();
		ResultSet res;

		PreparedStatement preStmt = connection.prepareStatement("SELECT location, comment, commentNr "
					+ "FROM comments WHERE " 
					+ "chapter = " + chapter.getDB_ID()
					+ " AND level = " + level);
		res = preStmt.executeQuery();
			
		while ( res.next() ) {
			DialogComment comment = new DialogComment(chapter, res.getInt("location"), res.getString("comment"), res.getInt("commentNr"));
			comments.add(comment);
		}			
		res.close();
		stmt.close();
		return comments;
	}
	
    /**
     * Load the IllocutionUnitRoots for the chapter identified by <code>chapterID</code>
     * @param chapterID
     * @return The IllocutionUnitRoots for the given <code>chapterID</code>, <code>null</code> if chapter could not be found
     * @throws DBC_LoadException
     */
    public synchronized IllocutionUnitRoots loadIllocutionUnitRoots(Integer chapterID) throws DBC_LoadException {
		if(chapterID == null)
		    throw new NullPointerException();
		
		Chapter chapter = null;
		Vector<IllocutionUnitRoot> roots = null;
		Vector<FunctionWord> fws = null;
		Vector<ConstitutiveWord> cws = null;
		Vector<MeaningUnit> mus = null;
		IllocutionUnitRoots iurs = null;
		try {
		    chapter = getChapter(chapterID.intValue());
		    if(chapter == null)
		    	return null;
		    
		    roots = makeIllocutionUnitRoots(chapter);
		    fws = loadFunctionWords(chapter, roots);
		    cws = loadConstitutiveWords(chapter, roots);
		    mus = loadMeaningUnits(chapter, roots, fws, cws);
		    loadSememeGroups(chapter, roots, mus, fws);
		    iurs = new IllocutionUnitRoots(key, chapter, roots);
		    loadCheckings(chapter, iurs, mus);
		    loadMacroSentences(chapter, iurs);
		} catch (Exception e) {
		    logger.log(Level.WARNING, "Loading IllocutionUnitRoots for Chapter "+chapterID+" failed.", e);
		    throw new DBC_LoadException("Server Error");
		}
		
		return iurs;
    }

    /**
     * Create IllocutionUnitRoots for given <code>chapter</code>
     * @param chapter
     * @return A Vector of IllocutionUnitRoot Elements
     * @throws Exception
     */
    private Vector<IllocutionUnitRoot> makeIllocutionUnitRoots(Chapter chapter)
    throws Exception {
		Vector ius = chapter.getIllocutionUnits();
		Vector<IllocutionUnitRoot> roots = new Vector<IllocutionUnitRoot>(ius.size());
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery("SELECT * FROM illocution_units "
			+ "WHERE chapter = "
			+ chapter.getDB_ID());

		while (res.next()) {
		    int start = res.getInt("start");
		    int path = res.getInt("path");
		    int numerus_path = res.getInt("numerus_paths");
		    int phrastic = res.getInt("phrastic");
		    IllocutionUnit iu = chapter.getIllocutionUnitAtPosition(start);
		    roots.add(new IllocutionUnitRoot(key, iu, path, numerus_path, phrastic));
		}
		stmt.close();
		return roots;
    }

    private Vector<FunctionWord> loadFunctionWords(Chapter chapter, Vector<IllocutionUnitRoot> roots)
    throws Exception {
		if(chapter == null || roots == null)
		    throw new NullPointerException();
		
		Statement stmt = connection.createStatement();
		Vector<FunctionWord> fwords = new Vector<FunctionWord>();
		ResultSet res = stmt.executeQuery("SELECT * FROM function_words "
			+ "WHERE chapter = "
			+ chapter.getDB_ID());

		while (res.next()) {
		    Token token = chapter.getTokenAtPosition(res.getInt("start"));
		    if( token ==  null) {
				logger.warning("No Token found at position "+res.getInt("start")+" in chapter "+chapter);
				continue;
		    }
		    if(token.getIllocutionUnit() == null) // TODO Check if it is acceptable that a token has no IllocutionUnit
		    	continue;
		    
		    IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(token
			    .getIllocutionUnit().getDB_ID(), roots);
		    
		    if (token instanceof Word) {
		    	TR_Assignation assignation = loadAssignation(res.getInt("assignation_id"));
		    	fwords.add(new FunctionWord(key, root, res.getInt("id"),
		    			(Word) token, res.getInt("start"), res.getInt("end"), res
		    			.getBoolean("accepted"), assignation));
		    }
		}
		stmt.close();
		return fwords;
    }

	/**
     * Inserts, updates or removes <code>assignations</code> in the Database dependent on their state.
     * @param assignations
     * @return assignations with updates DB_IDs
     * @throws SQLException
     * @throws NullPointerException if <code>assignations</code> is null
     */
    private synchronized TR_Assignation[] saveAssignations(TR_Assignation ... assignations)
    throws SQLException, NullPointerException {
		logger.entering(this.getClass().getName(), "saveAssignation", assignations);
		if(assignations == null)
		    throw new IllegalArgumentException("");

		PreparedStatement stmt = null;
		ResultSet res = null;
	
		try {
		    for (TR_Assignation tr_assignation : assignations) {
				if(tr_assignation == null) {
				    logger.info("assignation is null");
				    continue;
				}
				if(tr_assignation.isUnchanged()) {
				    logger.finest("assignation unchanged");
				    continue;
				}
	
				TR_Assignation_DB assignation_db = tr_assignation.new TR_Assignation_DB();
				stmt = connection.prepareStatement(
					"SELECT * FROM assignations WHERE id = ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				stmt.setInt(1, assignation_db.getDB_ID());
				res = stmt.executeQuery();
	
				if (assignation_db.hasChanged()) {
				    if(! res.next()) 
				    	res.moveToInsertRow();
				    
				    res.updateBytes("tr_type", assignation_db.getTypesBinary());
				    res.updateBytes("tr_genus", assignation_db.getGeneraBinary());
				    res.updateBytes("tr_numerus", assignation_db.getNumeriBinary());
				    res.updateBytes("tr_determination", assignation_db.getDeterminationsBinary());
				    res.updateBytes("tr_case", assignation_db.getCasesBinary());
				    res.updateBytes("tr_person", assignation_db.getPersonsBinary());
				    res.updateBytes("tr_tempus", assignation_db.getTemporaBinary());
				    res.updateBytes("tr_konjugation", assignation_db.getKonjugationBinary());
				    res.updateBytes("tr_diathese", assignation_db.getDiathesesBinary());
				    res.updateBytes("tr_wordclass", assignation_db.getWordclassesBinary());
				    res.updateBytes("tr_wortart1", assignation_db.getWortarten1Binary());
				    res.updateBytes("tr_wortart2", assignation_db.getWortarten2Binary());
				    res.updateBytes("tr_wortart3", assignation_db.getWortarten3Binary());
				    res.updateBytes("tr_wortart4", assignation_db.getWortarten4Binary());
				    res.updateBytes("tr_subclass_verb", assignation_db.getWordsubclassesVerbBinary());
				    res.updateBytes("tr_subclass_verb_modification", assignation_db.getSubclassesVerbModificationBinary());
				    res.updateBytes("tr_subclass_adjective", assignation_db.getWordsubclassesAdjectiveBinary());
				    res.updateBytes("tr_subclass_pronoun", assignation_db.getWordsubclassesPronounBinary());
				    res.updateBytes("tr_subclass_punctuationmark", assignation_db.getWordsubclassesPunctuationMarkBinary());
				    res.updateString("description", assignation_db.getDescription());
				    res.updateString("etymol", assignation_db.getEtymol());
				    res.updateString("abbreviation", assignation_db.getAbbreviation());
				    
				    if(res.isFirst()) 
				    	res.updateRow();
				    else {
						res.insertRow();
						res.last();
						tr_assignation.setDB_ID(key, res.getInt("id"));
						logger.fine("new assignation inserted. ID="+tr_assignation.getDB_ID());
				    }
				    assignation_db.resetState(key);
				}
				else if(assignation_db.isRemoved() && res.next())
				    res.deleteRow();
			}
		}
		catch ( SQLException e ) {
			System.err.println(e.getMessage());
		    logger.severe(e.getLocalizedMessage());
		    throw e;
		}
		finally {
		    try {
			if (res  != null)
			    res.close();
			if (stmt  != null)
			    stmt.close();
		    }
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return assignations;
    }
    
    private TR_Assignation loadAssignation(int id) throws SQLException
    {
		PreparedStatement stmt = null;
		ResultSet res = null;
		TR_Assignation_DB assignation = null;
		try {
		    stmt = connection.prepareStatement("SELECT * FROM assignations WHERE id = ?");
		    stmt.setInt(1, id);
		    res = stmt.executeQuery();
		    if(res.next()) 
		    {
				assignation = new TR_Assignation().new TR_Assignation_DB();
				assignation.setTypesBinary(res.getBytes("tr_type"));
				assignation.setCasesBinary( res.getBytes("tr_case"));
				assignation.setDeterminationsBinary(res.getBytes("tr_determination"));
				assignation.setKonjugationBinary(res.getBytes("tr_konjugation"));
				assignation.setDiathesesBinary(res.getBytes("tr_diathese"));
				assignation.setGeneraBinary(res.getBytes("tr_genus"));
				assignation.setNumeriBinary(res.getBytes("tr_numerus"));
				assignation.setPersonsBinary(res.getBytes("tr_person"));
				assignation.setTemporaBinary(res.getBytes("tr_tempus"));
				assignation.setWordclassesBinary(res.getBytes("tr_wordclass"));
				assignation.setWortarten1Binary(res.getBytes("tr_wortart1"));
				assignation.setWortarten2Binary(res.getBytes("tr_wortart2"));
				assignation.setWortarten3Binary(res.getBytes("tr_wortart3"));
				assignation.setWortarten4Binary(res.getBytes("tr_wortart4"));
				assignation.setWordsubclassesAdjectiveBinary(res.getBytes("tr_subclass_adjective"));
				assignation.setWordsubclassesVerbBinary(res.getBytes("tr_subclass_verb"));
				assignation.setWordsubclassesPronounBinary(res.getBytes("tr_subclass_pronoun"));
				
				//TODO: add other columns
				assignation.setDB_ID(key, res.getInt("id"));
				assignation.resetState(key);
		    }
		}
		catch ( SQLException e ) {
			logger.severe(e.getLocalizedMessage());
		    throw e;
		}
		finally {
		    try {
				if (res  != null)
				    res.close();
				if (stmt  != null)
				    stmt.close();
		    }
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return assignation;
    }

    /**
     * Load Meaning Units for Chapter with id = <code>chapter.getDB_ID()</code>
     * @param chapter
     * @param roots
     * @param funtionWords (allowed to be null)
     * @param constitutiveWords
     * @return a Vector of Meaning Units
     */
    /* TODO: Die Erstellung der MUs sollte hier nur vorbereitet (speichern der IDs der referenzierten Elemente (CW, FW, ..))
     * und erst auf der Klient Seite abgeschossen werden. Siehe loadComplexes
     */
    private Vector<MeaningUnit> loadMeaningUnits(
	    Chapter chapter,
	    Vector<IllocutionUnitRoot> roots,
	    Vector<FunctionWord> funtionWords, /* should be made optional */
	    Vector<ConstitutiveWord> constitutiveWords) {
	
		if(chapter==null || roots== null || funtionWords==null || constitutiveWords==null)
		    throw new NullPointerException();
		
		Vector<MeaningUnit> mus = new Vector<MeaningUnit>();
		PreparedStatement stmt = null;
		ResultSet res = null;
		
		try {
		    stmt = connection.prepareStatement(
		    	"SELECT * FROM meaning_units " +
		    	"WHERE illocution_unit in " +
		    	"(SELECT id FROM illocution_units WHERE chapter = ?)");
		    stmt.setInt(1, chapter.getDB_ID());
			res = stmt.executeQuery();
			
			while (res.next()) {
			    IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(res
				    .getInt("illocution_unit"), roots);
			    FunctionWord fw = (FunctionWord) getElement(res
				    .getInt("function_word"), funtionWords);
			    ConstitutiveWord cw = (ConstitutiveWord) getElement(res
				    .getInt("constitutive_word"), constitutiveWords);
			    
			    if(root == null || cw == null) {
					logger.warning("Not enough information to create Meaning Unit. Skipping MU#"+res.getInt("id"));
					continue;
			    }
			    mus.add( new MeaningUnit(key, root, res.getInt("id"), fw, cw,
		    		    res.getInt("path"), res.getInt("numerus_paths"), res.getBoolean("accepted")));
			}
		} catch (SQLException e) {
		    logger.log(Level.SEVERE, "Unable to load Meaning Units", e);
		} finally {    
		    try {
			if(res != null)
			    res.close();
			if(stmt != null)
			    stmt.close();
		    } catch (SQLException e) { /* IGNORE */ }
		}
		return mus;
    }
    
    private void saveMeaningUnits(Vector meaningUnits)
    throws Exception {
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
				    	res.updateInt("function_word", mu.getFunctionWord().getDB_ID());
				    else
						res.updateNull("function_word");
					res.updateInt("constitutive_word", mu.getConstitutiveWord().getDB_ID());
					res.updateInt("path", mu.getPath());
					res.updateBoolean("accepted", mu.isAccepted());
					res.updateInt("numerus_paths", mu.getNumerusPath());
					res.updateRow();
					mu.resetState(key);
				}
				else if (mu.isRemoved())
				    res.deleteRow();
			}
		    else if (!mu.isRemoved()) {
				res.moveToInsertRow();
				res.updateInt("illocution_unit", mu.getRoot().getIllocutionUnit().getDB_ID());
				if (mu.getFunctionWord() != null)
				    res.updateInt("function_word", mu.getFunctionWord().getDB_ID());
				res.updateInt("constitutive_word", mu.getConstitutiveWord().getDB_ID());
				res.updateInt("path", mu.getPath());
				res.updateInt("numerus_paths", mu.getNumerusPath());
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
		    // schreibe Aenderung in die DB
		    connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
    }

    private Vector loadSememeGroups(Chapter chapter, Vector roots, Vector meaningUnits, Vector funtionWords)
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

    private Vector loadCheckings(Chapter chapter, IllocutionUnitRoots iurs, Vector mus)
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
	    IllocutionUnitRoots iurs) throws Exception {
	
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
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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

    private void saveSememeGroups(Vector sememeGroups)
    throws Exception {
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,	ResultSet.CONCUR_UPDATABLE);
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
				    res.updateInt("illocution_unit", sg.getRoot().getIllocutionUnit().getDB_ID());
				    if (sg.getFunctionWord() != null)
				    	res.updateInt("function_word", sg.getFunctionWord().getDB_ID());
				    else
				    	res.updateNull("function_word");
				    res.updateInt("meaning_unit_1", sg.getFirst().getDB_ID());
				    res.updateInt("meaning_unit_2", sg.getSecond().getDB_ID());
				    res.updateInt("path", sg.getPath());
				    res.updateInt("numerus_paths", sg.getNumerusPath());
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
				res.updateInt("illocution_unit", sg.getRoot().getIllocutionUnit().getDB_ID());
				if (sg.getFunctionWord() != null)
				    res.updateInt("function_word", sg.getFunctionWord().getDB_ID());
				else
				    res.updateNull("function_word");
				res.updateInt("meaning_unit_1", sg.getFirst().getDB_ID());
				res.updateInt("meaning_unit_2", sg.getSecond().getDB_ID());
				res.updateInt("path", sg.getPath());
				res.updateInt("numerus_paths", sg.getNumerusPath());
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
			// schreibe Aenderung in die DB
			connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
    }

    private void saveCheckings(Vector checkings)
    throws Exception {
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
	//TODO ??	    res.updateInt("numerus_path", ch.getNumerusPath());
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
		    // schreibe Aenderung in die DB
		    connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
    }

    private void saveMacroSentences(Vector macroSentences)
    throws Exception {
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
		//TODO	    res.updateInt("numerus_path", ms.getPath());
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
	//TODO		res.updateInt("numerus_path", ms.getPath());
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
		    // schreibe Aenderung in die DB
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
		    fwords.add(new String(res.getBytes("content"), "ISO-8859-1"));
	
		stmt.close();
		return fwords;
    }

    public synchronized Vector loadFunctionWordsCategories()
    throws Exception {
		Statement stmt = connection.createStatement();
	
		// Anzahl der Kategorien bestimmen
		ResultSet res = stmt.executeQuery("SELECT count(*) FROM function_words_categories");
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

    private void saveFunctionWords(Vector<FunctionWord> words) throws Exception {
    	logger.entering(this.getClass().getName(), "saveFunctionWords", words);
    	if(words == null) {
    	    logger.warning("unexpected null pointer as method parameter");
    	    return;
    	}

    	PreparedStatement stmt = null;
    	ResultSet res = null;
    	try {
	    	connection.setAutoCommit(false);
	    	stmt = connection.prepareStatement("SELECT * FROM function_words WHERE id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	  	
	    	for (FunctionWord functionWord : words) {
	    		// Check for exceptions
	    		if(functionWord == null) {
	    		    logger.warning("functionWordfunctionWord is unexpectedly a null pointer");
	    		    continue;
	    		}

	    		if (functionWord.getWord() == null) {
	    		    logger.warning("functionWord has unexpectedly no associated word");
	    		    continue;
	    		}
	    		// Get assignation
	    		TR_Assignation assi = functionWord.getAssignation();              

	    		stmt.setInt(1, functionWord.getDB_ID());			   
	    		res = stmt.executeQuery();

    			if (functionWord.hasChanged()) {
    			    if(assi != null)
    			    	saveAssignations(assi);
    			    if(! res.next()) 
    			    	res.moveToInsertRow();
	    			    
    			    res.updateInt("chapter", functionWord.getWord().getChapter().getDB_ID());
    			    res.updateInt("word", functionWord.getWord().getDB_ID());
    			    res.updateInt("start", functionWord.getStartPosition());
    			    res.updateInt("end", functionWord.getEndPosition());
    			    res.updateBoolean("accepted", functionWord.isAccepted());
    			    if(assi != null)
    			    	res.updateInt("assignation_id", functionWord.getAssignation().getDB_ID());
    			    if(res.isFirst()) 
    			    	res.updateRow();
    			    else {
    			    	res.insertRow();
    			    	res.last();
    			    	functionWord.setDB_ID(key, res.getInt("id"));
    			    }
    			    functionWord.resetState(key);
    			}
    			else if(functionWord.isRemoved() && res.next()) {
    			    res.deleteRow();
    			    if(assi != null) {
	    				assi.remove();
	    				saveAssignations(assi);
    			    }
	   			}			
    			
    			connection.commit();
    		}
		/*	for (int i = 0; i < words.size(); i++) {
			    if (!(words.get(i) instanceof FunctionWord))
				continue;
		
			    FunctionWord word = (FunctionWord) words.get(i);
		
			    if (word.getWord() == null) {
				System.err.println("Funktionswort " + word + "(" + word.getDB_ID() + ", " + word.getState() + ")" + " ist keinem Wort zugeordnet!");
				continue;
			    }
		
			    if (word.isUnchanged())
				continue;
		
			    //	  Get assignation
				TR_Assignation assi = word.getAssignation();         
			    res = stmt.executeQuery("SELECT * FROM function_words WHERE id = " + word.getDB_ID());
		
			    if (res.next() && word.getDB_ID() != -1) {
			    	if (word.hasChanged()) {
						if(assi != null)
							saveAssignations(assi);
					    res.updateInt("chapter", word.getWord().getChapter().getDB_ID());
					    res.updateInt("word", word.getWord().getDB_ID());
					    res.updateInt("start", word.getStartPosition());
					    res.updateInt("end", word.getEndPosition());
					    res.updateBoolean("accepted", word.isAccepted());
					    if(assi != null)
					    	res.updateInt("assignation_id", word.getAssignation().getDB_ID());
					    res.updateRow();
					    res.close();
					    word.resetState(key);
					} else if (word.isRemoved()) {
					    res.deleteRow();
						if(assi != null) {
							assi.remove();
							saveAssignations(assi);
					    }
					}
			    } else if (!word.isRemoved() && word.getDB_ID() == -1) {
					res.moveToInsertRow();
					res.updateInt("chapter", word.getWord().getChapter().getDB_ID());
					res.updateInt("word", word.getWord().getDB_ID());
					res.updateInt("start", word.getStartPosition());
					res.updateInt("end", word.getEndPosition());
					res.updateBoolean("accepted", word.isAccepted());
					res.insertRow();
					res.close();
					word.resetState(key);
			
					res = stmt.executeQuery("SELECT id FROM function_words WHERE word = " + word.getWord().getDB_ID() + " and chapter = "
						+ word.getWord().getChapter().getDB_ID() + " and start = " + word.getStartPosition() + " and end = " + word.getEndPosition());
			
					// hole die DB-ID zu dem neu angelegten FW
					if (res.next())
					    word.setDB_ID(key, res.getInt("id"));
					else
					    throw new DBC_SaveException("Funktionswort " + word + " konnte nicht in der " + "DB gespeichert werden!");
				    }
				    else
					System.err.println("Funktionswort " + word + "(" + word.getDB_ID() + ", " + word.getState() + ")" + " hat beim speichern einen Fehler verursacht!");
				    res.close();
				    connection.commit();
			}
			stmt.close();
			connection.setAutoCommit(true);*/
    	}
    	catch ( SQLException e ) {
    	    if(connection != null)
    		connection.rollback();
    	    logger.severe(e.getLocalizedMessage());
    	    throw e;
    	}
    	finally {
    	    try {
	    		if(connection != null)
	    		    connection.setAutoCommit(true);
	    		if (res  != null)
	    		    res.close();
	    		if (stmt  != null)
	    		    stmt.close();
    	    }
    	    catch (SQLException e) {
    	    	logger.warning(e.getLocalizedMessage());
    	    }
    	}
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
		    String content = new String(res.getBytes("content"), "ISO-8859-1");
		    int position = res.getInt("position");
		    int start = res.getInt("start");
		    int end = res.getInt("end");
	
		    DB_Tupel tupel = new DB_Tupel();
		    tupel.put("content", content.substring(start - position, Math.min(end - position + 1
		    		, content.length())));
		    tupel.put("start", start - position);
		    tupel.put("end", end - position);
		    tupel.put("chapter", res.getInt("fws.chapter"));
		    tupel.put("position", position);
		    result.add(tupel);
		}
	
		stmt.close();
		return result;
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
		    cwords.add(new String(res.getBytes("content"), "ISO-8859-1"));
	
		stmt.close();
		return cwords;
    }

    private Vector<ConstitutiveWord> loadConstitutiveWords(Chapter chapter, Vector<IllocutionUnitRoot> roots) throws Exception
    {
		PreparedStatement stmt = null;
		ResultSet res = null;
		Vector<ConstitutiveWord> cwords = new Vector<ConstitutiveWord>();
		try {
		    stmt = connection.prepareStatement(
			    "SELECT * FROM constitutive_words WHERE chapter = ?" );
		    stmt.setInt(1, chapter.getDB_ID());
		    res = stmt.executeQuery();

		    while (res.next()) {
				Token token = chapter.getTokenAtPosition(res.getInt("start"));
				if(token.getIllocutionUnit() == null) // TODO Check if it is acceptable that a token has no IllocutionUnit
					continue;
			
				IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(token
					.getIllocutionUnit().getDB_ID(), roots);
				
				if (token instanceof Word) {
					TR_Assignation assi = loadAssignation(res.getInt("assignation_id"));
				    ConstitutiveWord cw = new ConstitutiveWord(key, root, res.getInt("id"),
					    (Word) token, res.getInt("start"), res.getInt("end"), res.getBoolean("accepted"), res.getInt("lexprag_path"),
					    res.getInt("lexprag_level"), res.getInt("text_gr_path"), res.getInt("sem_path"),
					    assi);
				    cwords.add(cw);
				}
		    }
		}
		catch ( SQLException e ) {
		    logger.severe(e.getLocalizedMessage());
		    throw e;
		}
		finally {
		    try {
				if (res  != null)
				    res.close();
				if (stmt  != null)
				    stmt.close();
		    }
		    catch (SQLException e) {
				logger.warning(e.getLocalizedMessage());
		    }
		}
		return cwords;
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
		    stmt = connection.prepareStatement("SELECT * FROM constitutive_words WHERE id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
				    if(! res.next()) 
				    	res.moveToInsertRow();
				    
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
				    if(res.isFirst()) 
				    	res.updateRow();
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
		    try {
			if(connection != null)
			    connection.setAutoCommit(true);
			if (res  != null)
			    res.close();
			if (stmt  != null)
			    stmt.close();
		    }
		    catch (SQLException e) {
			logger.warning(e.getLocalizedMessage());
		    }
		}
    }

    public synchronized Boolean existsConstitutiveWord(Integer wordID, Integer length)
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
		String query = "SELECT DISTINCT "
			+ "SUBSTRING(CAST(content AS CHAR), start-position+1, end-position+1) AS content "
			+ "FROM constitutive_words AS cws, words, words_in_chapter AS winc "
			+ "WHERE words.id = cws.word "
			+ "AND words.id = winc.word "
			+ "AND winc.chapter = cws.chapter "
			+ "AND winc.position <= cws.start "
			+ "AND cws.end <= winc.position + length(content) "
			+ "AND words.language = '"
			+ language
			+ "' ORDER BY content";
		ResultSet res = stmt.executeQuery(query);
	
		while (res.next()) {
		    DB_Tupel tupel = new DB_Tupel();
		    tupel.put("content", res.getString("content"));
		    result.add(tupel);
		}
	
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
		    String content = new String(res.getBytes("content"), "ISO-8859-1");
		    int position = res.getInt("position");
		    int start = res.getInt("start");
		    int end = res.getInt("end");
	
		    content = content.substring(start - position, Math.min(end
			    - position
			    + 1, content.length()));
	
		    if (content.toLowerCase().matches(".*" + word.toLowerCase() + ".*")
			    || word.toLowerCase().matches(".*" + content.toLowerCase()+ ".*")) {
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
    
    private PathNode loadPaths() {
		PathNode rootPathNode = new PathNode(key, 0, null, "pathroot", "---");
		try {
		    Statement stmt = connection.createStatement();
		    ResultSet res = stmt.executeQuery("SELECT * FROM paths WHERE id > 0 ORDER BY parent");
		    while (res.next()) {
				PathNode parent = rootPathNode.getNode(res.getInt("parent"));
				new PathNode(key, res.getInt("id"), parent, res.getString("name"),
						res.getString("description"));
			}
		    stmt.close();
		}
		catch (SQLException e) {
		    e.printStackTrace();
		}
		return rootPathNode;
    }

    public PathNode getPaths() {
    	return root;
    }

    private PathNode loadNumerusPaths() {
		PathNode rootPathNode = new PathNode(key, 0, null, "numerusPathRoot", "---");
		try {
		    Statement stmt = connection.createStatement();
		    ResultSet res = stmt.executeQuery("select * from numerus_paths where id > 0 order by parent");
		    while (res.next()) {
				PathNode parent = rootPathNode.getNode(res.getInt("parent"));
				new PathNode(key, res.getInt("id"), parent, res.getString("name"), 
						res.getString("description"));
		    }
		    stmt.close();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		return rootPathNode;
    }

    public PathNode getNumerusPaths() {
    	return numerusRoot;
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
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("delete FROM themas WHERE chapter = " + chapterID);
		ResultSet res = stmt.executeQuery("SELECT * FROM themas WHERE chapter = " + chapterID);
	
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
		res = stmt.executeQuery("SELECT * FROM themas WHERE chapter = "	+ chapterID);
		while (res.next()) {
		    Thema_DB thema = (Thema_DB) themas.get(res.getInt("index"));
		    thema.setDB_ID(key, res.getInt("id"));
		}
	
		// Referenzen speichern
		res = stmt.executeQuery("SELECT * FROM themas WHERE chapter = " + chapterID);
		while (res.next()) {
		    Thema_DB thema = (Thema_DB) themas.get(res.getInt("index"));
		    if (thema.getFirstOccurrence() != null)
				res.updateInt("first_occurrence", thema.getFirstOccurrence().getDB_ID());
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
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
	
		    // Pruefe, ob schon ein Tupel mit der ID in der DB
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
				// ...und wird geloescht
				else if (isotope.isRemoved())
				    res.deleteRow();
		    }
		    // Tupel wird neu angelegt, wenn es nicht schon
		    // wieder in der Java-Datenstruktur geloescht wurde
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
		    // schreibe Aenderung in die DB
		    connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
		return isotopes;
    }

    /**
     * Erstellt eine Isotopie-Sammlung ueber alle in diesem Kapitel vorkommenden
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
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
		    res.updateString("hierachy", "bloedes Java will "
			    + "nicht aufs erste mal Objekte schreiben...");
		    res.insertRow();
		    doAgain = true;
		}
	
		stmt.close();
		connection.commit();
		connection.setAutoCommit(true);
	
		// Es ist nicht moeglich, Objects sofort in eine neue Zeile
		// zu schreiben, deswegen wird die Zeile neu angelegt und
		// die Zelle, die eigentlich das Object speichern soll,
		// wird mit Nonsens gefuellt, erst beim wiederholten Aufruf
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

    public Comments loadComments(Integer ChapterID, Integer ownerClassCode, Integer level)
    throws Exception {
		Comments c = new Comments();
		String subquery;
		
		switch (ownerClassCode.intValue()) {
			case Comments.CLASS_CODE_ILLOCUTION_UNIT :
			   subquery = "SELECT id FROM illocution_units WHERE chapter = "
				   + ChapterID
				   + " AND level = " + level;
			   break;
			case Comments.CLASS_CODE_DIRECT_SPEECH :
			   subquery = "SELECT id FROM direct_speeches WHERE chapter = "
				   + ChapterID
				   + " AND level = " + level;
			   break;
			case Comments.CLASS_CODE_DIALOG :
			   subquery = "SELECT id FROM dialogs WHERE chapter = " + ChapterID
			   		+ " AND level = " + level;
			   break;
			case Comments.CLASS_CODE_DIALOG_FOLLOWUP :
			   subquery = "SELECT id FROM dialogs WHERE chapter = " + ChapterID	
			   		+ " AND level = " + level;
			   break;
			case Comments.CLASS_CODE_DIALOG_RUNUP :
			   subquery = "SELECT id FROM dialogs WHERE chapter = " + ChapterID
			   		+ " AND level = " + level;
			   break;
			default :
			   subquery = "";
			break;
		}
		
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery("SELECT owner_id, owner_class_code, "
			+ "program, comment FROM comments_old "
			+ "WHERE owner_class_code = "
			+ ownerClassCode
			+ " and owner_id in ("
			+ subquery
			+ ")");
		
		while (res.next()) {
		   CommentKey ck = new CommentKey(key, res.getInt("owner_id"), 
				   res.getInt("owner_class_code"), res.getString("program"));
		   c.setComment(key, ck, res.getString("comment"));
		}
		
		return c;
    }

    public void saveComments(Comments comments, Integer level) throws Exception {
    	connection.setAutoCommit(false);
    	Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	ResultSet res;
    	Enumeration cks = comments.getKeys();

    	while (cks.hasMoreElements()) {
    		CommentKey ck = (CommentKey) cks.nextElement();
    		Comment c = comments.getComment(ck);

    		res = stmt.executeQuery("SELECT * FROM comments WHERE owner_id = "
    				+ ck.getOwnerID() + " and owner_class_code = "
    				+ ck.getOwnerClassCode() + " and program = '"
    				+ ck.getProgramID() + "'"
    				+ " AND level = " + level);

    		if (res.next()) {
    			if (c.isRemoved())
    				res.deleteRow();
    			else if (c.hasChanged()) {
    				res.updateString("comment", c.getComment());
    				res.updateRow();
    			}
    		} else {
    			if (c.isNew() || c.hasChanged()) {
    				res.moveToInsertRow();
    				res.updateInt("owner_id", ck.getOwnerID());
    				res.updateInt("owner_class_code", ck.getOwnerClassCode());
    				res.updateString("program", ck.getProgramID());
    				res.updateString("comment", c.getComment());
    				res.updateInt("level", level);	
    				res.insertRow();
    			}
    		}
    		connection.commit();
    	}

    	stmt.close();
    	connection.setAutoCommit(true);
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
		    if( res.next())
		    	max_id = res.getInt(1);
	
		    stmt_insert = connection.prepareStatement(
				    "INSERT INTO complexes (id, consitutive_word_id, chapter, member_type) VALUES (?, ?, ?, ?)");
		    stmt_delete = connection.prepareStatement("DELETE FROM complexes WHERE id = ?");
	
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
		    try {
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
		    catch (SQLException e) {
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
			    "SELECT * FROM complexes WHERE chapter = ? ORDER BY id");
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
		    try	{
			connection.setAutoCommit(true);
			if (res  != null)
			    res.close();
			if (stmt  != null)
			    stmt.close();
		    }
		    catch (SQLException e) {
			logger.warning(e.getLocalizedMessage());
		    }
		}
		return result;
    }

    public Renominalisations saveRenominalisations(Renominalisations renominalisations, Integer chapterID)
    throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		IllocutionUnitRoots iur = loadIllocutionUnitRoots(chapterID);
		renominalisations.setChapter(key, chapter, iur);
		Vector renoms = renominalisations.getAllRenominalisations(key);
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
	
		    // Pruefe, ob schon ein Tupel mit der ID in der DB
		    // gespeichert ist
		    res = stmt.executeQuery("SELECT * "
			    + "FROM renominalisations WHERE id = "
			    + renominalisation.getDB_ID());
	
		    // Tupel ist vorhanden...
		    if (res.next()) {
				// ...und wird aktualisiert
				if (renominalisation.hasChanged()) {
				    res.updateInt("category", categoryID);
				    res.updateInt("constitutive_word", renominalisation.getConstitutiveWord().getDB_ID());
				    res.updateInt("chapter", renominalisation.getChapter().getDB_ID());
				    res.updateRow();
				    renominalisation.resetState(key);
				}
				// ...und wird geloescht
				else if (renominalisation.isRemoved())
				    res.deleteRow();
		    }
		    // Tupel wird neu angelegt, wenn es nicht schon
		    // wieder in der Java-Datenstruktur geloescht wurde
		    else if (!renominalisation.isRemoved()) {
				res.moveToInsertRow();
				res.updateInt("category", categoryID);
				res.updateInt("constitutive_word", renominalisation.getConstitutiveWord().getDB_ID());
				res.updateInt("chapter", renominalisation.getChapter().getDB_ID());
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
		    // schreibe Aenderung in die DB
		    connection.commit();
		}
		connection.setAutoCommit(true);
		stmt.close();
		return renominalisations;
    }

    /**
     * Erstellt eine Renominalisation-Sammlung ueber alle in diesem Kapitel
     * vorkommenden Renominalisation.
     */
    public Renominalisations loadRenominalisations(Integer chapterID)
    throws Exception {
		Chapter chapter = getChapter(chapterID.intValue());
		Renominalisations renominalisations = new Renominalisations(chapter);
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery("SELECT renominalisations.id, renominalisations.constitutive_word,  "
			+ "renominalisation_categories.category "
			+ "FROM renominalisations, renominalisation_categories "
			+ "WHERE renominalisations.chapter = "
			+ chapter.getDB_ID()
			+ " and renominalisation_categories.id = renominalisations.category");
		IllocutionUnitRoots iur = loadIllocutionUnitRoots(chapterID);
		while (res.next()) {
		    renominalisations.setRenominalisation(key, res.getInt("id"), iur
			    .getConstitutiveWordWithID(res.getInt("constitutive_word")), res
			    .getString("category"));
		}
		stmt.close();
		return renominalisations;
    }

    public synchronized WordListElement[] loadWordListElement(String content) throws SQLException
    {
		//TODO: change to all possible languages
		return this.loadWordListElement(content, "DE");
    }

    public synchronized WordListElement[] loadWordListElement(String content, String language)
    throws SQLException {
		PreparedStatement stmt = null;
		ResultSet res = null;
		WordListElement[] elements = null;
	
		try {
		    // WLEs with assignation
		    stmt = connection.prepareStatement(
			    "SELECT word_list_elements.* " +
			    "FROM word_list_elements, words " +
			    "WHERE word_list_elements.word_id = words.id " +
			    "AND words.content = ? AND language = ?");
		    stmt.setBytes(1, content.getBytes("ISO-8859-1"));
		    stmt.setString(2, language);
	
		    res = stmt.executeQuery();
		    res.last();
		    int rows = res.getRow();
		    res.beforeFirst();
	
		    elements = new WordListElement[rows];
	
		    int i=0;
		    while( res.next() ) {
		    	elements[i] = new WordListElement(content, language);
				
		    	if(res.getInt("assignation_id") > 0 ) 
				    elements[i].setAssignation(loadAssignation(res.getInt("assignation_id")));
								
		    	elements[i].setDB_ID(key, res.getInt("id"));
				elements[i].resetState(key);
				i++;
		    }
		    stmt.close();
		}
		catch ( SQLException e ) {
		    logger.severe(e.getLocalizedMessage());
		    throw e;
		} catch (UnsupportedEncodingException e) {
		    logger.warning(e.getMessage());
		}
		finally {
		    try {
				if (res  != null)
				    res.close();
				if (stmt  != null)
				    stmt.close();
		    }
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return elements;
    }

    public WordListElement loadWordListElement(Integer id) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet res = null;
		WordListElement element = null;
		
		try {
		    stmt = connection.prepareStatement(
		    		"SELECT * FROM word_list_elements, words "
		    		+ "WHERE word_list_elements.id = ? "
		    		+ "AND word_list_elements.word_id = words.id");
		    stmt.setInt(1, id);
		    res = stmt.executeQuery();
		
		    if(res.next()) {
				try {
				    element = new WordListElement(new String(res.getBytes("content"), "ISO-8859-1"), res.getString("language"));
		    	}catch (UnsupportedEncodingException e) {
				    logger.warning(e.getMessage());
				}
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
		    try {
				if (res  != null)
				    res.close();
				if (stmt  != null)
				    stmt.close();
		    }
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return element;
	}

    /**
     * 
     * @param assigID
     * @return WordListElement[] oder null
     * @throws SQLException
     */
    public WordListElement loadWordListElementWithAssigID(Integer assigID) throws SQLException 
    {
    	PreparedStatement stmt = null;
    	ResultSet res = null;
    	WordListElement element = null;

    	//TODO: soll das WordListElement mit der Assignation ID assigID zur�ckgeben
    	try {
    		stmt = connection.prepareStatement(
    			"SELECT * FROM word_list_elements " +
    			"WHERE word_list_elements.assignation_id = ?");
    		stmt.setInt(1, assigID);
    		res = stmt.executeQuery();
    		
    		while (res.next()) {
    			// id laden
    			int id = res.getInt("id");
  
    			// assigantion laden
    			TR_Assignation assignation = loadAssignation(assigID);
     			
    			// zu word_id passenden content laden
    			int word_id = res.getInt("word_id");
    			stmt = connection.prepareStatement(
    					"SELECT content FROM words WHERE id = " + word_id);
        		res = stmt.executeQuery();
        		String content = null;
        		if (res.next()) 
        			content = res.getString("content");
        	     		
        		// wordListElement wird erstellt
        		element = new WordListElement(content);
    			element.setAssignation(assignation);
    			element.setDB_ID(key, id);
    			element.resetState(key);
    		}
    	}
    	catch ( SQLException e ) {
    		logger.throwing(this.getClass().getName(), "loadWordListElementWithAssigID", e);
    		throw e;
    	}
    	return element;
    }
    
    /**
     * Inserts, updates or removes <code>elements</code> data including their {@link TR_Assignation} and Word data in the Database dependent on their state.
     * @param elements
     * @throws SQLException
     */
    public synchronized WordListElement[] saveWordListElements(WordListElement ... elements) 
    throws SQLException {
		PreparedStatement stmt = null, stmt2 = null;
		ResultSet res = null, res2 = null;
		try {
		    // save all words and assignations
		    stmt = connection.prepareStatement(
			    "INSERT IGNORE INTO words " +
			    "(content, language) " +
			    "VALUES(?, ?)");
		    TR_Assignation[] assignations = new TR_Assignation[elements.length];
		    
		    for (int i = 0; i < elements.length; i++) {
				WordListElement wordListElement = elements[i];
				if(wordListElement == null) {
				    logger.info("wordListElement is null");
				    continue;
				}
				stmt.setBytes(1, wordListElement.getContent().getBytes("ISO-8859-1"));
				stmt.setString(2, wordListElement.getLanguage());	   
				stmt.addBatch();
				
				assignations[i] = wordListElement.getAssignation();
				if(assignations[i]==null)
				    logger.warning("WordListElement without assignation");
		    }			 
		    stmt.executeBatch();
		    saveAssignations(assignations);
	
		    // save WLEs
		    stmt = connection.prepareStatement(
			    "SELECT * FROM word_list_elements WHERE id = ?"
			    , ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	
		    for (WordListElement wordListElement : elements) {
				if(wordListElement == null) {
				    logger.info("wordListElement is null");
				    continue;
				}
				if(wordListElement.isUnchanged()) {
				    logger.finest("wordListElement unchanged");
				    continue;
				}
				boolean duplicate = false;
				WordListElement wle[] = loadWordListElement(wordListElement.getContent());
				// don't save the same entry several times
				for(int i = 0; i < wle.length; i++) {
					int a = wle[i].getAssignation().getDB_ID();
					int b = wordListElement.getAssignation().getDB_ID();
					String c = wle[i].getContent();
					String d =	wordListElement.getContent();			
					if(a == b && c == d)
						duplicate = true;
				}
				
				if(!duplicate) {
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
						    "WHERE content = ? AND language = ?");
					    stmt2.setBytes(1, wordListElement.getContent().getBytes("ISO-8859-1"));
					    stmt2.setString(2, wordListElement.getLanguage());
					    res2 = stmt2.executeQuery();
			
					    // if a word ID could be found
					    if( res2.next() ) {
							// write WLE to DB
							res.updateInt("word_id", res2.getInt(1));
							if(wordListElement.getAssignation() != null
								&& wordListElement.getAssignation().getDB_ID() != TR_Assignation.DEFAULT_ID)
							    	res.updateInt("assignation_id", wordListElement.getAssignation().getDB_ID());
				
							if(res.isFirst()) 
							    res.updateRow();
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
					else if(wordListElement.isRemoved() && res.next())
					    res.deleteRow();
			    }
		    }
		}
		catch ( SQLException e ) {
		    logger.severe(e.getLocalizedMessage());
		    throw e;
		} catch (UnsupportedEncodingException e) {
		    logger.warning(e.getMessage());
		}
		finally {
		    try {
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
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return elements;
	}
         
    /**
     * delete wle and the coressponding assignation and set the assignation in cw to null
     * @param chapterID
     * @throws Exception
     */
    public synchronized void deleteWLE(Integer wleID, Integer assigID)
    throws Exception {
		Statement stmt = connection.createStatement();
		// delete wle
		stmt.execute("DELETE FROM word_list_elements WHERE id = "+wleID);
		//	nicht einfach die assig l�schen, auch cw bzw. fw updaten
		stmt.execute("DELETE FROM assignations WHERE id = "+assigID);
		
		stmt.execute("UPDATE constitutive_words SET assignation_id = NULL WHERE assignation_id = " + assigID);
		stmt.execute("UPDATE function_words SET assignation_id = NULL WHERE assignation_id = " + assigID);
	}
    
/* wenn DELETEWLE funktioniert kann das raus
 *    public synchronized void deleteWLECW(Integer wleID, Integer assigID)//, Integer cwID)
    throws Exception {
		Statement stmt = connection.createStatement();
		// delete wle
		stmt.execute("DELETE FROM word_list_elements WHERE id = "+wleID);
		//	nicht einfach die assig l�schen, auch cw updaten
		stmt.execute("DELETE FROM assignations WHERE id = "+assigID);
		//stmt.execute("DELETE FROM constitutive_words WHERE id = "+cwID);
		stmt.execute("UPDATE constitutive_words SET assignation_id = NULL WHERE assignation_id = " + assigID);
	}*/
    /**
     * wenn DELETEWLE funktioniert kann das raus
     * delete wle and the coressponding assignation and set the assignation in fw to null
     * @param chapterID
     * @throws Exception
     */
/* wenn DELETEWLE funktioniert kann das raus   
 * public synchronized void deleteWLEFW(Integer wleID, Integer assigID)
    throws Exception {
		Statement stmt = connection.createStatement();
		// delete wle
		stmt.execute("DELETE FROM word_list_elements WHERE id = "+wleID);
		stmt.execute("DELETE FROM assignations WHERE id = "+assigID);
		stmt.execute("UPDATE function_words SET assignation_id = NULL WHERE assignation_id = " + assigID);
	}*/
    
    public Vector<String> loadWordsWithAbbreviation(String abbr) {
    	Vector<String> words = new Vector<String>();
    	if (abbr != null) {		
			try {
				PreparedStatement stmt = connection.prepareStatement(
						"SELECT content FROM words WHERE words.id IN" +
						"(SELECT word_id from word_list_elements where word_list_elements.assignation_id IN " +
						"(SELECT id FROM assignations WHERE abbreviation = ?));",
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				stmt.setString(1, abbr);
				ResultSet res = stmt.executeQuery();
	    		
	    		while (res.next()) 
	    			words.add(res.getString("content"));
	    		
	    		res.close();
	    		stmt.close();
	    	}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return words;
    }
    
    
   /* public Vector<String> loadWordsWithKonjugation(TR_Assignation.Konjugation konjug) {
    	Vector<String> words = new Vector<String>();
    	byte[] konj = new byte[0];
		if (konjug != null) 
			konj = TR_Assignation.setBit(konj, konjug.ordinal(), true);
        		
		try	{
			// wandelt den byte array in ein int, da es bei der 
			// setBytes Methode (s.u.) Probleme gab (byte wurde nicht als byte gesendet). 
			int konjAsInt = 0;
			for (int i = 0; i < konj.length; i++) {
				int n = (konj[i] < 0 ? (int)konj[i] + 256 : (int)konj[i]) << (8 * i);
				konjAsInt += n;
			}
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT content FROM words WHERE id IN " +
					"(SELECT word_id FROM word_list_elements WHERE assignation_id IN " +
					"(SELECT id FROM assignations WHERE tr_konjugation & ? ));",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			//stmt.setBytes(1, konj);
			stmt.setInt(1, konjAsInt);
			ResultSet res = stmt.executeQuery();
    		
    		while (res.next()) 
    			words.add(res.getString("content"));
    
    		res.close();
    		stmt.close();
    	}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return words;
    }*/

    public Vector<String> loadWordsWithPronoun(TR_Assignation.WordsubclassPronoun pron) {
    	Vector<String> words = new Vector<String>();
    	byte[] pro = new byte[0];
    	
		if (pron != null) 
			pro = TR_Assignation.setBit(pro, pron.ordinal(), true);
        
		// wandelt den byte array in ein int, da es bei der 
		// setBytes Methode (s.u.) Probleme gab (byte wurde nicht als byte gesendet). 
		int proAsInt = 0;
		for (int i = 0; i < pro.length; i++) {
			int n = (pro[i] < 0 ? (int)pro[i] + 256 : (int)pro[i]) << (8 * i);
			proAsInt += n;
		}
		
		try {
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT content FROM words WHERE words.id IN" +
					"(SELECT word_id from word_list_elements where word_list_elements.assignation_id IN " +
					"(SELECT id FROM assignations WHERE tr_subclass_pronoun & ?));",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			//stmt.setBytes(1, pro);
			stmt.setInt(1, proAsInt);
			ResultSet res = stmt.executeQuery();
    		
    		while (res.next()) 
    			words.add(res.getString("content"));
    		
    		res.close();
    		stmt.close();
    	}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return words;
    }

    public Vector<String> loadWordsWithWortArt1(TR_Assignation.Wortart1 wortArt) {
    	Vector<String> words = new Vector<String>();
    	byte[] wa1 = new byte[0];
    	
		if (wortArt != null)  	
    		wa1 = TR_Assignation.setBit(wa1, wortArt.ordinal(), true);

		// wandelt den byte array in ein int, da es bei der 
		// setBytes Methode (s.u.) Probleme gab (byte wurde nicht als byte gesendet). 
		int wortArt1AsInt = 0;
		for (int i = 0; i < wa1.length; i++) {
			int n = (wa1[i] < 0 ? (int)wa1[i] + 256 : (int)wa1[i]) << (8 * i);
			wortArt1AsInt += n;
		}
		try {
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT content FROM words WHERE words.id IN" +
					"(SELECT word_id from word_list_elements where word_list_elements.assignation_id IN " +
					"(SELECT id FROM assignations WHERE tr_wortart1 & ?));",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			//stmt.setBytes(1, wa1);
			stmt.setInt(1, wortArt1AsInt);
			ResultSet res = stmt.executeQuery();
    		
    		while (res.next()) 
    			words.add(res.getString("content"));
    		
    		res.close();
    		stmt.close();
    	}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return words;
    }
    
 	/**
     * Inserts, updates or removes <code>relations</code> in the Database dependent on their state.
     * @param relations
     * @throws SQLException
     * @throws DBC_SaveException
     */
    public synchronized Relation[] saveRelations(Relation ... relations)
    throws SQLException, DBC_SaveException {
		if(relations == null) 
		    throw new NullPointerException("");
	
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
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				stmt.setInt(1, relation.getDB_ID());
				res = stmt.executeQuery();
		
				if (relation.hasChanged()) {
				    if(! res.next()) 
				    	res.moveToInsertRow();
				    res.updateInt("origin", relation.getOrigin().getDB_ID());
				    res.updateInt("target", relation.getTarget().getDB_ID());
				    res.updateString("type", relation.getType().name());
		
				    if(res.isFirst())
				    	res.updateRow();
				    else {
						res.insertRow();
						res.last();
						relation.setDB_ID(key, res.getInt("id"));
				    }
				    relation.resetState(key);
				}
				else if(relation.isRemoved() && res.next())
				    res.deleteRow();
		    }
		    connection.commit();
		}
		catch ( SQLException e ) {
		    logger.severe(e.getLocalizedMessage());
		    throw e;
		}
		finally {
		    try {
				connection.setAutoCommit(true);
				if (res  != null)
				    res.close();
				if (stmt  != null)
				    stmt.close();
		    }
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return relations;
    }

    public Relation_DB[] loadRelations(WordListElement wordlistelement)
    throws SQLException {
		if(wordlistelement == null)
		    throw new NullPointerException("");
	
		if(wordlistelement.getDB_ID() == -1){
		    logger.info("WordListElement ist noch nicht gespeichert, daher k�nnen keine Relations geladen werden.");
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

    
    /**
     * TODO: KOMMENTAR BITTE von irgendwem!!
     * @param c
     * @param category
     * @return
     * @throws SQLException
     */
    public boolean isEdited(Chapter c, int category) throws SQLException {
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
     * Alle Eintr�ge der text_raw Tabelle werden geladen. 
     * Die Bedingungen k�nnen durch die Parameter angegeben werden.
     * @param strTitle
     * @param strId
     * @param strCreator
     * @param strLang
     * @param strDate
     * @return
     */
    public Vector<Vector<String>> loadText_Raw (String strTitle, String strId, String strCreator, String strLang, String strDate)
    {
	    String sqlStatement = "SELECT * FROM text_raw WHERE ";
	    
	    if (!strTitle.equals("")) {
	        if (sqlStatement.endsWith("WHERE "))
	            sqlStatement += "title LIKE '%" + strTitle + "%' ";
	        else
	            sqlStatement += "AND title LIKE '%" + strTitle + "%' ";
	    }
	    if (!strId.equals("")) {
	        if (sqlStatement.endsWith("WHERE "))
	            sqlStatement += "id LIKE '%" + strId + "%' ";
	        else
	            sqlStatement += "AND id LIKE '%" + strId + "%' ";
	    }
	    if (!strCreator.equals("")) {
	        if (sqlStatement.endsWith("WHERE "))
	            sqlStatement += "creator LIKE '%" +	strCreator + "%' ";
	        else
	            sqlStatement += "AND creator LIKE '%" +	strCreator + "%' ";
	    }
	    if (!strLang.equals("")) {
	        if (sqlStatement.endsWith("WHERE "))
	            sqlStatement += "language LIKE '%" + strLang + "%' ";
	        else
	            sqlStatement += "AND language LIKE '%" + strLang + "%' ";
	    }
	    if (!strDate.equals("")) {
	        if (sqlStatement.endsWith("WHERE "))
	            sqlStatement += "created LIKE '%" +	strDate + "%' ";
	        else
	            sqlStatement += "AND created LIKE '%" +	strDate + "%' ";
	    }
	    if (sqlStatement.endsWith("WHERE "))
	        sqlStatement = "SELECT * FROM text_raw";
	    
	    
	    ResultSet res = null;
	    Vector<Vector<String>> toChange = new Vector<Vector<String>>();
	   
	    try {
	    	PreparedStatement stmt = connection.prepareStatement(sqlStatement);
			res = stmt.executeQuery();    		
			
    		while (res.next()) {
    			String id = res.getString("id");
    			String title = res.getString("title");
    			String creator = res.getString("creator");
    			String language = res.getString("language");
    			Date created = res.getDate("created");
    			String uri = res.getString("uri");
    			
    			Vector<String> tmp = new Vector<String>();
    			if (id != null)
    				tmp.add(id);
    			else
    				tmp.add("");
    			
    			if (title != null)
    				tmp.add(title);
    			else
    				tmp.add("");
    			
    			if (creator != null)
    				tmp.add(creator);
    			else
    				tmp.add("");
    			
    			if (language != null)
    				tmp.add(language);
    			else
    				tmp.add("");
    			
    			if (created != null)
    				tmp.add(created.toString());
    			else
    				tmp.add("");
    			if (uri != null)
    				tmp.add(uri.toString());
    			else
    				tmp.add("");
    			
    			toChange.add(tmp);    			
    		}
    		res.close();
    	}
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    return toChange;
	   }
    
   
    /**
     * save all of <code>translations</code> whose state indicates an "out of sync with DB" status.
     * @param translations
     * @return <code>translations</code> with updated states and DB_IDs
     * @throws SQLException
     */
    public synchronized ArrayList<WorkingTranslation_DB> saveWorkingTranslations(
			ArrayList<WorkingTranslation_DB> translations) throws SQLException {
		
    	connection.setAutoCommit(false);
		PreparedStatement stmt_insert = null, stmt_delete = null, stmt_max_id = null, stmt_count = null;
		ResultSet res = null;
		int max_id = 0;

		try {			
			stmt_count = connection.prepareStatement(
				"SELECT count(*) FROM working_translation " 
				+ "WHERE language = ? AND translation = ?");
			stmt_insert = connection.prepareStatement(
				"INSERT INTO working_translation (id, language, original, translation)"
				+ "VALUES (null, ?, ?, ?)");
			stmt_delete = connection.prepareStatement(
					"DELETE FROM working_translation WHERE id = ?");

			for (WorkingTranslation_DB translation_DB : translations) {

				if (translation_DB.getStateAsInt() == WorkingTranslation_DB.NEW)
					translation_DB.setDB_ID(key, ++max_id);
				
				// remove existing entries
				if (translation_DB.getStateAsInt() == WorkingTranslation_DB.REMOVE
						|| translation_DB.getStateAsInt() == WorkingTranslation_DB.CHANGE) {
					stmt_delete.setInt(1, translation_DB.getDB_ID());
					stmt_delete.addBatch();
				}
				if (translation_DB.getStateAsInt() == WorkingTranslation_DB.NEW
						|| translation_DB.getStateAsInt() == WorkingTranslation_DB.CHANGE) {
					
					// check if translation exists
					stmt_count.setString(1, translation_DB.getLanguage());
					stmt_count.setString(2, translation_DB.getTranslation());
					res = stmt_count.executeQuery();
					res.next();
					int count = res.getInt(1);
					if (count > 0)
						continue;
					
					// add noun
					stmt_insert.setString(1, translation_DB.getLanguage());
					stmt_insert.setString(2, translation_DB.getOrginal());
					stmt_insert.setString(3, translation_DB.getTranslation());
					stmt_insert.addBatch();

					translation_DB.resetState(key);
				}
			}
			stmt_delete.executeBatch();
			stmt_insert.executeBatch();
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			logger.severe(e.getLocalizedMessage());
			throw e;
		} finally {
			try {
				connection.setAutoCommit(true);
				if (res != null)
					res.close();
				if (stmt_insert != null)
					stmt_insert.close();
				if (stmt_delete != null)
					stmt_delete.close();
				if (stmt_max_id != null)
					stmt_max_id.close();
			} catch (SQLException e) {
				logger.warning(e.getLocalizedMessage());
			}
		}
		return translations;
	}
    
    /**
     * TODO: Vern�nftigen Kommentar schreiben :)
     * @throws Exception
     */
	public Vector<WorkingTranslation_DB> loadWorkingTranslations(String pLg, String pOriginal)
	throws Exception {
		Vector<WorkingTranslation_DB> result = new Vector<WorkingTranslation_DB>();
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			stmt = connection.prepareStatement(
					"SELECT * FROM working_translation WHERE language = ? " 
					+ "AND original = ? ORDER BY translation ASC");
			stmt.setString(1, pLg);
			stmt.setString(2, pOriginal);
			res = stmt.executeQuery();
			WorkingTranslation_DB complex = null;
			while (res.next()) {
				complex = new WorkingTranslation(key).new WorkingTranslation_DB(key);
				complex.setDB_ID(key, res.getInt("id"));
				complex.setLanguage(res.getString("language"));
				complex.setOrginal(res.getString("original"));
				complex.setTranslation(res.getString("translation"));
				complex.resetState(key);
				result.add(complex);
			}
		} catch (SQLException e) {
			logger.severe(e.getLocalizedMessage());
			throw e;
		} finally {
			try {
				connection.setAutoCommit(true);
				if (res != null)
					res.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				logger.warning(e.getLocalizedMessage());
			}
		}
		return result;
	}

	public Vector<String> loadWorkingTranslationsLanguage() throws Exception {

		Vector<String> result = new Vector<String>();
        Statement stmt = null;
        ResultSet res = null;
        try {
            stmt = connection.createStatement();
            res = stmt.executeQuery("SELECT language FROM working_translation" 
            		+ " ORDER BY language DESC");
           
            while (res.next())
                result.add(res.getString("language"));
           
        } catch (SQLException e) {
            logger.severe(e.getLocalizedMessage());
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
                if (res != null)
                    res.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                logger.warning(e.getLocalizedMessage());
            }
        }
        return result;
    }

	/**
     * save all of <code>criticisms</code> whose state indicates an "out of sync with DB" status.
     * @param criticisms
     * @return <code>criticisms</code> with updated states and DB_IDs
     * @throws SQLException
     */
    public synchronized ArrayList<LiteraryCriticism1_DB> saveLiteraryCriticism1( ArrayList<LiteraryCriticism1_DB> criticisms) 
    throws SQLException {
		connection.setAutoCommit(false);
		PreparedStatement stmt_insert = null, stmt_delete = null, stmt_max_id = null;
		ResultSet res = null;
		int max_id = 0;
	
		try {
		    // compute free id value for insertion
		    stmt_max_id = connection.prepareStatement("SELECT MAX(id) FROM literary_criticism_1");
		    res = stmt_max_id.executeQuery();
		    connection.commit();
		    if( res.next())
		    	max_id = res.getInt(1);
		    
		    stmt_insert = connection.prepareStatement(
			    "INSERT INTO literary_criticism_1 "
		    	+ "(id, chapter_id, char_pos1_start, char_pos1_end, char_pos2_start, char_pos2_end, annotation)"
		    	+ " VALUES (?, ?, ?, ?, ?, ?, ?)");
		    stmt_delete = connection.prepareStatement("DELETE FROM literary_criticism_1 WHERE id = ?");
	
		    for (LiteraryCriticism1_DB criticism_DB : criticisms) {
				if(criticism_DB.getStateAsInt() == LiteraryCriticism1_DB.NEW) 
				    criticism_DB.setDB_ID(key, ++max_id);
				
				// remove existing entries
				if(criticism_DB.getStateAsInt() == LiteraryCriticism1_DB.REMOVE
					|| criticism_DB.getStateAsInt() == LiteraryCriticism1_DB.CHANGE) {
				    stmt_delete.setInt(1, criticism_DB.getDB_ID());
				    stmt_delete.addBatch();
				}
				
				if(criticism_DB.getStateAsInt() == LiteraryCriticism1_DB.NEW
					|| criticism_DB.getStateAsInt() == LiteraryCriticism1_DB.CHANGE) {
				    // add noun
				    stmt_insert.setInt(1, criticism_DB.getDB_ID());
				    stmt_insert.setInt(2, criticism_DB.chapterID);
				    stmt_insert.setInt(3, criticism_DB.getPos1_start());
				    stmt_insert.setInt(4, criticism_DB.getPos1_end());
				    stmt_insert.setInt(5, criticism_DB.getPos2_start());
				    stmt_insert.setInt(6, criticism_DB.getPos2_end());
				    stmt_insert.setString(7, criticism_DB.getAnnotation());
				    stmt_insert.addBatch();
		
				    criticism_DB.resetState(key);
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
		    try {
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
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return criticisms;
    }
    
  
    /**
     * Load all complexes in chapter with ID <code>chapterID</code>
     * @param chapterID
     * @return a Vector of PronounComplex_DB. All elements are expected to get converted into PronounComplex on the client side.
     * @throws Exception
     */
    public Vector<LiteraryCriticism1_DB> loadLiteraryCriticism1(Integer chapterID) 
    throws Exception {
		Vector<LiteraryCriticism1_DB> result = new Vector<LiteraryCriticism1_DB>();
		PreparedStatement stmt = null;
		ResultSet res = null;
	
		try {
		    stmt = connection.prepareStatement(
			    "SELECT * FROM literary_criticism_1 WHERE chapter_id = ? ORDER BY id");
		    stmt.setInt(1, chapterID);
		    res = stmt.executeQuery();
		    LiteraryCriticism1_DB criticism = null;
		    while(res.next()) {
				criticism = new LiteraryCriticism1(key).new LiteraryCriticism1_DB(key);
				criticism.setDB_ID(key, res.getInt("id"));
				criticism.chapterID = chapterID;
				criticism.setPos1_start(res.getInt("char_pos1_start"));
				criticism.setPos1_end(res.getInt("char_pos1_end"));
				criticism.setPos2_start(res.getInt("char_pos2_start"));
				criticism.setPos2_end(res.getInt("char_pos2_end"));
				criticism.setAnnotation(res.getString("annotation"));
				criticism.resetState(key);
				result.add(criticism);
		    }
		}
		catch ( SQLException e ) {
		    logger.severe(e.getLocalizedMessage());
		    throw e;
		}
		finally {
		    try {
				connection.setAutoCommit(true);
				if (res  != null)
				    res.close();
				if (stmt  != null)
				    stmt.close();
		    }
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return result;
    }
    
   
    /**
     * save all of <code>criticisms</code> whose state indicates an "out of sync with DB" status.
     * @param criticisms
     * @return <code>criticisms</code> with updated states and DB_IDs
     * @throws SQLException
     */
    public synchronized ArrayList<LiteraryCriticism2_DB> saveLiteraryCriticism2( ArrayList<LiteraryCriticism2_DB> criticisms) 
    throws SQLException {
		connection.setAutoCommit(false);
		PreparedStatement stmt_insert = null, stmt_delete = null, stmt_max_id = null;
		ResultSet res = null;
		int max_id = 0;
	
		try {
		    // compute free id value for insertion
		    stmt_max_id = connection.prepareStatement("SELECT MAX(id) FROM literary_criticism_2");
		    res = stmt_max_id.executeQuery();
		    connection.commit();
		    if( res.next()) 
		    	max_id = res.getInt(1);
		    
		    stmt_insert = connection.prepareStatement(
			    "INSERT INTO literary_criticism_2 "
		    	+ "(id, chapter_id, char_pos_start, char_pos_end, type, annotation1, annotation2)"
		    	+ " VALUES (?, ?, ?, ?, ?, ?, ?)"
		    );
		    stmt_delete = connection.prepareStatement("DELETE FROM literary_criticism_2 WHERE id = ?");
	
		    for (LiteraryCriticism2_DB criticism_DB : criticisms) {
				if(criticism_DB.getStateAsInt() == LiteraryCriticism2_DB.NEW)
				    criticism_DB.setDB_ID(key, ++max_id);
	
				// remove existing entries
				if(criticism_DB.getStateAsInt() == LiteraryCriticism2_DB.REMOVE
						|| criticism_DB.getStateAsInt() == LiteraryCriticism2_DB.CHANGE) {
				    stmt_delete.setInt(1, criticism_DB.getDB_ID());
				    stmt_delete.addBatch();
				}
				
				if(criticism_DB.getStateAsInt() == LiteraryCriticism2_DB.NEW
						|| criticism_DB.getStateAsInt() == LiteraryCriticism2_DB.CHANGE) {
					// add noun
				    stmt_insert.setInt(1, criticism_DB.getDB_ID());
				    stmt_insert.setInt(2, criticism_DB.chapterID);
				    stmt_insert.setInt(3, criticism_DB.getPos_start());
				    stmt_insert.setInt(4, criticism_DB.getPos_end());
				    stmt_insert.setInt(5, criticism_DB.getType());
				    stmt_insert.setString(6, criticism_DB.getAnnotation1());
				    stmt_insert.setString(7, criticism_DB.getAnnotation2());
				    stmt_insert.addBatch();
		
				    criticism_DB.resetState(key);
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
		    try {
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
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return criticisms;
    }
    
  
    /**
     * Load all complexes in chapter with ID <code>chapterID</code>
     * @param chapterID
     * @return a Vector of PronounComplex_DB. All elements are expected to get converted into PronounComplex on the client side.
     * @throws Exception
     */
    public Vector<LiteraryCriticism2_DB> loadLiteraryCriticism2(Integer chapterID)
    throws Exception {
		Vector<LiteraryCriticism2_DB> result = new Vector<LiteraryCriticism2_DB>();
		PreparedStatement stmt = null;
		ResultSet res = null;
	
		try {
		    stmt = connection.prepareStatement(
			    "SELECT * FROM literary_criticism_2 WHERE chapter_id = ? ORDER BY id");
		    stmt.setInt(1, chapterID);
		    res = stmt.executeQuery();
		    LiteraryCriticism2_DB criticism = null;

		    while(res.next()) {
				criticism = new LiteraryCriticism2(key).new LiteraryCriticism2_DB(key);
				criticism.setDB_ID(key, res.getInt("id"));
				criticism.chapterID = chapterID;
				criticism.setPos_start(res.getInt("char_pos_start"));
				criticism.setPos_end(res.getInt("char_pos_end"));
				criticism.setType(res.getInt("type"));
				criticism.setAnnotation1(res.getString("annotation1"));
				criticism.setAnnotation2(res.getString("annotation2"));
				criticism.resetState(key);
				result.add(criticism);
		    }
		}
		catch ( SQLException e ) {
		    logger.severe(e.getLocalizedMessage());
		    throw e;
		}
		finally {
		    try {
				connection.setAutoCommit(true);
				if (res  != null)
				    res.close();
				if (stmt  != null)
				    stmt.close();
		    }
		    catch (SQLException e) {
		    	logger.warning(e.getLocalizedMessage());
		    }
		}
		return result;
    }
    
   
    /**
     * ==========================================
     * =============== IU_Comment ===============
     * ========================================== 
     */
    public synchronized ArrayList<IU_Comment> loadIUComments (Integer chapterID)
    throws Exception {
    	Chapter chapter = getChapter(chapterID.intValue());
    	ArrayList<IU_Comment> comments = new ArrayList<IU_Comment>();
    	Statement stmt = connection.createStatement();
    	ResultSet res;

    	PreparedStatement preStmt = connection.prepareStatement(
    		"SELECT * FROM iu_comments WHERE chapter = " + chapter.getDB_ID());
    	res = preStmt.executeQuery();

    	while ( res.next() ) {
    		IU_Comment comment = new IU_Comment(res.getInt("ID"), res.getInt("iu"), res.getString("comment"), chapter.getDB_ID(), res.getString("author"), res.getTimestamp("last_update"));
    		comments.add(comment);
    	}
    	res.close();
    	stmt.close();
    	return comments;
    }

    public synchronized void saveIUComment(IU_Comment comment) throws Exception {
    	connection.setAutoCommit(false);
    	Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	stmt.executeUpdate(
    		"INSERT INTO iu_comments (iu, chapter, comment, author) "
    		+ "VALUES('" + comment.IU_ID + "', '" + comment.chapter + "', '" 
    		+ comment.text + "', '" + comment.author +"')");
    	stmt.close();
    	connection.commit();
    	connection.setAutoCommit(true);
    }

    public synchronized void deleteIUComment(Integer IU_ID) throws Exception {
    	connection.setAutoCommit(false);
    	Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	stmt.executeUpdate("DELETE FROM iu_comments WHERE ID=" + IU_ID.toString());
    	stmt.close();
    	connection.commit();
    	connection.setAutoCommit(true);
    }

    public synchronized void editIUComment(Integer IU_ID, String text) throws Exception {
    	connection.setAutoCommit(false);
    	Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	stmt.executeUpdate("UPDATE iu_comments SET comment='" + text + "' WHERE ID=" + IU_ID.toString());
    	stmt.close();
    	connection.commit();
    	connection.setAutoCommit(true);
    }

  
    /**
     * Das gleiche wie die Wortlisten, blo�, dass die Ausgabe in der Klasse
     * LonleyConstitutiveWord gekapselt ist.
     * 
     * @param content
     * @param language
     * @deprecated Ohne Funktion! Wortliste ist nun �ber WordListElements erreichbar.
     */
    public void saveWordList(Vector list) throws Exception
    {
	return; // SQL Statements sind ung�ltig

//	connection.setAutoCommit(false);
//	Statement stmt = connection
//	.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
//	ResultSet.CONCUR_UPDATABLE);

//	ResultSet res;

//	for (int i = 0; i < list.size(); i++) {
//	DB_Tupel tupel = (DB_Tupel) list.get(i);

//	// Tupel soll geloescht werden
//	if (tupel.getState() == DB_Tupel.DELETE) {
//	res = stmt.executeQuery("SELECT * FROM word_list WHERE id = "
//	+ tupel.getInt("id"));
//	if (res.next()) {
//	// logger.info("loesche "+ tupel);
//	res.deleteRow();
//	connection.commit();
//	}
//	}

//	// Tupel wird in der DB neu angelegt oder ge�ndert
//	else {
//	// F�ge erst eventuell das Wort in die Wort-DB ein


//	int rowCount = stmt.executeUpdate(String.format(

//	"INSERT IGNORE INTO words " +
//	"(content, language, cont_lang_checksum) " +
//	"VALUES('%1$s', '%2$s', UNHEX(MD5(CONCAT('%1$s', '%2$s'))))",

//	tupel.getString("content"), tupel.getString("language")));

//	res = stmt.executeQuery(String.format(

//	"SELECT id, language, content " +
//	"FROM words " +
//	"AND words.cont_lang_checksum = UNHEX(MD5(CONCAT('%1$s', '%2$s')))",

//	mask(tupel.getString("content")), tupel.getString("language")));

//	// hole die Wort-ID aud der Wort-DB
//	int wordID = res.getInt("id");
//	res.close();
//	// logger.info("Wort-ID: "+ wordID);

//	// Tupel soll ge�ndert werden
//	if (tupel.getState() == DB_Tupel.CHANGE) {
//	res = stmt.executeQuery("SELECT * FROM word_list WHERE id = "
//	+ tupel.getInt("id"));
//	if (res.next()) {
//	// logger.info("�ndere "+ tupel);
//	res.updateInt("word", wordID);

//	if (tupel.containsKey("tr_genus"))
//	res.updateByte("tr_genus", tupel.getByte("tr_genus"));

//	if (tupel.containsKey("tr_numerus"))
//	res.updateByte("tr_numerus", tupel.getByte("tr_numerus"));

//	if (tupel.containsKey("tr_case"))
//	res.updateByte("tr_case", tupel.getByte("tr_case"));

//	if (tupel.containsKey("tr_determination"))
//	res.updateByte("tr_determination", tupel
//	.getByte("tr_determination"));

//	if (tupel.containsKey("tr_person"))
//	res.updateByte("tr_person", tupel.getByte("tr_person"));

//	if (tupel.containsKey("tr_wordclass"))
//	res.updateByte("tr_wordclass", tupel
//	.getByte("tr_wordclass"));

//	if (tupel.containsKey("tr_wordsubclass"))
//	res.updateByte("tr_wordsubclass", tupel
//	.getByte("tr_wordsubclass"));

//	if (tupel.containsKey("tr_conjugation"))
//	res.updateByte("tr_conjugation", tupel
//	.getByte("tr_conjugation"));

//	if (tupel.containsKey("tr_pronoun"))
//	res.updateByte("tr_pronoun", tupel.getByte("tr_pronoun"));

//	if (tupel.containsKey("tr_tempus"))
//	res.updateByte("tr_tempus", tupel.getByte("tr_tempus"));

//	if (tupel.containsKey("tr_diathese"))
//	res.updateByte("tr_diathese", tupel
//	.getByte("tr_diathese"));

//	if (tupel.containsKey("type"))
//	res.updateByte("type", tupel.getByte("type"));

//	if (tupel.containsKey("multiple"))
//	res.updateInt("multiple", tupel.getInt("multiple"));
//	res.updateRow();
//	connection.commit();
//	}
//	else {
//	// logger.info(tupel +" konnte nicht ge�ndert
//	// werden.\n" +
//	// "Lege es neu an...");
//	tupel.setState(DB_Tupel.SAVE);
//	i--;
//	continue;
//	}
//	}

//	// Tupel wird neu angelegt
//	else {
//	String query = "SELECT * FROM word_list WHERE word = " + wordID;

//	if (tupel.containsKey("tr_genus"))
//	query += " and tr_genus = " + tupel.getByte("tr_genus");

//	if (tupel.containsKey("tr_numerus"))
//	query += " and tr_numerus = " + tupel.getByte("tr_numerus");

//	if (tupel.containsKey("tr_case"))
//	query += " and tr_case = " + tupel.getByte("tr_case");

//	if (tupel.containsKey("tr_determination"))
//	query += " and tr_determination = "
//	+ tupel.getByte("tr_determination");

//	if (tupel.containsKey("tr_person"))
//	query += " and tr_person = " + tupel.getByte("tr_person");

//	if (tupel.containsKey("tr_wordclass"))
//	query += " and tr_wordclass = "
//	+ tupel.getByte("tr_wordclass");

//	if (tupel.containsKey("tr_wordsubclass"))
//	query += " and tr_wordsubclass = "
//	+ tupel.getByte("tr_wordsubclass");

//	if (tupel.containsKey("tr_conjugation"))
//	query += " and tr_conjugation = "
//	+ tupel.getByte("tr_conjugation");

//	if (tupel.containsKey("tr_pronoun"))
//	query += " and tr_pronoun = " + tupel.getByte("tr_pronoun");

//	if (tupel.containsKey("tr_tempus"))
//	query += " and tr_tempus = " + tupel.getByte("tr_tempus");

//	if (tupel.containsKey("tr_diathese"))
//	query += " and tr_diathese = " + tupel.getByte("tr_diathese");

//	if (tupel.containsKey("type"))
//	query += " and type = " + tupel.getByte("type");

//	res = stmt.executeQuery(query);

//	// Tupel in der Wortliste noch nicht vorhanden,
//	// wird also neu angelegt
//	if (!res.next()) {
//	// logger.info("speichere "+ tupel);
//	res.moveToInsertRow();
//	res.updateInt("word", wordID);

//	if (tupel.containsKey("tr_genus"))
//	res.updateByte("tr_genus", tupel.getByte("tr_genus"));

//	if (tupel.containsKey("tr_numerus"))
//	res.updateByte("tr_numerus", tupel.getByte("tr_numerus"));

//	if (tupel.containsKey("tr_case"))
//	res.updateByte("tr_case", tupel.getByte("tr_case"));

//	if (tupel.containsKey("tr_determination"))
//	res.updateByte("tr_determination", tupel
//	.getByte("tr_determination"));

//	if (tupel.containsKey("tr_person"))
//	res.updateByte("tr_person", tupel.getByte("tr_person"));

//	if (tupel.containsKey("tr_wordclass"))
//	res.updateByte("tr_wordclass", tupel
//	.getByte("tr_wordclass"));

//	if (tupel.containsKey("tr_wordsubclass"))
//	res.updateByte("tr_wordsubclass", tupel
//	.getByte("tr_wordsubclass"));

//	if (tupel.containsKey("tr_conjugation"))
//	res.updateByte("tr_conjugation", tupel
//	.getByte("tr_conjugation"));

//	if (tupel.containsKey("tr_pronoun"))
//	res.updateByte("tr_pronoun", tupel.getByte("tr_pronoun"));

//	if (tupel.containsKey("tr_tempus"))
//	res.updateByte("tr_tempus", tupel.getByte("tr_tempus"));

//	if (tupel.containsKey("tr_diathese"))
//	res
//	.updateByte("tr_diathese", tupel
//	.getByte("tr_diathese"));

//	if (tupel.containsKey("type"))
//	res.updateByte("type", tupel.getByte("type"));

//	if (tupel.containsKey("multiple"))
//	res.updateInt("multiple", tupel.getInt("multiple"));

//	res.insertRow();
//	connection.commit();
//	}
//	else {
//	// logger.info(tupel +" schon vorhanden");
//	}
//	}
//	}
//	}
//	stmt.close();
//	connection.setAutoCommit(true);
    }
    
  
    /**
     * Bereitet den �bergebenen String zur Verwendung in einer mySQL Abfrage vor, indem jedes Vorkommen von `'' mit `\'' ersetzt.
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


//gibt f�r jede Assigantion eines Strings die Wortklasse und Subklasse zur�ck
//public Vector loadWordClasses(Vector contents) throws Exception {
//Vector<Vector> resultSet = new Vector<Vector>();
//for(int i=0; i != contents.size(); ++i)
//{
//String content = (String)contents.get(i);
//Vector assignations = loadWordListElement(content).getAssignations();
//Vector<Long> wordClasses = new Vector<Long>();
//Vector<Long> wordSubClasses = new Vector<Long>();
//for (int j=0; j != assignations.size(); ++j)
//{
//TR_Assignation assi = (TR_Assignation)assignations.get(j); 
//wordClasses.add(assi.getWordclassesBinary());
//if (assi.getWordsubclassAdjectivesBinary()        != 0)
//wordSubClasses.add(assi.getWordsubclassAdjectivesBinary());
//else if (assi.getWordsubclassConnectorsBinary()   != 0)
//wordSubClasses.add(assi.getWordsubclassConnectorsBinary());
//else if (assi.getWordsubclassPrepositionsBinary() != 0)
//wordSubClasses.add(assi.getWordsubclassPrepositionsBinary());
//else if (assi.getWordsubclassPronounsBinary()     != 0)
//wordSubClasses.add(assi.getWordsubclassPronounsBinary());
//else if (assi.getWordsubclassPunctuationMarksBinary()        != 0)
//wordSubClasses.add(assi.getWordsubclassPunctuationMarksBinary());
//else if (assi.getWordsubclassVerbsBinary()        != 0)
//wordSubClasses.add(assi.getWordsubclassVerbsBinary());
//else 
//wordSubClasses.add(new Long(0));
//}
//resultSet.add(wordClasses);
//resultSet.add(wordSubClasses);
//}
//return resultSet;	  
//}


/**
 * @param assignation
 * @return the WordListElement characterized by the assignation, <code>null</code> if none exists
 * @throws SQLException
 * @throws NullPointerException if the assignation is <code>null</code>
 */
//public synchronized WordListElement loadWordListElement(TR_Assignation assignation) throws SQLException, NullPointerException
//{	
//if(assignation == null) {
//throw new NullPointerException("");
//}
//if(assignation.getDB_ID() == -1) {
//return null;
//}

//WordListElement element = null;
//PreparedStatement stmt = null;
//ResultSet res = null;

//try {	
//stmt = connection.prepareStatement(

//"SELECT words.content " +
//"FROM word_list_elements, words " +
//"WHERE assignation_id = ? " +
//"AND words.id = assignation_id.word_id");

//stmt.setInt(1, assignation.getDB_ID());

//res = stmt.executeQuery();

//if( res.next() ) {
//element = loadWordListElement(res.getString(1));
//}
//}
//catch ( SQLException e ) {
//logger.severe(e.getLocalizedMessage());
//throw e;
//}
//finally {
//try
//{
//if (res  != null)
//res.close();
//if (stmt  != null)
//stmt.close();
//}
//catch (SQLException e)
//{
//logger.warning(e.getLocalizedMessage());
//}
//}

//return element;
//}


///**
//* 
//* @param assigID
//* @return WordListElement[] oder null
//* @throws SQLException
//*/
/*    public WordListElement[] loadWordListElementWithAssigID(Integer assigID) throws SQLException 
{
	PreparedStatement stmt = null;
	ResultSet res = null;
	WordListElement[] elements = null;

	//TODO: soll das WordListElement mit der Assignation ID assigID zur�ckgeben
	try 
	{
		stmt = connection.prepareStatement("SELECT * FROM word_list_elements WHERE word_list_elements.assignation_id = " + assigID);
		res = stmt.executeQuery();
		
		while (res.next()) 
		{
			// zu word_id passenden content laden
			int word_id = res.getInt("word_id");
			stmt = connection.prepareStatement("SELECT content FROM words WHERE id = " + word_id);
  		res = stmt.executeQuery();
  		String content = null;
  		while (res.next()) {
  			content = res.getString("content");
  		}
			
  		if (content != null) {
  			elements = loadWordListElement(content);
			}
		}
	}
	catch ( SQLException e ) {
		e.printStackTrace();
	}
	return elements;
}
*/