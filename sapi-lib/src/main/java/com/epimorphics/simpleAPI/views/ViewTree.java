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
            String npath = path.isEmpty() ? jname : path + "_" + jname;
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
        
}
