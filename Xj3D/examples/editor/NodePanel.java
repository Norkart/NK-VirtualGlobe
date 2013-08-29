/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.web3d.x3d.sai.profileName;

// Local imports
import org.xj3d.ui.awt.widgets.IconLoader;

/**
 * A panel that holds the list of nodes available for a given profile.
 * <p>
 * The panel presents a series of tabs where each tab contains the nodes
 * of the given profile. At the moment the contents of each tab is hard
 * coded with the nodes. Ideally in the end it would load these from the
 * appropriate DTD information.
 */
class NodePanel extends JPanel implements ActionListener
{
    /** Node types that we need, but aren't part of the profiles */
    private static final String[] OTHER_NODES = {
        "ExternProtoDeclare","field","Header","meta","ProtoDeclare",
        "ProtoInstance","Prototype","Scene","ROUTE"
    };

    /** The listing of all the core nodes */
    private static final String[] CORE_NODES = {
        "Anchor","Appearance","Background","ColorInterpolator","Color",
        "Coordinate","DirectionalLight","Group","ImageTexture","Inline",
        "KeySensor","LOD","Material","NavigationInfo",
        "OrientationInterpolator","PointSet","PositionInterpolator","Shape",
        "TextureCoordinate","TextureTransform","TimeSensor","Transform",
        "Viewpoint","WorldInfo", "X3D"
    };

    /** The list of all the baseline profile nodes */
    private static final String[] BASE_NODES = {
        "AudioClip","Billboard","Box","Collision","Cone",
        "CoordinateInterpolator","Cylinder","CylinderSensor","ElevationGrid",
        "Extrusion","Fog","FontStyle","IndexedFaceSet","IndexedLineSet",
        "MovieTexture","Normal","NormalInterpolator","PixelTexture",
        "PlaneSensor","PointLight","ProximitySensor","proxy",
        "ScalarInterpolator","Script","Sound","Sphere","SphereSensor",
        "SpotLight","StringSensor","Switch","Text","TouchSensor",
        "VisibilitySensor"
    };

    /** List of GeoVrml nodes */
    private static final String[] GEO_NODES = {
    };

    /** List of H-anim nodes */
    private static final String[] HANIM_NODES = {
    };

    /** List of NURBS profile nodes */
    private static final String[] NURBS_NODES = {
    };

    /** List of Lattice profile nodes */
    private static final String[] LATTICE_NODES = {
    };

    /** List of DIS-JAVA-VRML profile nodes */
    private static final String[] DJV_NODES = {
    };

    /** The registered listener for node creation requests */
    private NodeCreationListener listener;

    /**
     * Create a new instance of the node panel
     */
    NodePanel()
    {
        JTabbedPane tab_pane = new JTabbedPane();
        tab_pane.setTabPlacement(JTabbedPane.TOP);
        JPanel panel = createNodePanel(OTHER_NODES);
        tab_pane.addTab("Misc", panel);

        panel = createNodePanel(BASE_NODES);
        tab_pane.addTab(profileName.BaseLine.toString(), panel);

        panel = createNodePanel(CORE_NODES);
        tab_pane.addTab(profileName.Core.toString(), panel);

        panel = createNodePanel(GEO_NODES);
        tab_pane.addTab(profileName.GeoVrml.toString(), panel);

        panel = createNodePanel(HANIM_NODES);
        tab_pane.addTab(profileName.HumanoidAnimation.toString(), panel);

        panel = createNodePanel(NURBS_NODES);
        tab_pane.addTab(profileName.Nurbs.toString(), panel);

        panel = createNodePanel(LATTICE_NODES);
        tab_pane.addTab(profileName.Lattice.toString(), panel);

        panel = createNodePanel(DJV_NODES);
        tab_pane.addTab(profileName.DisJavaVrml.toString(), panel);

        add(tab_pane, BorderLayout.CENTER);
    }

    /**
     * Create the panel representing the core nodes.
     *
     * @param nodeList A string list of the node names to use
     * @return A panel containing the right buttons
     */
    private JPanel createNodePanel(String[] nodeList)
    {
        JPanel ret_val = new JPanel(new BorderLayout());

        if(nodeList.length == 0)
        {
            JLabel label = new JLabel("None Defined", SwingConstants.CENTER);
            ret_val.add(label, BorderLayout.CENTER);
        }
        else
        {
            JPanel panel = new JPanel(new GridLayout(2, 0));
            Insets border = new Insets(0, 0, 0, 0);
            JButton button;
            String name;

            for(int i = 0; i < nodeList.length; i++)
            {
                name = nodeList[i];
                Icon icon = IconLoader.loadIcon(name);

                if(icon == null) {
                    System.out.println("No icon for: " + name);
                    continue;
                }

                button = new JButton(icon);
                button.setMargin(border);
                button.setToolTipText(name);
                button.setActionCommand(name);
                button.addActionListener(this);
                panel.add(button);
            }

            ret_val.add(panel, BorderLayout.CENTER);
        }

        return ret_val;
    }

    /**
     * Set the node creation listener to the given instance. Any existing
     * listener is removed and replaced with this. Use the value of null to
     * clear the existing listener.
     *
     * @param l The new listener to add
     */
    void setNodeCreationListener(NodeCreationListener l)
    {
        listener = l;
    }

    /**
     * Process a button press on this item.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        if(listener == null)
            return;

        String cmd = evt.getActionCommand();
        try
        {
            listener.createNode(cmd);
        }
        catch(Exception e)
        {
            System.err.println("Error when requesting node creation: " + e);
            e.printStackTrace();
        }
    }
}
