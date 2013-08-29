package org.web3d.vrml.scripting.external.eai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.*;

/**
 *  EventOutSFImageWrapper provides the functional implementation of
 *  EventOutSFImage for the SimpleBrowser class.  The functionality of this 
 *  class is specified in the EAI 2.0 specification.
 * <P>
 * See VRML97 Specification, section 5.5 for details of the data encoding
 * used for this data type.  The abreviated explaination is that it is
 * width height num-components pixel-values.
 */

class EventOutSFImageWrapper extends EventOutSFImage 
implements ExternalOutputBuffer, EventWrapper {
    /** Indicates whether to load the value locally or retrieve from node */
    boolean isStored;

    /** The stored pixes if isStored is true.  Unused otherwise. */
    int[] storedPixels;

    /** The stored height if isStored is true.  Unused otherwise. */
    int storedHeight;

    /** The stored width if isStored is true.  Unused otherwise. */
    int storedWidth;

    /** The stored components if isStored is true.  Unused otherwise. */
    int storedComponents;

    /** Used to get the ExternalEventAdapter. */
    EAIEventAdapterFactory theEventAdapterFactory;

    /** The underlying Node implementation uses unique integer field ID's */
    int theFieldID;

    /** Reference to the underlying Node implementation */
    VRMLNodeType theNode;

    /** Construct the EventOut wrapper instance.
      * @param buffer Should the value be loaded immediately.
      * @param aNode  The underlying VRMLNodeType instance.
      * @param ID     The field ID on the underlying node.
      * @param anAdapterFactory The Event Adapter factory.
      */

    EventOutSFImageWrapper(
        VRMLNodeType aNode, int ID, EAIEventAdapterFactory anAdapterFactory, 
        boolean buffer
    ) {
        fieldType=SFImage;
        theNode=aNode;
        theFieldID=ID;
        theEventAdapterFactory=anAdapterFactory;
        isStored=buffer;
        if (isStored)
            loadOutputValue();
    }

    /** @see vrml.eai.field.EventOut#addVrmlEventListener */
    public void addVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).addListener(
            theFieldID,listener
        );
    }

    /** Two eventOut's are equal if they are connected to the same actual 
      * node and field.
      * @param other The object to compare against.
      */
    public boolean equals(Object other) {
        if (other == null)
            return false;
        else if (other instanceof EventWrapper) {
            EventWrapper otherWrapper=(EventWrapper)other;
            return (
                otherWrapper.getFieldNode()==theNode && 
                otherWrapper.getFieldID()==theFieldID && 
                otherWrapper.getType()==getType()
            );
        } else
            return super.equals(other);
    }

    /** @see vrml.eai.field.EventOutSFImage#getComponents */
    public int getComponents() {
        if (isStored)
            return storedComponents;
        else {
            /* The actual number of components is supposed to be the 
             * third integer */
            try {
            	VRMLFieldData value=theNode.getFieldValue(theFieldID);
            	if (value.intArrayValue==null)
            		return 0;
            	else
            		return value.intArrayValue[2];
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting value.",ife);
            } catch (NullPointerException npe) {
                throw new RuntimeException(
                    "NullPointerException Error getting value.",npe
                );
            }
        }
    }

    /** @see vrml.eai.field.EventOutSFImage#getHeight */
    public int getHeight() {
        if (isStored)
            return storedHeight;
        else {
            /* The height of the image is supposed to be the second integer */
            try {
            	VRMLFieldData value=theNode.getFieldValue(theFieldID);
            	if (value.intArrayValue==null)
            		return 0;
            	else
            		return value.intArrayValue[1];
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting value.");
            } catch (NullPointerException npe) {
                throw new RuntimeException(
                    "NullPointerException Error getting value."
                );
            }
        }
    }

    /** The underlying field ID 
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldID
      */
    public int getFieldID() {
        return theFieldID;
    }
  
    /** The underlying implementation node. 
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldNode
      */
    public VRMLNodeType getFieldNode() {
        return theNode;
    }

    /** @see vrml.eai.field.EventOut#getUserData */
    public Object getUserData() {
        try {
            return theNode.getUserData(theFieldID);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error getting user data");
        }
    }

    /** @see vrml.eai.field.EventOutSFImage#getWidth */
    public int getWidth() {
        if (isStored) {
            return storedWidth;
        } else {
            /* The width of the image is supposed to be the first integer */
            try {
            	VRMLFieldData value=theNode.getFieldValue(theFieldID);
            	if (value.intArrayValue==null)
            		return 0;
            	else
            		return value.intArrayValue[0];
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting value.");
            } catch (NullPointerException npe) {
                throw new RuntimeException(
                    "NullPointerException Error getting value."
                );
            }
        }
    }

    /** @see vrml.eai.field.EventOutSFImage#getPixels */
    public void getPixels(int[] dest) {
        if (isStored) {
            System.arraycopy(
                storedPixels,0,dest,0,storedPixels.length
            );
        } else {
            /* The actual pixels for the image should be everything but */
            /* the first three integers. */
            try {
                VRMLFieldData imageData=theNode.getFieldValue(theFieldID);
                System.arraycopy(
                    imageData.intArrayValue,3,dest,0,imageData.numElements-3
                );
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting value.");
            }
        }
    }

    /** @see vrml.eai.field.EventOutSFImage#getPixels */
    public int[] getPixels() {
        /* The actual pixels for the image should be everything but the first
         * three integers. */
        if (isStored) {
            int pixels[]=new int[storedPixels.length];
            System.arraycopy(
                storedPixels,0,pixels,0,storedPixels.length
            );
            return pixels;
        } else {
            try {
                VRMLFieldData imageData=theNode.getFieldValue(theFieldID);
                if (imageData.numElements<3)
                	return new int[0];
                int pixels[]=new int[imageData.numElements-3];
                System.arraycopy(
                    imageData.intArrayValue,3,pixels,0,imageData.numElements-3
                );
                return pixels;
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting value.");
            }
        }
    }

	/** Since the equals implementation is given by the spec,
	 *  it is implied that hashCode is defined so that equal
	 *  items have equal hash codes.
	  * @see java.lang.Object#hashCode
	 **/
	public int hashCode() {
		return theNode.hashCode()+theFieldID;
	}

    /** @see ExternalOutputBuffer#loadOutputValue */
    public void loadOutputValue() {
        // Should we be using the supposed image size, or the amount of
        // data the user actually supplied?
        try {
            VRMLFieldData imageData=theNode.getFieldValue(theFieldID);
            int newWidth=imageData.intArrayValue[0];
            int newHeight=imageData.intArrayValue[1];
            if (
                storedPixels==null || 
                newWidth!=storedWidth || 
                newHeight!=storedHeight
            ) {
                // Unable to reuse old storage
                storedPixels=new int[imageData.numElements-3];
                storedHeight=newHeight;
                storedWidth=newWidth;
            }
            storedComponents=imageData.intArrayValue[2];
            if (storedHeight!=0 & storedWidth!=0) {
                System.arraycopy(
                    imageData.intArrayValue,3,storedPixels,0,
                    storedPixels.length
                );
            } else {
                storedPixels=new int[0];
            }
        } catch (InvalidFieldException ife) {
            throw new RuntimeException("Error getting stored field value.");
        }
    }


    /** @see vrml.eai.field.EventOut#removeVrmlEventListener */
    public void removeVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).removeListener(
            theFieldID,listener
        );
    }

    /** @see vrml.eai.field.EventOut#setUserData */
    public void setUserData(Object data) {
        try {
            theNode.setUserData(theFieldID,data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

    /** Re-initialize the buffer so that it can service another node
      * @param aNode The new underlying node
      * @param ID The new field ID
      */
    public void initialize(VRMLNodeType aNode, int ID) {
        theNode=aNode;
        theFieldID=ID;
    }

    /** Clear out any stored VRMLNodeType references */
    public void reset() {
        theNode=null;
        theFieldID=-1;
        isStored=false;
    }

}
