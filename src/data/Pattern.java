package data;

import connection.DBC_Key;

public class Pattern extends DB_Element
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String description;
	private String tdType;
	private int level;
	private int mu;
	private int path;
	
	private Chapter chapter;
	private int index;
	
	/**
	 * @param DBC_KEY key
	 * @param int id
	 * @param String name
	 * @param String description
	 * @param String tdType
	 * @param int level
	 * @param int mu
	 * @param int path
	 */
	public Pattern(DBC_Key key, int id, String name, String description, String tdType, int level, int mu, int path)
	{
		 super(id);
	     key.unlock();
	     this.name = name;
	     this.description = description;
	     this.tdType = tdType;
	     this.level = level;
	     this.mu = mu;
	     this.path = path;
	}
	
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
		 super(-1);
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

	@Override
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
	   this.index = index;
	   changeState(CHANGE);
	}
	
	@Override
	public boolean remove() {
		changeState(REMOVE);
	    return true;
	}

	@Override
	public void setChapter(DBC_Key key, Chapter chapter) {
		key.unlock();
	    this.chapter = chapter;	
	}
}
