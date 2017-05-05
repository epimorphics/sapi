/******************************************************************
 * File:        TestTemplateUtil.java
 * Created by:  Dave Reynolds
 * Created on:  5 May 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.simpleAPI.requests.Request;

public class TestTemplateUtil {

    @Test
    public void testTemplates() {
        Request request = new Request();
        request.add("foo", "42");
        request.add("bar", "zz");
        assertEquals("Izz am 42.", TemplateUtil.instatiateTemplate("I${bar} am ${foo}.", request) );
        assertTrue( TemplateUtil.isTemplate("I${bar} am ${foo}.") );
        assertFalse( TemplateUtil.isTemplate("I am not.") );
    }

}
