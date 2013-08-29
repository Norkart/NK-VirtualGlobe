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

// Local imports
import org.web3d.vrml.sav.ContentHandler;

import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for the X3D Material node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class Material extends AbstractEncodable {
    
    /** Field value */
    public float ambientIntensity;
    
    /** Field value */
    public float[] diffuseColor;
    
    /** Field value */
    public float[] emissiveColor;
    
    /** Field value */
    public float shininess;
    
    /** Field value */
    public float[] specularColor;
    
    /** Field value */
    public float transparency;
    
    /**
     * Constructor
     */
    public Material() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Material(String defName) {
        super("Material", defName);
        
        ambientIntensity = -1;
        shininess = -1;
        transparency = -1;
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler.
     */
    public void encode() {
        
        if (handler != null) {
            if (useName == null) {
                handler.startNode(name, defName);
                
                if ((ambientIntensity >= 0) && (ambientIntensity <= 1)) {
                    handler.startField("ambientIntensity");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(ambientIntensity);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(ambientIntensity));
                        break;
                    }
                }
                
                if (diffuseColor != null) {
                    handler.startField("diffuseColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(diffuseColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(diffuseColor, 3));
                        break;
                    }
                }
                
                if (emissiveColor != null) {
                    handler.startField("emissiveColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(emissiveColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(emissiveColor, 3));
                        break;
                    }
                }
                
                if ((shininess >= 0) && (shininess <= 1)) {
                    handler.startField("shininess");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(shininess);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(shininess));
                        break;
                    }
                }
                
                if (specularColor != null) {
                    handler.startField("specularColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(specularColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(specularColor, 3));
                        break;
                    }
                }
                
                if ((transparency >= 0) && (transparency <= 1)) {
                    handler.startField("transparency");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(transparency);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(transparency));
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
        
        if (name.equals("ambientIntensity")) {
            if (value instanceof String) {
                ambientIntensity = Float.parseFloat((String)value);
            } else if (value instanceof Float) {
                ambientIntensity = ((Float)value).floatValue();
            }
        } else if (name.equals("diffuseColor")) {
            if (value instanceof String) {
                diffuseColor = fieldReader.SFColor((String)value);
            } else if (value instanceof String[]) {
                diffuseColor = fieldReader.SFColor((String[])value);
            } else if (value instanceof float[]) {
                diffuseColor = (float[])value;
            }
        } else if (name.equals("emissiveColor")) {
            if (value instanceof String) {
                emissiveColor = fieldReader.SFColor((String)value);
            } else if (value instanceof String[]) {
                emissiveColor = fieldReader.SFColor((String[])value);
            } else if (value instanceof float[]) {
                emissiveColor = (float[])value;
            }
        } else if (name.equals("shininess")) {
            if (value instanceof String) {
                shininess = Float.parseFloat((String)value);
            } else if (value instanceof Float) {
                shininess = ((Float)value).floatValue();
            }
        } else if (name.equals("specularColor")) {
            if (value instanceof String) {
                specularColor = fieldReader.SFColor((String)value);
            } else if (value instanceof String[]) {
                specularColor = fieldReader.SFColor((String[])value);
            } else if (value instanceof float[]) {
                specularColor = (float[])value;
            }
        } else if (name.equals("transparency")) {
            if (value instanceof String) {
                transparency = Float.parseFloat((String)value);
            } else if (value instanceof Float) {
                transparency = ((Float)value).floatValue();
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
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        Material m = new Material();
        copy(m);
        m.ambientIntensity = this.ambientIntensity;
        m.shininess = this.shininess;
        m.transparency = this.transparency;
        if (this.diffuseColor != null) {
            m.diffuseColor = new float[3];
            System.arraycopy(this.diffuseColor, 0, m.diffuseColor, 0, 3);
        }
        if (this.emissiveColor != null) {
            m.emissiveColor = new float[3];
            System.arraycopy(this.emissiveColor, 0, m.emissiveColor, 0, 3);
        }
        if (this.specularColor != null) {
            m.specularColor = new float[3];
            System.arraycopy(this.specularColor, 0, m.specularColor, 0, 3);
        }
        return(m);
    }
}
