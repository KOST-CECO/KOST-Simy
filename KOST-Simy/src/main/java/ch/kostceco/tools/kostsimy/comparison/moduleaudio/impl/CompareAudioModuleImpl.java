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

package ch.kostceco.tools.kostsimy.comparison.moduleaudio.impl;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;

import ch.kostceco.tools.kostsimy.comparison.ComparisonModuleImpl;
import ch.kostceco.tools.kostsimy.comparison.moduleaudio.CompareAudioModule;
import ch.kostceco.tools.kostsimy.exception.moduleaudio.CompareAudioException;
import ch.kostceco.tools.kostsimy.service.ConfigurationService;
import ch.kostceco.tools.kostsimy.util.StreamGobbler;
import ch.kostceco.tools.kostsimy.util.Util;

/** Vergleicht die beiden Audiodateien (anhand Spectrum von ffmpeg) mit ImageMagick Compare und
 * wertet das Resultat aus
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class CompareAudioModuleImpl extends ComparisonModuleImpl implements CompareAudioModule
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
	public boolean validate( File origDatei, File repDatei, File directoryOfLogfile,
			String imToleranceTxt ) throws CompareAudioException
	{
		boolean isValid = true;
		boolean isValidDuration = true;
		boolean isValidMeta = true;
		boolean isMetaOrig = true;
		boolean isMetaRep = true;
		// boolean isValidFailed = false;
		boolean compResult = false;
		int allInt = 0;
		boolean allNoInt = false;
		String allStr = "";

		String imTolerance = "5%";
		float percentageInvalid = 99.9999f;
		float percentageDurationInvalid = 99.9999f;

		/* Initialisierung ffmpeg -> überprüfen der Angaben: existiert die Dateien am vorgegebenen Ort? */
		String ffmpegExe = "ffmpeg.exe";
		String ffprobeExe = "ffprobe.exe";
		String ffmpeg = "resources" + File.separator + "ffmpeg-4.0-win32-static";
		boolean ffmpegExist = true;
		File fFfmpegExe = new File( ffmpeg + File.separator + "bin" + File.separator + ffmpegExe );
		File fFfprobeExe = new File( ffmpeg + File.separator + "bin" + File.separator + ffprobeExe );
		if ( !fFfmpegExe.exists() ) {
			// fFfmpegExe existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
							+ getTextResourceService().getText( ERROR_XML_FFMPEG_MISSING, ffmpegExe ) );
			ffmpegExist = false;
		}
		if ( !fFfprobeExe.exists() ) {
			// fFfprobeExe existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
							+ getTextResourceService().getText( ERROR_XML_FFMPEG_MISSING, fFfprobeExe ) );
			ffmpegExist = false;
		}
		if ( !ffmpegExist ) {
			// Bestandteile von ffmpeg fehlen: Abbruch
			return false;
		}

		String pathToFfmpegExe = fFfmpegExe.getAbsolutePath();
		String pathToFfprobeExe = fFfprobeExe.getAbsolutePath();

		/* Initialisierung ImageMagick -> überprüfen der Angaben: existiert die compare.exe,
		 * msvcp120.dll, msvcr120.dll, vcomp120.dll am vorgegebenen Ort? */
		String compareExe = "compare.exe";
		String msvcp120Dll = "msvcp120.dll";
		String msvcr120Dll = "msvcr120.dll";
		String vcomp120Dll = "vcomp120.dll";
		String im = "resources" + File.separator + "ImageMagickCompare-6.9.1-Q16";
		boolean imExist = true;
		File fCompareExe = new File( im + File.separator + compareExe );
		if ( !fCompareExe.exists() ) {
			// Compare.exe von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, compareExe ) );
			imExist = false;
		}
		File fMsvcp120Dll = new File( im + File.separator + msvcp120Dll );
		if ( !fMsvcp120Dll.exists() ) {
			// msvcp120.dll von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, msvcp120Dll ) );
			imExist = false;
		}
		File fMsvcr120Dll = new File( im + File.separator + msvcr120Dll );
		if ( !fMsvcr120Dll.exists() ) {
			// msvcr120.dll von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, msvcr120Dll ) );
			imExist = false;
		}
		File fVcomp120Dll = new File( im + File.separator + vcomp120Dll );
		if ( !fVcomp120Dll.exists() ) {
			// vcomp120.dll von ImageMagick existiert nicht, kein Vergleich --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, vcomp120Dll ) );
			imExist = false;
		}
		if ( !imExist ) {
			// compare.exe/msvcp120.dll/msvcr120.dll/vcomp120.dll von ImageMagick existiert nicht -->
			// Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_CI )
							+ getTextResourceService().getText( ERROR_XML_IMCMP_MISSING, vcomp120Dll ) );
			return false;
		}

		if ( imToleranceTxt.contains( "N" ) || imToleranceTxt.contains( "n" ) ) {
			// null = 0%
			imTolerance = "0%";
			percentageInvalid = (float) 100.000000000;
			percentageDurationInvalid = (float) 100.000000000;
			imToleranceTxt = "N";
		} else if ( imToleranceTxt.contains( "S" ) || imToleranceTxt.contains( "s" ) ) {
			// small = 2%
			imTolerance = "2%";
			percentageInvalid = (float) 99.9999;
			percentageDurationInvalid = (float) 99.95;
			imToleranceTxt = "S";
		} else if ( imToleranceTxt.contains( "M" ) || imToleranceTxt.contains( "m" ) ) {
			// medium = 5%
			imTolerance = "5%";
			imToleranceTxt = "M";
			percentageInvalid = (float) 99.999;
			percentageDurationInvalid = (float) 99.90;
		} else if ( imToleranceTxt.contains( "XL" ) || imToleranceTxt.contains( "xl" ) ) {
			// xlarge = 15%
			imTolerance = "15%";
			percentageInvalid = (float) 99.9;
			percentageDurationInvalid = (float) 99.80;
			imToleranceTxt = "XL";
		} else if ( imToleranceTxt.contains( "L" ) || imToleranceTxt.contains( "l" ) ) {
			// large = 10%
			imTolerance = "10%";
			percentageInvalid = (float) 99.99;
			percentageDurationInvalid = (float) 99.85;
			imToleranceTxt = "L";
		} else {
			// null = 0%
			imTolerance = "0%";
			percentageInvalid = (float) 100.000000000;
			percentageDurationInvalid = (float) 100.000000000;
			imToleranceTxt = "N";
		}
		String pathToCompareExe = fCompareExe.getAbsolutePath();

		File report;
		File reportId;
		String imgPx1 = "1";

		StringBuffer concatenatedOutputs = new StringBuffer();

		File fDestSpectroOrig = null;
		File fDestSpectroRep = null;
		File fTxtOrig = null;
		File fTxtRep = null;
		File fMetaOrig = null;
		File fMetaRep = null;

		// TODO: Marker: Vorbereitungen zum Vergleich der Audiodateien
		Process procFfmpeg = null;
		Runtime rtFfmpeg = null;
		Process procFfprobe = null;
		Runtime rtFfprobe = null;
		try {
			// Informationen zum Arbeitsverzeichnis holen
			String pathToWorkDir = getConfigurationService().getPathToWorkDir();
			/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property name="configurationService"
			 * ref="configurationService" /> */
			File workDir = new File( pathToWorkDir );
			File tmpDirOrig = new File( pathToWorkDir + File.separator + "orig" );
			File tmpDirRep = new File( pathToWorkDir + File.separator + "rep" );

			// bestehendes Workverzeichnis ggf. löschen
			if ( tmpDirOrig.exists() ) {
				Util.deleteDir( tmpDirOrig );
				tmpDirOrig.mkdirs();
			}
			if ( tmpDirRep.exists() ) {
				Util.deleteDir( tmpDirRep );
				tmpDirRep.mkdirs();
			}

			String srcSpectroOrig = origDatei.getAbsolutePath();
			String destSpectroOrig = workDir.getAbsolutePath() + File.separator + "orig" + File.separator
					+ origDatei.getName() + ".png";
			String srcSpectroRep = repDatei.getAbsolutePath();
			String destSpectroRep = workDir.getAbsolutePath() + File.separator + "rep" + File.separator
					+ repDatei.getName() + ".png";
			fDestSpectroOrig = new File( destSpectroOrig );
			fDestSpectroRep = new File( destSpectroRep );

			String txtOrig = workDir.getAbsolutePath() + File.separator + "orig" + File.separator
					+ "Orig.txt";
			String txtRep = workDir.getAbsolutePath() + File.separator + "rep" + File.separator
					+ "Rep.txt";
			fTxtOrig = new File( txtOrig );
			fTxtRep = new File( txtRep );

			String metaOrig = workDir.getAbsolutePath() + File.separator + "orig" + File.separator
					+ "MetaOrig.txt";
			String metaRep = workDir.getAbsolutePath() + File.separator + "rep" + File.separator
					+ "MetaRep.txt";
			fMetaOrig = new File( metaOrig );
			fMetaRep = new File( metaRep );

			// TODO: Marker: Längen zum Vergleich der Audiodateien
			/* Duration in Sekunden
			 * 
			 * ffprobe -i <file> -show_entries format=duration -v quiet -of csv="p=0"
			 * 
			 * > Vergleichbar je nach Konfiguration (N100, S99.95, M99.9, L99.85 XL99.8) */
			String durationOrig = "1";
			String durationRep = "2";

			String commandTimeOrig = "cmd /c \"\"" + pathToFfprobeExe + "\" -i \""
					+ origDatei.getAbsolutePath()
					+ "\" -show_entries format=duration -v quiet -of csv=\"p=0\" >\"" + fTxtOrig + "\"";
			String commandTimeRep = "cmd /c \"\"" + pathToFfprobeExe + "\" -i \""
					+ repDatei.getAbsolutePath()
					+ "\" -show_entries format=duration -v quiet -of csv=\"p=0\" >\"" + fTxtRep + "\"";
			/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
			 * gehts: cmd /c\"urspruenlicher Befehl\" */

			Util.switchOffConsole();
			rtFfprobe = Runtime.getRuntime();
			procFfprobe = rtFfprobe.exec( commandTimeOrig.toString().split( " " ) );
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

			// Fehleroutput holen
			StreamGobbler errorGobblerOrigTime = new StreamGobbler( procFfprobe.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobblerOrigTime = new StreamGobbler( procFfprobe.getInputStream(),
					"OUTPUT" );

			// Threads starten
			errorGobblerOrigTime.start();
			outputGobblerOrigTime.start();

			// Warte, bis wget fertig ist
			procFfprobe.waitFor();

			rtFfprobe = Runtime.getRuntime();
			procFfprobe = rtFfprobe.exec( commandTimeRep.toString().split( " " ) );
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

			// Fehleroutput holen
			StreamGobbler errorGobblerRepTime = new StreamGobbler( procFfprobe.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobblerRepTime = new StreamGobbler( procFfprobe.getInputStream(),
					"OUTPUT" );

			// Threads starten
			errorGobblerRepTime.start();
			outputGobblerRepTime.start();

			// Warte, bis wget fertig ist
			procFfprobe.waitFor();

			Util.switchOnConsole();
			durationOrig = FileUtils.readFileToString( fTxtOrig );
			durationRep = FileUtils.readFileToString( fTxtRep );
			if ( !durationOrig.equals( durationRep ) ) {

				/* Dauer mit einer Abweichung (Int): Prozent ermitteln und mit percentageInvalid abgleichen */
				double z1 = 0;
				double z2 = 0;
				float percentageCalc = (float) 0.0;
				float percentageCalcInv = (float) 0.0;

				z2 = Double.parseDouble( durationRep );
				z1 = Double.parseDouble( durationOrig );
				if ( z2 < z1 ) {
					z2 = z1;
					z1 = Double.parseDouble( durationRep );
				}

				percentageCalc = (float) (100 - (100 / z2 * z1));
				percentageCalcInv = 100 - percentageCalc;

				// Prozentzahlen vergleichen
				if ( percentageDurationInvalid > percentageCalcInv ) {
					// Laenge mit einer groesseren Abweichung
					isValidDuration = false;

					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_DURATION )
									+ getTextResourceService().getText( ERROR_XML_FFMPEG_DURATION, durationOrig,
											durationRep, imToleranceTxt, percentageCalcInv ) );
				}
			}

			// TODO: metadaten ffmpeg -i in.mp4 -f ffmetadata in.txt
			// > N: identisch
			// > S: TITLE, ARTIST, COPYRIGHT, ALBUM, COMPOSER, DATE, GENRE
			// > M: TITLE, ARTIST, COPYRIGHT, ALBUM
			// > L: TITLE, ARTIST, COPYRIGHT
			// > XL: TITLE
			String commandMetaOrig = "\"" + pathToFfmpegExe + "\" -i \"" + srcSpectroOrig
					+ "\" -f ffmetadata \"" + fMetaOrig.getAbsolutePath() + "\"";
			String commandMetaRep = "cmd /c \"\"" + pathToFfmpegExe + "\" -i \"" + srcSpectroRep
					+ "\" -f ffmetadata \"" + fMetaRep.getAbsolutePath() + "\"";
			/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
			 * gehts: cmd /c\"urspruenlicher Befehl\" */

			Util.switchOffConsole();
			rtFfmpeg = Runtime.getRuntime();
			procFfmpeg = rtFfmpeg.exec( commandMetaOrig.toString().split( " " ) );
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

			// Fehleroutput holen
			StreamGobbler errorGobblerOrig = new StreamGobbler( procFfmpeg.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobblerOrig = new StreamGobbler( procFfmpeg.getInputStream(), "OUTPUT" );

			// Threads starten
			errorGobblerOrig.start();
			outputGobblerOrig.start();

			// Warte, bis wget fertig ist
			procFfmpeg.waitFor();

			rtFfmpeg = Runtime.getRuntime();
			procFfmpeg = rtFfmpeg.exec( commandMetaRep.toString().split( " " ) );
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

			// Fehleroutput holen
			StreamGobbler errorGobblerMRep = new StreamGobbler( procFfmpeg.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobblerMRep = new StreamGobbler( procFfmpeg.getInputStream(), "OUTPUT" );

			// Threads starten
			errorGobblerMRep.start();
			outputGobblerMRep.start();

			// Warte, bis wget fertig ist
			procFfmpeg.waitFor();

			Util.switchOnConsole();

			// Kontrolle ob die beiden Metadaten existieren
			if ( !fMetaOrig.exists() ) {
				isMetaOrig = false;
			}
			if ( !fMetaRep.exists() ) {
				isMetaRep = false;
			}

			if ( !isMetaOrig && !isMetaRep ) {
				// bei beiden hat es anscheinend keine lesbaren metadaten => identisch
				isValidMeta = true;
			} else {
				// Auswertung der Metadaten
				String sMetaOrig = FileUtils.readFileToString( fMetaOrig );
				String sMetaRep = FileUtils.readFileToString( fMetaRep );
				if ( sMetaOrig.equals( sMetaRep ) ) {
					// Metadaten identisch
					isValidMeta = true;
				} else {
					// Metadaten nicht identisch
					if ( imToleranceTxt.equals( "N" ) ) {
						// bei N müssen sie identisch sein
						isValidMeta = false;
						String diffOrig = "<Message> -> " + sMetaOrig + "</Message>";
						String diffRep = "<Message> !=  " + sMetaRep + "</Message>";

						getMessageService()
								.logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_META )
												+ getTextResourceService().getText( ERROR_XML_FFMPEG_META,
														diffOrig + diffRep ) );
					} else {
						String title = " ";
						String artist = " ";
						String copy = " ";
						String album = " ";
						String composer = " ";
						String date = " ";
						String genre = " ";
						String titleOrig = "_";
						String artistOrig = "_";
						String copyOrig = "_";
						String albumOrig = "_";
						String composerOrig = "_";
						String dateOrig = "_";
						String genreOrig = "_";
						String titleRep = "_";
						String artistRep = "_";
						String copyRep = "_";
						String albumRep = "_";
						String composerRep = "_";
						String dateRep = "_";
						String genreRep = "_";
						/* TODO: start Metadaten einzeln herauslesen titleOrig, artistOrig, copyOrig, albumOrig,
						 * composerOrig, dateOrig, genreOrig */

						BufferedReader in = new BufferedReader( new FileReader( fMetaOrig ) );
						String line;
						while ( (line = in.readLine()) != null ) {
							// Linien als String speichern, welche
							// TITLE, ARTIST, COPYRIGHT, ALBUM, COMPOSER, DATE, GENRE enthalten
							if ( line.contains( "TITLE=" ) || line.contains( "title=" ) ) {
								titleOrig = line.substring( 6 );
							} else if ( line.contains( "ARTIST=" ) || line.contains( "artist=" ) ) {
								if ( line.contains( "_ARTIST=" ) || line.contains( "_artist=" ) ) {
								} else {
									artistOrig = line.substring( 7 );
								}
							} else if ( line.contains( "COPYRIGHT=" ) || line.contains( "copyright=" ) ) {
								copyOrig = line.substring( 10 );
							} else if ( line.contains( "ALBUM=" ) || line.contains( "album=" ) ) {
								albumOrig = line.substring( 6 );
							} else if ( line.contains( "COMPOSER=" ) || line.contains( "composer=" ) ) {
								composerOrig = line.substring( 9 );
							} else if ( line.contains( "DATE=" ) || line.contains( "date=" ) ) {
								dateOrig = line.substring( 5 );
							} else if ( line.contains( "GENRE=" ) || line.contains( "genre=" ) ) {
								genreOrig = line.substring( 6 );
							}
						}
						in.close();
						in = new BufferedReader( new FileReader( fMetaRep ) );
						while ( (line = in.readLine()) != null ) {
							// Linien als String speichern, welche
							// TITLE, ARTIST, COPYRIGHT, ALBUM, COMPOSER, DATE, GENRE enthalten
							if ( line.contains( "TITLE=" ) || line.contains( "title=" ) ) {
								titleRep = line.substring( 6 );
							} else if ( line.contains( "ARTIST=" ) || line.contains( "artist=" ) ) {
								if ( line.contains( "_ARTIST=" ) || line.contains( "_artist=" ) ) {
								} else {
									artistRep = line.substring( 7 );
								}
							} else if ( line.contains( "COPYRIGHT=" ) || line.contains( "copyright=" ) ) {
								copyRep = line.substring( 10 );
							} else if ( line.contains( "ALBUM=" ) || line.contains( "album=" ) ) {
								albumRep = line.substring( 6 );
							} else if ( line.contains( "COMPOSER=" ) || line.contains( "composer=" ) ) {
								composerRep = line.substring( 9 );
							} else if ( line.contains( "DATE=" ) || line.contains( "date=" ) ) {
								dateRep = line.substring( 5 );
							} else if ( line.contains( "GENRE=" ) || line.contains( "genre=" ) ) {
								genreRep = line.substring( 6 );
							}
						}
						in.close();
						// Thread.sleep( 20000 );

						// > XL: TITLE
						if ( !titleOrig.equals( titleRep ) ) {
							isValidMeta = false;
							title = "<Message> -> TITLE: " + titleOrig + " != " + titleRep + "</Message>";
						}
						// > L: TITLE, ARTIST, COPYRIGHT
						if ( imToleranceTxt.equals( "M" ) || imToleranceTxt.equals( "M" )
								|| imToleranceTxt.equals( "S" ) ) {
							if ( !artistOrig.equals( artistRep ) ) {
								isValidMeta = false;
								artist = "<Message> -> ARTIST: " + artistOrig + " != " + artistRep + "</Message>";
							}
							if ( !copyOrig.equals( copyRep ) ) {
								isValidMeta = false;
								copy = "<Message> -> COPYRIGHT: " + copyOrig + " != " + copyRep + "</Message>";
							}
						}
						// > M: TITLE, ARTIST, COPYRIGHT, ALBUM
						if ( imToleranceTxt.equals( "M" ) || imToleranceTxt.equals( "S" ) ) {
							if ( !albumOrig.equals( albumRep ) ) {
								isValidMeta = false;
								album = "<Message> -> ALBUM: " + albumOrig + " != " + albumRep + "</Message>";
							}
						}
						// > S: TITLE, ARTIST, COPYRIGHT, ALBUM, COMPOSER, DATE, GENRE
						if ( imToleranceTxt.equals( "S" ) ) {
							if ( !composerOrig.equals( composerRep ) ) {
								isValidMeta = false;
								composer = "<Message> -> COMPOSER: " + composerOrig + " != " + composerRep
										+ "</Message>";
							}
							if ( !dateOrig.equals( dateRep ) ) {
								isValidMeta = false;
								date = "<Message> -> DATE: " + dateOrig + " != " + dateRep + "</Message>";
							}
							if ( !genreOrig.equals( genreRep ) ) {
								isValidMeta = false;
								genre = "<Message> -> GENRE: " + genreOrig + " != " + genreRep + "</Message>";
							}
						}
						if ( !isValidMeta ) {
							// title, artist, copy, album, composer, date, genre
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_META )
											+ getTextResourceService().getText( ERROR_XML_FFMPEG_META,
													title + artist + copy + album + composer + date + genre ) );
						}
					}
				}
			}

			// TODO: Marker: Spectrum erstellen der beiden Audiodateien
			/* ffmpeg -i audio.flac -lavfi showspectrumpic=s=2048x1024 spectrogram.png
			 * 
			 * ffmpeg -i "srcSpectroOrig" -lavfi showspectrumpic=s=2048x1024 "destSpectroOrig"
			 * 
			 * ffmpeg -i "srcSpectroRep" -lavfi showspectrumpic=s=2048x1024 "destSpectroRep" */

			String commandOrig = "\"" + pathToFfmpegExe + "\" -i \"" + srcSpectroOrig
					+ "\" -lavfi showspectrumpic=s=2048x1024 \"" + destSpectroOrig + "\"";
			String commandRep = "cmd /c \"\"" + pathToFfmpegExe + "\" -i \"" + srcSpectroRep
					+ "\" -lavfi showspectrumpic=s=2048x1024 \"" + destSpectroRep + "\"";
			/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
			 * gehts: cmd /c\"urspruenlicher Befehl\" */

			Util.switchOffConsole();
			rtFfmpeg = Runtime.getRuntime();
			procFfmpeg = rtFfmpeg.exec( commandOrig.toString().split( " " ) );
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

			// Fehleroutput holen
			StreamGobbler errorGobblerSOrig = new StreamGobbler( procFfmpeg.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobblerSOrig = new StreamGobbler( procFfmpeg.getInputStream(), "OUTPUT" );

			// Threads starten
			errorGobblerSOrig.start();
			outputGobblerSOrig.start();

			// Warte, bis wget fertig ist
			procFfmpeg.waitFor();

			rtFfmpeg = Runtime.getRuntime();
			procFfmpeg = rtFfmpeg.exec( commandRep.toString().split( " " ) );
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

			// Fehleroutput holen
			StreamGobbler errorGobblerRep = new StreamGobbler( procFfmpeg.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobblerRep = new StreamGobbler( procFfmpeg.getInputStream(), "OUTPUT" );

			// Threads starten
			errorGobblerRep.start();
			outputGobblerRep.start();

			// Warte, bis wget fertig ist
			procFfmpeg.waitFor();

			Util.switchOnConsole();

			// Kontrolle ob die beiden Spectrum existieren
			if ( !fDestSpectroOrig.exists() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
								+ getTextResourceService().getText( ERROR_XML_FFMPEG_NOREPORT,
										destSpectroOrig + " (FFMPEG)" ) );
				if ( !fDestSpectroRep.exists() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
									+ getTextResourceService().getText( ERROR_XML_FFMPEG_NOREPORT,
											destSpectroRep + " (FFMPEG)" ) );
					return false;
				}
				return false;
			}
			if ( !fDestSpectroRep.exists() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
								+ getTextResourceService().getText( ERROR_XML_FFMPEG_NOREPORT,
										destSpectroRep + " (FFMPEG)" ) );
				return false;
			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
							+ getTextResourceService().getText( ERROR_XML_FFMPEG_SERVICEFAILED, e.getMessage() ) );
			return false;
		} finally {
			if ( procFfmpeg != null ) {
				closeQuietly( procFfmpeg.getOutputStream() );
				closeQuietly( procFfmpeg.getInputStream() );
				closeQuietly( procFfmpeg.getErrorStream() );
			}
		}

		// TODO: Marker: Bildervergleichen
		try {

			String pathToOutput = directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_compare_report.txt";
			String pathToOutputId = directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_identify_report.txt";
			String pathToMask = directoryOfLogfile.getAbsolutePath() + File.separator
					+ origDatei.getName() + "_mask.jpg";

			/* compare -fuzz 15% -metric AE -quiet -identify -verbose -highlight-color DarkRed Image_1.jpg
			 * Image_2.jpg mask.jpg >>results_id.txt 2>results.txt */

			String command = "cmd /c \"\"" + pathToCompareExe + "\" -fuzz " + imTolerance
					+ " -metric AE -quiet -identify -verbose -highlight-color DarkRed \""
					+ fDestSpectroOrig.getAbsolutePath() + "\" \"" + fDestSpectroRep.getAbsolutePath()
					+ "\" \"" + pathToMask + "\" >>\"" + pathToOutputId + "\" 2>\"" + pathToOutput + "\"";
			/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
			 * gehts: cmd /c\"urspruenlicher Befehl\" */

			Process proc = null;
			Runtime rt = null;

			try {
				report = new File( pathToOutput );
				reportId = new File( pathToOutputId );
				File mask = new File( pathToMask );
				// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, löschen
				// wir es
				if ( report.exists() ) {
					report.delete();
				}
				if ( reportId.exists() ) {
					reportId.delete();
				}
				if ( mask.exists() ) {
					mask.delete();
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
				// Kontrolle ob die Reports existieren
				if ( !report.exists() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
											report.getAbsolutePath() ) );
					if ( !reportId.exists() ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
										+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
												reportId.getAbsolutePath() ) );
						return false;
					}
					return false;
				}
				if ( !reportId.exists() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
											reportId.getAbsolutePath() ) );
					return false;
				}
			} catch ( Exception e ) {
				getMessageService()
						.logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
										+ getTextResourceService().getText( ERROR_XML_IMCMP_SERVICEFAILED,
												e.getMessage() ) );
				return false;
			} finally {
				if ( proc != null ) {
					closeQuietly( proc.getOutputStream() );
					closeQuietly( proc.getInputStream() );
					closeQuietly( proc.getErrorStream() );
				}
			}
			// Kontrolle ob die Reports existieren
			if ( !report.exists() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
								+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
										report.getAbsolutePath() ) );
				if ( !reportId.exists() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
											reportId.getAbsolutePath() ) );
					return false;
				}
				return false;
			}
			if ( !reportId.exists() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
								+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORT,
										reportId.getAbsolutePath() ) );
				return false;
			}

			// Ende IMCMP direkt auszulösen

			// TODO: Marker: ReportId und auswerten (Grösse und der Pixel)
			try {
				BufferedReader in = new BufferedReader( new FileReader( reportId ) );
				String line;
				String imgSize1 = "1";
				String imgSize2 = "2";
				String imgPx2 = "2";

				while ( (line = in.readLine()) != null ) {

					concatenatedOutputs.append( line );
					concatenatedOutputs.append( NEWLINE );

					/* Format: TIFF (Tagged Image File Format) Mime type: image/tiff
					 * 
					 * Geometry: 2469x3568+0+0
					 * 
					 * Channel statistics:
					 * 
					 * Pixels: 8809392
					 * 
					 * Geometry und Pixels scheinen immer ausgegeben zu werden
					 * 
					 * Gemotry und Pixels müssen identisch sein */
					if ( line.contains( "  Geometry: " ) ) {
						if ( imgSize1.equals( "1" ) ) {
							imgSize1 = line;
						} else {
							imgSize2 = line;
						}
					} else if ( line.contains( "  Pixels: " ) ) {
						if ( imgPx1.equals( "1" ) ) {
							imgPx1 = line;
						} else {
							imgPx2 = line;
						}
					}

					// TODO: Marker: Auswertung und Fehlerausgabe wenn nicht bestanden.
				}
				if ( imgPx1.equals( "1" ) && imgPx2.equals( "2" ) && imgSize1.equals( "1" )
						&& imgSize2.equals( "2" ) ) {
					// identify_report ist leer oder enthält nicht das was er sollte
					isValid = false;
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_SPEC )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOREPORTTEXT ) );
					in.close();
					return false;
				}
				if ( !imgPx1.equals( imgPx2 ) ) {
					// die beiden Bilder haben nicht gleich viel Pixels
					isValid = false;
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_SPEC )
									+ getTextResourceService().getText( ERROR_XML_CI_PIXELINVALID, imgPx1, imgPx2 ) );
				}
				if ( !imgSize1.equals( imgSize2 ) ) {
					// die beiden Bilder sind nicht gleich gross
					isValid = false;
					getMessageService()
							.logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_SPEC )
											+ getTextResourceService().getText( ERROR_XML_CI_SIZEINVALID, imgSize1,
													imgSize2 ) );
				}
				if ( !isValid ) {
					// die beiden Bilder sind nicht gleich gross
					in.close();
					return false;
				}
				in.close();
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
										"identify: " + e.getMessage() ) );
				return false;
			}

			// TODO: Marker: Report auswerten (Bildvergleich) wenn grösse & PixelAnzahl identisch
			try {
				BufferedReader in = new BufferedReader( new FileReader( report ) );
				String line;
				boolean allNull = false;
				boolean allExist = false;

				while ( (line = in.readLine()) != null ) {

					concatenatedOutputs.append( line );
					concatenatedOutputs.append( NEWLINE );

					/* img1.tif[0] TIFF 2469x3568 2469x3568+0+0 8-bit Grayscale Gray 8.833MB 1.451u 0:01.774
					 * 
					 * img2.jp2[0] JP2 2469x3568 2469x3568+0+0 8-bit Gray 1.842MB 1.357u 0:01.378
					 * 
					 * all: 0
					 * 
					 * in den ersten zwei zeilen sind die eigenschaften der beiden Bilder enthalten
					 * 
					 * Danach die Anzahl Pixel mit einer grösseren Abweichung aus, allg: 0= vergleichbar */
					if ( line.contains( " all: " ) ) {
						allExist = true;
						if ( line.contains( " all: 0" ) ) {
							allNull = true;
						} else {
							/* Invalide Px extrahieren "    all: 3563" extrahieren */
							String lineReportAll = line.substring( 9 );
							try {
								// lineReport = 3563
								allInt = Integer.parseInt( lineReportAll );
							} catch ( Exception e ) {
								allNoInt = true;
								allStr = lineReportAll;
							}
						}
					}

					if ( allNull ) {
						compResult = true;
					}
				}
				in.close();
				if ( !allExist ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
									+ getTextResourceService().getText( ERROR_XML_IMCMP_NOALL ) );
					return false;
				}
				if ( !compResult ) {
					// Bildvergleich nicht bestanden
					if ( allNoInt ) {
						/* Bilder mit vielen Pixels die Abweichen (Potenz -> String): Vereinfachte Fehlerausgabe */

						/* Invalide [allStr] und total px z2 aus imgPx1 "    Pixels: 8809392" extrahieren */
						String lineReport = imgPx1.substring( 12 );
						// lineReport = 8809392

						isValid = false;

						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_SPEC )
										+ getTextResourceService().getText( ERROR_XML_CI_CIINVALIDSTR, lineReport,
												imToleranceTxt, allStr ) );

					} else {
						/* Bilder mit einer Abweichung (Int): Prozent ermitteln und mit percentageInvalid
						 * abgleichen */
						double z1 = 0;
						double z2 = 0;
						float percentageCalc = (float) 0.0;
						float percentageCalcInv = (float) 0.0;

						/* Invalide z1 [allInt] und total px z2 aus imgPx1 "    Pixels: 8809392" extrahieren */
						String lineReport = imgPx1.substring( 12 );
						// lineReport = 8809392
						z2 = Double.parseDouble( lineReport );
						z1 = allInt;

						percentageCalc = (float) (100 - (100 / z2 * z1));
						percentageCalcInv = 100 - percentageCalc;

						// Prozentzahlen vergleichen
						if ( percentageInvalid > percentageCalc ) {
							// Bilder mit einer grösseren Abweichung
							isValid = false;

							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO_SPEC )
											+ getTextResourceService().getText( ERROR_XML_CI_CIINVALID,
													percentageCalcInv, z2, imToleranceTxt, z1 ) );
						}
					}
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
								+ getTextResourceService()
										.getText( ERROR_XML_UNKNOWN, "compare: " + e.getMessage() ) );
				return false;
			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_AUDIO )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// reports löschen
		if ( report.exists() ) {
			report.delete();
		}
		if ( reportId.exists() ) {
			reportId.delete();
		}
		if ( isValid ) {
			if ( !isValidDuration ) {
				isValid = isValidDuration;
			} else if ( !isValidMeta ) {
				isValid = isValidMeta;
			}
		}
		return isValid;
	}
}
