package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class WorkingTranslation extends DB_Element/* implements ChapterElement */{

    /**
     * 
     */
    private static final long serialVersionUID = 1344211026581426603L;

    private transient Chapter chapter;

    private String language;

    private String orginal;

    private String translation;

    public WorkingTranslation(Chapter chapter) {
	if ( chapter == null )
	    throw new IllegalArgumentException("Chapter must not be null");
	this.chapter = chapter;
    }

    public WorkingTranslation(DBC_Key key) {
	if( key == null )
	    throw new IllegalArgumentException("No access without a DBC key");
    }

    public WorkingTranslation(DBC_Key key, Chapter chapter, WorkingTranslation_DB pcdb) throws Exception {
	this(key);
	setDB_ID(pcdb.getDB_ID());
	changeState(pcdb.getStateAsInt());

	if (key == null || chapter == null || pcdb == null)
	    throw new IllegalArgumentException("All arguments must not be null");

	if(pcdb.chapterID == chapter.getDB_ID())
	    this.chapter = chapter;
	else
	    throw new IllegalArgumentException("Chapter IDs do not match");

	this.language = pcdb.getLanguage();
	this.orginal = pcdb.getOrginal();
	this.translation = pcdb.getTranslation();

    }

    public Chapter getChapter() {
	return chapter;
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

    @Override
    public void setChapter(DBC_Key key, Chapter chapter) {
	if( key == null )
	    throw new IllegalArgumentException("No access without a DBC key");
	this.chapter = chapter;
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

	 public int chapterID;

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
	     // chapter id
	     if(WorkingTranslation.this.chapter != null)
		 this.chapterID = WorkingTranslation.this.chapter.getDB_ID();

	     this.setLanguage(WorkingTranslation.this.getLanguage());
	     this.setOrginal(WorkingTranslation.this.getOrginal());
	     this.setTranslation(WorkingTranslation.this.getTranslation());
	 }

    }

}