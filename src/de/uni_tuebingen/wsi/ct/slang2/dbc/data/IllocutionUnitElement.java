/*
 * Erstellt: 28.01.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

/**
 * Ein Interface für die Elemente, die unterhalb einer Wurzel einer
 * Äußerungseinheit gespeichert werden
 * 
 * @author Volker Klöbb
 * @see IllocutionUnitRoot
 */
public interface IllocutionUnitElement {

   /**
    * Wird vom DBC benötigt
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