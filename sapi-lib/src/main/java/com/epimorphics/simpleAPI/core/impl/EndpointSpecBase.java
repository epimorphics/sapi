/******************************************************************
 * File:        EndpointSpecBase.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.EndpointSpec;
import com.epimorphics.simpleAPI.core.EndpointSpecFactory;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Base implementation for EndpointSpecs.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class EndpointSpecBase implements EndpointSpec {
    protected API api;
    protected JsonObject config;
    protected JSONMap map;
    protected String rawQuery;   // Optional, query may be constructed from mapping specification
    protected PrefixMapping localPrefixes;
    protected PrefixMapping prefixes;
    
    public EndpointSpecBase(API api, JsonObject config) {
        this.api = api;
        this.config = config;
    }
    
    public void setQueryTemplate(String query) {
        this.rawQuery = query;
    }
    
    @Override
    public API getAPI() {
        return api;
    }

    @Override
    public JsonObject getMetadata() {
        return config;
    }

    @Override
    public JSONMap getMap() {
        return map;
    }
    
    @Override
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
    
    public void addLocalPrefix(String prefix, String uri) {
        if (localPrefixes == null) {
            localPrefixes = PrefixMapping.Factory.create();
        }
        localPrefixes.setNsPrefix(prefix, uri);
    }
    
    @Override
    public abstract String getQuery(RequestParameters request);

    protected String expandPrefixes(String query) {
        return PrefixUtils.expandQuery(query, getPrefixes());
    }
    
    public String getItemName() {
        return JsonUtil.getStringValue(config, EndpointSpecFactory.ITEM_NAME, "items");
    }

    public String getName() {
        return JsonUtil.getStringValue(config, EndpointSpecFactory.NAME);
    }

    public void setMapping(JSONMap map) {
        this.map = map;
    }
       
    @Override
    public List<String> getFormatNames() {
        List<String> formats = new ArrayList<String>();
        if (config.hasKey(EndpointSpecFactory.HAS_FORMAT)) {
            JsonValue format = config.get(EndpointSpecFactory.HAS_FORMAT);
            if (format.isString()) {
                formats.add(format.getAsString().value());
            } else if (format.isArray()) {
                for (Iterator<JsonValue> i = format.getAsArray().iterator(); i.hasNext();) {
                    formats.add( i.next().getAsString().value() );
                }
            }
        }
        return formats;
    }
    
    public List<String> getFormats(String request, String skipFormat) {
        List<String> formats = new ArrayList<>();
        for (String format : getFormatNames()) {
            if (skipFormat.equals(format))
                continue;
            Matcher m = URIPAT.matcher(request);
            if (m.matches()) {
                formats.add(m.group(1) + "." + format
                        + (m.group(3) == null ? "" : m.group(3)));
            } else {
                formats.add(request + "." + format);
            }
        }
        return formats;
    }
    
    /** Output and array of alternative hasFormat values */
    public void writeFormats(String request, JSFullWriter out) {
        List<String> formats = getFormats(request, "json");
        if (!formats.isEmpty()) {
            out.key("hasFormat");
            out.startArray();
            for (String f : formats) {
                out.arrayElement(f);
            }
            out.finishArray();
        }
    }

    protected static final Pattern URIPAT = Pattern
            .compile("([^?]*)(\\.[a-z]*)?(\\?.*)?");    
}
