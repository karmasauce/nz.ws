package com.bosch.portal.ant.task;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Ant task for loading the classpath from eclipse .classpath files
 * 
 * The classpath is loaded as Path object and is is set as property specified by
 * pathId attribute. Only the lib and var entries from .classpath file are
 * resolved. For variable resolution are used ant properties.
 * 
 * Installation: add jar file to ant's lib directory (in Eclipse add to ant's
 * classpath)
 * 
 * Usage: <taskdef
 * resource="com/bosch/portal/ant/task/eclipseclasspath.properties"/> <eclipsecp
 * pathid="build.classpath"/> ... <javac srcdir="${src}" ... <classpath> <path
 * refid="build.classpath"/> </classpath> </javac>
 * 
 * for Eclipse define all referenced variables as properties
 * 
 * @author Martin Cernak
 * 
 */
public class EclipseClasspathTask extends Task {

	public static final String DEFAULT_PATHID = "classpath";

	private String pathId = DEFAULT_PATHID;
	private boolean verbose = false;

	/**
	 * @return the pathId
	 */
	public String getPathId() {
		return pathId;
	}

	/**
	 * @param pathId
	 *            the pathId to set
	 */
	public void setPathId(String pathId) {
		this.pathId = pathId;
	}

	/**
	 * @return the verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 *            the verbose to set
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		ClassPathParser parser = new ClassPathParser();

		Path classPath = new Path(getProject());
		ClasspathHandler handler = new ClasspathHandler(classPath, getProject()
				.getBaseDir());

		parser.parse(new File(getProject().getBaseDir().getAbsolutePath(),
				".classpath"), handler);
		this.getProject().addReference(this.pathId, classPath);
	}

	/**
	 * Handler for SAX Parser
	 * 
	 * @author cem8fe
	 */
	class ClasspathHandler extends DefaultHandler {
		protected static final String ATTRNAME_PATH = "path";
		protected static final String ATTRNAME_KIND = "kind";
		protected static final String ATTR_LIB = "lib";
		protected static final String ATTR_VAR = "var";
		protected static final String ATTR_SRC = "src";
		protected static final String ATTR_OUTPUT = "output";

		private File projDir;
		private Path classPath = null;
		
		
		class OutputHandler extends DefaultHandler {
			private String outDir;
						
			public void startElement(String uri, String localName, String qName,
					Attributes atts) throws SAXException {
				if (localName.equalsIgnoreCase("classpathentry")) {
					// start by checking if the classpath is coherent at all
					String kind = atts.getValue(ATTRNAME_KIND);
					if (kind == null)
						throw new BuildException(
								"classpathentry 'kind' attribute is mandatory");
					String path = atts.getValue(ATTRNAME_PATH);
					if (path == null)
						throw new BuildException(
								"classpathentry 'path' attribute is mandatory");
					
					
					if (kind.equalsIgnoreCase(ATTR_OUTPUT)) {						
						outDir = path.trim();										
					}
				}
			}

			public String getOutDir() {
				return outDir;
			}
			
			
		}

		public ClasspathHandler(Path classPath, File projDir) {
			super();
			this.projDir = projDir;
			this.classPath = classPath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
		 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (localName.equalsIgnoreCase("classpathentry")) {
				// start by checking if the classpath is coherent at all
				String kind = atts.getValue(ATTRNAME_KIND);
				if (kind == null)
					throw new BuildException(
							"classpathentry 'kind' attribute is mandatory");
				String path = atts.getValue(ATTRNAME_PATH);
				if (path == null)
					throw new BuildException(
							"classpathentry 'path' attribute is mandatory");

				// und now try to get referenced library
				String reference;

				// get reference from VAR
				if (kind.equalsIgnoreCase(ATTR_VAR)) {
					String regex = "(\\w+)/(.*)";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(path);

					if (matcher.find()) {
						// location = (String) variables.get(matcher.group(1));
						reference = (String) getProject().getProperty(
								matcher.group(1));

						if (reference != null) {
							reference = reference + "/" + matcher.group(2);
						} else {
							log("Unable to map variable [" + matcher.group(1)
									+ "]");
							return;
						}
					} else {
						// location = (String) variables.get(name);
						reference = (String) getProject().getProperty(path);

						if (reference == null) {
							log("Unable to map variable [" + path + "]");
							return;
						}
					}

					if (verbose)
						log("Adding " + reference + ", (" + path + ")");
				}

				// get referenced from LIB
				else if (kind.equalsIgnoreCase(ATTR_LIB)) {
					if (path.startsWith("/")) {
						String temp = getProject().getBaseDir().getParent()
								+ path;
						File dir = new File(temp);
						if (dir.exists()) {
							reference = temp;
						} else {
							System.out
									.println("!!! Can't find reference in workspace : "
											+ temp);
							reference = path;
						}
					} else {
						reference = path;
					}

					if (verbose)
						log("Adding " + reference);
					// zik8fe: in case of "src" check if it starts with a "/" ->
					// used by eclipse for Project references. "src" without leading "/"
					// is used for src.api, src.core etc.
				} else if (kind.equalsIgnoreCase(ATTR_SRC)) {
					if (path.startsWith("/")) {
						
						ClassPathParser parser = new ClassPathParser();
											
						OutputHandler handler = new OutputHandler();
						parser.parse(new File(getProject().getBaseDir().getParent()
								+ path + "/.classpath"), handler);
						
						String temp = getProject().getBaseDir().getParent()
								+ path + "/" + handler.getOutDir();					
						
						
						File dir = new File(temp);
						if (dir.exists()) {
							reference = temp;
						} else {
							System.out
									.println("!!! Can't find reference in workspace : "
											+ temp);
							reference = path;
						}
						if (verbose)
							log("Adding " + reference);
					} else {
						if (verbose)
							log("Skipping entry of type: [" + kind + "]");
						return;
					}

				}
				// skip source and out
				else {
					if (verbose)
						log("Skipping entry of type: [" + kind + "]");
					return;
				}

				// now add reference to classpath
				try {
					// get file object for referenced lib
					File file = new File(reference);
					if (!file.isAbsolute()) {
						file = new File(projDir, reference);
					}

					// add to classpath
					Path.PathElement e = classPath.createPathElement();
					e.setLocation(file);
				} catch (Exception e) {
					log("Error by processing: " + e.getMessage());
				}
			}
		}

	}
}
