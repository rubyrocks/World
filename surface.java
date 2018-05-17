import java.awt.*;
import java.lang.Math;
import java.lang.Object;
import java.util.*;

import point;
import vertice;
import lineseg;

	class surface	// a surface is described by three points and describes a point (at)
	{				// a surface has three neigbhors
		lineseg frst;
		lineseg hypo;
		lineseg last;
		int id = -1;						// unique surface ID
		vertice atv = null;					// point at the center of the surface
		surface stack = null;				// pointer to the next available surface
		surface snext = null;				// pointer to the next pointer
		int depth = -1;						// level of detail
		vector faceat = new vector();		// a normalize vertor perpendicular to the surface
		lineseg parent = null;				// pointer to the parent lineseg
		double prjtoview=-1000.0;				// =0 for forward facing surface, +1 adjacency
		boolean mark = false;				// processed flag
		int valid = -1;						// valid current_camera id

//
//	Surface Constructor
//
//////////////////////////		
		surface(int dpth)
		{
			frst = null;
			hypo = null;
			last = null;
			depth = dpth;
			valid = -1;
		}

//
//	face_at - determine a vector perpendicular to this surface
//
//////////////////////////
		void face_at(vector temp1, vector temp2)	// center between it's describing points
		{
			vector p0 = frst.from.at, p1 = hypo.from.at, p2 = last.from.at;

			atv.at.set(p0);
			atv.at.add(p1);
			atv.at.add(p2);
			atv.at.mult(0.3333333333);
			temp1.set(p0);
			temp2.set(p2);
			
			temp1.sub(p1);
			temp2.sub(p1);
			
			faceat.set_to_cross_of(temp1,temp2);
			faceat.normalize();
			faceat.mult(0.0000001);
			return;
		}

//
//	validate - test this surface for validity
//
//////////////////////////
/***	boolean validate(int dpth, boolean testmode, int trim_depth)
		{
			if (!testmode&&dpth>=trim_depth) return true;
			for (int i=0;i<3;i++)
			{
				boolean test0=false;
				boolean test1=false;

				test0 = false;
				test1 = false;
				if (side[i].back!=null)
				{
					if (side[i].back.poly!=null)
					{
						if (side[i].back.poly!=this)
							for (int j=0;j<3;j++)
							{
								if (side[i].back.poly.side[j].from==side[i].from)
									test0=true;
								if (side[i].back.poly.side[j].to==side[i].to)
									test1=true;
							}
		
						if (!(test0&&test1))
						{
							System.out.println("Surface Neighbor Error");
							return false;
						}
					}
				}
			}
			return true;
		}
***/

//
//	surface_draw - draw this surface
//
//////////////////////		
		void surface_draw(MyCamera cam, dodeca model, int dpth, int trim_depth, int contour_interval, int courseness)
		{
			vector draw1 = new vector(), draw2 = new vector();
			vector cline[] = new vector[2];
			int i,k;
			
		// Draw Triangle
			if (prjtoview>-0.1)
			{
				if (dpth<trim_depth)
				{
						cam.DrawTo( model, Color.black, frst.from.at, frst.to.at );
						cam.DrawTo( model, Color.black, hypo.from.at, hypo.to.at );
						cam.DrawTo( model, Color.black, last.from.at, last.to.at );
				}
				else
				{
		// Draw outline of terrain
					int bot=50000,top=-50000;
					top = Math.max(frst.from.elevation,hypo.from.elevation);
					top = Math.max(top,last.from.elevation);
					bot = Math.min(frst.from.elevation,hypo.from.elevation);
					bot = Math.min(bot,last.from.elevation);

					bot -= (bot%contour_interval);
					if (bot<0) bot = 0;

					if (top>=0)
					{
						int e;
						e = 0;
						k = 0;
						boolean a = frst.from.elevation>e;
						boolean b = hypo.from.elevation>e;
						boolean c = last.from.elevation>e;
						
						if (a^b)
							cline[k++] = frst.interpulate(e + (frst.from.elevation+frst.to.elevation)>>2);	// Draw Terrain

						if (b^c)
							cline[k++] = hypo.interpulate(e + (hypo.from.elevation+hypo.to.elevation)>>2);	// Draw Terrain

						if (k==2)
							cam.DrawTo(model, Color.black, cline[0], cline[1] );
						else
						{
							if (c^a)
								cline[k++] = last.interpulate(e + (last.from.elevation+last.to.elevation)>>2);	// Draw Terrain

							if (k==2)
							{	
								cam.DrawTo( model, Color.black, cline[0], cline[1] );
							}
						}
					}

		// Draw Sea Horizon
					if (frst.back!=null)
						if (frst.back.poly!=null)
							if (prjtoview<0.1)
							{
								cam.DrawTo( model, Color.red, frst.from.at, frst.to.at );
								vector to = new vector(model.view_point);
								to.sub(frst.to.at);
								to.SetMag(1.0);
								to.add(frst.to.at);
								cam.DrawTo( model, Color.magenta, frst.to.at, to );
							}
						
					if (hypo.back!=null)
						if (hypo.back.poly!=null)
							if (prjtoview<0.1)
								cam.DrawTo( model, Color.red, hypo.from.at, hypo.to.at );

					if (last.back!=null)
						if (last.back.poly!=null)
							if (prjtoview<0.1)
								cam.DrawTo( model, Color.red, last.from.at, last.to.at );
				}
			}
		}

//
//	surface_deallocate - deallocate this surface
//
//////////////////////		
		void surface_deallocate()
		{
			valid=-1;
			atv = null;
			if (parent!=null)
			{
				parent.child = null;
				parent = null;
			}
			prjtoview = -1000;
			if (depth<0) 
				return;
			depth = -1;
		}

//
//	set_neighbor_inview - set inview to the number of steps over the horizon
//
/////////////////////////
		void set_neighbor_inview(int span,int nghbrhd)
		{
			prjtoview = Math.max(prjtoview, span + 1 - nghbrhd);
			if (span>=0)
			{
				span--;

				surface surf;
				if (frst.back!=null)
				{
					surf = frst.back.poly;
					if (surf!=null)
						if (!surf.mark)
							if (surf.prjtoview<(span + 1 - nghbrhd))
								surf.set_neighbor_inview(span,nghbrhd);
				}

				if (hypo.back!=null)
				{
					surf = hypo.back.poly;
					if (surf!=null)
						if (!surf.mark)
							if (surf.prjtoview<(span + 1 - nghbrhd))
								surf.set_neighbor_inview(span,nghbrhd);
				}
			
				if (last.back!=null)
				{
					surf = last.back.poly;
					if (surf!=null)
						if (!surf.mark)
							if (surf.prjtoview<(span + 1 - nghbrhd))
								surf.set_neighbor_inview(span,nghbrhd);
				}
			}
		}
		
//
//	set_neighbor_inview - identify those surfaces just over the horizon
//
/////////////////////////
		void set_neighbor_inview(int nghbrhd)
		{
			set_neighbor_inview(nghbrhd,nghbrhd);
		}

//
//	build0 - build a surface
//
/////////////////////////
		void build0(dodeca d, lineseg parent, int dpth)
		{
			lineseg seg;
			
			if (last==null)
			{
				seg = d.lineseg_allocate(dpth+1);
				last = seg.attach(this, parent.from, parent.back.poly.atv);
			}
			
			if (frst==null)
			{
				seg = d.lineseg_allocate(dpth+1);
				frst = seg.attach(this, parent.back.poly.atv, parent.poly.atv);
			}
			if (hypo==null)
			{
				seg = d.lineseg_allocate(dpth+1);
				hypo = seg.attach(this, parent.poly.atv, parent.from);
			}
			frst.circum = hypo;
			hypo.circum = last;
			last.circum = frst;
		}

		void join(lineseg thisside, lineseg thatside)
		{
			thatside.back = thisside;
			thisside.back = thatside;
		}

		void apply_force(vector temp1, vector temp2, int dpth, int trim_depth)
		{
			frst.apply_force(atv, temp1, temp2, dpth, trim_depth);
			hypo.apply_force(atv, temp1, temp2, dpth, trim_depth);
			last.apply_force(atv, temp1, temp2, dpth, trim_depth);
		}

	}

