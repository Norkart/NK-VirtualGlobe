
/** Constant object identifiers of the encoding type */
public class tEncode {

	/** Identifier of the smoke test value */
	public final static tEncode XML = new tEncode( "XML" );
	
	/** Identifier of the field value */
	public final static tEncode CLASSIC = new tEncode( "Classic" );
	
	/** Implementation detail */
	String name;
	
	/** Restricted Constructor */
	protected tEncode( final String name ) {
		this.name = name;
	}
	
	/** Return the <code>String</code> value of the object */
	public String toString( ) {
		return( name );
	}
}

