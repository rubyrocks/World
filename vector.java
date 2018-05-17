public class vector
{
	protected double x=0.0,y=0.0,z=0.0,m=0.0;
	private boolean magflag = true;
	private boolean normflag = false;
	
	vector()
	{
	}
	
	vector (double _x, double _y, double _z)
	{
		x = _x;
		y = _y;
		z = _z;
		magflag = false;
		normflag = false;		
	}
	
	vector (vector p)
	{
		x = p.x;
		y = p.y;
		z = p.z;
		m = p.m;
		magflag = true;
		normflag = false;
	}
	
	void zero ()
	{
		x = 0.0;
		y = 0.0;
		z = 0.0;
		m = 0.0;
		magflag = true;
		normflag = false;
	}
	
	void set (double _x, double _y, double _z)
	{
		x = _x;
		y = _y;
		z = _z;
		magflag = false;
		normflag = false;
	}
	
	void set (vector p)
	{
		x = p.x;
		y = p.y;
		z = p.z;
		m = p.m;
		magflag = p.magflag;
		normflag = p.normflag;
	}
	
	void add (double _x, double _y, double _z)
	{
		x += _x;
		y += _y;
		z += _z;
		magflag = false;
		normflag = false;
	}
	
	void add (vector p)
	{
		x += p.x;
		y += p.y;
		z += p.z;
		magflag = false;
		normflag = false;
	}

	void sub (vector p)
	{
		x -= p.x;
		y -= p.y;
		z -= p.z;
		magflag = false;
		normflag = false;
	}

	void mult ( double f)
	{
		x *= f;
		y *= f;
		z *= f;
		m *= f;
		normflag = false;
	}
	
	void div ( double f)
	{
		x /= f;
		y /= f;
		z /= f;
		m /= f;
		normflag = false;
	}

	void rotate( quaternion q )
	{
//		q.mat = null;
		q.rotation_matrix();
		quaternion in = new quaternion();
		quaternion out = new quaternion();
		in.set(this);
		out.set(in, q.mat);
		set(out);
	}
	
	void rotate( float _x, float _y, float _z, float _v )
	{
		quaternion q = new quaternion();
		q.set(_x,_y,_z,_v);
		q.normalize();
		rotate(q);
	}
	
	void set( quaternion q )
	{
		x = q.v.x;
		y = q.v.y;
		z = q.v.z;
		magflag = false;
		normflag = false;
	}
	
	double dist2( vector p )
	{
		return ((x-p.x)*(x-p.x)) + ((y-p.y)*(y-p.y)) + ((z-p.z)*(z-p.z));
	}

	double dist2( double _x, double _y, double _z )
	{
		return ((x-_x)*(x-_x)) + ((y-_y)*(y-_y)) + ((z-_z)*(z-_z));
	}

	float dist( vector p )
	{
		return (float) Math.sqrt(dist2( p ));
	}
	
	float dist( float _x, float _y, float _z )
	{
		return (float) Math.sqrt(dist2(_x, _y, _z));
	}

	double dot( vector v )
	{
		return x*v.x + y*v.y + z*v.z;
	}
	
	double mag()
	{
		if (magflag) return m;
		m = Math.sqrt(dot(this));
		magflag = true;
		return m;
	}
	
	double mag2()
	{
		if (magflag) return m*m;
		return dot(this);
	}
	
	void SetMag(double mag)
	{
		double setmp;

		if (magflag)
			setmp = m;
		else 
			setmp = dist(0.0f,0.0f,0.0f);

		mult(mag/setmp); 
		m = mag;
		magflag = true;
	}

	double prj(vector vect2)
	{
			SetMag(((float) dot(vect2))/(magflag?m:mag()));
			return m;
	}

	void set_to_cross_of( vector vect1, vector vect2 )
	{
		x = (vect1.y*vect2.z) - (vect1.z*vect2.y);
		y = (vect1.z*vect2.x) - (vect1.x*vect2.z);
		z = (vect1.x*vect2.y) - (vect1.y*vect2.x);
		magflag = false;
		normflag = false;
	}

/***	
	void cross( vector vect )
	{
		vector v = new vector();
		
		v.x = (y*vect.z) - (z*vect.y);
		v.y = (z*vect.x) - (x*vect.z);
		v.z = (x*vect.y) - (y*vect.x);
		set(v);
		magflag = false;
		normflag = false;
	}
***/
	
	void normalize()
	{
		if (normflag) return;
		
		if (!magflag) 
			mag();
		
		normflag = true;

		if (m==1.000000) return;

		if (m>0.000000)
		{
			mult(1.0/m);
		}
		else
			normflag = false;
	}
	
	void interpulate(vector from, vector to, float elevation)
	{
		x += (to.x-from.x)*elevation;
		y += (to.y-from.y)*elevation;
		z += (to.z-from.z)*elevation;
		magflag = false;
		normflag = false;
	}

	double Get(int i)
	{
		switch(i)
		{
		case 0: return x;
		case 1: return y;
		case 2: return z;
		default: return 0.0f;
		}
	}
	
	void SetX(double  v) {	x = v; magflag = false; }
	void SetY(double  v) {	y = v; magflag = false; }
	void SetZ(double  v) {	z = v; magflag = false; }

	boolean isequal(vector p)
	{
		if ((x==p.x)&&(y==p.y)&&(z==p.z)) return true;
		else return false;
	}

	void CameraWorld( MyCamera cam, dodeca model, vector at )
	{
		vector temp = new vector();

		set(0.0, 0.0, 0.0);
		sub(cam.viewscr_pos);

		temp.set(0.0, 0.0, -5.0);
		temp.rotate(model.camera_orient);
		temp.mult(model.view_scale);

		add(temp);

		temp.set(at);
		temp.rotate(model.camera_orient);
		temp.rotate(model.error_orient);
		temp.mult(model.view_scale);

		add(temp);
	}
}

/*
// Rotate about X
//        (1    0      0    0)
//Rx(q) = (0  cos q  sin q  0)
//        (0 -sin q  cos q  0)
//        (0    0     0     1)
	void rotateX ( vector orientation )
	{
		matrix rmat = new matrix();
		rmat.Set(0,0,1.0f);
		rmat.Set(1,1,(float) Math.cos(orientation.x));
		rmat.Set(1,2,(float) Math.sin(orientation.x));
		rmat.Set(2,1,(float) -Math.sin(orientation.x));
		rmat.Set(2,2,(float) Math.cos(orientation.x));
		rmat.Set(3,3,1.0f);
		quaternion q = new quaternion(), r = new quaternion();
		q.set(this);
		r.set(q,rmat);
		set(r);
//		this.x = (float) this.x;
//		float Y = (float) ((this.y*Math.cos(orientation.x)) - (this.z*Math.sin(orientation.x)));
//		this.z = (float) ((this.y*Math.sin(orientation.x)) + (this.z*Math.cos(orientation.x)));
//		this.y = Y;
	}
*/

/*
// Rotate about Y
//        (cos q  0  -sin q   0)
//Ry(q) = (0      1    0      0)
//        (sin q  0  cos q    0)
//        (0      0    0     1) 
	void rotateY ( vector orientation )
	{
		matrix rmat = new matrix();
		rmat.Set(0,0,(float) Math.cos(orientation.y));
		rmat.Set(0,2,(float) -Math.sin(orientation.y));
		rmat.Set(1,1,1.0f);
		rmat.Set(2,0,(float) Math.sin(orientation.y));
		rmat.Set(2,2,(float) Math.cos(orientation.y));
		rmat.Set(3,3,1.0f);
		quaternion q = new quaternion(), r = new quaternion();
		q.set(this);
		r.set(q,rmat);
		set(r);
//		double sn = Math.sin(orientation.y), cs = Math.cos(orientation.y);
//		float X = (float) (this.x*Math.cos(orientation.y) - (this.z*Math.sin(orientation.y)));
//		this.y = (float) this.y;
//		this.z = (float) (this.x*Math.sin(orientation.y) + (this.z*Math.cos(orientation.y)));
//		this.x = X;
	}
*/

/*
// Rotate about Z
//         ( cos q  sin q  0  0)
//Rz (q) = (-sin q  cos q  0  0)
//         ( 0        0    1  0)
//         ( 0        0    0  1)
	void rotateZ ( vector orientation )
	{
		matrix rmat = new matrix();
		rmat.Set(0,0,(float) Math.cos(orientation.z));
		rmat.Set(1,1,(float) Math.sin(orientation.z));
		rmat.Set(1,0,(float) -Math.sin(orientation.z));
		rmat.Set(1,1,(float) Math.cos(orientation.z));
		rmat.Set(2,2,1.0f);
		rmat.Set(3,3,1.0f);
		quaternion q = new quaternion(), r = new quaternion();
		q.set(this);
		r.set(q,rmat);
		set(r);
//		float X = (float) (this.x*Math.cos(orientation.z) - (this.y*Math.sin(orientation.z)));
//		this.y = (float) (this.x*Math.sin(orientation.z) + (this.y*Math.cos(orientation.z)));
//		this.z = (float) this.z;
//		this.x = X;
	}
*/

/*	
	void set_to_spherical(vector vf)
	{
		float X, Y;

//		if (Math.abs(_z)>.0000001f)
//			X = (float) Math.atan(Math.sqrt(_x*_x + _y*_y)/_z);
//		else
//			X = 1.57080f;
//		
//		if (Math.abs(_x)>.0000001f)
//			Y = (float) Math.atan(_y/_x);
//		else
//			if (_y>=0.00f)
//				Y = -1.57080f;
//			else
//				Y = 1.57080f;

//		double hypot = Math.pow(Math.pow(vf.GetX(),2.0)
//						 +Math.pow(vf.GetY(),2.0)
//						 +Math.pow(vf.GetZ(),2.0),0.5);

//		X = (float) Math.asin((double) (vf.GetX()/hypot));
//		Y = (float) Math.asin((double) (vf.GetY()/hypot));

		float _x = vf.GetX(), _y = vf.GetY(), _z = vf.GetZ();
		float zz = _z*_z;
		
		X = (float) Math.atan(_x/(Math.sqrt(_y*_y + zz) + 0.00000001f));
		Y = (float) Math.atan(_y/(Math.sqrt(_x*_x + zz) + 0.00000001f));

		SetX(X);
		SetY(Y);
		SetZ(0.0f);
	}
*/
/*
	void rotate(vector orientation)
	{
		double rmat[][] = new double[4][4];
		
		double A = Math.cos(orientation.x);
		double B = Math.sin(orientation.x);
		double C = Math.cos(orientation.y);
		double D = Math.sin(orientation.y);
		double E = Math.cos(orientation.z);
		double F = Math.sin(orientation.z);

		double AD = A * D;
		double BD = B * D;

		rmat[0][0]  =   C * E;
		rmat[1][0]  =  -C * F;
		rmat[2][0]  =  -D;
		rmat[0][1]  = -BD * E + A * F;
		rmat[1][1]  =  BD * F + A * E;
	    rmat[2][1]  =  -B * C;
		rmat[0][2]  =  AD * E + B * F;
		rmat[1][2]  = -AD * F + B * E;
		rmat[2][2] =   A * C;

		rmat[3][0]  =  rmat[3][1] = rmat[3][2] = rmat[0][3] = rmat[1][3] = rmat[2][3] = 0.0f;
		rmat[3][3] =  1.0f;

		double m_rpc[] = new double[4];		
		float ObjCoord[] = new float[4];
		
		ObjCoord[0] = GetX();
		ObjCoord[1] = GetY();
		ObjCoord[2] = GetZ();
		ObjCoord[3] = 1.0f;
		
		for (int j=0;j<4;j++)
		{
			double vb = 0.0f;
			for (int i=0;i<4;i++)
				vb += (ObjCoord[i] * rmat[i][j]);
			m_rpc[j] = vb;
		}
		
		x = ObjCoord[0];
		y = ObjCoord[1];
		z = ObjCoord[2];
	}
*/
/*	
	void rotate(vector orientation)
	{
		double rmat[][] = new double[4][4];
		
		double m_rpc[] = new double[4];		
		float ObjCoord[] = new float[4];

		float xx      = x * x;
		float xy      = x * y;
		float xz      = x * z;
		float xw      = x * 1.0f;

		float yy      = y * y;
		float yz      = y * z;
		float yw      = y * 1.0f;

		float zz      = z * z;
		float zw      = z * 1.0f;

		rmat[0][0]  = 1 - 2 * ( yy + zz );
		rmat[1][0]  =     2 * ( xy - zw );
		rmat[2][0]  =     2 * ( xz + yw );

		rmat[0][1]  =     2 * ( xy + zw );
		rmat[1][1]  = 1 - 2 * ( xx + zz );
		rmat[2][1]  =     2 * ( yz - xw );

		rmat[0][2]  =     2 * ( xz - yw );
		rmat[1][2]  =     2 * ( yz + xw );
		rmat[2][2]  = 1 - 2 * ( xx + yy );

		rmat[3][0]  = rmat[3][1] = rmat[3][2] = rmat[0][3] = rmat[1][3] = rmat[2][3] = 0;
	    rmat[3][3] = 1;

		ObjCoord[0] = GetX();
		ObjCoord[1] = GetY();
		ObjCoord[2] = GetZ();
		ObjCoord[3] = 1.0f;
		
		for (int j=0;j<4;j++)
		{
			double vb = 0.0f;
			for (int i=0;i<4;i++)
				vb += (ObjCoord[i] * rmat[i][j]);
			m_rpc[j] = vb;
		}
		
		x = ObjCoord[0];
		y = ObjCoord[1];
		z = ObjCoord[2];
	}
*/
/*
This is the final rotation matrix. As a 4x4 matrix this is:

         |  CE      -CF      -D   0 |
    M  = | -BDE+AF   BDF+AE  -BC  0 |
         |  ADE+BF  -ADF+BE   AC  0 |
         |  0        0        0   1 |

  The individual values of A,B,C,D,E and F are evaluated first. Also, the
  values of BD and AD are also evaluated since they occur more than once.

  Thus, the final algorithm is as follows:

    -----------------------

    A       = cos(angle_x);
    B       = sin(angle_x);
    C       = cos(angle_y);
    D       = sin(angle_y);
    E       = cos(angle_z);
    F       = sin(angle_z);

    AD      =   A * D;
    BD      =   B * D;

    mat[0]  =   C * E;
    mat[1]  =  -C * F;
    mat[2]  =  -D;
    mat[4]  = -BD * E + A * F;
    mat[5]  =  BD * F + A * E;
    mat[6]  =  -B * C;
    mat[8]  =  AD * E + B * F;
    mat[9]  = -AD * F + B * E;
    mat[10] =   A * C;

    mat[3]  =  mat[7] = mat[11] = mat[12] = mat[13] = mat[14] = 0;
    mat[15] =  1;

    -----------------------
*/

