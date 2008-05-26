package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class DialogFaces extends DB_Element implements Serializable, Cloneable, CommentOwner
{
	private static final long serialVersionUID = 6534135368830250747L;
	private int iuIndex;
	private String description;
	private DialogSpeaker speakers;
	private Chapter chapter;
	
	public DialogFaces (DBC_Key key, int id, Chapter chapter, String description, DialogSpeaker speakers, int iuIndex) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
		this.speakers = speakers;
	}
	
	public DialogFaces (Chapter chapter, int iuIndex, String description, DialogSpeaker speakers)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
		this.speakers = speakers;
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
	
	public String getDescription() {
		return this.description;
	}
	
	
	public void setSpeaker(DialogSpeaker speakers)
	{
		this.speakers = speakers;
	}
	
	public DialogSpeaker getSpeakers()
	{
		return speakers;
	}

	public int getClassCode() {
		return Comments.CLASS_CODE_DIALOG_Faces;
	}

	@Override
	public int getIndex() {
		return -1;
	}

	@Override
	public boolean remove() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setChapter(DBC_Key key, Chapter chapter) {
		key.unlock();
	    this.chapter = chapter;		
	}
}
