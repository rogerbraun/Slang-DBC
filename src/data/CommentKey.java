/*
 * Erstellt: 16.12.2004
 */

package data;

import java.io.Serializable;

import connection.DBC_Key;

/**
 * @author Volker Klöbb
 */
public class CommentKey
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = -5506931110681902982L;

   private CommentOwner      owner;
   private int               ownerID;
   private int               ownerClassCode;
   private String            programID;

   CommentKey(String programID, CommentOwner owner) {
      this.owner = owner;
      this.programID = programID.toUpperCase().trim();
   }

   public CommentKey(DBC_Key key,
         int ownerID,
         int ownerClassCode,
         String programID) {
      key.unlock();
      this.ownerID = ownerID;
      this.ownerClassCode = ownerClassCode;
      this.programID = programID;
   }

   public boolean equals(Object o) {
      if (o instanceof CommentKey) {
         CommentKey k = (CommentKey) o;
         return programID.equals(k.programID)
               && getOwnerClassCode() == k.getOwnerClassCode()
               && getOwnerID() == k.getOwnerID();
      }
      return false;
   }

   public int hashCode() {
      return programID.hashCode()
            ^ (getOwnerID() << 0x123456)
            ^ (getOwnerClassCode() << 0x789);
   }

   public int getOwnerClassCode() {
      if (owner == null)
         return ownerClassCode;
      return owner.getClassCode();
   }

   public int getOwnerID() {
      if (owner == null)
         return ownerID;
      return owner.getDB_ID();
   }

   public String getProgramID() {
      return programID;
   }

   public String toString() {
      return getOwnerClassCode() + "-" + getOwnerID() + "/" + programID;
   }
}