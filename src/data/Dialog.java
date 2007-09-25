/*
 * Erstellt: 19.11.2004
 */

package data;

import java.io.Serializable;
import java.util.Vector;

import connection.DBC_Key;

/**
 * Ein Dialog ist die Zusammenfassung mehrerer �u�erungseinheiten. Dabei wird
 * unterschieden zwischen dem Dialog selbst und einem Vor- und Nachfeld.
 * 
 * @author Volker Kl�bb
 */
public class Dialog extends DB_Element
      implements
         Serializable,
         Cloneable,
         CommentOwner {

   /**
    * 
    */
   private static final long        serialVersionUID = -8279514791427660970L;

   private transient Chapter        chapter;
   private int                      index;
   private int                      depth;
   private boolean                  accepted;
   private transient IllocutionUnit dialogStart;
   private transient IllocutionUnit dialogEnd;
   private int                      dialogStartIndex;
   private int                      dialogEndIndex;
   private DialogRunUp              runUp;
   private DialogFollowUp           followUp;
   private Vector                   speakerChanges;
   private Vector                   allSpeakerChanges;

   /**
    * Konstruktor f�r die Datenbank. Nicht verwenden!
    * 
    * @param chapter
    *        Das Kapitel, in dem der Dialog vorkommt
    * @param index
    *        Der Index des Dialoges innerhalb des Kapitels
    * @param depth
    *        Die Verschachtelungstiefe des Dialoges
    * @param accepted
    *        Der Akzeptiert-Status des Dialogs.
    */
   public Dialog(DBC_Key key,
         int id,
         Chapter chapter,
         int index,
         int depth,
         boolean accepted) {
      super(id);
      key.unlock();
      this.chapter = chapter;
      this.index = index;
      this.depth = depth;
      this.accepted = accepted;
      speakerChanges = new Vector();
      runUp = new DialogRunUp(this);
      followUp = new DialogFollowUp(this);
      allSpeakerChanges = new Vector();
   }

   /**
    * Konstruktor f�r neu angelegte Dialoge, die noch keine ID erhalten haben
    * 
    * @param chapter
    *        Das Kapitel, in dem der Dialog vorkommt
    * @param index
    *        Der Index des Dialoges innerhalb des Kapitels
    * @param depth
    *        Die Verschachtelungstiefe des Dialoges
    * @param accepted
    *        Der Akzeptiert-Status des Dialogs.
    */
   public Dialog(Chapter chapter, int index, int depth, boolean accepted) {
      super(-1);
      this.chapter = chapter;
      this.index = index;
      this.depth = depth;
      this.accepted = accepted;
      runUp = new DialogRunUp(this);
      followUp = new DialogFollowUp(this);
      speakerChanges = new Vector();
      allSpeakerChanges = new Vector();
   }

   /**
    * Konstruktor f�r neu angelegte Dialoge, die noch keine ID von der
    * Datenbank erhalten haben. Position und Tiefe werden auf 0 gesetzt,
    * accepted auf false.
    * 
    * @param chapter
    */
   public Dialog(Chapter chapter) {
      this(chapter, 0, 0, false);
   }

   public Dialog(Dialog dialog) {
      super(dialog.getDB_ID());
      
      chapter = dialog.chapter;
      index = dialog.index;
      depth = dialog.depth;
      accepted = dialog.accepted;
      speakerChanges = dialog.speakerChanges;

      dialogStart = dialog.dialogStart;
      dialogEnd = dialog.dialogEnd;

      runUp = dialog.runUp;
      followUp = dialog.followUp;
   }

   public SpeakerChange addSpeakerChange(Vector words,
         int speakerChange,
         int index) {
      SpeakerChange sc = new SpeakerChange(this, speakerChange, index);
      sc.addWords(words);
      speakerChanges.add(sc);
      allSpeakerChanges.add(sc);
      return sc;
   }

   public SpeakerChange getSpeakerChangeAtIndex(int index) {
      for (int i = 0; i < speakerChanges.size(); i++) {
         SpeakerChange sc = (SpeakerChange) speakerChanges.get(i);
         if (sc.getIndex() == index)
            return sc;
      }
      return null;
   }

   public SpeakerChange getSpeakerChangeAtPosition(int position) {
      for (int i = 0; i < speakerChanges.size(); i++) {
         SpeakerChange sc = (SpeakerChange) speakerChanges.get(i);
         if (sc.containsPosition(position))
            return sc;
      }
      return null;
   }

   public Vector getSpeakerChanges() {
      return speakerChanges;
   }

   public Vector getAllSpeakerChanges(DBC_Key key) {
      key.unlock();
      return allSpeakerChanges;
   }

   void delete(SpeakerChange speakerchange) {
      speakerChanges.remove(speakerchange);
      allSpeakerChanges.remove(speakerchange);
   }

   void add(SpeakerChange speakerChange) {
      speakerChanges.add(speakerChange);
      allSpeakerChanges.add(speakerChange);
   }

   public void remove(SpeakerChange speakerChange) {
      speakerChanges.remove(speakerChange);
      speakerChange.changeState(REMOVE);
   }

   /**
    * Klont diesen Dialog
    */
   public Object clone() {
      return new Dialog(this);
   }

   /**
    * Pr�ft, ob dieser Dialog schon akzeptiert wurde
    */
   public boolean isAccepted() {
      return accepted;
   }

   /**
    * Setzt den Akzeptiert-Status
    */
   public void setAccepted(boolean accepted) {
      this.accepted = accepted;
      changeState(CHANGE);
   }

   /**
    * Die Verschachtelungstiefe des Dialogs.
    */
   public int getDepth() {
      return depth;
   }

   /**
    * Setzt die Verschachtelungstiefe von diesem Dialog
    */
   public void setDepth(int depth) {
      this.depth = depth;
      changeState(CHANGE);
   }

   /**
    * Das Kapitel, in dem der Dialog vorkommt
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Der Index des Dialogs in dem Kapitel
    */
   public int getIndex() {
      return index;
   }

   /**
    * �ndert den Index des Dialoges innerhalb des Kapitels. Dabei d�rfen
    * keine Dialoge mit gleichem Index vorkommen. Falls doch, wird sp�testens
    * beim abspeichern in die Datenbank eine Ausnahme geworfen.
    * 
    * @param index
    */
   public void setIndex(int index) {
      this.index = index;
      changeState(CHANGE);
   }

   /**
    * Gibt alle Tokens in einem Vektor zur�ck, die in diesem Dialog vorkommen.
    * 
    * @return Vector mit Tokens
    */
   public Vector getDialogTokens() {
      return chapter.getTokenSequence(dialogStart.getFirstToken(), dialogEnd
            .getLastToken());
   }

   /**
    * Gibt die Indizies der Tokens zur�ck, die in diesem Dialog vorkommen.
    * 
    * @return int-Array mit Token-Indizies
    */
   public int[] getDialogTokenIndices() {
      return chapter.getIndexSequence(dialogStart.getFirstToken(), dialogEnd
            .getLastToken());
   }

   /**
    * Gibt alle Tokens in einem Vektor zur�ck, die in dem Vorfeld des Dialog
    * vorkommen.
    * 
    * @return Vector mit Tokens
    */
   public Vector getRunUpTokens() {
      if (hasRunUp())
         return chapter.getTokenSequence(runUp.getFirstToken(), runUp.getLastToken());
      return null;
   }

   /**
    * Gibt die Indizies der Tokens zur�ck, die in dem Vorfeld von diesem
    * Dialog vorkommen.
    * 
    * @return int-Array mit Token-Indizies.
    */
   public int[] getRunUpTokenIndices() {
      if (hasRunUp())
         return chapter.getIndexSequence(runUp.getFirstToken(), runUp
               .getLastToken());
      return null;
   }

   /**
    * Gibt alle Tokens in einem Vektor zur�ck, die in dem Nachfeld des Dialog
    * vorkommen.
    * 
    * @return Vector mit Tokens
    */
   public Vector getFollowUpTokens() {
      if (hasFollowUp())
         return chapter.getTokenSequence(followUp.getFirstToken(), followUp
               .getLastToken());
      return null;
   }

   /**
    * Gibt die absoluten Positionen der Tokens zur�ck, die in dem Nachfeld von
    * diesem Dialog vorkommen.
    * 
    * @return int-Array mit absoluten Token-Positionen.
    */
   public int[] getFollowUpTokenIndicies() {
      if (hasFollowUp())
         return chapter.getIndexSequence(followUp.getFirstToken(), followUp
               .getLastToken());
      return null;
   }

   /**
    * F�llt den Dialog neu. Der alte Dialog wird dabei ersetzt. Wichtig ist
    * hierbei der erste und letzte Eintrag in dem Array. Es wird angenommen, das
    * der Array eine durchgehende Folge von Tokenpositionen ohne L�cke
    * speichert.
    * 
    * @param positions
    *        ein Array mit den absoluten Position, die diesen Dialog
    *        repr�sentieren.
    */
   public void setDialogTokens(int[] positions) {
      if (positions.length == 1) {
         setDialogStart(positions[0]);
         setDialogEnd(positions[0]);
      }
      else if (positions.length > 1) {
         setDialogStart(positions[0]);
         setDialogEnd(positions[positions.length - 1]);
      }
      else
         clearDialog();
      changeState(CHANGE);
   }

   /**
    * F�llt das Vorfeld neu. Das alte Vorfeld wird dabei ersetzt. Wichtig ist
    * hierbei der erste und letzte Eintrag in dem Array. Es wird angenommen, das
    * der Array eine durchgehende Folge von Tokenpositionen ohne L�cke
    * speichert.
    * 
    * @param positions
    *        ein Array mit den absoluten Position, die dieses Vorfeld
    *        repr�sentieren.
    */
   public void setRunUpTokens(int[] positions) {
      if (positions.length == 1) {
         setRunUpStart(positions[0]);
         setRunUpEnd(positions[0]);
      }
      else if (positions.length > 1) {
         setRunUpStart(positions[0]);
         setRunUpEnd(positions[positions.length - 1]);
      }
      else
         clearRunUp();
      changeState(CHANGE);
   }

   /**
    * F�llt das Nachfeld neu. Das alte Nachfeld wird dabei ersetzt. Wichtig
    * ist hierbei der erste und letzte Eintrag in dem Array. Es wird angenommen,
    * das der Array eine durchgehende Folge von Tokenpositionen ohne L�cke
    * speichert.
    * 
    * @param positions
    *        ein Array mit den absoluten Position, die dieses Nachfeld
    *        repr�sentieren.
    */
   public void setFollowUpTokens(int[] positions) {
      if (positions.length == 1) {
         setFollowUpStart(positions[0]);
         setFollowUpEnd(positions[0]);
      }
      else if (positions.length > 1) {
         setFollowUpStart(positions[0]);
         setFollowUpEnd(positions[positions.length - 1]);
      }
      else
         clearFollowUp();
      changeState(CHANGE);
   }

   /**
    * Setzt den Start von diesem Dialog neu. Es Wird als Start die
    * �u�erungseinheit gew�hlt, in der dieser Token vorkommt.
    * 
    * @param token
    *        der neue Starttoken.
    * @see Dialog#setDialogStart(IllocutionUnit)
    */
   public void setDialogStart(Token token) {
      dialogStart = token.getIllocutionUnit();
      dialogStartIndex = dialogStart.getIndex();
      changeState(CHANGE);
   }

   /**
    * Setzt den Start von diesem Dialog neu
    * 
    * @param iu
    *        die �u�erungseinheit, die den Start von diesem Dialog bildet
    */
   public void setDialogStart(IllocutionUnit iu) {
      this.dialogStart = iu;
      dialogStartIndex = dialogStart.getIndex();
      changeState(CHANGE);
   }

   /**
    * Setzt die neue Startposition von diesem Dialog. Als Dialogstart wird die
    * �u�erungseinheit gew�hlt, die diesen Index abdeckt.
    * 
    * @param index
    *        die Startposition als Index eines Tokens
    * @see Dialog#setDialogStart(IllocutionUnit)
    */
   public void setDialogStart(int index) {
      dialogStart = chapter.getIllocutionUnit(chapter.getTokenAtIndex(index));
      dialogStartIndex = dialogStart.getIndex();
      changeState(CHANGE);
   }

   /**
    * Setzt das Ende von diesem Dialog. Als Ende wird dabei die
    * �u�erungseinheit gesetzt, in der dieser Token vorkommt.
    * 
    * @param token
    *        der neue Endtoken.
    * @see Dialog#setDialogEnd(IllocutionUnit)
    */
   public void setDialogEnd(Token token) {
      dialogEnd = token.getIllocutionUnit();
      dialogEndIndex = dialogEnd.getIndex();
      changeState(CHANGE);
   }

   /**
    * Setzt das Ende von diesem Dialog
    * 
    * @param iu
    *        Die �u�erungseinheit, die das neue Ende von diesem Dialog
    *        bilden soll.
    */
   public void setDialogEnd(IllocutionUnit iu) {
      this.dialogEnd = iu;
      dialogEndIndex = dialogEnd.getIndex();
      changeState(CHANGE);
   }

   /**
    * Setzt die neue Endposition von diesem Dialog. Als Ende von diesem Dialog
    * wird aber die entsprechende �u�erungseinheit gew�hlt, die diesen
    * Index beinhaltet.
    * 
    * @param index
    *        die Endposition als Index eines Tokens
    * @see Dialog#setDialogEnd(IllocutionUnit)
    */
   public void setDialogEnd(int index) {
      dialogEnd = chapter.getIllocutionUnit(chapter.getTokenAtIndex(index));
      dialogEndIndex = dialogEnd.getIndex();
      changeState(CHANGE);
   }

   /**
    * Setzt die Startposition des Vorfelds. Als Vorfeldstart wird allerdings die
    * entsprechende �u�erungseinheit gew�hlt, die diesen Index beinhaltet.
    * 
    * @param index
    *        die Startposition auf Tokenbasis
    */
   public void setRunUpStart(int index) {
      runUp.setStart(chapter.getIllocutionUnit(chapter.getTokenAtIndex(index)));
      changeState(CHANGE);
   }

   /**
    * Setzt den Start des Vorfeldes
    * 
    * @param iu
    *        Die �u�erungseinheit, die den Start des Vorfeldes darstellen
    *        soll.
    */
   public void setRunUpStart(IllocutionUnit iu) {
      runUp.setStart(iu);
      changeState(CHANGE);
   }

   /**
    * Setzt den Start des Vorfeldes. Intern wird aber die entsprechende
    * �u�erungseinheit als Start gew�hlt, in der dieser Token vorkommt.
    * 
    * @param token
    */
   public void setRunUpStart(Token token) {
      runUp.setStart(token.getIllocutionUnit());
      changeState(CHANGE);
   }

   /**
    * Setzt die Endposition des Vorfelds. Als Ende wird die �u�erungseinheit
    * gew�hlt, die diesen Index abdeckt.
    * 
    * @param index
    *        die Endposition als Index eines Tokens
    */
   public void setRunUpEnd(int index) {
      runUp.setEnd(chapter.getIllocutionUnit(chapter.getTokenAtIndex(index)));
      changeState(CHANGE);
   }

   /**
    * Setzt das Ende des Vorfelds.
    * 
    * @param iu
    *        Die �u�erungseinheit, die das Ende des Vorfelds darstellen
    *        soll.
    */
   public void setRunUpEnd(IllocutionUnit iu) {
      runUp.setEnd(iu);
      changeState(CHANGE);
   }

   /**
    * Setzt das Ende des Vorfelds. Entscheidend ist aber die
    * �u�erungseinheit, in der dieser Token vorkommt.
    * 
    * @param token
    */
   public void setRunUpEnd(Token token) {
      runUp.setEnd(token.getIllocutionUnit());
      changeState(CHANGE);
   }

   /**
    * Setzt die Startposition des Nachfelds. Intern wird aber die
    * �u�erungseinheit gespeichert, in der dieser Index vorkommt.
    * 
    * @param index
    *        die Startposition als Tokenindex
    */
   public void setFollowUpStart(int index) {
      followUp.setStart(chapter.getIllocutionUnit(chapter
            .getTokenAtIndex(index)));
      changeState(CHANGE);
   }

   /**
    * Setzt den Start des Nachfelds.
    * 
    * @param iu
    *        Die �u�erungseinheit, die den neuen Start des Nachfelds bilden
    *        soll.
    */
   public void setFollowUpStart(IllocutionUnit iu) {
      followUp.setStart(iu);
      changeState(CHANGE);
   }

   /**
    * Setzt den Start des Nachfelds. Intern wird aber die �u�erungseinheit
    * als Start verwendet, in der dieser Token vorkommt.
    * 
    * @param token
    */
   public void setFollowUpStart(Token token) {
      followUp.setStart(token.getIllocutionUnit());
      changeState(CHANGE);
   }

   /**
    * Setzt die Endposition des Nachfelds. Als Ende wird die
    * �u�erungseinheit gew�hlt, die diesen Index abdeckt.
    * 
    * @param index
    *        die Endposition als tokenindex
    */
   public void setFollowUpEnd(int index) {
      followUp
            .setEnd(chapter.getIllocutionUnit(chapter.getTokenAtIndex(index)));
      changeState(CHANGE);
   }

   /**
    * Setzt das Ende des Nachfelds.
    * 
    * @param iu
    *        Die �u�erungseinheit, die das neue Ende des Nachfelds bilden
    *        soll.
    */
   public void setFollowUpEnd(IllocutionUnit iu) {
      followUp.setEnd(iu);
      changeState(CHANGE);
   }

   /**
    * Setzt das Ende des Nachfelds. Intern wird dabei die �u�erungseinheit
    * als Ende gespeichert, in der dieser Token vorkommt.
    * 
    * @param token
    */
   public void setFollowUpEnd(Token token) {
      followUp.setEnd(token.getIllocutionUnit());
      changeState(CHANGE);
   }

   /**
    * Pr�ft, ob dieser Dialog die Zeichenposition abdeckt.
    * 
    * @param position
    *        die zu �berpr�fende Zeichenposition
    * @return true, falls die Position innerhalb des Dialogs liegt, sonst false.
    */
   public boolean containsPosition(int position) {
      return dialogStart.comparePosition(position) >= 0
            && dialogEnd.comparePosition(position) <= 0;
   }

   /**
    * Pr�ft, ob der Token in dem Dialog vorkommt.
    * 
    * @param token
    *        der zu pr�fende Token
    */
   public boolean containsDialogToken(Token token) {
      return token != null
            && dialogStart.compare(token) >= 0
            && dialogEnd.compare(token) <= 0;
   }

   /**
    * Pr�ft, ob der Token in dem Vorfeld vorkommt.
    * 
    * @param token
    *        der zu pr�fende Token
    */
   public boolean containsRunUpToken(Token token) {
      if (runUp.getStart() == null || runUp.getEnd() == null)
         return false;
      return token != null
            && runUp.getStart().compare(token) >= 0
            && runUp.getEnd().compare(token) <= 0;
   }

   /**
    * Pr�ft, ob der Token in dem Nachfeld vorkommt.
    * 
    * @param token
    *        der zu pr�fende Token
    */
   public boolean containsFollowUpToken(Token token) {
      if (followUp.getStart() == null || followUp.getEnd() == null)
         return false;
      return token != null
            && followUp.getStart().compare(token) >= 0
            && followUp.getEnd().compare(token) <= 0;
   }

   /**
    * Pr�ft, ob der Token in dem Dialog oder in dem Vorfeld oder in dem
    * Nachfeld vorkommt.
    * 
    * @param token
    *        der zu pr�fende Token
    */
   public boolean containsToken(Token token) {
      return containsDialogToken(token)
            || containsRunUpToken(token)
            || containsFollowUpToken(token);
   }

   /**
    * Pr�ft, ob diese �u�erungseinheit in dem Dialog oder in dem Vorfeld
    * oder in dem Nachfeld vorkommt.
    * 
    * @param iu
    *        die zu pr�fende �u�erungseinheit
    */
   public boolean containsIllocutionUnit(IllocutionUnit iu) {
      return containsDialogIllocutionUnit(iu)
            || containsRunUpIllocutionUnit(iu)
            || containsFollowUpIllocutionUnit(iu);
   }

   /**
    * Pr�ft, ob diese direkte Rede in dem Dialog vorkommt
    * 
    * @param speech
    *        die zu �berpr�fende direkte Rede
    */
   public boolean containsDirectSpeech(DirectSpeech speech) {
      return speech != null
            && dialogStart.compare(speech.getFirstToken()) >= 0
            && dialogEnd.compare(speech.getLastToken()) <= 0;
   }

   /**
    * Pr�ft, ob diese �u�erungseinheit in dem Dialog vorkommt.
    * 
    * @param iu
    *        die zu pr�fende �u�erungseinheit
    */
   public boolean containsDialogIllocutionUnit(IllocutionUnit iu) {
      return iu != null
            && dialogStart.compare(iu) >= 0
            && dialogEnd.compare(iu) <= 0;
   }

   /**
    * Pr�ft, ob diese �u�erungseinheit in dem Vorfeld vorkommt.
    * 
    * @param iu
    *        die zu pr�fende �u�erungseinheit
    */
   public boolean containsRunUpIllocutionUnit(IllocutionUnit iu) {
      if (runUp.getStart() == null || runUp.getEnd() == null)
         return false;
      return iu != null
            && runUp.getStart().compare(iu) >= 0
            && runUp.getEnd().compare(iu) <= 0;
   }

   /**
    * Pr�ft, ob diese �u�erungseinheit in dem Nachfeld vorkommt.
    * 
    * @param iu
    *        die zu pr�fende �u�erungseinheit
    */
   public boolean containsFollowUpIllocutionUnit(IllocutionUnit iu) {
      if (followUp.getStart() == null || followUp.getEnd() == null)
         return false;
      return iu != null
            && followUp.getStart().compare(iu) >= 0
            && followUp.getEnd().compare(iu) <= 0;
   }

   /**
    * Die letzte �u�erungseinheit des Dialogs
    */
   public IllocutionUnit getDialogEnd() {
      return dialogEnd;
   }

   /**
    * Die erste �u�erungseinheit des Dialogs.
    */
   public IllocutionUnit getDialogStart() {
      return dialogStart;
   }

   /**
    * Alle IUs des Dialogs in einem Vektor
    */
   public Vector getDialogIllocutionUnits() {
      return chapter.getIllocutionUnitsFromIndex(dialogStartIndex,
            dialogEndIndex);
   }

   /**
    * Alle IUs des Vorfelds in einem Vektor. Null, wenn der Dialog kein Vorfeld
    * hat.
    */
   public Vector getFollowUpIllocutionUnits() {
      if (hasFollowUp())
         return chapter.getIllocutionUnitsFromIndex(followUp.getStartIndex(),
               followUp.getEndIndex());
      return null;
   }

   public Vector getIllocutionUnits() {
      Vector res = new Vector();
      res.addAll(getRunUpIllocutionUnits());
      res.addAll(getDialogIllocutionUnits());
      res.addAll(getFollowUpIllocutionUnits());
      return res;
   }

   /**
    * Alle IUs des Nachfelds in einem Vektor. Null, wenn der Dialog kein
    * Nachfeld hat.
    */
   public Vector getRunUpIllocutionUnits() {
      if (hasRunUp())
         return chapter.getIllocutionUnitsFromIndex(runUp.getStartIndex(),
               runUp.getEndIndex());
      return null;
   }

   /**
    * Die letzte �u�erungseinheit des Nachfelds
    */
   public IllocutionUnit getFollowUpEnd() {
      if (hasFollowUp())
         return followUp.getEnd();
      return null;
   }

   /**
    * Die erste �u�erungseinheit des Nachfelds
    */
   public IllocutionUnit getFollowUpStart() {
      if (hasFollowUp())
         return followUp.getStart();
      return null;
   }

   /**
    * Die letzte �u�erungseinheit des Vorfelds
    */
   public IllocutionUnit getRunUpEnd() {
      if (hasRunUp())
         return runUp.getEnd();
      return null;
   }

   /**
    * Die erste �u�erungseinheit des Vorfelds
    */
   public IllocutionUnit getRunUpStart() {
      if (hasRunUp())
         return runUp.getStart();
      return null;
   }

   /**
    * Gibt das Vorfeld des Dialogs zur�ck. Hilfreich f�r die Speicherung von
    * Kommentaren.
    */
   public DialogRunUp getDialogRunUp() {
      return runUp;
   }

   /**
    * Gibt das Nachfeld des Dialogs zur�ck. Hilfreich f�r die Speicherung
    * von Kommentaren.
    */
   public DialogFollowUp getDialogFollowUp() {
      return followUp;
   }

   /**
    * Setzt den Dialog zur�ck
    * 
    */
   public void clearDialog() {
      dialogStart = null;
      dialogEnd = null;
   }

   /**
    * Setzt das Vorfeld zur�ck
    * 
    */
   public void clearRunUp() {
      runUp.clear();
   }

   /**
    * Setzt das Nachfeld zur�ck
    * 
    */
   public void clearFollowUp() {
      followUp.clear();
   }

   public boolean equals(Object o) {
      if (o instanceof Dialog) {
         Dialog test = (Dialog) o;
         return dialogStart.equals(test.dialogStart)
               && dialogEnd.equals(test.dialogEnd)
               && runUp.equals(test.runUp)
               && followUp.equals(test.followUp);
      }
      return false;
   }

   /**
    * Pr�ft, ob schon ein Vorfeld zu diesem Dialog angegeben wurde.
    */
   public boolean isRunUpSet() {
      return runUp.getStart() != null && runUp.getEnd() != null;
   }

   /**
    * Pr�ft, ob schon ein Nachfeld zu diesem Dialog angegeben wurde.
    * 
    */
   public boolean isFollowUpSet() {
      return followUp.getStart() != null && followUp.getEnd() != null;
   }

   /**
    * Gibt den Dialog als String zur�ck (Testausgabe)
    */
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("\nDialog " + getDB_ID() + " / " + index + ":");

      if (isRunUpSet())
         sb.append("\nVorfeld: "
               + chapter.getIllocutionUnitsFromPositions(runUp.getStart()
                     .getStartPosition(), runUp.getEnd().getEndPosition()));

      sb.append("\nDialog: "
            + chapter.getIllocutionUnitsFromPositions(dialogStart
                  .getStartPosition(), dialogEnd.getEndPosition()));

      if (isFollowUpSet())
         sb.append("\nNachfeld: "
               + chapter.getIllocutionUnitsFromPositions(followUp.getStart()
                     .getStartPosition(), followUp.getEnd().getEndPosition()));
      return sb.toString();
   }

   public boolean remove() {
      changeState(REMOVE);
      return true;
   }

   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      this.chapter = chapter;
      dialogStart = chapter.getIllocutionUnitAtIndex(dialogStartIndex);
      dialogEnd = chapter.getIllocutionUnitAtIndex(dialogEndIndex);
      runUp.setChapter(key, chapter);
      followUp.setChapter(key, chapter);

      for (int i = 0; i < allSpeakerChanges.size(); i++) {
         SpeakerChange sc = (SpeakerChange) allSpeakerChanges.get(i);
         sc.setChapter(key, chapter);
      }
   }

   public int getClassCode() {
      return Comments.CLASS_CODE_DIALOG;
   }

   public boolean hasRunUp() {
      return runUp.getStart() != null && runUp.getEnd() != null;
   }

   public boolean hasFollowUp() {
      return followUp.getStart() != null && followUp.getEnd() != null;
   }

   public void updateIDs(DBC_Key key, Dialog d) {
      super.updateIDs(key, d);

      for (int i = 0; i < speakerChanges.size(); i++) {
         SpeakerChange sc1 = (SpeakerChange) speakerChanges.get(i);
         SpeakerChange sc2 = (SpeakerChange) d.speakerChanges.get(i);
         sc1.updateIDs(key, sc2);
      }
   }
   
   void resetIDs() {
      super.resetIDs();
      for(int i = 0; i < speakerChanges.size(); i++)
         ((SpeakerChange)speakerChanges.get(i)).resetIDs();
   }
}