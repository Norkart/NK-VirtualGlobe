//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * PickListener.java
 *
 * Created on 19. februar 2007, 15:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.viewer.av3d;

import java.awt.event.MouseEvent;

/**
 *
 * @author runaas
 */
public interface PickListener {
    public void picked(MouseEvent e);
}
