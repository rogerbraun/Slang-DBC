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
		assignation = new TR_Assignation();
		this.content = content;
	}
	
	public WordListElement(String content, String language){
		super(0);
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
		this.assignation = assignation;
	}
	
	public boolean remove() {
		return false;
	}
	public int getIndex() {
		return 0;
	}
	public void setChapter(DBC_Key key, Chapter chapter) {
		// TODO Auto-generated method stub
		
	}
}
