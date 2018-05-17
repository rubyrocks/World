
public class quaternion
{
	protected vector v;
	protected double w=0.0f;
	protected matrix mat = null;
	private boolean normflag=false;

	quaternion()
	{
		v = new vector();
	}
	
	void mult(double f)
	{
		w *= f;
		normflag = false;
	}	
	
	double dot( quaternion q1 )
	{
		return v.dot(q1.v) + w*q1.w;
	}

	void normalize()
	{
		if (normflag) return;

		double d = dot(this);

		if (d>0.0)
		{
			d = 1.0/Math.sqrt(d);
			mat = null;
		}
		else return;

		v.mult(d);
		w *= d;
		normflag = true;
	}
	
	void set(quaternion q)
	{
		v.set(q.v);
		w = q.w;
//		mat = null;
		normflag = q.normflag;
	}
	
// Promote a vector to a quaternion
// (usually for matrix transformation)
	void set(vector _v)
	{
		v = new vector(_v);
		w = 0.0f;
		mat = null;
//		normalize();
	}
	
	void set( double _x, double _y, double _z, double _w)
	{
		v = new vector(_x, _y, _z);
		w = _w;
		mat = null;
//		normalize();
	}
	
// Given two (normalized) vectors, 
// set this quaternion to the transformation
// from v0 to v1
	void xset(vector v0, vector v1)
	{
		vector va = new vector(v1);
		vector vb = new vector(v0);
	
		va.normalize();
		vb.normalize();

		mat = null;

		double cost = va.dot(vb);
		
		if (cost > 0.99999)
		{
			v.set(0.0, 0.0, 0.0);
			w = 1.0;
			normflag = true;
			return;
		}

		if (cost < -0.99999)
		{	// check if we can use cross product of from vector with[1,0,0]
			double len, temp, dist;
			vector t = new vector(0.0, va.x, -va.y);

			len = Math.sqrt(t.y*t.y + t.z*t.z);
			
			if (len < 1e-6)
			{	// nope! we need cross product of from vector with [0,1,0]
				t.x = -va.z;
				t.y = 0.0;
				t.z = va.x;
			}
			// normalize
			v.set(t);
			v.normalize();
			w = 0.0;
			normflag = true;
			return;
		}

		// ...else we can just cross two vectors
		
		v.set_to_cross_of(va, vb);
		v.normalize();
		
		// we have to use half-angle formulae (sin^2 t = ( 1-cos(2t))/2)
		
		double ss = Math.sqrt(0.5f * (1.0f - cost));
		// scale the axis th get the normalized quaternion
		v.mult(ss);
		
		// cos^2 t = ( 1 + cos(2t))/2
		// w part is cosine of half the rotation angle
		
		w = Math.sqrt( 0.5f * (1.0 + cost));

//		normalize();
	}
	
	// Given a pivot and two points, 
	// set this to the transformation 
	// quaternion from to
	void set(vector pivot, vector from, vector to)
	{
		vector Sphere0 = new vector(from);
		Sphere0.sub(pivot);
		Sphere0.normalize();
		
		vector Sphere1 = new vector(to);
		Sphere1.sub(pivot);
		Sphere1.normalize();
		
		xset(Sphere0, Sphere1);
		normalize();
	}

	// Given a vector ([4] = 0.0f), and a rotation matrix,
	// apply the matrix and set this quaternion as outcome vector
	void set(quaternion ObjCoord, matrix m_Trans)
	{
		set(m_Trans.mult(ObjCoord));
//		normalize();
	}

	double GetX()	{		return v.x;	}
	double GetY()	{		return v.y;	}
	double GetZ()	{		return v.z;	}
	double GetW()	{		return w;	}

/***
	void rotation_matrix()
	{
		double wx, wy, wz, xx, yy, yz, xy, xz, zz, x2, y2, z2;

		if (mat!=null) return;
		
		normalize();

		x2 = v.x + v.x;
		y2 = v.y + v.y;
		z2 = v.z + v.z;
		xx = v.x * x2;
		xy = v.x * y2;
		xz = v.x * z2;
		yy = v.y * y2;
		yz = v.y * z2;
		zz = v.z * z2;
		wx = w * x2;
		wy = w * y2;
		wz = w * z2;
		
		mat = new matrix();
		mat.Set(0, 0, 1.0 - (yy + zz));
		mat.Set(0, 1, xy - wz );
		mat.Set(0, 2, xz + wy );
		mat.Set(0, 3, 0.0 );
		
		mat.Set(1, 0, xy + wz );
		mat.Set(1, 1, 1.0 - (xx + zz) );
		mat.Set(1, 2, yz - wx );
		mat.Set(1, 3, 0.0 );
		
		mat.Set(2, 0, xz - wy );
		mat.Set(2, 1, yz + wx );
		mat.Set(2, 2, 1.0 - (xx + yy) );
		mat.Set(2, 3, 0.0);
		
		mat.Set(3, 0, 0.0 );
		mat.Set(3, 1, 0.0 );
		mat.Set(3, 2, 0.0 );
		mat.Set(3, 3, 1.0 );
	}
}
***/

	void rotation_matrix()
	{
		double xw, yw, zw, xx, yy, yz, xy, xz, zz;

		if (mat!=null) return;

		if (mat==null)
			mat = new matrix();
		else
			mat.matrix_init();
		
	    xx      = v.x * v.x;
	    xy      = v.x * v.y;
	    xz      = v.x * v.z;
	    xw      = v.x * w;

	    yy      = v.y * v.y;
	    yz      = v.y * v.z;
	    yw      = v.y * w;

	 	zz      = v.z * v.z;
    	zw      = v.z * w;

	    mat.Set(0, 0, 1 - 2.0 * ( yy + zz ) );
    	mat.Set(0, 1,     2.0 * ( xy - zw ) );
    	mat.Set(0, 2,     2.0 * ( xz + yw ) );

    	mat.Set(1, 0,     2.0 * ( xy + zw ) );
    	mat.Set(1, 1, 1 - 2.0 * ( xx + zz ) );
    	mat.Set(1, 2,     2.0 * ( yz - xw ) );

    	mat.Set(2, 0,     2.0 * ( xz - yw ) );
    	mat.Set(2, 1,     2.0 * ( yz + xw ) ); 
    	mat.Set(2, 2, 1 - 2.0 * ( xx + yy ) );

		mat.Set(3, 3, 1.0 );
//    	mat[3]  = mat[7] = mat[11 = mat[12] = mat[13] = mat[14] = 0;
 //   	mat[15] = 1;
    }
}
/***	
// Given the Spherical coordinate of the axis of rotation 
// and an angle of rotation set the quaternion
	void set(float latitude, float longitude, float angle)
	{
		double sin_a    = Math.sin( angle / 2.0 );
		double cos_a    = Math.cos( angle / 2.0 );

		double sin_lat  = Math.sin( latitude );
		double cos_lat  = Math.cos( latitude );

		double sin_long = Math.sin( longitude );
		double cos_long = Math.cos( longitude );

		v.set(
			(float) (sin_a * cos_lat * sin_long),
			(float) (sin_a * sin_lat),
			(float) (sin_a * sin_lat * cos_long)
			);
		v.normalize();
		w = (float) cos_a;
		mat = null;
	}

	boolean isequal(quaternion p)
	{
		if (v.isequal(p.v)&&(w==p.w)) return true;
		else return false;
	}

	double Get(int i)
	{
		switch(i)
		{
		case 0: return v.x;
		case 1: return v.y;
		case 2: return v.z;
		case 3: return w;
		default: return 0.0f;
		}
	}
	
	void Set(int i, float _v)
	{
		switch(i)
		{
		case 0: v.SetX(_v); break;
		case 1: v.SetY(_v); break;
		case 2: v.SetZ(_v); break;
		case 3: w = _v; break;
		default: break;
		}
		mat = null;
	}
	
	//	void SetMag()	{	m = (float) Math.sqrt(x*x + y*y + z*z + w*w); mflag = true; }

	void SetX(float _x)	{		v.SetX(_x); mat = null; }
	void SetY(float _y)	{		v.SetY(_y); mat = null; }
	void SetZ(float _z)	{		v.SetZ(_z); mat = null; }
	void SetW(float _w)	{		w = _w; mat = null; }

***/	
/***	
	void mult ( quaternion q )
	{
		double s0 = this.v.dot(q.v);
		double w0 = this.w*q.w;
		vector v0 = new vector();
		vector v1 = new vector();
		vector v2 = new vector();
		
		v0.set(q.v);
		v0.cross(this.v);
		v1.set(this.v);
		v1.mult(q.w);
		v2.set(q.v);
		v2.mult(this.w);		
		this.v.set(v0);
		this.v.add(v1);
		this.v.add(v2);
		this.w = s0 - w0;
		
		this.normalize();
	}
	
	void mult(double _x, double _y, double _z, double _w)
	{
		quaternion q = new quaternion();
		q.set(_x,_y,_z,_w);
		mult(q);
	}
***/
	
//	void div ( float f)
//	{
//		w /= f;
//		mat = null;
//	}

/***
[w, v] (where v = (x, y, z) 
is called a "vector" and w is called a "scalar") 
(Eq. 2) 

I will use the second notation throughout this article. 
Now that you know how quaternions are represented, let's 
start with some basic operations that use them. 

It is extremely important to note that only unit quaternions 
represent rotations, and you can assume that when I talk about 
quaternions, I'm talking about unit quaternions unless otherwise 
specified. 

Since you've just seen how other methods represent rotations, 
let's see how we can specify rotations using quaternions. It 
can be proven (and the proof isn't that hard) that the rotation 
of a vector v by a unit quaternion q can be represented as 

v´ = q v q-1 (where v = [0, v]) 

The result, a rotated vector v´, will always have a 0 scalar 
value for w (recall Eq. 2 earlier), so you can omit it from 
your computations. 

Table 1. Basic operations using quaternions.
Addition: q + q´ = [w + w´, v + v´] 
Multiplication: qq´ = [ww´ - v · v´, v x v´ + wv´ +w´v] 
	(· is vector dot product and x is vector cross product); 
	Note: qq´ ? q´q 
Conjugate: q* = [w, -v] 
Norm: N(q) = w2 + x2 + y2 + z2 
Inverse: q-1 = q* / N(q) 
Unit Quaternion: q is a unit quaternion if N(q)= 1 
	and then q-1 = q* 
Identity: [1, (0, 0, 0)] (when involving multiplication) 
		and [0, (0, 0, 0)] (when involving addition) 

ANGLE AND AXIS. Converting from angle and axis notation 
to quaternion notation involves two trigonometric operations, 
as well as several multiplies and divisions. It can be represented as 

q = [cos(Q/2), sin(Q /2)v] (where Q is an angle and v is an axis) 
(Eq. 4) 

EULER ANGLES. Converting Euler angles into quaternions is a similar 
process - you just have to be careful that you perform the operations 
in the correct order. For example, let's say that a plane in a flight 
simulator first performs a yaw, then a pitch, and finally a roll. You 
can represent this combined quaternion rotation as 

q = qyaw qpitch qroll where: 
               qroll = [cos (y/2), (sin(y/2), 0, 0)] 
               qpitch = [cos (q/2), (0, sin(q/2), 0)] 
               qyaw = [cos(f /2), (0, 0, sin(f /2)] 
(Eq. 5) 

The order in which you perform the multiplications is important. 
Quaternion multiplication is not commutative (due to the vector 
cross product that's involved). In other words, changing the order 
in which you rotate an object around various axes can produce different 
resulting orientations, and therefore, the order is important. 

ROTATION MATRIX. Converting from a rotation matrix to a quaternion 
representation is a bit more involved, and its implementation can be 
seen in Listing 1.

Conversion between a unit quaternion and a rotation matrix can be 
specified as 

?????????????????????????????

It's very difficult to specify a rotation directly using quaternions. 
It's best to store your character's or object's orientation as a 
Euler angle and convert it to quaternions before you start interpolating. 
It's much easier to increment rotation around an angle, after getting 
the user's input, using Euler angles (that is, roll = roll + 1), than 
to directly recalculate a quaternion. 

Since converting between quaternions and rotation matrices and Euler 
angles is performed often, it's important to optimize the conversion 
process. Very fast conversion (involving only nine muls) between a 
unit quaternion and a matrix is presented in Listing 2. Please note 
that the code assumes that a matrix is in a right-hand coordinate 
system and that matrix rotation is represented in a column major 
format (for example, OpenGL compatible). 


Listing 1: Matrix to quaternion code. 

MatToQuat(float m[4][4], QUAT * quat)
{
  float  tr, s, q[4];
  int    i, j, k;

  int nxt[3] = {1, 2, 0};

  tr = m[0][0] + m[1][1] + m[2][2];

  // check the diagonal
  if (tr > 0.0) {
    s = sqrt (tr + 1.0);
    quat->w = s / 2.0;
    s = 0.5 / s;
    quat->x = (m[1][2] - m[2][1]) * s;
    quat->y = (m[2][0] - m[0][2]) * s;
    quat->z = (m[0][1] - m[1][0]) * s;
} else {		
	 // diagonal is negative
    	  i = 0;
          if (m[1][1] > m[0][0]) i = 1;
	     if (m[2][2] > m[i][i]) i = 2;
            j = nxt[i];
            k = nxt[j];

            s = sqrt ((m[i][i] - (m[j][j] + m[k][k])) + 1.0);
      
	     q[i] = s * 0.5;
            
            if (s != 0.0) s = 0.5 / s;

	    q[3] = (m[j][k] - m[k][j]) * s;
            q[j] = (m[i][j] + m[j][i]) * s;
            q[k] = (m[i][k] + m[k][i]) * s;

	  quat->x = q[0];
	  quat->y = q[1];
	  quat->z = q[2];
	  quat->w = q[3];
  }
}



If you aren't dealing with unit quaternions, additional 
multiplications and a division are required. Euler angle 
to quaternion conversion can be coded as shown in Listing 3. 

One of the most useful aspects of quaternions that we game 
programmers are concerned with is the fact that it's easy 
to interpolate between two quaternion orientations and 
achieve smooth animation. To demonstrate why this is so, 
let's look at an example using spherical rotations. 
Spherical quaternion interpolations follow the shortest 
path (arc) on a four-dimensional, unit quaternion sphere. 
Since 4D spheres are difficult to imagine, I'll use a 3D 
sphere (Figure 3) to help you visualize quaternion rotations 
and interpolations. 

Let's assume that the initial orientation of a vector 
emanating from the center of the sphere can be represented 
by q1 and the final orientation of the vector is q3. The 
arc between q1 and q3 is the path that the interpolation 
would follow. Figure 3 also shows that if we have an 
intermediate position q2, the interpolation from 
q1 -> q2 -> q3 will not necessarily follow the same path 
as the q1 ->q3 interpolation. The initial and final 
orientations are the same, but the arcs are not. 

Quaternions simplify the calculations required when 
compositing rotations. For example, if you have two 
or more orientations represented as matrices, it is 
easy to combine them by multiplying two intermediate 
rotations. 

R = R2R1 (rotation R1 followed by a rotation R2) 
(Eq. 7) 


Listing 2: Quaternion-to-matrix conversion. 

QuatToMatrix(QUAT * quat, float m[4][4]){
  float wx, wy, wz, xx, yy, yz, xy, xz, zz, x2, y2, z2;

  // calculate coefficients
  x2 = quat->x + quat->x;
  y2 = quat->y + quat->y; 
  z2 = quat->z + quat->z;
  xx = quat->x * x2;   xy = quat->x * y2;   xz = quat->x * z2;
  yy = quat->y * y2;   yz = quat->y * z2;   zz = quat->z * z2;
  wx = quat->w * x2;   wy = quat->w * y2;   wz = quat->w * z2;

  m[0][0] = 1.0 - (yy + zz); 	m[0][1] = xy - wz;
  m[0][2] = xz + wy;		m[0][3] = 0.0;
 
  m[1][0] = xy + wz;		m[1][1] = 1.0 - (xx + zz);
  m[1][2] = yz - wx;		m[1][3] = 0.0;

  m[2][0] = xz - wy;		m[2][1] = yz + wx;
  m[2][2] = 1.0 - (xx + yy);		m[2][3] = 0.0;

  m[3][0] = 0;			m[3][1] = 0;
  m[3][2] = 0;			m[3][3] = 1;
}


This composition involves 27 multiplications and 18 additions, 
assuming 3x3 matrices. On the other hand, a quaternion composition 
can be represented as 

q = q2q1 (rotation q1 followed by a rotation q2) 
(Eq. 8) 

As you can see, the quaternion method is analogous 
to the matrix composition. However, the quaternion 
method requires only eight multiplications and four 
divides (Listing 4), so compositing quaternions 
is computationally cheap compared to matrix composition. 
Savings such as this are especially important when working 
with hierarchical object representations and inverse kinematics. 

Now that you have an efficient multiplication routine, 
see how can you interpolate between two quaternion rotations 
along the shortest arc. Spherical Linear intERPolation 
(SLERP) achieves this and can be written as 


(Eq. 9) 

where pq = cos(q) and parameter t goes from 0 to 1. The 
implementation of this equation is presented in Listing 5. 
If two orientations are too close, you can use linear 
interpolation to avoid any divisions by zero. 


Figure 3. Quaternion rotations. 


Listing 3: Euler-to-quaternion conversion. 

EulerToQuat(float roll, float pitch, float yaw, QUAT * quat)
{
	float cr, cp, cy, sr, sp, sy, cpcy, spsy;

// calculate trig identities
cr = cos(roll/2);
	cp = cos(pitch/2);
	cy = cos(yaw/2);

	sr = sin(roll/2);
	sp = sin(pitch/2);
	sy = sin(yaw/2);
	
	cpcy = cp * cy;
	spsy = sp * sy;

	quat->w = cr * cpcy + sr * spsy;
	quat->x = sr * cpcy - cr * spsy;
	quat->y = cr * sp * cy + sr * cp * sy;
	quat->z = cr * cp * sy - sr * sp * cy;
}


The basic SLERP rotation algorithm is shown in Listing 6. 
Note that you have to be careful that your quaternion represents 
an absolute and not a relative rotation. You can think of a 
relative rotation as a rotation from the previous (intermediate) 
orientation and an absolute rotation as the rotation from 
the initial orientation. This becomes clearer if you think 
of the q2 quaternion orientation in Figure 3 as a relative 
rotation, since it moved with respect to the q1 orientation. 
To get an absolute rotation of a given quaternion, you can just 
multiply the current relative orientation by a previous absolute 
one. The initial orientation of an object can be represented 
as a multiplication identity [1, (0, 0, 0)]. This means that 
the first orientation is always an absolute one, because 

q = qidentity q 
(Eq. 10) 


Listing 4: Efficient quaternion multiplication. 

QuatMul(QUAT *q1, QUAT *q2, QUAT *res){

float A, B, C, D, E, F, G, H;

A = (q1->w + q1->x)(q2->w + q2->x);
B = (q1->z - q1->y)(q2->y - q2->z);
C = (q1->x - q1->w)(q2->y - q2->z);
D = (q1->y + q1->z)(q2->x - q2->w);
E = (q1->x + q1->z)(q2->x + q2->y);
F = (q1->x - q1->z)( q2->x - q2->y);
G = (q1->w + q1->y)(q2->w - q2->z);
H = (q1->w - q1->y)(q2->w + q2->z);

res->w = B + (-E - F + G + H) /2;
res->x = A - (E + F + G + H)/2; 
res->y = -C + (E - F + G - H)/2;
res->z = - D + (E - F - G + H)/2;
}

As I stated earlier, a practical use for quaternions involves 
camera rotations in third-person-perspective games. Ever since 
I saw the camera implementation in TOMB RAIDER, I've wanted 
to implement something similar. So let's implement a third-person 
camera (Figure 4).

To start off, let's create a camera that is always positioned 
above the head of our character and that points at a spot that 
is always slightly above the character's head. The camera 
is also positioned d units behind our main character. We 
can also implement it so that we can vary the roll (angle q 
in Figure 4) by rotating around the x axis. 

As soon as a player changes the orientation of the character, 
you rotate the character instantly and use SLERP to reorient 
the camera behind the character (Figure 5). This has the dual 
benefit of providing smooth camera rotations and making players 
feel as though the game responded instantly to their input. 


Figure 4. Third-person camera. 


Figure 5. Camera from top. 

You can set the camera's center of rotation (pivot point) 
as the center of the object it is tracking. This allows you 
to piggyback on the calculations that the game already makes 
when the character moves within the game world. 

Note that I do not recommend using quaternion interpolation 
for first-person action games since these games typically 
require instant response to player actions, and SLERP does 
take time. 

However, we can use it for some special scenes. For instance, 
assume that you're writing a tank simulation. Every tank 
has a scope or similar targeting mechanism, and you'd 
like to simulate it as realistically as possible. 
The scoping mechanism and the tank's barrel are controlled 
by a series of motors that players control. Depending 
on the zoom power of the scope and the distance to 
a target object, even a small movement of a motor 
could cause a large change in the viewing angle, 
resulting in a series of huge, seemingly disconnected 
jumps between individual frames. To eliminate this 
unwanted effect, you could interpolate the orientation 
according to the zoom and distance of object. This type 
of interpolation between two positions over several frames 
helps dampen the rapid movement and keeps players from 
becoming disoriented. 

Another useful application of quaternions is for prerecorded 
(but not prerendered) animations. Instead of recording camera 
movements by playing the game (as many games do today), 
you could prerecord camera movements and rotations using 
a commercial package such as Softimage 3D or 3D Studio MAX. 
Then, using an SDK, export all of the keyframed camera/object 
quaternion rotations. This would save both space and rendering 
time. Then you could just play the keyframed camera motions 
whenever the script calls for cinematic scenes. 


Listing 5: SLERP implementation. 


QuatSlerp(QUAT * from, QUAT * to, float t, QUAT * res)
      {
        float           to1[4];
        double        omega, cosom, sinom, scale0, scale1;

        // calc cosine
        cosom = from->x * to->x + from->y * to->y + from->z * to->z
			       + from->w * to->w;

        // adjust signs (if necessary)
        if ( cosom <0.0 ){ cosom = -cosom; to1[0] = - to->x;
		to1[1] = - to->y;
		to1[2] = - to->z;
		to1[3] = - to->w;
        } else  {
		to1[0] = to->x;
		to1[1] = to->y;
		to1[2] = to->z;
		to1[3] = to->w;
        }

        // calculate coefficients

       if ( (1.0 - cosom) > DELTA ) {
                // standard case (slerp)
                omega = acos(cosom);
                sinom = sin(omega);
                scale0 = sin((1.0 - t) * omega) / sinom;
                scale1 = sin(t * omega) / sinom;

        } else {        
    // "from" and "to" quaternions are very close 
	    //  ... so we can do a linear interpolation
                scale0 = 1.0 - t;
                scale1 = t;
        }
	// calculate final values
	res->x = scale0 * from->x + scale1 * to1[0];
	res->y = scale0 * from->y + scale1 * to1[1];
	res->z = scale0 * from->z + scale1 * to1[2];
	res->w = scale0 * from->w + scale1 * to1[3];
}

After reading Chris Hecker's columns on physics last year, 
I wanted to add angular velocity to a game engine on which 
I was working. Chris dealt mainly with matrix math, and 
because I wanted to eliminate quaternion-to-matrix and 
matrix-to-quaternion conversions (since our game engine 
is based on quaternion math), I did some research and 
found out that it is easy to add angular velocity 
(represented as a vector) to a quaternion orientation. 
The solution (Eq. 11) can be represented as a differential 
equation.

where quat(angular) is a quaternion with a zero scalar 
part (that is, w = 0) and a vector part equal to the 
angular velocity vector. Q is our original quaternion 
orientation. 

To integrate the above equation (Q + dQ/dt), I recommend 
using the Runge-Kutta order four method. If you are using 
matrices, the Runge-Kutta order five method achieves better 
results within a game. (The Runge-Kutta method is a way of 
integrating differential equations. A complete description 
of the method can be found in any elementary numerical 
algorithm book, such as Numerical Recipes in C. It has a 
complete section devoted to numerical, differential i
ntegration.) For a complete derivation of angular velocity 
integration, consult Dave Baraff's SIGGRAPH tutorials. 





(Eq. 11) 

Quaternions can be a very efficient and extremely useful 
method of storing and performing rotations, and they 
offer many advantages over other methods. Unfortunately, 
they are also impossible to visualize and completely 
unintuitive. However, if you use quaternions to represent 
rotations internally, and use some other method (for example, 
angle-axis or Euler angles) as an immediate representation, 
you won't have to visualize them. 

Nick Bobick is a game developer at Caged Entertainment Inc. 
and he is currently working on a cool 3D game. He can be 
contacted at nb@netcom.ca. The author would like to thank 
Ken Shoemake for his research and publications. Without him, 
this article would not have been possible. 

***/
