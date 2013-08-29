/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2007 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
// None

/**
 * Collada tag and attribute Strings.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public abstract class ColladaStrings {
    
    ///////////////////////////////////////////////////////////////////////////////
    // root element
    public static final String COLLADA = "COLLADA";
    ///////////////////////////////////////////////////////////////////////////////
    // root children
    public static final String ASSET = "asset";
    public static final String LIBRARY_ANIMATIONS = "library_animations";
    public static final String LIBRARY_CAMERAS = "library_cameras";
    public static final String LIBRARY_EFFECTS = "library_effects";
    public static final String LIBRARY_GEOMETRIES = "library_geometries";
    public static final String LIBRARY_MATERIALS = "library_materials";
    public static final String LIBRARY_NODES = "library_nodes";
    public static final String LIBRARY_VISUAL_SCENES = "library_visual_scenes";
    public static final String SCENE = "scene";
    ///////////////////////////////////////////////////////////////////////////////
    // core elements
    public static final String ACCESSOR = "accessor";
    public static final String ANIMATION = "animation";
    public static final String BIND_MATERIAL = "bind_material";
    public static final String BOOL_ARRAY = "bool_array";
    public static final String CAMERA = "camera";
    public static final String CHANNEL = "channel";
    public static final String EFFECT = "effect";
    public static final String FLOAT_ARRAY = "float_array";
    public static final String GEOMETRY = "geometry";
    public static final String INPUT = "input";
    public static final String INSTANCE_CAMERA = "instance_camera";
    public static final String INSTANCE_EFFECT = "instance_effect";
    public static final String INSTANCE_GEOMETRY = "instance_geometry";
    public static final String INSTANCE_MATERIAL = "instance_material";
    public static final String INSTANCE_NODE = "instance_node";
    public static final String INSTANCE_VISUAL_SCENE = "instance_visual_scene";
    public static final String INT_ARRAY = "int_array";
    public static final String LINES = "lines";
    public static final String LINESTRIPS = "linestrips";
    public static final String LOOKAT = "lookat";
    public static final String MATERIAL = "material";
    public static final String MATRIX = "matrix";
    public static final String MESH = "mesh";
    public static final String NODE = "node";
    public static final String OPTICS = "optics";
    public static final String ORTHOGRAPHIC = "orthographic";
    public static final String PERSPECTIVE = "perspective";
    public static final String POLYGONS = "polygons";
    public static final String POLYLIST = "polylist";
    public static final String ROTATE = "rotate";
    public static final String SAMPLER = "sampler";
    public static final String SCALE = "scale";
    public static final String SKEW = "skew";
    public static final String TARGET = "target";
    public static final String TECHNIQUE_COMMON = "technique_common";
    public static final String TRANSLATE = "translate";
    public static final String TRIANGLES = "triangles";
    public static final String TRIFANS = "trifans";
    public static final String TRISTRIPS = "tristrips";
    public static final String VERTICES = "vertices";
    public static final String VISUAL_SCENE = "visual_scene";
    ///////////////////////////////////////////////////////////////////////////////
    // <asset> child elements
    public static final String CONTRIBUTOR = "contributor";
    public static final String KEYWORDS = "keywords";
    public static final String MODIFIED = "modified";
    public static final String REVISION = "revision";
    public static final String SUBJECT = "subject";
    public static final String TITLE = "title";
    public static final String UNIT = "unit";
    public static final String UP_AXIS = "up_axis";
    ///////////////////////////////////////////////////////////////////////////////
    // <effect> child elements
    public static final String PROFILE_COMMON = "profile_COMMON";
    public static final String BLINN = "blinn";
    public static final String CONSTANT = "constant";
    public static final String LAMBERT = "lambert";
    public static final String PHONG = "phong";
    public static final String TECHNIQUE = "technique";
    //
    public static final String AMBIENT = "ambient";
    public static final String DIFFUSE = "diffuse";
    public static final String EMISSION = "emission";
    public static final String SPECULAR = "specular";
    public static final String SHININESS = "shininess";
    public static final String TRANSPARENCY = "transparency";
    //
    public static final String COLOR = "color";
    public static final String FLOAT = "float";
    ///////////////////////////////////////////////////////////////////////////////
    // <unit> attributes
    public static final String METER = "meter";
    ///////////////////////////////////////////////////////////////////////////////
    // <up_axis> values
    public static final String X_UP = "X_UP";
    public static final String Y_UP = "Y_UP";
    public static final String Z_UP = "Z_UP";
    ///////////////////////////////////////////////////////////////////////////////
    // misc. child elements
    public static final String P = "p"; // primative
    public static final String PARAM = "param";
    public static final String VCOUNT = "vcount";
    ///////////////////////////////////////////////////////////////////////////////
    // <orthographic>, <perspective> - child elements
    public static final String ASPECT_RATIO = "aspect_ratio";
    public static final String XFOV = "xfov";
    public static final String YFOV = "yfov";
    public static final String ZNEAR = "znear";
    public static final String ZFAR = "zfar";
    ///////////////////////////////////////////////////////////////////////////////
    // misc. attributes
    public static final String COUNT = "count";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String OFFSET = "offset";
    public static final String SEMANTIC = "semantic";
    public static final String SET = "set";
    public static final String SID = "sid";
    public static final String SOURCE = "source";
    public static final String STRIDE = "stride";
    public static final String SYMBOL = "symbol";
    public static final String TYPE = "type";
    public static final String URL = "url";
    ///////////////////////////////////////////////////////////////////////////////
    // <param> name attributes
    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";
    public static final String S = "S";
    public static final String T = "T";
    ///////////////////////////////////////////////////////////////////////////////
    // <input> semantic attributes
    public static final String NORMAL = "NORMAL";
    public static final String POSITION = "POSITION";
    public static final String VERTEX = "VERTEX";
    public static final String TEXCOORD = "TEXCOORD";
    public static final String INPUT_SEMANTIC = "INPUT";
    public static final String OUTPUT = "OUTPUT";
    public static final String INTERPOLATION = "INTERPOLATION";
    ///////////////////////////////////////////////////////////////////////////////
    
    /** Map of library tags keyed by resource type */
    private static final Map<String,String> libraries;
    
    static {
        libraries = new HashMap<String,String>();
        libraries.put(ANIMATION, LIBRARY_ANIMATIONS);
        libraries.put(CAMERA, LIBRARY_CAMERAS);
        libraries.put(EFFECT, LIBRARY_EFFECTS);
        libraries.put(GEOMETRY, LIBRARY_GEOMETRIES);
        libraries.put(MATERIAL, LIBRARY_MATERIALS);
        libraries.put(NODE, LIBRARY_NODES);
        libraries.put(VISUAL_SCENE, LIBRARY_VISUAL_SCENES);
    }
    
    /**
     * Return the tag name of the library for the argument resource type.
     *
     * @param resource The resource type
     * @return The library tag name
     */
    public static String getLibraryTagName(String resource) {
        return(libraries.get(resource));
    }
}
