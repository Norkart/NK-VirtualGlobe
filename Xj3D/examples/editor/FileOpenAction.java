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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * A simple action for file open requests.
 * <p>
 *
 * The method in this listener only gets called when a file name it requested
 * can be be opened.
 */
class FileOpenAction extends AbstractAction
{
    /** The title of this action */
    private static final String ACTION_TITLE = "Open...";

    /** The accelarator key for the action */
    private static final String ACCEL_KEY = "o";

    /** The listener registered to get events from this action */
    private FileOpenListener listener;

    /** The file chooser to present to the users */
    private JFileChooser chooser;

    /** The parent component of this one for the dialog */
    private Component parent;

    /**
     * Create a new action instance that uses the given component as a parent
     * for the dialog.
     *
     * @param parent The parent component of the dialog
     */
    FileOpenAction(Component parent)
    {
        super(ACTION_TITLE);

        this.parent = parent;

        setEnabled(false);

        String dir_str = System.getProperty("user.dir");
        File working_dir = new File(dir_str);

        chooser = new JFileChooser(working_dir);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new XMLFileFilter());


    }

    /**
     * Set the listener used for this instance. Calling more than once will
     * replace the existing listener with the new instance. Passing a value of
     * null will remove the listener.
     *
     * @param l The new listener instance to use
     */
    void setListener(FileOpenListener l)
    {
        setEnabled(l != null);
        listener = l;
    }

    /**
     * Process an action request on this item. Will place a dialog in front
     * of the user asking them to select a file to use. If the user does not
     * cancel the dialog it will notify the listener.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        int ret_val = chooser.showOpenDialog(parent);
        if(ret_val == JFileChooser.APPROVE_OPTION)
        {
            File selected = chooser.getSelectedFile();

            try
            {
                if(selected.exists())
                    listener.openFile(selected);
            }
            catch(Exception e)
            {
                System.err.println("Error handling file " + e);
                e.printStackTrace();
            }
        }
    }
}
