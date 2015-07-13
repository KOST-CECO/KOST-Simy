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

package ch.kostceco.tools.kostsimy.service.impl;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import ch.kostceco.tools.kostsimy.KOSTSimy;
import ch.kostceco.tools.kostsimy.logging.Logger;
import ch.kostceco.tools.kostsimy.service.ConfigurationService;
import ch.kostceco.tools.kostsimy.service.TextResourceService;

public class ConfigurationServiceImpl implements ConfigurationService
{

	private static final Logger	LOGGER	= new Logger( ConfigurationServiceImpl.class );
	XMLConfiguration						config	= null;
	private TextResourceService	textResourceService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	private XMLConfiguration getConfig()
	{
		if ( this.config == null ) {

			try {

				String path = "configuration/kostsimy.conf.xml";

				URL locationOfJar = KOSTSimy.class.getProtectionDomain().getCodeSource().getLocation();
				String locationOfJarPath = locationOfJar.getPath();

				if ( locationOfJarPath.endsWith( ".jar" ) ) {
					File file = new File( locationOfJarPath );
					String fileParent = file.getParent();
					path = fileParent + "/" + path;
				}

				config = new XMLConfiguration( path );

			} catch ( ConfigurationException e ) {
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_1 ) );
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_2 ) );
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_3 ) );
				System.exit( 1 );
			}
		}
		return config;
	}

	@Override
	public String getPathToWorkDir()
	{
		/** Gibt den Pfad des Arbeitsverzeichnisses zurück. Dieses Verzeichnis wird zum Extrahieren des
		 * jpeg-Files aus pdf verwendet.
		 * 
		 * @return Pfad des Arbeitsverzeichnisses */
		Object prop = getConfig().getProperty( "pathtoworkdir" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getPathToLogfile()
	{
		/** Gibt den Pfad des Logverzeichnisses zurück.
		 * 
		 * @return Pfad des Logverzeichnisses */
		Object prop = getConfig().getProperty( "pathtologfile" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getRandomTest()
	{
	/** Gibt die prozentuale Stichprobe zurück.
	 * 
	 * @return Stichprobe in Prozent */
	Object prop = getConfig().getProperty( "randomtest" );
	if ( prop instanceof String ) {
		String value = (String) prop;
		return value;
	}
	return null;
}

	@Override
	public String getPathToImgcmpExe()
	{
		/** Gibt den Pfad des IMGCMP-Verzeichnisses zurück.
		 * 
		 * @return Pfad des IMGCMP-Verzeichnisses */
		Object prop = getConfig().getProperty( "pathtoimgcmpexe" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String imgcmpLicenseKey()
	{
		/** Lizenz-Schlüssel zu ImageCompareCommandLine (IMGCMP)
		 * 
		 * @return Lizenz-Schlüssel */
		Object prop = getConfig().getProperty( "imgcmplicensekey" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String imgcmpTolerance()
	{
		/** Toleranz beim Bildervergleich small = 5% diff pro Pixel medium = 10% diff pro Pixel large =
		 * 20% diff pro Pixel
		 * 
		 * @return toleranz-text */
		Object prop = getConfig().getProperty( "imgcmptolerance" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

}
