package CostSaver;

public class GradientAscent {
	public double theta0;
	public double theta1;
	public double theta2;
	private double alpha;
	
	public GradientAscent(){
		theta0=0;
		theta1=0;
		theta2=0;
		alpha= 0.1;
	}
	
	public double h(double[] x) {
		theta0=1;
		theta1=1;
		theta2=-1;
		double z = theta0 + theta1*x[0] +theta2*x[1];
		double h = 1/(1+Math.exp(-1*z));
		return h;
	}
	
	public void update(double[] x, int y){
		theta0 = theta0 + alpha*(y-h(x));
		theta1 = theta1 + alpha*(y-h(x))*x[0];
		theta2 = theta2 + alpha*(y-h(x))*x[1];
//		System.out.println("theta0:"+theta0+" theta1:"+theta1+" theta2:"+theta2);
//		System.out.println(h(x)+":"+y);
	}
}
