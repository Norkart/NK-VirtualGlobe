
import org.web3d.x3d.sai.SFImage;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/** Test wrapper for the SFImage X3DField type */
public class tSFImage implements tX3DField {

    /** Default smoke test write values */
    public static final int smoke_width = 1;
    public static final int smoke_height = 1;
    public static final int smoke_cmp = 1;
    public static final int[] smoke_pixel = { 255 };

    /** The field */
    final SFImage field;

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
    public tSFImage( final X3DNode node, final X3DFieldDefinition def, final tController control ) {
        this.nodeName = node.getNodeName( );
        this.fieldName = def.getName( );
        this.field = (SFImage)node.getField( fieldName );
        this.access = def.getAccessType( );
        this.control = control;
    }
    /**
     * Execute a 'smoke' test.
     * @return results, <code>true</code> for pass, <code>false</code> for fail
     */
    public boolean smoke( ) {
        int width;
        int height;
        int component;
        int[] pixel = new int[smoke_pixel.length];
        switch ( access ) {
        case X3DFieldTypes.INPUT_ONLY:
        case X3DFieldTypes.INITIALIZE_ONLY:
            field.setValue( smoke_width, smoke_height, smoke_cmp, smoke_pixel );
            break;
        case X3DFieldTypes.OUTPUT_ONLY:
            width = field.getWidth( );
            height = field.getHeight( );
            component = field.getComponents( );
            field.getPixels( pixel );
            break;
        case X3DFieldTypes.INPUT_OUTPUT:
            //
            control.bufferUpdate( );
            field.setValue( smoke_width, smoke_height, smoke_cmp, smoke_pixel );
            control.flushUpdate( );
            //
            width = field.getWidth( );
            height = field.getHeight( );
            component = field.getComponents( );
            field.getPixels( pixel );
            if ( width != smoke_width ) {
                control.logMessage( tMessageType.ERROR, new String[]{
                        nodeName +":"+ fieldName,
                            "\tdata mismatch at image width: ",
                            "\twrote " + smoke_width,
                            "\tread  " + width } );
                return( FAIL );
            }
            if ( height != smoke_height ) {
                control.logMessage( tMessageType.ERROR, new String[]{
                        nodeName +":"+ fieldName,
                            "\tdata mismatch at image height: ",
                            "\twrote " + smoke_height,
                            "\tread  " + height } );
                return( FAIL );
            }
            if ( component != smoke_cmp ) {
                control.logMessage( tMessageType.ERROR, new String[]{
                        nodeName +":"+ fieldName,
                            "\tdata mismatch at image component: ",
                            "\twrote " + smoke_cmp,
                            "\tread  " + component } );
                return( FAIL );
            }
            for ( int i = 0; i < smoke_pixel.length; i++ ) {
                if ( smoke_pixel[i] != pixel[i] ) {
                    control.logMessage( tMessageType.ERROR, new String[]{
                            nodeName +":"+ fieldName,
                                "\tdata mismatch at pixel index: " + i,
                                "\twrote [ " + smoke_pixel[i] +" ]",
                                "\tread  [ " + pixel[i] +" ]" } );
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
        final int[] r_value = new int[4];
        if ( source == tValue.FIELD ) {
            r_value[0] = field.getWidth( );
            r_value[1] = field.getHeight( );
            r_value[2] = field.getComponents( );
            final int[] pixel = new int[1];
            field.getPixels( pixel );
            r_value[3] = pixel[0];
        }
        else if ( source == tValue.SMOKE ) {
            r_value[0] = smoke_width;
            r_value[1] = smoke_height;
            r_value[2] = smoke_cmp;
            r_value[3] = smoke_pixel[0];
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

