/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Transparency;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.*;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.j3d.util.ImageUtils;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DException;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;
import org.web3d.x3d.sai.SFImage;

// Local imports
// none

/**
 * A testcase for SFImage that will exercise setValue(), setImage(), getImage() and setSubImage().
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SFImageTest3 extends JFrame implements ActionListener, ChangeListener {
	
	Container contentPane;
	
	JButton readButton;
	JButton writeButton;
	JButton subButton;
	JButton loadButton;
	
	SpinnerNumberModel model;
	JSpinner spinner;
	
	X3DComponent x3dComponent;
	ExternalBrowser browser;
	SFImage sfimage;
	
	boolean useImageIO = true;
	RenderedImage image;
	
	File lastDirectory;
	
	public SFImageTest3( ) {
		super( "SFImageTest3" );
		
		System.setProperty( "x3d.sai.factory.class", 
			"org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl" );
		
		contentPane = this.getContentPane( );
		contentPane.setLayout( new BorderLayout( ) );
		
		createBrowser( );
		loadScene( );
		setPixelTexture( 1 );
		
		contentPane.add( (Component)x3dComponent, BorderLayout.CENTER );
		
		JPanel buttonPanel = new JPanel( );
		buttonPanel.setLayout( new GridLayout( 1, 4, 1, 1 ) );
		
		model = new SpinnerNumberModel( 1, 1, 4, 1 );
		spinner = new JSpinner( model );
		spinner.addChangeListener( this );
		spinner.setToolTipText( 
			"Set the texture of the Box to a 4x4xN texture with N = to the "+
			"number of components specified by the spinner value" );
		buttonPanel.add( spinner );
		
		readButton = new JButton( "Read" );
		readButton.addActionListener( this );
		readButton.setToolTipText( 
			"Read the texture image into a BufferedImage object and print " +
			"it's characteristics to System.out" );
		buttonPanel.add( readButton );
		
		writeButton = new JButton( "Write" );
		writeButton.addActionListener( this );
		writeButton.setToolTipText( 
			"Set the last read texture image to the Box. If a texture " +
			"has not been read yet, this will do nothing. " );
		buttonPanel.add( writeButton );
		
		subButton = new JButton( "Sub" );
		subButton.addActionListener( this );
		subButton.setToolTipText( 
			"Copy the top left quarter of the current texture into the center of the Box" );
		buttonPanel.add( subButton );
		
		loadButton = new JButton( "Load" );
		loadButton.addActionListener( this );
		loadButton.setToolTipText( 
			"Pop a file chooser dialog to select an image to load." );
		buttonPanel.add( loadButton );
		
		contentPane.add( buttonPanel, BorderLayout.SOUTH );
	}
	
	//---------------------------------------------------------
	// Local Methods
	//---------------------------------------------------------
	
	public static void main( String[] args ) {
		SFImageTest3 frame = new SFImageTest3( );
		frame.pack( );
		frame.setSize( 512, 512 );
		frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
		frame.setVisible( true );
	}
	
	private void createBrowser( ) {
		HashMap params = new HashMap( );
		params.put( "Xj3D_ShowConsole", Boolean.TRUE );
		params.put( "Xj3D_NavbarShown", Boolean.FALSE );
		params.put( "Xj3D_StatusBarShown", Boolean.FALSE );
		params.put( "Xj3D_FPSShown", Boolean.FALSE );
		params.put( "Xj3D_NavbarPosition", "bottom" );
		params.put( "Xj3D_LocationShown", Boolean.FALSE );
		params.put( "Xj3D_LocationPosition", "top" );
		//params.put( "Xj3D_ContentDirectory", System.getProperty( "user.dir" ) );
		params.put( "Xj3D_OpenButtonShown", Boolean.FALSE );
		params.put( "Xj3D_ReloadButtonShown", Boolean.FALSE );
		
		x3dComponent = BrowserFactory.createX3DComponent( params );
		browser = x3dComponent.getBrowser( );
	}
	
	private void loadScene( ) {
		X3DScene scene = null;
		try {
			File file = new File( System.getProperty( "user.dir" ) + "/PixTex.x3dv" );
			scene = browser.createX3DFromURL( new String[]{ file.toURL( ).toExternalForm( ) } );
			X3DNode pixelTex = scene.getNamedNode( "pixelTex" );
			sfimage = (SFImage)pixelTex.getField( "image" );
		}
		catch ( Exception e ) {
			System.out.println( e.getMessage( ) );
		}
		
		browser.replaceWorld( scene );
	}
	
	public void actionPerformed( ActionEvent ae ) {
		Object source = ae.getSource( );
		if ( source == readButton ) {
			image = sfimage.getImage( );
			if ( image instanceof BufferedImage ) {
				reportBufferedImageInfo( (BufferedImage)image );
			}
			else {
				System.out.println( "Image = " + image );
			}
		}
		else if ( source == writeButton ) {
			if ( image != null ) {
				System.out.println( "Setting image " + image );
				sfimage.setImage( image );
			}
		}
		else if ( source == subButton ) {
			image = sfimage.getImage( );
			int width = sfimage.getWidth( );
			int height = sfimage.getHeight( );
			System.out.println( "Setting sub-image " + image );
			sfimage.setSubImage( image, width/2, height/2, 0, 0, width/4, height/4 );
		}
		else if ( source == loadButton ) {
			JFileChooser chooser = new JFileChooser();
			if ( lastDirectory == null ) {
				chooser.setCurrentDirectory( new File( System.getProperty( "user.dir" ) ) );
			} else {
				chooser.setCurrentDirectory( lastDirectory );
			}
			
			int returnVal = chooser.showOpenDialog( this );
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				lastDirectory = chooser.getCurrentDirectory( );
				File image_file = chooser.getSelectedFile( );
				BufferedImage image = null;
				try {
					if ( useImageIO ) {
						InputStream in = image_file.toURL( ).openStream( );
						image = ImageIO.read( in );
					} else {
						Object content = image_file.toURL( ).openConnection( ).getContent( );
						
						if ( content instanceof BufferedImage ) {
							image = (BufferedImage)content;
						}
						else if ( content instanceof ImageProducer ) {
							image = ImageUtils.createBufferedImage((ImageProducer)content);
						}
					}
					System.out.println( "Setting image " + image );
					sfimage.setImage( image );
				}
				catch ( IOException ioe ) {
					System.out.println( ioe.getMessage( ) );
				}
			}
		}
	}
	
	/** Change the pixel texture to have the number of components from the spinner */
	public void stateChanged( ChangeEvent ce ) {
		setPixelTexture( model.getNumber( ).intValue( ) );
	}
	
	/** Set the pixel texture to have the specified number of components */
	void setPixelTexture( int i ) {
		switch( i ) {
		case 1:
			//pixelTex.image.setValue( 2, 2, 1, new int[]{ 0xFF, 0xCF, 0x7F, 0x3F } );
			
			//pixelTex.image.setValue( 3, 3, 1, 
			//	new int[]{
			//		0xFF, 0xBF, 0x7F,
			//		0xEF, 0xAF, 0x6F,
			//		0xDF, 0x9F, 0x5F,
			//		} 
			//	);
			
			sfimage.setValue( 4, 4, 1, 
				new int[]{
					0xFF, 0xBF, 0x7F, 0x3F,
					0xEF, 0xAF, 0x6F, 0x2F,
					0xDF, 0x9F, 0x5F, 0x1F,
					0xCF, 0x8F, 0x4F, 0x0F,
				} );
			break;
		case 2:
			//sfimage.setValue( 2, 2, 2, new int[]{ 0xFFFF, 0xFFCF, 0xFF7F, 0xFF3F } );
			sfimage.setValue( 4, 4, 2, 
				new int[] {
					0xFFFF, 0xBFFF, 0x7FFF, 0x3FFF,
					0xFFCF, 0xBFCF, 0x7FCF, 0x3FCF,
					0xFF7F, 0xBF7F, 0x7F7F, 0x3F7F,
					0xFF3F, 0xBF3F, 0x7F3F, 0x3F3F,
				} );
			break;
		case 3:
			//sfimage.setValue( 2, 2, 3, new int[]{ 0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF } );
			sfimage.setValue( 4, 4, 3, 
				new int[] { 
					0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF,
					0xBFBFBF, 0xBF0000, 0x00BF00, 0x0000BF,
					0x7F7F7F, 0x7F0000, 0x007F00, 0x00007F,
					0x3F3F3F, 0x3F0000, 0x003F00, 0x00003F, 
				} );
			break;
		case 4:
			//sfimage.setValue( 2, 2, 4, new int[]{ 0xFFFFFF00, 0xFF00007F, 0x00FF007F, 0x0000FF7F } );
			sfimage.setValue( 4, 4, 4, 
				new int[] { 
					0xFFFFFFFF, 0xFF0000FF, 0x00FF00FF, 0x0000FFFF,
					0xBFBFBFBF, 0xBF0000BF, 0x00BF00BF, 0x0000BFBF,
					0x7F7F7F7F, 0x7F00007F, 0x007F007F, 0x00007F7F,
					0x3F3F3F3F, 0x3F00003F, 0x003F003F, 0x00003F3F, 
				} );
			break;
		}
	}
	
	static void reportBufferedImageInfo( BufferedImage image ) {
		switch ( image.getType( ) ) {
		case BufferedImage.TYPE_3BYTE_BGR:
			//Represents an image with 8-bit RGB color components, corresponding to a Windows-style BGR color model) with the colors Blue, Green, and Red stored in 3 bytes.
			System.out.println( "BufferedImage.ImageType = TYPE_3BYTE_BGR" );
			break;
		case BufferedImage.TYPE_4BYTE_ABGR:
			//Represents an image with 8-bit RGBA color components with the colors Blue, Green, and Red stored in 3 bytes and 1 byte of alpha.
			System.out.println( "BufferedImage.ImageType = TYPE_4BYTE_ABGR" );
			break;
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
			//Represents an image with 8-bit RGBA color components with the colors Blue, Green, and Red stored in 3 bytes and 1 byte of alpha.
			System.out.println( "BufferedImage.ImageType = TYPE_4BYTE_ABGR_PRE" );
			break;
		case BufferedImage.TYPE_BYTE_BINARY:
			//Represents an opaque byte-packed 1, 2, or 4 bit image.
			System.out.println( "BufferedImage.ImageType = TYPE_BYTE_BINARY" );
			break;
		case BufferedImage.TYPE_BYTE_GRAY:
			//Represents a unsigned byte grayscale image, non-indexed.
			System.out.println( "BufferedImage.ImageType = TYPE_BYTE_GRAY" );
			break;
		case BufferedImage.TYPE_BYTE_INDEXED:
			//Represents an indexed byte image.
			System.out.println( "BufferedImage.ImageType = TYPE_BYTE_INDEXED" );
			break;
		case BufferedImage.TYPE_CUSTOM:
			//Image type is not recognized so it must be a customized image.
			System.out.println( "BufferedImage.ImageType = TYPE_CUSTOM" );
			break;
		case BufferedImage.TYPE_INT_ARGB:
			//Represents an image with 8-bit RGBA color components packed into integer pixels.
			System.out.println( "BufferedImage.ImageType = TYPE_INT_ARGB" );
			break;
		case BufferedImage.TYPE_INT_ARGB_PRE:
			//Represents an image with 8-bit RGBA color components packed into integer pixels.
			System.out.println( "BufferedImage.ImageType = TYPE_INT_ARGB_PRE" );
			break;
		case BufferedImage.TYPE_INT_BGR:
			//Represents an image with 8-bit RGB color components, corresponding to a Windows- or Solaris- style BGR color model, with the colors Blue, Green, and Red packed into integer pixels.
			System.out.println( "BufferedImage.ImageType = TYPE_INT_BGR" );
			break;
		case BufferedImage.TYPE_INT_RGB:
			//Represents an image with 8-bit RGB color components packed into integer pixels.
			System.out.println( "BufferedImage.ImageType = TYPE_INT_RGB" );
			break;
		case BufferedImage.TYPE_USHORT_555_RGB:
			//Represents an image with 5-5-5 RGB color components (5-bits red, 5-bits green, 5-bits blue) with no alpha.
			System.out.println( "BufferedImage.ImageType = TYPE_USHORT_555_RGB" );
			break;
		case BufferedImage.TYPE_USHORT_565_RGB:
			//Represents an image with 5-6-5 RGB color components (5-bits red, 6-bits green, 5-bits blue) with no alpha.
			System.out.println( "BufferedImage.ImageType = TYPE_USHORT_565_RGB" );
			break;
		case BufferedImage.TYPE_USHORT_GRAY:
			//Represents an unsigned short grayscale image, non-indexed).
			System.out.println( "BufferedImage.ImageType = TYPE_USHORT_GRAY" );
			break;
		}
		ColorModel colorModel = image.getColorModel( );
		System.out.println( colorModel );
		int num_cmp = colorModel.getNumComponents( );
		System.out.println( "NumComponents = " + num_cmp );
		System.out.println( "NumColorComponents = " + colorModel.getNumColorComponents( ) );
		switch ( colorModel.getTransparency( ) ) {
		case Transparency.BITMASK:
			//Represents image data that is guaranteed to be either completely opaque, with an alpha value of 1.0, or completely transparent, with an alpha value of 0.0.
			System.out.println( "Transparency = BITMASK" );
			break;
		case Transparency.OPAQUE:
			//Represents image data that is guaranteed to be completely opaque, meaning that all pixels have an alpha value of 1.0.
			System.out.println( "Transparency = OPAQUE" );
			break;
		case Transparency.TRANSLUCENT:
			//Represents image data that contains or might contain arbitrary alpha values between and including 0.0 and 1.0.
			System.out.println( "Transparency = TRANSLUCENT" );
			break;
		}
	}
}

