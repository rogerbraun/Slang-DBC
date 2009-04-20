/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

/**
 *
 * @author W. Jurczyk
 */
public class IU_Comment implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7610303571050155950L;

	/**
	 * ID des KOMMENTARS!!! Nicht des IU!
	 */
	public int ID;
	
	/**
	 * ID der IlloctionUnits!!!!
	 */
    public int IU_ID;
    public String text;
    public int chapter;
    
    public String author;
    public java.sql.Timestamp last_update;

    public IU_Comment(int newIU_ID, String newText, int newChapter, String newAuthor)
    {
        IU_ID = newIU_ID;
        text = newText;
        chapter = newChapter;
        author = newAuthor;  
        if(author == "") author = "kein Author angegeben";      
    }
    
    public IU_Comment(int newID, int newIU_ID, String newText, int newChapter, String newAuthor, java.sql.Timestamp newLast_update)
    {
        ID = newID;
    	IU_ID = newIU_ID;
        text = newText;
        chapter = newChapter;
        author = newAuthor; 
        if(author == "") author = "kein Author angegeben";
        last_update = newLast_update;
    }
    
    public String toString()
    {
    	return text;
    }
    
    /**
     * Im Kommentartool werden die Kommentare formatiert und mit Hyperlinks angezeigt, 
     * damit sie bearbeitet und gelöscht werden können.
     * @return ein HTML-formatierter String
     */
    public String toHTML()
    {
    	String formattedText = "<table border=0 width=400>";
    	
    	formattedText += "<tr>";
		formattedText += "<td colspan=2 bgcolor= '#F0F8FF'>";
   		formattedText += text.replaceAll("\n", "<br>");		//Zeilenumbrüche werden in HTML anders dargestellt
       	formattedText += "</td>";
    	formattedText += "</tr>";
    	
    	formattedText += "<tr>";
   		formattedText += "<td bgcolor= '#F0F8FF'>";
  		formattedText += "<b>" + author + "</b>";
       	formattedText += "</td>";
   		formattedText += "<td align=right bgcolor= '#F0F8FF'>";
		formattedText += "<i>zuletzt geändert: " + last_update + "</i>";
		formattedText += "</td>";
    	formattedText += "</tr>";
    	
    	formattedText += "<tr>";
		formattedText += "<td bgcolor='#C0C0C0'>";
		formattedText += "<a href='delete" + ID + "'>L&ouml;schen</a>";
    	formattedText += "</td>";
		formattedText += "<td align=right bgcolor='#C0C0C0'>";
		formattedText += "<a href='edit" + ID + "'>Bearbeiten</a>";
	    formattedText += "</td>";
		formattedText += "</tr>";
    	
    	formattedText += "</table>";
    	return formattedText;
    }
}