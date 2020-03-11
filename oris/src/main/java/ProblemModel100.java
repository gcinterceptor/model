import java.math.BigDecimal;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class ProblemModel100 {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place S_fin = net.addPlace("S_fin");
    Place S_is_task_executing = net.addPlace("S_is_task_executing");
    Place S_req_arrived = net.addPlace("S_req_arrived");
    Place S_task_exec = net.addPlace("S_task_exec");
    Transition T_impact = net.addTransition("T_impact");
    Transition T_no = net.addTransition("T_no");
    Transition T_service = net.addTransition("T_service");
    Transition T_yes = net.addTransition("T_yes");

    //Generating Connectors
    net.addPostcondition(T_service, S_is_task_executing);
    net.addPostcondition(T_no, S_fin);
    net.addPrecondition(S_req_arrived, T_service);
    net.addPrecondition(S_is_task_executing, T_no);
    net.addPrecondition(S_is_task_executing, T_yes);
    net.addPostcondition(T_impact, S_fin);
    net.addPrecondition(S_task_exec, T_impact);
    net.addPostcondition(T_yes, S_task_exec);

    //Generating Properties
    marking.setTokens(S_fin, 0);
    marking.setTokens(S_is_task_executing, 0);
    marking.setTokens(S_req_arrived, 1);
    marking.setTokens(S_task_exec, 0);
    T_impact.addFeature(StochasticTransitionFeature.newExponentialInstance("0.00003"));
    T_no.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.997", net)));
    T_no.addFeature(new Priority(0));
    T_service.addFeature(StochasticTransitionFeature.newExponentialInstance("0.0036"));
    T_yes.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.003", net)));
    T_yes.addFeature(new Priority(0));
  }
}
