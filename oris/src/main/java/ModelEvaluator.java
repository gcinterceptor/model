import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.oristool.analyzer.Succession;
import org.oristool.analyzer.log.AnalysisLogger;
import org.oristool.models.gspn.GSPNSteadyState;
import org.oristool.models.gspn.GSPNTransient;
import org.oristool.models.pn.PetriStateFeature;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.MarkingCondition;
import org.oristool.petrinet.PetriNet;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.Sequencer.SequencerEvent;
import org.oristool.simulator.SequencerObserver;
import org.oristool.simulator.TimeSeriesRewardResult;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.Reward;
import org.oristool.simulator.rewards.RewardEvaluator;
import org.oristool.simulator.rewards.RewardTime;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;
import org.oristool.util.Pair;

public class ModelEvaluator {
	private static final String R1_SERV = "R1_serv";
	private static final String R2_SERV = "R2_serv";
	private static final String LB_STATE = "lb";

	static class TokenCounterState {
		Marking marking;
		double sojourn;
		double timestamp;
	}

	static class ExecutedRequests {
		double timestamp;
		double responseTime;
	}

	static class TokenCounter implements SequencerObserver {
		public List<TokenCounterState> state = new LinkedList<>();
		private Sequencer sequencer;
		private RewardTime time;
		private BigDecimal duration;
		private BigDecimal elapsedTime = BigDecimal.ZERO;

		public TokenCounter(Sequencer s, double step, double duration) {
			this.sequencer = s;
			this.time = new ContinuousRewardTime(new BigDecimal(step));
			this.duration = new BigDecimal(duration);
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
				Succession succ = this.sequencer.getLastSuccession();
				PetriStateFeature childFeature = succ.getChild().getFeature(PetriStateFeature.class);
				BigDecimal sojourn = this.time.getSojournTime(succ);

				// Fill up marking information.
				TokenCounterState next = new TokenCounterState();
				next.marking = childFeature.getMarking();
				next.timestamp = this.elapsedTime.doubleValue();
				next.sojourn = sojourn.doubleValue();
				this.state.add(next);
				
				// Move elapsed time forward.
				this.elapsedTime = this.elapsedTime.add(sojourn);
				
				if (this.elapsedTime.compareTo(duration) >= 0) {
					this.sequencer.removeCurrentRunObserver(this);
					this.sequencer.removeObserver(this);
				}
				break;
			case RUN_END:
				this.sequencer.removeCurrentRunObserver(this);
				break;
			case SIMULATION_END:
				break;
			case SIMULATION_START:
				break;
			default:
				break;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		final double step = 1;
		final double duration = 50000;
		PetriNet pn = new PetriNet();
		Marking initialMarking = new Marking();
		Model.build(pn, initialMarking);

		System.out.println("## Simulation -- TOKENS ##");
		// Inspiration:
		// https://github.com/oris-tool/sirio/wiki/Generalized-Stochastic-Petri-Nets
		Sequencer s = new Sequencer(pn, initialMarking, new STPNSimulatorComponentsFactory(), new AnalysisLogger() {
			@Override
			public void log(String message) {
				// System.out.println(message);
			}

			@Override
			public void debug(String message) {
				// System.out.println(message);
			}
		});
		TokenCounter tc = new TokenCounter(s, step, duration);
		s.simulate();
		final String tokenSimResultsFileName = "./token_sim_resut_100_1.csv";
		saveTokenCountResultsToFile(tc.state, tokenSimResultsFileName);

		Queue<Double> r1Start = new LinkedList<>();
		Queue<Double> r2Start = new LinkedList<>();
		int r1LastTokens = 0;
		int r2LastTokens = 0;
		List<Double> responseTimes = new LinkedList<>();
		// Assumes that each transition can only move one token at a time.
		for (TokenCounterState c : tc.state) {
			int r1Tokens = c.marking.getTokens(R1_SERV);
			if (r1Tokens < r1LastTokens) { // Request finished.
				responseTimes.add(c.timestamp + c.sojourn - r1Start.remove());
				r1LastTokens = r1Tokens;
			} else if (r1Tokens > r1LastTokens) { // Request started.
				r1Start.add(c.timestamp);
				r1LastTokens = r1Tokens;
			}
			int r2Tokens = c.marking.getTokens(R2_SERV);
			if (r2Tokens < r2LastTokens) { // Request finished.
				responseTimes.add(c.timestamp + c.sojourn - r2Start.remove());
				r2LastTokens = r2Tokens;
			} else if (r2Tokens > r2LastTokens) { // Request started.
				r2Start.add(c.timestamp);
				r2LastTokens = r2Tokens;
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append("response_time\n");
		responseTimes.stream().forEach(rt -> {builder.append(rt); builder.append("\n");});
		Files.writeString(Paths.get("response_times.csv"), builder.toString());

		Pair<Map<Marking, Integer>, double[][]> tr = GSPNTransient.builder().timePoints(0.0, duration, step).build()
				.compute(pn, initialMarking);
		double[][] trValues = tr.second();
		Map<Marking, Integer> trMarkings = tr.first();

		//System.out.println("\n\n## Transient Analysis - PROBS ##");
		final String transProbFileName = "./trans_prob_resut_100_1.csv";
		// saveTransientAnalysisResultsToFile(trMarkings, trValues, transProbFileName);

		System.out.println("\n\n## Steady State Analysis -- PROBS");
		final String steadyStateResultsFileName = "./steady_prob_resut_100_1.csv";
		//steadyStateAnalysis(pn, initialMarking, steadyStateResultsFileName);

		/*
		 * ######## REWARDS #######
		 */

		// Follow:
		// https://github.com/oris-tool/sirio/blob/5015a2fba7f84f959127a909a1eebad43e424a07/sirio/src/test/java/org/oristool/models/gspn/client/AbsorbingRewardTest.java#L90
		// System.out.println("\n\n## Rewards ##\n\n");

		// PetriNet finNet = new PetriNet();
		// Marking initialFinNetMarking = new Marking();
		// ModelFin.build(finNet, initialFinNetMarking);
		// TransientSolution<Marking, RewardRate> simulate = simulate(finNet,
		// initialFinNetMarking,
		// new BigDecimal(duration), new BigDecimal(step), "If(Fin>0,1,0)", 200);
		// System.out.println(Arrays.deepToString(simulate.getSolution()));
	}

	static void saveTokenCountResultsToFile(List<TokenCounterState> state, String fileName) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("n,ts,sojourn,tk_lb,tk_r1,tk_r2");
		builder.append("\n");
		for (int i = 0; i < state.size(); i++) {
			TokenCounterState s = state.get(i);
			builder.append(i + 1);
			builder.append(",");
			builder.append(String.format("%.3f", s.timestamp));
			builder.append(",");
			builder.append(String.format("%.3f", s.sojourn));
			builder.append(",");
			builder.append(s.marking.getTokens(LB_STATE));
			builder.append(",");
			builder.append(s.marking.getTokens(R1_SERV));
			builder.append(",");
			builder.append(s.marking.getTokens(R2_SERV));
			builder.append("\n");
		}
		builder.deleteCharAt(builder.length() - 1); // removing last "\n"
		Files.writeString(Paths.get(fileName), builder.toString());
		System.out.printf("Simulation finished. Results written to %s\n", fileName);
	}

	public static void steadyStateAnalysis(PetriNet pn, Marking initialMarking, String fileName) throws IOException {
		Map<Marking, Double> steadyResults = GSPNSteadyState.builder().build().compute(pn, initialMarking);		
		StringBuilder builder = new StringBuilder();
		Set<Marking> markingSet = steadyResults.keySet();
		for (Marking m : markingSet) {
			builder.append(m.toString().trim().replace(" ", "_"));
			builder.append(",");
		}
		builder.deleteCharAt(builder.length() - 1); // removing last ","
		builder.append("\n");
		for (Marking m : markingSet) {
			builder.append(String.format("%.4f", steadyResults.get(m)));
			builder.append(",");
		}
		builder.deleteCharAt(builder.length() - 1); // removing last ","
		Files.writeString(Paths.get(fileName), builder.toString());
		System.out.printf("Steady state probability calculation finished. Results written to %s\n", fileName);
	}

	public static void saveTransientAnalysisResultsToFile(Map<Marking, Integer> trMarkings, double[][] trValues,
			String fileName) throws IOException {
		Map<Integer, Marking> byID = new HashMap<>();
		for (Entry<Marking, Integer> e : trMarkings.entrySet()) {
			byID.put(e.getValue(), e.getKey());
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < trValues[0].length; i++) {
			builder.append(byID.get(i).toString().trim().replace(" ", "_"));
			builder.append(",");
		}
		builder.deleteCharAt(builder.length() - 1); // removing last ","
		builder.append("\n");
		for (int row = 0; row < trValues.length; row++) {
			for (int col = 0; col < trValues[row].length; col++) {
				builder.append(String.format("%.4f", trValues[row][col]));
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1); // removing last ","
			builder.append("\n");
		}
		builder.deleteCharAt(builder.length() - 1); // removing last "\n"
		Files.writeString(Paths.get(fileName), builder.toString());
		System.out.printf("Transient probability calculation finished. Results written to %s\n", fileName);
	}

	public static TransientSolution<Marking, RewardRate> simulate(PetriNet net, Marking initialMarking,
			BigDecimal timeLimit, BigDecimal timeStep, String rewardString, int runsNumber) {
		Sequencer s = new Sequencer(net, initialMarking, new STPNSimulatorComponentsFactory(), new AnalysisLogger() {
			@Override
			public void log(String message) {
			}

			@Override
			public void debug(String string) {
			}
		});

		// Derive the number of time points
		int samplesNumber = (timeLimit.divide(timeStep)).intValue() + 1;

		// Create a reward (which is a sequencer observer)
		TransientMarkingConditionProbability reward = new TransientMarkingConditionProbability(s,
				new ContinuousRewardTime(timeStep), samplesNumber, MarkingCondition.fromString(rewardString));

		// Create a reward evaluator (which is a reward observer)
		new RewardEvaluator(reward, runsNumber);

		// Run simulation
		s.simulate();

		// Get simulation results
		TimeSeriesRewardResult result = (TimeSeriesRewardResult) reward.evaluate();

		return getTransientSolutionFromSimulatorResult(result, rewardString, initialMarking, timeLimit, timeStep);
	}

	public static TransientSolution<Marking, RewardRate> getTransientSolutionFromSimulatorResult(
			TimeSeriesRewardResult result, String rewardString, Marking initialMarking, BigDecimal timeLimit,
			BigDecimal timeStep) {

		RewardRate rewardRate = RewardRate.fromString(rewardString);
		List<Marking> regenerations = new ArrayList<>(Arrays.asList(initialMarking));
		List<RewardRate> columnStates = new ArrayList<>();
		columnStates.add(rewardRate);

		TransientSolution<Marking, RewardRate> solution = new TransientSolution<>(timeLimit, timeStep, regenerations,
				columnStates, initialMarking);

		List<Marking> mrkTmp = new ArrayList<>(result.getMarkings());
		TransientSolution<Marking, Marking> tmpSolution = new TransientSolution<>(timeLimit, timeStep, regenerations,
				mrkTmp, initialMarking);

		for (int t = 0; t < tmpSolution.getSolution().length; t++) {
			for (int i = 0; i < mrkTmp.size(); i++) {
				tmpSolution.getSolution()[t][0][i] = result.getTimeSeries(mrkTmp.get(i))[t].doubleValue();
			}
		}

		// Evaluate the reward
		TransientSolution<Marking, RewardRate> rewardTmpResult = TransientSolution.computeRewards(false, tmpSolution,
				rewardRate);
		for (int t = 0; t < solution.getSolution().length; t++) {
			solution.getSolution()[t][0][columnStates.indexOf(rewardRate)] = rewardTmpResult.getSolution()[t][0][0];
		}

		return solution;
	}

}
