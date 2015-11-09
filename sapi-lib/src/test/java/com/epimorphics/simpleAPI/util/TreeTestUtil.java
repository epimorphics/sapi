/******************************************************************
 * File:        TreeTestUtil.java
 * Created by:  Dave Reynolds
 * Created on:  30 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import com.epimorphics.simpleAPI.results.TreeResult;

public class TreeTestUtil {
    
    public static final String NS = "http://localhost/test/";
    
    
    public static RDFNode lit(String s) {
        return ResourceFactory.createPlainLiteral(s);
    }
    
    public static RDFNode res(String s) {
        return ResourceFactory.createResource( NS + s );
    }
    
    public static TreeResult tree(String id) {
        return new TreeResult(null, res(id));
    }

    public static Set<RDFNode> set(RDFNode...values) {
        Set<RDFNode> set = new HashSet<>();
        for (RDFNode value : values) {
            set.add(value);
        }
        return set;
    }
}
