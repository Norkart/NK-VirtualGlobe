/*
 * PostDrawListener.java
 *
 * Created on 11. mai 2007, 15:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer;

import javax.media.opengl.GL;

/**
 *
 * @author runaas
 */
public interface PostDrawListener {
    
    public void postDraw(GL gl);
}
