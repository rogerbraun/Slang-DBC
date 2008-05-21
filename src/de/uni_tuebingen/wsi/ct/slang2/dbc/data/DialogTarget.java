package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class DialogTarget extends DB_Element implements Serializable, Cloneable, CommentOwner
{
	private static final long serialVersionUID = 6534135368830250747L;
	private int iuIndex;
	private String description;
	private int targetNr;
	private String target;
	private Chapter chapter;
	
	public DialogTarget (DBC_Key key, int id, Chapter chapter, String description, int number, String target, int iuIndex) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
		this.targetNr = number;
		this.target = target;
	}
	
	public DialogTarget (Chapter chapter, int iuIndex, String description, int number, String target)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
		this.targetNr = number;
		this.target = target;
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
	
	public void setTargetNr (int number)
	{
		this.targetNr = number;
	}
	
	public int getTargetNr()
	{
		return targetNr;
	}
	
	public void setTarget(String target)
	{
		this.target = target;
	}
	
	public String getTarget()
	{
		return target;
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
