/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes;

// External imports
import java.lang.ref.WeakReference;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;

import org.j3d.aviatrix3d.Texture;
import org.j3d.aviatrix3d.Texture2D;
import org.j3d.aviatrix3d.Texture3D;

// Local imports
// none

/**
 * A cache for Texture instance management where the objects stay according
 * to Java's WeakReference system.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class TextureCache {

    /** The cache map */
    protected HashMap<String,ArrayList<WeakReference<Texture>>> textureMap;

    /** Enabled flag */
    protected boolean enabled;

    /** The instance */
    protected static TextureCache cache;

    /**
     * Construct a new instance of the empty cache.
     */
    protected TextureCache( ) {
        textureMap = new HashMap<String,ArrayList<WeakReference<Texture>>>( );
        enabled = true;
    }

    /**
     * Return the TextureCache instance
     *
     * @return The TextureCache instance
     */
    public static TextureCache getInstance( ) {
        if ( cache == null ) {
            cache = new TextureCache( );
        }
        return( cache );
    }

    /**
     * Explicitly remove the Texture from the cache. If the object
     * has already been freed, this request is silently ignored.
     *
     * @param tag The texture id, typically it's URL string
     * @param texture The Texture to release
     */
    public synchronized void release( String tag, Texture texture ) {
        if ( enabled && ( tag != null ) && ( texture != null ) ) {
            ArrayList<WeakReference<Texture>> cacheList = textureMap.get( tag );
            if ( cacheList != null ) {
                // the tag exists, walk through the entries looking for a match
                for ( int i = cacheList.size( )-1; i >= 0; i-- ) {
                    WeakReference<Texture> ref = cacheList.get( i );
                    if ( ref == null ) {
                        cacheList.remove( i );
                    } else {
                        Texture cachedTexture = ref.get( );
                        if ( cachedTexture == null ) {
                            cacheList.remove( i );
                        }
                        else if ( isMatch( cachedTexture, texture ) ) {
                            cacheList.remove( i );
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Explicitly remove the Texture from the cache. If the object
     * has already been freed, this request is silently ignored.
     *
     * @param url The URL the texture was registered under
     * @param texture The Texture to release
     */
    public void release( URL url, Texture texture ) {
        if ( url != null ) {
            release( url.toExternalForm( ), texture );
        }
    }

    /**
     * Clear the cache.
     */
    public void clear( ) {
        textureMap.clear( );
    }

    /**
     * Set the enabled state of the cache
     *
     * @param enabled The enabled state of the cache
     */
    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
    }

    /**
     * Register a Texture in the cache with the argument id. If the id is
     * null, the argument Texture is not cached and is returned. If a Texture
     * that matches the id and properties of the argument Texture is
     * already in the cache, it will be returned and the argument Texture
     * will not be stored. Otherwise, the argument Texture is stored and
     * is returned.
     *
     * @param url The texture id
     * @param texture The candidate texture to cache
     * @return The cached Texture
     */
    public Texture register( URL url, Texture texture ) {
        Texture returnTexture = texture;
        if ( url != null ) {
            returnTexture = register( url.toExternalForm( ), texture );
        }
        return( returnTexture );
    }

    /**
     * Register a Texture in the cache with the argument id. If the id is
     * null, the argument Texture is not cached and is returned. If a Texture
     * that matches the id and properties of the argument Texture is
     * already in the cache, it will be returned and the argument Texture
     * will not be stored. Otherwise, the argument Texture is stored and
     * is returned.
     *
     * @param tag The texture id, typically it's URL string
     * @param texture The candidate texture to cache
     * @return The cached Texture
     */
    public synchronized Texture register( String tag, Texture texture ) {

        Texture returnTexture = texture;
        if ( enabled && ( tag != null ) && ( texture != null ) ) {
            ArrayList<WeakReference<Texture>> cacheList = textureMap.get( tag );
            if ( cacheList == null ) {
                // nothing has been cached yet with the argument tag
                cacheList = new ArrayList<WeakReference<Texture>>( 1 );
                cacheList.add( new WeakReference<Texture>( texture ) );
                textureMap.put( tag, cacheList );

            } else {
                // the tag exists, walk through the entries looking for a match
                boolean cacheHit = false;
                for ( int i = cacheList.size( )-1; i >= 0; i-- ) {
                    WeakReference<Texture> ref = cacheList.get( i );
                    if ( ref == null ) {
                        cacheList.remove( i );
                    } else {
                        Texture cachedTexture = ref.get( );
                        if ( cachedTexture == null ) {
                            cacheList.remove( i );
                        }
                        else if ( isMatch( cachedTexture, texture ) ) {
                            returnTexture = cachedTexture;
                            cacheHit = true;
                            break;
                        }
                    }
                }
                if ( !cacheHit ) {
                    cacheList.add( new WeakReference<Texture>( texture ) );
                }
            }
        }
        return( returnTexture );
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------

    /**
     * Compare the properties of the argument Textures for a match
     */
    private boolean isMatch( Texture tex1, Texture tex2 ) {

        boolean match =
            ( tex1.getTextureType( ) == tex2.getTextureType( ) ) &
            ( tex1.getFormat( ) == tex2.getFormat( ) ) &
            ( tex1.getWidth( ) == tex2.getWidth( ) ) &
            ( tex1.getBoundaryModeS( ) == tex2.getBoundaryModeS( ) ) &
            ( tex1.getMinFilter( ) == tex2.getMinFilter( ) ) &
            ( tex1.getMagFilter( ) == tex2.getMagFilter( ) ) &
            //( tex1.getMipMapMode( ) == tex2.getMipMapMode( ) ) &
            ( tex1.getAnisotropicFilterMode( ) == tex2.getAnisotropicFilterMode( ) ) &
            ( tex1.getAnisotropicFilterDegree( ) == tex2.getAnisotropicFilterDegree( ) );

        if ( ( tex1 instanceof Texture2D ) && ( tex2 instanceof Texture2D ) ) {
            Texture2D tex1_2D = (Texture2D)tex1;
            Texture2D tex2_2D = (Texture2D)tex2;
            match &= ( tex1_2D.getHeight( ) == tex2_2D.getHeight( ) ) &
                ( tex1_2D.getBoundaryModeT( ) == tex2_2D.getBoundaryModeT( ) );
        }

        else if ( ( tex1 instanceof Texture3D ) && ( tex2 instanceof Texture3D ) ) {
            Texture3D tex1_3D = (Texture3D)tex1;
            Texture3D tex2_3D = (Texture3D)tex2;
            match &= ( tex1_3D.getHeight( ) == tex2_3D.getHeight( ) ) &
                ( tex1_3D.getBoundaryModeT( ) != tex2_3D.getBoundaryModeT( ) ) &
                ( tex1_3D.getDepth( ) == tex2_3D.getDepth( ) ) &
                ( tex1_3D.getBoundaryModeR( ) == tex2_3D.getBoundaryModeR( ) );
        }
        return( match );
    }
}
