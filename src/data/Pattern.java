package data;

import java.io.Serializable;
import java.util.Vector;

import connection.DBC_Key;

public class Pattern implements Serializable 
{
	private int id;
	private String name;
	private String description;
	private String tdType;
	private int level;
	private int mu;
	private int path;
	
	public Pattern(DBC_Key key, int id, String name, String tdType, int level, int mu, int path){
	      key.unlock();
	      this.id = id;
	      this.name = name;
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

	   /**
	    * Wird vom DBC benötigt
	    */
	   public void setDB_ID(DBC_Key key, int id) {
	      key.unlock();
	      this.id = id;
	   }
	   
	   public String getName(){
		   return name;
	   }
	   
	   public String gettdType(){
		   return tdType;
	   }
	   
	   public int getLevel(){
		   return level;
	   }
	   
	   public int getMu(){
		   return mu;
	   }
	   
	   public int getPath(){
		   return path;
	   }
}
