package com.danielfireman.ctc.model;

import org.oristool.models.stpn.RewardRate;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

public class Test {
	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		Marking m = new Marking();
		Model.build(pn, m);
		
		RewardRate r = RewardRate.fromString("If(Available==0,1,2)");
		System.out.println(r.evaluate(2, m));
	}
}
