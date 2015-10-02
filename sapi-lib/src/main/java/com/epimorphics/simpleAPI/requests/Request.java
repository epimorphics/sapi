/******************************************************************
 * File:        RequestParameters.java
 * Created by:  Dave Reynolds
 * Created on:  1 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.core.API;

/**
 * Encapsulates a query request, whether from query parameters, path parameters
 * or a POSTed json request.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Request {
    protected String requestedURI;
    protected MultivaluedMap<String, String> parameters = new MultivaluedStringMap();
    
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
     * Remove the values for a parameter. As parameters are processed they are normally
     * removed from the request.
     */
    public void remove(String parameter) {
        parameters.remove(parameter);
    }
    
    public void setRequestedURI(String requestedURI) {
        this.requestedURI = requestedURI;
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
     * Construct a request object from the URI, query and path parameters in a jersey call
     */
    public static Request from(API api, UriInfo uriInfo) {
        String requestedURI = api.getBaseURI() + uriInfo.getPath();
        Request request = new Request(requestedURI, uriInfo.getQueryParameters());
        request.addAll(uriInfo.getPathParameters());
        return request;
    }

    /**
     * Construct a request object a jersey call plus a json object (probably passed in to a POST request).
     * Assumes no nesting of the json object
     */
    public static Request from(API api, UriInfo uriInfo, JsonObject postargs) {
        Request request = from(api, uriInfo);
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