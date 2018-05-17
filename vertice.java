import java.awt.*;
import java.lang.Math;
import java.lang.Object;
import java.util.*;

import MyCamera;

import vertice;

class vertice
{
	int id=-1;
	vector at = new vector();
	vector force = new vector();
	int elevation=0;
	long random_seed;
	int depth=-1;
	boolean locked=false;
	vertice stack = null;
	vertice vnext = null;
	boolean valid = false;
	
	vertice(int dpth)
	{
		depth = dpth;
	}

	void makeodd()
	{
		if (elevation%2==1)
			return;
		else
			elevation += 1;
	}

	void vertice_deallocate()
	{
		if (depth<0) return;
		depth = -1;
	}
}
