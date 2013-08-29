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

package org.web3d.vrml.renderer.mobile;

// Standard imports
import java.util.List;

// Application specific imports
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.proto.ExternalPrototypeDecl;
import org.web3d.vrml.renderer.CRExternProtoCreator;

/**
 * A class that is used to create stub instances of extern protos from their
 * definitions.
 * <p>
 *
 * The creator strips the definition apart and builds a runtime node based on
 * the details and the node factory provided. The creator can handle one
 * instance at a time, athough it will correctly parse and build nested proto
 * declarations without extra effort.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class MobileExternProtoCreator extends CRExternProtoCreator {

    /** The proto instance we're building up */
    private MobileProtoInstance protoInstance;

    /**
     * Create a new creator instance for the given world root URL
     *
     * @param worldURL the current world's root URL
     */
    public MobileExternProtoCreator(String worldURL) {

      super(worldURL);
    }

    /**
     * Build a stub instance of the proto from the given description.
     *
     * @param proto The extern proto to stub from
     * @return A grouping node representing the body of the active node
     */
    MobileProtoInstance createInstance(ExternalPrototypeDecl proto) {

        List field_list = proto.getAllFields();

        VRMLFieldDeclaration[] field_decls =
            new VRMLFieldDeclaration[field_list.size()];
        field_list.toArray(field_decls);

        isVRML97 = proto.isVRML97();

        MobileProtoInstance protoInstance =
            new MobileProtoInstance(proto.getVRMLNodeName(),
                                 isVRML97,
                                 field_decls,
                                 0);

        super.createInstance(protoInstance);

        return protoInstance;
    }
}
