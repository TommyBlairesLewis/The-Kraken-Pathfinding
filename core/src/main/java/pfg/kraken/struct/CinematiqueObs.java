/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.struct;

import java.io.Serializable;

import pfg.kraken.memory.MemPoolState;
import pfg.kraken.memory.Memorizable;
import pfg.kraken.obstacles.RectangularObstacle;

/**
 * Une cinématique + un obstacle associé
 * 
 * @author pf
 *
 */

public final class CinematiqueObs implements Memorizable, Serializable
{
	public final Cinematique cinem;
	private static final long serialVersionUID = 1L;
	public volatile RectangularObstacle obstacle;
	public volatile double maxSpeed; // in m/s
	public volatile double possibleSpeed; // in m/s

	public static final double[] maxSpeedLUT;

	private volatile int indiceMemory;

	static
	{
		maxSpeedLUT = new double[500];
		maxSpeedLUT[0] = Double.MAX_VALUE;
		for(int i = 1; i < 500; i++)
			maxSpeedLUT[i] = Math.sqrt(10. / i);
	}
	
	public String toString()
	{
		return super.toString()+", maxSpeed = "+maxSpeed;
	}
	
	public CinematiqueObs(RectangularObstacle vehicleTemplate)
	{
		cinem = new Cinematique();
		obstacle = vehicleTemplate.clone();
		maxSpeed = -1;
	}

	public void copy(CinematiqueObs autre)
	{
		cinem.copy(autre.cinem);
		autre.maxSpeed = maxSpeed;
		autre.possibleSpeed = possibleSpeed;
		obstacle.copy(autre.obstacle);
	}

	@Override
	public void setIndiceMemoryManager(int indice)
	{
		indiceMemory = indice;
	}

	@Override
	public int getIndiceMemoryManager()
	{
		return indiceMemory;
	}

	/**
	 * Met à jour les données
	 * 
	 * @param x
	 * @param y
	 * @param orientation
	 * @param enMarcheAvant
	 * @param courbure
	 * @param vitesseMax
	 * @param obstacle
	 * @param r
	 */
	public void update(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure, double rootedMaxAcceleration, boolean stop)
	{
		cinem.update(x, y, orientationGeometrique, enMarcheAvant, courbure, stop);
		maxSpeed = rootedMaxAcceleration * maxSpeedLUT[(int) Math.round(Math.abs(10*courbure))];
		obstacle.update(cinem.position, cinem.orientationReelle);
	}
	
	public void updateWithMaxSpeed(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure, double rootedMaxAcceleration, double maxSpeed, boolean stop)
	{
		cinem.update(x, y, orientationGeometrique, enMarcheAvant, courbure, stop);
		this.maxSpeed = Math.min(maxSpeed, rootedMaxAcceleration * maxSpeedLUT[(int) Math.round(Math.abs(10*courbure))]);
		obstacle.update(cinem.position, cinem.orientationReelle);
	}
	
/*	public void updateReel(double x, double y, double orientationReelle, double courbure, double rootedMaxAcceleration)
	{
		super.updateReel(x, y, orientationReelle, courbure);
		maxSpeed = rootedMaxAcceleration * maxSpeedLUT[(int) Math.round(Math.abs(10*courbure))];
		obstacle.update(position, orientationReelle);
	}*/

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
	
	@Override
	public CinematiqueObs clone()
	{
		CinematiqueObs out = new CinematiqueObs(obstacle);
		copy(out);
		return out;
	}

	public void update(ItineraryPoint p)
	{
		cinem.update(p);
		maxSpeed = p.maxSpeed;
		possibleSpeed = p.possibleSpeed;
		obstacle.update(cinem.position, cinem.orientationReelle);
	}
}
