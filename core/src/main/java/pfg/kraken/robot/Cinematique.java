/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.robot;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;
import pfg.kraken.utils.XY_RW;

/**
 * Une structure qui regroupe des infos de cinématique
 * 
 * @author pf
 *
 */

public class Cinematique implements Serializable
{
	private static final long serialVersionUID = 1548985891767047059L;
	protected final XY_RW position = new XY_RW();
	public volatile double orientationGeometrique; // il s'agit de l'orientation
													// qui avance. donc
													// l'arrière du robot s'il
													// recule
	public volatile boolean enMarcheAvant;
	public volatile double courbureGeometrique;
	public volatile double orientationReelle;
	public volatile double courbureReelle;
	public volatile boolean stop;
	private final static NumberFormat formatter = new DecimalFormat("#0.000");
	
	public Cinematique(XYO xyo)
	{
		updateReel(xyo.position.getX(), xyo.position.getY(), xyo.orientation, 0);
	}
	
	public XYO getXYO()
	{
		return new XYO(position, orientationReelle);
	}
	
	public Cinematique(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbure, boolean stop)
	{
		update(x, y, orientationGeometrique, enMarcheAvant, courbure, stop);
	}

	/**
	 * Constructeur par copie
	 * 
	 * @param cinematique
	 */
/*	public Cinematique(Cinematique cinematique)
	{
		enMarcheAvant = cinematique.enMarcheAvant;
		courbureGeometrique = cinematique.courbureGeometrique;
	}*/

	/**
	 * Cinématique vide
	 */
	public Cinematique()
	{}

	/**
	 * Copie this dans autre
	 * 
	 * @param autre
	 */
	public synchronized void copy(Cinematique autre)
	{
		synchronized(autre)
		{
			position.copy(autre.position);
			autre.orientationGeometrique = orientationGeometrique;
			autre.orientationReelle = orientationReelle;
			autre.enMarcheAvant = enMarcheAvant;
			autre.courbureGeometrique = courbureGeometrique;
			autre.courbureReelle = courbureReelle;
			autre.stop = stop;
		}
	}

	public final XY getPosition()
	{
		return position;
	}

/*	public final XY_RW getPositionEcriture()
	{
		return position;
	}*/

	@Override
	public String toString()
	{
		return position + ", " + formatter.format(orientationReelle) + ", " + (enMarcheAvant ? "marche avant" : "marche arrière") + ", courbure : " + formatter.format(courbureReelle)+ (stop ? ", stop" : "");
	}

	@Override
	public int hashCode()
	{
		// TODO faire quelque chose de propre…
		
		// Il faut fusionner les points trop proches pour pas que le PF ne
		// s'entête dans des coins impossibles
		// Par contre, il ne faut pas trop fusionner sinon on ne verra pas les
		// chemins simples et ne restera que les compliqués

		int codeSens = 0;
		if(enMarcheAvant)
			codeSens = 1;
		int codeCourbure, codeOrientation;
		if(courbureReelle < -3)
			codeCourbure = 0;
		// else if(courbureReelle < -2)
		// codeCourbure = 1;
		else if(courbureReelle < 0)
			codeCourbure = 2;
		// else if(courbureReelle < 2)
		// codeCourbure = 3;
		else if(courbureReelle < 3)
			codeCourbure = 4;
		else
			codeCourbure = 5;

		// System.out.println("codeCourbure : "+codeCourbure+", "+courbure);
		orientationReelle = orientationReelle % (2 * Math.PI);
		if(orientationReelle < 0)
			orientationReelle += 2 * Math.PI;
		else if(orientationReelle > 2 * Math.PI)
			orientationReelle -= 2 * Math.PI;

		codeOrientation = (int) (orientationReelle / (Math.PI / 6));
		// System.out.println("codeOrientation : "+codeOrientation+"
		// "+orientation);

		// return ((((((int)(position.getX()) + 1500) / 100) * 2000 +
		// (int)(position.getY()) / 100) * 2 + codeSens) * 16 + codeOrientation)
		// * 6 + codeCourbure;

		return ((((((int) (position.getX()) + 1500) / 30) * 200 + (int) (position.getY()) / 30) * 2 + codeSens) * 16 + codeOrientation) * 6 + codeCourbure;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.hashCode() == hashCode();
	}

	/**
	 * Met à jour la cinématique à partir d'info réelle
	 * 
	 * @param x
	 * @param y
	 * @param orientationGeometrique
	 * @param enMarcheAvant
	 * @param curvature
	 */
	public void updateReel(double x, double y, double orientationReelle, double courbureReelle)
	{
		if(enMarcheAvant)
		{
			orientationGeometrique = orientationReelle;
			courbureGeometrique = courbureReelle;
		}
		else
		{
			orientationGeometrique = orientationReelle + Math.PI;
			courbureGeometrique = -courbureReelle;
		}

		position.setX(x);
		position.setY(y);
		this.orientationReelle = orientationReelle;
		this.courbureReelle = courbureReelle;
	}

	protected void update(double x, double y, double orientationGeometrique, boolean enMarcheAvant, double courbureGeometrique, boolean stop)
	{
		if(enMarcheAvant)
		{
			orientationReelle = orientationGeometrique;
			courbureReelle = courbureGeometrique;
		}
		else
		{
			orientationReelle = orientationGeometrique + Math.PI;
			courbureReelle = -courbureGeometrique;
		}

		position.setX(x);
		position.setY(y);
		this.stop = stop;
		this.orientationGeometrique = orientationGeometrique;
		this.enMarcheAvant = enMarcheAvant;
		this.courbureGeometrique = courbureGeometrique;
	}

	/**
	 * Doit être évité à tout prix
	 */
	@Override
	public Cinematique clone()
	{
		Cinematique out = new Cinematique();
		copy(out);
		return out;
	}
	
	public void update(ItineraryPoint p)
	{
		enMarcheAvant = p.goingForward;
		updateReel(p.x, p.y, p.orientation, p.curvature);
		stop = p.stop;
	}
}
