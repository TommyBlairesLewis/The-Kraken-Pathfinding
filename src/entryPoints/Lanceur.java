package entryPoints;
import strategie.Execution;
import container.Container;
import container.ServiceNames;

/**
 * Lanceur TechTheTroll
 * @author pf
 *
 */

/* TODO LIST
 * se renseigner sur la compilation sans bytecode (GCJ)
 * pouvoir arrêter une recherche avant la fin (hook de fin de match)
 * problème script tapis exécuté plusieurs fois!
 * passer les méthodes utilisées uniquement par les tests en deprecated
 */

public class Lanceur {

	public static void main(String[] args) {

		try {
			Container container = new Container();
			container.startAllThreads();
			Execution execution = (Execution)container.getService(ServiceNames.EXECUTION);
			execution.boucleExecution();
			container.destructor();
		} catch (Exception e) {
			System.out.println("Abandon du lanceur.");
			e.printStackTrace();
			return;
		}
		
	}

}