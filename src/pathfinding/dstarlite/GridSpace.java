package pathfinding.dstarlite;

import java.util.ArrayList;
import java.util.BitSet;

import obstacles.ObstaclesFixes;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstacleProximity;
import pathfinding.ChronoGameState;
import pathfinding.astarCourbe.AStarCourbeNode;
import table.GameElementNames;
import table.Table;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;
import container.Service;
import enums.Tribool;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding DStarLite.
 * Notifie quand il y a un changement d'obstacles
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIteratorPresent iteratorDStarLite;
	private ObstaclesIteratorPresent iteratorRemoveNearby;
	private ObstaclesMemory obstaclesMemory;
	private Table table;

	private int distanceApproximation;
	private int distanceMinimaleEntreProximite;
	private int rayonRobot;

	public class PointDirige
	{
		public int point;
		public Direction dir;

		public PointDirige(int point, Direction dir) {
			this.point = point;
			this.dir = dir;
		}
		
		@Override
		public int hashCode()
		{
			return (point << 3) + dir.ordinal();
		}
		
		@Override
		public boolean equals(Object d)
		{
			return d instanceof PointDirige && hashCode() == ((PointDirige)d).hashCode();
		}
	}
	
	public enum Direction
	{
		NO,SE,NE,SO,
		N,S,O,E;
	}
	
	/**
	 * Comme on veut que le DStarLite recherche plus de noeuds qu'il n'y en aurait besoin, ce coeff ne vaut pas 1
	 */
//	private static final int COEFF_HEURISTIQUE = 2;

	public static final int PRECISION = 6;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.)+1;
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./(NB_POINTS_POUR_TROIS_METRES-1);
	public static final int DISTANCE_ENTRE_DEUX_POINTS_1024 = (int)(1024*3000./(NB_POINTS_POUR_TROIS_METRES-1));
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	private static final int X_MAX = NB_POINTS_POUR_TROIS_METRES - 1;
	private static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES - 1;
	
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private static BitSet grilleStatique = null;
	
	private static ArrayList<PointDirige> masque = new ArrayList<PointDirige>();
	private static int centreMasque;
	private long deathDateLastObstacle;
	
	public GridSpace(Log log, ObstaclesMemory obstaclesMemory, Table table)
	{
		this.obstaclesMemory = obstaclesMemory;
		this.log = log;
		this.table = table;
		iteratorDStarLite = new ObstaclesIteratorPresent(log, obstaclesMemory);
		iteratorRemoveNearby = new ObstaclesIteratorPresent(log, obstaclesMemory);
		
		if(grilleStatique == null)
		{
			// Initialisation, une fois pour toutes, de la grille statique
			grilleStatique = new BitSet(NB_POINTS);
			for(int i = 0; i < NB_POINTS; i++)
				for(ObstaclesFixes o : ObstaclesFixes.values)
				{
					if(o.getObstacle().squaredDistance(computeVec2(i)) < (int)(DISTANCE_ENTRE_DEUX_POINTS/2 * DISTANCE_ENTRE_DEUX_POINTS/2))
					{
//						log.debug(i+" : obstacle");
						grilleStatique.set(i);
						break; // on ne vérifie pas les autres obstacles
					}
//					log.debug(i+" : pas obstacle");
				}
			log.debug("Grille statique initialisée");
		}
	}
	
	/**
	 * On utilise la distance octile pour l'heuristique (surtout parce que c'est rapide)
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public static final int distanceHeuristiqueDStarLite(int pointA, int pointB)
	{
		int dx = Math.abs((pointA & (NB_POINTS_POUR_TROIS_METRES - 1)) - (pointB & (NB_POINTS_POUR_TROIS_METRES - 1))); // ceci est un modulo
		int dy = Math.abs((pointA >> PRECISION) - (pointB >> PRECISION)); // ceci est une division
		return 1000 * Math.max(dx, dy) + 414 * Math.min(dx, dy);
//		return (int) Math.round(COEFF_HEURISTIQUE * 1000 * Math.hypot(dx, dy));
	}

	private Direction convertToDirection(int deltaX, int deltaY)
	{
		if(deltaY == 1)
		{
			if(deltaX == -1)
				return Direction.NO; // NO
			else if(deltaX == 0)
				return Direction.N; // N
			else
				return Direction.NE; // NE
		}
		else if(deltaY == 0)
		{
			if(deltaX == -1)
				return Direction.O; // O
			else if(deltaX == 0)
			{
				log.critical("Erreur : direction nulle");
				return null;
			}
			else
				return Direction.E; // E
		}
		else
		{
			if(deltaX == -1)
				return Direction.SO; // SO
			else if(deltaX == 0)
				return Direction.S; // S
			else
				return Direction.SE; // SE
		}
	}
	
	/**
	 * Récupère le voisin de "point" dans la direction indiquée.
	 * Renvoie -1 si un tel voisin est hors table
	 * @param point
	 * @param direction
	 * @return
	 */
	public static int getGridPointVoisin(int point, Direction direction)
	{
		int x = point & (NB_POINTS_POUR_TROIS_METRES - 1);
		int y = point >> PRECISION;

		switch(direction)
		{
			case NO:
				if(x > 0 && y < Y_MAX)
					return point + NB_POINTS_POUR_TROIS_METRES - 1;
				return -1; // hors table
	
			case SE:
				if(x < X_MAX && y > 0)
					return point - NB_POINTS_POUR_TROIS_METRES + 1;
				return -1; // hors table
	
			case NE:
				if(x < X_MAX && y < Y_MAX)
					return point + NB_POINTS_POUR_TROIS_METRES + 1;
				return -1; // hors table
	
			case SO:
				if(x > 0 && y > 0)
					return point - NB_POINTS_POUR_TROIS_METRES - 1;
				return -1; // hors table
	
			case N:
				if(y < Y_MAX)
					return point + NB_POINTS_POUR_TROIS_METRES;
				return -1; // hors table
	
			case S:
				if(y > 0)
					return point - NB_POINTS_POUR_TROIS_METRES;
				return -1; // hors table
	
			case O:
				if(x > 0) // en fait, on pourrait directement renvoyer point - 1 dans tous les cas…
					return point - 1;
				return -1; // hors table
	
			case E:
			default:
				if(x < X_MAX)
					return point + 1;
				return -1; // hors table
		}
		
	}

	@Override
	public void useConfig(Config config)
	{
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);		
		distanceMinimaleEntreProximite = config.getInt(ConfigInfo.DISTANCE_BETWEEN_PROXIMITY_OBSTACLES);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		int rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		int rayonPoint = (int) Math.round((rayonEnnemi + rayonRobot) / DISTANCE_ENTRE_DEUX_POINTS);
		int tailleMasque = 2*(rayonPoint+1)+1;
		centreMasque = tailleMasque / 2;
		for(int i = 0; i < tailleMasque; i++)
			for(int j = 0; j < tailleMasque; j++)
				if((i-centreMasque) * (i-centreMasque) + (j-centreMasque) * (j-centreMasque) > rayonPoint*rayonPoint)
					for(int a = -1; a <= 1; a++)
						for(int b = -1; b <= 1; b++)
						{
							if(a == 0 && b == 0)
								continue;
							Direction dir = convertToDirection(a, b);
							int i2 = i + a, j2 = j + b;
							if((i2-centreMasque) * (i2-centreMasque) + (j2-centreMasque) * (j2-centreMasque) <= rayonPoint*rayonPoint)
								masque.add(new PointDirige(((j << PRECISION) +i), dir));
						}
/*		log.debug("Taille du masque : "+masque.size());
		System.out.println("Masque : ");
		for(int i = 0; i < tailleMasque; i++)
		{
			for(int j = 0; j < tailleMasque; j++)
			{
				boolean aff = false;
				for(Integer c : masque)
				{
					if((i << PRECISION) + j == (c & ((1 << DEUXIEME_POINT_COUPLE) - 1)))
					{
						aff = true;
						System.out.print("X");
						break;
					}
				}
				if(!aff)
					System.out.print(".");
			}
			System.out.println();
		}*/
	}

	@Override
	public void updateConfig(Config config)
	{}

	/**
	 * Signale si on peut passer d'un point à un de ses voisins.
	 * On suppose que ce voisin n'est pas hors table (sinon, ça lève une exception)
	 * @param gridpoint
	 * @param direction
	 * @return
	 */
	private boolean isTraversableStatique(int gridpoint, Direction direction)
	{
		int voisin = getGridPointVoisin(gridpoint, direction);
		return voisin != -1 && !grilleStatique.get(voisin);
	}

	public static final int getGridPointX(Vec2<ReadOnly> p)
	{
		return (int) Math.round((p.x+1500) / GridSpace.DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static final int getGridPointY(Vec2<ReadOnly> p)
	{
		return (int) Math.round(p.y / GridSpace.DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static final int getGridPoint(int x, int y)
	{
		return (y << PRECISION) + x;
	}

	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * @param p
	 * @return
	 */
	public static final int computeGridPoint(Vec2<ReadOnly> p)
	{
		return (int) (NB_POINTS_POUR_TROIS_METRES*(int) Math.round(p.y / GridSpace.DISTANCE_ENTRE_DEUX_POINTS) + Math.round((p.x+1500) / GridSpace.DISTANCE_ENTRE_DEUX_POINTS));
	}

	/**
	 * Renvoie la distance en fonction de la direction.
	 * Attention ! Ne prend pas en compte les obstacles dynamiques
	 * @param i
	 * @return
	 */
	public int distanceStatique(int point, Direction i) {
		if(!isTraversableStatique(point, i))
			return Integer.MAX_VALUE;
		if(i.ordinal() < 4) // cf ordre des directions
			return 1414;
		return 1000;
	}

	public static final Vec2<ReadOnly> computeVec2(int gridpoint)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		computeVec2(out, gridpoint);
		return out.getReadOnly();
	}

	public static final void computeVec2(Vec2<ReadWrite> v, int gridpoint)
	{
		v.x = (((gridpoint & (NB_POINTS_POUR_TROIS_METRES - 1)) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10) - 1500;
		v.y = ((gridpoint >> PRECISION) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10;
	}

	/**
	 * Ajoute le contour d'un obstacle de proximité dans la grille dynamique
	 * @param o
	 */
	private ArrayList<PointDirige> getMasqueObstacle(Vec2<ReadOnly> position)
	{
		int x = getGridPointX(position);
		int y = getGridPointY(position);
//		log.debug("xy : "+x+" "+y);
		int xC1, yC1, xC2, yC2;
		ArrayList<PointDirige> out = new ArrayList<PointDirige>();
		for(PointDirige c : masque)
		{
//			log.debug("c : "+c);
			int p1 = c.point;
			Direction dir = c.dir;
			xC1 = (p1 & (NB_POINTS_POUR_TROIS_METRES - 1)) + x - centreMasque;
			yC1 = (p1 >> PRECISION) + y - centreMasque;
			int	gridpoint = getGridPointVoisin(p1, dir);
			xC2 = (gridpoint & (NB_POINTS_POUR_TROIS_METRES - 1)) + x - centreMasque;
			yC2 = (gridpoint >> PRECISION) + y - centreMasque;

//			log.debug("Obtenu : "+((((yC1 << PRECISION) +xC1) << DEUXIEME_POINT_COUPLE) + (yC2 << PRECISION) +xC2));
			
//			log.debug("Lecture masque : "+xC1+" "+yC1+" "+xC2+" "+yC2);
//			log.debug("Lecture masque : "+computeVec2((yC1 << PRECISION) + xC2));

			// On vérifie que tous les points sont bien dans la table
			if(xC1 >= 0 && xC1 <= X_MAX && yC1 >= 0 && yC1 <= Y_MAX
					&& xC2 >= 0 && xC2 <= X_MAX && yC2 >= 0 && yC2 <= Y_MAX)
			{
				out.add(new PointDirige(getGridPoint(xC1,yC1), dir));
//				log.debug("Ajout !");
			}
		}
		return out;
	}

	public Tribool isDoneTable(GameElementNames g)
	{
		return table.isDone(g);
	}

	public void setDoneTable(GameElementNames g, Tribool done)
	{
		table.setDone(g, done);
	}

	public synchronized void deleteOldObstacles()
	{
//		log.debug("Appel de deleteOldObstacles");
		// S'il y a effectivement suppression, on régénère la grille
		if(obstaclesMemory.deleteOldObstacles())
			notify(); // changement de la grille dynamique !
	}

	public long getNextDeathDate()
	{
		return obstaclesMemory.getNextDeathDate();
	}

	/**
	 * Utilisé uniquement pour les tests
	 * @param point
	 * @return
	 */
	public boolean isTraversableStatique(int point)
	{
		return !grilleStatique.get(point);
	}
	
	/**
	 * Un nouveau DStarLite commence. Il faut lui fournir les obstacles actuels
	 * @return
	 */
	public ArrayList<PointDirige> startNewPathfinding() // TODO renommer
	{
		iteratorDStarLite.reinit();
		ArrayList<PointDirige> out = new ArrayList<PointDirige>();
		ObstacleProximity o = null;
		while(iteratorDStarLite.hasNext())
		{
			o = iteratorDStarLite.next();
//			log.debug("Ajout d'un obstacle au début du dstarlite");
			out.addAll(o.getMasque());
		}
		if(o != null)
			deathDateLastObstacle = o.getDeathDate();
		else
			deathDateLastObstacle = 0;
		
		return out;
	}
	
	/**
	 * Retourne les obstacles à supprimer (indice 0) et ceux à ajouter (indice 1) dans le DStarLite
	 */
	public ArrayList<ObstacleProximity>[] getOldAndNewObstacles()
	{
		synchronized(obstaclesMemory)
		{
			@SuppressWarnings("unchecked")
			ArrayList<ObstacleProximity>[] out = new ArrayList[2];
			out[0] = new ArrayList<ObstacleProximity>();
			out[1] = new ArrayList<ObstacleProximity>();
	
			while(iteratorDStarLite.hasNextDead())
				out[0].add(iteratorDStarLite.next());
			ObstacleProximity p;
			while((p = obstaclesMemory.pollMortTot()) != null)
				out[0].add(p);
	
			long tmp = deathDateLastObstacle;
			while(iteratorDStarLite.hasNext())
			{
				ObstacleProximity o = iteratorDStarLite.next();
				long deathDate = o.getDeathDate();
				if(deathDate > deathDateLastObstacle)
				{
					tmp = deathDate;
					out[1].add(o);
				}
			}
			deathDateLastObstacle = tmp;
			iteratorDStarLite.reinit(); // l'itérateur reprendra juste avant les futurs obstacles périmés
			return out;
		}
	}
	
    /**
     * Utilisé pour savoir si ce qu'on voit est un obstacle fixe.
     * @param position
     * @return
     */
    public boolean isObstacleFixePresentCapteurs(Vec2<ReadOnly> position)
    {
    	for(ObstaclesFixes o: ObstaclesFixes.obstaclesFixesVisibles)
    		if(o.getObstacle().squaredDistance(position) < distanceApproximation * distanceApproximation)
                return true;
        return false;
    }

    /**
	 * Appelé par le thread des capteurs par l'intermédiaire de la classe capteurs
	 * Ajoute l'obstacle à la mémoire et dans le gridspace
     * Supprime les obstacles mobiles proches
     * Ça allège le nombre d'obstacles.
     * Utilisé par les capteurs
     * @param position
     * @return 
     * @return
     */
    public ObstacleProximity addObstacleAndRemoveNearbyObstacles(Vec2<ReadOnly> position)
    {
    	iteratorRemoveNearby.reinit();
    	while(iteratorRemoveNearby.hasNext())
        	if(iteratorRemoveNearby.next().isProcheCentre(position, distanceMinimaleEntreProximite))
        	{
        		log.debug("Suppression d'un obstacle");
        		iteratorRemoveNearby.remove();
        	}

    	ArrayList<PointDirige> masque = getMasqueObstacle(position);
		ObstacleProximity o = obstaclesMemory.add(position, masque);
		// pour un ajout, pas besoin de tout régénérer
		return o;
    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Utilisé pour savoir s'il y a un ennemi devant nous.
     * @param position
     * @return
     */
    public boolean isThereObstacle(ChronoGameState state, Vec2<ReadOnly> position)
    {
    	state.iterator.reinit();
    	while(state.iterator.hasNext())
        	if(state.iterator.next().isProcheObstacle(position, rayonRobot + 20))
        		return true;
       return false;
    }
	
	/**
	 * Y a-t-il collision sur le chemin d'une trajectoire courbe ?
	 * @param node
	 * @return
	 */
	public boolean isTraversableCourbe(AStarCourbeNode node, boolean shoot)
	{
		ObstacleArcCourbe obs = node.came_from_arc.obstacle;

		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values)
    		if(o.getObstacle().isColliding(obs))
    			return false;

    	// Collision avec un obstacle de proximité ?
    	node.state.iterator.reinit();
    	while(node.state.iterator.hasNext())
           	if(node.state.iterator.next().isColliding(obs))
        		return false;
    	
    	// On vérifie si on collisionne un élément de jeu
    	if(!shoot)
    		for(GameElementNames g : GameElementNames.values)
    			if(table.isDone(g) != Tribool.FALSE && g.getObstacle().isColliding(obs))
    				return false;

    	return true;
	}
	
	/**
	 * g est-il proche de position? (utilisé pour vérifier si on shoot dans un élément de jeu)
	 * @param g
	 * @param positionCentreRotation
	 * @param rayon_robot_adverse
	 * @return
	 */
	public boolean didTheEnemyTakeIt(GameElementNames g, ObstacleProximity o)
	{
		return g.getObstacle().isProcheObstacle(o.getPosition(), o.radius);
	}

	public boolean didWeShootIt(GameElementNames g, Vec2<ReadOnly> position)
	{
		return g.getObstacle().isProcheObstacle(position, rayonRobot);
	}

}
