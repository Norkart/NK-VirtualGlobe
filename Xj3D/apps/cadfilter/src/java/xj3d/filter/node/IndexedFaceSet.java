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
 * Wrapper for an X3D IndexedFaceSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class IndexedFaceSet extends ComposedGeometry {
    
    /** Field value */
    public int[] coordIndex;
    
    /** Number of indices in the coordIndex array */
    public int num_coordIndex;
    
    /** Field value */
    public int[] colorIndex;
    
    /** Number of indices in the colorIndex array */
    public int num_colorIndex;
    
    /** Field value */
    public int[] normalIndex;
    
    /** Number of indices in the normalIndex array */
    public int num_normalIndex;
    
    /** Field value */
    public int[] texCoordIndex;
    
    /** Number of indices in the texCoordIndex array */
    public int num_texCoordIndex;
    
    /** Field value */
    public boolean convex;
    
    /** Field value */
    public float creaseAngle;
    
    /**
     * Constructor
     */
    public IndexedFaceSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public IndexedFaceSet(String defName) {
        super("IndexedFaceSet", defName);
        
        convex = true;
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
                
                super.encode();
                
                if (coordIndex != null) {
                    handler.startField("coordIndex");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(coordIndex, num_coordIndex);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(coordIndex, num_coordIndex));
                        break;
                    }
                }
                if (colorIndex != null) {
                    handler.startField("colorIndex");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(colorIndex, num_colorIndex);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(colorIndex, num_colorIndex));
                        break;
                    }
                }
                if (normalIndex != null) {
                    handler.startField("normalIndex");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(normalIndex, num_normalIndex);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(normalIndex, num_normalIndex));
                        break;
                    }
                }
                if (texCoordIndex != null) {
                    handler.startField("texCoordIndex");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(texCoordIndex, num_texCoordIndex);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(texCoordIndex, num_texCoordIndex));
                        break;
                    }
                }
                
                handler.startField("convex");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(convex);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Boolean.toString(convex));
                    break;
                }
                
                if (creaseAngle >= 0) {
                    handler.startField("creaseAngle");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(creaseAngle);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(creaseAngle));
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
        
        super.setValue(name, value);
        
        if (name.equals("coordIndex")) {
            if (value instanceof String) {
                coordIndex = fieldReader.MFInt32((String)value);
                num_coordIndex = coordIndex.length ;
            } else if (value instanceof String[]) {
                coordIndex = fieldReader.MFInt32((String[])value);
                num_coordIndex = coordIndex.length;
            } else if (value instanceof int[]) {
                coordIndex = (int[])value;
                num_coordIndex = coordIndex.length;
            }
        } else if (name.equals("colorIndex")) {
            if (value instanceof String) {
                colorIndex = fieldReader.MFInt32((String)value);
                num_colorIndex = colorIndex.length ;
            } else if (value instanceof String[]) {
                colorIndex = fieldReader.MFInt32((String[])value);
                num_colorIndex = colorIndex.length;
            } else if (value instanceof int[]) {
                colorIndex = (int[])value;
                num_colorIndex = colorIndex.length;
            }
        } else if (name.equals("normalIndex")) {
            if (value instanceof String) {
                normalIndex = fieldReader.MFInt32((String)value);
                num_normalIndex = normalIndex.length ;
            } else if (value instanceof String[]) {
                normalIndex = fieldReader.MFInt32((String[])value);
                num_normalIndex = normalIndex.length;
            } else if (value instanceof int[]) {
                normalIndex = (int[])value;
                num_normalIndex = normalIndex.length;
            }
        } else if (name.equals("texCoordIndex")) {
            if (value instanceof String) {
                texCoordIndex = fieldReader.MFInt32((String)value);
                num_texCoordIndex = texCoordIndex.length ;
            } else if (value instanceof String[]) {
                texCoordIndex = fieldReader.MFInt32((String[])value);
                num_texCoordIndex = texCoordIndex.length;
            } else if (value instanceof int[]) {
                texCoordIndex = (int[])value;
                num_texCoordIndex = texCoordIndex.length;
            }
        } else if (name.equals("convex")) {
            if (value instanceof String) {
                convex = Boolean.parseBoolean((String)value);
            } else if (value instanceof Boolean) {
                convex = ((Boolean)value).booleanValue();
            }
        } else if (name.equals("creaseAngle")) {
            if (value instanceof String) {
                creaseAngle = Float.parseFloat((String)value);
            } else if (value instanceof Float) {
                creaseAngle = ((Float)value).floatValue();
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
        
        super.setValue(name, value, len);
        
        if (name.equals("coordIndex")) {
            if (value instanceof int[]) {
                coordIndex = (int[])value;
                num_coordIndex = len;
            }
        } else if (name.equals("colorIndex")) {
            if (value instanceof int[]) {
                colorIndex = (int[])value;
                num_colorIndex = len;
            }
        } else if (name.equals("normalIndex")) {
            if (value instanceof int[]) {
                normalIndex = (int[])value;
                num_normalIndex = len;
            }
        } else if (name.equals("texCoordIndex")) {
            if (value instanceof int[]) {
                texCoordIndex = (int[])value;
                num_texCoordIndex = len;
            }
        } 
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @return a copy of this.
     */
    public Encodable clone() {
        IndexedFaceSet ifs = new IndexedFaceSet();
        copy(ifs);
        ifs.convex = this.convex;
        ifs.creaseAngle = this.creaseAngle;
        if (this.coordIndex != null) {
            ifs.coordIndex = new int[this.num_coordIndex];
            System.arraycopy(this.coordIndex, 0, ifs.coordIndex, 0, this.num_coordIndex);
        }
        if (this.colorIndex != null) {
            ifs.colorIndex = new int[this.num_colorIndex];
            System.arraycopy(this.colorIndex, 0, ifs.colorIndex, 0, this.num_colorIndex);
        }
        if (this.normalIndex != null) {
            ifs.normalIndex = new int[this.num_normalIndex];
            System.arraycopy(this.normalIndex, 0, ifs.normalIndex, 0, this.num_normalIndex);
        }
        if (this.texCoordIndex != null) {
            ifs.texCoordIndex = new int[this.num_texCoordIndex];
            System.arraycopy(this.texCoordIndex, 0, ifs.texCoordIndex, 0, this.num_texCoordIndex);
        }
        return(ifs);
    }
}
