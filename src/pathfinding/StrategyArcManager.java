package pathfinding;

import java.util.ArrayList;
import java.util.Vector;

import hook.Hook;
import hook.types.HookFactory;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Decision;
import scripts.Script;
import scripts.ScriptManager;
import strategie.GameState;
import utils.Log;
import utils.Config;
import container.Service;
import enums.PathfindingNodes;
import enums.ScriptNames;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import exceptions.UnknownScriptException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;

public class StrategyArcManager implements Service, ArcManager {

	private Log log;
	private ScriptManager scriptmanager;
	private AStar astar;
	private HookFactory hookfactory;
	
	private ArrayList<Decision> listeDecisions = new ArrayList<Decision>();
	private int iterator;
	private Vector<Integer> hashes = new Vector<Integer>();
	
	public StrategyArcManager(Log log, Config config, ScriptManager scriptmanager, GameState<RobotReal> real_gamestate, HookFactory hookfactory)
	{
		this.log = log;
		this.scriptmanager = scriptmanager;
		this.hookfactory = hookfactory;
	}

	@Override
	public void reinitIterator(GameState<RobotChrono> gamestate)
	{
		listeDecisions.clear();
		for(ScriptNames s: ScriptNames.values())
		{
			if(s.canIDoIt())
			{
				try {
					Script script = scriptmanager.getScript(s);
					for(Integer v: script.meta_version(gamestate))
					{
						// On n'ajoute que les versions qui sont accessibles
						try {
							ArrayList<PathfindingNodes> chemin = astar.computePath(gamestate, script.point_entree(v), true, false);
							listeDecisions.add(new Decision(chemin, s, v, true));
						} catch (PathfindingException
								| PathfindingRobotInObstacleException
								| FinMatchException e) {
							e.printStackTrace();
						}
						try {
							ArrayList<PathfindingNodes> chemin = astar.computePath(gamestate, script.point_entree(v), false, false);
							listeDecisions.add(new Decision(chemin, s, v, true));
						} catch (PathfindingException
								| PathfindingRobotInObstacleException
								| FinMatchException e) {
						}
					}
				} catch (UnknownScriptException e) {
					log.warning("Script inconnu: "+s, this);
					// Ne devrait jamais arriver
					e.printStackTrace();
				}
			}
		}
		
		iterator = -1;
	}

	@Override
	public boolean hasNext(GameState<RobotChrono> state)
	{
		iterator++;
		return iterator < listeDecisions.size();
	}

	@Override
	public Arc next()
	{
//		log.debug("Prochain voisin: "+listeDecisions.get(iterator).script_name, this);
		return listeDecisions.get(iterator);
	}

	@Override
	public double distanceTo(GameState<RobotChrono> state, Arc arc) {
		Decision d = (Decision)arc;
		try {
			Script s = scriptmanager.getScript(d.script_name);
			try {
				int old_points = state.robot.getPointsObtenus();
				long old_temps = state.robot.getTempsDepuisDebutMatch();
				ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScripts(state);
				state.robot.suit_chemin(d.chemin, hooks_table);
				s.execute(d.version, state);
				int new_points = state.robot.getPointsObtenus();
				long new_temps = state.robot.getTempsDepuisDebutMatch();
				return -((double)(new_points - old_points))/((double)(new_temps - old_temps));
				
				// On renvoie une valeur négative, car le A* minimise la distance.
				// En minimisant l'opposé du nombre de points qu'on fait,
				// on maximise le nombre de points qu'on fait.
			} catch (UnableToMoveException e) {
				e.printStackTrace();
			} catch (SerialConnexionException e) {
				e.printStackTrace();
			} catch (FinMatchException e) {
				// C'est normal et probable, donc pas de printStackTrace
			}
		} catch (UnknownScriptException e) {
			log.warning("Script inconnu: "+d.script_name, this);
			// Ne devrait jamais arriver
			e.printStackTrace();
		}
		// En cas d'erreur
		return Double.MAX_VALUE;
	}

	@Override
	public double heuristicCost(GameState<RobotChrono> state1,
			GameState<RobotChrono> state2)
	{
		int points1 = state1.robot.getPointsObtenus();
		long temps1 = state1.robot.getTempsDepuisDebutMatch();
		int points2 = state2.robot.getPointsObtenus();
		long temps2 = state2.robot.getTempsDepuisDebutMatch();
		return -((double)(points2 - points1))/((double)(temps2 - temps1));
	}

	@Override
	public int getHash(GameState<RobotChrono> state)
	{
		int hash = (int)(100000*((double)state.robot.getPointsObtenus())/((double)state.robot.getTempsDepuisDebutMatch()));
		int indice = hashes.indexOf(hash);
		if(indice == -1)
		{
			hashes.add(hash);
			return hashes.size()-1;
		}
		else
			return indice;
	}

	@Override
	public void updateConfig()
	{
	}

	public void setAStar(AStar astar)
	{
		this.astar = astar;
	}
	
	public void reinitHashes()
	{
		hashes.clear();
	}
	
	public String toString()
	{
		return "Arbre des possibles";
	}

}
