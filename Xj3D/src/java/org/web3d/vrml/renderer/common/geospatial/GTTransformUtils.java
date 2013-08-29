/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.geospatial;

// External imports
import org.opengis.referencing.cs.*;
import org.opengis.referencing.datum.*;

import java.util.HashMap;

import javax.units.SI;
import javax.units.NonSI;
import javax.units.Unit;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;

import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;


// Local imports
import org.web3d.util.HashSet;

/**
 * Utility class to create a GeoTools2 transformation class for a given
 * geoSystem field definition.
 * <p>
 *
 * This class runs as a singleton to allow for caching of internal fetched
 * transformation routines.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class GTTransformUtils {

    /** Set of all valid ellipsoid names in the geoSystem field */
    private static final HashSet allEllipsoidSet;

    /** A unit expressed as degrees */
    private static final Unit DEGREE;

    /** The shared singleton instance */
    private static GTTransformUtils instance;

    /** Factory for producing transformations between coordinate systems */
    private CoordinateOperationFactory transformFactory;

    /** Factory for creating referebce coordinate system setups */
    private CRSFactory crsFactory;

    /** Factory for creating coordinate systems on the way to a full CRS */
    private CSFactory csFactory;

    /** Factory for creating new Datums */
    private DatumFactory datumFactory;

    /** Factory for creating 3D UTM transformations */
    private MathTransformFactory mathFactory;

    /** Geotools helper for creating UTM transforms */
    private FactoryGroup factoryHelper;

    /** We always want to generate this coordinate system as output */
    private CoordinateReferenceSystem outputCRS;

    /** CRS used for the height to make a compound CRS for UTM */
    private CoordinateReferenceSystem heightOnlyCRS;

    /** CRS used for the stock Geodetic values defined by X3D */
    private CoordinateReferenceSystem geodeticCRS;

    /** Axis pointing north for geographic CS */
    private CoordinateSystemAxis northAxis;

    /** Axis pointing east for geographic CS */
    private CoordinateSystemAxis eastAxis;

    /** Axis indicating height for the geographic CS */
    private CoordinateSystemAxis heightAxis;

    /** Datum for WGS84 geoid (when we work out how to support it) */
    private VerticalDatum amslVertical;

    /** WGS84 ellipsoid heights */
    private VerticalDatum defaultVertical;

    /** Meridian representing Greenwich */
    private PrimeMeridian greenwichMeridian;

    /** Ellipsoid representing the WGS84 setup */
    private Ellipsoid wgs84Ellipsoid;

    /** Height datum representing the WGS84 ellipsoid */
    private GeodeticDatum wgs84EllipsoidHD;

    /** 2D generic cartesian CS for the UTM projections */
    private CartesianCS utmCartesianCS;

    /** WGS84 geographic CS for doing UTM projections with */
    private GeographicCRS utmGeographicCRS;

    /**
     * A map to use as a temporary. Place the systemDef string array here.
     * Makes it much easier to look up a specific flag for a coordinate system
     * because X3D does not guarantee a specific declaration order.
     */
    private HashSet csDefs;

    /**
     * Cached set of GeoTools ellipsoids. Maps the constant string from
     * table 25.3 to the Geotools Ellipsoid class of the appropriate type.
     * Filled in only as needed.
     */
    private HashMap cachedEllipsoidMap;

    /** Mapping of UTM zone strings to it's projected coordinate system. */
    private HashMap utmZoneMap;

    /**
     * Static constructor for creating the common data sets
     */
    static {
        allEllipsoidSet = new HashSet();
        allEllipsoidSet.add("AA");
        allEllipsoidSet.add("AM");
        allEllipsoidSet.add("AN");
        allEllipsoidSet.add("BN");
        allEllipsoidSet.add("BR");
        allEllipsoidSet.add("CC");
        allEllipsoidSet.add("CD");
        allEllipsoidSet.add("EA");
        allEllipsoidSet.add("EB");
        allEllipsoidSet.add("EC");
        allEllipsoidSet.add("ED");
        allEllipsoidSet.add("EE");
        allEllipsoidSet.add("FA");
        allEllipsoidSet.add("HE");
        allEllipsoidSet.add("HO");
        allEllipsoidSet.add("ID");
        allEllipsoidSet.add("IN");
        allEllipsoidSet.add("KA");
        allEllipsoidSet.add("RF");
        allEllipsoidSet.add("SA");
        allEllipsoidSet.add("WD");
        allEllipsoidSet.add("WE");

        DEGREE = SI.RADIAN.multiply(Math.PI / 180);
    }

    /**
     * Create a default instance of this class.
     */
    private GTTransformUtils() throws FactoryException {
        HashMap map;

        Hints hints = new Hints(null);
        transformFactory = FactoryFinder.getCoordinateOperationFactory(hints);
        datumFactory = FactoryFinder.getDatumFactory(hints);
        crsFactory = FactoryFinder.getCRSFactory(hints);
        mathFactory = FactoryFinder.getMathTransformFactory(hints);

        factoryHelper =
            new FactoryGroup(datumFactory, csFactory, crsFactory, mathFactory);

        map = new HashMap();
        map.put("name", "Greenwich Meridian");
        greenwichMeridian =
            datumFactory.createPrimeMeridian(map, 0, DEGREE);

        map = new HashMap();
        map.put("name", "WGS 84");
        wgs84Ellipsoid = datumFactory.createFlattenedSphere(map,
                                                            6378137,
                                                            298.257223563,
                                                            SI.METER);
        csDefs = new HashSet();
        utmZoneMap = new HashMap();

        cachedEllipsoidMap = new HashMap();
        // pre-populate with the default WE ellipsoid
        cachedEllipsoidMap.put("WE", wgs84Ellipsoid);

        // The default, output coordinate system for use in a right-handed
        // 3D cartesian coordinate system
        csFactory = FactoryFinder.getCSFactory(hints);

        map = new HashMap();
        map.put("name", "Geocentric X");
        CoordinateSystemAxis x_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "X",
                                                 AxisDirection.GEOCENTRIC_X,
                                                 SI.METER);
        map = new HashMap();
        map.put("name", "Geocentric Y");
        CoordinateSystemAxis y_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "Y",
                                                 AxisDirection.GEOCENTRIC_Y,
                                                 SI.METER);
        map = new HashMap();
        map.put("name", "Geocentric Z");
        CoordinateSystemAxis z_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "Z",
                                                 AxisDirection.GEOCENTRIC_Z,
                                                 SI.METER);

        map = new HashMap();
        map.put("name", "X");
        CoordinateSystemAxis geo_x_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "X",
                                                 AxisDirection.OTHER,
                                                 SI.METER);
        map = new HashMap();
        map.put("name", "Y");

        CoordinateSystemAxis geo_y_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "Y",
                                                 AxisDirection.EAST,
                                                 SI.METER);
        CoordinateSystemAxis geo_neg_y_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "Y",
                                                 AxisDirection.WEST,
                                                 SI.METER);
        map = new HashMap();
        map.put("name", "Z");
        CoordinateSystemAxis geo_z_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "Z",
                                                 AxisDirection.NORTH,
                                                 SI.METER);



        map = new HashMap();
        map.put("name", "Geodetic Lattitude");
        northAxis = csFactory.createCoordinateSystemAxis(map,
                                                         "N",
                                                         AxisDirection.NORTH,
                                                         NonSI.DEGREE_ANGLE);

        map = new HashMap();
        map.put("name", "Geodetic Longitude");
        eastAxis = csFactory.createCoordinateSystemAxis(map,
                                                        "E",
                                                        AxisDirection.EAST,
                                                        NonSI.DEGREE_ANGLE);

        map = new HashMap();
        map.put("name", "Ellipsoidal height");
        heightAxis = csFactory.createCoordinateSystemAxis(map,
                                                          "Up",
                                                          AxisDirection.UP,
                                                          SI.METER);

        map = new HashMap();
        map.put("name", "Northing");
        CoordinateSystemAxis n_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "N",
                                                 AxisDirection.NORTH,
                                                 SI.METER);

        map = new HashMap();
        map.put("name", "Easting");
        CoordinateSystemAxis e_axis =
            csFactory.createCoordinateSystemAxis(map,
                                                 "E",
                                                 AxisDirection.EAST,
                                                 SI.METER);

        // Put together the Cartesian coordinate system that the rendered
        // points go to.

        map = new HashMap();
        map.put("name", "Rendered Cartesian CS");
        CartesianCS world_cs = csFactory.createCartesianCS(map,
                                                           geo_x_axis,
                                                           geo_z_axis,
                                                           geo_neg_y_axis);



        map = new HashMap();
        map.put("name", "WGS84 Height Datum");
        wgs84EllipsoidHD = datumFactory.createGeodeticDatum(map,
                                                            wgs84Ellipsoid,
                                                            greenwichMeridian);

        map = new HashMap();
        map.put("name", "Output Cartesian CS");
        outputCRS =
            crsFactory.createGeocentricCRS(map,
                                           wgs84EllipsoidHD,
                                           world_cs);


        map = new HashMap();
        map.put("name", "UTM Cartesian CS");
        utmCartesianCS = csFactory.createCartesianCS(map,
                                                     e_axis,
                                                     n_axis,
                                                     heightAxis);

        map = new HashMap();
        map.put("name", "Geodetic reference cartesian CS");
        CartesianCS geo_cs = csFactory.createCartesianCS(map,
                                                         x_axis,
                                                         y_axis,
                                                         z_axis);


        map = new HashMap();
        map.put("name", "X3D Geodetic CRS");
        geodeticCRS =
            crsFactory.createGeocentricCRS(map,
                                           wgs84EllipsoidHD,
                                           geo_cs);

//        map = new HashMap();
//        map.put("name", "WGS84 Geoid height");
//        amslVertical =
//            datumFactory.createVerticalCoordinateSystem(map,
//                                                        VerticalDatumType.GEOID);

        map = new HashMap();
        map.put("name", "WGS84 Ellispoidal height");
        defaultVertical =
            datumFactory.createVerticalDatum(map,
                                             VerticalDatumType.ELLIPSOIDAL);


        map = new HashMap();
        map.put("name", "UTM geodetic");
        EllipsoidalCS utm_cs = csFactory.createEllipsoidalCS(map,
                                                             eastAxis,
                                                             northAxis,
                                                             heightAxis);


        map = new HashMap();
        map.put("name", "3D geographic CRS");
        utmGeographicCRS =
            crsFactory.createGeographicCRS(map,
                                           wgs84EllipsoidHD,
                                           utm_cs);

        map = new HashMap();
        map.put("name", "UTM Height CS");
        VerticalCS utm_h_cs = csFactory.createVerticalCS(map, heightAxis);

        map = new HashMap();
        map.put("name", "UTM WGS84 Height CRS");
        heightOnlyCRS =
            crsFactory.createVerticalCRS(map,
                                         defaultVertical,
                                         utm_h_cs);
    }

    /**
     * Fetch the singleton instance of the factory. If needed, a new instance
     * will be created.
     *
     * @return The shared singleton instance of this class
     * @throws FactoryException There was a problem creating the class
     */
    public static GTTransformUtils getInstance() throws FactoryException {
        if(instance == null)
            instance = new GTTransformUtils();

        return instance;
    }

    /**
     * From the given system definition strings, generate a coordinate
     * transformation service.
     * <p>
     * Check whether we need to reverse the two primary coordinate systems. If
     * true, then the user needs to swap the coordinate values for X and Y.
     *
     * @param systemDef The X3D system definition string
     * @param coordSwap an array of length 1 for returning the primary
     *    coordinate swap flag
     * @return A transform for taking geo coordinates to cartesian
     */
    public synchronized MathTransform createSystemTransform(String[] systemDef,
                                               boolean[] coordSwap)
        throws FactoryException {

        for(int i = 0; i < systemDef.length; i++)
            csDefs.add(systemDef[i]);

        CoordinateOperation op = createCoordSystem(systemDef, coordSwap);

        csDefs.clear();

        return op.getMathTransform();
    }

    /**
     * Create the coordinate system corresponding to the provided set of
     * X3D definition strings.
     *
     * @param systemDef The X3D system definition string
     * @return The CRS defined by the system strings
     */
    private CoordinateOperation createCoordSystem(String[] systemDef,
                                                        boolean[] coordSwap)
        throws FactoryException {

        CoordinateOperation ret_val = null;

        // first index is always the basic X3D type. Will be one of
        // "GD", ("GDC"), "GC", ("GCC"), or "UTM"
        if(csDefs.contains("GD") || csDefs.contains("GDC")) {
            coordSwap[0] = false;

            Ellipsoid ell = fetchEllipsoid(systemDef);

            EllipsoidalCS e_crs;
            HashMap map = new HashMap();
            // do we need to swap the axis definitions? By default the
            // order is (lat, long), this swaps it to (long, lat)
            if(csDefs.contains("longitude_first")) {
                map.put("name", "<long>,<lat> geodetic");
                e_crs = csFactory.createEllipsoidalCS(map,
                                                      eastAxis,
                                                      northAxis,
                                                      heightAxis);

            } else {
                map.put("name", "<long>,<lat> geodetic");
                e_crs = csFactory.createEllipsoidalCS(map,
                                                      northAxis,
                                                      eastAxis,
                                                      heightAxis);

            }

            // Are we using a WGS84 horizontal datum or something else?
            if(csDefs.contains("WGS84")) {
System.out.println("Geospatial cannont handle WGS84 geoid heights yet");
// This should end up replacing the default wgs84EllipsoidHD as the geodetic
// datum passed to create the CRS.
            }

            map = new HashMap();
            map.put("name","Map for geoSystem");

            CoordinateReferenceSystem crs =
                crsFactory.createGeographicCRS(map,
                                               wgs84EllipsoidHD,
                                               e_crs);

            ret_val = transformFactory.createOperation(crs,
                                                       outputCRS);

        } else if(csDefs.contains("GC") || csDefs.contains("GCC")) {
            // Since the output is already a geocentric system, then just
            // make the output CS the same as the input CS. Effectively this
            // should be an identity operation.
            ret_val = transformFactory.createOperation(geodeticCRS,
                                                       outputCRS);
            coordSwap[0] = false;
        } else if(csDefs.contains("UTM")) {
            String zone_id = null;
            String zone_str = null;
            boolean south = false;

            for(int i = 1; i < systemDef.length; i++) {
                if(systemDef[i].charAt(0) == 'Z') {
                    zone_str = systemDef[i];
                    zone_id = systemDef[i].substring(1);
                } else if(systemDef[i].charAt(0) == 'S')
                    south = true;
            }

            if(zone_id == null)
                throw new IllegalArgumentException("No UTM zone specified");

            if(csDefs.contains("easting_first"))
                coordSwap[0] = false;
            else
                coordSwap[0] = true;

            // Do we have this zone already created? If so, don't create a
            // new one.
            ret_val = (CoordinateOperation)utmZoneMap.get(zone_str);

            if(ret_val != null)
                return ret_val;

            // convert the zone ID to an integer
            int zone_num = 0;
            try {
                zone_num = Integer.parseInt(zone_id);
            } catch(NumberFormatException nfe) {
                throw new IllegalArgumentException("UTM zone number not an integer");
            }

            if(zone_num < 0 || zone_num > 60)
                throw new IllegalArgumentException("UTM zone number not between 0 and 60");

            if(csDefs.contains("WGS84")) {
System.out.println("cannot handle geoid-based UTM yet");
            }

            ParameterValueGroup parameters =
                mathFactory.getDefaultParameters("Transverse_Mercator");
            parameters.parameter("semi_major").setValue(wgs84Ellipsoid.getSemiMajorAxis());
            parameters.parameter("semi_minor").setValue(wgs84Ellipsoid.getSemiMinorAxis());
            parameters.parameter("central_meridian").setValue(-180 + zone_num * 6 - 3);
            parameters.parameter("latitude_of_origin").setValue(0.0);
            parameters.parameter("scale_factor").setValue(0.9996);
            parameters.parameter("false_easting").setValue(500000.0);

            if(csDefs.contains("S"))
                parameters.parameter("false_northing").setValue(10000000);
            else
                parameters.parameter("false_northing").setValue(0.0);

            HashMap map = new HashMap();
            map.put("name","WGS 84 / UTM Zone " + zone_id);

            CoordinateReferenceSystem proj_3d =
                factoryHelper.createProjectedCRS(map,
                                                 utmGeographicCRS,
                                                 null, // Optional
                                                 parameters,
                                                 utmCartesianCS);


            ret_val = transformFactory.createOperation(proj_3d, outputCRS);

            utmZoneMap.put(zone_str, ret_val);
        }

        return ret_val;
    }

    /**
     * Find the ellispoid corresponding to the required definition. If nothing
     * is defined, then it will default to the WGS84 ellipsoid.
     */
    private Ellipsoid fetchEllipsoid(String[] systemDef)
        throws FactoryException {
        Ellipsoid ret_val = null;
        String e_def = "WE";

        for(int i = 0; i < systemDef.length; i++) {
            if(allEllipsoidSet.contains(systemDef[i])) {
                e_def = systemDef[i];
                break;
            }
        }

        ret_val = (Ellipsoid)cachedEllipsoidMap.get(e_def);
        if(ret_val == null) {
            // not in the cache, so better go create a new one
            ret_val = addEllipsoidToCache(e_def);
        }

        return ret_val;
    }

    /**
     * Add a new ellipsoid definition to the cache from the given ellipsoid
     * string. If none is found, then we should probably issue an error somewhere.
     *
     * @param ellipsoidString Description of the ellipsoid abreviation code
     * @return A GT2 description of the Ellipsoid
     */
    private Ellipsoid addEllipsoidToCache(String ellipsoidString)
        throws FactoryException {
        Ellipsoid ret_val = null;

        // http://cartome.org/nima-grids.htm#ZZ18
        if(ellipsoidString.equals("AA")) {
            HashMap map = new HashMap();
            map.put("name", "Airy 1830");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377563.396,
                                                         299.3249646,
                                                         SI.METER);
        } else if(ellipsoidString.equals("AM")) {
            HashMap map = new HashMap();
            map.put("name", "Modified Airy");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377340.189,
                                                         299.3249646,
                                                         SI.METER);
        } else if(ellipsoidString.equals("AN")) {
            HashMap map = new HashMap();
            map.put("name", "Australia National");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378160,
                                                         298.25,
                                                         SI.METER);
        } else if(ellipsoidString.equals("BN")) {
            HashMap map = new HashMap();
            map.put("name", "Bessel 1841 (Namibia)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377483.856,
                                                         299.1528128,
                                                         SI.METER);
        } else if(ellipsoidString.equals("BR")) {
            HashMap map = new HashMap();
            map.put("name", "Bessel 1841 (Ethiopia)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377397.155,
                                                         299.1528128,
                                                         SI.METER);
        } else if(ellipsoidString.equals("CC")) {
            HashMap map = new HashMap();
            map.put("name", "Clarke 1866");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378206.4,
                                                         294.9786982,
                                                         SI.METER);
        } else if(ellipsoidString.equals("CD")) {
            HashMap map = new HashMap();
            map.put("name", "Clarke 1880");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378249.145,
                                                         293.465,
                                                         SI.METER);
        } else if(ellipsoidString.equals("EA")) {
            HashMap map = new HashMap();
            map.put("name", "Everest (India 1830)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377276.345,
                                                         300.8017,
                                                         SI.METER);
        } else if(ellipsoidString.equals("EB")) {
            HashMap map = new HashMap();
            map.put("name", "Everest (Sabah & Sarawak)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377298.556,
                                                         300.8017,
                                                         SI.METER);
        } else if(ellipsoidString.equals("EC")) {
            HashMap map = new HashMap();
            map.put("name", "Everest (India 1956)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377301.243,
                                                         300.8017,
                                                         SI.METER);
        } else if(ellipsoidString.equals("ED")) {
            HashMap map = new HashMap();
            map.put("name", "Everest (W. Malaysia 1969)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377295.664,
                                                         300.8017,
                                                         SI.METER);
        } else if(ellipsoidString.equals("EE")) {
            HashMap map = new HashMap();
            map.put("name", "Everest (W Malaysia & Singapore 1948)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377304.063,
                                                         300.8017,
                                                         SI.METER);
        } else if(ellipsoidString.equals("EF")) {
            HashMap map = new HashMap();
            map.put("name", "Everest (Pakistan)");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6377309.613,
                                                         299.3249646,
                                                         SI.METER);
        } else if(ellipsoidString.equals("FA")) {
            HashMap map = new HashMap();
            map.put("name", "Modified Fischer 1960");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378155,
                                                         298.3,
                                                         SI.METER);
        } else if(ellipsoidString.equals("HE")) {
            HashMap map = new HashMap();
            map.put("name", "Helmert 1906");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378200,
                                                         298.3,
                                                         SI.METER);
        } else if(ellipsoidString.equals("HO")) {
            HashMap map = new HashMap();
            map.put("name", "Hough 1960");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378270,
                                                         297,
                                                         SI.METER);
        } else if(ellipsoidString.equals("ID")) {
            HashMap map = new HashMap();
            map.put("name", "Indonesia 1974");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378160,
                                                         298.247,
                                                         SI.METER);
        } else if(ellipsoidString.equals("IN")) {
            HashMap map = new HashMap();
            map.put("name", "International 1924");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378388,
                                                         297,
                                                         SI.METER);
        } else if(ellipsoidString.equals("KA")) {
            HashMap map = new HashMap();
            map.put("name", "Krassovsky 1940");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378245,
                                                         298.3,
                                                         SI.METER);
        } else if(ellipsoidString.equals("RF")) {
            HashMap map = new HashMap();
            map.put("name", "Geodetic Reference System 1980");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378137,
                                                         298.257222101,
                                                         SI.METER);
        } else if(ellipsoidString.equals("SA")) {
            HashMap map = new HashMap();
            map.put("name", "South American 1969");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378160,
                                                         298.25,
                                                         SI.METER);
        } else if(ellipsoidString.equals("WD")) {
            HashMap map = new HashMap();
            map.put("name", "WGS 72");
            ret_val = datumFactory.createFlattenedSphere(map,
                                                         6378135,
                                                         298.26,
                                                         SI.METER);
        } else
System.out.println("Unknown ellipsoid definition passed: " + ellipsoidString);

        if(ret_val != null)
            cachedEllipsoidMap.put(ellipsoidString, ret_val);

        return ret_val;

    }
}
