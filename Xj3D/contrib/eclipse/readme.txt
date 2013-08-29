Building Xj3D under Eclipse
By Brad Vender
Last Major Revision: May 02, 2005.

	The ease of building Xj3D under Eclipse is directly proportional
to one's ability to have all of the support libraries for all of the
renderers installed before building.  The following steps are used
under Eclipse 3.0.2 to build Xj3D.
  0.  Get a copy of Xj3D, presumably from CVS, in Java project in Eclipse.
      For peace of mind, under the "Build Path" option for that
      project's properties, remove all of the supplied
      default source paths, and set the output path to
      <Project Root>/bin or <Project Root>/classes or something
      similar.
      You will most likely want to turn off automatic rebuild
      while setting up the project.
  1.  Correct the path to JavaCC in /contrib/Eclipse/build.xml
      to work with where your copy of JavaCC is installed.  I'm not sure
      if this is still required in Eclipse 3.0, but it still works to install
      seperately.
  2.  Run build.xml using the Run->Ant build... context menu.  The
      purpose of this step is to generate the parser source files
      in src/java/org/web3d/parser/vrml and src/java/org/web3d/parser/x3d
  3.  Decide which of the Java3D, OpenGL and mobile renderers you wish to 
      build.  You'll need the support libraries for the renders you want and need to 
      exclude the ones you don't want from the build list.
      Java3D -> org.web3d.vrml.renderer.j3d and org.web3d.j3d.browser
      OpenGL -> org.web3d.vrml.renderer.ogl and org.web3d.ogl.browser
      Mobile -> org.web3d.vrml.renderer.mobile
      
      The main source directory is src/java, and resources are in the src/images and
      src/config directories.  Because Eclipse is going to act badly if you include both
      src/ and src/java/ in the build path, you will need to specify src as a source 
      directory with
        include: images/;config/
        exclude: *
      and the include src/java/ seperately.  Note that the trailing slashes are
      important to Eclipse, so if you don't see the sub-directories of images/ and config/
      show up as packages under src/, Xj3D won't find its configuration files when it runs.
      
  5.  Other packages which may be excluded if they cause problems:
      org.web3d.vrml.device.jinput
      org.web3d.j3d.device.elumens
      org.web3d.j3d.loaders
      org.web3d.vrml.export
      org.web3d.x3d.sai.geom3d
      org.web3d.x3d.sai.group
      org.web3d.x3d.sai.shape
  6.  If you get an error compiling a file, check to see if it is listed in
      the Makefile for that package.  If it isn't in the makefile, then do whatever
      you want to to get Eclipse to stop complaining.
  7.  Eclipse seems to lack the ability to exclude a single file from
      the build order, but I haven't tried that yet, and I don't know how
      well that script is maintained.
  8.  If you turned automatic build off, you should probably turn it back on.
  9.  Depending on your personal preferences, you can add parsetest/eai,
      parsetest/sai/external, apps/browser/src/java to the build path of your main
      project or try building those in a seperate project dependent on your main
      project.

--Brad Vender