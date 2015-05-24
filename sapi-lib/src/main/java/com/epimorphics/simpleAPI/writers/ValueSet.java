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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONNodeDescription;
import com.epimorphics.simpleAPI.core.impl.JSONMapEntry;
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
    protected RDFNode id = null;
    
    public ValueSet() {
    }
    
    public ValueSet(RDFNode id) {
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
    public void addRow(QuerySolution row) {
        for (Iterator<String> vi = row.varNames(); vi.hasNext();) {
            String var = vi.next();
            if (!var.equals("id")) {
                put(var, row.get(var));
            }
        }
    }
    
    /**
     * Add a query result row to the results, skipping "?id" which is assumed
     * to represent the resource. Uses the map to create nested values when
     * nodes are declared as nested.
     */
    public void addRow(QuerySolution row, JSONMap map) {
        if (map == null) {
            addRow(row);
        } else {
            for (Iterator<String> vi = row.varNames(); vi.hasNext();) {
                String var = vi.next();
                RDFNode value = row.get(var);
                if (value != null) {
                    if (!var.equals("id")) {
                        JSONNodeDescription node = map.getEntry(var);
                        if (node.isChild()) {
                            // Skip children, they will get built when we meet the parents
                        } else if (node.isParent()) {
                            startNested(this, row, map, (JSONMapEntry)node, var, value);
                        } else {
                            put(var, row.get(var));
                        }
                    }
                }
            }
        }
    }
    
    protected void startNested(ValueSet valueset, QuerySolution row, JSONMap map, JSONMapEntry entry, String key, RDFNode value) {
        KeyValues kv = valueset.getKeyValues(key);
        ValueSet vs = null;
        if (kv == null) {
            vs = new ValueSet(value);
            valueset.put(key, vs);
        } else {
            vs = kv.makeValueSet(value);
        }
        addNested(vs, row, entry.getNestedMap());
    }

    protected void addNested(ValueSet parent, QuerySolution row, JSONMap map) {
        for (JSONMapEntry entry : map.getMapping()) {
            String key = entry.getJsonName();
            RDFNode value = row.get(key);
            if (value != null) {
                if (entry.isParent()) {
                    startNested(parent, row, map, entry, key, value);
                } else {
                    parent.put(key, value);
                }
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
    
    public void setId(RDFNode id) {
        this.id = id;
    }
    
    public RDFNode getId() {
        return id;
    }
    
    public String getStringID() {
        if (id.isURIResource()) {
            return id.asResource().getURI();
        } else {
            return null;
        }
    }
    
    public static ValueSet fromResource(JSONMap map, Resource root) {
        return fromResource(map, root, new HashSet<Resource>());
    }
    
    protected static ValueSet fromResource(JSONMap map, Resource root, Set<Resource> seen) {
        seen.add(root);
        ValueSet values = new ValueSet( root );
        for (StmtIterator i = root.listProperties(); i.hasNext(); ) {
            Statement s = i.next();
            String key = map.keyFor(s.getPredicate());
            RDFNode value = s.getObject();
            if (value.isResource()) {
                Resource r = value.asResource();
                if (r.listProperties().hasNext() && !seen.contains(r)) {
                    ValueSet nested = fromResource(map, value.asResource(), seen);
                    values.put(key, nested);
                } else {
                    values.put( key, value );
                }
            } else {
                values.put( key, value );
            }
        }
        seen.remove(root);
        return values;
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{@id:" + id);
        for (KeyValues  v : values.values()) {
            buf.append(", " + v);
        }
        buf.append("}");
        return buf.toString();
    }

}
