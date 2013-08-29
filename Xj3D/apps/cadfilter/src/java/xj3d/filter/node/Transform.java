/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.node;

// External imports
import javax.vecmath.Matrix4f;

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for an X3D Transform node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class Transform extends AbstractGroup {
    
    /** Yet another wrapper class, around BaseTransform, used for it's
    *  calculation of the matrix from the field values. */
    private TransformMatrix matrixSource;
    
    /** Field value */
    public float[] translation;
    
    /** Field value */
    public float[] rotation;
    
    /** Field value */
    public float[] scale;
    
    /** Field value */
    public float[] scaleOrientation;
    
    /** Field value */
    public float[] center;
    
    /**
     * Constructor
     */
    public Transform() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Transform(String defName) {
        super("Transform", defName);
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler
     */
    public void encode() {
        
        if (handler != null) {
            if (useName == null) {
                handler.startNode(name, defName);
                
                super.encode();
                
                if (translation != null) {
                    handler.startField("translation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(translation, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(translation, 3));
                        break;
                    }
                }
                
                if (scale != null) {
                    handler.startField("scale");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(scale, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(scale, 3));
                        break;
                    }
                }
                
                if (center != null) {
                    handler.startField("center");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(center, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(center, 3));
                        break;
                    }
                }
                
                if (rotation != null) {
                    handler.startField("rotation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(rotation, 4);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(rotation, 4));
                        break;
                    }
                }
                
                if (scaleOrientation != null) {
                    handler.startField("scaleOrientation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(scaleOrientation, 4);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(scaleOrientation, 4));
                        break;
                    }
                }
                handler.endNode();
            } else {
                handler.useDecl(useName);
            }
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    public void setValue(String name, Object value) {
        
        if (name.equals("children")) {
            if (value instanceof Encodable) {
                children.add((Encodable)value);
            } else if (value instanceof Encodable[]) {
                Encodable[] e = (Encodable[])value;
                for (int i = 0; i < e.length; i++) {
                    children.add(e[i]);
                }
            }
        } else if (name.equals("translation")) {
            if (value instanceof String) {
                translation = fieldReader.SFVec3f((String)value);
            } else if (value instanceof String[]) {
                translation = fieldReader.SFVec3f((String[])value);
            } else if (value instanceof float[]) {
                translation = (float[])value;
            }
        } else if (name.equals("center")) {
            if (value instanceof String) {
                center = fieldReader.SFVec3f((String)value);
            } else if (value instanceof String[]) {
                center = fieldReader.SFVec3f((String[])value);
            } else if (value instanceof float[]) {
                center = (float[])value;
            }
        } else if (name.equals("scale")) {
            if (value instanceof String) {
                scale = fieldReader.SFVec3f((String)value);
            } else if (value instanceof String[]) {
                scale = fieldReader.SFVec3f((String[])value);
            } else if (value instanceof float[]) {
                scale = (float[])value;
            }
        } else if (name.equals("rotation")) {
            if (value instanceof String) {
                rotation = fieldReader.SFRotation((String)value);
            } else if (value instanceof String[]) {
                rotation = fieldReader.SFRotation((String[])value);
            } else if (value instanceof float[]) {
                rotation = (float[])value;
            }
        } else if (name.equals("scaleOrientation")) {
            if (value instanceof String) {
                scaleOrientation = fieldReader.SFRotation((String)value);
            } else if (value instanceof String[]) {
                scaleOrientation = fieldReader.SFRotation((String[])value);
            } else if (value instanceof float[]) {
                scaleOrientation = (float[])value;
            }
        } 
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    public void setValue(String name, Object value, int len) {
        
        if (name.equals("children")) {
            if (value instanceof Encodable[]) {
                Encodable[] e = (Encodable[])value;
                for (int i = 0; i < len; i++) {
                    children.add(e[i]);
                }
            }
        } else if (name.equals("translation")) {
            if (value instanceof float[]) {
                translation = (float[])value;
            }
        } else if (name.equals("center")) {
            if (value instanceof float[]) {
                center = (float[])value;
            }
        } else if (name.equals("scale")) {
            if (value instanceof float[]) {
                scale = (float[])value;
            }
        } else if (name.equals("rotation")) {
            if (value instanceof float[]) {
                rotation = (float[])value;
            }
        } else if (name.equals("scaleOrientation")) {
            if (value instanceof float[]) {
                scaleOrientation = (float[])value;
            }
        } 
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Return the transform matrix
     *
     * @return the transform matrix
     */
    public Matrix4f getMatrix() {
        if (matrixSource == null) {
            matrixSource = new TransformMatrix();
        }
        if (translation != null) {
            matrixSource.setTranslation(translation);
        }
        if (rotation != null) {
            matrixSource.setRotation(rotation);
        }
        if (scale != null) {
            matrixSource.setScale(scale);
        }
        if (scaleOrientation != null) {
            matrixSource.setScaleOrientation(scaleOrientation);
        }
        if (center != null) {
            matrixSource.setCenter(center);
        }
        return(matrixSource.getMatrix());
    }
}
