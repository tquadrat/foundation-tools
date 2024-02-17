/*
 * ============================================================================
 *  Copyright © 2002-2024 by Thomas Thrien.
 *  All Rights Reserved.
 * ============================================================================
 *  Licensed to the public under the agreements of the GNU Lesser General Public
 *  License, version 3.0 (the "License"). You may obtain a copy of the License at
 *
 *       http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package org.tquadrat.foundation.tools;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.Arrays.stream;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.CommonConstants.UTF8;
import static org.tquadrat.foundation.lang.Objects.isNull;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.util.StringUtils.splitString;
import static org.tquadrat.foundation.xml.builder.XMLBuilderUtils.createProcessingInstruction;
import static org.tquadrat.foundation.xml.builder.XMLBuilderUtils.createXMLDocument;
import static org.tquadrat.foundation.xml.builder.XMLBuilderUtils.createXMLElement;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.annotation.ProgramClass;
import org.tquadrat.foundation.util.Stack;
import org.tquadrat.foundation.util.stringconverter.FileStringConverter;
import org.tquadrat.foundation.util.stringconverter.URIStringConverter;
import org.tquadrat.foundation.xml.builder.Namespace;
import org.tquadrat.foundation.xml.builder.ProcessingInstruction;
import org.tquadrat.foundation.xml.builder.XMLDocument;
import org.tquadrat.foundation.xml.builder.XMLElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  Beautifies XML code.
 *
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @version $Id: HexUtils.java 747 2020-12-01 12:40:38Z tquadrat $
 *  @since 0.4.2
 *
 *  @UMLGraph.link
 */
@SuppressWarnings( "UseOfSystemOutOrSystemErr" )
@ClassVersion( sourceVersion = "$Id: HexUtils.java 747 2020-12-01 12:40:38Z tquadrat $" )
@API( status = STABLE, since = "0.4.2" )
@ProgramClass
public final class XMLBeautifier
{
        /*---------------*\
    ====** Inner Classes **====================================================
        \*---------------*/
    /**
     *  The implementation of
     *  {@link DefaultHandler}
     *  that is used to parse the XML code and to build the
     *  {@link org.tquadrat.foundation.xml.builder.XMLDocument}
     *  for the output.
     *
     *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
     *  @version $Id: HexUtils.java 747 2020-12-01 12:40:38Z tquadrat $
     *  @since 0.4.2
     *
     *  @UMLGraph.link
     */
    @SuppressWarnings( "UseOfSystemOutOrSystemErr" )
    @ClassVersion( sourceVersion = "$Id: HexUtils.java 747 2020-12-01 12:40:38Z tquadrat $" )
    @API( status = STABLE, since = "0.4.2" )
    private static final class XMLHandler extends DefaultHandler
    {
            /*------------*\
        ====** Attributes **===================================================
            \*------------*/
        /**
         *  The document for the output.
         */
        private XMLDocument m_Document;

        /**
         *  The element stack.
         */
        @SuppressWarnings( "UseOfConcreteClass" )
        private final Stack<XMLElement> m_ElementStack = new Stack<>();

        /**
         *  The processing instructions.
         */
        private final Collection<ProcessingInstruction> m_ProcessingInstructions = new ArrayList<>();

            /*--------------*\
        ====** Constructors **=================================================
            \*--------------*/
        /**
         *  Creates a new instance of {@code XMLHandler}.
         */
        public XMLHandler()
        {
            // Just exists …
        }   //  XMLHandler()

            /*---------*\
        ====** Methods **======================================================
            \*---------*/
        /**
         *  {@inheritDoc}
         */
        @Override
        public final void endDocument() throws SAXException
        {
            m_ProcessingInstructions.forEach( m_Document::addProcessingInstruction );
        }   //  endDocument()

        /**
         *  {@inheritDoc}
         */
        @Override
        public final void endElement( final String uri, final String localName, final String qName ) throws SAXException
        {
            if( m_ElementStack.hasMore() ) m_ElementStack.pop();
        }   //  endElement()

        /**
         *  {@inheritDoc}
         */
        @Override
        public void error( final SAXParseException e ) throws SAXException
        {
            err.println( "*** ERROR! ***" );
            e.printStackTrace( err );
        }   //  error()

        /**
         *  {@inheritDoc}
         */
        @Override
        public void fatalError( final SAXParseException e ) throws SAXException
        {
            err.println( "****** FATAL ERROR! ******" );
            e.printStackTrace( err );
        }   //  fatalError()

        /**
         *  Returns the output.
         *
         *  @return An instance of
         *      {@link Optional}
         *      that holds the beautified code.
         */
        public final Optional<String> getOutput()
        {
            final var retValue = Optional.ofNullable( m_Document )
                .map( XMLDocument::toString );

            //---* Done *----------------------------------------------------------
            return retValue;
        }   //  getOutput()

        /**
         *  {@inheritDoc}
         */
        @Override
        public final void processingInstruction( final String target, final String data ) throws SAXException
        {
            m_ProcessingInstructions.add( createProcessingInstruction( target, data ) );
        }   //  processingInstruction()

        /**
         *  Writes the given attributes to the target element.
         *
         *  @param  attributes  The attributes.
         *  @param  attributeSetter The attributes setter method.
         *  @param  namespaceSetter The namespace setter method.
         */
        private final void setAttributes( final Attributes attributes, final BiConsumer<? super String,? super CharSequence> attributeSetter, final Consumer<? super Namespace> namespaceSetter )
        {
            requireNonNullArgument( attributeSetter, "attributeSetter" );
            for( var i = 0; i < requireNonNullArgument( attributes, "attributes" ).getLength(); ++i )
            {
                final var attributeName = isNull( attributes.getQName( i ) ) ? attributes.getLocalName( i ) : attributes.getQName( i );
                final var attributeValue = attributes.getValue( i );

                if( attributeName.startsWith( "xmlns" ) )
                {
                    final var nameParts = splitString( attributeName, ':' );
                    final var uri = URIStringConverter.INSTANCE.fromString( attributeValue );
                    final var namespace = nameParts.length == 1 ? new Namespace( uri ) : new Namespace( nameParts [1], uri );
                    namespaceSetter.accept( namespace );
                }
                else
                {
                    attributeSetter.accept( attributeName, attributeValue );
                }
            }
        }   //  setAttributes()

        /**
         *  {@inheritDoc}
         */
        @Override
        public final void startElement( final String uri, final String localName, final String qName, final Attributes attributes ) throws SAXException
        {
            final var elementName = isNull( qName ) ? localName : qName;
            if( isNull( m_Document ) )
            {
                m_Document = createXMLDocument( elementName );
                setAttributes( attributes, m_Document::setAttribute, m_Document::setNamespace );
            }
            else
            {
                final XMLElement currentElement;
                if( m_ElementStack.peek().isEmpty() )
                {
                    currentElement = createXMLElement( elementName, m_Document );
                }
                else
                {
                    final var parent = m_ElementStack.peek();
                    //noinspection OptionalGetWithoutIsPresent
                    currentElement = createXMLElement( elementName, parent.get() );
                }
                setAttributes( attributes, currentElement::setAttribute, currentElement::setNamespace );
                m_ElementStack.push( currentElement );
            }
        }   //  startElement()

        /**
         *  {@inheritDoc}
         */
        @Override
        public final void warning( final SAXParseException e ) throws SAXException
        {
            err.println( "WARNING!" );
            e.printStackTrace( err );
        }   //  warning()
    }
    //  class XMLHandler

        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/
    /**
     *  The XML to beautify.
     */
    private final String m_XMLCode;

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code BeautifyXML}.
     *
     *  @param  xmlCode The XML code to beautify.
     */
    public XMLBeautifier( final String xmlCode )
    {
        m_XMLCode = xmlCode;
    }   //  XMLBeautifier()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  The program entry point.
     *
     *  @param  args    The command line arguments.
     */
    @SuppressWarnings( "OverlyBroadCatchBlock" )
    public static final void main( final String... args )
    {
        try
        {
            if( args.length == 1 )
            {
                final var file = FileStringConverter.INSTANCE.fromString( args [0] );
                final var path = file.toPath();
                final var xmlCode = Files.readString( path, UTF8 );
                final var processor = new XMLBeautifier( xmlCode );
                final var result = processor.process();
                out.println( result );
                out.println( "Done!" );
            }
            else
            {
                final var buffer = new StringJoiner( " " );
                buffer.setEmptyValue( "[missing Filename]" );
                stream( args ).forEach( buffer::add );
                err.printf( "Invalid Command Line Arguments: %s %s%n", XMLBeautifier.class.getName(), buffer.toString() );
            }
        }
        catch( final Throwable t )
        {
            t.printStackTrace( err );
        }
    }   //  main()

    /**
     *  Processes the XML code and returns the beautified version of it.
     *
     *  @return The beautified code.
     *  @throws ParserConfigurationException    There is a problem on loading
     *      the SAX parser.
     *  @throws SAXException    A problem occurred while parsing the XML code.
     *  @throws IOException A problem occurred when reading the XML code.
     */
    public final String process() throws ParserConfigurationException, SAXException, IOException
    {
        //---* Initialize the parser *-----------------------------------------
        final var factory = SAXParserFactory.newInstance();
        final var parser = factory.newSAXParser();

        //---* Parse the XML code *--------------------------------------------
        final var inputStream = new ByteArrayInputStream( m_XMLCode.getBytes( UTF8 ) );
        final var handler = new XMLHandler();
        parser.parse( inputStream, handler );

        final var retValue = handler.getOutput()
            .orElse( "<No Output>" );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  process()
}
//  class XMLBeautifier

/*
 *  End of File
 */