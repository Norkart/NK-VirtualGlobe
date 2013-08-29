/*****************************************************************************
 *                        J3D.org Copyright (c) 2005 - 2006
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.util;

// External imports
import java.util.Iterator;
import java.util.List;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.rendering.CustomRenderable;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.HashSet;

/**
 * Utility class that traverses an Aviatrix3D scene graph, reporting
 * values to the end user through the use of the
 * {@link SceneGraphTraversalObserver}.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class SceneGraphTraverser
{
    /** Flag to describe if we are currently in a traversal */
    private boolean inUse;

    /** Temporary map during traversal for use references */
    private HashSet nodeRefs;

    /** The detailObs for the scene graph */
    private SceneGraphTraversalObserver observer;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /**
     * Create a new traverser ready to go.
     */
    public SceneGraphTraverser()
    {
        inUse = false;
        nodeRefs = new HashSet();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Set the detailObs to be used. If an detailObs is already set, it is
     * replaced by the new one. A value of null will clear the current
     * detailObs.
     *
     * @param obs The new detailObs reference to use
     */
    public void setObserver(SceneGraphTraversalObserver obs)
    {
        observer = obs;
    }

    /**
     * Traverse the given scene graph now. If the call is currently in progress
     * then this will issue an exception. Any node can be used as the root
     * node. If no detailObs is set, this method returns immediately. This method
     * is equivalent to calling <code>traverseGraph(null, source);</code>.
     *
     * @param source The root of the scene graph to traverse
     * @throws IllegalStateException Attempt to call this method while it is
     *     currently traversing a scene graph
     */
    public void traverseGraph(SceneGraphObject source)
        throws IllegalStateException
    {
        traverseGraph(null, source);
    }

    /**
     * Traverse the given scene graph now with the option of providing an
     * explicit, parent reference. If the call is currently in progress
     * then this will issue an exception. Any node can be used as the root
     * node. If no observer is set or the source is null, this method returns
     * immediately.
     * <p>
     * A explicit root may be provided for various reasons. The most common
     * would be for loading externprotos where the root of the traversed graph
     * is actually going to be in a separate file and scene graph structure
     * from where we are starting this traversal from.
     *
     * @param source The root of the scene graph to traverse
     * @throws IllegalStateException Attempt to call this method while it is
     *     currently traversing a scene graph
     */
    public void traverseGraph(SceneGraphObject parent, SceneGraphObject source)
        throws IllegalStateException
    {
        if(inUse)
            throw new IllegalStateException("Currently traversing");

        if((observer == null) || (source == null))
            return;

        inUse = true;

        try
        {
            processSimpleNode(parent, source, 0);
        }
        finally
        {
            // this is for error recovery
            inUse = false;
        }
    }

    /**
     * Clear the use map.  This will not be cleared between traversal calls
     */
    public void reset()
    {
        nodeRefs.clear();
    }

    /**
     * Internal convenience method that separates the startup traversal code
     * from the recursive mechanism using the detailed detailObs.
     *
     * @param parent The root of the current item to traverse
     * @param depth The scenegraph depth
     */
    private void recurseSceneGraphChild(SceneGraphObject parent, int depth)
    {
        if(parent instanceof Group)
        {
            int currDepth = depth+1;

            Group g = (Group)parent;
            int num_kids = g.numChildren();

            for(int i = 0; i < num_kids; i++)
                processSimpleNode(parent, g.getChild(i), currDepth);
        }
        else if(parent instanceof Layer)
        {
        }
        else if(parent instanceof SimpleScene)
        {
            SimpleScene sc = (SimpleScene)parent;
            processSimpleNode(parent, sc.getRenderedGeometry(), depth+1);
        }
        else if(parent instanceof CustomRenderable)
        {
            // What to do here? Maybe need an extra callback interface on
            // the traversal observer that will pass back the real children
            // list to process.
        } else if (parent instanceof SharedNode) {
            SharedNode snode = (SharedNode) parent;

            processSimpleNode(parent, snode.getChild(), depth+1);
        }
    }

    /**
     * Process a single simple node with its callback
     *
     * @param parent The parent node that was just processed
     * @param kid The child node that is about to be processed
     * @param depth The scenegraph depth
     */
    private void processSimpleNode(SceneGraphObject parent,
                                   SceneGraphObject kid,
                                   int depth)
    {
        boolean copy = nodeRefs.contains(kid);
        if(!copy)
            nodeRefs.add(kid);

        try
        {
            observer.observedNode(parent, kid, copy, depth);
        }
        catch(Exception e)
        {
            errorReporter.warningReport("Traversal error ", e);
        }

        // now recurse
        recurseSceneGraphChild(kid,depth++);
    }
}
