//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.viewer.av3d;

import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.Point;


import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.MouseInputAdapter;
        
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Point3f;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.output.graphics.BaseSurface;
import org.j3d.aviatrix3d.picking.PickRequest;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

import com.norkart.virtualglobe.components.feature.Feature3D;

import com.norkart.virtualglobe.util.SingletonDialog;
import com.norkart.virtualglobe.util.MyBrowserLauncher;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class PickHandler extends MouseInputAdapter {
    private GraphicsOutputDevice surface;
    private Group               root;
    private PickRequest request = new PickRequest();
    private Matrix4f view_xform = new Matrix4f();
    private Point3f  view_point = new Point3f();
    private Point3f  eye_point  = new Point3f();
    private Vector3f view_dir   = new Vector3f();
    private boolean has_new_pick_request = false;
    private MouseEvent event;
    
    PickHandler(GraphicsOutputDevice surface, Group root) {
        this.surface = surface;
        this.root    = root;
    }
    
    /*
    public void mouseMoved(MouseEvent e) {
        event = e;
        int x = e.getX();
        int y = e.getY();
        int h = e.getComponent().getHeight();
        
        ((BaseSurface)surface).getSurfaceToVWorld(x, h-y, 0, 0, view_xform);
        ((BaseSurface)surface).getPixelLocationInSurface(x, h-y, 0, 0, view_point);
        ((BaseSurface)surface).getCenterEyeInSurface(x, h-y, 0, 0, eye_point);
        
        view_xform.transform(view_point);
        view_xform.transform(eye_point);
        view_dir.sub(view_point, eye_point);
        // System.out.println("Origin    " + origin);
        // System.out.println("Viewpoint " + view_point);
        // System.out.println("Viewdir   " + view_dir);
        
        eye_point.get(request.origin);
        view_dir.get(request.destination);
        request.pickGeometryType = PickRequest.PICK_RAY;
        request.pickSortType = PickRequest.SORT_ORDERED;
        request.pickType = PickRequest.FIND_ALL;
        request.useGeometry = true;
        
        has_new_pick_request = true;
    }
    */
    
    public void	mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2)
            return;
        event = e;
        int x = e.getX();
        int y = e.getY();
        int h = e.getComponent().getHeight();
        
        ((BaseSurface)surface).getSurfaceToVWorld(x, h-y, 0, 0, view_xform, "MyClick", false);
        ((BaseSurface)surface).getPixelLocationInSurface(x, h-y, 0, 0, view_point, "MyClick", false);
        ((BaseSurface)surface).getCenterEyeInSurface(x, h-y, 0, 0, eye_point, "MyClick", false);
        
        view_xform.transform(view_point);
        view_xform.transform(eye_point);
        view_dir.sub(view_point, eye_point);
        // System.out.println("Origin    " + origin);
        // System.out.println("Viewpoint " + view_point);
        // System.out.println("Viewdir   " + view_dir);
        
        eye_point.get(request.origin);
        view_dir.get(request.destination);
        request.pickGeometryType = PickRequest.PICK_RAY;
        request.pickSortType = PickRequest.SORT_ORDERED;
        request.pickType = PickRequest.FIND_ALL;
        request.useGeometry = true;
        
        has_new_pick_request = true;
    }
    
    
    private void showInfoViewer(String text) {
        int x = event.getX();
        int y = event.getY();
        Point p = event.getComponent().getLocationOnScreen();
        x += p.x;
        y += p.y;
        
        JEditorPane infoViewer = new JEditorPane();
        infoViewer.setEditable(false);
        infoViewer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        infoViewer.setContentType("text/html");
        infoViewer.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                    if (!MyBrowserLauncher.openURL(e.getURL()))
                        System.out.println("Supposed to open " + e.getURL() + ", but it didn't work");
            }
        });
        infoViewer.setText(text);
        
        SingletonDialog.openDialog(new JScrollPane(infoViewer), x, y);
    }
    
    private boolean handlePath(SceneGraphPath path) {
        for (int i=path.getNodeCount()-1; i>=0; --i) {
            // System.out.println("Node " + i + " er " + path.getNode(i));
            Node n = path.getNode(i);
            Object user_data = n.getUserData();
            if (user_data instanceof PickListener) {
                ((PickListener)user_data).picked(event);
                return true;
            }
            else if (user_data instanceof Feature3D) {
                String info = ((Feature3D)user_data).getInfo();
                if (info != null && !info.equals("")) {
                    showInfoViewer(info);
                    return true;
                }
            } else if (user_data instanceof URL) {
                URL infoURL = (URL)user_data;
                if (MyBrowserLauncher.openURL(infoURL))
                    return true;
                else
                    System.out.println("Supposed to open " + infoURL + ", but it didn't work");
            }
        }
        return false;
    }
    
    public void handleLastPick() {
        if (!has_new_pick_request) return;
        has_new_pick_request = false;
        request.foundPaths = null;
        request.pickCount = 0;
        root.pickSingle(request);
        
        if (request.pickCount <= 0) return;
        
        Object o = request.foundPaths;
        if (o instanceof SceneGraphPath)
            handlePath((SceneGraphPath)o);
        else if (o instanceof ArrayList) {
            Iterator it = ((ArrayList)o).iterator();
            while (it.hasNext())
                if (handlePath((SceneGraphPath)it.next()))
                    break;
        }
    }
}
