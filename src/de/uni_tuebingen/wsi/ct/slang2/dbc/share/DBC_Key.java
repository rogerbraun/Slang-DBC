package de.uni_tuebingen.wsi.ct.slang2.dbc.share;

/**
 * Ein Schl�ssel, um bestimmte Funktionen freizuschalten, die nur vom DBC
 * verwendet werden d�rfen.
 * 
 * @author Volker Kl�bb
 */
public final class DBC_Key {

    private DBC_Key() {
    }

    /**
     * Testet den Schl�sel auf G�ltigkeit
     * 
     */
    public void unlock() {
    }

    /**
     * Erstellt einen Schl�ssel f�r eine registrierte Anwendung
     * @param acceptor
     * @throws IllegalArgumentException if <code>acceptor</code> has no permission to get one
     */
    public static void makeKey(DBC_KeyAcceptor acceptor) {
		if (acceptor == null)
		    return;
		if (acceptor.getClass().getName().startsWith(
			"de.uni_tuebingen.wsi.ct.slang2.dbc."))
		    acceptor.setKey(new DBC_Key());
		else {
		    throw new IllegalArgumentException(acceptor.getClass().getName()
			    + " has no permission to get a key");
		}
    }
}