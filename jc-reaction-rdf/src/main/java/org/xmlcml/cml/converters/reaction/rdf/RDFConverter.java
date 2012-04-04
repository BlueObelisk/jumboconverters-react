package org.xmlcml.cml.converters.reaction.rdf;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import nu.xom.Document;
import nu.xom.Element;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLSerializer;
import org.xmlcml.cml.converters.reaction.rxn.RXNConverter;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLName;
import org.xmlcml.cml.element.CMLProduct;
import org.xmlcml.cml.element.CMLProductList;
import org.xmlcml.cml.element.CMLProperty;
import org.xmlcml.cml.element.CMLPropertyList;
import org.xmlcml.cml.element.CMLReactant;
import org.xmlcml.cml.element.CMLReactantList;
import org.xmlcml.cml.element.CMLReaction;
import org.xmlcml.cml.element.CMLScalar;

/**
 * class to read and write RDFiles
 * 
 * This is not a complete implementation; moreover there are formats I don't
 * understand
 * 
 * The following came out of ISISBASE, and this implemntation is derived from
 * it... $RDFILE 1 $DATM 12/11/2002 16:25:3 $RIREG 1 $DTYPE
 * ROOT:SUBSTRATES(1):SUBSTRATE STRUCTURE $DATUM $MFMT
 * 
 * -ISIS- 12110216252D
 * 
 * 3 2 0 0 0 0 0 0 0 0999 V2000 2.4042 -0.2458 0.0000 N 0 0 0 0 0 0 0 0 0 0 0 0
 * 3.1186 0.1667 0.0000 O 0 0 0 0 0 0 0 0 0 0 0 0 1.6897 0.1667 0.0000 O 0 0 0 0
 * 0 0 0 0 0 0 0 0 1 2 1 0 0 0 0 1 3 2 0 0 0 0 M END $DTYPE
 * ROOT:PRODUCTS(1):PRODUCT STRUCTURE $DATUM $MFMT ... though I am not sure
 * whether it is documented.
 * 
 * @author (C) P. Murray-Rust, 1996, 1998, 2000
 */

public class RDFConverter implements CMLConstants {

    final static Logger logger = Logger.getLogger(RDFConverter.class.getName());

    /** */
    public final static String _RXN = "$RXN";

    /** */
    public final static String RFMT = "$RFMT";

    /** */
    public final static String RDFILE = "$RDFILE";

    /** */
    public final static String RIREG = "$RIREG";

    /** */
    public final static String REREG = "$REREG";

    /** */
    public final static String DTYPE = "$DTYPE";

    /** */
    public final static String DATUM = "$DATUM";

    /** */
    public final static String MFMT = "$MFMT";

    /** */
    public final static String MIREG = "$MIREG";

    /** */
    public final static String MEREG = "$MEREG";

    /** */
    public final static String PRODUCT = "PRODUCT";

    /** */
    public final static String REACTANT = "REACTANT";

    /** */
    public final static String SUBSTRATE = "SUBSTRATE";

    /** */
    public final static String RDF = "$RDF";

    LineNumberReader br;

    CMLCml cml;

    String line;

    String id;

    CMLReaction reaction;

    Document doc; // overrides abstractConverter doc

    CMLMolecule mol;

    /**
     * constructor.
     */
    public RDFConverter() {
        this(S_EMPTY);
    }

    /**
     * constructor.
     * 
     * @param id
     */
    public RDFConverter(String id) {
        this.id = id;
    }

    /**
     * Current version
     * 
     * @return version
     */
    public String getVersion() {
        return "V2000";
    }

    /**
     * read from lineReader.
     * 
     * @param br
     * @throws IOException
     * @throws RuntimeException
     * @return document
     */
    public Document read(LineNumberReader br) throws RuntimeException, IOException {
        /*
         * CTfile Formats (August 2002) Page 40 Chapter 8: RDfiles An RDfile
         * (reaction-data file) consists of a set of editable ?records.? Each
         * record defines a molecule or reaction, and its associated data. An
         * example RDfile incorporating the rxnfile described in Chapter 7 is
         * shown later in this chapter. The format for an RDfile is:
         * 
         * [RDFile Header] [Molecule or reaction identifier] [Data-field
         * identifier] [Data] where: d is repeated for each data item r is
         * repeated for each reaction or molecule Each logical line in an RDfile
         * starts with a keyword in column 1 of a physical line. One or more
         * blanks separate the first argument (if any) from the keyword. The
         * blanks are ignored when the line is read. After the first argument,
         * blanks are significant. An argument longer than 80 characters breaks
         * at column 80 and continues in column 1 of the next line. (The
         * argument may continue on additional lines up to the physical limits
         * on text length imposed by the database.) The RDfile must not contain
         * any blank lines except as part of embedded molfiles, rxnfiles, or
         * data. An identifier separates records. RDfile Header Line 1: $RDFILE
         * 1: The [RDfile Header] must occur at the beginning of the physical
         * file and identifies the file as an RDfile. The version stamp ?1? is
         * intended for future expansion of the format. Line 2: $DATM: Date/time
         * (M/D/Y, H:m) stamp. This line is treated as a comment and ignored
         * when the program is read. Molecule and Reaction Identifiers A
         * [Molecule or Reaction Identifier] defines the start of each complete
         * record in an RDfile. The form of a molecule identifier must be one of
         * the following: $MFMT [$MIREG internal-regno [$MEREG external-regno]]
         * embedded molfile $MIREG internal-regno $MEREG external-regno where: 
         * $MFMT defines a molecule by specifying its connection table as a
         * molfile  $MIREG internal-regno is the internal registry number
         * (sequence number in the database) of the molecule  $MEREG
         * external-regno is the external registry number of the molecule (any
         * uniquely identifying character string known to the database, for
         * example, CAS number)  Square brackets ( [] ) enclose optional
         * parameters  An embedded molfile (see Chapter 4) follows immediately
         * after the $MFMT line The forms of a reaction identifier closely
         * parallel that of a molecule: d *r CTfile Formats (August 2002) Page
         * 41 $RFMT [$RIREG internal-regno [$REREG external-regno]] embedded
         * rxnfile $RIREG internal-regno $REREG external-regno where:  $RFMT
         * defines a reaction by specifying its description as a rxnfile 
         * $RIREG internal-regno is the internal registry number (sequence
         * number in the database) of the reaction  $REREG external-regno is
         * the external registry number of the reaction (any uniquely
         * identifying character string known to the database)  Square
         * brackets ( [ ] ) enclose optional parameters  An embedded rxnfile
         * (see Chapter 7) follows immediately after the $RFMT line Data-field
         * Identifier The [Data-field Identifier] specifies the name of a data
         * field in the database. The format is: $DTYPE field name Data Data
         * associated with a field follows the field name on the next line and
         * has the form: $DATUM datum The format of datum depends upon the data
         * type of the field as defined in the database. For example: integer,
         * real number, real range, text, molecule regno. For fields whose data
         * type is ?molecule regno,? the datum must specify a molecule and, with
         * the exception noted below, use one of the formats defined above for a
         * molecular identifier. For example: $DATUM $MFMT embedded molfile
         * $DATUM $MEREG external-regno $DATUM $MIREG internal-regno In
         * addition, the following special format is accepted: $DATUM
         * molecule-identifier Here, molecule-identifier acts in the same way as
         * external-regno in that it can be any text string known to the
         * database that uniquely identifies a molecule. (It is usually
         * associated with a data field different from the external-regno.)
         * CTfile Formats (August 2002) Page 42 Figure 15 - Example of a
         * reaction RDfile
         * 
         * $RDFILE 1 $DATM 10/17/91 10:41 $RFMT $RIREG 7439 $RXN
         * 
         * REACCS81 101791141 7439
         * 
         * 2 1 $MOL
         * 
         * REACCS8110179110412D 1 0.00380 0.0000 315
         * 
         * 4 3 0 0 0 0 0 0 0 0 0 ... 1 4 1 0 0 0 4 $MOL
         * 
         * REACCS8110179110412D 1 0.00371 0.0000 8
         * 
         * 6 6 0 0 0 0 0 0 0 0 0 ... 5 6 2 0 0 0 2 $MOL
         * 
         * REACCS8110179110412D 1 0.00374 0.0000 255
         * 
         * 9 9 0 0 0 0 0 0 0 0 0 ... 6 9 2 0 0 0 2 $DTYPE
         * rxn:VARIATION(1):rxnTEXT(1) $DATUM CrC13 $DTYPE
         * rxn:VARIATION(1):LITTEXT(1) $DATUM A G Repin, Y Y
         * Makarov-Zemlyanskii, Zur Russ Fiz-Chim, 44. p. 2360, 1974
         * 
         * $DTYPE rxn:VARIATION(1):CATALYST(1):REGNO $DATUM $MFMT $MIREG 688
         * 
         * REACCS8110179110412D 1 0.00371 0.0000 0
         * 
         * 4 3 0 0 0 0 0 0 0 0 0 ... 1 4 1 0 0 0 0 $DTYPE
         * rxn:VARIATION(1):PRODUCT(1):YIELD $DATUM 70.0
         * 
         * $RFMT $RIREG 8410 $RXN
         * 
         * REACCS81 1017911041 8410
         * 
         * 
         * 2 1 $MOL ... Rxnfile Header # Reactants, # Products Molfile for first
         * reactant Molfile for second reactant Molfile for product RDfile
         * Header Mol/Rxn identifier Data block for reaction Start of next
         * record
         */
        cml = new CMLCml();
        doc = new Document(cml);

        readFileHeader(br);
        line = br.readLine();
        while (true) {
            Element ab = readMoleculeOrReaction(br);
            if (ab == null) {
                break;
            }
            cml.appendChild(ab);
        }

        // end of input
        return (Document) doc;
    }

    /**
     * read document.
     * 
     * @param doc
     * @param br
     * @exception RuntimeException
     * @exception IOException
     * @return document
     */
    public Document read(Document doc, LineNumberReader br)
            throws RuntimeException, IOException {
        // return read((CMLDocument) doc, br);
        return read(br);
    }

    // public CMLDocument read(CMLDocument doc, LineNumberReader br) throws

    /*
     * 
     * $RDFILE 1 $DATM 12/11/2002 16:25:3
     */
    /*
     * Line 1: $RDFILE 1: The [RDfile Header] must occur at the beginning of the
     * physical file and identifies the file as an RDfile. The version stamp ?1?
     * is intended for future expansion of the format. Line 2: $DATM: Date/time
     * (M/D/Y, H:m) stamp. This line is treated as a comment and ignored when
     * the program is read.
     */
    CMLElement readMoleculeOrReaction(LineNumberReader br) throws IOException,
            RuntimeException {

        /*
         * identifier must be one of the following: $MFMT [$MIREG internal-regno
         * [$MEREG external-regno]] embedded molfile $MIREG internal-regno
         * $MEREG external-regno where:  $MFMT defines a molecule by
         * specifying its connection table as a molfile  $MIREG internal-regno
         * is the internal registry number (sequence number in the database) of
         * the molecule  $MEREG external-regno is the external registry number
         * of the molecule (any uniquely identifying character string known to
         * the database, for example, CAS number)  Square brackets ( [] )
         * enclose optional parameters  An embedded molfile (see Chapter 4)
         * follows immediately after the $MFMT line The forms of a reaction
         * identifier closely parallel that of a molecule: d *r CTfile Formats
         * (August 2002) Page 41 $RFMT [$RIREG internal-regno [$REREG
         * external-regno]] embedded rxnfile $RIREG internal-regno $REREG
         * external-regno where:  $RFMT defines a reaction by specifying its
         * description as a rxnfile  $RIREG internal-regno is the internal
         * registry number (sequence number in the database) of the reaction 
         * $REREG external-regno is the external registry number of the reaction
         * (any uniquely identifying character string known to the database) 
         * Square brackets ( [ ] ) enclose optional parameters  An embedded
         * rxnfile (see Chapter 7) follows immediately after the $RFMT line
         * Data-field Identifier
         */
        // logger.info("RMOR"+line);
        if (line == null) {
            return null;
        }
        if (line.startsWith(MFMT) || line.startsWith(MIREG)
                || line.startsWith(MEREG)) {
            return readMolecule(br);
        } else if (line.startsWith(RFMT) || line.startsWith(RIREG)
                || line.startsWith(REREG)) {
            return readReaction(br);
        } else {
            throw new RuntimeException("Corrupt RDF entry: " + line + " (line: "
                    + br.getLineNumber() + S_RBRAK);
        }
    }

    /*
     * $RIREG 1 $DTYPE ROOT:UNIQUE IDENTIFIER $DATUM 1 $DTYPE ROOT:EC NUMBER
     * $DATUM 1.7.99.3 $DTYPE ROOT:PDB CODE $DATUM 1nid $DTYPE ROOT:ENZYME NAME
     * $DATUM Nitrite Reductase (Copper containing) $DTYPE
     * ROOT:SUBSTRATES(1):SUBSTRATE NUMBER $DATUM 1 $DTYPE
     * ROOT:SUBSTRATES(1):SUBSTRATE STRUCTURE $DATUM $MFMT
     * 
     * -ISIS- 12160210312D
     * 
     * 3 2 0 0 0 0 0 0 0 0999 V2000 2.4042 -0.2458 0.0000 N 0 0 0 0 0 0 0 0 0 0
     * 0 0 3.1186 0.1667 0.0000 O 0 0 0 0 0 0 0 0 0 0 0 0 1.6897 0.1667 0.0000 O
     * 0 0 0 0 0 0 0 0 0 0 0 0 1 2 1 0 0 0 0 1 3 2 0 0 0 0 M END $DTYPE
     * ROOT:SUBSTRATES(1):SUBSTRATE NAME $DATUM Nitrous Acid $DTYPE
     * ROOT:PRODUCTS(1):PRODUCT NUMBER $DATUM 1 $DTYPE ROOT:PRODUCTS(1):PRODUCT
     * STRUCTURE $DATUM $MFMT
     * 
     * -ISIS- 12160210312D
     * 
     * 2 1 0 0 0 0 0 0 0 0999 V2000 1.9667 -8.4625 0.0000 N 0 0 0 0 0 0 0 0 0 0
     * 0 0 2.6811 -8.0500 0.0000 O 0 0 0 0 0 0 0 0 0 0 0 0 1 2 2 0 0 0 0 M END
     * $DTYPE ROOT:PRODUCTS(1):PRODUCT_NAME $DATUM Nitric Oxide $DTYPE
     * ROOT:REACTION STAGES(1):REACTION STAGES $DATUM 1 $DTYPE
     * ROOT:REFERENCES(1):REFERENCE NUMBER $DATUM 1 $DTYPE
     * ROOT:REFERENCES(1):REFERENCE $DATUM Boulanger et al. JBC 275 (31)
     * 23957-23964 (2000) $RIREG 2 ...
     */
    CMLMolecule readMolecule(LineNumberReader br) throws RuntimeException,
            IOException {
        StringTokenizer st = new StringTokenizer(line);
        String keyw = st.nextToken();
        if (keyw.equals(MIREG)) {
            mol = new CMLMolecule();
            if (st.countTokens() == 1) {
                mol.setRef(line);
            } else {
                logger.severe("Bad " + MIREG + S_SPACE + line);
            }
        } else if (line.startsWith(MEREG)) {
            line = line.substring(MEREG.length()).trim();
            mol = new CMLMolecule();
            if (st.countTokens() == 1) {
                String tok = st.nextToken();
                mol.setRef(tok);
                mol.setConvention("external");
            } else {
                logger.severe("Bad " + MEREG + S_SPACE + line);
            }
        } else if (keyw.equals(MFMT)) {
            // mol = new MDLConverter().readMolecule(doc, br); // FIXME

            if (st.countTokens() >= 2) {
                if (st.nextToken().equals(MIREG)) {
                    mol.setRef(st.nextToken());
                }
            }
            if (st.countTokens() >= 2) {
                if (st.nextToken().equals(MEREG)) {
                    CMLName name = new CMLName();
                    name.setConvention("external");
                    name.setXMLContent(st.nextToken());
                    mol.addName(name);
                }
            }
        }
        readData(br, mol);
        return mol;
    }

    CMLReaction readReaction(LineNumberReader br) throws RuntimeException,
            IOException {
        StringTokenizer st = new StringTokenizer(line);
        String keyw = st.nextToken();
        if (keyw.equals(RIREG)) {
            reaction = new CMLReaction();
            if (st.hasMoreTokens()) {
                String tok = st.nextToken();
                logger.info("RIREG: " + tok);
                reaction.setId("r" + tok);
            } else {
                logger.severe("Bad " + RIREG + S_SPACE + line);
            }
        } else if (keyw.equals(REREG)) {
            reaction = new CMLReaction();
            if (st.countTokens() == 1) {
                reaction.setRef(line);
                reaction.setConvention("external");
            } else {
                logger.severe("Bad " + RIREG + S_SPACE + line);
            }
        } else if (keyw.equals(RFMT)) {
            reaction = new RXNConverter().readReaction(doc, br);
        } else if (keyw.equals(MFMT)) {
            // mol = new MDLConverter().readMolecule(doc, br); //FIXME
            if (st.countTokens() >= 2) {
                if (st.nextToken().equals(RIREG)) {
                    mol.setRef(st.nextToken());
                }
            }
            if (st.countTokens() >= 2) {
                if (st.nextToken().equals(REREG)) {
                    CMLName name = new CMLName();
                    name.setConvention("external");
                    name.setXMLContent(st.nextToken());
                    mol.addName(name);
                }
            }
        }
        readData(br, reaction);
        return reaction;
    }

    // not sure which of these can contain data

    void readData(LineNumberReader br, Element ab) throws IOException,
            RuntimeException {
        CMLPropertyList propertyList = null;

        line = br.readLine();
        while (true) {
            if (line == null) {
                break;
            }
            if (line.startsWith(MFMT) || line.startsWith(MIREG)
                    || line.startsWith(MEREG) || line.startsWith(RFMT)
                    || line.startsWith(RIREG) || line.startsWith(REREG)) {
                break;
            } else if (line.startsWith(DTYPE)) {
                String dtype = line.substring(DTYPE.length() + 1).trim();
                line = br.readLine();
                if (!line.startsWith(DATUM)) {
                    throw new RuntimeException("Expected " + DATUM + " at " + line);
                }
                String datum = line.substring(DATUM.length() + 1).trim();
                if (datum.startsWith(_RXN)) {
                    CMLReaction rxn = new RXNConverter().readReaction(doc, br);
                    ab.appendChild(rxn);
                    line = br.readLine();
                } else if (datum.startsWith(MFMT)) {
                    // mol = new MDLConverter().readMolecule(doc, br); // FIXME
                    CMLReaction reaction = (CMLReaction) ab;
                    if (isReactant(dtype)) {
                        CMLReactantList reactantList = new CMLReactantList();
                        reaction.addReactantList(reactantList);
                        CMLReactant reactant = new CMLReactant();
                        reactantList.addReactant(reactant);
                        reactant.addMolecule(mol);
                    } else {
                        CMLProductList productList = new CMLProductList();
                        reaction.addProductList(productList);
                        CMLProduct product = new CMLProduct();
                        productList.addProduct(product);
                        product.addMolecule(mol);
                    }
                    line = br.readLine();
                } else {
                    if (propertyList == null) {
                        propertyList = (CMLPropertyList) new CMLPropertyList();
                        if (ab instanceof CMLMolecule) {
                            ((CMLMolecule) ab).addPropertyList(propertyList);
                        }
                        if (ab instanceof CMLReaction) {
                            ((CMLReaction) ab).addPropertyList(propertyList);
                        }
                    }
                    CMLScalar scalar = new CMLScalar();
                    scalar.setTitle(dtype);
                    while (true) {
                        line = br.readLine();
                        if (line == null || line.charAt(0) == '$') {
                            break;
                        }
                        datum += CMLElement.S_NL + line;
                    }

                    scalar.setXMLContent(datum);
                    CMLProperty property = new CMLProperty();
                    propertyList.addProperty(property);
                    property.appendChild(scalar);
                }
            } else {
                throw new RuntimeException("Unrecognised line reading $DTYPE "
                        + line);
            }
        }
    }

    /* really horrible, but we don't know the database schema */
    boolean isReactant(String dtype) {
        if (dtype.toLowerCase().indexOf("substrate structure") != -1) {
            return true;
        }
        if (dtype.toLowerCase().indexOf("product structure") != -1) {
            return false;
        }
        return true;
    }

    /**
     * read start of file.
     * 
     * @param br
     * @throws IOException
     * @throws RuntimeException
     */
    protected void readFileHeader(LineNumberReader br) throws IOException,
            RuntimeException {

        /**
         * this could be all sorts of stuff... ------RIREG format-----------
         * $RDFILE 1 $DATM 12/11/2002 16:25:3 $RIREG 1 ... $RIREG 2 ...
         * ------------------- --
         */
        line = br.readLine();
        if (line == null) {
            throw new RuntimeException("Empty RDF file");
        }
        if (!line.startsWith(RDFILE)) {
            throw new IOException("non-RDF: RDF file must start with '"
                    + RDFILE + "'");
        }
        line = br.readLine();
        if (line == null) {
            throw new RuntimeException("Corrupt RDF file");
        }
        if (!line.startsWith("$DATM ")) {
            throw new RuntimeException("non-RDF " + line);
        }
    }

    /*--
     void output(Writer writer, CMLList list)
     throws RuntimeException, IOException {

     }
     --*/
    /**
     * outputs CML as an RDF if possible. NYI
     * 
     * @param writer
     *            to output it to
     * @throws RuntimeException
     * @throws IOException
     * @return string
     */
    public String output(Writer writer) throws RuntimeException, IOException {
        return null;
    }

    /**
     * NYI
     * 
     * @param writer
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws RuntimeException
     *             DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     */
    public String writeData(Writer writer) throws RuntimeException, IOException {
        return null;
    }

    /**
     * main.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            logger
                    .info("Usage: org.xmlcml.legacy.reaction.RDFConverter [options]");
            logger.info("        -IN inputFile (RDF)");
            logger.info("        -OUT outputFile (CML)");
            logger.info("        -ID id");
            System.exit(0);
        }
        int i = 0;
        String infile = S_EMPTY;
        String outfile = S_EMPTY;
        String id = S_EMPTY;
        while (i < args.length) {
            if (1 == 2) {
                ;
            } else if (args[i].equalsIgnoreCase("-ID")) {
                id = args[++i];
                i++;
            } else if (args[i].equalsIgnoreCase("-IN")) {
                infile = args[++i];
                i++;
            } else if (args[i].equalsIgnoreCase("-OUT")) {
                outfile = args[++i];
                i++;
            } else {
                logger.severe("Unknown arg: " + args[i]);
                i++;
            }
        }
        Document doc = null;
        try {
            if (!infile.equals(S_EMPTY)) {
                LineNumberReader lnr = new LineNumberReader(new FileReader(
                        infile));
                RDFConverter rdf = new RDFConverter(id);
                doc = (Document) rdf.read(lnr);
            }
            // if (!id.equals(S_EMPTY)) {
            // CMLReaction reaction = (CMLReaction) doc.getDocumentElement();
            // reaction.setId(id);
            // }
            if (!outfile.equals(S_EMPTY)) {
                FileOutputStream fos = new FileOutputStream(outfile);
                CMLSerializer serializer = new CMLSerializer(fos);
                serializer.write(doc);
                fos.close();
            } else {
                ;
                // PMRDOMUtil.debug(doc);
            }

        } catch (IOException ioe) {
            logger.info("IOEXception: " + ioe);
        } catch (RuntimeException cmle) {
            logger.info("CMLEXception: " + cmle);
            cmle.printStackTrace();
        }
    }
}
