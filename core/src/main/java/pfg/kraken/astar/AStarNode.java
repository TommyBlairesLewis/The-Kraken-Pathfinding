/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar;

import pfg.kraken.astar.tentacles.DynamicTentacle;
import pfg.kraken.astar.tentacles.StaticTentacle;
import pfg.kraken.astar.tentacles.Tentacle;
import pfg.kraken.memory.MemPoolState;
import pfg.kraken.memory.Memorizable;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.RobotState;

/**
 * A node of the A*.
 * 
 * @author pf
 *
 */

public final class AStarNode implements Memorizable
{
	public RobotState robot; // the cinematic state + the duration since the beginning of the search
	public int g_score; // distance du point de départ à ce point
	public int f_score; // g_score + heuristique = meilleure distance qu'on
							// peut espérer avec ce point
	public AStarNode parent; // the parent of this node (used for reconstruction when a path is found)
	
	/*
	 * If a node has a parent, then we must have the arc between the parent and the node
	 * There are two types of tentacles : fixed-length and random-length.
	 * The fixed-length tentacle is always instantiated (for performance reason)
	 * 
	 * To know with arc to look, checkout the getArc() method
	 */
	public final StaticTentacle cameFromArcStatique; 
	public DynamicTentacle cameFromArcDynamique = null;
	
	/*
	 * Used by the memory pool
	 */
	private volatile int indiceMemoryManager;

	/**
	 * Dummy node
	 */
	public AStarNode()
	{
		cameFromArcStatique = null;
		robot = null;
		state = null;
	}
	
	public AStarNode(RobotState robot, RectangularObstacle vehicleTemplate)
	{
		cameFromArcStatique = new StaticTentacle(vehicleTemplate);
		this.robot = robot;
	}

	public Tentacle getArc()
	{
		if(parent == null)
			return null;
		if(cameFromArcDynamique != null)
			return cameFromArcDynamique;
		assert cameFromArcStatique != null;
		return cameFromArcStatique;
	}

	/**
	 * Initialisation for the A* (only used for the start point)
	 */
	public void init()
	{
		g_score = Integer.MAX_VALUE;
		f_score = Integer.MAX_VALUE;
		robot.initDate();
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.hashCode() == hashCode();
	}

	@Override
	public int hashCode()
	{
		return robot.getCinematique().hashCode();
	}

	/**
	 * Used by the memory pool
	 */
	@Override
	public void setIndiceMemoryManager(int indice)
	{
		indiceMemoryManager = indice;
	}

	/**
	 * Used by the memory pool
	 */
	@Override
	public int getIndiceMemoryManager()
	{
		return indiceMemoryManager;
	}
	
	private volatile MemPoolState state = MemPoolState.FREE;
	
	@Override
	public void setState(MemPoolState state)
	{
		this.state = state;
	}
	
	@Override
	public MemPoolState getState()
	{
		return state;
	}
}
