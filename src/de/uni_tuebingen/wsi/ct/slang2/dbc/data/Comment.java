/*
 * Erstellt: 16.12.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

/**
 * @author Volker Klöbb
 */
public class Comment
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 7848680704633108993L;

   private static final int  NORMAL           = 0;
   private static final int  CHANGE           = 1;
   private static final int  NEW              = 2;
   private static final int  DELETE           = 3;

   private String            comment;
   private int               state;

   Comment(String comment) {
      this.comment = comment;
      state = NORMAL;
   }

   void setNew() {
      state = NEW;
   }

   void remove() {
      state = DELETE;
   }

   public String getComment() {
      return comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
      state = CHANGE;
   }

   public boolean hasChanged() {
      return state == CHANGE;
   }

   public boolean isNew() {
      return state == NEW;
   }

   public boolean isRemoved() {
      return state == DELETE;
   }

   public void resetChange() {
      state = NORMAL;
   }

   public String toString() {
      return comment;
   }

}