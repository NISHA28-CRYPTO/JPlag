package de.jplag.emf.model.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jplag.ParsingException;
import de.jplag.emf.dynamic.parser.DynamicEcoreParser;
import de.jplag.emf.util.EMFUtil;

/**
 * Parser for EMF metamodels based on dynamically created tokens.
 * @author Timur Saglam
 */
public class DynamicModelParser extends DynamicEcoreParser {
    private static final Logger logger = LoggerFactory.getLogger(DynamicModelParser.class.getSimpleName());
    private static final List<EPackage> metapackages = new ArrayList<>();
    private static final String ALL_EXTENSIONS = "*";

    /**
     * Creates the parser.
     */
    public DynamicModelParser() {
        EMFUtil.registerModelExtension(ALL_EXTENSIONS);
    }

    @Override
    protected void parseModelFile(File file) throws ParsingException {
        // implicit assumption: Metamodel gets parsed first!
        if (file.getName().endsWith(de.jplag.emf.Language.FILE_ENDING)) {
            parseMetamodelFile(file);
        } else {
            if (metapackages.isEmpty()) {
                logger.warn("Loading model instances without any metamodel!");
            }
            super.parseModelFile(file);
        }
    }

    private void parseMetamodelFile(File file) throws ParsingException {
        metapackages.clear();
        List<EObject> model = EMFUtil.loadModel(file);
        if (model == null) {
            throw new ParsingException(file, "Could not load metamodel file!");
        } else {
            for (EObject object : model) {
                if (object instanceof EPackage ePackage) {
                    metapackages.add(ePackage);
                } else {
                    logger.error("Error, not a metapackage: " + object);
                }
            }
            EMFUtil.registerEPackageURIs(metapackages);
        }
    }
}
