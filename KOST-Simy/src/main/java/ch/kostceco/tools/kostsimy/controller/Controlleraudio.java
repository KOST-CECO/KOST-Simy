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

package ch.kostceco.tools.kostsimy.controller;

import java.io.File;

import ch.kostceco.tools.kostsimy.comparison.moduleaudio.CompareAudioModule;
import ch.kostceco.tools.kostsimy.exception.moduleaudio.CompareAudioException;
import ch.kostceco.tools.kostsimy.logging.Logger;
import ch.kostceco.tools.kostsimy.logging.MessageConstants;
import ch.kostceco.tools.kostsimy.service.TextResourceService;

/** kostsimy -->
 * 
 * Der Controller ruft die benötigten Module zum Audiovergleich in der benötigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controlleraudio implements MessageConstants
{

	private static final Logger	LOGGER	= new Logger( Controlleraudio.class );
	private TextResourceService	textResourceService;

	private CompareAudioModule	compareAudioModule;

	public CompareAudioModule getCompareAudioModule()
	{
		return compareAudioModule;
	}

	public void setCompareAudioModule( CompareAudioModule compareAudioModule )
	{
		this.compareAudioModule = compareAudioModule;
	}

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory( File origDatei, File repDatei, File directoryOfLogfile,
			String imToleranceTxt )
	{
		boolean valid = true;

		// Audiovergleich
		try {
			if ( this.getCompareAudioModule().validate( origDatei, repDatei, directoryOfLogfile,
					imToleranceTxt ) ) {
				this.getCompareAudioModule().getMessageService().print();
			} else {
				this.getCompareAudioModule().getMessageService().print();
				return false;
			}
		} catch ( CompareAudioException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getCompareAudioModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;

	}
}
