import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.oristool.models.gspn.GSPNTransient;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.util.Pair;

public class ProblemEvaluator {

	public static void main(String[] args) throws IOException {
		PetriNet pn = new PetriNet();
		Marking im = new Marking();
		ProblemModel100.build(pn, im);
		TransientSolution<Marking, RewardRate> reward = calculateRewards(pn, im);
		
		PetriNet pn1 = new PetriNet();
		Marking im1 = new Marking();
		ProblemModelZeroGC.build(pn1, im1);
		TransientSolution<Marking, RewardRate> reward1 = calculateRewards(pn1, im1);

		StringBuilder b = new StringBuilder();

		for (int t = 1; t < reward.getSamplesNumber(); t++) {
			b.append(reward.getStep().multiply(new BigDecimal(t)));
			b.append(",");
			b.append(reward.getSolution()[t][0][0]);
			b.append(",");
			b.append(reward1.getSolution()[t][0][0]);
			b.append("\n");
		}
		Files.writeString(Paths.get("problem_cmp.csv"), b.toString());
	}

	private static TransientSolution<Marking, RewardRate> calculateRewards(PetriNet pn, Marking im) {
		final long step = 1;
		final long endTime = 30000;
		Pair<Map<Marking, Integer>, double[][]> result = GSPNTransient.builder()
				.error(1e-6)
				.timePoints(0, endTime, step)
				.build()
				.compute(pn, im);
		
		TransientSolution<Marking, Marking> solution = TransientSolution.fromArray(
				result.second(),
				step,
				result.first(),
				im);

		TransientSolution<Marking, RewardRate> reward = TransientSolution.computeRewards(
				false, // cumulative
				solution, // transient solution
				"If(S_fin>0,1,0)"); // reward function
		return reward;
	}
}
