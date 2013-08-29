/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * A simple action for exiting the application
 * <p>
 *
 * This action can either pass shutdown requests to another part of the
 * application or perform the process itself. If a listener is not
 * registered and the activates this action, it will immediately shut the
 * application down. If a listener is registered then it will pass the
 * notification onwards.
 */
class NewDocAction extends AbstractAction
{
    /** The title of this action */
    private static final String ACTION_TITLE = "New...";

    /** The accelarator key for the action */
    private static final String ACCEL_KEY = "n";

    /** The exit listener for shutdown events. */
    private NewDocListener listener;

    /**
     * Create a new action instance.
     */
    NewDocAction()
    {
        super(ACTION_TITLE);
    }

    /**
     * Set the listener used for this instance. Calling more than once will
     * replace the existing listener with the new instance. Passing a value of
     * null will remove the listener.
     *
     * @param l The new listener instance to use
     */
    void setListener(NewDocListener l)
    {
        listener = l;
    }

    /**
     * Process an action request on this item. Passes the new document request
     * onto the registered listener.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        if(listener != null)
            listener.startNewDocument();
    }
}
