/* == KOST-Simy =================================================================================
 * The KOST-Simy application is used for Compare Image-Files. Copyright (C) 2015-2016 Claire
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

package ch.kostceco.tools.kostsimy.logging;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import ch.kostceco.tools.kostsimy.service.TextResourceService;

public class LogConfigurator implements MessageConstants
{

	/** @author Rc Claire Röthlisberger, KOST-CECO */

	private static final ch.kostceco.tools.kostsimy.logging.Logger	LOGGER	= new ch.kostceco.tools.kostsimy.logging.Logger(
																																							LogConfigurator.class );

	private TextResourceService																			textResourceService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public String configure( String directoryOfLogfile, String nameOfLogfile )
	{

		String logFileName = directoryOfLogfile + File.separator + nameOfLogfile + ".kost-simy.log.xml";
		Logger rootLogger = Logger.getRootLogger();

		MessageOnlyLayout layout = new MessageOnlyLayout();
		try {
			FileAppender logfile = new FileAppender( layout, logFileName );
			logfile.setName( "logfile" );
			logfile.setAppend( false );
			logfile.activateOptions();

			rootLogger.addAppender( logfile );

		} catch ( IOException e ) {
			LOGGER.logError( getTextResourceService().getText( ERROR_IOE,
					getTextResourceService().getText( ERROR_LOGGING_NOFILEAPPENDER ) ) );
		}

		return logFileName;
	}

}
