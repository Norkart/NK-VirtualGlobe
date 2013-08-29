
import org.web3d.x3d.sai.X3DFieldTypes;

/** Access type id to name converter */
public abstract class tAccess {
	
	/** field type name */
	public static final String FIELD = "field";
	
	/** exposedField type name */
	public static final String EXPOSED_FIELD = "exposedField";
	
	/** eventIn type name */
	public static final String EVENT_IN = "eventIn";
	
	/** eventOut type name */
	public static final String EVENT_OUT = "eventOut";
	
	/** Access type identifiers */
	final static int[] accessType = new int[] {
		X3DFieldTypes.INITIALIZE_ONLY,
		X3DFieldTypes.INPUT_ONLY,
		X3DFieldTypes.OUTPUT_ONLY,
		X3DFieldTypes.INPUT_OUTPUT,
	};
	/** Access type identifier names */
	final static String[] accessTypeName = new String[] {
		FIELD,
		EVENT_IN,
		EVENT_OUT,
		EXPOSED_FIELD,
	};
	/**
	 * Return the name of the access type identifier
	 * @param type the access type identifier
	 * @return the access type identifier name, 
	 * <code>null</code> if the type identifier is unknown
	 */
	public static String getName( final int type ) {
		for ( int i = 0; i < accessType.length; i++ ) {
			if ( type == accessType[i] ) { return( accessTypeName[i] ); }
		}
		return( null );
	}
	/**
	 * Print the list of type ids and names
	 */
	public static void main( final String[] args ) {
		for ( int i = 0; i < accessType.length; i++ ) {
			System.out.println( accessType[i] +" = "+ accessTypeName[i] );
		}
	}
}

