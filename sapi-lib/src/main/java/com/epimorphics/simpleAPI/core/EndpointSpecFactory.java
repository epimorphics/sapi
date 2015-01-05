/******************************************************************
 * File:        EndpointFactory.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.yaml.snakeyaml.Yaml;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.impl.DescribeEndpointSpecImpl;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;

/**
 * Constructs endpoint specifications from json/yaml configuration files
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class EndpointSpecFactory {
    public static final String TYPE = "type";
    public static final String TYPE_ITEM = "item";
    public static final String TYPE_LIST = "list";
    public static final String QUERY     = "query";
    public static final String ITEM_NAME = "itemName";
    public static final String NAME      = "name";

    /**
     * Load a json/yaml endpoint configuration from a jar resource
     */
    public static EndpointSpec readResource(API api, String filename) {
        InputStream is = EndpointSpecFactory.class.getClassLoader().getResourceAsStream(filename);
        return read(api, filename, is);
    }
    
    /**
     * Load a json/yaml endpoint configuration from a named file.
     */
    public static EndpointSpec read(API api, String filename) {
        try {
            return read(api, filename, new FileInputStream(filename));
        } catch (IOException e) {
            throw new EpiException("Could not read " + filename, e);
        }
    }

    /**
     * Load a json/yaml endpoint configuration
     */
    public static EndpointSpec read(API api, String filename, InputStream is) {
        JsonValue json = null;
        if (filename.endsWith(".yaml")) {
            json = JsonUtil.asJson( new Yaml().load(is) ) ;
        } else {
            json = JSON.parseAny(is);
        }
        return parse(api, filename, json);
    }
    
    /**
     * Parse a json/yaml specification
     */
    public static EndpointSpec parse(API api, String filename, JsonValue json) {
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            if (!jo.hasKey(NAME)) {
                String defaultName = NameUtils.removeExtension( new File(filename).getName() );
                jo.put(NAME, defaultName);
            }
            if (TYPE_ITEM.equals( JsonUtil.getStringValue(jo, TYPE, TYPE_ITEM) )) {
                DescribeEndpointSpecImpl spec = new DescribeEndpointSpecImpl(api, jo);
                if (jo.hasKey(QUERY)) {
                    spec.setQueryTemplate( JsonUtil.getStringValue(jo, QUERY) );
                }
                return spec;
            } else {
                // TODO
                return null;
            }
        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }
    }
    
    /**
     * Construct a simple describe specification from an explicit query
     */
    public static DescribeEndpointSpec makeDescribeSpec(API api, String query) {
        return new DescribeEndpointSpecImpl(api, JsonUtil.makeJson(QUERY, query));
    }
}
