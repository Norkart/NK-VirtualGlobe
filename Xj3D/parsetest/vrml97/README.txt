  VRML97 Parsing tests:

empty*.wrl
  Empty scenes that only contain a header. Variations on spacing and the use
of comments.

bad_header*.wrl
  Empty worlds with bad header structures. Bits missing or badly formatted
text.

single_node*.wrl
  A world with a single Group node in it with different declarations of
brackets.

field*.wrl 
  Worlds with single nodes in them that contain various different field types
both MF and SF.	Also tests the handling of DEF/USE declarations without the
actual use of them.

sfnode*.wrl
  Worlds that contain combinations of SFNode references. No MFNode children
values with multiple node declarations. There are no field declarations other
than those need to describe single children nodes.

mfnode*.wrl
  Worlds that contain straight mfnode declarations. There are no field 
declarations in these nodes other than those needed to describe children
nodes.

combo*.wrl
  Worlds that contain combinations of field declarations, SF and MFNode 
children types.

proto*.wrl
  Worlds with various forms of proto declarations. The root node type in 
each case will be a Group. The worlds test the following in order: It does
not test the combination of protos and scripts
  1 - Simplest, empty brackets
  2 - Interface has an eventIn and EventOut
  3 - Interface now also includes a field value with default value
  4 - Empty interface but body contains SF/MFNode decls and fields
  5 - Body contains multiple node decls at the top level
  6 - Body uses an IS for eventIn
  7 - Body uses an IS for field value
  8 - Body has nested proto declarations
