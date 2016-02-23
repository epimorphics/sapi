/******************************************************************
 * File:        LibAPI.java
 * Created by:  Dave Reynolds
 * Created on:  23 Feb 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.templates.LibPlugin;
import com.epimorphics.simpleAPI.results.wappers.WJSONArray;
import com.epimorphics.simpleAPI.results.wappers.WJSONObject;

/**
 * Plugin for the Lib templating library to support API related actions.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class LibAPI extends ComponentBase implements LibPlugin {
    static final Logger log = LoggerFactory.getLogger( LibAPI.class );
    
    /**
     * Issue a GET request the given URL which is expected to be a sapis tyle API.
     * Returns the list of items as wrapped JSON objects.
     * Returns null in the event of any sort of error.
     */
    public Object fetchJSON(String url) {
        try{
            Client client = ClientBuilder.newClient();
            Response response = client.target( url )
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
            if (response.getStatus() != 200) {
                log.warn( String.format("FetchJSON: %s - %d - %s", url, response.getStatus(), response.readEntity(String.class) ) );
                return null;
            }
            JsonObject json = response.readEntity( JsonObject.class );
            JsonArray items = json.getJsonArray("items");
            return asWJSON(items);
        } catch (Exception e) {
            log.warn( "FetchJSON error on " + url + " - " + e);
            return null;
        }
    }
    
    private Object asWJSON( JsonValue value ) {
        if (value instanceof JsonObject) {
            JsonObject jo = (JsonObject) value;
            WJSONObject result = new WJSONObject();
            for (String key : jo.keySet()) {
                result.put(key, asWJSON( jo.get(key) ) ); 
            }
            return result;
        } else if (value instanceof JsonArray) {
            WJSONArray result = new WJSONArray();
            for (JsonValue v : ((JsonArray)value)) {
                result.add( asWJSON(v) );
            }
            return result;
        } else {
            return value;
        }
    }

}
