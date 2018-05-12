/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;


import java.util.ArrayList;
import java.util.List;

/**
 * Informations accessibles par la config
 * Les informations de config.ini surchargent celles-ci
 * Certaines valeurs sont constantes, ce qui signifie qu'elles ne peuvent être
 * modifiées dynamiquement au cours d'un match.
 * Chaque variable a une valeur par défaut, afin de pouvoir lancer le programme
 * sans config.ini.
 * 
 * @author pf
 *
 */

import pfg.config.ConfigInfo;

/**
 * The configuration keys
 * @author pf
 *
 */

public enum ConfigInfoKraken implements ConfigInfo
{
	/**
	 * Config du navmesh
	 */
	NAVMESH_OBSTACLE_DILATATION(100), // dilatation des obstacles dans le D* Lite.
									// Comme c'est une heuristique, on peut
									// prendre plus petit que la vraie valeur
	LARGEST_TRIANGLE_AREA_IN_NAVMESH(20000), // in mm²
	LONGEST_EDGE_IN_NAVMESH(200), // in mm
	NAVMESH_FILENAME("navmesh.krk"), // the filename of the navmesh
	
	NECESSARY_MARGIN(40), // minimun distance that MUST be available in the current path, in mm
	PREFERRED_MARGIN(60), // preferred distance that should be available in the current path, in mm
	MARGIN_BEFORE_COLLISION(100), // maximal distance before the detected collision, in mm
	INITIAL_MARGIN(100), // distance of the trajectory after the current point that can't be modified, in mm
	
	/**
	 * Research and mechanical parameter
	 */
	MAX_CURVATURE_DERIVATIVE(5), // maximal curvature derivative, in m⁻¹s⁻¹
	MAX_LATERAL_ACCELERATION(3), // maximal lateral acceleration, in m/s²
	MAX_LINEAR_ACCELERATION(2), // maximal linear acceleration, in m/s²
	DEFAULT_MAX_SPEED(1), // in m/s (or mm/ms)
	MINIMAL_SPEED(0), // in m/s
	MAX_CURVATURE(5), // in m⁻¹
	STOP_DURATION(800), // temps qu'il faut au robot pour s'arrêter et repartir
						// (par exemple à cause d'un rebroussement) in ms
	SEARCH_TIMEOUT(10000), // in ms
	THREAD_NUMBER(1), // the number of threads for the tentacle computing. Recommended value for highest performance : nb cores + 1
//	ALLOW_SPINNING(false), // can the robot spin ?
	
	ENABLE_DEBUG_MODE(false), // enable the debug mode
	FAST_AND_DIRTY(false),
	CHECK_NEW_OBSTACLES(false),
	
	/**
	 * Paramètres sur la gestion de la mémoire
	 */
	NODE_MEMORY_POOL_SIZE(20000),
	OBSTACLES_MEMORY_POOL_SIZE(50000), // nombre d'instances pour les obstacles
			
	ALLOW_BACKWARD_MOTION(true); // allow the pathfinding to find a path with backward motion by default

	private Object defaultValue;
	public volatile boolean uptodate;

	public static List<ConfigInfo> getGraphicConfigInfo()
	{
		List<ConfigInfo> out = new ArrayList<ConfigInfo>();
		for(ConfigInfoKraken c : values())
			if(c.toString().startsWith("GRAPHIC_"))
				out.add(c);
		return out;
	}
	
	/**
	 * Par défaut, une valeur est constante
	 * 
	 * @param defaultValue
	 */
	private ConfigInfoKraken(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}
}
