package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class DialogFaces extends DB_Element implements Serializable, Cloneable, CommentOwner
{
	private static final long serialVersionUID = 6534135368830250747L;
	private int row;
	private String typ;
	private int speaker;
	private Chapter chapter;
	
	public DialogFaces(DBC_Key key, int id, Chapter chapter, int index, int row) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.row = row;
		this.typ = typ;
		this.speaker = speaker;
	}
		
	public void setIndex(int row) {
		this.row = row;
	}
	
	public void setTyp(String typ) {
		this.typ = typ;
	}
	
	public void setSpeaker(int speaker)
	{
		this.speaker = speaker;
	}
	
	public String getTyp() {
		return typ;
	}
	
	public int getSpeaker()
	{
		return speaker;
	}

	public int getClassCode() {
		return Comments.CLASS_CODE_DIALOG_Faces;
	}

	@Override
	public int getIndex() {
		return row;
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
