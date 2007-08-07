package data;

import java.sql.SQLException;

import server.DBC_Server;


/*
 * Diese Klasse dient als Testklasse, um festzustellen, ob ein Chapter schon hinsichtlich einer Kategorie bearbeitet wurde.
 * 
 * author: Martin Schaefer
 */


public class ChapterEditingTester {
	public static final int CONSTITUTIVE_WORD 	= 0;
	public static final int FUNCTION_WORD 		= 1;
	public static final int COMPLEX 				= 2;
	public static final int DIALOG 				= 3;
	public static final int DIRECT_SPEECH 		= 4;
	public static final int ILLOCUTION_UNIT 		= 5;
	public static final int ISOTOPE 				= 6;
	public static final int MACRO_SENTENCE 		= 7;
	public static final int RENOMINALISATION 		= 8;
	public static final int THEMA 				= 9;
	
	public boolean isEdited(Chapter c, int category, DBC_Server dbc) throws SQLException{
		return dbc.isEdited(c, category);
	}
}
