package com.danielfireman.ctc.model;

import java.math.BigDecimal;

import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.TransientSolutionViewer;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

public class ProducerConsumer {
    public static void main(String[] args) {
    	PetriNet pn = new PetriNet();
    	Marking m = new Marking();
        Model.build(pn, m);
        
        // transient until time=12, error 0.005 (per epoch), integration step=0.02
        RegTransient analysis = RegTransient.builder()
                .greedyPolicy(new BigDecimal("12"), new BigDecimal("0.005"))
                .timeStep(new BigDecimal("0.02")).build();

        TransientSolution<DeterministicEnablingState, Marking> solution = 
                analysis.compute(pn, m);

        // display transient probabilities
        new TransientSolutionViewer(solution);
    }
}
