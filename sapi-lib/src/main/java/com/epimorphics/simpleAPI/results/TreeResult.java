/******************************************************************
 * File:        KeyValueSet.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.simpleAPI.writers.JsonWriterUtil;
import com.epimorphics.simpleAPI.writers.RDFWriterUtil;
import com.epimorphics.util.EpiException;

/**
 * Represents a simplified tree view over some RDF resource.
 * Comprises an identifier (URI resource or blank node) and a set of key/value bindings.
 * The values are either simple RDFNodes or nested Result trees.
 * The keys are short names as using in JSON, CSV  rendering or a SPARQL result set.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TreeResult extends ResultBase implements Result {
    protected Map<String, Set<Object>> values = new HashMap<>();
    protected RDFNode id = null;
    
    public TreeResult(Call call) {
        super(call);
    }
    
    public TreeResult(Call call, RDFNode id) {
        super(call);
        this.id = id;
    }
    
    public void setId(RDFNode id) {
        this.id = id;
    }
    
    public RDFNode getId() {
        return id;
    }
    
    public String getStringID() {
        if (id != null && id.isURIResource()) {
            return id.asResource().getURI();
        } else {
            return null;
        }
    }

    public Collection<String> getKeys() {
        return values.keySet();
    }
    
    /**
     * Return ordered list of keys 
     */
    public List<String> getSortedKeys() {
        List<String> keys = new ArrayList<String>( values.size() );
        for (String key : values.keySet()) {
            keys.add( key );
        }
        Collections.sort(keys);
        return keys;
    }     
    
    
    public Collection<Object> getValues(String key) {
        return values.get(key);
    }
    
    /**
     * Return a nested value for this key corresponding to the given id, if it exists
     */
    public TreeResult getNested(String key, RDFNode id) {
        Set<Object> bindings = values.get(key);
        if (bindings != null) {
            for (Object value : bindings) {
                if (value instanceof TreeResult) {
                    TreeResult result = (TreeResult)value;
                    if (id.equals(result.getId())) {
                        return result;
                    }
                }
            }
        }
        return null;
    }
    
    public List<Object> getSortedValues(String key) {
        List<Object> v = new ArrayList<Object>( values.get(key) );
        Collections.sort(v, valueComparator);
        return v;
    }
   
    public void add(String key, Object value) {
        Set<Object> v = values.get(key);
        if (v == null) {
            v = new HashSet<>();
            values.put(key, v);
        }
        v.add(value);
    }
    
    public void add(String key, Object...values) {
        for (Object value : values) {
            add(key, value);
        }
    }

    /**
     * Retrieve the values from a location in the tree identified via a path object.
     * Return null if the path is invalid
     */
    public Set<RDFNode> get(ViewPath path) {
        Set<TreeResult> next = Collections.singleton(this);
        for (Iterator<String> i = path.asList().iterator(); i.hasNext();) {
            String key = i.next();
            if (i.hasNext()) {
                next = stepDown(next, key);
                if (next == null) return null;
            } else {
                Set<RDFNode> leaves = new HashSet<>();
                for (TreeResult t : next) {
                    Collection<Object> vs = t.getValues(key);
                    if (vs == null) return null;
                    for (Object v : vs) {
                        if (v instanceof RDFNode) {
                            leaves.add( (RDFNode)v );
                        } else if (v instanceof TreeResult) {
                            leaves.add( ((TreeResult)v).getId() );
                        } else {
                            throw new EpiException("Can't happen");
                        }
                    }
                }
                return leaves;
            }
        }
        return null;
    }
    
    protected Set<TreeResult> stepDown(Set<TreeResult> from, String key) {
        Set<TreeResult> next = new HashSet<>();
        for (TreeResult t : from) {
            Collection<Object> vs = t.getValues(key);
            if (vs == null) return null;
            for (Object v : vs) {
                if (v instanceof TreeResult) {
                    next.add( (TreeResult)v );
                }
            }
        }
        return next; 
    }
    
    // --- JSON rendering --------------------------------------
    
    public void writeJson(JSFullWriter out) {
        JsonWriterUtil.writeResult(this, out);
    }
    
    
    // --- String rendering for debug --------------------------------------
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{@id:" + id + " ");
        boolean started = false;
        for (String key : getKeys()) {
            if (started) {
                buf.append(", ");
            } else {
                started = true;
            }
            buf.append(key + " = ");
            for (Object value : getValues(key) ) {
                buf.append("" + value + " ");
            }
        }
        buf.append("}");
        return buf.toString();
    }
    
    // --- Comparator for ordering values sets --------------------------------------
    
    static final Comparator<Object> valueComparator = new ValueComparator();

    static class ValueComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            return objectString(o1).compareTo( objectString(o2) );
        }
        
        private String objectString(Object o) {
            if (o instanceof RDFNode) {
                return nodeString( (RDFNode)o );
            } else if (o instanceof TreeResult) {
                return nodeString( ((TreeResult)o).getId() );
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

    @Override
    public Resource asResource() {
        return asResource(ModelFactory.createDefaultModel());
    }

    @Override
    public Resource asResource(Model model) {
        return RDFWriterUtil.writeResult(this, model);
    }

}
