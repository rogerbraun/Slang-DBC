package de.uni_tuebingen.wsi.ct.slang2.dbc.tools;

import java.util.Vector;

import de.uni_tuebingen.wsi.ct.slang2.dbc.client.DBC;
import de.uni_tuebingen.wsi.ct.slang2.dbc.data.WorkingTranslation;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_Key;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.DBC_KeyAcceptor;
import de.uni_tuebingen.wsi.ct.slang2.dbc.share.exceptions.DBC_ConnectionException;

public class TranslationAccess implements DBC_KeyAcceptor {

	private DBC_Key key;
	private DBC dbc;
	private WorkingTranslation wt;

	/**
	 * Erstellt einen neuen Writer, der eine Verbindung zu dem angegebenen
	 * Slang2-Server aufbaut.
	 * 
	 * @param server
	 *            Der Server, normlaerweise "kloebb.dyndns.org"
	 * @throws DBC_ConnectionException
	 */
	public TranslationAccess(String server) throws DBC_ConnectionException {
		DBC_Key.makeKey(this);
		dbc = new DBC(server);
		dbc.close();
	}

	//@Override
	public void setKey(DBC_Key pKey) {
		// TODO Auto-generated method stub
		this.key = pKey;
	}

	public void saveWorkingTranslation(String pLanguage, String pOriginal, String pTranslation) {
		wt = new WorkingTranslation(key);
		wt.setLanguage(pLanguage);
		wt.setOrginal(pOriginal);
		wt.setTranslation(pTranslation);

		try {
			dbc.open();
			dbc.saveWorkingTranslations(wt);
			dbc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Vector<WorkingTranslation>loadWorkingTranslations(String pLg, String pOriginal)
	{
		Vector<WorkingTranslation> result = null;
		try {
			dbc.open();
			result = dbc.loadWorkingTranslations(pLg, pOriginal);
			dbc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Vector<String> loadWorkingTranslationsLanguage()
	{
		Vector<String> result = null;
		try {
			dbc.open();
			result = dbc.loadWorkingTranslationsLanguage();
			dbc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	
}