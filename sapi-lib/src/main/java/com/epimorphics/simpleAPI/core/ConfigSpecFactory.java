/******************************************************************
 * File:        EndpointSpecFactory.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import static com.epimorphics.simpleAPI.core.ConfigConstants.NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.yaml.snakeyaml.Yaml;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.impl.ConfigItem;
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
        } else {
            json = JSON.parseAny(is);
        }
        return parse(api, filename, json);
    }
    
    /**
     * Parse a json/yaml specification
     */
    public static ConfigItem parse(API api, String filename, JsonValue json) {
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            if (!jo.hasKey(NAME)) {
                String defaultName = NameUtils.removeExtension( new File(filename).getName() );
                jo.put(NAME, defaultName);
            }
            // TODO implement
            ConfigItem ep = new ConfigItem();
            ep.setName(jo.get(NAME).getAsString().value());
            return ep;
        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }
    }

}
