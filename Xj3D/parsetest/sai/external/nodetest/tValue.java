
/** Constant object identifiers of a field value source */
public class tValue {

	/** Identifier of the smoke test field value  */
	public final static tValue SMOKE = new tValue( "Smoke" );
	
	/** Identifier of the 'actual' field value */
	public final static tValue FIELD = new tValue( "Field" );
	
	/** Implementation detail */
	String name;
	
	/** Restricted Constructor */
	protected tValue( final String name ) {
		this.name = name;
	}
	
	/** Return the <code>String</code> value of the object */
	public String toString( ) {
		return( name );
	}
}

