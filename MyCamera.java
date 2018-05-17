import java.awt.*;
import java.lang.Math;
import java.lang.Object;
import java.util.*;

import point;
import matrix;
import quaternion;

public class MyCamera
{
	Graphics g;
// The Camera/Viewpoint are fixed, object position must be rotated/moved into the viewport
	vector viewscr_pos = new vector(0.0f, 0.0f, 250.0f);	// X and Y are fixed to zero
	vector world_position = new vector();
	vector world_orientation = new vector();
	
	float screenDist = 0.0f;	
	int x0, y0, x1, y1;				// Screen position/dimensions
	double SCALE, SIZE;				// Screen SCALE/SIZE
	int x, y;						// Screen pixel
	int uv0=0, uw0=0, uv1=0, uw1=0;	// Screen line segment
	
	matrix m_Trans = new matrix();
	quaternion ObjCoord = new quaternion();			// World Object Coordinates
	
	double vr;						// View point distance
	quaternion m_rpc = new quaternion();	// Object coord output array
	double S,T;						// working varibles

	float deg = (1.0f / 180.0f) * 3.14152f;
	vector temp1 = new vector(), temp2 = new vector();
	vector draw = new vector();

	Color current_color;

	int i,j,current_camera;

	MyCamera(Graphics awtg, int p1, int p2, int p3, int p4)
	{
		 x0 = p1;
		 y0 = p2;
		 x1 = p3;
		 y1 = p4;
		 SCALE = 20;
		 SIZE = Math.min(p3,p4)>>1;
		 
		 g = awtg;

		ObjCoord.set( 0.0f, 0.0f, 0.0f, 1.0f );

		float sn,cs;					// sin & cos
		
		sn = (float) Math.sin(0.0f);
		cs = (float) Math.cos(0.0f);
		vr = -1.0f / viewscr_pos.z;

		m_Trans.Set(1, 1, 1.0f);
		m_Trans.Set(3, 3, 1.0f);
		
		m_Trans.Set(0, 0, cs);
		m_Trans.Set(2, 0, sn);
		m_Trans.Set(0, 3, -vr * sn);
		m_Trans.Set(2, 3, vr * cs);
		current_camera = 0;
		current_color = Color.black;
	}
	 
	void setScale(double s)
	{
		if (SCALE==s)
			return;
		SCALE = s;
		current_camera++;
	}
	
//	void setSize(int s)
//	{
//		if (SIZE==s)
//			return;
//		SIZE = s;
//		current_camera++;
//	}
	
	double getSize()
	{
		return SIZE;
	}
	
	void SetMyCamera(Graphics awtg)
	{
		g = awtg;
	}

//	void setColor(Color c)
//	{
//		 g.setColor(c);
//	}

	void fillRect()
	{
		g.fillRect(x0,y0,x1,y1);
	}

	void Perspective(vector at)
	{
		double vb,za;
		ObjCoord.set(at.x, at.y, at.z, 1.0);

		m_rpc.set(ObjCoord, m_Trans);
		
		za = m_rpc.GetW();
		
		if (za == 0.0f) za = 0.00001f;
		
		S = (m_rpc.GetX() / za);
		T = (m_rpc.GetY() / za);
		
		x = (int) ((SCALE*S) + SIZE);
		y = (int) ((SCALE*T) + SIZE);
	}

	vector rep = new vector();
	
	boolean DrawToScreen( dodeca model, vector at )
	{
		draw.CameraWorld( this, model, at );
			
		if (draw.z>15.0f) return false;
		
		Perspective(draw);
		
		return true;
	}

	void Test( dodeca model, vector at)
	{
		draw.CameraWorld( this, model, at );

		surface surf = model.GetFirst(6);

		Perspective(draw);

		if (surf!=null)
		{
//			rep.CameraWorld( this, model, model.view_focus );
//			rep.sub( viewscr_pos );

//			rep.set(model.model_nearest);
			rep.set(model.view_point);
//			rep.set(surf.faceat);

			float v = (float) rep.mag();

			System.out.println("DrawToScreen Test " 
				+ x + " " + y + " "
				+ ((float) rep.x) + " " 
				+ ((float) rep.y) + " " 
				+ ((float) rep.z) + " -> " + v  
				+ " (" +  model.view_scale  
				+ ")"
				);
		}
	}

	void DrawTo(dodeca model, vector from, vector to )
	{
		vector temp2 = new vector(model.view_point);

		if (!DrawToScreen( model, from )) return;
		uv0 = x;
		uw0 = y;
		
		if (uv0<0||uv0>=600) return;
		
		if (!DrawToScreen( model, to )) return;
		uv1 = x;
		uw1 = y;

		if (uv1<0||uv1>=600) return;

		g.drawLine(uv0, uw0, uv1, uw1);
	}
	
	void DrawTo(dodeca model, Color color, vector from, vector to )
	{
		SetColor(color);
		DrawTo( model, from, to );
	}
	
	void SetColor(Color color)
	{
		if (current_color!=color)
		{
			g.setColor(color);
			current_color = color;
		}
	}
	
	// facing: a point representing the normalized vector perpendicular to the front of a surface
	// at: a point on the surface of a model
	// center: center of the model in worldspace
	// orient: orientation of model

	void drawString(String s, int x, int y)
	{
//		g.drawString(s, x, y);
//		g.setColor(Color.black);
		g.drawString("Hello", x, y);
	} 

	boolean SetToward(dodeca model, int dpth, surface surf, vector view_point) //, point orientation)
	{
		if (((current_camera-surf.valid)>>2)<(surf.prjtoview))	// ||surf.inview>2.0)
			return (surf.prjtoview>0.0000000);
		
		boolean Toward=false;

		draw.set(surf.atv.at);
		draw.add(surf.faceat);

		if (view_point.dist2(draw)<view_point.dist2(surf.atv.at))
			Toward = false;
		else
			Toward = true;

		if (Toward)
		{
			surf.prjtoview = 1.0f;	// view_point.prj(surf.atv.at);
//			draw.set(view_point);
//			surf.prjtoview = draw.prj(surf.atv.at);
		}
		else
			surf.prjtoview = -1000.0;

		surf.valid = current_camera;

		return (surf.prjtoview>0.000000);
	}
}

