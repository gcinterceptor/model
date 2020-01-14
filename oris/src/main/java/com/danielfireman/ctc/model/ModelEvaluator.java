package com.danielfireman.ctc.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.oristool.analyzer.log.AnalysisLogger;
import org.oristool.models.gspn.GSPNSteadyState;
import org.oristool.models.gspn.GSPNTransient;
import org.oristool.models.pn.PetriStateFeature;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.Sequencer.SequencerEvent;
import org.oristool.simulator.SequencerObserver;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.RewardTime;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.util.Pair;

public class ModelEvaluator {
	private static final String BUSY_STATE = "Busy";
	private static final String UNAVAVAILABLE_STATE = "Unav";
	private static final String AVAILABLE_STATE = "Avai";
	private static final String LB_STATE = "LB";
	static final double step = 1;
	static final double duration = 100;

	public static void main(String[] args) {
		PetriNet pn = new PetriNet();
		Marking m = new Marking();
		Model.build(pn, m);

		// https://github.com/oris-tool/sirio/wiki/Generalized-Stochastic-Petri-Nets

		/*
		 * ######## SIMULATION #######
		 */
		System.out.println("## Simulation");
		Sequencer s = new Sequencer(pn, m, new STPNSimulatorComponentsFactory(), new AnalysisLogger() {
			@Override
			public void log(String message) {
				//System.out.println(message);
			}

			@Override
			public void debug(String message) {
				// System.out.println(message);
			}
		});

		class TokenCounterState {
			Marking marking;
			double sojourn;
			double timestamp;
		}

		class TokenCounter implements SequencerObserver {
			public List<TokenCounterState> state = new LinkedList<>();
			private Sequencer sequencer;
			private RewardTime time;
			private BigDecimal duration;
			private BigDecimal elapsedTime = BigDecimal.ZERO;

			public TokenCounter(Sequencer s, double step, double duration) {
				this.sequencer = s;
				this.time = new ContinuousRewardTime(new BigDecimal(step));
				this.duration = new BigDecimal(duration);

				TokenCounterState state = new TokenCounterState();
				state.marking = s.getInitialMarking();
				state.timestamp = 0;
				this.state.add(state);
				this.sequencer.addObserver(this);
			}

			@Override
			public void update(SequencerEvent event) {
				switch (event) {
				case RUN_START:
					this.sequencer.addCurrentRunObserver(this);
					break;
				case FIRING_EXECUTED:
					// Stores the before the firing of the event.
					double timeBeforeFiring = this.elapsedTime.doubleValue();

					BigDecimal sojourn = this.time.getSojournTime(this.sequencer.getLastSuccession());

					// Increments elapsed time.
					this.elapsedTime = this.elapsedTime.add(sojourn);

					// Fill up last marking information.
					TokenCounterState last = this.state.get(this.state.size() - 1);
					last.timestamp = timeBeforeFiring;
					last.sojourn = sojourn.doubleValue();

					// Add next marking.
					TokenCounterState next = new TokenCounterState();
					next.marking = this.sequencer.getLastSuccession().getChild().getFeature(PetriStateFeature.class)
							.getMarking();
					this.state.add(next);

					if (this.elapsedTime.compareTo(duration) >= 0) {
						this.sequencer.removeCurrentRunObserver(this);
						this.sequencer.removeObserver(this);
					}
					break;
				case RUN_END:
					this.sequencer.removeCurrentRunObserver(this);
					break;
				}
			}
		}

		TokenCounter tc = new TokenCounter(s, step, duration);
		s.simulate();
		StringBuilder builder = new StringBuilder();
		builder.append("n,ts,sojourn,tk_lb,tk_available,tk_busy,tk_unavailable");
		builder.append("\n");
		for (int i = 0; i < tc.state.size(); i++) {
			TokenCounterState state = tc.state.get(i);
			builder.append(i+1);
			builder.append(",");
			builder.append(String.format("%.3f", state.timestamp));
			builder.append(",");
			builder.append(String.format("%.3f", state.sojourn));
			builder.append(",");
			builder.append(state.marking.getTokens(LB_STATE));
			builder.append(",");
			builder.append(state.marking.getTokens(AVAILABLE_STATE));
			builder.append(",");
			builder.append(state.marking.getTokens(BUSY_STATE));
			builder.append(",");
			builder.append(state.marking.getTokens(UNAVAVAILABLE_STATE));
			builder.append("\n");
		}
		System.out.println(builder);

		/*
		 * ######## TRANSIENT ANALYSIS #######
		 * 
		 */
		Pair<Map<Marking, Integer>, double[][]> tr = GSPNTransient.builder().timePoints(0.0, duration, step).build()
				.compute(pn, m);
		double[][] trValues = tr.second();
		Map<Marking, Integer> trMarkings = tr.first();

		/*
		 * ######## FULL PROB #######
		 */
		System.out.println("\n\n## Transient Analysis ##\n\n");

		// Pretty-printing results.
		Map<Integer, Marking> byID = new HashMap<>();
		for (Entry<Marking, Integer> e : trMarkings.entrySet()) {
			byID.put(e.getValue(), e.getKey());
		}
		for (int i = 0; i < trValues[0].length; i++) {
			System.out.print(byID.get(i) + "\t");
		}
		System.out.println();
		for (int row = 0; row < trValues.length; row++) {
			for (int col = 0; col < trValues[row].length; col++) {
				System.out.format("%.4f\t", trValues[row][col]);
			}
			System.out.println();
		}

		/*
		 * ######## STEAD-STATE ANALYSIS #######
		 */
		System.out.println("\n\n## Steady State Analysis");
		Map<Marking, Double> dist = GSPNSteadyState.builder().build().compute(pn, m);
		System.out.println(dist.entrySet().stream().map(e -> String.format("%s -> %.6f%n", e.getKey(), e.getValue()))
				.collect(Collectors.joining()));

		/*
		 * ######## REWARDS #######
		 */

		// Follow:
		// https://github.com/oris-tool/sirio/blob/5015a2fba7f84f959127a909a1eebad43e424a07/sirio/src/test/java/org/oristool/models/gspn/client/AbsorbingRewardTest.java#L90
		System.out.println("\n\n## Rewards ##\n\n");
		System.out.println("Mean Response Time: ");

		TransientSolution<Marking, Marking> solution = TransientSolution.fromArray(trValues, step, trMarkings, m);
		solution.getColumnStates();

		boolean cumulative = true;
		TransientSolution<Marking, RewardRate> reward = TransientSolution.computeRewards(cumulative, solution,
				"If(B>0,1.0,0);B;t");
		System.out.println(reward.toString());

	}
}
