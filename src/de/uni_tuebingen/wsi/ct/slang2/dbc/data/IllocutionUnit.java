/*
 * Erstellt: 29.04.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Eine Äußerunsgeinheit, die wiederum mehrere Tokens (Wörter oder Satzzeichen)
 * beinhaltet. Die Äußerungseinheit kann sich dabei auch auf Teilwörter
 * beziehen.
 * 
 * @author Volker Klöbb
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
   * Wird vom DBC benötigt
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
    * Wird vom DBC benötigt
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
    * Das Kapitel, in dem diese Äußerungseinheit steht.
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Die eindeutige ID der Äußerungseinheit. Nur wichtig für die Datenbank
    * selbst.
    * 
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
    * Liefert die Position des ersten Zeichens des ersten Tokens in dieser
    * Äußerungseinheit.
    * 
    * @see IllocutionUnit#getEndPosition()
    */
   public int getStartPosition() {
      return start;
   }

   /**
    * Liefert die Position des letzten Zeichens des letzten Tokens in dieser
    * Äußerungseinheit.
    * 
    * @see IllocutionUnit#getStartPosition()
    */
   public int getEndPosition() {
      return end;
   }
   
   /**
    * Liefert das Kriterium über das diese Äußerungseinheit definiert wurde.
    */
   public String getKriterium() {
      return kriterium;
   }

   /**
    * Der Index des ersten Tokens, dass diese Äußerungseinheit einleitet.
    * 
    * @see IllocutionUnit#getEndIndex()
    */
   public int getStartIndex() {
      return chapter.getTokenAtPosition(start).getIndex();
   }

   /**
    * Der Index des letzten Tokens, dass diese Äußerungseinheit beendet.
    * 
    * @see IllocutionUnit#getStartIndex()
    */
   public int getEndIndex() {
      return chapter.getTokenAtPosition(end).getIndex();
   }

   /**
    * Gibt den ersten Token dieser Äußerungseinheit zurück.
    * 
    * @return der erste Token dieser Äußerungseinheit
    * @see IllocutionUnit#getLastToken()
    */
   public Token getFirstToken() {
      return chapter.getTokenAtPosition(start);
   }

   /**
    * Gibt den letzten Token dieser Äußerungseinheit zurück.
    * 
    * @return der letzte Token von dieser Äußerungseinheit.
    * @see IllocutionUnit#getFirstToken()
    */
//   public Token getLastToken_ALT() {
//      return chapter.getTokenAtPosition(end);
//   }
   
   /**
    * Gibt den letzten Token dieser Äußerungseinheit zurück.
    * ACHTUNG: Oben: alte Variante ohne - 1.
    * -1 für dialog-tool eingebaut. wenn keine Probleme auftreten kann obige 
    * Methode gelöscht werden.
    * @return der letzte Token von dieser Äußerungseinheit.
    * @see IllocutionUnit#getFirstToken()
    */
   public Token getLastToken() {
      return chapter.getTokenAtPosition(end-1);
   }

   /**
    * Testet auf Gleichheit. Kriterium ist dabei der Index der Äußerungseinheit.
    */
   public boolean equals(Object o) {
      if (o instanceof IllocutionUnit)
         return index == ((IllocutionUnit) o).index;
      return false;
   }

   /**
    * Die Größe dieser Äußerungseinheit. Berechnet sich aus Endposition minus
    * Startposition, beides bezogen auf Zeichenpositionen.
    */
   public int size() {
      return end - start;
   }

   /**
    * Die Anzahl der Tokens, die von dieser Äußerungseinheit eingeschlossen
    * werden.
    */
   public int countTokens() {
      return getTokens().size();
   }

   /**
    * Gibt den Index der Äußerungseinheit innerhalb des Kapitels zurück
    * 
    * @return den Index der Äußerungseinheit, angefangen bei 0.
    */
   public int getIndex() {
      return index;
   }

   /**
    * Gibt alle Tokens dieser Äußerungseinheit in einem Vektor zurück.
    * 
    */
   public Vector<Token> getTokens() {
      return chapter.getTokenSequence(start, end);
   }

   /**
    * Gibt die nächste Äußerungseinheit zurück
    * 
    * @return die nächste Äußerungseinheit oder null, falls dies schon die
    *         letzte Äußerungseinheit von diesem Kapitel ist.
    * @see IllocutionUnit#getPreviousIllocutionUnit()
    */
   public IllocutionUnit getNextIllocutionUnit() {
      return chapter.getIllocutionUnitAtIndex(index + 1);
   }

   /**
    * Gibt die vorherige Äußerungseinheit zurück.
    * 
    * @return die Äußerungseinheit vor dieser, oder null, falls diese
    *         Äußerungseinheit die erste in diesem Kapitel ist.
    * @see IllocutionUnit#getNextIllocutionUnit()
    */
   public IllocutionUnit getPreviousIllocutionUnit() {
      return chapter.getIllocutionUnitAtIndex(index - 1);
   }

   /**
    * Überprüft, ob diese Äußerungseinheit die Zeichenposition abdeckt. Dabei
    * kann eine Ungenauigkeit mit übergeben werden.
    * 
    * @param position
    *        die Position auf Zeichenbasis
    * @param delta
    *        die Ungenauigkeit
    * @return true, falls diese Äußerunsgeinheit mit einer Ungenauigkeit von
    *         plus/minus delta die Position abdeckt. Sonst false.
    */
   public boolean containsPosition(int position, int delta) {
      return (start - delta) <= position && position <= (end + delta);
   }

   /**
    * Überprüft, ob diese Äußerungseinheit die Zeichenposition abdeckt.
    * 
    * @param position
    *        die zu prüfende Position (bezogen auf Zeichen)
    * @return true, falls gilt start <= position <= end, sonst false
    */
   public boolean containsPosition(int position) {
      return containsPosition(position, 0);
   }

   /**
    * Gibt -1, 0 oder 1 zurück, je nachdem, ob die Zeichenposition vor dieser
    * Äußerungseinheit, in dieser Äußerungseinheit oder danach ist.
    * 
    * @param position
    *        die Position, bezogen auf Zeichen
    * @return -1, wenn die Position vor der Äußerungseinheit ist. <br>
    *         0, wenn die Position in der Äußerungseinheit ist. <br>
    *         1, wenn die Position hinter der Äußerungseinheit ist.
    */
   public int comparePosition(int position) {
      if (position < start)
         return -1;
      else if (position > end)
         return 1;
      return 0;
   }

   /**
    * Gibt den Token an dieser Zeichenposition zurück.
    * 
    * @param position
    *        die Position in Bezug auf Zeichenbasis
    * @return den Token, falls die Position in dieser Äußerungseinheit liegt.
    *         Sonst null. Zeigt die Position auf ein Leerzeichen, wird auch null
    *         zurückgegeben.
    */
   public Token getTokenAtPosition(int position) {
      if (containsPosition(position)) {
         return chapter.getTokenAtPosition(position);
      }
      return null;
   }

   /**
    * Gibt den Token an der realtiven Position bezogen auf diese
    * Äußerungseinheit zurück. (Relative Position entsprechen dem Index der
    * Tokens innerhalb einer Äußerungseinheit.)
    * 
    * @param position
    *        die relative Position, angefangen bei 0.
    * @return den Token, oder null bei einer ungültigen Position.
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
    * Gibt den Token von diesem Index zurück.
    * 
    * @param index
    *        der Index
    * @return den Token oder null, falls diese Äußerungseinheit diese absoluten
    *         Position nicht abdeckt.
    */
   public Token getTokenAtIndex(int index) {
      if (containsIndex(index))
         return chapter.getTokenAtIndex(index);
      return null;
   }

   /**
    * Überprüft, ob dieser Token-Index von dieser Äußerungseinheit abgedeckt
    * wird.
    * 
    * @param index
    *        der Index eines Token
    */
   public boolean containsIndex(int index) {
      return getStartIndex() <= index && index <= getEndIndex();
   }

   /**
    * Gibt -1, 0 oder 1 zurück, je nachdem ob der Token-Index vor, in oder nach
    * dieser Äußerungseinheit liegt.
    * 
    * @param index
    *        der Index eines Token
    * @return -1, falls der Index vor dieser Äußerungseinheit liegt. <br>
    *         0, falls der Index durch diese Äußerungseinheit abgedeckt wird.
    *         <br>
    *         1, falls der Index hinter dieser Äußerungseinheit liegt.
    */
   public int compareIndex(int index) {
      if (index < getStartIndex())
         return -1;
      else if (index > getEndIndex())
         return 1;
      return 0;
   }

   /**
    * Gibt -1, 0 oder 1 zurück, je nachdem ob der Token vor, in oder nach dieser
    * Äußerungseinheit liegt.
    * 
    * @param token
    *        der zu prüfende Token
    * @return -1, falls der Token vor dieser Äußerungseinheit liegt. <br>
    *         0, falls der Token in dieser Äußerungseinheit liegt. <br>
    *         1, falls der Token hinter dieser Äußerungseinheit liegt.
    */
   public int compare(Token token) {
      return comparePosition(token.getStartPosition());
   }

   /**
    * Gibt -1, 0 oder 1 zurück, je nachdem ob die übergebene Äußerungseinheit
    * vor, in oder nach dieser Äußerungseinheit liegt.
    * 
    * @param iu
    *        die zu prüfende Äußerungseinheit
    * @return -1, falls die zu prüfende Äußerungseinheit vor dieser
    *         Äußerungseinheit liegt. <br>
    *         0, falls die zu prüfende Äußerungseinheit gleich dieser
    *         Äußerungseinheit ist. <br>
    *         1, falls die zu prüfende Äußerungseinheit hinter dieser
    *         Äußerungseinheit liegt.
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
    * Gibt den Index des Absatzs zurück, in dem diese Äußerungseinheit steht.
    * Der Erste Absatz hat Index 0.
    */
   public int getParagraph() {
      return paragraph;
   }

   /**
    * Gibt die relative Position der Äußerungseinheit in diesem Absatz zurück.
    * Die erste Äußerungseinheit steht an Index 0.
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
