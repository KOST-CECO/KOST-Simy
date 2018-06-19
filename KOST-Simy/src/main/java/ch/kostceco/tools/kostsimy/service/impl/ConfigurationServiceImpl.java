/* == KOST-Simy =================================================================================
 * The KOST-Simy application is used for Compare Image-Files. Copyright (C) 2015-2018 Claire
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

package ch.kostceco.tools.kostsimy.service.impl;

import java.io.File;

import org.apache.commons.configuration.XMLConfiguration;

import ch.kostceco.tools.kostsimy.service.ConfigurationService;
import ch.kostceco.tools.kostsimy.service.TextResourceService;

public class ConfigurationServiceImpl implements ConfigurationService
{

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

	@Override
	public String getPathToWorkDir()
	{
		/** Gibt den Pfad des Arbeitsverzeichnisses zurück. Dieses Verzeichnis wird temporaere Bilder
		 * verwendet.
		 * 
		 * @return Pfad des Arbeitsverzeichnisses = USERPROFILE/.kost-simy/temp_KOST-Simy */
		String pathtoworkdir = System.getenv( "USERPROFILE" ) + File.separator + ".kost-simy"
				+ File.separator + "temp_KOST-Simy";
		File dir = new File( pathtoworkdir );
		if ( !dir.exists() ) {
			dir.mkdirs();
		}
		return pathtoworkdir;
	}

	@Override
	public String getPathToLogfile()
	{
		/** Gibt den Pfad des Logverzeichnisses zurück.
		 * 
		 * @return Pfad des Logverzeichnisses = USERPROFILE/.kost-simy/logs */
		String logs = System.getenv( "USERPROFILE" ) + File.separator + ".kost-simy" + File.separator
				+ "logs";
		File dir = new File( logs );
		if ( !dir.exists() ) {
			dir.mkdirs();
		}
		return logs;
	}

}
