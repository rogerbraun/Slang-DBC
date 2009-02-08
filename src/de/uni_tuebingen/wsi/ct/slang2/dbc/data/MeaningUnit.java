/*
 * Erstellt: 22.01.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.WordNotInIllocutionUnitException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.pathselector.PathSelector;


/**
 * Eine semantische Einheit, die aus genau einem semantisch konstitutiven Wort und
 * eventuell einem Funktionswort besteht. Dabei können auch Teilworte
 * berücksicht werden.
 * 
 * @author Volker Klöbb
 */
public class MeaningUnit extends DB_Element
      implements
         IllocutionUnitElement {

   /**
    * 
    */
   private static final long  serialVersionUID = -7700696916108448465L;
   private IllocutionUnitRoot root;
   private FunctionWord       functionWord;
   private ConstitutiveWord   constitutiveWord;
   private Vector             sememeGroups;
   private int                path;
   private int 				  numerusPath;
   private boolean            accepted;


   /**
 * @param key
 * @param root
 * @param id
 * @param functionWord
 * @param constitutiveWord
 * @param path
 * @param accepted
 */
public MeaningUnit(DBC_Key key,
         IllocutionUnitRoot root, /* should be dropped in favor of getting the root from constitutiveWord.getRoot() */
         int id,
         FunctionWord functionWord,
         ConstitutiveWord constitutiveWord,
         int path, int numerusPath,
         boolean accepted) {
      super(id);
        
      if(key == null || root == null || constitutiveWord == null)
	  throw new NullPointerException();
      
      if( ! root.equals(constitutiveWord.getRoot()))
	  throw new IllegalArgumentException();
      
      if( functionWord != null && ! root.equals(functionWord.getRoot()))
	  throw new IllegalArgumentException();
      
      this.root = root;
      this.functionWord = functionWord;
      this.constitutiveWord = constitutiveWord;
      this.path = path;
      this.numerusPath = numerusPath;
      this.accepted = accepted;
      sememeGroups = new Vector<SememeGroup>();

      if (functionWord != null) {
         functionWord.setMeaningUnit(key, this);
         root.remove(functionWord);
      }

      constitutiveWord.setMeaningUnit(key, this);
      root.remove(constitutiveWord);

      root.add(this);
      root.register(this);
   }

   /**
    * Erstellt eine neue semantische Einheit
    * 
    * @param root
    *        Die Wurzel der Äußerungseinheit
    * @param functionWord
    *        Das Funktionswort
    * @param constitutiveWord
    *        Das semantisch konstitutive Wort
    * @throws WordNotInIllocutionUnitException
    */
   public MeaningUnit(IllocutionUnitRoot root,
         FunctionWord functionWord,
         ConstitutiveWord constitutiveWord)
         throws WordNotInIllocutionUnitException {
      super(-1);
      this.root = root;
      this.functionWord = functionWord;
      this.constitutiveWord = constitutiveWord;
      sememeGroups = new Vector();

      if (!root.contains(functionWord) || !root.contains(constitutiveWord))
         throw new WordNotInIllocutionUnitException();

      functionWord.setMeaningUnit(this);
      constitutiveWord.setMeaningUnit(this);

      root.remove(functionWord);
      root.remove(constitutiveWord);
      if (root.add(this))
         root.register(this);

   }

   /**
    * Erstellt eine neue semantische Einheit
    * 
    * @param root
    *        Die Wurzel der Äußerungseinheit
    * @param constitutiveWord
    *        Das semantisch konstitutive Wort
    * @throws WordNotInIllocutionUnitException
    */
   public MeaningUnit(IllocutionUnitRoot root, ConstitutiveWord constitutiveWord)
         throws WordNotInIllocutionUnitException {
      super(-1);
      this.root = root;
      this.constitutiveWord = constitutiveWord;
      sememeGroups = new Vector();

      if (!root.contains(constitutiveWord))
         throw new WordNotInIllocutionUnitException();

      constitutiveWord.setMeaningUnit(this);

      root.remove(constitutiveWord);
      if (root.add(this))
         root.register(this);

   }

   public boolean equals(Object o) {
      if (o instanceof MeaningUnit) {
         MeaningUnit mu = (MeaningUnit) o;
         boolean cb = constitutiveWord.equals(mu.constitutiveWord);
         if (functionWord != null && mu.functionWord != null)
            return cb && functionWord.equals(mu.functionWord);
         if (functionWord == null && mu.functionWord == null)
            return cb;
      }
      return false;
   }

   /**
    * Prüft, ob diese semantische Einheit schon akzeptiert wurde.
    * 
    */
   public boolean isAccepted() {
      return accepted;
   }

   /**
    * Legt fest, ob diese semantisch Einheit vom Benutzer abgesegnet wurde oder
    * nicht.
    * 
    */
   public void setAccepted(boolean accepted) {
      changeState(CHANGE);
      this.accepted = accepted;
   }

   /**
    * Gibt alle Sememegruppen zurück, bei denen diese semantische Einheit
    * dazugehört.
    */
   public Vector getSememeGroups() {
      return sememeGroups;
   }

   boolean addSememeGroup(SememeGroup sg) {
      if (!sememeGroups.contains(sg)) {
         sememeGroups.add(sg);
         return true;
      }
      return false;
   }

   boolean removeSememeGroup(SememeGroup sg) {
      return sememeGroups.remove(sg);
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
   }

   /**
    * Das semantisch konstitutive Wort dieser semantischen Einheit
    */
   public ConstitutiveWord getConstitutiveWord() {
      return constitutiveWord;
   }

   /**
    * Das Funktionswort der semantischen Einheit oder null, falls keines gesetzt
    * wurde.
    */
   public FunctionWord getFunctionWord() {
      return functionWord;
   }

   /**
    * Die Wurzel der übergeordneten Äußerungseinheit.
    */
   public IllocutionUnitRoot getRoot() {
      return root;
   }

   public String toString() {
      return toString("");
   }


   /**
    * gibt die mu ohne erweiterungen zurück
    */
   public String toWrittenString() {
	   if(functionWord == null)
	   	   return constitutiveWord.toString();
	   return functionWord.toString() + " " + constitutiveWord.toString();
   }

   public String toString(String tab) {
      if (functionWord == null)
         return tab + "{MU CW: " + constitutiveWord + "}";
      return tab
            + "{MU"
            + " FW: "
            + functionWord
            + " CW: "
            + constitutiveWord
            + "}";
   }

   /**
    * Wird bis jetzt nicht verwendet.
    */
   public int getIndex() {
      return 0;
   }

   /**
    * Die Startposition der semantischen Einheit. In diesem Fall das am weiteste
    * links stehende Wort.
    */
   public int getStartPosition() {
      int fw = Integer.MAX_VALUE;
      int cw = Integer.MAX_VALUE;
      if (functionWord != null)
         fw = functionWord.getStartPosition();
      if (constitutiveWord != null)
         cw = constitutiveWord.getStartPosition();
      return Math.min(fw, cw);
   }

   /**
    * Die Endposition der semantischen Einheit. Das am weitestens rechts
    * stehende Wort dieser semantischen Einheit.
    */
   public int getEndPosition() {
      int fw = Integer.MIN_VALUE;
      int cw = Integer.MIN_VALUE;
      if (functionWord != null)
         fw = functionWord.getEndPosition();
      if (constitutiveWord != null)
         cw = constitutiveWord.getEndPosition();
      return Math.max(fw, cw);
   }

   /**
    * Entfernt diese semantische Einheit und stellt das CW und evetuell, falls
    * vorhanden, das FW wieder her. Sememegruppen, die auf diese semantische
    * Einheit aufbauen, werden ebenso gelöscht.
    */
   public boolean remove() {
      root.remove(this);

      constitutiveWord.setMeaningUnit(null);
      root.add(constitutiveWord);

      if (functionWord != null) {
         functionWord.setMeaningUnit(null);
         root.add(functionWord);
      }

      for (int i = 0; i < sememeGroups.size(); i++) {
         SememeGroup sg = (SememeGroup) sememeGroups.get(i);
         sg.remove();
      }

      changeState(REMOVE);
      return false;
   }

   /**
    * Die ID des Pfades.
    */
   public int getPath() {
      return path;
   }

   /**
    * Setzt den Pafd dieser semantischen Einheit
    * 
    * @param pathID
    *        die ID des Pfades
    * @see PathSelector
    */
   public void setPath(int pathID) {
      changeState(CHANGE);
      path = pathID;
   }


   
   /**
    * Die ID des Numerus-Pfades.
    */
   public int getNumerusPath() {
      return numerusPath;
   }
   
   /**
    * Setzt den Numerus-Pfad dieser semantischen Einheit
    * 
    * @param pathID
    *        die ID des Pfades
    * @see PathSelector
    */
   public void setNumerusPath(int pathID) {
      changeState(CHANGE);
      numerusPath = pathID;
   }

   public boolean containsPosition(int position) {
      if (functionWord == null)
         return constitutiveWord.containsPosition(position);
      return functionWord.containsPosition(position)
            || constitutiveWord.containsPosition(position);
   }
}