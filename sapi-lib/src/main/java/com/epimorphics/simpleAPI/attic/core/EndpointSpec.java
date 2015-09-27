/******************************************************************
 * File:        APIConfig.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.attic.core;

import java.util.List;

import org.apache.jena.atlas.json.JsonObject;

import org.apache.jena.shared.PrefixMapping;

/**
 * Encapsulates the specification for an API endpoint. The specification includes information on:
 * <ul>
 *   <li>the query to run (an explicit query template or an implicit query built from the JSON map</li>
 *   <li>JSON map guiding how the query result should be mapped to JSON</li>
 *   <li>metadata (in the form of a JSON Object) which might be used in documenting the API endpoint</li>
 * </ul>
 * <p>Sub-interfaces provide a streaming writer for the endpoint based on the type (item/list) and
 * the configured JSON mapping.</p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface EndpointSpec {

    /**
     * Return the parent API associated with this endpoint 
     */
    public API getAPI();

    /**
     * Return a name for this endpoint, the name is used to retrieve the spec from the parent API, 
     * it is not directly visible to API users
     */
    public String getName();
   
    /**
     * Return metadata on the query (which can include all the original configuration properties).
     */
    public JsonObject getMetadata();
    
    /**
     * Return the prefix bindings defined for this Endpoint (including the App-wide prefixes)
     */
    public PrefixMapping getPrefixes();
    
    /**
     * Return the query template with no instantiation.
     */
    public String getQuery( );
    
    /**
     * Return the query after instantiating it according to the supplied
     * request parameters.
     */
    public String getQuery( RequestParameters request );
    
    /**
     * Return the JSON mapping specification which controls serializations from this endpoint
     */
    public JSONMap getMap();
    
    /**
     * Return the list of formats available for this endpoint (as strings given the filename extension for the format)
     * May be empty in no formats are specified.
     */
    public List<String> getFormatNames();
    
    /**
     * Return the list of formats available for this endpoint (as strings given the request URI with the
     * format extension added
     */
    public List<String> getFormats(String request, String skipFormat);
}
