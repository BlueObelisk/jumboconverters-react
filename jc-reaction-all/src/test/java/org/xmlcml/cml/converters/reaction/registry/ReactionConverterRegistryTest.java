package org.xmlcml.cml.converters.reaction.registry;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.cml.converters.Converter;
import org.xmlcml.cml.converters.MimeType;
import org.xmlcml.cml.converters.TypePair;
import org.xmlcml.cml.converters.cml.CML2CMLLiteConverter;
import org.xmlcml.cml.converters.cml.CMLCommon;
import org.xmlcml.cml.converters.reaction.rxn.RXNModule;
import org.xmlcml.cml.converters.registry.ConverterRegistry;

public class ReactionConverterRegistryTest {

	String CML = "chemical/x-cml";
	String CDX = "chemical/x-cdx";
	String FOO = "chemical/x-foo";
	TypePair PAIR_OK  = new TypePair(FOO, CML);
	TypePair PAIR_MISSING  = new TypePair(CML, CDX);
	int MAP_SIZE = 3;
	int CONVERTER_SIZE = 3;

    @Test
    public void testMap() {
    	Map<TypePair, List<Converter>> map = ConverterRegistry.getDefaultConverterRegistry().getMap();
    	Assert.assertNotNull(map);
    	// size will change as more are added
    	Assert.assertEquals(MAP_SIZE, map.size());
    }

    @Test
    public void testList() {
    	List<Converter> converterList = ConverterRegistry.getDefaultConverterRegistry().getConverterList();
    	Assert.assertNotNull(converterList);
    	for (Converter converter : converterList) {
    		System.out.println(converter);
    	}
    	Assert.assertEquals(CONVERTER_SIZE, converterList.size());
    }

    @Test
    public void testList1() {
    	List<Converter> converterList = ConverterRegistry.getDefaultConverterRegistry().getConverterList();
    	boolean found = false;
    	for (Converter converter : converterList) {
    		if (CML2CMLLiteConverter.class.equals(converter.getClass())) {
    			found = true;
    			break;
    		}
    	}
    	Assert.assertTrue("converter", found);
    }

    @Test
    public void testMap1() {
    	Map<TypePair, List<Converter>> map = ConverterRegistry.getDefaultConverterRegistry().getMap();
    	Assert.assertTrue(map.containsKey(PAIR_OK));
    	Assert.assertFalse(map.containsKey(PAIR_MISSING));
    	for (TypePair typePair1 : map.keySet()) {
    		System.out.println(typePair1);
    	}
    }

    @Test
    public void testFindConverter() {
    	List<Converter> converters = ConverterRegistry.getDefaultConverterRegistry().findConverters(
    			RXNModule.RXN_TYPE.getMimeType(), CMLCommon.CML_TYPE.getMimeType());
    	Assert.assertNotNull("rxn", converters);
    	for (Converter converter : converters) {
    		System.out.println("Converter: "+converter);
    	}
    	Assert.assertEquals("rxn", 1, converters.size());
    	Assert.assertEquals("rxn", "org.xmlcml.cml.converters.reaction.rxn.RXN2CMLConverter", converters.get(0).getClass().getName());
    }

    @Test
    public void testFindConverter1() {
    	List<Converter> converters = ConverterRegistry.getDefaultConverterRegistry().findConverters(CML, CML);
    	Assert.assertNotNull("cml", converters);
    	Assert.assertEquals("cml", 1, converters.size());
    }

	@Test
	public void testRegistryLoadsConverterList() {
		List<Converter> list = ConverterRegistry.getDefaultConverterRegistry().getConverterList();
		assertTrue(list.size()>0);
	}

	@Test
	public void testFindFoo2BarConverter() {
		List<Converter> converterList = ConverterRegistry.getDefaultConverterRegistry().findConverters("foo", "bar");
		assertNull(converterList);
	}

	@Test
	public void testFindTypesFromSuffix() {
		Set<MimeType> types = ConverterRegistry.getDefaultConverterRegistry().getTypes("cml");
		Assert.assertNotNull("get types", types);
		Assert.assertEquals("get types", 1, types.size());
		Assert.assertEquals("get types", "chemical/x-cml", ((MimeType)types.toArray()[0]).getMimeType());
	}

	@Test
	public void testFindTypesFromSuffix1() {
		Set<MimeType> types = ConverterRegistry.getDefaultConverterRegistry().getTypes("foo");
		Assert.assertNotNull("get types", types);
		Assert.assertEquals("types count", 1, types.size());
		Assert.assertEquals("type", "chemical/x-foo", ((MimeType)types.toArray()[0]).getMimeType());
	}

	@Test
	public void testFindSingleTypeFromSuffix() {
		MimeType type = ConverterRegistry.getDefaultConverterRegistry().getSingleTypeFromSuffix("cml");
		Assert.assertNotNull("get type", type);
	}

	@Test
	public void testSingletonConverterRegistry() {
		Assert.assertNotNull(ConverterRegistry.getDefaultConverterRegistry());
	}

	@Test
	public void testCreateRegistryList0() {
		ConverterRegistry converterRegistry = new ConverterRegistry(ConverterRegistry.class.getClassLoader());
		List<Converter> converterList = converterRegistry.getConverterList();
		Assert.assertNotNull(converterList);
		converterRegistry.createConvertersList();
		converterList = converterRegistry.getConverterList();
		Assert.assertNotNull(converterList);
	}

	@Test
	public void testCreateRegistryList() {
		ConverterRegistry converterRegistry = new ConverterRegistry(ConverterRegistry.class.getClassLoader());
		converterRegistry.populateAndRegister();
		List<Converter> converterList = converterRegistry.getConverterList();
		converterRegistry.createConvertersList();
		converterList = converterRegistry.getConverterList();
		// should at least contain org.xmlcml.cml.converters.cml.CML2CMLLiteConverter@76f2d004
		Assert.assertTrue(converterList.size()>0);
		boolean hasCmllite = hasConverter(converterList, "org.xmlcml.cml.converters.cml.CML2CMLLiteConverter");
		Assert.assertTrue("has cmllite", hasCmllite);
	}

	private boolean hasConverter(List<Converter> converterList, String className) {
		for (Converter converter : converterList) {
			if (converter.getClass().getName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testSingletonConverterRegistryList0() {
		ConverterRegistry converterRegistry = ConverterRegistry.getDefaultConverterRegistry();
		List<Converter> converterList = converterRegistry.getConverterList();
		Assert.assertNotNull(converterList);
		Assert.assertEquals("converterList", CONVERTER_SIZE, converterList.size());
	}

}
