<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
    extension-element-prefixes="redirect">
    
    <xsl:output method="html"  doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
        indent="yes"/>

    <xsl:param name="output.dir" select="'.'"/>

    <!-- Background color for all html pages -->
    <!--xsl:variable name="bcolor">
        <xsl:value-of select="doc/pageinfo/background"/>
    </xsl:variable-->

    <!-- Font color for all html pages -->
    <!--xsl:variable name="fcolor">
        <xsl:value-of select="doc/pageinfo/foreground"/>
    </xsl:variable-->
    <xsl:variable name="fcolor" select="'#000000'" />
    <xsl:variable name="bcolor" select="'#CCCCCC'" />

    <xsl:template match="doc">

        <!-- create the stylesheet.css -->
        <redirect:write file="{$output.dir}/stylesheet.css">
            <xsl:call-template name="stylesheet.css"/>
        </redirect:write>


        <!-- If categories are present go here -->
        <xsl:if test="count(category-name)  &gt; 0">

            <!-- create the index.html -->
            <redirect:write file="{$output.dir}/index.html">
                <xsl:apply-templates select="." mode="directories.list"/>
            </redirect:write>

            <!-- process all directories -->
            <xsl:apply-templates select="." mode="write"/>
            
                <!-- for each picture, create a html file -->
                <xsl:for-each select="img">
                    <xsl:variable name="directory.dir">
                        <xsl:value-of select="category"/>
                    </xsl:variable>
                    <xsl:variable name="name">
                        <xsl:value-of select= "substring-before(@src, '.')"/>
                    </xsl:variable>
                    <redirect:write file="{$output.dir}/{$directory.dir}/html/{$name}.html">
                        <xsl:apply-templates select="." mode="pictures.html"/>
                    </redirect:write>
                </xsl:for-each>
            </xsl:if>

            
            <!-- Check if we have any categories present and if not generate a default directory set-->
            <xsl:if test="count(category-name) = 0">

                <!-- create a index.html including the frames -->
                <redirect:write file="{$output.dir}/index.html">
                    <xsl:apply-templates select="." mode="no-category-frames.html"/>
                </redirect:write>

                <!-- create a thumbs.html  -->
                <redirect:write file="{$output.dir}/thumbs.html">
                    <xsl:apply-templates select="." mode="no-category-thumbs.html"/>
                </redirect:write>

                <!-- Generate image html files for each image -->
                <xsl:for-each select="img">
                    <xsl:variable name="name">
                        <xsl:value-of select= "substring-before(@src, '.')"/>
                    </xsl:variable>
                    <redirect:write file="{$output.dir}/html/{$name}.html">
                        <xsl:apply-templates select="." mode="no-category-html.html"/>
                    </redirect:write>
                </xsl:for-each>
           </xsl:if>
        </xsl:template>
        <!-- End of category version -->

        <!-- list of directories in the index.html page-->
        <xsl:template match="doc" mode="directories.list">
            <html>
                <head>
                    <LINK TITLE="Style" TYPE="text/css" REL="stylesheet" href="stylesheet.css"></LINK>           
                </head>
                <BODY>
                    <xsl:variable name="title">
                        <xsl:value-of select="pageinfo/title"/>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="string-length($title) &gt; 0">
                            <H2><xsl:value-of select="pageinfo/title"/></H2>
                        </xsl:when>
                        <xsl:otherwise>
                            <H2>All Pictures</H2>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <TABLE WIDTH="100%">
                        <xsl:for-each select="category-name">
                            <xsl:variable name="name">
                                <xsl:value-of select="current()"/>
                            </xsl:variable>
                            <xsl:variable name="test1">
                                <xsl:call-template name="replaceCharsInString">
                                    <xsl:with-param name="stringIn" select="string($name)"/>
                                    <xsl:with-param name="charsIn" select="'ä'"/>
                                    <xsl:with-param name="charsOut" select="'%E4'"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:variable name="test2">
                                <xsl:call-template name="replaceCharsInString">
                                    <xsl:with-param name="stringIn" select="string($test1)"/>
                                    <xsl:with-param name="charsIn" select="'ö'"/>
                                    <xsl:with-param name="charsOut" select="'%F6'"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:variable name="test3">
                                <xsl:call-template name="replaceCharsInString">
                                    <xsl:with-param name="stringIn" select="string($test2)"/>
                                    <xsl:with-param name="charsIn" select="'Ä'"/>
                                    <xsl:with-param name="charsOut" select="'%C4'"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:variable name="test4">
                                <xsl:call-template name="replaceCharsInString">
                                    <xsl:with-param name="stringIn" select="string($test3)"/>
                                    <xsl:with-param name="charsIn" select="'Ö'"/>
                                    <xsl:with-param name="charsOut" select="'%D6'"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <tr>
                                <td>
                                    <a href="{$test4}/{$test4}.html"><xsl:value-of select="current()"/></a>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </TABLE>
                </BODY>
            </html>
        </xsl:template>

    <!-- html files into individual categories -->
    <xsl:template match="category-name" mode="write">

        <xsl:variable name="directory.dir">
            <xsl:value-of select="current()"/>
        </xsl:variable>

        <!-- create a picture.html in the package directory -->
        <redirect:write file="{$output.dir}/{$directory.dir}/{$directory.dir}.html">
            <xsl:apply-templates select="." mode="directory.html"/>
        </redirect:write>

        <!-- create a thumbs.html in the package directory -->
        <redirect:write file="{$output.dir}/{$directory.dir}/thumbs.html">
            <xsl:apply-templates select="." mode="thumbs.html"/>
        </redirect:write>

        <!-- create a picture.html in the package directory -->
        <redirect:write file="{$output.dir}/{$directory.dir}/picture.html">
            <xsl:apply-templates select="." mode="picture.html"/>
        </redirect:write>

   </xsl:template>

    <!-- for each category generate the frames-->
    <xsl:template match="category-name" mode="directory.html">
        <html>
            <head>
                <title> <xsl:value-of select="category-name"/></title>
            </head>
            <frameset cols="25%,75%">
                <frame src="thumbs.html" name="thumbsFrame"/>
                <frame src="picture.html" name="picturesFrame"/>
            </frameset>
        </html>
    </xsl:template>

    <!-- Generate the main frame page and set the first image to the right side visible -->
    <xsl:template match="doc" mode="no-category-frames.html">
            <xsl:variable name="name">
                <xsl:value-of select="substring-before(img/@src, '.')"/>
            </xsl:variable>
        <html>
            <head>
                <title>Pictures</title>
            </head>
            <frameset cols="25%,75%">
                <frame src="thumbs.html" name="thumbsFrame"/>
                <frame src="html/{$name}.html" name="picturesFrame"/>
            </frameset>
        </html>
    </xsl:template>

<!-- If category names present generates thumbs.html -->
<xsl:template match="category-name" mode="thumbs.html">
    <xsl:variable name="cat-name">
        <xsl:value-of select="current()"/>
    </xsl:variable>

    <html>
        <head>
            <LINK TITLE="Style" TYPE="text/css" REL="stylesheet" href="../stylesheet.css"></LINK>           
         </head>
        <BODY BGCOLOR="{$bcolor}" TEXT="{$fcolor}">
            <H2><xsl:value-of select="current()"/></H2>
            
            <TABLE WIDTH="100%" BGCOLOR="{$bcolor}">
                <xsl:for-each select="(ancestor::doc)/img">
                    <xsl:variable name="name">
                        <xsl:value-of select="substring-before(@src, '.')"/>
                    </xsl:variable>
                    <xsl:variable name="cat">
                        <xsl:value-of select="category"/>
                    </xsl:variable>

                    <xsl:if test="$cat = $cat-name">
                         <tr>
                             <td ALIGN="center">
                                 <a href="html/{$name}.html" target="picturesFrame"><IMG src="thumbs/{@src}" border="0" alt="{@src}"></IMG></a>
                             </td>
                         </tr>
                     </xsl:if>
                </xsl:for-each>
                <TR>
                    <TD>
                        <a href="../index.html" target="_top">Back to index</a>
                    </TD>
                </TR>
             </TABLE>
        </BODY>
    </html>
</xsl:template>

<!-- Generates thumbs.html if no categories are there -->
<xsl:template match="doc" mode="no-category-thumbs.html">
    <html>
        <head>
            <LINK TITLE="Style" TYPE="text/css" REL="stylesheet" href="stylesheet.css"></LINK>           
         </head>
        <BODY BGCOLOR="{$bcolor}" TEXT="{$fcolor}">
            <H2 ALIGN="center"><xsl:value-of select="pageinfo/title"/></H2>

            <TABLE WIDTH="100%" BGCOLOR="{$bcolor}">
                <xsl:for-each select="img|albumlink">
                    <xsl:choose>
                        <xsl:when test="string-length(@link) = 0">

                            <xsl:variable name="name">
                                <xsl:value-of select="substring-before(@src, '.')"/>
                            </xsl:variable>
                            
                            <tr>
                                <td ALIGN="center" >
                                    <a href="html/{$name}.html" target="picturesFrame"><IMG src="thumbs/{@src}" border="1" alt="{@src}"></IMG></a>
                                    <BR><xsl:value-of select="text"/></BR>
                                </td>
                            </tr>

                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="lastLink">
                                <xsl:call-template name="last">
                                    <xsl:with-param name="lastString" select="@link"/>
                                </xsl:call-template>
                            </xsl:variable>

                            <xsl:variable name="newLink">
                                <xsl:value-of select="substring(@link,1,string-length($lastLink)+1)"/>
                            </xsl:variable>

                            <tr>
                                <td ALIGN="center" >
                                    <a href="{$newLink}/index.html" target="_window"><xsl:value-of select="text"/></a>
                                </td>
                            </tr>

                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:for-each>
                <TR>
                    <TD ALIGN="center">
                        <a href="../index.html" target="_top">Takaisin pääsivulle /<BR></BR>Back to index</a>
                    </TD>
                </TR>
                <TR>
                    <TD ALIGN="center">Copyright 2001- 2004 Tarja Hakala</TD>
                </TR>
               </TABLE>
        </BODY>
    </html>
</xsl:template>

<!-- Generates picture.html -->
<xsl:template match="category-name" mode="picture.html">
    <html>
        <head>
            <LINK TITLE="Style" TYPE="text/css" REL="stylesheet" href="../stylesheet.css"></LINK>           
         </head>
        <BODY BGCOLOR="{$bcolor}" TEXT="{$fcolor}">
        </BODY>
    </html>
</xsl:template>

<!-- Generates picture.html -->
<xsl:template match="img" mode="no-category-html.html">
    <html>
        <head>
            <LINK TITLE="Style" TYPE="text/css" REL="stylesheet" href="stylesheet.css"></LINK>           
         </head>
        <BODY BGCOLOR="{$bcolor}" TEXT="{$fcolor}">
            <TABLE WIDTH="100%" BGCOLOR="{$bcolor}">
                <tr>
                    <td ALIGN="center">
                            <IMG src="../pictures/{@src}" border="0" alt="{@src}"></IMG>
                    </td>
                </tr>
               </TABLE>
        </BODY>
    </html>
</xsl:template>

<xsl:template match="img" mode="pictures.html">
    <html>
        <head>
            <LINK TITLE="Style" TYPE="text/css" REL="stylesheet" href="../stylesheet.css"></LINK>           
         </head>
        <BODY BGCOLOR="{$bcolor}" TEXT="{$fcolor}">
            <TABLE WIDTH="100%" BGCOLOR="{$bcolor}">
                <tr>
                    <td ALIGN="center">
                            <IMG src="../pictures/{@src}" border="0" alt="{@src}"></IMG>
                        </td>
                    </tr>
               </TABLE>
        </BODY>
    </html>
</xsl:template>

<xsl:template name="replaceCharsInString">
  <xsl:param name="stringIn"/>
  <xsl:param name="charsIn"/>
  <xsl:param name="charsOut"/>
  <xsl:choose>
    <xsl:when test="contains($stringIn,$charsIn)">
      <xsl:value-of select="concat(substring-before($stringIn,$charsIn),$charsOut)"/>
      <xsl:call-template name="replaceCharsInString">
        <xsl:with-param name="stringIn" select="substring-after($stringIn,$charsIn)"/>
        <xsl:with-param name="charsIn" select="$charsIn"/>
        <xsl:with-param name="charsOut" select="$charsOut"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$stringIn"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template name="last">
    <xsl:param name="lastString"/>
    <xsl:choose>
       <xsl:when test="contains($lastString,'/')">
           <xsl:call-template name="last">
               <xsl:with-param name="lastString"
select="substring-after($lastString,'/')"/>
           </xsl:call-template>
       </xsl:when>
       <xsl:otherwise>
           <xsl:value-of select="$lastString"/>
       </xsl:otherwise>
   </xsl:choose>
</xsl:template>
 

<xsl:template name="stylesheet.css">
    body {
         margin-left: 10;
         margin-right: 10;
         font:normal 100% arial,sanserif;
       }

       th, td {
       font:normal 100% arial;
      }
      th {
       font-weight:bold;
       background: #ccc;
       color: black;
      }
      tr {
       padding: 0px;
      }

      td {
       padding-top: 10px;
       padding-bottom: 10px;
      }

      table, th, td {
       border: none
      }
      
      h2 {
       font-weight:bold;
       font-size:140%;
       margin-bottom: 5;
      }
      h3 {
       font-size:100%;
       font-weight:bold;
       background: #525D76;
       color: white;
       text-decoration: none;
       padding: 5px;
       margin-right: 2px;
       margin-left: 2px;
       margin-bottom: 0;
      }

      a:link     {
            font-family: Arial, Sans-serif;
            color:blue;
            text-decoration:none;
            font-size:10pt
      }

      a:visited  {
            font-family: Arial, Sans-serif;
            color:blue;
            text-decoration:none;
            font-size:10pt
      }

      a:active   {
            font-family: Arial, Sans-serif;
            color:white;
            text-decoration:none;
            font-size:10pt
       }
      
      pre {
       font-size:120%;
      }
    </xsl:template>


</xsl:stylesheet>


