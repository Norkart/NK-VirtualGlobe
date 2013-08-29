REM @echo off

SET CP="examples/DOMViewer;jars/j3d-org-all_0.9.0.jar;jars/gnu-regexp-1.0.8.jar;jars/httpclient.jar;jars/j3d-org-images.jar;jars/js.jar;jars/uri.jar;jars/vlc_uri.jar;jars/xj3d-common.jar;jars/xj3d-core.jar;jars/xj3d-eai.jar;jars/xj3d-ecmascript.jar;jars/xj3d-j3d.jar;jars/xj3d-jaxp.jar;jars/xj3d-jsai.jar;jars/xj3d-jsai.jar;xj3d-net.jar;jars/xj3d-norender.jar;jars/xj3d-ogl-sg.jar;jars/xj3d-ogl.jar;jars/xj3d-parser.jar;jars/xj3d-render.jar;jars/xj3d-runtime.jar;jars/xj3d-sav.jar;jars/xj3d-script-base.jar;jars/xj3d-xml-util.jar;jars/dom2.jar"

echo Running Xj3D DOMViewer

java -classpath %CP% -Xmx256M DOMViewer
