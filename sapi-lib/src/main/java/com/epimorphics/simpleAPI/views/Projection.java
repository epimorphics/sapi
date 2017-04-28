/******************************************************************
 * File:        Projection.java
 * Created by:  Dave Reynolds
 * Created on:  28 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.epimorphics.util.EpiException;

/**
 * Represents a tree of properties which can be applied to a model to get a view,
 * or applied to a view to get a restricted view. Each property in the tree is denoted
 * by its short (json) name. Supports a textual syntax that can be used in query parameters:
 * <pre>
 *   prop1(prop2,prop3(label,prop4)),f.g
 * </pre>
 * which is equivalent to
 * <pre>
 *   prop1.prop2,prop1.prop3.label,prop1.prop3.prop4,f.g
 * </pre>
 * 
 */
public class Projection {
    protected static final String DELIMS = ".,()";
    protected static boolean isDelim(String x) { return x.length() == 1 && DELIMS.contains(x); }
    
    protected Node root = new Node(null);
    
    public Projection() {}
    
    public Projection(String path) {
        addPath(path);
    }
    
    public void addPath(String path) {
        root.parseSeq( new StringTokenizer(path, DELIMS, true) );
    }
    
    public Node getRoot() {
        return root;
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Node child : root.getChildren()) {
            buf.append( child.toString() );
        }
        return buf.toString();
    }
    
    public class Node {
        protected String name;
        protected Map<String, Node> children;
        
        public Node(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isWildcard() {
            return "*".equals( name );
        }
        
        public Node getOrAddChild(String name) {
            if (children == null) {
                children = new LinkedHashMap<>();
            }
            Node child = children.get(name);
            if (child == null) {
                child = new Node(name);
                children.put(name, child);
            }
            return child;
        }
        
        public Collection<Node> getChildren() {
            return children.values();
        }
        
        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }
        
        protected String parsePath(StringTokenizer tokens) {
            if (tokens.hasMoreTokens()) {
                String next = tokens.nextToken();
                if (isDelim(next)) {
                    throw new EpiException("Illegal path");
                } else {
                    Node child = getOrAddChild(next);
                    if (tokens.hasMoreTokens()) {
                        next = tokens.nextToken();
                        if (next.equals(".")) {
                            next = child.parsePath(tokens);
                        } else if (next.equals("(")) {
                            next = child.parseSeq(tokens);
                            if (next == null || !next.equals(")")) throw new EpiException("Illegal path");
                            next = (tokens.hasMoreTokens()) ? tokens.nextToken() : null;
                        }
                        return next;
                    }
                }
            }
            return null;
        }
        
        protected String parseSeq(StringTokenizer tokens) {
            String next = null;
            do {
                next = parsePath(tokens);
            } while (next != null && next.equals(","));
            return next;
        }

        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            printTo(buf, "");
            return buf.toString();
        }
        
        protected void printTo(StringBuffer buf, String indent) {
            buf.append(indent);
            if (children == null) {
                buf.append(name);
                buf.append("\n");
            } else {
                buf.append(name + ".\n");
                for (Node child : children.values()) {
                    child.printTo(buf, indent + "  ");
                }
            }
        }
    }

}
