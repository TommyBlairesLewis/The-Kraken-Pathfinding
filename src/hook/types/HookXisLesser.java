package hook.types;

import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import hook.types.HookX;
import strategie.GameState;
import utils.Log;
import utils.Config;
import utils.Vec2;

/**
 * Déclenche un évènement dès que le robot a une coordonnée X (sur la table) inférieure a celle fournie
 * @author pf, marsu
 *
 */

public class HookXisLesser extends HookX
{
    /**
     * Instancie le hook sur coordonnée X. Valeur en X et tolérance paramétrable.
     * @param config : sur quel objet lire la configuration du match
     * @param log : la sortie de log à utiliser
     * @param realState : lien avec le robot a surveiller pour le déclenchement du hook
     * @param xValue : la valeur en x ou doit se déclencher le hook
     * @param tolerancy : imprécision admise sur la position qui déclenche le hook
	 * @param isYellowTeam La table étant symétrisée si l'on est équipe jaune, le XisLesser devient un XisGreater si l'on est jaune
     */
    public HookXisLesser(Config config, Log log, GameState<?> state, float xValue, float tolerancy)
    {
		super(config, log, state, xValue, tolerancy);
	}


    /**
     * Déclenche le hook si la coordonnée x du robot est plus petite que xValue
     * @return true si la position/orientation du robot a été modifiée par cette méthode.
     * @throws ScriptHookException 
     * @throws WallCollisionDetectedException 
     */
    @Override
    public void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException
    {
        if(state.robot.getPosition().x < xValue)
            trigger();
    }
    
	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		return (pointA.x < xValue) || (pointB.x < xValue);
	}

}
