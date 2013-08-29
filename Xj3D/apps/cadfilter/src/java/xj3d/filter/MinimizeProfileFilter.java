/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// Local imports
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.xj3d.core.loading.*;


import org.web3d.vrml.lang.*;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.parser.DefaultVRMLParserFactory;

import org.xj3d.sai.X3DNodeComponentMapper;

/**
 * Minimize a files profile and component declaration to the minimal
 * declaration.  Prefer a profile decl to a long component decl.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class MinimizeProfileFilter extends TwoPassFilter {

    /** Nodes in the interchage profile */
    private VRMLNodeFactory interchangeFactory;

    /** Nodes in the interactive profile */
    private VRMLNodeFactory interactiveFactory;

    /** Nodes in the immersive profile */
    private VRMLNodeFactory immersiveFactory;

    /** Are we in the first pass */
    private boolean firstPass;

    /** Are we within the interchange profile */
    private boolean interchange;

    /** Are we within the interactive profile */
    private boolean interactive;

    /** Are we withing the immersive profile */
    private boolean immersive;

    /** List of added components from base profile */
    private ArrayList<String> components;

    /** List of component levels added */
    private ArrayList<Integer> compLevels;

    /** The major spec version */
    private int major;

    /** The minor spec version */
    private int minor;

    /** Component mapper */
    private X3DNodeComponentMapper compMapper;

    /** A node to component mapper */
    public MinimizeProfileFilter() {
        firstPass = true;

        interchange = true;
        interactive = true;
        immersive = true;

        components = new ArrayList<String>();
        compLevels = new ArrayList<Integer>();
    }

    //----------------------------------------------------------
    // Methods defined by TwoPassFilter
    //----------------------------------------------------------

    /**
     * Notification that the start of the first pass is beginning.
     */
    public void startFirstPass() {
        firstPass = true;
    }

    /**
     * Notification that the second of the first pass is beginning.
     */
    public void startSecondPass() {
        firstPass = false;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The full VRML version string of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        compMapper = X3DNodeComponentMapper.getInstance();

        major = 3;

        // Parse the number string looking for the version minor number.
        int dot_index = version.indexOf('.');
        String minor_num = version.substring(dot_index + 1);

        // Should this look for a badly formatted number here or just
        // assume the parsing beforehad has correctly identified something
        // already dodgy?
        minor = Integer.parseInt(minor_num);

        interchangeFactory = DefaultNodeFactory.createFactory(DefaultNodeFactory.NULL_RENDERER);
        interchangeFactory.setSpecVersion(major,minor);
        interchangeFactory.setProfile("Interchange");

        interactiveFactory = DefaultNodeFactory.createFactory(DefaultNodeFactory.NULL_RENDERER);
        interactiveFactory.setSpecVersion(major,minor);
        interactiveFactory.setProfile("Interactive");

        immersiveFactory = DefaultNodeFactory.createFactory(DefaultNodeFactory.NULL_RENDERER);
        immersiveFactory.setSpecVersion(major,minor);
        immersiveFactory.setProfile("Immersive");

        // Handled in SAVAdapter
        contentHandler.startDocument(uri, url, encoding, type, version, comment);
    }

    /**
     * A profile declaration has been found in the code. IAW the X3D
     * specification, this method will only ever be called once in the lifetime
     * of the parser for this document. The name is the name of the profile
     * for the document to use.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {

        if (!firstPass) {
            if (interchange)
                profileName = "Interchange";
            else if (interactive)
                profileName = "Interactive";
            else if (immersive)
                profileName = "Immersive";

            errorHandler.messageReport("New profile: " + profileName);
        } else {
            errorHandler.messageReport("Original profile: " + profileName);
        }

        contentHandler.profileDecl(profileName);

        int len = components.size();

        for(int i=0; i < len; i++) {
            contentHandler.componentDecl(components.get(i) + ":" + compLevels.get(i));
        }
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentName The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void componentDecl(String componentInfo)
        throws SAVException, VRMLException {

        if (firstPass)
            contentHandler.componentDecl(componentInfo);
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        contentHandler.startNode(name, defName);

        try {
            VRMLNode n = interchangeFactory.createVRMLNode(name, false);
        } catch(UnsupportedNodeException une) {
            interchange = false;
        }

        try {
            VRMLNode n = interactiveFactory.createVRMLNode(name, false);
        } catch(UnsupportedNodeException une) {
            interactive = false;
        }

        try {
            VRMLNode n = immersiveFactory.createVRMLNode(name, false);
        } catch(UnsupportedNodeException une) {
            // Keep in Immersive
            String comp = compMapper.getComponentName(name);
            Integer level = compMapper.getComponentLevel(name);

            if (comp != null) {
                components.add(comp);
                compLevels.add(level);
            } else {
                errorHandler.messageReport("Can't find component mapping for: " + name);
            }
        }
    }
}
