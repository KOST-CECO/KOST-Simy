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

package ch.kostceco.tools.kostsimy.logging;

/** Interface für den Zugriff auf Resourcen aus dem ResourceBundle.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */
public interface MessageConstants
{

	// Initialisierung und Parameter-Ueberpruefung
	String	ERROR_IOE															= "error.ioe";
	String	ERROR_PARAMETER_USAGE									= "error.parameter.usage";
	String	ERROR_LOGDIRECTORY_NODIRECTORY				= "error.logdirectory.nodirectory";
	String	ERROR_LOGDIRECTORY_NOTWRITABLE				= "error.logdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_NOTDELETABLE			= "error.workdirectory.notdeletable";
	String	ERROR_WORKDIRECTORY_NOTWRITABLE				= "error.workdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_EXISTS						= "error.workdirectory.exists";
	String	ERROR_LOGGING_NOFILEAPPENDER					= "error.logging.nofileappender";
	String	ERROR_INCORRECTFILEENDING							= "error.incorrectfileending";
	String	ERROR_INCORRECTFILEENDINGS						= "error.incorrectfileendings";
	String	ERROR_NOREP														= "error.norep";
	String	ERROR_NOORIGDIR												= "error.noorigdir";
	String	ERROR_NOREPDIR1												= "error.norepdir1";
	String	ERROR_NOREPDIR2												= "error.norepdir2";
	String	ERROR_NOREPDIR3												= "error.norepdir3";
	String	ERROR_WRONG_JRE												= "error.wrong.jdk";
	String	ERROR_SPECIAL_CHARACTER								= "error.special.character";

	// Globale Meldungen
	String	MESSAGE_XML_HEADER										= "message.xml.header";
	String	MESSAGE_XML_START											= "message.xml.start";
	String	MESSAGE_XML_END												= "message.xml.end";
	String	MESSAGE_XML_INFO											= "message.xml.info";
	String	MESSAGE_COMPARISON										= "message.comparison";
	String	MESSAGE_XML_COMPFILES									= "message.compfiles";
	String	MESSAGE_XML_COMPFILE									= "message.compfile";
	String	MESSAGE_XML_VALERGEBNIS								= "message.xml.valergebnis";
	String	MESSAGE_XML_VALTYPE										= "message.xml.valtype";
	String	MESSAGE_XML_STATISTICS								= "message.xml.statistics";
	
	String	MESSAGE_XML_IMAGE1										= "message.xml.image1";
	String	MESSAGE_XML_IMAGE2										= "message.xml.image2";
	String	MESSAGE_XML_LOGEND										= "message.xml.logend";
	String	MESSAGE_XML_VALERGEBNIS_VALID					= "message.xml.valergebnis.valid";
	String	MESSAGE_XML_VALERGEBNIS_INVALID				= "message.xml.valergebnis.invalid";
	String	MESSAGE_XML_VALERGEBNIS_NOTVALIDATED	= "message.xml.valergebnis.notvalidated";
	String	MESSAGE_XML_VALERGEBNIS_CLOSE					= "message.xml.valergebnis.close";

	String	MESSAGE_XML_MODUL_PDF_EXTRACT					= "message.xml.modul.pdf.extract";
	String	MESSAGE_XML_MODUL_CI									= "message.xml.modul.ci";

	String	MESSAGE_XML_CONFIGURATION_ERROR_1			= "message.xml.configuration.error.1";
	String	MESSAGE_XML_CONFIGURATION_ERROR_2			= "message.xml.configuration.error.2";
	String	MESSAGE_XML_CONFIGURATION_ERROR_3			= "message.xml.configuration.error.3";

	String	ERROR_XML_UNKNOWN											= "error.xml.unknown";

	// *************Modul-Meldungen*************************************************************************
	// Modul PDF Extract
	String	ERROR_XML_PDFA_JPEG										= "error.xml.pdfa.jpeg";
	String	ERROR_XML_PDFA_JP2										= "error.xml.pdfa.jp2";
	String	ERROR_XML_PDFA_JPEGS									= "error.xml.pdfa.jpegs";
	String	ERROR_XML_PDFA_JP2S										= "error.xml.pdfa.jp2s";
	String	ERROR_XML_PDFA_JPEGJP2								= "error.xml.pdfa.jpegjp2";
	String	ERROR_XML_PDFA_JBIG2									= "error.xml.pdfa.jbig2";
	String	ERROR_XML_PDFA_CCITT									= "error.xml.pdfa.ccitt";

	// Modul IMGCMP
	String	ERROR_XML_IMCMP_MISSING								= "error.xml.imcmp.missing";
	String	ERROR_XML_IMCMP_SERVICEFAILED					= "error.xml.imcmp.servicefailed";
	String	ERROR_XML_IMCMP_NOREPORT							= "error.xml.imcmp.noreport";
	String	ERROR_XML_IMCMP_NOALL									= "error.xml.imcmp.noall";
	String	ERROR_XML_CI_CIINVALID								= "error.xml.ci.ciinvalid";
	String	ERROR_XML_CI_CIINVALIDSTR							= "error.xml.ci.ciinvalidstr";
	String	ERROR_XML_CI_SIZEINVALID							= "error.xml.ci.sizeinvalid";
	String	ERROR_XML_CI_PIXELINVALID							= "error.xml.ci.pixelinvalid";
}
