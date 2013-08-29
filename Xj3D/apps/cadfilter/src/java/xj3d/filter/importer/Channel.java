/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Data binding for Collada <channel> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Channel {
    
    /** source attribute */
    String source;
    
    /** target attribute */
    String target;
    
    /**
     * Constructor
     * 
     * @param channel_element The Element
     */
    Channel(Element channel_element) {
        
        if (channel_element == null) {
            throw new IllegalArgumentException( 
                "Channel: channel_element must be non-null");
            
        } else if (!channel_element.getTagName().equals(ColladaStrings.CHANNEL)) {
            throw new IllegalArgumentException( 
                "Channel: channel_element must be a <channel> Element" );
        }
        source = channel_element.getAttribute(ColladaStrings.SOURCE);
        target = channel_element.getAttribute(ColladaStrings.TARGET);
    }
    
    /**
     * Return the set of Channel objects contained in the NodeList
     *
     * @param channel_list A NodeList of <channel> Elements
     * @return The array of Channel objects corresponding to the argument list
     */
    static Channel[] getChannels(NodeList channel_list) {
        int num_channels = channel_list.getLength();
        Channel[] channel = new Channel[num_channels];
        for (int i = 0; i < num_channels; i++) {
            Element channel_element = (Element)channel_list.item(i);
            channel[i] = new Channel(channel_element);
        }
        return(channel);
    }
}
