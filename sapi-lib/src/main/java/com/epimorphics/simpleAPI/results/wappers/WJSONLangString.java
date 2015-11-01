/******************************************************************
 * File:        WJSONLangString.java
 * Created by:  Dave Reynolds
 * Created on:  1 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results.wappers;

public class WJSONLangString {
    protected String language;
    protected String lexicalForm;
    
    public WJSONLangString(String lexicalForm, String language) {
        this.lexicalForm = lexicalForm;
        this.language = language;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getLexicalForm() {
        return lexicalForm;
    }
    
    public String getName() {
        return lexicalForm;
    }
    
    public String getLabel() {
        return lexicalForm;
    }

    public boolean isObject() {
        return false;
    }
    
    public boolean isArray() {
        return false;
    }
    
    public boolean isResource() {
        return false;
    }
    
    public boolean isLangString() {
        return true;
    }
    
    public boolean isTypedLiteral() {
        return false;
    }
    
    
}
