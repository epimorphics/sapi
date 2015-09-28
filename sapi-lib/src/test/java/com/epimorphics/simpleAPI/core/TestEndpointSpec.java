/******************************************************************
 * File:        TestEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;

public class TestEndpointSpec {
    App app;
    API api;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/baseEPTest/app.conf"));
        api = app.getA(API.class);
        app.startup();
    }

    @Test
    public void testEndpointsExist() {
        assertNotNull( api.getSpec("describeTest") );
        assertNotNull( api.getSpec("listTest") );
        assertNull( api.getSpec("otherTest") );
    }
}
