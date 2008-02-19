package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Der Index steht für einen bestimmten Sprecher. Ist der Sprecher über mehrere Zeilen
 * aktiv, wird die Start- bzw. Endzeile als Startindex und Endindex gespeichert.
 */
public class DialogSpeaker extends DB_Element implements Serializable, Cloneable, CommentOwner
{
	private Chapter chapter;
	private Vector<Integer> rows;
	private int index;
		
	public DialogSpeaker() {
		this.rows = new Vector<Integer>();
		this.index = -1;
	}
	
	public void addRowIndex(int row) {
		this.rows.add(row);
	}
	
	public Vector<Integer> getRowIndizes() {
		return rows;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public int getIndex() {
		return this.index;
	}

	public int getClassCode() {
		return Comments.CLASS_CODE_DIALOG_SPEAKERS;
	}

	@Override
	public boolean remove() {
		 changeState(REMOVE);
	     return true;
	}

	@Override
	public void setChapter(DBC_Key key, Chapter chapter) {
		key.unlock();
	    this.chapter = chapter;
	}
}
