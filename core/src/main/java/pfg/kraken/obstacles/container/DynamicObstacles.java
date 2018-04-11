/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


/**
 * Interface for dynamic obstacles.
 * Due to the wide range of behaviour, the implementation is left to the user.
 */

package pfg.kraken.obstacles.container;

import java.util.Iterator;
import java.util.List;

import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.robot.CinematiqueObs;

/**
 * The interface of a dynamic obstacles container
 * @author pf
 *
 */

public interface DynamicObstacles
{
	public int isThereCollision(List<CinematiqueObs> l);
	public Iterator<Obstacle> getCurrentDynamicObstacles();
}
