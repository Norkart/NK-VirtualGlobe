
import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserListener;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DScene;
//
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DNode;

/**
 * Base Test Controller functionality
 */
public abstract class tController implements BrowserListener, X3DFieldEventListener {
	
	/** Synchronization variable, indicating the browser setup
	 *  has completed and the event system is ready. */
	boolean browserInitialized;
	
	/** Synchronization variable, indicating the last event
	 *  cascade has completed. */
	boolean eventCascadeOccured;
	
	/** X3D browser instance */
	ExternalBrowser browser;
	
	/** X3D profile info instance */
	ProfileInfo profile;
	
	/** X3D scene instance */
	X3DScene scene;
	
	/** Node used to signal event cascade completion */
	Transform tickle;
	
	/** Exit status */
	static final int ERROR = -1;
	static final int NO_ERROR = 0;
	int exitStatus = NO_ERROR;
	
	/** Setup listeners to control operation sequencing */
	public void initialize( ) {
		//
		// browser - must be initialized by sub class
		this.browser.addBrowserListener( this );
		//
		// scene - must be initialized by sub class
		this.tickle = new Transform( this.scene );
		this.tickle.translation.addX3DEventListener( this );
		this.scene.addRootNode( this.tickle.node ); 
		this.browser.replaceWorld( this.scene ); 
	}
	
	/** X3D browser event listener */
	public void browserChanged( final BrowserEvent be ) {
		if ( be.getID( ) == BrowserEvent.INITIALIZED ) {
			synchronized( this ) {
				logMessage( tMessageType.STATUS, "Browser Initialized" );
				this.browserInitialized = true;
				notifyAll( );
			}
		}
	}
	
	/** X3D field event listener */
	public void readableFieldChanged( final X3DFieldEvent xfe ) {
		if ( xfe.getSource( ).equals( this.tickle.translation ) ) {
			if ( !this.eventCascadeOccured ) {
				synchronized( this ) {
					this.eventCascadeOccured = true;
					notifyAll( );
				}
			}
		}
	}
	
	/** Signal the controller that updates to the browser shall be buffered. */
	public void bufferUpdate( ) {
		this.browser.beginUpdate( );
	}
	
	/** Signal the controller that all pending updates shall be released to
	 *  the browser. This method blocks until notification is received that
	 *  the updates have been processed. */
	public void flushUpdate( ) {
		synchronized( this ) {
			this.eventCascadeOccured = false;
			this.tickle.translation.setValue( new float[]{ 0.0f, 0.0f, 0.0f } );
			this.browser.endUpdate( );
			try { 
				while ( !this.eventCascadeOccured ) { wait( ); } 
				this.eventCascadeOccured = false;
			}
			catch ( InterruptedException ie ) { ; }
		}
	}
	
	/**
	 * Log a message
	 * @param type the type of message
	 * @param message the message <code>String</code>
	 */
	public void logMessage( final tMessageType type, final String message ) {
		logMessage( type, new String[]{ message } );
	}
	
	/**
	 * Log a message with an <code>Exception</code> stack trace
	 * @param type the type of message
	 * @param message the message <code>String</code>
	 * @param e the <code>Exception</code>
	 */
	public void logMessage( final tMessageType type, final String message, final Exception e ) {
		logMessage( type, new String[]{ message }, e );
	}
	
	/**
	 * Log a multi-part message
	 * @param type the type of message
	 * @param message the <code>String</code> array of message segments
	 */
	public void logMessage( final tMessageType type, final String[] message ) {
		logMessage( type, message, null );
	}
	
	/**
	 * Log a multi-part message
	 * @param type the type of message
	 * @param message the <code>String</code> array of message segments
	 */
	public void logMessage( final tMessageType type, final String[] message, final Exception e ) {
		if ( type == tMessageType.ERROR ) { exitStatus = ERROR; }
		System.out.println( type.toString( ) + "\t\t#################################################" );
		for ( int i = 0; i < message.length; i++ ) { System.out.println( message[i] ); }
		if ( e != null ) { e.printStackTrace( System.out ); }
	}
	
	/** An SAI Transform node wrapper. */
	class Transform {
		
		/** The node */
		public final X3DNode node;
		
		/** The metadata exposedField */
		public final SFNode metadata;
		
		/** The children exposedField */
		public final MFNode children;
		
		/** The addChildren eventIn */
		public final MFNode addChildren;
		
		/** The removeChildren eventIn */
		public final MFNode removeChildren;
		
		/** The bboxCenter field */
		public final SFVec3f bboxCenter;
		
		/** The bboxSize field */
		public final SFVec3f bboxSize;
		
		/** The center exposedField */
		public final SFVec3f center;
		
		/** The rotation exposedField */
		public final SFRotation rotation;
		
		/** The scale exposedField */
		public final SFVec3f scale;
		
		/** The scaleOrientation exposedField */
		public final SFRotation scaleOrientation;
		
		/** The translation exposedField */
		public final SFVec3f translation;
		
		/** Constructor */
		public Transform( final X3DScene scene ) {
			node = scene.createNode("Transform");
			metadata=(SFNode)node.getField("metadata");
			children=(MFNode)node.getField("children");
			addChildren=(MFNode)node.getField("addChildren");
			removeChildren=(MFNode)node.getField("removeChildren");
			bboxCenter=(SFVec3f)node.getField("bboxCenter");
			bboxSize=(SFVec3f)node.getField("bboxSize");
			center=(SFVec3f)node.getField("center");
			rotation=(SFRotation)node.getField("rotation");
			scale=(SFVec3f)node.getField("scale");
			scaleOrientation=(SFRotation)node.getField("scaleOrientation");
			translation=(SFVec3f)node.getField("translation");
		}
	}
}

