import java.awt.*;
import java.lang.Math;
import java.lang.Object;
import java.util.*;

import lineseg;
import surface;
import MyCamera;

public class dodeca
{
	boolean testmode = false;
	
	surface dodeca[] = new surface[20];
	vertice tri[] = new vertice[12];

	vertice vertice_list = null;
	vertice vertice_end = null;
	vertice vertice_stack = null;
	int next_vertice_id=0;
	int vertice_allocated=0;
	
	lineseg lineseg_list = null;
	lineseg lineseg_end = null;
	lineseg lineseg_stack = null;
	int next_lineseg_id=0;
	int lineseg_allocated=0;
	
	surface surface_list = null;
	surface surface_end = null;
	surface surface_stack = null;
	int next_surface_id=0;
	int surface_allocated=0;
	
	vector center_point = new vector();
	vector view_point = new vector(0.0, 0.0, 15.0);
	vector view_focus = new vector(0.0f, 0.0f, 5.0f);
	vector model_nearest = new vector(0.0f, 0.0f, 5.0f);
//	quaternion model_orient = new quaternion();
	quaternion camera_orient = new quaternion();
	quaternion error_orient = new quaternion();
											 
	double total_force = 1.0;
	short courseness = 32;
	int trim_depth=3;	//4;
	int contour_interval=2048;

	double view_scale=1.0f;
	
	Random rand;
	
	long start, elapse, timer_cnt, now[] = new long[10], accum[] = new long[10];
	
	int surfbydpthcnt[] = new int[20];	
	
	long tot_accum[] = new long[20];
	String log_time = "";

	surface render = null;

	dodeca(int seed)
	{
//	Dodecahedren Data, all faces are listed in a clockwise order
		timer_cnt = 0L;
		
		int dodeca_list[][] = 
		{
			{ 0,	1,	2 },	////	0	1	2	0	1
			{ 3,	4,	5 },	////	3	4	5	3	4
			{ 0,	6,	7 },	////	0	6	7	0	6
			{ 9,	8,	3 },	////	9	8	3	9	8
			{ 1,	4,	8 },	////	1	4	8	1	4

			{ 6,	9,	10 },	////	6	9	10	6	9
			{ 11,	1,	0 },	////	11	1	0	11	1
			{ 5,	7,	10 },	////	5	7	10	5	7
			{ 9,	6,	2 },	////	9	6	2	9	6
			{ 11,	7,	5 },	////	11	7	5	11	7
	
			{ 2,	8,	9 },	////	2	8	9	2	8
			{ 11,	5,	4 },	////	11	5	4	11	5
			{ 8,	2,	1 },	////	8	2	1	8	2
			{ 10,	9,	3 },	////	10	9	3	10	9
			{ 11,	4,	1 },	////	11	4	1	11	4

			{ 10,	7,	6 },	////	10	7	6	10	7
			{ 0,	7,	11 },	////	0	7	11	0	7
			{ 8,	4,	3 },	////	8	4	3	8	4
			{ 0,	2,	6 },	////	0	2	6	0	2
			{ 3,	5,	10 }	////	3	5	10	3	5
		};
		
		rand = new Random(seed);
		
		allocate_all(6000);
		deallocate_all();

		for (int i=0;i<20;i++)
		{
			surfbydpthcnt[i] = 0;
			tot_accum[i] = 0;
		}
	
		// Create vertices
		for (int i=0;i<12;i++)
		{
			tri[i] = vertice_allocate(0);
			tri[i].elevation = rnd(2000) + rnd(2000) + rnd(2000) + rnd(2000);
			tri[i].random_seed = rand.nextLong();
			tri[i].makeodd();
		}
		
		center_point.zero();

// Create surfaces and perimeter lines
		for (int i=0;i<20;i++)
		{
			dodeca[i] = surface_allocate(0);
			
			lineseg seg;
			seg = lineseg_allocate(0);
			dodeca[i].frst = seg.attach(dodeca[i], tri[dodeca_list[i][0]], tri[dodeca_list[i][(0+1)%3]]);

			seg = lineseg_allocate(0);
			dodeca[i].hypo = seg.attach(dodeca[i], tri[dodeca_list[i][1]], tri[dodeca_list[i][(1+1)%3]]);

			seg = lineseg_allocate(0);
			dodeca[i].last = seg.attach(dodeca[i], tri[dodeca_list[i][2]], tri[dodeca_list[i][(2+1)%3]]);
		}

// Determine neighbor data
		int neighbor[][] = new int[12][12];
		
		for (int i=0;i<12;i++)
			for (int j=0;j<12;j++)
				neighbor[i][j] = -1;
		
		for (int i=0;i<20;i++)
		{
			for (int j=0;j<3;j++)
			{
				int m = dodeca_list[i][j];
				int n = dodeca_list[i][(j+1)%3];
				{
					if (neighbor[m][n]==(-1))
						neighbor[m][n] = i;
					else
					{
						if (neighbor[n][m]!=(-1))
							System.out.println("No neighbor slot found");
						else
							neighbor[n][m] = i;
					}
				}
				m = n;
			}
		}
		
// Assign neighbors 
		for (int i=0;i<20;i++)
		{
			int m,n,k;
			
			m = dodeca_list[i][0];
			n = dodeca_list[i][(0+1)%3];
			k = neighbor[n][m];

			System.out.println("Neighbor " + i + " link " + k);

			if (k==(-1))
				System.out.println("No neighbor found");

			if (k>=0&&k!=i)	
			{
				dodeca[i].frst.join(dodeca[k].frst);
				dodeca[i].frst.join(dodeca[k].hypo);
				dodeca[i].frst.join(dodeca[k].last);

				dodeca[i].frst.circum = dodeca[i].hypo;

				if (dodeca[i].frst.back.poly != dodeca[k])
					System.out.println("neighbor doesn't match");
			}
			else
				System.out.println("Two unique neighbors not found");

			m = dodeca_list[i][1];
			n = dodeca_list[i][(1+1)%3];
			k = neighbor[n][m];

			System.out.println("Neighbor " + i + " link " + k);

			if (k==(-1))
				System.out.println("No neighbor found");

			if (k>=0&&k!=i)	
			{
				dodeca[i].hypo.join(dodeca[k].frst);
				dodeca[i].hypo.join(dodeca[k].hypo);
				dodeca[i].hypo.join(dodeca[k].last);

				dodeca[i].hypo.circum = dodeca[i].last;

				if (dodeca[i].hypo.back.poly != dodeca[k])
					System.out.println("neighbor doesn't match");
			}
			else
				System.out.println("Two unique neighbors not found");

			m = dodeca_list[i][2];
			n = dodeca_list[i][(2+1)%3];
			k = neighbor[n][m];

			System.out.println("Neighbor " + i + " link " + k);

			if (k==(-1))
				System.out.println("No neighbor found");

			if (k>=0&&k!=i)	
			{
				dodeca[i].last.join(dodeca[k].frst);
				dodeca[i].last.join(dodeca[k].hypo);
				dodeca[i].last.join(dodeca[k].last);

				dodeca[i].last.circum = dodeca[i].frst;

				if (dodeca[i].last.back.poly != dodeca[k])
					System.out.println("neighbor doesn't match");
			}
			else
				System.out.println("Two unique neighbors not found");

			elev(dodeca[i],0);
		}

		tri[0].at.set(		4.752306f,		1.105192f,		1.105192f );
		tri[1].at.set(		1.1051924f,		4.752306f,		1.1051921f );
		tri[2].at.set(		1.1051924f,		1.105192f,		4.752306f );
		tri[3].at.set(		-4.748478f,		-1.1014568f,	-1.101457f );
		tri[4].at.set(		-2.5615678f,	3.444807f,		-2.5615678f );
		tri[5].at.set(		-1.1014574f,	-1.1014569f,	-4.7484775f );
		tri[6].at.set(		2.55875f,		-3.4504695f,	2.55875f );
		tri[7].at.set(		3.4448068f,		-2.5615678f,	-2.5615678f );
		tri[8].at.set(		-3.4504693f,	2.5587502f,		2.5587502f );
		tri[9].at.set(		-2.5615678f,	-2.5615678f,	3.4448066f );
		tri[10].at.set(		-1.1014574f,	-4.7484775f,	-1.1014572f );
		tri[11].at.set(		2.5587502f,		2.55875f,		-3.4504695f );
		
//		center_point.set(1.0f,1.0f,1.0f);
	}

	surface GetFirst(int dpth)
	{
		surface surf = surface_list;
		
		while(surf!=null&&surf.depth!=dpth)
			surf = surf.snext;
		
		return surf;
	}

//
//	Reporting functions
//
///////////////////////////////
	void SurfaceCount(MyCamera cam, int dpth)
	{
		surface surf = surface_list;
		int i=0,d=0,in=0,out=0;
		
//		System.out.print(" Surf ");

		for (i=0;i<20;i++)
			surfbydpthcnt[i] = 0;

		while(surf!=null)
		{
			if (surf.depth>=0)
			{
				surfbydpthcnt[surf.depth]++;
				
				if (surf.prjtoview>-100.0)
				{
					in++;
					if (surf.depth==dpth) d++;
				}
				else out++;
			}
			surf = surf.snext;
		}


//		System.out.print(d);
//		System.out.print("/");
//		System.out.print(in);
//		System.out.print("/");
//		System.out.print(out);
	}

	void VecticeCount()
	{
		System.out.print(" Vertex_locked ");
		int i=0;
		vertice vert = vertice_list;
		while(vert!=null)
		{
			if (vert.depth>=0&&vert.locked)	i++;
			vert = vert.vnext;
		}
		
		System.out.print(i + "/" + vertice_allocated);
	}

	void LinesegCount()
	{
		int i=0;
		System.out.print(" Line ");
		System.out.print(lineseg_allocated);
		System.out.print("/");
		lineseg seg = lineseg_list;
		while(seg!=null)
		{
			i++;
			seg = seg.next;
		}

		System.out.println(i);
	}

	void timer_start()
	{
		for (int i=0;i<10;i++)
		{
			now[i] = 0;
			accum[i] = 0;
		}

		if (timer_cnt<=0) elapse = 0L;
		
		start = System.currentTimeMillis();
		now[0] = start;
	}
	
	private void timer_report(MyCamera cam, int dpth)
	{
		double t;
//		System.out.print(timer_cnt);
//		System.out.print(" Depth ");
//		System.out.print(dpth);
//		System.out.print("[");
//		System.out.print((int)20*Math.pow(3.,(double)dpth));
//		System.out.print("]");
//		System.out.print(" Avg ");
//		t =  elapse/timer_cnt;
//		System.out.print(t);
//		System.out.print("(");
//		System.out.print(System.currentTimeMillis()-start);
//		System.out.print(")");

		SurfaceCount(cam,dpth);
//		VecticeCount();
//		LinesegCount();
		
		for (int i=0;i<4;i++)
			tot_accum[i] += accum[i];
//		log_time = "Log_time: " + accum[0]/timer_cnt + " "
//						   + accum[1]/timer_cnt + " "
//						   + accum[2]/timer_cnt + " "
//						   + accum[3]/timer_cnt;
	}

	void timer_accum(int i)
	{
		if (i<1||i>9) return;
		
		now[i] = System.currentTimeMillis();
		accum[i-1] += now[i] - now[i-1];
	}
	
	void timer_finish(MyCamera cam, int depth)
	{
		elapse += (System.currentTimeMillis()-start);
		timer_cnt++;
		if (timer_cnt>=60)
		{
			timer_report(cam, depth);
			cam.SetColor(Color.black);
			int cnt0=0,cnt1=0;
			for (int i=0; i<depth; i++)
			{
				cam.g.drawString("LOD(" + i + "): " 
					+ surfbydpthcnt[i], 60, 80+(i*10) );
				cnt0 += surfbydpthcnt[i];
				cnt1 += ((int) (20*Math.pow(3,i)));
				if (surfbydpthcnt[i]<(20*Math.pow(3,i)))
					cam.g.drawString("" + ((int) (20*Math.pow(3,i))), 160, 80+(i*10) );
			}
//			cam.g.drawString("Surfs: " + next_surface_id +
//				" Vertice: " + next_vertice_id +
//				" Lineseg: " + next_lineseg_id, 100, 580 );
			cam.g.drawString("Surfs: " + surface_allocated +
				" Vertice: " + vertice_allocated +
 				" Lineseg: " + lineseg_allocated, 100, 580 );
			if (depth>0)
				cam.g.drawString("" + cnt0 + "    " 
					+ ((int) cnt0 - cnt1), 
					100, 80+(depth*10) );
		}
	}

	void time_report(Graphics g)
	{
		if (timer_cnt>0)
			log_time = "Log_time: " + tot_accum[0]/timer_cnt + " "
						   + tot_accum[1]/timer_cnt + " "
						   + tot_accum[2]/timer_cnt + " "
						   + tot_accum[3]/timer_cnt;
		g.drawString(log_time, 100, 540);
	}

	void refocus(MyCamera cam, int dpth)
	{
		cam.current_camera++;

		vector draw0 = new vector();
		vector draw1 = new vector();
		vector draw2 = new vector();

		draw0.set(view_focus);
		draw0.add(view_focus);
		draw0.sub(view_point);

		camera_orient.set( view_focus, draw0, center_point );

		model_nearest.set(view_point);
		model_nearest.SetMag(5.0);

		draw1.set(view_point);
		draw1.sub(model_nearest);
//		draw1.sub(view_focus);

		view_scale = (cam.viewscr_pos.mag() * 2.0) / draw1.mag();

		cam.setScale(1.0);

		error_orient.set(0.0, 0.0, 0.0, 0.0);
		
		draw0.CameraWorld( cam, this, center_point );
		draw1.CameraWorld( cam, this, view_point );
		draw2.set(cam.viewscr_pos);
		error_orient.set( draw0, draw1, draw2 );

		cam.Test(this, view_focus);
	}

// Apply Force to each vertice
	private void apply_force()
	{
		vertice vert = vertice_list;

		while(vert!=null)
		{
			if (vert.depth>=0)
			{
				vert.force.mult(0.5f);
				vert.at.add(vert.force);
				vert.at.sub(center_point);
				vert.force.zero();
			}
			vert = vert.vnext;
		}
		center_point.zero();
	}
		
// Update center_point
	private void center(int dpth)
	{
		vector temp1 = new vector(), temp2 = new vector(), temp3 = new vector();

		surface surf = surface_list;

		if (dpth<trim_depth)
		{
			center_point.zero();
	
			long surf_cnt=0;
	
			while(surf!=null)
			{
				if ((dpth<trim_depth&&surf.depth==dpth)||surf.depth==trim_depth-1)
				{
//					if (!surf.atv.locked)
						surf.face_at(temp1,temp2);
					center_point.add(surf.atv.at);
					surf_cnt++;
				}
				surf = surf.snext;
			}

			if (surf_cnt>0)
				center_point.mult(1.0/(float) surf_cnt);
		}
	}
	
// pull surface vertice
	private void pull_surface(int dpth)
	{
		vector temp1 = new vector(), temp2 = new vector();
		
		surface surf = surface_list;

		lineseg seg = null;
		
		while (surf!=null)
		{
			if (surf.depth>=0)
				surf.apply_force(temp1, temp2, dpth, trim_depth);
			surf = surf.snext;
		}
	}
	
// force vertice to a fixed distance from the center_point
	private void push_vertice()
	{
		vector temp1 = new vector(), temp2 = new vector(), temp3 = new vector();

		vertice vert = vertice_list;
		while (vert!=null)
		{
			if (vert.depth>=0&&!vert.locked)
			{
				float d = (float) center_point.dist2(vert.at);
				if (d>0.0&&vert.force.mag()>0.00000)	
				{
					temp2.set(vert.at);
					temp2.sub(center_point);
					double f = 5.0000f - temp2.mag();
					temp1.set(temp2);
					temp2.prj(vert.force);
					vert.force.sub(temp2);
					if (Math.abs(f)>0.00f)
					{
						temp1.SetMag(f);
						vert.force.add(temp1);
					}
				}
			}
			vert = vert.vnext;
		}
	}
	
	boolean inflate(int dpth)
	{
//		point temp1 = new point(), temp2 = new point(), temp3 = new point();

		if (testmode)
			lineseg_list.lineseg_list_validation(testmode,trim_depth);
		
		apply_force();
		center(dpth);
		pull_surface(dpth);		
		push_vertice();
		change(dpth);
		if (total_force<=(0.2f*(dpth+1))) return false;
		else return true;
	}
	
	void vertice_deallocate(vertice vert)
	{
		vert.vertice_deallocate();
		vert.stack = vertice_stack;
		vertice_stack = vert;
		vertice_allocated--;
	}
	
	vertice vertice_allocate(int dpth)
	{
		vertice_allocated++;
		vertice vert=null;
		
		if (vertice_stack==null)
		{
			vert = new vertice(-1);
			vert.id = next_vertice_id++;
			
			if (vertice_list==null)
			{
				vertice_list = vertice_end = vert;
			}
			else
			{
				vertice_end.vnext = vert;
				vertice_end = vert;
			}
			vertice_deallocate(vert);
			
			for (int i=0;i<9999;i++)
			{
				vert = new vertice(dpth);
				vertice_allocated++;
				vert.id = next_vertice_id++;
				vertice_end.vnext = vert;
				vertice_end = vert;
				vertice_deallocate(vert);
			}
		}

		vert = vertice_stack;
		vertice_stack = vertice_stack.stack;
		vert.stack = null;
		vert.depth = dpth;
		
		vert.at.zero();
		vert.force.zero();
		vert.locked = false;

		return vert;
	}

	void lineseg_deallocate(lineseg seg)
	{
		if (seg.child!=null)
		{
			seg.child.parent = null;
			surface_deallocate(seg.child);
		}

		seg.lineseg_deallocate();
		seg.stack = lineseg_stack;
		lineseg_stack = seg;
		lineseg_allocated--;
	}
	
	lineseg lineseg_allocate(int dpth)
	{
		lineseg_allocated++;
		if (lineseg_stack==null)
		{
			lineseg seg = new lineseg(dpth);
			seg.id = next_lineseg_id++;

			if (lineseg_list==null)
				lineseg_list = lineseg_end = seg;
			else
			{
				lineseg_end.next = seg;
				lineseg_end = seg;
			}

			for (int i=0;i<9999;i++)
			{
				lineseg_deallocate(seg);
				lineseg_allocated++;
				seg = new lineseg(dpth);
				seg.id = next_lineseg_id++;
				lineseg_end.next = seg;
				lineseg_end = seg;
			}
			
			return seg;
		}
		else
		{
			lineseg seg = lineseg_stack;
			lineseg_stack = lineseg_stack.stack;
			seg.stack = null;
			seg.depth = dpth;

			return seg;
		}
	}

	int rnd(int i)
	{
		return (rand.nextInt() % i);
	}

	int rgen(int i)
	{	
		if (i==0) return(0);
		i = Math.abs((int) i);
		i += (i>>2);
		return rnd(i);
	}

	void elev(surface surf, int dpth)
	{
		int a,b,c,e,s,t;
		if (dpth==-1)
			a = 0;				
		a = surf.frst.from.elevation;
		b = surf.hypo.from.elevation;
		c = surf.last.from.elevation;
		e = (courseness>>dpth) + courseness/(dpth+1);
		s = (a+b+c)/3;
		rand.setSeed(surf.frst.from.random_seed^surf.hypo.from.random_seed^surf.last.from.random_seed);
		surf.atv.random_seed = rand.nextLong();
		t = rgen(s-a) + rgen(s-b) + rgen(s-c);
		t += s + rnd(e) + rnd(e);
		surf.atv.elevation = t;
		surf.atv.makeodd();
	}
		
	void surface_deallocate(surface surf)
	{
		vertice_deallocate(surf.atv);

		if (surf.frst!=null)
		{
			lineseg_deallocate(surf.frst);
			surf.frst = null;
		}

		if (surf.hypo!=null)
		{
			lineseg_deallocate(surf.hypo);
			surf.hypo = null;
		}

		if (surf.last!=null)
		{
			lineseg_deallocate(surf.last);
			surf.last = null;
		}

		surf.surface_deallocate();
		surf.stack = surface_stack;
		surface_stack = surf;
		surface_allocated--;
		surf.prjtoview = -1000.0;
	}
	
	surface surface_allocate(int dpth)
	{
		surface_allocated++;

		surface surf = null;

		if (surface_stack==null)
		{
			surf = new surface(dpth);
			surf.id = next_surface_id++;
			surf.atv = vertice_allocate(dpth+1);

			if (surface_list==null)
				surface_list = surface_end = surf;
			else
			{
				surface_end.snext = surf;
				surface_end = surf;
			}

			for (int i=0;i<9999;i++)
			{
				surface_deallocate(surf);
				
				surf = new surface(dpth);
				surface_allocated++;
				surf.id = next_surface_id++;
				surf.atv = vertice_allocate(dpth+1);
				surface_end.snext = surf;
				surface_end = surf;
			}
		}
		else
		{
			surf = surface_stack;

			surface_stack = surface_stack.stack;
			surf.stack = null;
			surf.depth = dpth;
			surf.atv = vertice_allocate(dpth+1);
			surf.atv.at.zero();
		}
		if (dpth<trim_depth) 
			surf.prjtoview = 1.0;
		else
			surf.prjtoview = -1000;

		return surf;
	}
	
	void trim_helper(MyCamera cam, int dpth)
	{
		surface surf = surface_list;

//		if (testmode)
//		while (surf!=null)
//		{
//			if (surf.depth>=0)
//				surf.validate(dpth,testmode,trim_depth);
//			surf = surf.snext;
//		}

		surf = surface_list;

		while (surf!=null)
		{
			surf.mark = false;
			if (surf.depth>=0)
				surf.mark = cam.SetToward(this, dpth, surf, view_point);
			surf = surf.snext;
		}

		surf = surface_list;
		
		while (surf!=null)
		{
			if (surf.mark&&surf.depth>=0
//				&&((cam.current_camera-surf.valid)>>2)<(surf.inview)
				)
					surf.set_neighbor_inview(5);
//			surf.mark = false;
			surf = surf.snext;
		}

		surf = surface_list;
		
		while (surf!=null)
		{
			if (surf.depth>trim_depth&&surf.prjtoview<-5) 	// -3 max or trim_helper() fails
			{
				if (cam.SetToward(this, dpth, surf, view_point))
					System.out.println("Bad surface_deallocate\n");
				else
					surface_deallocate(surf);
			}
			surf.mark = false;
			surf = surf.snext;
		}
	}

	void collect_loose()
	{
		surface surf = surface_list;
		
		surface closest = null;
		
		while (surf!=null)
		{
			if (surf.depth==trim_depth)
			{
				if (closest!=null)
				{
					if (view_focus.dist2(closest.atv.at)
						>view_focus.dist2(surf.atv.at))
							closest = surf;
				}
				else
					closest = surf;
			}
			surf.mark = false;
			surf = surf.snext;
		}

		while (closest!=null&&closest.frst!=null&&closest.frst.child!=null)
		{
			closest = closest.frst.child;
			if (closest!=null)
			{
				MarkNeighbors(closest);

				surf = surface_list;
				while (surf!=null)
				{
					if (surf.depth==closest.depth&&surf.mark!=true)
						surface_deallocate(surf);
					surf = surf.snext;
				}
			}
		}
	}
	
	void fracture_helper(MyCamera cam, int dpth)
	{
		vector temp1 = new vector(), temp2 = new vector();
		
		if (testmode)
			lineseg_list.lineseg_list_validation(testmode, trim_depth);

		lineseg seg = lineseg_list;

// for each lineseg build the child surface and its perimeter linesegs
		while (seg!=null)
		{
			if (seg.depth==dpth&&seg.child==null)
				seg.child = seg.build_child(this, dpth, temp1, temp2);
			seg = seg.next;
		}

// For each new line find back line
		surface surf = surface_list;

		surf = surface_list;

		while (surf!=null)
		{
			if (surf.mark&&surf.depth==dpth+1)
			{
				seg = surf.parent.back;
				if (seg!=null)
					if (seg.back!=null)
						if (seg.back.child!=null)
							seg.link_back();
			}
			surf.mark = false;
			surf = surf.snext;
		}

//		surf = surface_list;

//		while (surf!=null&&testmode)
//		{
//			if (surf.depth>=0)
//				surf.validate(dpth,testmode,trim_depth);
//			surf = surf.snext;
//		}

		if (testmode)
			lineseg_list.lineseg_list_validation(testmode,trim_depth);

		total_force = 5.0f;
	}
	
	void fracture(MyCamera cam, int dpth)
	{
		if (dpth<trim_depth)
		{
			fracture_helper(cam, dpth);
		}
		else
		for (int i=trim_depth;i<=dpth;i++)
		{
			fracture_helper(cam, i);
			trim_helper(cam, i);
		}
	}
	
	void deallocate_all()
	{
		surface surf = surface_list;
		
		while (surf!=null)
		{
			if (surf.depth>=0)
				surface_deallocate(surf);
			surf = surf.snext;
		}

		lineseg seg = lineseg_list;
		
		while (seg!=null)
		{
			if (seg.depth>=0)
				lineseg_deallocate(seg);
			seg = seg.next;
		}

		vertice vert = vertice_list;

		while (vert!=null)
		{
			if (vert.depth>=0)
				vertice_deallocate(vert);
			vert = vert.vnext;
		}
	}		
	
	void allocate_all(int count)
	{
		for (int i=0;i<count;i++)
		{
			vertice vert = vertice_allocate(511);
			vertice_deallocate(vert);
			lineseg seg = lineseg_allocate(511);
			lineseg_deallocate(seg);
			seg = lineseg_allocate(511);
			lineseg_deallocate(seg);
			seg = lineseg_allocate(511);
			lineseg_deallocate(seg);
			surface surf = surface_allocate(511);
			surface_deallocate(surf);
		}
	}

	void setScale(MyCamera cam, int dpth)
	{
		point temp1 = new point(), temp2 = new point();

		vertice v = vertice_list;

		while (v!=null&&v.depth!=dpth)
			v = v.vnext;
		
		if (v==null) return;
		
		double c = center_point.dist(v.at);
		if (c==0.0f)
			total_force = 0.0f;

		cam.setScale((int) ((cam.getSize()*1.0f)/c));
	}
	
	boolean drawShapeZ(MyCamera cam, boolean front_only, int dpth)
	{
		if (render==null)
		{
//			setScale(cam, dpth);
			cam.SetColor(Color.white);
			cam.fillRect();
			render = surface_list;
		}		

		cam.SetColor(Color.black);
		int m=0;

		while (render!=null&&m<6000)
		{
			if (render.depth>=0)
			{
			// test level of detail
				if (render.depth==dpth)
				// draw surface
					render.surface_draw(cam, this, dpth, trim_depth, contour_interval, courseness);	//	,SCALE,SIZE);
				else
			// test sea
				if (render.depth<7&&render.atv.elevation<0&&dpth>=trim_depth&&render.prjtoview>0.0)
				{
				// draw sea at all levels 
					cam.DrawTo( this, Color.blue, render.atv.at, render.atv.at );
				}
			}
			render = render.snext;
			m++;
		}

//		cam.setColor(Color.red);
		vector from, to;
		
		from = new vector();
		to = new vector();

		from.set(0.0f, 0.0f, 5.0f);
		to.set(5.0f, 0.0f, 0.0f);
		cam.DrawTo( this, Color.red, from, to );

		from.set(0.0f, 0.0f, 5.0f);
		to.set(0.0f, 5.0f, 0.0f);
		cam.DrawTo( this, Color.red, from, to );

		from.set(5.0f, 0.0f, 0.0f);
		to.set(0.0f, 5.0f, 0.0f);
		cam.DrawTo( this, Color.red, from, to );

		from.set(0.0f, 0.0f, 0.0f);
		to.set(5.0f, 0.0f, 0.0f);
		cam.DrawTo( this, Color.red, from, to );

		from.set(0.0f, 0.0f, 0.0f);
		to.set(0.0f, 5.0f, 0.0f);
		cam.DrawTo( this, Color.blue, from, to );

		from.set(0.0, 0.0, 0.0);
		to.set(0.0, 0.0, 5.0);
		cam.DrawTo( this, Color.green, from, to );

		from.set(view_focus);
		to.set(view_focus);
		to.add(0.000, 0.000, 0.0000020);
		cam.DrawTo( this, Color.red, from, to );

		from.set(view_focus);
		from.add(0.000, -0.000001, 0.0000010);
		to.set(view_focus);
		to.add(0.000, 0.000001, 0.0000010);
		cam.DrawTo( this, Color.red, from, to );

		from.set(view_focus);
		from.add(-0.000001, 0.000, 0.0000010);
		to.set(view_focus);
		to.add(0.000001, 0.000, 0.0000010);
		cam.DrawTo( this, Color.red, from, to );

		from.set(center_point);
		to.set(model_nearest);
		to.SetMag(5.0f);
		cam.DrawTo( this, Color.black, from, to );
		
		if (render==null)
			return true;
		else
			return false;
	}
	
	double change(int dpth)	//	, point temp1, point temp2)
	{
		total_force = 0.0;
		boolean found=false;
		double work;
		
		vertice vtmp = null;
		vertice v = vertice_list;

		while (v!=null)
		{
			if (v.depth>=0)
			{
				work = v.force.dist2(0.0,0.0,0.0);
				v.locked = (work<0.000001);
				if (v.depth==dpth)	// &&!v.locked)
					total_force += work;
				else if (v.depth<=trim_depth)
					v.locked = false;
			}
			v = v.vnext;
		}
		
		return total_force;
	}

	void MarkNeighbors(surface poly)
	{
		lineseg seg,seglist=null,segtop;
		surface surf;

		System.out.println("MarkNeighbors");
		seglist = segtop = poly.frst;
		segtop.tmplst = poly.hypo;	segtop = segtop.tmplst;
		segtop.tmplst = poly.last;	segtop = segtop.tmplst;
		poly.mark = true;
		
		do
		{
			seg = seglist;
			seglist = seglist.tmplst;
			if (seg!=null&&seg.back!=null&&seg.back.poly!=null)
			{
				surf = seg.back.poly;
				if (surf.mark==true) continue;
				if (surf.depth!=poly.depth) continue;
				surf.mark = true;
				
				segtop.tmplst = surf.frst;	segtop = segtop.tmplst;
				segtop.tmplst = surf.hypo;	segtop = segtop.tmplst;
				segtop.tmplst = surf.last;	segtop = segtop.tmplst;
			}
		}
		while(seglist!=segtop);
	}
}

