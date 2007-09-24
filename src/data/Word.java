/*
 * Erstellt: 29.04.2005
 */

package data;

/**
 * Ein Wort
 * 
 * @author Volker Kl�bb
 */
public class Word extends Token {

   /**
    * 
    */
   private static final long serialVersionUID = -1439083769932761430L;
   private String            content;
   private String            language;

   /**
    * Wird vom DBC ben�tigt
    */
   Word(int id, Chapter chapter, int start, String content, String language) {
      super(id, chapter, start, content.length() - 1);
      this.content = content;
      this.language = language;
   }

   /**
    * Das Wort selbst
    */
   public String getContent() {
      return content;
   }

   /**
    * Die Sprache des Wortes (Z.B. DE f�r Deutsch, EN f�r Englisch)
    */
   public String getLanguage() {
      return language;
   }

   public String getInformation() {
      return "Word"
            + "\ncontent: "
            + content
            + "\nlanguage: "
            + language
            + "\nid: "
            + getDB_ID()
            + "\nchapter-id: "
            + getChapter().getDB_ID()
            + "\nindex: "
            + getIndex()
            + "\nstart-pos: "
            + getStartPosition()
            + "\nend-pos: "
            + getEndPosition();
   }

}
