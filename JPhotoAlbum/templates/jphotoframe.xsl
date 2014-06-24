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
      <script language="JavaScript">
      <![CDATA[
        function getPreviousLink() {
          return "[Previous]";
        }
        function getNextLink() {
          return "[Next]";
        }
        function getPhotoHeading(photoname) {
          return "&nbsp;"+photoname+"&nbsp;";
        }
        // Any customized JavaScript should go here instead of jphoto.js
      ]]>
     </script>
    </head>
      <xsl:if test="./img[position()='1']"> 
      <FRAMESET COLS="25%,75%">
      <FRAME Name="Thumbs" src="{./pageinfo/album-name}-thumbs.html"></FRAME>
       <xsl:for-each select="./img[position()='1']">
          <xsl:if test="not(./@src='')"> 
          <FRAME Name="ImageWindow" src="pictures/{./@src}"></FRAME>
        </xsl:if>
        <xsl:if test="./@src=''"> 
          <FRAME Name="ImageWindow" ></FRAME>
        </xsl:if>
       </xsl:for-each>
      </FRAMESET>
      </xsl:if>
      <xsl:if test="not(./img[position()='1'])"> 
      <FRAMESET COLS="*">
      <FRAME Name="Thumbs" src="{./pageinfo/album-name}.html"></FRAME>
      </FRAMESET>
      </xsl:if>
   </html>
  </xsl:template>

</xsl:stylesheet>
