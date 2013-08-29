
/** Requirements of a 'test' wrapper for an X3DField. */
public interface tX3DField {
	
	/** Success flag */
	public static final boolean SUCCESS = true;
	
	/** Falure flag */
	public static final boolean FAIL = false;
	
	/**
	 * Execute a 'smoke' test.
	 * @return results, <code>true</code> for pass, <code>false</code> for fail
	 */
	public boolean smoke( );
	
	/**
	 * Return the field value in an encoded string
	 * @param source the identifier of the source of the value to encode
	 * @param encode the identifier of encoding scheme
	 * @return the field value in an encoded string
	 */
	public String encode( tValue source, tEncode encode );
}

