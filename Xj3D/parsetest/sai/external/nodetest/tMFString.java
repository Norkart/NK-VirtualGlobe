
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/** Test wrapper for the MFString X3DField type */
public class tMFString implements tX3DField {

    /** Default smoke test write value */
    public String[] smoke_value = new String[]{ "Foo", "Bar" };

    /** The field */
    final MFString field;

    /** The field name */
    final String fieldName;

    /** The field access type */
    final int access;

    /** The node name */
    final String nodeName;

    /** The test controller */
    final tController control;

    /**
     * Constructor
     * @param node the <code>X3DNode</code> this field belongs to
     * @param def the <code>X3DFieldDefinition</code> of the field
     */
    public tMFString( final X3DNode node, final X3DFieldDefinition def, final tController control ) {
        this.nodeName = node.getNodeName( );
        this.fieldName = def.getName( );
        this.field = (MFString)node.getField( fieldName );
        this.access = def.getAccessType( );
        this.control = control;
        //
        if ( nodeName.equals( "FontStyle" ) && fieldName.equals( "justify" ) ) {
            smoke_value = new String[]{ "END", "FIRST" };
        }
    }
    /**
     * Execute a 'smoke' test.
     * @return results, <code>true</code> for pass, <code>false</code> for fail
     */
    public boolean smoke( ) {
        String[] r_value;
        final String[] w_value = smoke_value;
        final int w_size = w_value.length;
        switch ( access ) {
        case X3DFieldTypes.INPUT_ONLY:
        case X3DFieldTypes.INITIALIZE_ONLY:
                field.setValue( w_size, w_value );
            break;
        case X3DFieldTypes.OUTPUT_ONLY:
            r_value = new String[field.size( )];
            field.getValue( r_value );
            break;
        case X3DFieldTypes.INPUT_OUTPUT:
            //
            control.bufferUpdate( );
            field.setValue( w_size, w_value );
            control.flushUpdate( );
            //
            final int r_size = field.size( );
            if ( r_size != w_size ) {
                control.logMessage( tMessageType.ERROR, new String[]{
                        nodeName +":"+ fieldName,
                            "\twrite size = " + w_size,
                            "\tread size  = " + r_size } );
                return( FAIL );
            }
            r_value = new String[r_size];
            field.getValue( r_value );
            for ( int i = 0; i < r_value.length; i++ ) {
                if ( !w_value[i].equals( r_value[i] ) ) {
                    control.logMessage( tMessageType.ERROR, new String[]{
                            nodeName +":"+ fieldName,
                                "\tdata mismatch at array index: " + i,
                                "\twrote [ " + w_value[i] +" ]",
                                "\tread  [ " + r_value[i] +" ]" } );
                    return( FAIL );
                }
            }
            break;
        default:
            control.logMessage( tMessageType.ERROR, nodeName +":"+ fieldName + " invalid access type: " + access );
            return( FAIL );
        }
        control.logMessage( tMessageType.SUCCESS, nodeName +":"+ fieldName );
        return( SUCCESS );
    }

    /**
     * Return the field value in an encoded string
     * @param source the identifier of the source of the value to encode
     * @param encode the identifier of encoding scheme
     * @return the field value in an encoded string
     */
    public String encode( final tValue source, final tEncode encode ) {
        String[] r_value = null;
        if ( source == tValue.FIELD ) {
            final int r_size = field.size( );
            r_value = new String[r_size];
            field.getValue( r_value );
        }
        else if ( source == tValue.SMOKE ) {
            r_value = smoke_value;
        }
        if ( encode == tEncode.XML ) {
            return( tEncodingUtils.encodeXML( fieldName, r_value ) );
        }
        else if ( encode == tEncode.CLASSIC ) {
            return( tEncodingUtils.encodeClassic( fieldName, r_value ) );
        }
        else { return( null ); }
    }
}

