package Experiment;

public class Worker {
	public String id;
	public int numMutiple;
	public int numSame;
	public int numOther;
	public int TPLocation;
	public int TNLocation;
	public int numRightSame;
	public int numRightOther;
	public int numLocation;
	public double rate;

	public Worker(String id) {
		this.id = id;
		numMutiple = 0;
		numSame = 0;
		numOther = 0;
		TPLocation = 0;
		TNLocation = 0;
		numRightSame = 0;
		numLocation = 0;
		numRightOther = 0;
	}
	
	public double getRate() {
		double output = ((double)TPLocation+TNLocation+6)/(numLocation+10);
		return output;
	}
	
	public double getAccuracy(){
//		return (double)(TPLocation+TNLocation)/numLocation;
		double output = ((double)TPLocation+TNLocation+6)/(numLocation+10);
		return output;
	}
	
}
