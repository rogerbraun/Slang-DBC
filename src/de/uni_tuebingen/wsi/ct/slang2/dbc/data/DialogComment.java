package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class DialogComment extends DB_Element implements Serializable, Cloneable
{
	private static final long serialVersionUID = 7523694191229309496L;
	private int iuIndex;
	private String comment;
	private int commentNr;
	private Chapter chapter;
	
	public DialogComment (DBC_Key key, int id, Chapter chapter, String comment, int commentNr,  int iuIndex) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.commentNr = commentNr;
		this.comment = comment;
	}
	
	public DialogComment (Chapter chapter, int iuIndex, String comment, int commentNr)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.commentNr = commentNr;
		this.comment = comment;
	}
		
	public void setIUIndex(int iuindex) {
		this.iuIndex = iuindex;
	}
	
	public Integer getIUIndex() {
		return iuIndex;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public int getCommentNr()
	{
		return commentNr;
	}

	@Override
	public int getIndex() {
		return -1;
	}

	@Override
	public boolean remove() {
		return false;
	}

	@Override
	public void setChapter(DBC_Key key, Chapter chapter) {
		key.unlock();
	    this.chapter = chapter;		
	}
}
