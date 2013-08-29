<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:template match="/" >
<X3D profile="Immersive">
<Scene>
   <Viewpoint position="0 0 1100" description="Initial" />
   <NavigationInfo avatarSize="10 1.6 0.75" type='"FLY" "ANY"' />
   <xsl:apply-templates select="Universe/Galaxy" />
</Scene>
</X3D>

</xsl:template>

<xsl:template match="Galaxy" >
   <xsl:apply-templates select="Star" />
   <xsl:apply-templates select="Planet" />
</xsl:template>

<xsl:template match="Star" >
   <Transform translation="{@position}">
      <Shape>
               <xsl:choose>
                  <xsl:when test="@class='M'">
                     <Appearance>
                        <Material transparency="0.6" diffuseColor="1 0 0" />                  
                     </Appearance>
                     <Sphere radius="139.1" />
                  </xsl:when>
                  <xsl:when test="@class='G'">
                     <Appearance>
                        <Material transparency="0.6" diffuseColor="1 1 0" />                  
                     </Appearance>
                     <Sphere radius="139.1" />
                  </xsl:when>
                  <xsl:when test="@class='O'">
                     <Appearance>
                        <Material transparency="0.6" diffuseColor="0 0 1" />                  
                     </Appearance>
                     <Sphere radius="139.1" />
                  </xsl:when>
               </xsl:choose>
      </Shape>
   </Transform>   
</xsl:template>

<xsl:template match="Planet" >
   <Transform translation="{@position}">
      <Shape>
           <xsl:choose>
              <xsl:when test="@class='M'">
                 <Appearance>
                    <Material diffuseColor="0 0 1" />
                 </Appearance>
                 <Sphere radius="1.2742" />
              </xsl:when>
           </xsl:choose>
      </Shape>
   </Transform>   
</xsl:template>

</xsl:stylesheet>