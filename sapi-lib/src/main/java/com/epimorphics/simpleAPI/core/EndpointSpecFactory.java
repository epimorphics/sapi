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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.yaml.snakeyaml.Yaml;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.impl.DescribeEndpointSpecImpl;
import com.epimorphics.simpleAPI.core.impl.EndpointSpecBase;
import com.epimorphics.simpleAPI.core.impl.SelectEndpointSpecImpl;
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

    public static final String MAPPING   = "mapping";
    public static final String PROPERTY  = "prop";
    public static final String OPTIONAL  = "optional";
    public static final String MULTIVALUED = "multi";
    public static final String NESTED    = "nested";
    
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
            EndpointSpecBase spec = null;
            if (TYPE_ITEM.equals( JsonUtil.getStringValue(jo, TYPE, TYPE_ITEM) )) {
                spec = new DescribeEndpointSpecImpl(api, jo);
            } else if (TYPE_ITEM.equals( JsonUtil.getStringValue(jo, TYPE, TYPE_LIST) )) {
                spec = new SelectEndpointSpecImpl(api, jo);
                // TODO basequery, 
            }
            // TODO prefixes
            if (jo.hasKey(QUERY)) {
                spec.setQueryTemplate( JsonUtil.getStringValue(jo, QUERY) );
            }
            if (jo.hasKey(MAPPING)) {
                spec.setMapping( parseMappingList(jo.get(MAPPING)) );
            }
            return spec;
        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }
    }
    
    private static List<JSONMapEntry> parseMappingList(JsonValue list) {
        List<JSONMapEntry> entries = new ArrayList<JSONMapEntry>();
        if (list.isArray()) {
            for (Iterator<JsonValue> pi = list.getAsArray().iterator(); pi.hasNext(); ) {
                JSONMapEntry entry = null;
                JsonValue prop = pi.next();
                if (prop.isString()) {
                    entry = new JSONMapEntry( prop.getAsString().value() );
                } else if (prop.isObject()) {
                    JsonObject propO = prop.getAsObject();
                    String p = JsonUtil.getStringValue(propO, PROPERTY);
                    String name = JsonUtil.getStringValue(propO, NAME);
                    entry = new JSONMapEntry(name, p);
                    if (propO.hasKey(OPTIONAL)) {
                        entry.setOptional( JsonUtil.getBooleanValue(propO, OPTIONAL, false) );
                    }
                    if (propO.hasKey(MULTIVALUED)) {
                        entry.setMultivalued( JsonUtil.getBooleanValue(propO, MULTIVALUED, false) );
                    }
                    if (propO.hasKey(NESTED)) {
                        List<JSONMapEntry> nested = parseMappingList( propO.get(NESTED) );
                        entry.setNested(nested);
                    }
                }
                entries.add(entry);
            }
        } else {
            throw new EpiException("Illegal JSON mapping spec, value must be an array of mapping specifications");
        }
        return entries;
    }
    
    /**
     * Construct a simple describe specification from an explicit query
     */
    public static DescribeEndpointSpec makeDescribeSpec(API api, String query) {
        DescribeEndpointSpecImpl spec = new DescribeEndpointSpecImpl(api, JsonUtil.makeJson(TYPE, TYPE_ITEM, QUERY, query));
        spec.setQueryTemplate(query);
        return spec;
    }
}
