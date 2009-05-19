package de.uni_tuebingen.wsi.ct.slang2.dbc.tools;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.Chapter;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.DB_Element;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class WorkingTranslation extends DB_Element/* implements ChapterElement */{

    /**
     * 
     */
    private static final long serialVersionUID = 1344211026581426603L;
    private String language;
    private String orginal;
    private String translation;

    public WorkingTranslation(DBC_Key key) {
		if( key == null )
		    throw new IllegalArgumentException("No access without a DBC key");
    }

    public WorkingTranslation(DBC_Key key, WorkingTranslation_DB pcdb) throws Exception {
		this(key);
		setDB_ID(pcdb.getDB_ID());
		changeState(pcdb.getStateAsInt());
	
		if (key == null || pcdb == null)
		    throw new IllegalArgumentException("All arguments must not be null");
	
		this.language = pcdb.getLanguage();
		this.orginal = pcdb.getOrginal();
		this.translation = pcdb.getTranslation();
    }

    public String getLanguage() {
    	return language;
    }

    public String getOrginal() {
    	return orginal;
    }

    @Override
    public int getIndex() {
    	return 0;
    }

    @Override
    public boolean remove() {
    	changeState(REMOVE);
    	return true;
    }

    public void setLanguage(String language) {
    	this.language = language;
    }

    public void setOrginal(String orginal) {
    	this.orginal = orginal;
    }

    public String getTranslation() {
    	return translation;
    }

    public void setTranslation(String translation) {
    	this.translation = translation;
    }

    public class WorkingTranslation_DB extends WorkingTranslation {
		/**
		 * 
		 */
		 private static final long serialVersionUID = -7372981136687624664L;
	
		 /**
		  * @param c
		  * @throws Exception
		  */
		 public WorkingTranslation_DB(DBC_Key key) throws Exception {
		     super(key);
		     // DB ID
		     this.setDB_ID(WorkingTranslation.this.getDB_ID());
		     // state
		     this.changeState(WorkingTranslation.this.getStateAsInt());
		     
		     this.setLanguage(WorkingTranslation.this.getLanguage());
		     this.setOrginal(WorkingTranslation.this.getOrginal());
		     this.setTranslation(WorkingTranslation.this.getTranslation());
		 }
    }

	@Override
	public void setChapter(DBC_Key key, Chapter chapter) {
		// TODO Auto-generated method stub	
	}
}