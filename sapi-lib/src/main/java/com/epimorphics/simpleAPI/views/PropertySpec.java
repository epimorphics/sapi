/******************************************************************
 * File:        ViewEntry.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import static com.epimorphics.simpleAPI.core.ConfigConstants.*;

import java.util.regex.Pattern;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.sparql.terms.URI;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;

/**
 * Represents view information for a single property within the context of some ViewMap.
 * Provides a short ("jsonname") for each property.
 */
public class PropertySpec {
	
    protected String jsonname;
    
    protected URI property;
    
    protected boolean optional = false;
    protected boolean multivalued = false;
    protected boolean filterable = true;
    protected String range;
    protected String comment;
    protected ClassSpec nested = null;
    protected String valueBase = null;
    protected boolean hide = false;
    
    public PropertySpec(String jsonname, URI property) {
        this.jsonname = jsonname == null ? makeJsonName(property) : jsonname;
        if (!legalSparqlVar.matcher(this.jsonname).matches()) {
            throw new EpiException("Illegal sparql name in view: " + jsonname);
        }
        this.property = property;
    }
    
    static Pattern legalSparqlVar = Pattern.compile("[a-zA-Z0-9_]*");   // Restrictive ASCII version, could allow other unicode
    
    public PropertySpec(URI property) {
        this.jsonname = makeJsonName(property);
        this.property = property;
    }

    static String makeJsonName(URI property) {
        String name = RDFUtil.getLocalname(property.getURI());
        if (name.isEmpty()) {
            // curie/qname format but at this stage no expansion to URI
            name = NameUtils.splitAfterLast(property.getURI(), ":");
        }
        name = name.replace("-", "_");
        return name;
    }
    
    /**
     * Shallow clone only
     */
    public PropertySpec clone() {
        PropertySpec ps = new PropertySpec(jsonname, property);
        ps.optional = optional;
        ps.multivalued = multivalued;
        ps.filterable = filterable;
        ps.range = range;
        ps.comment = comment;
        ps.valueBase = valueBase;
        ps.hide = hide;
        ps.nested = nested;
        return ps;
    }
    
    /**
     * Clone nested classes as well
     */
    public PropertySpec deepclone() {
        PropertySpec clone = clone();
        if (nested != null) {
            clone.nested = nested.deepclone();
        }
        return clone;
    }
    
    /**
     * Shallow clone filling in nested from Model.
     * If model is null then does no expansion.
     */
    public PropertySpec cloneWithClosure(ModelSpec model) {
        PropertySpec ps = clone();
        if (nested == null && model != null) {
            String rangeURI = ps.getRange();
            ps.setNested( model.getClassSpec( rangeURI ) );
        }
        return ps;
    }

    public URI getProperty() {
        return property;
    }

    public void setProperty(URI property) {
        this.property = property;
    }
    
    public void setJsonName(String name) {
        this.jsonname = name;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isMultivalued() {
        return multivalued;
    }

    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String typeURI) {
        this.range = typeURI;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isNested() {
        return nested != null;
    }
    
    public ClassSpec getNested() {
        return nested;
    }

    public void setNested(ClassSpec nested) {
        this.nested = nested;
    }
    
    public String getJsonName() {
        return jsonname;
    }
    
    public String getValueBase() {
        return valueBase;
    }

    public void setValueBase(String valueBase) {
        this.valueBase = valueBase;
    }
    
    

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }



    public static class PV {
    	public final URI property;
    	public final Var var;
    	
    	public PV(URI property, Var var) {
    		this.property = property;
    		this.var = var;
    	}
    }
    
    public PV asQueryRow(String parent) {
        String varname = parent.isEmpty() ? jsonname : parent + "_" + jsonname;
        Var var = new Var(varname);
        return new PV(property, var);  
    }
    
    /**
     * Return a view entry based on a path of short names, or null if it is not specified in the view
     */
    public PropertySpec findEntry(ViewPath path) {
        if (path.isEmpty()) {
            return this;
        } else if (isNested()) {
            return getNested().findEntry(path);
        } else {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return print(new StringBuffer(), "").toString();
    }
    
    protected StringBuffer print(StringBuffer buf, String indent) {
        buf.append(indent);
        buf.append(jsonname);
        buf.append("(" + property + ")");
        if (filterable) buf.append(" filterable");
        if (optional) buf.append(" optional");
        if (multivalued) buf.append(" multi");
        if (valueBase != null) buf.append(" base(" + valueBase + ")");
        if (comment != null) buf.append(" '" + comment + "'");
        if (isNested()) {
            buf.append("\n");
            nested.print(buf, indent + "  ");
        }
        return buf;
    }
    
    /**
     * Parse a JSON definition of the spec in the context of some overall
     * ModelSpec which supplies default property context and prefixes
     */
    public static PropertySpec parseFromJson(ModelSpec model, JsonValue prop) {
        if (prop.isString()) {
            return model.getOrCreateProperty( prop.getAsString().value() );

        } else if (prop.isObject()) {
            JsonObject propO = prop.getAsObject();
            String p = JsonUtil.getStringValue(propO, PROPERTY);
            PropertySpec ps = model.getOrCreateProperty(p);
            if (propO.hasKey(NAME)) {
                ps.setJsonName( JsonUtil.getStringValue(propO, NAME) );
            }
            if (propO.hasKey(OPTIONAL)) {
                ps.setOptional( JsonUtil.getBooleanValue(propO, OPTIONAL, false) );
            }
            if (propO.hasKey(MULTIVALUED)) {
                ps.setMultivalued( JsonUtil.getBooleanValue(propO, MULTIVALUED, false) );
            }
            if (propO.hasKey(NESTED)) {
                ClassSpec nested = ClassSpec.parseInline(model, propO.get(NESTED) );
                ps.setNested(nested);                        
            }
            if (propO.hasKey(FILTERABLE)) {
                ps.setFilterable( JsonUtil.getBooleanValue(propO, FILTERABLE, true) );
            }
            if (propO.hasKey(PROP_TYPE)) {
                String ty = JsonUtil.getStringValue(propO, PROP_TYPE);
                ps.setRange( model.getPrefixes().expandPrefix(ty) );
                // TODO - used to have to delay prefix expansion, why?
            }
            if (propO.hasKey(RANGE) || propO.hasKey(PROP_TYPE)) {
                String ty = JsonUtil.getStringValue(propO, RANGE, JsonUtil.getStringValue(propO, PROP_TYPE));
                ps.setRange( model.getPrefixes().expandPrefix(ty) );
                // TODO - used to have to delay prefix expansion, why?
            }
            if (propO.hasKey(COMMENT)) {
                String comment = JsonUtil.getStringValue(propO, COMMENT);
                ps.setComment(comment);
            }
            if (propO.hasKey(VALUE_BASE)) {
                String vb = JsonUtil.getStringValue(propO, VALUE_BASE);
                ps.setValueBase(vb);
            }
            if (propO.hasKey(SUPPRESSID)) {
                boolean sid = JsonUtil.getBooleanValue(propO, SUPPRESSID, false);
                ps.setHide(sid);
            }
            if (propO.hasKey(HIDE)) {
                boolean sid = JsonUtil.getBooleanValue(propO, HIDE, false);
                ps.setHide(sid);
            }
            return ps;
        } else {
            throw new EpiException("PropertySpec must be a string or object: " + prop);
        }
    }
        
}
