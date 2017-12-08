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
     * Return true if the result is a simple leaf result with no property values
     */
    public boolean isSimple() {
        return values.isEmpty();
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
        Collection<Object> leaves = getValues(path);
        if (leaves == null) {
            return null;
        }
        Set<RDFNode> results = new HashSet<>( leaves.size() );
        for (Object v: leaves) {
            if (v instanceof RDFNode) {
                results.add( (RDFNode)v );
            } else if (v instanceof TreeResult) {
                results.add( ((TreeResult)v).getId() );
            } else {
                throw new EpiException("Can't happen");
            }
        }
        return results;
    }
    
    protected Collection<Object> getValues(ViewPath path) {
        if (path.isEmpty()) {
            return Collections.singleton(this);
        }
        Set<TreeResult> next = Collections.singleton(this);
        for (Iterator<String> i = path.asList().iterator(); i.hasNext();) {
            String key = i.next();
            if (i.hasNext()) {
                next = stepDown(next, key);
                if (next == null) return null;
            } else {
                Set<Object> leaves = null;
                for (TreeResult t : next) {
                    Collection<Object> kv = t.getValues(key);
                    if (kv != null) {
                        if (leaves == null) {
                            leaves = new HashSet<>();
                        }
                        leaves.addAll( kv );
                    }
                }
                return leaves;
            }
        }
        return Collections.singleton(getId());
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
        if ( isSimple() ) {
            return getId().asResource();
        } else {
            return asResource(ModelFactory.createDefaultModel());
        }
    }

    @Override
    public Resource asResource(Model model) {
        return RDFWriterUtil.writeResult(this, model);
    }
    
    /**
     * Return a copy of the tree with the value at the path replaced with the given value.
     * Assumes that the path has a unique location within the tree.
     * The copy will be deep enough to enable to value substitution but no deeper.
     */
    public TreeResult cloneWithValue(ViewPath path, Object value) {
        TreeResult clone = new TreeResult(call, id);
        String firstStep = path.first();
        for (String key : values.keySet()) {
            Set<Object> kv = values.get(key);
            if (key.equals(firstStep)) {
                if ( path.isSingleton() ) {
                    clone.add(key, value);
                } else if (kv.size() != 1) {
                    throw new EpiException("Non-unique path in view clone");
                } else {
                    Object v = kv.iterator().next();
                    if (v instanceof TreeResult) {
                        clone.add(key, ((TreeResult)v).cloneWithValue(path.rest(), value));
                    } else {
                        throw new EpiException("Path not present in view");
                    }
                }
            } else {
                for (Object v : kv) {
                    clone.add(key, v);
                }
            }
        }
        return clone;
    }  
    
    /**
     * Return a copy of the tree with the value at the path omitted.
     * Assumes that the path has a unique location within the tree.
     * The copy will be deep enough to enable to value substitution but no deeper.
     */
    public TreeResult cloneWithout(ViewPath path) {
        TreeResult clone = new TreeResult(call, id);
        String firstStep = path.first();
        for (String key : values.keySet()) {
            Set<Object> kv = values.get(key);
            if (key.equals(firstStep)) {
                if ( path.isSingleton() ) {
                    // Omit
                } else if (kv.size() != 1) {
                    throw new EpiException("Non-unique path in view clone");
                } else {
                    Object v = kv.iterator().next();
                    if (v instanceof TreeResult) {
                        clone.add(key, ((TreeResult)v).cloneWithout(path.rest()));
                    } else {
                        throw new EpiException("Path not present in view");
                    }
                }
            } else {
                for (Object v : kv) {
                    clone.add(key, v);
                }
            }
        }
        return clone;
    }

    /**
     * Return a set of cloned trees one with each of the values at the given path.
     * Returns a singleton containing this tree if the values are empty or there's just one.
     */
    public Collection<TreeResult> splitAt(ViewPath path) {
        Collection<Object> values = getValues(path);
        if (values == null || values.isEmpty() || values.size() == 1) {
            return Collections.singleton( this );
        }
        Collection<TreeResult> results = new ArrayList<>( values.size() );
        for (Object value : values) {
            results.add( cloneWithValue(path, value) );
        }
        return results;
    }

}
