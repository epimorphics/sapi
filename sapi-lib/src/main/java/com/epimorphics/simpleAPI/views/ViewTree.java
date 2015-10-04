/******************************************************************
 * File:        ViewTree.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import static com.epimorphics.simpleAPI.core.ConfigConstants.COMMENT;
import static com.epimorphics.simpleAPI.core.ConfigConstants.FILTERABLE;
import static com.epimorphics.simpleAPI.core.ConfigConstants.MULTIVALUED;
import static com.epimorphics.simpleAPI.core.ConfigConstants.NAME;
import static com.epimorphics.simpleAPI.core.ConfigConstants.NESTED;
import static com.epimorphics.simpleAPI.core.ConfigConstants.OPTIONAL;
import static com.epimorphics.simpleAPI.core.ConfigConstants.PROPERTY;
import static com.epimorphics.simpleAPI.core.ConfigConstants.PROP_TYPE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.util.EpiException;

/**
 * Represents a hierarchical view over a set of RDF resources, to support mapping to JSON.
 * The root of the tree is a ViewMap
 */
public class ViewTree implements Iterable<ViewEntry> {
    protected Map<String, ViewEntry> children = new LinkedHashMap<>();
    
    public ViewTree() {
    }

    // TODO constructor to deep clone an existing tree?
    
    public void addChild(ViewEntry entry) {
        children.put(entry.getJsonName(), entry);
    }

    @Override
    public Iterator<ViewEntry> iterator() {
        return children.values().iterator();
    }

    public List<ViewEntry> getChildren() {
        return new ArrayList<>(children.values());
    }
    
    public ViewEntry getEntry(String shortname) {
        return children.get(shortname);
    }
    
    /**
     * Synthesize a SPARQL query for this subtree.
     * @param buf Buffer into which to append the query text
     * @param var the SPARQL variable representing the root of the tree
     * @param path a prefix to prepend to the variable names to ensure distinct values down the tree
     */
    protected void renderAsQuery(StringBuffer buf, String var, String path) {
        boolean started = false;
        for (ViewEntry map : children.values()) {
            if (!map.isOptional()) {
                if (!started){
                    started = true;
                    buf.append("    ?" + var + "\n");                    
                }
                buf.append("        " + map.asQueryRow(path) + " ;\n");
            }
        }
        if (started) buf.append("    .\n");
        for (ViewEntry map : children.values()) {
            String jname = map.getJsonName();
            String npath = addToPath(path, jname);
            if (map.isOptional()) {
                if (map.isNested()) {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow(path) + " .\n" );
                    ViewTree nested = map.getNested();
                    nested.renderAsQuery(buf, jname, npath);
                    buf.append("    }\n" );
                    
                } else {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow(path) + " .}\n" );
                }
            } else if (map.isNested()) {
                ViewTree nested = map.getNested();
                nested.renderAsQuery(buf, jname, npath);
            }
        }
    }
    
    private String addToPath(String path, String jname) {
        if (path.isEmpty()) {
            return jname.replace("_", "__");
        } else {
            return path + "_" + jname.replace("_", "__");
        }
    }

    public static ViewTree parseFromJson(API api, JsonValue list) {
        ViewTree tree = new ViewTree();
        if (list.isArray()) {
            for (Iterator<JsonValue> pi = list.getAsArray().iterator(); pi.hasNext(); ) {
                ViewEntry entry = null;
                JsonValue prop = pi.next();
                if (prop.isString()) {
                    entry = new ViewEntry( prop.getAsString().value() );
                } else if (prop.isObject()) {
                    JsonObject propO = prop.getAsObject();
                    String p = JsonUtil.getStringValue(propO, PROPERTY);
                    String name = JsonUtil.getStringValue(propO, NAME);
                    entry = new ViewEntry(name, p);
                    if (propO.hasKey(OPTIONAL)) {
                        entry.setOptional( JsonUtil.getBooleanValue(propO, OPTIONAL, false) );
                    }
                    if (propO.hasKey(MULTIVALUED)) {
                        entry.setMultivalued( JsonUtil.getBooleanValue(propO, MULTIVALUED, false) );
                    }
                    if (propO.hasKey(NESTED)) {
                        ViewTree nested = parseFromJson(api, propO.get(NESTED) );
                        entry.setNested(nested);                        
                    }
                    if (propO.hasKey(FILTERABLE)) {
                        entry.setFilterable( JsonUtil.getBooleanValue(propO, FILTERABLE, true) );
                    }
                    if (propO.hasKey(PROP_TYPE)) {
                        String ty = JsonUtil.getStringValue(propO, PROP_TYPE);
                        entry.setTypeURI( ty );  // Unexpanded prefix, have to delay expansion until runtime structure is built
                    }
                    if (propO.hasKey(COMMENT)) {
                        String comment = JsonUtil.getStringValue(propO, COMMENT);
                        entry.setComment(comment);
                    }
                }
                tree.addChild(entry);
            }
        } else {
            throw new EpiException("Illegal JSON mapping spec, value must be an array of mapping specifications");
        }
        return tree;
    }
    
    @Override
    public String toString() {
        return print(new StringBuffer(), "").toString();
    }
    
    protected StringBuffer print(StringBuffer buf, String indent) {
        for (ViewEntry child : this) {
            child.print(buf, indent);
            buf.append("\n");
        }
        return buf;
    }
    
    /**
     * Locate an entry which matches a request name, this is either unique within the tree or 
     * is assumed to be a "p.q.r" dotted notation for a path.
     * Returns null if this is not a legal path
     */
    public ViewPath pathTo(String name) {
        if (name.contains(".")) {
            // Full dotted path
            ViewPath path = new ViewPath(name.split("\\."));
            if (findEntry(path) != null) {
                return path;
            } else {
                return null;
            }
        } else {
            // Breadth first search to locate a matching short name
            return pathTo(name, new ViewPath());
        }
    }
    
    protected ViewPath pathTo(String name, ViewPath path) {
        for (ViewEntry child : this) {
            if (child.getJsonName().equals(name)) {
                return path.add(name);
            }
        }
        for (ViewEntry child : this) {
            if (child.isNested()) {
                ViewPath fullPath = child.getNested().pathTo(name, path.add(child.getJsonName()));
                if (fullPath != null) {
                    return fullPath;
                }
            }
        }
        return null;
    }
    
    /**
     * Return a view entry based on a path of short names, or null if it is not specified in the view.
     */
    public ViewEntry findEntry(ViewPath path) {
        if (path.isEmpty()) {
            return null;
        } else {
            String elt = path.first();
            ViewEntry first = children.get(elt);
            if (first == null) {
                return null;
            } else {
                return first.findEntry(path.rest());
            }
        }
    }

}
