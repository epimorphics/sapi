/******************************************************************
 * File:        TestResultAccess.java
 * Created by:  Dave Reynolds
 * Created on:  28 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import static com.epimorphics.simpleAPI.util.TreeTestUtil.lit;
import static com.epimorphics.simpleAPI.util.TreeTestUtil.res;
import static com.epimorphics.simpleAPI.util.TreeTestUtil.set;
import static com.epimorphics.simpleAPI.util.TreeTestUtil.tree;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
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
        
        assertEquals( set(res("A")), A.get( new ViewPath() ) );
    }
    
    @Test
    public void testViewClone() {
        TreeResult C1 = tree("C1"); C1.add("pa", lit("v1a")); C1.add("pb", lit("v1b"));
        TreeResult C2 = tree("C2"); C2.add("pa", lit("v2a")); C2.add("pb", lit("v2b"));
        TreeResult  B = tree("B");  B.add("s", C1);  B.add("s", C2);
        TreeResult  A = tree("A");  A.add("r", B); A.add("q", lit("c")); A.add("p", lit("a")); A.add("p", lit("b"));

        ViewPath fPath = ViewPath.fromDotted("r.s");
        TreeResult A1 = A.cloneWithValue(fPath, C1);
        TreeResult A2 = A.cloneWithValue(fPath, C2);
        
        assertEquals( set(lit("c")),           A1.get( path("q") ) );
        assertEquals( set(lit("a"), lit("b")), A1.get( path("p") ) );
        assertEquals( set(lit("v1a")),         A1.get( path("r.s.pa") ) );
        assertEquals( set(lit("v1b")),         A1.get( path("r.s.pb") ) );
        
        assertEquals( set(lit("c")),           A2.get( path("q") ) );
        assertEquals( set(lit("a"), lit("b")), A2.get( path("p") ) );
        assertEquals( set(lit("v2a")),         A2.get( path("r.s.pa") ) );
        assertEquals( set(lit("v2b")),         A2.get( path("r.s.pb") ) );
        
        Collection<TreeResult> splits = A.splitAt( fPath );
        assertEquals( 2, splits.size() );
        TreeResult split1 = splits.iterator().next();
        assertEquals( set(lit("c")),           split1.get( path("q") ) );
        Set<RDFNode> pavalues = split1.get( path("r.s.pa") );
        assertTrue( pavalues.equals( set(lit("v1a")) ) || pavalues.equals( set(lit("v2a")) ) );

        splits = A.splitAt( path("q") );
        assertEquals( 1, splits.size() );
        assertEquals( A, splits.iterator().next() );
        
        splits = A.splitAt( path("q.r.s") );   // Does not exist
        assertEquals( 1, splits.size() );
        assertEquals( A, splits.iterator().next() );
    }
    
    @Test
    public void testCloneWithOmission() {
        TreeResult B = tree("B"); B.add("t", lit("a")); B.add("u", lit("b"));
        TreeResult A = tree("A"); A.add("r", B); A.add("s", lit("c"));

        TreeResult Aminus = A.cloneWithout( ViewPath.fromDotted("r.t"));
        assertEquals("{@id:http://localhost/test/A r = {@id:http://localhost/test/B u = b } , s = c }", Aminus.toString());

        TreeResult Aminus2 = A.cloneWithout( ViewPath.fromDotted("r"));
        assertEquals("{@id:http://localhost/test/A s = c }", Aminus2.toString());
    }
    
    ViewPath path(String dotted) {
        return ViewPath.fromDotted(dotted);
    }

}
