/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  AttributeOptimizer.java
 *
 * Created on 18. oktober 2007, 13:05
 *
 */

package com.norkart.virtualglobe.viewer.av3d.nodes;

import java.util.*;

import org.j3d.aviatrix3d.*;

/**
 *
 * @author runaas
 */
public class AppearanceOptimizer {
   
    private Map<Appearance, Appearance> used = new TreeMap<Appearance, Appearance>();
    
    /** Creates a new instance of AttributeOptimizer */
    public AppearanceOptimizer() {
    }
    
    public void optimize(Node root) {
        Set<Node> visited = new HashSet<Node>();
        recurse(root, visited);
    }
    
    private void recurse(Node root, Set<Node> visited) {
        if (root == null)
            return;
        if (visited.contains(root))
            return;
        if (root instanceof Group) {
            Group g = (Group)root;
            Node [] children = g.getAllChildren();
            int num_children = g.numChildren();
            for (int i=0; i<num_children; ++i)
                recurse(children[i], visited);
        }
        
        if (root instanceof SharedNode) {
            SharedNode s = (SharedNode)root;
            recurse(s.getChild(), visited);
        }
        
        if (!(root instanceof Shape3D))
            return;
        
        Shape3D s = (Shape3D)root;
        /*
        Appearance a = s.getAppearance();
        if (a == null)
            return;
        Appearance used_a = used.get(a);
        if (used_a == null)
            used.put(a,a);
        else if (used_a != a)
            s.setAppearance(used_a);
         */
        Geometry g = s.getGeometry();
        if (g instanceof VertexGeometry)
            ((VertexGeometry)g).setVBOEnabled(true);
    }
}
