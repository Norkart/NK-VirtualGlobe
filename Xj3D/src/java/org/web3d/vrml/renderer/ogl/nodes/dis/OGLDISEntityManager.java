/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.dis;

// External imports
import java.util.ArrayList;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.dis.BaseDISEntityManager;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.ogl.nodes.dis.OGLEspduTransform;
import mil.navy.nps.dis.EntityStatePdu;
import mil.navy.nps.dis.EntityID;
import org.web3d.xmsf.dis.*;

/**
 * OGL renderer implementation of a DISEntityManager node.
 * <p>
 *
 * This node is purely informational within the scenegraph. It does not have
 * a renderable representation.
 *
 * @author Alan Hudson, Vivian Gottesman
 * @version $Revision: 1.2 $
 */
public class OGLDISEntityManager extends BaseDISEntityManager
    implements OGLVRMLNode, VRMLSingleExternalNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.SingleExternalNodeType };

    /** New entities, will become addedEntities at end of frame */
    private ArrayList addedEntities;

    /** Removed entities, will become removedEntities at end of frame */
    private ArrayList removedEntities;

    /** Are there new added entities */
    private boolean newAddedEntities;

    /** Are there new removed entities */
    private boolean newRemovedEntities;

    /** The world URL */
    private String worldURL;

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public OGLDISEntityManager() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLDISEntityManager(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    //----------------------------------------------------------
    // Methods overriding BaseGroup class.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propogate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    public void allEventsComplete() {
        if (newAddedEntities) {
            vfAddedEntities.clear();

            synchronized(addedEntities) {
                vfAddedEntities.addAll(addedEntities);
                addedEntities.clear();
                newAddedEntities = false;
            }

            hasChanged[FIELD_ADDED_ENTITIES] = true;
            fireFieldChanged(FIELD_ADDED_ENTITIES);
        }

        if (newRemovedEntities) {
            vfRemovedEntities.clear();

            synchronized(removedEntities) {
                vfRemovedEntities.addAll(removedEntities);
                removedEntities.clear();
                newRemovedEntities = false;
            }

            hasChanged[FIELD_REMOVED_ENTITIES] = true;
            fireFieldChanged(FIELD_REMOVED_ENTITIES);
        }
    }

    /**
     * An entity has been removed from the simulation.
     *
     * @param espdu The entity being removed
     */
    public void entityRemoved(VRMLDISNodeType node) {
        synchronized(removedEntities) {
            removedEntities.add(node);
            newRemovedEntities = true;
        }
        stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * A new entity has arrived.
     *
     * @param entity The new entity.
     */
    public void entityArrived(EntityStatePduType espdu) {
        System.out.println("DISXML entity arrival ignored");
    }

    /**
     * A new entity has arrived.
     *
     * @param entity The new entity.
     */
    public void entityArrived(EntityStatePdu espdu) {
        if (nodeFactory == null) {
            nodeFactory = DefaultNodeFactory.createFactory(
                DefaultNodeFactory.OPENGL_RENDERER );
        }

        try {
            // Is this available from AbstractNode?
            nodeFactory.setSpecVersion(vrmlMajorVersion, vrmlMinorVersion);
            nodeFactory.setProfile("Immersive");
            nodeFactory.addComponent("DIS",1);
        } catch(UnsupportedProfileException upe) {
            upe.printStackTrace();
        }

        OGLEspduTransform node =
            (OGLEspduTransform)nodeFactory.createVRMLNode("EspduTransform",
                                                          false);

        int idx, domain, kind, country, subCategory, category, extra, specific;
        country = espdu.getEntityTypeCountry().intValue();
        domain = espdu.getEntityTypeDomain().intValue();
        category = espdu.getEntityTypeCategory().intValue();
        subCategory = espdu.getEntityTypeSubcategory().intValue();
        kind = espdu.getEntityTypeKind().intValue();
        specific = espdu.getEntityTypeSpecific().intValue();
        extra = espdu.getEntityTypeExtra().intValue();

        int len = vfMapping.size();
        String[] urlString = null;
        int currentMatched, previousMatched;
        previousMatched = -1;
        VRMLNodeType mappingNode;
        for (int i=0; i < len; i++) {
            currentMatched = 0;
            int[] compare = new int [7];
            mappingNode = (VRMLNodeType)vfMapping.get(i);

            idx = mappingNode.getFieldIndex("kind");
            compare[0] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("domain");
            compare[1] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("country");
            compare[2] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("category");
            compare[3] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("subCategory");
            compare[4] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("specific");
            compare[5] = (mappingNode.getFieldValue(idx)).intValue;
            idx = mappingNode.getFieldIndex("extra");
            compare[6] = (mappingNode.getFieldValue(idx)).intValue;

            if (kind == compare[0]) {
                currentMatched++;
                if (domain == compare[1]) {
                    currentMatched++;
                    if (country == compare[2]) {
                        currentMatched++;
                        if (category == compare[3]) {
                            currentMatched++;
                            if (subCategory == compare[4]) {
                                currentMatched++;
                                if (specific == compare[5]) {
                                    currentMatched++;
                                    if (extra == compare[6]) {
                                        currentMatched++;
                                    } else if (compare[6] != 0) {
                                        currentMatched = 0;
                                    }
                                } else if (compare[5] != 0) {
                                    currentMatched = 0;
                                }
                            } else if (compare[4] != 0) {
                                currentMatched = 0;
                            }
                        } else if (compare[3] != 0) {
                            currentMatched = 0;
                        }
                    } else if (compare[2] != 0) {
                        currentMatched = 0;
                    }
                } else if (compare[1] != 0) {
                    currentMatched = 0;
                } else if (compare[0] != 0) {
                    currentMatched = 0;
                }
            }

            if (currentMatched > previousMatched) {
                idx = mappingNode.getFieldIndex("url");
                urlString = (mappingNode.getFieldValue(idx)).stringArrayValue;
                previousMatched = currentMatched;
            }
            if (currentMatched == 7) {
                break;
            }
        }

        idx = node.getFieldIndex("entityCountry");
        node.setValue(idx, country);
        idx = node.getFieldIndex("entityDomain");
        node.setValue(idx, domain);
        idx = node.getFieldIndex("entityCategory");
        node.setValue(idx, category);
        idx = node.getFieldIndex("entitySubCategory");
        node.setValue(idx, subCategory);
        idx = node.getFieldIndex("entityKind");
        node.setValue(idx, kind);
        idx = node.getFieldIndex("entitySpecific");
        node.setValue(idx, specific);
        idx = node.getFieldIndex("entityExtra");
        node.setValue(idx, extra);
//        idx = node.getFieldIndex("marking");
//        node.setValue(idx, urlString);

        idx = node.getFieldIndex("entityID");
        EntityID entityID = espdu.getEntityID();
        node.setValue(idx, entityID.getEntityIDValue());
        idx = node.getFieldIndex("applicationID");
        node.setValue(idx, entityID.getApplicationIDValue());
        idx = node.getFieldIndex("siteID");
        node.setValue(idx, entityID.getSiteIDValue());
        idx = node.getFieldIndex("networkMode");
        node.setValue(idx, "networkReader");
        idx = node.getFieldIndex("address");
        node.setValue(idx, vfAddress);
        idx = node.getFieldIndex("port");
        node.setValue(idx, vfPort);
        node.setFrameStateManager(stateManager);
        node.setupFinished();

        if (urlString != null) {
            VRMLNodeType inline =
                (VRMLNodeType)nodeFactory.createVRMLNode("Inline",false);

            idx = inline.getFieldIndex("url");

            inline.setValue(idx, urlString, urlString.length);
            inline.setFrameStateManager(stateManager);
            ((VRMLExternalNodeType)inline).setWorldUrl(worldURL);
            inline.setupFinished();

            idx = node.getFieldIndex("children");

            node.setValue(idx, inline);
        }

        synchronized(addedEntities) {
            addedEntities.add(node);
            newAddedEntities = true;
        }
        stateManager.addEndOfThisFrameListener(this);
    }


    //----------------------------------------------------------
    // Methods defined  by VRMLExternalNodeType
    //----------------------------------------------------------

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    public int getLoadState() {
        return VRMLSingleExternalNodeType.LOAD_COMPLETE;
    }

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    public void setLoadState(int state) {
    }

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    public void setWorldUrl(String url) {
        if((url == null) || (url.length() == 0))
            return;

        // check for a trailing slash. If it doesn't have one, append it.
        if(url.charAt(url.length() - 1) != '/') {
            worldURL = url + '/';
        } else {
            worldURL = url;
        }

        worldURL = url;
    }

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    public String getWorldUrl() {
        return worldURL;
    }

    /**
     * Sets the URL to a new value.  We will load only one
     * of these URL's.  The list provides alternates.
     *
     * @param newURL Array of candidate URL strings
     * @param numValid The number of valid values to copy from the array
     */
    public void setUrl(String[] newURL, int numValid) {
    }

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    public String[] getUrl() {
        return new String[0];
    }

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype) {
        return true;
    }

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param uri The URI used to load this content
     */
    public void setLoadedURI(String uri) {
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param ul The listener instance to add
     */
    public void addUrlListener(VRMLUrlListener ul) {
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param ul The listener to be removed
     */
    public void removeUrlListener(VRMLUrlListener ul) {
    }

    /**
     * Add a listener to this node instance for the content state changes. If
     * the listener is already added or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addContentStateListener(VRMLContentStateListener l) {
    }

    /**
     * Remove a listener from this node instance for the content state changes.
     * If the listener is null or not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeContentStateListener(VRMLContentStateListener l) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Internal convenience method to initialise the OpenGL data structures.
     */
    private void init() {
        addedEntities = new ArrayList();
        removedEntities = new ArrayList();
        newAddedEntities = false;
        newRemovedEntities = false;
    }
}

