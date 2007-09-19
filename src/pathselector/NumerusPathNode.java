* Erstellt: 01.08.2007
 */

package pathselector;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import connection.DBC;
import connection.DBC_ConnectionException;

/**
 * Ein Dialog, um einen Pfad anzuzeigen und auszuwählen.
 * 
 * @author Volker Klöbb extended by Thomas Müller
 */
public class NumerusPathSelector extends JComponent
      implements
         TreeSelectionListener,
         ActionListener {

	/**
	 * TODO: die filter-options einfügen????
	 */
	
   /**
    * 
    */
   private static final long      serialVersionUID = -1670109740923219278L;
   private JDialog                dialog;
   private PathNode               rootPath;
   private DefaultMutableTreeNode rootTree;
   private JTree                  tree;
   private JTextArea              text;
   private JButton                okButton;
   private JButton                cancelButton;
   private PathNode               selection;

   // private int selectedID = 0;

   /**
    * Erstellt einen Dialog, um Pfade auszuwählen.
    * 
    * @param server
    *        Die URL des Server
    * @throws DBC_ConnectionException
    */
   public NumerusPathSelector(String server) throws Exception {
      DBC dbc = new DBC(server);
      rootPath = dbc.getNumerusPaths();
      dbc.close();
   }

   /**
    * Zeigt diesen Dialog an Position 0 / 0 an, ohne einen Pfad zu selektieren.
    * 
    * @param parent
    *        Das Hauptfenster, von dem aus der Dialog angezeigt wird
    */
   public void showDialog(Frame parent) {
      showDialog(parent, 0, 0, -1);
   }

   /**
    * Zeigt diesen Dialog an Position 0 / 0 an, und selektiert einen Pfad
    * 
    * @param parent
    *        Das Hauptfenster, von dem aus der Dialog angezeigt wird
    * @param nodeID
    *        die ID des anzuzeigenden Pfades
    */
   public void showDialog(Frame parent, int nodeID) {
      showDialog(parent, 0, 0, nodeID);
   }

   /**
    * Zeigt diesen Dialog an an Position x / y an.
    * 
    * @param parent
    *        Das Hauptfenster, von dem aus der Dialog angezeigt wird
    * @param x
    *        die x-Koordinate des Dialogs
    * @param y
    *        die y-Koordinate des Dialogs
    * @param nodeID
    *        die ID des anzuzeigenden Pfades
    */
   public void showDialog(Frame parent, int x, int y, int nodeID) {
      dialog = createDialog(parent, nodeID);
      dialog.pack();
      dialog.setLocation(x, y);
      dialog.setVisible(true);
      dialog.dispose();
   }

   private JDialog createDialog(Frame parent, int nodeID) {
      JDialog dialog = new JDialog(parent, true);
      dialog.setTitle("Pathselector");

      Container contentPane = dialog.getContentPane();
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.NORTH;
      c.insets = new Insets(5, 5, 5, 5);
      c.weightx = 1.0;
      c.weighty = 1.0;

      contentPane.setLayout(new GridBagLayout());

      JScrollPane sp1 = new JScrollPane(makeTree(),
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      sp1.setBorder(new TitledBorder("Numerus paths"));
      sp1.setPreferredSize(new Dimension(300, 400));
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 2;
      contentPane.add(sp1, c);

      text = new JTextArea(5, 5);
      text.setEditable(false);
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      JScrollPane sp2 = new JScrollPane(text,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 2;
      contentPane.add(sp2, c);

      okButton = new JButton("ok");
      okButton.addActionListener(this);
      c.gridx = 0;
      c.gridy = 2;
      c.gridwidth = 1;
      contentPane.add(okButton, c);

      cancelButton = new JButton("cancel");
      cancelButton.addActionListener(this);
      c.gridx = 1;
      c.gridy = 2;
      c.gridwidth = 1;
      contentPane.add(cancelButton, c);

      if (nodeID >= 0) {
         selectNode(nodeID);
      }

      return dialog;
   }

   private JTree makeTree() {
      rootTree = new DefaultMutableTreeNode("path");
      tree = new JTree(rootTree);

      tree.addTreeSelectionListener(this);
      createBranch(rootTree, rootPath);

      tree.makeVisible(new TreePath(rootTree.getNextNode().getPath()));
      return tree;
   }

   private void createBranch(DefaultMutableTreeNode parent, PathNode root) {
      Vector children = root.getChildren();
      for (int i = 0; i < children.size(); i++) {
         PathNode node = (PathNode) children.get(i);
         DefaultMutableTreeNode n = new DefaultMutableTreeNode(node);
         parent.add(n);

         createBranch(n, node);
      }
   }

   private void selectNode(int id) {
      if (tree != null) {
         PathNode pathNode = rootPath.getNode(id);
         if (pathNode != null) {
            TreePath path = new TreePath(getTreeNode(pathNode).getPath());
            tree.scrollPathToVisible(path);
            tree.setSelectionPath(path);
            text.setText(pathNode.getDescription());
         }
      }
   }

   private DefaultMutableTreeNode getTreeNode(Object o) {
      Enumeration e = rootTree.breadthFirstEnumeration();
      while (e.hasMoreElements()) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
         if (node.getUserObject() == o)
            return node;
      }
      return null;
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == cancelButton) {
         dialog.dispose();
      }
      else if (e.getSource() == okButton) {
         dialog.dispose();
      }
   }

   /**
    * Gibt die ID des selektierten Pfades zurück
    */
   public int getSelectedPathID() {
      if (selection != null)
         return selection.getId();
      return 0;
   }

   public PathNode getSelectedPath() {
      return selection;
   }

   public void valueChanged(TreeSelectionEvent e) {
      try {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
               .getSelectionPath().getLastPathComponent();
         PathNode pathNode = (PathNode) node.getUserObject();
         text.setText(pathNode.getDescription());
         selection = pathNode;
      }
      catch (ClassCastException ce) {
         selection = null;
      }
   }
}