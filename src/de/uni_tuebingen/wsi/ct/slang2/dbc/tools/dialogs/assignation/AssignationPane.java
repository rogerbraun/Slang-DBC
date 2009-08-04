package de.uni_tuebingen.wsi.ct.slang2.dbc.tools.dialogs.assignation;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Case;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Determination;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Diathese;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Genus;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Numerus;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Person;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Tempus;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Type;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Wordclass;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.WordsubclassAdjective;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.WordsubclassPronoun;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.WordsubclassPunctuationMark;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.WordsubclassVerb;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Wortart1;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Wortart2;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Wortart3;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Wortart4;

public class AssignationPane extends JPanel implements ActionListener, Saveable {

    JOptionPane             optionPane;
    JPanel SelectionPane;
    JComboBox[] comboboxes;
    TR_Assignation assi;
    Class<?>[] enums;
    
    public enum SubsetVariant {
	CW,
	FW,
	WLE
    }

    public AssignationPane(TR_Assignation assi, SubsetVariant variant ) {
	this.assi = assi;
	if(assi != null)
	    SelectionPane = createAssignationPane(assi, variant);
	else {
	    SelectionPane = new JPanel(new BorderLayout());
	    JButton jb = new JButton("Add Assignation");
	    jb.setActionCommand("add_assignation");
	    jb.addActionListener(this);
	    SelectionPane.add(jb);
	}
	add(SelectionPane);

    }

    private JPanel createAssignationPane( TR_Assignation assignation, SubsetVariant variant ) {

	JPanel mainret = new JPanel(new BorderLayout());

	JPanel ret = new JPanel(new GridLayout(0, 2));
	ret.setBorder(BorderFactory.createTitledBorder("TR_Attributes"));

	// fetch the constants for the given variant
	Enum<?>[][] constants = null;
	
	switch (variant) {
	case CW:
	    constants = new Enum<?>[][] { assignation.getNumeri(), assignation.getGenera(), assignation.getCases(), assignation.getWordclasses(),
		    assignation.getTypes(), assignation.getDeterminations(), assignation.getPersons(), assignation.getTempora(), assignation.getDiatheses(),
		    assignation.getWordsubclassesAdjective(), assignation.getWordsubclassesPronoun(),
		    assignation.getWordsubclassesPunctuationMark(), assignation.getWordsubclassesVerb()
		    };
	    break;
	case FW:
	    constants = new Enum<?>[][] { assignation.getNumeri(), assignation.getGenera(), assignation.getCases(),
		    assignation.getWortarten1(),  assignation.getWortarten2(),
		    assignation.getWortarten3(),  assignation.getWortarten4() };
	    break;
	case WLE:
	default:
	    constants = new Enum<?>[][] { assignation.getNumeri(), assignation.getGenera(), assignation.getCases(), assignation.getWordclasses(),
		assignation.getTypes(), assignation.getDeterminations(), assignation.getPersons(),
		assignation.getTempora(), assignation.getDiatheses(), assignation.getWordsubclassesAdjective(),
		    assignation.getWordsubclassesPronoun(), assignation.getWordsubclassesPunctuationMark(),
		    assignation.getWordsubclassesVerb(),
		    assignation.getWortarten1(),  assignation.getWortarten2(), assignation.getWortarten3(),  assignation.getWortarten4() };
	    break;
	}
	
	// save Enum class for each Enum[] in constants
	enums = new Class<?>[ constants.length ];
	for (int i = 0; i < enums.length; i++) {
	    enums[i] = constants[i].getClass().getComponentType();
	}
	
	comboboxes = new JComboBox[enums.length];
	
	/* 
	 * fill and add and "empty" first entry for each combobox
	 */
	for (int i = 0; i < comboboxes.length; i++) {
	    comboboxes[i] = new JComboBox( enums[i].getEnumConstants() );
	    comboboxes[i].insertItemAt("", 0);
	    comboboxes[i].setSelectedIndex(0);
	    if(constants[i].length != 0)
		comboboxes[i].setSelectedItem(constants[i][0]);
	}
	

	// add the boxes to the Panel
	final GridBagConstraints gbc = new GridBagConstraints();
	// Set common constraints:
	gbc.gridwidth=1;
	gbc.gridheight = 1;
	gbc.weightx=1.0;
	gbc.weighty=1.0;
	gbc.fill=GridBagConstraints.NONE;
	gbc.ipadx = gbc.ipady = 10;

	for (int i = 0; i < comboboxes.length; i++) {
	    gbc.gridy=i;
	    gbc.gridx=0;
	    ret.add(new JLabel( enums[i].getSimpleName() ));

	    gbc.gridx=1;
	    ret.add(comboboxes[i], gbc);
	}

	mainret.add(ret, BorderLayout.NORTH);

	return mainret;

    }

    public void save() {
	if(assi != null) {

	    for (int i = 0; i < comboboxes.length; i++) {
		if( enums[i].equals(Case.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setCases( (Case) comboboxes[i].getSelectedItem() );
		    else
			assi.setCases( new Case[0] );

		else if( enums[i].equals(Numerus.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setNumeri( (Numerus) comboboxes[i].getSelectedItem() );
		    else
			assi.setNumeri( new Numerus[0] );

		else if( enums[i].equals(Genus.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setGenera( (Genus) comboboxes[i].getSelectedItem() );
		    else
			assi.setGenera( new Genus[0] );

		else if( enums[i].equals(Determination.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setDeterminations( (Determination) comboboxes[i].getSelectedItem() );
		    else
			assi.setDeterminations( new Determination[0] );

		else if( enums[i].equals(Type.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setTypes( (Type) comboboxes[i].getSelectedItem() );
		    else
			assi.setTypes( new Type[0] );

		else if( enums[i].equals(Person.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setPersons( (Person) comboboxes[i].getSelectedItem() );
		    else
			assi.setPersons( new Person[0] );

		else if( enums[i].equals(Tempus.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setTempora( (Tempus) comboboxes[i].getSelectedItem() );
		    else
			assi.setTempora( new Tempus[0] );

		else if( enums[i].equals(Diathese.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setDiatheses( (Diathese) comboboxes[i].getSelectedItem() );
		    else
			assi.setDiatheses( new Diathese[0] );

		else if( enums[i].equals(Wordclass.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWordclasses( (Wordclass) comboboxes[i].getSelectedItem() );
		    else
			assi.setWordclasses( new Wordclass[0] );

		else if( enums[i].equals(WordsubclassVerb.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWordsubclassesVerb( (WordsubclassVerb) comboboxes[i].getSelectedItem() );
		    else
			assi.setWordsubclassesVerb( new WordsubclassVerb[0] );

		else if( enums[i].equals(WordsubclassAdjective.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWordsubclassesAdjective( (WordsubclassAdjective) comboboxes[i].getSelectedItem() );
		    else
			assi.setWordsubclassesAdjective( new WordsubclassAdjective[0] );

		else if( enums[i].equals(WordsubclassPronoun.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWordsubclassesPronoun( (WordsubclassPronoun) comboboxes[i].getSelectedItem() );
		    else
			assi.setWordsubclassesPronoun( new WordsubclassPronoun[0] );

		else if( enums[i].equals(WordsubclassPunctuationMark.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWordsubclassesPunctuationMark( (WordsubclassPunctuationMark) comboboxes[i].getSelectedItem() );
		    else
			assi.setWordsubclassesPunctuationMark( new WordsubclassPunctuationMark[0] );

		else if( enums[i].equals(Wortart1.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWortarten1( (Wortart1) comboboxes[i].getSelectedItem() );
		    else
			assi.setWortarten1( new Wortart1[0] );

		else if( enums[i].equals(Wortart2.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWortarten2( (Wortart2) comboboxes[i].getSelectedItem() );
		    else
			assi.setWortarten2( new Wortart2[0] );

		else if( enums[i].equals(Wortart3.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWortarten3( (Wortart3) comboboxes[i].getSelectedItem() );
		    else
			assi.setWortarten3( new Wortart3[0] );

		else if( enums[i].equals(Wortart4.class) )
		    if(comboboxes[i].getSelectedIndex() != 0)
			assi.setWortarten4( (Wortart4) comboboxes[i].getSelectedItem() );
		    else
			assi.setWortarten4( new Wortart4[0] );
	    }
	}
    }

    public void actionPerformed(ActionEvent arg0) {
	if ("add_assignation".equals(arg0.getActionCommand())) {
	    assi = new TR_Assignation();
	    SelectionPane.removeAll();
	    SelectionPane.add(createAssignationPane(assi, SubsetVariant.WLE));
	}
    }


}
