package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


/**
 * Der Index steht für einen bestimmten Sprecher. Ist der Sprecher über mehrere Zeilen
 * aktiv, wird die Start- bzw. Endzeile als Startindex und Endindex gespeichert.
 */
public class DialogISignal extends DB_Element implements Serializable, Cloneable
{
	private Chapter chapter;
	private int iuIndex;
	private boolean signal;
			
	public DialogISignal() 
	{
		this.iuIndex = -1;
		signal = false;
	}
	
	public DialogISignal(DBC_Key key, int id, Chapter chapter, boolean signal, int iuIndex) 
	{
		super(id);
		key.unlock();
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.signal = signal;
	}
	
	/**
	 * 
	 * @param chapter
	 * @param value
	 * @param iuIndex
	 */
	public DialogISignal(Chapter chapter, boolean signal , int iuIndex)
	{
		super(-1);
		this.chapter = chapter;
		this.iuIndex = iuIndex;
		this.signal = signal;
	}
	
	public void setIUIndex(int iuIndex) {
		this.iuIndex = iuIndex;
	}
	
	public Integer getIUIndex() {
		return iuIndex;
	}
	
	public void setSignal(boolean signal)
	{
		this.signal = signal;
	}
	
	public boolean getSignal()
	{
		return signal;
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
