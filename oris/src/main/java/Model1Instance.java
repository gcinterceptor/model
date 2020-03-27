import java.math.BigDecimal;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.EnablingFunction;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class Model1Instance {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place S_check = net.addPlace("S_check");
    Place S_exec_tasks = net.addPlace("S_exec_tasks");
    Place S_serv = net.addPlace("S_serv");
    Place S_wait_reqs = net.addPlace("S_wait_reqs");
    Place lb = net.addPlace("lb");
    Transition T1_choice = net.addTransition("T1_choice");
    Transition T_load = net.addTransition("T_load");
    Transition T_no_unav = net.addTransition("T_no_unav");
    Transition T_serv = net.addTransition("T_serv");
    Transition T_tasks_start = net.addTransition("T_tasks_start");
    Transition T_unav_fin = net.addTransition("T_unav_fin");
    Transition T_unav_start = net.addTransition("T_unav_start");

    //Generating Connectors
    net.addPrecondition(S_check, T_unav_start);
    net.addPostcondition(T_load, lb);
    net.addPrecondition(S_check, T_no_unav);
    net.addPrecondition(lb, T1_choice);
    net.addPostcondition(T1_choice, S_serv);
    net.addPrecondition(S_serv, T_serv);
    net.addPrecondition(S_wait_reqs, T_tasks_start);
    net.addPrecondition(S_exec_tasks, T_unav_fin);
    net.addPostcondition(T_unav_start, S_wait_reqs);
    net.addPostcondition(T_serv, S_check);
    net.addPostcondition(T_tasks_start, S_exec_tasks);

    //Generating Properties
    marking.setTokens(S_check, 0);
    marking.setTokens(S_exec_tasks, 0);
    marking.setTokens(S_serv, 0);
    marking.setTokens(S_wait_reqs, 0);
    marking.setTokens(lb, 0);
    T1_choice.addFeature(new EnablingFunction("S_exec_tasks+S_wait_reqs+S_serv+S_check<2"));
    T1_choice.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    T1_choice.addFeature(new Priority(0));
    T_load.addFeature(new EnablingFunction("lb==0"));
    T_load.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
    T_no_unav.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.7", net)));
    T_no_unav.addFeature(new Priority(0));
    T_serv.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("0.5", net)));
    T_tasks_start.addFeature(new EnablingFunction("S_serv<2"));
    T_tasks_start.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    T_tasks_start.addFeature(new Priority(0));
    T_unav_fin.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("0.2", net)));
    T_unav_start.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.3", net)));
    T_unav_start.addFeature(new Priority(0));
  }
}
