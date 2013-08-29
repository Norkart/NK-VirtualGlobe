/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.web3d.x3d.sai.*;

/**
 * Test that illustrates bug ID 245 - replaceWorld(null) causing an exception.
 * The button is pressed to trigger the replace.
 */

public class ClearWorldTest implements ActionListener {

    private ExternalBrowser browser;

    public ClearWorldTest() {
        browser = SAITestFactory.getBrowser();
        X3DScene s = browser.createX3DFromString(
                "#VRML V3.0 utf8\n"
                + "PROFILE Interactive\n"
                +"Transform { translation -3 0 0 children [Shape { geometry Sphere {}} ]}\n"
        );
        browser.replaceWorld(s);
        JFrame f=new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton doIt=new JButton("Clear World");
        f.getContentPane().add(doIt);
        doIt.addActionListener(this);
        f.pack();
        f.show();
    }

    public static void main(String[] args) {
        ClearWorldTest tester = new ClearWorldTest();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            browser.replaceWorld(null);
        } catch (Exception exception) {
            System.out.println("Test failed");
            exception.printStackTrace();
        }
    }
}
