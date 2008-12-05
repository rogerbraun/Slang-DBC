package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class LiteraryCriticism1 extends DB_Element implements ChapterElement {

    /**
     * 
     */
    private static final long serialVersionUID = -2263177226540156084L;

    private transient Chapter chapter;

    private int pos1_start;

    private int pos1_end;

    private int pos2_start;

    private int pos2_end;
    
    private String annotation;
    
    public LiteraryCriticism1(Chapter chapter) {
	if ( chapter == null )
	    throw new IllegalArgumentException("Chapter must not be null");
	this.chapter = chapter;
    }

    public LiteraryCriticism1(DBC_Key key) {
	if( key == null )
	    throw new IllegalArgumentException("No access without a DBC key");
    }

    public LiteraryCriticism1(DBC_Key key, Chapter chapter2, LiteraryCriticism1_DB criticism1_DB) {
	this(key);
	setDB_ID(criticism1_DB.getDB_ID());
	changeState(criticism1_DB.getStateAsInt());
	setChapter(key, chapter2);
	setPos1_start(criticism1_DB.getPos1_start());
	setPos1_end(criticism1_DB.getPos1_end());
	setPos2_start(criticism1_DB.getPos2_start());
	setPos2_end(criticism1_DB.getPos2_end());
	setAnnotation(criticism1_DB.getAnnotation());
    }

    @Override
    public int getIndex() {
	// TODO Auto-generated method stub
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

    public Chapter getChapter() {
	return this.chapter;
    }

    public int getEndPosition() {
	return pos1_start;
    }

    public int getStartPosition() {
	return pos2_end;
    }

    public int getPos1_start() {
        return pos1_start;
    }

    public void setPos1_start(int pos1_start) {
        this.pos1_start = pos1_start;
    }

    public int getPos1_end() {
        return pos1_end;
    }

    public void setPos1_end(int pos1_end) {
        this.pos1_end = pos1_end;
    }

    public int getPos2_start() {
        return pos2_start;
    }

    public void setPos2_start(int pos2_start) {
        this.pos2_start = pos2_start;
    }

    public int getPos2_end() {
        return pos2_end;
    }

    public void setPos2_end(int pos2_end) {
        this.pos2_end = pos2_end;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public class LiteraryCriticism1_DB extends LiteraryCriticism1 {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 6291234328500958091L;
	public int chapterID;

	 /**
	  * @param c
	  * @throws Exception
	  */
	 public LiteraryCriticism1_DB(DBC_Key key) throws Exception {
	     super(key);
	     // DB ID
	     this.setDB_ID(LiteraryCriticism1.this.getDB_ID());
	     // state
	     this.changeState(LiteraryCriticism1.this.getStateAsInt());
	     // chapter id
	     if(LiteraryCriticism1.this.chapter != null)
		 this.chapterID = LiteraryCriticism1.this.chapter.getDB_ID();

	     this.setPos1_start((LiteraryCriticism1.this.getPos1_start()));
	     this.setPos1_end((LiteraryCriticism1.this.getPos1_end()));
	     this.setPos2_start((LiteraryCriticism1.this.getPos2_start()));
	     this.setPos2_end((LiteraryCriticism1.this.getPos2_end()));
	     this.setAnnotation(LiteraryCriticism1.this.getAnnotation());
	 }

    }
}
