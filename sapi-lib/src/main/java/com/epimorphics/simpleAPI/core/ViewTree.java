/******************************************************************
 * File:        ViewTree.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a hierarchical view over a set of RDF resources, to support mapping to JSON.
 * The root of the tree is a ViewMap
 */
public class ViewTree implements Iterable<ViewEntry> {
    protected List<ViewEntry> children = new ArrayList<ViewEntry>();
    
    public ViewTree() {
    }

    // TODO constructor to deep clone an existing tree?
    
    public void addChild(ViewEntry entry) {
        children.add(entry);
    }

    @Override
    public Iterator<ViewEntry> iterator() {
        return children.iterator();
    }

    public List<ViewEntry> getChildren() {
        return children;
    }
    
    /**
     * Synthesize a SPARQL query for this subtree.
     * @param buf Buffer into which to append the query text
     * @param var the SPARQL variable representing the root of the tree
     */
    protected void renderAsQuery(StringBuffer buf, String var) {
        boolean started = false;
        for (ViewEntry map : children) {
            if (!map.isOptional()) {
                if (!started){
                    started = true;
                    buf.append("    ?" + var + "\n");                    
                }
                buf.append("        " + map.asQueryRow() + " ;\n");
            }
        }
        if (started) buf.append("    .\n");
        for (ViewEntry map : children) {
            if (map.isOptional()) {
                if (map.isNested()) {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .\n" );
                    ViewTree nested = map.getNested();
                    nested.renderAsQuery(buf, map.getJsonName());
                    buf.append("    }\n" );
                    
                } else {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .}\n" );
                }
            } else if (map.isNested()) {
                ViewTree nested = map.getNested();
                nested.renderAsQuery(buf, map.getJsonName());
            }
        }
    }
    
}
