import java.awt.*;
import java.lang.Math;
import java.lang.Object;
import java.util.*;

import point;
import vertice;
import surface;

	class lineseg	// A lineseg connects two points and seperates two surfaces
	{
		int id=-1;
		surface child = null;		// child surface
		surface poly = null;		// this polygon 
		vertice from = null;		// a vertice of the neighbor surfaces
		vertice to = null;			// a vertice of the neighbor surfaces

		lineseg back = null;		// lineseg to->from where back.poly is adjacent to poly, seperated by this/back
		lineseg circum = null;		// next lineseg on the poly perimeter
		lineseg next = null;
		lineseg stack = null;
		lineseg tmplst = null;
		
		int depth = -1;
		boolean valid = false;

		lineseg(int dpth)
		{
			this.depth = dpth;
		}
		
		void adj_valid()
		{
			if (to==null)
				System.out.println("to not set");

			if (circum!=null)
				if (circum.circum!=null)
					if (from!=circum.circum.to)
						System.out.println("not cirnavigated");

			if (back!=null)
			{
				if (back.poly==null)
					System.out.println("back.poly not set");
				if (from!=back.to||to!=back.from)
					System.out.println("not adjacent");
			}
		}

		boolean lineseg_list_validation(boolean testmode, int trim_depth)
		{
			lineseg seg = this;
			boolean flag=true;
			
			if (!testmode) return true;

			while (seg!=null)
			{
				if (seg.depth>=0)
				{
					if (seg.depth>=0)
						seg.adj_valid();
					
					boolean test00=false;
					boolean test01=false;
					boolean test10=false;
					boolean test11=false;
			
					if (seg.depth<trim_depth)
					{
						if (seg.poly.frst.from==seg.from)
							test00=true;
						if (seg.poly.frst.from==seg.to)
							test01=true;

						if (seg.poly.hypo.from==seg.from)
							test00=true;
						if (seg.poly.hypo.from==seg.to)
							test01=true;

						if (seg.poly.last.from==seg.from)
							test00=true;
						if (seg.poly.last.from==seg.to)
							test01=true;

						if (seg.from==null)
							System.out.println("from not set");

						if (seg.to==null)
							System.out.println("to not set");

						if (seg.back!=null)
						{
							if (seg.back.poly==null)
								System.out.println("poly not set");

							if (!(seg.back.from==seg.to)
								||!(seg.back.to==seg.from))
									System.out.println("LineSeg Error");
						}
						else
							System.out.println("back not set");
					}
				}
				seg = seg.next;
			}
		
			return flag;
		}

		vector interpulate( int e )
		{
			vector mark = new vector(from.at);
			mark.interpulate(from.at, to.at,
				Math.abs(e-from.elevation)
				/((float) Math.abs(to.elevation-from.elevation)));
			return mark;
		}

		void lineseg_deallocate()
		{
			if (circum!=null)
			{
				if (circum.circum!=null)
					circum.circum.circum = null;
				circum = null;
			}

			if (back!=null)
			{
				back.back = null;
				back = null;
			}

			child = null;
			from = null;
			to = null;
			poly = null;
			circum = null;
			
			if (depth<0) 
				return;

			depth = -1;
		}

		void link_back()
		{
			if (circum!=null)
				if (circum.child!=null)
						circum.child.hypo.join(back.child.last);					

			if (child!=null)
				child.frst.join(back.child.frst);

			lineseg seg = back.circum.circum.back;

			if (seg!=null)
				if (seg.child!=null)
					seg.child.last.join(back.child.hypo);
		}

		void join(lineseg seg)
		{
			if (this.to==seg.from)
			{
				this.back = seg;
				seg.back = this;
			}
		}

		lineseg attach(surface surf,
				vertice vec0, vertice vec1)
		{
			from = vec0;
			to = vec1;
			poly = surf;
			return this;
		}

		void apply_force(vector temp1, vector temp2, int dpth, int trim_depth)
		{
			temp1.set(from.at);
			temp2.set(to.at);
			temp2.sub(temp1);
			temp2.mult(0.25f);
			from.force.add(temp2);
		}

		void apply_force(vertice atv, vector temp1, vector temp2)
		{
			temp1.set(atv.at);
			temp2.set(from.at);
			temp2.sub(temp1);
			temp2.mult(0.25f);
			atv.force.add(temp2);
		}

		void apply_force(vertice atv, vector temp1, vector temp2, int dpth, int trim_depth)
		{
			if (!from.locked&&(dpth<trim_depth||from.depth>to.depth))
				apply_force(temp1, temp2, dpth, trim_depth);
			if (!atv.locked)
				apply_force(atv, temp1, temp2);
		}

		surface build_child(dodeca d, int dpth, vector temp1, vector temp2)
		{
			if (back!=null)
			{
				surface surf = d.surface_allocate(dpth+1);
				surf.mark = true;
				surf.build0(d, this, dpth);

				surf.parent = this;
				d.elev(surf,dpth+1);		
//				if (!surf.atv.locked)
					surf.face_at(temp1,temp2);
//				seg.child = surf;					// link the lineseg to the lineseg-surface
				return surf;
			}
			else return child;
		}
	}
