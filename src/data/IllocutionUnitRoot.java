/*
 * Erstellt: 28.01.2005
 */

package data;

import java.io.Serializable;
import java.util.Vector;

import pathselector.PathSelector;
import connection.DBC_Key;

/**
 * Die Wurzel eine Äußerungseinheit. Hier werden Sememegruppen, Semantische
 * Einheiten oder isolierte Funktionswörter, usw. in einer Baumstruktur
 * gespeichert.
 * 
 * @author Volker Klöbb
 */
public class IllocutionUnitRoot extends DB_Element
      implements
         Serializable {

   /**
    * 
    */
   private static final long        serialVersionUID = 5812670241878239329L;
   private transient Chapter        chapter;
   private transient IllocutionUnit illocutionUnit;
   private int                      illocutionUnitIndex;
   private int                      path;
   private Vector                   elements;
   private Vector                   allElements;
   private Vector                   checkings;
   private transient Vector         fwsCache;
   private transient Vector         cwsCache;
   private transient Vector         musCache;
   private transient Vector         sgsCache;

   /**
    * Wird vom DBC benötigt
    */
   public IllocutionUnitRoot(DBC_Key key, IllocutionUnit iu, int path) {
      super(iu.getDB_ID());
      key.unlock();
      chapter = iu.getChapter();
      illocutionUnit = iu;
      illocutionUnitIndex = iu.getIndex();
      this.path = path;
      elements = new Vector();
      allElements = new Vector();
      checkings = new Vector();
   }

   void addChecking(Checking checking) {
      if (!checkings.contains(checking))
         checkings.add(checking);
   }

   void removeChecking(Checking checking) {
      checkings.remove(checking);
   }

   /**
    * Gibt alle Checkings zurück, die in dieser Äußerungseinheit vorkommen
    */
   public Vector getCheckings() {
      return checkings;
   }

   /**
    * Gibt der/die/das Checking zurück, welches sich auf die übergebene
    * MeaningUnit bezieht oder null, falls es keines gibt.
    * 
    * @param meaningUnit
    *        Die MeaningUnit, zu der das passende Checking gefunden werden soll.
    */
   public Checking getChecking(MeaningUnit meaningUnit) {
      for (int i = 0; i < checkings.size(); i++) {
         Checking c = (Checking) checkings.get(i);
         if (c.getMeaningUnit() == meaningUnit)
            return c;
      }
      return null;
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      this.chapter = chapter;
      illocutionUnit = chapter.getIllocutionUnitAtIndex(illocutionUnitIndex);

      for (int i = 0; i < allElements.size(); i++) {
         IllocutionUnitElement iue = (IllocutionUnitElement) allElements.get(i);
         iue.setChapter(key, chapter);
      }
   }

   /**
    * Das Kapitel, in dem die Äußerungseinheit dieser Wurzel vorkommt
    */
   public Chapter getChapter() {
      return chapter;
   }

   /**
    * Die ID des Pfades.
    */
   public int getPath() {
      return path;
   }

   /**
    * Setzt den Pafd dieser Äußerungseinheit
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
    * Die Äußerungseinheit, zu der diese Wurzel gehört
    */
   public IllocutionUnit getIllocutionUnit() {
      return illocutionUnit;
   }

   void register(IllocutionUnitElement element) {
      if (!allElements.contains(element))
         allElements.add(element);
   }

   /**
    * Fügt ein neues Element an die Stelle ein, die der Wort-Position
    * entspricht.
    * 
    * @param element
    *        das einzufügende Element
    * @return true, wenn das Element eingefügt werden konnte, sonst false.
    */
   boolean add(IllocutionUnitElement element) {
      // teste, ob Element zu dieser Wurzel gehört
      if (!illocutionUnit.containsPosition(element.getStartPosition()))
         return false;

      for (int i = 0; i < elements.size(); i++) {
         if (elements.get(i).equals(element))
            return false;
      }

      elements.add(element);
      resetCache(element);
      return true;
   }

   void remove(IllocutionUnitElement element) {
      elements.remove(element);
      resetCache(element);
   }

   private void resetCache(IllocutionUnitElement element) {
      if (element instanceof FunctionWord)
         fwsCache = null;
      else if (element instanceof ConstitutiveWord)
         cwsCache = null;
      else if (element instanceof MeaningUnit)
         musCache = null;
      else if (element instanceof SememeGroup)
         sgsCache = null;
   }

   void fillCache() {
      // getFunctionWords();
      // getConstitutiveWords();
      // getMeaningUnits();
      // getSememeGroups();

      fwsCache = new Vector();
      cwsCache = new Vector();
      musCache = new Vector();
      sgsCache = new Vector();

      for (int i = 0; i < allElements.size(); i++) {
         Object o = allElements.get(i);
         if (o instanceof FunctionWord)
            fwsCache.add(o);
         else if (o instanceof ConstitutiveWord)
            cwsCache.add(o);
         else if (o instanceof MeaningUnit)
            musCache.add(o);
         else if (o instanceof SememeGroup)
            sgsCache.add(o);
      }
   }

   /**
    * Überprüft, ob das Element schon eingefügt wurde
    * 
    * @param element
    *        das zu prüfende Element
    * @return true, falls das Element vorhanden ist
    */
   boolean contains(IllocutionUnitElement element) {
      return elements.contains(element);
   }

   /**
    * Wird vom DBC benötigt.
    */
   public Vector getMeaningUnits(DBC_Key handler) {
      Vector mus = getMeaningUnits();
      for (int i = 0; i < allElements.size(); i++) {
         if (allElements.get(i) instanceof MeaningUnit
               && !mus.contains(allElements.get(i)))
            mus.add(allElements.get(i));
      }
      return mus;
   }

   /**
    * Wird vom DBC benötigt.
    */
   public Vector getFunctionWords(DBC_Key handler) {
      Vector fws = getFunctionWords();
      for (int i = 0; i < allElements.size(); i++) {
         if (allElements.get(i) instanceof FunctionWord
               && !fws.contains(allElements.get(i)))
            fws.add(allElements.get(i));
      }
      return fws;
   }

   /**
    * Wird vom DBC benötigt.
    */
   public Vector getConstitutiveWords(DBC_Key handler) {
      Vector cws = getConstitutiveWords();
      for (int i = 0; i < allElements.size(); i++) {
         if (allElements.get(i) instanceof ConstitutiveWord
               && !cws.contains(allElements.get(i)))
            cws.add(allElements.get(i));
      }
      return cws;
   }

   /**
    * Alle semantischen Einheiten dieser Äußerungseinheit.
    */
   public Vector getMeaningUnits() {
      if (musCache != null)
         return musCache;

      Vector mus = new Vector();

      for (int i = 0; i < elements.size(); i++) {
         if (elements.get(i) instanceof MeaningUnit) {
            MeaningUnit mu = (MeaningUnit) elements.get(i);
            mus.add(mu);
         }
      }

      musCache = mus;
      return mus;
   }

   /**
    * Gibt die MeaningUnit an Stelle "position" zurück, oder null, falls keine
    * gefunden wurde.
    * 
    * @param position
    *        die Zeichenposition, die überprüft werden soll
    */
   public MeaningUnit getMeaningUnitAtPosition(int position) {
      Vector mus = getMeaningUnits();
      for (int i = 0; i < mus.size(); i++) {
         MeaningUnit mu = (MeaningUnit) mus.get(i);
         if (mu.containsPosition(position))
            return mu;
      }
      return null;
   }

   /**
    * Alle Sememegruppen dieser Äußerungseinheit
    */
   public Vector getSememeGroups() {
      if (sgsCache != null)
         return sgsCache;

      Vector sgs = new Vector();

      for (int i = 0; i < elements.size(); i++) {
         if (elements.get(i) instanceof SememeGroup) {
            SememeGroup sg = (SememeGroup) elements.get(i);
            sgs.add(sg);
         }
      }

      sgsCache = sgs;
      return sgs;
   }

   /**
    * Wird vom DBC benötigt.
    */
   public Vector getSememeGroups(DBC_Key handler) {
      Vector sgs = getSememeGroups();
      for (int i = 0; i < allElements.size(); i++) {
         if (allElements.get(i) instanceof SememeGroup
               && !sgs.contains(allElements.get(i)))
            sgs.add(allElements.get(i));
      }
      return sgs;
   }

   /**
    * Überprüft diese Wurzel, ob schon eine Semantische Einheit mit diesen
    * Funktions- und konstitutiven Worten existiert.
    */
   public boolean existMeaningUnit(FunctionWord fw, ConstitutiveWord cw) {
      Vector mus = getMeaningUnits();
      for (int i = 0; i < mus.size(); i++) {
         MeaningUnit m = (MeaningUnit) mus.get(i);
         if (fw == m.getFunctionWord() && cw == m.getConstitutiveWord())
            return true;
      }
      return false;
   }

   /**
    * Überprüft diese Wurzel, ob schon eine Semantische Einheit mit diesen
    * Funktionswort existiert.
    */
   public boolean existMeaningUnit(ConstitutiveWord cw) {
      return existMeaningUnit(null, cw);
   }

   /**
    * Gibt alle isolierten Funktionswörter der Wurzel in einem Vektor zurück.
    */
   public Vector getIsolatedFunctionWords() {
      Vector fws = new Vector();
      for (int i = 0; i < elements.size(); i++) {
         if (elements.get(i) instanceof FunctionWord) {
            FunctionWord fw = (FunctionWord) elements.get(i);
            if (fw.getSememeGroup() == null)
               fws.add(fw);
         }
      }
      return fws;
   }

   /**
    * Gibt alle Funktionswörter dieser Wurzel in einem Vektor zurück. Dazu
    * zählen isolierte Funktionswörter, Funktionswörter von semantischen
    * Einheiten und Funktionswörter von Sememegruppen.
    */
   public Vector getFunctionWords() {
      if (fwsCache != null)
         return fwsCache;

      Vector fws = new Vector();
      for (int i = 0; i < elements.size(); i++) {
         FunctionWord fw = null;
         if (elements.get(i) instanceof FunctionWord)
            fw = (FunctionWord) elements.get(i);
         else if (elements.get(i) instanceof MeaningUnit) {
            MeaningUnit mu = (MeaningUnit) elements.get(i);
            if (mu.getFunctionWord() != null)
               fw = mu.getFunctionWord();
         }

         if (fw != null) {
            boolean insert = false;
            for (int j = 0; j < fws.size(); j++) {
               FunctionWord t = (FunctionWord) fws.get(j);
               if (fw.getStartPosition() < t.getStartPosition()) {
                  fws.add(j, fw);
                  insert = true;
                  break;
               }
            }
            if (!insert)
               fws.add(fw);
         }
      }

      fwsCache = fws;
      return fws;
   }

   /**
    * Gibt alle semantisch Constitutiven Wörter der Wurzel in einem Vektor
    * zurück. Dazu zählen freistehende CWs und CWs einer semantischen Einheit.
    */
   public Vector getConstitutiveWords() {
      if (cwsCache != null)
         return cwsCache;

      Vector cws = new Vector();
      for (int i = 0; i < elements.size(); i++) {
         ConstitutiveWord cw = null;
         if (elements.get(i) instanceof ConstitutiveWord)
            cw = (ConstitutiveWord) elements.get(i);
         else if (elements.get(i) instanceof MeaningUnit)
            cw = (((MeaningUnit) elements.get(i)).getConstitutiveWord());

         if (cw != null) {
            boolean insert = false;
            for (int j = 0; j < cws.size(); j++) {
               ConstitutiveWord t = (ConstitutiveWord) cws.get(j);
               if (cw.getStartPosition() < t.getStartPosition()) {
                  cws.add(j, cw);
                  insert = true;
                  break;
               }
            }
            if (!insert)
               cws.add(cw);
         }
      }

      cwsCache = cws;
      return cws;
   }

   boolean exitsConstitutiveWords(int start, int end) {
      Vector cws = getConstitutiveWords();
      for (int i = 0; i < cws.size(); i++) {
         ConstitutiveWord cw = (ConstitutiveWord) cws.get(i);
         if (cw.containsPosition(start)
               || cw.containsPosition(end)
               || (cw.getStartPosition() >= start && cw.getEndPosition() <= end))
            return true;
      }
      return false;
   }

   boolean exitsFunctionWords(int start, int end) {
      Vector fws = getFunctionWords();
      for (int i = 0; i < fws.size(); i++) {
         FunctionWord fw = (FunctionWord) fws.get(i);
         if (fw.containsPosition(start)
               || fw.containsPosition(end)
               || (fw.getStartPosition() >= start && fw.getEndPosition() <= end))
            return true;
      }
      return false;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer("{IU");
      for (int i = 0; i < elements.size(); i++) {
         IllocutionUnitElement e = (IllocutionUnitElement) elements.get(i);
         sb.append('\n');
         sb.append(e.toString("   "));

         if (e instanceof ConstitutiveWord)
            sb.append(" (CW)");
         if (e instanceof FunctionWord)
            sb.append(" (FW)");
         if (e instanceof MeaningUnit)
            sb.append(" (MU)");
         if (e instanceof SememeGroup)
            sb.append(" (SG)");
      }
      sb.append("}");
      return sb.toString();
   }

   /**
    * Der Index der Äußerungseinheit
    */
   public int getIndex() {
      return illocutionUnitIndex;
   }

   int getElementPosition(IllocutionUnitElement element) {
      return elements.indexOf(element);
   }

   public void updateIDs(DBC_Key key, IllocutionUnitRoot answer) {
      key.unlock();
      super.updateIDs(key, answer);

      Vector fws1 = getFunctionWords();
      Vector fws2 = answer.getFunctionWords();
      for (int i = 0; i < fws2.size(); i++) {
         FunctionWord fw2 = (FunctionWord) fws2.get(i);
         for (int j = 0; j < fws1.size(); j++) {
            FunctionWord fw1 = (FunctionWord) fws1.get(j);
            if (fw2.equals(fw1)) {
               fw1.updateIDs(key, fw2);
               break;
            }
         }
      }

      Vector cws1 = getConstitutiveWords();
      Vector cws2 = answer.getConstitutiveWords();
      for (int i = 0; i < cws2.size(); i++) {
         ConstitutiveWord cw2 = (ConstitutiveWord) cws2.get(i);
         for (int j = 0; j < cws1.size(); j++) {
            ConstitutiveWord cw1 = (ConstitutiveWord) cws1.get(j);
            if (cw2.equals(cw1)) {
               cw1.updateIDs(key, cw2);
               break;
            }
         }
      }
      
      Vector mus1 = getMeaningUnits();
      Vector mus2 = answer.getMeaningUnits();
      for (int i = 0; i < mus2.size(); i++) {
         MeaningUnit mu2 = (MeaningUnit) mus2.get(i);
         for (int j = 0; j < mus1.size(); j++) {
            MeaningUnit mu1 = (MeaningUnit) mus1.get(j);
            if (mu2.equals(mu1)) {
               mu1.updateIDs(key, mu2);
               break;
            }
         }
      }

      Vector sgs1 = getSememeGroups();
      Vector sgs2 = answer.getSememeGroups();
      for (int i = 0; i < sgs2.size(); i++) {
         SememeGroup sg2 = (SememeGroup) sgs2.get(i);
         for (int j = 0; j < sgs1.size(); j++) {
            SememeGroup sg1 = (SememeGroup) sgs1.get(j);
            if (sg2.equals(sg1)) {
               sg1.updateIDs(key, sg2);
               break;
            }
         }
      }

      allElements.clear();
      for (int i = 0; i < elements.size(); i++)
         allElements.add(elements.get(i));
   }

   /**
    * Überprüft, ob in dieser Äußerungseinheit schon ein Funktionswort zu diesem
    * Wort angelegt wurde. Teilwörter werden dabei nicht beachtet.
    * 
    * @param word
    *        Das zu überprüfende Wort.
    * @return true, falls es in dieser Äußerungseinheit ein Funktionswort zu dem
    *         Wort gibt.
    */
   public boolean existsFunctionWord(Word word) {
      Vector fws = getFunctionWords();
      for (int i = 0; i < fws.size(); i++) {
         FunctionWord fw = (FunctionWord) fws.get(i);
         if (word.getContent().equalsIgnoreCase(fw.getContent()))
            return true;
      }
      return false;
   }

   /**
    * Überprüft, ob in dieser Äußerungseinheit schon ein konstitutives Wort zu
    * diesem Wort angelegt wurde. Teilwörter werden dabei nicht beachtet.
    * 
    * @param word
    *        Das zu überprüfende Wort.
    * @return true, falls es in dieser Äußerungseinheit ein konstitutives Wort
    *         zu dem Wort gibt.
    */
   public boolean existsConstitutiveWord(Word word) {
      Vector cws = getConstitutiveWords();
      for (int i = 0; i < cws.size(); i++) {
         ConstitutiveWord cw = (ConstitutiveWord) cws.get(i);
         if (word.getContent().equalsIgnoreCase(cw.getContent()))
            return true;
      }
      return false;
   }

   /**
    * Eine Wurzel kann nicht gelöscht werden.
    */
   public boolean remove() {
      return false;
   }

   void resetIDs() {
      super.resetIDs();

      Vector cws = getConstitutiveWords();
      for (int i = 0; i < cws.size(); i++)
         ((ConstitutiveWord) cws.get(i)).resetIDs();

      Vector fws = getFunctionWords();
      for (int i = 0; i < fws.size(); i++)
         ((FunctionWord) fws.get(i)).resetIDs();

      Vector mus = getMeaningUnits();
      for (int i = 0; i < mus.size(); i++)
         ((MeaningUnit) mus.get(i)).resetIDs();

      Vector sgs = getSememeGroups();
      for (int i = 0; i < sgs.size(); i++)
         ((SememeGroup) sgs.get(i)).resetIDs();
   }

}