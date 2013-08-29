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

public class TextResources_no extends ListResourceBundle {
  protected static final Object[][] contents = {
    {"CAMERA_POSITION", "Kamera posisjon"},
    {"POINTER_POSITION", "Peker posisjon"},
    {"LIGHT_MENU", "Lyssetting"},
    {"CAMERA_MENU", "Kamera"},
    {"NORTH_UP", "Nord opp"},
    {"CACHE", "Lokalt datalager"},
    {"CACHE_ENABLED", "Lokalt datalager aktivt"},
    {"CACHE_DIR", "Mappe for lokalt datalager"},
    {"CACHE_SIZE", "St�rrelse p� lokalt datalager (Megabyte)"},
    {"CACHE_DELETE", "Slett lokalt datalager"},
    {"RECONFIGURE_MESSAGE", "Programmet m� rekonfigurere seg for � tilpasses din maskin."},
    {"RESTART_MESSAGE", "Den Virtuelle Globusen b�r avsluttes og startes p� nytt."}, 
    {"FOV_BOX_TITLE", "Horisontal synsvinkel"},
    {"FOV_LABEL", "Synsvinkel : {0,number,#.#} grader"},
    {"FOV_TIPS", "Horisontal synsvinkel for kameraet"},
    {"DETAIL_SIZE_BOX_TITLE", "Oppl�sning"},
    {"DETAIL_SIZE_LABEL", "Max detaljst�rrelse : {0,number,#.#} pixler"},
    {"DETAIL_SIZE_TIPS", "Maksimum st�rrelse p� synlige detaljer i skjerm pixler"},
    {"FRAMERATE_LABEL", "Hastighet : {0,number,#.#} bilder pr. sekund"},
    {"LIGHT_DIRECTION_TITLE", "Lyskildeplassering"},
    {"LIGHT_DIRECTION_LABEL", "Lysretning : {0,number,#.#} grader"},
    {"LIGHT_DIRECTION_TIPS", "Lyretning relativt til synsretningen"},
    {"LIGHT_ELEVATION_LABEL", "Lysh�yde : {0,number,#.#} grader"},
    {"LIGHT_ELEVATION_TIPS", "Lysh�ydevinkel relativt til synsretningen"},
    {"LIGHT_INTENCITY_TITLE","Lysstyrke"},
    {"LIGHT_DIRECTIONAL_INTENCITY_LABEL","Retningslys : {0,number,#.#}%"},
    {"LIGHT_DIRECTIONAL_INTENCITY_TIPS","Styrke p� retningslys"},
    {"LIGHT_SPECULAR_INTENCITY_LABEL","Glans : {0,number,#.#}%"},
    {"LIGHT_SPECULAR_INTENCITY_TIPS","Styrke p� glans refleksjon"},
    {"LIGHT_AMBIENT_INTENCITY_LABEL","Bakgrunn : {0,number,#.#}%"},
    {"LIGHT_AMBIENT_INTENCITY_TIPS","Styrke p� bakgrunnslys"},

    {"MEASUREMENT_TOOL", "M�leredskap"},
    {"TOTAL_LENGTH", "Total lengde"},
    {"NEXT_LENGTH", "Lengde til neste punkt"},
    {"NEXT_DH", "H�ydeforskjell til neste punkt"},
    {"NEXT_AZ", "Retning til neste punkt"},
    {"CLEAR_LAST", "Slett siste punkt"},
    {"CLEAR_ALL", "Slett alle punkter"},
    
    {"WORLD_TITLE", "Verden"},
    {"GLOBE_SURFACE_TITLE", "Globeflate"},
    {"LAYERED_COVERAGE_TITLE", "Kartlag"},
    {"LAYERED_COVERAGE_SERVER", "Kartserver"},
    {"SIMPLE_COVERAGE_TITLE", "Kartlag"},
    {"SIMPLE_COVERAGE_SERVER", "Kartserver"},
    {"FEATURE_SET_TITLE", "3D objekter"},
    {"ELEVATION_URL_LABEL", "H�ydeserver"},
    {"WMS_URL", "WMS URL"},
    {"WIREFRAME_LABEL", "Tr�dmodell"},
    {"ELEVATION_MULT_LABEL", "H�ydeoverdrivelse : {0,number,#.#}"},
    {"ELEVATION_MULT_TIPS", "Multiplikationsfaktor for h�ydeverdier"},
    {"TRANSPARENCY_LABEL", "Gjennomsiktighet : {0,number,#.#}"},
    {"TRANSPARENCY_TIPS", "Gjennomsiktighet for globeflaten (0 = helt gjennomsiktig, 100 = helt ugjennomsiktig"},

    
    {"GEO_RSS_TITLE", "GeoRSS"},
    
    {"TITLE", "Overskrift"},
    {"DATE", "Dato"},
    {"POSITION", "Posisjon"},
    {"TYPE", "Type"},
    {"SEARCH", "S�k"},
    
    {"OPEN", "�pne"},
    {"FLY_TO", "Fly til dette sted"},
    
    
    {"OK", "Ok"},
    {"APPLY", "Bruk"},
    {"CANCEL", "Avbryt"},
    {"ENABLED", "Aktivisert"},
    {"DATASET_LABEL", "Dataset"},
    {"LAT_LABEL","Breddegrad"},
    {"LON_LABEL","Lengdegrad"},
    {"H_SEA_LABEL","H�yde o. havet"},
    {"H_TERR_LABEL","H�yde o. terreng"},
    {"AZ_LABEL","Kompassretning"},
    {"HA_LABEL","H�ydevinkel"},
    {"FILE_MENU","Fil"},
    {"TOOLS_MENU","Verkt�y"},
    {"HELP_MENU","Hjelp"},
    {"EXIT_MENU","Avslutt"},
    {"MAP_MENU", "�pne oversiktskart"},
    {"MAP_DIALOG", "Oversiktskart"},
    {"RETURN_MENU", "Returner til startpunkt"},
    {"COPY_URL_MENU","Kopier peker for dette utkikkspunkt"},
    {"PASTE_URL_MENU","Lim inn peker for et utkikkspunkt"},
    {"COPY_IMAGE_MENU","Kopier skjermbilde"},
    {"SAVE_IMAGE_MENU","Lagre skjermbilde"},
    {"NO_SAVE_MESSAGE","Kan ikke lagre : {0}"},
    {"NO_OPEN_MESSAGE","Kan ikke �pne : {0}"},
    {"OPEN_WORLD", "�pne verden"},
    {"SAVE_WORLD", "Lagre verden"},
    {"VIEWPOINTS", "Utkikkspunkter"},
    {"SETTINGS", "Innstillinger"},
    {"GRAPHICS", "Grafikk"},
    {"ENVIRONMENT", "Milj�effekter"},
    {"USE_HAZE", "Dis"},
    {"USE_SKY_COLOR", "Himmelfarge"},
    {"USE_COMPRESSED_TEXTURE", "Komprimerte teksturer"},
    {"USE_VBO", "Vertex Buffer Objects"},
    {"MAX_FPS_LABEL","Max bilder pr. sekund"},
    {"TEX_MEM_LABEL","Teksturminne (MB)"},
    {"FLYPATH", "Flyrute"},
    {"RECORD", "Opptak"},
    {"STOP", "Stopp opptak"},
    {"PLAY", "Spill av flyrute"},
    {"LOAD", "Les fil"},
    {"SAVE", "Lagre fil"},
    {"VIDEO", "Ta opp video"},
    {"SAVE_VIDEO", "Video fil..."},
    {"ADD_VIEWPOINT_BUTTON", "Lag"},
    {"DELETE", "Slett"},
    {"COPY", "Kopier"},
    {"CUT", "Klipp ut"},
    {"PASTE", "Lim inn"},
    {"GOTO", "G� til"},
    {"CLOSE", "Lukk"},
    {"VIEWPOINT_NAME_INPUT", "Navn p� utkikkspunkt"},
    {"NAME_LABEL", "Navn"},
    {"TOOL_LEFT_TURN", "Sving til venstre"},
    {"TOOL_RIGHT_TURN", "Sving til h�yre"},
    {"TOOL_ACCELERATE", "Fly, �k hastighet"},
    {"TOOL_DECELERATE", "Fly, reduser hastighet eller g� bakover"},
    {"TOOL_STOP", "Fly, stopp"},
    {"TOOL_ASCEND", "G� h�yere"},
    {"TOOL_DESCEND", "G� lavere"},
    {"TOOL_UP_TURN", "Sving oppover"},
    {"TOOL_DOWN_TURN", "Sving nedover"},
    {"TOOL_GOTO_VIEWPOINT", "G� til utkikkspunkt"},
    {"TOOL_PLAY_FLIGHT_PATH", "Kj�r flyrute"},
    {"TOOL_TOGGLE_WMS_LAYER","Sl� av/p� WMS kartlag"},

    {"TOOL_NORTH_UP","Velg mellom nord opp / synsretning opp"},
    {"TOOL_MAP_SCALE_SLIDER","2D kart skala"},
    
    {"TOOL_DUMP_TO_CLIP", "Kopier 3D vindu til utklippstavle"},
    {"TOOL_SETTINGS", "�pne meny for innstillinger"},
    {"TOOL_HELP", "�pne hjelp vindu"},

    {"TOOL_CACHE", "Innstillinger for bruk av lokalt lager"},
    {"TOOL_GRAPHICS", "Grafikksysteminnstillinger"},
    {"TOOL_ENVIRONMENT", "Milj�effekter"},
    {"TOOL_MAX_FPS", "Begrens oppdateringsraten under flyving for � spare maskinressurser til annet arbeid"},
    {"TOOL_MAX_TEX_MEM", "Begrens mengden av videominne brukt for terrengteksturer"},
    {"TOOL_COMPRESS_TEX", "Bruk komprimerte teksturer. P� de fleste maskiner gir dette en sterk ytelsesforbedring, men gir ogs� litt mindre pene terrengteksturer"},
    {"TOOL_USE_VBO", "Bruk Vertex Buffer Objects. P� de fleste maskiner gir dette en sterk ytelsesforbedring. " +
    "Hvis programmet virker bedre med dette sl�tt av b�r du fors�ke � oppgradere grafikkdriverne."},
    {"TOOL_TEX_FILTER", "Teksturfiltrering: H�yere verdier gir et skarpere bilde, "+
    "spesielt for terreng som blir betraktet fra en skr� vinkel, men kan ogs� redusere ytelsen"},
    {"TOOL_MULTISAMPLE", "Antialiasing (kantutjamning): H�yere verdier gir et glattere bilde, men kan ogs� redusere ytelsen"},
    {"TOOL_ENABLE_CACHE", "Sl� lokalt disklager av/p�. " +
    "Bruk av lokalt disklager gj�r det mulig � komme tilbake til de samme omr�der uten � hente mere data fra serveren. Kan �ke ytelsen ved svak nettverksforbindelse"},
    {"TOOL_CACHE_DIR", "Velg en mappe for lokalt disklager"},
    {"TOOL_HAZE", "Sl� av/p� en svak bl�lig dis for � gi en mere naturlig atmosf�re"},
    {"TOOL_SKY_COLOR", "Sl� av/p� en h�ydeavhengig himmelfarge for � gi en mere naturlig atmosf�re"},

    {"WORLD_TREE_USAGE", "<HTML><BODY>Programmet kan beh�ve noen sekunder " +
    "for � laste data. Hvis grafikkvinduet til h�yre virker uskarpt kan dette "+
    "skyldes at ikke alle detaljer er hentet enn�.<P>" +
    "Musbevegelser for � g� omkring i grafikkvinduet:<DL>" +
    "<DT><b>Venstre mustast:</b><DD>Bevege seg i samme h�yde" +
    "<DT><b>Midtre mustast (hjul som tast):</b><DD>Endre synsretning" +
    "<DT><b>H�yre mustast:</b><DD>Roter synspunktet rundt det du peker p�" +
    "<DT><b>Drei hjul:</b><DD>G� forover og bakover" +
    "</DL><P>" +
    "Taster for � g� omkring i grafikkvinduet:<DL>" +
    "<DT><b>Piltaster:</b><DD>Bevege seg i samme h�yde" +
    "<DT><b>[PageUp]/[PageDown]-tast:</b><DD>G� forover og bakover" +
    "<DT><b>[ctrl]-piltast:</b><DD>Endre synsretning" +
    "</DL><P>" +
    "For � fly:<DL>" +
    "<DT><b>a-tast:</b><DD>Akselerer framover" +
    "<DT><b>z-tast:</b><DD>Brems / akselerer bakover" +
    "<DT><b>[Mellomrom]:</b><DD>Stopp" +
    "</DL>Kombiner med [shift] �ker virkningen av tasten<P>" +
    "Velg et element i datastrukturtreet " +
    "i vinduet over for � komme til " +
    "dataavhengige menyer.</BODY>>/HTML>"},
  };

  public Object[][] getContents() {
    return contents;
  }
}