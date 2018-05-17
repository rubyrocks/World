import java.awt.*;

public class point
{
	float x=0.0f,y=0.0f,z=0.0f;
	
	point()
	{
	}
	
	point (float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	void set (float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	void set (point p)
	{
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}
	
	void add (float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	void add (point p)
	{
		this.x += p.x;
		this.y += p.y;
		this.z += p.z;
	}

	void mult ( float f)
	{
		this.x *= f;
		this.y *= f;
		this.z *= f;
	}
	
	double dist2( point p )
	{
		return (double) Math.pow(this.x-p.x,2.0) + Math.pow(this.y-p.y,2.0) + Math.pow(this.z-p.z,2.0);
	}

	double dist2( float x, float y, float z )
	{
		return (double) Math.pow(this.x-x,2.0) + Math.pow(this.y-y,2.0) + Math.pow(this.z-z,2.0);
	}

	float dist( point p )
	{
		return (float) Math.sqrt(dist2( p ));
	}
}

