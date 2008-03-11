package de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * A Dialog to display an error message and detailed view of the exception that caused it.
 * @author christoph
 *
 */
public class ExceptionDialog {

	/**
	 * Show the dialog
	 * @param parent
	 * @param t
	 * @param title
	 * @param message
	 */
	public static void show(
			Component parent,
			Throwable t,
			String title,
			String message)
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
		final GridBagConstraints gbc = new GridBagConstraints();

		final JTextArea errorText = new JTextArea();

		cp.setLayout(new GridBagLayout());

		// Set common constraints:
		gbc.gridwidth=1;
		gbc.gridheight = 1;
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.fill=GridBagConstraints.BOTH;
		gbc.ipadx = gbc.ipady = 10;

		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridheight = 2;
		// Get the icon which is normally used with the JOptionPane:
		Icon errorIcon = (Icon) UIManager.getLookAndFeel()
		.getDefaults().get("OptionPane.errorIcon");
		cp.add( new JLabel(errorIcon), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		JPanel mp = new JPanel();
		mp.add( new JLabel(message));
		mp.add( new JLabel(t.getMessage()));
		cp.add(mp, gbc);

		
		JPanel p = new JPanel();
		JButton jb;
		p.add(jb = new JButton("Ok"));
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ dlg.dispose(); }
		});
		dlg.getRootPane().setDefaultButton(jb);
		
		p.add(jb = new JButton("Details..."));
		jb.setMnemonic('D');
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{ errorText.setVisible(!errorText.isVisible()); dlg.pack(); }
		});

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		cp.add(p, gbc);

		// Produce error text.
		// Create dialog message by spewing stack trace into a string:
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		errorText.setText(sw.toString());
		errorText.setVisible(false);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		cp.add( errorText, gbc);

		dlg.pack();
		try {
			// This was added in a fairly recent version of Java, so may throw:
			dlg.setLocationRelativeTo(parent);
		} catch (Exception e) { } // just ignore
		dlg.setVisible(true);       
	}
}
