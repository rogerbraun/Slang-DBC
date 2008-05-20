package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Der Index steht für einen bestimmten Sprecher. Ist der Sprecher über mehrere Zeilen
 * aktiv, wird die Start- bzw. Endzeile als Startindex und Endindex gespeichert.
 */
public class DialogSpeakerChange extends DB_Element implements Serializable, Cloneable, CommentOwner
{
	private Chapter chapter;
	private int row;
	private int index;
		
	public DialogSpeakerChange() 
	{
		this.row = -1;
		this.index = -1;
	}
	
	/**
	 * 
	 * @param key
	 * @param id
	 * @param chapter
	 * @param index
	 * @param row
	 */
	public DialogSpeakerChange(DBC_Key key, int id, Chapter chapter, int index, int row) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.row = row;
		this.index = index;
	}
	
	public DialogSpeakerChange(Chapter chapter, int index, int row)
	{
		super(-1);
		this.chapter = chapter;
		this.row = row;
		this.index = index;
	}
	
	public void addRowIndex(int row) {
		this.row = row;
	}
	
	public Integer getRowIndex() {
		return row;
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

	public void setChapter(DBC_Key key, Chapter chapter) {
		key.unlock();
	    this.chapter = chapter;
	}
}
