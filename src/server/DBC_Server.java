/*
 * Erstellt: 23.10.2004
 */

package server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import pathselector.PathNode;
import connection.DBC_ConnectionException;
import connection.DBC_Key;
import connection.DBC_SaveException;
import data.*;

public class DBC_Server extends Thread {

   static DBC_Key     key;
   private DBC_Cache  chapterCache;
   private PathNode   root;
   private Connection connection;
   private String     server;
   private String     user;
   private String     password;
   private int        counter;
   private int        keepAlive;

   DBC_Server(String server, String user, String password, int keepAlive)
         throws DBC_ConnectionException {
      this.server = server;
      this.user = user;
      this.password = password;
      this.keepAlive = keepAlive;
      counter = keepAlive;

      key = DBC_Key.makeKey(this);
      chapterCache = new DBC_Cache(5);
      open();
      root = loadPaths();
   }

   DBC_Server(String server, int keepAlive) throws DBC_ConnectionException {
      this(server, "slang2", "kauderwelsch", keepAlive);
   }

   /**
    * Beendet die Verbindung zu dem Datenbank-Server
    * 
    */
   void close() {
      if (connection != null) {
         try {
            connection.close();
            connection = null;
            System.out.println("DB zu");
         }
         catch (SQLException e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * ï¿½ffnet die Verbindung zur Datenbank. Wird vom Konstruktor aufgerufen. Auch
    * nï¿½tzlich, wenn die Verbindung mit close() beendet wurde und nun wieder
    * hergestellt werden soll.
    * 
    * @throws DBC_ConnectionException
    *         Falls kein Verbindungsaufbau mï¿½glich ist.
    * 
    * @see #close()
    */
   void open()
         throws DBC_ConnectionException {
      if (connection == null) {
         try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://"
                  + server
                  + ":3306/slang2_test?useUnicode=true&" //":3306/slang2_test?useUnicode=true&"
                  + "characterEncoding=ISO-8859-1", user, password);
            System.out.println("DB auf");
         }
         catch (Exception e) {
            throw new DBC_ConnectionException(
                  "Keine Verbindung zur Datenbank\n" + e.toString());
         }
      }
   }

   public void run() {
      while (true) {
         if (counter == 0)
            close();
         else
            counter--;
         try {
            Thread.sleep(1000);
         }
         catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   public synchronized void resetCounter()
         throws DBC_ConnectionException {
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
      ResultSet res = stmt.executeQuery("select * from books");

      while (res.next()) {
         books.add(new Book(key, res.getInt("id"), res.getString("title"), res
               .getString("author"), res.getInt("year")));
      }

      for (int i = 0; i < books.size(); i++) {
         Book book = (Book) books.get(i);
         res = stmt.executeQuery("select * from chapters where book = "
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
      ResultSet res = stmt.executeQuery("select * "
            + "from books where id = "
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

      ResultSet res = stmt.executeQuery("select * from books "
            + "where title like binary '"
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

      res = stmt.executeQuery("select * from books "
            + "where title like binary '"
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

   private Chapter loadChapter(int chapterID)
         throws Exception {
      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery("select * "
            + "from chapters where id = "
            + chapterID);

      if (res.next()) {
         // Kapiteldaten
    	 Timestamp ts = res.getTimestamp("date");
    	 if(ts == null){ //noch keine Zeit gesetzt, wird neu initialisiert
    		 ts = new Timestamp(0);
    	 }
     	 ts.setNanos(0);
         Chapter chapter = new Chapter(key, res.getInt("id"), res
               .getInt("book"), res.getInt("index"), res.getString("title"), ts.toString().substring(0,ts.toString().length()-2));

         // Wï¿½rter des Kapitels
         res = stmt.executeQuery("select words.id as id, "
               + "words_in_chapter.position, "
               + "words.content, words.language "
               + "from words_in_chapter, words "
               + "where words_in_chapter.chapter = "
               + chapter.getDB_ID()
               + " and words_in_chapter.word = words.id "
               + "order by words_in_chapter.position");
         while (res.next())
            chapter.addWord(key,
                  res.getInt("id"),
                  res.getString("content"),
                  res.getString("language"),
                  res.getInt("position"));

         // Satzzeichen des Kapitels
         res = stmt.executeQuery("select signs.id as id, "
               + "signs_in_chapter.position, "
               + "signs.sign "
               + "from signs_in_chapter, signs "
               + "where signs_in_chapter.chapter = "
               + chapter.getDB_ID()
               + " and signs_in_chapter.sign = signs.id "
               + "order by signs_in_chapter.position");
         while (res.next())
            chapter.addSign(key, res.getInt("id"), res.getString("sign")
                  .charAt(0), res.getInt("position"));

         // Absï¿½tze des Kapitels
         res = stmt.executeQuery("select position "
               + "from paragraphs_in_chapter "
               + "where chapter = "
               + chapter.getDB_ID()
               + " order by position");
         while (res.next())
            chapter.addNewline(key, res.getInt("position"));

         // ï¿½uï¿½erungseinheiten des Kapitels
         res = stmt.executeQuery("select * from illocution_units "
               + "where chapter = "
               + chapter.getDB_ID());
         while (res.next())
            chapter.addIllocutionUnit(key, res.getInt("id"), res
                  .getInt("start"), res.getInt("end"));

         chapter.calculateIndicies(key);
         stmt.close();

         setChapter(chapter);
         return chapter;
      }

      stmt.close();
      return null;
   }

   public synchronized Chapter loadChapter(Integer chapterID)
         throws Exception {
      return getChapter(chapterID.intValue());
   }

   public synchronized Chapter saveChapter(Chapter chapter)
         throws Exception {
      chapter.calculateIndicies(key);

      connection.setAutoCommit(false);
      Statement stmt = connection
            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_UPDATABLE);
      ResultSet res;

      res = stmt.executeQuery("select * from chapters where book = "
            + chapter.getBookID()
            + " and `index` = "
            + chapter.getIndex());

      // Kapitel wurde schon gespeichert -> lï¿½sche es
      if (res.next()) {
         System.out.println("l\u00f6sche altes Kapitel");
         res.deleteRow();
         connection.commit();
      }

      res.moveToInsertRow();
      res.updateInt("book", chapter.getBookID());
      res.updateInt("index", chapter.getIndex());
      res.updateString("title", chapter.getTitle());
      res.updateTimestamp("date", new Timestamp(System.currentTimeMillis())); //todo: don't instantiate with currentMills
      res.insertRow();
      res.close();

      res = stmt.executeQuery("select * from chapters where book = "
            + chapter.getBookID()
            + " and `index` = "
            + chapter.getIndex());
      if (res.next())
         chapter.setDB_ID(key, res.getInt("id"));
      else {
         connection.rollback();
         stmt.close();
         connection.setAutoCommit(true);
         throw new DBC_SaveException("Kapitel "
               + chapter.getTitle()
               + "konnte nicht in der "
               + "DB gespeichert werden!");
      }
      System.out.println("lege neues Kapitel an:");

      Vector words = chapter.getWords();
      System.out.println("\nspeichere " + words.size() + " W\u00f6rter:");
      for (int i = 0; i < words.size(); i++) {
         Word word = (Word) words.get(i);

         if (word.getDB_ID() == -1) {
            // prÃ¼fe, ob schon ein solches Wort gespeichert wurde
            res = stmt.executeQuery("select * from words "
                  + "where content like binary '"
                  + mask(word.getContent())
                  + "' and language like '"
                  + word.getLanguage()
                  + "'");

            // Wort ist vorhanden
            if (res.next()) {
               word.setDB_ID(key, res.getInt("id"));
               System.out.print('-');
            }

            // Wort muss gespeichert werden
            else {
               res.moveToInsertRow();
               res.updateString("content", word.getContent());
               res.updateString("language", word.getLanguage());
               res.insertRow();

               res = stmt.executeQuery("select * from words "
                     + "where content like binary '"
                     + mask(word.getContent())
                     + "' and language like '"
                     + word.getLanguage()
                     + "'");

               // Wort ist vorhanden
               if (res.next()) {
                  word.setDB_ID(key, res.getInt("id"));
               }
               else {
                  connection.rollback();
                  stmt.close();
                  connection.setAutoCommit(true);
                  throw new DBC_SaveException("Wort "
                        + word
                        + " konnte nicht in der "
                        + "DB gespeichert werden!");
               }
               System.out.print('+');
            }

            // speichere Wort im Kapitel
            res = stmt.executeQuery("select * from words_in_chapter");
            res.moveToInsertRow();
            res.updateInt("chapter", word.getChapter().getDB_ID());
            res.updateInt("word", word.getDB_ID());
            res.updateInt("position", word.getStartPosition());
            res.insertRow();
         }
      }

      Vector signs = chapter.getSigns();
      System.out.println("\n\nspeichere " + signs.size() + " Satzzeichen:");
      for (int i = 0; i < signs.size(); i++) {
         Sign sign = (Sign) signs.get(i);

         if (sign.getDB_ID() == -1) {
            // prï¿½fe, ob schon ein solches Zeichen gespeichert wurde
            res = stmt.executeQuery("select * from signs "
                  + "where sign = '"
                  + mask(sign.getContent())
                  + "'");

            // Satzzeichen ist vorhanden
            if (res.next()) {
               sign.setDB_ID(key, res.getInt("id"));
               System.out.print('-');
            }

            // Satzzeichen muss gespeichert werden
            else {
               res.moveToInsertRow();
               res.updateString("sign", sign.getContent());
               res.insertRow();

               res = stmt.executeQuery("select * from signs "
                     + "where sign = '"
                     + mask(sign.getContent())
                     + "'");

               // Satzzeichen ist vorhanden
               if (res.next()) {
                  sign.setDB_ID(key, res.getInt("id"));
               }
               else {
                  connection.rollback();
                  stmt.close();
                  connection.setAutoCommit(true);
                  throw new DBC_SaveException("Satzzeichen "
                        + sign
                        + " konnte nicht in der "
                        + "DB gespeichert werden!");
               }
               System.out.print('+');
            }

            // speichere Satzzeichen im Kapitel
            res = stmt.executeQuery("select * from signs_in_chapter");
            res.moveToInsertRow();
            res.updateInt("chapter", sign.getChapter().getDB_ID());
            res.updateInt("sign", sign.getDB_ID());
            res.updateInt("position", sign.getStartPosition());
            res.insertRow();
         }
      }

      // Speichere Absï¿½tze
      Vector paragraphs = chapter.getParagraphs();
      System.out.println("\n\nspeichere "
            + paragraphs.size()
            + " Abs\u00e4tze:");
      res = stmt.executeQuery("select * from paragraphs_in_chapter");
      for (int i = 0; i < paragraphs.size(); i++) {
         Integer p = (Integer) paragraphs.get(i);

         res.moveToInsertRow();
         res.updateInt("chapter", chapter.getDB_ID());
         res.updateInt("position", p.intValue());
         res.insertRow();
         System.out.print('+');
      }

      // Speichere ï¿½uï¿½erungseinheiten
      Vector ius = chapter.getIllocutionUnits();
      System.out.println("\n\nspeichere "
            + ius.size()
            + " Ã„uÃŸerungseinheiten:");
      res = stmt.executeQuery("select * from illocution_units");
      for (int i = 0; i < ius.size(); i++) {
         IllocutionUnit iu = (IllocutionUnit) ius.get(i);
         res.moveToInsertRow();
         res.updateInt("chapter", chapter.getDB_ID());
         res.updateInt("start", iu.getStartPosition());
         res.updateInt("end", iu.getEndPosition());
         res.insertRow();

         res = stmt.executeQuery("select * from illocution_units "
               + "where chapter = "
               + chapter.getDB_ID()
               + " and start = "
               + iu.getStartPosition()
               + " and end = "
               + iu.getEndPosition());
         if (res.next())
            iu.setDB_ID(key, res.getInt("id"));
         else {
            connection.rollback();
            stmt.close();
            connection.setAutoCommit(true);
            throw new DBC_SaveException("Ã„uÃŸerungseinheit "
                  + iu
                  + " konnte nicht in der "
                  + "DB gespeichert werden!");
         }

         System.out.print('+');
      }
      System.out.println("\nfertig");

      connection.commit();
      stmt.close();
      connection.setAutoCommit(true);
      return chapter;
   }
   
   public synchronized void deleteChapter(Integer chapterID)
   throws Exception {
	   Statement stmt = connection.createStatement();
	   ResultSet res = stmt.executeQuery("select * "
			   + "from chapters where id = "
			   + chapterID);
	   if (res.next()) {
		   int bookID = res.getInt("book");
		   res = stmt.executeQuery("select * "
				   + "from chapters where book = "
				   + bookID);
		   res.next(); //zeigt auf zu löschendes, auf jeden Fall vorhandene Chapter
		   if(!res.next()){ //Buch kann gelöscht werden, da kein weiteres Kapitel von diesem Buch vorhanden ist
			   stmt.execute("delete from books where id = "+bookID);
		   }
		   stmt.execute("delete from chapters where id = "+chapterID);
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
      res = stmt.executeQuery("select * "
            + "from direct_speeches where chapter = "
            + chapter.getDB_ID()
            + " order by `index`");

      while (res.next())
         speeches.add(new DirectSpeech(key, res.getInt("id"), chapter, res
               .getInt("index"), res.getInt("depth"), res
               .getBoolean("accepted"), res.getInt("possible_question")));

      // ï¿½uï¿½erungseinheiten zu den direkten Reeden einlesen.
      for (int i = 0; i < speeches.size(); i++) {
         DirectSpeech ds = (DirectSpeech) speeches.get(i);
         res = stmt.executeQuery("select illocution_unit "
               + "from ius_from_direct_speeches "
               + "where direct_speech = "
               + ds.getDB_ID()
               + " order by illocution_unit");

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
			res = stmt.executeQuery("select * "
					+ "from direct_speeches "
					+ "where id = "
					+ ds.getDB_ID());
			
			if(res.next())
				res.deleteRow();
			
			//lösche alle ius_from_direct_speeches Einträge aus oldDss die bereits in der Datenbank gespeichert
			res = stmt.executeQuery("select * "
					+ "from ius_from_direct_speeches "
					+ "where direct_speech = "
					+ ds.getDB_ID());
			
			if(res.next())
				res.deleteRow();
			
		}
			
		// speichere alle aktuellen Direct Speeches des chapters
		for (int i = 0; i < newDss.size(); i++)	
		{
			DirectSpeech ds = (DirectSpeech) newDss.get(i);
			
			res = stmt.executeQuery("select * "
					+ "from direct_speeches "
					+ "where id = "
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
			
				res = stmt.executeQuery("select id "
						+ "from direct_speeches where chapter = "
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
				
				res = stmt.executeQuery("select * from ius_from_direct_speeches");
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

         res = stmt.executeQuery("select * "
               + "from direct_speeches "
               + "where id = "
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
                     .executeQuery("select * from ius_from_direct_speeches "
                           + "where direct_speech = "
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

            res = stmt.executeQuery("select id "
                  + "from direct_speeches where chapter = "
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
            res = stmt.executeQuery("select * from ius_from_direct_speeches");
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

         res = stmt.executeQuery("select * "
               + "from dialogs "
               + "where id = "
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

               res = stmt.executeQuery("select * from run_up where dialog = "
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
                     .executeQuery("select * from follow_up where dialog = "
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

            res = stmt.executeQuery("select id "
                  + "from dialogs where chapter = "
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
               res = stmt.executeQuery("select * from follow_up");
               res.moveToInsertRow();
               res.updateInt("dialog", d.getDB_ID());
               res.updateInt("start", d.getFollowUpStart().getDB_ID());
               res.updateInt("end", d.getFollowUpEnd().getDB_ID());
               res.insertRow();
               res.close();
            }

            if (d.hasRunUp()) {
               res = stmt.executeQuery("select * from run_up");
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

         res = stmt.executeQuery("select * "
               + "from speaker_changes "
               + "where id = "
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
               res = stmt.executeQuery("select * from words_in_speaker_change "
                     + "where speaker_change = "
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

            res = stmt.executeQuery("select id "
                  + "from speaker_changes where dialog = "
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
            res = stmt.executeQuery("select * words_in_speaker_change");
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
            .executeQuery("select id, `index`, depth, start, end, accepted "
                  + "from dialogs where chapter = "
                  + chapter.getDB_ID()
                  + " order by `index`");

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
         res = stmt.executeQuery("select start, end "
               + "from run_up where dialog = "
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
         res = stmt.executeQuery("select start, end "
               + "from follow_up where dialog = "
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
         res = stmt.executeQuery("select * "
               + "from speaker_changes where dialog = "
               + dialog.getDB_ID());

         if (res.next()) {
            SpeakerChange sc = new SpeakerChange(key, res.getInt("id"), dialog,
                  res.getInt("speaker_change"), res.getInt("index"), res
                        .getBoolean("accepted"));

            res = stmt.executeQuery("select word from words_in_speaker_change "
                  + "where speaker_change = "
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
      ResultSet res = stmt.executeQuery("select * from illocution_units "
            + "where chapter = "
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

   private Vector loadFunctionWords(Chapter chapter, Vector roots)
         throws Exception {
      Statement stmt = connection.createStatement();
      Vector fwords = new Vector();

      ResultSet res = stmt.executeQuery("select function_words.id, "
            + "word, start, end, accepted "
            + "from function_words "
            + "where chapter = "
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

   private Vector loadConstitutiveWords(Chapter chapter, Vector roots)
         throws Exception {
      Statement stmt = connection.createStatement();
      Vector cwords = new Vector();

      ResultSet res = stmt.executeQuery("select * "
            + "from constitutive_words "
            + "where chapter = "
            + chapter.getDB_ID());
      while (res.next()) {
         Token token = chapter.getTokenAtPosition(res.getInt("start"));
         IllocutionUnitRoot root = (IllocutionUnitRoot) getElement(token
               .getIllocutionUnit().getDB_ID(), roots);
         if (token instanceof Word) {
            cwords.add(new ConstitutiveWord(key, root, res.getInt("id"),
                  (Word) token, res.getInt("start"), res.getInt("end"), res
                        .getBoolean("accepted"), res.getInt("lexprag_path"),
                  res.getInt("lexprag_level"), res.getInt("text_gr_path"), res
                        .getInt("sem_path")));
         }
      }

      stmt.close();
      return cwords;
   }

   private Vector loadMeaningUnits(Chapter chapter,
         Vector roots,
         Vector funtionWords,
         Vector constetutiveWords)
         throws Exception {
      Vector mus = new Vector();
      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery("select * from meaning_units "
            + "where illocution_unit in"
            + "(select id from illocution_units where chapter = "
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
      ResultSet res = stmt.executeQuery("select * from sememe_groups "
            + "where illocution_unit in"
            + "(select id from illocution_units where chapter = "
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
      ResultSet res = stmt.executeQuery("select * "
            + "from checkings "
            + "where chapter = "
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
      ResultSet res = stmt.executeQuery("select * "
            + "from macro_sentences "
            + "where chapter = "
            + chapter.getDB_ID());

      while (res.next())
         mss.add(new MacroSentence(key, res.getInt("id"), iurs, iurs
               .getRootWithID(res.getInt("head")), res.getInt("path"), res
               .getBoolean("accepted")));
      res.close();

      for (int i = 0; i < mss.size(); i++) {
         MacroSentence ms = (MacroSentence) mss.get(i);
         res = stmt.executeQuery("select * from macro_sentences_dependencies "
               + "where macro_sentence = "
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

         res = stmt.executeQuery("select * "
               + "from illocution_units "
               + "where id = "
               + root.getIllocutionUnit().getDB_ID());
         if (res.next() && root.hasChanged()) {
            res.updateInt("path", root.getPath());
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

         res = stmt.executeQuery("select * "
               + "from function_words "
               + "where id = "
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

            res = stmt.executeQuery("select id "
                  + "from function_words where word = "
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

   private void saveConstitutiveWords(Vector words)
         throws Exception {
      connection.setAutoCommit(false);
      Statement stmt = connection
            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_UPDATABLE);
      ResultSet res;

      for (int i = 0; i < words.size(); i++) {
         if (!(words.get(i) instanceof ConstitutiveWord))
            continue;

         ConstitutiveWord word = (ConstitutiveWord) words.get(i);
         System.out.print("\nbetrachte CW "
               + word
               + " ("
               + word.getDB_ID()
               + ")");

         if (word.getWord() == null) {
            System.err.println("Konstitutives Wort "
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

         res = stmt.executeQuery("select * "
               + "from constitutive_words "
               + "where id = "
               + word.getDB_ID());
         if (res.next() && word.getDB_ID() != -1) {
            if (word.hasChanged()) {
               res.updateInt("chapter", word.getWord().getChapter().getDB_ID());
               res.updateInt("word", word.getWord().getDB_ID());
               res.updateInt("start", word.getStartPosition());
               res.updateInt("end", word.getEndPosition());
               res.updateBoolean("accepted", word.isAccepted());
               //res.updateByte("tr_genus", word.getTrGenus());
//               res.updateByte("tr_numerus", word.getTrNumerus());
//               res.updateLong("tr_case", word.getTrCase());
//               res.updateByte("tr_determination", word.getTrDetermination());
//               res.updateByte("tr_person", word.getTrPerson());
//               res.updateShort("tr_wordclass", word.getWordsubclassPronoun());
//               res.updateShort("tr_conjunction", word.getTrConjugation());
//               res.updateShort("tr_pronoun", word.getTrProNoun());
//               res.updateByte("tr_tempus", word.getTrTempus());
//               res.updateByte("tr_diathese", word.getTrDiathese());
               res.updateInt("lexprag_path", word.getLexpragPath());
               res.updateInt("lexprag_level", word.getLexpragLevel());
               res.updateInt("text_gr_path", word.getTextGrPath());
               res.updateInt("sem_path", word.getSemPath());
               res.updateRow();
               res.close();
               word.resetState(key);

               System.out.print(" => gespeichert");
            }
            else if (word.isRemoved()) {
               res.deleteRow();

               System.out.print(" => gelï¿½scht");
            }
         }
         else if (!word.isRemoved() && word.getDB_ID() == -1) {
            res.moveToInsertRow();
            res.updateInt("chapter", word.getWord().getChapter().getDB_ID());
            res.updateInt("word", word.getWord().getDB_ID());
            res.updateInt("start", word.getStartPosition());
            res.updateInt("end", word.getEndPosition());
            res.updateBoolean("accepted", word.isAccepted());
//            res.updateByte("tr_genus", word.getTrGenus());
//            res.updateByte("tr_numerus", word.getTrNumerus());
//            res.updateLong("tr_case", word.getTrCase());
//            res.updateByte("tr_determination", word.getTrDetermination());
//            res.updateByte("tr_person", word.getTrPerson());
//            res.updateShort("tr_wordclass", word.getWordsubclassPronoun());
//            res.updateShort("tr_conjunction", word.getWordsubclassPronoun());
//            res.updateShort("tr_pronoun", word.getTrProNoun());
//            res.updateByte("tr_tempus", word.getTrTempus());
//            res.updateByte("tr_diathese", word.getTrDiathese());
          res.updateInt("lexprag_path", word.getLexpragPath());
            res.updateInt("lexprag_level", word.getLexpragLevel());
            res.updateInt("text_gr_path", word.getTextGrPath());
            res.updateInt("sem_path", word.getSemPath());
            res.insertRow();
            res.close();
            word.resetState(key);

            res = stmt.executeQuery("select id "
                  + "from constitutive_words where word = "
                  + word.getWord().getDB_ID()
                  + " and chapter = "
                  + word.getWord().getChapter().getDB_ID()
                  + " and start = "
                  + word.getStartPosition()
                  + " and end = "
                  + word.getEndPosition());

            // hole die DB-ID zu dem neu angelegten CW
            if (res.next())
               word.setDB_ID(key, res.getInt("id"));

            else
               throw new DBC_SaveException("Konstitutives Wort "
                     + word
                     + " konnte nicht in der "
                     + "DB gespeichert werden!");

            System.out.print(" => neu angelegt");
         }

         else
            System.err.println("Konstitutives Wort "
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

         res = stmt.executeQuery("select * from "
               + "meaning_units where id = "
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

            res = stmt.executeQuery("select id "
                  + "from meaning_units where illocution_unit = "
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

         res = stmt.executeQuery("select * from "
               + "sememe_groups where id = "
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

            res = stmt.executeQuery("select id "
                  + "from sememe_groups where meaning_unit_1 = "
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

         res = stmt.executeQuery("select * from "
               + "checkings where id = "
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

            res = stmt.executeQuery("select id "
                  + "from checkings where meaning_unit = "
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

         res = stmt.executeQuery("select * from "
               + "macro_sentences where id = "
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

               res = stmt.executeQuery("select * from "
                     + "macro_sentences_dependencies "
                     + "where macro_sentence = "
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

            res = stmt.executeQuery("select id "
                  + "from macro_sentences where checking = "
                  + ms.getHead().getDB_ID());

            if (res.next())
               ms.setDB_ID(key, res.getInt("id"));

            else
               throw new DBC_SaveException("Makrosatz "
                     + ms
                     + " konnte nicht in der "
                     + "DB gespeichert werden!");

            res = stmt.executeQuery("select * from "
                  + "macro_sentences_dependencies "
                  + "where macro_sentence = "
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

      ResultSet res = stmt.executeQuery("select content "
            + "from words where id in "
            + "(select distinct word "
            + "from function_words)");
      while (res.next())
         fwords.add(res.getString(1));

      stmt.close();
      return fwords;
   }

   public synchronized Vector loadFunctionWordsCategories()
         throws Exception {
      Statement stmt = connection.createStatement();

      // Anzahl der Kategorien bestimmen
      ResultSet res = stmt.executeQuery("select count(*) "
            + "from function_words_categories");
      res.next();
      Vector cats = new Vector(res.getInt(1));

      // Kategorien auslesen.
      res = stmt.executeQuery("select category from "
            + "function_words_categories "
            + "order by category");
      while (res.next())
         cats.add(res.getString("category"));

      stmt.close();
      return cats;
   }

   public synchronized Vector loadConstitutiveWords()
         throws Exception {
      Statement stmt = connection.createStatement();
      Vector cwords = new Vector();

      ResultSet res = stmt.executeQuery("select content "
            + "from words where id in "
            + "(select distinct word "
            + "from constitutive_words)");
      while (res.next())
         cwords.add(res.getString(1));

      stmt.close();
      return cwords;
   }

   private PathNode loadPaths() {
      PathNode root = new PathNode(key, 0, null, "pathroot", "---");
      try {
         Statement stmt = connection.createStatement();
         ResultSet res = stmt
               .executeQuery("select * from paths where id > 0 order by parent");

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

   public PathNode getPaths() {
      return root;
   }

   public synchronized Boolean existsFunctionWord(Integer wordID, Integer length)
         throws Exception {
      Boolean result = new Boolean(false);
      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery("select * from function_words "
            + "where word = "
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
      ResultSet res = stmt.executeQuery("select * from constitutive_words "
            + "where word = "
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
            .executeQuery("select distinct "
                  + "substring(content, start-position+1, end-position+1) as content "
                  + "from constitutive_words as cws, words, "
                  + "words_in_chapter as winc where words.id = cws.word "
                  + "and words.id = winc.word and winc.chapter = cws.chapter "
                  + "and winc.position <= cws.start "
                  + "and cws.end <= winc.position + length(content) "
                  + "and words.language = '"
                  + language
                  + "' order by content");

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
      ResultSet res = stmt.executeQuery("select distinct "
            + "substring(content, start-position+1, end-position+1) as string "
            + "from function_words as fws, words, "
            + "words_in_chapter as winc where words.id = fws.word "
            + "and words.id = winc.word and winc.chapter = fws.chapter "
            + "and winc.position <= fws.start "
            + "and fws.end <= winc.position + length(content) "
            + "and words.language = '"
            + language
            + "' order by string");

      while (res.next())
         result.add(res.getString("string"));

      stmt.close();
      return result;
   }

   public synchronized Vector getConstitutiveWords(String word, String language)
         throws Exception {
      Vector result = new Vector();
      Statement stmt = connection.createStatement();
      String query = word;

      if (word.length() > 6)
         query = "%" + word.substring(0, word.length() - 2) + "%";
      else if (word.length() > 3)
         query = "%" + word + "%";

      ResultSet res = stmt
            .executeQuery("select cws.chapter, content, position, start, end, "
                  + "lexprag_path, lexprag_level, "
                  + "text_gr_path, sem_path "
                  + "from constitutive_words as cws, words, "
                  + "words_in_chapter as winc "
                  + "where words.id = cws.word "
                  + "and words.id = winc.word "
                  + "and winc.chapter = cws.chapter "
                  + "and winc.position <= cws.start "
                  + "and cws.end <= winc.position+length(content) "
                  + "and words.id in "
                  + "(select id from words where content like '"
                  + query
                  + "' and language = '"
                  + language
                  + "')");

      while (res.next()) {
         String content = res.getString("content");
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
            .executeQuery("select fws.chapter, content, position, start, end "
                  + "from function_words as fws, words, words_in_chapter as winc "
                  + "where words.id = fws.word "
                  + "and words.id = winc.word "
                  + "and winc.chapter = fws.chapter "
                  + "and winc.position <= fws.start "
                  + "and fws.end <= winc.position+length(content) "
                  + "and words.id in "
                  + "(select id from words where content = '"
                  + word
                  + "' and language = '"
                  + language
                  + "')");

      while (res.next()) {
         String content = res.getString("content");
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
      ResultSet res = stmt.executeQuery("select * from themas "
            + "where chapter = "
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

         res = stmt.executeQuery("select * from thema_occurrences "
               + "where thema = "
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
      stmt.executeUpdate("delete from themas where chapter = " + chapterID);
      ResultSet res = stmt.executeQuery("select * from themas where chapter = "
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
      res = stmt.executeQuery("select * from themas where chapter = "
            + chapterID);
      while (res.next()) {
         Thema_DB thema = (Thema_DB) themas.get(res.getInt("index"));
         thema.setDB_ID(key, res.getInt("id"));
      }

      // Referenzen speichern
      res = stmt.executeQuery("select * from themas where chapter = "
            + chapterID);
      while (res.next()) {
         Thema_DB thema = (Thema_DB) themas.get(res.getInt("index"));
         if (thema.getFirstOccurrence() != null)
            res.updateInt("first_occurrence", thema.getFirstOccurrence()
                  .getDB_ID());
         res.updateRow();
      }

      // Vorkommen speichern
      res = stmt.executeQuery("select * from thema_occurrences");
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
         res = stmt.executeQuery("select id "
               + "from isotope_categories "
               + "where category like binary '"
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
         res = stmt.executeQuery("select * "
               + "from isotopes where id = "
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

            res = stmt.executeQuery("select id "
                  + "from isotopes where category = "
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
      ResultSet res = stmt.executeQuery("select isotopes.id, isotopes.index, "
            + "isotope_categories.category "
            + "from isotopes, isotope_categories "
            + "where isotopes.chapter = "
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
      ResultSet res = stmt.executeQuery("select hierachy "
            + "from isotope_hierachies "
            + "where chapter = "
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
      ResultSet res = stmt.executeQuery("select chapter, hierachy "
            + "from isotope_hierachies "
            + "where chapter = "
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
      ResultSet res = stmt.executeQuery("select count(*) as c, language "
            + "from words group by language order by c desc");

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
            subquery = "select id from illocution_units where chapter = "
                  + ChapterID;
            break;
         case Comments.CLASS_CODE_DIRECT_SPEECH :
            subquery = "select id from direct_speeches where chapter = "
                  + ChapterID;
            break;
         case Comments.CLASS_CODE_DIALOG :
            subquery = "select id from dialogs where chapter = " + ChapterID;
            break;
         case Comments.CLASS_CODE_DIALOG_FOLLOWUP :
            subquery = "select id from dialogs where chapter = " + ChapterID;
            break;
         case Comments.CLASS_CODE_DIALOG_RUNUP :
            subquery = "select id from dialogs where chapter = " + ChapterID;
            break;
         default :
            subquery = "";
            break;
      }

      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery("select owner_id, owner_class_code, "
            + "program, comment from comments "
            + "where owner_class_code = "
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

         res = stmt.executeQuery("select * from comments where owner_id = "
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
    */

  

   public void saveWordList(Vector list)
         throws Exception {
      connection.setAutoCommit(false);
      Statement stmt = connection
            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_UPDATABLE);

      ResultSet res;

      for (int i = 0; i < list.size(); i++) {
         DB_Tupel tupel = (DB_Tupel) list.get(i);

         // Tupel soll gelï¿½scht werden
         if (tupel.getState() == DB_Tupel.DELETE) {
            res = stmt.executeQuery("select * from word_list where id = "
                  + tupel.getInt("id"));
            if (res.next()) {
               // System.out.println("lï¿½sche "+ tupel);
               res.deleteRow();
               connection.commit();
            }
         }

         // Tupel wird in der DB neu angelegt oder geï¿½ndert
         else {
            // Fï¿½ge erst eventuell das Wort in die Wort-DB ein
            res = stmt.executeQuery("select * from words where content like '"
                  + tupel.getString("content")
                  + "' and language like '"
                  + tupel.getString("language")
                  + "'");
            if (!res.next()) {
               res.moveToInsertRow();
               res.updateString("content", tupel.getString("content"));
               res.updateString("language", tupel.getString("language"));
               res.insertRow();
               i--;
               connection.commit();
               continue;
            }

            // hole die Wort-ID aud der Wort-DB
            int wordID = res.getInt("id");
            res.close();
            // System.out.println("Wort-ID: "+ wordID);

            // Tupel soll geï¿½ndert werden
            if (tupel.getState() == DB_Tupel.CHANGE) {
               res = stmt.executeQuery("select * from word_list where id = "
                     + tupel.getInt("id"));
               if (res.next()) {
                  // System.out.println("ï¿½ndere "+ tupel);
                  res.updateInt("word", wordID);

                  if (tupel.containsKey("tr_genus"))
                     res.updateByte("tr_genus", tupel.getByte("tr_genus"));

                  if (tupel.containsKey("tr_numerus"))
                     res.updateByte("tr_numerus", tupel.getByte("tr_numerus"));

                  if (tupel.containsKey("tr_case"))
                     res.updateByte("tr_case", tupel.getByte("tr_case"));

                  if (tupel.containsKey("tr_determination"))
                     res.updateByte("tr_determination", tupel
                           .getByte("tr_determination"));

                  if (tupel.containsKey("tr_person"))
                     res.updateByte("tr_person", tupel.getByte("tr_person"));

                  if (tupel.containsKey("tr_wordclass"))
                     res.updateByte("tr_wordclass", tupel
                           .getByte("tr_wordclass"));

                  if (tupel.containsKey("tr_wordsubclass"))
                     res.updateByte("tr_wordsubclass", tupel
                           .getByte("tr_wordsubclass"));

                  if (tupel.containsKey("tr_conjugation"))
                     res.updateByte("tr_conjugation", tupel
                           .getByte("tr_conjugation"));

                  if (tupel.containsKey("tr_pronoun"))
                     res.updateByte("tr_pronoun", tupel.getByte("tr_pronoun"));

                  if (tupel.containsKey("tr_tempus"))
                     res.updateByte("tr_tempus", tupel.getByte("tr_tempus"));

                  if (tupel.containsKey("tr_diathese"))
                     res
                           .updateByte("tr_diathese", tupel
                                 .getByte("tr_diathese"));

                  if (tupel.containsKey("type"))
                     res.updateByte("type", tupel.getByte("type"));

                  if (tupel.containsKey("multiple"))
                     res.updateInt("multiple", tupel.getInt("multiple"));
                  res.updateRow();
                  connection.commit();
               }
               else {
                  // System.out.println(tupel +" konnte nicht geï¿½ndert
                  // werden.\n" +
                  // "Lege es neu an...");
                  tupel.setState(DB_Tupel.SAVE);
                  i--;
                  continue;
               }
            }

            // Tupel wird neu angelegt
            else {
               String query = "select * from word_list where word = " + wordID;

               if (tupel.containsKey("tr_genus"))
                  query += " and tr_genus = " + tupel.getByte("tr_genus");

               if (tupel.containsKey("tr_numerus"))
                  query += " and tr_numerus = " + tupel.getByte("tr_numerus");

               if (tupel.containsKey("tr_case"))
                  query += " and tr_case = " + tupel.getByte("tr_case");

               if (tupel.containsKey("tr_determination"))
                  query += " and tr_determination = "
                        + tupel.getByte("tr_determination");

               if (tupel.containsKey("tr_person"))
                  query += " and tr_person = " + tupel.getByte("tr_person");

               if (tupel.containsKey("tr_wordclass"))
                  query += " and tr_wordclass = "
                        + tupel.getByte("tr_wordclass");

               if (tupel.containsKey("tr_wordsubclass"))
                  query += " and tr_wordsubclass = "
                        + tupel.getByte("tr_wordsubclass");

               if (tupel.containsKey("tr_conjugation"))
                  query += " and tr_conjugation = "
                        + tupel.getByte("tr_conjugation");

               if (tupel.containsKey("tr_pronoun"))
                  query += " and tr_pronoun = " + tupel.getByte("tr_pronoun");

               if (tupel.containsKey("tr_tempus"))
                  query += " and tr_tempus = " + tupel.getByte("tr_tempus");

               if (tupel.containsKey("tr_diathese"))
                  query += " and tr_diathese = " + tupel.getByte("tr_diathese");

               if (tupel.containsKey("type"))
                  query += " and type = " + tupel.getByte("type");

               res = stmt.executeQuery(query);

               // Tupel in der Wortliste noch nicht vorhanden,
               // wird also neu angelegt
               if (!res.next()) {
                  // System.out.println("speichere "+ tupel);
                  res.moveToInsertRow();
                  res.updateInt("word", wordID);

                  if (tupel.containsKey("tr_genus"))
                     res.updateByte("tr_genus", tupel.getByte("tr_genus"));

                  if (tupel.containsKey("tr_numerus"))
                     res.updateByte("tr_numerus", tupel.getByte("tr_numerus"));

                  if (tupel.containsKey("tr_case"))
                     res.updateByte("tr_case", tupel.getByte("tr_case"));

                  if (tupel.containsKey("tr_determination"))
                     res.updateByte("tr_determination", tupel
                           .getByte("tr_determination"));

                  if (tupel.containsKey("tr_person"))
                     res.updateByte("tr_person", tupel.getByte("tr_person"));

                  if (tupel.containsKey("tr_wordclass"))
                     res.updateByte("tr_wordclass", tupel
                           .getByte("tr_wordclass"));

                  if (tupel.containsKey("tr_wordsubclass"))
                     res.updateByte("tr_wordsubclass", tupel
                           .getByte("tr_wordsubclass"));

                  if (tupel.containsKey("tr_conjugation"))
                     res.updateByte("tr_conjugation", tupel
                           .getByte("tr_conjugation"));

                  if (tupel.containsKey("tr_pronoun"))
                     res.updateByte("tr_pronoun", tupel.getByte("tr_pronoun"));

                  if (tupel.containsKey("tr_tempus"))
                     res.updateByte("tr_tempus", tupel.getByte("tr_tempus"));

                  if (tupel.containsKey("tr_diathese"))
                     res
                           .updateByte("tr_diathese", tupel
                                 .getByte("tr_diathese"));

                  if (tupel.containsKey("type"))
                     res.updateByte("type", tupel.getByte("type"));

                  if (tupel.containsKey("multiple"))
                     res.updateInt("multiple", tupel.getInt("multiple"));

                  res.insertRow();
                  connection.commit();
               }
               else {
                  // System.out.println(tupel +" schon vorhanden");
               }
            }
         }
      }
      stmt.close();
      connection.setAutoCommit(true);
   }

   public Vector saveComplexes(Vector complexes, Integer chapterID)
         throws Exception {
      Chapter chapter = getChapter(chapterID.intValue());

      connection.setAutoCommit(false);
      Statement stmt = connection
            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_UPDATABLE);
      ResultSet res;

      for (int i = 0; i < complexes.size(); i++) {
         Complex_DB complex = (Complex_DB) complexes.get(i);

         if (complex.isUnchanged())
            continue;

         res = stmt.executeQuery("select * from "
               + "complexes where id = "
               + complex.getDB_ID());

         if (res.next()) {
            if (complex.hasChanged()) {
               res.updateInt("type", complex.getComplexType());
               res.updateInt("hash", complex.hashCode());
               complex.resetState(key);

               // noch die Nomen des Komplexes abspeichern
               Vector ns = complex.getNounsID();
               res = stmt.executeQuery("select * from "
                     + "complex_nouns where complex = "
                     + complex.getDB_ID());

               // die alten Eintrï¿½ge lï¿½schen
               while (res.next())
                  res.deleteRow();

               for (int j = 0; j < ns.size(); j++) {
                  Integer id = (Integer) ns.get(j);
                  res.moveToInsertRow();
                  res.updateInt("complex", complex.getDB_ID());
                  res.updateInt("noun", id.intValue());
                  res.insertRow();
               }

               // noch die Deiktikas des Komplexes abspeichern
               Vector ds = complex.getDeicticasID();
               res = stmt.executeQuery("select * from "
                     + "deicticons where complex = "
                     + complex.getDB_ID());

               // die alten Eintrï¿½ge lï¿½schen
               while (res.next())
                  res.deleteRow();

               for (int j = 0; j < ds.size(); j++) {
                  Integer id = (Integer) ds.get(j);
                  res.moveToInsertRow();
                  res.updateInt("complex", complex.getDB_ID());
                  res.updateInt("deicticon", id.intValue());
                  res.insertRow();
               }
            }
            else if (complex.isRemoved())
               res.deleteRow();
         }
         else if (!complex.isRemoved()) {
            res.moveToInsertRow();
            res.updateInt("chapter", chapter.getDB_ID());
            res.updateInt("type", complex.getComplexType());
            res.updateInt("hash", complex.hashCode());
            res.insertRow();
            res.close();
            complex.resetState(key);

            res = stmt.executeQuery("select id from complexes where chapter = "
                  + chapter.getDB_ID()
                  + " and type = "
                  + complex.getComplexType()
                  + " and hash = "
                  + complex.hashCode());

            if (res.next())
               complex.setDB_ID(key, res.getInt("id"));

            else
               throw new DBC_SaveException("Komplex "
                     + complex
                     + " konnte nicht in der "
                     + "DB gespeichert werden!");

            // noch die Deiktikas des Komplexes abspeichern
            Vector ds = complex.getDeicticasID();
            res = stmt.executeQuery("select * from "
                  + "deicticons where complex = "
                  + complex.getDB_ID());

            // die alten Eintrï¿½ge lï¿½schen
            while (res.next())
               res.deleteRow();

            for (int j = 0; j < ds.size(); j++) {
               Integer id = (Integer) ds.get(j);
               res.moveToInsertRow();
               res.updateInt("complex", complex.getDB_ID());
               res.updateInt("deicticon", id.intValue());
               res.insertRow();
            }

            // noch die Nomen des Komplexes abspeichern
            Vector ns = complex.getNounsID();
            res = stmt.executeQuery("select * from "
                  + "complex_nouns where complex = "
                  + complex.getDB_ID());

            // die alten Eintrï¿½ge lï¿½schen
            while (res.next())
               res.deleteRow();

            for (int j = 0; j < ns.size(); j++) {
               Integer id = (Integer) ns.get(j);
               res.moveToInsertRow();
               res.updateInt("complex", complex.getDB_ID());
               res.updateInt("noun", id.intValue());
               res.insertRow();
            }
         }
         res.close();
         // schreibe ï¿½nderung in die DB
         connection.commit();
      }
      connection.setAutoCommit(true);
      stmt.close();
      return complexes;
   }

   public Vector loadComplexes(Integer chapterID)
         throws Exception {
      Vector result = new Vector();
      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery("select id, type "
            + "from complexes where chapter = "
            + chapterID);

      while (res.next())
         result.add(new Complex_DB(key, res.getInt("id"), res.getInt("type")));

      for (int i = 0; i < result.size(); i++) {
         Complex_DB c = (Complex_DB) result.get(i);
         res = stmt.executeQuery("select deicticon from deicticons "
               + "where complex = "
               + c.getDB_ID());
         while (res.next())
            c.addDeicticonID(key, res.getInt("deicticon"));
         c.resetState(key);
      }

      for (int i = 0; i < result.size(); i++) {
         Complex_DB c = (Complex_DB) result.get(i);
         res = stmt.executeQuery("select noun from complex_nouns "
               + "where complex = "
               + c.getDB_ID());
         while (res.next())
            c.addNounID(key, res.getInt("noun"));
         c.resetState(key);
      }

      stmt.close();
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
         res = stmt.executeQuery("select id "
               + "from renominalisation_categories "
               + "where category like binary '"
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
         res = stmt.executeQuery("select * "
               + "from renominalisations where id = "
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

            res = stmt.executeQuery("select id "
                  + "from renominalisations where category = "
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
            .executeQuery("select renominalisations.id, renominalisations.constitutive_word,  "
                  + "renominalisation_categories.category "
                  + "from renominalisations, renominalisation_categories "
                  + "where renominalisations.chapter = "
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
   
   
   
   
   
   
   
   
   
public WordListElement saveWordListElement(WordListElement element) throws Exception{
	
	connection.setAutoCommit(false);
	   Statement stmt = connection
	   		.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_UPDATABLE);
	   ResultSet res = stmt.executeQuery("select * from words "
            + "where content like binary '"
            + mask(element.getContent())
            + "' and language like '"
            + element.getLanguage()
            + "'");
	   connection.commit();
      
    int wordId = 0;
    //Wort ist vorhanden
	if (res.next()) {
         wordId = res.getInt("id");
         System.out.print('-');
	}
    else {// Wort muss gespeichert werden
     	 //res.moveToInsertRow();
    	  res.close();
    	  stmt.executeUpdate("insert into "
                + "words (content,language) "
                + "values('" 
                + element.getContent()+"','"
                + element.getLanguage() +"'"
                +")");
    	  connection.commit();
   	   		
         res = stmt.executeQuery("select * from words "
                 + "where content like binary '"
                 + mask(element.getContent())
                 + "' and language like '"
                 + element.getLanguage()
                 + "'");
         connection.commit();
         res.next();
         wordId = res.getInt("id"); //problem: leeres result set
      }
	   res.close();
	
	   for(int i = 0; i != element.getAssignations().size();i++){
		   int idBefore = ((TR_Assignation)element.getAssignations().get(i)).DB_ID;
		   int assignationId = saveAssignation((TR_Assignation)element.getAssignations().get(i));
		   if(idBefore == -1){
		   connection.setAutoCommit(false);
		   stmt.executeUpdate("insert into "
                  + "word_list_elements (word_id,assignation_id) "
                  + "values('" 
                  + wordId+"','"
                  + assignationId +"'"
                  +")");
		   connection.commit();
		   }
		   
	   }
	   stmt.close();
	   connection.setAutoCommit(true);
	   return element;
}
public synchronized WordListElement loadWordListElement(String content) throws Exception{
	WordListElement element = null;
	connection.setAutoCommit(false);
	Statement stmt = connection.createStatement();
    ResultSet res = stmt.executeQuery("select * from words where content = '"+content+"'");
	connection.commit();
	if(res.next()){
		element = new WordListElement(content);
		int wordID = res.getInt("id");
		res = stmt.executeQuery("select * from word_list_elements where word_id = "+wordID);
		connection.commit();
		Vector<Integer> assignationIDs = new Vector<Integer>();
		while(res.next()){
			int assignationID = res.getInt("assignation_id");
			assignationIDs.add(assignationID);
		}
		for(int i = 0; i != assignationIDs.size(); i++){
			res = stmt.executeQuery("select * from assignations where id = "+assignationIDs.get(i));
			connection.commit();
			if(res.next()){
				TR_Assignation assi = new TR_Assignation(res.getByte("tr_type"), res.getByte("tr_genus"), res.getByte("tr_numerus"), res.getByte("tr_determination"), 
						res.getLong("tr_case"), res.getByte("tr_person"), res.getShort("tr_conjugation"), res.getByte("tr_tempus"), res.getByte("tr_diathese"), 
						res.getInt("tr_wordclass"), res.getByte("tr_subclass_connector"), res.getByte("tr_subclass_verb"), res.getByte("tr_subclass_adjective"), 
						res.getShort("tr_subclass_pronoun"), res.getShort("tr_subclass_sign"), res.getShort("tr_wortart1"), res.getShort("tr_wortart2"), 
						res.getByte("tr_wortart3"), res.getByte("tr_wortart4"), res.getString("etymol"), res.getString("description"),content);
				assi.DB_ID = res.getInt("id");
				element.addAssignation(assi);
			}
			
		}
	}
	else{
		res.close();
		stmt.close();
		connection.setAutoCommit(true);
		return null;
	}
	res.close();
	stmt.close();
	connection.setAutoCommit(true);
	return element;
}

	//gibt für jede Assigantion eines Strings die Wortklasse und Subklasse zurück
	public Vector loadWordClasses(Vector contents) throws Exception {
		Vector<Vector> resultSet = new Vector<Vector>();
		for(int i=0; i != contents.size(); ++i)
		{
			String content = (String)contents.get(i);
			Vector assignations = loadWordListElement(content).getAssignations();
			Vector<Long> wordClasses = new Vector<Long>();
			Vector<Long> wordSubClasses = new Vector<Long>();
			for (int j=0; j != assignations.size(); ++j)
			{
				TR_Assignation assi = (TR_Assignation)assignations.get(j); 
				wordClasses.add(assi.getWordclassesBinary());
				if (assi.getWordsubclassAdjectivesBinary()        != 0)
					wordSubClasses.add(assi.getWordsubclassAdjectivesBinary());
				else if (assi.getWordsubclassConnectorsBinary()   != 0)
					wordSubClasses.add(assi.getWordsubclassConnectorsBinary());
				else if (assi.getWordsubclassPrepositionsBinary() != 0)
					wordSubClasses.add(assi.getWordsubclassPrepositionsBinary());
				else if (assi.getWordsubclassPronounsBinary()     != 0)
					wordSubClasses.add(assi.getWordsubclassPronounsBinary());
				else if (assi.getWordsubclassSignsBinary()        != 0)
					wordSubClasses.add(assi.getWordsubclassSignsBinary());
				else if (assi.getWordsubclassVerbsBinary()        != 0)
					wordSubClasses.add(assi.getWordsubclassVerbsBinary());
				else 
					wordSubClasses.add(new Long(0));
			}
			resultSet.add(wordClasses);
			resultSet.add(wordSubClasses);
		}
		return resultSet;	  
	}


public synchronized WordListElement loadWordListElement(TR_Assignation assignation) throws Exception{
	WordListElement element = null;
	if(assignation.DB_ID == -1){
		return null;
	}
	connection.setAutoCommit(false);
	Statement stmt = connection.createStatement();
	ResultSet res = stmt.executeQuery("select * from word_list_elements where assignation_id = "+assignation.DB_ID);
	connection.commit();
	if(res.next()){
		int wordId = res.getInt("word_id");
		res = stmt.executeQuery("select * from words where id = "+wordId);
		connection.commit();
		if(res.next()){
			String content = res.getString("content");
			element = loadWordListElement(content);
		}
	}
	else{
		res.close();
		stmt.close();
		connection.setAutoCommit(true);
		return null;
	}
	res.close();
	stmt.close();
	connection.setAutoCommit(true);
	return element;
}

public synchronized int saveAssignation(TR_Assignation assignation) throws Exception{
	   
	   connection.setAutoCommit(false);
	   Statement stmt = connection
	   		.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
               ResultSet.CONCUR_UPDATABLE);
	   if(assignation.DB_ID != -1){
		   stmt.executeUpdate("update assignations "
	                  + "set tr_type = '" + assignation.getTypesBinary() +"',"
	                  + "tr_genus = '" + assignation.getGenusBinary() +"',"
	                  + "tr_numerus = '" + assignation.getNumerusBinary() +"',"
	                  + "tr_determination = '" + assignation.getDeterminationBinary() +"',"
	                  + "tr_case = '" + assignation.getCasesBinary() +"',"
	                  + "tr_person = '" + assignation.getPersonsBinary() +"',"
	                  + "tr_conjugation = '" + assignation.getConjugationsBinary() +"',"
	                  + "tr_tempus = '" + assignation.getTempusBinary() +"',"
	                  + "tr_diathese = '" + assignation.getDiathesesBinary() +"',"
	                  + "tr_wordclass = '" + assignation.getWordclassesBinary() +"',"
	                  + "tr_subclass_connector = '" + assignation.getWordsubclassConnectorsBinary() +"',"
	                  + "tr_subclass_verb = '" + assignation.getWordsubclassVerbsBinary() +"',"
	                  + "tr_subclass_adjective = '" + assignation.getWordsubclassAdjectivesBinary() +"',"
	                  + "tr_subclass_preposition = '" + assignation.getWordsubclassPrepositionsBinary() +"',"
	                  + "tr_subclass_pronoun = '" + assignation.getWordsubclassPronounsBinary() +"',"
	                  + "tr_subclass_sign = '" + assignation.getWordsubclassSignsBinary() +"',"
	                  + "tr_wortart1 = '" + assignation.getWortarten1Binary() +"',"
	                  + "tr_wortart2 = '" + assignation.getWortarten2Binary() +"',"
	                  + "tr_wortart3 = '" + assignation.getWortarten3Binary() +"',"
	                  + "tr_wortart4 = '" + assignation.getWortarten4Binary() +"',"
	                  + "description = '" + assignation.getDescription() +"',"
	                  + "etymol = '" + assignation.getEtymol() +"'"
	                  + " where id = "+assignation.DB_ID
	       );
	   }
	   else{ //Assignation exists, just change existing values
		   stmt.executeUpdate("insert into "
	                  + "assignations (tr_type,tr_genus,tr_numerus,tr_determination,tr_case,tr_person,tr_conjugation,tr_tempus,tr_diathese,tr_wordclass,tr_subclass_connector," +
	                  		"tr_subclass_verb,tr_subclass_adjective,tr_subclass_preposition,tr_subclass_pronoun,tr_subclass_sign,tr_wortart1,tr_wortart2,tr_wortart3," +
	                  		"tr_wortart4,description,etymol) "
	                  + "values("
	                  + "'" + assignation.getTypesBinary() +"'," 
	                  + "'" + assignation.getGenusBinary() +"',"
	                  + "'" + assignation.getNumerusBinary() +"',"
	                  + "'" + assignation.getDeterminationBinary() +"',"
	                  + "'" + assignation.getCasesBinary() +"',"
	                  + "'" + assignation.getPersonsBinary() +"',"
	                  + "'" + assignation.getConjugationsBinary() +"',"
	                  + "'" + assignation.getTempusBinary() +"',"
	                  + "'" + assignation.getDiathesesBinary() +"',"
	                  + "'" + assignation.getWordclassesBinary() +"',"
	                  + "'" + assignation.getWordsubclassConnectorsBinary() +"',"
	                  + "'" + assignation.getWordsubclassVerbsBinary() +"',"
	                  + "'" + assignation.getWordsubclassAdjectivesBinary() +"',"
	                  + "'" + assignation.getWordsubclassPrepositionsBinary() +"',"
	                  + "'" + assignation.getWordsubclassPronounsBinary() +"',"
	                  + "'" + assignation.getWordsubclassSignsBinary() +"',"
	                  + "'" + assignation.getWortarten1Binary() +"',"
	                  + "'" + assignation.getWortarten2Binary() +"',"
	                  + "'" + assignation.getWortarten3Binary() +"',"
	                  + "'" + assignation.getWortarten4Binary() +"',"
	                  + "'" + assignation.getDescription() +"',"
	                  + "'" + assignation.getEtymol() +"'"+
	       ")");
		   ResultSet res = stmt.getGeneratedKeys();
		   int ID = -1;
		   res.next();
		   ID = res.getInt(1);
		   assignation.DB_ID = ID;
	   }
	   connection.commit();
	   
	   stmt.close();
	   connection.setAutoCommit(true);
	   return assignation.DB_ID;
   }
   
   public synchronized Relation saveRelation(Relation r) throws SQLException, DBC_SaveException{
	   	  if(r.getOrigin().DB_ID == -1 || r.getTarget().DB_ID == -1){
	   		throw new DBC_SaveException("Speichern der relation fehlgeschlagen: Assignation muss vorher gespeichert werden");
	   	  }
	   	      connection.setAutoCommit(false);
	      Statement stmt = connection
	            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
	                  ResultSet.CONCUR_UPDATABLE);
	      ResultSet res;

	      res = stmt.executeQuery("select * from relations where origin = "
	            + r.getOrigin().DB_ID
	            + " and target = "
	            + r.getTarget().DB_ID
	            + " and type = "
	            + r.getType());

	      if (res.next()) { //schon gespeichert, setze DB ID
	         int ID = res.getInt(1);
	         r.setDB_ID(ID);
	         connection.commit();
	         return r;
	      }
	      stmt.executeUpdate("insert into relations (origin,target,type) values('"
	    		  +r.getOrigin().DB_ID+"','"
	    		  +r.getTarget().DB_ID+"','"
	      		  +r.getType()+"')");
	      res = stmt.getGeneratedKeys();
		  res.next();
		  int ID = res.getInt(1);
		  r.setDB_ID(ID);
		  return r;
   }
   
   public Vector loadRelations(TR_Assignation assignation) throws SQLException{
	   Vector resultSet = new Vector();
	   if(assignation.DB_ID == -1){
		   System.out.println("Assignation ist noch nicht gespeichert, daher können keine Relations geladen werden.");
		   return resultSet;
	   }
	   	  connection.setAutoCommit(false);
	      Statement stmt = connection
	            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
	                  ResultSet.CONCUR_UPDATABLE);
	      ResultSet res;
	      //first case: relation from given assignation as origin
	      res = stmt.executeQuery("select * from relations where origin = "
	            + assignation.DB_ID);
	      Vector<Integer> relationIds = new Vector<Integer>();
	      Vector<Integer> targetIds = new Vector<Integer>();
	      Vector<Integer> types = new Vector<Integer>();
	      while (res.next()) {   
	    	 int dbID = res.getInt(1);
	    	 relationIds.add(dbID);
	         int targetID = res.getInt("target");
	         targetIds.add(targetID);
	         int type = res.getInt("type");
	         types.add(type);
	      }
	      for(int i = 0; i != relationIds.size();i++){
	         res = stmt.executeQuery("select * from assignations where id = "
	 	            + targetIds.get(i));
	        
			connection.commit();
			TR_Assignation targetAssignation = null;
			if(res.next()){
			   targetAssignation = new TR_Assignation(res.getByte("tr_type"), res.getByte("tr_genus"), res.getByte("tr_numerus"), res.getByte("tr_determination"), 
							res.getLong("tr_case"), res.getByte("tr_person"), res.getShort("tr_conjugation"), res.getByte("tr_tempus"), res.getByte("tr_diathese"), 
							res.getInt("tr_wordclass"), res.getByte("tr_subclass_connector"), res.getByte("tr_subclass_verb"), res.getByte("tr_subclass_adjective"), 
							res.getShort("tr_subclass_pronoun"), res.getShort("tr_subclass_sign"), res.getShort("tr_wortart1"), res.getShort("tr_wortart2"), 
							res.getByte("tr_wortart3"), res.getByte("tr_wortart4"), res.getString("etymol"), res.getString("description"));
					targetAssignation.DB_ID = res.getInt("id");
				}
			res = stmt.executeQuery("select * from word_list_elements where assignation_id = "
	 	            + targetAssignation.DB_ID);
			 connection.commit();
			 if(res.next()){
				 int wordId = res.getInt("word_id");
				 res = stmt.executeQuery("select * from words where id = "
			 	            + wordId);
				 if(res.next()){
					 String content = res.getString("content");
					 targetAssignation.setContent(content);
				 }
					 
			 }
			 Relation r = new Relation(assignation,targetAssignation,types.get(i));
	         r.setDB_ID(relationIds.get(i));
			 resultSet.add(r);
	         connection.commit();
	         
	      }
	      //second case: relation to given assignation as target
	      res = stmt.executeQuery("select * from relations where target = "
		            + assignation.DB_ID);
	      relationIds = new Vector<Integer>();
	      Vector<Integer> sourceIds = new Vector<Integer>();
	      types = new Vector<Integer>();
	      while (res.next()) {   
		    	 int dbID = res.getInt(1);
		    	 relationIds.add(dbID);
		         int sourceID = res.getInt("origin");
		         sourceIds.add(sourceID);
		         int type = res.getInt("type");
		         types.add(type);
		      }
		      for(int i = 0; i != relationIds.size();i++){
		         res = stmt.executeQuery("select * from assignations where id = "
		 	            + sourceIds.get(i));
		         connection.commit();
				TR_Assignation originAssignation = null;
				if(res.next()){
				   originAssignation = new TR_Assignation(res.getByte("tr_type"), res.getByte("tr_genus"), res.getByte("tr_numerus"), res.getByte("tr_determination"), 
								res.getLong("tr_case"), res.getByte("tr_person"), res.getShort("tr_conjugation"), res.getByte("tr_tempus"), res.getByte("tr_diathese"), 
								res.getInt("tr_wordclass"), res.getByte("tr_subclass_connector"), res.getByte("tr_subclass_verb"), res.getByte("tr_subclass_adjective"), 
								res.getShort("tr_subclass_pronoun"), res.getShort("tr_subclass_sign"), res.getShort("tr_wortart1"), res.getShort("tr_wortart2"), 
								res.getByte("tr_wortart3"), res.getByte("tr_wortart4"), res.getString("etymol"), res.getString("description"));
						originAssignation.DB_ID = res.getInt("id");
					}
				 res = stmt.executeQuery("select * from word_list_elements where assignation_id = "
		 	            + originAssignation.DB_ID);
				 connection.commit();
				 if(res.next()){
					 int wordId = res.getInt("word_id");
					 res = stmt.executeQuery("select * from words where id = "
				 	            + wordId);
					 if(res.next()){
						 String content = res.getString("content");
						 originAssignation.setContent(content);
					 }
						 
				 }
				
				 Relation r = new Relation(originAssignation,assignation,types.get(i));
		         r.setDB_ID(relationIds.get(i));
				 resultSet.add(r);
		         connection.commit();
		         
		      }

	   return resultSet;
   }
   
   public Vector loadRelations(Vector assignations) throws SQLException{
	   Vector resultSet = new Vector();
	   for(int i = 0; i != assignations.size();i++){
		   TR_Assignation assignation = (TR_Assignation)assignations.get(i);
		   resultSet.addAll(loadRelations(assignation));
	   }
	   return resultSet;
	  
   }
   
   public boolean isEdited(Chapter c, int category) throws SQLException{
	   Vector resultSet = new Vector();
	   connection.setAutoCommit(false);
	   Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	   ResultSet res = null;
	   switch(category){
	   		
		   case ChapterEditingTester.CONSTITUTIVE_WORD:
			   res = stmt.executeQuery("select * from constitutive_words where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.FUNCTION_WORD:
			   res = stmt.executeQuery("select * from function_words where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.COMPLEX:
			   res = stmt.executeQuery("select * from complexes where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.DIALOG:
			   res = stmt.executeQuery("select * from dialogs where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.DIRECT_SPEECH:
			   res = stmt.executeQuery("select * from direct_speeches where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.ILLOCUTION_UNIT:
			   res = stmt.executeQuery("select * from illocution_units where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.ISOTOPE:
			   res = stmt.executeQuery("select * from isotopes where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.MACRO_SENTENCE:
			   res = stmt.executeQuery("select * from macro_sentences where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.RENOMINALISATION:
			   res = stmt.executeQuery("select * from renominalisations where chapter="+c.getDB_ID());
			   break;
		   case ChapterEditingTester.THEMA:
			   res = stmt.executeQuery("select * from themas where chapter="+c.getDB_ID());
			   break;
	   }
	      
	   return res.next();
   }
   
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
   
}