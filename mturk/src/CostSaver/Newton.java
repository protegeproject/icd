package CostSaver;

class Newton {

	static double f(double x) {
		return x*x;
	}

	static double fprime(double x) {
		return 2*x;
	}

	public static void main(String argv[]) {

		double tolerance = .000000001; // Our approximation of zero
		int max_count = 200; // Maximum number of Newton's method iterations

		/*
		 * x is our current guess. If no command line guess is given, we take 0
		 * as our starting point.
		 */

		double x = 100;

		if (argv.length == 1) {
			x = Double.valueOf(argv[0]).doubleValue();
		}

		for (int count = 1; (Math.abs(f(x)) > tolerance) && (count < max_count); count++) {
			x = x - f(x) / fprime(x);
			System.out.println("Step: " + count + " x:" + x + " Value:" + f(x));
		}

		if (Math.abs(f(x)) <= tolerance) {
			System.out.println("Zero found at x=" + x);
		} else {
			System.out.println("Failed to find a zero");
		}
	}

}