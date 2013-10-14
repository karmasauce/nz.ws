package com.bosch.portal.ant.task;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Tool to start SAX parser
 * 
 * @author Martin Cernak
 */
public class ClassPathParser
{
	void parse(File file, ContentHandler handler) throws BuildException
	{
		String fName = file.getName();
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser saxParser = factory.newSAXParser();
			
			XMLReader reader = saxParser.getXMLReader();
			reader.setContentHandler(handler);
			
			reader.parse( new InputSource(file.getAbsolutePath()));
		}
		catch (ParserConfigurationException pceException)
		{
			throw new BuildException("Parser configuration failed", pceException);
		}
		catch (SAXParseException exc)
		{
			Location location = new Location(fName.toString(), exc.getLineNumber(), exc.getColumnNumber());
			Throwable throwable = exc.getException();
			if ((Object) throwable instanceof BuildException)
			{
				BuildException be = (BuildException) (Object) throwable;
				if (be.getLocation() == Location.UNKNOWN_LOCATION)
					be.setLocation(location);
				throw be;
			}
			throw new BuildException(exc.getMessage(), throwable, location);
		}
		catch (SAXException exc)
		{
			Throwable throwable = exc.getException();
			if ((Object) throwable instanceof BuildException)
				throw (BuildException) (Object) throwable;
			throw new BuildException(exc.getMessage(), throwable);
		}
		catch (IOException exc)
		{
			throw new BuildException("Error reading file", exc);
		}
	}
}
