
package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * 
 * @author Volker Klöbb
 * 
 */
public class SpeakerChange extends DB_Element {

   /**
    * 
    */
   private static final long serialVersionUID       = -6644201865454870982L;

   /**
    * speaker change is unknown
    */
   public static final int   SPEAKER_CHANGE_UNKNOWN = 0;

   /**
    * a partner of dialoge wants to start his contribution
    */
   public static final int   SPEAKER_CHANGE_AI      = 1;

   /**
    * the speaker actually speaking wants to continue
    */
   public static final int   SPEAKER_CHANGE_WI      = 2;

   /**
    * tha actual speakers indicates the end of his contribution
    */
   public static final int   SPEAKER_CHANGE_EI      = 3;

   /**
    * a partner has not the interest to start a speech contribution
    */
   public static final int   SPEAKER_CHANGE_NI      = 4;

   /**
    * the speaker encourages the partner to start his contribution
    */
   public static final int   SPEAKER_CHANGE_AII     = 5;

   /**
    * the speaker encourages the partner to continue his speech
    */
   public static final int   SPEAKER_CHANGE_WII     = 6;

   /**
    * the speaker wants to stop the speech of the partner
    */
   public static final int   SPEAKER_CHANGE_EII     = 7;

   /**
    * the speaker doesn't want the partner to begin with his contribution
    */
   public static final int   SPEAKER_CHANGE_NII     = 8;

   private int               speakerChange;
   private int               index;
   private boolean           accepted;
   private Dialog            dialog;
   // Wörter selbst sollen nicht übertragen werden, sondern nur deren IDs
   private transient Vector  words;
   private Vector            wordIDs;

   /**
    * Konstruktor für die Datenbank. Nicht verwenden!
    * 
    */
   public SpeakerChange(DBC_Key key,
         int id,
         Dialog dialog,
         int speakerChange,
         int index,
         boolean accepted) {
      super(id);
      this.dialog = dialog;
      this.speakerChange = speakerChange;
      this.index = index;
      this.accepted = accepted;

      words = new Vector();
      wordIDs = new Vector();

      dialog.add(this);
   }

   SpeakerChange(Dialog dialog, int speakerChange, int index) {
      super(-1);
      this.dialog = dialog;
      this.speakerChange = speakerChange;
      this.index = index;

      words = new Vector();
      wordIDs = new Vector();
      accepted = false;
   }

   /**
    * Fügt ein neues Wort dewm Sprecherwechsel hinzu
    * 
    * @param word
    */
   public void addWord(Word word) {
      if (words.contains(word))
         return;

      for (int i = 0; i < words.size(); i++) {
         Word w = (Word) words.get(i);
         if (word.getStartPosition() < w.getStartPosition()) {
            words.add(i, word);
            wordIDs.add(i, new Integer(word.getDB_ID()));
            return;
         }
      }

      words.add(words);
      wordIDs.add(new Integer(word.getDB_ID()));

      changeState(CHANGE);
   }

   /**
    * Fügt eine Sammlung von Wörtern dem Sprecherwechsel hinzu
    * 
    * @param words
    */
   public void addWords(Vector words) {
      for (int i = 0; i < words.size(); i++) {
         Object o = words.get(i);
         if (o instanceof Word)
            addWord((Word) o);
      }
   }

   /**
    * Ersetzt die bisherigen Wörter des Sprecherwechsels durch diese
    * 
    * @param words
    */
   public void setWords(Vector words) {
      words.clear();
      wordIDs.clear();

      addWords(words);
   }

   /**
    * Ersetzt die bisherigen Wörter des Sprecherwechsels durch die Wörter, die
    * im Intervall start - end vorkommen.
    * 
    * @param start
    *        Die Zeichenposition, die den Begin des Intervalls markiert
    * @param end
    *        Die Zeichenposition, die das Ende des Intervalls markiert
    */
   public void setWords(int start, int end) {
      addWords(dialog.getChapter().getTokenSequence(start, end));
   }

   public boolean remove() {
      dialog.remove(this);
      return true;
   }

   /**
    * @return Returns the speaker_change.
    */
   public int getSpeakerChange() {
      return speakerChange;
   }

   /**
    * 
    * @param speakerChange
    *        eine Konstante der Art SPEAKER_CHANGE_XYZ
    */
   public void setSpeakerChange(int speakerChange) {
      this.speakerChange = speakerChange;
      changeState(CHANGE);
   }

   /**
    * @return Returns the words.
    */
   public Vector getWords() {
      return words;
   }

   public boolean containsPosition(int position) {
      for (int i = 0; i < words.size(); i++) {
         Word word = (Word) words.get(i);
         if (word.containsPosition(position))
            return true;
      }
      return false;
   }

   public boolean contains(Word word) {
      return containsPosition(word.getStartPosition());
   }

   /**
    * @return Returns the dialog.
    */
   public Dialog getDialog() {
      return dialog;
   }

   public void setDialog(Dialog dialog) {
      if (this.dialog != dialog && dialog != null) {
         this.dialog.delete(this);
         this.dialog = dialog;
         this.dialog.add(this);
         changeState(CHANGE);
      }
   }

   /**
    * Setzt die Kapiteldaten neu, da diese nicht übertragen werden.
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();

      words.clear();
      for (int i = 0; i < wordIDs.size(); i++) {
         int id = ((Integer) wordIDs.get(i)).intValue();
         Word word = chapter.getWordWithID(id);
         if (word != null)
            words.add(word);
      }
   }

   /**
    * Gibt den Index des Sprecherwechsels zurück.
    */
   public int getIndex() {
      return index;
   }

   public void setIndex(int index) {
      this.index = index;
      changeState(CHANGE);
   }

   /**
    * Gibt die Position des ersten Zeichen des ersten Wortes von diesem
    * Sprecherwechsel zurück.
    * 
    */
   public int getStartPosition() {
      if (words.isEmpty())
         return -1;
      return ((Word) words.firstElement()).getStartPosition();
   }

   /**
    * Gibt die Position des letzten Zeichen des letzten Wortes von diesem
    * Sprecherwechsel zurück.
    */
   public int getEndPosition() {
      if (words.isEmpty())
         return -1;
      return ((Word) words.lastElement()).getEndPosition();
   }

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

   public String getContent() {
      if (words.isEmpty())
         return "no words";
      Word start = (Word) words.firstElement();
      Word end = (Word) words.lastElement();
      return dialog.getChapter().getContent(start, end);
   }

   public String toString() {
      return "Sprecherwechsel "
            + index
            + ": "
            + speakerChange
            + "\t"
            + getContent();
   }

}
