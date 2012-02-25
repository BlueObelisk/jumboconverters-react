package org.xmlcml.cml.converters.reaction.rxn;

import java.io.IOException;


import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.cml.converters.testutils.JumboConvertersRegressionSuite;

public class RXN2CMLConverterTest {

	@Test
	public void testDummy() {
		
	}
	
   @Test
   @Ignore
   public void testConvertToXMLElement() throws IOException {
       JumboConvertersRegressionSuite.run("cdx/cdx", "cdx", "cdx.xml",
                            new RXN2CMLConverter());
   }
}
