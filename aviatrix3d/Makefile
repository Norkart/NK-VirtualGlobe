#
# Top Level makefile for the Aviatrix3D project
#
# This makefile is designed to build the entire library from scratch. It is
# not desigend as a hacking system. It is recommended that you use the normal
# javac/CLASSPATH setup for that.
#
# The following commands are offered:
#
# - class:  Compile just the classes. Don't make JAR file
# - jar:      Make the java JAR file
# - javadoc:  Generate the javadoc information
# - shaders:  Copy the shader definitions into the classes area
# - all:      Build everything (including docs)
# - clean:    Blow everything away
#

ifndef PROJECT_ROOT
export PROJECT_ROOT=$(PWD)
endif

include $(PROJECT_ROOT)/make/Makefile.inc

# Default instruction is to print out the help list
help:
	$(PRINT) 
	$(PRINT) "                   The Aviatrix3D Project"
	$(PRINT) 
	$(PRINT) "More information on this project can be found at http://aviatrix3d.j3d.org"
	$(PRINT) 
	$(PRINT) "The following options are offered and will build the entire codebase:"
	$(PRINT) 
	$(PRINT) "class:       Compile just the classes. Don't make JAR files."
	$(PRINT) "jar:         Make the java JAR file"
	$(PRINT) "javadoc:     Generate the javadoc information"
	$(PRINT) "shaders:     Copy the shader source files into the classes area"
	$(PRINT) "shaderjar:   Build the shader files into a separate JAR"
	$(PRINT) "all:         Build everything (including docs)"
	$(PRINT) "clean:       Blow all the library classes away"
	$(PRINT) 

all: class shaders jar javadoc

class:
	make -f $(JAVA_DIR)/Makefile buildall

jar:
	make -f $(SHADER_DIR)/Makefile jar
	make -f $(JAVA_DIR)/Makefile jar

shaders:
	make -f $(SHADER_DIR)/Makefile buildall

shaderjar:
	make -f $(SHADER_DIR)/Makefile jar

javadoc:
	make -f $(JAVA_DIR)/Makefile javadoc

clean:
	make -f $(JAVA_DIR)/Makefile clean
	make -f $(SHADER_DIR)/Makefile clean
