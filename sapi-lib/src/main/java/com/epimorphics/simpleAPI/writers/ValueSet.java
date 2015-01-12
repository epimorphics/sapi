/******************************************************************
 * File:        KeyValueSet.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.epimorphics.simpleAPI.core.JSONOldMap;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * An orderable list of key/values sets and associate ID (e.g. URI). Can represent an RDF
 * resource for serialisation whether from a Describe or a coalesced result set.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueSet {
    protected Map<String, KeyValues> values = new HashMap<String, KeyValues>();
    protected String id = null;
    
    public ValueSet() {
    }
    
    public ValueSet(String id) {
        this.id = id;
    }
    
    public KeyValues getKeyValues(String key) {
        return values.get(key);
    }
    
    public void put(String key, Object value) {
        KeyValues kv = getKeyValues(key);
        if (kv == null) {
            kv = new KeyValues(key, value);
            values.put(key, kv);
        } else {
            kv.add(value);
        }
    }
    
    /**
     * Add a query result row to the results, skipping "?id" which is assumed
     * to represent the resource.
     */
    @@
    // Mapped version
    public void addRow(QuerySolution row) {
        for (Iterator<String> vi = row.varNames(); vi.hasNext();) {
            String var = vi.next();
            if (!var.equals("id")) {
                put(var, row.get(var));
            }
        }
    }
    
    /**
     * Return ordered list of keys 
     */
    public List<String> listSortedKeys() {
        List<String> keys = new ArrayList<String>( values.size() );
        for (String key : values.keySet()) {
            keys.add( key );
        }
        Collections.sort(keys);
        return keys;
    }    
    
    /**
     * Return (un-sorted) collection of all the key values 
     */
    public Collection<KeyValues> listKeyValues() {
        return values.values();
    }    
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public static ValueSet fromResource(JSONOldMap map, Resource root) {
        // TODO implement
        // Handle nesting
        @@ 
        ValueSet values = new ValueSet( root.getURI() );  // Null ID is perfectly legal here
        for (StmtIterator i = root.listProperties(); i.hasNext(); ) {
            Statement s = i.next();
            values.put( map.keyFor(s.getPredicate()), s.getObject() );
        }
        return values;
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{@id:" + id);
        for (KeyValues  v : values) {
            buf.append(", " + v);
        }
        buf.append("}");
        return buf.toString();
    }
}
