
/** Utilities for encoding arrays of primative types into X3D compatible strings */
public abstract class tArrayUtils {
	
	/** Default string size */
	static final int SIZE = 1024;
	
	/**
	 * Return an XML encoding <code>String</code> representation of a <code>boolean</code> array.
	 * @return an XML encoding <code>String</code> representation of a <code>boolean</code> array.
	 */
	public static String toXMLString( final boolean[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			sb.append( array[i] );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return a Classic encoding <code>String</code> representation of a <code>boolean</code> array.
	 * @return a Classic encoding <code>String</code> representation of a <code>boolean</code> array.
	 */
	public static String toClassicString( final boolean[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			//sb.append( array[i] );
			if ( array[i] ) { sb.append( TRUE ); }
			else { sb.append( FALSE ); }
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return a <code>String</code> representation of an <code>int</code> array.
	 * @return a <code>String</code> representation of an <code>int</code> array.
	 */
	public static String toString( final int[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			sb.append( array[i] );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return a <code>String</code> representation of a <code>float</code> array.
	 * @return a <code>String</code> representation of a <code>float</code> array.
	 */
	public static String toString( final float[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			sb.append( array[i] );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return a <code>String</code> representation of a <code>double</code> array.
	 * @return a <code>String</code> representation of a <code>double</code> array.
	 */
	public static String toString( final double[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			sb.append( array[i] );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return an XML encoding <code>String</code> representation of a <code>String</code> array.
	 * @return an XML encoding <code>String</code> representation of a <code>String</code> array.
	 */
	public static String toXMLString( final String[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			final StringBuffer s = new StringBuffer( array[i] );
			//
			// replace double and single quotation marks
			// with their respective character entities
			for( int j = s.indexOf( QUOT ); j != -1; j = s.indexOf( QUOT, j + QUOT_REF.length( ) ) ) {
				s.replace( j, j+1, QUOT_REF );
			}
			for( int j = s.indexOf( APOS ); j != -1; j = s.indexOf( APOS, j + QUOT_REF.length( ) ) ) {
				s.replace( j, j+1, APOS_REF );
			}
			sb.append( QUOT );
			sb.append( s );
			sb.append( QUOT );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return a Classic encoding <code>String</code> representation of a <code>String</code> array.
	 * @return a Classic encoding <code>String</code> representation of a <code>String</code> array.
	 */
	public static String toClassicString( final String[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			final StringBuffer s = new StringBuffer( array[i] );
			//
			// delineate backslash and double quotation
			// marks with a preceeding backslash
			for( int j = s.indexOf( BACK ); j != -1; j = s.indexOf( BACK, j+2 ) ) {
				s.insert( j, BACK );
			}
			for( int j = s.indexOf( QUOT ); j != -1; j = s.indexOf( QUOT, j+2 ) ) {
				s.insert( j, BACK );
			}
			sb.append( QUOT );
			sb.append( s );
			sb.append( QUOT );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final boolean data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( data );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final int data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( data );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final float data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( data );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final double data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( data );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final String data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		//sb.append( QUOT );
		sb.append( toXMLString( new String[]{ data } ) );
		//sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final boolean[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( toXMLString( data ) );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final int[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( toString( data ) );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final float[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( toString( data ) );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final double[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( QUOT );
		sb.append( toString( data ) );
		sb.append( QUOT );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final String[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( EQUALS );
		sb.append( APOS );
		sb.append( toXMLString( data ) );
		sb.append( APOS );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final boolean data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		//sb.append( data );
		if ( data ) { sb.append( TRUE ); }
		else { sb.append( FALSE ); }
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final int data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( data );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final float data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( data );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final double data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( data );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final String data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( toClassicString( new String[]{ data } ) );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final boolean[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( toClassicString( data ) );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final int[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( toString( data ) );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final float[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( toString( data ) );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final double[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( toString( data ) );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final String[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName );
		sb.append( SPACE );
		sb.append( OPEN_BRACKET );
		sb.append( toClassicString( data ) );
		sb.append( CLOSE_BRACKET );
		sb.append( NL );
		return( sb.toString( ) );
	}
	/** Equals string */
	static final String EQUALS = "=";
	
	/** New line string */
	static final String NL = "\n";
	
	/** Space string */
	static final String SPACE = " ";
	
	/** Quote string */
	static final String QUOT = "\"";
	
	/** Quote reference */
	static final String QUOT_REF = "&quot;";
	
	/** Apostrophe string */
	static final String APOS = "\'";
	
	/** Apostrophe reference */
	static final String APOS_REF = "&apos;";
	
	/** Backslash string */
	static final String BACK = "\\";
	
	/** Open bracket string */
	static final String OPEN_BRACKET = "[";
	
	/** Close bracket string */
	static final String CLOSE_BRACKET = "]";
	
	/** Classic encoding true */
	static final String TRUE = "TRUE";
	
	/** Classic encoding false */
	static final String FALSE = "FALSE";
}

