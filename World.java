
/*
 * @(#)World.java 1.0 01/01/20
 *
 * You can modify the template of this file in the
 * directory ..\JCreator\Templates\Template_2\Project_Name.java
 *
 * You can also create your own project template by making a new
 * folder in the directory ..\JCreator\Template\. Use the other
 * templates as examples.
 *
 */

import java.awt.*;
import java.applet.*;


public class World extends Applet implements Runnable
{
	static Image bufferI0,bufferI1;
	static Graphics bufferG0,bufferG1;
	static dodeca dodeca = null;
	static short i;
	static int depth;
	static boolean busyflag = false;
	boolean finished=false;
	static MyCamera cam = null;
	Graphics lastbuffer=null;
	String sStatus = "Welcome to world";
	
	public void init()
	{
		i = 0;
		depth = 0;
		bufferI0 = createImage(600,600);
		bufferG0 = bufferI0.getGraphics();
		bufferG0.setColor(Color.white);
		bufferG0.fillRect(0,0,600,600);
		bufferI1 = createImage(600,600);
		bufferG1 = bufferI1.getGraphics();
		bufferG1.setColor(Color.white);
		bufferG1.fillRect(0,0,600,600);

		cam = new MyCamera(bufferG0,0,0,600,600);

		dodeca = new dodeca(1);
		dodeca.view_point.set(1.30, 1.30, 6.00);
		dodeca.view_focus.set(1.442, 0.7875, 5.000);
		dodeca.view_focus.SetMag(5.0);

		Thread animator = new Thread(this);
	    animator.start();

	}

	private void cycle_buffer(Graphics g, Image i_buf, Graphics g_buf )
	{
		g_buf.setColor(Color.black);
		g_buf.fillOval(i%(600-30),0,30,30);

		g_buf.drawString("Welcome to Java!!", 50, 60 );

		g.drawImage(i_buf,0,0,null);

		dodeca.time_report(g);
	}
	
	public void paint(Graphics g)
	{
		if (cam.g==bufferG0)
			cycle_buffer(g, bufferI1, bufferG1);
		else
			cycle_buffer(g, bufferI0, bufferG0);
	}

	void braking(float time, vector position, vector momentum)
	{
		vector p = new vector(position);
		vector m = new vector(momentum);
		
		m.mult(time);
		p.add(m);
		if (p.mag()<5.0)
			momentum.mult(1.0 - (1.0/time));
		else
//			momentum.mult(0.9675);
			momentum.mult(0.965);
	}	

	public void run()
	{	
		vector motion = new vector();
		vector orbital_axis = new vector(1.0f, 1.0f, 0.0f);
		vector position = new vector();
		vector momentum = new vector();
		double mass=100.0;
		int allocated=0;
	
		motion.set_to_cross_of(dodeca.view_point,orbital_axis);
//		orbital_axis.set_to_cross_of(dodeca.view_point,motion);
		orbital_axis.set(dodeca.view_focus);
//		motion.SetMag(1.0);
//		momentum.set(motion);
		dodeca.refocus(cam,0);
		dodeca.timer_accum(1);
		
		while (true) 
		{
			i += 5;
			try 
			{
			    Thread.sleep( 20 ); // + (int) Math.pow((double) dodeca.next_vertice_id,0.60));
			} catch (InterruptedException e) {}
		
			boolean buildflag=false;

			dodeca.timer_accum(3);

			if (!busyflag)
			{
				busyflag = true;
				lastbuffer = cam.g;
				repaint();
				busyflag = false;
			}
			
			if (!buildflag)
			{
				buildflag = true;

				dodeca.timer_finish(cam, depth);

				if (finished=dodeca.drawShapeZ(cam,true,depth))
				{
					if (cam.g==bufferG0)
						cam.SetMyCamera(bufferG1);
					else
						cam.SetMyCamera(bufferG0);
				}

				dodeca.timer_start();

				if (depth>dodeca.trim_depth
					&&dodeca.surface_allocated+12500<allocated)
				{
					dodeca.collect_loose();
					allocated = dodeca.surface_allocated;
				}

				if (!dodeca.inflate(depth)&&allocated>0)
				{
					if (dodeca.surface_allocated<(10000+(1000*depth))
						&&lastbuffer!=cam.g)
					{
						dodeca.render = null;
						dodeca.collect_loose();
						allocated = 0;
						depth++;
					}
				}
				dodeca.timer_accum(1);

				if (finished)
				{
// inertia
					float time=10.0f;
				
					mass = 100.0/(dodeca.view_point.mag()-5.0);
					momentum.mult(mass);
// orbital force
//					motion.set_to_cross_of(momentum, orbital_axis);
//					motion.SetMag((dodeca.view_point.mag()-5.0)/200.0f);
//					motion.SetMag(0.001/mass);
//					momentum.add(motion);
// attractor force
					motion.set(dodeca.view_focus);
					motion.sub(dodeca.view_point);
					motion.SetMag((motion.mag()+0.001)/time);
					momentum.add(motion);
					momentum.mult(1.0/mass);
					braking(time, dodeca.view_point, momentum);
// move view_point
					position.set(dodeca.view_point);
					position.add(momentum);
// keep view_point above ground
					double altitude = position.mag();
					if (altitude < 5.000001)
						position.SetMag(5.000001);
					dodeca.view_point.set(position);
// refocus
					dodeca.refocus(cam,depth);
// refracture
					dodeca.fracture(cam, depth-1);
					if (allocated==0)
						allocated = dodeca.surface_allocated;
				}
				buildflag = false;
				dodeca.timer_accum(2);
			}
		}
	}
}
