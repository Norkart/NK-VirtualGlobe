/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.parser.x3d;

import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.util.QualifiedNameArray;
import com.sun.xml.fastinfoset.util.LocalNameQualifiedNamesMap;
import com.sun.xml.fastinfoset.util.DuplicateAttributeVerifier;
import com.sun.xml.fastinfoset.util.KeyIntMap;

/**
 * A fixed vocabulary for X3D FastInfoset files.
 *
 * @author Alan Hudson
 * @version
 */
public class X3DBinaryVocabulary {
    public static final SerializerVocabulary serializerVoc;
    public static final ParserVocabulary parserVoc;

    private static QualifiedName name;
    private static LocalNameQualifiedNamesMap.Entry entry;
    private static int idx;

    static {
        serializerVoc = new SerializerVocabulary();
        parserVoc = new ParserVocabulary();

        serializerVoc.encodingAlgorithm.add(ByteEncodingAlgorithm.ALGORITHM_URI);
        serializerVoc.encodingAlgorithm.add(DeltazlibIntArrayAlgorithm.ALGORITHM_URI);
        serializerVoc.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI);

        parserVoc.encodingAlgorithm.add(ByteEncodingAlgorithm.ALGORITHM_URI);
        parserVoc.encodingAlgorithm.add(DeltazlibIntArrayAlgorithm.ALGORITHM_URI);
        parserVoc.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI);

        QualifiedName name;
        LocalNameQualifiedNamesMap.Entry entry;
        String nodeName;
        int idx;

        addElement("Shape");
        addElement("Appearance");
        addElement("Material");
        addElement("IndexedFaceSet");
        addElement("ProtoInstance");
        addElement("Transform");
        addElement("ImageTexture");
        addElement("TextureTransform");
        addElement("Coordinate");
        addElement("Normal");
        addElement("Color");
        addElement("ColorRGBA");
        addElement("TextureCoordinate");
        addElement("ROUTE");
        addElement("fieldValue");
        addElement("Group");
        addElement("LOD");
        addElement("Switch");
        addElement("Script");
        addElement("IndexedTriangleFanSet");
        addElement("IndexedTriangleSet");
        addElement("IndexedTriangleStripSet");
        addElement("MultiTexture");
        addElement("MultiTextureCoordinate");
        addElement("MultiTextureTransform");
        addElement("IndexedLineSet");
        addElement("PointSet");
        addElement("StaticGroup");
        addElement("Sphere");
        addElement("Box");
        addElement("Cone");
        addElement("Anchor");
        addElement("Arc2D");
        addElement("ArcClose2D");
        addElement("AudioClip");
        addElement("Background");
        addElement("Billboard");
        addElement("BooleanFilter");
        addElement("BooleanSequencer");
        addElement("BooleanToggle");
        addElement("BooleanTrigger");
        addElement("Circle2D");
        addElement("Collision");
        addElement("ColorInterpolator");
        addElement("Contour2D");
        addElement("ContourPolyline2D");
        addElement("CoordinateDouble");
        addElement("CoordinateInterpolator");
        addElement("CoordinateInterpolator2D");
        addElement("Cylinder");
        addElement("CylinderSensor");
        addElement("DirectionalLight");
        addElement("Disk2D");
        addElement("EXPORT");
        addElement("ElevationGrid");
        addElement("EspduTransform");
        addElement("ExternProtoDeclare");
        addElement("Extrusion");
        addElement("FillProperties");
        addElement("Fog");
        addElement("FontStyle");
        addElement("GeoCoordinate");
        addElement("GeoElevationGrid");
        addElement("GeoLOD");
        addElement("GeoLocation");
        addElement("GeoMetadata");
        addElement("GeoOrigin");
        addElement("GeoPositionInterpolator");
        addElement("GeoTouchSensor");
        addElement("GeoViewpoint");
        addElement("HAnimDisplacer");
        addElement("HAnimHumanoid");
        addElement("HAnimJoint");
        addElement("HAnimSegment");
        addElement("HAnimSite");
        addElement("IMPORT");
        addElement("IS");
        addElement("Inline");
        addElement("IntegerSequencer");
        addElement("IntegerTrigger");
        addElement("KeySensor");
        addElement("LineProperties");
        addElement("LineSet");
        addElement("LoadSensor");
        addElement("MetadataDouble");
        addElement("MetadataFloat");
        addElement("MetadataInteger");
        addElement("MetadataSet");
        addElement("MetadataString");
        addElement("MovieTexture");
        addElement("NavigationInfo");
        addElement("NormalInterpolator");
        addElement("NurbsCurve");
        addElement("NurbsCurve2D");
        addElement("NurbsOrientationInterpolator");
        addElement("NurbsPatchSurface");
        addElement("NurbsPositionInterpolator");
        addElement("NurbsSet");
        addElement("NurbsSurfaceInterpolator");
        addElement("NurbsSweptSurface");
        addElement("NurbsSwungSurface");
        addElement("NurbsTextureCoordinate");
        addElement("NurbsTrimmedSurface");
        addElement("OrientationInterpolator");
        addElement("PixelTexture");
        addElement("PlaneSensor");
        addElement("PointLight");
        addElement("Polyline2D");
        addElement("Polypoint2D");
        addElement("PositionInterpolator");
        addElement("PositionInterpolator2D");
        addElement("ProtoBody");
        addElement("ProtoDeclare");
        addElement("ProtoInterface");
        addElement("ProximitySensor");
        addElement("ReceiverPdu");
        addElement("Rectangle2D");
        addElement("ScalarInterpolator");
        addElement("Scene");
        addElement("SignalPdu");
        addElement("Sound");
        addElement("SphereSensor");
        addElement("SpotLight");
        addElement("StringSensor");
        addElement("Text");
        addElement("TextureBackground");
        addElement("TextureCoordinateGenerator");
        addElement("TimeSensor");
        addElement("TimeTrigger");
        addElement("TouchSensor");
        addElement("TransmitterPdu");
        addElement("TriangleFanSet");
        addElement("TriangleSet");
        addElement("TriangleSet2D");
        addElement("TriangleStripSet");
        addElement("Viewpoint");
        addElement("VisibilitySensor");
        addElement("WorldInfo");
        addElement("X3D");
        addElement("component");
        addElement("connect");
        addElement("field");
        addElement("head");
        addElement("humanoidBodyType");
        addElement("meta");

        // TODO: Need to reserve up to 512

        addAttribute("DEF");
        addAttribute("USE");
        addAttribute("containerField");
        addAttribute("fromNode");
        addAttribute("fromField");
        addAttribute("toNode");

        addAttribute("toField");
        addAttribute("name");
        addAttribute("value");
        addAttribute("color");
        addAttribute("colorIndex");
        addAttribute("coordIndex");

        addAttribute("texCoordIndex");
        addAttribute("normalIndex");
        addAttribute("colorPerVertex");
        addAttribute("normalPerVertex");
        addAttribute("rotation");
        addAttribute("scale");

        addAttribute("center");
        addAttribute("scaleOrientation");
        addAttribute("translation");
        addAttribute("url");
        addAttribute("repeatS");
        addAttribute("repeatT");

        addAttribute("point");
        addAttribute("vector");
        addAttribute("range");
        addAttribute("ambientIntensity");
        addAttribute("diffuseColor");
        addAttribute("emissiveColor");

        addAttribute("shininess");
        addAttribute("specularColor");
        addAttribute("transparency");
        addAttribute("whichChoice");
        addAttribute("index");
        addAttribute("mode");

        addAttribute("source");
        addAttribute("function");
        addAttribute("alpha");
        addAttribute("vertexCount");
        addAttribute("radius");
        addAttribute("size");

        addAttribute("height");
        addAttribute("solid");
        addAttribute("ccw");
        addAttribute("key");
        addAttribute("keyValue");
        addAttribute("enabled");

        addAttribute("direction");
        addAttribute("position");
        addAttribute("orientation");
        addAttribute("bboxCenter");
        addAttribute("bboxSize");
        addAttribute("AS");

        addAttribute("InlineDEF");
        addAttribute("accessType");
        addAttribute("actionKeyPress");
        addAttribute("actionKeyRelease");
        addAttribute("address");
        addAttribute("altKey");

        addAttribute("antennaLocation");
        addAttribute("antennaPatternLength");
        addAttribute("antennaPatternType");
        addAttribute("applicationID");
        addAttribute("articulationParameterArray");
        addAttribute("articulationParameterChangeIndicatorArray");

        addAttribute("articulationParameterCount");
        addAttribute("articulationParameterDesignatorArray");
        addAttribute("articulationParameterIdPartAttachedArray");
        addAttribute("articulationParameterTypeArray");
        addAttribute("attenuation");
        addAttribute("autoOffset");

        addAttribute("avatarSize");
        addAttribute("axisOfRotation");
        addAttribute("backUrl");
        addAttribute("beamWidth");
        addAttribute("beginCap");
        addAttribute("bindTime");

        addAttribute("bottom");
        addAttribute("bottomRadius");
        addAttribute("bottomUrl");
        addAttribute("centerOfMass");
        addAttribute("centerOfRotation");

        addAttribute("child1Url");
        addAttribute("child2Url");
        addAttribute("child3Url");
        addAttribute("child4Url");
        addAttribute("class");
        addAttribute("closureType");

        addAttribute("collideTime");
        addAttribute("content");
        addAttribute("controlKey");
        addAttribute("controlPoint");
        addAttribute("convex");

        addAttribute("coordinateSystem");
        addAttribute("copyright");
        addAttribute("creaseAngle");
        addAttribute("crossSection");
        addAttribute("cryptoKeyID");
        addAttribute("cryptoSystem");

        addAttribute("cutOffAngle");
        addAttribute("cycleInterval");
        addAttribute("cycleTime");
        addAttribute("data");
        addAttribute("dataFormat");
        addAttribute("dataLength");

        addAttribute("dataUrl");
        addAttribute("date");
        addAttribute("deadReckoning");
        addAttribute("deletionAllowed");
        addAttribute("description");
        addAttribute("detonateTime");

        addAttribute("dir");
        addAttribute("directOutput");
        addAttribute("diskAngle");
        addAttribute("displacements");
        addAttribute("documentation");
        addAttribute("elapsedTime");

        addAttribute("ellipsoid");
        addAttribute("encodingScheme");
        addAttribute("endAngle");
        addAttribute("endCap");
        addAttribute("enterTime");
        addAttribute("enteredText");

        addAttribute("entityCategory");
        addAttribute("entityCountry");
        addAttribute("entityDomain");
        addAttribute("entityExtra");
        addAttribute("entityID");
        addAttribute("entityKind");

        addAttribute("entitySpecific");
        addAttribute("entitySubCategory");
        addAttribute("exitTime");
        addAttribute("extent");
        addAttribute("family");
        addAttribute("fanCount");

        addAttribute("fieldOfView");
        addAttribute("filled");
        addAttribute("finalText");
        addAttribute("fireMissionIndex");
        addAttribute("fired1");
        addAttribute("fired2");

        addAttribute("firedTime");
        addAttribute("firingRange");
        addAttribute("firingRate");
        addAttribute("fogType");
        addAttribute("forceID");
        addAttribute("frequency");

        addAttribute("frontUrl");
        addAttribute("fuse");
        addAttribute("geoCoords");
        addAttribute("geoGridOrigin");
        addAttribute("geoSystem");
        addAttribute("groundAngle");

        addAttribute("groundColor");
        addAttribute("hatchColor");
        addAttribute("hatchStyle");
        addAttribute("hatched");
        addAttribute("headlight");
        addAttribute("horizontal");

        addAttribute("horizontalDatum");
        addAttribute("http-equiv");
        addAttribute("image");
        addAttribute("importedDEF");
        addAttribute("info");
        addAttribute("innerRadius");

        addAttribute("inputFalse");
        addAttribute("inputNegate");
        addAttribute("inputSource");
        addAttribute("inputTrue");
        addAttribute("integerKey");
        addAttribute("intensity");

        addAttribute("jump");
        addAttribute("justify");
        addAttribute("keyPress");
        addAttribute("keyRelease");
        addAttribute("knot");
        addAttribute("lang");

        addAttribute("language");
        addAttribute("leftToRight");
        addAttribute("leftUrl");
        addAttribute("length");
        addAttribute("lengthOfModulationParameters");
        addAttribute("level");

        addAttribute("limitOrientation");
        addAttribute("lineSegments");
        addAttribute("linearAcceleration");
        addAttribute("linearVelocity");
        addAttribute("linetype");
        addAttribute("linewidthScaleFactor");

        addAttribute("llimit");
        addAttribute("load");
        addAttribute("loadTime");
        addAttribute("localDEF");
        addAttribute("location");
        addAttribute("loop");

        addAttribute("marking");
        addAttribute("mass");
        addAttribute("maxAngle");
        addAttribute("maxBack");
        addAttribute("maxExtent");
        addAttribute("maxFront");

        addAttribute("maxPosition");
        addAttribute("metadataFormat");
        addAttribute("minAngle");
        addAttribute("minBack");
        addAttribute("minFront");
        addAttribute("minPosition");

        addAttribute("modulationTypeDetail");
        addAttribute("modulationTypeMajor");
        addAttribute("modulationTypeSpreadSpectrum");
        addAttribute("modulationTypeSystem");
        addAttribute("momentsOfInertia");
        addAttribute("multicastRelayHost");

        addAttribute("multicastRelayPort");
        addAttribute("munitionApplicationID");
        addAttribute("munitionEndPoint");
        addAttribute("munitionEntityID");
        addAttribute("munitionQuantity");
        addAttribute("munitionSiteID");

        addAttribute("munitionStartPoint");
        addAttribute("mustEvaluate");
        addAttribute("navType");
        addAttribute("networkMode");
        addAttribute("next");
        addAttribute("nodeField");

        addAttribute("offset");
        addAttribute("on");
        addAttribute("order");
        addAttribute("originator");
        addAttribute("outerRadius");
        addAttribute("parameter");

        addAttribute("pauseTime");
        addAttribute("pitch");
        addAttribute("points");
        addAttribute("port");
        addAttribute("power");
        addAttribute("previous");

        addAttribute("priority");
        addAttribute("profile");
        addAttribute("progress");
        addAttribute("protoField");
        addAttribute("radioEntityTypeCategory");
        addAttribute("radioEntityTypeCountry");

        addAttribute("radioEntityTypeDomain");
        addAttribute("radioEntityTypeKind");
        addAttribute("radioEntityTypeNomenclature");
        addAttribute("radioEntityTypeNomenclatureVersion");
        addAttribute("radioID");
        addAttribute("readInterval");

        addAttribute("receivedPower");
        addAttribute("receiverState");
        addAttribute("reference");
        addAttribute("relativeAntennaLocation");
        addAttribute("resolution");
        addAttribute("resumeTime");

        addAttribute("rightUrl");
        addAttribute("rootUrl");
        addAttribute("rotateYUp");
        addAttribute("rtpHeaderExpected");
        addAttribute("sampleRate");
        addAttribute("samples");

        addAttribute("shiftKey");
        addAttribute("side");
        addAttribute("siteID");
        addAttribute("skinCoordIndex");
        addAttribute("skinCoordWeight");
        addAttribute("skyAngle");

        addAttribute("skyColor");
        addAttribute("spacing");
        addAttribute("spatialize");
        addAttribute("speed");
        addAttribute("speedFactor");
        addAttribute("spine");

        addAttribute("startAngle");
        addAttribute("startTime");
        addAttribute("stiffness");
        addAttribute("stopTime");
        addAttribute("string");
        addAttribute("stripCount");

        addAttribute("style");
        addAttribute("summary");
        addAttribute("tdlType");
        addAttribute("tessellation");
        addAttribute("tessellationScale");
        addAttribute("time");

        addAttribute("timeOut");
        addAttribute("timestamp");
        addAttribute("title");
        addAttribute("toggle");
        addAttribute("top");
        addAttribute("topToBottom");

        addAttribute("topUrl");
        addAttribute("touchTime");
        addAttribute("transmitFrequencyBandwidth");
        addAttribute("transmitState");
        addAttribute("transmitterApplicationID");
        addAttribute("transmitterEntityID");

        addAttribute("transmitterRadioID");
        addAttribute("transmitterSiteID");
        addAttribute("transparent");
        addAttribute("triggerTime");
        addAttribute("triggerTrue");
        addAttribute("triggerValue");

        addAttribute("type");
        addAttribute("uDimension");
        addAttribute("uKnot");
        addAttribute("uOrder");
        addAttribute("uTessellation");
        addAttribute("ulimit");

        addAttribute("vDimension");
        addAttribute("vKnot");
        addAttribute("vOrder");
        addAttribute("vTessellation");
        addAttribute("version");
        addAttribute("verticalDatum");

        addAttribute("vertices");
        addAttribute("visibilityLimit");
        addAttribute("visibilityRange");
        addAttribute("warhead");
        addAttribute("weight");
        addAttribute("whichGeometry");

        addAttribute("writeInterval");
        addAttribute("xDimension");
        addAttribute("xSpacing");
        addAttribute("yScale");
        addAttribute("zDimension");
        addAttribute("zSpacing");

        //TODO: Need to reserve up to 1024
        addAttributeValue("false");
        addAttributeValue("true");
    }

    /**
     * Add an element to the element table.
     *
     * @param eName The element name
     */
    public static final void addElement(String eName) {

        int localNameIndex = serializerVoc.localName.obtainIndex(eName);
        if (localNameIndex > -1)
            System.out.println("Duplicate Element found: " + eName);
        else
            parserVoc.localName.add(eName);

        idx = serializerVoc.elementName.getNextIndex();
        name = new QualifiedName("", "", eName, idx, -1, -1, idx);
        parserVoc.elementName.add(name);
        entry = serializerVoc.elementName.obtainEntry(eName);
        entry.addQualifiedName(name);
    }

    /**
     * Add an attribute to the attribute table.
     *
     * @param aName The attribute name
     */
    public static final void addAttribute(String aName) {

        int localNameIndex = serializerVoc.localName.obtainIndex(aName);
        if (localNameIndex > -1)
            System.out.println("Duplicate Attribute found: " + aName);
        else
            parserVoc.localName.add(aName);

        idx = serializerVoc.attributeName.getNextIndex();
        name = new QualifiedName("", "", aName, idx, -1, -1, idx);
        name.createAttributeValues(DuplicateAttributeVerifier.MAP_SIZE);

        parserVoc.attributeName.add(name);
        entry = serializerVoc.attributeName.obtainEntry(aName);
        entry.addQualifiedName(name);
    }

    private static final void addAttributeValue(String s) {
        if (serializerVoc.attributeValue.obtainIndex(s) == KeyIntMap.NOT_PRESENT) {
            parserVoc.attributeValue.add(s);
        }
    }
}