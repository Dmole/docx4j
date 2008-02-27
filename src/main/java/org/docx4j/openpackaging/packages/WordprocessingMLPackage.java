/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.openpackaging.packages;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;







public class WordprocessingMLPackage extends Package {
	
	// What is a Word document these days?
	//
	// Well, a package is a logical entity which holds a collection of parts	
	// And a word document is exactly a WordProcessingML package	
	// Which has a Main Document Part, and optionally, a Glossary Document Part

	/* So its a Word doc if:
	 * 1. _rels/.rels tells you where to find an office document
	 * 2. [Content_Types].xml tells you that office document is   
	 *    of content type application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml
	
	 * A minimal docx has:
	 * 
	 * [Content_Types].xml containing:
	 * 1. <Default Extension="rels" ...
	 * 2. <Override PartName="/word/document.xml"...
	 * 
	 * _rels/.rels with a target for word/document.xml
	 * 
	 * word/document.xml
	 */
	
	protected static Logger log = Logger.getLogger(WordprocessingMLPackage.class);
		
	
	// Main document
	protected MainDocumentPart mainDoc;
	
	// (optional) Glossary document
	protected GlossaryDocumentPart glossaryDoc;
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public WordprocessingMLPackage() {
		super();
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT));
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public WordprocessingMLPackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT));
	}
	
	/**
	 * Convenience method to create a WordprocessingMLPackage
	 * from an existing File.
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public static WordprocessingMLPackage load(java.io.File docxFile) throws Docx4JException {
		
		LoadFromZipFile loader = new LoadFromZipFile();
		return (WordprocessingMLPackage)loader.get(docxFile);		
	}

	/**
	 * Convenience method to save a WordprocessingMLPackage
	 * to a File.
     *
	 * @param docxFile
	 *            The docx file 
	 */	
	public void save(java.io.File docxFile) throws Docx4JException {
		
		SaveToZipFile saver = new SaveToZipFile(this); 
		saver.save(docxFile);
	}
	
	
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			log.info("Set shortcut for docPropsCorePart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			log.info("Set shortcut for docPropsExtendedPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.DOCUMENT)) {
			mainDoc = (MainDocumentPart)part;
			log.info("Set shortcut for mainDoc");
			return true;			
		} else {	
			return false;
		}
	}
	
	public MainDocumentPart getMainDocumentPart() {
		return mainDoc;
	}
	
	

	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public void html(javax.xml.transform.Result result) throws Exception {
    	
    	/*
    	 * Given that word2html.xsl is freely available, we use the second
    	 * approach.
    	 * 
    	 * The question then is how the stylesheet is made to work with
    	 * our main document and style definition parts.
    	 * 
    	 * For now, I've just edited it a little to accept our parts wrapped
    	 * in a <w:wordDocument> element.  Since that's a completely
    	 * arbitrary format, it may be better in due course to process
    	 * pck:package/pck:part
    	 * 
    	 */
    	
		// so, put the 2 parts together into a single document 
    	// The JAXB object org.docx4j.wml.WordDocument is
    	// custom built for this purpose.
    	
    	// Create a org.docx4j.wml.WordDocument object
    	org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
    	org.docx4j.wml.WordDocument wd = factory.createWordDocument();
    	// Set its parts
    	// .. the main document part
		MainDocumentPart documentPart = getMainDocumentPart(); 
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();		
    	wd.setDocument(wmlDocumentEl);
    	// .. the style part
    	org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart stylesPart = documentPart.getStyleDefinitionsPart();
    	org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)stylesPart.getJaxbElement();
    	wd.setStyles(styles);
    	// Now marshall it
		JAXBContext jc = Context.jc;
		Marshaller marshaller=jc.createMarshaller();
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.neww3cDomDocument();

		marshaller.marshal(wd, doc);
		
		log.info("wordDocument created for PDF rendering!");
		
		
		
		// Now transform this into XHTML
		javax.xml.transform.TransformerFactory tfactory = javax.xml.transform.TransformerFactory.newInstance();
		javax.xml.transform.dom.DOMSource domSource = new javax.xml.transform.dom.DOMSource(doc);

		// Get the xslt file
		java.io.InputStream is = null;
			// Works in Eclipse - note absence of leading '/'
			is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/openpackaging/packages/wordml2html-2007.xslt");
				
		// Use the factory to create a template containing the xsl file
		javax.xml.transform.Templates template = tfactory.newTemplates(
				new javax.xml.transform.stream.StreamSource(is));
		
		// Use the template to create a transformer
		javax.xml.transform.Transformer xformer = template.newTransformer();

		//DEBUGGING 
		// use the identity transform if you want to send wordDocument;
		// otherwise you'll get the XHTML
		//javax.xml.transform.Transformer xformer = tfactory.newTransformer();
		
		xformer.transform(domSource, result);

		log.info("wordDocument transformed to xhtml ..");
    	
    }

	/** Create a pdf version of the document. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * 
	 * */     
    public void pdf(OutputStream os) throws Exception {
    	
    	/*
    	 * There are 2 broad approaches we could use to render the document
    	 * as a PDF:
    	 * 
    	 * 1.  XSL-FO
    	 * 2.  XHTML to PDF
    	 * 
    	 * Given that a word2html.xsl is already freely available, we use 
    	 * the second approach.
    	 * 
    	 * The question then is how the stylesheet is made to work with
    	 * our main document and style definition parts.
    	 * 
    	 * For now, I've just edited it a little to accept our parts wrapped
    	 * in a <w:wordDocument> element.  Since that's a completely
    	 * arbitrary format, it may be better in due course to process
    	 * pck:package/pck:part
    	 * 
    	 */
				
        // Put the html in result
		org.w3c.dom.Document xhtmlDoc = org.docx4j.XmlUtils.neww3cDomDocument();
		javax.xml.transform.dom.DOMResult result = new javax.xml.transform.dom.DOMResult(xhtmlDoc);
		html(result);
				
		// Now render the XHTML
		org.xhtmlrenderer.pdf.ITextRenderer renderer = new org.xhtmlrenderer.pdf.ITextRenderer();
		
		// TODO: Handle fonts
		// - this is platform specific
		// Algorithm - to be implemented:
		// 1.  Get a list of all the fonts in the document
		// 2.  For each font, find the closest match on the system (use OO's VCL.xcu to do this)
		//     - do this in a general way, since docx4all needs this as well to display fonts
		// 3.  Ensure that the font names in the XHTML have been mapped to these matches
		//     possibly via an extension function in the XSLT
		// 4.  Use addFont code like that below as necessary for the fonts
		
			// See https://xhtmlrenderer.dev.java.net/r7/users-guide-r7.html#xil_32
		org.xhtmlrenderer.extend.FontResolver resolver = renderer.getFontResolver();
		
		log.info("OS: " + System.getProperty("os.name") );
		if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS")>-1) {
			log.info("Detected Windows - ");
			renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\ARIAL.TTF", true);
			renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\COMIC.TTF", true);
			renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\TREBUC.TTF", true);
			renderer.getFontResolver().addFont("C:\\WINDOWS\\FONTS\\VERDANA.TTF", true);
			
		}
		
		renderer.setDocument(xhtmlDoc, null);
		renderer.layout();
		
		renderer.createPDF(os);
		
	}	
	

	public static WordprocessingMLPackage createTestPackage() throws InvalidFormatException {
		
				
		// Create a package
		WordprocessingMLPackage wmlPack = new WordprocessingMLPackage();

		// Create main document part
		Part wordDocumentPart = new org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart();		
		
		// Create main document part content
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();

		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("Hello world, from docx4j");

		org.docx4j.wml.R  run = factory.createR();
		run.getRunContent().add(t);		
		
		org.docx4j.wml.P  para = factory.createP();
		para.getParagraphContent().add(run);
		
		org.docx4j.wml.Body  body = factory.createBody();
		body.getBlockLevelElements().add(para);
		
		org.docx4j.wml.Document wmlDocumentEl = factory.createDocument();
		wmlDocumentEl.setBody(body);
				
		// Put the content in the part
		((org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart)wordDocumentPart).setJaxbElement(wmlDocumentEl);
						
		// Add the main document part to the package relationships
		// (creating it if necessary)
		wmlPack.addTargetPart(wordDocumentPart);
				
		// Create a styles part
		Part stylesPart = new org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart();
		try {
			((org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart) stylesPart)
					.unmarshalDefaultStyles();
			
			// Add the styles part to the main document part relationships
			// (creating it if necessary)
			wordDocumentPart.addTargetPart(stylesPart); // NB - add it to main doc part, not package!			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();			
		}
		// Return the new package
		return wmlPack;
		
	}
	
}
