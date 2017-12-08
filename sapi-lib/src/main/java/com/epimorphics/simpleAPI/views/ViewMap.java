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

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigItem;
import com.epimorphics.simpleAPI.writers.CSVMap;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.templates.Settings;
import com.epimorphics.sparql.terms.TermAtomic;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.util.EpiException;
import static com.epimorphics.simpleAPI.core.ConfigConstants.*;

/**
 * Represents a singled configured tree view over the data which can be directly mapped to JSON.
 */
public class ViewMap extends ConfigItem {
    protected ClassSpec tree;
    protected API api;
    protected List<ViewPath> allPaths;
    protected CSVMap csvmap;
    protected String geometryProp;
    
    public ViewMap(API api) {
        super();
        this.api = api;
    }
    
    public ViewMap(API api, ClassSpec tree) {
        super();
        this.tree = tree;
        this.api = api;
    }
    
    public API getAPI() {
        return api;
    }
    
    // TODO constructor to clone an existing tree/map?
    
    public ClassSpec getTree() {
        return tree;
    }
    
    /**
     * Return a SPARQL query string representing the bindings for this map
     */
    public String asQuery() {
    	QueryShape q = new QueryShape();
        q.addEarlyPattern( asPattern() );
        return q.toSparqlConstruct(new Settings());
    }
    
    public GraphPattern asPattern() {
        return getTree().buildPattern(ROOT_VAR, "");
    }
    
    public GraphPattern patternForPath(ViewPath path) {
        return tree.patternForPath(path, "", ROOT_VAR);
    }
    
    public void injectTreePatternInfo(QueryShape q) {
        q.addEarlyPattern( asPattern() );
    }
    
    /**geometryProp
     * Return a SPARQL describe query which describes the neste elements in the tree
     */
    public QueryShape asDescribe() {
    	Set<String> vars = new HashSet<>();
    	GraphPattern pattern = getTree().buildPattern(ROOT_VAR, "", vars, false);
    	QueryShape sq = new QueryShape();
    	sq.addDescribeElements(list(new Var(ROOT_VAR)));
    	sq.addEarlyPattern(pattern);
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
            return ROOT_VAR;
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
    public PropertySpec findEntry(String name) {
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
    public PropertySpec findEntryByURI(String uri) {
        return getTree().findEntryByURI(uri);
    }
    
    /**
     * Retrieve the view entry via a full path description
     */
    public PropertySpec findEntry(ViewPath path) {
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
     * Return a list of the paths to all the leaf nodes in the map
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
            return new ViewMapReference(api, list.getAsString().value());
        } else if (list.isArray()) {
            // Inline view specification
            return new ViewMap(api, ClassSpec.parseFromJson( new ModelSpec(prefixes), list) );
        } else if (list.isObject()) {
            // Model/project reference cases
            JsonObject jo = list.getAsObject();
            ViewMap view = null;
            if (jo.hasKey(VIEW) && jo.hasKey(MVIEW_PROJECTION)) {
                String viewReference = JsonUtil.getStringValue(jo, VIEW);
                String projection = JsonUtil.getStringValue(jo, MVIEW_PROJECTION);
                view = new ViewMapProjection(api, viewReference, projection);
            } else if (jo.hasKey(MVIEW_MODEL) || jo.hasKey(MVIEW_CLASS)) {
                String modelReference = JsonUtil.getStringValue(jo, MVIEW_MODEL);
                String baseClass = JsonUtil.getStringValue(jo, MVIEW_CLASS);
                String projection = JsonUtil.getStringValue(jo, MVIEW_PROJECTION);
                view = new ViewMapModelProjection(api, modelReference, baseClass, projection);
            } else {
                throw new EpiException("Could now parse model/view reference: " + list);
            }
            if (jo.hasKey(GEOM_PROP)) {
                view.setGeometryProp( JsonUtil.getStringValue(jo, GEOM_PROP));
            }
            return view;
            
        } else {
            throw new EpiException("Illegal view specification must be a name or an array of view entries: " + list);
        }
    }
    
    public CSVMap getCsvMap() {
        return csvmap;
    }

    public void setCsvMap(CSVMap csvmap) {
        this.csvmap = csvmap;
    }

    public boolean hasCsvMap() {
        return csvmap != null;
    }
    
    public String getGeometryProp() {
        return geometryProp;
    }

    public void setGeometryProp(String geometryProp) {
        this.geometryProp = geometryProp;
    }

    @Override
    public String toString() {
        return getTree().toString();
    }
    
    /**
     * Filter a view retaining only those properties in the projection 
     */
    public ViewMap project(Projection projection) {
        ViewMap map = new ViewMap(api, tree.project(projection) );
        map.initFrom(this);
        return map;
    }
     
    /**
     * Initialize values in a view from a separate view
     */
    public void initFrom(ViewMap view) {
        this.csvmap = view.getCsvMap();
        this.geometryProp = view.getGeometryProp();
    }
}
