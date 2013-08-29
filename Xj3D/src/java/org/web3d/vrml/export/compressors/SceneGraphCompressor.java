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
package org.web3d.vrml.export.compressors;

// Standard library imports

import java.io.DataOutputStream;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.sav.ContentHandler;

/**
 * Compresses a node and all its children.  A marker interface for NodeCompressors
 * to denote they will handle all children of a node.
 *
 * An example might be an IFS compressor that compacts the Coordinate and Normal node
 * children of an IFS.  Or a StaticGroup which folds transforms.
 *
 * DEFed nodes must be preserved.
 *
 * @author Alan Hudson.
 * @version $Revision: 1.3 $
 */
public interface SceneGraphCompressor extends NodeCompressor {
}
