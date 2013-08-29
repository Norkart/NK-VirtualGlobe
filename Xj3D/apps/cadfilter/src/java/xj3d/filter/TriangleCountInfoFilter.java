/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// External imports
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.VRMLException;

/**
 * Counts the number of triangles in a world.
 * <p>
 *
 * Right now this code assumes the IFS is all triangles.  Later versions will
 * actually triangulate the code for you.  Must have trailing -1 on coordIndex
 * Doesn't handle USE coordinates or binary content paths.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class TriangleCountInfoFilter extends AbstractFilter {
    /** A stack of node names */
    private SimpleStack nodeStack;

    /** A stack of field names */
    private SimpleStack fieldStack;

    /** A stack of field values */
    private SimpleStack fieldValuesStack;

    /** A stack of def names */
    private SimpleStack defStack;

    /** A list of current defnames and URL's.  Non ImageTextures will be null. */
    private HashSet defNames;

    /** A mapping of urls to field values */
    private HashMap urlMap;

    /** A mapping of urls to def names */
    private HashMap urlDefMap;

    /** Are we inside an IndexedFaceSet */
    private boolean insideIFS;

    /** The triangle count */
    private int triCnt;

    public TriangleCountInfoFilter() {
        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        fieldValuesStack = new SimpleStack();
        defStack = new SimpleStack();
        defNames = new HashSet();
        urlMap = new HashMap();
        urlDefMap = new HashMap();
        insideIFS = false;

        triCnt = 0;
    }

    //----------------------------------------------------------
    // ContentHandler methods
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endDocument() throws SAVException, VRMLException {

        defNames.clear();
        urlMap.clear();
        urlDefMap.clear();

        errorHandler.messageReport("Total Triangle Cnt: " + triCnt);
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (name.equals("IndexedFaceSet")) {
            insideIFS = true;
            fieldValuesStack.push(new HashMap());
            if (defName != null)
                defNames.add(defName);

            defStack.push(defName);
            nodeStack.push(name);

            return;
        }

        if (defName != null)
            defNames.add(defName);

        defStack.push(defName);
        nodeStack.push(name);

        if (insideIFS)
            return;
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endNode() throws SAVException, VRMLException {
        String nodeName = (String) nodeStack.pop();
        String defName = (String) defStack.pop();
        HashMap fieldValues;

        if (nodeName.equals("IndexedFaceSet")) {
            insideIFS = false;
            fieldValues = (HashMap) fieldValuesStack.pop();

            // Issue all other ITS fields

            String[] coord = (String[]) fieldValues.get("Coordinate.point");
            String[] coordIndex = (String[]) fieldValues.get("IndexedFaceSet.coordIndex");

            if (coord == null || coordIndex == null) {
                return;
            }

            int len = coordIndex.length / 4;

            triCnt += len;
        }

        if (insideIFS)
            return;
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startField(String name) throws SAVException, VRMLException {
        fieldStack.push(name);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void useDecl(String defName) throws SAVException, VRMLException {
        fieldStack.pop();
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endField() throws SAVException, VRMLException {
        fieldStack.pop();
    }

    //-----------------------------------------------------------------------
    //Methods for interface RouteHandler
    //-----------------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.pop();
        String nodeName = (String) nodeStack.peek();

        if (insideIFS) {
            HashMap fieldValues = (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }
    }

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String value) throws SAVException, VRMLException {
        String fieldName = (String) fieldStack.pop();
        String nodeName = (String) nodeStack.peek();

        if (insideIFS) {
            HashMap fieldValues = (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }
    }
}
