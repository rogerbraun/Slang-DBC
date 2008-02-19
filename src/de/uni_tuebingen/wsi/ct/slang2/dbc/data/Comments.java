/*
 * Erstellt: 16.12.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * In dieser Klasse werden Kommentare jeglicher Art gespeichert. Der Zugriff auf
 * die Kommentare erfolgt über eine Programm-ID und die entsprechende Klasse, zu
 * der der Kommentar gehört. <br>
 * 
 * <b>Programm-IDs: </b> <br>
 * Slang2 Top-Down: TD
 * 
 * @author Volker Klöbb
 */
public class Comments
      implements
         Serializable {

   public static final int   CLASS_CODE_ILLOCUTION_UNIT = 1;
   public static final int   CLASS_CODE_DIRECT_SPEECH   = 2;
   public static final int   CLASS_CODE_DIALOG          = 3;
   //public static final int   CLASS_CODE_DIALOG_RUNUP    = 4;
   //public static final int   CLASS_CODE_DIALOG_FOLLOWUP = 5;
   public static final int   CLASS_CODE_DIALOG_COSMOLOGIES = 4;
   public static final int   CLASS_CODE_DIALOG_SPEAKERS = 5;

   /**
    * 
    */
   private static final long serialVersionUID           = -8239179091611699801L;

   private Hashtable         comments;

   /**
    * Erstellt eine leere Kommentarsammlung.
    */
   public Comments() {
      comments = new Hashtable();
   }

   /**
    * Fügt einen neuen Kommentar in die Sammlung ein
    * 
    * @param programID
    *        Die Programm-ID, siehe Klassenbeschreibung oben
    * @param owner
    *        Das Element, zu dem der Kommentar gespeichert werden soll
    * @param comment
    *        der Kommentar selbst. Die maximale Länge eines Kommentars beträgt
    *        255 Zeichen.
    */
   public void addComment(String programID, CommentOwner owner, String comment) {

      CommentKey key = new CommentKey(programID, owner);
      if (comments.containsKey(key)) {
         Comment c = (Comment) comments.get(key);
         c.setComment(comment);
      }
      else {
         Comment value = new Comment(comment);
         value.setNew();
         comments.put(key, value);
      }
   }

   /**
    * Wird von der Datenbank benötigt. Nicht verwenden!
    */
   public void setComment(DBC_Key key, CommentKey cKey, String comment) {
      key.unlock();
      Comment cValue = new Comment(comment);
      comments.put(cKey, cValue);
   }

   /**
    * Ließt einen Kommentar aus.
    * 
    * @param programID
    *        Die Programm-ID, siehe Klassenbeschreibung oben
    * @param owner
    *        Das Element, dessen Kommentar ausgelesen werden soll
    * @return Der Kommentar, oder null, falls es zu diesem Element unter dieser
    *         Programm-ID keinen Eintrag gibt.
    */
   public String getComment(String programID, CommentOwner owner) {
      CommentKey key = new CommentKey(programID, owner);
      if (comments.containsKey(key)) {
         Comment c = (Comment) comments.get(key);
         if (!c.isRemoved())
            return c.getComment();
      }
      return null;
   }

   /**
    * Löscht eine Kommentar aus der Sammlung.
    * 
    * @param programID
    *        Die Programm-ID, siehe Klassenbeschreibung oben
    * @param owner
    *        Das Element, dessen Kommentar gelöscht werden soll
    */
   public void removeComment(String programID, CommentOwner owner) {
      CommentKey key = new CommentKey(programID, owner);
      if (comments.containsKey(key)) {
         Comment c = (Comment) comments.get(key);
         c.remove();
      }
   }

   public Enumeration getKeys() {
      return comments.keys();
   }

   public Comment getComment(CommentKey key) {
      if (comments.containsKey(key))
         return (Comment) comments.get(key);
      return null;
   }

   public void add(Comments c) {
      Enumeration cks = c.comments.keys();
      while (cks.hasMoreElements()) {
         CommentKey ck = (CommentKey) cks.nextElement();
         comments.put(ck, c.comments.get(ck));
      }
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      Enumeration e = comments.keys();
      while (e.hasMoreElements()) {
         CommentKey key = (CommentKey) e.nextElement();
         Comment comment = (Comment) comments.get(key);
         sb.append(key + ": " + comment + "\n");
      }
      return sb.toString();
   }

   public void resetChange(DBC_Key key) {
      key.unlock();
      Enumeration cks = comments.keys();
      while (cks.hasMoreElements()) {
         CommentKey ck = (CommentKey) cks.nextElement();
         Comment c = (Comment) comments.get(ck);
         if (c.isRemoved())
            comments.remove(ck);
         else
            c.resetChange();
      }
   }
}