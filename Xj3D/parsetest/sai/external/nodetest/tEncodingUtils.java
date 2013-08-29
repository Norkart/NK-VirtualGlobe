
/** Utilities for encoding nodes, fields and arrays of primative types into X3D compatible strings */
public abstract class tEncodingUtils {
	
	/** Default string size */
	static final int SIZE = 1024;
	
	/** Default XML header */
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	
	/** X3D, XML encoding, Immersive profile, header */
	public static final String X3D_XML_IMMERSIVE_HEADER = "<X3D version=\"3.0\" profile=\"Immersive\">";
	
	/** X3D, XML encoding, Scene header */
	public static final String X3D_XML_SCENE_HEADER = "<Scene>";
	
	/** X3D, XML encoding, Scene footer */
	public static final String X3D_XML_SCENE_FOOTER = "</Scene>";
	
	/** X3D, XML encoding, footer */
	public static final String X3D_XML_FOOTER = "</X3D>";
	
	/** X3D, Classic encoding, Immersive profile, header */
	public static final String X3D_CLASSIC_IMMERSIVE_HEADER = "#X3D V3.0 utf8\nPROFILE Immersive";
	
	/** X3D, XML encoding, file extension */
	public static final String X3D_XML_EXT = "x3d";
	
	/** X3D, Classic encoding, file extension */
	public static final String X3D_CLASSIC_EXT = "x3dv";
	
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
	 * The returned <code>String</code> will have any XML special characters substituted with their
	 * respective equivalent character entities.
	 * The returned <code>String</code> DOES include delimiters (double quotation marks) for the
	 * individual <code>String</code>s. 
	 * Each <code>String</code> of the array is delimited with double quotation marks and separated
	 * by white space.
	 * This representation does NOT include the delimiters for the complete
	 * array of <code>String</code>s (single quotation marks). For XML encoding, the complete
	 * <code>String</code> array is delimited by the markup.
	 * @return an XML encoding <code>String</code> representation of a <code>String</code> array.
	 */
	public static String toXMLString( final String[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			sb.append( QUOT );
			sb.append( toXMLString( array[i] ) );
			sb.append( QUOT );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return an XML encoding <code>String</code> representation of a <code>String</code>.
	 * The representation will have any XML special characters substituted with their
	 * respective equivalent character entities.
	 * The representation does NOT include delimiters (i.e. quotation marks). For XML
	 * encoding, a single <code>String</code> value is delimited by the markup.
	 * @return an XML encoding <code>String</code> representation of a <code>String</code>.
	 */
	public static String toXMLString( final String value ) {
		if ( value == null ) { return( "" ); }
		final StringBuffer sb = new StringBuffer( value );
		//
		// replace XML special characters with
		// their respective character entities
		for ( int i = 0; i < CHAR_ENT.length; i++ ) {
			for( int j = sb.indexOf( CHAR_ENT[i][0] ); j != -1; j = sb.indexOf( CHAR_ENT[i][0], j + CHAR_ENT[i][1].length( ) ) ) {
				sb.replace( j, j+CHAR_ENT[i][0].length( ), CHAR_ENT[i][1] );
			}
		}
		return( sb.toString( ) );
	}
	/**
	 * Return a Classic encoding <code>String</code> representation of a <code>String</code> array.
	 * Each <code>String</code> of the array is delimited with double quotation marks and separated
	 * by white space.
	 * @return a Classic encoding <code>String</code> representation of a <code>String</code> array.
	 */
	public static String toClassicString( final String[] array ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		final int length = array.length;
		final int lastItem = length - 1;
		for ( int i = 0; i < length; i++ ) {
			sb.append( toClassicString( array[i] ) );
			if ( i != lastItem ) { sb.append( SPACE ); }
		}
		return( sb.toString( ) );
	}
	/**
	 * Return a Classic encoding <code>String</code> representation of a <code>String</code>.
	 * This representation DOES include delimiters (i.e. double quotation marks) for the
	 * individual <code>String</code>.
	 * @return a Classic encoding <code>String</code> representation of a <code>String</code>.
	 */
	public static String toClassicString( final String value ) {
		if ( value == null ) { return( QUOT+QUOT ); }
		final StringBuffer s = new StringBuffer( value );
		//
		// delineate backslash and double quotation
		// marks with a preceeding backslash
		for( int j = s.indexOf( BACK ); j != -1; j = s.indexOf( BACK, j+2 ) ) {
			s.insert( j, BACK );
		}
		for( int j = s.indexOf( QUOT ); j != -1; j = s.indexOf( QUOT, j+2 ) ) {
			s.insert( j, BACK );
		}
		s.insert( 0, QUOT );
		s.append( QUOT );
		return( s.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final boolean data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( data );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final int data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( data );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final float data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( data );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final double data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( data );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final String data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( toXMLString( data ) );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final boolean[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( toXMLString( data ) );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final int[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( toString( data ) );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final float[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( toString( data ) );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final double[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + QUOT );
		sb.append( toString( data ) );
		sb.append( QUOT );
		return( sb.toString( ) );
	}
	/**
	 * Return the XML encoding of a field
	 * @return the XML encoding of a field
	 */
	public static String encodeXML ( final String fieldName, final String[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + EQUALS + APOS );
		sb.append( toXMLString( data ) );
		sb.append( APOS );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final boolean data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName + SPACE );
		if ( data ) { sb.append( TRUE ); }
		else { sb.append( FALSE ); }
		sb.append( SPACE );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final int data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName + SPACE );
		sb.append( data );
		sb.append( SPACE );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final float data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName + SPACE );
		sb.append( data );
		sb.append( SPACE );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final double data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName + SPACE );
		sb.append( data );
		sb.append( SPACE );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final String data ) {
		final StringBuffer sb = new StringBuffer( );
		sb.append( fieldName + SPACE );
		sb.append( toClassicString( data ) );
		sb.append( SPACE );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final boolean[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + SPACE + OPEN_BRACKET + SPACE );
		sb.append( toClassicString( data ) );
		sb.append( SPACE + CLOSE_BRACKET );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final int[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + SPACE + OPEN_BRACKET + SPACE );
		sb.append( toString( data ) );
		sb.append( SPACE + CLOSE_BRACKET );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final float[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + SPACE + OPEN_BRACKET + SPACE );
		sb.append( toString( data ) );
		sb.append( SPACE + CLOSE_BRACKET );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final double[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + SPACE + OPEN_BRACKET + SPACE );
		sb.append( toString( data ) );
		sb.append( SPACE + CLOSE_BRACKET );
		return( sb.toString( ) );
	}
	/**
	 * Return the Classic encoding of a field
	 * @return the Classic encoding of a field
	 */
	public static String encodeClassic ( final String fieldName, final String[] data ) {
		final StringBuffer sb = new StringBuffer( SIZE );
		sb.append( fieldName + SPACE + OPEN_BRACKET + SPACE );
		sb.append( toClassicString( data ) );
		sb.append( SPACE + CLOSE_BRACKET );
		return( sb.toString( ) );
	}
	/** Equals string */
	static final String EQUALS = "=";
	
	/** New line string */
	static final String NL = "\n";
	
	/** Space string */
	static final String SPACE = " ";
	
	/** Left bracket string */
	static final String LT = "<";
	
	/** Left bracket character entity */
	static final String LT_CHAR_ENT = "&lt;";
	
	/** Right bracket string */
	static final String GT = ">";
	
	/** Right bracket character entity */
	static final String GT_CHAR_ENT = "&gt;";
	
	/** Ampersand string */
	static final String AMP = "&";
	
	/** Ampersand character entity */
	static final String AMP_CHAR_ENT = "&amp;";
	
	/** Quote string */
	static final String QUOT = "\"";
	
	/** Quote character entity */
	static final String QUOT_CHAR_ENT = "&quot;";
	
	/** Apostrophe string */
	static final String APOS = "\'";
	
	/** Apostrophe character entity */
	static final String APOS_CHAR_ENT = "&apos;";
	
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
	
	/** XML Special character entities */
	static final String[][] CHAR_ENT = {
		{ LT, LT_CHAR_ENT },
		{ GT, GT_CHAR_ENT },
		{ AMP, AMP_CHAR_ENT },
		{ QUOT, QUOT_CHAR_ENT },
		{ APOS, APOS_CHAR_ENT },
	};
}

