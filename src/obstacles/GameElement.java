package obstacles;

import enums.GameElementNames;
import enums.Tribool;
import smartMath.Vec2;
import utils.Log;

/**
 * Un élément de jeu.
 * On demande à ce qu'il soit circulaire afin de faciliter les calculs de collision.
 * @author pf
 *
 */

public class GameElement extends ObstacleCircular
{
	private Tribool done = Tribool.FALSE; 
	private GameElementNames nom;
	
	public GameElement(Log log, Vec2 position, int rayon, GameElementNames nom)
	{
		super(log, position, rayon);
		this.position = position;
		this.nom = nom;
	}
	
	public void setDone(Tribool etat)
	{
		done = etat;
	}
	
	public Tribool isDone()
	{
		return done;
	}
	
	/**
	 * Attention, ce clone ne modifie que ce qui peut changer, c'est-à-dire done.
	 * Utilisé pour mettre à jour rapidement un objet.
	 * @param other
	 */
	public void fastClone(GameElement other)
	{
		other.done = done;
	}

	public void clone(GameElement other)
	{
		other.position = position.clone();
		other.done = done;
	}
	
	public GameElementNames getName()
	{
		return nom;
	}

}