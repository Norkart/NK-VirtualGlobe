//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.components.Universe;
import com.norkart.virtualglobe.util.ApplicationSettings;
import java.util.Vector;
import java.util.prefs.Preferences;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.Box;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.Toolkit;
import java.awt.HeadlessException;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
// import com.norkart.virtualglobe.util.Viewpoint;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class ViewpointDialog extends JDialog {
    private JTable table;
    private ViewpointTableModel tableModel;
    private Universe universe;
    private GlobeNavigator navigator;
    
    static final String PREF_VIEWPOINTLIST = "viewpointList";
   
    static final private String OLD_PREF_NODE_NAME = "com/sintef/VirtualGlobe";
    static final private String PREF_NODE_NAME     = "com/norkart/VirtualGlobe";
    
    class ViewpointTableModel extends AbstractTableModel {
        private Vector viewpointList = new Vector();
        
        ViewpointTableModel() {
            load();
        }
        
        private void save() {
            saveNewStyle();
        }
        
        private void saveNewStyle() {
            StringBuffer stbuf = new StringBuffer();
            for (int i=0; i<viewpointList.size(); ++i) {
                Viewpoint v = (Viewpoint)viewpointList.get(i);
                v.save(stbuf);
                stbuf.append("\n");
            }
            
            try {
                Preferences root = Preferences.userRoot();
                Preferences prefs = root.node(PREF_NODE_NAME);
                // Preferences prefs = Preferences.userNodeForPackage(getClass());
                int i;
                for (i=0; i*Preferences.MAX_VALUE_LENGTH < stbuf.length(); ++i) {
                    int end = (i+1)*Preferences.MAX_VALUE_LENGTH;
                    if (end > stbuf.length()) end = stbuf.length();
                    String str = stbuf.substring(i*Preferences.MAX_VALUE_LENGTH, end);
                    prefs.put(PREF_VIEWPOINTLIST+"_"+i, str);
                }
                while (prefs.get(PREF_VIEWPOINTLIST+"_"+i, null) != null) {
                    prefs.remove(PREF_VIEWPOINTLIST+"_"+i);
                    ++i;
                }
            } catch (Exception ex) {
                System.err.print("Error in writing of viewpoint preferences: ");
                System.err.println(ex);
            }
        }
        
        private void saveOldStyle() {
            StringBuffer stbuf = new StringBuffer();
            for (int i=0; i<viewpointList.size(); ++i) {
                Viewpoint v = (Viewpoint)viewpointList.get(i);
                stbuf.append(v.name);
                for (int j=0; j<v.data.length; ++j) {
                    stbuf.append(":");
                    stbuf.append(v.data[j]);
                }
                stbuf.append("\n");
            }
            
            try {
                Preferences prefs = Preferences.userNodeForPackage(getClass());
                String str;
                if (stbuf.length() <= Preferences.MAX_VALUE_LENGTH)
                    str = stbuf.toString();
                else
                    str = stbuf.substring(0, Preferences.MAX_VALUE_LENGTH);
                prefs.put(PREF_VIEWPOINTLIST, str);
            } catch (SecurityException ex) {
                System.err.print("Error in writing of viewpoint preferences: ");
                System.err.println(ex);
            }
        }
        
        private void loadOldStyle(String str) {
            int fromIx = 0;
            while (fromIx < str.length()) {
                int nextIx = str.indexOf("\n", fromIx);
                if (nextIx < fromIx) nextIx = str.length();
                String line = str.substring(fromIx, nextIx);
                String[] tokens = line.split(":");
                if (tokens.length >= 6) {
                    int last_name = tokens.length-1;
                    Viewpoint v = new Viewpoint();
                    for (int i=4; i >= 0; --i, --last_name)
                        v.data[i] = Double.parseDouble(tokens[last_name]);
                    v.name = tokens[0];
                    for (int i=1; i < last_name;  ++i)
                        v.name += ":"+tokens[i];
                    
                    viewpointList.add(v);
                }
                fromIx = nextIx+1;
            }
        }
        
        private void loadNewStyle(String str) {
            int fromIx = 0;
            while (fromIx < str.length()) {
                int nextIx = str.indexOf("\n", fromIx);
                if (nextIx < fromIx) nextIx = str.length();
                String line = str.substring(fromIx, nextIx);
                Viewpoint v = new Viewpoint();
                if (v.parse(line))
                    viewpointList.add(v);
                fromIx = nextIx+1;
            }
        }
        private void load() {
            String str = null;
            Preferences root = Preferences.userRoot();
            try {
                
                if (root.nodeExists(OLD_PREF_NODE_NAME)) {
                Preferences prefs = root.node(OLD_PREF_NODE_NAME); // Preferences.userNodeForPackage(com.sintef.VirtualGlobe.ViewpointDialog.class);
                str = prefs.get(PREF_VIEWPOINTLIST, null);
                if (str != null) {
                    loadOldStyle(str);
                    loadNewStyle(str);
                    prefs.remove(PREF_VIEWPOINTLIST);
                    return;
                }
                }
            } catch (Exception ex) {
                System.err.print("Error in reading of viewpoint preferences: ");
                System.err.println(ex);
            }
            
            try {
                if (root.nodeExists(PREF_NODE_NAME)) {
                Preferences prefs = root.node(PREF_NODE_NAME); //Preferences prefs = Preferences.userNodeForPackage(getClass());
                str = prefs.get(PREF_VIEWPOINTLIST, null);
                if (str != null) {
                    loadOldStyle(str);
                    loadNewStyle(str);
                    prefs.remove(PREF_VIEWPOINTLIST);
                    return;
                }
                }
            } catch (Exception ex) {
                System.err.print("Error in reading of viewpoint preferences: ");
                System.err.println(ex);
            }
            
            try {
                StringBuffer stbuf = new StringBuffer();
                Preferences prefs = root.node(OLD_PREF_NODE_NAME);
                //  Preferences prefs = Preferences.userNodeForPackage(com.sintef.VirtualGlobe.ViewpointDialog.class);
                int i = 0;
                while ((str = prefs.get(PREF_VIEWPOINTLIST+"_"+i, null)) != null) {
                    stbuf.append(str);
                    ++i;
                }
                loadNewStyle(stbuf.toString());
            } catch (SecurityException ex) {
                System.err.print("Error in reading of viewpoint preferences: ");
                System.err.println(ex);
            }
            
            try {
                StringBuffer stbuf = new StringBuffer();
                if (root.nodeExists(PREF_NODE_NAME)) {
                Preferences prefs = root.node(PREF_NODE_NAME); //Preferences prefs = Preferences.userNodeForPackage(getClass());
                int i = 0;
                while ((str = prefs.get(PREF_VIEWPOINTLIST+"_"+i, null)) != null) {
                    stbuf.append(str);
                    ++i;
                }
                loadNewStyle(stbuf.toString());
                }
            } catch (Exception ex) {
                System.err.print("Error in reading of viewpoint preferences: ");
                System.err.println(ex);
            }
        }
        
        public String getColumnName(int col) {
            ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
            switch (col) {
                case 0:
                    return settings.getResourceString("NAME_LABEL");
                case 1:
                    return settings.getResourceString("DATASET_LABEL");
                case 2:
                    return settings.getResourceString("LON_LABEL");
                case 3:
                    return settings.getResourceString("LAT_LABEL");
                case 4:
                    return settings.getResourceString("H_SEA_LABEL");
                case 5:
                    return settings.getResourceString("AZ_LABEL");
                case 6:
                    return settings.getResourceString("HA_LABEL");
            }
            return "";
        }
        public int getRowCount() { return viewpointList.size(); }
        public int getColumnCount() { return 7; }
        public Object getValueAt(int row, int col) {
            Viewpoint v = (Viewpoint)viewpointList.get(row);
            if (col == 0) return v.name;
            if (col == 1) return v.dataset == null ? "" : v.dataset.toString();
            return new Double(v.data[col-2]);
        }
        public boolean isCellEditable(int row, int col)  {
            return true;
        }
        public void setValueAt(Object value, int row, int col) {
            Viewpoint v = (Viewpoint)viewpointList.get(row);
            if (col == 0)
                v.name = (String)value;
            else if (col == 1) {
                v.dataset = null;
                if (value != null) {
                    try {
                        v.dataset = new URL((String)value);
                    } catch (MalformedURLException ex) {}
                }
            } else if (value instanceof Number) {
                v.data[col-2] = ((Number)value).doubleValue();
            } else if (value instanceof String) {
                v.data[col-2] =  Double.parseDouble((String)value);
            } else
                return;
            
            
            fireTableCellUpdated(row, col);
            save();
        }
        
        public void addViewpoint(Viewpoint viewpoint, int index) {
/*
      String name = viewpoint.name;
      Viewpoint v = null;
      int index = -1;
      boolean found = false;
      boolean addedRow = false;
 
      if (name != null) {
        int i = 0;
        while (!found && (i < viewpointList.size())) {
          v = (Viewpoint)viewpointList.elementAt(i);
          if (v.name == name) {
            found = true;
            index = i;
          } else {
            i++;
          }
        }
      }
      if (found) return;
 
      if (found) { //update old
        viewpointList.setElementAt(viewpoint, index);
        fireTableRowsUpdated(index, index);
      }
      else { //add new
 */
            for (int i=0; i<viewpointList.size(); ++i) {
                Viewpoint v = (Viewpoint)viewpointList.elementAt(i);
                if (v.dataset != null &&
                        v.dataset.equals(viewpoint.dataset) &&
                        v.isSamePoint(viewpoint))
                    return;
            }
            if (index < 0 || index > viewpointList.size())
                index = viewpointList.size();
            viewpointList.insertElementAt(viewpoint, index);
            fireTableRowsInserted(index, index);
            //}
            save();
        }
        
        public void deleteRows(int from, int count) {
            for (int i=0; i<count; ++i)
                viewpointList.remove(from);
            this.fireTableRowsDeleted(from, from+count);
            save();
        }
        
        public Viewpoint getViewpoint(int index) {
            return (Viewpoint)viewpointList.get(index);
        }
    }
    
    public ViewpointDialog(JFrame frame, Universe u, GlobeNavigator n) throws HeadlessException {
        super(frame, ApplicationSettings.getApplicationSettings().getResourceString("VIEWPOINTS"));
        ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        universe = u;
        navigator = n;
        
        tableModel = new ViewpointTableModel();
        
        Box box = Box.createVerticalBox();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        // table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        box.add(scrollPane);
        
        box.add(new JSeparator());
        
        Box button_box = Box.createHorizontalBox();
        // Create buttons
        JButton add_button = new JButton(settings.getResourceString("ADD_VIEWPOINT_BUTTON"));
        add_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                Viewpoint v = new Viewpoint();
                v.setLon(navigator.getLon());
                v.setLat(navigator.getLat());
                v.setH(navigator.getEllipsHeight());
                v.setHa(navigator.getHeightAngle());
                v.setAz(navigator.getAzimut());
               
                v.dataset = universe.getUrl();
                v.name = JOptionPane.showInputDialog(ViewpointDialog.this, ApplicationSettings.getApplicationSettings().getResourceString("VIEWPOINT_NAME_INPUT"));
                if (v.name != null)
                    tableModel.addViewpoint(v, table.getSelectedRow());
            }
        });
        button_box.add(add_button);
        
        JButton delete_button = new JButton(settings.getResourceString("DELETE"));
        delete_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                tableModel.deleteRows(row, table.getSelectedRowCount());
            }
        });
        button_box.add(delete_button);
        
        JButton copy_button = new JButton(settings.getResourceString("COPY"));
        copy_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                ApplicationSettings as = ApplicationSettings.getApplicationSettings();
                int count = table.getSelectedRowCount();
                StringBuffer stbuf = new StringBuffer();
                for (int i = row; i < row+count; ++i) {
                    Viewpoint v = (Viewpoint)tableModel.viewpointList.get(i);
                    stbuf.append(as.getGlobeStarterURLString());
                    v.save(stbuf);
                    stbuf.append("\n");
                }
                Clipboard clipboard =  Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection data = new StringSelection(stbuf.toString());
                clipboard.setContents(data, data);
            }
        });
        button_box.add(copy_button);
        
        JButton cut_button = new JButton(settings.getResourceString("CUT"));
        cut_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                ApplicationSettings as = ApplicationSettings.getApplicationSettings();
                int count = table.getSelectedRowCount();
                StringBuffer stbuf = new StringBuffer();
                for (int i = row; i < row+count; ++i) {
                    Viewpoint v = (Viewpoint)tableModel.viewpointList.get(i);
                    stbuf.append(as.getGlobeStarterURLString());
                    v.save(stbuf);
                    stbuf.append("\n");
                }
                Clipboard clipboard =  Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection data = new StringSelection(stbuf.toString());
                clipboard.setContents(data, data);
                tableModel.deleteRows(row, count);
            }
        });
        button_box.add(cut_button);
        
        
        JButton paste_button = new JButton(settings.getResourceString("PASTE"));
        paste_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                Clipboard clipboard =  Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable data = clipboard.getContents(this);
                if (data == null) return;
                String str = null;
                try {
                    str = (String)data.getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException ex) {} catch (IOException ex) {}
                if (str == null) return;
                int fromIx = 0;
                int row = table.getSelectedRow();
                while (fromIx < str.length()) {
                    int nextIx = str.indexOf("\n", fromIx);
                    if (nextIx < fromIx) nextIx = str.length();
                    String line = str.substring(fromIx, nextIx);
                    Viewpoint v = new Viewpoint();
                    if (v.parse(line)) {
                        tableModel.addViewpoint(v, row);
                        if (row >= 0) ++row;
                    }
                    fromIx = nextIx+1;
                }
            }
        });
        button_box.add(paste_button);
        
        JButton goto_button = new JButton(settings.getResourceString("GOTO"));
        goto_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                Viewpoint v = tableModel.getViewpoint(row);
                if (v.dataset != null)
                    universe.setDataSet(v.dataset);
                navigator.gotoViewpoint(v.data[0], v.data[1], v.data[2], v.data[3], v.data[4]);
            }
        });
        button_box.add(goto_button);
        button_box.add(Box.createHorizontalGlue());
        
        JButton close_button = new JButton(settings.getResourceString("CLOSE"));
        close_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
            }
        });
        button_box.add(close_button);
        box.add(button_box);
        
        getContentPane().add(box);
        pack();
    }
    
    public void addViewpoint(Viewpoint v) {
        tableModel.addViewpoint(v, -1);
    }
}