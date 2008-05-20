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
	private int iuIndex;
	private String description;
		
	public DialogSpeakerChange() 
	{
		this.iuIndex = -1;
		this.description = "";
	}
	
	/**
	 * 
	 * @param key
	 * @param id
	 * @param chapter
	 * @param description
	 * @param iuIndex
	 */
	public DialogSpeakerChange(DBC_Key key, int id, Chapter chapter, String description, int iuIndex) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
	}
	
	public DialogSpeakerChange(Chapter chapter, String description, int iuIndex)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
	}
	
	public void setIUIndex(int iuindex) {
		this.iuIndex = iuindex;
	}
	
	public Integer getIUIndex() {
		return iuIndex;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription ()
	{
		return description;
	}
	
	@Override
	public int getIndex() {
		return -1;
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
