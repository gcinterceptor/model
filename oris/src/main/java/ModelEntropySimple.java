import java.math.BigDecimal;

import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.EnablingFunction;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class ModelEntropySimple {
	public static int r = 1;
	public static double mu = 1;
	public static double lambda = 1;
	public static double omega = 1;
	public static double pImpact = 1;

	public static void build(PetriNet net, Marking marking) {

		// Generating Nodes
		Place S_check = net.addPlace("S_check");
		Place S_exec_tasks = net.addPlace("S_exec_tasks");
		Place S_fin = net.addPlace("S_fin");
		Place S_serv = net.addPlace("S_serv");
		Transition T_arrival = net.addTransition("T_arrival");
		Transition T_impact = net.addTransition("T_impact");
		Transition T_no_unav = net.addTransition("T_no_unav");
		Transition T_serv = net.addTransition("T_serv");
		Transition T_unav_start = net.addTransition("T_unav_start");

		// Generating Connectors
		net.addPostcondition(T_serv, S_check);
		net.addPostcondition(T_no_unav, S_fin);
		net.addPrecondition(S_check, T_unav_start);
		net.addPrecondition(S_fin, T_arrival);
		net.addPrecondition(S_serv, T_serv);
		net.addPostcondition(T_arrival, S_serv);
		net.addPrecondition(S_check, T_no_unav);
		net.addPrecondition(S_exec_tasks, T_impact);
		net.addPostcondition(T_unav_start, S_exec_tasks);
		net.addPostcondition(T_impact, S_fin);

		// Generating Properties
		marking.setTokens(S_check, 0);
		marking.setTokens(S_exec_tasks, 0);
		marking.setTokens(S_fin, r);
		marking.setTokens(S_serv, 0);
		T_arrival.addFeature(new EnablingFunction(String.format("(S_serv+S_exec_tasks)<%d", r)));
		T_arrival.addFeature(
				StochasticTransitionFeature.newExponentialInstance(Double.toString(lambda)));
		T_impact.addFeature(StochasticTransitionFeature.newExponentialInstance(Double.toString(omega)));
		T_no_unav.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"),
				MarkingExpr.from(Double.toString(1-pImpact), net)));
		T_no_unav.addFeature(new Priority(0));
		T_serv.addFeature(StochasticTransitionFeature.newExponentialInstance(Double.toString(mu)));
		T_unav_start.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"),
				MarkingExpr.from(Double.toString(pImpact), net)));
		T_unav_start.addFeature(new Priority(0));
	}
}
