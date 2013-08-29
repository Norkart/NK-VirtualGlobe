package org.web3d.vrml.scripting.external.buffer;

import org.web3d.vrml.nodes.VRMLNodeType;

/** ExternalOutputBuffer are used in buffering the event data when sending
  * updates to the user.  Rather than have the EventAdapter have to know
  * about the myriad eventOut types, it just deals with them as EventOut
  * instances that happen to implement ExternalOutputBuffer.
  */

public interface ExternalOutputBuffer {

    /** Initialize for a new field
      * @param srcNode The underlying node which is associated with the event.
      * @param fieldNumber The field of the underlying node.
      */
    public void initialize(VRMLNodeType srcNode, int fieldNumber);

    /** Load the current value from the underlying implementation. */
    public void loadOutputValue();

    /** Lose the stored value to save on references */
    public void reset();

}
