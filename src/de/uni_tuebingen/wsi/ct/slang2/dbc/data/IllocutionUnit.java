/*
 * Erstellt: 29.04.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Eine �u�erunsgeinheit, die wiederum mehrere Tokens (W�rter oder Satzzeichen)
 * beinhaltet. Die �u�erungseinheit kann sich dabei auch auf Teilw�rter
 * beziehen.
 * 
 * @author Volker Kl�bb
 */
public class IllocutionUnit
      implements
         IDOwner,
         CommentOwner,
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = -3250379063422262883L;
   private int               id;
   private Chapter           chapter;
   private int               index;
   private int               start;
   private int               end;
   private int               paragraph;
   private int               paragraphIndex;

   private String kriterium;
   
   /**
   * Wird vom DBC ben�tigt
    */
   IllocutionUnit(int id, Chapter chapter, int index, int start, int end) {
      this.id = id;
      this.chapter = chapter;
      this.index = index;
      this.start = start;
      this.end = end;
      this.kriterium="";
   }

   /**
    * Wird vom DBC ben�tigt
    */
   IllocutionUnit(int id, Chapter chapter, int index, int start, int end, String kriterium) {
      this.id = id;
      this.chapter = chapter;
      this.index = index;
      this.start = start;
      this.end = end;
      this.kriterium = kriterium;
   }
   /**
    * Das Kapitel, in dem diese �u�erungseinheit steht.
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Die eindeutige ID der �u�erungseinheit. Nur wichtig f�r die Datenbank
    * selbst.
    * 
    */
   public int getDB_ID() {
      return id;
   }

   /**
    * Wird vom DBC ben�tigt
    */
   public void setDB_ID(DBC_Key key, int id) {
      key.unlock();
      this.id = id;
   }

   /**
    * Liefert die Position des ersten Zeichens des ersten Tokens in dieser
    * �u�erungseinheit.
    * 
    * @see IllocutionUnit#getEndPosition()
    */
   public int getStartPosition() {
      return start;
   }

   /**
    * Liefert die Position des letzten Zeichens des letzten Tokens in dieser
    * �u�erungseinheit.
    * 
    * @see IllocutionUnit#getStartPosition()
    */
   public int getEndPosition() {
      return end;
   }
   
   /**
    * Liefert das Kriterium �ber das diese �u�erungseinheit definiert wurde.
    */
   public String getKriterium() {
      return kriterium;
   }

   /**
    * Der Index des ersten Tokens, dass diese �u�erungseinheit einleitet.
    * 
    * @see IllocutionUnit#getEndIndex()
    */
   public int getStartIndex() {
      return chapter.getTokenAtPosition(start).getIndex();
   }

   /**
    * Der Index des letzten Tokens, dass diese �u�erungseinheit beendet.
    * 
    * @see IllocutionUnit#getStartIndex()
    */
   public int getEndIndex() {
      return chapter.getTokenAtPosition(end).getIndex();
   }

   /**
    * Gibt den ersten Token dieser �u�erungseinheit zur�ck.
    * 
    * @return der erste Token dieser �u�erungseinheit
    * @see IllocutionUnit#getLastToken()
    */
   public Token getFirstToken() {
      return chapter.getTokenAtPosition(start);
   }

   /**
    * Gibt den letzten Token dieser �u�erungseinheit zur�ck.
    * 
    * @return der letzte Token von dieser �u�erungseinheit.
    * @see IllocutionUnit#getFirstToken()
    */
//   public Token getLastToken_ALT() {
//      return chapter.getTokenAtPosition(end);
//   }
   
   /**
    * Gibt den letzten Token dieser �u�erungseinheit zur�ck.
    * ACHTUNG: Oben: alte Variante ohne - 1.
    * -1 f�r dialog-tool eingebaut. wenn keine Probleme auftreten kann obige 
    * Methode gel�scht werden.
    * @return der letzte Token von dieser �u�erungseinheit.
    * @see IllocutionUnit#getFirstToken()
    */
   public Token getLastToken() {
      return chapter.getTokenAtPosition(end-1);
   }

   /**
    * Testet auf Gleichheit. Kriterium ist dabei der Index der �u�erungseinheit.
    */
   public boolean equals(Object o) {
      if (o instanceof IllocutionUnit)
         return index == ((IllocutionUnit) o).index;
      return false;
   }

   /**
    * Die Gr��e dieser �u�erungseinheit. Berechnet sich aus Endposition minus
    * Startposition, beides bezogen auf Zeichenpositionen.
    */
   public int size() {
      return end - start;
   }

   /**
    * Die Anzahl der Tokens, die von dieser �u�erungseinheit eingeschlossen
    * werden.
    */
   public int countTokens() {
      return getTokens().size();
   }

   /**
    * Gibt den Index der �u�erungseinheit innerhalb des Kapitels zur�ck
    * 
    * @return den Index der �u�erungseinheit, angefangen bei 0.
    */
   public int getIndex() {
      return index;
   }

   /**
    * Gibt alle Tokens dieser �u�erungseinheit in einem Vektor zur�ck.
    * 
    */
   public Vector<Token> getTokens() {
      return chapter.getTokenSequence(start, end);
   }

   /**
    * Gibt die n�chste �u�erungseinheit zur�ck
    * 
    * @return die n�chste �u�erungseinheit oder null, falls dies schon die
    *         letzte �u�erungseinheit von diesem Kapitel ist.
    * @see IllocutionUnit#getPreviousIllocutionUnit()
    */
   public IllocutionUnit getNextIllocutionUnit() {
      return chapter.getIllocutionUnitAtIndex(index + 1);
   }

   /**
    * Gibt die vorherige �u�erungseinheit zur�ck.
    * 
    * @return die �u�erungseinheit vor dieser, oder null, falls diese
    *         �u�erungseinheit die erste in diesem Kapitel ist.
    * @see IllocutionUnit#getNextIllocutionUnit()
    */
   public IllocutionUnit getPreviousIllocutionUnit() {
      return chapter.getIllocutionUnitAtIndex(index - 1);
   }

   /**
    * �berpr�ft, ob diese �u�erungseinheit die Zeichenposition abdeckt. Dabei
    * kann eine Ungenauigkeit mit �bergeben werden.
    * 
    * @param position
    *        die Position auf Zeichenbasis
    * @param delta
    *        die Ungenauigkeit
    * @return true, falls diese �u�erunsgeinheit mit einer Ungenauigkeit von
    *         plus/minus delta die Position abdeckt. Sonst false.
    */
   public boolean containsPosition(int position, int delta) {
      return (start - delta) <= position && position <= (end + delta);
   }

   /**
    * �berpr�ft, ob diese �u�erungseinheit die Zeichenposition abdeckt.
    * 
    * @param position
    *        die zu pr�fende Position (bezogen auf Zeichen)
    * @return true, falls gilt start <= position <= end, sonst false
    */
   public boolean containsPosition(int position) {
      return containsPosition(position, 0);
   }

   /**
    * Gibt -1, 0 oder 1 zur�ck, je nachdem, ob die Zeichenposition vor dieser
    * �u�erungseinheit, in dieser �u�erungseinheit oder danach ist.
    * 
    * @param position
    *        die Position, bezogen auf Zeichen
    * @return -1, wenn die Position vor der �u�erungseinheit ist. <br>
    *         0, wenn die Position in der �u�erungseinheit ist. <br>
    *         1, wenn die Position hinter der �u�erungseinheit ist.
    */
   public int comparePosition(int position) {
      if (position < start)
         return -1;
      else if (position > end)
         return 1;
      return 0;
   }

   /**
    * Gibt den Token an dieser Zeichenposition zur�ck.
    * 
    * @param position
    *        die Position in Bezug auf Zeichenbasis
    * @return den Token, falls die Position in dieser �u�erungseinheit liegt.
    *         Sonst null. Zeigt die Position auf ein Leerzeichen, wird auch null
    *         zur�ckgegeben.
    */
   public Token getTokenAtPosition(int position) {
      if (containsPosition(position)) {
         return chapter.getTokenAtPosition(position);
      }
      return null;
   }

   /**
    * Gibt den Token an der realtiven Position bezogen auf diese
    * �u�erungseinheit zur�ck. (Relative Position entsprechen dem Index der
    * Tokens innerhalb einer �u�erungseinheit.)
    * 
    * @param position
    *        die relative Position, angefangen bei 0.
    * @return den Token, oder null bei einer ung�ltigen Position.
    */
   public Token getTokenAtRelativeIndex(int position) {
      try {
         return chapter.getTokenAtIndex(position + getStartIndex());
      }
      catch (Exception e) {
         return null;
      }
   }

   /**
    * Gibt den Token von diesem Index zur�ck.
    * 
    * @param index
    *        der Index
    * @return den Token oder null, falls diese �u�erungseinheit diese absoluten
    *         Position nicht abdeckt.
    */
   public Token getTokenAtIndex(int index) {
      if (containsIndex(index))
         return chapter.getTokenAtIndex(index);
      return null;
   }

   /**
    * �berpr�ft, ob dieser Token-Index von dieser �u�erungseinheit abgedeckt
    * wird.
    * 
    * @param index
    *        der Index eines Token
    */
   public boolean containsIndex(int index) {
      return getStartIndex() <= index && index <= getEndIndex();
   }

   /**
    * Gibt -1, 0 oder 1 zur�ck, je nachdem ob der Token-Index vor, in oder nach
    * dieser �u�erungseinheit liegt.
    * 
    * @param index
    *        der Index eines Token
    * @return -1, falls der Index vor dieser �u�erungseinheit liegt. <br>
    *         0, falls der Index durch diese �u�erungseinheit abgedeckt wird.
    *         <br>
    *         1, falls der Index hinter dieser �u�erungseinheit liegt.
    */
   public int compareIndex(int index) {
      if (index < getStartIndex())
         return -1;
      else if (index > getEndIndex())
         return 1;
      return 0;
   }

   /**
    * Gibt -1, 0 oder 1 zur�ck, je nachdem ob der Token vor, in oder nach dieser
    * �u�erungseinheit liegt.
    * 
    * @param token
    *        der zu pr�fende Token
    * @return -1, falls der Token vor dieser �u�erungseinheit liegt. <br>
    *         0, falls der Token in dieser �u�erungseinheit liegt. <br>
    *         1, falls der Token hinter dieser �u�erungseinheit liegt.
    */
   public int compare(Token token) {
      return comparePosition(token.getStartPosition());
   }

   /**
    * Gibt -1, 0 oder 1 zur�ck, je nachdem ob die �bergebene �u�erungseinheit
    * vor, in oder nach dieser �u�erungseinheit liegt.
    * 
    * @param iu
    *        die zu pr�fende �u�erungseinheit
    * @return -1, falls die zu pr�fende �u�erungseinheit vor dieser
    *         �u�erungseinheit liegt. <br>
    *         0, falls die zu pr�fende �u�erungseinheit gleich dieser
    *         �u�erungseinheit ist. <br>
    *         1, falls die zu pr�fende �u�erungseinheit hinter dieser
    *         �u�erungseinheit liegt.
    */
   public int compare(IllocutionUnit iu) {
      if (index > iu.index)
         return -1;
      if (index < iu.index)
         return 1;
      return 0;
   }

   public String toString() {
      return chapter.getContent(start, end);
   }

   void setParagraphPosition(int paragraph, int index) {
      this.paragraph = paragraph;
      this.paragraphIndex = index;
   }

   /**
    * Gibt den Index des Absatzs zur�ck, in dem diese �u�erungseinheit steht.
    * Der Erste Absatz hat Index 0.
    */
   public int getParagraph() {
      return paragraph;
   }

   /**
    * Gibt die relative Position der �u�erungseinheit in diesem Absatz zur�ck.
    * Die erste �u�erungseinheit steht an Index 0.
    */
   public int getParagraphIndex() {
      return paragraphIndex;
   }

   public int getClassCode() {
      return Comments.CLASS_CODE_ILLOCUTION_UNIT;
   }
   
   void resetID() {
      id = -1;
   }
}
