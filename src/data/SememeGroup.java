/*
 * Erstellt: 01.04.2005
 */

package data;

import connection.DBC_Key;
import data.FunctionWord;
import data.IllocutionUnitRoot;
import data.MeaningUnit;

/**
 * Eine Sememegruppe, bestehend aus zwei semantischen Einheiten und eventuell
 * einem Funktionswort.
 * 
 * @author Volker Klöbb
 */
public class SememeGroup extends DB_Element
      implements
         IllocutionUnitElement {

   /**
    * 
    */
   private static final long  serialVersionUID = -6735253497209559396L;
   private IllocutionUnitRoot root;
   private FunctionWord       functionWord;
   private MeaningUnit        meaningUnit1;
   private MeaningUnit        meaningUnit2;
   private int                path;
   private int 				  numerusPath;
   private boolean            accepted;

   /**
    * Wird vom DBC benötigt.
    */
   public SememeGroup(DBC_Key key,
         IllocutionUnitRoot root,
         int id,
         FunctionWord fw,
         MeaningUnit mu1,
         MeaningUnit mu2,
         int path,
         boolean accepted) {
      super(id);
      key.unlock();
      this.root = root;
      functionWord = fw;
      meaningUnit1 = mu1;
      meaningUnit2 = mu2;
      this.path = path;
      this.accepted = accepted;

      if (fw != null) {
         fw.setSememeGroup(this);
      }
      mu1.addSememeGroup(this);
      mu2.addSememeGroup(this);
      root.register(this);
      root.add(this);
   }

   /**
    * Wird vom DBC benötigt.
    */
   public SememeGroup(DBC_Key key,
         IllocutionUnitRoot root,
         int id,
         FunctionWord fw,
         MeaningUnit mu1,
         MeaningUnit mu2,
         int path,
         int numerusPath,
         boolean accepted) {
      super(id);
      key.unlock();
      this.root = root;
      functionWord = fw;
      meaningUnit1 = mu1;
      meaningUnit2 = mu2;
      this.path = path;
      this.numerusPath = numerusPath;
      this.accepted = accepted;

      if (fw != null) {
         fw.setSememeGroup(this);
      }
      mu1.addSememeGroup(this);
      mu2.addSememeGroup(this);
      root.register(this);
      root.add(this);
   }

   /**
    * Erstellt eine neue Sememegruppe aus zwei semantischen Einheiten und einem
    * Funktionswort.
    * 
    * @param root
    *        Die Wurzel der Äußerungseinheit, unter der die ganzen Elemente zu
    *        finden sind.
    * @param fw
    *        Das Funkrionswort
    * @param mu1
    *        die erste semantische Einheit, Reihenfolge spielt keine Rolle
    * @param mu2
    *        die zweite semantische Einheit, Reihenfolge spielt keine Rolle
    */
   public SememeGroup(IllocutionUnitRoot root,
         FunctionWord fw,
         MeaningUnit mu1,
         MeaningUnit mu2) {
      super(-1);

      this.root = root;
      functionWord = fw;
      meaningUnit1 = mu1;
      meaningUnit2 = mu2;

      if (fw != null) {
         fw.setSememeGroup(this);
      }
      mu1.addSememeGroup(this);
      mu2.addSememeGroup(this);
      root.register(this);
      root.add(this);
   }

   /**
    * Erstellt eine neue Sememegruppe aus zwei semantischen Einheiten.
    * 
    * @param root
    *        Die Wurzel der Äußerungseinheit, unter der die ganzen Elemente zu
    *        finden sind.
    * @param mu1
    *        die erste semantische Einheit, Reihenfolge spielt keine Rolle
    * @param mu2
    *        die zweite semantische Einheit, Reihenfolge spielt keine Rolle
    */
   public SememeGroup(IllocutionUnitRoot root, MeaningUnit mu1, MeaningUnit mu2) {
      this(root, null, mu1, mu2);
   }

   /**
    * Prüft, ob diese Sememegruppe schon akzeptiert wurde.
    * 
    */
   public boolean isAccepted() {
      return accepted;
   }

   /**
    * Legt fest, ob diese Sememegruppe vom Benutzer abgesegnet wurde oder nicht.
    * 
    */
   public void setAccepted(boolean accepted) {
      changeState(CHANGE);
      this.accepted = accepted;
   }

   public boolean equals(Object o) {
      if (o instanceof SememeGroup) {
         SememeGroup sg = (SememeGroup) o;
         boolean b = meaningUnit1.equals(sg.meaningUnit1)
               && meaningUnit2.equals(sg.meaningUnit2);
         if (functionWord != null && sg.functionWord != null)
            return b && functionWord.equals(sg.functionWord);
         if (functionWord == null && sg.functionWord == null)
            return b;
      }
      return false;
   }

   /**
    * Die Wurzel der Äußerungseinheit.
    */
   public IllocutionUnitRoot getRoot() {
      return root;
   }

   /**
    * Die ID des Pfads
    */
   public int getPath() {
      return path;
   }

   /**
    * Die ID des Numerus-Pfads
    */
   public int getNumerusPath() {
      return numerusPath;
   }
   
   /**
    * Setzt den Pfad dieser Sememegruppe
    * 
    * @param pathID
    *        die PfadID
    */
   public void setPath(int pathID) {
      changeState(CHANGE);
      path = pathID;
   }

   /**
    * Setzt den Numerus-Pfad dieser Sememegruppe
    * 
    * @param pathID
    *        die PfadID
    */
   public void setNumerusPath(int pathID) {
      changeState(CHANGE);
      numerusPath = pathID;
   }

   /**
    * Das Funktionswort oder null, falls dieser Sememegruppe keines zugeordnet
    * wurde.
    */
   public FunctionWord getFunctionWord() {
      return functionWord;
   }

   /**
    * Die erste semantische Einheit.
    */
   public MeaningUnit getFirst() {
      return meaningUnit1;
   }

   /**
    * Die zweite semantische Einheit.
    */
   public MeaningUnit getSecond() {
      return meaningUnit2;
   }

   /**
    * Entfernt diese Sememegruppe.
    */
   public boolean remove() {
      root.remove(this);
      changeState(REMOVE);
      meaningUnit1.removeSememeGroup(this);
      meaningUnit2.removeSememeGroup(this);
      if (functionWord != null) {
         functionWord.setSememeGroup(null);
      }
      return true;
   }

   /**
    * Gibt den Partner der semantischen EInheit zurück
    * 
    * @param mu
    *        das "Gegenstück"
    * @return die andere semantische Einheit, oder null, falls die übergebende
    *         semantische Einheit nicht zu dieser Sememegruppe gehört.
    */
   public MeaningUnit getPartner(MeaningUnit mu) {
      if (mu == meaningUnit1)
         return meaningUnit2;
      else if (mu == meaningUnit2)
         return meaningUnit1;
      return null;
   }

   /**
    * Wird nicht verwendet
    */
   public int getIndex() {
      return 0;
   }

   public String toString() {
      return "SG: "
            + meaningUnit1
            + " --<"
            + functionWord
            + ">-- "
            + meaningUnit2;
   }

   /**
    * Wird vom DBC benötigt
    */
   public void setChapter(DBC_Key key, Chapter chapter) {}


   /**
    * Die Startposition der Sememegruppe, oder genauer gesagt die Startposition
    * des am weitesten links stehende Element dieser Sememegruppe.
    */
   public int getStartPosition() {
      int fw = Integer.MAX_VALUE;
      int mu1 = Integer.MAX_VALUE;
      int mu2 = Integer.MAX_VALUE;
      if (functionWord != null)
         fw = functionWord.getStartPosition();
      if (meaningUnit1 != null)
         mu1 = meaningUnit1.getStartPosition();
      if (meaningUnit2 != null)
         mu2 = meaningUnit2.getStartPosition();
      return Math.min(fw, Math.min(mu1, mu2));
   }

   /**
    * Die Endposition der Sememegruppe, oder genauer gesagt die Endposition des
    * am weitesten rechts stehende Element dieser Sememegruppe.
    */
   public int getEndPosition() {
      int fw = Integer.MIN_VALUE;
      int mu1 = Integer.MIN_VALUE;
      int mu2 = Integer.MIN_VALUE;
      if (functionWord != null)
         fw = functionWord.getEndPosition();
      if (meaningUnit1 != null)
         mu1 = meaningUnit1.getEndPosition();
      if (meaningUnit2 != null)
         mu2 = meaningUnit2.getEndPosition();
      return Math.max(fw, Math.max(mu1, mu2));
   }

   public String toString(String tab) {
      return tab + toString();
   }
}