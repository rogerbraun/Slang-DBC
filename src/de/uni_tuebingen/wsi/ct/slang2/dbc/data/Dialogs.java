/*
 * Created on 05.07.2004 To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

package de.uni_tuebingen.wsi.ct.slang2.dbc.data;

import java.io.Serializable;
import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;

/**
 * alle Dialoge werden hier in einem Vektor gespeichert, damit sind die Dialoge
 * besser zu verwalten
 * 
 * @author Katrin-Shanthy Raff
 */
public class Dialogs implements Serializable {

	private static final long serialVersionUID = -6104818456049790298L;
	private Vector<Dialog> dialogs;
	private Vector<Dialog> deletedDialogs;

	/**
	 * Vector f√ºr das Speichern aller Dialoge wird erzeugt
	 */
	public Dialogs() {
		dialogs = new Vector<Dialog>();
		deletedDialogs = new Vector<Dialog>();
	}

	/**
	 * klont Dialogs
	 * 
	 * @return Object
	 */
	public Object clone() {
		try {
			Dialogs dialogsClone = new Dialogs();
			dialogsClone.dialogs = clone(dialogs);
			dialogsClone.deletedDialogs = clone(deletedDialogs);
			return dialogsClone;
		} catch (Exception e) {
			System.out.println("Fehler beim Clonen von Dialogs");
		}
		return null;
	}

	private static Vector clone(Vector v) {
		if (v == null)
			return null;

		Vector vc = new Vector(v.capacity());
		for (int i = 0; i < v.size(); i++) {
			Object o = v.get(i);
			if (o instanceof Dialog)
				vc.add(i, ((Dialog) o).clone());
		}
		return vc;
	}

	/**
	 * f√ºgt einen Dialog hinten im Vektor hinzu
	 * 
	 * @param dialog
	 *        Dialog
	 */
	public void add(Dialog dialog) {
		dialogs.add(dialog);
	}

	/**
	 * f√ºgt einen Dialog an der i-ten Stelle hinzu
	 * 
	 * @param dialog
	 *        Dialog
	 * @param i
	 *        int
	 * @return Dialogs
	 */
	public Dialogs add(Dialog dialog, int i) {
		dialogs.insertElementAt(dialog, i);
		updatePositions();
		return this;
	}

	/**
	 * einen Dialog entfernen
	 * 
	 * @param dialog
	 *        Dialog
	 */
	public void removeDialog(Dialog dialog) {
		if (dialog != null) {
			dialog.remove();
			dialogs.remove(dialog);
			deletedDialogs.add(dialog);
		}
	}

	/**
	 * falls ein Dialog z.B. gel√∂scht wurde, m√ºssen die Positionen angepasst
	 * werden, damit keine L√ºcken entstehen
	 */
	public void updatePositions() {
		int counter = 0;
		for (int i = 0; i < dialogs.size(); i++) {
			Dialog d = (Dialog) dialogs.get(i);
			if (d.getDialogTokens().size() != 0) {
				d.setIndex(++counter);
			} else {
				dialogs.remove(i);
			}
		}
	}

	/**
	 * den Vektor mit den Dialogen zur√ºckgeben
	 * 
	 * @return Vector dialogs
	 */

	public Vector<Dialog> getVector() {
		return dialogs;
	}

	public Vector<Dialog> getAllDialogs(DBC_Key key) {
		key.unlock();
		Vector<Dialog> res = new Vector<Dialog>();
		res.addAll(deletedDialogs);
		res.addAll(dialogs);
		return res;
	}

	public Vector getAllSpeakerChanges(DBC_Key key) {
		key.unlock();
		Vector res = new Vector();
		for (int i = 0; i < dialogs.size(); i++) {
			Dialog d = (Dialog) dialogs.get(i);
			res.addAll(d.getAllSpeakerChanges(key));
		}
		return res;
	}

	/**
	 * i-ten Dialog zur¸ckgeben
	 * 
	 * @param i
	 *        int
	 * @return Dialog
	 */
	public Dialog get(int i) {
		return (Dialog) dialogs.get(i);
	}

	/**
	 * Gibt den Dialog zur¸ck, in dem diese ƒuﬂerungseinheit vorkommt. Dabei wird
	 * der Dialog und dessen Vor- und Nachfeld ¸berpr¸ft.
	 * 
	 * @param iu
	 * @return Den Dialog oder null, falls keiner gefunden wurde
	 */
	public Dialog get(IllocutionUnit iu) {
		for (int i = 0; i < dialogs.size(); i++) {
			Dialog d = (Dialog) dialogs.get(i);
			if (d.containsIllocutionUnit(iu))
				return d;
		}
		return null;
	}

	/**
	 * Gibt den Dialog zur¸ck, in dem diese Direkte Rede steht
	 * 
	 * @param ds
	 *        die Direkte Rede
	 * @return der Dialog oder null, falls keiner gefunden wurde
	 */
	public Dialog get(DirectSpeech ds) {
		Vector ius = ds.getIllocutionUnits();
		for (int i = 0; i < ius.size(); i++) {
			IllocutionUnit iu = (IllocutionUnit) ius.get(i);
			Dialog d = get(iu);
			if (d != null)
				return d;
		}
		return null;
	}

	/**
	 * Anzahl der Dialoge
	 * 
	 * @return int
	 */
	public int size() {
		return dialogs.size();
	}

	/**
	 * testet, ob ein Objekt gleich den Dialogs ist
	 * 
	 * @param o
	 *        Object
	 * @return boolean
	 */
	public boolean equals(Object o) {
		if (o instanceof Dialogs) {
			Dialogs ds = (Dialogs) o;
			for (int i = 0; i < dialogs.size(); i++) {
				if (!(ds.get(i)).equals(dialogs.get(i)))
					return false;
			}
		}
		return false;
	}

	public String toString() {
		String s = new String();
		for (int i = 0; i < dialogs.size(); i++) {
			s += ((Dialog) dialogs.get(i)).getDialogTokens();
			s += "\n";
		}
		return s;
	}

	public void setChapter(DBC_Key key, Chapter chapter) {
		for (int i = 0; i < dialogs.size(); i++) {
			Dialog d = (Dialog) dialogs.get(i);
			d.setChapter(key, chapter);
		}
	}

	public void updateIDs(DBC_Key key, Dialogs ds) {
		for (int i = 0; i < dialogs.size(); i++) {
			Dialog d1 = (Dialog) dialogs.get(i);
			Dialog d2 = (Dialog) ds.dialogs.get(i);
			d1.updateIDs(key, d2);
		}
	}

	void resetIDs() {
		for (int i = 0; i < dialogs.size(); i++)
			((Dialog) dialogs.get(i)).resetIDs();
	}
}