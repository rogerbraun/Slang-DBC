/*
 * Erstellt: 29.04.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import de.uni_tuebingen.wsi.ct.slang2.dbc.client.DBC;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.chapterloader.ChapterLoader;

/**
 * Das Kapitel ist die grundlegende Datenstruktur, die immer benötigt wird. Hier
 * erhält man Zugriff auf die einzelnen Tokens (Wörter oder Satzzeichen) und die
 * Äußerungseinheiten (illocutionunit). <br>
 * Die Adressierung der Tokens und Äußerungseinheit ist über zwei Wege möglich:
 * <ul>
 * <li>über den Index, eine Folgem beginnend bei 0 (wie bei einem Array)</li>
 * <li>über die Zeichenposition (oder einfach nur Position), also die Position
 * des Buchstabens, auch beginnend bei 0. Hier kann es aber vorkommen, das die
 * angegebene Zeichenposition auch auch ein Leerzeichen zeigen kann</li>
 * </ul>
 * Jedes Zusatzmodul baut auf dem Kapitel auf, verändert jedoch nicht dessen
 * Daten. <br>
 * Ein Kapitel wird mit der Funktion loadChapter() vom DBC geladen, eine
 * Übersicht über alle Bücher und Kapitel bietet der ChapterLoader
 * 
 * @author Volker Klöbb
 * @see DBC#loadChapter(int)
 * @see ChapterLoader
 */
public class Chapter
      implements
         Serializable, IDOwner {

   /**
    * 
    */
   private static final long serialVersionUID = 999540791506505684L;
   private int               id;
   private int               bookID;
   private int               index;
   private String 			  date;
   private String            title;
   private Vector<Token>     tokens;
   private Vector            paragraphs;
   private Vector<IllocutionUnit>            illocutionUnits;
   private String            content;
   private Hashtable         wordCache;

   /**
    * Wird vom DBC benötigt
    */
   public Chapter(DBC_Key key, int id, int bookID, int index, String title, String date) {
      key.unlock();
      this.id = id;
      this.bookID = bookID;
      this.index = index;
      this.title = title;
      this.date = date;

      tokens = new Vector();
      paragraphs = new Vector();
      illocutionUnits = new Vector();
      wordCache = new Hashtable();
   }

   /**
    * Die eindeutige ID des Kapitels innerhalb der Datenbank
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
    * Der Index des Kapitels. Die Zählung beginnt bei 0.
    * 
    */
   public int getIndex() {
      return index;
   }

   private int getTokenIndex(int position) {
      int lastIndex = 0;
      for (int i = 0; i < tokens.size(); i++) {
         Token t = (Token) tokens.get(i);
         if (t.getEndPosition() > position)
            return lastIndex;
         lastIndex++;
      }
      return lastIndex;
   }

   private int getIllocutionUnitIndex(int startPosition) {
      int lastIndex = 0;
      for (int i = 0; i < illocutionUnits.size(); i++) {
         IllocutionUnit iu = (IllocutionUnit) illocutionUnits.get(i);
         String g = iu.getKriterium();
         if (iu.getStartPosition() > startPosition)
            return lastIndex;
         lastIndex++;
      }
      return lastIndex;
   }

   /**
    * Wird vom DBC benötigt
    */
   public void addWord(DBC_Key key,
         int id,
         String content,
         String language,
         int startPosition) {
      key.unlock();
      int index = getTokenIndex(startPosition);
      tokens.add(index, new Word(id, this, startPosition, content, language));
      content = null;
   }

   /**
    * Wird vom DBC benötigt
    */
   public void addWord(DBC_Key handler, String content, int startPosition) {
      addWord(handler, -1, content, "DE", startPosition);
   }

   /**
    * Wird vom DBC benötigt
    */
   public void addWord(DBC_Key handler,
         String content,
         String language,
         int startPosition) {
      addWord(handler, -1, content, language, startPosition);
   }

   /**
    * Wird vom DBC benötigt
    */
   public void addSign(DBC_Key key, int id, char sign, int position) {
      key.unlock();
      if (!Character.isSpaceChar(sign)) {
         int index = getTokenIndex(position);
         tokens.add(index, new Sign(id, this, position, sign));
         content = null;
      }
   }

   /**
    * Wird vom DBC benötigt
    */
   public void addSign(DBC_Key handler, char sign, int position) {
      addSign(handler, -1, sign, position);
   }

   /**
    * Wird vom DBC benÃ¶tigt
    */
   public void addNewline(DBC_Key key, int position) {
      key.unlock();
      Integer p = new Integer(position);
      if (!paragraphs.contains(p)) {
         paragraphs.add(p);
         content = null;
      }
   }

   /**
    * Wird vom DBC benÃ¶tigt
    */
   public void addIllocutionUnit(DBC_Key key,
         int id,
         int startPosition,
         int endPosition) {
      key.unlock();
      int index = getIllocutionUnitIndex(startPosition);
      IllocutionUnit iu = new IllocutionUnit(id, this, index, startPosition, endPosition);
      illocutionUnits.add(index, iu);
   }

   /**
    * Wird vom DBC benÃ¶tigt
    */
   public void addIllocutionUnit(DBC_Key key,
         int id,
         int startPosition,
         int endPosition,
         String kriterium) {
      key.unlock();
      int index = getIllocutionUnitIndex(startPosition);
      IllocutionUnit iu = new IllocutionUnit(id, this, index, startPosition, endPosition, kriterium);
      illocutionUnits.add(index, iu);
   }
   
   /**
    * Wird vom DBC benÃ¶tigt
    */
   public void addIllocutionUnit(DBC_Key handler,
         int startPosition,
         int endPosition) {
      addIllocutionUnit(handler, -1, startPosition, endPosition, "");
   }
   
   /**
    * Wird vom DBC benÃ¶tigt
    */
   public void addIllocutionUnit(DBC_Key handler,
         int startPosition,
         int endPosition,
         String kriterium) {
      addIllocutionUnit(handler, -1, startPosition, endPosition, kriterium);
   }

   /**
    * Keine Ahnung was das soll! Der Index wird in der Tabelle gespeichert und wohl zum Unterscheiden der Kapitel benutzt.
    */
   public void calculateIndicies(DBC_Key key) {
      key.unlock();
      for (int i = 0; i < tokens.size(); i++) {
         Token t = (Token) tokens.get(i);
         t.setIndex(i);
         t.setIllocutionUnit(getIllocutionUnit(t));
         if (t instanceof Word)
            cacheWord((Word) t);
      }

      int lastIU = 0;
      int paragraphIndex = 0;
      for (; paragraphIndex < paragraphs.size(); paragraphIndex++) {
         int paragraph = ((Integer) paragraphs.get(paragraphIndex)).intValue();

         for (int i = 0; lastIU < illocutionUnits.size(); i++) {
            IllocutionUnit iu = (IllocutionUnit) illocutionUnits.get(lastIU);
            if (iu.getEndPosition() <= paragraph) {
               iu.setParagraphPosition(paragraphIndex, i);
               lastIU++;
            }
            else
               break;
         }
      }

      for (int i = 0; lastIU < illocutionUnits.size(); i++, lastIU++) {
         IllocutionUnit iu = (IllocutionUnit) illocutionUnits.get(lastIU);
         iu.setParagraphPosition(paragraphIndex, i);
      }
   }

   private void cacheWord(Word word) {
      String key = word.getContent().toLowerCase();
      if (wordCache.containsKey(key)) {
         Vector words = (Vector) wordCache.get(key);
         words.add(word);
      }
      else {
         Vector words = new Vector();
         words.add(word);
         wordCache.put(key, words);
      }
   }

   /**
    * Gibt alle Wörter des Kapitels zurück, die den gleichen Content wie das
    * übergebene Wort haben. Groß/Kleinschreibung spielt dabei keine Rolle.
    * 
    * @param word
    *        Das Wort, dessen Content alle gefundenen Wörter haben sollen
    * @return Ein Vektor mit allen Wörtern mit dem gleichen Content
    */
   public Vector getWords(Word word) {
      return getWords(word.getContent());
   }

   /**
    * Gibt alle Wörter des Kapitels zurück, die den gleichen Content haben.
    * Groß/Kleinschreibung spielt dabei keine Rolle.
    * 
    * @param content
    *        Der Content, denn alle gefundenen Wörter haben sollen
    * @return Ein Vektor mit allen Wörtern mit dem gleichen Content
    */
   public Vector getWords(String content) {
      String key = content.toLowerCase();
      Object o = wordCache.get(key);
      if (o == null)
         return null;
      return (Vector) o;
   }

   /**
    * Gibt alle Wörter des Kapitels zurück, die den gleichen Content haben.
    * 
    * @param content
    *        Der Content, denn alle gefundenen Wörter haben sollen
    * @param ignoreCase
    *        gibt an, ob de Groß/Kleinschreibung ignortiert werden soll.
    * @return Ein Vektor mit allen Wörtern mit dem gleichen Content
    */
   public Vector getWords(String content, boolean ignoreCase) {
      Object o = wordCache.get(content.toLowerCase());
      if (o == null)
         return new Vector();
      if (ignoreCase)
         return (Vector) o;

      Vector res = new Vector();
      Vector data = (Vector) o;
      for (int i = 0; i < data.size(); i++) {
         Word w = (Word) data.get(i);
         if (w.getContent().equals(content))
            res.add(w);
      }
      return res;
   }

   public Word getWordWithID(int id) {
      // nur lineare suche möglich, da Wörter nicht nach IDs geordnet sind
      for (int i = 0; i < tokens.size(); i++) {
         Object o = tokens.get(i);

         if (o instanceof Word) {
            Word word = (Word) o;
            if (word.getDB_ID() == id)
               return word;
         }
      }

      return null;
   }

   public String toString() {
      return getContent();
   }

   /**
    * Gibt das komplette Kapitel als String zurÃ¼ck, einschlieÃŸlich der
    * ZeilenumbrÃ¼che.
    */
   public String getContent() {
      if (content != null)
         return content;

      StringBuffer sb = new StringBuffer();
      int currentParagraph = 0;
      int currentPosition = 0;

      for (int i = 0; i < tokens.size(); i++) {
         Token t = (Token) tokens.get(i);

         int nextParagraph = getNextParagraph(t.getStartPosition());
         if (nextParagraph > currentParagraph) {
            currentParagraph = nextParagraph;
            sb.append('\n');
            currentPosition++;
         }

         sb.append(makeBlanks(currentPosition, t.getStartPosition()));
         sb.append(t.getContent());
         currentPosition = t.getEndPosition();
      }

      content = sb.toString();
      return content;
   }

   /**
    * Gibt einen Ausschnitt aus dem Kapitel als String zurÃ¼ck. ZeilenumbrÃ¼che
    * werden nicht beachtet.
    * 
    * @param start
    *        Der Token, der den Ausschnitt einleitet
    * @param end
    *        Der Token, der den Ausschnitt beendet
    */
   public String getContent(Token start, Token end) {
      if (start == null || end == null)
         return null;

      StringBuffer sb = new StringBuffer();
      int currentPosition = start.getEndPosition();

      for (int i = start.getIndex(); i <= end.getIndex(); i++) {
         Token t = (Token) tokens.get(i);
         sb.append(makeBlanks(currentPosition, t.getStartPosition()));
         sb.append(t.getContent());
         currentPosition = t.getEndPosition();
      }

      return sb.toString();
   }

   /**
    * Gibt einen Ausschnitt aus dem Kapitel als String zurÃ¼ck. ZeilenumbrÃ¼che
    * werden nicht beachtet.
    * 
    * @param start
    *        Die Zeichenposition, die den Ausschnitt einleitet
    * @param end
    *        Die Zeichenposition, die den Ausschnitt beendet
    */
   public String getContent(int start, int end) {
      if (start > end) {
         int temp = start;
         start = end;
         end = temp;
      }
      Token s = getTokenAtPosition(start);
      Token e = getTokenAtPosition(end);
      int si = start;
      int ei = end;

      while (s == null) {
         s = getTokenAtPosition(--si);
         if (start - si > 10)
            break;
      }
      while (e == null) {
         e = getTokenAtPosition(--ei);
         if (ei - end > 10)
            break;
      }

      if (s != null && e != null) {
    	  String res = getContent(s, e);
          start = Math.max(0, start - s.getStartPosition());
          end = Math.min(res.length(), end - s.getStartPosition() + 1);
          return res.substring(start, end);
      }
      else {
    	  Logger.global.severe("Eins der beiden Token konnte nicht gefunden werden.");
    	  return "";
      }
   }

   private int getNextParagraph(int pos) {
      int lastParagraph = 0;
      for (int i = 0; i < paragraphs.size(); i++) {
         int p = ((Integer) paragraphs.get(i)).intValue();
         if (p > pos)
            return lastParagraph;
         lastParagraph = p;
      }
      return lastParagraph;
   }

   private static String makeBlanks(int lastPos, int startPos) {
      StringBuffer blanks = new StringBuffer();
      for (int i = lastPos; i < startPos - 1; i++)
         blanks.append(' ');
      return blanks.toString();
   }

   /**
    * Alle WÃ¶rter des Kapitels in einem Vektor.
    * 
    * @see Word
    */
   public Vector<Word> getWords() {
      Vector<Word> words = new Vector<Word>((int) (tokens.size() * 0.9));
      for (int i = 0; i < tokens.size(); i++) {
         Token t = (Token) tokens.get(i);
         if (t instanceof Word)
            words.add( (Word) t);
      }
      return words;
   }

   /**
    * Alle Satzzeichen des Kapitels in einem Vektor.
    * 
    * @see Sign
    */
   public Vector getSigns() {
      Vector signs = new Vector((int) (tokens.size() * 0.9));
      for (int i = 0; i < tokens.size(); i++) {
         Token t = (Token) tokens.get(i);
         if (t instanceof Sign)
            signs.add(t);
      }
      return signs;
   }

   /**
    * Die Zeichenpositionen der ZeilenumbrÃ¼che.
    */
   public Vector getParagraphs() {
      return paragraphs;
   }

   /**
    * Der Titel von diesem Kapitel
    */
   public String getTitle() {
      return title;
   }

   /**
    * Die Datenbank-ID des Buches, in dem das Kapitel steht.
    */
   public int getBookID() {
      return bookID;
   }

   /**
    * Wird vom DBC benÃ¶tigt.
    */
   public void setBookID(DBC_Key key, int id) {
      key.unlock();
      bookID = id;
   }

   /**
    * Gibt den Token an diesem Index zurÃ¼ck.
    * 
    * @param index
    *        der Index (ZÃ¤hlung beginnt bei 0)
    * @return den Token, oder null bei einem ungÃ¼ltigen Index.
    */
   public Token getTokenAtIndex(int index) {
      try {
         return (Token) tokens.get(index);
      }
      catch (Exception e) {
         return null;
      }
   }

   /**
    * Gibt den Token an dieser Zeichenposition zurück
    * 
    * @param position
    *        die Zeichenposition
    * @return den Token, falls an dieser Stelle einer steht. Bei ungültiger
    *         Position oder einem Leerzeichen wird <code>null</code> zurükgegeben.
    */
   public Token getTokenAtPosition(int position) {
      int start = 0;
      int end = tokens.size();
      int middle;

      while (end - start > 5) {
         middle = start + ((end - start) >> 1);
         Token token = getTokenAtIndex(middle);
         int c = token.comparePosition(position);

         // davor
         if (c < 0)
            end = middle;

         // dahinter
         else if (c > 0)
            start = middle;

         // Treffer
         else
            return token;
      }

      for (int i = start; i < end; i++) {
         Token token = getTokenAtIndex(i);
         if (token.containsPosition(position))
            return token;
      }
      return null;
   }

   /**
    * Gibt all die Tokens zurÃ¼ck, die in dem Intervall liegen, das durch den
    * Start- und End-Token abgedeckt wird
    * 
    * @param startToken
    *        der Begin des Intervalls
    * @param endToken
    *        Das Ende des Intervalls
    * @return ein int-Array mit den Indizes.
    */
   public int[] getIndexSequence(Token startToken, Token endToken) {
      Vector tokens = getTokenSequence(startToken, endToken);
      if (tokens != null) {
         int[] res = new int[tokens.size()];
         for (int i = 0; i < tokens.size(); i++) {
            Token t = (Token) tokens.get(i);
            res[i] = t.getIndex();
         }
         return res;
      }
      return null;
   }

   /**
    * Gibt die Indizes der Tokens zurÃ¼ck, die durch das Intervall ebgedeckt
    * werden.
    * 
    * @param start
    *        der Begin des Intervalls (Zeichenposition)
    * @param end
    *        das Ende des Intervalls (Zeichenposition)
    * @return ein int-Array mit den Indizes.
    */
   public int[] getIndexSequence(int start, int end) {

      Token startToken = getTokenAtPosition(start);
      Token endToken = getTokenAtPosition(end);

      // falls start auf ein Leerzeichen zeigt,
      // gehe eins weiter nach rechts
      if (startToken == null)
         startToken = getTokenAtPosition(start + 1);
      // falls end auf ein Leerzeichen zeigt,
      // gehe eins weiter nach links
      if (endToken == null)
         endToken = getTokenAtPosition(end - 1);

      return getIndexSequence(startToken, endToken);
   }

   /**
    * Gibt all die Tokens zurÃ¼ck, die in dem Intervall liegen, das durch den
    * Start- und End-Token abgedeckt wird
    * 
    * @param startToken
    *        der Begin des Intervalls
    * @param endToken
    *        Das Ende des Intervalls
    * @return ein Vektor mit den entsprechenden Tokens.
    */
   public Vector getTokenSequence(Token startToken, Token endToken) {
      if (startToken == null || endToken == null)
         return null;

      Token t = startToken;
      Vector res = new Vector();

      while (t != endToken) {
         res.add(t);
         t = t.getNextToken();
      }

      res.add(endToken);
      return res;
   }

   /**
    * Gibt all die Tokens in einem Vektor zurÃ¼ck, die in dem durch start und
    * end festgelegten Intervall liegen. Zeigt start oder end auf ein
    * Leerzeichen, wird automatisch start oder end verschoben.
    * 
    * @param start
    *        der Begin des Intervalls, auf Zeichenbasis. start sollte kleiner
    *        als end sein. ;-)
    * @param end
    *        das Ende des Intervalls, auf Zeichenbasis.
    * @return ein Vektor mit allen Tokens, die von dem Intervall "berÃ¼hrt"
    *         werden, oder null, falls start oder end ungÃ¼ltig sind.
    * @see Token
    */
   public Vector<Token> getTokenSequence(int start, int end) {
      Token s = getTokenAtPosition(start);
      Token e = getTokenAtPosition(end);
      int si = start;
      int ei = end;

      while (s == null) {
         s = getTokenAtPosition(++si);// liegts an der neuen (Stand Anfang 2009) Segmentierung? altes war: s = getTokenAtPosition(--si);
									  // also jetzt suchen wir vorwärts statt rückwärts
         if (start - si > 10)
            break;
      }
      while (e == null) {
	  e = getTokenAtPosition(--ei); // liegts an der neuen (Stand Anfang 2009) Segmentierung? altes war: e = getTokenAtPosition(++ei);
	  								// also jetzt suchen wir rückwärts statt vorwärts
         if (ei - end > 10)
            break;
      }

      return getTokenSequence(s, e);
   }

   /**
    * Alle Tokens dieses Kapitels in einem Vektor.
    * 
    * @see Token
    */
   public Vector getTokens() {
      return tokens;
   }

   /**
    * Gibt die Ã„uÃŸerungseinheit mit diesem Index zurÃ¼ck
    * 
    * @param index
    *        der Index einer Ã„uÃŸerungseinheit
    * @return Die Ã„uÃŸerungseinheit oder null bei ungÃ¼ltigen Index.
    */
   public IllocutionUnit getIllocutionUnitAtIndex(int index) {
      try {
         return (IllocutionUnit) illocutionUnits.get(index);
      }
      catch (Exception e) {
         return null;
      }
   }
   
   public int getIndexOfIllocutionUnit(IllocutionUnit iu)
   {
	   return illocutionUnits.indexOf(iu);
   }

   /**
    * Gibt die Ã„uÃŸerungseinheit zurÃ¼ck, die diese Zeichenposition beinhaltet
    * 
    * @param position
    *        eine Zeichenposition
    * @return Die Ã„uÃŸerungseinheit, die diese Zeichenposition beinhaltet oder
    *         null, falls zu dieser Postion keine Ã„uÃŸerungseinheit gespeichert
    *         ist
    */
   public IllocutionUnit getIllocutionUnitAtPosition(int position) {
      int start = 0;
      int end = illocutionUnits.size();
      int middle;

      while (end - start > 5) {
         middle = start + ((end - start) >> 1);
         IllocutionUnit iu = getIllocutionUnitAtIndex(middle);
         int c = iu.comparePosition(position);

         // davor
         if (c < 0)
            end = middle;

         // dahinter
         else if (c > 0)
            start = middle;

         // Treffer
         else
            return iu;
      }

      for (int i = start; i < end; i++) {
         IllocutionUnit iu = getIllocutionUnitAtIndex(i);
         if (iu.containsPosition(position))
            return iu;
      }
      return null;
   }

   /**
    * Gibt die Ã„uÃŸerungseinheit zurÃ¼ck, die in dem Absatz an besagter Stelle
    * zu finden ist
    * 
    * @param paragraph
    *        Der Index des Absatz. Der erste Absatz hat Index 0.
    * @param index
    *        Die relative Position innerhalb des Absatz. Die erste
    *        Ã„uÃŸerungseinheit eines Absatz hat Index 0.
    * @return Die Ã„uÃŸerungseinheit oder null bei falschen Angaben.
    */
   public IllocutionUnit getIllocutionUnitAtParagraph(int paragraph, int index) {
      for (int i = 0; i < illocutionUnits.size(); i++) {
         IllocutionUnit iu = (IllocutionUnit) illocutionUnits.get(i);
         if (iu.getParagraph() > paragraph)
            break;
         if (iu.getParagraph() == paragraph && iu.getParagraphIndex() == index)
            return iu;
      }
      return null;
   }

   /**
    * Gibt die Ã„uÃŸerungseinheit zurÃ¼ck, die diesen Token beinhaltet.
    * 
    * @param token
    *        der Token, dessen Ã„uÃŸerungseinheit gefunden werden solll
    * @return Die Ã„uÃŸerungseinheit oder null, falls keine zu diesem Token
    *         gepeichert wurde.
    */
   public IllocutionUnit getIllocutionUnit(Token token) {
      int start = 0;
      int end = illocutionUnits.size();
      int middle;

      while (end - start > 5) {
         middle = start + ((end - start) >> 1);
         IllocutionUnit iu = (IllocutionUnit) illocutionUnits.get(middle);
         int c = iu.compare(token);

         // davor
         if (c < 0)
            end = middle;

         // dahinter
         else if (c > 0)
            start = middle;

         // Treffer
         else
            return iu;
      }

      for (int i = start; i < end; i++) {
         IllocutionUnit iu = (IllocutionUnit) illocutionUnits.get(i);
         if (iu.compare(token) == 0)
            return iu;
      }
      return null;
   }

   /**
    * Gibt die Ã„uÃŸerungseinheit zurÃ¼ck, die diese ID (vergeben von der
    * Datenbank) hat.
    * 
    * @param id
    *        die Datenbank-ID
    * @return die entsprechende Ã„uÃŸerungseinheit oder null, falls es keine mit
    *         dieser ID gibt.
    */
   public IllocutionUnit getIllocutionUnitWithID(int id) {
      // nur lineares Suchen mÃ¶glich, da nicht sicher ist,
      // ob AEE's (oder deren ID's) sortiert gespeichert werden.
      for (int i = 0; i < illocutionUnits.size(); i++) {
         IllocutionUnit iu = illocutionUnits.get(i);
         if (iu.getDB_ID() == id)
            return iu;
      }
      return null;
   }

   /**
    * Alle Aeusserungseinheiten dieses Kapitels in einem Vektor.
    * 
    */
   public Vector<IllocutionUnit> getIllocutionUnits() {
      return illocutionUnits;
   }

   /**
    * Gibt alle Ã„uÃŸerungseinheiten zurÃ¼ck, die von dem Intervall start - end
    * abgedeckt werden.
    * 
    * @param start
    *        die Startposition des Intervalls, bezogen auf die Zeichenposition.
    * @param end
    *        die Endposition des Intervalls, bezogen auf die Zeichenposition.
    * @return ein Vektor von Ã„uÃŸerungseinheiten.
    * @see de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnit
    */
   public Vector<IllocutionUnit> getIllocutionUnitsFromPositions(int start, int end) {
      Vector<IllocutionUnit> res = new Vector<IllocutionUnit>();
      IllocutionUnit startIU = getIllocutionUnitAtPosition(start);
      IllocutionUnit endIU = getIllocutionUnitAtPosition(end);
      IllocutionUnit a = startIU;

      while (a != endIU) {
         res.add(a);
         a = a.getNextIllocutionUnit();
      }

      res.add(endIU);
      return res;
   }

   /**
    * Gibt alle Ã„uÃŸerungseinheiten zurÃ¼ck, die von dem Intervall abgedeckt
    * werden.
    * 
    * @param start
    *        der Index der ersten Ã„uÃŸerungseinheit des Intervalls
    * @param end
    *        der Index der letzten Ã„uÃŸerungseinheit des Intervalls
    * @return ein Vector von Ã„uÃŸerungseinheiten.
    * @see de.uni_tuebingen.wsi.ct.slang2.dbc.data.IllocutionUnit
    */
   public Vector getIllocutionUnitsFromIndex(int start, int end) {
      Vector res = new Vector();
      IllocutionUnit startIU = getIllocutionUnitAtIndex(start);
      IllocutionUnit endIU = getIllocutionUnitAtIndex(end);
      IllocutionUnit a = startIU;

      while (a != endIU) {
         res.add(a);
         a = a.getNextIllocutionUnit();
      }

      res.add(endIU);
      return res;
   }

   /**
    * Wird vom DBC benÃ¶tigt
    */
   public void updateIDs(DBC_Key key, Chapter answer) {
      key.unlock();

      setDB_ID(key, answer.getDB_ID());

      for (int i = 0; i < tokens.size(); i++) {
         Token t1 = (Token) tokens.get(i);
         Token t2 = (Token) answer.tokens.get(i);
         t1.setDB_ID(key, t2.getDB_ID());
      }

      for (int i = 0; i < illocutionUnits.size(); i++) {
         IllocutionUnit iu1 = (IllocutionUnit) illocutionUnits.get(i);
         IllocutionUnit iu2 = (IllocutionUnit) answer.illocutionUnits.get(i);
         iu1.setDB_ID(key, iu2.getDB_ID());
      }
   }

   /**
    * Gibt den Index des Absatzes zurÃ¼ck, der die Zeichenposition abdeckt.
    * 
    * @param position
    *        die Zeichenposition
    * @return Der Index des Absatzes. Die Zahlung beginnt bei 0.
    */
   public int getParagraphIndex(int position) {
      int last = 0;
      for (int i = 0; i < paragraphs.size(); i++) {
         int paragraph = ((Integer) paragraphs.get(i)).intValue();
         if (paragraph > position)
            return last;
         last++;
      }
      return last;
   }

   void resetIDs() {
      id = -1;
      index += 10000;
      title = title + " (Copy)";

      for (int i = 0; i < illocutionUnits.size(); i++)
         ((IllocutionUnit) illocutionUnits.get(i)).resetID();

      for (int i = 0; i < tokens.size(); i++)
         ((Token) tokens.get(i)).resetID();

   }

public String getDate() {
	return date;
}

public void setDate(String date) {
	this.date = date;
}

}
