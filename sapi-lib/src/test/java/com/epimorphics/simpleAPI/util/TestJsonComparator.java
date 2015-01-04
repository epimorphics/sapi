/******************************************************************
 * File:        TestJsonComparator.java
 * Created by:  Dave Reynolds
 * Created on:  15 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.util;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestJsonComparator {

    @Test
    public void testJsonComparator() {
        doTest("{a : 42}", "{a : 42}", true);
        doTest("{a : 42}", "{a : 43}", false);
        doTest("[1,2,3]", "[1,2,3]", true);
        doTest("[1,2,3]", "[2,3,1]", true);
        doTest("[1,2,3]", "[1,2]", false);
        doTest("[1,2,3]", "[1,2, 4]", false);
        doTest("[1,2,3]", "[1,2,3,4]", false);
        doTest("{a : 42, b : [1, true]}", "{b : [true, 1], a : 42}", true);
        doTest("{a : 42, b : [1, true]}", "{b : [false, 1], a : 42}", false);
    }
    
    private void doTest(String expected, String actual, boolean succeed) {
        JsonValue je = JSON.parseAny(expected);
        JsonValue ja = JSON.parseAny(actual);
        assertEquals(succeed, JsonComparator.equal(ja, je));
    }
}
