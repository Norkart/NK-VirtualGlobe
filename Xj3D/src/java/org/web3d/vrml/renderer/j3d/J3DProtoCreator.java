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
import java.util.*;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ProtoInstancePlaceHolder;

import org.web3d.vrml.renderer.CRExternPrototypeDecl;
import org.web3d.vrml.renderer.CRProtoCreator;
import org.web3d.vrml.renderer.CRProtoInstance;


/**
 * A class that is used to create real instances of protos from their
 * definitions.
 * <p>
 *
 * The creator strips the definition apart and builds a runtime node based on
 * the details and the node factory provided. The creator can handle one
 * instance at a time, athough it will correctly parse and build nested proto
 * declarations without extra effort.
 * <p>
 *
 * We have a small conundrum to deal with - if the proto definition contains
 * SF/MFNode fields, we don't know whether the values should be also generated
 * as real runtime nodes too. Maybe the usage of this node will provide values
 * that are dealt with after this class has finished. Other times, these defaul
 * values must be used. For this implementation, we have gone with the
 * safety-first approach: Always parse the definition of any SF or MFNode field
 * and turn those into runtime instances. Although this may create extra
 * garbage, there seems to be no nice way of dealing with this issue without a
 * completely different architecture for the library.
 * <p>
 *
 * Note:
 * Under the current implementation, EXTERNPROTOs are not yet catered for.
 *
 * @author Alan Hudson
 * @version $Revision: 1.15 $
 */
public class J3DProtoCreator extends CRProtoCreator {

    /** An instance of ourselves for dealing with nested protos */
    private J3DProtoCreator protoCreator;

    /** The creator used to instantiate externprotos, if encountered */
    private J3DExternProtoCreator externProtoCreator;

    /**
     * The class constructor
     *
     * @param fac The factory to use (Must generate VRMLNodeType instances)
     * @param worldURL the current world's root URL
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     */
    public J3DProtoCreator(VRMLNodeFactory fac,
                           String worldURL,
                           int major,
                           int minor) {
        super(fac, worldURL, major, minor);
    }

    /**
     * Build an instance of the node template from the given description.
     *
     * @param template The source template to build nodes from
     * @param root The execution space this node belongs in
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param staticNode Whether this node is will be modified
     * @return A grouping node representing the body of the active node
     */
    public VRMLNode newInstance(VRMLNodeTemplate template,
                                VRMLExecutionSpace root,
                                int major,
                                int minor,
                                boolean staticNode) {

        PrototypeDecl proto = (PrototypeDecl)template;

        rootSpace = root;
        List field_list = proto.getAllFields();

        VRMLFieldDeclaration[] field_decls =
            new VRMLFieldDeclaration[field_list.size()];
        field_list.toArray(field_decls);

        isVRML97 = proto.isVRML97();

        VRMLGroupingNodeType src_body = proto.getBodyGroup();

        protoInstance = new J3DProtoInstance(proto.getVRMLNodeName(),
                                             isVRML97,
                                             field_decls,
                                             src_body.getChildrenSize());

        protoInstance.setVersion(major, minor, staticNode);
        finishCreate(proto);

        protoInstance.setComplete();

        return protoInstance;
    }

    /**
     * Notification of a proto instance.
     *
     * @param parent The parent node of this node
     * @param proto The proto node that has been found
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    protected void protoNode(VRMLNodeType parent,
                             VRMLProtoInstance proto,
                             int field,
                             boolean used) {

        if(protoCreator == null) {
            protoCreator = new J3DProtoCreator(factory,
                                               worldURL,
                                               majorVersion,
                                               minorVersion);
            protoCreator.setFrameStateManager(stateManager);
            protoCreator.setErrorReporter(errorReporter);
        }

        VRMLNodeType j3d_proto;

        if(used) {
            j3d_proto = (VRMLNodeType)nodeMap.get(proto);
        } else {
            ProtoInstancePlaceHolder ph = (ProtoInstancePlaceHolder)proto;

            VRMLNodeTemplate adecl = ph.getProtoDefinition();
            PrototypeDecl decl = null;

            scene.addTemplate(adecl);

            if(adecl instanceof PrototypeDecl)
                decl = (PrototypeDecl)adecl;
            else {
                CRExternPrototypeDecl ex_proto = (CRExternPrototypeDecl)adecl;

                int ls = ex_proto.getLoadState();
                if (ls == VRMLExternalNodeType.LOAD_COMPLETE) {
                    decl = (PrototypeDecl)ex_proto.getProtoDetails();
                } else {
                    if(externProtoCreator == null)
                        externProtoCreator =
                            new J3DExternProtoCreator(worldURL);

                    j3d_proto = externProtoCreator.createInstance(ex_proto);
                    j3d_proto.setVersion(majorVersion, minorVersion, false);

                    // Need a parent node so that when it is loaded it can
                    // notify the parent of it's load state. However, if
                    // someone has defined a proto that uses an externproto
                    // as the root node, then this will give us a null parent
                    // reference here. Fix that by assigning it the
                    // protoInstance that we're filling in here.
                    VRMLNodeType j3d_parent = (VRMLNodeType)nodeMap.get(parent);

                    ex_proto.addInstance(j3d_parent, field, j3d_proto);

                    nodeMap.put(proto, j3d_proto);
                    currentObserver.observedNode(parent, j3d_proto, field, used);

                    return;
                }
            }

            j3d_proto = (VRMLNodeType)protoCreator.newInstance(decl,
                                                               rootSpace,
                                                               majorVersion,
                                                               minorVersion,
                                                               false);

            // I think it's not static. Say no for now....
            j3d_proto.setVersion(majorVersion, minorVersion, false);

            // Now, fetch the raw field values and set those:
            List fields = decl.getAllFields();
            Iterator itr = fields.iterator();

            while(itr.hasNext()) {
                try {
                    VRMLFieldDeclaration field_decl =
                        (VRMLFieldDeclaration)itr.next();

                    int access = field_decl.getAccessType();
                    int data_type = field_decl.getFieldType();

                    if((access == FieldConstants.EVENTIN) ||
                       (access == FieldConstants.EVENTOUT) ||
                       (data_type == FieldConstants.SFNODE) ||
                       (data_type == FieldConstants.MFNODE))
                       continue;

                    String field_name = field_decl.getName();
                    int field_index = ph.getFieldIndex(field_name);
                    VRMLFieldData field_value = ph.getFieldValue(field_index);

                    field_index = j3d_proto.getFieldIndex(field_name);

                    setProtoField(j3d_proto, field_index, field_decl.getFieldSize(), field_value);
                } catch(FieldException fe) {
                    // should never happen!
                    errorReporter.errorReport(
                        "Waaaa! Proto fields don't match!", fe);
                }
            }

            nodeMap.put(proto, j3d_proto);
        }

        currentObserver.observedNode(parent, j3d_proto, field, used);
    }
}
