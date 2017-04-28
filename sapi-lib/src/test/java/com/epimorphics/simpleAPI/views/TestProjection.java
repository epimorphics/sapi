/******************************************************************
 * File:        TestProjection.java
 * Created by:  Dave Reynolds
 * Created on:  28 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import static org.junit.Assert.assertEquals;

import org.apache.jena.util.FileManager;
import org.junit.Test;

/**
 * Tests for the project abstraction
 */
public class TestProjection {

    @Test
    public void testParse() {
        doParseTest("p.q.r",            "p.\n  q.\n    r\n");
        doParseTest("p.r,p(s,t.z),f.g", "p.\n  r\n  s\n  t.\n    z\nf.\n  g\n");
        doParseTest("p(r,a),p(s,t.z)",  "p.\n  r\n  a\n  s\n  t.\n    z\n");
    }
    
    private void doParseTest(String path, String expected) {
        Projection p = new Projection();
        p.addPath(path);
        assertEquals(expected, p.toString());
    }
    
//    @Test
//    public void testFullViewExtraction() {
//        ModelSpec model = TestSpecParse.loadModel("src/test/specTests/orgExample.yaml");
//        ViewSpec view = ViewSpec.projectFrom(model, "org:Site");
//        assertEquals( expected("src/test/specTests/site-expected.txt"), view.toString() );
//        
//        view = ViewSpec.projectFrom(model, "org:Organization");
//        assertEquals( expected("src/test/specTests/org-expected.txt"), view.toString() );
//    }
//    
//    @Test
//    public void testProjections() {
//        ModelSpec model = TestSpecParse.loadModel("src/test/specTests/orgExample.yaml");
//        doTestProjection(model, "org:Site", "label,siteAddress(locality,postal_code)", "src/test/specTests/site-project-expected.txt");
//        ViewSpec orgView = doTestProjection(model, "org:Organization", "label,classification(prefLabel,notation),hasMember(name)", "src/test/specTests/org-project-expected.txt");
//        doTestProjection(model, "org:Organization", "classification(prefLabel,notation,broader(prefLabel,notation))", "src/test/specTests/concept-project-expected.txt");
//        doTestProjection(model, "org:Organization", "label,classification.*", "src/test/specTests/org-wildcard1-expected.txt");
//        
//        ViewSpec projected = orgView.project( new Projection("classification.prefLabel,hasMember.*") );
//        assertEquals( expected("src/test/specTests/org-project-expected2.txt").trim(), projected.toString().trim());
//    }
//
//    private ViewSpec doTestProjection(ModelSpec model, String classURI, String projection, String expected) {
//        Projection p = new Projection(projection);
//        ViewSpec view = ViewSpec.project(model, classURI, p);
//        assertEquals( expected(expected).trim(), view.toString().trim());
//        return view;
//    }
    
    private String expected(String filename) {
        return FileManager.get().readWholeFileAsUTF8(filename);
    }
}
