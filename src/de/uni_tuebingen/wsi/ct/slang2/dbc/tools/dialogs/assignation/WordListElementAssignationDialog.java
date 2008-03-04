package de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.assignation;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.ConstitutiveWord;

public class WordListElementAssignationDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Vector<Saveable> saveables = new Vector<Saveable>();

	public static void show(
		Component parent,
		ConstitutiveWord cw,
		String title)
	{
	    final JDialog dlg;

	    while (true) {
		if (parent instanceof Dialog) {
		    dlg = new JDialog((Dialog) parent, title, true);
		    break;
		} else if (parent instanceof Frame) {
		    dlg = new JDialog((Frame) parent, title, true);
		    break;
		}
		parent = parent.getParent();
	    }
	    final Container cp = dlg.getContentPane();
	    cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
	    
	    // The property tabs
	    final JTabbedPane tp = new JTabbedPane();
	    
	    AssignationPane ap = new AssignationPane(cw.getAssignation(), AssignationPane.SubsetVariant.WLE );
	    saveables.add(ap);
	    tp.addTab("Assignation", ap);
	    
	    tp.addTab("Pfade", new JPanel());

	    cp.add(tp);
	    
	    // The buttons
	    JPanel buttons = new JPanel();
	    buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
	    
	    JButton sa = new JButton("OK");
	    sa.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    save();
		    dlg.dispose();
		}
	    });
	    buttons.add(sa);

	    JButton exit = new JButton("CANCEL");
	    exit.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    dlg.dispose();
		}
	    });
	    buttons.add(exit);
	    cp.add(buttons);

	    dlg.pack();
	    try {
		// This was added in a fairly recent version of Java, so may throw:
		dlg.setLocationRelativeTo(parent);
	    } catch (Exception e) { } // just ignore
	    dlg.setVisible(true);
	}
	

	final static void save() {
		for (Saveable s : saveables) {
		    s.save();
		}
	}

}
