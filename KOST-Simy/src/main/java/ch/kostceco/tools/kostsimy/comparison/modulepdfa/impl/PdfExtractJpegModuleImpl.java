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

package ch.kostceco.tools.kostsimy.comparison.modulepdfa.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.kostceco.tools.kostsimy.comparison.ComparisonModuleImpl;
import ch.kostceco.tools.kostsimy.comparison.modulepdfa.PdfExtractJpegModule;
import ch.kostceco.tools.kostsimy.exception.modulepdfa.PdfExtractJpegException;
import ch.kostceco.tools.kostsimy.service.ConfigurationService;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/** Extrahiert mit iText das vorhandene JPEG aus dem PDF und speichert es im Workverzeichnis ab
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class PdfExtractJpegModuleImpl extends ComparisonModuleImpl implements PdfExtractJpegModule
{
	boolean												isJPEG				= false;
	boolean												isJPEGs				= false;
	boolean												isCCITT				= false;
	boolean												isJP2					= false;
	boolean												isJBIG2				= false;

	int														jpegCounter		= 0;
	int														ccittCounter	= 0;
	int														jp2Counter		= 0;
	int														jbig2Counter	= 0;
	private ConfigurationService	configurationService;

	public static String					NEWLINE				= System.getProperty( "line.separator" );

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
			throws PdfExtractJpegException
	{
		boolean valid = true;
		boolean validOrig = true;
		boolean validRep = true;

		// Informationen zum Arbeitsverzeichnis holen
		String pathToWorkDir = getConfigurationService().getPathToWorkDir();
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		File workDir = new File( pathToWorkDir );

		String srcPdfOrig = origDatei.getAbsolutePath();
		String destImageOrig = workDir.getAbsolutePath() + File.separator + origDatei.getName()
				+ "_orig.jpg";
		String srcPdfRep = repDatei.getAbsolutePath();
		String destImageRep = workDir.getAbsolutePath() + File.separator + repDatei.getName()
				+ "_rep.jpg";

		if ( srcPdfOrig.endsWith( "pdf" ) || srcPdfOrig.endsWith( "pdfa" ) ) {
			try {
				extractImages( srcPdfOrig, destImageOrig );
			} catch ( DocumentException e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				validOrig = false;
			} catch ( IOException e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				validOrig = false;
			}
		}
		if ( srcPdfRep.endsWith( "pdf" ) || srcPdfRep.endsWith( "pdfa" ) ) {
			try {
				extractImages( srcPdfRep, destImageRep );
			} catch ( DocumentException e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				validRep = false;
			} catch ( IOException e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				validRep = false;
			}
		}
		if ( validOrig && validRep ) {
			// keine Exception
			if ( isJPEGs ) {
				// mehrere JPEGs in PDF vorhanden
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_PDFA_JPEGS, jpegCounter ) );
				valid = false;
			}
			if ( isJP2 ) {
				// JP2 in PDF vorhanden
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_PDFA_JP2, jp2Counter ) );
				valid = false;
			}
			if ( isJBIG2 ) {
				// JBIG2 in PDF vorhanden
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_PDFA_JBIG2, jbig2Counter ) );
				valid = false;
			}
			if ( isCCITT ) {
				// CCITT in PDF vorhanden
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_PDFA_CCITT, ccittCounter ) );
				valid = false;
			}

		} else {
			valid = false;
		}

		return valid;
	}

	/** Parses a PDF and extracts all the images.
	 * 
	 * @param src
	 *          the source PDF
	 * @param dest
	 *          the resulting Image */
	public void extractImages( String srcPdf, String destImage ) throws IOException,
			DocumentException
	{
		try {
			PdfReader reader = new PdfReader( srcPdf );
			PdfReaderContentParser parser = new PdfReaderContentParser( reader );
			MyImageRenderListener listener = new MyImageRenderListener( destImage );
			for ( int i = 1; i <= reader.getNumberOfPages(); i++ ) {
				parser.processContent( i, listener );
			}
			reader.close();
		} catch ( IOException e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
	}

	public class MyImageRenderListener implements RenderListener
	{
		String	path	= "";

		/** Creates a RenderListener that will look for images. */
		public MyImageRenderListener( String path )
		{
			this.path = path;
		}

		public void beginTextBlock()
		{
		}

		public void endTextBlock()
		{
		}

		public void renderImage( ImageRenderInfo renderInfo )
		{
			try {
				String filenamePath = path;
				FileOutputStream os;
				PdfImageObject image = renderInfo.getImage();

				PdfName filter = null;
				try {
					PdfObject myObj = image.get( PdfName.FILTER );
					myObj = PdfReader.getPdfObject( myObj );
					if ( myObj instanceof PdfName ) {
						filter = (PdfName) myObj;
					}

					if ( PdfName.DCTDECODE.equals( filter ) ) {
						if ( jpegCounter == 0 ) {
							jpegCounter = jpegCounter + 1;
							/* JPEG Bild: Das JPEG wird im Logverzeichnis wie vorgängig definiert gespeichert */
							os = new FileOutputStream( filenamePath );
							os.write( image.getImageAsBytes() );
							os.flush();
							os.close();
							isJPEG = true;
						} else {
							// es wurde bereits ein JPEG extrahiert
							jpegCounter = jpegCounter + 1;
							isJPEGs = true;
						}
					} else if ( PdfName.JPXDECODE.equals( filter ) ) {
						// JP2-Bild
						jp2Counter = jp2Counter + 1;
						isJP2 = true;
					} else if ( PdfName.JBIG2DECODE.equals( filter ) ) {
						// JBIG2-Bild
						jbig2Counter = jbig2Counter + 1;
						isJBIG2 = true;
					} else if ( PdfName.CCITTFAXDECODE.equals( filter ) ) {
						/* Bild mit der CCITTFAX Komprimierung */
						ccittCounter = ccittCounter + 1;
						isCCITT = true;
					} else {
						/* kein JPEG, JP2 oder JBIG2. Es wird entsprechend keine Validierung gemacht. */
					}
				} catch ( IOException ioe ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, ioe.getMessage() ) );
				}
			} catch ( IOException e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_PDF_EXTRACT )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			}
		}

		@Override
		public void renderText( TextRenderInfo renderInfo )
		{
		}

	}
}
