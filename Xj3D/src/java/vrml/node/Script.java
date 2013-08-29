/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package vrml.node;

// Standard imports
import java.util.Map;

// Application specific imports
import vrml.*;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * This is the general Script class, to be subclassed by all scripts.
 * Note that the provided methods allow the script author to explicitly
 * throw tailored exceptions in case something goes wrong in the
 * script.
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public abstract class Script extends BaseNode {

    /** Mapping of the field name to the field value */
    private Map fieldMap;

    /**
     * Prepare the script for execution. Should never be called by user code.
     * belongs to a local usage only.
     *
     * @param b The browser reference to use
     * @param fields The mapping of field names to JSAI field instances
     * @param node The real node representing this script
     */
    public void prepareScript(Browser b, Map fields, VRMLNodeType node) {
        browser = b;
        fieldMap = fields;
        nodeName = "Script";
        realNode = node;
    }

    /**
     * Get a Field by name.
     *
     * @param fieldName The requested field
     * @throws InvalidFieldException if fieldName isn't a valid field name
     */
    protected final Field getField(String fieldName)
        throws InvalidFieldException {

        Field ret_val = (Field)fieldMap.get(fieldName);

        if(ret_val == null)
            throw new InvalidFieldException("Field not known:" + fieldName);

        return ret_val;
    }

    /**
     * Get an EventOut by name.
     *
     * @param fieldName The requested eventOut
     * @throws InvalidEventOutException if fieldName isn't a valid name
     */
    protected final Field getEventOut(String fieldName)
        throws InvalidEventOutException {

        Field ret_val = (Field)fieldMap.get(fieldName);

        if(ret_val == null)
            throw new InvalidEventOutException("Field not known:" + fieldName);

        return ret_val;
    }

    /**
     * Get an EventIn by name.
     *
     * @param fieldName The requested eventIn
     * @throws InvalidEventInException if fieldName isn't a valid name
     */
    protected final Field getEventIn(String fieldName)
        throws InvalidEventInException {

        Field ret_val = (Field)fieldMap.get(fieldName);

        if(ret_val == null)
            throw new InvalidEventInException("Field not known:" + fieldName);

        return ret_val;
    }

    /**
     * Called before any event is generated
     */
    public void initialize() {
    }

    /**
     * Called automatically when the script receives some set of events. It
     * shall not be called directly except by its subclass.
     *
     * @param count indicates the number of events delivered.
     * @param events The events to process
     */
    public void processEvents(int count, Event[] events ) {
        for (int i = 0; i < count; i++){
            processEvent(events[i]);
        }
    }

    /**
     * Called when there is an Event to be processed
     *
     * @param event The event to process
     */
    public void processEvent(Event event) {
    }

    /**
     * Called after every invocation of processEvents()
     */
    public void eventsProcessed() {
    }

    /**
     * Called when the Script node is deleted.
     */
    public void shutdown() {
    }
}

