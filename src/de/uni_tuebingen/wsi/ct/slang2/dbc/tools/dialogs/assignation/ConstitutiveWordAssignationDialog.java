package de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.assignation;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.ConstitutiveWord;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Case;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Wordclass;

public class ConstitutiveWordAssignationDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JOptionPane             optionPane;
	JPanel SelectionPane;
	JList cb_case, cb_genus, cb_person, cb_determination, cb_proform, cb_numerus;

	private ConstitutiveWord element;
	private JList cb_wordclass;

	public ConstitutiveWordAssignationDialog(ConstitutiveWord element) {
		super(new JFrame(), true);
		
		this.element = element;    

		JPanel mainPanel = new JPanel();

		if(element.getAssignation() != null)
			SelectionPane = createAssignationPane(element.getAssignation());
		else {
			SelectionPane = new JPanel(new BorderLayout());
			JButton jb = new JButton("Add Assignation");
			jb.setActionCommand("add_assignation");
			jb.addActionListener(this);
			SelectionPane.add(jb);
		}
		mainPanel.add(SelectionPane);

		JButton sa = new JButton("Save Assignation");
		sa.setActionCommand("save_assignation");
		sa.addActionListener(this);
		mainPanel.add(sa);

		JButton exit = new JButton("Close");
		exit.setActionCommand("exit");
		exit.addActionListener(this);
		mainPanel.add(exit);

		setTitle("Edit Constitutive Word");
		setContentPane(mainPanel);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
		pack();
		setVisible(true);
	}

	private JPanel createAssignationPane( TR_Assignation assignation ) {

		JPanel mainret = new JPanel(new BorderLayout());

		JPanel ret = new JPanel(new GridLayout(0, 2));
		ret.setBorder(BorderFactory.createTitledBorder("TR_Attributes"));


		JPanel JP_numerus = new JPanel();
		JP_numerus.setBorder(BorderFactory.createTitledBorder("Numerus"));
		JPanel JP_genus = new JPanel();
		JP_genus.setBorder(BorderFactory.createTitledBorder("Genus"));
//		JPanel JP_proform = new JPanel();
//		JP_proform.setBorder(BorderFactory.createTitledBorder("Proform"));
		JPanel JP_person = new JPanel();
		JP_person.setBorder(BorderFactory.createTitledBorder("Person"));
		JPanel JP_determination = new JPanel();
		JP_determination.setBorder(BorderFactory.createTitledBorder("Determination"));


		// kasus
		cb_case = new JList(Case.values());
		cb_case.setVisibleRowCount(5);
		for(Case c : assignation.getCases())
			cb_case.setSelectedValue(c, false);
		JScrollPane JP_cases = new JScrollPane(cb_case);
		JP_cases.setBorder(BorderFactory.createTitledBorder("Kasus"));

		// wordclass
		cb_wordclass = new JList(Wordclass.values());
		cb_wordclass.setVisibleRowCount(5);
		for(Wordclass c : assignation.getWordclasses())
			cb_wordclass.setSelectedValue(c, false);
		JScrollPane JP_wordclass = new JScrollPane(cb_wordclass);
		JP_wordclass.setBorder(BorderFactory.createTitledBorder("Wordclass"));

//		// numerus
//		cb_numerus = new CheckBoxList();
//		cb_numerus.setListData(Numerus.values());
//		for(Numerus c : assignation.getNumeri())
//		cb_numerus.setSelectedValue(c, false);
//		JP_numerus.add(cb_numerus);

//		// genus
//		cb_genus = new CheckBoxList();
//		cb_genus.setListData(Genus.values());
//		for(Genus c : assignation.getGenera())
//		cb_genus.setSelectedValue(c, false);
//		JP_genus.add(cb_genus);

//		// person
//		cb_person = new CheckBoxList();
//		cb_person.setListData(Person.values());
//		for(Person c : assignation.getPersons())
//		cb_person.setSelectedValue(c, false);
//		JP_person.add(cb_person);

//		// determination
//		cb_determination = new CheckBoxList();
//		cb_determination.setListData(Determination.values());
//		for(Determination c : assignation.getDeterminations())
//		cb_determination.setSelectedValue(c, false);
//		JP_determination.add(cb_determination);

		//TODO
//		cb_proform = new JComboBox(Proform.values());
//		cb_proform.setSelectedItem(deicticon.getProform());
//		JP_proform.add(cb_proform);

		ret.add(JP_cases);
		ret.add(JP_numerus);
		ret.add(JP_genus);
//		ret.add(JP_proform);
		ret.add(JP_person);
		ret.add(JP_determination);
		ret.add(JP_wordclass);

		mainret.add(ret, BorderLayout.NORTH);

		return mainret;

	}

	protected void save() {
		if(element.getAssignation() != null) {
			element.getAssignation().setCases((Case[]) cb_case.getSelectedValues());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("add_assignation".equals(e.getActionCommand())) {
			element.setAssignation(new TR_Assignation());
			SelectionPane.removeAll();
			SelectionPane.add(createAssignationPane(element.getAssignation()));
		}
		else if ("save_assignation".equals(e.getActionCommand())) {
			save();
		}
		else if ("exit".equals(e.getActionCommand())) {
			dispose();
		}
	}

}
