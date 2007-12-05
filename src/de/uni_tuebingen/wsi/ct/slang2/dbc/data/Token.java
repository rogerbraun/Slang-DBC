/*
 * Erstellt: 29.04.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Ein Token ist entweder ein Wort oder ein Satzzeichen, die durch die Klassen
 * Word und Sign repräsentiert werden.
 * 
 * @author Volker Klöbb
 * @see Word
 * @see Sign
 */
public abstract class Token
      implements
         Serializable {

   /**
    * Die Datenbank-ID <br>
    * Hier die ID vom Wort oder vom Zeichen
    */
   private int            id;

   /**
    * Das Kapitel, in dem dieser Token vorkommt
    */
   private Chapter        chapter;

   /**
    * Die Position des Zeichens, mit dem dieser Token beginnt
    */
   private int            start;

   /**
    * Die Position des Zeichens, mit dem dieser Token endet.
    */
   private int            end;

   private int            index;

   private IllocutionUnit illocutionUnit;

   /**
    * Wird vom DBC benötigt
    */
   protected Token(int id, Chapter chapter, int start, int length) {
      this.id = id;
      this.chapter = chapter;
      this.start = start;
      this.end = start + length;
   }

   /**
    * Die Datenbank-ID
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
    * Das Kapitel, in dem der Token vorkommt
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Wird vom DBC benötigt
    */
   void setIndex(int index) {
      this.index = index;
   }

   /**
    * Der Index des Tokens, also die absolute Position des Tokens innerhalb des
    * Kapitels, beginnend bei 0.
    */
   public int getIndex() {
      return index;
   }

   /**
    * Die Position des Zeichen, mit dem dieser Token beginnt. Ausgehend vom
    * ersten Zeichen des Kapitels, das an Position 0 steht.
    * 
    */
   public int getStartPosition() {
      return start;
   }

   /**
    * Die Position des Zeichen, mit dem dieser Token endet. Ausgehend vom ersten
    * Zeichen des Kapitels, das an Position 0 steht.
    * 
    */
   public int getEndPosition() {
      return end;
   }

   /**
    * Der Inhalt des Tokens, also entweder das Wort oder das Satzzeichen
    */
   public abstract String getContent();

   /**
    * Steht dieses Token an besagter Zeichenposition?
    * 
    * @param position
    *        die zu prüfende Position (bezogen auf Zeichen)
    * @return true, falls gilt start <= position <= end, sonst false
    */
   public boolean containsPosition(int position) {
      return start <= position && position <= end;
   }

   /**
    * Gibt an, ob die Zeichenposition vor, in oder hinter diesem Token liegt.
    * 
    * @param position
    *        die zu prüfende Position auf Zeichenbasis.
    * @return -1, wenn die Position vor dem Token liegt. <br>
    *         0, wenn die Position innerhalb des Token liegt oder <br>
    *         1, wenn die Position hinter diesem Token liegt.
    */
   public int comparePosition(int position) {
      if (position < start)
         return -1;
      else if (position > end)
         return 1;
      return 0;
   }

   /**
    * Vergleicht die Positionen zweier Tokens.
    * 
    * @param token
    *        ein Token
    * @return -1, wenn der zu testende Token vor diesem liegt. <br>
    *         0, wenn der zu testende Token an gleicher Position liegt (sollte
    *         dann hoffentlich identisch sein). <br>
    *         1, wenn der zu testende Token hinter diesem Token liegt.
    */
   public int compare(Token token) {
      if (getIndex() < token.getIndex())
         return -1;
      else if (getIndex() > token.getIndex())
         return 1;
      return 0;
   }

   /**
    * Gibt die Entfernung zu dem Token zurück. Liegt die zu prüfende Position
    * vor dem Token, wird der Abstand von der Position zu dem ersten Zeichen
    * zurückgegeben. Liegt die Position hinter dem Token, wird die Entfernung
    * vom letzten Zeichen des Tokens zur Position zurückgegeben. Liegt die
    * Position innerhalb des Tokens, beträgt die Entfernung 0.
    * 
    * @param position
    *        die Position auf Zeichenbasis
    * @return die Entfernung von dem Token zu der Position. Die Entfernung ist
    *         immer größer-gleich 0.
    */
   public int getDistance(int position) {
      switch (comparePosition(position)) {
         case -1 :
            return start - position;
         case 1 :
            return position - end;
         default :
            return 0;
      }
   }

   public String toString() {
      return getContent();
   }

   /**
    * Gibt den nächsten Token (innerhalb von diesem Kapitel) zurück.
    * 
    * @return den nächsten Token oder null, falls dieser Token schon der letzte
    *         in diesem Kapitel ist.
    * @see Token#getPreviousToken()
    */
   public Token getNextToken() {
      return chapter.getTokenAtIndex(getIndex() + 1);
   }

   /**
    * Gibt den vorherigen Token zurück.
    * 
    * @return den vorherigen Token oder null, falls dieser Token schon der erste
    *         in diesem Kapitel ist.
    * @see Token#getNextToken()
    */
   public Token getPreviousToken() {
      return chapter.getTokenAtIndex(getIndex() - 1);
   }

   void setIllocutionUnit(IllocutionUnit iu) {
      illocutionUnit = iu;
   }

   /**
    * Die Äußerungseinheit von diesem Token
    */
   public IllocutionUnit getIllocutionUnit() {
      return illocutionUnit;
   }
   
   public void resetID() {
      id = -1;
   }
}
