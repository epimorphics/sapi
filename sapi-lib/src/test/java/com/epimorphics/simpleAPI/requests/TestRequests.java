/******************************************************************
 * File:        TestRequests.java
 * Created by:  Dave Reynolds
 * Created on:  20 Jun 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.webapi.test.MockUriInfo;

public class TestRequests {
    protected API api = new API();
    
    @Test
    public void testFilenameGeneration() {
        Request r = makeRequest("http://localhost/api?foo=42&foo=bar&_view=normal&_ignore=fool");
        assertEquals("api-_view-normal-foo-42-bar", r.asFilename());
        
        r = makeRequest("http://localhost/api?aQuitelongParamNameWithsomePadding1=aQuitelongParamValueWithsomePadding"
                + "&aQuitelongParamNameWithsomePadding2=aQuitelongParamValueWithsomePadding"
                + "&aQuitelongParamNameWithsomePadding2=aQuitelongParamValueWithsomePadding"
                + "&aQuitelongParamNameWithsomePadding2=aQuitelongParamValueWithsomePadding");
        assertEquals("api-f6c000ca15d0ef68d20b9959cef304eb", r.asFilename());
    }
    
    protected Request makeRequest(String url) {
        return Request.from(api, new MockUriInfo(url), null);
    }
}
