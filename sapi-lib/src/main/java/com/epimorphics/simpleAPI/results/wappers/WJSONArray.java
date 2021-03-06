/******************************************************************
 * File:        WJSONObject.java
 * Created by:  Dave Reynolds
 * Created on:  1 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results.wappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A wrapped version of a JSON Object generated from an API Result.
 * Provides a representation and helper methods to support scripted HTML rendering e.g. via Velocity.
 */
public class WJSONArray extends ArrayList<Object> implements List<Object>{ 
    private static final long serialVersionUID = -2298626376058840220L;

    public boolean isObject() {
        return false;
    }
    
    public boolean isArray() {
        return true;
    }
    
    public boolean isResource() {
        return false;
    }
    
    public boolean isAnon() {
        return false;
    }
    
    public boolean isLangString() {
        return false;
    }
    
    public boolean isTypedLiteral() {
        return false;
    }

    public boolean hasValue(Object expected) {
        return contains(expected);
    }
    
    public boolean hasResourceValue(String expected) {
        for (Object value : this) {
            if (value instanceof WJSONObject) {
                if ( expected.equals( ((WJSONObject)value).getURI() ) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * Return the value indicated by a tree path.
     * For arrays returns a set of all values
     */
    public Set<Object> getFromPath(String path) {
        Set<Object> values = new HashSet<>(); 
        for (Object v : this) {
            if (v instanceof WJSONObject) {
                values.add( ((WJSONObject)v).getFromPath(path) );
            } else if (v instanceof WJSONArray){
                values.addAll( ((WJSONArray)v).getFromPath(path) );
            }
        }
        return values;
    }
    
    
    // Order independent equality for testing
    @Override
    public boolean equals(Object other) {
        if (other instanceof WJSONArray) {
            return new HashSet<>( this ).equals( new HashSet<>( (WJSONArray)other ) );
        } else {
            return false;
        }
    }
  
}
