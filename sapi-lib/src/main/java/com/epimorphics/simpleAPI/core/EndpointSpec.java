/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import com.epimorphics.appbase.monitor.ConfigInstance;

/**
 * Encapsulates the specification of a single endpoint.
 */
public interface EndpointSpec extends ConfigInstance {

    public String getName();
    
}
