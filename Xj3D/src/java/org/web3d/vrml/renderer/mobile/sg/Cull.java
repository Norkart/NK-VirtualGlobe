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

package org.web3d.vrml.renderer.mobile.sg;

// Standard imports
import javax.vecmath.Matrix4f;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLEnum;

import gl4java.drawable.GLDrawable;

public class Cull {
    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 200;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 100;

    Node renderList[];
    int renderOp[];
    int lastRender;

    public Cull() {
        renderList = new Node[LIST_START_SIZE];
        renderOp = new int[LIST_START_SIZE];
        lastRender = 0;
    }

    public void cull(Node node) {
        lastRender=0;
        updateTraverse(node, null);
        cullTraverse(node);
    }

    /**
     * Traverse the SG and update all transform and bounding boxes.
     * Currently walks the whole tree.  Should be a better way to
     * optimize this.
     */
    private void updateTraverse(Node node, Matrix4f trans) {
        int len;
        Node kids[];

        node.updateTransform(trans);
        if (node instanceof Group) {
            trans = node.getTransform();
            kids = ((Group)node).getAllChildren();
            len = ((Group)node).numChildren();
            for(int i=0; i < len; i++) {
                if (kids[i] == null) continue;
                updateTraverse(kids[i], trans);
            }
        }
        node.updateBounds();
    }

    /**
     * Traverse the SG to produce a list of nodes to render.
     * Should cull nodes not in view.  Currently does no culling.
     */
    private void cullTraverse(Node node) {
        int len;
        Node kids[];

        resizeList();
        renderList[lastRender] = node;
        renderOp[lastRender++] = Draw.RENDER;

        // Convert to getPrimaryType()
        if (node instanceof Group) {
            kids = ((Group)node).getAllChildren();
            len = ((Group)node).numChildren();
            for(int i=0; i < len; i++) {
                if (kids[i] == null) continue;
                cullTraverse(kids[i]);
            }
        }

        renderList[lastRender] = node;
        renderOp[lastRender++] = Draw.POSTRENDER;
    }

    public Node[] getRenderList() {
        return renderList;
    }

    public int[] getRenderOp() {
        return renderOp;
    }

    public int getRenderListSize() {
        return lastRender;
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution.
     *
     * Resize by 2 since we will always job by that amount
     */
    private final void resizeList() {

        if((lastRender + 2) == renderList.length) {
            int old_size = renderList.length;
            int new_size = old_size + LIST_INCREMENT;

            Node[] tmp_nodes = new Node[new_size];
            int[] tmp_ops = new int[new_size];

            System.arraycopy(renderList, 0, tmp_nodes, 0, old_size);
            System.arraycopy(renderOp, 0, tmp_ops, 0, old_size);

            renderList = tmp_nodes;
            renderOp = tmp_ops;
        }
    }
}