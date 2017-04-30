/******************************************************************
 * File:        EndpointSpecBase.java
 * Created by:  Dave Reynolds
 * Created on:  2 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import static com.epimorphics.simpleAPI.core.ConfigConstants.GEO_ALGORITHM;
import static com.epimorphics.simpleAPI.core.ConfigConstants.GEO_PARAMETER;
import static com.epimorphics.simpleAPI.core.ConfigConstants.ROOT_VAR;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigConstants;
import com.epimorphics.simpleAPI.core.ConfigItem;
import com.epimorphics.simpleAPI.core.Engine;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.util.PrefixUtils;

/**
 * Provides some useful common methods for implementing EndpointSpecs
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class EndpointSpecBase extends ConfigItem implements EndpointSpec {
    protected API api;
    protected String url;
    protected Map<String, ViewMap> views = new HashMap<>();
    protected PrefixMapping localPrefixes;
    protected PrefixMapping prefixes;
    protected String templateName;
    protected JsonObject geoSearch;
    protected String textSearchRoot = ROOT_VAR;
    protected String itemName;
    protected String flattenPath;
    protected boolean suppressID;
    protected Engine engine;

    public EndpointSpecBase(API api) {
        super();
        this.api = api;
    }
    
    /**
     * Return the API instance this endpoint is associated with
     */
    @Override
    public API getAPI() {
        return api;
    }
    
    /**
     * Return the URL pattern for this endpoint, relative to the base of the API.
     * May be null if the endpoint is configured by code
     */
    @Override
    public String getURL() {
        return url;
    }
    
    /**
     * Return the view, if any, which controls formating of query results
     */
    @Override
    public ViewMap getView() {
        return getView(DEFAULT_VIEWNAME);
    }
    
    /**
     * Return a named view which controls formating of query results
     */
    public ViewMap getView(String viewname) {
        return views.get(viewname);
    }
    
    /**
     * List all available views names
     */
    public Collection<String> listViewNames() {
        return views.keySet();
    }

    @Override public QueryBuilder getQueryBuilder() {
        return getQueryBuilder(DEFAULT_VIEWNAME);
    }
    
    @Override
    public QueryBuilder getQueryBuilder(Request request) {   
        
        QueryBuilder builder = getQueryBuilder( request.getViewName() );
        if (builder instanceof ListQueryBuilder) {
            return builder;
        } else {
            return builder.bind(ConfigConstants.ROOT_VAR, ResourceFactory.createResource(request.getRequestedURI()));
        }
    }
    
    abstract public QueryBuilder getQueryBuilder(String name);
    
    /**
     * Finalize a query builder by running the query processors
     * configured for this API (e.g. applying limits, generic filters, geoqueries etc)
     */
    @Override
    public QueryBuilder finalizeQueryBuilder( QueryBuilder builder, Request request ) {
        return getEngine().finalizeQueryBuilder(request, builder, this  );
    }
    
    /**
     * Return the prefix bindings defined for this Endpoint (including the App-wide prefixes)
     */
    public PrefixMapping getPrefixes() {
        if (prefixes == null) {
            prefixes = api.getPrefixes();
            if (prefixes == null) {
                prefixes = localPrefixes;
            } else if (localPrefixes != null) {
                prefixes = PrefixUtils.merge(prefixes, localPrefixes);
            }
        }
        return prefixes;
    }

    /**
     * Add prefix declaration specific to this endpoint
     */
    public void addLocalPrefix(String prefix, String uri) {
        if (localPrefixes == null) {
            localPrefixes = PrefixMapping.Factory.create();
        }
        localPrefixes.setNsPrefix(prefix, uri);
    }

    public void setApi(API api) {
        this.api = api;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setView(ViewMap view) {
        views.put(DEFAULT_VIEWNAME, view);
    }
    
    public void addView(String viewname, ViewMap view) {
        views.put(viewname, view);
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public void setGeoSearch(JsonObject geoSearch) {
        this.geoSearch = geoSearch;
    }
    
    public JsonObject getGeoSearch() {
        return geoSearch;
    }

    public String getGeoParameter() {
        if (geoSearch != null) {
            return JsonUtil.getStringValue(geoSearch, GEO_PARAMETER, ROOT_VAR);
        } else {
            return null;
        }
    }
    
    public String getGeoAlgorithm() {
        if (geoSearch != null) {
            return JsonUtil.getStringValue(geoSearch, GEO_ALGORITHM, "withinBox");
        } else {
            return null;
        }
        
    }

    public String getTextSearchRoot() {
        return textSearchRoot;
    }

    public void setTextSearchRoot(String textSearchRoot) {
        this.textSearchRoot = textSearchRoot;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String getFlattenPath() {
        return flattenPath;
    }

    /**
     * Set a comma-separated list of paths that, if multi-valued, should
     * force duplication of records in flag notations such as CSV
     */
    public void setFlattenPath(String flattenPath) {
        this.flattenPath = flattenPath;
    }

    @Override
    public boolean isSuppressID() {
        return suppressID;
    }

    public void setSuppressID(boolean suppressID) {
        this.suppressID = suppressID;
    }

    public Engine getEngine() {
        if (engine == null) {
            engine = api.getDefaultEngine();
        }
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
    
}
