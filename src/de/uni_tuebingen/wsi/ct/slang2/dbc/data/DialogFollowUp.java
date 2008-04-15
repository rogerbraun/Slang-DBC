//
// Erstellt: 19.01.2005
 

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * @author Volker Klöbb
 */
public class DialogFollowUp
      implements
         Serializable,
         CommentOwner {

   /**
    * 
    */
   private static final long        serialVersionUID = -1906402865196649411L;

   private Dialog                   dialog;
   private transient IllocutionUnit start;
   private transient IllocutionUnit end;
   private int                      startIndex;
   private int                      endIndex;

   DialogFollowUp(Dialog dialog) {
      this.dialog = dialog;
   }

   public boolean equals(Object o) {
      if (o instanceof DialogFollowUp) {
         DialogFollowUp test = (DialogFollowUp) o;
         try {
            return start.equals(test.start) && end.equals(test.end);
         }
         catch (NullPointerException e) {
            return start == test.start && end == test.end;
         }
      }
      return false;
   }

   public int getDB_ID() {
      return dialog.getDB_ID();
   }

   IllocutionUnit getEnd() {
      return end;
   }

   void setEnd(IllocutionUnit end) {
      this.end = end;
      if (end == null)
         endIndex = -1;
      else
         endIndex = end.getIndex();
   }

   IllocutionUnit getStart() {
      return start;
   }

   int getStartIndex() {
      return startIndex;
   }

   int getEndIndex() {
      return endIndex;
   }

   void setStart(IllocutionUnit start) {
      this.start = start;
      if (start == null)
         startIndex = -1;
      else
         startIndex = start.getIndex();
   }

   Token getFirstToken() {
      if (start != null)
         return start.getFirstToken();
      return null;
   }

   Token getLastToken() {
      if (end != null)
         return end.getLastToken();
      return null;
   }

   void clear() {
      start = end = null;
   }

   boolean isValid() {
      return (start == null && end != null) || (start != null && end == null);
   }

   public String toString() {
      return "Nachfeld";
   }

   public void setChapter(DBC_Key key, Chapter chapter) {
      key.unlock();
      start = chapter.getIllocutionUnitAtIndex(startIndex);
      end = chapter.getIllocutionUnitAtIndex(endIndex);
   }

   public int getClassCode() {
      return Comments.CLASS_CODE_DIALOG_FOLLOWUP;
   }
}