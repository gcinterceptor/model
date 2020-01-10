package com.danielfireman.ctc.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.oristool.models.gspn.GSPNSteadyState;
import org.oristool.models.gspn.GSPNTransient;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.util.Pair;

public class ProducerConsumer {
	static final double step = 0.1;
	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		Marking m = new Marking();
		Model.build(pn, m);
		
		// https://github.com/oris-tool/sirio/wiki/Generalized-Stochastic-Petri-Nets

		// transient until time=12, error 0.005 (per epoch), integration step=0.02
//		RegTransient analysis = RegTransient.builder().greedyPolicy(new BigDecimal("12"), new BigDecimal("0.005"))
//				.timeStep(new BigDecimal("0.02")).build();

		// TransientSolution<DeterministicEnablingState, Marking> solution =
		// analysis.compute(pn, m);
		// display transient probabilities
		// new TransientSolutionViewer(solution);

		/* ########
		 * STEAD-STATE ANALYSIS
		 * #######
		 */
		System.out.println("Steady State Analysis");
		Map<Marking, Double> dist = GSPNSteadyState.builder().build().compute(pn, m);
		System.out.println(dist.entrySet().stream()
				.map(e -> String.format("%s -> %.6f%n", e.getKey(), e.getValue()))
				.collect(Collectors.joining()));
		
		/* ########
		 * TRANSIENT ANALYSIS
		 * #######
		 */
		Pair<Map<Marking, Integer>, double[][]> tr = GSPNTransient.builder()
			    .timePoints(0.0, 5.0, step)
			    .build()
			    .compute(pn, m);
		double[][] trValues = tr.second();
		Map<Marking,Integer> trMarkings = tr.first();
		
		/* ########
		 * REWARDS
		 * #######
		 */
		
		// Follow: https://github.com/oris-tool/sirio/blob/5015a2fba7f84f959127a909a1eebad43e424a07/sirio/src/test/java/org/oristool/models/gspn/client/AbsorbingRewardTest.java#L90

		/* ########
		 * FULL PROB
		 * #######
		 */
		System.out.println("Transient Analysis");


		// Pretty-printing results.
		Map<Integer, Marking> byID = new HashMap<>();
		for (Entry<Marking, Integer> e : trMarkings.entrySet()) {
			byID.put(e.getValue(), e.getKey());
		}
		for (int i=0; i<trValues[0].length; i++) {
			System.out.print(byID.get(i) + "\t");
		}
		System.out.println();
		for (int row=0; row<trValues.length;row++) {
			for (int col=0;col<trValues[row].length;col++) {
				System.out.format("%.8f\t", trValues[row][col]);
			}
			System.out.println();
		}
	}
}
