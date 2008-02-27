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

package org.docx4j.fonts.microsoft;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.fonts.microsoft package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.fonts.microsoft
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MicrosoftFonts }
     * 
     */
    public MicrosoftFonts createMicrosoftFonts() {
        return new MicrosoftFonts();
    }

    /**
     * Create an instance of {@link MicrosoftFonts.Font.Italic }
     * 
     */
    public MicrosoftFonts.Font.Italic createFontsFontItalic() {
        return new MicrosoftFonts.Font.Italic();
    }

    /**
     * Create an instance of {@link MicrosoftFonts.Font }
     * 
     */
    public MicrosoftFonts.Font createFontsFont() {
        return new MicrosoftFonts.Font();
    }

    /**
     * Create an instance of {@link MicrosoftFonts.Font.Bolditalic }
     * 
     */
    public MicrosoftFonts.Font.Bolditalic createFontsFontBolditalic() {
        return new MicrosoftFonts.Font.Bolditalic();
    }

    /**
     * Create an instance of {@link MicrosoftFonts.Font.Bold }
     * 
     */
    public MicrosoftFonts.Font.Bold createFontsFontBold() {
        return new MicrosoftFonts.Font.Bold();
    }

}
