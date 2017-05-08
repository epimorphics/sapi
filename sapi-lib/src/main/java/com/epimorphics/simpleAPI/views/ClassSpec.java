/******************************************************************
 * File:        ViewTree.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import static com.epimorphics.simpleAPI.core.ConfigConstants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.views.PropertySpec.PV;
import com.epimorphics.sparql.exprs.Infix;
import com.epimorphics.sparql.exprs.Op;
import com.epimorphics.sparql.graphpatterns.And;
import com.epimorphics.sparql.graphpatterns.Basic;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.graphpatterns.Optional;
import com.epimorphics.sparql.terms.Filter;
import com.epimorphics.sparql.terms.Triple;
import com.epimorphics.sparql.terms.URI;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.util.EpiException;

/**
 * Information on a class in the model. May be an actual RDFS class (with URI and shortname)
 * or may be an anonymous nested class showing the substructure to be found/rendered for a 
 * parent property spec. The order of properties in the original yaml are preserved and used
 * for rendering.
 * <p>
 * Replaces ViewSpec in Sapi2.
 * </p>
 */
public class ClassSpec implements Iterable<PropertySpec> {
    protected URI uri;
    protected String jsonname;
    protected Map<String, PropertySpec> children = new LinkedHashMap<>();
    
    /**
     * Construct an anonymous class
     */
    public ClassSpec() {
    }
    
    /**
     * Construct a class with default shortname
     */
    public ClassSpec(URI uri) {
        this( PropertySpec.makeJsonName(uri), uri);
    }
    
    /**
     * Construct a class with explict shortname
     */
    public ClassSpec(String name, URI uri) {
        this.uri = uri;
        this.jsonname = name;
    }
    
    public ClassSpec clone() {
        return new ClassSpec(jsonname, uri);
    }
    
    public ClassSpec deepclone() {
        ClassSpec clone = clone();
        for (PropertySpec p : children.values()) {
            clone.addChild( p.deepclone() );
        }
        return clone;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getJsonName() {
        return jsonname;
    }

    public void setJsonName(String jsonname) {
        this.jsonname = jsonname;
    }
    
    public void addChild(PropertySpec entry) {
        children.put(entry.getJsonName(), entry);
    }

    @Override
    public Iterator<PropertySpec> iterator() {
        return children.values().iterator();
    }

    public List<PropertySpec> getChildren() {
        return new ArrayList<>(children.values());
    }
    
    public PropertySpec getEntry(String shortname) {
        return children.get(shortname);
    }
    
    
    protected GraphPattern buildPattern(String var, String path) {
        return buildPattern(var, path, new HashSet<>(), true);
    }
    
    protected GraphPattern buildPattern(String var, String path, Set<String> vars, boolean includeNonNested) {

    	List<GraphPattern> patterns = new ArrayList<GraphPattern>();
    	
    	for (PropertySpec map : children.values()) {
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
            		ClassSpec nested = map.getNested();
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
                    ClassSpec nested = map.getNested();
                    patterns.add(nested.buildPattern(npath, npath, vars, includeNonNested));            		
            	} else if (includeNonNested){
                    patterns.add(basic);
            	}
            }
			
			if ( ! map.getExcludedValues().isEmpty() ) {
			    for (String exclude : map.getExcludedValues()) {
			        patterns.add( new Basic( new Filter( new Infix(pv.var, Op.opNe, new URI(exclude)) ) ) );
			    }
			}
        }
    	
    	return new And(patterns);    	
    }
    
    protected GraphPattern patternForPath(ViewPath path, String pathSoFar, String priorVar) {
        String var = path.first();
        PropertySpec entry = getEntry(var);
        if (entry == null) {
            throw new EpiException("Path not recognized: " + var);
        }
        PV pv = entry.asQueryRow(pathSoFar);
        Triple t = new Triple(new Var(priorVar), pv.property, pv.var);
        ViewPath rest = path.rest();
        if (rest.isEmpty()) {
            return new Basic(t);
        } else {
            ClassSpec nested = entry.getNested();
            if (nested == null) {
                throw new EpiException("Path does not match nesting:" + path);
            }
            return new And( new Basic(t), nested.patternForPath(rest, addToPath(pathSoFar, var), pv.var.getName()));
        }
    }
      
    /**
     * Extend a list of all paths in the tree 
     */
    protected void collectPaths(ViewPath parent, List<ViewPath> paths) {
        for (PropertySpec map : children.values()) {
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
    
    /**
     * Copy the whole property tree defined by root into this view, expanding any non-nested
     * property whose type is part of the model
     */
    protected void addClosure(ModelSpec model, ClassSpec root, Set<String> seen) {
        for (PropertySpec ps : root) {
            PropertySpec pc = ps.deepclone();
            addChild(pc);
            if ( ps.getNested() == null ) {
                String rangeURI = ps.getRange();
                if ( !seen.contains(rangeURI) ) {
                    ClassSpec range = model.getClassSpec( rangeURI );
                    if (range != null) {
                        ClassSpec nested = range.clone();
                        Set<String> nseen = new HashSet<>( seen );
                        nseen.add(rangeURI);
                        nested.addClosure(model, range, nseen);
                        pc.setNested(nested);
                    }
                }
            }
        }
    }
    
    /**
     * Create a copy of the property tree that matches the given projection.
     * Will expand any non-nested properties as part of the projection.
     */
    public ClassSpec project(ModelSpec model, Projection projection) {
        return project(model, projection.getRoot());
    }
    
    /**
     * Create a copy of the property tree that matches the given projection.
     */
    public ClassSpec project(Projection projection) {
        return project(null, projection.getRoot());
    }

    protected ClassSpec project(ModelSpec model, Projection.Node projection) {
        ClassSpec cs = clone();
        for (Projection.Node p : projection.getChildren()) {
            if (p.isWildcard()) {
                for( PropertySpec ps : this) {
                    cs.addChild( project(model, ps, p) );
                }
            } else {
                PropertySpec ps = getByJsonName( p.getName() );
                if (ps == null) {
                    throw new EpiException("Property " + p.getName() + " not found in class " + cs.getUri());
                }
                cs.addChild( project(model, ps, p) );
            }
        }
        return cs;
    }
    
    private static PropertySpec project(ModelSpec model, PropertySpec prop, Projection.Node p) {
        PropertySpec ps = prop.cloneWithClosure(model);
        if (p.hasChildren()) {
            ClassSpec nested = ps.getNested();
            if (nested == null) {
                throw new EpiException("No expansion found for " + ps);
            }
            ps.setNested( nested.project(model, p) );
        } else {
            ps.nested = null;
        }
        return ps;
    }
    
    /**
     * Parse a JSON definition of the spec in the context of some overall
     * ModelSpec which supplies default property context and prefixes
     */
    public static ClassSpec parseFromJson(ModelSpec model, JsonValue json) {
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            ClassSpec cs = jo.hasKey(PROPERTIES) ? parseInline(model, jo.get(PROPERTIES)) : null;
            if ( jo.hasKey(NAME) ) {
                cs.setJsonName( JsonUtil.getStringValue(jo, NAME) );
            }
            if ( jo.hasKey(CLASS) ) {
                String uri = JsonUtil.getStringValue(jo, CLASS);
                uri = model.getPrefixes().expandPrefix(uri);
                cs.setUri( new URI(uri) );
            }
            return cs;
        } else if (json.isArray()) {
            return parseInline(model, json);
        } else {
            // TODO might handle names as references with late binding into model?
            throw new EpiException("Class references not yet supported (or broken class definition): " + json);
        }
    }
    
    /**
     * Parse inline verison of the spec which is just the list of property definitions,
     * with no URI or name
     */
    public static ClassSpec parseInline(ModelSpec model, JsonValue json) {
        if (json.isArray()) {
            ClassSpec cs = new ClassSpec();
            for (Iterator<JsonValue> pi = json.getAsArray().iterator(); pi.hasNext(); ) {
                PropertySpec ps = PropertySpec.parseFromJson(model, pi.next());
                cs.addChild(ps);
            }
            return cs;
        } else {
            throw new EpiException("Class properties need to be an array: " + json);
        }
    }
    
    @Override
    public String toString() {
        return print(new StringBuffer(), "").toString();
    }
    
    protected StringBuffer print(StringBuffer buf, String indent) {
        buf.append(indent);
        buf.append("Class ");
        if (uri != null) {
            buf.append( String.format("%s(%s)", jsonname, uri.toString()) );
        }
        buf.append("\n");
        String nested = indent + "  ";
        for (PropertySpec child : getChildren()) {
            child.print(buf, nested);
            buf.append("\n");
        }
        return buf;
    }
    
    /**
     * Get the property with the given short name (does not support paths)
     */
    public PropertySpec getByJsonName(String name) {
        return children.get(name);
    }
    
    /**
     * Locate an entry which matches a request name, this is either unique within the tree or 
     * is assumed to be a "p.q.r" dotted notation for a path.
     * Returns null if this is not a legal path
     */
    public ViewPath pathTo(String name) {
        if (name.equals("@id")) {
            // Special case to handle root path
            return new ViewPath();
        }
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
        for (PropertySpec child : this) {
            if (child.getJsonName().equals(name)) {
                return path.withAdd(name);
            }
        }
        for (PropertySpec child : this) {
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
    public PropertySpec findEntryByURI(String uri) {
        for (PropertySpec child : this) {
            if (child.getProperty().getURI().equals(uri)) {
                return child;
            }
        }
        for (PropertySpec child : this) {
            if (child.isNested()) {
                PropertySpec entry = child.getNested().findEntryByURI(uri);
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
    public PropertySpec findEntry(ViewPath path) {
        if (path.isEmpty()) {
            return null;
        } else {
            String elt = path.first();
            PropertySpec first = children.get(elt);
            if (first == null) {
                return null;
            } else {
                return first.findEntry(path.rest());
            }
        }
    }

}
