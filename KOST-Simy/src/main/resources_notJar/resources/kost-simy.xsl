<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<head>
				<style>
					body {font-family: Verdana, Geneva, sans-serif; font-size:
					10pt; }
					table {font-family: Verdana, Geneva, sans-serif; font-size:
					10pt; }
					.logow {font-family: Verdana, Geneva, sans-serif;
					background-color: #ffffff; font-weight:bold; font-size: 32pt;
					color: #ffffff; }
					.logoff {font-family: Verdana, Geneva, sans-serif;
					background-color: #ffffff; font-weight:bold; font-size: 18pt;
					color: #000000; }
					.logo {font-family: Verdana, Geneva, sans-serif;
					background-color: #000000; font-weight:bold; font-size: 32pt;
					color: #ffffff; }
					.logov {font-family: Verdana, Geneva, sans-serif;
					background-color: #000000; font-weight:bold; font-size: 32pt;
					color: #0cc10c; }
					.logol {font-family: Verdana, Geneva, sans-serif;
					background-color: #000000; font-weight:bold; font-size: 32pt;
					color: #000000; }
					h1 {font-family: Verdana, Geneva, sans-serif;
					font-weight:bold; font-size: 18pt; color: #000000; }
					h2
					{font-family: Verdana, Geneva, sans-serif; font-weight:bold;
					font-size: 14pt; color: #000000; }
					h3 {font-family: Verdana, Geneva,
					sans-serif; font-size: 10pt; color: #808080; }
					.footer {font-family:
					Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; }
					tr
					{background-color: #f0f0f0;}
					tr.caption {background-color: #eeafaf;
					font-weight:bold}
					tr.captionm {background-color: #f8dfdf}
					tr.captionio {background-color: #afeeaf; font-weight:bold}
					tr.captioniom {background-color: #ccffcc}
					tr.captioninfo {background-color: #b2b2c5; font-weight:bold}
					tr.captioninfom {background-color: #b2b2c5}
				</style>
			</head>
			<body>
				<p class="logow">
					<span class="logol">.</span>
					<span class="logo">KOST-Sim</span>
					<span class="logov">y</span>
					<span class="logol">.</span>
					<span class="logox"> - </span>
				</p>
				<xsl:for-each select="KOSTSimyLog/IoExeption">
					<h1>Error:</h1>
					<div>
						<table width="100%">
							<tr class="caption">
								<td>
									<xsl:value-of select="Error" />
								</td>
							</tr>
						</table>
					</div>
					<br />
				</xsl:for-each>
				<xsl:for-each select="KOSTSimyLog/Image/Info">
					<div>
						<table width="100%">
							<tr class="captioninfo">
								<td>
									<xsl:value-of select="Message" />
								</td>
							</tr>
						</table>
					</div>
					<br />
				</xsl:for-each>
				<h2>Dissimilar: (<xsl:value-of select="KOSTSimyLog/Statistics/DissimilarPercentage"/>%)</h2>
				<xsl:for-each select="KOSTSimyLog/Image/Comparison">
					<xsl:if test="Dissimilar">
						<div>
							<table width="100%">
								<tr class="caption">
									<td>
										<xsl:value-of select="ValType" />
										<xsl:value-of select="PdfaVL" />
										->
										<xsl:value-of select="ValFile" />
									</td>
								</tr>
							</table>
							<table width="100%">
								<xsl:for-each select="Error">
									<tr class="captionm">
										<td width="25%">
											<xsl:value-of select="Modul" />
										</td>
										<td width="75%">
											<xsl:value-of select="Message" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</div>
						<br />
					</xsl:if>
				</xsl:for-each>
				<h2>Similar: (<xsl:value-of select="KOSTSimyLog/Statistics/SimilarPercentage"/>%)</h2>
				<xsl:for-each select="KOSTSimyLog/Image/Comparison">
					<xsl:if test="Similar">
						<div>
							<table width="100%">
								<tr class="captionio">
									<td>
										<xsl:value-of select="ValType" />
										->
										<xsl:value-of select="ValFile" />
									</td>
								</tr>
							</table>
							<table width="100%">
								<xsl:for-each select="Error">
									<tr class="captioniom">
										<td width="25%">
											<xsl:value-of select="Modul" />
										</td>
										<td width="75%">
											<xsl:value-of select="Message" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</div>
					</xsl:if>
				</xsl:for-each>
				<br />
				<h2>Not compared: (<xsl:value-of select="KOSTSimyLog/Statistics/NotComparedPercentage"/>%)</h2>
				<xsl:for-each select="KOSTSimyLog/Image/Comparison">
					<xsl:if test="NotCompared">
						<div>
							<table width="100%">
								<tr class="captioninfo">
									<td width="25%">
										<xsl:value-of select="ValFile" />
									</td>
									<td width="75%">
										<xsl:value-of select="Message" />
									</td>
								</tr>
							</table>
						</div>
					</xsl:if>
				</xsl:for-each>
				<br />
				<hr noshade="noshade" size="1" />
				<h3>
					<xsl:value-of select="KOSTSimyLog/Infos/Start" />
					-
					<xsl:value-of select="KOSTSimyLog/Infos/End" />
				</h3>
				<p class="footer">
					<xsl:value-of select="KOSTSimyLog/Infos/Info" />
				</p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>