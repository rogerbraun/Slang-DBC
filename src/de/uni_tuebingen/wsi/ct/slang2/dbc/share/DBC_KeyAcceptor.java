package de.uni_tuebingen.wsi.ct.slang2.dbc.share;

/**
 * 
 * A Class that thinks it is chosen to get a DBC_Key via <code>DBC_Key.makeKey(DBC_KeyAcceptor o)</code>must implement this.
 * Generally it should be implement in this way:
 * <code><pre>
 * private DBC_Key key;
 * public void setKey(DBC_Key key) {
 * 	this.key = key;
 * }
 * </pre></code>
 * @author christoph
 */
public interface DBC_KeyAcceptor {

    /**
     * Called by <code>DBC_Key.makeKey(DBC_KeyAcceptor a)</code> to set the requested DBC_Key
     * @param key
     */
    public void setKey(DBC_Key key);
}
