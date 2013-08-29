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

package org.web3d.vrml.renderer.j3d.nodes.geom3d;

// External imports
import java.util.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.*;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseExtrusion;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DGeometryListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;

/**
 * Java3D implementation of an Extrusion
 *
 * @author Andrzej Kapolka
 * @author Rick Goldberg
 * @version $Revision: 1.9 $
 */
public class J3DExtrusion extends BaseExtrusion
    implements J3DGeometryNodeType {

    /** Message when we detect a solid of revolution */
    private static final String SOR_ERR =
        "Invalid Extrusion data; looks like a solid of revolution";

    /** The Java3D geometry implmentation */
    private GeometryArray impl;

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    private GeometryInfo gi;

    // data -> gi
    private Point3f[] coords;
    private int[] coordIndex;
    private int[] stripCounts;

    private Point3f[] spines;
    private Vector3f[] scales;
    private AxisAngle4f[] orientations;
    private Transform3D[] spineTransforms;
    private Point3f[] crossSectionPts;

    // will contain the per spine
    // transform composed with orientation
    private Matrix3f[] rotations;
    private Transform3D[] transforms;

    private boolean collinear=false;
    private boolean closed=false; // spines

    int numTris = 0;

    public static boolean hardDebug = false;

    /** Temp var to hold the capability bits until setupFinished called */
    private int[] capReqdBits;
    private int[] freqReqdBits;

    /**
     * Construct a default sphere instance
     */
    public J3DExtrusion() {
        listeners = new ArrayList();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DExtrusion(VRMLNodeType node) {
        super(node);

        listeners = new ArrayList();
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the j3d representation
     * only once per frame.
     */
    public void allEventsComplete() {
        buildImpl();
    }

    //-------------------------------------------------------------
    // Methods defined by J3DGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Returns a J3D Geometry node
     *
     * @return A Geometry node
     */
    public Geometry[] getGeometry() {
        Geometry[] geom = new Geometry[1];

        geom[0] = impl;
        return geom;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    /**
     * Add a listener for geometry changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addGeometryListener(J3DGeometryListener l) {
        if((l == null) || listeners.contains(l))
            return;

        listeners.add(l);
    }

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeGeometryListener(J3DGeometryListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
    }

    /**
     * fire a geometry changed event to the listeners.
     *
     * @param items The geometry items that have changed or null for all
     */
    protected void fireGeometryChanged(int[] items) {
        int size = listeners.size();
        J3DGeometryListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DGeometryListener)listeners.get(i);
                l.geometryChanged(items);
            } catch(Exception e) {
                System.out.println("Error sending geometry change message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //-------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, value);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, value);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, value, numValid);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }


    //----------------------------------------------------------
    // Methods defined by J3DVRMLNode
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {

        if(capBits != null && capBits.containsKey(QuadArray.class))
            capReqdBits = (int[])capBits.get(QuadArray.class);

        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null ||
           !freqBits.containsKey(QuadArray.class))
            return;

        freqReqdBits = (int[])freqBits.get(QuadArray.class);
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return impl;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        buildImpl();
    }

    private void buildImpl() {
        // endcaps may need special handling
        gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        // convert vrml data to intermediate form
        initSetup();
        // calculate per spine SCP transforms
        // results in transforms[] being filled withs SCP info
        // complete with scale, translation and orientation from
        // fields
        if(!calculateSCP())
            return;

        // transform the crossSections to coordinates
        createExtrusion();
        // indexify, including endcaps if needed
        // leaves coordIndex with index and stripCounts with counts
        // per facet.
        createIndices();
        if ( hardDebug ) {
            System.out.println("coords");
            for ( int i = 0; i < coords.length; i ++ )
                System.out.println(coords[i]);
            System.out.println("coordIndex");
            for ( int i = 0; i < coordIndex.length; i ++ )
                System.out.println(coordIndex[i]);
            System.out.println("stripCounts");
            for ( int i = 0; i < stripCounts.length; i ++ )
                System.out.println(stripCounts[i]);
        }
        gi.setCoordinates(coords);
        gi.setCoordinateIndices(coordIndex);
        gi.setStripCounts(stripCounts);
        //Stripifier st = new Stripifier();
        //st.stripify(gi);
        float ca = vfCreaseAngle;
        if ( ca < 0.0f ) {
            ca = 0.0f;
        }
        if ( ca > (float)Math.PI ) {
            ca -= (float)Math.PI;
        }
        NormalGenerator ng = new NormalGenerator(ca);
        ng.generateNormals(gi);
        impl = gi.getGeometryArray();

        impl.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        impl.setCapability(GeometryArray.ALLOW_COUNT_READ);
        impl.setCapability(GeometryArray.ALLOW_FORMAT_READ);
        if(capReqdBits != null) {
            for(int i = 0; i < capReqdBits.length; i++)
                impl.setCapability(capReqdBits[i]);
        }

        if(J3DGlobalStatus.haveFreqBitsAPI && freqReqdBits != null) {
            for(int i = 0; i < freqReqdBits.length; i++)
                impl.setCapabilityIsFrequent(freqReqdBits[i]);
        }

        capReqdBits = null;
        freqReqdBits = null;

        fireGeometryChanged( new int[] { 0 } );
    }

    private void initSetup() {
        // load the crossSectionPts data
        crossSectionPts = new Point3f[ vfCrossSection.length/2 ];
        if ( hardDebug ) System.out.println(crossSectionPts.length);
        for ( int i = 0; i < crossSectionPts.length; i++ ) {
            crossSectionPts[i] = new Point3f(vfCrossSection[i*2],0.0f,vfCrossSection[i*2+1]);
        }
        // load the scales
        // scales size may not match spine size, if so
        // use previously set scale
        scales = new Vector3f[ vfSpine.length/3 ];
        for ( int i = 0; i < scales.length; i++ ) {
            if ( i*2 < vfScale.length )
            {
                scales[i] = new Vector3f(vfScale[i*2],1.0f,vfScale[i*2+1]);
            }
            else
            {
                scales[i] = new Vector3f(vfScale[vfScale.length-2],1.0f,vfScale[vfScale.length-1]);
            }
        }
        // load the spines
        spines = new Point3f[ vfSpine.length/3 ];
        for ( int i = 0; i < spines.length; i++ ) {
            spines[i] = new Point3f(vfSpine[i*3],vfSpine[i*3+1],vfSpine[i*3+2]);
        }
        // load the per spine orientation modifiers
        orientations = new AxisAngle4f[ vfSpine.length/3 ];
        for ( int i = 0; i < orientations.length; i++ ) {
            if ( i*4 < vfOrientation.length )
            {
                orientations[i] = new AxisAngle4f(
                    vfOrientation[i*4],
                    vfOrientation[i*4+1],
                    vfOrientation[i*4+2],
                    vfOrientation[i*4+3]
                );
            }
            else
            {
                orientations[i] = new AxisAngle4f(
                    vfOrientation[vfOrientation.length-4],
                    vfOrientation[vfOrientation.length-3],
                    vfOrientation[vfOrientation.length-2],
                    vfOrientation[vfOrientation.length-1]
                );
            }
        }
        rotations = new Matrix3f[ vfSpine.length/3 ];
        // if the tail meets the head
        if (spines[0].equals(spines[spines.length - 1])) {
            closed = true;
        }
        // if entirely collinear
        Vector3d v2 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v0 = new Vector3d();
        double d=0.0;
        for ( int i = 1; i < spines.length - 1; i++ ) {
            v2.set(spines[i+1]);
            v1.set(spines[i]);
            v0.set(spines[i-1]);
            v2.sub(v1);
            v1.sub(v0);
            v0.cross(v2,v1);
            d += v0.dot(v0);
        }
        collinear=(d==0.0);
        if ( hardDebug && collinear )
            System.out.println("spine is straight");
    }

    private boolean calculateSCP() {
        // find an orthonormal basis and construct rotation matrix
        // for each spine. handle special cases in second pass
        Vector3f[] x,y,z;
        Vector3f u,v;
        Vector3f zero = new Vector3f(0.0f,0.0f,0.0f);
        int last = spines.length-1;
        x = new Vector3f[ spines.length ];
        y = new Vector3f[ spines.length ];
        z = new Vector3f[ spines.length ];
        if ( collinear ) {
            if (closed) {
                errorReporter.warningReport(SOR_ERR, null);
                StringBuffer buf = new StringBuffer("Spine data:");

                for(int i = 0; i < spines.length; i++) {
                   buf.append(spines[i]);
                   buf.append(' ');
                }

                errorReporter.messageReport(buf.toString());

                return false;
            }
            // Direction is the first spine point that does not equal to
            // spines[0]
            Vector3f direction = null;
            for (int i = 0; i < spines.length; i++) {
                if (!spines[0].equals(spines[i])) {
                    direction = new Vector3f(spines[i]);
                }
            }
            y[0] = new Vector3f();
            y[0].sub( direction, spines[0] );
            try {
                norm(y[0]);
            } catch ( ArithmeticException ae ) {
                ae.printStackTrace();
            }
            // Create an initial x[0]
            if (y[0].x == 1.0f) {
                x[0] = new Vector3f(0.0f,-1.0f,0.0f);
            }
            else if (y[0].x == -1.0f) {
                x[0] = new Vector3f(0.0f,1.0f,0.0f);
            } else {
                x[0] = new Vector3f(1.0f,0.0f,0.0f);
            }
            // Create z[0]
            z[0] = new Vector3f();
            z[0].cross(x[0],y[0]);
            // Create final x[0]
            x[0].cross(y[0],z[0]);
            for ( int i = 1; i < spines.length; i++ ) {
                // redo, this should take the direction of y
                // redone by Pasi Paasiala <<check this>>
                x[i] = new Vector3f(x[0]);
                y[i] = new Vector3f(y[0]);
                z[i] = new Vector3f(z[0]);
            }
        } else {
            // find y[i] for all but first and last
            // most times the exception cases are bad data and hopefully
            // wont happen. It is free to try catch you later, so hopes
            // 99% cases will be one if faster by not checking the if

            for ( int i = 1; i < last; i++ ) {
                y[i] = new Vector3f();
                y[i].sub( spines[i+1], spines[i-1] );
                try {
                    norm(y[i]);
                } catch ( ArithmeticException ae ) {
                    if ( hardDebug ) System.out.println(ae+" "+y[i]);
                    // spines[i+1] equals spines[i-1]
                    try {
                        y[i].sub( spines[i+1], spines[i] );
                        norm(y[i]);
                    } catch ( ArithmeticException ae1 ) {
                        if ( hardDebug ) System.out.println(ae1+" "+y[i]);
                        // spines[i+1] equaled spines[i]
                        try {
                            y[i].sub( spines[i], spines[i-1] );
                            norm(y[i]);
                        } catch ( ArithmeticException ae2 ) {
                            if ( hardDebug ) System.out.println(ae2+" "+y[i]);
                            // spines[i] equaled spines[i-1]
                            // real bad case, do something
                            int w=i+2;
                            while (( w < last+1 ) && (spines[i-1].equals(spines[w])))
                                w++;
                            if ( w < last+1 ) {
                                y[i].sub(spines[w],spines[i-1]);
                                if ( hardDebug ) System.out.println("did something "+y[i]);
                                norm(y[i]); // should never divide by zero here
                            } else { // worst worst case
                                if ( hardDebug ) System.out.println("worst worst y "+y[i]);
                                y[i] = new Vector3f(0.0f,1.0f,0.0f);
                            }
                        }
                    }
                }
            }

            // y for ends
            if ( closed ) {
                // closed and not collinear -> not all one point
                y[0] = new Vector3f();
                y[0].sub(spines[1],spines[last-1]);
                try {
                    norm(y[0]);
                } catch ( ArithmeticException ae ) {
                    // bad case that the spine[n-2] == spine[1]
                    int w=last-2;
                    while((w > 1) && (spines[1].equals(spines[w])))
                        w--;
                    if (w > 1) {
                        y[0].sub(spines[1],spines[w]);
                        norm(y[0]); // should never divide by zero here
                    } else
                        // how did this happen?
                        y[0].set(0.0f,0.0f,1.0f);
                }
                y[last] = new Vector3f(y[0]);
            } else {
                y[0] = new Vector3f();
                y[last] = new Vector3f();
                y[0].sub(spines[1],spines[0]);
                try {
                    norm(y[0]);
                } catch ( ArithmeticException ae ) {
                    int w=2;
                    while ((w < last) && (spines[0].equals(spines[w])))
                    w++;
                    if (w < last) {
                        y[0].sub(spines[w],spines[0]);
                        norm(y[0]); // should not divide by zero here
                    } else
                        y[0].set(0.0f,0.0f,1.0f);
                }

                y[last] = new Vector3f();
                y[last].sub(spines[last],spines[last-1]);
                try {
                    norm(y[last]);
                } catch ( ArithmeticException ae ) {
                    int w=last-2;
                    while ((w > -1) && (spines[last].equals(spines[w])))
                        w--;

                    if (w > -1)  {
                        y[last].sub(spines[last],spines[w]);
                        norm(y[last]);
                    } else
                        y[last].set(0.0f,0.0f,1.0f);
                }
            }
            // now z axis for each spine
            // first all except first and last
            boolean recheck = false;
            for ( int i = 1; i < last; i++ ) {
                u = new Vector3f();
                v = new Vector3f();
                z[i] = new Vector3f();
                u.sub(spines[i-1],spines[i]);
                v.sub(spines[i+1],spines[i]);
                // spec seems backwards on u and v
                // shouldn't it be z[i].cross(u,v)???
                //z[i].cross(v,u);
                //--> z[i].cross(u,v); is correct <<check this>>
                // Modified by Pasi Paasiala (Pasi.Paasiala@solibri.com)
                z[i].cross(u,v);
                try {
                    norm(z[i]);
                } catch ( ArithmeticException ae ) {
                    recheck=true;
                }
            }
            if ( closed ) {
                z[0] = z[last] = new Vector3f();
                u = new Vector3f();
                v = new Vector3f();
                u.sub(spines[last-1],spines[0]);
                v.sub(spines[1],spines[0]);
                try {
                    z[0].cross(u,v);
                } catch ( ArithmeticException ae ) {
                    recheck=true;
                }
            } else { // not closed
                z[0] = new Vector3f(z[1]);
                z[last] = new Vector3f(z[last-1]);
            }

            if ( recheck ) { // found adjacent collinear spines
                // first z has no length ?
                if ( hardDebug )
                System.out.println("rechecking, found adjacent collinear spines");
                if ( z[0].dot(z[0]) == 0.0f ) {
                    for ( int i = 1; i < spines.length; i++ ) {
                        if ( z[i].dot(z[i]) > 0.0f )
                            z[0] = new Vector3f(z[i]);
                    }
                    // test again could be most degenerate of cases
                    if ( z[0].dot(z[0]) == 0.0f )
                        z[0] = new Vector3f(0.0f,0.0f,1.0f);
                }
                // check rest of z's
                for ( int i = 1; i < last+1; i++ ) {
                    if ( z[i].dot(z[i]) == 0.0f )
                        z[i] = new Vector3f(z[i-1]);
                }
            }
            // finally, do a neighbor comparison
            // and evaluate the x's
            for ( int i = 0; i < spines.length; i++ ) {
                if ( i > 0 )
                if ( z[i].dot(z[i-1]) < 0.0f )
                    z[i].negate();

                // at this point, y and z should be nice
                x[i] = new Vector3f();
                //Original was: x[i].cross(z[i],y[i]); <<check this>>
                //but it doesn't result in right handed coordinates
                // Modified by Pasi Paasiala
                x[i].cross(y[i],z[i]);
                try {
                    norm(x[i]);
                } catch ( ArithmeticException ae ) {
                    // this should not happen
                    ae.printStackTrace();
                }

                if( hardDebug ) System.out.println("x["+i+"] "+x[i]);
            }
        }

        // should now have orthonormal vectors for each
        // spine. create the rotation matrix with scale for
        // each spine. spec is unclear whether a twist imparted
        // at one of the spines is inherited by its "children"
        // so assume not.
        // also, the order looks like SxTxRscpxRo , ie ,
        // the spec doc looks suspect, double check
        Matrix3f m = new Matrix3f();
        transforms = new Transform3D[spines.length];
        for ( int i = 0; i < spines.length; i++ ) {
            rotations[i] = new Matrix3f();
            if ( hardDebug ) {
                Vector3f xd = new Vector3f(spines[i]);
                xd.add(x[i]);
                Vector3f yd = new Vector3f(spines[i]);
                yd.add(y[i]);
                Vector3f zd = new Vector3f(spines[i]);
                zd.add(z[i]);
                System.out.println("\northos (ABS) "+
                        i+" "+xd+" "+yd+" "+zd+" "+orientations[i]);
                System.out.println("orthos "+
                        i+" "+x[i]+" "+y[i]+" "+z[i]+" "+orientations[i]);
            }
            // Original had setRow. This is correct <<check this>>
            // Modified by Pasi Paasiala
            rotations[i].setColumn(0,x[i]);
            rotations[i].setColumn(1,y[i]);
            rotations[i].setColumn(2,z[i]);
        }
        Matrix3f[] correctionRotations = createCorrectionRotations(z);
        // Create the transforms
        for ( int i = 0; i < spines.length; i++ ) {
            rotations[i].mul(correctionRotations[i]);
            m.set(orientations[i]);
            rotations[i].mul(m);
            transforms[i]=new Transform3D();
            transforms[i].setScale(new Vector3d(scales[i]));
            transforms[i].setTranslation(new Vector3d(spines[i]));
            transforms[i].setRotation(rotations[i]);
        }

        return false;
    }

    /**
     * Creates a rotation for each spine point to avoid twisting of the profile
     * when the orientation of SCP changes.
     * @author Pasi Paasiala
     * @param z the vector containing the z unit vectors for each spine point
     */
    private Matrix3f[] createCorrectionRotations(Vector3f[] z) {
        Matrix3f[] correctionRotations = new Matrix3f[spines.length];
        correctionRotations[0] = new Matrix3f();
        correctionRotations[0].setIdentity();
        AxisAngle4f checkAngle = new AxisAngle4f();
        // testPoint is used to find the angle that gives the smallest distance
        // between the previous and current rotation. Find a point that is not
        // in the origin.
        Point3f testPoint = crossSectionPts[0];
        for (int i = 0; i < crossSectionPts.length; i++) {
            if (crossSectionPts[i].x != 0 || crossSectionPts[i].z != 0) {
                testPoint = crossSectionPts[i];
                break;
            }
        }

        // Fix the orientations by using the angle between previous z and current z
        for (int i = 1; i < spines.length; i++) {
            float angle = z[i].angle(z[i-1]);
            correctionRotations[i] = correctionRotations[i-1];
            if (angle != 0) {
                correctionRotations[i] = new Matrix3f(correctionRotations[i-1]);
                Point3f previous = new Point3f();
                //Point3f previous = testPoint;
                // Test with negative angle:
                Matrix3f previousRotation = new Matrix3f(rotations[i-1]);
                previousRotation.mul(correctionRotations[i-1]);
                previousRotation.transform(testPoint, previous);
                Matrix3f delta = new Matrix3f();
                delta.setIdentity();
                delta.rotY(-angle);
                correctionRotations[i].mul(delta);
                Matrix3f negativeRotation = new Matrix3f(rotations[i]);
                negativeRotation.mul(correctionRotations[i]);
                Point3f pointNegative = new Point3f();
                negativeRotation.transform(testPoint,pointNegative);
                float distNegative = pointNegative.distance(previous);
                // Test with positive angle
                delta.rotY(angle*2);
                correctionRotations[i].mul(delta);
                Matrix3f positiveRotation = new Matrix3f(rotations[i]);
                positiveRotation.mul(correctionRotations[i]);
                Point3f pointPositive = new Point3f();
                positiveRotation.transform(pointPositive);
                float distPositive = pointPositive.distance(previous);
                if (distPositive > distNegative) {
                    // Reset correctionRotations to negative angle
                    delta.rotY(-angle*2);
                    correctionRotations[i].mul(delta);
                }
                if (hardDebug) {
                    System.out.println("i = " + i + " Angle is " +
                            (distPositive > distNegative ? "negative " : "positive ") +
                            "\n previous = " + previous +
                            "\npointNegative =" + pointNegative + "\npointPositive =" + pointPositive +
                            angle + " dist+ = " + distPositive + " dist- = " + distNegative + "\n");
                }
                // Check that the angle is not more than PI.
                // If it is subtract PI from angle
                checkAngle.set(correctionRotations[i]);
                if (((float)Math.PI - checkAngle.angle) < 0.001) {
                    correctionRotations[i].rotY((float)(checkAngle.angle - Math.PI));
                }
            }
        }
        return correctionRotations;
    }

    // create a list of unique coords ( of Point3f )
    // by applying the transforms to the crossSectionPts
    private void createExtrusion() {
        coords = new Point3f[ spines.length * crossSectionPts.length ];
        if (hardDebug) {
            System.out.println("Transformations");
            for (int i = 0; i < transforms.length; i++) {
                Matrix3d rotation = new Matrix3d();
                Vector3d location = new Vector3d();
                transforms[i].get(rotation, location);
                System.out.println(rotation.toString() + "\n" + location.toString() + "\n");
            }
        }
        for ( int i = 0; i < spines.length; i++ ) {
            for ( int j = 0; j < crossSectionPts.length; j++ ) {
                int ind = i*(crossSectionPts.length) + j;
                coords[ind] = new Point3f(crossSectionPts[j]);
                if (hardDebug) {
                    System.out.print("Transforming " + j +" "+ crossSectionPts[j] + " i =" + i);
                }
                transforms[i].transform(coords[ind]);
                if (hardDebug) {
                    System.out.println("Result = " + coords[ind]);
                }
            }
        }
    }

    // wind the coords with indexed connectivity and create
    // stripCounts see page 47 of small bluebook
    private void createIndices() {
        int m = 0; // coordIndex length
        int k = crossSectionPts.length;
        int l = coords.length;
        int s = 0;
        int n = 0; // coordIndex count

        if(vfEndCap) {
            m += k-1;
            s++;
        }

        if(vfBeginCap) {
            m += k-1;
            s++;
        }

        m += (spines.length-1)*(4*(k-1));
        coordIndex = new int[m];
        stripCounts = new int[s + (spines.length-1) * (k-1)];
        s = 0;

        // start with extrusion body from bottom
        if(vfCCW) {
            for(int i = 0; i < spines.length-1; i++) {
                for(int j = 0; j < k-1; j++) {
                    coordIndex[n++] = (i*k) + j;
                    coordIndex[n++] = (i*k) + j + 1;
                    coordIndex[n++] = ((i+1)*k) + j + 1;
                    coordIndex[n++] = ((i+1)*k) + j;
                    stripCounts[s++]=4;
                    numTris+=2;
                }
            }
        } else {
            for(int i = 0; i < spines.length-1; i++) {
                for(int j = 0; j < k-1; j++) {
                    coordIndex[n++] = (i*k) + j;
                    coordIndex[n++] = ((i+1)*k) + j;
                    coordIndex[n++] = ((i+1)*k) + j + 1;
                    coordIndex[n++] = (i*k) + j + 1;
                    stripCounts[s++]=4;
                    numTris+=2;
                }
            }
        }

        // add top and bottom
        // note: when switching cw from ccw notice that
        // the index is off by one, this is ok since there
        // is one extra point in the cross-section, each
        // cap has 2 ways to be drawn
        // also note top and bottom caps are reverse oriented to
        // each other
        if(vfBeginCap && vfEndCap) {
            int indB = m-(2*(k-1));
            int indE = m-(k-1);
            if(!vfCCW) {
                for(int i = 0; i<k-1; i++)
                    coordIndex[indB++] = i;
                for(int i = l-1; i>l-k; i--)
                    coordIndex[indE++] = i;
            } else {
                for(int i = k-1; i>0; i--)
                    coordIndex[indB++] = i;
                for(int i = 0; i<k-1; i++)
                    coordIndex[indE++] = l-(k-1)+i;
            }

            stripCounts[s++]=k-1;
            stripCounts[s++]=k-1;
            numTris += k-1; // best guess what gi did?
        } else if(vfBeginCap) {
            int ind = m-(k-1);
            if(!vfCCW) {
                for ( int i = 0; i < k-1; i++)
                    coordIndex[ind++] = i;
            } else {
                // this is ok since extra x-sectpt give off by one
                for(int i = k-1; i > 0; i--)
                    coordIndex[ind++] = i;
            }
            stripCounts[s++]=k-1;
            numTris += k-1;
        } else if(vfEndCap) {
            int ind = m-(k-1);
            if( vfCCW) {
                for(int i = l-(k-1); i<l; i++)
                    coordIndex[ind++] = i;
            } else {
                for(int i = l-1; i>l-k; i--)
                    coordIndex[ind++] = i;
            }

            stripCounts[s++]=k-1;
            numTris += k-1;
        }
    }

    // the vecmath package was not throwing ArithmeticExceptions as
    // expected from the normalize() method.
    private void norm(Vector3f n) {
        float norml = (float)Math.sqrt(n.x*n.x + n.y*n.y + n.z*n.z);
        if ( norml == 0.0f ) throw new ArithmeticException();
        n.x /= norml;
        n.y /= norml;
        n.z /= norml;
    }
}
