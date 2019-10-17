package de.javamagazin.docServices.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.TocGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DocService{

    private static final Logger logger = LoggerFactory.getLogger(DocService.class);
    
    public OutputStream merge(InputStream docx, InputStream xml) throws Docx4JException
    {
    	ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		// Load input docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(docx);
			
		// Do the binding:
		// FLAG_NONE means that all the steps of the binding will be done,
		// otherwise you could pass a combination of the following flags:
		// FLAG_BIND_INSERT_XML: inject the passed XML into the document
		// FLAG_BIND_BIND_XML: bind the document and the xml (including any OpenDope handling)
		// FLAG_BIND_REMOVE_SDT: remove the content controls from the document (only the content remains)
		// FLAG_BIND_REMOVE_XML: remove the custom xml parts from the document 
									
		//Docx4J.bind(wordMLPackage, xmlStream, Docx4J.FLAG_NONE);
		//If a document doesn't include the Opendope definitions, eg. the XPathPart,
		//then the only thing you can do is insert the xml
		//the example document binding-simple.docx doesn't have an XPathPart....
    	Docx4J.bind(wordMLPackage, xml, Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML);
		logger.info("documents merged");
		
		TocGenerator tocGenerator = new TocGenerator(wordMLPackage);
		
		Docx4jProperties.setProperty("docx4j.toc.BookmarksIntegrity.remediate", true);
		tocGenerator.updateToc();
		logger.info("TOC updated");
			
		Docx4J.bind(wordMLPackage, xml, Docx4J.FLAG_BIND_REMOVE_SDT | Docx4J.FLAG_BIND_REMOVE_XML);
			
		Docx4J.save(wordMLPackage, result, Docx4J.FLAG_SAVE_ZIP_FILE);
		
		return result;
    }
}
