package org.web3d.vrml.scripting.external.buffer;

/** ExternalOutputBufferReclaimer is used to reclaim ExternalOutputBuffer instances
  * so that they may be reused for future event broadcasts.
  */

public interface ExternalOutputBufferReclaimer {

    /** Reclaim an ExternalOutputBuffer for reuse
      * The purpose of the tag parameter is specific to the particular
      * buffer reclaimer implementation but is likely to be used to
      * identify where the buffer goes.
      * @param buffer The buffer to reclaim
      * @param tag A numeric tag for the receiver's use */
    public void reclaimEventOutBuffer(
        ExternalOutputBuffer buffer,
        int tag
    );

}
