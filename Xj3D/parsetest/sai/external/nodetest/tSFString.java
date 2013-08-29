
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/** Test wrapper for the SFString X3DField type */
public class tSFString implements tX3DField {

    /** Default smoke test write value */
    public String smoke_value = "FooBar";

    /** The field */
    final SFString field;

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
    public tSFString( final X3DNode node, final X3DFieldDefinition def, final tController control ) {
        this.nodeName = node.getNodeName( );
        this.fieldName = def.getName( );
        this.field = (SFString)node.getField( fieldName );
        this.access = def.getAccessType( );
        this.control = control;
        //
        if ( nodeName.equals( "Fog" ) && fieldName.equals( "fogType" ) ) { smoke_value = "EXPONENTIAL"; }
    }
    /**
     * Execute a 'smoke' test.
     * @return results, <code>true</code> for pass, <code>false</code> for fail
     */
    public boolean smoke( ) {
        String r_value;
        String w_value = smoke_value;
        switch ( access ) {
        case X3DFieldTypes.INPUT_ONLY:
        case X3DFieldTypes.INITIALIZE_ONLY:
            field.setValue( w_value );
            break;
        case X3DFieldTypes.OUTPUT_ONLY:
            r_value = field.getValue( );
            break;
        case X3DFieldTypes.INPUT_OUTPUT:
            //
            control.bufferUpdate( );
            field.setValue( w_value );
            control.flushUpdate( );
            //
            r_value = field.getValue( );
            if ( !w_value.equals( r_value ) ) {
                control.logMessage( tMessageType.ERROR, new String[]{
                        nodeName +":"+ fieldName,
                            "\twrote " + w_value,
                            "\tread  " + r_value } );
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
        String r_value = null;
        if ( source == tValue.FIELD ) {
            r_value = field.getValue( );
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

