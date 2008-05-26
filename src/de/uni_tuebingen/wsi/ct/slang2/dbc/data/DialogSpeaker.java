package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Der Index steht für einen bestimmten Sprecher. Ist der Sprecher über mehrere Zeilen
 * aktiv, wird die Start- bzw. Endzeile als Startindex und Endindex gespeichert.
 */
public class DialogSpeaker extends DB_Element implements Serializable, Cloneable
{
	private Chapter chapter;
	private int iuIndex;
	// maping von AkteurNr auf AkteurNamen
	private Map<Integer, String> speakers;
	// entweder ein Akteur oder ein Kommunikationspartner (KP)
	private String typ;
		
	public DialogSpeaker() 
	{
		this.iuIndex = -1;
		this.speakers = new HashMap<Integer, String>();
		this.typ = "";
	}
	
	/**
	 * 
	 * @param key
	 * @param id
	 * @param chapter
	 * @param speakers
	 * @param typ
	 * @param iuIndex
	 */
	public DialogSpeaker(DBC_Key key, int id, Chapter chapter, Map<Integer, String> speakers, String typ, int iuIndex) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.typ = typ;
		this.speakers = speakers;
	}
	
	/**
	 * 
	 * @param chapter
	 * @param value
	 * @param iuIndex
	 */
	public DialogSpeaker(Chapter chapter, Map<Integer, String> speakers, String typ, int iuIndex)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.typ = typ;
		this.speakers = speakers;
	}
	
	public void setIUIndex(int iuIndex) {
		this.iuIndex = iuIndex;
	}
	
	public Integer getIUIndex() {
		return iuIndex;
	}
	
	public void setSpeakers(Map<Integer, String> speakers ) {
		this.speakers = speakers;
	}
	
	public Map<Integer, String> getSpeakerMap()
	{
		return speakers;
	}
	
	public void setTyp(String typ)
	{
		this.typ = typ;
	}
	
	public String getTyp()
	{
		return typ;
	}
	
	@Override
	public int getIndex() {
		return -1;
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
