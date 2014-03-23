package nsp.util;

import java.util.Iterator;
import java.util.Map;

import nsp.Occupancy;

public class Stats {

	private boolean binary = true;
	private int n_infested;
	private double Kno;
	private double Kallocation;
	private double Kquantity;
	private double Khisto;
	private double Kstandard;
	private double chanceAgreement;
	private double quantityAgreement;
	private double allocationAgreement;
	private double allocationDisagreement;
	private double quantityDisagreement;
	private double pierceSkill;
	private double figureOfMerit;

	/**
	 * Return statistics based on Pontius' Excel spreadsheet
	 * 
	 * @param matrix
	 *            - A square confusion matrix of arbitrary size
	 * @return - an array of statistics values. 0 = Kno; 1=Kallocation;
	 *         2=Kquantity; 3=Khisto; 4=Kstandard; 5=Chance Agreement;
	 *         6=Quantity Agreement; 7=Allocation Agreement; 8=Allocation
	 *         Disagreement; 9=Quantity Disagreeement; 10=Pierce Skill (if
	 *         binary); 11=Figure Of Merit (if binary)
	 */

	public double[] pontiusStats(int[][] matrix) {
		double[] da = new double[binary ? 13 : 11];
		int bins = matrix.length;
		double binfrac = 1d / bins;

		int[] rs = MatrixUtils.rowSum(matrix);
		int[] cs = MatrixUtils.colSum(matrix);
		int total = MatrixUtils.sum(rs);
		
		n_infested = cs[1];

		double[] cprop = MatrixUtils.scalarDivide(MatrixUtils.int2double(rs),
				total);
		double[] rprop = MatrixUtils.scalarDivide(MatrixUtils.int2double(cs),
				total);
		//double[] agreement = MatrixUtils.scalarDivide(
		//		MatrixUtils.int2double(MatrixUtils.diag(matrix)), total);
		//double[] commission = MatrixUtils.subtract(rprop, agreement);
		//double[] omission = MatrixUtils.subtract(cprop, agreement);
		//double[] totalDifference = MatrixUtils.add(commission, omission);
		//double[] quantityDifference = MatrixUtils.abs(MatrixUtils.subtract(
		//		omission, commission));
		//double[] allocationDifference = MatrixUtils.scalarMultiply(
		//		MatrixUtils.minOf(commission, omission), 2d);
		//double[] commissionIntensity = MatrixUtils.arrayDivide(commission,
		//		rprop);
		double[] mincprop = MatrixUtils.minOf(cprop, binfrac);
		double[] min2cprop = MatrixUtils.minOf(rprop, cprop);
		double[] product = MatrixUtils.arrayMultiply(rprop, cprop);
		//double[] gainIntensity = MatrixUtils.arrayDivide(omission, cprop);

		double[][] allocation = new double[3][3];
		allocation[0][0] = MatrixUtils.sum(mincprop);
		allocation[0][1] = MatrixUtils.sum(min2cprop);
		allocation[0][2] = 1d;
		allocation[1][1] = MatrixUtils.trace(matrix) / (double) total;
		allocation[2][0] = binfrac;
		allocation[2][1] = MatrixUtils.sum(product);
		allocation[2][2] = MatrixUtils.dot(cprop, cprop);

		Kno = (allocation[1][1] - binfrac) / (binfrac);
		Kallocation = (allocation[1][1] - allocation[2][1])
				/ (allocation[0][1] - allocation[2][1]);

		allocation[1][0] = binfrac + Kallocation
				* (allocation[0][0] - allocation[2][0]);
		allocation[1][2] = allocation[2][2] + Kallocation
				* (1 - allocation[2][2]);

		Kquantity = (allocation[1][1] - allocation[1][0])
				/ (allocation[1][2] - allocation[1][0]);
		Khisto = (allocation[0][1] - allocation[2][1]) / (1 - allocation[2][1]);
		Kstandard = (allocation[1][1] - allocation[2][1])
				/ (1 - allocation[2][1]);
		chanceAgreement = MatrixUtils.min(new double[] {
				allocation[1][1], allocation[2][1] }, binfrac);
		quantityAgreement = MatrixUtils.min(new double[] { binfrac,
				allocation[2][1], allocation[1][1] }) == binfrac ? Math
				.min(allocation[2][1] - binfrac, allocation[1][1] - binfrac)
				: 0;
		allocationAgreement = Math.max(allocation[1][1]
				- allocation[2][1], 0);
		allocationDisagreement = (allocation[0][1] - allocation[1][1]);
		quantityDisagreement = (1 - allocation[0][1]);

		da[0] = n_infested;
		da[1] = Kno;
		da[2] = Kallocation;
		da[3] = Kquantity;
		da[4] = Khisto;
		da[5] = Kstandard;
		da[6] = chanceAgreement;
		da[7] = quantityAgreement;
		da[8] = allocationAgreement;
		da[9] = allocationDisagreement;
		da[10] = quantityDisagreement;

		if (binary) {
			pierceSkill = (matrix[0][0] / ((double) matrix[0][0] + (double) matrix[1][0]))
					- (matrix[0][1] / ((double) matrix[0][1] + (double) matrix[1][1]));
			figureOfMerit = matrix[0][0]
					/ ((double) matrix[0][0] + (double) matrix[1][0] + matrix[0][1]);
			da[11] = pierceSkill;
			da[12] = figureOfMerit;
		}

		return da;
	}
	
	public int[][] makeConfusionMatrix(boolean[] a, boolean[] b){
		
		if (a == null) {
			throw new IllegalArgumentException("Reference patches are null");
		}
		if (b == null) {
			throw new IllegalArgumentException("Observed patches are null");
		}
		if (a.length!=b.length) {
			throw new IllegalArgumentException(
					"Patch sets do not contain the same number of items.  Reference: "
							+ a.length + ",Comparison: "
							+ b.length);
		}
		
		// TODO Possibly change to more general form

		int[][] c_matrix = new int[2][2];
		for(int i = 0; i < a.length; i++){		
				boolean comp_infested = a[i];
				boolean ref_infested = b[i];
			
				if (comp_infested && ref_infested) {
					c_matrix[0][0]++;
					continue;
				}
				if (comp_infested && !ref_infested){
					c_matrix[1][0]++;
					continue;
				}
				if (!comp_infested && ref_infested) {
					c_matrix[0][1]++;
					continue;
				}
				if (!comp_infested	&& !ref_infested) {
					c_matrix[1][1]++;
					continue;
				}
		}

		return c_matrix;
		
	}

	public int[][] makeConfusionMatrix(Map<Integer, Occupancy> reference,
			Map<Integer, Occupancy> comparison) {
		if (reference == null) {
			throw new IllegalArgumentException("Reference patches are null");
		}
		if (comparison == null) {
			throw new IllegalArgumentException("Observed patches are null");
		}
		if (reference.size() != comparison.size()) {
			throw new IllegalArgumentException(
					"Patch sets do not contain the same number of items.  Reference: "
							+ reference.size() + ",Comparison: "
							+ comparison.size());
		}

		// TODO Possibly change to more general form

		int[][] c_matrix = new int[2][2];
		Iterator<Integer> it = comparison.keySet().iterator();

		while (it.hasNext()) {

				int key = it.next();
				
				if(comparison.get(key).hasNoData()||reference.get(key).hasNoData()){
					continue;
				}
				
				boolean comp_infested = comparison.get(key).isInfested();
				boolean ref_infested = reference.get(key).isInfested();
			
				if (!comp_infested && !ref_infested) {
					c_matrix[0][0]++;
					continue;
				}
				if (comp_infested && !ref_infested){
					c_matrix[1][0]++;
					continue;
				}
				if (!comp_infested && ref_infested) {
					c_matrix[0][1]++;
					continue;
				}
				if (comp_infested	&& ref_infested) {
					c_matrix[1][1]++;
					continue;
				}
		}

		return c_matrix;
	}
	
	public int getNInfested(){
		return n_infested;
	}
	
	public void setBinary(boolean binary){
		this.binary=binary;
	}
	
	public boolean isBinary(){
		return binary;
	}
	
	public double getKno() {
		return Kno;
	}

	public double getKallocation() {
		return Kallocation;
	}

	public double getKquantity() {
		return Kquantity;
	}

	public double getKhisto() {
		return Khisto;
	}

	public double getKstandard() {
		return Kstandard;
	}

	public double getChanceAgreement() {
		return chanceAgreement;
	}

	public double getQuantityAgreement() {
		return quantityAgreement;
	}

	public double getAllocationAgreement() {
		return allocationAgreement;
	}

	public double getAllocationDisagreement() {
		return allocationDisagreement;
	}

	public double getQuantityDisagreement() {
		return quantityDisagreement;
	}

	public double getPierceSkill() {
		return pierceSkill;
	}

	public double getFigureOfMerit() {
		return figureOfMerit;
	}

}
