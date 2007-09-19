/*
 * Erstellt: 31.10.2004
 */

package data;

import java.util.BitSet;

import connection.DBC_Key;
import exceptions.NoWordFoundAtPositionException;
import exceptions.OverlappingException;
import exceptions.PositionNotInTokenException;
import exceptions.UnequalTokensException;
import exceptions.WordNotInIllocutionUnitException;

/**
 * Ein semantisch konstitutives Wort (CW), oder auch nur ein Teilwort (siehe
 * Dach-decker)
 * 
 * @author Volker Kl�bb
 */
public class ConstitutiveWord extends DB_Element
      implements
         IllocutionUnitElement {

   private static final long  serialVersionUID = -2779193424865840627L;

   public static final int    LEXPRAG_E1       = 1;
   public static final int    LEXPRAG_E2       = 2;
   public static final int    LEXPRAG_E3       = 3;
   public static final int    LEXPRAG_P1       = 4;
   public static final int    LEXPRAG_P2       = 5;
   public static final int    LEXPRAG_P3       = 6;
   public static final int    LEXPRAG_A1       = 7;
   public static final int    LEXPRAG_A2       = 8;
   public static final int    LEXPRAG_A3       = 9;

   private IllocutionUnitRoot root;
   private MeaningUnit        meaningUnit;
   private transient Word     word;
   private int                start;
   private int                end;
   private boolean            accepted;
   private TR_Assignation     assignation;
   private int                lexpragPath      = 0;
   private int                lexpragLevel     = 0;
   private int                textGrPath       = 0;
   private int                semPath          = 0;

   /**
    * Wird vom DBC ben�tigt.
    */
   
   
   //todo: asssignation
   
   public ConstitutiveWord(DBC_Key key,
         IllocutionUnitRoot root,
         int id,
         Word word,
         int start,
         int end,
         boolean accepted,
         int lexpragPath,
         int lexpragLevel,
         int textGrPath,
         int semPath) {
      super(id);
      key.unlock();
      this.root = root;
      this.word = word;
      this.start = start;
      this.end = end;
      this.accepted = accepted;
      this.lexpragPath = lexpragPath;
      this.lexpragLevel = lexpragLevel;
      this.textGrPath = textGrPath;
      this.semPath = semPath;

      root.add(this);
      root.register(this);
   }

   /**
    * Erstellt ein neues CW, das sich auf eine komplettes Wort oder auch nur ein
    * Teilwort bezieht.
    * 
    * @param root
    *        Die Wurzel der �u�erungseinheit, unter der das CW zu finden ist
    * @param start
    *        Der Begin des CW's, bezogen auf Zeichenpositionen
    * @param end
    *        Das Ende des CW's, bezogen auf Zeichenpositionen
    * @throws PositionNotInTokenException
    * @throws NoWordFoundAtPositionException
    * @throws WordNotInIllocutionUnitException
    * @throws OverlappingException
    * @throws UnequalTokensException
    */
   public ConstitutiveWord(IllocutionUnitRoot root, int start, int end)
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
      accepted = false;

      if (root.add(this))
         root.register(this);
   }

   /**
    * Erstellt ein neues CW, das sich auf ein komplettes Wort bezieht
    * 
    * @param root
    *        Die Wurzel der �u�erungseinheit, unter der das CW zu finden ist
    * @param word
    *        Das Wort, welches zu einem CW werden soll.
    * @throws PositionNotInTokenException
    * @throws NoWordFoundAtPositionException
    * @throws WordNotInIllocutionUnitException
    * @throws OverlappingException
    * @throws UnequalTokensException
    */
   public ConstitutiveWord(IllocutionUnitRoot root, Word word)
         throws PositionNotInTokenException, NoWordFoundAtPositionException,
         WordNotInIllocutionUnitException, OverlappingException,
         UnequalTokensException {
      this(root, word.getStartPosition(), word.getEndPosition());
   }

   public boolean equals(Object o) {
      if (o instanceof ConstitutiveWord) {
         ConstitutiveWord cw = (ConstitutiveWord) o;
         return start == cw.start && end == cw.end;
      }
      return false;
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      word = (Word) chapter.getTokenAtPosition(start);
   }

   /**
    * Pr�ft, ob dieses semantisch konstitutive Wort schon akzeptiert wurde.
    * 
    */
   public boolean isAccepted() {
      return accepted;
   }

   /**
    * Legt fest, ob dieses semantisch konstitutive Wort vom Benutzer abgesegnet
    * wurde oder nicht.
    * 
    */
   public void setAccepted(boolean accepted) {
      changeState(CHANGE);
      this.accepted = accepted;
   }

   /**
    * Die Semantische Einheit des CW
    * 
    * @return die Semantische Einheit oder null, falls noch keine bestimmt wurde
    */
   public MeaningUnit getMeaningUnit() {
      return meaningUnit;
   }

   public void setMeaningUnit(MeaningUnit meaningUnit) {
      this.meaningUnit = meaningUnit;
      changeState(CHANGE);
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public void setMeaningUnit(DBC_Key key, MeaningUnit meaningUnit) {
      key.unlock();
      this.meaningUnit = meaningUnit;
   }

  

   /**
    * Achtung! F�r ein Wort sind mehrere Bestimmung m�glich, also kann mit dem
    * R�ckgabewert unter Umst�nden nicht viel angefangen werden.
    */
   
  /* public boolean hasTrCase(int trCase) {
      return assignation.hasCase(trCase);
   }*/

   
  /* public void removeTrCase(int trCase) {
      assignation.removeCase(trCase);
      changeState(CHANGE);
   }*/

   
      
   public void setAssignation(TR_Assignation assignation){
      this.assignation = assignation;
      changeState(CHANGE);
   }
   
   public TR_Assignation getAssignation() {
      return assignation;
   }

   /**
    * Gibt das Wort zur�ck, auf dem das CW aufbaut. Bezieht sich das CW nur auf
    * ein Teilwort, wird trotzdem das komplette Wort zur�ckgegeben.
    */
   public Word getWord() {
      return word;
   }

   /**
    * Das Wort als String
    */
   public String toString() {
      return getContent();
   }

   public String getContent() {
      return root.getChapter().getContent(start, end);
   }

   public void setLexpragPath(int path) {
      lexpragPath = path;
      changeState(CHANGE);
   }

   public int getLexpragPath() {
      return lexpragPath;
   }

   public void setLexpragLevel(int level) {
      lexpragLevel = level;
      changeState(CHANGE);
   }

   public int getLexpragLevel() {
      return lexpragLevel;
   }

   public void setTextGrPath(int path) {
      textGrPath = path;
      changeState(CHANGE);
   }

   public int getTextGrPath() {
      return textGrPath;
   }

   public void setSemPath(int path) {
      semPath = path;
      changeState(CHANGE);
   }

   public int getSemPath() {
      return semPath;
   }

   /**
    * Keine Bedeutung
    */
   public int getIndex() {
      return 0;
   }

   /**
    * Gibt die Wurzel der �u�erungseinheit zur�ck, der das CW untergeordnet ist.
    */
   public IllocutionUnitRoot getRoot() {
      return root;
   }

   /**
    * Gibt die Zeichenposition von dem Begin des CW zur�ck
    */
   public int getStartPosition() {
      return start;
   }

   /**
    * Gibt die Zeichenposition von dem Ende des CW zur�ck
    */
   public int getEndPosition() {
      return end;
   }

   public boolean containsPosition(int position) {
      return start <= position && position <= end;
   }

   public String toString(String tab) {
      return tab + this;
   }

   public String getInformation() {
      return "ConstitutiveWord"
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
    * L�scht das CW und stellt das eigentliche Wort wieder her. Alle
    * �bergeordneten Strukturen wie Semantische Einheit werden auch gel�scht
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
      return false;
   }

}