package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.util.ArrayList;

import de.uni_tuebingen.wsi.ct.slang2.dbc.data.TR_Assignation.Wordclass;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

/**
 * This class can be used to group a noun and its deictica into a complex
 * @author christoph
 *
 */
public class PronounComplex extends DB_Element implements ChapterElementContainer, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 5478362692526908343L;

    /**
     * 
     */
    private transient ConstitutiveWord nomen;

    /**
     * 
     */
    private transient ArrayList<ConstitutiveWord> deictica;

    /**
     * Das Kapitel, in dem der Komplex liegt
     */
    private transient Chapter chapter;


    /**
     * 
     */
    public PronounComplex() {
	this.deictica = new ArrayList<ConstitutiveWord>();
    }

    /**
     * Reconstruct a Complex from a PronounComplex_DB which (most probably) just contains the DB_IDs of the referenced DB_Elements.
     * That means: Try to find the referenced elements in roots by their DB_ID.
     * This method is called by the DBC after loading the object from the server. At this state it only contains the DB_IDs of the referenced DB_Elements and not their object representation.
     * @param key
     * @param roots
     * @return
     * @throws Exception 
     */
    public PronounComplex(DBC_Key key, IllocutionUnitRoots roots, PronounComplex_DB pcdb) throws Exception {
	super(pcdb.getDB_ID());
	key.unlock();
	if(pcdb.chapterID == roots.getChapter().getDB_ID())
	    this.chapter = roots.getChapter();

	ConstitutiveWord nomen = roots.getConstitutiveWordWithID(pcdb.nomenID);
	if(nomen != null)
	    this.nomen = nomen;

	this.deictica = new ArrayList<ConstitutiveWord>();
	for (int id : pcdb.deicticaID) {
	    ConstitutiveWord deicticon = roots.getConstitutiveWordWithID(id);
	    if(deicticon != null)
		this.addDeictica(deicticon);
	}
	changeState(pcdb.getStateAsInt());
    }

    /**
     * Add a deicticon to this complex
     * @param deictica
     * @throws Exception
     */
    public void addDeictica(ConstitutiveWord ... deictica) throws Exception {
	for (ConstitutiveWord constitutiveWord : deictica) {
	    if(constitutiveWord.getPronounComplex() != null && constitutiveWord.getPronounComplex() != this)
		throw new Exception("ConstitutiveWord is already part of another complex");
	    if(constitutiveWord.getAssignation() == null || ! constitutiveWord.getAssignation().hasWordclass(Wordclass.PRONOUN))
		throw new Exception("At least on ConstitutiveWord has not Wordclass PRONOUN");
	    if(this.chapter != null && this.chapter != constitutiveWord.getRoot().getChapter())
		throw new Exception("Chapters do not match");
	}

	for (ConstitutiveWord constitutiveWord : deictica) {
	    if( ! this.deictica.contains(constitutiveWord) ) { // Should duplicates throw an exception ?
		this.deictica.add(constitutiveWord);
		constitutiveWord.setPronounComplex(this);
	    }
	}

	if(this.chapter == null && this.deictica.size() != 0)
	    this.chapter = this.deictica.iterator().next().getRoot().getChapter();
    }

    /**
     * Set the Nomen for this Complex
     * @param nomen
     * @throws Exception
     */
    public void setNomen(ConstitutiveWord nomen) throws Exception {
	if(nomen.getPronounComplex() != null && nomen.getPronounComplex() != this)
	    throw new Exception("ConstitutiveWord is already part of another complex");
	if(nomen.getAssignation() == null || ! nomen.getAssignation().hasWordclass(Wordclass.NOUN))
	    throw new Exception("ConstitutiveWord has not Wordclass NOUN");
	if(this.chapter == null)
	    this.chapter = nomen.getRoot().getChapter();
	else
	    if(this.chapter != nomen.getRoot().getChapter())
		throw new Exception("Chapters do not match");
	this.nomen = nomen;
	nomen.setPronounComplex(this);
    }

    /**
     * Remove the nomen form this Complex
     */
    public boolean removeNomen() {
	if(this.nomen == null)
	    return false;
	this.nomen.setPronounComplex(null);
	this.nomen = null;
	if(deictica.size() == 0)
	    this.chapter = null;
	return true;
    }

    /**
     * Remove a deicticon form this Complex 
     * @param deicticon
     * @return
     */
    public boolean removeDeicticon(ConstitutiveWord deicticon) {
	if (this.deictica.remove(deicticon)) {
	    deicticon.setPronounComplex(null);
	    if(deictica.size() == 0 && nomen == null)
		this.chapter = null;
	    return true;
	}
	return false;
    }

    /**
     * remove all Deictica from this complex
     */
    public void clearDeictica() {
	for (ConstitutiveWord d : this.deictica) {
	    d.setPronounComplex(null);
	}
	this.deictica.clear();
	if(this.nomen == null)
	    this.chapter = null;
    }

    /**
     * @return the nomen in this Complex
     */
    public ConstitutiveWord getNomen() {
	return nomen;
    }

    /**
     * @return the deictica in this Complex
     */
    public ArrayList<ConstitutiveWord> getDeictica() {
	ArrayList<ConstitutiveWord> ret = new ArrayList<ConstitutiveWord>();
	for (ConstitutiveWord constitutiveWord : deictica) {
	    ret.add(constitutiveWord);
	}
	return ret;
    }

    /**
     * @return the Chapter where the Complex is located
     */
    public Chapter getChapter() {
	return this.chapter;
    }

    @Override
    public int getIndex() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove() {
	this.changeState(REMOVE);
	return true;
    }

    @Override
    public void setChapter(DBC_Key key, Chapter chapter) {
	throw new UnsupportedOperationException("Chapter is determined automatically by the elements of the complex");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
	return "«" + nomen + "»+" + deictica;
    }
    
    public Object clone() {
	Object theClone = null;
	try {
	    theClone = super.clone();
	}
	catch(CloneNotSupportedException e) {
	}
	return theClone;
    }


    /* (non-Javadoc)
     * @see de.uni_tuebingen.wsi.ct.slang2.dbc.data.ChapterElementContainer#getChapterElements()
     */
    public ChapterElement[] getChapterElements() {
	ChapterElement[] ret = new ChapterElement[ deictica.size() + (nomen!=null ? 1 : 0) ];
	System.arraycopy(deictica.toArray(), 0, ret, 0, deictica.size());
	if(nomen!=null)
	    ret[ret.length-1] = nomen;
	return ret;
    }


    /**
     * This class is used by the DBC to serialize just the DB_Element.getDB_ID() instead of the referenced DB_Element Objects
     * @author christoph
     *
     */
    public class PronounComplex_DB extends PronounComplex {

	private static final long serialVersionUID = 8184759437334025861L;

	public int chapterID, nomenID;
	public int[] deicticaID;

	/**
	 * @param c
	 * @throws Exception
	 */
	public PronounComplex_DB(DBC_Key key) throws Exception {
	    key.unlock();
	    // DB ID
	    this.setDB_ID(PronounComplex.this.getDB_ID());
	    // state
	    this.changeState(PronounComplex.this.getStateAsInt());
	    // nomen id
	    this.nomenID = (PronounComplex.this.getNomen() != null) ? PronounComplex.this.nomen.getDB_ID() : ConstitutiveWord.DEFAULT_ID;
	    // chapter id
	    if(PronounComplex.this.chapter != null)
		this.chapterID = PronounComplex.this.chapter.getDB_ID();
	    // array of deictica ids		
	    this.deicticaID = new int[deictica.size()];
	    int i = 0;
	    for(ConstitutiveWord id : PronounComplex.this.deictica) {
		this.deicticaID[i++] = id.getDB_ID();
	    }
	}

    }
}
