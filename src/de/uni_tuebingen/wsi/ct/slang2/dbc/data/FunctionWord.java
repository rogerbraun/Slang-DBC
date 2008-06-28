/*
 * Erstellt: 30.10.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Type;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.NoWordFoundAtPositionException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.OverlappingException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.PositionNotInTokenException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.UnequalTokensException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.WordNotInIllocutionUnitException;

/**
 * Ein Funktionswort
 * 
 * @author Volker Klöbb
 */
public class FunctionWord extends DB_Element
      implements
         IllocutionUnitElement, ChapterElement {

   /**
    * 
    */
   private static final long  serialVersionUID            = -2242998244015007068L;

   private IllocutionUnitRoot root;
   private MeaningUnit        meaningUnit;
   private transient Word     word;
   private int                start;
   private int                end;
   private boolean            accepted;
   
   private SememeGroup        sememeGroup;

   private TR_Assignation assignation;

   /**
    * Wird von der Datenbank benötigt
    */
   public FunctionWord(DBC_Key key,
         IllocutionUnitRoot root,
         int id,
         Word word,
         int start,
         int end,
         boolean accepted,
         TR_Assignation assignation) {
      super(id);
      key.unlock();

      this.root = root;
      this.start = start;
      this.word = word;
      this.end = end;
      this.accepted = accepted;
      this.assignation = assignation;
      
      root.add(this);
      root.register(this);
   }

   /**
    * Ein neues Functionswort, auch für Teilworte verwendbar (König_s_haus)
    * 
    * @param root
    *        Die Wurzel der Äußerungseinheit, der dieses Funktionswort
    *        untergeordnet ist
    * @param start
    *        Die Startposition des FWs, bezogen auf die Zeichenposition
    * @param end
    *        Die Endposition des FWs, bezogen auf die Zeichenposition
    * @throws PositionNotInTokenException
    * @throws NoWordFoundAtPositionException
    * @throws WordNotInIllocutionUnitException
    * @throws OverlappingException
    * @throws UnequalTokensException
    */
   public FunctionWord(IllocutionUnitRoot root, int start, int end)
         throws PositionNotInTokenException, NoWordFoundAtPositionException,
         WordNotInIllocutionUnitException, OverlappingException,
         UnequalTokensException {
      super(-1);

      Token tokenStart = root.getChapter().getTokenAtPosition(start);
      Token tokenEnd = root.getChapter().getTokenAtPosition(end);

      if (tokenStart == null || !(tokenStart instanceof Word))
         throw new NoWordFoundAtPositionException("Kein Wort an Position "
               + start
               + " gefunden");

      if (tokenEnd == null || !(tokenEnd instanceof Word))
         throw new NoWordFoundAtPositionException("Kein Wort an Position "
               + end
               + " gefunden");

      if (tokenStart != tokenEnd)
         throw new UnequalTokensException("Token '"
               + tokenStart
               + "' und '"
               + tokenEnd
               + "' sind ungleich");

      if (!root.getIllocutionUnit().containsPosition(start))
         throw new WordNotInIllocutionUnitException();

      if (!root.getIllocutionUnit().containsPosition(end))
         throw new WordNotInIllocutionUnitException();

      word = (Word) tokenStart;
      if (!word.containsPosition(start))
         throw new PositionNotInTokenException(
               "Startposition liegt nicht im Wort", word, start);
      if (!word.containsPosition(end))
         throw new PositionNotInTokenException(
               "Endposition liegt nicht im Wort", word, end);

      if (root.exitsConstitutiveWords(start, end))
         throw new OverlappingException(
               "Schon ein CW in diesem Intervall vorhanden ["
                     + start
                     + ", "
                     + end
                     + "]");

      if (root.exitsFunctionWords(start, end))
         throw new OverlappingException(
               "Schon ein FW in diesem Intervall vorhanden ["
                     + start
                     + ", "
                     + end
                     + "]");

      this.root = root;
      this.start = start;
      this.end = end;

      if (root.add(this))
         root.register(this);
   }

   /**
    * Ein neues Funktionswort, bezogen auf ein eigenständiges Wort
    * 
    * @param root
    *        Die Wurzel der Äußerungseinheit, der dieses Funktionswort
    *        untergeordnet ist
    * @param word
    *        Das Wort an sich
    * @throws PositionNotInTokenException
    * @throws NoWordFoundAtPositionException
    * @throws WordNotInIllocutionUnitException
    * @throws OverlappingException
    * @throws UnequalTokensException
    */
   public FunctionWord(IllocutionUnitRoot root, Word word)
         throws PositionNotInTokenException, NoWordFoundAtPositionException,
         WordNotInIllocutionUnitException, OverlappingException,
         UnequalTokensException {
      this(root, word.getStartPosition(), word.getEndPosition());
   }

   public boolean equals(Object o) {
      if (o instanceof FunctionWord) {
         FunctionWord fw = (FunctionWord) o;
         return start == fw.start && end == fw.end;
      }
      return false;
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      word = (Word) chapter.getTokenAtPosition(start);
   }

   /**
    * Prüft, ob dieses Funktionsort schon akzeptiert wurde.
    * 
    */
   public boolean isAccepted() {
      return accepted;
   }

   /**
    * Legt fest, ob dieses Funktionswort vom Benutzer abgesegnet wurde oder
    * nicht.
    * 
    */
   public void setAccepted(boolean accepted) {
      changeState(CHANGE);
      this.accepted = accepted;
   }

   /**
    * Die Semantische Einheit von diesem Funktionswort
    * 
    * @return die Semantische Einheit oder null, falls diesem Funnktionswort
    *         noch keine Semantische Einheit zugeordnet wurde
    */
   public MeaningUnit getMeaningUnit() {
      return meaningUnit;
   }

   void setMeaningUnit(MeaningUnit meaningUnit) {
      if (sememeGroup == null) {
         this.meaningUnit = meaningUnit;
         changeState(CHANGE);
      }
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setMeaningUnit(DBC_Key key, MeaningUnit meaningUnit) {
      key.unlock();
      this.meaningUnit = meaningUnit;
   }

   void setSememeGroup(SememeGroup sg) {
      if (meaningUnit == null) {
         sememeGroup = sg;
      }
   }

   /**
    * Die Sememegruppe von diesem Funktionswort
    * 
    * @return die Sememegruppe oder null, falls dieses Funktionswort noch keiner
    *         Sememegruppe zugeordnet wurde
    */
   public SememeGroup getSememeGroup() {
      return sememeGroup;
   }

   /**
    * Gibt das Wort zurück, auf dem das Funktionswort aufbaut. Bezieht sich das
    * Funktionswort nur auf ein Teilwort, wird trotzdem das komplette Wort
    * zurückgegeben.
    */
   public Word getWord() {
      return word;
   }

   /**
    * Gibt die Zeichenposition von dem Begin des Funktionswortes zurück
    */
   public int getStartPosition() {
      return start;
   }

   /**
    * Gibt die Zeichenposition von dem Ende des Funktionswortes zurück
    */
   public int getEndPosition() {
      return end;
   }

   public boolean containsPosition(int position) {
      return start <= position && position <= end;
   }

   
   /**
    * Das Wort als String
    */
   public String getContent() {
      return root.getChapter().getContent(start, end);
   }

   public String toString() {
      return getContent();
   }

   /**
    * Keine Bedeutung
    */
   public int getIndex() {
      return 0;
   }

   /**
    * Gibt die Wurzel der Äußerungseinheit zurück, der das Funktionswort
    * untergeordnet ist.
    */
   public IllocutionUnitRoot getRoot() {
      return root;
   }

   public String toString(String tab) {
      return tab + toString();
   }

   public String getInformation() {
      return "FunctionWord"
            + "\ncontent: "
            + getContent()
            + "\nid: "
            + getDB_ID()
            + "\nstart-pos: "
            + getStartPosition()
            + "\nend-pos: "
            + getEndPosition()
            + "\nword: ["
            + word.getInformation()
            + "]";
   }

   /**
    * Entfernt das Funktionswort aus der Wurzel. Ist das funktionswort
    * Bestandteil höherer Strukturen wie Semantischen Einheiten, so werden diese
    * auch gelöscht.
    */
   public boolean remove() {
      if (meaningUnit != null) {
         if (meaningUnit.remove()) {
            root.remove(this);
            changeState(REMOVE);
            return true;
         }
      }
      else {
         root.remove(this);
         changeState(REMOVE);
         return true;
      }

      if (sememeGroup != null)
         sememeGroup.remove();

      return false;
   }
   
   //TODO: Zugriff auf die Assignation sollte nicht direkt geschehen, sondern restriktiv bzw. selectiv über cw eigene methoden.
   // Warum? Eine Assignation kann mehrere / wiedersprüchliche werte annehmen.
   

   /**
    * @return the assignation
    */
   public TR_Assignation getAssignation() {
	   return assignation;
   }

   /**
    * @param assignation the assignation to set
    */
   public void setAssignation(TR_Assignation assignation) {
	   this.assignation = assignation;
	   changeState(CHANGE);
   }

   /**
    * Setzt die Assignation mit der DB-ID db_id.
    * @param assignation
    * @param db_id
    */
   public void setAssignation(TR_Assignation assignation, int db_id){
	      this.assignation = assignation;
	      this.assignation.setDB_ID(db_id);
	      changeState(CHANGE);
   }
   
    public Chapter getChapter() {
        return this.root.getChapter();
    }

}