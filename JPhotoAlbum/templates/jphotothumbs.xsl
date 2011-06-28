<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:import href="jphotolist.xsl"/>

  <xsl:template match="img">
   <p>
   <xsl:if test="string-length(@src) &gt; 0">
    <center>
    <a name="{./@src}" href="pictures/{./@src}" target="ImageWindow" onclick="return parent.setPhoto('{./@src}')">
      <xsl:if test="@w"> 
        <img src="thumbs/{@src}" alt="{@src}" width="{@w div 4}" height="{@h div 4}" />
      </xsl:if>
      <xsl:if test="not(@w)"> 
        <img src="thumbs/{@src}" alt="{@src}" />
      </xsl:if>
    </a>
    </center>
    <br/>
   </xsl:if>
   <xsl:apply-templates />
   </p>
  </xsl:template>

  <xsl:template match="albumlink">
    <p><a href="{./@link}-thumbs.html">
    <br/>[<xsl:apply-templates />]</a></p>
  </xsl:template>


</xsl:stylesheet>
