
public class CalculateAccuracy {
	public static int minputs[] = {4,6,8,4,1,0,0,5,0,0,0,0,0,0,9,0,1,4,3,4,10,2,2,2,2,1,0,9  ,5,0,5,0,0,0,0,6,1,9,1,1,5,6,7,6,6,0,4,1,0,0,0,2,0,0,7,5,7,7,5,0,1,3,1,0,7,7,8,6,6,5,1,2,2,2,0,1,1,4,1,2,0  ,9,3,4,2,  7,5,5,0,5,4,5,7,3,1  ,8,2,2,2,1,2,4,1,2,2,5,4,1,1,1,9,1,1};
	public static int binputs[] = {5,5,9,7,3,1,1,5,1,1,0,2,0,1,8,1,5,3,9,6,10,2,2,2,3,0,3,10,9,0,9,0,0,2,0,6,2,8,2,2,8,4,3,6,8,1,4,1,1,4,3,2,4,2,5,8,8,5,7,1,5,5,5,4,5,8,8,5,7,7,1,2,5,7,2,1,0,6,5,3,7,10,2,4,3,10,9,8,4,7,7,8,9,6,1,10,0,6,2,4,3,4,3,2,3,9,5,2,1,1,8,0,0};
	public static int rightAnws[] = {1,1,1,1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,1,1,1 , 0,0,0,0,0,0,1  ,1,0,1,0,0,0,0,1,0,1,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,1,0,0,1,1,1,1,1,1,0,0,0,1,0,0,0,1,0,0,0,1,  0,0,0,1,  1,1,0,1,1,1,1,1,0,1,  0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0};
	
//	public static int minputs[] = {0,0,2,0,0,7,5,7,7,5,0,1,3,1,0};
//	public static int binputs[] = {4,3,2,4,2,5,8,8,5,7,1,5,5,5,4};
//	public static int rightAnws[] = {0,0,0,0,0,1,1,1,1,1,0,0,1,0,0};
	
	static int lower = 3;
	static int higher = 4;
	public static int[] getResult(int inputs[]){
		int output[] = new int[inputs.length];
		for(int i = 0;i<inputs.length;i++){
			if(inputs[i]<=lower){
				output[i] = 0;
			} else if(inputs[i]>higher){
				output[i] = 1;
			} else {
				output[i] = -1;
			}
		}
		return output;
	}

	public static void getAccuracy(int inputs[]) {
		int tp = 0;
		int p = 0;
		int fp = 0;
		int n = 0;
		for(int i = 0; i< inputs.length; i++){
			if(inputs[i]==rightAnws[i]&&rightAnws[i]==1){
				tp++;
			}
			if(inputs[i]==1&&rightAnws[i]==0){
				fp++;
			}
			if(rightAnws[i]==1){
				p++;
			} else {
				n++;
			}
		}
		System.out.println("TPR:"+(double)tp/p);
		System.out.println("FPR:"+(double)fp/n);
	}
	
	public static void plotRecallAccur(int origin[]) {
		double TRP[] = new double[10];
		double FPR[] = new double[10];
		for(int j = 1; j < 10;j++){
			lower=j;
			higher=j;
			int inputs[] =getResult(origin);
			int tp = 0;
			int p = 0;
			int fp = 0;
			int n = 0;
			for(int i = 0; i< inputs.length; i++){
				if(inputs[i]==rightAnws[i]&&rightAnws[i]==1){
					tp++;
				}
				if(inputs[i]==1&&rightAnws[i]==0){
					fp++;
				}
				if(rightAnws[i]==1){
					p++;
				} else {
					n++;
				}
			}
			System.out.println("cutoff:"+j);
			TRP[j] = (double)tp/p;
			FPR[j] = (double)fp/n;
			System.out.println("TPR:"+(double)tp/p);
			System.out.println("FPR:"+(double)fp/n);
			System.out.println();
		}
		for(int i = 1; i<10; i++) {
			System.out.print(TRP[i]+",");
		}
		System.out.println();
		for(int i = 1; i<10; i++) {
			System.out.print(FPR[i]+",");
		}
		System.out.println();
	}
	
	public static void main(String[] args){
//		int bresults[] =getResult(binputs);
//		int mresults[] =getResult(minputs);
//		getAccuracy(bresults);
//		getAccuracy(mresults);
		System.out.println(minputs.length);
//		System.out.println(binputs.length);
//		System.out.println(rightAnws.length);
		
//		plotRecallAccur(binputs);
		plotRecallAccur(minputs);
	}
}
