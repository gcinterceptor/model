package com.danielfireman.ctc.model;

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
    Place Available = net.addPlace("Available");
    Place Busy = net.addPlace("Busy");
    Place Controller = net.addPlace("Controller");
    Place LB = net.addPlace("LB");
    Place Unavailable = net.addPlace("Unavailable");
    Transition R_Accepted = net.addTransition("R_Accepted");
    Transition R_Arrived = net.addTransition("R_Arrived");
    Transition R_Finished = net.addTransition("R_Finished");
    Transition R_Started = net.addTransition("R_Started");
    Transition T_Finished = net.addTransition("T_Finished");
    Transition T_Start = net.addTransition("T_Start");

    //Generating Connectors
    net.addPostcondition(R_Accepted, Controller);
    net.addPrecondition(Available, R_Accepted);
    net.addPostcondition(R_Started, Busy);
    net.addPostcondition(T_Finished, Available);
    net.addPrecondition(Busy, R_Finished);
    net.addPrecondition(Controller, T_Start);
    net.addPrecondition(Controller, R_Started);
    net.addPrecondition(LB, R_Accepted);
    net.addPrecondition(Unavailable, T_Finished);
    net.addPostcondition(R_Finished, Available);
    net.addPostcondition(T_Start, Unavailable);
    net.addPostcondition(R_Arrived, LB);

    //Generating Properties
    marking.setTokens(Available, 1);
    marking.setTokens(Busy, 0);
    marking.setTokens(Controller, 0);
    marking.setTokens(LB, 0);
    marking.setTokens(Unavailable, 0);
    R_Accepted.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    R_Accepted.addFeature(new Priority(0));
    R_Arrived.addFeature(new EnablingFunction("Available>0"));
    R_Arrived.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
    R_Finished.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
    R_Started.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    R_Started.addFeature(new Priority(0));
    T_Finished.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"), MarkingExpr.from("1", net)));
    T_Start.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("0.1", net)));
    T_Start.addFeature(new Priority(0));
  }
}
