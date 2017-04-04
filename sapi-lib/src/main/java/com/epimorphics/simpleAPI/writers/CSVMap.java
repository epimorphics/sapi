/******************************************************************
 * File:        CSVMap.java
 * Created by:  Dave Reynolds
 * Created on:  4 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.util.EpiException;

/**
 * Support for mapping view paths to an ordered, named set of CSV columns
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class CSVMap {
    protected List<Entry> columns = new ArrayList<>();
    
    public class Entry {
        String header;
        String path;
        
        public Entry(String header, String path) {
            this.header = header;
            this.path = path;
        }

        public String getHeader() {
            return header;
        }

        public String getPath() {
            return path;
        }
    }
    
    public void put(String header, String path) {
        columns.add( new Entry(header, path) );
    }
    
    public List<Entry> getColumns() {
        return columns;
    }
    
    public List<String> getColumnNames() {
        return columns.stream().map(Entry::getHeader).collect(Collectors.toList());
    }
    
    public List<ViewPath> getPaths() {
        return columns.stream().map( e -> ViewPath.fromDotted(e.getPath()) ).collect(Collectors.toList());
    }
    
    public static CSVMap parseFromJson(JsonValue spec) {
        if (spec.isArray()){
            CSVMap map = new CSVMap();
            Iterator<JsonValue> i = spec.getAsArray().iterator();
            while( i.hasNext() ) {
                JsonValue e = i.next();
                if (e.isObject()) {
                    JsonObject m = e.getAsObject();
                    for (String key: m.keys()) {
                        String path = m.get(key).getAsString().value();
                        map.put(key, path);
                    }
                } else {
                    throw new EpiException("Illegal csvmap specification, must be an array of column/path maps");
                }
            }
            return map;
        } else {
            throw new EpiException("Illegal csvmap specification, must be an array of column/path maps");
        }
        
    }
}
