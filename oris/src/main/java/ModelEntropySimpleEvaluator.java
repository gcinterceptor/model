import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import org.oristool.analyzer.log.AnalysisMonitor;
import org.oristool.models.gspn.GSPNSteadyState;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.SteadyStateSolution;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

public class ModelEntropySimpleEvaluator {
	public static int N_REPLICAS = 2;
	public static String rewardStr = String.format("If(S_lb>0 && S_exec_tasks==%d,1,0);If(S_serv,1,0)", N_REPLICAS);
	public static int DECIMAL_PLACES = 6;

	public static void main(String[] args) throws IOException {
		// PetriNet pn = new PetriNet();
		// Marking im = new Marking();
		// ModelEntropySimple.build(pn, im);

//		TransientSolution<Marking, RewardRate> reward = calculateRewards(pn, im, rewardStr);
//
//		StringBuilder b = new StringBuilder();
//		b.append("ts,pdv,un");
//		for (int t = 1; t < reward.getSamplesNumber(); t++) {
//			b.append(reward.getStep().multiply(new BigDecimal(t)));
//			b.append(",");
//			b.append(String.format("%.5f", reward.getSolution()[t][0][0]));
//			b.append(",");
//			b.append(String.format("%.5f", reward.getSolution()[t][0][1]));
//			b.append("\n");
//		}
//		Files.writeString(Paths.get("model_entropy_simple.csv"), b.toString());
		StringBuilder b = new StringBuilder();
		b.append("nReplicas,nMarkings,nClasses,pvn,pcp\n");
		for (int replicas = 1; replicas <= 5; replicas++) {
			// Setup.
			PetriNet pn = new PetriNet();
			Marking im = new Marking();
			ModelEntropySimple.r = replicas;
			ModelEntropySimple.omega = 0.0001;
			ModelEntropySimple.mu = 0.0036;
			ModelEntropySimple.pImpact = 0.003;
			ModelEntropySimple.lambda = 1;
			ModelEntropySimple.build(pn, im);
			String[] rewardsStrings = {
					String.format("If(S_exec_tasks==%d,1,0)", replicas),
					String.format("If(S_exec_tasks>0,1,0)", replicas)};

			// Execution.
			// Doing this because RewardRate does not override equals.
			SteadyStateResult result = steadyState(pn, im, String.join(";", rewardsStrings));
			Map<String, BigDecimal> rewards = result.solution.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
			double pvn =rewards.get(rewardsStrings[0]).setScale(DECIMAL_PLACES, RoundingMode.HALF_EVEN).doubleValue();
			double pcp = rewards.get(rewardsStrings[1]).setScale(DECIMAL_PLACES, RoundingMode.HALF_EVEN).doubleValue(); 
			b.append(String.format("%d,%d,%d,%f,%f\n", replicas, result.markingCount, result.classCount, pvn, pcp));
		}
		System.out.println(b.toString());
		Files.writeString(Paths.get("model_entropy_simple.csv"), b.toString());
	}

	private static class SteadyStateResult {
		int classCount;
		int markingCount;
		Map<RewardRate, BigDecimal> solution;
		SteadyStateResult(int classCount, int markingCount, Map<RewardRate, BigDecimal> solution) {
			this.classCount = classCount;
			this.solution = solution;
			this.markingCount = markingCount;
		}
	}
	
	private static SteadyStateResult steadyState(PetriNet pn, Marking im, String rewardStr) {
		class ClassNumberAnalysisMon implements AnalysisMonitor {
			public int classes = 0;
			@Override
			public void notifyMessage(String message) {
				classes = Integer.parseInt(message.split(" ")[1]);
				
			}

			@Override
			public boolean interruptRequested() {
				return false;
			}
			
		}
		ClassNumberAnalysisMon mon = new ClassNumberAnalysisMon();
		Map<Marking, Double> result = GSPNSteadyState.builder().monitor(mon).build().compute(pn, im);
		Map<Marking, BigDecimal> collect = result.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, v -> new BigDecimal(v.getValue()), (k, v) -> k));

		return new SteadyStateResult(
				mon.classes,
				collect.size(),
				SteadyStateSolution.computeRewards(new SteadyStateSolution<Marking>(collect), rewardStr)
				.getSteadyState());
	}

//	private static TransientSolution<Marking, RewardRate> calculateRewards(PetriNet pn, Marking im, String rewardStr) {
//		final long step = 10;
//		final long endTime = 100000;
//		Pair<Map<Marking, Integer>, double[][]> result = GSPNTransient.builder().error(1e-6)
//				.timePoints(0, endTime, step).build().compute(pn, im);
//
//		TransientSolution<Marking, Marking> solution = TransientSolution.fromArray(result.second(), step,
//				result.first(), im);
//
//		TransientSolution<Marking, RewardRate> reward = TransientSolution.computeRewards(false, // cumulative
//				solution, // transient solution
//				rewardStr); // reward function
//		return reward;
//	}
}
