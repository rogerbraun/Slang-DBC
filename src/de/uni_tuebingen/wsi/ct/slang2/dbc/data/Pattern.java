/**
 * 
 */
package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

public class Pattern implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1444772305224415589L;
	private int id;
	private String name;
	private String description;
	private String tdType;
	private int level;
	private int mu;
	private int path;
	
	/**
	 * @param String name
	 * @param String description
	 * @param String tdType
	 * @param int level
	 * @param int mu
	 * @param int path
	 */
	public Pattern(String name, String description, String tdType, int level, int mu, int path)
	{
		this.id = -1;
		this.name = name;
	    this.description = description;
	    this.tdType = tdType;
	    this.level = level;
	    this.mu = mu;
	    this.path = path;
	}
	
	/**
	 * @param int id
	 * @param String name
	 * @param String description
	 * @param String tdType
	 * @param int level
	 * @param int mu
	 * @param int path
	 */
	public Pattern(int id, String name, String description, String tdType, int level, int mu, int path)
	{
		this.id = id;
		this.name = name;
	    this.description = description;
	    this.tdType = tdType;
	    this.level = level;
	    this.mu = mu;
	    this.path = path;
	}
	
	  /**
	    * Die ID, die von der Datenbank vergeben wurde
	    * 
	    * @return die Datenbank-ID
	    */
	   public int getDB_ID() {
	      return id;
	   }
	   
	   public void setDB_ID(int db_id) {
			id = db_id;
		}

	   public String getName() {
		   return name;
	   }
	   
	   public String getDescription() {
		   return description;
	   }
	   
	   public String gettdType() {
		   return tdType;
	   }
	   
	   public int getLevel() {
		   return level;
	   }
	   
	   public int getMu() {
		   return mu;
	   }
	   
	   public int getPath() {
		   return path;
	   }
}
