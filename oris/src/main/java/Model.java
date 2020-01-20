import java.math.BigDecimal;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.EnablingFunction;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class Model {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place R1_check = net.addPlace("R1_check");
    Place R1_serv = net.addPlace("R1_serv");
    Place R1_unav = net.addPlace("R1_unav");
    Place R1_wait_req_fin = net.addPlace("R1_wait_req_fin");
    Place R2_check = net.addPlace("R2_check");
    Place R2_serv = net.addPlace("R2_serv");
    Place R2_unav = net.addPlace("R2_unav");
    Place R2_wait_req_fin = net.addPlace("R2_wait_req_fin");
    Place lb = net.addPlace("lb");
    Transition T1_no_unav = net.addTransition("T1_no_unav");
    Transition T1_serv = net.addTransition("T1_serv");
    Transition T1_serv_fin = net.addTransition("T1_serv_fin");
    Transition T1_unav = net.addTransition("T1_unav");
    Transition T1_unav_fin = net.addTransition("T1_unav_fin");
    Transition T1_unav_start = net.addTransition("T1_unav_start");
    Transition T2_no_unav = net.addTransition("T2_no_unav");
    Transition T2_serv = net.addTransition("T2_serv");
    Transition T2_serv_fin = net.addTransition("T2_serv_fin");
    Transition T2_unav = net.addTransition("T2_unav");
    Transition T2_unav_fin = net.addTransition("T2_unav_fin");
    Transition T2_unav_start = net.addTransition("T2_unav_start");
    Transition T_req_arrived = net.addTransition("T_req_arrived");

    //Generating Connectors
    net.addPrecondition(R1_check, T1_no_unav);
    net.addPrecondition(lb, T2_serv);
    net.addPrecondition(R1_check, T1_unav);
    net.addPrecondition(R1_wait_req_fin, T1_unav_start);
    net.addPostcondition(T2_serv_fin, R2_check);
    net.addPrecondition(lb, T1_serv);
    net.addPostcondition(T2_serv, R2_serv);
    net.addPostcondition(T1_serv_fin, R1_check);
    net.addPostcondition(T2_unav_start, R2_unav);
    net.addPostcondition(T1_unav_start, R1_unav);
    net.addPrecondition(R1_serv, T1_serv_fin);
    net.addPrecondition(R2_unav, T2_unav_fin);
    net.addPrecondition(R2_check, T2_no_unav);
    net.addPrecondition(R2_wait_req_fin, T2_unav_start);
    net.addPostcondition(T_req_arrived, lb);
    net.addPrecondition(R2_serv, T2_serv_fin);
    net.addPrecondition(R2_check, T2_unav);
    net.addPrecondition(R1_unav, T1_unav_fin);
    net.addPostcondition(T1_unav, R1_wait_req_fin);
    net.addPostcondition(T1_serv, R1_serv);
    net.addPostcondition(T2_unav, R2_wait_req_fin);

    //Generating Properties
    marking.setTokens(R1_check, 0);
    marking.setTokens(R1_serv, 0);
    marking.setTokens(R1_unav, 0);
    marking.setTokens(R1_wait_req_fin, 0);
    marking.setTokens(R2_check, 0);
    marking.setTokens(R2_serv, 0);
    marking.setTokens(R2_unav, 0);
    marking.setTokens(R2_wait_req_fin, 0);
    marking.setTokens(lb, 0);
    T1_no_unav.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.5", net)));
    T1_no_unav.addFeature(new Priority(0));
    T1_serv.addFeature(new EnablingFunction("R1_serv<1 && R1_unav==0 && R1_wait_req_fin==0"));
    T1_serv.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    T1_serv.addFeature(new Priority(0));
    T1_serv_fin.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("0.5", net)));
    T1_unav.addFeature(new EnablingFunction("R1_unav==0 && R1_wait_req_fin==0"));
    T1_unav.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.5", net)));
    T1_unav.addFeature(new Priority(0));
    T1_unav_fin.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("0.2", net)));
    T1_unav_start.addFeature(new EnablingFunction("R1_serv==0 && R1_check==0"));
    T1_unav_start.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    T1_unav_start.addFeature(new Priority(0));
    T2_no_unav.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.5", net)));
    T2_no_unav.addFeature(new Priority(0));
    T2_serv.addFeature(new EnablingFunction("R2_serv<1 && R2_unav==0 && R2_wait_req_fin==0"));
    T2_serv.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.5", net)));
    T2_serv.addFeature(new Priority(0));
    T2_serv_fin.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("0.5", net)));
    T2_unav.addFeature(new EnablingFunction("R2_unav==0 && R2_wait_req_fin==0"));
    T2_unav.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.5", net)));
    T2_unav.addFeature(new Priority(0));
    T2_unav_fin.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("0.2", net)));
    T2_unav_start.addFeature(new EnablingFunction("R2_serv==0 && R2_check==0"));
    T2_unav_start.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    T2_unav_start.addFeature(new Priority(0));
    T_req_arrived.addFeature(new EnablingFunction("lb==0"));
    T_req_arrived.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
  }
}
