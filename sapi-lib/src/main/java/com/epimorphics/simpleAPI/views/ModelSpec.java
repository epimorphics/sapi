/******************************************************************
 * File:        ModelSpec.java
 * Created by:  Dave Reynolds
 * Created on:  28 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigItem;
import com.epimorphics.sparql.terms.URI;
import com.epimorphics.util.EpiException;

import static com.epimorphics.simpleAPI.core.ConfigConstants.*;

/**
 * Represents the structure of the data as seen through the API.
 * Comprises at set of prefixes, a set of global property specs and a set of class specs.
 * The global property specs provide default shortnames and types for properties which
 * can be overridden for specific classes (and can be used for Elda style query/render).
 */
public class ModelSpec extends ConfigItem {
    protected PrefixMapping prefixes;
    protected Map<String, PropertySpec> globalProperties = new LinkedHashMap<>();
    protected Map<String, ClassSpec> classes = new LinkedHashMap<>();
    
    public ModelSpec(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }
    
    public PrefixMapping getPrefixes() {
        return prefixes;
    }
    
    public void addProperty(PropertySpec prop) {
        globalProperties.put(prop.getProperty().getURI(), prop);
    }
    
    public PropertySpec getProperty(String uri) {
        return globalProperties.get(uri);
    }
    
    public PropertySpec getOrCreateProperty(String uri) {
        String exp = prefixes.expandPrefix(uri);
        PropertySpec p = globalProperties.get(exp);
        return p == null ? new PropertySpec( new URI(exp) ) : p.clone();
    }
    
    public Collection<PropertySpec> getProperties() {
        return globalProperties.values();
    }
    
    public void addClassSpec(ClassSpec cspec) {
        String uri = cspec.getUri().getURI();
        if (uri == null) {
            throw new EpiException("Can't register an anonymous class");
        }
        classes.put(uri, cspec);
    }
    
    public ClassSpec getClassSpec(String uri) {
        if (uri == null) return null;
        return classes.get( prefixes.expandPrefix(uri) );
    }
    
    public Collection<ClassSpec> getClassSpecs() {
        return classes.values();
    }
    
    /**
     * Parse ModelSpec from a json specification
     */
    public static ModelSpec parseFromJson(PrefixMapping prefixes, JsonValue json) {
        ModelSpec model = new ModelSpec(prefixes);
        if (json.isObject()) {
            JsonObject jo = json.getAsObject();
            model.setName( JsonUtil.getStringValue(jo, NAME) );
            if (jo.hasKey(PREFIXES)) {
                JsonObject pf = jo.get(PREFIXES).getAsObject();
                for (String key : pf.keys()) {
                    model.getPrefixes().setNsPrefix(key, JsonUtil.getStringValue(pf, key));
                }
            }
            if (jo.hasKey(PROPERTIES)) {
                JsonValue properties = jo.get(PROPERTIES);
                if (properties.isArray()) {
                    for (JsonValue j : properties.getAsArray()) {
                        PropertySpec ps = PropertySpec.parseFromJson(model, j);
                        model.addProperty(ps);
                    }
                } else {
                    throw new EpiException("Expected model properties to be an array: " + properties);
                }
            }
            if (jo.hasKey(CLASSES)) {
                JsonValue classes = jo.get(CLASSES);
                if (classes.isArray()) {
                    for (JsonValue j : classes.getAsArray()) {
                        ClassSpec cs = ClassSpec.parseFromJson(model, j);
                        model.addClassSpec(cs);
                    }
                } else {
                    throw new EpiException("Expected model classes to be an array: " + classes);
                }
            }
            if (jo.hasKey(NAME)) {
                model.setName( JsonUtil.getStringValue(jo, NAME) );
            }
            return model;
        } else {
            throw new EpiException("Model spec must be an object: " + json);
        }
    }


    /**
     * Create a view starting from the given root class (may be a curi) showing only those
     * properties in the projection. The projection can unfold recursion so this is not
     * the same as creating a default view then filtering it by a projection.
     */
    public ClassSpec projectClass(String rootURI, Projection projection) {
        ClassSpec root = getClassSpec(rootURI);
        if (root == null) return null;
        return root.project(this, projection.getRoot());
    }
    
    /**
     * Create a view starting from the given root class (may be a curi) showing all properties in the model.
     * Blocks recursion.
     */
    public ClassSpec projectClass(String rootClassURI) {
        ClassSpec rootClass = getClassSpec(rootClassURI);
        if (rootClass == null) return null;
        ClassSpec view = new ClassSpec();
        view.setJsonName( rootClass.getJsonName() );
        view.setUri( rootClass.getUri() );
        view.addClosure(this, rootClass, new HashSet<>());
        return view;
    }
    
    /**
     * Create a view of this model starting from the given root class and following the projection
     */
    public ViewMap projectView(API api, String rootURI, Projection projection) {
        return new ViewMap(api, projectClass(rootURI, projection));
    }
    
    /**
     * Create a view of this model starting from the given root class and showing all properties in the model
     */
    public ViewMap projectView(API api, String rootURI) {
        return new ViewMap(api, projectClass(rootURI));
    }

}

