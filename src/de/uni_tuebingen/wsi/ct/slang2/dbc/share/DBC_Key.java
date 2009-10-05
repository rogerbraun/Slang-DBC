package de.uni_tuebingen.wsi.ct.slang2.dbc.share;

/**
 * Ein Schlüssel, um bestimmte Funktionen freizuschalten, die nur vom DBC
 * verwendet werden dürfen.
 * 
 * @author Volker Klöbb
 */
public final class DBC_Key {

    private DBC_Key() {
    }

    /**
     * Testet den Schlüsel auf Gültigkeit
     * 
     */
    public void unlock() {
    }

    /**
     * Erstellt einen Schlüssel für eine registrierte Anwendung
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