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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Local imports
import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.sav.ContentHandler;

/**
 * Factory for producing Encodable node instances.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class EncodableFactory {
    
    /** The field parser */
    private VRMLFieldReader reader;
    
    /** Content Handler reference */
    private ContentHandler handler;
    
    /**
     * Constructor
     */
    public EncodableFactory(ContentHandler handler, VRMLFieldReader reader) {
        this.handler = handler;
        this.reader = reader;
    }
    
    /**
     * Return the Encodable node per the argument node name
     *
     * @param name The node name
     * @param defName The DEF name to assign to the node
     */
    public Encodable getEncodable(String name, String defName) {
        
        AbstractEncodable enc = null;
        try {
            String classname = "xj3d.filter.node." + name;
            Class c = Class.forName(classname);
            Class[] param = new Class[]{String.class};
            Constructor constructor = c.getConstructor(param);
            Object[] arg = new Object[]{defName};
            enc = (AbstractEncodable)constructor.newInstance(arg);
            
            enc.setContentHandler(handler);
            enc.setFieldReader(reader);
            
        } catch (ClassNotFoundException cnfe) {
        } catch (NoSuchMethodException nsme) {
        } catch (InvocationTargetException ite) {
        } catch (InstantiationException ie) {
        } catch (IllegalAccessException iae) {
        }
        return(enc);
    }
}
