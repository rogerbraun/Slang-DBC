/*
 * Erstellt: 15.07.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Token;

public class PositionNotInTokenException extends Exception {

   /**
    * 
    */
   private static final long serialVersionUID = 5680833604636797671L;

   private Token             token;
   private int               position;

   public PositionNotInTokenException(String message, Token token, int position) {
      super(message);
      this.token = token;
      this.position = position;
   }

   public int getPosition() {
      return position;
   }

   public Token getToken() {
      return token;
   }

}
