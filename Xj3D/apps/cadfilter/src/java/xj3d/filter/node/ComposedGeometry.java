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
 * Base abstract impl wrapper for X3DComposedGeometry nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public abstract class ComposedGeometry extends Geometry {
    
    /** Field value */
    public boolean ccw;
    
    /** Field value */
    public boolean colorPerVertex;
    
    /** Field value */
    public boolean normalPerVertex;
    
    /** Field value */
    public boolean solid;
    
    /** The Coordinate node */
    private Coordinate coord;
    
    /** The Normal node */
    private Normal normal;
    
    /** The Color node */
    private Color color;
    
    /** The TextureCoordinate node */
    private TextureCoordinate texCoord;
    
    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected ComposedGeometry(String name, String defName) {
        super(name, defName);
        
        ccw = true;
        colorPerVertex = true;
        normalPerVertex = true;
        solid = true;
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Push the node contents to the ContentHandler.
     */
    public void encode() {
        
        if (handler != null) {
            
            if (coord != null) {
                handler.startField("coord");
                coord.encode();
                handler.endField();
            }
            
            if (normal != null) {
                handler.startField("normal");
                normal.encode();
                handler.endField();
            }
            
            if (color != null) {
                handler.startField("color");
                color.encode();
                handler.endField();
            }
            
            if (texCoord != null) {
                handler.startField("texCoord");
                texCoord.encode();
                handler.endField();
            }
            
            handler.startField("ccw");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(ccw);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(ccw));
                break;
            }
            
            handler.startField("colorPerVertex");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(colorPerVertex);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(colorPerVertex));
                break;
            }
            
            handler.startField("normalPerVertex");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(normalPerVertex);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(normalPerVertex));
                break;
            }
            
            handler.startField("solid");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(solid);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(solid));
                break;
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
        
        if (name.equals("coord")) {
            if (value instanceof Coordinate) {
                coord = (Coordinate)value;
            }
        } else if (name.equals("normal")) {
            if (value instanceof Normal) {
                normal = (Normal)value;
            }
        } else if (name.equals("color")) {
            if (value instanceof Color) {
                color = (Color)value;
            }
        } else if (name.equals("texCoord")) {
            if (value instanceof TextureCoordinate) {
                texCoord = (TextureCoordinate)value;
            }
        } else if (name.equals("ccw")) {
            if (value instanceof String) {
                ccw = Boolean.parseBoolean((String)value);
            } else if (value instanceof Boolean) {
                ccw = ((Boolean)value).booleanValue();
            }
        } else if (name.equals("colorPerVertex")) {
            if (value instanceof String) {
                colorPerVertex = Boolean.parseBoolean((String)value);
            } else if (value instanceof Boolean) {
                colorPerVertex = ((Boolean)value).booleanValue();
            }
        } else if (name.equals("normalPerVertex")) {
            if (value instanceof String) {
                normalPerVertex = Boolean.parseBoolean((String)value);
            } else if (value instanceof Boolean) {
                normalPerVertex = ((Boolean)value).booleanValue();
            }
        } else if (name.equals("solid")) {
            if (value instanceof String) {
                solid = Boolean.parseBoolean((String)value);
            } else if (value instanceof Boolean) {
                solid = ((Boolean)value).booleanValue();
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
    
    //----------------------------------------------------------
    // Methods defined by AbstractEncodable
    //----------------------------------------------------------
    
    /**
     * Copy the working objects of this into the argument. Used
     * by subclasses to initialize a clone.
     * 
     * @param enc The encodable to initialize.
     */
    protected void copy(AbstractEncodable enc) {
        super.copy(enc);
        if (enc instanceof ComposedGeometry) {
            ComposedGeometry that = (ComposedGeometry)enc;
            that.ccw = this.ccw;
            that.colorPerVertex = this.colorPerVertex;
            that.normalPerVertex = this.normalPerVertex;
            that.solid = this.solid;
            if (this.coord != null) {
                that.coord = (Coordinate)this.coord.clone();
            }
            if (this.normal != null) {
                that.normal = (Normal)this.normal.clone();
            }
            if (this.color != null) {
                that.color = (Color)this.color.clone();
            }
            if (this.texCoord != null) {
                that.texCoord = (TextureCoordinate)this.texCoord.clone();
            }
        }
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Set the Coordinate node wrapper
     *
     * @param coord The Coordinate node wrapper
     */
    public void setCoordinate(Coordinate coord) {
        this.coord = coord;
    }
    
    /**
     * Get the Coordinate node wrapper
     *
     * @return The Coordinate node wrapper
     */
    public Coordinate getCoordinate() {
        return(coord);
    }
    
    /**
     * Set the Normal node wrapper
     *
     * @param normal The Normal node wrapper
     */
    public void setNormal(Normal normal) {
        this.normal = normal;
    }
    
    /**
     * Get the Normal node wrapper
     *
     * @return The Normal node wrapper
     */
    public Normal getNormal() {
        return(normal);
    }
    
    /**
     * Set the Color node wrapper
     *
     * @param color The Color node wrapper
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Get the Color node wrapper
     *
     * @return The Color node wrapper
     */
    public Color getColor() {
        return(color);
    }
    
    /**
     * Set the TextureCoordinate node wrapper
     *
     * @param texCoord The TextureCoordinate node wrapper
     */
    public void setTextureCoordinate(TextureCoordinate texCoord) {
        this.texCoord = texCoord;
    }
    
    /**
     * Get the TextureCoordinate node wrapper
     *
     * @return The TextureCoordinate node wrapper
     */
    public TextureCoordinate getTextureCoordinate() {
        return(texCoord);
    }
}
