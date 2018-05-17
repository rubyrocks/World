public class matrix
{
	private double mat[] = new double[16];
	private boolean mflag[] = new boolean[16];	
//	private double X=0.0f, Y=0.0f, Z=0.0f, W=0.0f;
//	private quaternion c = new quaternion();
//	private double x = 0.0f, y = 0.0f, z = 0.0f, w = 0.0f;
//	private vector V=null,C=c.v;
	
	void matrix_init()
	{
		for (int i=0;i<16;i++)
		{
			mat[i] = 0.0f;
			mflag[i] = false;
		}
	}
	
	matrix()
	{
		matrix_init();
	}
	
	void identity()
	{
		matrix_init();
		for (int i=0;i<16;i+=4)
			mat[i] = 1.0f;
	}
	
	void Set(int i, int j, double x)
	{
		int k=i+(j<<2);
		mat[k] = x;
		if (!mflag[k]&&x!=0.00000f)
			mflag[k] = true;
	}


//
	quaternion mult(quaternion q)
	{
		double V = 0.0;
		quaternion c = new quaternion();
		double x = q.v.x, y = q.v.y, z = q.v.z, w = q.w;

		V = 0.0f;
		if (mflag[0]) V += x*mat[0];
		if (mflag[1]) V += y*mat[1];
		if (mflag[2]) V += z*mat[2];
		if (mflag[3]) V += w*mat[3];
		c.v.x = V;

		V = 0.0f;
		if (mflag[4]) V += x*mat[4];
		if (mflag[5]) V += y*mat[5];
		if (mflag[6]) V += z*mat[6];
		if (mflag[7]) V += w*mat[7];
		c.v.y = V;

		V = 0.0f;
		if (mflag[8]) V += x*mat[8];
		if (mflag[9]) V += y*mat[9];
		if (mflag[10]) V += z*mat[10];
		if (mflag[11]) V += w*mat[11];
		c.v.z = V;

		V = 0.0f;
		if (mflag[12]) V += x*mat[12];
		if (mflag[13]) V += y*mat[13];
		if (mflag[14]) V += z*mat[14];
		if (mflag[15]) V += w*mat[15];
		c.w = V;
		
//		c.normalize();
		return c;
	}
}

