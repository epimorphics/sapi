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
import static com.epimorphics.simpleAPI.core.ConfigConstants.SUPPRESSID;
import static com.epimorphics.simpleAPI.core.ConfigConstants.VALUE_BASE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.views.ViewEntry.PV;
import com.epimorphics.sparql.graphpatterns.And;
import com.epimorphics.sparql.graphpatterns.Basic;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.graphpatterns.Optional;
import com.epimorphics.sparql.terms.Triple;
import com.epimorphics.sparql.terms.URI;
import com.epimorphics.sparql.terms.Var;
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
    
    
    protected GraphPattern buildPattern(String var, String path) {
        return buildPattern(var, path, new HashSet<>(), true);
    }
    
    protected GraphPattern buildPattern(String var, String path, Set<String> vars, boolean includeNonNested) {

    	List<GraphPattern> patterns = new ArrayList<GraphPattern>();
    	
    	for (ViewEntry map : children.values()) {
            String jname = map.getJsonName();
            String npath = addToPath(path, jname);
        	PV pv = map.asQueryRow(path);
        	Triple t = new Triple(new Var(var), pv.property, pv.var);
        	Basic basic = new Basic(t);
        
        	if (map.isNested() || includeNonNested) {
                vars.add( npath );
        	}
        	
			if (map.isOptional()) {
            	if (map.isNested()) {
            		ViewTree nested = map.getNested();
            		GraphPattern p = nested.buildPattern(npath, npath, vars, includeNonNested);
            		GraphPattern both = new And(basic, p);
            		patterns.add(new Optional(both));
            	} else {
            	    if ( includeNonNested ) {
                        	        patterns.add(new Optional(basic));
            	    }
            	}
            } else {
            	if (map.isNested()) {
                    patterns.add(basic);
                    ViewTree nested = map.getNested();
                    patterns.add(nested.buildPattern(npath, npath, vars, includeNonNested));            		
            	} else if (includeNonNested){
                    patterns.add(basic);
            	}
            }
        }
    	
    	return new And(patterns);    	
    }
      
    /**
     * Extend a list of all paths in the tree 
     */
    protected void collectPaths(ViewPath parent, List<ViewPath> paths) {
        for (ViewEntry map : children.values()) {
            paths.add( parent.withAdd(map.getJsonName()) );
            if (map.isNested()) {
                map.getNested().collectPaths(parent.withAdd(map.getJsonName()), paths);
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

    public static ViewTree parseFromJson(API api, PrefixMapping prefixes, JsonValue list) {
        ViewTree tree = new ViewTree();
        if (list.isArray()) {
            for (Iterator<JsonValue> pi = list.getAsArray().iterator(); pi.hasNext(); ) {
                ViewEntry entry = null;
                JsonValue prop = pi.next();
                if (prop.isString()) {
                    String p = prop.getAsString().value();
                    if (prefixes != null) p = prefixes.expandPrefix(p);
                    entry = new ViewEntry( new URI(p) );
                } else if (prop.isObject()) {
                    JsonObject propO = prop.getAsObject();
                    String p = JsonUtil.getStringValue(propO, PROPERTY);
                    if (prefixes != null) p = prefixes.expandPrefix(p);
                    String name = JsonUtil.getStringValue(propO, NAME);
                    entry = new ViewEntry(name, new URI(p));
                    if (propO.hasKey(OPTIONAL)) {
                        entry.setOptional( JsonUtil.getBooleanValue(propO, OPTIONAL, false) );
                    }
                    if (propO.hasKey(MULTIVALUED)) {
                        entry.setMultivalued( JsonUtil.getBooleanValue(propO, MULTIVALUED, false) );
                    }
                    if (propO.hasKey(NESTED)) {
                        ViewTree nested = parseFromJson(api, prefixes, propO.get(NESTED) );
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
                    if (propO.hasKey(VALUE_BASE)) {
                        String vb = JsonUtil.getStringValue(propO, VALUE_BASE);
                        entry.setValueBase(vb);
                    }
                    if (propO.hasKey(SUPPRESSID)) {
                        boolean sid = JsonUtil.getBooleanValue(propO, SUPPRESSID, false);
                        entry.setSuppressId(sid);
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
                return path.withAdd(name);
            }
        }
        for (ViewEntry child : this) {
            if (child.isNested()) {
                ViewPath fullPath = child.getNested().pathTo(name, path.withAdd(child.getJsonName()));
                if (fullPath != null) {
                    return fullPath;
                }
            }
        }
        return null;
    }
    
    /**
     * Locate an entry which matches a property URI, breadth first search
     */
    public ViewEntry findEntryByURI(String uri) {
        for (ViewEntry child : this) {
            if (child.getProperty().getURI().equals(uri)) {
                return child;
            }
        }
        for (ViewEntry child : this) {
            if (child.isNested()) {
                ViewEntry entry = child.getNested().findEntryByURI(uri);
                if (entry != null) {
                    return entry;
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
