/*
 * Erstellt: 28.01.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

/**
 * Ein Interface f�r die Elemente, die unterhalb einer Wurzel einer
 * �u�erungseinheit gespeichert werden
 * 
 * @author Volker Kl�bb
 * @see IllocutionUnitRoot
 */
public interface IllocutionUnitElement {

   /**
    * Wird vom DBC ben�tigt
    */
   public void setChapter(DBC_Key key, Chapter chapter);
  
   /**
    * Die Wurzel, dem dieses Element untergeordnet ist
    */
   public IllocutionUnitRoot getRoot();

   /**
    * Die Startposition des Elements, bezogen auf Zeichenbasis
    */
   public int getStartPosition();

   /**
    * Die Endposition des Elements, bezogen auf Zeichenbasis
    */
   public int getEndPosition();

   public String toString(String tab);

   /**
    * Entfernt dieses Element aus der Wurzel
    */
   public boolean remove();
}