
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/** Factory for constructing tX3DField instances. */
public abstract class tX3DFieldFactory {
	/**
	 * Return an instance of a tX3DField.
	 * @param node the <code>X3DNode</code> this field belongs to
	 * @param def the <code>X3DFieldDefinition</code> of the field
	 * @return the field if it exists as part of the node, <code>null</code> otherwise.
	 */
	public static tX3DField getInstance( final X3DNode node, final X3DFieldDefinition def, final tController control ) {
		tX3DField field = null;
		final int type = def.getFieldType( );
		switch( type ) {
		case X3DFieldTypes.SFBOOL:
			field = new tSFBool( node, def, control );
			break;
		case X3DFieldTypes.SFCOLOR:
			field = new tSFColor( node, def, control );
			break;
		case X3DFieldTypes.SFCOLORRGBA:
			field = new tSFColorRGBA( node, def, control );
			break;
		case X3DFieldTypes.SFDOUBLE:
			field = new tSFDouble( node, def, control );
			break;
		case X3DFieldTypes.SFFLOAT:
			field = new tSFFloat( node, def, control );
			break;
		case X3DFieldTypes.SFIMAGE:
			field = new tSFImage( node, def, control );
			break;
		case X3DFieldTypes.SFINT32:
			field = new tSFInt32( node, def, control );
			break;
		case X3DFieldTypes.SFNODE:
			field = new tSFNode( node, def, control );
			break;
		case X3DFieldTypes.SFROTATION:
			field = new tSFRotation( node, def, control );
			break;
		case X3DFieldTypes.SFSTRING:
			field = new tSFString( node, def, control );
			break;
		case X3DFieldTypes.SFTIME:
			field = new tSFTime( node, def, control );
			break;
		case X3DFieldTypes.SFVEC2D:
			field = new tSFVec2d( node, def, control );
			break;
		case X3DFieldTypes.SFVEC2F:
			field = new tSFVec2f( node, def, control );
			break;
		case X3DFieldTypes.SFVEC3D:
			field = new tSFVec3d( node, def, control );
			break;
		case X3DFieldTypes.SFVEC3F:
			field = new tSFVec3f( node, def, control );
			break;
		case X3DFieldTypes.MFBOOL:
			field = new tMFBool( node, def, control );
			break;
		case X3DFieldTypes.MFCOLOR:
			field = new tMFColor( node, def, control );
			break;
		case X3DFieldTypes.MFCOLORRGBA:
			field = new tMFColorRGBA( node, def, control );
			break;
		case X3DFieldTypes.MFDOUBLE:
			field = new tMFDouble( node, def, control );
			break;
		case X3DFieldTypes.MFFLOAT:
			field = new tMFFloat( node, def, control );
			break;
		case X3DFieldTypes.MFIMAGE:
			field = new tMFImage( node, def, control );
			break;
		case X3DFieldTypes.MFINT32:
			field = new tMFInt32( node, def, control );
			break;
		case X3DFieldTypes.MFNODE:
			field = new tMFNode( node, def, control );
			break;
		case X3DFieldTypes.MFROTATION:
			field = new tMFRotation( node, def, control );
			break;
		case X3DFieldTypes.MFSTRING:
			field = new tMFString( node, def, control );
			break;
		case X3DFieldTypes.MFTIME:
			field = new tMFTime( node, def, control );
			break;
		case X3DFieldTypes.MFVEC2D:
			field = new tMFVec2d( node, def, control );
			break;
		case X3DFieldTypes.MFVEC2F:
			field = new tMFVec2f( node, def, control );
			break;
		case X3DFieldTypes.MFVEC3D:
			field = new tMFVec3d( node, def, control );
			break;
		case X3DFieldTypes.MFVEC3F:
			field = new tMFVec3f( node, def, control );
			break;
		//default:
		//	logMessage( MESSAGE_ERROR, nodeName +":"+ def.getName( ) +":"+  
		//		" unknown field type: " + def.getFieldTypeString( ) );
		}
		return( field );
	}
}

