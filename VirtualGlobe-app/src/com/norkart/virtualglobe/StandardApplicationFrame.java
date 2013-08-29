//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe;

import com.norkart.virtualglobe.ApplicationFrame;
import com.norkart.virtualglobe.components.WorldComponent;
import com.norkart.virtualglobe.util.ApplicationSettings;
import java.io.*;

import java.util.Iterator;
import java.util.HashMap;

import java.text.MessageFormat;

import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.EtchedBorder;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import com.norkart.virtualglobe.gui.ImageSelection;
import com.norkart.virtualglobe.gui.ImageFileFilter;

import com.norkart.virtualglobe.viewer.navigator.GlobeNavigator;
import com.norkart.virtualglobe.viewer.PerspectiveCamera;
import com.norkart.virtualglobe.viewer.PerspectiveCameraPanel;
import com.norkart.virtualglobe.gui.*;
import com.norkart.virtualglobe.components.DataTreeNode;

import com.sun.opengl.util.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class StandardApplicationFrame extends ApplicationFrame  {
    
    JSplitPane  dataSplitPane = new JSplitPane();
    JDialog cameraDialog;
    JDialog lightDialog;
    JDialog measureDialog;
    
    GlobeNavigatorPanel navPanel;
    
    //JPanel      mapPane = new JPanel();
    
    // JTabbedPane systemPane = new JTabbedPane();
    DataTree       dataTree;
    JPanel      dataViewer = new JPanel(new CardLayout());
    
    StandardApplicationFrame() {
        super();
        
        final ApplicationSettings settings = ApplicationSettings.getApplicationSettings();
        
        dataTree = new DataTree(universe.getDataTreeModel());
          
        dataSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        // mapPane.setMinimumSize(new Dimension(100, 100));
        //mapPane.setPreferredSize(new Dimension(200, 200));
        dataTree.setMinimumSize(new Dimension(100, 100));
        dataTree.setPreferredSize(new Dimension(200, 200));
        dataViewer.setMinimumSize(new Dimension(100, 100));
        dataViewer.setPreferredSize(new Dimension(200, 200));
        
        dataSplitPane.setTopComponent(new JScrollPane(dataTree));
        dataSplitPane.setBottomComponent(dataViewer);
        // mapDataSplitPane.setTopComponent(mapPane);
        mapDataSplitPane.setBottomComponent(dataSplitPane);
        
        // Connect data objects to GUI
        dataTree.addTreeSelectionListener(new TreeSelectionListener() {
            HashMap<String, Component> dataPanels = new HashMap();
            
            // TreeSelectionListener
            public void valueChanged(TreeSelectionEvent e) {
                DataTreeNode node = (DataTreeNode)dataTree.getLastSelectedPathComponent();
                if (node == null) return;
                Component c = dataPanels.get(node.toString());
                if (c == null) {
                    c = new JScrollPane(node.getInfoPanel());
                    dataPanels.put(node.toString(), c);
                    dataViewer.add(c, node.toString());
                    ((CardLayout)dataViewer.getLayout()).addLayoutComponent(c, node.toString());
                }
                ((CardLayout)dataViewer.getLayout()).show(dataViewer, node.toString());
            }
        });
        universe.getDataTreeModel().addTreeModelListener(new TreeModelListener() {
            public void	treeNodesChanged(TreeModelEvent e) {
            }
            public void	treeNodesInserted(TreeModelEvent e) {
                dataTree.expandPath(e.getTreePath());
            }
            public void	treeNodesRemoved(TreeModelEvent e) {
            }
            public void	treeStructureChanged(TreeModelEvent e) {
                dataTree.expandPath(e.getTreePath());
            }
        });
        dataTree.expandAll(true);
        
        // Initial data viewer help
        JEditorPane ta = new JEditorPane();
        ta.setEditable(false);
        ta.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        ta.setContentType("text/html");
        ta.setText(settings.getResourceString("WORLD_TREE_USAGE"));
        dataViewer.add(new JScrollPane(ta), "Usage");
        
        // Create system settings tabs
        // systemPane.addTab(settings.getResourceString("NAVIGATOR_TAB"), new JScrollPane(camera.getNavigator().getPanel()));
        // systemPane.addTab(settings.getResourceString("CAMERA_TAB"), new JScrollPane(camera.getCameraPanel()));
        //systemPane.addTab(settings.getResourceString("LIGHT_TAB"), new JScrollPane(camera.getLightPanel()));
        
        navPanel = new GlobeNavigatorPanel(perspectiveCamera.getNavigator());
        
        graphicView.add(navPanel, BorderLayout.SOUTH);
        
        // Menu bar
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        final JMenu fileMenu = new JMenu(settings.getResourceString("FILE_MENU"));
        
        final OpenWorldDialog openWorld = new OpenWorldDialog(this, universe);
        JMenuItem openMi = new JMenuItem(settings.getResourceString("OPEN_WORLD")+"...");
        openMi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openWorld.setLocationRelativeTo(fileMenu);
                openWorld.setListModel(universe.getLastDataSets());
                openWorld.pack();
                openWorld.setVisible(true);
            }
        });
        fileMenu.add(openMi);
        
        final JFileChooser fileChooser = new JFileChooser();
        JMenuItem saveMi = new JMenuItem(settings.getResourceString("SAVE_WORLD")+"...");
        saveMi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showSaveDialog(StandardApplicationFrame.this) != JFileChooser.APPROVE_OPTION)
                    return;
                
                try {
                    Document document = universe.getApplicationLoader().newDocument();
                    Element vgml = document.createElement("vgml");
                    vgml.appendChild(universe.save(document));
                    document.appendChild(vgml);
                    Transformer transformer =     TransformerFactory.newInstance().newTransformer();
                    DOMSource xmlSource = new DOMSource(document);
                    StreamResult result = new StreamResult(fileChooser.getSelectedFile());
                    transformer.transform(xmlSource , result );
                } catch (TransformerException ex) {
                    ex.printStackTrace();
                }
            }
        });
        fileMenu.add(saveMi);
        fileMenu.addSeparator();
        
        JMenuItem exitMi = new JMenuItem(settings.getResourceString("EXIT_MENU"));
        exitMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processEvent(new WindowEvent(StandardApplicationFrame.this, WindowEvent.WINDOW_CLOSING));
                System.exit(0);
            }
        });
        fileMenu.add(exitMi);
        menuBar.add(fileMenu);
        
        // Tools menu
        final JMenu toolsMenu = new JMenu(settings.getResourceString("TOOLS_MENU"));
        /*
        JMenuItem mapMi = new JMenuItem(settings.getResourceString("MAP_MENU"));
        mapMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mapDialog == null)
                    mapDialog = new MapDialog(StandardApplicationFrame.this, universe, camera);
                // mapDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                mapDialog.setLocationRelativeTo(toolsMenu);
                mapDialog.setVisible(true);
            }
        });
        toolsMenu.add(mapMi);
         */
        JMenuItem cameraMi = new JMenuItem(settings.getResourceString("CAMERA_MENU")+"...");
        cameraMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cameraDialog == null) {
                    cameraDialog = new JDialog(StandardApplicationFrame.this, settings.getResourceString("CAMERA_MENU"));
                    cameraDialog.getContentPane().add(new PerspectiveCameraPanel(perspectiveCamera), BorderLayout.CENTER);
                    cameraDialog.pack();
                }
                // mapDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                cameraDialog.setLocationRelativeTo(toolsMenu);
                cameraDialog.setVisible(true);
            }
        });
        toolsMenu.add(cameraMi);
        
        JMenuItem lightMi = new JMenuItem(settings.getResourceString("LIGHT_MENU")+"...");
        lightMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lightDialog == null) {
                    lightDialog = new JDialog(StandardApplicationFrame.this, settings.getResourceString("LIGHT_MENU"));
                    lightDialog.getContentPane().add(perspectiveCamera.getViewerManager().getLightModel().getLightPanel(), BorderLayout.CENTER);
                    lightDialog.pack();
                }
                // mapDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                lightDialog.setLocationRelativeTo(toolsMenu);
                lightDialog.setVisible(true);
            }
        });
        toolsMenu.add(lightMi);
        
        final MeasurePanel measurePanel = new MeasurePanel(perspectiveCamera.getNavigator());
        JMenuItem measureMi = new JMenuItem(settings.getResourceString("MEASUREMENT_TOOL")+"...");
        measureMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (measureDialog == null) {
                    measureDialog = new JDialog(StandardApplicationFrame.this, settings.getResourceString("MEASUREMENT_TOOL"));
                    measureDialog.addWindowListener(new WindowListener() {
                        public void	windowOpened(WindowEvent e) {
                        }
                        public void	windowClosing(WindowEvent e) {
                            perspectiveCamera.getCanvas().removeMouseListener(measurePanel);
                            perspectiveCamera.getCanvas().removeMouseMotionListener(measurePanel);
                            measurePanel.clear();
                        }
                        public void	windowClosed(WindowEvent e) {
                        }
                        public void	windowIconified(WindowEvent e) {
                        }
                        public void	windowDeiconified(WindowEvent e) {
                        }
                        public void	windowActivated(WindowEvent e) {
                        }
                        public void	windowDeactivated(WindowEvent e) {
                        }
                    }
                    );
                    measureDialog.getContentPane().add(measurePanel, BorderLayout.CENTER);
                    measureDialog.pack();
                }
                perspectiveCamera.getCanvas().addMouseListener(measurePanel);
                perspectiveCamera.getCanvas().addMouseMotionListener(measurePanel);
                perspectiveCamera.addPostDrawListener(measurePanel);
                measureDialog.setLocationRelativeTo(toolsMenu);
                measureDialog.setVisible(true);
            }
        });
        toolsMenu.add(measureMi);
        
        JMenuItem viewpointMi = new JMenuItem(settings.getResourceString("VIEWPOINTS")+"...");
        viewpointMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewpointDialog.setLocationRelativeTo(toolsMenu);
                viewpointDialog.setVisible(true);
            }
        });
        toolsMenu.add(viewpointMi);
        
        JMenuItem flypathMi = new JMenuItem(settings.getResourceString("FLYPATH")+"...");
        flypathMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flyPathDialog.setLocationRelativeTo(toolsMenu);
                flyPathDialog.setVisible(true);
            }
        });
        toolsMenu.add(flypathMi);

        /*
        boolean has_jmf = false;
        try {
            if (Class.forName("javax.media.CaptureDeviceManager") != null) has_jmf = true;
        } catch (Throwable ex) {}
        if (has_jmf) {
            JMenuItem videoMi = new JMenuItem(settings.getResourceString("VIDEO")+"...");
            videoMi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    videoDialog.setLocationRelativeTo(toolsMenu);
                    videoDialog.setVisible(true);
                }
            });
            toolsMenu.add(videoMi);
        }
         */
        toolsMenu.addSeparator();
        
        JMenuItem returnMi = new JMenuItem(settings.getResourceString("RETURN_MENU"));
        returnMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (startPoint.length == 5) {
                    perspectiveCamera.getNavigator().gotoViewpoint(startPoint[0], startPoint[1], startPoint[2], startPoint[3], startPoint[4]);
                } else if (startPoint.length == 3) {
                    perspectiveCamera.getNavigator().gotoLookat(startPoint[0], startPoint[1], startPoint[2], true, Math.toRadians(-20));
                }
            }    
        });
        toolsMenu.add(returnMi);
                
        JMenuItem copyUrlMi = new JMenuItem(settings.getResourceString("COPY_URL_MENU"));
        copyUrlMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String urlString = ApplicationSettings.getApplicationSettings().getGlobeStarterURLString();
                if (universe.getUrl() != null)
                    urlString += "dataset=" + universe.getUrl().toString() + "&";
                if (perspectiveCamera != null && perspectiveCamera.getNavigator() != null) {
                    GlobeNavigator nav = perspectiveCamera.getNavigator();
                    urlString += "viewpoint=";
                    urlString += Math.toDegrees(nav.getLon())+",";
                    urlString += Math.toDegrees(nav.getLat())+",";
                    urlString += nav.getEllipsHeight()+",";
                    urlString += Math.toDegrees(nav.getAzimut())+",";
                    urlString += Math.toDegrees(nav.getHeightAngle())+"&";
                }
                if (perspectiveCamera != null && perspectiveCamera.getViewerManager() instanceof com.norkart.virtualglobe.viewer.av3d.AV3DViewerManager) {
                    urlString += "libs=Xj3D";
                }
                Clipboard clipboard =  Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection data = new StringSelection(urlString);
                clipboard.setContents(data, data);
            }
        });
        toolsMenu.add(copyUrlMi);
        
        JMenuItem pasteUrlMi = new JMenuItem(settings.getResourceString("PASTE_URL_MENU"));
        pasteUrlMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard =  Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable data = clipboard.getContents(this);
                if (data == null) return;
                String str = null;
                try {
                    str = (String)data.getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException ex) {} catch (IOException ex) {}
                if (str == null) return;
                int fromIx = 0;
                Viewpoint v = new Viewpoint();
                while (fromIx < str.length()) {
                    int nextIx = str.indexOf("\n", fromIx);
                    if (nextIx < fromIx) nextIx = str.length();
                    String line = str.substring(fromIx, nextIx);
                    if (v.parse(line)) {
                        viewpointDialog.addViewpoint(v);
                        if (v.dataset != null)
                            universe.setDataSet(v.dataset);
                        if (perspectiveCamera != null && perspectiveCamera.getNavigator() != null) {
                            GlobeNavigator nav = perspectiveCamera.getNavigator();
                            nav.gotoViewpoint(v.data[0], v.data[1], v.data[2], v.data[3], v.data[4]);
                        }
                        break;
                    }
                    fromIx = nextIx+1;
                }
            }
        });
        toolsMenu.add(pasteUrlMi);
        toolsMenu.addSeparator();
        
        JMenuItem copyImageMi = new JMenuItem(settings.getResourceString("COPY_IMAGE_MENU"));
        copyImageMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread() { public void run() {
                    BufferedImage image = perspectiveCamera.getCapture();
                    ImageSelection data = new ImageSelection(image);
                    Clipboard clipboard =  Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(data, data);
                }}.start();
            }
        });
        toolsMenu.add(copyImageMi);
        
        saveCaptureFileChooser.setFileFilter(new ImageFileFilter());
        final JMenuItem saveImageMi = new JMenuItem(settings.getResourceString("SAVE_IMAGE_MENU")+"...");
        saveImageMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Component source = (Component)e.getSource();
                int reply = saveCaptureFileChooser.showSaveDialog(toolsMenu);
                if (reply == JFileChooser.APPROVE_OPTION) {
                    final File file = saveCaptureFileChooser.getSelectedFile();
                    new Thread() { public void run() {
                        BufferedImage image = perspectiveCamera.getCapture();
                        try {
                            ImageIO.write(image, FileUtil.getFileSuffix(file), file);
                        } catch (Exception ex) {
                            Object arg[] = {file.toString()};
                            String message = MessageFormat.format(ApplicationSettings.getApplicationSettings().getResourceString("NO_SAVE_MESSAGE"), arg);
                            JOptionPane.showMessageDialog(source,
                                    message, null,
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }}.start();
                }
            }
        });
        toolsMenu.add(saveImageMi);
        toolsMenu.addSeparator();
        
        JMenuItem settingsMi = new JMenuItem(settings.getResourceString("SETTINGS")+"...");
        settingsMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SettingsDialog settingsDialog = new SettingsDialog(StandardApplicationFrame.this, universe);
                settingsDialog.setLocationRelativeTo(toolsMenu);
                settingsDialog.setVisible(true);
            }
        });
        toolsMenu.add(settingsMi);
        menuBar.add(toolsMenu);
        
        // Help menu (to the extreme right)
        menuBar.add(Box.createHorizontalGlue());
        final JMenu helpMenu = new JMenu(settings.getResourceString("HELP_MENU"));
        JMenuItem helpDialogMi = new JMenuItem(settings.getResourceString("HELP_MENU"));
        helpDialogMi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                helpDialog.setLocationRelativeTo(helpMenu);
                helpDialog.setVisible(true);
            }
        });
        helpMenu.add(helpDialogMi);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
        
        /*
          camera.getCanvas().addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent ev) {
                    if (ev.getClickCount() != 2) return;
                    
                    GlobeNavigator nav = camera.getNavigator();
                    double lat  = nav.getPointerLat();
                    double lon  = nav.getPointerLon();
                    // double dist = nav.getPointerDist();
                    try {
                        nav.setUpdater(new com.norkart.VirtualGlobe.Util.Navigation.FlytoLookatUpdater(nav, lon, lat, 500, Math.toRadians(-20)));
                    } catch (Exception ex) {
                        ex.printStackTrace(); 
                    }
                }
            });
        */
    }
}