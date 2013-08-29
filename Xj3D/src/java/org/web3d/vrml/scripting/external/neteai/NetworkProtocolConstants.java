/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

/**
 * NetworkProtocolConstants is a utility class for specifying constants for the
 * communications framework and utility methods for reading and writing data.
 * 
 * Upon opening a connection, the client will transmit the integer
 * CONNECTION_MAGIC_NUMBER_CLIENT and expect to read CONNECTION_MAGIC_NUMBER_SERVER.
 * The server expects to read CONNECTION_MAGIC_NUMBER_CLIENT and then transmits
 * CONNECTION_MAGIC_NUMBER_SERVER.  Deviation from this handshake routine
 * causes the connection to be closed.
 * 
 * The network protocol is broken into a sequence of packets.  
 * Requests from the client to the server follows the format of
 *  <PACKET_TYPE>
 *  <PACKET_ID>
 *  <PACKET_DATA>
 * Responses to client requests follow the format
 *  <PACKET_TYPE>
 *  <PACKET_ID>
 *  <RESPONSE_TYPE>
 *  <PACKET_DATA>
 * where PACKET_ID for a response matches the PACKET_ID of the
 * original request, and RESPONSE_TYPE is a normal response
 * (calling method should return normally),
 * an unexpected error (calling method should indicate a
 * run time exception), or a routine exception response
 * (calling method should generate an expected exception).
 * 
 * For spontaneous client to server messages such as
 * the various event notifications, the format is instead
 *  <PACKET_TYPE>
 *  <PACKET_DATA>
 * 
 * The format of PACKET_DATA is naturally specific to the packet type. 
 * 
 * When dealing with arrays, the array length is transmitted before
 * the array elements.  For null arrays an array length of -1 is
 * specified.
 * 
 * For Node objects only the ID number of each node is specified.
 * The ID -1 is reserved to specify a null Node object.
 * 
 * Strings are transmitted using the readUTF and writeUTF methods.
 * All other field types (int, float, etc.) use the appropriate
 * read and write methods on the data input/output stream.
 */
public abstract class NetworkProtocolConstants {

	/** Magic number sent by client for connection handshake */
	static final int CONNECTION_MAGIC_NUMBER_CLIENT = 0x4224;
	
	/** Magic number sent by server for connection handshake */
	static final int CONNECTION_MAGIC_NUMBER_SERVER = 0x1001;
	
	/** Request from client to server to register a browser listener */
	static final int ADD_BROWSER_LISTENER_REQUEST = 10;
	
	/** Response from server to client replying to addBrowserListener */
	static final int ADD_BROWSER_LISTENER_REPLY = 11;

	/** Request from client to server to register an eventOut listener */
	static final int ADD_EVENT_OUT_LISTENER_REQUEST = 12;
	
	/** Reply from server concerning addEventOutListener request */
	static final int ADD_EVENT_OUT_LISTENER_REPLY = 13;
	
	/** Request from client to server to add a route to the scene */
	static final int ADD_ROUTE_REQUEST = 14;
	
	/** Reply from server to client concerning addRoute request */
	static final int ADD_ROUTE_REPLY = 15;
	
	/** Request from client to server to construct VRML nodes */
	static final int CREATE_VRML_FROM_STRING_REQUEST = 16;
	
	/** Response from server to client specifying the node ID's for the new geometry */
	static final int CREATE_VRML_FROM_STRING_REPLY = 17;
	
	/** Request from client to server for createVrmlFromURL */
	static final int CREATE_VRML_FROM_URL_REQUEST = 18;
	
	/** Response from server to client concerning createVrmlFromURL request */
	static final int CREATE_VRML_FROM_URL_REPLY = 19;
	
	/** Notification from client to server that a given node is no longer referenced */
	static final int DISPOSE_NODE_REQUEST = 20;
	
	/** Response from server to client for return status of DISPOSE_NODE_REQUEST */
	static final int DISPOSE_NODE_REPLY = 21;
	
	/** Request from client to server for current frame rate */
	static final int GET_CURRENT_FRAME_RATE_REQUEST = 22;
	
	/** Response from server with the current frame rate */
	static final int GET_CURRENT_FRAME_RATE_REPLY = 23;
	
	/** Request from client to server with current speed */
	static final int GET_CURRENT_SPEED_REQUEST = 24;
	
	/** Response from server with the current speed */
	static final int GET_CURRENT_SPEED_REPLY = 25;
	
	/** Request from client to server to find a node by name and return a node ID */
	static final int GET_NODE_REQUEST = 26;
	
	/** Response from server to client specifying a node ID for a given named node */
	static final int GET_NODE_REPLY = 27;
	
	/** Client enquiry on type of node */
	static final int GET_NODE_TYPE_REQUEST = 28;
	
	/** Server answer on type of node */
	static final int GET_NODE_TYPE_REPLY = 29;
	
	/** Request from client to server to find an eventIn on a node.  Node is specified by
	 *  ID, field is specified by name. */
	static final int GET_EVENTIN_REQUEST = 30;
	
	/** Response from server to client specifying a field ID for the requested field. */
	static final int GET_EVENTIN_REPLY = 31;
	
	/** Request from client to server to find an eventOut on a node.  Node is specified by
	 *  ID, field is specified by name. */
	static final int GET_EVENTOUT_REQUEST = 32;
	
	/** Response from server to client specifying a field ID for the requested field
	 */
	static final int GET_EVENTOUT_REPLY = 33;
	
	/** Request from client to server for current value of field.  Specified field ID */
	static final int GET_FIELD_VALUE_REQUEST = 34;
	
	/** Response from server containing field value for getFieldValue request */
	static final int GET_FIELD_VALUE_REPLY = 35;
	
	/** Request from client to server for SFImage sub value */
	static final int GET_IMAGE_SUB_VALUE_REQUEST = 36;
	
	/** Response from server with image sub data */
	static final int GET_IMAGE_SUB_VALUE_REPLY = 37;
	
	/** Request for the number of field values for a field.  Needed to implement MFField.size nicely */
	static final int GET_NUM_FIELD_VALUES_REQUEST = 38;
	
	/** Response from server for number of field values for a field */
	static final int GET_NUM_FIELD_VALUES_REPLY = 39;
	
	/** Request from client to server for name of server */
	static final int GET_SERVER_NAME_REQUEST = 40;
	
	/** Response from server for name of server */
	static final int GET_SERVER_NAME_REPLY = 41;
	
	/** Request from client to server for version of server */
	static final int GET_SERVER_VERSION_REQUEST = 42;
	
	/** Response from server for version of server */
	static final int GET_SERVER_VERSION_REPLY = 43;
	
	/** Request from client to server for current world URL */
	static final int GET_WORLD_URL_REQUEST = 44;
	
	/** Response from server for current world URL */
	static final int GET_WORLD_URL_REPLY = 45;
	
	/** Request from client to server to perform loadURL action */
	static final int LOAD_URL_REQUEST = 46;
	
	/** Response from server to client about loadURL action results */
	static final int LOAD_URL_REPLY = 47;
	
	/** Request from client to server to remove registered browser listener */
	static final int REMOVE_BROWSER_LISTENER_REQUEST = 48;
	
	/** Response from server concerning removeBrowserListener request */
	static final int REMOVE_BROWSER_LISTENER_REPLY = 49;
	
	/** Request from client to server to remove registered eventOut listener */
	static final int REMOVE_EVENT_OUT_LISTENER_REQUEST = 50;
	
	/** Response from server for removeEventOutListener request */
	static final int REMOVE_EVENT_OUT_LISTENER_REPLY = 51;
	
	/** Request from client to server to remove a route from the scene */
	static final int REMOVE_ROUTE_REQUEST = 52;
	
	/** Reply from server to client concerning removeRoute request */
	static final int REMOVE_ROUTE_REPLY = 53;
	
	/** Request from client to server to do a replaceWorld(Node[]) */
	static final int REPLACE_WORLD_REQUEST = 54;
	
	/** Response from client to server completing replaceWorld call */
	static final int REPLACE_WORLD_REPLY = 55;
	
	/** Request from client to server to change browser description */
	static final int SET_DESCRIPTION_REQUEST = 56;
	    
	/** Response from server reguarding change to browser description */
	static final int SET_DESCRIPTION_REPLY = 57;
	
	/** Request from client to server to perform setFieldValue */
	static final int SET_FIELD_VALUE_REQUEST = 58;
	
	/** Response from server to client about result of setFieldValue */
	static final int SET_FIELD_VALUE_REPLY = 59;

	/** Request from client to server to shutdown system. */
	static final int SHUTDOWN_SYSTEM = 60;
	
	/** Notification from server to client that a registered eventOut has
	 *  changed in value. */
	static final int EVENTOUT_CHANGED_BROADCAST = 70;
	
	/** Notification from server to client that the browser has generated 
	 *  a BrowserListener event */
	static final int BROWSER_CHANGED_BROADCAST = 80;
		
	/** Response type byte for a request which completed without error */
	static final byte RESPONSE_OKAY = 0;
	
	/** Response type byte for a request which encountered a server side error */
	static final byte RESPONSE_ERROR = 1;
	
	/** Response type byte for a request which should generate an exception */
	static final byte RESPONSE_EXCEPTION = 2;
}
