/******************************************************************
 * File:        EndpointSpecFactory.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints;

import static com.epimorphics.simpleAPI.core.ConfigConstants.LIMIT;
import static com.epimorphics.simpleAPI.core.ConfigConstants.PREFIXES;
import static com.epimorphics.simpleAPI.core.ConfigConstants.QUERY;
import static com.epimorphics.simpleAPI.core.ConfigConstants.SOFT_LIMIT;
import static com.epimorphics.simpleAPI.core.ConfigConstants.TYPE;
import static com.epimorphics.simpleAPI.core.ConfigConstants.TYPE_ITEM;
import static com.epimorphics.simpleAPI.core.ConfigConstants.TYPE_LIST;
import static com.epimorphics.simpleAPI.core.ConfigConstants.VIEW;
import static com.epimorphics.simpleAPI.core.ConfigConstants.VIEWS;
import static com.epimorphics.simpleAPI.core.ConfigConstants.URL;
import static com.epimorphics.simpleAPI.core.ConfigConstants.BASE_QUERY;
import static com.epimorphics.simpleAPI.core.ConfigConstants.TEMPLATE;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.impl.SparqlEndpointSpec;
import com.epimorphics.simpleAPI.endpoints.impl.SparqlListEndpointSpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.util.EpiException;

/**
 * Support for creating endpoint spec objects by parsing a json/yaml source.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class EndpointSpecFactory {

    public static SparqlEndpointSpec parse(API api, String filename, JsonValue json) {
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            String type =  JsonUtil.getStringValue(jo, TYPE, TYPE_ITEM);
            SparqlEndpointSpec spec = new SparqlEndpointSpec(api);
            if (TYPE_ITEM.equals(type)) {
                if (jo.hasKey(QUERY)) {
                    spec.setBaseQuery( JsonUtil.getStringValue(jo, QUERY) );
                }
            } else if (TYPE_LIST.equals(type)) {
                SparqlListEndpointSpec lspec = new SparqlListEndpointSpec(api);
                spec = lspec;
                if (jo.hasKey(BASE_QUERY)) {
                    lspec.setBaseQuery( JsonUtil.getStringValue(jo, BASE_QUERY) );
                }
                if (jo.hasKey(LIMIT)) {
                    lspec.setHardLimit( JsonUtil.getIntValue(jo, LIMIT, Integer.MAX_VALUE) );
                }
                if (jo.hasKey(SOFT_LIMIT)) {
                    lspec.setSoftLimit( JsonUtil.getIntValue(jo, SOFT_LIMIT, Integer.MAX_VALUE) );
                }
            } else {
                throw new EpiException("Did not recognize type of endpoint configuration " + type + " in " + filename);
            }
            if (jo.hasKey(PREFIXES)) {
                JsonObject prefixes = jo.get(PREFIXES).getAsObject();
                for (String key : prefixes.keys()) {
                    spec.addLocalPrefix(key, JsonUtil.getStringValue(prefixes, key));
                }
            }
            if (jo.hasKey(VIEW)) {
                spec.setView( ViewMap.parseFromJson(api, spec.getPrefixes(), jo.get(VIEW)) );
            }
            if (jo.hasKey(VIEWS)) {
                JsonValue views = jo.get(VIEWS);
                if (views.isArray() || views.isString()) {
                    spec.setView( ViewMap.parseFromJson(api, spec.getPrefixes(), views) );
                } else if (views.isObject()) {
                    JsonObject viewsO = views.getAsObject();
                    for (String key : viewsO.keys()) {
                        spec.addView(key, ViewMap.parseFromJson(api, spec.getPrefixes(), viewsO.get(key)));
                    }
                }
            }
            if (jo.hasKey(URL)) {
                String url = JsonUtil.getStringValue(jo, URL);
                if (url == null) {
                    throw new EpiException("Could not parse url field, should be a string: " + jo.get(URL));
                }
                spec.setUrl(url);
            }
            if (jo.hasKey(TEMPLATE)) {
                spec.setTemplateName( JsonUtil.getStringValue(jo, TEMPLATE) );
            }
            return spec;
        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }
    }
}
