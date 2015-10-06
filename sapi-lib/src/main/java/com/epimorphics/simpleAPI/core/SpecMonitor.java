/******************************************************************
 * File:        SpecMonitor.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.appbase.monitor.ConfigMonitor;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.webapi.dispatch.TemplateSet;

public class SpecMonitor extends ConfigMonitor<ConfigItem> {
    protected API api;
    protected TemplateSet<EndpointSpec> endpoints = new TemplateSet<>();
    
    public SpecMonitor(API api) {
        this.api = api;
    }

    @Override
    protected Collection<ConfigItem> configure(File file) {
        return Collections.singletonList( ConfigSpecFactory.read(api, file.getPath()) );
    }
    
    @Override
    protected void doAddEntry(ConfigItem entry) {
        super.doAddEntry(entry);
        if (entry instanceof EndpointSpec) {
            EndpointSpec ep = (EndpointSpec)entry;
            if (ep.getURL() != null) {
                endpoints.register(ep.getURL(), ep);
            }
        }
    }

    @Override
    protected void doRemoveEntry(ConfigItem entry) {
        super.doRemoveEntry(entry);
        if (entry instanceof EndpointSpec) {
            EndpointSpec ep = (EndpointSpec)entry;
            if (ep.getURL() != null) {
                endpoints.unregister(ep.getURL());
            }
        }
    }
    
    /**
     * Set up a call based on this request. Looks up the endpoint in the 
     * register of templates and extracts the request parameters.
     * @throws NotFoundException if no endpoint matches 
     */
    public Call getCall(UriInfo uriInfo) {
        Map<String, String> bindings = new HashMap<>();
        String path = uriInfo.getPath();
        EndpointSpec endpoint = endpoints.lookup(bindings, path, uriInfo.getQueryParameters());
        if (endpoint == null) {
            // TODO should this revert to a describe instead?
            throw new NotFoundException("No endpoint matched request: " + path);
        }
        Request request = Request.from(api, uriInfo);
        for (Map.Entry<String, String> binding : bindings.entrySet()) {
            request.add(binding.getKey(), binding.getValue());
        }
        return new Call(endpoint, request);
    }

}
