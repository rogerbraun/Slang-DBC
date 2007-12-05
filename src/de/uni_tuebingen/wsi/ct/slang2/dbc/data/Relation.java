 package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

/**
 * Used for relations between two <code>WordListElement</code>s
 * @author christoph
 *
 */
public class Relation extends DB_Element implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Enumeration of relation types
	 * @author christoph
	 *
	 */
	public enum Types {
		HOLONYM,
		SYNONYM,
		ANTONYM,
		HYPERONYM,
		HYPONYM,
		MERONYM,
		POLISEMIC,
		FLEXION,
		ISOTOPY;
	}
	
	private transient WordListElement origin;
	private transient WordListElement target;
	private Types type;
	
	public Relation(WordListElement origin, WordListElement target, Types type) {
		if(origin == null || target == null || type == null)
			throw new IllegalArgumentException("Method arguments must not be null");
		this.origin = origin;
		this.target = target;
		this.type = type;
	}
	
	public Relation(DBC_Key key) {
		key.unlock();
	}

	public WordListElement getOrigin() {
		return origin;
	}

	public WordListElement getTarget() {
		return target;
	}

	public Types getType() {
		return type;
	}

	public void setOrigin(WordListElement origin) {
		this.origin = origin;
	}

	public void setTarget(WordListElement target) {
		this.target = target;
	}

	public void setType(Types type) {
		this.type = type;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean remove() {
		return this.changeState(REMOVE);
	}

	@Override
	public void setChapter(DBC_Key key, Chapter chapter) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * This class is used by the DBC to serialize just the <code>DB_Element.getDB_ID()</code> instead of the whole object of referenced DB_Elements.
	 * That requires that the elements have to be saved prior to this one 
	 * @author christoph
	 *
	 */
	public class Relation_DB extends Relation {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3326894903159574251L;
		public int origin_cw_id, target_cw_id;
		
		public Relation_DB(DBC_Key key) {
			super(key);
			key.unlock();
			if(Relation.this.getOrigin() != null)
			    this.origin_cw_id = Relation.this.getOrigin().getDB_ID();
			if(Relation.this.getTarget() != null)
			    this.target_cw_id = Relation.this.getTarget().getDB_ID();
		}
	}
}
