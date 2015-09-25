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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Represents the value(s) of a key in an output stream. The key normally corresponds
 * to the JSON name in an output JSON structure and the variable name in any supplying SPARQL 
 * query solution row. Each value may be an RDFNode or a nested ValueSet (when the
 * mapping includes nested values).
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class KeyValues implements Comparable<KeyValues>{
    protected String key;
    protected List<Object> values = new ArrayList<Object>();
    protected boolean sorted = false;
    
    public KeyValues(String key) {
        this.key = key;
    }
    
    public KeyValues(String key, Object value) {
        this.key = key;
        values.add(value);
    }
    
    public String getKey() {
        return key;
    }
    
    public List<Object> values() {
        return values;
    }
    
    public List<Object> getValues() {
        return values;
    }
    
    public List<Object> getSortedValues() {
        if (!sorted) {
            Collections.sort(values, valueComparator);
            sorted = true;
        }
        return values;
    }
    
    public Object getValue() {
        if (values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }
    
    public void add(Object value) {
        if ( ! values.contains(value) ) {
            values.add(value);
            sorted = false;
        }
    }
    
    /**
     * If this contains a ValueSet with the given ID then return it,
     * otherwise return null;
     */
    public ValueSet findValueSet(RDFNode id) {
        for (Object value : values) {
            if (value instanceof ValueSet) {
                ValueSet vs = (ValueSet)value;
                if ( vs.getId().equals(id) ) {
                    return vs;
                }
            }
        }
        return null;
    }
    
    /**
     * Find or create a ValueSet entry for the given ID
     */
    public ValueSet makeValueSet(RDFNode id) {
        ValueSet vs = findValueSet(id);
        if (vs == null) {
            vs = new ValueSet(id);
            values.add(vs);
        }
        return vs;
    }

    @Override
    public int compareTo(KeyValues o) {
        return key.compareTo(o.key);
    }
    

    @Override
    public String toString() {
        return key + "=" + values;
    }
    
    static final Comparator<Object> valueComparator = new ValueComparator();
    
    static class ValueComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            return objectString(o1).compareTo( objectString(o2) );
        }
        
        private String objectString(Object o) {
            if (o instanceof RDFNode) {
                return nodeString( (RDFNode)o );
            } else if (o instanceof ValueSet) {
                return nodeString( ((ValueSet)o).getId() );
            } else {
                // Should happen
                return o.toString();
            }
        }
        
        private String nodeString(RDFNode id) {
            if (id.isURIResource()) {
                return ((Resource)id).getURI();
            } else if (id.isAnon()) {
                return ((Resource)id).getId().getLabelString();
            } else {
                return ((Literal)id).getLexicalForm();
            }
        }
        
    }
}
