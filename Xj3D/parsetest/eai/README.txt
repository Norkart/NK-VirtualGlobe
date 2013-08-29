TEST DESCRIPTION AND STATUS
Last Updated: June 19, 2002
by Brad Vender

Warning:  The specification for the EAI and the behavior of the VRML 
environment during authoring manipulation is remarkably vague about various 
behaviors.  Tests are listed as working in the opinion of the test author.

It is difficult to write a test of the EAI functionality which tests
just one (or even just two) services at a time.  For example, to test the
VrmlEventListener interface, it is necessary to get a Browser instance,
get a Node instance, get an EventOut reference, register a VrmlEventListener,
and either have nodes which generate events or use even more methods to
generate an event using EventIn's.  Currently there are issues concerning the 
various nodes which reference external content (texture nodes, Inline nodes, 
Script nodes, etc.).

Several of these tests are as complicated as they are
because they were used to detect, diagnose, and fix previous errors.

BasicReplaceWorld: Working.  One should see one blue sphere.

CreateFromString: Working.  One should see one blue and one red sphere.
  Uses replaceWorld.
CreateFromString2: Working.  One should see one blue and one red sphere.
  Somewhat convoluted because it has to wait for a url to load first.
CreateFromString3: Working.  One should see a moving sphere and lots of
  event out reports.  Uses replaceWorld.
CreateFromString4: Working.  One should see a moving sphere and lots of
  event out reports.  Uses replaceWorld and differs from CreateFromString3
  as to when the nodes are created.
CreateFromString5: Working.  One should see a moving sphere and lots of
  event out reports.  Creates the content, calls loadURL, and then adds
  the content to the new scene.
CreateFromString6: Not working.  One should see a moving sphere and lots
  of event out reports.  Creates the content, calls replaceWorld and then
  adds the content to the new scene.  This uses an ECMAScript script and
  addRoute, and it isn't clear what the failure is.

ListenerTest: Working.  One should see one scene followed rapidly by a
  second scene containing just a sphere, accompanied by text output for
  shutdown and restart events.

LoadURL: Mostly working.  Testing the loadURL function.  Mostly works, but the 
  URL_ERROR events aren't generated.  One should see a red cone from
  parsetest/geometry/cone.wrl.

SetGetTest1: Crashes due to internal Xj3D errors.
SetGetTest2: Crashes due to internal XJ3D errors.


UpdateTest1: Working.  One should see a blue and a red sphere which have been
  constructed with beginUpdate/endUpdate calls thrown in.

UpdateTest2: Test doesn't do what it describes in file.

UpdateTest3: Not working.  deleteRoute doesn't appear to delete the route.
  One should see a 0.5 event for both A and B, and only one 0.3 event for A.
  The good news is that addRoute and removeRoute both work.  The bad news
  is that its possible to add a route, send an event, and then remove
  the route before the event cascade occurs.  Since nodes are currently
  firing their event listeners independently of the routing mechanism,
  this is what is causing the errors.

UserDataTest: Working.  One should see text from tests which check for
  the EventOut's and EventIn's of exposedFields to be equal, and checks that 
  get/setEventOut on a few node and field types gives back the user's data,
  all of which should say 'equivalence is held' or 'get/setTest ok' or such
  things.

VrmlEventListener1: Working.  One should see the standard two spheres and
  output from the generic verbose vrml event listener from scene construction.

VrmlEventListener2: Working.  No geometry is supposed to be visible since
  this test concentrates on node manipulation without adding the nodes to the 
  visible scene graph, and listening for some of the events.  One should see
  output from the generic verbose vrml event listener similar to 
  VrmlEventListener1.

OTHER FILES

TestFactory.java, GenericFieldListener.java, 
GenericBrowserListener.java, and root.wrl are support files
and classes used by the tests.

Using 
  javac -classpath "C:/cygwin/projects/x3d/classes;." *.java
and
  java -classpath "C:/cygwin/projects/x3d/classes;." TestName
to compile and run the tests (adjusted for differences in location, naturally)
should be okay.

