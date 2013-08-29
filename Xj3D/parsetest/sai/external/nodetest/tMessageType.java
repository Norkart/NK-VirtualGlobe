
/** Constant object identifiers of logged message types */
public class tMessageType {
	
	/** Error message type */
	public static final tMessageType ERROR = new tMessageType( "Error" );
	
	/** Warning message type */
	public static final tMessageType WARNING = new tMessageType( "Warning" );
	
	/** Status message type */
	public static final tMessageType STATUS = new tMessageType( "Status" );
	
	/** Success message type */
	public static final tMessageType SUCCESS = new tMessageType( "Success" );
	
	/** The message type identifier */
	final String type;
	
	/** Restricted Constructor */
	protected tMessageType( final String type ) {
		this.type = type;
	}
	
	/** Return the <code>String</code> representation of this message type
	 *  @return the <code>String</code> representation of this message type */
	public String toString( ) {
		return( type );
	}
}

