/*
 * Erstellt: 29.10.2004
 */

package dialogs.chapterloader;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import data.Book;
import data.Chapter;

/**
 * @author Volker Klöbb
 */
class CellRenderer extends JLabel
      implements
         ListCellRenderer {

   /**
    * 
    */
   private static final long serialVersionUID = 8506316686001821106L;

   public CellRenderer() {
      setOpaque(true);
   }

   public Component getListCellRendererComponent(JList list,
         Object value,
         int index,
         boolean isSelected,
         boolean cellHasFocus) {

      if (isSelected) {
         setBorder(new LineBorder(Color.black, 2));
         setBackground(Color.blue);
         setForeground(Color.white);
      }
      else {
         setBorder(new EmptyBorder(2, 2, 2, 2));
         setBackground(Color.white);
         setForeground(Color.black);
      }

      if (value instanceof Book) {
         Book book = (Book) value;
         setText("<html> <b>"
               + book.getTitle()
               + "</b><hr>"
               + book.getAuthor()
               + "<br>"
               + book.getYear()
               + "</html>");
      }
      else if (value instanceof Chapter) {
         Chapter chapter = (Chapter) value;
         setText("<html>" + chapter.getTitle() + "("+chapter.getDate()+")"+"</html>");
      }

      return this;
   }
}