/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.dstarlite.navmesh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The mesh itself
 * @author pf
 *
 */

public final class TriangulatedMesh implements Serializable
{
	private static final long serialVersionUID = 1L;
	public final int obsHashCode;
	public final NavmeshNode[] nodes;
	public final NavmeshEdge[] edges;
	public final NavmeshTriangle[] triangles;
	
	public TriangulatedMesh(NavmeshNode[] nodes, NavmeshEdge[] edges, NavmeshTriangle[] triangles, int obsHashCode)
	{
		this.nodes = nodes;
		this.edges = edges;
		this.triangles = triangles;
		this.obsHashCode = obsHashCode;
	}

	public static TriangulatedMesh loadNavMesh(String filename) throws IOException, ClassNotFoundException
	{
		InputStream is = TriangulatedMesh.class.getResourceAsStream(filename);
		if(is == null)
			is = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(is);
		TriangulatedMesh o = (TriangulatedMesh) ois.readObject();
		ois.close();
		for(NavmeshEdge e : o.edges)
			e.loadFromSave(o.nodes);
		for(NavmeshNode n : o.nodes)
			n.loadFromSave(o.edges);		
		for(NavmeshTriangle t : o.triangles)
			t.loadFromSave(o.edges);
		return o;
	}

	public void saveNavMesh(String filename) throws IOException
	{
		FileOutputStream fichier;
		ObjectOutputStream oos;

		new File(filename).createNewFile();
		fichier = new FileOutputStream(filename);
		oos = new ObjectOutputStream(fichier);
		oos.writeObject(this);
		oos.flush();
		oos.close();
	}
	
	@Override
	public String toString()
	{
		String out = "Triangulated mesh : \n";
		for(int i = 0; i < triangles.length; i++)
			out += triangles[i].toString()+"\n";
		return out;
	}
}
