package data;

import java.util.Vector;

import connection.DBC_Key;

public class WordListElement extends DB_Element{
	private static final long          serialVersionUID = -8098356250731727256L;
	private Vector<TR_Assignation> assignations;
	
	private String content = null;
	private String language = "DE";
	
	public WordListElement(String content){
		super(0);
		assignations = new Vector<TR_Assignation>();
		this.content = content;
	}
	
	public String getContent(){
			return content;
		
	}
	public String getLanguage(){	
		return language;
	}
	public Vector getAssignations() {
		return assignations;
	}
	public void setAssignations(Vector<TR_Assignation> assignations) {
		for(int i = 0; i != assignations.size(); i ++){
			assignations.get(i).setContent(content);
		}
		this.assignations = assignations;
	}
	public void addAssignation(TR_Assignation assignation){
		assignation.setContent(content);
		assignations.add(assignation);
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
