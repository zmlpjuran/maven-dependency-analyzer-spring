package org.apache.maven.shared.dependency.analyzer.spring;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class DefaultSpringXmlParser implements SpringXmlParser {

	@Override
	public void parse(InputStream springXml,
			SpringFileBeanVisitor visitor) throws Exception 
	{
		final InputStream in = new BufferedInputStream( springXml );
		try {
			final XMLInputFactory factory = XMLInputFactory.newInstance();
			final XMLEventReader reader = factory.createXMLEventReader( in );

			boolean isSpringXml = false;
			while(reader.hasNext()) 
			{
				final XMLEvent event = reader.nextEvent();

				if (event.getEventType() != XMLEvent.START_ELEMENT) {
					continue;
				}
				
				final StartElement startElement = event.asStartElement();
				final String tagName = startElement.getName().getLocalPart();
				
				if ( ! isSpringXml ) 
				{
					if ( ! "beans".equals( tagName ) ) {
						throw new NoSpringXmlException(
								"Not a Spring XML file, expected <beans> root element", 
								event.getLocation().getCharacterOffset() 
						);
					}
					isSpringXml = true;
				} 
				else if ( "bean".equals( tagName ) ) 
				{
					final String clasz =
						getClassNameFromBeanTag( startElement );

					if ( clasz != null ) {
						visitor.visitBeanDefinition( clasz );
					}
				}
			}

		}
		finally {
			in.close();
		}
	}

	@SuppressWarnings("unchecked")
	protected String getClassNameFromBeanTag(StartElement startElement) {

		// TODO: Maybe add support for factory beans ??
		// TODO: Would require analyzing the factory method to determine the return type

		final Iterator<Attribute> it = startElement.getAttributes();
		while ( it.hasNext() ) {
			final Attribute a = it.next();
			if ( "class".equals( a.getName().getLocalPart() ) ) 
			{
				return a.getValue();
			}
		}
		return null;
	}
}