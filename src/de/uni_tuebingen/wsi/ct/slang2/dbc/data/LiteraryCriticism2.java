package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

public class LiteraryCriticism2 extends DB_Element implements ChapterElement {

    /**
     * 
     */
    private static final long serialVersionUID = -2263177226540156084L;

    private transient Chapter chapter;

    private int pos_start;

    private int pos_end;
    
    private int type;
    
    private String annotation1;
    
    private String annotation2;
    
    public LiteraryCriticism2(Chapter chapter) {
	if ( chapter == null )
	    throw new IllegalArgumentException("Chapter must not be null");
	this.chapter = chapter;
    }

    public LiteraryCriticism2(DBC_Key key) {
	if( key == null )
	    throw new IllegalArgumentException("No access without a DBC key");
    }

    public LiteraryCriticism2(DBC_Key key, Chapter chapter2, LiteraryCriticism2_DB criticism2_DB) {
	this(key);
	setDB_ID(criticism2_DB.getDB_ID());
	changeState(criticism2_DB.getStateAsInt());
	setChapter(key, chapter2);
	setPos_start(criticism2_DB.getPos_start());
	setPos_end(criticism2_DB.getPos_end());
	setType(criticism2_DB.getType());
	setAnnotation1(criticism2_DB.getAnnotation1());
	setAnnotation2(criticism2_DB.getAnnotation2());
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

    public int getPos_start() {
        return pos_start;
    }

    public void setPos_start(int pos_start) {
        this.pos_start = pos_start;
    }

    public int getPos_end() {
        return pos_end;
    }

    public void setPos_end(int pos_end) {
        this.pos_end = pos_end;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAnnotation1() {
        return annotation1;
    }

    public void setAnnotation1(String annotation1) {
        this.annotation1 = annotation1;
    }

    public String getAnnotation2() {
        return annotation2;
    }

    public void setAnnotation2(String annotation2) {
        this.annotation2 = annotation2;
    }

    public class LiteraryCriticism2_DB extends LiteraryCriticism2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9205254844365037362L;
	
	public int chapterID;

	 /**
	  * @param c
	  * @throws Exception
	  */
	 public LiteraryCriticism2_DB(DBC_Key key) throws Exception {
	     super(key);
	     // DB ID
	     this.setDB_ID(LiteraryCriticism2.this.getDB_ID());
	     // state
	     this.changeState(LiteraryCriticism2.this.getStateAsInt());
	     // chapter id
	     if(LiteraryCriticism2.this.chapter != null)
		 this.chapterID = LiteraryCriticism2.this.chapter.getDB_ID();

	     this.setPos_start((LiteraryCriticism2.this.getPos_start()));
	     this.setPos_end((LiteraryCriticism2.this.getPos_end()));
	     this.setType(LiteraryCriticism2.this.getType());
	     this.setAnnotation1(LiteraryCriticism2.this.getAnnotation1());
	     this.setAnnotation2(LiteraryCriticism2.this.getAnnotation2());
	 }

    }

    public int getEndPosition() {
	return getPos_end();
    }

    public int getStartPosition() {
	return getPos_start();
    }
}
