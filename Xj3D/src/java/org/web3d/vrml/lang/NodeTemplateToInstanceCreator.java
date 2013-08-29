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

package org.web3d.vrml.lang;

// External imports
// None

// Local imports
import org.web3d.util.ErrorReporter;

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
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface NodeTemplateToInstanceCreator {

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

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
                                boolean staticNode);

    /**
     * Given a stubbed instance, fill in the rest of the details.
     * This is used for extern protos who create a light instance for the
     * scenegraph and then after the EP is loaded they fill in the details.
     *
     * @param template The proto definition loaded from the EP
     * @param space The execution space this node belongs in
     * @param nodeInstance The instance to fill out
     */
    public void fillinInstance(VRMLNodeTemplate template,
                               VRMLNode nodeInstance,
                               VRMLExecutionSpace space);
}
