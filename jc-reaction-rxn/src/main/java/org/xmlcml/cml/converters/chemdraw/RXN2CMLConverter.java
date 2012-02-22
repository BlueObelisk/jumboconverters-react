package org.xmlcml.cml.converters.chemdraw;

import nu.xom.Element;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.cml.converters.AbstractConverter;
import org.xmlcml.cml.converters.MimeType;
import org.xmlcml.cml.converters.cml.CMLCommon;
import org.xmlcml.cml.element.CMLCml;

public class RXN2CMLConverter extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(RXN2CMLConverter.class);
	public static final String REG_MESSAGE = "Chemdraw CDX to CDXML conversion";
	static {
		LOG.setLevel(Level.INFO);
	}
	
	/**
	 * converts a CDK object to CML. returns cml:cml/cml:molecule
	 * 
	 * @param bytes
	 */
	public Element convertToXML(byte[] bytes) {
		Element element = new CMLCml();
		return element;
	}
	
	public MimeType getInputType() {
		return RXNModule.RXN_TYPE;
	}
	
	public MimeType getOutputType() {
		return CMLCommon.CML_TYPE;
	}
	
	public String getDescription() {
		return REG_MESSAGE;
	}


}
