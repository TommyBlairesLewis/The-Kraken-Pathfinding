/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles;

import java.util.ArrayList;
import java.util.List;

import pfg.kraken.struct.XY;
import pfg.kraken.struct.XY_RW;

/**
 * Rectangle ayant subi une rotation.
 * Ce rectangle peut être le robot, ou bien l'espace que parcourera le robot
 * pendant un segment
 * 
 * @author pf
 *
 */

public class RectangularObstacle extends Obstacle
{
	// Position est le centre de rotation

	private static final long serialVersionUID = 7643797598957137648L;
	// Longueur entre le centre et un des coins
	protected double demieDiagonale;
	protected XY_RW centreGeometrique;

	// calcul des positions des coins
	// ces coins sont dans le repère de l'obstacle !
	protected XY coinBasGauche;
	protected XY coinHautGauche;
	protected XY coinBasDroite;
	protected XY coinHautDroite;

	// ces coins sont dans le repère de la table
	protected XY_RW coinBasGaucheRotate;
	protected XY_RW coinHautGaucheRotate;
	protected XY_RW coinBasDroiteRotate;
	protected XY_RW coinHautDroiteRotate;

	protected XY_RW in = new XY_RW();

	protected double angle, cos, sin;

	public RectangularObstacle(XY position, int sizeX, int sizeY)
	{
		this(position, sizeX, sizeY, 0);
	}

	/**
	 * Cet angle est celui par lequel le rectangle a été tourné.
	 * C'est donc l'opposé de l'angle par lequel on va tourner les points afin
	 * de considérer
	 * le rectangle comme aligné
	 * Le rectangle est centré sur la position
	 * 
	 * @param log
	 * @param config
	 * @param position
	 * @param sizeX
	 * @param sizeY
	 * @param angle
	 */
	public RectangularObstacle(XY position, int sizeX, int sizeY, double angle)
	{
		this(position, sizeX / 2, sizeX / 2, sizeY / 2, sizeY / 2, angle);
	}

	public RectangularObstacle(int distanceToFront, int distanceToBack, int distanceToLeft, int distanceToRight)
	{
		this(distanceToFront, distanceToBack, distanceToLeft, distanceToRight, 0);
	}

	public RectangularObstacle(int distanceToFront, int distanceToBack, int distanceToLeft, int distanceToRight, double tiltAngle)
	{
		this(new XY(0,0), distanceToBack, distanceToFront, distanceToLeft, distanceToRight, tiltAngle);
	}
	
	public RectangularObstacle(XY position, int sizeLeftX, int sizeRightX, int sizeUpY, int sizeDownY, double angle)
	{
		this(position, new XY(sizeRightX, sizeUpY), new XY(-sizeLeftX, -sizeDownY), angle);
	}

	private RectangularObstacle(XY position, XY topRightCorner, XY bottomLeftCorner, double angle)
	{
		// topRightCorner et bottomLeftCorner sont dans le repère de l'obstacle !
		super(position);
		this.angle = angle;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		coinBasGauche = bottomLeftCorner.clone();
		coinHautGauche = new XY(bottomLeftCorner.getX(), topRightCorner.getY());
		coinBasDroite = new XY(topRightCorner.getX(), bottomLeftCorner.getY());
		coinHautDroite = topRightCorner.clone();
		coinBasGaucheRotate = new XY_RW();
		coinHautGaucheRotate = new XY_RW();
		coinBasDroiteRotate = new XY_RW();
		coinHautDroiteRotate = new XY_RW();
		convertitVersRepereTable(coinBasGauche, coinBasGaucheRotate);
		convertitVersRepereTable(coinHautGauche, coinHautGaucheRotate);
		convertitVersRepereTable(coinBasDroite, coinBasDroiteRotate);
		convertitVersRepereTable(coinHautDroite, coinHautDroiteRotate);
		centreGeometrique = coinBasDroiteRotate.plusNewVector(coinHautGaucheRotate).scalar(0.5);
		demieDiagonale = topRightCorner.distance(bottomLeftCorner) / 2;
	}

	public RectangularObstacle clone()
	{
		return new RectangularObstacle(position, coinHautDroite, coinBasGauche, angle);
	}
	
	/**
	 * Effectue la rotation d'un point, ce qui équivaut à la rotation de cet
	 * obstacle,
	 * ce qui équivaut à le faire devenir un ObstacleRectagularAligned
	 * On utilise ici -angle, ce qui explique que la formule n'est pas la
	 * formule de rotation traditionnelle.
	 * 
	 * @param point
	 * @return
	 */
	protected void convertitVersRepereObstacle(XY point, XY_RW out)
	{
		out.setX(cos * (point.getX() - position.getX()) + sin * (point.getY() - position.getY()));
		out.setY(-sin * (point.getX() - position.getX()) + cos * (point.getY() - position.getY()));
	}

	/**
	 * Rotation dans le sens +angle
	 * Passe du repère de l'obstacle au repère de la table
	 * 
	 * @param point
	 * @return
	 */
	protected void convertitVersRepereTable(XY point, XY_RW out)
	{
		out.setX(cos * point.getX() - sin * point.getY() + position.getX());
		out.setY(sin * point.getX() + cos * point.getY() + position.getY());
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * 
	 * @param point
	 * @return
	 */
	protected double getXConvertiVersRepereObstacle(XY point)
	{
		return cos * (point.getX() - position.getX()) + sin * (point.getY() - position.getY());
	}

	/**
	 * Donne l'abscisse du point après rotation de +angle
	 * 
	 * @param point
	 * @return
	 */
	protected double getYConvertiVersRepereObstacle(XY point)
	{
		return -sin * (point.getX() - position.getX()) + cos * (point.getY() - position.getY());
	}

	/**
	 * Calcul s'il y a collision avec un ObstacleRectangularAligned.
	 * Attention! Ne pas utiliser un ObstacleRectangular au lieu de
	 * l'ObstacleRectangularAligned!
	 * Utilise le calcul d'axe de séparation
	 * 
	 * @param r
	 * @return
	 */
	@Override
	public boolean isColliding(RectangularObstacle r)
	{
		// Calcul simple permettant de vérifier les cas absurdes où les
		// obstacles sont loin l'un de l'autre
		if(centreGeometrique.squaredDistance(r.centreGeometrique) >= (demieDiagonale + r.demieDiagonale) * (demieDiagonale + r.demieDiagonale))
			return false;
		// Il faut tester les quatres axes
		return !testeSeparation(coinBasGauche.getX(), coinBasDroite.getX(), getXConvertiVersRepereObstacle(r.coinBasGaucheRotate), getXConvertiVersRepereObstacle(r.coinHautGaucheRotate), getXConvertiVersRepereObstacle(r.coinBasDroiteRotate), getXConvertiVersRepereObstacle(r.coinHautDroiteRotate)) && !testeSeparation(coinBasGauche.getY(), coinHautGauche.getY(), getYConvertiVersRepereObstacle(r.coinBasGaucheRotate), getYConvertiVersRepereObstacle(r.coinHautGaucheRotate),
				getYConvertiVersRepereObstacle(r.coinBasDroiteRotate), getYConvertiVersRepereObstacle(r.coinHautDroiteRotate)) && !testeSeparation(r.coinBasGauche.getX(), r.coinBasDroite.getX(), r.getXConvertiVersRepereObstacle(coinBasGaucheRotate), r.getXConvertiVersRepereObstacle(coinHautGaucheRotate), r.getXConvertiVersRepereObstacle(coinBasDroiteRotate), r.getXConvertiVersRepereObstacle(coinHautDroiteRotate)) && !testeSeparation(r.coinBasGauche.getY(), r.coinHautGauche.getY(), r
						.getYConvertiVersRepereObstacle(coinBasGaucheRotate), r.getYConvertiVersRepereObstacle(coinHautGaucheRotate), r.getYConvertiVersRepereObstacle(coinBasDroiteRotate), r.getYConvertiVersRepereObstacle(coinHautDroiteRotate));
	}

	/**
	 * Teste la séparation à partir des projections.
	 * Vérifie simplement si a et b sont bien séparés de a2, b2, c2 et d2,
	 * c'est-à-dire s'il existe x tel que a < x, b < x et
	 * a2 > x, b2 > x, c2 > x, d2 > x
	 * 
	 * @param a
	 * @param b
	 * @param a2
	 * @param b2
	 * @param c2
	 * @param d2
	 * @return
	 */
	private final boolean testeSeparation(double a, double b, double a2, double b2, double c2, double d2)
	{
		double min1 = Math.min(a, b);
		double max1 = Math.max(a, b);

		double min2 = Math.min(Math.min(a2, b2), Math.min(c2, d2));
		double max2 = Math.max(Math.max(a2, b2), Math.max(c2, d2));

		return min1 > max2 || min2 > max1; // vrai s'il y a une séparation
	}

	@Override
	public String toString()
	{
		return "ObstacleRectangulaire " + coinBasGaucheRotate + " " + coinBasDroiteRotate + " " + coinHautGaucheRotate + " " + coinHautDroiteRotate + " " + super.toString();
	}

	/**
	 * Fourni la plus petite distance au carré entre le point fourni et
	 * l'obstacle
	 * 
	 * @param in
	 * @return la plus petite distance au carré entre le point fourni et
	 * l'obstacle
	 */
	@Override
	public double squaredDistance(XY v)
	{
		convertitVersRepereObstacle(v, in);

		if(in.getX() < coinBasGauche.getX() && in.getY() < coinBasGauche.getY())
			return in.squaredDistance(coinBasGauche);

		if(in.getX() < coinHautGauche.getX() && in.getY() > coinHautGauche.getY())
			return in.squaredDistance(coinHautGauche);

		if(in.getX() > coinBasDroite.getX() && in.getY() < coinBasDroite.getY())
			return in.squaredDistance(coinBasDroite);

		if(in.getX() > coinHautDroite.getX() && in.getY() > coinHautDroite.getY())
			return in.squaredDistance(coinHautDroite);

		if(in.getX() > coinHautDroite.getX())
			return (in.getX() - coinHautDroite.getX()) * (in.getX() - coinHautDroite.getX());

		if(in.getX() < coinBasGauche.getX())
			return (in.getX() - coinBasGauche.getX()) * (in.getX() - coinBasGauche.getX());

		if(in.getY() > coinHautDroite.getY())
			return (in.getY() - coinHautDroite.getY()) * (in.getY() - coinHautDroite.getY());

		if(in.getY() < coinBasGauche.getY())
			return (in.getY() - coinBasGauche.getY()) * (in.getY() - coinBasGauche.getY());

		// Sinon, on est dans l'obstacle
		return 0;
	}

	public double getDemieDiagonale()
	{
		return demieDiagonale;
	}

	@Override
	public XY[] getExpandedConvexHull(double expansion, double longestAllowedLength)
	{
		double coeff = expansion / demieDiagonale;
		XY[] coins = new XY[] {coinBasDroiteRotate.minusNewVector(centreGeometrique).scalar(coeff).plus(coinBasDroiteRotate),
				coinHautDroiteRotate.minusNewVector(centreGeometrique).scalar(coeff).plus(coinHautDroiteRotate),
				coinHautGaucheRotate.minusNewVector(centreGeometrique).scalar(coeff).plus(coinHautGaucheRotate),
				coinBasGaucheRotate.minusNewVector(centreGeometrique).scalar(coeff).plus(coinBasGaucheRotate)};
		List<XY> points = new ArrayList<XY>();
		
		
		for(int i = 0; i < 4; i++)
			points.add(coins[i]);
		
		int nbTotalPoints = 0;
		for(int i = 0; i < 4; i++)
		{
			XY pointA = coins[i];
			XY pointB = coins[(i+1)%4];
			double distance = pointA.distance(pointB);
			int nbPoints = (int) Math.ceil(distance / longestAllowedLength);
			double delta = distance / nbPoints;
			for(int j = 1; j < nbPoints; j++)
				points.add(pointB.minusNewVector(pointA).scalar((j * delta) / distance).plus(pointA));
			nbTotalPoints += nbPoints;
		}
		XY[] out = new XY[nbTotalPoints];
		points.toArray(out);
		return out;
	}

	@Override
	public boolean isInObstacle(XY pos)
	{
		convertitVersRepereObstacle(pos, in);
		if(in.getX() >= coinBasGauche.getX() && in.getX() <= coinHautDroite.getX() && in.getY() >= coinBasGauche.getY() && in.getY() <= coinHautDroite.getY())
			return true;
		return false;
	}
	
	@Override
	public boolean isColliding(XY pointA, XY pointB)
	{
		if(XY.segmentIntersection(pointA, pointB, coinBasGaucheRotate, coinHautGaucheRotate)
				|| XY.segmentIntersection(pointA, pointB, coinHautGaucheRotate, coinHautDroiteRotate)
				|| XY.segmentIntersection(pointA, pointB, coinHautDroiteRotate, coinBasDroiteRotate)
				|| XY.segmentIntersection(pointA, pointB, coinBasDroiteRotate, coinBasGaucheRotate))
			return true;

	    // dernière possibilité, A ou B dans le cercle
	    return isInObstacle(pointA) || isInObstacle(pointB);
	}
	

	/**
	 * Met à jour cet obstacle
	 * 
	 * @param obstacle
	 */
	public void copy(RectangularObstacle obstacle)
	{
		obstacle.update(position, angle);
	}
	
	public RectangularObstacle update(XY position, double orientation)
	{
		return update(position.getX(), position.getY(), orientation);
	}
	
	public RectangularObstacle update(double x, double y, double orientation)
	{
		this.position.setX(x);
		this.position.setY(y);
		this.angle = orientation;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		convertitVersRepereTable(coinBasGauche, coinBasGaucheRotate);
		convertitVersRepereTable(coinHautGauche, coinHautGaucheRotate);
		convertitVersRepereTable(coinBasDroite, coinBasDroiteRotate);
		convertitVersRepereTable(coinHautDroite, coinHautDroiteRotate);
		coinBasDroiteRotate.copy(centreGeometrique);
		centreGeometrique = centreGeometrique.plus(coinHautGaucheRotate).scalar(0.5);

		return this;
	}

}
