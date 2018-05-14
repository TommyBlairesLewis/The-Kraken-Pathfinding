/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import java.util.ArrayList;
import java.util.List;

import pfg.kraken.Kraken;
import pfg.kraken.SearchParameters;
import pfg.kraken.astar.autoreplanning.DynamicPath;
import pfg.kraken.astar.autoreplanning.PathDiff;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.obstacles.container.DefaultDynamicObstacles;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;


/**
 * Example for replaning
 * @author pf
 *
 */

public class Example6
{

	public static void main(String[] args) throws InterruptedException
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY(800,200), 200, 200));
		obs.add(new RectangularObstacle(new XY(800,300), 200, 200));
		obs.add(new RectangularObstacle(new XY(-800,1200), 100, 200));
		obs.add(new RectangularObstacle(new XY(-1000,300), 500, 500));
		obs.add(new RectangularObstacle(new XY(200,1600), 800, 300));
		obs.add(new RectangularObstacle(new XY(1450,700), 300, 100));
		obs.add(new CircularObstacle(new XY(500,600), 100));
		
		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110); 

		/*
		 * The list of dynamic obstacles.
		 * "DefaultDynamicObstacles" is the default manager ; you can use a manager of your own if you want/need to
		 */
		DefaultDynamicObstacles obsDyn = new DefaultDynamicObstacles();

		Kraken kraken = new Kraken(robot, obs, obsDyn, new XY(-1500,0), new XY(1500, 2000), "kraken-examples.conf", "trajectory");

		try
		{
			/*
			 * We enable the autoreplanning
			 * We won't use "kraken.initializeNewSearch()" or"kraken.search()"
			 */
			DynamicPath dpath = kraken.enableAutoReplanning();
			kraken.startContinuousSearch(new SearchParameters(new XYO(0, 200, 0), new XY(1000, 1000)));
			PathDiff diff;
			
			/*
			 * The research part. We wait for dpath until a new path is available
			 */
			do {
				diff = dpath.waitDiff();
			} while(!diff.isComplete);

			Thread.sleep(2000);
			
			/*
			 * The research is continuous : at the moment a new obstacle is added, Kraken tries to find a new path
			 */
			Obstacle newObs1 = new CircularObstacle(new XY(400,800), 100);
			obsDyn.add(newObs1);
			
			/*
			 * We wait the new path
			 */
			do {
				diff = dpath.waitDiff();
			} while(!diff.isComplete);
			
			Thread.sleep(2000);

			/*
			 * We add a second obstacle
			 */
			Obstacle newObs2 = new CircularObstacle(new XY(100,1200), 100);
			obsDyn.add(newObs2);
			
			do {
				diff = dpath.waitDiff();
			} while(!diff.isComplete);
			
			/*
			 * When the continuous search isn't needed anymore, we can stop it.
			 * You need to end the search to start a search with different parameters
			 */
			kraken.endContinuousSearch();
		}
		catch(PathfindingException e)
		{
			/*
			 * This exception is thrown when no path is found
			 */
			e.printStackTrace();
		}
	}
}
