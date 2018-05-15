/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.types;

import java.awt.Color;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.ClothoidesComputer;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.struct.Cinematique;

/**
 * Arc de clothoïde qui fait un demi-tour
 * FIXME UNUSED
 * @author pf
 *
 */

public enum TurnoverTentacle implements TentacleType
{
	DEMI_TOUR_DROITE(ClothoTentacle.DROITE_2),
	DEMI_TOUR_GAUCHE(ClothoTentacle.GAUCHE_2);

	public ClothoTentacle v;

	private TurnoverTentacle(ClothoTentacle v)
	{
		this.v = v;
	}

	@Override
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
		// on évite les demi-tours absurdes
		if(((v.positif && c.courbureGeometrique < -1) || (!v.positif && c.courbureGeometrique > 1)))
			return false;

		return true;
	}

	@Override
	public int getNbArrets(boolean firstMove)
	{
		return 1;
	}
	
	@Override
	public Color getColor()
	{
		return Color.GRAY;
	}

	@Override
	public Class<? extends TentacleComputer> getComputer()
	{
		return ClothoidesComputer.class;
	}
	
	@Override
	public double getComputationalCost()
	{
		return 2;
	}

}
