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

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.query.impl.SparqlListQuery;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.util.EpiException;

/**
 * Support for creating endpoint spec objects by parsing a json/yaml source.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class EndpointSpecFactory {

    public static EndpointSpec parse(API api, String filename, JsonValue json) {
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            String type =  JsonUtil.getStringValue(jo, TYPE, TYPE_ITEM);
            EndpointSpec spec = new EndpointSpec(api);
            if (TYPE_ITEM.equals(type)) {
                // TODO
            } else if (TYPE_LIST.equals(type)) {
                ListEndpointSpec lspec = new ListEndpointSpec(api);
                spec = lspec;
                if (jo.hasKey(QUERY)) {
                    lspec.setQuery( new SparqlListQuery( JsonUtil.getStringValue(jo, QUERY) ) );
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
                spec.setView( ViewMap.parseFromJson(api, jo.get(VIEW)) );
            }
            return spec;
        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }
    }
}
