/******************************************************************
 * File:        ViewMap.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigItem;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.query.Query;
import com.epimorphics.sparql.templates.Settings;
import com.epimorphics.sparql.terms.TermAtomic;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.util.EpiException;

/**
 * Represents a singled configured tree view over the data which can be directly mapped to JSON.
 */
public class ViewMap extends ConfigItem {
    private ViewTree tree;
    private String viewReference;
    protected API api;
    protected List<ViewPath> allPaths;
    
    public ViewMap(API api) {
        this.api = api;
    }
    
    public ViewMap(API api, ViewTree tree) {
        this.tree = tree;
        this.api = api;
    }
    
    public ViewMap(API api, String viewReference) {
        this.viewReference = viewReference;
        this.api = api;
    }
    
    public API getAPI() {
        return api;
    }
    
    // TODO constructor to clone an existing tree/map?
    
    public ViewTree getTree() {
        if (tree == null) {
            tree = api.getView(viewReference).getTree();
        }
        return tree;
    }
    
    /**
     * Return a SPARQL query string representing the bindings for this map
     */
    public String asQuery() {
    	Query q = new Query();
        q.addPattern(getTree().buildPattern("id", ""));
        return q.toSparqlConstruct(new Settings());
    }
    
    /**
     * Return a SPARQL describe query which describes the neste elements in the tree
     */
    public Query asDescribe() {
    	Set<String> vars = new HashSet<>();
    	Query sq = new Query();
    	getTree().renderForDescribe(sq, "id", "", vars);
    	sq.addDescribeElements(list(new Var("id")));
    	for (String v: vars) sq.addDescribeElements(list(new Var(v)));
    	return sq;
    }
    
    private List<TermAtomic> list(Var var) {
    	List<TermAtomic> l = new ArrayList<TermAtomic>();
		l.add(var);
		return l;
	}

	/**
     * Convert a property name to the corresponding variable name used in generated queries.
     * If the property name is unambiguous then it will be located anywhere in the tree,
     * otherwise use an explicit "p.q.r" notation. Any "_" characters in the names will be handled.
     * @return the legal variable name or null if the name/path is not present within the view
     */
    public String asVariableName(String name) {
        if ("@id".equals(name)) {
            return "id";
        }
        ViewPath path = getTree().pathTo(name);
        if (path != null) {
            return path.asVariableName();
        } else {
            return null;
        }
    }
    
    /**
     * Find the ViewEntry defining a property name within the view.
     * If the property name is unambiguous then it will be located anywhere in the tree,
     * otherwise use an explicit "p.q.r" notation. Any "_" characters in the names will be handled.
     */
    public ViewEntry findEntry(String name) {
        ViewPath path = getTree().pathTo(name);
        if (path != null) {
            return getTree().findEntry(path);
        } else {
            return null;
        }
    }
    
    /**
     * Find the ViewEntry for a property URI
     */
    public ViewEntry findEntryByURI(String uri) {
        return getTree().findEntryByURI(uri);
    }
    
    /**
     * Retrieve the view entry via a full path description
     */
    public ViewEntry findEntry(ViewPath path) {
        return getTree().findEntry(path);
    }
    
    /**
     * Locate an entry which matches a request name, this is either unique within the tree or 
     * is assumed to be a "p.q.r" dotted notation for a path.
     * Returns null if this is not a legal path
     */
    public ViewPath pathTo(String name) {
        return getTree().pathTo(name);
    }
    
    /**
     * Return a list of the paths to all the leave nodes in the map
     */
    public List<ViewPath> getAllPaths() {
        if (allPaths == null) {
            allPaths = new ArrayList<>();
            allPaths.add( new ViewPath() );
            getTree().collectPaths(new ViewPath(), allPaths);
        }
        return allPaths;
    }
    
    public static ViewMap parseFromJson(API api, PrefixMapping prefixes, JsonValue list) {   
        if (list.isString()) {
            // Named view reference
            return new ViewMap(api, list.getAsString().value());
        } else if (list.isArray()) {
            // Inline view specification
            return new ViewMap(api, ViewTree.parseFromJson(api, prefixes, list) );
        } else {
            throw new EpiException("Illegal view specification must be a name or an array of view entries: " + list);
        }
    }

    @Override
    public String toString() {
        return getTree().toString();
    }
}
