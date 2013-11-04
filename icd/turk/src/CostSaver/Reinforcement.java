package CostSaver;

import org.apache.commons.math3.distribution.NormalDistribution;

public class Reinforcement {
	public double means[] = new double[9];
	public double sd[] = new double[9];
	public double total_responses[] = new double[9];
	public double total_count[] = new double[9];
	public double total_square_responses[] = new double[9];

	public Reinforcement(double total_responses[],
			double total_square_responses[], double total_count[]) {
		this.total_responses = total_responses;
		this.total_square_responses = total_square_responses;
		this.total_count = total_count;
		for (int i = 0; i < means.length; i++) {
			means[i] = total_responses[i] / total_count[i];
			sd[i] = (total_square_responses[i] - total_responses[i]
					* total_responses[i] / total_count[i])
					/ total_count[i];
			sd[i] = Math.sqrt(sd[i]);
		}
	}
	
	public Reinforcement() {
//		double total_responses[]= {48.9,52.5,56.1,59.7,68.1,78.5,85.4,93.2,110.2};
//		double total_square_responses[]={85.45,111.35,130.02,148.93,193.02,259.78,325.03,397.02,515.22};
//		double total_count[]={10.9,10.9,10.9,10.9,10.9,10.9,10.9,10.9,10.9};
		
		double total_responses[]= {975,1024,1068,1106,1195,1302,1371,1461,1647};
		double total_square_responses[]={26468.0,31076.0,34202.0,36914.0,43015.0,51279.0,59378.0,68417.0,84656.0};
		double total_count[]={109,109,109,109,109,109,109,109,109};
		
		this.total_responses = total_responses;
		this.total_square_responses = total_square_responses;
		this.total_count = total_count;
		for (int i = 0; i < means.length; i++) {
			means[i] = total_responses[i] / total_count[i];
			sd[i] = (total_square_responses[i] - total_responses[i]
					* total_responses[i] / total_count[i])
					/ total_count[i];
			sd[i] = Math.sqrt(sd[i]);
		}
	}
	
	public double getCutoff(double budget, int total_disease){
		double max = 0;
		int max_index = 0;
		for(int i = 0; i<total_responses.length; i++) {
			double cur_sd = sd[i]*Math.sqrt(total_disease);
			if(cur_sd==0)
				System.out.println();
			NormalDistribution dis = new NormalDistribution(means[i]*total_disease, cur_sd);
			double p = dis.cumulativeProbability(budget);
			double score = p * 0.1 * (1+i) * total_disease;
			score -= (1-p) * total_disease;
			if(score>max) {
				max = score;
				max_index = i;
			}
		}
		double cutoff = 0.1 * (max_index+1);
		return cutoff;
	}
	
	public void update(int index, int responses){
		total_responses[index]+=responses;
		total_square_responses[index]+=responses*responses;
		total_count[index]++;
		means[index] = total_responses[index] / total_count[index];
		sd[index] = (total_square_responses[index] - total_responses[index]
				* total_responses[index] / total_count[index])
				/ total_count[index];
		sd[index] = Math.sqrt(sd[index]);
	}
}
