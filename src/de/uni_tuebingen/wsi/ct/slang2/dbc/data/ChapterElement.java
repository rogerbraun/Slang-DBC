package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

public interface ChapterElement
{	
	/**
	 * @return the chapter the element is in
	 */
	public Chapter getChapter();
	/**
	 * @return the position of the first character of this element relative to the chapter it is in
	 */
	public int getStartPosition();

	/**
	 * @return
	 */
	public int getEndPosition();
}
