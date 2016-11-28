import java.util.ArrayList;
import java.util.Comparator;

public class Main {

	public static void main(String[] args) {

		double[][] stackPay = new double[Constances.DISTRIBUTION][Constances.LOOP];  // [distribtuion][loop]
		double[][] truthfulPay = new double[Constances.DISTRIBUTION][Constances.LOOP];
		double[][] optPay = new double[Constances.DISTRIBUTION][Constances.LOOP];
		double[][] vcgPay = new double[Constances.DISTRIBUTION][Constances.LOOP];
		
//		double[][][][] rawdate = new double[dis][V][ALG][LOOP];
		double[][][][] rawdate = new double[Constances.DISTRIBUTION][Constances.MAXV/Constances.STEPV][Constances.AGENTNUM][Constances.LOOP];
	
		double [] sameV = new double[Constances.AGENTNUM]; 
		
		System.out.println("-------Generated Random Agents..............");

		for (int targetV = Constances.INITIALV; targetV <= Constances.MAXV; targetV = targetV + Constances.STEPV) {

			for (int loop = 1; loop <= Constances.LOOP; loop++) {

				for (int distribution = 0; distribution <Constances.DISTRIBUTION; distribution++) {

					// System.out.println("<<<<<<<<<<<<"+loop+">>>>>>>>>>>>>");
					// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
					// @generate random agents instance
					Agents agentsObj = new Agents();
					ArrayList<Agents> agentList = new ArrayList<Agents>();
					agentList = agentsObj.generateRandomAgents(distribution);
					int agentlen = agentList.size();

					
					//保存首次生成的V分布，使得同一个loop内，5个分布的V值都对应相等；
					if( distribution == 0){
						for(int i = 0; i < Constances.AGENTNUM; i ++){
							sameV[i] = agentList.get(i).agentV; 
						}
					}else{
						for(int i = 0; i < Constances.AGENTNUM; i ++){
							agentList.get(i).setV(sameV[i]); 
							agentList.get(i).resetVC();
						}
					}
					
					System.out.println("-------Generated Random Agents:"
							+ targetV + "~~~~" + loop + "~~~~" + distribution
							
							);

					// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
					// ==============================
					// algorithm1: StackelBergMech
					// ==============================
					StackelBerg stackInstance = new StackelBerg();

					// delete some bad input
					double totalV = 0;
					for (int i = 0; i < agentlen; i++) {
						stackInstance.agents.add(agentList.get(i));
						totalV += agentList.get(i).agentV;
						agentList.get(i).setLevel(0);
					}
					if (totalV < targetV) {
						loop = loop - 1;
						System.out.println(totalV
								+ "total is too small....................");
						continue;
					}
					// bisection to compute payment
					double hpay = Constances.HIGHPAY; // intial value
					double lpay = 0;
					double mpay = 0;
					boolean flag = true;
					double mv = 0;
					double pripay = 0;

					while (flag == true) {
						pripay = mpay;
						mpay = (hpay + lpay) / 2;

						mv = stackInstance.stackelBergMech(mpay, targetV, loop);
						// System.out.println("mpay:" + mpay + ".....mv:" + mv);
						if (mv < targetV) {
							lpay = mpay;
						} else if (mv > targetV) {
							hpay = mpay;
							if ((mv - targetV) < 0.001 * targetV)
								flag = false;
						}
						
						if (pripay == mpay) {
							System.out.println("!!!!!!!!!!!!!!!!!!!!----Warning:bisection break\n");
							break;
						}

					}// flag

					// Store payment
					stackPay[distribution][loop - 1] = mpay;

					System.out.println(mpay);
					System.out.println(".........StackelBerg---over");
					// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
					// ==============================
					// OPT
					// ==============================
					int optlen = stackInstance.agents.size();
					// System.out.println("agents LEN:"+ optlen);
					ArrayList<Agents> optList = new ArrayList<Agents>();
					for (int i = 0; i < optlen; i++) {
						Agents tem = new Agents();
						tem = stackInstance.agents.get(i);
						tem.resetLeval();
						optList.add(tem);
					}
										
					// System.out.println("c:"+optList.get(0).agentC+"----V:"+
					// optList.get(0).agentV);
					double[] optres = new double[2];
					optres = getOPT(targetV, optList);
					optPay[distribution][loop - 1] = optres[0];

					System.out.println(optres[0]);
					System.out.println(".........Optpayment---over");

					// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
					// ==============================
					// VCG mechanism
					// ==============================
					int optUserNum = (int) optres[1];
					double lastUX = optres[2];

					// vcg initialization
					int vcgLen = stackInstance.agents.size();
					ArrayList<Agents> vcgList = new ArrayList<Agents>();
					for (int i = 0; i < vcgLen; i++) {
						Agents vcgTem = new Agents();
						vcgTem = stackInstance.agents.get(i);
						vcgTem.resetLeval();
						vcgList.add(vcgTem);
					}

					// for(int i = 0; i < 10; i++){
					// System.out.println(vcgList.get(i).agentC + " " +
					// vcgList.get(i).agentV);
					// }

					// compute VCG
					double vcgTotal = 0;

//					System.out.println(vcgList.get(optUserNum - 1).ratioVC);

					for (int j = 1; j <= optUserNum; j++) {

						double vcgPX = 0;
						if (j == optUserNum) {
							vcgPX += vcgList.get(optUserNum - 1).agentC
									* lastUX;
						} else {
							vcgPX += vcgList.get(j - 1).agentC;
						}
						double curC = vcgList.get(j - 1).agentC;
						double curV = vcgList.get(j - 1).agentV;
						double curX = 1;
//						System.out.println(j + "   " + curV / curC + "  "+ curX);

						do {
							curC += 0.02;
							curX = getVCG(targetV, vcgList, j, curV / curC);
							vcgPX += curX * curC;
//							System.out.println(j + "   " + curV / curC + "  " + curX);
						} while (curX > 0);

						vcgTotal += vcgPX;

					}
					vcgPay[distribution][loop - 1] = vcgTotal;

					System.out.println(vcgTotal);
					System.out.println(".........VCGpayment---over");

					// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
					// ///////////////////////////////////////////////
					TruthfulAllocationMech truthfulMech = new TruthfulAllocationMech();
					truthfulMech.setV(targetV);
					for (int i = 0; i < agentlen; i++) {
						agentList.get(i).resetLeval();
						truthfulMech.agents.add(agentList.get(i));
					}

					// for(int i = 0; i < 10; i++){
					// System.out.println(truthfulMech.agents.get(i).agentC +
					// " " + truthfulMech.agents.get(i).agentV);
					// }
					//

					// get fx[] and px[] from ALG2
					double[][] res = new double[2][agentlen];
					res = truthfulMech.TruthfulMech(targetV, loop);

					// To compute budget sumPay
					double sumPay = 0;
					int reslen = res[1].length;
					for (int i = 0; i < reslen; i++) {
						sumPay += res[1][i];
					}

					truthfulPay[distribution][loop - 1] = sumPay;
					System.out.println(sumPay);
					System.out.println(".........TruthfulMech---over  ");

				
				}// distribution
			}// loop
			
			for( int distri = 0; distri < Constances.DISTRIBUTION; distri ++){
				for(int i = 0; i < Constances.LOOP; i++){
					rawdate[distri][targetV/Constances.STEPV-1][0][i] = vcgPay[distri][i];
 					rawdate[distri][targetV/Constances.STEPV-1][1][i] = truthfulPay[distri][i];
 					rawdate[distri][targetV/Constances.STEPV-1][2][i] = stackPay[distri][i];
					rawdate[distri][targetV/Constances.STEPV-1][3][i] = optPay[distri][i];
				}
			}
		}// targetV

		
		saveRawdate(rawdate);
		saveAsd(rawdate);
 
	}

	public static double getVCG(int target, ArrayList<Agents> vcgList,
			int user, double userVC) {
		double aim = target;
		double sum = 0;
		int len = vcgList.size();
		double userX = 0;

		// System.out.println("......optList:"+ len);
		for (int i = 0; i < len; i++) {
			// System.out.println(i+"sum:"+ sum +"-----aim:"+aim);
			if (sum == aim) {
				break;
			} else if (i == user - 1) {
				continue;
			} else {
				double v = vcgList.get(i).agentV;
				double curvc = v / vcgList.get(i).agentC;

				if (curvc > userVC) {
					double ratio = (aim - sum) / v;
					if (ratio >= 1) {
						sum = sum + v;
					} else if (ratio > 0) {
						sum = sum + v * ratio;
						userX = 0;
						break;
					}
				} else {
					double ratio = (aim - sum) / vcgList.get(user - 1).agentV;
					if (ratio >= 1) {
						userX = 1;
						break;
					} else if (ratio > 0) {
						userX = ratio;
						break;
					}
				}
			}

		}// for

		return userX;
	}

	public static double[] getOPT(int target, ArrayList<Agents> optList) {
		double aim = target;
		double sum = 0;
		double optpay = 0;
		int len = optList.size();
		int num = 0;
		double lastUserX = 1;

		// System.out.println("......optList:"+ len);
		for (int i = 0; i < len; i++) {
			// System.out.println(i+"sum:"+ sum +"-----aim:"+aim);
			if (sum == aim) {
				break;
			} else {
				// System.out.println(".................p:"+optpay);
				double v = optList.get(i).agentV;
				double c = optList.get(i).agentC;

				double ratio = (aim - sum) / v;
				// System.out.println("ration:"+ ratio);
				if (ratio >= 1) {
					sum = sum + v;
					num += 1;
					optpay = optpay + c;
				} else if (ratio > 0) {
					sum = sum + v * ratio;
					num += 1;
					optpay = optpay + c * ratio;
					lastUserX = ratio;

				}
				// System.out.println("--------------optPay:"+optpay);
			}

		}// for

		double[] res = new double[3];
		res[0] = optpay;
		res[1] = num;
		res[2] = lastUserX;

		return res;
	}


	public static void pause() {
		Thread thd = new Thread();
		try {
			thd.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveRawdate(double rawdate[][][][]){
		// save rawdate
		
		WriteFile fw = new WriteFile();
		for( int dist = 0; dist < Constances.DISTRIBUTION; dist ++){
			fw.rawDateWriter(Constances.RAWDATEPATH[dist], rawdate[dist]);
		}
	}
	
	public static void saveAsd(double rawdate[][][][]){
		
		double [][][] asd = new double [Constances.MAXV/Constances.STEPV][Constances.ALGORITHM][2];
		double avg = 0;
		double sd = 0;
		
		for(int dist = 0; dist < Constances.DISTRIBUTION; dist ++){
			for(int targetV = Constances.INITIALV; targetV <= Constances.MAXV; targetV += Constances.STEPV){
				for(int alg = 0; alg < Constances.ALGORITHM; alg ++){
					
					avg = 0;					
					//计算均值
					for(int loop = 0; loop < Constances.LOOP; loop ++){
						avg += rawdate[dist][targetV/Constances.STEPV - 1][alg][loop];
					}					
					avg = avg / Constances.LOOP;
					
					//计算方差
					sd = 0;
					for(int loop = 0; loop < Constances.LOOP; loop ++){
						sd +=  Math.pow(rawdate[dist][targetV/Constances.STEPV - 1][alg][loop] - avg, 2);
					}
					sd = Math.sqrt(sd/Constances.LOOP);
					
					asd[targetV/Constances.STEPV - 1][alg][0] = avg;
					asd[targetV/Constances.STEPV - 1][alg][1] = sd;
				}// alg
			}//V
			WriteFile fw = new WriteFile();
			fw.FileWriter(Constances.AVERAGEPATH[dist], asd);			
		}//dist
		
	}
}
