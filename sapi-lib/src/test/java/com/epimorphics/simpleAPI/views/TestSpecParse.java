/******************************************************************
 * File:        TestSpecParse.java
 * Created by:  Dave Reynolds
 * Created on:  16 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import java.util.Collection;
import java.util.Iterator;

import org.apache.jena.atlas.json.JsonValue;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.core.PrefixService;
import com.epimorphics.json.JsonUtil;

public class TestSpecParse {

    public static ModelSpec loadModel(String filename) {
        JsonValue json = JsonUtil.loadFromFile(filename);
        ModelSpec model = ModelSpec.parseFromJson( PrefixService.getDefault(), json);
        return model;
    }
    
    @Ignore @Test
    public void testSimpleSpecParse() {
        ModelSpec model = loadModel("src/test/specTests/simpleModelProperties.yaml");

        Collection<PropertySpec> props = model.getProperties();
        assertEquals(2, props.size());
        
        Iterator<PropertySpec> i = props.iterator();
        
        // Simple case with defaults - also checks inline prefix definition
        PropertySpec label = i.next();
        assertEquals("label", label.getJsonName());
        assertEquals("http://example.com/label", label.getProperty().getURI());
        assertEquals(true, label.isFilterable());
        assertEquals(false, label.isHide());
        assertEquals(false, label.isOptional());
        assertEquals(false, label.isMultivalued());
        
        // Override range of defaults with explicit values
        PropertySpec ty = i.next();
        assertEquals("mytype", ty.getJsonName());
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ty.getProperty().getURI());
        assertEquals(false, ty.isFilterable());
        assertEquals(false, ty.isHide());
        assertEquals(true, ty.isOptional());
        assertEquals(true, ty.isMultivalued());
        assertEquals("hello", ty.getComment());
        
        Collection<ClassSpec> classes = model.getClassSpecs();
        assertTrue( classes.size() >= 2 );
        
        Iterator<ClassSpec> ci = classes.iterator();
        ClassSpec myclass = ci.next();
        assertEquals("http://example.com/MyClass", myclass.getUri().getURI());
        assertEquals("myClass", myclass.getJsonName());
        
        // Inherits from globals
        ty = myclass.getChildren().iterator().next();
        assertEquals("mytype", ty.getJsonName());
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ty.getProperty().getURI());
        assertEquals(false, ty.isFilterable());
        assertEquals(true, ty.isMultivalued());
        assertEquals("hello", ty.getComment());

        // Overrides some globals
        ClassSpec myclass2 = ci.next();
        assertEquals("http://example.com/MyClass2", myclass2.getUri().getURI());
        ty = myclass2.getChildren().iterator().next();
        assertEquals("otherType", ty.getJsonName());
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", ty.getProperty().getURI());
        assertEquals(false, ty.isFilterable());
        assertEquals(false, ty.isMultivalued());
        assertEquals("hello", ty.getComment());
        
        ClassSpec anon = ty.getNested();
        PropertySpec ps = anon.getChildren().iterator().next();
        assertEquals("http://www.w3.org/2000/01/rdf-schema#label", ps.getProperty().getURI());
        assertEquals("label", ps.getJsonName());
    }
}
