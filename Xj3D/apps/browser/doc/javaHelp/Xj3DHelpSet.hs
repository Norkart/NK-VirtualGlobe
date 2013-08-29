<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset   
PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="2.0">

  <!-- title -->
  <title>Xj3D Browser - Help</title>
    <size width="400" height="600" />
    <location x="200" y="300" />

  <!-- maps -->
  <maps>
     <homeID>SplashImage</homeID>
     <mapref location="Xj3DMap.jhm"/>
  </maps>

  <!-- views -->
  <view mergetype="javax.help.UniteAppendMerge">
    <name>TOC</name>
    <label>Table Of Contents</label>
    <type>javax.help.TOCView</type>
    <data>Xj3DHelpTOC.xml</data>
    <size width="400" height="600" />
  </view>

  <!-- don't include this until we've built Xj3DHelpIndex.xml
  <view mergetype="javax.help.SortMerge">
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>Xj3DHelpIndex.xml</data>
  </view>  -->

  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>

  <!-- presentation addition was taken from JavaHelp User Guide section 4.4.1 Helpset File Format but still not working -->
  <presentation default=true>
    <name>main window</name>
    <size width="400" height="600" />
    <location x="200" y="300" />
    <title>Xj3D Browser</title>
    <toolbar>
        <helpaction>javax.help.BackAction</helpaction>
        <helpaction>javax.help.ForwardAction</helpaction>
        <helpaction image="homeicon">javax.help.HomeAction</helpaction>
    </toolbar>
  </presentation>

  <!-- This is where we install a filter for html pages so we can check for file/mime types that
       are not supported in the minimal Swing html renderer.  The PDFInterceptor class looks only
       for pdfs, and if it finds one, it sends it to an external browser through AUVWorkbenchConfig. -->

  <!-- This is not the right class path, need to fix later

  <impl>
    <viewerregistry viewertype="text/html" viewerclass="workbench.main.javahelp.PDFInterceptor"/>
  </impl>
  -->
</helpset>

