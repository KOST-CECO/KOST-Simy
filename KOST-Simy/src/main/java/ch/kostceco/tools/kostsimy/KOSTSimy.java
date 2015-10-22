/* == KOST-Simy =================================================================================
 * The KOST-Simy application is used for Compare Image-Files. Copyright (C) 2015 Claire
 * Röthlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * KOST-Simy is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This application is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or
 * see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostsimy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostsimy.controller.Controllerci;
import ch.kostceco.tools.kostsimy.controller.Controllerpdfa;
import ch.kostceco.tools.kostsimy.logging.LogConfigurator;
import ch.kostceco.tools.kostsimy.logging.Logger;
import ch.kostceco.tools.kostsimy.logging.MessageConstants;
import ch.kostceco.tools.kostsimy.service.ConfigurationService;
import ch.kostceco.tools.kostsimy.service.TextResourceService;
import ch.kostceco.tools.kostsimy.util.Util;

/** Dies ist die Starter-Klasse, verantwortlich für das Initialisieren des Controllers, des Loggings
 * und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class KOSTSimy implements MessageConstants
{

	private static final Logger		LOGGER	= new Logger( KOSTSimy.class );

	private TextResourceService		textResourceService;
	private ConfigurationService	configurationService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	/** Die Eingabe besteht aus 2 Parameter: [0] Original-Ordner [1] Replica-Ordner
	 * 
	 * @param args
	 * @throws IOException */

	public static void main( String[] args ) throws IOException
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		// Zeitstempel Start
		java.util.Date nowStart = new java.util.Date();
		java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
		String ausgabeStart = sdfStart.format( nowStart );

		KOSTSimy kostsimy = (KOSTSimy) context.getBean( "kostsimy" );
		File configFile = new File( "configuration" + File.separator + "kostsimy.conf.xml" );

		// Ueberprüfung des Parameters (Log-Verzeichnis)
		String pathToLogfile = kostsimy.getConfigurationService().getPathToLogfile();

		File directoryOfLogfile = new File( pathToLogfile );

		if ( !directoryOfLogfile.exists() ) {
			directoryOfLogfile.mkdir();
		}

		// Im Logverzeichnis besteht kein Schreibrecht
		if ( !directoryOfLogfile.canWrite() ) {
			System.out.println( kostsimy.getTextResourceService().getText(
					ERROR_LOGDIRECTORY_NOTWRITABLE, directoryOfLogfile ) );
			System.exit( 1 );
		}

		if ( !directoryOfLogfile.isDirectory() ) {
			System.out.println( kostsimy.getTextResourceService()
					.getText( ERROR_LOGDIRECTORY_NODIRECTORY ) );
			System.exit( 1 );
		}

		// Ist die Anzahl Parameter (2) korrekt?
		if ( args.length > 3 ) {
			System.out.println( kostsimy.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
			System.exit( 1 );
		}

		File origDir = new File( args[0] );
		File repDir = new File( args[1] );
		File logDatei = null;
		logDatei = origDir;

		// Informationen zum Arbeitsverzeichnis holen
		String pathToWorkDir = kostsimy.getConfigurationService().getPathToWorkDir();
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		// Konfiguration des Loggings, ein File Logger wird zusätzlich erstellt
		LogConfigurator logConfigurator = (LogConfigurator) context.getBean( "logconfigurator" );
		String logFileName = logConfigurator.configure( directoryOfLogfile.getAbsolutePath(),
				logDatei.getName() );
		File logFile = new File( logFileName );
		// Ab hier kann ins log geschrieben werden...

		LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_HEADER ) );
		LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_START, ausgabeStart ) );
		LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_END ) );
		LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_INFO ) );
		System.out.println( "KOST-Simy" );
		System.out.println( "" );

		if ( !origDir.exists() ) {
			// Das Original-Verzeichnis existiert nicht
			LOGGER
					.logError( kostsimy.getTextResourceService().getText(
							ERROR_IOE,
							kostsimy.getTextResourceService()
									.getText( ERROR_NOORIGDIR, origDir.getAbsolutePath() ) ) );
			System.out.println( kostsimy.getTextResourceService().getText( ERROR_NOORIGDIR,
					origDir.getAbsolutePath() ) );
			System.exit( 1 );
		}

		if ( !repDir.exists() ) {
			// Das Replica-Verzeichnis existiert nicht
			LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_IOE,
					kostsimy.getTextResourceService().getText( ERROR_NOREPDIR1, repDir.getAbsolutePath() ) ) );
			System.out.println( kostsimy.getTextResourceService().getText( ERROR_NOREPDIR1,
					repDir.getAbsolutePath() ) );
			System.exit( 1 );
		}

		File xslOrig = new File( "resources" + File.separator + "kost-simy.xsl" );
		File xslCopy = new File( directoryOfLogfile.getAbsolutePath() + File.separator
				+ "kost-simy.xsl" );
		if ( !xslCopy.exists() ) {
			Util.copyFile( xslOrig, xslCopy );
		}

		// Informationen zur prozentualen Stichprobe holen
		String randomTest = kostsimy.getConfigurationService().getRandomTest();
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		int iRandomTest = 100;
		try {
			iRandomTest = Integer.parseInt( randomTest );
		} catch ( Exception ex ) {
			// unzulaessige Eingabe --> 50 wird gesetzt
			iRandomTest = 50;
		}
		if ( iRandomTest > 100 || iRandomTest < 1 ) {
			// unzulaessige Eingabe --> 50 wird gesetzt
			iRandomTest = 50;
		}

		File tmpDir = new File( pathToWorkDir );

		/* bestehendes Workverzeichnis Abbruch wenn nicht leer, da am Schluss das Workverzeichnis
		 * gelöscht wird und entsprechend bestehende Dateien gelöscht werden können */
		if ( tmpDir.exists() ) {
			if ( tmpDir.isDirectory() ) {
				// Get list of file in the directory. When its length is not zero the folder is not empty.
				String[] files = tmpDir.list();
				if ( files.length > 0 ) {
					LOGGER.logError( kostsimy.getTextResourceService()
							.getText(
									ERROR_IOE,
									kostsimy.getTextResourceService().getText( ERROR_WORKDIRECTORY_EXISTS,
											pathToWorkDir ) ) );
					System.out.println( kostsimy.getTextResourceService().getText(
							ERROR_WORKDIRECTORY_EXISTS, pathToWorkDir ) );
					System.exit( 1 );
				}
			}
		}

		// Im Pfad keine Sonderzeichen Programme können evtl abstürzen

		String patternStr = "[^!#\\$%\\(\\)\\+,\\-_\\.=@\\[\\]\\{\\}\\~:\\\\a-zA-Z0-9 ]";
		Pattern pattern = Pattern.compile( patternStr );

		String name = tmpDir.getAbsolutePath();

		String[] pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];

			Matcher matcher = pattern.matcher( element );

			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_IOE,
						kostsimy.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name ) ) );
				System.out.println( kostsimy.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER,
						name ) );
				System.exit( 1 );
			}
		}

		// die Anwendung muss mindestens unter Java 6 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.6.0" ) < 0 ) {
			LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_IOE,
					kostsimy.getTextResourceService().getText( ERROR_WRONG_JRE ) ) );
			System.out.println( kostsimy.getTextResourceService().getText( ERROR_WRONG_JRE ) );
			System.exit( 1 );
		}

		// bestehendes Workverzeichnis wieder anlegen
		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
			File origDirTmp = new File( tmpDir.getAbsolutePath() + File.separator + "orig" );
			File repDirTmp = new File( tmpDir.getAbsolutePath() + File.separator + "rep" );
			origDirTmp.mkdir();
			repDirTmp.mkdir();
		}

		// Im workverzeichnis besteht kein Schreibrecht
		if ( !tmpDir.canWrite() ) {
			LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_IOE,
					kostsimy.getTextResourceService().getText( ERROR_WORKDIRECTORY_NOTWRITABLE, tmpDir ) ) );
			System.out.println( kostsimy.getTextResourceService().getText(
					ERROR_WORKDIRECTORY_NOTWRITABLE, tmpDir ) );
			System.exit( 1 );
		}

		// Im Pfad keine Sonderzeichen --> Absturzgefahr
		name = origDir.getAbsolutePath();
		pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];
			Matcher matcher = pattern.matcher( element );
			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_IOE,
						kostsimy.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name ) ) );
				System.out.println( kostsimy.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER,
						name ) );
				System.exit( 1 );
			}
		}
		name = repDir.getAbsolutePath();
		pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];
			Matcher matcher = pattern.matcher( element );
			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_IOE,
						kostsimy.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name ) ) );
				System.out.println( kostsimy.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER,
						name ) );
				System.exit( 1 );
			}
		}

		LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_IMAGE1 ) );
		float count = 0;
		int countNio = 0;
		int countIo = 0;
		float countVal = 0;
		int countNotVal = 0;
		float percentage = (float) 0.0;

		if ( !origDir.isDirectory() ) {
			// TODO: Bildervergleich zweier Dateien --> erledigt --> nur Marker

			if ( repDir.isDirectory() ) {
				// Das Replica-ist ein Verzeichnis, aber Original eine Datei
				LOGGER
						.logError( kostsimy.getTextResourceService().getText(
								ERROR_IOE,
								kostsimy.getTextResourceService().getText( ERROR_NOREPDIR2,
										repDir.getAbsolutePath() ) ) );
				System.out.println( kostsimy.getTextResourceService().getText( ERROR_NOREPDIR2,
						repDir.getAbsolutePath() ) );
				System.exit( 1 );
			}
			boolean compFile = compFile( origDir, logFileName, directoryOfLogfile, repDir, tmpDir );

			float statIo = 0;
			int statNio = 0;
			float statUn = 0;
			if ( compFile ) {
				statIo = 100;
			} else {
				statNio = 100;
			}

			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_IMAGE2 ) );
			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_STATISTICS, statIo,
					statNio, statUn ) );

			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
			// Zeitstempel End
			java.util.Date nowEnd = new java.util.Date();
			java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
			String ausgabeEnd = sdfEnd.format( nowEnd );
			ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
			Util.valEnd( ausgabeEnd, logFile );
			Util.amp( logFile );

			// Die Konfiguration hereinkopieren
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating( false );

				factory.setExpandEntityReferences( false );

				Document docConfig = factory.newDocumentBuilder().parse( configFile );
				NodeList list = docConfig.getElementsByTagName( "configuration" );
				Element element = (Element) list.item( 0 );

				Document docLog = factory.newDocumentBuilder().parse( logFile );

				Node dup = docLog.importNode( element, true );

				docLog.getDocumentElement().appendChild( dup );
				FileWriter writer = new FileWriter( logFile );

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ElementToStream( docLog.getDocumentElement(), baos );
				String stringDoc2 = new String( baos.toByteArray() );
				writer.write( stringDoc2 );
				writer.close();

				// Der Header wird dabei leider verschossen, wieder zurück ändern
				String newstring = kostsimy.getTextResourceService().getText( MESSAGE_XML_HEADER );
				String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KOSTSimyLog>";
				Util.oldnewstring( oldstring, newstring, logFile );

			} catch ( Exception e ) {
				LOGGER.logError( "<Error>"
						+ kostsimy.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				System.out.println( "Exception: " + e.getMessage() );
			}

			if ( compFile ) {
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				// Validierte Datei valide
				System.exit( 0 );
			} else {
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				// Fehler in Validierte Datei --> invalide
				System.exit( 2 );

			}
		} else {
			// TODO: Bildervergleich zweier Verzeichnisse --> in Arbeit --> nur Marker
			if ( !repDir.isDirectory() ) {
				// Das Replica-ist eine Datei, aber Original ein Ordner
				LOGGER
						.logError( kostsimy.getTextResourceService().getText(
								ERROR_IOE,
								kostsimy.getTextResourceService().getText( ERROR_NOREPDIR3,
										repDir.getAbsolutePath() ) ) );
				System.out.println( kostsimy.getTextResourceService().getText( ERROR_NOREPDIR3,
						repDir.getAbsolutePath() ) );
				System.exit( 1 );
			}

			Map<String, File> fileMap = Util.getFileMap( origDir, false );
			Set<String> fileMapKeys = fileMap.keySet();
			boolean other = false;

			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
				String entryName = iterator.next();
				File newFile = fileMap.get( entryName );
				if ( !newFile.isDirectory() ) {
					origDir = newFile;
					count = count + 1;
					if ( (origDir.getAbsolutePath().toLowerCase().endsWith( ".pdf" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".pdfa" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".tif" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".tiff" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jpeg" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jpg" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jpe" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jp2" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".gif" )
							|| origDir.getAbsolutePath().toLowerCase().endsWith( ".png" ) || origDir
							.getAbsolutePath().toLowerCase().endsWith( ".bmp" )) ) {
						percentage = 100 / count * countVal;
						if ( percentage < iRandomTest ) {
							// if ( 100 / count * (countVal + 1) <= iRandomTest ) {

							countVal = countVal + 1;

							String origWithOutExt = FilenameUtils.removeExtension( origDir.getName() );

							File repFile = new File( repDir.getAbsolutePath() + File.separator + origWithOutExt
									+ ".pdf" );
							if ( !repFile.exists() ) {
								repFile = new File( repDir.getAbsolutePath() + File.separator + origWithOutExt
										+ ".pdfa" );
								if ( !repFile.exists() ) {
									repFile = new File( repDir.getAbsolutePath() + File.separator + origWithOutExt
											+ ".tif" );
									if ( !repFile.exists() ) {
										repFile = new File( repDir.getAbsolutePath() + File.separator + origWithOutExt
												+ ".tiff" );
										if ( !repFile.exists() ) {
											repFile = new File( repDir.getAbsolutePath() + File.separator
													+ origWithOutExt + ".jpeg" );
											if ( !repFile.exists() ) {
												repFile = new File( repDir.getAbsolutePath() + File.separator
														+ origWithOutExt + ".jpg" );
												if ( !repFile.exists() ) {
													repFile = new File( repDir.getAbsolutePath() + File.separator
															+ origWithOutExt + ".jpe" );
													if ( !repFile.exists() ) {
														repFile = new File( repDir.getAbsolutePath() + File.separator
																+ origWithOutExt + ".jp2" );
														if ( !repFile.exists() ) {
															repFile = new File( repDir.getAbsolutePath() + File.separator
																	+ origWithOutExt + ".gif" );
															if ( !repFile.exists() ) {
																repFile = new File( repDir.getAbsolutePath() + File.separator
																		+ origWithOutExt + ".png" );
																if ( !repFile.exists() ) {
																	repFile = new File( repDir.getAbsolutePath() + File.separator
																			+ origWithOutExt + ".bmp" );
																	if ( !repFile.exists() ) {
																		other = true;
																		LOGGER.logError( kostsimy.getTextResourceService().getText(
																				MESSAGE_XML_VALERGEBNIS ) );
																		LOGGER.logError( kostsimy.getTextResourceService().getText(
																				MESSAGE_XML_COMPFILE, origDir ) );
																		LOGGER.logError( kostsimy.getTextResourceService().getText(
																				MESSAGE_XML_VALERGEBNIS_NOTVALIDATED ) );
																		LOGGER.logError( kostsimy.getTextResourceService().getText(
																				ERROR_NOREP, origDir.getName() ) );
																		LOGGER.logError( kostsimy.getTextResourceService().getText(
																				MESSAGE_XML_VALERGEBNIS_CLOSE ) );
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}

							if ( !other ) {
								boolean compFile = compFile( origDir, logFileName, directoryOfLogfile, repFile,
										tmpDir );
								if ( compFile ) {
									// Vergleich bestanden
									countIo = countIo + 1;
								} else {
									// Vergleich nicht bestanden
									countNio = countNio + 1;
								}
							}
						} else {
							countNotVal = countNotVal + 1;
							LOGGER
									.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
							LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_COMPFILE,
									origDir ) );
							LOGGER.logError( kostsimy.getTextResourceService().getText(
									MESSAGE_XML_VALERGEBNIS_NOTVALIDATED ) );
							LOGGER.logError( kostsimy.getTextResourceService().getText(
									MESSAGE_XML_VALERGEBNIS_CLOSE ) );
						}
					} else {
						countNotVal = countNotVal + 1;
						LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
						LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_COMPFILE,
								origDir ) );
						LOGGER.logError( kostsimy.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_NOTVALIDATED ) );
						LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
								origDir ) );
						LOGGER.logError( kostsimy.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_CLOSE ) );
					}
				}
			}

			if ( countNio == 0 && countIo == 0 ) {
				// keine Dateien verglichen
				LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_INCORRECTFILEENDINGS ) );
				System.out
						.println( kostsimy.getTextResourceService().getText( ERROR_INCORRECTFILEENDINGS ) );
			}

			float statIo = 100 / (float) count * (float) countIo;
			float statNio = 100 / (float) count * (float) countNio;
			float statUn = 100 - statIo - statNio;

			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_IMAGE2 ) );
			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_STATISTICS, statIo,
					statNio, statUn ) );
			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
			// Zeitstempel End
			java.util.Date nowEnd = new java.util.Date();
			java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
			String ausgabeEnd = sdfEnd.format( nowEnd );
			ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
			Util.valEnd( ausgabeEnd, logFile );
			Util.amp( logFile );

			// Die Konfiguration hereinkopieren
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating( false );

				factory.setExpandEntityReferences( false );

				Document docConfig = factory.newDocumentBuilder().parse( configFile );
				NodeList list = docConfig.getElementsByTagName( "configuration" );
				Element element = (Element) list.item( 0 );

				Document docLog = factory.newDocumentBuilder().parse( logFile );

				Node dup = docLog.importNode( element, true );

				docLog.getDocumentElement().appendChild( dup );
				FileWriter writer = new FileWriter( logFile );

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ElementToStream( docLog.getDocumentElement(), baos );
				String stringDoc2 = new String( baos.toByteArray() );
				writer.write( stringDoc2 );
				writer.close();

				// Der Header wird dabei leider verschossen, wieder zurück ändern
				String newstring = kostsimy.getTextResourceService().getText( MESSAGE_XML_HEADER );
				String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KOSTSimyLog>";
				Util.oldnewstring( oldstring, newstring, logFile );

			} catch ( Exception e ) {
				LOGGER.logError( "<Error>"
						+ kostsimy.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				System.out.println( "Exception: " + e.getMessage() );
			}

			if ( countNio == 0 && countIo == 0 ) {
				// keine Dateien verglichen bestehendes Workverzeichnis ggf. löschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				System.exit( 1 );
			} else if ( countNio == 0 ) {
				// bestehendes Workverzeichnis ggf. löschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				// alle Validierten Dateien valide
				System.exit( 0 );
			} else {
				// bestehendes Workverzeichnis ggf. löschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				// Fehler in Validierten Dateien --> invalide
				System.exit( 2 );
			}
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
				tmpDir.deleteOnExit();
			}
		}
	}

	// TODO: compFile --> Vergleich zweier Bilder (Sub-Programm) --> in Arbeit --> nur Marker
	private static boolean compFile( File origDir, String logFileName, File directoryOfLogfile,
			File repDir, File tmpDir ) throws IOException

	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		KOSTSimy kostsimy = (KOSTSimy) context.getBean( "kostsimy" );
		String originalName = origDir.getAbsolutePath();
		String replicaName = repDir.getAbsolutePath();
		boolean compFile = false;
		boolean okMandatoryPdfa = true;
		boolean okFileO = false;
		boolean okFileR = true;
		boolean okMandatory = false;

		if ( (origDir.getAbsolutePath().toLowerCase().endsWith( ".tif" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".tiff" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jpeg" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jpg" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jpe" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".jp2" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".gif" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".png" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".pdf" )
				|| origDir.getAbsolutePath().toLowerCase().endsWith( ".pdfa" ) || origDir.getAbsolutePath()
				.toLowerCase().endsWith( ".bmp" )) ) {
			// Das Format wird Unterstützt (Original)
			okFileO = true;

			// Log für Vergleich der Datei beginnen
			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
					kostsimy.getTextResourceService().getText( MESSAGE_COMPARISON ) ) );
			LOGGER.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_COMPFILES,
					originalName, replicaName ) );
			System.out.println( kostsimy.getTextResourceService().getText( MESSAGE_COMPARISON ) );
			System.out.println( origDir.getName() + " ?= " + repDir.getName() );

			if ( repDir.getAbsolutePath().toLowerCase().endsWith( ".pdf" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".pdfa" )
					|| origDir.getAbsolutePath().toLowerCase().endsWith( ".pdf" )
					|| origDir.getAbsolutePath().toLowerCase().endsWith( ".pdfa" ) ) {
				// Aus PDF(s) Bild extrahieren (original und Replica)
				Controllerpdfa controller2 = (Controllerpdfa) context.getBean( "controllerpdfa" );
				okMandatoryPdfa = controller2.executeMandatory( origDir, repDir, directoryOfLogfile );

				// Kontrolle ob die Bilder extrahiert wurden und auf origDir resp. repDir umschreiben
				if ( origDir.getAbsolutePath().toLowerCase().endsWith( ".pdf" )
						|| origDir.getAbsolutePath().toLowerCase().endsWith( ".pdfa" ) ) {
					// Das Format wird Unterstützt (Original)
					okFileO = true;
					File origDirJPEG = new File( tmpDir.getAbsolutePath() + File.separator + "orig"
							+ File.separator + origDir.getName() + ".jpg" );
					if ( !origDirJPEG.exists() ) {
						File origDirJP2 = new File( tmpDir.getAbsolutePath() + File.separator + "orig"
								+ File.separator + origDir.getName() + ".jp2" );
						if ( !origDirJP2.exists() ) {
							okFileO = false;
						} else {
							origDir = origDirJP2;
						}
					} else {
						origDir = origDirJPEG;
					}
				}
				if ( repDir.getAbsolutePath().toLowerCase().endsWith( ".pdf" )
						|| repDir.getAbsolutePath().toLowerCase().endsWith( ".pdfa" ) ) {
					// Das Format wird Unterstützt (Replikat)
					okFileR = true;
					File repDirJPEG = new File( tmpDir.getAbsolutePath() + File.separator + "rep"
							+ File.separator + repDir.getName() + ".jpg" );
					if ( !repDirJPEG.exists() ) {
						File repDirJP2 = new File( tmpDir.getAbsolutePath() + File.separator + "rep"
								+ File.separator + repDir.getName() + ".jp2" );
						if ( !repDirJP2.exists() ) {
							okFileR = false;
						} else {
							repDir = repDirJP2;
						}
					} else {
						repDir = repDirJPEG;
					}
				}
			}

			if ( (repDir.getAbsolutePath().toLowerCase().endsWith( ".tif" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".tiff" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".jpeg" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".jpg" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".jpe" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".jp2" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".gif" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".png" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".bmp" )
					|| repDir.getAbsolutePath().toLowerCase().endsWith( ".pdf" ) || repDir.getAbsolutePath()
					.toLowerCase().endsWith( ".pdfa" )) ) {
				// Das Format wird Unterstützt (Replikat)
				okFileR = true;
			} else {
				// Datei wird nicht unterstützt
				okFileR = false;
				okMandatory = false;
				LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
						repDir.getName() ) );
				System.out.println( kostsimy.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
						repDir.getName() ) );
			}

			if ( okFileO && okFileR && okMandatoryPdfa && repDir.exists() && origDir.exists() ) {
				// JPEG und JP2 konnte aus PDF extrahiert werden
				Controllerci controller1 = (Controllerci) context.getBean( "controllerci" );
				okMandatory = controller1.executeMandatory( origDir, repDir, directoryOfLogfile );
			} else {
				// Fehler JPEG konnte nicht aus PDF extrahiert werden --> invalide
				System.out.println( "Error" );
				System.out.println( "" );
			}

			if ( okMandatory ) {
				// Bilder sind ähnlich --> valide Konvertierung

				// Maske und IM-Reports löschen
				File reportIm = new File( directoryOfLogfile.getAbsolutePath() + File.separator
						+ origDir.getName() + "_compare_report.txt" );
				if ( reportIm.exists() ) {
					Util.deleteDir( reportIm );
				}
				File reportImId = new File( directoryOfLogfile.getAbsolutePath() + File.separator
						+ origDir.getName() + "_identify_report.txt" );
				if ( reportImId.exists() ) {
					Util.deleteDir( reportImId );
				}
				File maskImgcmp = new File( directoryOfLogfile.getAbsolutePath() + File.separator
						+ origDir.getName() + "_mask.jpg" );
				if ( maskImgcmp.exists() ) {
					Util.deleteDir( maskImgcmp );
				}
				LOGGER
						.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
				LOGGER
						.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( "Similar" );
				System.out.println( "" );
			} else {
				// Bilder unterscheiden sich --> evtl invalide Konvertierung

				// IMGCMP-Report löschen
				File reportIm = new File( directoryOfLogfile.getAbsolutePath() + File.separator
						+ origDir.getName() + "_compare_report.txt" );
				if ( reportIm.exists() ) {
					Util.deleteDir( reportIm );
				}
				File reportImId = new File( directoryOfLogfile.getAbsolutePath() + File.separator
						+ origDir.getName() + "_identify_report.txt" );
				if ( reportImId.exists() ) {
					Util.deleteDir( reportImId );
				}
				File maskImgcmp = new File( directoryOfLogfile.getAbsolutePath() + File.separator
						+ origDir.getName() + "_mask.jpg" );

				LOGGER.logError( kostsimy.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_INVALID, maskImgcmp.getName() ) );
				LOGGER
						.logError( kostsimy.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( "Dissimilar" );
				System.out.println( "" );
			}
		} else {
			okFileO = false;
			okMandatory = false;
			LOGGER.logError( kostsimy.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
					origDir.getName() ) );
			System.out.println( kostsimy.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
					origDir.getName() ) );
		}
		compFile = okMandatory;
		return compFile;
	}

	public static void ElementToStream( Element element, OutputStream out )
	{
		try {
			DOMSource source = new DOMSource( element );
			StreamResult result = new StreamResult( out );
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.transform( source, result );
		} catch ( Exception ex ) {
		}
	}

}
