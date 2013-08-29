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
package org.web3d.vrml.nodes;

// Standard imports
import java.util.List;

// Application specific imports
import org.web3d.vrml.lang.FieldExistsException;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLFieldDeclaration;

/**
 * Shell representation of a script node.
 * <p>
 *
 * The script is different to all the other nodes. While it represents
 * a script, it doesn't have the normal content of a node. This will be an
 * interface to interact between the script and an external scripting engine.
 * Quite how we are going to do this remains an interesting thing to consider.
 * <p>
 *
 * When implementing this interface, the coder must be aware of how scripts
 * fit into the larger event model picture. A script cannot act just on the
 * content alone. It has certain other notification responsibilities that
 * require extra hoops to be jumped through. In particular, there are the
 * following requirements:
 *
 * <ul>
 * <li>A script does not call initialize() immediately that the content is
 *     set. It must wait for the appropriate call as the timing of this is
 *     defined by the event model specification.
 * </li>
 * <li>When a setUrl() call is made where there is already content set, the
 *     implementation should not call shutdown or delete the reference to the
 *     user script code. It must wait for the appropriate external call.
 * </li>
 * <li>When the node is being deleted from the scene graph, it will be told
 *     when to call shutdown on the nodes. It should not override the
 *     updateRefCount() method to determine when to do it.
 * </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public interface VRMLScriptNodeType
    extends VRMLChildNodeType, VRMLSingleExternalNodeType {

    /**
     * Get the count of the number of fields currently registered.
     *
     * @return The number of fields available
     */
    public int getFieldCount();

    /**
     * Append a field declaration to this node. This is added to the current
     * list on the end. If the field already exists with the given name but
     * different values exception will be generated. If the field has exactly
     * the same signature it will silently ignore the request.
     *
     * @param field The new field to add
     * @return The index that this field was added at
     * @throws FieldExistsException A conflicting field of the same name
     *   already exists for this node
     */
    public int appendField(VRMLFieldDeclaration field)
        throws FieldExistsException;

    /**
     * Make a listing of all fields that are currently registered in this
     * node. The list contains instances of
     * {@link org.web3d.vrml.lang.VRMLFieldDeclaration}.
     *
     * @return A list of the current field declarations
     */
    public List getAllFields();

    /**
     * Set the execution space to be this new value. Usually set at some time
     * after the initial loading of the class, but before content is set. A
     * value of null will clear the current space.
     *
     * @param space The space that this script is operating in
     */
    public void setExecutionSpace(VRMLExecutionSpace space);

    /**
     * Get the execution space that this script is operating under. If there
     * is none, this will return null.
     *
     * @return The current execution space or null
     */
    public VRMLExecutionSpace getExecutionSpace();

    /**
     * Notification to call the prepare-events for scripts at the start of the
     * timestamp. For scripts that are in an X3D world, this will also call the
     * prepareEvents SAI service. This method will be called every frame.
     *
     * @param timestamp The time of the current frame in VRML time
     */
    public void prepareEvents(double timestamp);

    /**
     * Notification by the route manager that an event cascade is complete.
     * This should allow the underlying scripting engine to call
     * <code>processEvents()</code> (or equivalent) on the script code.
     */
    public void processEvents();

    /**
     * Notification by the route manager that an event cascade is complete.
     * This should allow the underlying scripting engine to call
     * <code>eventsProcessed()</code> on the script code.
     */
    public void eventsProcessed();

    /**
     * Notification that the script can now call the initialize() method on the
     * user script code. If this is called accidentally when there is no user
     * code set, silently ignore the request.
     *
     * @param timestamp The VRML time that the initialisation occured at
     */
    public void initialize(double timestamp);

    /**
     * Call shutdown on the user content now. It will no longer be needed. This
     * does not shut down the entire node. It is assumed that content will be
     * forcoming shortly.
     */
    public void shutdown();

    /**
     * Completely shutdown this script node. There's no life left in this one
     * so might as well clean up everything. The user code shutdown will be
     * guaranteed to be called before this method.
     */
    public void shutdownAll();
}
