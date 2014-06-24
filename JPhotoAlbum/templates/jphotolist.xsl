<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/>

  <xsl:template match="/doc">
   <html>
    <head>
      <xsl:if test="not(./pageinfo/description='')"> 
	<meta name="description" content="{./pageinfo/description}"/>
      </xsl:if>
      <xsl:if test="./pageinfo/description=''"> 
	<meta name="description" content="Valokuvia: {./pageinfo/title}"/>
      </xsl:if>
      <meta name="keywords" content="kuva, kuvia, album, photos, photo, {./pageinfo/keywords}"/>
      <title><xsl:value-of select="./pageinfo/title"/></title>
      <script type="text/javascript" src="jphoto.js"></script>    
    </head>
    <body bgcolor="{./pageinfo/background}" text="{./pageinfo/foreground}"
          vlink="#0000A0" link="#0000FF" alink="#0000F0"  onLoad='selectFirst()'>
      <h2>
      <xsl:value-of select="./pageinfo/title"/>
      </h2>
	<xsl:apply-templates select="//*[@src]" />
      <p/>
      <p><xsl:value-of select="./pageinfo/watermark"/></p>

      <small>Page generated with <a href="http://jphotoalbum.jpkware.com/">JPhotoAlbum</a></small>.
    </body>
   </html>
  </xsl:template>

  <xsl:template match="img">
    <p><img src="pictures/{./@src}" alt="{./@src}" />
    <br/><xsl:apply-templates /></p>
  </xsl:template>

  <xsl:template match="img[@w]">
    <p>
   <xsl:if test="string-length(@src) &gt; 0">
      <xsl:if test="@w"> 
       <img src="pictures/{@src}"  
	alt="{@src}" 
	width="{@w}" height="{@h}" />
      </xsl:if>
      <xsl:if test="not(@w)"> 
       <img src="pictures/{@src}"  alt="{@src}" />
      </xsl:if>
   </xsl:if>
    <xsl:if test="not(exif/aperture='')"> 
     <br/><small>[
	<xsl:value-of select="./exif/date"/>,
	<xsl:value-of select="./exif/exposure-time"/> @ <xsl:value-of select="./exif/aperture"/>
      <xsl:if test="not(./exif/exposure-bias='')">, Bias <xsl:value-of select="./exif/exposure-bias"/>
      </xsl:if>
      <xsl:if test="not(./exif/iso='')">, <xsl:value-of select="./exif/iso"/>
      </xsl:if>
	]</small>
    </xsl:if>
    <br/><xsl:apply-templates /></p>
  </xsl:template>

  <xsl:template match="albumlink">
    <table cellpadding="3" cellspacing="1" border="0"
    style="text-align: left; width: 100%;">
    <tbody>
    <tr>
    <td
    style="text-align: center; vertical-align: middle; height: 160px; width: 160px;"
    rowspan="2" colspan="1"><a href="{./@link}-frame.html">
      <xsl:if test="@w"> 
        <img src="thumbs/{@src}" alt="{@link}" width="{@w div 4}" height="{@h div 4}" />
      </xsl:if>
    </a>
    </td>
    <td style="vertical-align: middle;" rowspan="1" colspan="1"><xsl:apply-templates />
    </td>
    </tr>
    <tr>
    <td
    style="height: 32px; text-align: left; vertical-align: middle;">
	<a href="{./@link}-frame.html">[Thumbnails]</a> - <a href="{./@link}.html">[Large photos]</a>
    </td>
    </tr>
    </tbody>
    </table>
  </xsl:template>

  <xsl:template match="link">
    <a href="{./@ref}.html"><xsl:value-of select="."/></a>
  </xsl:template>

  <xsl:template match="toplink">
    <a href="{./@ref}.html" target="_top"><xsl:value-of select="."/></a>
  </xsl:template>

  <xsl:template match="imagelink">
    <a href="{./@ref}" target="_top"><xsl:value-of select="."/></a>
  </xsl:template>

  <xsl:template match="text">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="exif">
<!--
    <p><xsl:value-of select="aperture"/>,
	<xsl:value-of select="exposure-time"/>
	<xsl:value-of select="exposure-bias"/>,
	<xsl:value-of select="iso"/></p>
-->
  </xsl:template>
</xsl:stylesheet>
