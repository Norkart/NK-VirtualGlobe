/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.mobile.nodes.texture;

// Standard imports
import java.awt.image.*;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;

import javax.swing.ImageIcon;

// Application specific imports
import org.j3d.util.ImageUtils;
import org.j3d.texture.TextureCache;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.mobile.nodes.*;
import org.web3d.vrml.renderer.mobile.sg.Texture;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSingleExternalNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLUrlListener;
import org.web3d.vrml.nodes.VRMLContentStateListener;

import org.web3d.vrml.util.URLChecker;
import org.web3d.util.PropertyTools;

/**
 * Mobile implementation of a ImageTexture node.
 * <p>
 *
 * <b>Properties</b>
 * <p>
 * The properties for this file are loaded once on startup so they cannot be
 * initialized per run of loader.
 *
 * The following properties are used by this class:
 * <ul>
 * <li><code>org.web3d.vrml.nodes.loader.minfilter</code> The
 *    filter to use for minification of textures.  Valid values are
 *    "NICEST,FASTEST,BASE_LEVEL_POINT,BASE_LEVEL_LINEAR,FILTER4", </li>
 * <li><code>org.web3d.vrml.nodes.loader.maxfilter</code> The
 *    filter to use for maxification of textures.  Valid values are
 *    "NICEST,FASTEST,BASE_LEVEL_POINT,BASE_LEVEL_LINEAR,LINEAR_SHARPEN,
 *     LINEAR_SHARPEN_RGB,LINEAR_SHARPEN_ALPHA,FILTER4", </li>
 * <li><code>org.web3d.vrml.nodes.loader.texture.rescale</code> The
 *    method to use for rescalling textures.  Valid values are
 *    "NEAREST_NEIGHBOR, BILINEAR"</li>
 *
 * </ul>
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public class MobileImageTexture extends MobileTexture2DNode
	implements VRMLSingleExternalNodeType {
	
	/** Property describing the minification filter to use */
	private static final String MINFILTER_PROP =
		"org.web3d.vrml.nodes.loader.minfilter";
	
	/** The default filter to use for minification. */
	private static final int DEFAULT_MINFILTER = Texture.NICEST;
	
	/** The value read from the system property for MINFILTER */
	private static final int minfilter;
	
	/** Property describing the maxification filter to use */
	private static final String MAGFILTER_PROP =
		"org.web3d.vrml.nodes.loader.maxfilter";
	
	/** The default filter to use for magnification. */
	private static final int DEFAULT_MAGFILTER = Texture.NICEST;
	
	/** The value read from the system property for MAXFILTER */
	private static final int magfilter;
	
	/** Property describing the rescalling method to use */
	private static final String RESCALE_PROP =
		"org.web3d.vrml.nodes.loader.rescale";
	
	/** The default rescale method */
	private static final int DEFAULT_RESCALE =
		AffineTransformOp.TYPE_BILINEAR;
	
	/** The default usetexturecache value */
	private static final boolean DEFAULT_USETEXTURECACHE = true;
	
	/** The value read from the system property for RESCALE */
	private static final int rescale;
	
	/** Property describing the rescalling method to use */
	private static final String USETEXTURECACHE_PROP =
		"org.web3d.vrml.nodes.loader.usetexturecache";
	
	/** The value read from the system property for TEXTURECACHE */
	private static final boolean usetexturecache;
	
	/** Field Index */
	private static final int FIELD_URL = LAST_TEXTURENODETYPE_INDEX + 1;
	
	/** Loaded Index */
	private static final int FIELD_LOADED = LAST_TEXTURENODETYPE_INDEX + 2;
	
	/** Index of the last field of this node */
	private static final int LAST_IMAGETEXTURE_INDEX = FIELD_LOADED;
	
	/** Number of fields constant */
	private static final int NUM_FIELDS = LAST_IMAGETEXTURE_INDEX + 1;
	
	/** Array of VRMLFieldDeclarations */
	private static VRMLFieldDeclaration[] fieldDecl;
	
	/** Hashmap between a field name and its index */
	private static HashMap fieldMap;
	
	// Field values
	
	/** exposedField MFString url [] */
	private String vfURL[];
	
	/** Loaded Var */
	private boolean vfLoaded;
	
	/** URL Load State */
	private int loadState = NOT_LOADED;
	
	/** Class vars for performance */
	private ImageComponent2D image;
	private Texture2D implTex;
	private String worldURL;
	
	// List of those who want to know about Url changes.  Likely 1
	private ArrayList listenerList = new ArrayList(1);
	
	/** Property mapping for Minification and Maxification */
	private static final HashMap minmagMap;
	
	/** Property mapping for rescale */
	private static final HashMap rescaleMap;
	
	/** A map of URL's to their alpha values */
	private static HashMap alphas;
	
	/** List of those who want to know about content state changes. Likely 1 */
	private ArrayList contentListeners;
	
	//----------------------------------------------------------
	// Constructors
	//----------------------------------------------------------
	
	// Static constructor
	static {
		fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
		fieldMap = new HashMap(NUM_FIELDS*3);
		
		fieldDecl[FIELD_REPEATS] =
			new VRMLFieldDeclaration(FieldConstants.FIELD,
			"SFBool",
			"repeatS");
		fieldMap.put("repeatS",new Integer(FIELD_REPEATS));
		
		fieldDecl[FIELD_REPEATT] =
			new VRMLFieldDeclaration(FieldConstants.FIELD,
			"SFBool",
			"repeatT");
		fieldMap.put("repeatT",new Integer(FIELD_REPEATT));
		
		fieldDecl[FIELD_URL] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"MFString",
			"url");
		fieldMap.put("url",new Integer(FIELD_URL));
		fieldMap.put("set_url",new Integer(FIELD_URL));
		fieldMap.put("url_changed",new Integer(FIELD_URL));
		
		// Prototyped isLoaded mechanism
		
		fieldDecl[FIELD_LOADED] =
			new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
			"SFBool",
			"loaded");
		fieldMap.put("loaded",new Integer(FIELD_LOADED));
		fieldMap.put("url_loaded",new Integer(FIELD_LOADED));
		
		// Initialize Property Hashmaps
		minmagMap = new HashMap(8);
		minmagMap.put("NICEST", new Integer(Texture.NICEST));
		minmagMap.put("FASTEST", new Integer(Texture.FASTEST));
		minmagMap.put("BASE_LEVEL_POINT", new Integer(Texture.BASE_LEVEL_POINT));
		minmagMap.put("BASE_LEVEL_LINEAR", new Integer(Texture.BASE_LEVEL_LINEAR));
		minmagMap.put("LINEAR_SHARPEN", new Integer(Texture.LINEAR_SHARPEN));
		minmagMap.put("LINEAR_SHARPEN_RGB", new Integer(Texture.LINEAR_SHARPEN_RGB));
		minmagMap.put("LINEAR_SHARPEN_ALPHA", new Integer(Texture.LINEAR_SHARPEN_ALPHA));
		minmagMap.put("FILTER4", new Integer(Texture.FILTER4));
		
		rescaleMap = new HashMap(2);
		rescaleMap.put("BILINEAR", new Integer(AffineTransformOp.TYPE_BILINEAR));
		rescaleMap.put("NEAREST_NEIGBOR", new Integer(AffineTransformOp.TYPE_NEAREST_NEIGHBOR));
		
		String val;
		Integer i;
		
		minfilter = PropertyTools.fetchSystemProperty(MINFILTER_PROP, DEFAULT_MINFILTER, minmagMap);
		magfilter = PropertyTools.fetchSystemProperty(MAGFILTER_PROP, DEFAULT_MAGFILTER, minmagMap);
		rescale = PropertyTools.fetchSystemProperty(RESCALE_PROP, DEFAULT_RESCALE, rescaleMap);
		usetexturecache = PropertyTools.fetchSystemProperty(USETEXTURECACHE_PROP, DEFAULT_USETEXTURECACHE);
		alphas = new HashMap();
	}
	
	/**
	 * Empty constructor.
	 */
	public MobileImageTexture() {
		super("ImageTexture");
		
		vfURL = FieldConstants.EMPTY_MFSTRING;
		vfLoaded = false;
		contentListeners = new ArrayList();
		
		hasChanged = new boolean[NUM_FIELDS];
	}
	
	/**
	 * Construct a new instance of this node based on the details from the
	 * given node. If the node is not the same type, an exception will be
	 * thrown.
	 *
	 * @param node The node to copy
	 * @throws IllegalArgumentException Incorrect Node Type
	 */
	public MobileImageTexture(VRMLNodeType node) {
		this();
		
		checkNodeType(node);
		
		try {
			int index = node.getFieldIndex("url");
			VRMLFieldData field = node.getFieldValue(index);
			if(field.numElements != 0) {
				vfURL = new String[field.numElements];
				System.arraycopy(field.stringArrayValue,
					0,
					vfURL,
					0,
					field.numElements);
			}
		} catch(VRMLException ve) {
			throw new IllegalArgumentException(ve.getMessage());
		}
	}
	
	//--------------------------------------------------------------
	// Methods required by the VRMLSingleExternalNodeType interface.
	//--------------------------------------------------------------
	
	/**
	 * Ask the state of the load of this node. The value will be one of the
	 * constants defined above.
	 *
	 * @return The current load state of the node
	 */
	public int getLoadState() {
		return loadState;
	}
	
	/**
	 * Set the load state of the node. The value must be one of the constants
	 * defined above.
	 *
	 * @param state The new state of the node
	 */
	public void setLoadState(int state) {
		switch(state) {
		case VRMLSingleExternalNodeType.LOADING :
			//                System.out.println("Loading: " + vfURL[0]);
			break;
		case VRMLSingleExternalNodeType.LOAD_COMPLETE :
			//                System.out.println("Loading complete: " + vfURL[0]);
			break;
		case VRMLSingleExternalNodeType.LOAD_FAILED :
			if (vfURL != null && vfURL.length > 0)
				System.out.println("Loading failed: " + vfURL[0]);
			break;
		case VRMLSingleExternalNodeType.NOT_LOADED:
			break;
		default :
			System.out.println("Unknown state: " + state);
		}
		loadState = state;
		fireContentStateChanged();
	}
	
	/**
	 * Set the URL to a new value. If the value is null, it removes the old
	 * contents (if set) and treats it as though there is no content.
	 *
	 * @param url The list of urls to set or null
	 */
	public void setUrl(String[] newURL, int numValid) {
		if(numValid > 0) {
			if(worldURL != null) {
				vfURL = URLChecker.checkURLs(worldURL, newURL, false);
			} else {
				vfURL = newURL;
			}
		} else {
			vfURL = FieldConstants.EMPTY_MFSTRING;
			implTex = null;
			if (!inSetup)
				fireTextureImplChanged(null,null,null);
		}
		
		hasChanged[FIELD_URL] = true;
		fireFieldChanged(FIELD_URL);
		
		if(!inSetup)
			fireUrlChanged(FIELD_URL);
	}
	
	/**
	 * Set the world URL so that any relative URLs may be corrected to the
	 * fully qualified version. Guaranteed to be non-null.
	 *
	 * @param url The world URL.
	 */
	public void setWorldUrl(String url) {
		
		if((url == null) || (url.length() == 0))
			return;
		
		// check for a trailing slash. If it doesn't have one, append it.
		if(url.charAt(url.length() - 1) != '/') {
			worldURL = url + '/';
		} else {
			worldURL = url;
		}
		
		if(vfURL != null)
			URLChecker.checkURLsInPlace(worldURL, vfURL, false);
	}
	
	/**
	 * Get the world URL so set for this node.
	 *
	 * @return url The world URL.
	 */
	public String getWorldUrl(String url) {
		return worldURL;
	}
	
	/**
	 * Get the list of URLs requested by this node. If there are no URLs
	 * supplied in the text file then this will return a zero length array.
	 *
	 * @return The list of URLs to attempt to load
	 */
	public String[] getUrl() {
		return vfURL;
	}
	
	/**
	 * Check to see if the given MIME type is one that would be supported as
	 * content coming into this node.
	 *
	 * @param mimetype The type to check for
	 * @return true if this is OK, false if not
	 */
	public boolean checkValidContentType(String mimetype) {
		// TODO: Need to implement
		
		return true;
	}
	
	/**
	 * Set the content of this node to the given object. The object is then
	 * cast by the internal representation to the form it needs. This assumes
	 * at least some amount of intelligence on the part of the caller, but
	 * we also know that we should not pass something dumb to it when we can
	 * check what sort of content types it likes to handle. We assume the
	 * loader thread is operating in the same context as the one that created
	 * the node in the first place and thus knows the general types of items
	 * to pass through.
	 *
	 * @param mimetype The mime type of this object if known
	 * @param content The content of the object
	 * @throws IllegalArguementException The content object is not supported
	 */
	public void setContent(String mimetype, Object content)
		throws IllegalArgumentException {
		boolean alpha=false;
		boolean premultAlpha=false;
		BufferedImage buffImage;
		
		// Check the cache before we process this content
		if (vfURL[0] != null) {
			if (cache.checkTexture(vfURL[0]) == true) {
				try {
					implTex = (Texture2D) cache.fetchTexture(vfURL[0]);
					alpha = ((Boolean)alphas.get(vfURL[0])).booleanValue();
					
					// Notify listeners of new impl.
					fireTextureImplChanged(new Texture[] {implTex},
						new boolean[] {alpha}, getTexAttrs());
					
					loadState = LOAD_COMPLETE;
					vfLoaded = true;
					hasChanged[FIELD_LOADED] = true;
					fireFieldChanged(FIELD_LOADED);
					return;
				} catch(IOException io) {
					// ignore and reload
				}
				
			}
		}
		
		if (content == null)
			return;
		
		if (content instanceof BufferedImage) {
			buffImage = (BufferedImage) content;
			
			// Hack for handling ImageLoader problems
			if (mimetype.equals("image/jpeg"))
				alpha = false;
			else if (mimetype.equals("image/png"))
				alpha = true;
			else if (mimetype.equals("image/gif"))
				alpha=false;
			else {
				System.out.println("Unknown type for BufferedImage, assume alpa=false type:" + mimetype);
				alpha = false;
			}
		}
		else if (content instanceof ImageProducer) {
			buffImage = ImageUtils.createBufferedImage((ImageProducer)content);
			
			// Determine Alpha
			ColorModel cm = buffImage.getColorModel();
			
			alpha = cm.hasAlpha();
			premultAlpha = cm.isAlphaPremultiplied();
			
		}
		else {
			System.out.println("Unknown content type: " + content + " URL: " + vfURL[0]);
			implTex = null;
			// Notify listeners of new impl.
			fireTextureImplChanged(new Texture[] {implTex}, new boolean[] {alpha},
				getTexAttrs());
			return;
		}
		
		
		if (premultAlpha) {
			System.out.println("MobileImageTexture: Unhandled case where isAlphaPremultiplied = true");
		}
		
		int texType;
		int format = ImageComponent2D.FORMAT_RGBA;
		
		switch(buffImage.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
		case BufferedImage.TYPE_BYTE_BINARY:
		case BufferedImage.TYPE_INT_BGR:
		case BufferedImage.TYPE_INT_RGB:
			format = ImageComponent2D.FORMAT_RGB;
			break;
			
		case BufferedImage.TYPE_CUSTOM:
			// no idea what this should be, so default to RGBA
		case BufferedImage.TYPE_INT_ARGB:
		case BufferedImage.TYPE_INT_ARGB_PRE:
		case BufferedImage.TYPE_4BYTE_ABGR:
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
			format = ImageComponent2D.FORMAT_RGBA;
			break;
			
		case BufferedImage.TYPE_BYTE_GRAY:
		case BufferedImage.TYPE_USHORT_GRAY:
			format = ImageComponent2D.FORMAT_CHANNEL8;
			break;
			
		case BufferedImage.TYPE_BYTE_INDEXED:
			if (alpha)
				format = ImageComponent2D.FORMAT_RGBA;
			else
				format = ImageComponent2D.FORMAT_RGB;
			
			break;
			
		case BufferedImage.TYPE_USHORT_555_RGB:
			format = ImageComponent2D.FORMAT_RGB5;
			break;
			
		case BufferedImage.TYPE_USHORT_565_RGB:
			format = ImageComponent2D.FORMAT_RGB5;
			break;
		default:
			System.out.println("Unknown FORMAT for image: " + buffImage);
		}
		
		int newWidth = nearestPowerTwo(buffImage.getWidth());
		int newHeight = nearestPowerTwo(buffImage.getHeight());
		
		buffImage = scaleTexture(buffImage, newWidth, newHeight);
		
		try {
			image = new ImageComponent2D(format, buffImage,true,false);
			// These are needed for cacheing
			image.setCapability(ImageComponent.ALLOW_FORMAT_READ);
			image.setCapability(ImageComponent.ALLOW_SIZE_READ);
		} catch (Exception e) {
			System.out.println("Error creating image: " + vfURL[0]);
			e.printStackTrace();
			return;
		}
		
		alphas.put(vfURL[0], new Boolean(alpha));
		createTexture(image, alpha);
		if (usetexturecache) {
			cache.registerTexture(implTex,vfURL[0]);
		}
		
		vfLoaded = true;
		hasChanged[FIELD_LOADED] = true;
		fireFieldChanged(FIELD_LOADED);
	}
	
	/**
	 * Notify the node which URL was used to load the content.  It will be the
	 * complete URI with path, query and references parts.  This method will
	 * be called before setContent.
	 *
	 * @param URI The URI used to load this content
	 */
	public void setLoadedURI(String URI) {
		// Ignore
	}
	
	/**
	 * Add a listener to this node instance. If the listener is already added
	 * or null the request is silently ignored.
	 *
	 * @param ul The listener instance to add
	 */
	public void addUrlListener(VRMLUrlListener ul) {
		if (!listenerList.contains(ul))
			listenerList.add(ul);
	}
	
	/**
	 * Remove a listener from this node instance. If the listener is null or
	 * not registered, the request is silently ignored.
	 *
	 * @param ul The listener to be removed
	 */
	public void removeUrlListener(VRMLUrlListener ul) {
		listenerList.remove(ul);
	}
	
	/**
	 * Add a listener to this node instance for the content state changes. If
	 * the listener is already added or null the request is silently ignored.
	 *
	 * @param l The listener instance to add
	 */
	public void addContentStateListener(VRMLContentStateListener l) {
		if(!contentListeners.contains(l))
			contentListeners.add(l);
	}
	
	/**
	 * Remove a listener from this node instance for the content state changes.
	 * If the listener is null or not registered, the request is silently ignored.
	 *
	 * @param l The listener to be removed
	 */
	public void removeContentStateListener(VRMLContentStateListener l) {
		contentListeners.remove(l);
	}
	
	/**
	 * Send a notification to the registered listeners that a field has been
	 * changed. If no listeners have been registered, then this does nothing,
	 * so always call it regardless.
	 *
	 * @param index The index of the field that changed
	 */
	protected void fireUrlChanged(int index) {
		// Notify listeners of new value
		int num_listeners = listenerList.size();
		VRMLUrlListener ul;
		
		for(int i = 0; i < num_listeners; i++) {
			ul = (VRMLUrlListener)listenerList.get(i);
			ul.urlChanged(this, index);
		}
	}
	
	/**
	 * Send a notification to the registered listeners that the content state
	 * has been changed. If no listeners have been registered, then this does
	 * nothing, so always call it regardless.
	 */
	protected void fireContentStateChanged() {
		// Notify listeners of new value
		int num_listeners = contentListeners.size();
		VRMLContentStateListener csl;
		
		for(int i = 0; i < num_listeners; i++) {
			csl = (VRMLContentStateListener)contentListeners.get(i);
			csl.contentStateChanged(this, FIELD_URL, loadState);
		}
	}
	
	//-------------------------------------------------------------
	// Methods required by the J3DTextureNodeType interface.
	//-------------------------------------------------------------
	/**
	 * Request the Texture objects used to represent the layers of textures
	 * required for this surface. The order is from 0 for the top texture to
	 * the layers in blend order.
	 *
	 * @return An array of Textures to use
	 */
	public Texture[] getTextures() {
		if(implTex == null)
			return null;
		else
			return new Texture[] { implTex };
	}
	
	//----------------------------------------------------------
	// Methods required by the J3DVRMLNodeType interface.
	//----------------------------------------------------------
	
	/**
	 * Get the Java3D scene graph object representation of this node. This will
	 * need to be cast to the appropriate parent type when being used.
	 *
	 * @return The J3D representation.
	 */
	public SceneGraphObject getSceneGraphObject() {
		return implTex;
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
		
		if(vfURL.length > 0) {
			if(cache.checkTexture(vfURL[0]) == true) {
				try {
					implTex = (Texture2D) cache.fetchTexture(vfURL[0]);
					boolean alpha = ((Boolean)alphas.get(vfURL[0])).booleanValue();
					
					// Notify listeners of new impl.
					fireTextureImplChanged(new Texture[] {implTex},
						new boolean[] {alpha}, getTexAttrs());
					
					loadState = LOAD_COMPLETE;
				} catch(IOException io) {
					// ignore and reload
				}
				
			}
		}
		
		inSetup = false;
	}
	
	//----------------------------------------------------------
	// Methods required by the VRMLNode interface.
	//----------------------------------------------------------
	
	/**
	 * Get the index of the given field name. If the name does not exist for
	 * this node then return a value of -1.
	 *
	 * @param fieldName The name of the field we want the index from
	 * @return The index of the field name or -1
	 */
	public int getFieldIndex(String fieldName) {
		Integer index = (Integer) fieldMap.get(fieldName);
		
		return (index == null) ? -1 : index.intValue();
	}
	
	/**
	 * Get the declaration of the field at the given index. This allows for
	 * reverse lookup if needed. If the field does not exist, this will give
	 * a value of null.
	 *
	 * @param index The index of the field to get information
	 * @return A representation of this field's information
	 */
	public VRMLFieldDeclaration getFieldDeclaration(int index) {
		if(index < 0 || index > LAST_IMAGETEXTURE_INDEX)
			return null;
		
		return fieldDecl[index];
	}
	
	/**
	 * Get the number of fields.
	 *
	 * @param The number of fields.
	 */
	public int getNumFields() {
		return fieldDecl.length;
	}
	
	/**
	 * Get the primary type of this node.  Replaces the instanceof mechanism
	 * for use in switch statements.
	 *
	 * @return The primary type
	 */
	public int getPrimaryType() {
		return TypeConstants.VRMLTextureNodeType;
	}
	
	/**
	 * Get the secondary type of this node.  Replaces the instanceof mechanism
	 * for use in switch statements.
	 *
	 * @return The secondary type
	 */
	public int getSecondaryType() {
		return TypeConstants.VRMLSingleExternalNodeType;
	}
	
	/**
	 * Get the value of a field. If the field is a primitive type, it will
	 * return a class representing the value. For arrays or nodes it will
	 * return the instance directly.
	 *
	 * @param index The index of the field to change.
	 * @return The class representing the field value
	 * @throws InvalidFieldException The field index is not known
	 */
	public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
		
		switch(index) {
		case FIELD_URL:
			fieldData.clear();
			fieldData.stringArrayValue = vfURL;
			fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
			fieldData.numElements = vfURL.length;
			break;
			
		case FIELD_LOADED:
			fieldData.clear();
			fieldData.booleanValue = vfLoaded;
			fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
			fieldData.numElements = 1;
			break;
			
		default:
			return(super.getFieldValue(index));
		}
		
		return fieldData;
	}
	
	/**
	 * Send a routed value from this node to the given destination node. The
	 * route should use the appropriate setValue() method of the destination
	 * node. It should not attempt to cast the node up to a higher level.
	 * Routing should also follow the standard rules for the loop breaking and
	 * other appropriate rules for the specification.
	 *
	 * @param time The time that this route occurred (not necessarily epoch
	 *   time. Should be treated as a relative value only)
	 * @param srcIndex The index of the field in this node that the value
	 *   should be sent from
	 * @param destNode The node reference that we will be sending the value to
	 * @param destIndex The index of the field in the destination node that
	 *   the value should be sent to.
	 */
	public void sendRoute(double time,
		int srcIndex,
		VRMLNodeType destNode,
		int destIndex) {
		
		// Simple impl for now.  ignores time and looping
		
		try {
			switch(srcIndex) {
			case FIELD_URL :
				destNode.setValue(destIndex, vfURL, vfURL.length);
				break;
				
			case FIELD_LOADED:
				destNode.setValue(destIndex, vfLoaded);
				break;
				
			default:
				super.sendRoute(time, srcIndex, destNode, destIndex);
			}
		} catch(InvalidFieldException ife) {
			System.err.println("sendRoute: No field!" + ife.getFieldName());
		} catch(InvalidFieldValueException ifve) {
			System.err.println("sendRoute: Invalid field Value: " +
				ifve.getMessage());
		}
	}
	
	/**
	 * Set the value of the field at the given index as a string. This would
	 * be used to set SFString field types.
	 *
	 * @param index The index of destination field to set
	 * @param value The new value to use for the node
	 * @throws InvalidFieldException The index is not a valid field
	 * @throws InvalidFieldValueException The field value is not legal for
	 *   the field specified.
	 */
	public void setValue(int index, String value)
		throws InvalidFieldException, InvalidFieldValueException {
		
		switch(index) {
		case FIELD_URL :
			vfURL = new String[1];
			vfURL[0] = value;
			break;
			
		default:
			super.setValue(index, value);
		}
	}
	
	/**
	 * Set the value of the field at the given index as an array of strings.
	 * This would be used to set MFString field types.
	 *
	 * @param index The index of destination field to set
	 * @param value The new value to use for the node
	 * @throws InvalidFieldException The field index is not know
	 * @throws InvalidFieldValueException The field value is not legal for
	 *   the field specified.
	 */
	public void setValue(int index, String[] value, int numValid)
		throws InvalidFieldException, InvalidFieldValueException {
		
		switch(index) {
		case FIELD_URL:
			setUrl(value, numValid);
			break;
			
		default:
			super.setValue(index, value, numValid);
		}
	}
	
	/**
	 * From the image component format, generate the appropriate texture
	 * format.
	 *
	 * @param comp The image component to get the value from
	 * @return The appropriate corresponding texture format value
	 */
	protected int getTextureFormat(ImageComponent comp)
	{
		int ret_val = Texture.RGB;
		
		switch(comp.getFormat())
		{
		case ImageComponent.FORMAT_CHANNEL8:
			// could also be alpha, but we'll punt for now. We really need
			// the user to pass in this information. Need to think of a
			// good way of doing this.
			ret_val = Texture.LUMINANCE;
			break;
			
		case ImageComponent.FORMAT_LUM4_ALPHA4:
		case ImageComponent.FORMAT_LUM8_ALPHA8:
			ret_val = Texture.LUMINANCE_ALPHA;
			break;
			
		case ImageComponent.FORMAT_R3_G3_B2:
		case ImageComponent.FORMAT_RGB:
		case ImageComponent.FORMAT_RGB4:
		case ImageComponent.FORMAT_RGB5:
			ret_val = Texture.RGB;
			break;
			
		case ImageComponent.FORMAT_RGB5_A1:
			//            case ImageComponent.FORMAT_RGB8:
		case ImageComponent.FORMAT_RGBA:
		case ImageComponent.FORMAT_RGBA4:
			//            case ImageComponent.FORMAT_RGBA8:
			ret_val = Texture.RGBA;
			break;
		}
		
		return ret_val;
	}
	
	/**
	 * Scale a texture.  Generally used to scale a texture to a power of 2.
	 *
	 * @param bi The texture to scale
	 * @param newWidth The new width
	 * @param newHeight The new height
	 */
	private BufferedImage scaleTexture(BufferedImage bi, int newWidth, int newHeight) {
		int width = bi.getWidth();
		int height = bi.getHeight();
		if (width == newWidth && height == newHeight)
			return bi;
		
		System.out.println("Rescaling " + vfURL[0] + " to: " + newWidth + " x "
			+ newHeight);
		
		double xScale = (float)newWidth / (float)width;
		double yScale = (float)newHeight / (float)height;
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		
		AffineTransformOp atop = new AffineTransformOp(at,rescale);
		
		return atop.filter(bi, null);
	}
	
	/**
	 * Given an image component setup the texture for this node.
	 * @param
	 */
	private void createTexture(ImageComponent image, boolean alpha) {
		int texType = getTextureFormat(image);
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		implTex =
			new Texture2D(Texture2D.BASE_LEVEL, texType, width, height);
		
		implTex.setMinFilter(minfilter);
		implTex.setMagFilter(magfilter);
		implTex.setImage(0,image);
		implTex.setCapability(Texture.ALLOW_IMAGE_READ);
		implTex.setCapability(Texture.ALLOW_SIZE_READ);
		
		// Handle repeatS and repeatT fields
		int repeatMode;
		if(vfRepeatS)
			repeatMode = Texture2D.WRAP;
		else
			repeatMode = Texture2D.CLAMP;
		implTex.setBoundaryModeS(repeatMode);
		
		if(vfRepeatT)
			repeatMode = Texture2D.WRAP;
		else
			repeatMode = Texture2D.CLAMP;
		
		implTex.setBoundaryModeT(repeatMode);
		implTex.setCapability(Texture.ALLOW_FORMAT_READ);
		
		// Notify listeners of new impl.
		fireTextureImplChanged(new Texture[] {implTex},
			new boolean[] {alpha}, getTexAttrs());
	}
	
	/**
	 * Get the mode of the current texture.
	 */
	private TextureAttributes[] getTexAttrs() {
		TextureAttributes attrs[] = new TextureAttributes[1];
		attrs[0] = new TextureAttributes();
		
		switch(implTex.getFormat()) {
		case Texture.INTENSITY :
		case Texture.LUMINANCE :
		case Texture.ALPHA :
		case Texture.LUMINANCE_ALPHA :
			attrs[0].setTextureMode(TextureAttributes.MODULATE);
			break;
		default :
			attrs[0].setTextureMode(TextureAttributes.REPLACE);
		}
		
		return attrs;
	}
}
