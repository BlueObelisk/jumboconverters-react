package org.xmlcml.cml.converters.reaction.rxn;

import java.util.ArrayList;

import java.util.List;

import org.xmlcml.cml.converters.AbstractConverterModule;
import org.xmlcml.cml.converters.Converter;
import org.xmlcml.cml.converters.MimeType;
import org.xmlcml.cml.converters.MimeType.ObjectType;

/**
 * @author Sam Adams
 */
public class RXNModule extends AbstractConverterModule {


	public static final MimeType RXN_TYPE = new MimeType("chemical/x-rxn", ObjectType.BYTES, "cdx");
	
    public RXNModule() {
    	super();
    }

    public String getPrefix() {
    	return "chemdraw";
    }
    
	public List<Converter> getConverterList() {
		if (converterList == null) {
			converterList = new ArrayList<Converter>();
	        converterList.add(new RXN2CMLConverter());
		}
		return converterList;
    }

	public List<MimeType> getMimeTypeList() {
		if (mimeTypeList == null) {
			mimeTypeList = new ArrayList<MimeType>();
			mimeTypeList.add(RXN_TYPE);
		}
		return mimeTypeList;
	}

}
