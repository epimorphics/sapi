/******************************************************************
 * File:        TestResultAccess.java
 * Created by:  Dave Reynolds
 * Created on:  28 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

import com.epimorphics.simpleAPI.views.ViewPath;

public class TestResultAccess {
    static final String NS = "http://localhost/test/";
    
    @Test
    public void testResultAccess() {
        TreeResult C = tree("C"); C.add("p", lit("v1"));  C.add("q", lit("v2")); C.add("r", lit("v3"), lit("v4"));
        TreeResult D = tree("D");  D.add("p", lit("v5"));
        TreeResult E = tree("E");  E.add("p", lit("v6"));
        TreeResult B = tree("B");  B.add("bar", C);
        TreeResult A = tree("A");  A.add("foo", B);  A.add("baz", D, E);

        assertEquals(set(lit("v1")), A.get( path("foo.bar.p") ) );
        assertNull( A.get( path("bar") ));
        assertNull( A.get( path("foo.not") ));
        assertNull( A.get( path("foo.not.p") ));
        assertEquals(set(lit("v3"),lit("v4")), A.get( path("foo.bar.r") ) );
        assertEquals(set(res("B")), A.get( path("foo") ));
        assertEquals(set(res("B")), A.get( path("foo") ));
        assertEquals(set(res("D"), res("E")), A.get( path("baz") ));
        assertEquals(set(lit("v5"),lit("v6")), A.get( path("baz.p") ) );
    }
    
    ViewPath path(String dotted) {
        return ViewPath.fromDotted(dotted);
    }
    
    RDFNode lit(String s) {
        return ResourceFactory.createPlainLiteral(s);
    }
    
    RDFNode res(String s) {
        return ResourceFactory.createResource( NS + s );
    }
    
    TreeResult tree(String id) {
        return new TreeResult(null, res(id));
    }

    Set<RDFNode> set(RDFNode...values) {
        Set<RDFNode> set = new HashSet<>();
        for (RDFNode value : values) {
            set.add(value);
        }
        return set;
    }
}
