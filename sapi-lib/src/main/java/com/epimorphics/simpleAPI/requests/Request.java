/******************************************************************
 * File:        RequestParameters.java
 * Created by:  Dave Reynolds
 * Created on:  1 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;

/**
 * Encapsulates a query request, whether from query parameters, path parameters
 * or a POSTed json request.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Request {
    public static final String VIEW_KEY = "_view";
    
    public static final String BINDING_KEY_URI = "uri";
    public static final String BINDING_KEY_REQUEST = "request";
    public static final String BINDING_KEY_BASEURI = "baseURI";
    
    protected String requestedURI;
    protected String fullRequestedURI;
    protected MultivaluedMap<String, String> parameters = new MultivaluedStringMap();
    protected Set<String> consumed = new HashSet<>();
    protected Map<String, Object> bindings = new HashMap<>();
    
    public Request() {
    }
    
    public Request(String requestedURI) {
        this.requestedURI = requestedURI;
    }
    
    public Request(String requestedURI, MultivaluedMap<String, String> callparams) {
        this.requestedURI = requestedURI;
        addAll(callparams);
    }
    
    /**
     * Return the requested URI, this may not be the same as the actually URL
     * through which the request arrived but will have been mapped to the 
     * configured base URI for the service.
     */
    public String getRequestedURI() {
        return requestedURI;
    }
    
    /**
     * Return the requested URI, this may not be the same as the actually URL
     * through which the request arrived but will have been mapped to the 
     * configured base URI for the service. Includes any query parameters.
     */
    public String getFullRequestedURI() {
        return fullRequestedURI == null ?  requestedURI : fullRequestedURI;
    }
    
    /**
     * Return a collection of the parameters in the request
     */
    public Collection<String> getParameters() {
        return parameters.keySet();
    }
    
    /**
     * Test if the parameter is present (it may have an empty value)
     */
    public boolean hasParameter(String parameter) {
        return parameters.containsKey(parameter);
    }
    
    /**
     * Test if the parameter is present and not consumed
     */
    public boolean hasAvailableParameter(String parameter) {
        return parameters.containsKey(parameter) && !consumed.contains(parameter);
    }
    
    /**
     * Return the values for the given parameter
     */
    public List<String> get(String parameter) {
        return parameters.get(parameter);
    }
    
    /**
     * Return a single value for the given parameter
     */
    public String getFirst(String parameter) {
        return parameters.getFirst(parameter);
    }
    
    /**
     * Return all the parameter values
     */
    public MultivaluedMap<String, String> getParametersMap() {
        return parameters;
    }
    
    /**
     * Return a single value for the given parameter as an long,
     * or null if there is no such value.
     * If value exists but is not a long throws a WebApiExpception signalling a bad request
     */
    public Long getAsLong(String parameter) {
        String value = parameters.getFirst(parameter);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new WebApiException(Status.BAD_REQUEST, "Illegal parameter format for " + parameter);
            }
        } else {
            return null;
        }
    }
    
    /**
     * Return a single value for the given parameter as an double,
     * or null if there is no such value.
     * If value exists but is not a double throws a WebApiExpception signalling a bad request
     */
    public Double getAsDouble(String parameter) {
        String value = parameters.getFirst(parameter);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new WebApiException(Status.BAD_REQUEST, "Illegal parameter format for " + parameter);
            }
        } else {
            return null;
        }
    }
    
    /**
     * Return a single value for the given parameter as an number (Double or Long)
     * or null if there is no such value.
     * If value exists but is not a number throws a WebApiExpception signalling a bad request
     */
    public Number getAsNumber(String parameter) {
        String value = parameters.getFirst(parameter);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e2) {
                    throw new WebApiException(Status.BAD_REQUEST, "Illegal parameter format for " + parameter);
                }
            }
        } else {
            return null;
        }
    }
    
    /**
     * Return a single value for the given parameter as a boolean (with the given default)
     */
    public boolean getAsBoolean(String parameter, boolean dflt) {
        String value = parameters.getFirst(parameter);
        if (value != null) {
            return value.equalsIgnoreCase("true");
        } else {
            return dflt;
        }
    }
    
    /**
     * Return a single value for the given parameter as a lucene query.
     * Assumes the request is a set of strings and returns a conjunctive
     * wildcard query over them, with ' characters escape for embedding in SPARQL.
     */
    public String getAsLuceneQuery(String parameter) {
        String value = parameters.getFirst(parameter);
        if (value == null) return null; 
        String[] words = value.split("([\\+\\-\\&\\|!\\(\\)\\{\\}\\[\\]^\"~/\\.,]|\\s)+");
        // Note this doesn't include ' which may need to be treated as a special case
        
        String query = null;
        if (words.length >= 1) {
            query = "(";
            int i = 0;
            while (i < words.length -1) {
                query += words[i++] + "* AND ";
            }
            query += words[i] + "*)";
        } else {
            query =  words[0] + "*";
        }
        return query.replace("'", "\\'");
    }
    
    /**
     * Note that a parameter has been dealt with
     */
    public void consume(String parameter) {
        consumed.add(parameter);
    }
    
    /**
     * Unconsume a parameter. Useful when temporarily hiding
     * parameters from some processing
     */
    public void unhide(String parameter) {
        consumed.remove(parameter);
    }
    
    /**
     * Return all parameters that have not be consumed
     */
    public List<String> getRemainingParameters() {
        List<String> remainder = new ArrayList<>( parameters.keySet() );
        remainder.removeAll(consumed);
        for (Iterator<String> i = remainder.iterator(); i.hasNext(); ) {
            String p = i.next();
            if (p.startsWith("__")) {
                // This is an internal parameter, e.g. using in path regex, and doesn't count as an external request
                i.remove();
            }
        }
        return remainder;
    }
    
    public void setRequestedURI(String requestedURI) {
        this.requestedURI = requestedURI;
    }
    
    public void setFullRequestedURI(String fullRequestedURI) {
        this.fullRequestedURI = fullRequestedURI;
    }
    
    public void add(String parameter, String value) {
        parameters.add(parameter, value);
    }
    
    public void addAll(MultivaluedMap<String, String> callparams) {
        for (String key : callparams.keySet()) {
            parameters.addAll(key, callparams.get(key));
        }
    }

    public void addAll(String parameter, List<String> values) {
        parameters.addAll(parameter, values);
    }

    /**
     * Add an additional binding to be passed into the HTML rendering context
     */
    public void addRenderBinding(String key, Object value) {
        bindings.put(key, value);
    }
    
    /**
     * Return the bindings that should be included in the HTML rendering context
     * on top of the minimal default (app, lib, components, root)
     */
    public Map<String, Object> getRenderBindings() {
        for (String parameter : parameters.keySet()) {
            List<String> values = parameters.get(parameter);
            if (values.size() == 1) {
                safeAddRenderBinding(parameter, values.get(0));
            } else {
                safeAddRenderBinding(parameter, values);
            }
        }
        safeAddRenderBinding(BINDING_KEY_URI, getRequestedURI());
        return bindings;
    }
    
    protected void safeAddRenderBinding(String key, Object value) {
        if (!bindings.containsKey(key)) {
            bindings.put(key, value);
        }
    }
    public String getViewName() {
        String viewname = getFirst(VIEW_KEY);
        if (viewname == null) {
            viewname = EndpointSpec.DEFAULT_VIEWNAME;
        } else {
            consumed.add(VIEW_KEY);
        }
        return viewname;
    }

    /**
     * Construct a request object from the URI, query and path parameters in a jersey call
     */
    public static Request from(API api, UriInfo uriInfo, HttpServletRequest servletRequest) {
        String requestedURI = api.getBaseURI() + uriInfo.getPath();
        Request request = new Request(requestedURI, uriInfo.getQueryParameters());
        request.addAll(uriInfo.getPathParameters());
        request.addRenderBinding(BINDING_KEY_REQUEST, servletRequest);
        String baseURI = api.getBaseURI();
        if (uriInfo != null && uriInfo.getBaseUri() != null) {
            String requestBase = uriInfo.getBaseUri().toString();
            if ( requestBase.contains("http://localhost") ) {
                // Use configured base URI unless the request is a localhost (for which we assume this is a test situation)
                baseURI = requestBase;
            }
        }
        request.addRenderBinding(BINDING_KEY_BASEURI, baseURI);
        
        String rawRequest = uriInfo.getRequestUri().toString();
        if (rawRequest.contains("?")) {
            String query = rawRequest.substring( rawRequest.indexOf('?') );
            request.setFullRequestedURI( requestedURI + query );
        }        
        return request;
    }

    /**
     * Construct a request object a jersey call plus a json object (probably passed in to a POST request).
     * Assumes no nesting of the json object
     */
    public static Request from(API api, UriInfo uriInfo, HttpServletRequest servletRequest, JsonObject postargs) {
        Request request = from(api, uriInfo, servletRequest);
        for (Map.Entry<String, JsonValue> entry : postargs.entrySet()) {
            String key = entry.getKey();
            JsonValue value = entry.getValue();
            if (value.isObject()) {
                throw new WebApiException(Status.BAD_REQUEST, "Illegal format for json request, unexpected nested object");
            } else if (value.isArray()) {
                for (Iterator<JsonValue> i = value.getAsArray().iterator(); i.hasNext();) {
                    String v = jsonToString(i.next());
                    if (v != null) {
                        request.add(key, v);
                    }
                }
            } else {
                String v = jsonToString(value);
                if (v != null) {
                    request.add(key, v);
                }
            }
        }
        return request;
    }

    /**
     * Construct a request object a jersey call plus a request body which expected to be a JSON object
     * @throws WebApiException if the request body does not parse as a JSON object
     */
    public static Request from(API api, UriInfo uriInfo, HttpServletRequest servletRequest, String postargs) {
        try {
            return from(api, uriInfo, servletRequest, JSON.parse(postargs));
        } catch (Exception e) {
            throw new WebApiException(Status.BAD_REQUEST, "Illegal JSON object in request");
        }
        
    }

    private static String jsonToString(JsonValue value) {
        if (value.isString()) {
            return value.getAsString().value();
        } else if (value.isNumber()) {
            return value.getAsNumber().value().toString();
        } else if (value.isBoolean()) {
            return Boolean.toString(value.getAsBoolean().value());
        } else {
            return null;
        }
    }
    
}
