/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

// Standard imports

import java.net.*;

// Application specific imports

import mil.navy.nps.net.*;
import mil.navy.nps.dis.*;

/**
 * Dis helper class for Hashmap usage.  Holds fields needed for uniqueness in
 * DIS packects, SiteID, ApplicationID, and EntityID.
 *
 * Implements hashcode and equals.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class DISId implements Cloneable {
    protected int siteID;
    protected int applicationID;
    protected int entityID;
    private int hash;

    public DISId(int siteID, int applicationID, int entityID) {
        this.siteID = siteID;
        this.applicationID = applicationID;
        this.entityID = entityID;

        // Now regenerate the hash code
        long h = 0;

        h = 31 * h + siteID;
        h = 31 * h + applicationID;
        h = 31 * h + entityID;

        hash = (int)(h & 0xFFFFFFFF);
    }

    /**
     * Set the value of this id.
     */
    public void setValue(int siteID, int applicationID, int entityID) {
        this.siteID = siteID;
        this.applicationID = applicationID;
        this.entityID = entityID;

        // Now regenerate the hash code
        long h = 0;

        h = 31 * h + siteID;
        h = 31 * h + applicationID;
        h = 31 * h + entityID;

        hash = (int)(h & 0xFFFFFFFF);
    }

    /**
     * Calculate the hashcode for this object.
     *
     */
    public int hashCode()
    {
        return hash;
    }

    public boolean equals(Object o)
    {
        if(!(o instanceof DISId))
            return false;

        DISId id = (DISId)o;

        if (id.siteID != siteID || id.applicationID != applicationID ||
            id.entityID != entityID)
            return false;

        return true;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("DISId(");
        buff.append(hash);
        buff.append(") siteID: ");
        buff.append(siteID);
        buff.append(" appID: ");
        buff.append(applicationID);
        buff.append(" entityID: ");
        buff.append(entityID);

        return buff.toString();
    }

    /**
     * Clone method.
     *
     * @return A cloned object.
     */
    public Object clone() {
        DISId id = new DISId(siteID, applicationID, entityID);
        return id;
    }
}