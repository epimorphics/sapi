/******************************************************************
 * File:        KeyValues.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Pair of a short name key (to which values will be written) and an array of values.
 * Used as part of ordering and grouping a ResultSet or set of Resource properties.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class KeyValues implements Comparable<KeyValues>{
    protected String key;
    protected List<RDFNode> values = new ArrayList<RDFNode>();
    
    public KeyValues(String key) {
        this.key = key;
    }
    
    public KeyValues(String key, RDFNode value) {
        this.key = key;
        values.add(value);
    }
    
    public String getKey() {
        return key;
    }
    
    public List<RDFNode> values() {
        return values;
    }
    
    public List<RDFNode> getValues() {
        return values;
    }
    
    public RDFNode getValue() {
        if (values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }
    
    public void add(RDFNode value) {
        if ( ! values.contains(value) ) {
            values.add(value);
        }
    }

    @Override
    public int compareTo(KeyValues o) {
        return key.compareTo(o.key);
    }
    

    @Override
    public String toString() {
        return key + "=" + values;
    }
}
