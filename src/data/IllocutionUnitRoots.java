/*
 * Erstellt: 25.03.2005
 */

package data;

import java.io.Serializable;
import java.util.Vector;

import connection.DBC;
import connection.DBC_Key;

/**
 * Eine Sammlung von �u�erungseinheiten-Wurzel eines ganzen Kapitels. Diese
 * Datenstruktur wird vom DBC geladen und gespeichert.
 * 
 * @author Volker Kl�bb
 * @see DBC#loadIllocutionUnitRoots(Chapter)
 * @see DBC#saveIllocutionUnitRoots(IllocutionUnitRoots)
 */
public class IllocutionUnitRoots
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1203045166922169362L;
   private transient Chapter chapter;
   private Vector            roots;
   private Vector            checkings;
   private Vector            macroSentences;

   /**
    * Wird vom DBC ben�tigt
    */
   public IllocutionUnitRoots(DBC_Key key, Chapter chapter, Vector roots) {
      key.unlock();
      this.chapter = chapter;
      this.roots = roots;
      this.checkings = new Vector();
      this.macroSentences = new Vector();
   }

   void addChecking(Checking ch) {
      for (int i = 0; i < checkings.size(); i++) {
         Checking c = (Checking) checkings.get(i);
         if (c.getIndex() > ch.getIndex()) {
            checkings.add(i, ch);
            return;
         }
         if (!c.isRemoved() && ch.equals(c))
            return;
      }
      checkings.add(ch);
   }

   public Vector getCheckingsAtIndex(int index) {
      Vector res = new Vector();
      for (int i = 0; i < checkings.size(); i++) {
         Checking c = (Checking) checkings.get(i);
         if (!c.isRemoved() && c.containsIndex(index))
            res.add(c);
      }
      return res;
   }

   public Checking getCheckingAtPosition(int position) {
      for (int i = 0; i < checkings.size(); i++) {
         Checking c = (Checking) checkings.get(i);
         if (!c.isRemoved() && c.containsPosition(position))
            return c;
      }
      return null;
   }

   public Vector getCheckings() {
      Vector chs = new Vector();
      for (int i = 0; i < checkings.size(); i++) {
         Checking c = (Checking) checkings.get(i);
         if (!c.isRemoved())
            chs.add(c);
      }
      return chs;
   }

   public Vector getCheckings(DBC_Key key) {
      key.unlock();
      return checkings;
   }

   public void addMacroSentence(MacroSentence ms) {
      for (int i = 0; i < macroSentences.size(); i++) {
         MacroSentence m = (MacroSentence) macroSentences.get(i);
         if (m.getIndex() > ms.getIndex()) {
            macroSentences.add(i, ms);
            return;
         }
         if (!m.isRemoved() && m.getIndex() == ms.getIndex())
            return;
      }
      macroSentences.add(ms);
   }

   /**
    * Gibt den Makrosatz zur�ck, der an diesem Index steht
    */
   public MacroSentence getMacroSentenceAtIndex(int index) {
      for (int i = 0; i < macroSentences.size(); i++) {
         MacroSentence ms = (MacroSentence) macroSentences.get(i);
         if (!ms.isRemoved() && ms.containsIndex(index))
            return ms;
      }
      return null;
   }

   /**
    * Gibt den Makrosatz zur�ck, der an dieser Zeichenposition steht.
    */
   public MacroSentence getMacroSentenceAtPosition(int position) {
      for (int i = 0; i < macroSentences.size(); i++) {
         MacroSentence ms = (MacroSentence) macroSentences.get(i);
         if (!ms.isRemoved() && ms.containsPosition(position))
            return ms;
      }
      return null;
   }

   /**
    * Gibt alle Makros�tze des Kapitels in einem neuen(!) Vektor zur�ck.
    */
   public Vector getMacroSentences() {
      Vector mss = new Vector();
      for (int i = 0; i < macroSentences.size(); i++) {
         MacroSentence ms = (MacroSentence) macroSentences.get(i);
         if (!ms.isRemoved())
            mss.add(ms);
      }
      return mss;
   }

   /**
    * Wird vom DBC ben�tigt
    */
   public Vector getMacroSentences(DBC_Key key) {
      key.unlock();
      return macroSentences;
   }

   /**
    * Das Kapitel, zu dem die ganzen �u�erungseinheiten geh�ren
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Wird vom DBC ben�tigt
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      this.chapter = chapter;

      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         root.setChapter(key, chapter);
      }
   }

   /**
    * Wird vom DBC ben�tigt
    */
   public void updateIDs(DBC_Key key, IllocutionUnitRoots answer) {
      key.unlock();

      for (int i = 0; i < Math.min(roots.size(), answer.roots.size()); i++) {
         IllocutionUnitRoot r1 = (IllocutionUnitRoot) roots.get(i);
         IllocutionUnitRoot r2 = (IllocutionUnitRoot) answer.roots.get(i);
         r1.updateIDs(key, r2);
      }

      for (int i = 0; i < Math.min(checkings.size(), answer.checkings.size()); i++) {
         Checking c1 = (Checking) checkings.get(i);
         Checking c2 = (Checking) answer.checkings.get(i);
         c1.updateIDs(key, c2);
      }

      for (int i = 0; i < Math.min(macroSentences.size(), answer.macroSentences
            .size()); i++) {
         MacroSentence ms1 = (MacroSentence) macroSentences.get(i);
         MacroSentence ms2 = (MacroSentence) answer.macroSentences.get(i);
         ms1.updateIDs(key, ms2);
      }
   }

   /**
    * Alle Wurzeln in einem Vektor
    */
   public Vector getRoots() {
      return roots;
   }

   /**
    * Gibt die Wurzel zur�ck, deren �u�erungseinheit an diesem Index zu finden
    * ist.
    * 
    * @param index
    *        der Index der �u�erungseinheit
    * @return Die Wurzel zu der �u�erungseinheit
    */
   public IllocutionUnitRoot getRoot(int index) {
      return (IllocutionUnitRoot) roots.get(index);
   }

   /**
    * Gibt die Wurzel zu der �u�erungseinheit zur�ck.
    * 
    * @param iu
    *        die �u�erungseinheit
    * @return Die Wurzel zu der �u�erungseinheit
    */
   public IllocutionUnitRoot getRoot(IllocutionUnit iu) {
      return getRoot(iu.getIndex());
   }

   /**
    * Gibt die Wurzel zur�ck, deren �u�erungseinheit diesen Token beinhaltet.
    * 
    * @param token
    *        die Position der �u�erungseinheit
    * @return Die Wurzel zu der �u�erungseinheit
    */
   public IllocutionUnitRoot getRoot(Token token) {
      return getRoot(token.getIllocutionUnit());
   }

   /**
    * Gibt die Wurzel zur�ck, die diese Zeichenposition abdeckt
    * 
    * @param position
    *        die Zeichenposition
    * @return die Wurzel an dieser Zeichenposition
    */
   public IllocutionUnitRoot getRootAtPosition(int position) {
      return getRoot(chapter.getIllocutionUnitAtPosition(position));
   }

   public IllocutionUnitRoot getRootWithID(int id) {
      return getRoot(chapter.getIllocutionUnitWithID(id));
   }

   /**
    * Gibt die Funktionsw�rter zur�ck, die auf dem Wort aufbauen, welches an dem
    * besagten Index steht.
    */
   public Vector getFunctionWordsAtIndex(int index) {
      IllocutionUnitRoot iur = getRoot(chapter.getTokenAtIndex(index));
      Vector fws = iur.getFunctionWords();
      Vector res = new Vector();
      for (int i = 0; i < fws.size(); i++) {
         FunctionWord fw = (FunctionWord) fws.get(i);
         if (fw.getWord().getIndex() == index)
            res.add(fw);
      }
      return res;
   }

   /**
    * Gibt das Funktionswort zur�ck, das diese Zeichenposition abdeckt. Steht an
    * dieser Stelle kein Token, wird null zur�ckgegeben.
    * 
    * @param position
    *        Die Zeichenposition
    * @return Das FW oder null, falls an dieser Stelle kein FW gespeichert
    *         wurde.
    */
   public FunctionWord getFunctionWordAtPosition(int position) {
      Token token = chapter.getTokenAtPosition(position);
      if (token != null) {
         IllocutionUnitRoot iur = getRoot(token);
         Vector fws = iur.getFunctionWords();
         for (int i = 0; i < fws.size(); i++) {
            FunctionWord fw = (FunctionWord) fws.get(i);
            if (fw.getStartPosition() <= position
                  && position <= fw.getEndPosition())
               return fw;
         }
      }
      return null;
   }

   /**
    * Gibt die Funktionsw�rter zur�ck, die auf dem �bergebenen Wort aufbauen.
    * 
    * @param word
    *        Das zugrundeliegende Wort der Funktionsw�rter
    * @return Die Funktionsw�rter, die auf dem Wort aufbauen.
    */
   public Vector getFunctionWords(Word word) {
      IllocutionUnitRoot iur = getRoot(word);
      Vector fws = iur.getFunctionWords();
      Vector res = new Vector();
      for (int i = 0; i < fws.size(); i++) {
         FunctionWord fw = (FunctionWord) fws.get(i);
         if (fw.getWord() == word)
            res.add(fw);
      }
      return res;
   }

   /**
    * Gibt die konstitutiven W�rter zur�ck, die auf dem Wort aufbauen, welches
    * an dem Index steht.
    */
   public Vector getConstitutiveWordsAtIndex(int index) {
      IllocutionUnitRoot iur = getRoot(chapter.getTokenAtIndex(index));
      Vector cws = iur.getConstitutiveWords();
      Vector res = new Vector();
      for (int i = 0; i < cws.size(); i++) {
         ConstitutiveWord cw = (ConstitutiveWord) cws.get(i);
         if (cw.getWord().getIndex() == index)
            res.add(cw);
      }
      return res;
   }

   /**
    * Gibt das Konstitutive Wort zur�ck, das diese Zeichenposition abdeckt.
    * Steht an dieser Stelle kein Token, wird null zur�ckgegeben.
    * 
    * @param position
    *        Die Zeichenposition
    * @return Das CW oder null, falls an dieser Stelle kein CW gespeichert
    *         wurde.
    */
   public ConstitutiveWord getConstitutiveWordAtPosition(int position) {
      Token token = chapter.getTokenAtPosition(position);
      if (token != null) {
         IllocutionUnitRoot iur = getRoot(token);
         Vector cws = iur.getConstitutiveWords();
         for (int i = 0; i < cws.size(); i++) {
            ConstitutiveWord cw = (ConstitutiveWord) cws.get(i);
            if (cw.getStartPosition() <= position
                  && position <= cw.getEndPosition())
               return cw;
         }
      }
      return null;
   }

   public boolean existsConstitutiveWord(Word word,
         String cwContent,
         boolean ignoreCase) {
      IllocutionUnitRoot iur = getRoot(word);
      Vector cws = iur.getConstitutiveWords();
      for (int i = 0; i < cws.size(); i++) {
         ConstitutiveWord cw = (ConstitutiveWord) cws.get(i);
         if (cw.getWord() == word) {
            if (ignoreCase) {
               if (cw.getContent().equalsIgnoreCase(cwContent))
                  return true;
            }
            else if (cw.getContent().equals(cwContent))
               return true;
         }
      }
      return false;
   }

   public boolean existsFunctionWord(Word word,
         String fwContent,
         boolean ignoreCase) {
      IllocutionUnitRoot iur = getRoot(word);
      Vector fws = iur.getFunctionWords();
      for (int i = 0; i < fws.size(); i++) {
         FunctionWord fw = (FunctionWord) fws.get(i);
         if (fw.getWord() == word) {
            if (ignoreCase) {
               if (fw.getContent().equalsIgnoreCase(fwContent))
                  return true;
            }
            else if (fw.getContent().equals(fwContent))
               return true;
         }
      }
      return false;
   }

   /**
    * Gibt die konstitutiven W�rter zur�ck, die auf diesem Wort aufbauen.
    * 
    * @param word
    *        Das zugrunde liegende Wort der konstitutiven W�rter
    * @return Ein Vektor mit allen konstitutiven W�rtern, die auf diesem Wort
    *         aufbauen
    */
   public Vector getConstitutiveWords(Word word) {
      IllocutionUnitRoot iur = getRoot(word);
      Vector cws = iur.getConstitutiveWords();
      Vector res = new Vector();
      for (int i = 0; i < cws.size(); i++) {
         ConstitutiveWord cw = (ConstitutiveWord) cws.get(i);
         if (cw.getWord() == word)
            res.add(cw);
      }
      return res;
   }

   /**
    * Alle konstitutiven W�rter
    */
   public Vector getConstitutiveWords() {
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getConstitutiveWords());
      }
      return res;
   }

   /**
    * Liefert das CW mit der ID
    * 
    * @param id
    *        die DB-ID, zu der das CW gefunden werden soll
    * @return Das CW mit dieser ID oder null
    */
   public ConstitutiveWord getConstitutiveWordWithID(int id) {
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         Vector words = root.getConstitutiveWords();
         for (int j = 0; j < words.size(); j++) {
            ConstitutiveWord cw = (ConstitutiveWord) words.get(j);
            if (cw.getDB_ID() == id)
               return cw;
         }
      }
      return null;
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public Vector getConstitutiveWords(DBC_Key key) {
      key.unlock();
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getConstitutiveWords(key));
      }
      return res;
   }

   /**
    * Alle Funktionsw�rter
    */
   public Vector getFunctionWords() {
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getFunctionWords());
      }
      return res;
   }

   /**
    * Wird vom DBC ben�tigt
    */
   public Vector getFunctionWords(DBC_Key key) {
      key.unlock();
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getFunctionWords(key));
      }
      return res;
   }

   /**
    * Alle Semantischen Einheiten.
    */
   public Vector getMeaningUnits() {
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getMeaningUnits());
      }
      return res;
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public Vector getMeaningUnits(DBC_Key key) {
      key.unlock();
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getMeaningUnits(key));
      }
      return res;
   }

   /**
    * Alle Sememegruppen
    */
   public Vector getSememeGroups() {
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getSememeGroups());
      }
      return res;
   }

   /**
    * Wird vom DBC ben�tigt.
    */
   public Vector getSememeGroups(DBC_Key key) {
      key.unlock();
      Vector res = new Vector();
      for (int i = 0; i < roots.size(); i++) {
         IllocutionUnitRoot root = (IllocutionUnitRoot) roots.get(i);
         res.addAll(root.getSememeGroups(key));
      }
      return res;
   }

   /**
    * Pr�ft, ob schon eine semantische Einheit mit diesen Funktionswort und
    * diesem konstitutiven Wort existiert. Die beiden W�rter m�ssen unter der
    * selben Wurzel stehen, ansonsten wird false zur�ckgegeben.
    */
   public boolean existMeaningUnit(FunctionWord fw, ConstitutiveWord cw) {
      return fw.getRoot().existMeaningUnit(fw, cw);
   }

   /**
    * Pr�ft, ob schon eine semantische Einheit mit diesen Funktionswort
    * existiert.
    */
   public boolean existMeaningUnit(ConstitutiveWord cw) {
      return cw.getRoot().existMeaningUnit(cw);
   }

   /**
    * �berpr�ft, ob in diesem Kapitel schon ein Funktionswort zu diesem Wort
    * angelegt wurde. Teilw�rter werden dabei nicht beachtet.
    * 
    * @param word
    *        Das zu �berpr�fende Wort.
    * @return true, falls es in diesem Kapitel ein Funktionswort zu dem Wort
    *         gibt.
    */
   public boolean existsFunctionWord(Word word) {
      Vector words = chapter.getWords(word);
      for (int i = 0; i < words.size(); i++) {
         Word w = (Word) words.get(i);
         IllocutionUnitRoot iur = getRoot(w);
         if (iur.existsFunctionWord(word))
            return true;
      }
      return false;
   }

   /**
    * Wird vom DBC ben�tigt
    */
   public void fillCaches(DBC_Key key) {
      for (int i = 0; i < roots.size(); i++)
         ((IllocutionUnitRoot) roots.get(i)).fillCache();
   }

   /**
    * �berpr�ft, ob in diesem Kapitel schon ein konstitutives Wort zu diesem
    * Wort angelegt wurde. Dabei wird der Content des Wortes ber�cksichtigt, die
    * Position ist also unwichtig. Teilw�rter werden dabei nicht beachtet.
    * 
    * @param word
    *        Das zu �berpr�fende Wort.
    * @return true, falls es in diesem Kapitel ein konstitutives Wort zu dem
    *         Wort gibt.
    */
   public boolean existsConstitutiveWord(Word word) {
      Vector words = chapter.getWords(word);
      for (int i = 0; i < words.size(); i++) {
         Word w = (Word) words.get(i);
         IllocutionUnitRoot iur = getRoot(w);
         if (iur.existsConstitutiveWord(word))
            return true;
      }
      return false;
   }

   void resetIDs() {
      for (int i = 0; i < roots.size(); i++)
         ((IllocutionUnitRoot) roots.get(i)).resetIDs();
      for (int i = 0; i < checkings.size(); i++)
         ((Checking) checkings.get(i)).resetIDs();
      for (int i = 0; i < macroSentences.size(); i++)
         ((MacroSentence) macroSentences.get(i)).resetIDs();
   }

   public String toString() {
      return roots.toString();
   }
}