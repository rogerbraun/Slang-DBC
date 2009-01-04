package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


public class WordListElement extends DB_Element
{
	private static final long          serialVersionUID = -8098356250731727256L;
	private TR_Assignation assignation;
	
	private String content = null;
	private String language = "DE";
	
	public WordListElement(String content){
		super(0);
		if(content==null)
		    throw new IllegalArgumentException("");
		assignation = new TR_Assignation();
		this.content = content;
	}
	
	public WordListElement(String content, String language){
		super(0);
		if(content==null || language==null) // TODO: check language against possible values
		    throw new IllegalArgumentException("");
		assignation = new TR_Assignation();
		this.content = content;
		this.language = language;
	}
	
	public String getContent(){
		return content;
	}
	
	public String getLanguage(){	
		return language;
	}
	
	public TR_Assignation getAssignation() {
		return assignation;
	}
	
	public void setAssignation(TR_Assignation assignation) {
	    if(assignation==null)
	    	assignation = new TR_Assignation();
//		throw new IllegalArgumentException("");
	    this.assignation = assignation;
	    changeState(CHANGE);
	}
	
	/**
	 * Setzt die Assignation mit der DB-ID db_id.
	 * @param assignation
	 * @param db_id
	 */
	public void setAssignation(TR_Assignation assignation, int db_id){
	    this.assignation = assignation;
	    this.assignation.setDB_ID(db_id);
	    changeState(CHANGE);
	}
	
	public boolean remove() {
		this.changeState(REMOVE);
		return true;
	}
	public int getIndex() {
		return 0;
	}
	public void setChapter(DBC_Key key, Chapter chapter) {
		// TODO Auto-generated method stub
		
	}
}
