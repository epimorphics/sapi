/******************************************************************
 * File:        ListEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints;

public interface ListEndpointSpec extends EndpointSpec {

    /**
     * Return a soft limit value of the number of results allowed.
     * If the query does not state a limit this soft limit is used. 
     * The query is allowed to override this and return more results
     * May be null if no soft limit has been specified.
     */
    public Long getSoftLimit() ;

    /**
     * Return a hard limit value of the number of results allowed.
     * The number of results allow can be no more than this hard limit,
     * but the query can give a lower limit.
     * May be null if no hard limit has been specified.
     */
    public Long getHardLimit() ;
}
