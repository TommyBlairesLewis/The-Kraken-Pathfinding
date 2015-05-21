package threads;

import table.ObstacleManager;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread du manager d'obstacle. Surveille IncomingDataBuffer
 * @author pf
 *
 */

public class ThreadObstacleManager extends Thread implements Service
{
	private IncomingDataBuffer buffer;
	private ObstacleManager obstaclemanager;
	protected Log log;
	
	public ThreadObstacleManager(Log log, IncomingDataBuffer buffer, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.buffer = buffer;
		this.obstaclemanager = obstaclemanager;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			IncomingData e = null;
			synchronized(buffer)
			{
				try {
					buffer.wait();
					log.debug("Réveil de ThreadObstacleManager");
					e = buffer.poll();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			// Cet appel peut lancer un obstaclemanager.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas le buffer
			if(e != null)
				obstaclemanager.addIfUseful(e);
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
