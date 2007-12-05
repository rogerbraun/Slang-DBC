/*
 * Erstellt: 29.04.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

/**
 * Ein Satzzeichen
 * 
 * @author Volker Kl�bb
 */
public class Sign extends Token {

   /**
    * 
    */
   private static final long serialVersionUID = -1728763777975497615L;
   private char              sign;

   /**
    * Wird vom DBC ben�tigt
    */
   Sign(int id, Chapter chapter, int start, char sign) {
      super(id, chapter, start, 0);
      this.sign = sign;
   }

   /**
    * Gibt das Satzzeichen zur�ck
    */
   public String getContent() {
      return String.valueOf(sign);
   }

}
