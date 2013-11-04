package CostSaver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class QuestionModel {
	public HashSet<String> locs = new HashSet<String>();
	public HashMap<String, Integer> loc_mapping = new HashMap<String, Integer>();
	public double all_factor[] = new double[2];
	public double none_factor[] = new double[2];
	public double other_factor[] = new double[2];
	public double[] loc_distribution;
	public boolean all_question;
	public String best_index;
	public double best_prob;

	public QuestionModel(HashSet<String> locs, boolean all_question) {
		this.all_question = all_question;
		this.locs = locs;
		Iterator<String> itr = locs.iterator();
		int index = 0;
		while (itr.hasNext()) {
			String loc = itr.next();
			loc_mapping.put(loc, index);
			index++;
		}
		all_factor[0] = 1 - expWithNetwork.P_all;
		all_factor[1] = expWithNetwork.P_all;
		none_factor[0] = 1 - expWithNetwork.P_none;
		none_factor[1] = expWithNetwork.P_none;
		other_factor[0] = 1 - expWithNetwork.P_other;
		other_factor[1] = expWithNetwork.P_other;

		int array_length = (int) Math.pow(2, locs.size());
		loc_distribution = new double[array_length];
		for (int j = 0; j < loc_distribution.length; j++) {
			loc_distribution[j] = 1;
		}
	}

	public void normalize() {
		double total = all_factor[0] + all_factor[1];
		all_factor[0] = all_factor[0] / (total);
		all_factor[1] = all_factor[1] / (total);

		total = none_factor[0] + none_factor[1];
		none_factor[0] = none_factor[0] / (total);
		none_factor[1] = none_factor[1] / (total);

		total = other_factor[0] + other_factor[1];
		other_factor[0] = other_factor[0] / (total);
		other_factor[1] = other_factor[1] / (total);

		total = 0;
		for (int i = 0; i < loc_distribution.length; i++) {
			total += loc_distribution[i];
		}

		for (int i = 0; i < loc_distribution.length; i++) {
			loc_distribution[i] = loc_distribution[i] / total;
		}
	}

	public double solve() {
		double total = 0;
		double all = all_factor[1]*none_factor[0]*loc_distribution[loc_distribution.length-1];
		double none = all_factor[0]*none_factor[1]*loc_distribution[0];
		total+=all;
		total+=none;
		
		double probs[] = new double[loc_distribution.length];
		double max = none;
		int max_index = 0;
		probs[0] = none;
		for (int i = 1; i < loc_distribution.length; i++) {
			probs[i] = loc_distribution[i] * all_factor[0] * none_factor[0];
			if(probs[i] > max){
				max = probs[i];
				max_index= i;
			}
			total+=probs[i];
		}
//		System.out.println("all:"+all/total);
//		System.out.println("none:"+none/total);
		
//		String loc_string = "";
//		for (int i = 0; i < loc_distribution.length; i++) {
//			loc_string = loc_string + probs[i]/total + " ";
//		}
//		System.out.println(loc_string);
		if(max>all){
//			System.out.println("best:"+Integer.toBinaryString(max_index));
			best_index = Integer.toBinaryString(max_index);
			best_prob = max;
			return max;
		} else {
//			System.out.println("best: all");
			best_index = "all";
			best_prob = all;
			return all;
		}
//		System.out.println("");
	}
	
	public String toString() {
		String all_string = all_factor[0] + " " + all_factor[1];
		String none_string = none_factor[0] + " " + none_factor[1];
		// String all_string = all_factor[0]+" "+all_factor[1];

		String loc_string = "";
		for (int i = 0; i < loc_distribution.length; i++) {
			loc_string = loc_string + loc_distribution[i] + " ";
		}
		return "all_factor:" + all_string + " none_factor:" + none_string
				+ " loc:" + loc_string;
	}
}
