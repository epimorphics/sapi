/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.simpleAPI.queryTransforms;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.appbase.core.ComponentBase;

public class AppTransforms extends ComponentBase {

	final List<String> transformNames = new ArrayList<String>();
	
	public AppTransforms() {		
	}
	
	public void setInclude(String name) {
		transformNames.add(name);
	}
}
