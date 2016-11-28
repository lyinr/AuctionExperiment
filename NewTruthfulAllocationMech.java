import java.util.ArrayList;


public class NewTruthfulAllocationMech {
	ArrayList<Agents> agents = new ArrayList<Agents>();
	double targetV = 0;

	
	public void setV(double val){
		this.targetV = val;
	}
	
	public double UniformAllocation(int key, int loop) { //loop只是一个辅助变量，为了排错
		// To determine value of r for user key
		// Return: r for user key
		
		double r = Constances.INITIALR;
		double sum = 0;

		// initialization
		for (int i = 1; i <= agents.size(); i++) {
			if (i != key) {
				double v = agents.get(i - 1).agentV;
				double cv = 1 / agents.get(i - 1).ratioVC;
				double ecvr = 1 - (cv / r)/(Math.E-1);
				if (ecvr <= 0) {
					ecvr = 0;
				} 
				sum = sum + v * ecvr;		
//				System.out.println(r+" i:" + i + "v:" +agents.get(i - 1).agentV + "ecvr:" +ecvr);
			}//if
			
		}//for
 		
		// while for r
		double prisum = 0;
		while (sum <= targetV) {			
			r = r + Constances.INITIALR;// r = r + $
			prisum = sum;
			sum = 0;			
			for (int i = 1; i <= agents.size(); i++) {
				if (i != key) {
					double v = agents.get(i-1).agentV;
					double cv = 1 / agents.get(i-1).ratioVC;
					//double ecvr = Math.E - cv / r;
					double ecvr = 1 - (cv / r)/(Math.E-1);
					if (ecvr <= 0) {
						ecvr = 0;
					} 
					sum = sum + v * ecvr;
//					System.out.println(r +" i:" + i + "cv:" + 1/agents.get(i - 1).ratioVC + "ecvr:" +ecvr);
				}//if
			
			}// for() 
 
		}//sum<= target
		
		return r;
	}

	public double[][] TruthfulMech(double targetV, int loop) { //loop只是一个辅助变量，为了排错
		// To determine allocation of task
		// Return: write xi and pi into file
		int len = agents.size();
		double[] arrayFx = new double[len];
		double[] arrayPx = new double[len];
		double px = 0;
		double fx = 0;
		double r = 0;
		for (int i = 1; i <= agents.size(); i++) {
			
			//compute R
 			r = UniformAllocation(i, loop);
//			System.out.println("*************R:"+r);
//			pause();
//			pause();
 
			//computer f() and payment
			double xcv = 1 / agents.get(i-1).ratioVC;
			double v = agents.get(i-1).agentV;
			double c = agents.get(i-1).agentC;
			
			double ecvr = 1 - (xcv / r)/(Math.E-1); // e-1-y/r : y = c/v	
			if (ecvr <= 0) {
				ecvr = 0;
			}			
	
			fx =ecvr;

			
			if( fx == 0){
				px = 0;
			}else{
				//px = v * (xcv * ecvr + ((Math.E-1)*(Math.E-1)*r)/2 -(Math.E-1)*xcv + xcv*xcv/(2*r));
				px = c * (1 - c/(v*r*(Math.E-1))) + (Math.E-1)*v*r/2 - c + c*c/(2*v*(Math.E-1)*r);
			}
						
			arrayFx[i-1] =fx;
			arrayPx[i-1] = px;
		//	System.out.println(r + "---agent:" + i + "--fx:" + fx + "--px:" + px );
		}
		
		
		double effect = 0;
		double totalpay = 0;
		// Store and Return res
		double[][] res = new double[2][len];
		for (int i = 0; i < len; i++) {
			res[0][i] = arrayFx[i];
			res[1][i] = arrayPx[i];
			effect += arrayFx[i]*agents.get(i).agentV;
			totalpay += arrayPx[i];
			
//			System.out.println(i + "  v:" + agents.get(i).agentV + "   x:" + arrayFx[i]);
		}
		
 		return res;
	}
	
	public static void pause(){
		Thread thd = new Thread();
		try {
			thd.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
