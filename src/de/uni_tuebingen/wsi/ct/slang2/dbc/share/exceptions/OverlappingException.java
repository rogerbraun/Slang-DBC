/*
 * Erstellt: 14.11.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions;

public class OverlappingException extends Exception {

   private static final long serialVersionUID = 5090251214444012807L;

   public OverlappingException() {
      super();
   }

   public OverlappingException(String message) {
      super(message);
   }

   public OverlappingException(String message, Throwable cause) {
      super(message, cause);
   }

   public OverlappingException(Throwable cause) {
      super(cause);
   }

}
