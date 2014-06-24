<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/>

<xsl:template match="/all">
   <html>
    <head>
      <title>Index</title>
    </head>
    <body>
	<xsl:apply-templates select="doc" />
    </body>
   </html>
</xsl:template>

<xsl:template match="doc">
 <span class="index-album" id="{./pageinfo/album-name}">
  <xsl:apply-templates select="img"/>
 </span>
</xsl:template>

<xsl:template match="albumlink">
</xsl:template>

<xsl:template match="img[@src!='']">
 <div class="index-img" style="float: left">
  <a href="{../pageinfo/output-directory}{../pageinfo/album-name}-frame.html#{./@src}">
   <img src="{../pageinfo/output-directory}thumbs/{./@src}" alt="{../pageinfo/album-name}"/>
  </a>
 </div>
</xsl:template>

<xsl:template match="img[@src='']">
</xsl:template>

</xsl:stylesheet>
