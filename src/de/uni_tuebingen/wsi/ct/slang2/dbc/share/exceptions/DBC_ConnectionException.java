/*
 * Erstellt: 03.12.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions;

import de.uni_tuebingen.wsi.ct.slang2.dbc.client.DBC;

/**
 * Ausnahme, die geworfen wird, wenn kein Verbindungsaufbau zur Datenbank
 * m�glich ist.
 * 
 * @author Volker Kl�bb
 * @see DBC#open()
 */
public class DBC_ConnectionException extends Exception {

   /**
    * 
    */
   private static final long serialVersionUID = -116234630428275750L;

   public DBC_ConnectionException(String message) {
      super(message);
   }

}