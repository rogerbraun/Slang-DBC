package data;

import java.io.Serializable;

public class Relation implements Serializable{
	
	public class Types {
		public static final int HOLONYM = 0;
		public static final int SYNONYM = 1;
		public static final int ANTONYM = 2;
		public static final int HYPERONYM = 3;
		public static final int HYPONYM = 4;
		public static final int MERONYM = 5;
		public static final int POLISEMIC = 6;
		public static final int FLEXION = 7;
		public static final int ISOTOPY = 8;
	}
	
	private TR_Assignation origin;
	private TR_Assignation target;
	private int type;
	private int DB_ID;
	
	public Relation(TR_Assignation origin, TR_Assignation target, int type) {
		this.origin = origin;
		this.target = target;
		this.type = type;
	}

	public int getDB_ID() {
		return DB_ID;
	}

	public TR_Assignation getOrigin() {
		return origin;
	}

	public TR_Assignation getTarget() {
		return target;
	}

	public int getType() {
		return type;
	}

	public void setDB_ID(int db_id) {
		DB_ID = db_id;
	}

	public void setOrigin(TR_Assignation origin) {
		this.origin = origin;
	}

	public void setTarget(TR_Assignation target) {
		this.target = target;
	}

	public void setType(int type) {
		this.type = type;
	}	
}
