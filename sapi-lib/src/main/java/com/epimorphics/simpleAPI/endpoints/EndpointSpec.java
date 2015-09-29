/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints;

import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigItem;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.util.PrefixUtils;

/**
 * Encapsulates the specification of a single endpoint.
 */
public class EndpointSpec extends ConfigItem {
    protected API api;
    protected Query query;
    protected String url;
    protected ViewMap view;
    protected PrefixMapping localPrefixes;
    protected PrefixMapping prefixes;
    
    public EndpointSpec(API api) {
        this.api = api;
    }
        
    /**
     * Return the API instance this endpoint is associated with
     */
    public API getAPI() {
        return api;
    }
    
    /**
     * Return the prefix bindings defined for this Endpoint (including the App-wide prefixes)
     */
    public PrefixMapping getPrefixes() {
        if (prefixes == null) {
            if (api != null && api.getApp() != null) {
                prefixes = api.getApp().getPrefixes();
            }
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

    /**
     * Return the Query which implements this endpoint
     */
    public Query getQuery() {
        return query;
    }
    
    /**
     * Return the view, if any, which controls formating of query results
     */
    public ViewMap getView() {
        return view;
    }
    
    /**
     * Return the URL pattern for this endpoint, relative to the base of the API.
     * May be null if the endpoint is configured by code
     */
    public String getURL() {
        return url;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setView(ViewMap view) {
        this.view = view;
    }

}
