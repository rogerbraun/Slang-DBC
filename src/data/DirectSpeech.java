/*
 * Erstellt: 03.11.2004
 */

package data;

import java.io.Serializable;
import java.util.Vector;

import connection.DBC_Key;

/**
 * Eine direkte Rede setzt sich aus einer Liste von �u�erungseinheiten
 * zusammen.
 * 
 * @author Volker Kl�bb
 * @see data.IllocutionUnit
 */
public class DirectSpeech extends DB_Element
      implements
         Serializable,
         CommentOwner,
         Cloneable {

   /**
    * 
    */
   private static final long serialVersionUID = -7732376098571622842L;

   private transient Chapter chapter;
   private int               index;
   private int               depth;
   private boolean           accepted;
   private Vector            illocutionUnitsIndicies;

   private DirectSpeech      possibleQuestion;
   private int               possibleQuestionID;
   private Vector            possibleResponse;

   /**
    * Wird vom DBC ben�tigt.
    */
   public DirectSpeech(DBC_Key key,
         int id,
         Chapter chapter,
         int index,
         int depth,
         boolean accepted,
         int possibleQuestionID) {
      super(id);
      key.unlock();
      this.chapter = chapter;
      this.index = index;
      this.depth = depth;
      this.accepted = accepted;
      this.possibleQuestionID = possibleQuestionID;

      illocutionUnitsIndicies = new Vector();
      possibleResponse = new Vector();
   }

   /**
    * Konstruktor f�r neue direkten Reden, die bis jetzt noch nicht in der
    * Datenbank gespeichert sind.
    * 
    * @param chapter
    *        das Kapitel, in dem diese direkte Rede vorkommt.
    * @param index
    *        der Index der direkten Rede innerhalb des Kaptiels.
    * @param depth
    *        die Verschachtelungstiefe der direkten Rede.
    * @param accepted
    *        ob diese direkte Rede schon vom Benutzer akzeptiert wurde.
    */
   public DirectSpeech(Chapter chapter, int index, int depth, boolean accepted) {
      super(-1);
      this.chapter = chapter;
      this.index = index;
      this.depth = depth;
      this.accepted = accepted;

      illocutionUnitsIndicies = new Vector();
      possibleResponse = new Vector();
   }

   /**
    * Konstruktor f�r neu angelegte direkte Reden. Index und Tiefe werden
    * beide mit 0 initialisiert, accepted wird auf false gesetzt. <br>
    * <b>Wichtig </b>: Der Index muss unbedingt vor dem schreiben in die
    * Datenbank gesetzt werden. Da neu angelegte direkte Reden noch keine
    * Datenbank-ID bekommen haben, wird der Zugriff zu Beginn �ber den Index
    * geregelt.
    * 
    * @param chapter
    *        Das Kapitel, in dem die direkte Rede vorkommt.
    */
   public DirectSpeech(Chapter chapter) {
      this(chapter, 0, 0, false);
   }

   /**
    * Clont eine direkte Rede. Das Kapitel wird dabei nur als Referenz
    * �bergeben, da es sich hierbei um nicht ver�nderbare Daten handelt.
    */
   public Object clone() {
      DirectSpeech c = new DirectSpeech(chapter, index, depth, accepted);
      c.setDB_ID(getDB_ID());
      c.illocutionUnitsIndicies = (Vector) illocutionUnitsIndicies.clone();
      c.possibleResponse = (Vector) possibleResponse.clone();
      return c;
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      this.chapter = chapter;
   }

   public boolean equals(Object o) {
      if (o instanceof DirectSpeech) {
         DirectSpeech test = (DirectSpeech) o;
         return illocutionUnitsIndicies.equals(test.illocutionUnitsIndicies);
      }
      return false;
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public void setIllocutionUnit(DBC_Key key, int illocutionUnitID) {
      key.unlock();
      add(chapter.getIllocutionUnitWithID(illocutionUnitID));
   }

   /**
    * F�gt alle �u�erungseinheiten zu der direkten Rede hinzu, die von den
    * �bergebenen Indices abgedeckt werden. Die alten �u�erungseinheiten
    * werden dabei ersetzt.
    * 
    * @param indices
    *        die Indices aller Tokens.
    */
   public void setTokens(int[] indices) {
      illocutionUnitsIndicies.clear();
      for (int i = 0; i < indices.length; i++) {
         Token t = chapter.getTokenAtIndex(indices[i]);
         if (t != null)
            add(chapter.getIllocutionUnit(t));
      }
      changeState(CHANGE);
   }

   /**
    * Gibt alle Tokens, die zu der direkten Rede geh�ren in einem Vektor
    * zur�ck.
    * 
    * @return ein Vektor mit Tokens
    */
   public Vector getTokens() {
      Vector res = new Vector();
      for (int i = 0; i < illocutionUnitsIndicies.size(); i++) {
         IllocutionUnit iu = chapter
               .getIllocutionUnitAtIndex(((Integer) illocutionUnitsIndicies
                     .get(i)).intValue());
         res.addAll(iu.getTokens());
      }
      return res;
   }

   /**
    * Gibt den ersten Token dieser direkten Rede zur�ck.
    * 
    * @return den ersten Token
    * @see DirectSpeech#getLastToken()
    */
   public Token getFirstToken() {
      if (illocutionUnitsIndicies.isEmpty())
         return null;
      Integer index = (Integer) illocutionUnitsIndicies.firstElement();
      return chapter.getIllocutionUnitAtIndex(index.intValue()).getFirstToken();
   }

   /**
    * Gibt den letzten Token dieser direkten Rede zur�ck.
    * 
    * @return den letzen Token.
    * @see DirectSpeech#getFirstToken()
    */
   public Token getLastToken() {
      if (illocutionUnitsIndicies.isEmpty())
         return null;
      Integer index = (Integer) illocutionUnitsIndicies.lastElement();
      return chapter.getIllocutionUnitAtIndex(index.intValue()).getLastToken();
   }

   /**
    * F�gt eine neue �u�erungseinheit zu der direkten Rede hinzu.
    * 
    * @param illocutionUnit
    *        die �u�erungseinheit selbst.
    */
   public void add(IllocutionUnit illocutionUnit) {
      int index = illocutionUnit.getIndex();

      for (int i = 0; i < illocutionUnitsIndicies.size(); i++) {
         int i2 = ((Integer) illocutionUnitsIndicies.get(i)).intValue();
         if (i2 == index)
            return;
         if (i2 > index) {
            illocutionUnitsIndicies.add(i, new Integer(index));
            return;
         }
      }
      illocutionUnitsIndicies.add(new Integer(index));
      changeState(CHANGE);
   }

   /**
    * F�gt die �u�erungseinheit zu der direkten Rede hinzu, zu der der
    * Token geh�rt.
    * 
    * @param token
    *        ein Token.
    */
   public void add(Token token) {
      add(token.getIllocutionUnit());
   }

   /**
    * Entfernt eine �u�erungseinheit aus der direkten Rede
    * 
    * @param illocutionUnit
    *        die zu entfernende �u�erungseinheit.
    */
   public void remove(IllocutionUnit illocutionUnit) {
      Integer index = new Integer(illocutionUnit.getIndex());
      illocutionUnitsIndicies.remove(index);
      changeState(CHANGE);
   }

   /**
    * Entfernt die zu dem Token geh�rende �u�erungseinheit aus der
    * direkten Rede.
    * 
    * @param token
    *        der Token, dessen �u�erungseinheit entfernt werden soll.
    */
   public void remove(Token token) {
      remove(token.getIllocutionUnit());
   }

   /**
    * Entfernt die �u�erungseinheit, die diese (absolute) Tokenposition
    * abdeckt, aus der direkten Rede.
    * 
    * @param index
    *        der Index des Tokens, dessen �u�erungseinheit entfernt werden
    *        soll.
    */
   public void removeToken(int index) {
      remove(chapter.getIllocutionUnitAtIndex(index));
   }

   /**
    * Pr�ft, ob diese direkte Rede schon akzeptiert wurde.
    * 
    * @return ...naja, was wohl.
    */
   public boolean isAccepted() {
      return accepted;
   }

   /**
    * Legt fest, ob diese direkte Rede vom Benutzer abgesegnet wurde oder nicht.
    * 
    * @param accepted
    *        hmmm, ein boolean???
    */
   public void setAccepted(boolean accepted) {
      this.accepted = accepted;
      changeState(CHANGE);
   }

   /**
    * Die Tiefe der direkten Rede, beginnend bei 1
    * 
    * @return die Tiefe
    */
   public int getDepth() {
      return depth;
   }

   /**
    * Legt die Verschachtelungstiefe der direkten Rede fest.
    * 
    * @param depth
    *        die Tiefe
    */
   public void setDepth(int depth) {
      this.depth = depth;
      changeState(CHANGE);
   }

   /**
    * Das Kapitel, in dem die direkte Rede steht.
    * 
    * @return siehe oben
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Der Index der direkten Rede innerhalb des Kapitels
    * 
    * @return der Index
    */
   public int getIndex() {
      return index;
   }

   /**
    * Legt den Index der direkten Rede innerhalb des Kapitels fest. Die
    * Z�hlung beginnt bei 1.
    * 
    * @param index
    *        der Index
    */
   public void setIndex(int index) {
      this.index = index;
      changeState(CHANGE);
   }

   /**
    * Alle �u�erungseinheiten, die zu dieser direkten Rede geh�ren.
    * 
    * @return ein Vektor mit allen �u�erungseinheiten der direkten Rede.
    */
   public Vector getIllocutionUnits() {
      Vector res = new Vector();
      for (int i = 0; i < illocutionUnitsIndicies.size(); i++) {
         Integer index = (Integer) illocutionUnitsIndicies.get(i);
         res.add(chapter.getIllocutionUnitAtIndex(index.intValue()));
      }
      return res;
   }

   /**
    * Die Indices aller Tokens, die zu der direkten Rede geh�ren.
    * 
    * @return ein int-Array mit allen Indices der Tokens.
    */
   public int[] getTokenIndices() {
      Vector tokens = chapter.getTokenSequence(getFirstToken(), getLastToken());
      int[] res = new int[tokens.size()];
      for (int i = 0; i < tokens.size(); i++)
         res[i] = ((Token) tokens.get(i)).getIndex();
      return res;
   }

   /**
    * Pr�ft, ob diese direkte Rede diesen Index beinhaltet.
    * 
    * @param index
    *        der zu pr�fende Index
    * @return true, wenn irgendeine �u�erungseinheit dieser direkten Rede
    *         den Index abdeckt, sonst false.
    */
   public boolean containsIndex(int index) {
      Token token = chapter.getTokenAtIndex(index);
      if (token != null && token.getIllocutionUnit().containsIndex(index))
         return true;
      return false;
   }

   /**
    * Pr�ft, ob diese direkte Rede diese Zeichenposition abdeckt.
    * 
    * @param position
    *        die Zeichenposition
    * @return true, wenn irgendeine �u�erungseinheit diese Zeichenposition
    *         abdeckt, sonst false.
    */
   public boolean containsPosition(int position) {
      IllocutionUnit iu = chapter.getIllocutionUnitAtPosition(position);
      if (iu != null) {
         Integer index = new Integer(iu.getIndex());
         return illocutionUnitsIndicies.contains(index);
      }
      return false;
   }

   /**
    * Pr�ft, ob dieser Token in der direkten Rede vorkommt.
    * 
    * @param token
    *        der zu pr�fende Token
    * @return true, wenn der Token in der direkten Rede vorkommt.
    */
   public boolean contains(Token token) {
      return token != null
            && getFirstToken().compare(token) >= 0
            && getLastToken().compare(token) <= 0;
   }

   /**
    * Pr�ft, ob diese �u�erungseinheit in der direkten Rede vorkommt
    * 
    * @param iu
    *        die zu pr�fende �u�erungseinheit
    * @return true, wenn die �u�erungseinheit in der direkten Rede vorkommt,
    *         sonst false.
    */
   public boolean contains(IllocutionUnit iu) {
      if (iu != null) {
         Integer index = new Integer(iu.getIndex());
         return illocutionUnitsIndicies.contains(index);
      }
      return false;
   }

   public String toString() {
      return getIllocutionUnits().toString();
   }

   /**
    * Markiert diese Direkte Rede als gel�scht
    */
   public boolean remove() {
      changeState(REMOVE);
      return true;
   }

   public int getClassCode() {
      return Comments.CLASS_CODE_DIRECT_SPEECH;
   }

   private void addPossibleResponse(DirectSpeech ds) {
      if (!possibleResponse.contains(ds))
         possibleResponse.add(ds);
   }

   private void removePossibleResponse(DirectSpeech ds) {
      possibleResponse.remove(ds);
   }

   /**
    * Gibt alle Direkte Reden zur�ck, die m�gliche Antworten sein k�nnen
    */
   public Vector getPossibleResponse() {
      return possibleResponse;
   }

   /**
    * Gibt die Direkte Rede zur�ck, zu der diese eine m�gliche Antwort ist
    */
   public DirectSpeech getPossibleQuestion() {
      return possibleQuestion;
   }

   public int getPossibleQuestionID() {
      return possibleQuestionID;
   }

   /**
    * Setzt die m�gliche Antwort, auf die sich diese Direkte Rede beziehen
    * soll. Wird null �bergeben, wird diese Direkte Rede als m�gliche
    * Antwort gel�scht.
    */
   public void setPossibleQuestion(DirectSpeech ds) {
      if (possibleQuestion != null)
         possibleQuestion.removePossibleResponse(this);

      if (ds != null) {
         ds.addPossibleResponse(this);
         possibleQuestion = ds;
         possibleQuestionID = ds.getDB_ID();
      }
      else {
         possibleQuestion = null;
         possibleQuestionID = -1;
      }
      changeState(CHANGE);
   }

}