/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d;

// External imports
// None

// :Local imports
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.lang.NodeTemplateToInstanceCreator;
import org.web3d.vrml.renderer.CRExternPrototypeDecl;
import org.web3d.vrml.renderer.CRExternProtoBuilder;

/**
 * A SAV interface for dealing with building a single extern proto.
 * <p>
 *
 * The builder is designed to create a single proto. However, that single proto
 * may well have nested protos as part of it, so we must deal with that too.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.13 $
 */
public class J3DExternProtoBuilder extends CRExternProtoBuilder {

    /**
     * Create a new default instance of the scene builder. This uses the
     * default factory for nodes non-renderable nodes.
     *
     * @param fac The factory instance to create nodes of the instance
     * @throws NullPointerException The factory reference is null
     */
    public J3DExternProtoBuilder(VRMLNodeFactory fac)
        throws NullPointerException {

        super(fac);
    }


    //---------------------------------------------------------------
    // Methods defined by CRExternProtoBuilder
    //---------------------------------------------------------------

    /**
     * Create a declaration suitable for filling in as a new proto.
     *
     * @param name The name applied to the proto decl in the file
     * @param fac The factory to use for creating the instances
     * @param majorVersion The major version number of this scene
     * @param minorVersion The minor version number of this scene
     * @param creator The node creator for generating instances of ourself
     */
    public CRExternPrototypeDecl createDecl(String name,
                                            VRMLNodeFactory fac,
                                            int majorVersion,
                                            int minorVersion,
                                            NodeTemplateToInstanceCreator creator) {

        CRExternPrototypeDecl decl =
            new J3DExternPrototypeDecl(name,
                                       fac,
                                       majorVersion,
                                       minorVersion,
                                       creator);
        decl.setErrorReporter(errorReporter);

        return decl;
    }
}
