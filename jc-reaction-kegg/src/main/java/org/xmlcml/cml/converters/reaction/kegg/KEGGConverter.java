package org.xmlcml.cml.converters.reaction.kegg;

import java.io.EOFException;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nu.xom.Document;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLName;
import org.xmlcml.cml.element.CMLProduct;
import org.xmlcml.cml.element.CMLProductList;
import org.xmlcml.cml.element.CMLReactant;
import org.xmlcml.cml.element.CMLReactantList;
import org.xmlcml.cml.element.CMLReaction;
import org.xmlcml.cml.element.CMLReactionList;

/**
 * convert KEGG files.
 * 
 * @author pmr
 * 
 */
public class KEGGConverter implements CMLConstants {

    final static Logger logger = Logger
            .getLogger(KEGGConverter.class.getName());

    /** */
    public final static String KEGG = "KEGG";

    /** */
    public final static String K_ENTRY = "ENTRY";

    /** */
    public final static String K_NAME = "NAME";

    /** */
    public final static String K_DEFINITION = "DEFINITION";

    /** */
    public final static String K_EQUATION = "EQUATION";

    /** */
    public final static String K_PATHWAY = "PATHWAY";

    /** */
    public final static String K_ENZYME = "ENZYME";

    /** */
    public final static String K_SLASH3 = "///";

    /** */
    public final static String BLANK12 = "            ";

    String id;

    int nProducts;

    int nReactants;

    CMLReaction reaction;

    String line = null;

    Document doc;

    /**
     * read KEGG.
     * 
     * @param br
     * @return document
     * @throws RuntimeException
     * @throws IOException
     */
    public Document read(LineNumberReader br) throws RuntimeException, IOException {
        CMLReactionList reactionList = new CMLReactionList();
        doc = new Document(reactionList);

        while (true) {
            try {
                CMLReaction reaction = readKEGGEntry(br);
                if (reaction == null) {
                    break;
                }
                reactionList.addReaction(reaction);
            } catch (EOFException eof) {
                break;
            } catch (IOException ioe) {
                throw new IOException(S_EMPTY + ioe + "at line: "
                        + br.getLineNumber());
            } catch (RuntimeException cmle) {
                cmle.printStackTrace();
                throw new RuntimeException(S_EMPTY + cmle + "at line: "
                        + br.getLineNumber());
            }
        }
        return doc;
    }

    private CMLReaction readKEGGEntry(LineNumberReader br) throws RuntimeException,
            IOException {
        /*
         * ENTRY R00004 NAME Pyrophosphate phosphohydrolase DEFINITION
         * Pyrophosphate + H2O <=> 2 Orthophosphate EQUATION C00013 + C00001 <=>
         * 2 C00009 PATHWAY PATH: MAP00190 Oxidative phosphorylation ENZYME
         * 3.6.1.1 ///
         */
        line = br.readLine();
        if (line == null || line.equals(S_EMPTY)) {
            return null;
        }
        if (!line.startsWith(K_ENTRY)) {
            throw new RuntimeException("line: " + br.getLineNumber()
                    + "; expected " + K_ENTRY + "; found: " + line);
        }
        CMLReaction reaction = new CMLReaction();
        String id = line.substring(K_ENTRY.length()).trim();
        reaction.setId(id);
        // optional name
        line = br.readLine();
        if (line == null || line.equals(S_EMPTY)) {
            return null;
        }
        if (line.startsWith(K_NAME)) {
            String name = line.substring(K_NAME.length()).trim();
            name += readOverflow(br);
            CMLName cname = new CMLName();
            cname.setXMLContent(name);
            reaction.addName(cname);
        }
        // definition
        if (line == null || line.equals(S_EMPTY)) {
            return null;
        }
        if (!line.startsWith(K_DEFINITION)) {
            throw new RuntimeException("line: " + br.getLineNumber()
                    + "; expected " + K_DEFINITION + "; found: " + line);
        }
        String definition = line.substring(K_DEFINITION.length()).trim();
        definition += readOverflow(br);
        // equation
        if (line == null || line.equals(S_EMPTY)) {
            return null;
        }
        if (!line.startsWith(K_EQUATION)) {
            throw new RuntimeException("line: " + br.getLineNumber()
                    + "; expected " + K_EQUATION + "; found: " + line);
        }
        String equation = line.substring(K_EQUATION.length()).trim();
        equation += readOverflow(br);
        // pathway
        String pathway = null;
        if (line == null || line.equals(S_EMPTY)) {
            return null;
        }
        if (line.startsWith(K_PATHWAY)) {
            pathway = line.substring(K_EQUATION.length()).trim();
            pathway += readOverflow(br);
        }
        // enzyme
        String enzyme = null;
        if (line == null || line.equals(S_EMPTY)) {
            return null;
        }
        if (line.startsWith(K_ENZYME)) {
            enzyme = line.substring(K_ENZYME.length()).trim();
            enzyme += readOverflow(br);
        }
        // ///
        if (line == null || line.equals(S_EMPTY)) {
            return null;
        }
        if (!line.startsWith(K_SLASH3)) {
            throw new RuntimeException("line: " + br.getLineNumber()
                    + "; expected " + K_SLASH3 + "; found: " + line);
        }
        logger.info(S_PERIOD);
        KEGGReaction defReaction = new KEGGReaction(definition);
        KEGGReaction eqnReaction = new KEGGReaction(equation);
        CMLReactantList reactantList = new CMLReactantList();
        reaction.addReactantList(reactantList);
        CMLProductList productList = new CMLProductList();
        reaction.addProductList(productList);

        if (defReaction.rVector.size() != eqnReaction.rVector.size()
                || defReaction.pVector.size() != eqnReaction.pVector.size()) {
            logger.severe("Inconsistent def/eqn: " + definition + "#"
                    + equation);
        } else {
            for (int i = 0; i < defReaction.rVector.size(); i++) {
                KEGGSpecies defR = defReaction.rVector.get(i);
                KEGGSpecies eqnR = eqnReaction.rVector.get(i);
                if (defR.count != eqnR.count) {
                    logger.severe("Bad counts: " + definition + "#" + equation);
                }
                CMLMolecule mol = new CMLMolecule();
                CMLName name = new CMLName();
                name.setXMLContent(defR.name);
                name.setConvention(KEGG);
                mol.addName(name);
                mol.setId(eqnR.name);
                mol.setCount(defR.count);
                CMLReactant reactant = new CMLReactant();
                reactant.addMolecule(mol);
                reactantList.addReactant(reactant);
            }
            for (int i = 0; i < defReaction.pVector.size(); i++) {
                KEGGSpecies defP = defReaction.pVector.get(i);
                KEGGSpecies eqnP = eqnReaction.pVector.get(i);
                if (defP.count != eqnP.count) {
                    logger.severe("Bad counts: " + definition + "#" + equation);
                }
                CMLMolecule mol = new CMLMolecule();
                CMLName name = new CMLName();
                name.setXMLContent(defP.name);
                name.setConvention(KEGG);
                mol.addName(name);
                mol.setId(eqnP.name);
                mol.setCount(defP.count);
                CMLProduct product = new CMLProduct();
                product.addMolecule(mol);
                productList.addProduct(product);
            }
        }
        return reaction;
    }

    // kludgy; sets line to latest value
    private String readOverflow(LineNumberReader br) throws IOException {
        String extension = S_EMPTY;
        while (true) {
            line = br.readLine();
            if (line == null || line.equals(S_EMPTY)) {
                return extension;
            }
            if (!line.startsWith(BLANK12)) {
                return extension;
            }
            extension += S_SPACE + line.trim();
        }
    }
}

class KEGGReaction implements CMLConstants {

    List<KEGGSpecies> pVector;

    List<KEGGSpecies> rVector;

    /**
     * constructor
     * 
     * @param s
     */
    public KEGGReaction(String s) {
        int idx = s.indexOf("<=>");
        if (idx == -1) {
            KEGGConverter.logger.severe("No <=> in: " + s);
        }
        String rString = s.substring(0, idx).trim();
        rVector = split(rString);
        String pString = s.substring(idx + "<=>".length()).trim();
        pVector = split(pString);
    }

    List<KEGGSpecies> split(String s) {
        List<KEGGSpecies> v = new ArrayList<KEGGSpecies>();
        String f = S_EMPTY;
        while (true) {
            int idx = s.indexOf(" + ");
            if (idx == -1) {
                f = s;
            } else {
                f = s.substring(0, idx);
                s = s.substring(idx + "<=>".length());
            }
            KEGGSpecies ks = new KEGGSpecies(f);
            v.add(ks);
            if (idx == -1) {
                break;
            }
        }
        return v;
    }
}

class KEGGSpecies implements CMLConstants {
    int count = 1;

    String name;

    /**
     * constructor
     * 
     * @param s
     */
    public KEGGSpecies(String s) {
        s = s.trim() + S_SPACE;
        int idx = s.indexOf(S_SPACE);
        try {
            count = Integer.parseInt(s.substring(0, idx));
            s = s.substring(idx);
        } catch (NumberFormatException nfe) {
            count = 1;
        }
        name = s.trim();
    }
}
