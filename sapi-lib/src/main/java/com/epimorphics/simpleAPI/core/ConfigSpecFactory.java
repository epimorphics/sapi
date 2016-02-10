/******************************************************************
 * File:        EndpointSpecFactory.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import static com.epimorphics.simpleAPI.core.ConfigConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.yaml.snakeyaml.Yaml;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.endpoints.EndpointSpecFactory;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;

/**
 * Utility to load/parse specification objects which might be list or item endpoints
 * or view mapping.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ConfigSpecFactory {
    
    /**
     * Load a json/yaml endpoint configuration from a jar resource
     */
    public static ConfigItem readResource(API api, String filename) {
        InputStream is = SpecMonitor.class.getClassLoader().getResourceAsStream(filename);
        return read(api, filename, is);
    }
    
    /**
     * Load a json/yaml endpoint configuration from a named file.
     */
    public static ConfigItem read(API api, String filename) {
        try {
            return read(api, filename, new FileInputStream(filename));
        } catch (IOException e) {
            throw new EpiException("Could not read " + filename, e);
        }
    }

    /**
     * Load a json/yaml endpoint configuration
     */
    public static ConfigItem read(API api, String filename, InputStream is) {
        JsonValue json = null;
        if (filename.endsWith(".yaml")) {
            json = JsonUtil.asJson( new Yaml().load(is) ) ;
        } else if (filename.endsWith(".json")) {
            json = JSON.parseAny(is);
        } else {
            // Ignore other files, useful for hiding old endpoints
            return null;
        }
        return parse(api, filename, json);
    }
    
    /**
     * Parse a json/yaml specification
     */
    public static ConfigItem parse(API api, String filename, JsonValue json) {
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            String name = JsonUtil.getStringValue(jo, NAME, NameUtils.removeExtension( new File(filename).getName() ) );
            String type = JsonUtil.getStringValue(jo, TYPE);
            ConfigItem config  = null;
            if ( TYPE_VIEW.equals(type) ) { 
                if (jo.hasKey(MAPPING)) {
                    config = ViewMap.parseFromJson(api, api.getPrefixes(), jo.get(MAPPING));
                } else if (jo.hasKey(VIEW)) {
                    try {
                        config = ViewMap.parseFromJson(api, api.getPrefixes(), jo.get(VIEW));
                    } catch (EpiException e) {
                        throw new EpiException("Problem parsing " + filename + ": " + e.getMessage());
                    }
                } else {
                    throw new EpiException("Illegal view specification, no mapping declared: " + filename);                    
                }
            } else if ( TYPE_ITEM.equals(type) || TYPE_LIST.equals(type) ) {
                try {
                    config = EndpointSpecFactory.parse(api, filename, json);
                } catch (EpiException e) {
                    throw new EpiException("Problem parsing " + filename + ": " + e.getMessage());
                }
            } else {
                throw new EpiException("Illegal config specification, no type declared: " + filename);
            }
            config.setName(name);
            return config;
        } else {
            throw new EpiException("Illegal config specification: expected a json object, in " + filename);
        }
    }

}
