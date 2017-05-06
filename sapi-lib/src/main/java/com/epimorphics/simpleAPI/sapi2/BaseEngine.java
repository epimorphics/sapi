/******************************************************************
 * File:        BaseEngine.java
 * Created by:  Dave Reynolds
 * Created on:  27 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.sapi2;

import static com.epimorphics.simpleAPI.core.ConfigConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.Engine;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.requests.AliasRequestProcessor;
import com.epimorphics.simpleAPI.requests.FilterRequestProcessor;
import com.epimorphics.simpleAPI.requests.LimitRequestProcessor;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.requests.RequestProcessor;
import com.epimorphics.simpleAPI.requests.SortRequestProcessor;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.util.EpiException;

public class BaseEngine implements Engine {
    
    protected List<RequestProcessor> requestProcessors = new ArrayList<>();
    protected List<RequestProcessor> allRequestProcessors;
    
    // Configure built in standard request handlers here
    protected static final RequestProcessor[] standardRequestProcessors = new RequestProcessor[] {
//            new GeoRequestProcessor(),
//            new SearchRequestProcessor(),
            new AliasRequestProcessor(),
            new FilterRequestProcessor(),
            new SortRequestProcessor(),
            new LimitRequestProcessor()
    };

    @Override
    public EndpointSpec parse(API api, String filename, JsonValue json) {
        
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            String type =  JsonUtil.getStringValue(jo, TYPE, TYPE_ITEM);
            
            if (TYPE_ITEM.equals(type)) {
                try {
                    Sapi2ItemEndpointSpec spec = new Sapi2ItemEndpointSpec(api);
                    parseCommonParameters(api, spec, jo);
                    return spec;
                } catch (Exception e) {
                    throw new EpiException("Problem parsing: " + filename + ": " + e.getMessage());
                }

            } else if (TYPE_LIST.equals(type)) {
                try {
                    Sapi2ListEndpointSpec spec = new Sapi2ListEndpointSpec(api);
                    parseListParameters(api, spec, jo);
                    parseCommonParameters(api, spec, jo);
                    return spec;
                } catch (Exception e) {
                    throw new EpiException("Problem parsing: " + filename + ": " + e.getMessage());
                }

            } else {
                throw new EpiException("Did not recognize type of endpoint configuration " + type + " in " + filename);
            }

        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }        

    }
    
    protected void parseListParameters(API api, Sapi2ListEndpointSpec lspec, JsonObject jo) {
        if (jo.hasKey(BASE_QUERY)) {
            lspec.setBaseQuery( JsonUtil.getStringValue(jo, BASE_QUERY) );
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
        if( jo.hasKey( DISTINCT ) ) {
            lspec.setUseDistinct( JsonUtil.getBooleanValue(jo, DISTINCT, false) );
        }
        if( jo.hasKey( NESTED_SELECT_VARS ) ) {
            List<String> nestedVars = null;
            JsonValue nsv = jo.get(NESTED_SELECT_VARS);
            if (nsv.isString()) {
                nestedVars = Collections.singletonList( nsv.getAsString().value() );
            } else if (nsv.isArray()) {
                nestedVars = new ArrayList<>();
                for (Iterator<JsonValue> i = nsv.getAsArray().iterator(); i.hasNext();) {
                    JsonValue v = i.next();
                    if ( v.isString() ) {
                        nestedVars.add( v.getAsString().value() );
                    } else {
                        throw new EpiException("Could not parse nestedSelect vars, must be string or array of strings");
                    }
                }
            } else {
                throw new EpiException("Could not parse nestedSelect vars, must be string or array of strings");
            }
            lspec.setAdditionalProjectionVars( nestedVars );
            lspec.setUseNestedSelect( JsonUtil.getBooleanValue(jo, NESTED_SELECT, false) );
        }
        if( jo.hasKey( SUPPRESSID ) ) {
            lspec.setSuppressID( JsonUtil.getBooleanValue(jo, SUPPRESSID, false) );
        } 
        
        if (jo.hasKey(PROCESSORS)) {
            JsonValue jv = jo.get(PROCESSORS);
            List<String> processors = new ArrayList<>();
            if (jv.isString()) {
                processors.add( jv.getAsString().value() );
            } else if (jv.isArray()) {
                for (JsonValue v : jv.getAsArray()) {
                    if (v.isString()) {
                        processors.add( v.getAsString().value() );
                    } else {
                        throw new EpiException("Processors should be string or array of strings");
                    }
                }
            } else {
                throw new EpiException("Processors should be string or array of strings");
                
            }
            for (String processor : processors) {
                RequestProcessor rp = api.getApp().getComponentAs(processor, RequestProcessor.class);
                if (rp == null) {
                    throw new EpiException("Can't find request processsor: " + processor);
                }
                lspec.addRequestProcessor(rp);
            }
        }

    }

    
    protected void parseCommonParameters(API api, Sapi2BaseEndpointSpec spec, JsonObject jo) {
        spec.setEngine(this);
        
        if (jo.hasKey(QUERY)) {
            spec.setCompleteQuery( JsonUtil.getStringValue(jo, QUERY) );
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
            spec.setTransform(name);
        }
        
        if (jo.hasKey(TEMPLATE)) {
            spec.setTemplateName( JsonUtil.getStringValue(jo, TEMPLATE) );
        }
        
        if (jo.hasKey(ITEM_NAME)) {
            spec.setItemName( JsonUtil.getStringValue(jo, ITEM_NAME) );
        }        
       
        if (jo.hasKey(ALIAS)) {
            JsonValue jv = jo.get(ALIAS);
            if (jv.isObject()) {
                @SuppressWarnings("unchecked")
                Map<String,String> aliases = (Map<String, String>) JsonUtil.fromJson(jv);
                for (Map.Entry<String, String> map : aliases.entrySet()) {
                    spec.addAlias(map.getKey(), map.getValue());
                }
            } else {
                throw new EpiException("Alias should be specified as a json object (treated as a map from key to value)");
            }
        }
        
        if (jo.hasKey(BINDINGS)) {
            JsonValue jv = jo.get(BINDINGS);
            if (jv.isObject()) {
                for( Entry<String, JsonValue> b : jv.getAsObject().entrySet() ) {
                    JsonValue value = b.getValue();
                    if (value.isString()) {
                        spec.addBinding(b.getKey(), value.getAsString().value());
                    } else if (value.isNumber()) {
                        spec.addBinding(b.getKey(), value.getAsNumber().value().toString());
                    } else {
                        throw new EpiException("Binding value must be a string");
                    }
                }
            } else {
                throw new EpiException("Bindings should be a json object (treated as a map from parameter to value)");
            }
        }
    }

    @Override
    public QueryBuilder finalizeQueryBuilder(Request request, QueryBuilder builder, EndpointSpec spec) {
        if (spec instanceof Sapi2ListEndpointSpec) {
            ListQueryBuilder lbuilder = (ListQueryBuilder)builder;
            for (RequestProcessor proc : ((Sapi2ListEndpointSpec)spec).getRequestProcessors()) {
                lbuilder = proc.process(request, lbuilder, spec);
            }
            for (RequestProcessor proc : getRequestProcessors()) {
                lbuilder = proc.process(request, lbuilder, spec);
            }
            return lbuilder;
        } else {
            return builder;
        }
    }
    
    // ---- Support for request processing handlers ------------------------------------

    public void setRequestProcessor(RequestProcessor processor) {
        requestProcessors.add(processor);
    }
    
    public void setRequestProcessors(List<RequestProcessor> processors) {
        requestProcessors.addAll(processors);
    }
    
    public List<RequestProcessor> getRequestProcessors() {
        if (allRequestProcessors == null) {
            allRequestProcessors = new ArrayList<>( requestProcessors );
            for (RequestProcessor proc : standardRequestProcessors) {
                allRequestProcessors.add(proc);
            }
        }
        return allRequestProcessors;
    }

}
