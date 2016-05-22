/******************************************************************
 * File:        EndpointSpecFactory.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints;

import static com.epimorphics.simpleAPI.core.ConfigConstants.*;

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
                    spec.setCompleteQuery( JsonUtil.getStringValue(jo, QUERY) );
                }
            } else if (TYPE_LIST.equals(type)) {
                SparqlListEndpointSpec lspec = new SparqlListEndpointSpec(api);
                spec = lspec;
                if (jo.hasKey(BASE_QUERY)) {
                    lspec.setBaseQuery( JsonUtil.getStringValue(jo, BASE_QUERY) );
                }
                if (jo.hasKey(QUERY)) {
                    spec.setCompleteQuery( JsonUtil.getStringValue(jo, QUERY) );
                }
                if (jo.hasKey(LIMIT)) {
                    lspec.setHardLimit( JsonUtil.getIntValue(jo, LIMIT, Integer.MAX_VALUE) );
                }
                if (jo.hasKey(SOFT_LIMIT)) {
                    lspec.setSoftLimit( JsonUtil.getIntValue(jo, SOFT_LIMIT, Integer.MAX_VALUE) );
                }
                if (jo.hasKey(GEO)) {
                    JsonValue jv = jo.get(GEO);
                    if (jv.isBoolean() && jv.getAsBoolean().value()) {
                        lspec.setGeoSearch( new JsonObject() );
                    } else if (jv.isObject()) {
                        lspec.setGeoSearch( jv.getAsObject() );
                    } else {
                        throw new EpiException("Could not parse geoSearch specification, must be boolean or object");
                    }
                }
                if (jo.hasKey(TEXT_SEARCH)) {
                    JsonValue jv = jo.get(TEXT_SEARCH);
                    if (jv.isBoolean() && jv.getAsBoolean().value()) {
                        lspec.setTextSearchRoot(ROOT_VAR);
                    } else if (jv.isString()) {
                        lspec.setTextSearchRoot( jv.getAsString().value() );
                    } else {
                        throw new EpiException("Could not parse textSearch specification, must be boolean or a string given the variable to search on");
                    }
                }
                if( jo.hasKey( FLATTEN_PATH ) ) {
                    lspec.setFlattenPath( JsonUtil.getStringValue(jo, FLATTEN_PATH) );
                }
                if( jo.hasKey( NESTED_SELECT ) ) {
                    lspec.setUseNestedSelect( JsonUtil.getBooleanValue(jo, NESTED_SELECT, false) );
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
            
            if (jo.hasKey(TRANSFORM)) {
            	String name = JsonUtil.getStringValue(jo, TRANSFORM);
            	spec.useTransformByClassName(name);
            }
            
            if (jo.hasKey(TEMPLATE)) {
                spec.setTemplateName( JsonUtil.getStringValue(jo, TEMPLATE) );
            }
            
            if (jo.hasKey(ITEM_NAME)) {
                spec.setItemName( JsonUtil.getStringValue(jo, ITEM_NAME) );
            }
            return spec;
        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }
    }
}
