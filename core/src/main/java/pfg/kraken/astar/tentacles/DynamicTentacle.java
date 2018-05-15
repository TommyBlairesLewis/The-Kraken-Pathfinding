/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.List;

import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.struct.CinematiqueObs;

/**
 * Arc courbe de longueur inconnue à l'avance
 * 
 * @author pf
 *
 */

public final class DynamicTentacle extends Tentacle
{
	public List<CinematiqueObs> arcs;

	public DynamicTentacle(List<CinematiqueObs> arcs, TentacleType v)
	{
		vitesse = v;
		this.arcs = arcs;
	}

	@Override
	public int getNbPoints()
	{
		return arcs.size();
	}

	@Override
	public CinematiqueObs getPoint(int indice)
	{
		return arcs.get(indice);
	}

	@Override
	public CinematiqueObs getLast()
	{
		return arcs.get(arcs.size() - 1);
	}
}
