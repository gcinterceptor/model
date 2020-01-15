import java.math.BigDecimal;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.EnablingFunction;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class ModelFin {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place Avai = net.addPlace("Avai");
    Place Busy = net.addPlace("Busy");
    Place Cont = net.addPlace("Cont");
    Place Fin = net.addPlace("Fin");
    Place LB = net.addPlace("LB");
    Place Unav = net.addPlace("Unav");
    Transition R_Accepted = net.addTransition("R_Accepted");
    Transition R_Arrived = net.addTransition("R_Arrived");
    Transition R_Finished = net.addTransition("R_Finished");
    Transition R_Started = net.addTransition("R_Started");
    Transition T_Finished = net.addTransition("T_Finished");
    Transition T_Start = net.addTransition("T_Start");
    Transition t0 = net.addTransition("t0");

    //Generating Connectors
    net.addPrecondition(Unav, T_Finished);
    net.addPrecondition(Busy, R_Finished);
    net.addPostcondition(R_Finished, Avai);
    net.addPrecondition(Cont, T_Start);
    net.addPostcondition(R_Arrived, LB);
    net.addPostcondition(R_Finished, Fin);
    net.addPrecondition(Avai, R_Accepted);
    net.addPostcondition(R_Accepted, Cont);
    net.addPrecondition(LB, R_Accepted);
    net.addPostcondition(T_Finished, Avai);
    net.addPrecondition(Cont, R_Started);
    net.addPostcondition(T_Start, Unav);
    net.addPostcondition(R_Started, Busy);
    net.addPrecondition(Fin, t0);

    //Generating Properties
    marking.setTokens(Avai, 2);
    marking.setTokens(Busy, 0);
    marking.setTokens(Cont, 0);
    marking.setTokens(Fin, 0);
    marking.setTokens(LB, 0);
    marking.setTokens(Unav, 0);
    R_Accepted.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    R_Accepted.addFeature(new Priority(0));
    R_Arrived.addFeature(new EnablingFunction("Avai>0"));
    R_Arrived.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
    R_Finished.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
    R_Started.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    R_Started.addFeature(new Priority(0));
    T_Finished.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
    T_Start.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.1", net)));
    T_Start.addFeature(new Priority(0));
    t0.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t0.addFeature(new Priority(0));
  }
}
