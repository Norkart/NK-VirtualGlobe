/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Listing of constants relating to load states and the SAILoadState type.
 * <p>
 *
 * These constants will be returned by X3DExternProtoDeclares's
 * {@link X3DExternProtoDeclaration#getLoadState()}, method.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface X3DLoadStateTypes {

    /** The loading has not yet started state */
    public static final int LOAD_NOT_STARTED = 1;

    /** The loading has begun, but not yet finished */
    public static final int LOAD_IN_PROGRESS = 2;

    /** The loading has successfully completed. */
    public static final int LOAD_COMPLETED = 3;

    /** The loading has failed to complete for some reason */
    public static final int LOAD_FAILED = 4;

}
