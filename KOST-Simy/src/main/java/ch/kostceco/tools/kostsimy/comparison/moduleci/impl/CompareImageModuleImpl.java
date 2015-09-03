/* == KOST-Simy =================================================================================
 * The KOST-Simy application is used for Compare TIFF, JPEG and PDF/A-Files. Copyright (C) 2015
 * Claire Röthlisberger (KOST-CECO)
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

package ch.kostceco.tools.kostsimy.comparison.moduleci.impl;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ch.kostceco.tools.kostsimy.comparison.ComparisonModuleImpl;
import ch.kostceco.tools.kostsimy.comparison.moduleci.CompareImageModule;
import ch.kostceco.tools.kostsimy.exception.moduleci.CompareImageException;
import ch.kostceco.tools.kostsimy.service.ConfigurationService;
import ch.kostceco.tools.kostsimy.util.StreamGobbler;
import ch.kostceco.tools.kostsimy.util.Util;

/** Vergleicht die beiden Bilder mit CompareImageCommandLine und wertet den Report aus
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class CompareImageModuleImpl extends ComparisonModuleImpl implements CompareImageModule
{
	public static String					NEWLINE	= System.getProperty( "line.separator" );
	private ConfigurationService	configurationService;

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File origDatei, File repDatei, File directoryOfLogfile )
			throws CompareImageException
	{
		boolean isValid = true;
		boolean isValidFailed = false;

		// Initialisierung IMGCMP -> überprüfen der Angaben: existiert die imgcmpExe am angebenen Ort?
		String pathToImgcmpExe = getConfigurationService().getPathToImgcmpExe();
		String imgcmpLicenseKey = getConfigurationService().imgcmpLicenseKey();
		String imgcmpToleranceTxt = getConfigurationService().imgcmpTolerance();
		String imgcmpTolerance = "10%";
		double percentageInvalid = 99.9999;
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		File fImgcmpExe = new File( pathToImgcmpExe );
		if ( !fImgcmpExe.exists() || !fImgcmpExe.getName().equals( "imgcmp.exe" ) ) {
			// imgcmp.exe existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMGCMP_MISSING ) );
			return false;
		}

		if ( imgcmpLicenseKey.equalsIgnoreCase( "TrailVersion" ) ) {
			// imgcmp.exe existiert nicht in der Vollversion --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMGCMP_LICENSE ) );
			return false;
		}

		if ( imgcmpToleranceTxt.equalsIgnoreCase( "small" ) ) {
			// small = 5%
			imgcmpTolerance = "5%";
			percentageInvalid = 99.999999;
		} else if ( imgcmpToleranceTxt.equalsIgnoreCase( "large" ) ) {
			// large = 20%
			imgcmpTolerance = "20%";
			percentageInvalid = 99.99;
		} else {
			// medium = 10%
			imgcmpTolerance = "10%";
			percentageInvalid = 99.9999;
		}

		pathToImgcmpExe = "\"" + pathToImgcmpExe + "\"";

		File report;
		StringBuffer concatenatedOutputs = new StringBuffer();

		try {

			String pathToOutput = directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_imgcmp_report.txt";

			/* C:\Software\ImageCompareCommandLine\imgcmp.exe img1:C:\TEMP\tiff2pdfa\gpl2.tif
			 * img2:C:\TEMP\tiff2pdfa\gpl2.jpg outimg:C:\TEMP\tiff2pdfa\gpl2_mask.bmp tolerance:20%
			 * trans:20 reportto:C:\TEMP\tiff2pdfa\gpl2.txt license:(imgcmpLicenseKey) */

			/* pathToImgcmpExe img1:origDatei img2:repDatei outimg:directoryOfLogfile\origName_mask.bmp
			 * tolerance:imgcmpTolerance trans:20 reportto:directoryOfLogfile\origName_imgcmp_report.txt
			 * license:imgcmpLicenseKey */
			StringBuffer command = new StringBuffer( pathToImgcmpExe + " " );
			command.append( "img1:" + "\"" + origDatei.getAbsolutePath() + "\" " );
			command.append( "img2:" + "\"" + repDatei.getAbsolutePath() + "\" " );
			command.append( "outimg:" + "\"" + directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_mask.bmp\" " );
			command.append( "tolerance:" + imgcmpTolerance + " " );
			command.append( "trans:20 " );
			command.append( "reportto:" + "\"" + pathToOutput + "\" " );
			command.append( "license:" + imgcmpLicenseKey );

			Process proc = null;
			Runtime rt = null;

			try {
				report = new File( pathToOutput );

				// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, löschen
				// wir es
				if ( report.exists() ) {
					report.delete();
				}

				Util.switchOffConsole();

				rt = Runtime.getRuntime();
				proc = rt.exec( command.toString().split( " " ) );
				// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

				// Fehleroutput holen
				StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

				// Output holen
				StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

				// Threads starten
				errorGobbler.start();
				outputGobbler.start();

				// Warte, bis wget fertig ist
				proc.waitFor();

				Util.switchOnConsole();
			} catch ( Exception e ) {
				getMessageService()
						.logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
										+ getTextResourceService().getText( ERROR_XML_IMGCMP_SERVICEFAILED,
												e.getMessage() ) );
				return false;
			} finally {
				if ( proc != null ) {
					closeQuietly( proc.getOutputStream() );
					closeQuietly( proc.getInputStream() );
					closeQuietly( proc.getErrorStream() );
				}
			}
			// Ende IMGCMP direkt auszulösen

			try {
				@SuppressWarnings("resource")
				BufferedReader in = new BufferedReader( new FileReader( report ) );
				String line;
				String imgSize1 = "1";
				String imgSize2 = "1";
				while ( (line = in.readLine()) != null ) {

					concatenatedOutputs.append( line );
					concatenatedOutputs.append( NEWLINE );

					/* die Similarity-Zeile enthält normalerweise bei Ähnlichen Bilder [Similarity]: 100.00%
					 * -----------------------------------------------------------------
					 * 
					 * Bei sehr vielen Pixels hat es aber einen Rechnungsfehler und wird negativ...
					 * 
					 * Als Alternative wird "[Different Pixel Count in Sharing Image]: 0 of " verwendet
					 * ähnliche Bilder: "[Different Pixel Count in Sharing Image]: 0 of " (darin ist die
					 * Toleranz pro pixel bereits enthalten. Bilder mit einer Abweichung: <0 */

					if ( line.contains( "[Different Pixel Count in Sharing Image]" ) ) {
						if ( !line.contains( ": 0 of " ) ) {
							/* Bilder mit einer Abweichung: Prozent ermitteln und mit percentageInvalid abgleichen */
							double z1 = 0;
							double z2 = 0;
							double z2temp = 0;
							double percentageCalc = 0.0;
							double percentageCalcInv = 0.0;

							/* Invalide px und total px aus
							 * "[Different Pixel Count in Sharing Image]: 3 of 46768272 px" extrahieren */
							String lineReport = line.substring( 42 );
							// lineReport = 3 of 46768272 px

							// Wörter trennen und speichern der beiden Zahlen-Elemente
							String[] strArray = lineReport.split( "\\s" );
							for ( String element : strArray ) {
								if ( !element.contains( "of" ) && !element.contains( "px" ) ) {
									// zahl 3 oder 46768272 in unserem Beispiel
									if ( z1 == 0 ) {
										z1 = Double.parseDouble(element);
									} else {
										z2 = Double.parseDouble(element);
									}
								}
							}

							// Prozentzahl ausrechnen
							if ( z2 < z1 ) {
								z2temp = z1;
								z1=z2;
								z2=z2temp;
							}
percentageCalc = 100 - (100 / z2 * z1);
percentageCalcInv = 100 - percentageCalc;

							// Prozetzahlen vergleichen

							if ( percentageInvalid > percentageCalc ) {
								// Bilder mit einer grösseren Abweichung
								isValid = false;

								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
												+ getTextResourceService().getText( ERROR_XML_CI_CIINVALID, percentageCalcInv, z2,
														imgcmpToleranceTxt ) );
							}
						}
					}
					if ( line.contains( "[Image 1 Size]:" ) ) {
						/* Grösse Vergleichen: [Image 1 Size]: 662x936 = 619632 px [Image 2 Size]: 662x936 =
						 * 619632 px */
						imgSize1 = line.substring( 15 );
					}
					if ( line.contains( "[Image 2 Size]:" ) ) {
						imgSize2 = line.substring( 15 );
					}
					if ( line.contains( "Illegal license key:" ) ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
										+ getTextResourceService().getText( ERROR_XML_IMGCMP_LICENSE ) );
						return false;
					}
					if ( line.contains( "Failed to compare because:" ) ) {
						isValidFailed = true;
					}
				}
				if ( !imgSize1.equals( imgSize2 ) ) {
					// die beiden Bilder sind nicht gleich gross
					isValid = false;
					getMessageService()
							.logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
											+ getTextResourceService().getText( ERROR_XML_CI_SIZEINVALID, imgSize1,
													imgSize2 ) );
				}
				if ( isValidFailed ) {
					// Fehler evtl. Bild nicht io
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
									+ getTextResourceService().getText( ERROR_XML_CI_CIFAILED ) );
					isValid = false;
				}

				in.close();
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return isValid;
	}
}
