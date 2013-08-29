M3 Note: These are not working right now.  There is a lot of movement spec
wise on the SAI, we should revisit this when the dust settles.

     Examples of SAI and DOM scenegraph interaction
     
The examples in this directory show how you can modify a X3D scenegraph using
either the SAI or DOM interfaces. We can operated this on one of two levels
with or without a rendering engine. With the current implementation in our
factory code, when parsing an XML document, it will return objects that are
capable of using both the SAI and DOM methods.

When looking at the SAI interface specification, you will notice that each
class implements one of the DOM interfaces. Thus, with a correctly built 
DOM structure, you should be able to use either. Which one you should use
depends on your application. In the minds of the developers working on the
specification, it is assumed that SAI is for high speed interaction and the
DOM for the slower, higher integrated document collections.

This example shows how to load a new scene and then find elements using the
DOM interfaces. Once you have access to classes with DOM, you can then use
either the SAI or DOM to read the same piece. In the local directory we have
a file called example.xml. This contains a number of nodes that have been
DEF'd. We will grab these, make modifications and then write out the results
using both interfaces to compare the differences.

