/******************************************************************
 * File:        ParameterCheck.java
 * Created by:  Dave Reynolds
 * Created on:  19 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.util.regex.Pattern;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.util.EpiException;

/**
 * Specification of a single parameter check as part of a request validation
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ParameterCheck {
    public static final String P_REGEX    = "regex";
    public static final String P_DEFAULT  = "default";
    public static final String P_REQUIRED = "required";
    
    protected String parameter;
    protected String regex;
    protected Pattern pattern;
    protected String deflt;
    protected boolean required = false;
    
    public ParameterCheck(String parameter) {
        this.parameter = parameter;
    }
    
    public ParameterCheck(String parameter, String regex) {
        this.parameter = parameter;
        this.regex = regex;
    }
    
    public ParameterCheck(String parameter, String regex, String deflt) {
        this.parameter = parameter;
        this.regex = regex;
        this.deflt = deflt;
    }
    
    public ParameterCheck(String parameter, String regex, boolean required) {
        this.parameter = parameter;
        this.regex = regex;
        this.required = true;
    }
    
    public ParameterCheck(String parameter, boolean required) {
        this.parameter = parameter;
        this.required = true;
    }
    
    public static ParameterCheck fromJson(String param, JsonValue json) {
        if (json.isString()) {
            // If a string, assume its a regex
            return new ParameterCheck(param, json.getAsString().value());
        } if (json.isBoolean()) {
            // If boolean, assume required
            return new ParameterCheck(param, json.getAsBoolean().value());
        } else if (json.isObject()) {
            ParameterCheck check = new ParameterCheck(param);
            JsonObject jo = json.getAsObject();
            for (String key : jo.keys()) {
                if (key.equals(P_DEFAULT) && jo.get(key).isString()) {
                    check.setDeflt( jo.get(key).getAsString().value() );
                } else if (key.equals(P_REGEX) && jo.get(key).isString()) {
                    check.setRegex( jo.get(key).getAsString().value() );
                } else if (key.equals(P_REQUIRED) && jo.get(key).isBoolean()) {
                    check.setRequired( jo.get(key).getAsBoolean().value() );
                } else {
                    throw new EpiException("Could not understand spec " + key + " for parameter check: " + param);
                }
            }
            return check;
        } else {
            throw new EpiException("Could not understand spec for parameter check: " + param);
        }
    }
    
    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getRegex() {
        return regex;
    }
    
    public Pattern getPattern() {
        if (pattern == null && regex != null) {
            pattern = Pattern.compile(regex);
        }
        return pattern;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getDeflt() {
        return deflt;
    }

    public void setDeflt(String deflt) {
        this.deflt = deflt;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
    
    
    /**
     * Checks the given request against the parameter constraints.
     * May side-effect the request parameters to add in default values.
     * Returns null on success or an error string on failure
     */
    public String checkAndReport(Request request) {
        if (request.hasParameter(parameter)) {
            if (regex != null) {
                Pattern p = getPattern();
                for (String value : request.get(parameter)) {
                    if (!p.matcher(value).matches()) {
                        return String.format(ERROR_MSG, parameter, "illegal value: " + value);
                    }
                }
                
            }
            
        } else if (required) {
            return String.format(ERROR_MSG, parameter, "missing but is requried");
            
        } else if (deflt != null) {
            request.add(parameter, deflt);
            
        }
        return null;
    }
    
    protected static final String ERROR_MSG = "HTTP 400 Bad Request. Parameter %s %s";
}