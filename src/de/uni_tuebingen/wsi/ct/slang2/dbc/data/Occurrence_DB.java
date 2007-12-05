/*
 * Erstellt: 22.05.2005
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

/**
 * Vorkommen von Tokens in Thema/Rhema
 * 
 * @author Volker Klöbb
 */
public class Occurrence_DB
      implements
         Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = -633702600221012496L;
   private Thema_DB          thema;
   private int               start;
   private int               end;

   /**
    * Erstellt ein neues Vorkommen.
    * 
    * @param thema
    *        Das Thema/Rhema, dem dieses Vorkommen zugeordnet ist
    * @param start
    *        Der Index von dem ersten Token des Vorkommens
    * @param end
    *        Der Index von dem Letzten Token des Vorkommens
    */
   public Occurrence_DB(Thema_DB thema, int start, int end) {
      this.thema = thema;
      this.start = start;
      this.end = end;
   }

   /**
    * Erstellt ein neues Vorkommen.
    * 
    * @param thema
    *        Das Thema/Rhema, dem dieses Vorkommen zugeordnet ist
    * @param startToken
    *        Der erste Token des Vorkommens
    * @param endToken
    *        Der letzte Token des Vorkommens
    */
   public Occurrence_DB(Thema_DB thema, Token startToken, Token endToken) {
      this(thema, startToken.getIndex(), endToken.getIndex());
   }

   /**
    * Das Thema von diesem Vorkommen.
    */
   public Thema_DB getThema() {
      return thema;
   }

   /**
    * Gibt den ersten Token des Vorkommens zurück.
    */
   public Token getStart() {
      return thema.getChapter().getTokenAtIndex(start);
   }

   /**
    * Gibt den letzten Token des Vorkommens zurück.
    */
   public Token getEnd() {
      return thema.getChapter().getTokenAtIndex(end);
   }

   /**
    * Gibt den Index des ersten Tokens von diesem Vorkommen zurück.
    */
   public int getStartIndex() {
      return start;
   }

   /**
    * Gibt den Index des letzten Tokens von diesem Vorkommen zurück.
    */
   public int getEndIndex() {
      return end;
   }

   /**
    * Gibt die erste Zeichenposition von dem Vorkommen zurück. Genauer: das
    * erste Zeichen von dem ersten Token.
    */
   public int getStartPosition() {
      return getStart().getStartPosition();
   }

   /**
    * Gibt die letzte Zeichenposition von diesem Vorkommen zurück. Genauer: das
    * letzte Zeichen von dem letzten Token.
    */
   public int getEndPosition() {
      return getEnd().getEndPosition();
   }

   /**
    * Gibt den Inhalt von diesem Vorkommen zurück.
    */
   public String getContent() {
      return thema.getChapter().getContent(getStart(), getEnd());
   }

   public String toString() {
      return getContent();
   }
}
