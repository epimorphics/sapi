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
import com.epimorphics.simpleAPI.core.impl.JSONMapEntry;
import com.epimorphics.simpleAPI.core.impl.ListEndpointSpecImpl;
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
    public static final String PREFIXES  = "prefixes";
    public static final String BASE_QUERY= "baseQuery";
    public static final String MAPPING   = "mapping";
    public static final String BIND_VARS = "bindVars";
    public static final String LIMIT     = "limit";
    
    public static final String PROPERTY  = "prop";
    public static final String OPTIONAL  = "optional";
    public static final String MULTIVALUED = "multi";
    public static final String NESTED    = "nested";
    public static final String FILTERABLE= "filterable";
    public static final String PROP_TYPE = "type";
    
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
            String type =  JsonUtil.getStringValue(jo, TYPE, TYPE_ITEM);
            if (TYPE_ITEM.equals(type)) {
                spec = new DescribeEndpointSpecImpl(api, jo);
            } else if (TYPE_LIST.equals(type)) {
                spec = new ListEndpointSpecImpl(api, jo);
                if (jo.hasKey(BASE_QUERY)) {
                    ((ListEndpointSpecImpl)spec).setBaseQuery( JsonUtil.getStringValue(jo, BASE_QUERY));
                }
                if (jo.hasKey(BIND_VARS)) {
                    if (!jo.get(BIND_VARS).isArray()) {
                        throw new EpiException("bindVars should be an array of strings");
                    }
                    for (Iterator<JsonValue> i = jo.get(BIND_VARS).getAsArray().iterator(); i.hasNext();) {
                        JsonValue v = i.next();
                        if (v.isString()) {
                            ((ListEndpointSpecImpl)spec).addBindingParam(v.getAsString().value());
                        } else {
                            throw new EpiException("bindVars should be an array of strings");
                        }
                    }
                }
                if (jo.hasKey(LIMIT)) {
                    ((ListEndpointSpecImpl)spec).setHardLimit( JsonUtil.getIntValue(jo, LIMIT, Integer.MAX_VALUE) );
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
            if (jo.hasKey(QUERY)) {
                spec.setQueryTemplate( JsonUtil.getStringValue(jo, QUERY) );
            }
            if (jo.hasKey(MAPPING)) {
                spec.setMapping( parseMappingList(api, null, jo.get(MAPPING)) );
            } else {
                spec.setMapping( new JSONMap(api) );
            }
            return spec;
        } else {
            throw new EpiException("Illegal EndpointSpec: expected a json object, in " + filename);
        }
    }
    
    private static JSONMap parseMappingList(API api, JSONMapEntry parent, JsonValue list) {
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
                        JSONMap nested = parseMappingList(api, entry, propO.get(NESTED) );
                        entry.setNested(nested);                        
                    }
                    if (propO.hasKey(FILTERABLE)) {
                        entry.setFilterable( JsonUtil.getBooleanValue(propO, FILTERABLE, true) );
                    }
                    if (propO.hasKey(PROP_TYPE)) {
                        String ty = JsonUtil.getStringValue(propO, PROP_TYPE);
                        entry.setType( ty );  // Unexpanded prefix, have to delay expansion until runtime structure is built
                    }
                }
                entry.setParent(parent);
                entries.add(entry);
            }
        } else {
            throw new EpiException("Illegal JSON mapping spec, value must be an array of mapping specifications");
        }
        JSONMap map = new JSONMap(api);
        map.setMapping(entries);
        return map;
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
