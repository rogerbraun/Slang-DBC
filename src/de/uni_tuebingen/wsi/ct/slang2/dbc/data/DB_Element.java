/**
 * Abstrakte Klasse für die Speicherung in die Datenbank. Nur für die DBC
 * wichtig.
 * 
 * @author Volker Klöbb
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;


public abstract class DB_Element implements Serializable, IDOwner
{

	//TODO: change states to enum
	/**
	 * Indicated that element is in sync with DB
	 */
	public static final int NORMAL = 0;
	
	/**
	 * Indicates that element is out of sync with DB: to be updated
	 */
	public static final int CHANGE = 1;
	
	/**
	 * Indicates that element is out of sync with DB: to be deleted
	 */
	public static final int REMOVE = 2;
	
	/**
	 * Indicates that element is out of sync with DB: to be inserted
	 */
	public static final int NEW = 3;
	
	/**
	 * Indicates that elements DB state is undefined
	 */
	public static final int ERROR = 4;

	/**
	 * The default (invalid) DB_ID of the element.
	 */
	public static int DEFAULT_ID = -1;

	private int                id;
	private int                state;

	/**
	 * Construct with <code>DEFAULT_ID</code> and status <code>NEW</code>
	 * @param id
	 */
	protected DB_Element() {
		this.id = DEFAULT_ID;
		this.state = NEW;
	}

	/**
	 * Construct with DB_ID <code>id</code>.
	 * Elements state is <code>NEW</code> if <code>id</code> == <code>DEFAULT_ID</code>; <code>CHANGE</code> otherwise
	 * @param id
	 */
	protected DB_Element(int id) {
		this.id = id;
		if(id == DEFAULT_ID)
			this.state = NEW;
		else
			this.state = CHANGE;
	}

	/**
	 * Die Datenbank-ID
	 */
	public final int getDB_ID() {
		return id;
	}

	/**
	 * Wird vom DBC benötigt
	 */
	public final void setDB_ID(DBC_Key key, int id) {
		key.unlock();
		setDB_ID(id);
	}

	/**
	 * @param id
	 */
	protected final void setDB_ID(int id) {
		this.id = id;
		if(this.state == NEW)
			this.state = CHANGE;
	}


	/**
	 * Get elements state as string
	 * @return
	 */
	public final String getState() {
		switch (state) {
		case NORMAL :
			return "normal";
		case CHANGE :
			return "change";
		case REMOVE :
			return "remove";
		case NEW :
			return "new";
		case ERROR :
			return "error";
		default :
			return "unknown";
		}
	}

	/**
	 * Get elements state
	 * @return
	 */
	public final int getStateAsInt() {
		return state;
	}

	/**
	 * Call this to remove element from the DB at the next save operation
	 * @return
	 */
	public abstract boolean remove();

	/**
	 * Der Index des Elements, beginnend bei 0.
	 */
	public abstract int getIndex();

	/**
	 * Wird vom DBC benötigt
	 */
	public abstract void setChapter(DBC_Key key, Chapter chapter);

	/**
	 * Synchronize the DB_ID with answer and change state appropriately
	 */
	public final void updateIDs(DBC_Key key, DB_Element answer) {
		key.unlock();
		id = answer.getDB_ID();
		if (state != REMOVE && state != ERROR)
			state = NORMAL;
	}

	/**
	 * Change state of element
	 * @param state one of the status constants
	 * @return <code>true</code> if state could be changed; <code>false</code> otherwise. 
	 */
	protected final boolean changeState(int state) {
		if (isValidState(state) && this.state != REMOVE) {
			this.state = state;
			return true;
		}
		return false;
	}
	
	/**
	 * Change state of element
	 * @param state one of the status constants
	 * @return <code>true</code> if state could be changed; <code>false</code> otherwise. 
	 */
	public final boolean changeState(DBC_Key key, int state) {
		key.unlock();
		return changeState(state);
	}
	
	/**
	 * check if <code>state</code> is a valid state
	 * @param state
	 * @return true if <code>state</code> is a valid state, <code>false</code> otherwise
	 */
	private boolean isValidState(int state) {
		return (state >= 0 && state <= 4);
	}

	/**
	 * Wird vom DBC benötigt
	 */
	public final void resetState(DBC_Key key) {
		key.unlock();
		state = NORMAL;
	}

	protected final void resetStateIntern() {
		state = NORMAL;
	}

	/**
	 * Prüft, ob dieses Element geändert wurde
	 */
	public final boolean hasChanged() {
		return state == CHANGE || state == NEW;
	}

	/**
	 * Prüft, ob dieses Element gelöscht wurde (oder noch gelöscht werden soll)
	 */
	public final boolean isRemoved() {
		return state == REMOVE;
	}

	/**
	 * Prüft, ob dieses Element nicht verändert wurde
	 */
	public final boolean isUnchanged() {
		return state == NORMAL && id >= 0;
	}
	
	/**
	 * Check if elements is out of sync with the DB
	 * @return <code>true</code> if element is out of sync with DB, <code>false</code> otherwise. 
	 */
	public final boolean isOutOfSync() {
		return state == CHANGE || state == REMOVE || state == NEW;
	}
	
	public final boolean canBeReferenced() {
		return id != DEFAULT_ID && state != ERROR && state != REMOVE;
	}

	void resetIDs() {
		id = DEFAULT_ID;
		state = NEW;
	}
}