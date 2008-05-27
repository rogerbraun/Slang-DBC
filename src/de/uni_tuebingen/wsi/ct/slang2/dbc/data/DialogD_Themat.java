package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Der Index steht für einen bestimmten Sprecher. Ist der Sprecher über mehrere Zeilen
 * aktiv, wird die Start- bzw. Endzeile als Startindex und Endindex gespeichert.
 */
public class DialogD_Themat extends DB_Element implements Serializable, Cloneable, CommentOwner
{
	private Chapter chapter;
	private int iuIndex;
	private String description;
	private boolean agree;
	private boolean disagree;
	private boolean obey;
	private boolean refuse;
	private boolean accept;
	private boolean reject;
	private boolean approve;
	private boolean disapprove;
	
	private Vector<String> options;
		
	public DialogD_Themat() 
	{
		this.iuIndex = -1;
		this.description = "";
		agree 	   = false;
		disagree   = false;
		obey 	   = false;
		refuse     = false;
		accept     = false;
		reject     = false;
		approve    = false;
		disapprove = false;
		this.options = new Vector<String>();
	}
	
	/**
	 * 
	 * @param key
	 * @param id
	 * @param chapter
	 * @param description
	 * @param iuIndex
	 */
	public DialogD_Themat(DBC_Key key, 
			int id, 
			Chapter chapter, 
			String description, 
			int iuIndex, 
			Vector<String> options) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
		this.options = options;
		agree 	   = false;
		disagree   = false;
		obey 	   = false;
		refuse     = false;
		accept     = false;
		reject     = false;
		approve    = false;
		disapprove = false;
				
		for (String opt : options) 
		{
			if (opt.equals("agree")) {
				agree = true;
			}
			else if (opt.equals("disagree")) {
				disagree = true;
			}
			else if (opt.equals("obey")) {
				obey = true;
			}
			else if (opt.equals("refuse")) {
				refuse = true;
			}
			else if (opt.equals("accept")) {
				accept = true;
			}
			else if (opt.equals("reject")) {
				reject = true;
			}
			else if (opt.equals("approve")) {
				approve = true;
			}
			else if (opt.equals("disapprove")) {
				disapprove = true;
			}
		}
	}
	
	public DialogD_Themat(Chapter chapter, String description, int iuIndex, Vector<String> options)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.description = description;
		this.options = options;
		agree 	   = false;
		disagree   = false;
		obey 	   = false;
		refuse     = false;
		accept     = false;
		reject     = false;
		approve    = false;
		disapprove = false;
				
		for (String opt : options) 
		{
			if (opt.equals("agree")) {
				agree = true;
			}
			else if (opt.equals("disagree")) {
				disagree = true;
			}
			else if (opt.equals("obey")) {
				obey = true;
			}
			else if (opt.equals("refuse")) {
				refuse = true;
			}
			else if (opt.equals("accept")) {
				accept = true;
			}
			else if (opt.equals("reject")) {
				reject = true;
			}
			else if (opt.equals("approve")) {
				approve = true;
			}
			else if (opt.equals("disapprove")) {
				disapprove = true;
			}
		}
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
	
	public Vector<String> getOptions()
	{
		return options;
	}
	
	public boolean isAgree()
	{
		return this.agree;
	}
	
	public boolean isDisagree()
	{
		return this.disagree;
	}
	
	public boolean isObey()
	{
		return this.obey;
	}
	
	public boolean isRefuse()
	{
		return this.refuse;
	}
	
	public boolean isAccept()
	{
		return this.accept;
	}
	
	public boolean isReject()
	{
		return this.reject;
	}
	
	public boolean isApprove()
	{
		return this.approve;
	}
	
	public boolean isDisapprove()
	{
		return this.disapprove;
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
