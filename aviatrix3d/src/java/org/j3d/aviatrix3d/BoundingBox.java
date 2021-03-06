/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector4f;

// Local imports
import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * Bounds described as an axis-aligned bounding volume.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.27 $
 */
public class BoundingBox extends BoundingVolume
{
	/** The minimum coordinates of the box */
	private Point3f min;
	
	/**
	 * The maximum coordinates of the box. A point4f is used because
	 * this is also used as a temp during the transformation and we need
	 * to force normal transformations, rather than location transforms.
	 */
	private Point4f max;
	
	/** The center of the box based on the min/max values */
	private float[] center;
	
	/** The size of the box based on the center values */
	private float[] size;
	
	/** The vertices of the box */
	private float[][] vert;
	
	/** The transformed vertices of the box, used in checkIntersectionFrustum */
	private float[][] xvert;
	
	/**
	 * The default constructor with the sphere radius as one and
	 * center at the origin.
	 */
	public BoundingBox()
	{
		min = new Point3f();
		max = new Point4f();
		
		center = new float[3];
		size = new float[3];
		
		vert = new float[8][];
		xvert = new float[8][];
		for ( int i = 0; i < 8; i++ ) 
		{
			vert[i] = new float[3];
			xvert[i] = new float[3];
		}
		
		max.w = 0;
	}
	
	/**
	 * Construct a bounding box with minimum and maximum positions.
	 *
	 * @param min The minimum position of the bounds
	 * @param max The maximum position of the bounds
	 */
	public BoundingBox(float[] min, float[] max)
	{
		this();
		
		this.min.x = min[0];
		this.min.y = min[1];
		this.min.z = min[2];
		
		this.max.x = max[0];
		this.max.y = max[1];
		this.max.z = max[2];
		
		recalcExtents();
	}
	
	//---------------------------------------------------------------
	// Methods defined by BoundingVolume
	//---------------------------------------------------------------
	
	/**
	 * The type of bounds this object represents.
	 *
	 * @return One of the constant types defined
	 */
	public int getType()
	{
		return BOX_BOUNDS;
	}
	
	/**
	 * Get the maximum extents of the bounding volume.
	 *
	 * @param min The minimum position of the bounds
	 * @param max The maximum position of the bounds
	 */
	public void getExtents(float[] min, float[] max)
	{
		min[0] = this.min.x;
		min[1] = this.min.y;
		min[2] = this.min.z;
		
		max[0] = this.max.x;
		max[1] = this.max.y;
		max[2] = this.max.z;
	}
	
	/**
	 * Get the center of the bounding volume.
	 *
	 * @param center The center of the bounds will be copied here
	 */
	public void getCenter(float[] center)
	{
		center[0] = this.center[0];
		center[1] = this.center[1];
		center[2] = this.center[2];
	}
	
	/**
	 * Get the size of the bounding volume.
	 *
	 * @param size The size of the bounds will be copied here
	 */
	public void getSize(float[] size)
	{
		size[0] = this.size[0];
		size[1] = this.size[1];
		size[2] = this.size[2];
	}
	
	/**
	 * Check for the given point lieing inside this bounds.
	 *
	 * @param pos The location of the point to test against
	 * @return true if the point lies inside this bounds
	 */
	public boolean checkIntersectionPoint(float[] pos)
	{
		return pos[0] >= min.x && pos[0] <= max.x &&
			pos[1] >= min.y && pos[1] <= max.y &&
			pos[2] >= min.z && pos[2] <= max.z;
	}
	
	/**
	 * Check for the given ray intersecting this bounds. The line is
	 * described as a starting point and a vector direction.
	 *
	 * @param pos The start location of the ray
	 * @param dir The direction vector of the ray
	 * @return true if the ray intersects this bounds
	 */
	public boolean checkIntersectionRay(float[] pos, float[] dir)
	{
		// This is based on the Graphics Gems code by Andrew Woo.
		// http://www1.acm.org/pubs/tog/GraphicsGems/gems/RayBox.c
		// Since the original code always used fixed-length loops, I've
		// unrolled the loops here and used local variables instead of
		// arrays.
		
		boolean inside = true;
		boolean quadrant_0, quadrant_1, quadrant_2;
		float max_t_x, max_t_y, max_t_z;
		float c_plane_x = 0;
		float c_plane_y = 0;
		float c_plane_z = 0;
		
		// Find candidate planes; Unrlled loop
		if(pos[0] < min.x)
		{
			quadrant_0 = false;
			c_plane_x = min.x;
			inside = false;
		}
		else if(pos[0] > max.x)
		{
			quadrant_0 = false;
			c_plane_x = max.x;
			inside = false;
		}
		else
		{
			quadrant_0 = true;
		}
		
		if(pos[1] < min.y)
		{
			quadrant_1 = false;
			c_plane_y = min.y;
			inside = false;
		}
		else if(pos[1] > max.y)
		{
			quadrant_1 = false;
			c_plane_y = max.y;
			inside = false;
		}
		else
		{
			quadrant_1 = true;
		}
		
		if(pos[2] < min.z)
		{
			quadrant_2 = false;
			c_plane_z = min.z;
			inside = false;
		}
		else if(pos[2] > max.z)
		{
			quadrant_2 = false;
			c_plane_z = max.z;
			inside = false;
		}
		else
		{
			quadrant_2 = true;
		}
		
		// Ray origin inside bounding box - exit now.
		if(inside)
			return true;
		
		// Calculate T distances to candidate planes
		if(!quadrant_0 && dir[0] != 0)
			max_t_x = (c_plane_x - pos[0]) / dir[0];
		else
			max_t_x = -1;
		
		if(!quadrant_1 && dir[1] != 0)
			max_t_y = (c_plane_y - pos[1]) / dir[1];
		else
			max_t_y = -1;
		
		if(!quadrant_2 && dir[2] != 0)
			max_t_z = (c_plane_z - pos[2]) / dir[2];
		else
			max_t_z = -1;
		
		// Get largest of the max_t's for final choice of intersection
		float max_t = max_t_x;
		int plane = 0;
		
		if(max_t < max_t_y)
		{
			plane = 1;
			max_t = max_t_y;
		}
		
		if(max_t < max_t_z)
		{
			plane = 2;
			max_t = max_t_z;
		}
		
		// Check final candidate actually inside box
		boolean intersect = true;
		
		if(max_t < 0)
			intersect = false;
		else
		{
			if(plane != 0)
			{
				float coord = pos[0] + max_t * dir[0];
				
				if(coord < min.x || coord > max.x)
					intersect = false;
			}
			
			if(plane != 1 && intersect)
			{
				float coord = pos[1] + max_t * dir[1];
				
				if(coord < min.y || coord > max.y)
					intersect = false;
			}
			
			if(plane != 2 && intersect)
			{
				float coord = pos[2] + max_t * dir[2];
				
				if(coord < min.z || coord > max.z)
					intersect = false;
			}
		}
		
		return intersect;              /* ray hits box */
	}
	
	/**
	 * Check for the given sphere intersecting this bounds. The sphere is
	 * described by a centre location and the radius.
	 *
	 * @param center The location of the sphere's center
	 * @param radius The radius of the sphere
	 * @return true if the sphere intersects this bounds
	 */
	public boolean checkIntersectionSphere(float[] center, float radius)
	{
		float r2 = radius * radius;
		float d_min = 0;
		
		if(center[0] < min.x)
		{
			float d = center[0] - min.x;
			d_min += d * d;
		}
		else if(center[0] > max.x)
		{
			float d = center[0] - max.x;
			d_min += d * d;
		}
		
		if(center[1] < min.y)
		{
			float d = center[1] - min.y;
			d_min += d * d;
		}
		else if(center[1] > max.y)
		{
			float d = center[1] - max.y;
			d_min += d * d;
		}
		
		if(center[2] < min.z)
		{
			float d = center[2] - min.z;
			d_min += d * d;
		}
		else if(center[2] > max.z)
		{
			float d = center[2] - max.z;
			d_min += d * d;
		}
		
		return d_min <= r2;
	}
	
	/**
	 * Check for the given cylinder segment intersecting this bounds. The
	 * cylinder is described by a centre location, axial direction and the
	 * radius.
	 *
	 * @param center The location of the cylinder's center
	 * @param direction A unit vector indicating the axial direction
	 * @param radius The radius of the cylinder
	 * @param height The half-height of the cylinder from the center point
	 * @return true if the sphere intersects this bounds
	 */
	public boolean checkIntersectionCylinder(float[] center,
		float[] direction,
		float radius,
		float height)
	{
		return false;
	}
	
	/**
	 * Check for the given cone intersecting this bounds. The
	 * cone is described by the location of the vertex, a direction vector
	 * and the spread angle of the cone.
	 *
	 * @param vertex The location of the cone's vertex
	 * @param direction A unit vector indicating the axial direction
	 * @param angle The spread angle of the cone
	 * @return true if the sphere intersects this bounds
	 */
	public boolean checkIntersectionCone(float[] vertex,
		float[] direction,
		float angle)
	{
		return false;
	}
	
	/**
	 * Check for the given AA box intersecting this bounds. The box is
	 * described by the minimum and maximum extents on each axis.
	 *
	 * @param minExtents The minimum extent value on each axis
	 * @param maxExtents The maximum extent value on each axis
	 * @return true if the box intersects this bounds
	 */
	public boolean checkIntersectionBox(float[] minExtents, float[] maxExtents)
	{
		// a.min <= b.max && a.max >= b.min
		return minExtents[0] <= max.x && minExtents[1] <= max.y &&
			minExtents[2] <= max.z && maxExtents[0] >= min.x &&
			maxExtents[1] >= min.y && maxExtents[2] >= min.z;
	}
	
	/**
	 * Check whether this volume intersects with the view frustum.
	 *
	 * @param planes The 6 planes of the frustum
	 * @param mat The vworld to local transformation matrix
	 * @return int FRUSTUM_ALLOUT, FRUSTUM_ALLIN, FRUSTUM_PARTIAL.
	 */
	public int checkIntersectionFrustum(Vector4f[] planes, Matrix4d mat)
	{
		// transform the vertices of the bounding box
		// for comparison with the view frustum
		double x;
		double y;
		double z;
		
		for (int i = 0; i < 8; i++) 
		{
			x = vert[i][0];
			y = vert[i][1];
			z = vert[i][2];
			
			xvert[i][0] = (float)(mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03);
			xvert[i][1] = (float)(mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13);
			xvert[i][2] = (float)(mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23);
		}
		
		int cnt;
		int cnt2 = 0;
		
		for(int p = 0; p < 6; p++)
		{
			cnt = 0;
			if(planes[p].x * xvert[0][0] + planes[p].y * xvert[0][1] +
				planes[p].z * xvert[0][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[1][0] + planes[p].y * xvert[1][1] +
				planes[p].z * xvert[1][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[2][0] + planes[p].y * xvert[2][1] +
				planes[p].z * xvert[2][2] + planes[p].w > 0 )
				cnt++;
			 
			if(planes[p].x * xvert[3][0] + planes[p].y * xvert[3][1] +
				planes[p].z * xvert[3][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[4][0] + planes[p].y * xvert[4][1] +
				planes[p].z * xvert[4][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[5][0] + planes[p].y * xvert[5][1] +
				planes[p].z * xvert[5][2] + planes[p].w > 0 )
				cnt++;
			 
			if(planes[p].x * xvert[6][0] + planes[p].y * xvert[6][1] +
				planes[p].z * xvert[6][2] + planes[p].w > 0 )
				cnt++;
			 
			if(planes[p].x * xvert[7][0] + planes[p].y * xvert[7][1] +
				planes[p].z * xvert[7][2] + planes[p].w > 0 )
				cnt++;
			 
			if(cnt == 0)
			{
				return FRUSTUM_ALLOUT;
			}
			
			if(cnt == 8)
				cnt2++;
		}
		
		return (cnt2 == 6) ? FRUSTUM_ALLIN : FRUSTUM_PARTIAL;
	}
	/*
	public int checkIntersectionFrustum(Vector4f[] planes, Matrix4d mat)
	{
		double min_x;
		double min_y;
		double min_z;
		
		double max_x;
		double max_y;
		double max_z;
		
		double a = center[0];
		double b = center[1];
		double c = center[2];
		
		min_x = mat.m00 * a + mat.m01 * b + mat.m02 * c + mat.m03;
		min_y = mat.m10 * a + mat.m11 * b + mat.m12 * c + mat.m13;
		min_z = mat.m20 * a + mat.m21 * b + mat.m22 * c + mat.m23;
		
		a = size[0];
		b = size[1];
		c = size[2];
		
		max_x = mat.m00 * a + mat.m01 * b + mat.m02 * c;
		max_y = mat.m10 * a + mat.m11 * b + mat.m12 * c;
		max_z = mat.m20 * a + mat.m21 * b + mat.m22 * c;
		
		// Inlined Math.abs of sizes
		double sizex = 0;
		double sizey = 0;
		double sizez = 0;
		
		if (max_x < 0)
			sizex = -max_x;
		else
			sizex = max_x;
		
		if (max_y < 0)
			sizey = -max_y;
		else
			sizey = max_y;
		
		if (max_z < 0)
			sizez = -max_z;
		else
			sizez = max_z;
		
		int cnt;
		int cnt2 = 0;
		
		for(int p = 0; p < 6; p++)
		{
			cnt = 0;
			if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y - sizey) +
				planes[p].z * (min_z - sizez) + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y - sizey) +
				planes[p].z * (min_z - sizez) + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y + sizey) +
				planes[p].z * (min_z - sizez) + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y + sizey) +
				planes[p].z * (min_z - sizez) + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y - sizey) +
				planes[p].z * (min_z + sizez) + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y - sizey) +
				planes[p].z * (min_z + sizez) + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y + sizey) +
				planes[p].z * (min_z + sizez) + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y + sizey) +
				planes[p].z * (min_z + sizez) + planes[p].w > 0 )
				cnt++;
			
			if(cnt == 0)
			{
				return FRUSTUM_ALLOUT;
			}
			
			if(cnt == 8)
				cnt2++;
		}
		
		return (cnt2 == 6) ? FRUSTUM_ALLIN : FRUSTUM_PARTIAL;
	}
	*/
	
	/**
	 * Check whether this volume intersects with the view frustum.
	 *
	 * @param planes The 6 planes of the frustum
	 * @param mat The vworld to local transformation matrix
	 * @return int FRUSTUM_ALLOUT, FRUSTUM_ALLIN, FRUSTUM_PARTIAL.
	 */
	public int checkIntersectionFrustum(Vector4f[] planes, Matrix4f mat)
	{
		// transform the vertices of the bounding box
		// for comparison with the view frustum
		float x;
		float y;
		float z;
		
		for (int i = 0; i < 8; i++) 
		{
			x = vert[i][0];
			y = vert[i][1];
			z = vert[i][2];
			
			xvert[i][0] = mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03;
			xvert[i][1] = mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13;
			xvert[i][2] = mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23;
		}
		
		int cnt;
		int cnt2 = 0;
		
		for(int p = 0; p < 6; p++)
		{
			cnt = 0;
			if(planes[p].x * xvert[0][0] + planes[p].y * xvert[0][1] +
				planes[p].z * xvert[0][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[1][0] + planes[p].y * xvert[1][1] +
				planes[p].z * xvert[1][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[2][0] + planes[p].y * xvert[2][1] +
				planes[p].z * xvert[2][2] + planes[p].w > 0 )
				cnt++;
			 
			if(planes[p].x * xvert[3][0] + planes[p].y * xvert[3][1] +
				planes[p].z * xvert[3][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[4][0] + planes[p].y * xvert[4][1] +
				planes[p].z * xvert[4][2] + planes[p].w > 0 )
				cnt++;
			
			if(planes[p].x * xvert[5][0] + planes[p].y * xvert[5][1] +
				planes[p].z * xvert[5][2] + planes[p].w > 0 )
				cnt++;
			 
			if(planes[p].x * xvert[6][0] + planes[p].y * xvert[6][1] +
				planes[p].z * xvert[6][2] + planes[p].w > 0 )
				cnt++;
			 
			if(planes[p].x * xvert[7][0] + planes[p].y * xvert[7][1] +
				planes[p].z * xvert[7][2] + planes[p].w > 0 )
				cnt++;
			 
			if(cnt == 0)
			{
				return FRUSTUM_ALLOUT;
			}
			
			if(cnt == 8)
				cnt2++;
		}
		
		return (cnt2 == 6) ? FRUSTUM_ALLIN : FRUSTUM_PARTIAL;
	}
	/*
	public int checkIntersectionFrustum(Vector4f[] planes, Matrix4f mat)
	{
        float min_x;
        float min_y;
        float min_z;

        float max_x;
        float max_y;
        float max_z;

        float a = center[0];
        float b = center[1];
        float c = center[2];

        min_x = mat.m00 * a + mat.m01 * b + mat.m02 * c + mat.m03;
        min_y = mat.m10 * a + mat.m11 * b + mat.m12 * c + mat.m13;
        min_z = mat.m20 * a + mat.m21 * b + mat.m22 * c + mat.m23;

        a = size[0];
        b = size[1];
        c = size[2];

        max_x = mat.m00 * a + mat.m01 * b + mat.m02 * c;
        max_y = mat.m10 * a + mat.m11 * b + mat.m12 * c;
        max_z = mat.m20 * a + mat.m21 * b + mat.m22 * c;

        // Inlined Math.abs of sizes
        float sizex = 0;
        float sizey = 0;
        float sizez = 0;

        if (max_x < 0)
            sizex = -max_x;
        else
            sizex = max_x;

        if (max_y < 0)
            sizey = -max_y;
        else
            sizey = max_y;

        if (max_z < 0)
            sizez = -max_z;
        else
            sizez = max_z;

        int cnt;
        int cnt2 = 0;

        for(int p = 0; p < 6; p++)
        {
            cnt = 0;
            if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y - sizey) +
               planes[p].z * (min_z - sizez) + planes[p].w > 0 )
                cnt++;

            if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y - sizey) +
               planes[p].z * (min_z - sizez) + planes[p].w > 0 )
                cnt++;

            if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y + sizey) +
               planes[p].z * (min_z - sizez) + planes[p].w > 0 )
                cnt++;

            if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y + sizey) +
               planes[p].z * (min_z - sizez) + planes[p].w > 0 )
                cnt++;

            if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y - sizey) +
               planes[p].z * (min_z + sizez) + planes[p].w > 0 )
                cnt++;

            if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y - sizey) +
               planes[p].z * (min_z + sizez) + planes[p].w > 0 )
                cnt++;

            if(planes[p].x * (min_x - sizex) + planes[p].y * (min_y + sizey) +
               planes[p].z * (min_z + sizez) + planes[p].w > 0 )
                cnt++;

            if(planes[p].x * (min_x + sizex) + planes[p].y * (min_y + sizey) +
               planes[p].z * (min_z + sizez) + planes[p].w > 0 )
                cnt++;

            if(cnt == 0)
            {
                return FRUSTUM_ALLOUT;
            }

            if(cnt == 8)
                cnt2++;
        }

        return (cnt2 == 6) ? FRUSTUM_ALLIN : FRUSTUM_PARTIAL;
    }
	*/
	/**
	 * Check for the given line segment intersecting this bounds. The line is
	 * described as the line connecting the start and end points.
	 *
	 * @param start The start location of the segment
	 * @param end The start location of the segment
	 * @return true if the segment intersects this bounds
	 */
	public boolean checkIntersectionSegment(float[] start,
		float[] end)
		
	{
		// Implemented using the Box Overlap test
		// find the centerpoint of the line and direction. Translate
		// the center to the origin of the bounding box.
		
		float c_x = (end[0] + start[0]) * 0.5f - center[0];
		float c_y = (end[1] + start[1]) * 0.5f - center[1];
		float c_z = (end[2] + start[2]) * 0.5f - center[2];
		
		float w_x = c_x - (start[0] - center[0]);
		float w_y = c_y - (start[1] - center[1]);
		float w_z = c_z - (start[2] - center[2]);
		
		float v_x = w_x > 0 ? w_x : -w_x;
		float v_y = w_y > 0 ? w_y : -w_y;
		float v_z = w_z > 0 ? w_z : -w_z;
		
		float h_x = size[0];
		float h_y = size[1];
		float h_z = size[2];
		
		if((Math.abs(c_x) > v_x + h_x) ||
			(Math.abs(c_y) > v_y + h_y) ||
			(Math.abs(c_z) > v_z + h_z) ||
			(Math.abs(c_y * w_z - c_z * w_y) > h_y * v_z + h_z * v_y) ||
			(Math.abs(c_x * w_z - c_z * w_x) > h_x * v_z + h_z * v_x) ||
			(Math.abs(c_x * w_y - c_y * w_x) > h_x * v_y + h_y * v_x))
			
			return false;
		
		return true;
	}
	
	/**
	 * Transform the current postion by the given transformation matrix.
	 *
	 * @param mat The matrix to transform this bounds by
	 */
	public void transform(Matrix4d mat)
	{
		min.x = center[0];
		min.y = center[1];
		min.z = center[2];
		
		max.x = size[0];
		max.y = size[1];
		max.z = size[2];
		
		mat.transform(min);
		mat.transform(max);
		
		center[0] = min.x;
		center[1] = min.y;
		center[2] = min.z;
		
		size[0] = Math.abs(max.x);
		size[1] = Math.abs(max.y);
		size[2] = Math.abs(max.z);
		
		min.x = center[0] - size[0];
		min.y = center[1] - size[1];
		min.z = center[2] - size[2];
		
		max.x = center[0] + size[0];
		max.y = center[1] + size[1];
		max.z = center[2] + size[2];
	}
	
	/**
	 * Transform the current postion by the given transformation matrix.
	 *
	 * @param mat The matrix to transform this bounds by
	 */
	public void transform(Matrix4f mat)
	{
		min.x = center[0];
		min.y = center[1];
		min.z = center[2];
		
		max.x = size[0];
		max.y = size[1];
		max.z = size[2];
		
		mat.transform(min);
		mat.transform(max);
		
		center[0] = min.x;
		center[1] = min.y;
		center[2] = min.z;
		
		size[0] = Math.abs(max.x);
		size[1] = Math.abs(max.y);
		size[2] = Math.abs(max.z);
		
		min.x = center[0] - size[0];
		min.y = center[1] - size[1];
		min.z = center[2] - size[2];
		
		max.x = center[0] + size[0];
		max.y = center[1] + size[1];
		max.z = center[2] + size[2];
	}
	
	//---------------------------------------------------------------
	// Local methods
	//---------------------------------------------------------------
	
	/**
	 * Set both of the bounds of the box.
	 *
	 * @param min The minimum position of the bounds
	 * @param max The maximum position of the bounds
	 */
	public void setBounds(float[] min, float[] max)
	{
		this.min.x = min[0];
		this.min.y = min[1];
		this.min.z = min[2];
		
		this.max.x = max[0];
		this.max.y = max[1];
		this.max.z = max[2];
		
		recalcExtents();
	}
	
	/**
	 * Set the minimum bounds for the box.
	 *
	 * @param pos The new position of the box to be used
	 */
	public void setMinimum(float[] pos)
	{
		min.x = pos[0];
		min.y = pos[1];
		min.z = pos[2];
		
		recalcExtents();
	}
	
	/**
	 * Set the minimum bounds for the box.
	 *
	 * @param x The x component of the minimum position
	 * @param y The y component of the minimum position
	 * @param z The z component of the minimum position
	 */
	public void setMinimum(float x, float y, float z)
	{
		min.x = x;
		min.y = y;
		min.z = z;
		
		recalcExtents();
	}
	
	/**
	 * Get the minimum bounds position of the box.
	 *
	 * @param pos The position to copy the values into
	 */
	public void getMinimum(float[] pos)
	{
		pos[0] = min.x;
		pos[1] = min.y;
		pos[2] = min.z;
	}
	
	/**
	 * Set the maximum bounds for the box.
	 *
	 * @param pos The new position of the center of the sphere to be used
	 */
	public void setMaximum(float[] pos)
	{
		max.x = pos[0];
		max.y = pos[1];
		max.z = pos[2];
		
		recalcExtents();
	}
	
	/**
	 * Set the maximum bounds for the box.
	 *
	 * @param x The x component of the minimum position
	 * @param y The y component of the minimum position
	 * @param z The z component of the minimum position
	 */
	public void setMaximum(float x, float y, float z)
	{
		max.x = x;
		max.y = y;
		max.z = z;
		
		recalcExtents();
	}
	
	/**
	 * Get the maximum bounds position of the box.
	 *
	 * @param pos The position to copy the values axto
	 */
	public void getMaximum(float[] pos)
	{
		pos[0] = max.x;
		pos[1] = max.y;
		pos[2] = max.z;
	}
	
	/**
	 * Generate a string representation of this box.
	 *
	 * @return A string representing the bounds information
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer("Bounding Box: min(");
		buf.append(min.x);
		buf.append(' ');
		buf.append(min.y);
		buf.append(' ');
		buf.append(min.z);
		buf.append(") max(");
		buf.append(max.x);
		buf.append(' ');
		buf.append(max.y);
		buf.append(' ');
		buf.append(max.z);
		buf.append(")");
		
		return buf.toString();
	}
	
	/**
	 * Update the extents, center and vertices of the box based on the
	 * current min and max positions.
	 */
	private void recalcExtents()
	{
		center[0] = (max.x + min.x) * 0.5f;
		center[1] = (max.y + min.y) * 0.5f;
		center[2] = (max.z + min.z) * 0.5f;
		size[0] = (max.x - min.x) * 0.5f;
		size[1] = (max.y - min.y) * 0.5f;
		size[2] = (max.z - min.z) * 0.5f;
		
		if(size[0] < 0)
			size[0] = -size[0];
		
		if(size[1] < 0)
			size[1] = -size[1];
		
		if(size[2] < 0)
			size[2] = -size[2];
		
		vert[0][0] = max.x; vert[0][1] = max.y; vert[0][2] = max.z;
		vert[1][0] = max.x; vert[1][1] = max.y; vert[1][2] = min.z;
		vert[2][0] = max.x; vert[2][1] = min.y; vert[2][2] = max.z;
		vert[3][0] = max.x; vert[3][1] = min.y; vert[3][2] = min.z;
		vert[4][0] = min.x; vert[4][1] = max.y; vert[4][2] = max.z;
		vert[5][0] = min.x; vert[5][1] = max.y; vert[5][2] = min.z;
		vert[6][0] = min.x; vert[6][1] = min.y; vert[6][2] = max.z;
		vert[7][0] = min.x; vert[7][1] = min.y; vert[7][2] = min.z;
	}
}
