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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.epimorphics.simpleAPI.core.NodeWriterPolicy;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * An orderable list of key/values sets. Can represent an RDF
 * resource for serialisation whether from a Describe or a coalesced result set.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class KeyValueSet {
    protected List<KeyValues> values = new ArrayList<KeyValues>();
    protected String id = null;
    
    public KeyValueSet() {
    }
    
    public KeyValueSet(String id) {
        this.id = id;
    }
    
    public KeyValues getKeyValues(String key) {
        for (KeyValues kv : values) {
            if (kv.getKey().equals(key)) {
                return kv;
            }
        }
        return null;
    }
    
    public void put(String key, RDFNode value) {
        KeyValues kv = getKeyValues(key);
        if (kv == null) {
            kv = new KeyValues(key, value);
            values.add(kv);
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
     * Sort the keys into lexical order instead of arrival order
     */
    public void sort() {
        Collections.sort(values);
    }
    
    /**
     * Return ordered list of key values 
     */
    public List<KeyValues> listSortedKeyValues() {
        sort();
        return values;
    }    
    
    /**
     * Return list of key values 
     */
    public List<KeyValues> listKeyValues() {
        return values;
    }    
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public static KeyValueSet fromResource(NodeWriterPolicy policy, Resource root) {
        KeyValueSet values = new KeyValueSet( root.getURI() );  // Null ID is perfectly legal here
        for (StmtIterator i = root.listProperties(); i.hasNext(); ) {
            Statement s = i.next();
            values.put( policy.keyFor(s.getPredicate()), s.getObject() );
        }
        return values;
    }
}
