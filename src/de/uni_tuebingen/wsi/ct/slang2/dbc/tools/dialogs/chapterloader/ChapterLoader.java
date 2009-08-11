/*
 * Erstellt: 29.10.2004
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.chapterloader;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import de.uni_tuebingen.wsi.ct.slang2.dbc.client.DBC;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Book;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Chapter;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.CompleteAnalyse;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;
import de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.ExceptionDialog;

/**
 * Ein Dialog, um Kapitel aus der Datenbank zu laden. <br>
 * Codebeispiel: <br>
 * 
 * <pre>
 * String server = &quot;kloebb.dyndns.org&quot;;
 * try {
 *    ChapterLoader loader = new ChapterLoader(server);
 *    loader.showDialog(this);
 * 
 *    DBC dbc = new DBC(server);
 *    Chapter c = dbc.loadChapter(loader.getChapterID());
 *    dbc.close();
 *    System.out.println(c);
 * }
 * catch (DBC_ConnectionException e) {
 *    e.printStackTrace();
 * }
 * </pre>
 * 
 * @author Volker Klöbb
 */
public class ChapterLoader extends JComponent
      implements
         ListSelectionListener,
         ActionListener {

   /**
    * 
    */
   private static final long serialVersionUID = 2804378118791510239L;
   private String            server;
   private JDialog           dialog;
   private int               bookID;
   private int               chapterID;
   private Vector<Book>            books;
   private JList             bookList;
   private JList             chapterList;
   private JButton           loadButton;
   private JButton           copyButton;
   private JButton           deleteButton;
   private JButton           importButton;
   private JButton           exportButton;
   private JButton           cancelButton;

   /**
    * Erstellt einen Dialog, um ein Kapitel aus der Datenbank zu laden.
    * 
    * @param server
    *        Die URL des Server
    * @throws DBC_ConnectionException
    */
   public ChapterLoader(String server) throws Exception {
      this.server = server;
      DBC dbc = new DBC(server);
      try{
    	  books = dbc.loadBooks();
      }
      finally {
    	  dbc.close();
      }
   }

   /**
    * Zeigt diesen Dialog an Position 0 / 0 an.
    * 
    * @param parent
    */
   public void showDialog(Component parent) {
      showDialog(parent, 0, 0);
   }

   /**
    * Zeigt diesen Dialog an an Position x / y an.
    * 
    * @param parent
    */
   public void showDialog(Component parent, int x, int y) {
      dialog = createDialog((JFrame) parent);
      dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      dialog.pack();
      dialog.setLocation(x, y);
      dialog.setVisible(true);
      dialog.dispose();
   }

   private JDialog createDialog(JFrame parent) {
      JDialog dialog = new JDialog(parent, true);
      dialog.setTitle("Chapterloader");

      Container contentPane = dialog.getContentPane();
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.NORTH;
      c.insets = new Insets(5, 5, 5, 5);
      c.weightx = 1.0;
      c.weighty = 1.0;

      contentPane.setLayout(new GridBagLayout());

      bookList = new JList(books);
      bookList.addListSelectionListener(this);
      bookList.setCellRenderer(new CellRenderer());
      bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane sp1 = new JScrollPane(bookList,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      sp1.setBorder(new TitledBorder("Books"));
      sp1.setPreferredSize(new Dimension(300, 200));
      c.gridx = 0;
      c.gridy = 0;
      contentPane.add(sp1, c);

      chapterList = new JList();
      chapterList.addListSelectionListener(this);
      chapterList.setCellRenderer(new CellRenderer());
      chapterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane sp2 = new JScrollPane(chapterList,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      sp2.setBorder(new TitledBorder("Chapters"));
      sp2.setPreferredSize(new Dimension(300, 200));
      c.gridx = 0;
      c.gridy = 1;
      contentPane.add(sp2, c);

      JPanel p = new JPanel(new GridBagLayout());
      c.gridx = 1;
      c.gridy = 0;
      c.gridheight = 2;
      contentPane.add(p, c);

      loadButton = new JButton("load");
      loadButton.addActionListener(this);
      loadButton.setEnabled(false);
      c.gridx = 0;
      c.gridy = 0;
      c.gridheight = 1;
      p.add(loadButton, c);

      copyButton = new JButton("copy");
      copyButton.addActionListener(this);
      copyButton.setEnabled(false);
      c.gridx = 0;
      c.gridy = 1;
      p.add(copyButton, c);

      exportButton = new JButton("export");
      exportButton.addActionListener(this);
      exportButton.setEnabled(false);
      c.gridx = 0;
      c.gridy = 2;
      p.add(exportButton, c);
      
      deleteButton = new JButton("delete");
      deleteButton.addActionListener(this);
      deleteButton.setEnabled(false);
      c.gridx = 0;
      c.gridy = 3;
      c.gridheight = 1;
      p.add(deleteButton, c);

      
      importButton = new JButton("import");
      importButton.addActionListener(this);
      c.gridx = 0;
      c.gridy = 4;
      p.add(importButton, c);

      cancelButton = new JButton("cancel");
      cancelButton.addActionListener(this);
      c.gridx = 0;
      c.gridy = 5;
      p.add(cancelButton, c);

      return dialog;
   }

   public void valueChanged(ListSelectionEvent e) {
      if (e.getSource() == bookList) {
         Book book = (Book) bookList.getSelectedValue();
         if (book != null) {
            bookID = book.getDB_ID();
            chapterList.setListData(book.getChapters());
         }
         else {
            bookID = -1;
         }
         loadButton.setEnabled(false);
         copyButton.setEnabled(false);
         exportButton.setEnabled(false);
         deleteButton.setEnabled(false);
      }
      else if (e.getSource() == chapterList) {
         Chapter chapter = (Chapter) chapterList.getSelectedValue();
         if (chapter != null) {
            chapterID = chapter.getDB_ID();
            loadButton.setEnabled(true);
            copyButton.setEnabled(true);
            exportButton.setEnabled(true);
            deleteButton.setEnabled(true);
         }
      }
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == cancelButton) {
         dialog.dispose();
         bookID = -1;
         chapterID = -1;
      }
      else if (e.getSource() == loadButton) {
         dialog.dispose();
      }
      else if (e.getSource() == copyButton) {
	  DBC dbc = null;
	  try {
	      //TODO: Das Laden ist überflüssig! Das Kopieren sollte der Server Seite überlassen werden.
	      CompleteAnalyse ca = new CompleteAnalyse(server, chapterID, 0);
	      ca.resetIDs();
	      ca.save(server, true, 0);

	      // refreh list
	      bookList.clearSelection();
	      chapterList.clearSelection();
	      dbc = new DBC(server);
	      books = dbc.loadBooks();
	      bookList.setListData(books);
	      chapterList.setListData(new Vector<Object>());

	      bookID = -1;
	      chapterID = -1;
	  }
	  catch (Exception e1) {
	      ExceptionDialog.show(new JFrame(), e1, "Copy Failed", "");
	  }
	  finally {
	      if(dbc != null)
		  dbc.close();
	  }
      }
      else if (e.getSource() == deleteButton) {
	  DBC dbc = null;
	  try {
             int userDecision= JOptionPane.showConfirmDialog(this,"Are you sure you want to delete this chapter?","Warning",JOptionPane.YES_NO_OPTION);
        	 if(userDecision==0){
        	     dbc = new DBC(server);
	             dbc.deleteChapter(chapterID);
	             chapterList.clearSelection();

	             books = dbc.loadBooks();
	             bookList.setListData(books);
	             chapterList.setListData(new Vector<Object>());
	             
        	 }
          }
          catch (Exception e1) {
             e1.printStackTrace();
          }
          finally {
              if(dbc != null)
        	  dbc.close();
          }
       }
      else if (e.getSource() == importButton) {
         try {
            JFileChooser fc = new JFileChooser();
            if (fc.showDialog(this, "import") == JFileChooser.APPROVE_OPTION) {
               File file = fc.getSelectedFile();
               CompleteAnalyse ca = new CompleteAnalyse(file);
               ca.save(server, false, 0);
            }
         }
         catch (Exception e1) {
            e1.printStackTrace();
         }
      }
      else if (e.getSource() == exportButton) {
         try {
            JFileChooser fc = new JFileChooser();
            if (fc.showDialog(this, "export") == JFileChooser.APPROVE_OPTION) {
               File file = fc.getSelectedFile();
               CompleteAnalyse ca = new CompleteAnalyse(server, chapterID, 0);
               ca.export(file);
            }
         }
         catch (Exception e1) {
            e1.printStackTrace();
         }
      }
   }

   /**
    * die ID des selektierten Kapitels.
    * 
    * @return ID des Kapitels als int
    * @see Chapter
    * @see DBC#loadChapter(int)
    */
   public int getChapterID() {
      return chapterID;
   }

   /**
    * die ID des selektierten Buches.
    * 
    * @return ID des Buches als int
    * @see Book
    * @see DBC#loadBook(int)
    */
   public int getBookID() {
      return bookID;
   }

}