//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
/*
 * SingletonDialog.java
 *
 * Created on 19. februar 2007, 15:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.norkart.virtualglobe.util;

import java.awt.Component;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;


/**
 *
 * @author runaas
 */
public class SingletonDialog {
    private static JDialog dialog;
    
    /** Creates a new instance of SingletonDialog */
    public SingletonDialog() {
    }
    
    public static void openDialog(Component child, int x, int y) {
        if (dialog != null)
            dialog.dispose();
        dialog = new JDialog();
        dialog.getContentPane().add(child);
        dialog.pack();
        dialog.setLocation(x, y);
        dialog.addWindowListener(new WindowListener() {
            public void	windowDeactivated(WindowEvent e) {
                dialog.toFront();
            }
            public void	windowActivated(WindowEvent e) { }
            public void	windowClosed(WindowEvent e) {  }
            public void	windowClosing(WindowEvent e) {  }
            public void	windowDeiconified(WindowEvent e) {  }
            public void	windowIconified(WindowEvent e) {  }
            public void	windowOpened(WindowEvent e) {  }
        });
        dialog.setVisible(true);
    }
}
