package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Der Index steht für einen bestimmten Sprecher. Ist der Sprecher über mehrere Zeilen
 * aktiv, wird die Start- bzw. Endzeile als Startindex und Endindex gespeichert.
 */
public class DialogSpeaker extends DB_Element implements Serializable, Cloneable, CommentOwner
{
	private Chapter chapter;
	private int iuIndex;
	private int value;
		
	public DialogSpeaker() 
	{
		this.iuIndex = -1;
		this.value = -1;
	}
	
	/**
	 * 
	 * @param key
	 * @param id
	 * @param chapter
	 * @param value
	 * @param iuIndex
	 */
	public DialogSpeaker(DBC_Key key, int id, Chapter chapter, int value, int iuIndex) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.value = value;
	}
	
	/**
	 * 
	 * @param chapter
	 * @param value
	 * @param iuIndex
	 */
	public DialogSpeaker(Chapter chapter, int value, int iuIndex)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.value = value;
	}
	
	public void addIUIndex(int iuIndex) {
		this.iuIndex = iuIndex;
	}
	
	public Integer getIUIndex() {
		return iuIndex;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public int getIndex() {
		return this.value;
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
