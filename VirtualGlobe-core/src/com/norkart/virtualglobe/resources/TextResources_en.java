//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.resources;

import java.util.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */


public class TextResources_en extends ListResourceBundle {
  protected static final Object[][] contents = {
    {"CAMERA_POSITION", "Camera position"},
    {"POINTER_POSITION", "Pointer position"},
    {"LIGHT_MENU", "Lighting"},
    {"CAMERA_MENU", "Camera"},
    {"NORTH_UP", "North up"},
    {"CACHE", "Local file cache"},
    {"CACHE_ENABLED", "Cache enabled"},
    {"CACHE_DIR", "Cache directory"},
    {"CACHE_SIZE", "Cache size (Megabytes)"},
    {"CACHE_DELETE", "Delete file cache"},
    {"RECONFIGURE_MESSAGE", "The program has to reconfigure to adapt to your computer."},
    {"RESTART_MESSAGE", "Please exit and restart the Virual Globe."},
    {"FOV_BOX_TITLE", "Horizontal Field of View"},
    {"FOV_LABEL", "View angle : {0,number,#.#} degrees"},
    {"FOV_TIPS", "Horizontal Field of View for camera"},
    {"DETAIL_SIZE_BOX_TITLE", "Resolution"},
    {"DETAIL_SIZE_LABEL", "Max detail size : {0,number,#.#} pixels"},
    {"DETAIL_SIZE_TIPS", "Maximum size of details in screen pixels"},
    {"FRAMERATE_LABEL", "Framerate : {0,number,#.#} fps"},
    {"LIGHT_DIRECTION_TITLE", "Lightsource position"},
    {"LIGHT_DIRECTION_LABEL", "Light direction : {0,number,#.#} degrees"},
    {"LIGHT_DIRECTION_TIPS", "Light direction relative to view direction"},
    {"LIGHT_ELEVATION_LABEL", "Light elevation : {0,number,#.#} degrees"},
    {"LIGHT_ELEVATION_TIPS", "Light elevation relative to view direction"},
    {"LIGHT_INTENCITY_TITLE","Lightsource intencities"},
    {"LIGHT_DIRECTIONAL_INTENCITY_LABEL","Directional : {0,number,#.#}%"},
    {"LIGHT_DIRECTIONAL_INTENCITY_TIPS","Strength of directional light"},
    {"LIGHT_SPECULAR_INTENCITY_LABEL","Specular : {0,number,#.#}%"},
    {"LIGHT_SPECULAR_INTENCITY_TIPS","Strength of specular light"},
    {"LIGHT_AMBIENT_INTENCITY_LABEL","Ambient : {0,number,#.#}%"},
    {"LIGHT_AMBIENT_INTENCITY_TIPS","Strength of ambient light"},

    {"MEASUREMENT_TOOL", "Mesurement tool"},
    {"TOTAL_LENGTH", "Total length"},
    {"NEXT_LENGTH", "Distance to next point"},
    {"NEXT_DH", "Height difference to next point"},
    {"NEXT_AZ", "Direction to next point"},
    {"CLEAR_LAST", "Delete last point"},
    {"CLEAR_ALL", "Delete all points"},
    
    {"WORLD_TITLE", "World"},
    {"GLOBE_SURFACE_TITLE", "Globe surface"},
    {"LAYERED_COVERAGE_TITLE", "Map layers"},
    {"LAYERED_COVERAGE_SERVER", "Map layer server"},
    {"SIMPLE_COVERAGE_TITLE", "Map layers"},
    {"SIMPLE_COVERAGE_SERVER", "Map layer server"},
    {"FEATURE_SET_TITLE", "Feature set"},
    {"ELEVATION_URL_LABEL", "Elevation server"},
    {"WMS_URL", "WMS URL"},
    {"WIREFRAME_LABEL", "Wireframe"},
    {"ELEVATION_MULT_LABEL", "Elevation mult : {0,number,#.#}"},
    {"ELEVATION_MULT_TIPS", "Multiplication factor for the elevation values"},
    {"TRANSPARENCY_LABEL", "Transparency : {0,number,#.#}"},
    {"TRANSPARENCY_TIPS", "Transparency for the surface (0 = totally transparent, 100 = totally opaque"},
    
    {"GEO_RSS_TITLE", "GeoRSS"},
    {"TITLE", "Title"},
    {"DATE", "Date"},
    {"POSITION", "Position"},
    {"TYPE", "Type"},
    {"SEARCH", "Search"},

    {"OPEN", "Open"},
    {"FLY_TO", "Fly to this place"},
    
    {"OK", "Ok"},
    {"APPLY", "Apply"},
    {"CANCEL", "Cancel"},
    {"ENABLED", "Enabled"},
    {"DATASET_LABEL", "Dataset"},
    {"LAT_LABEL","Latitude"},
    {"LON_LABEL","Longitude"},
    {"H_SEA_LABEL","Height o. sea"},
    {"H_TERR_LABEL","Height o. terrain"},
    {"AZ_LABEL","Azimuth"},
    {"HA_LABEL","Height angle"},
    {"FILE_MENU","File"},
    {"TOOLS_MENU","Tools"},
    {"HELP_MENU","Help"},
    {"EXIT_MENU","Exit"},
    {"MAP_MENU", "Open map view"},
    {"MAP_DIALOG", "Overview map"},
    {"RETURN_MENU", "Return to start point"},
    {"COPY_URL_MENU","Copy link for current view"},
    {"PASTE_URL_MENU","Paste link for a view"},
    {"COPY_IMAGE_MENU","Copy image of current view"},
    {"SAVE_IMAGE_MENU","Save image of current view"},
    {"NO_SAVE_MESSAGE","Unable to save : {0}"},
    {"NO_OPEN_MESSAGE","Unable to open : {0}"},
    {"OPEN_WORLD", "Open world"},
    {"SAVE_WORLD", "Save world"},
    {"SETTINGS", "Settings"},
    {"GRAPHICS", "Graphics"},
    {"ENVIRONMENT", "Environment"},
    {"USE_HAZE", "Haze"},
    {"USE_SKY_COLOR", "Sky color"},
    {"USE_COMPRESSED_TEXTURE", "Compressed textures"},
    {"USE_VBO", "Vertex Buffer Objects"},
    {"MAX_FPS_LABEL","Max frames pr. second"},
    {"TEX_MEM_LABEL","Texture memory (MB)"},
    {"FLYPATH", "Fly Path"},
    {"RECORD", "Record"},
    {"STOP", "Stop"},
    {"PLAY", "Play"},
    {"LOAD", "Load"},
    {"SAVE", "Save"},
    {"VIDEO", "Capture video"},
    {"SAVE_VIDEO", "Video file..."},
    {"VIEWPOINTS", "Viewpoints"},
    {"ADD_VIEWPOINT_BUTTON", "Add"},
    {"DELETE", "Delete"},
    {"COPY", "Copy"},
    {"CUT", "Cut"},
    {"PASTE", "Paste"},
    {"GOTO", "Goto"},
    {"CLOSE", "Close"},
    {"VIEWPOINT_NAME_INPUT", "Name of viewpoint"},
    {"NAME_LABEL", "Name"},
    {"TOOL_LEFT_TURN", "Turn left"},
    {"TOOL_RIGHT_TURN", "Turn right"},
    {"TOOL_ACCELERATE", "Fly, increase speed"},
    {"TOOL_DECELERATE", "Fly, reduce speed or reverese"},
    {"TOOL_STOP", "Fly, stop"},
    {"TOOL_ASCEND", "Increase height"},
    {"TOOL_DESCEND", "Reduce height"},
    {"TOOL_UP_TURN", "Turn upwards"},
    {"TOOL_DOWN_TURN", "Turn downwards"},
    {"TOOL_GOTO_VIEWPOINT", "Go to viewpoint"},
    {"TOOL_PLAY_FLIGHT_PATH", "Play flight path"},
    {"TOOL_TOGGLE_WMS_LAYER","Toggle on/off WMS map layer"},

    {"TOOL_NORTH_UP","Switch between north up / face up"},
    {"TOOL_MAP_SCALE_SLIDER","2D map scale"},
    
    {"TOOL_DUMP_TO_CLIP", "Capture 3D image to clipboard"},
    {"TOOL_SETTINGS", "Open settings/preferences menu"},
    {"TOOL_HELP", "Open help window"},

    {"TOOL_CACHE", "Settings for local disk cache"},
    {"TOOL_GRAPHICS", "Graphic system settings"},
    {"TOOL_ENVIRONMENT", "Environmental effects"},
    {"TOOL_MAX_FPS", "Restrict the framerate when flying to save computer resources for other work"},
    {"TOOL_MAX_TEX_MEM", "Restrict the amount of video memory used for terrain textures"},
    {"TOOL_COMPRESS_TEX", "Use compressed textures. On most computers this is a great performance win. "},
    {"TOOL_USE_VBO", "Use Vertex Buffer Objects. On most computers this is a great performance win. " +
     "If the application works better with this turned off you shold try to upgrade your graphics drivers."},
    {"TOOL_TEX_FILTER", "Texture filtering: Higher values give a sharper image, "+
     "especially for terrain viewed in an obligue angle, but may also reduce performance."},
    {"TOOL_MULTISAMPLE", "Antialiasing: Higher values give a smoother image, but may also reduce performance"},
    {"TOOL_ENABLE_CACHE", "Toggle local disk cache on/off. " +
     "Using a local cache enables offline access to previously visited areas and may improve performance when using slow network connections"},
    {"TOOL_CACHE_DIR", "Choose a directory for the local file cache"},
    {"TOOL_HAZE", "Enable a light sky haze for a more natural looking atmosphere"},
    {"TOOL_SKY_COLOR", "Enable an elevation dependent sky color for a more natural looking atmosphere"},

    {"WORLD_TREE_USAGE", "<HTML><BODY>The application may need a few secounds to load its " +
    "data. If the graphics window to the right seems blurred, this may be because not all " +
    "details are loaded.<P>" +
    "To move around in the graphics window with the mouse:<DL>" +
    "<DT><b>Left Button:</b><DD>Move around horizontally" +
    "<DT><b>Middle Button (Wheel as mouse button):</b><DD>Change view direction" +
    "<DT><b>Right Button:</b><DD>Rotate viewpoint around place you are pointing at" +
    "<DT><b>Rotate wheel:</b><DD>Move forwards and backwards" +
    "</DL><P>" +
    "To move around in the graphics window with the keys:<DL>" +
    "<DT><b>Arrow keys:</b><DD>Move around horizontally" +
    "<DT><b>[PageUp]/[PageDown]-keys:</b><DD>Move forwards and backwards" +
    "<DT><b>[ctrl]-arrow keys:</b><DD>Change view direction" +
    "</DL><P>" +
    "To fly:<DL>" +
    "<DT><b>a-key:</b><DD>Accelerate forward" +
    "<DT><b>z-key:</b><DD>Brake / accelerate backwards" +
    "<DT><b>[Space]:</b><DD>Stop" +
    "</DL>Combining with [shift] accelerates the key action<P>" +
    "Select an element in the " +
    "data structure tree above " +
    "to enable data specific menues."+
    "</BODY>>/HTML>"},
  };

  public Object[][] getContents() {
    return contents;
  }
}
