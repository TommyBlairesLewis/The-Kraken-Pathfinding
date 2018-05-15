/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.struct.XY;

import java.io.IOException;

/**
 * A navmesh, used by the D* Lite.
 * It can load and save a navmesh. If necessary, it generate a new one.
 * 
 * @author pf
 *
 */

public final class Navmesh
{
	public TriangulatedMesh mesh;
	
	public Navmesh(Config config, StaticObstacles obs, NavmeshComputer computer)
	{
		String filename = config.getString(ConfigInfoKraken.NAVMESH_FILENAME);
		try {
			mesh = TriangulatedMesh.loadNavMesh(filename);
			if(mesh.obsHashCode != obs.hashCode())
				throw new NullPointerException("different obstacles ("+mesh.obsHashCode+" != "+obs.hashCode()+")"); // l'objectif est juste d'entrer dans le catch ci-dessous…
			if(!computer.checkNavmesh(mesh))
				throw new NullPointerException("invalid navmesh");
		}
		catch(IOException | ClassNotFoundException | NullPointerException e)
		{
			mesh = computer.generateNavMesh(obs);
			try {
				mesh.saveNavMesh(filename);
			}
			catch(IOException e1)
			{
			}
		}
		assert mesh != null;
	}
	
	@Override
	public String toString()
	{
		return mesh.toString();
	}
	
	public NavmeshNode getNearest(XY position)
	{
		NavmeshNode bestNode = null;
		double smallestDistance = 0;
		for(NavmeshNode n : mesh.nodes)
		{
			double candidateDistance = position.squaredDistance(n.position);
			if(bestNode == null || candidateDistance < smallestDistance)
			{
				bestNode = n;
				smallestDistance = candidateDistance;
			}
		}
		assert bestNode != null;
		return bestNode;
	}

}
