package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;


/**
 * DialogCosmologies ersetzt und vereinfacht DialogRunUp und DialogFollowUp.
 * Es werden nur der Start- und Endindex eines Eintrags, der zugehörige Dialog
 * und die Description gespeichert. Description kann einen der drei Werte 
 * Vorfeld, Bruecke oder Nachfeld beinhalten.
 */
public class DialogCosmology implements Serializable, CommentOwner 
{
	private static final long serialVersionUID = 6534135368830250747L;
	private Dialog dialog;
	private int startIndex;
	private int endIndex;
	private String description;
	
	/**
	 * 
	 * @param dialog
	 * @param start IU Index
	 * @param end IU Index
	 * @param desc
	 */
	public DialogCosmology(Dialog dialog, int start, int end, String desc) 
	{
		this.dialog = dialog;
		this.startIndex = start;
		this.endIndex = end;
		this.description = desc;
	}
		
	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}
	
	/**
	 * 
	 * @param start IU Index
	 */
	public void setStartIndex(int start) {
		this.startIndex = start;
	}
	
	/**
	 * 
	 * @param end IU Index
	 */
	public void setEndIndex(int end) {
		this.endIndex = end;
	}
	
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public Dialog getDialog() {
		return this.dialog;
	}
	
	/**
	 * 
	 * @return first IU index
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * 
	 * @return last IU index
	 */
	public int getEndIndex() {
		return endIndex;
	}
	
	public String getDescription() {
		return description;
	}

	public int getClassCode() {
		return Comments.CLASS_CODE_DIALOG_COSMOLOGIES;
	}

	public int getDB_ID() {
		return dialog.getDB_ID();
	}
}
