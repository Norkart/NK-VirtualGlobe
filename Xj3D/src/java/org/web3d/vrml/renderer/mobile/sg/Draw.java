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
//none

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLEnum;

import gl4java.drawable.GLDrawable;

public class Draw {
    public static final int RENDER = 0;
    public static final int POSTRENDER = 1;
    private GLDrawable gld;

    public Draw(GLDrawable gldrawable) {
        gld = gldrawable;
    }

    public void draw(Node renderList[], int renderOp[], int size) {
        for(int i=0; i < size; i++) {
            if (renderOp[i] == RENDER) {
                renderList[i].render(gld);
            } else {
                renderList[i].postRender(gld);
            }
        }
    }
}