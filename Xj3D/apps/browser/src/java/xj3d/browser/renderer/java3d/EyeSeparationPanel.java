/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003-2005
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser.renderer.java3d;

// Standard imports
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.media.j3d.PhysicalBody;

import javax.vecmath.Point3d;

import org.web3d.vrml.renderer.j3d.browser.VRMLUniverse;

// Application Specific imports
// None

/**
 * A panel that can be used to adjust the eye separation.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class EyeSeparationPanel extends JPanel
    implements ChangeListener {

	VRMLUniverse targetUniverse;
	
    /** Slider for the left eye */
    private JSlider leftEyeSlider;

    /** Slider for the right eye */
    private JSlider rightEyeSlider;

    /** Checkbox to lock the adjustments together */
    private JCheckBox lockSliders;

    /** The body used to control the eye */
    private PhysicalBody avatar;

    /** Position of the left eye */
    private Point3d leftPosition;

    /** Position of the left eye */
    private Point3d rightPosition;

    /**
     * Construct a new frame with the given options.
     *
     * @param title The window title to use
     * @param stereo True if this should force the use of stereo rendering
     * @param fullscreen True if to make the system fullscreen
     */
    public EyeSeparationPanel(PhysicalBody body, VRMLUniverse aUniverse) {

        super(new BorderLayout());

        targetUniverse=aUniverse;
        avatar = body;

        leftEyeSlider = new JSlider(0, 100, 5);
        leftEyeSlider.setInverted(true);
        leftEyeSlider.setSnapToTicks(false);
        leftEyeSlider.setPaintTicks(true);
        leftEyeSlider.setPaintLabels(true);
        leftEyeSlider.addChangeListener(this);

        rightEyeSlider = new JSlider(0, 100, 5);
        rightEyeSlider.setSnapToTicks(false);
        rightEyeSlider.setPaintTicks(true);
        rightEyeSlider.setPaintLabels(true);
        rightEyeSlider.addChangeListener(this);

        lockSliders = new JCheckBox("Lock adjustments", true);
        lockSliders.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel l1 = new JLabel("Left Eye", JLabel.CENTER);
        JLabel l2 = new JLabel("Right Eye", JLabel.CENTER);

        JPanel p1 = new JPanel(new GridLayout(2, 2));

        p1.add(l1);
        p1.add(l2);
        p1.add(leftEyeSlider);
        p1.add(rightEyeSlider);

        add(p1, BorderLayout.CENTER);
        add(lockSliders, BorderLayout.SOUTH);

        leftPosition = new Point3d();
        rightPosition = new Point3d();
        avatar.getLeftEyePosition(leftPosition);
        avatar.getRightEyePosition(rightPosition);

        int val = -(int)(leftPosition.x * 1000);
        leftEyeSlider.setValue(val);

        val = (int)(rightPosition.x * 1000);
        rightEyeSlider.setValue(val);
    }

    /**
     * A slider has been moved.
     *
     * @param evt The event that caused this method to be called.
     */
    public void stateChanged(ChangeEvent evt) {
    	
    	if (targetUniverse!=null)
    		targetUniverse.setPhysicalBody(avatar);
    	else
    		System.out.println("Your universe is null.");
    	
        Object src = evt.getSource();

        if(src == leftEyeSlider) {
            int val = leftEyeSlider.getValue();
            leftPosition.x = -val / 1000f;
            avatar.setLeftEyePosition(leftPosition);

            if(lockSliders.isSelected()) {
                rightPosition.x = -leftPosition.x;
                avatar.setRightEyePosition(rightPosition);
                rightEyeSlider.setValue(val);
            }
        } else {
            int val = rightEyeSlider.getValue();
            rightPosition.x = val / 1000f;
            avatar.setRightEyePosition(rightPosition);

            if(lockSliders.isSelected()) {
                leftPosition.x = -rightPosition.x;
                avatar.setRightEyePosition(rightPosition);
                leftEyeSlider.setValue(val);
            }
        }
    }
}
