package nsp.util;

import cern.colt.Arrays;

public class MatrixUtils {

	public static double[] abs(double[] da){
		double[] out = da.clone();
		for(int i = 0; i < out.length; i++){
			out[i] = Math.abs(out[i]);
		}
		return out;
	}
	
	public static double[] add(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]+b[i];
		}
		return out;
	}
	
	public static double[] arrayDivide(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]/b[i];
		}
		return out;
	}
	
	public static double[] arrayMultiply(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]*b[i];
		}
		return out;
	}
	
	public static int[] colSum(int[][] na){
		if(na==null){
			return null;
		}
		if(na.length==0){return new int[]{};}
		int[] out = new int[na.length];
		for(int i = 0; i < out.length;i++){
			int sum=0;
			for(int j = 0; j < na[i].length;j++){
				sum+=na[i][j];
			}
			out[i]=sum;
		}
		return out;
	}
	
	public static int[] diag(int[][] ia){
		int[] out = new int[ia.length];
		for(int i = 0; i < out.length; i++){
			out[i] = ia[i][i];
		}
		return out;
	}
	
	public static double[] difference(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]-b[i];
		}
		return out;
	}

	public static double dot(double[] a,double[]b){
		double out = 0;
		for(int i = 0; i < a.length; i++){
			out += a[i]*b[i];
		}
		return out;
	}
	
	public static int[] getCol(int[][] ia, int col){
		int[] out = new int[ia.length];
		for(int i = 0; i < out.length; i++){
			out[i] = ia[i][col];
		}
		return out;
	}
	
	public static int[] getRow(int[][] ia, int row){
		return ia[row].clone();
	}
	
	public static double[] int2double(int[] ia){
		double[] out = new double[ia.length];
		for(int i=0; i < ia.length; i++){
			out[i] = ia[i];
		}
		return out;
	}
	
	public static void main(String[] args){
		int[][] ia = new int[][]{{1,1,1},{2,2,2},{3,3,3},{4,4,4}};
		System.out.println(Arrays.toString(rowSum(ia)));
		System.out.println(Arrays.toString(colSum(ia)));
	}
	
	public static double min(double[] da){
		if(da.length==0){return Double.NaN;}
		double out = Double.POSITIVE_INFINITY;
		for(int i = 0; i < da.length; i++){
			out=Math.min(out, da[i]);
		}
		return out;
	}
	
	public static double min(double[] da, double val){
		double out = val;
		for(int i = 0; i< da.length; i++){
			out=Math.min(out, da[i]);
		}
		return out;
	}
	
	public static double[] minOf(double[] da, double val){
		double[] out = new double[da.length];
		for(int i = 0; i < out.length; i++){
			out[i] = Math.min(da[i], val);
		}
		return out;
	}
	
	public static double[] minOf(double[] a,double[] b){
		double[] out = new double[a.length];
		for(int i = 0; i < out.length; i++){
			out[i] = Math.min(a[i], b[i]);
		}
		return out;
	}
	
	public static int[] rowSum(int[][] na){
		if(na==null){
			return null;
		}
		if(na.length==0){return new int[]{};}

		int[] out = new int[na[0].length];
		for(int i = 0; i < na.length;i++){
			for(int j = 0; j < out.length;j++){
				out[j]+=na[i][j];
			}
		}
		return out;
	}
	
	public static double[] scalarDivide(double[] a,double b ){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]/b;
		}
		return out;
	}
	
	public static double[] scalarMultiply(double[] a,double b ){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]*b;
		}
		return out;
	}
	
	public static double[] subtract(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]-b[i];
		}
		return out;
	}
	
	public static int sum(int[] ia){
		int sum = 0;
		for(int i:ia){
			sum+=i;
		}
		return sum;
	}
	
	public static double sum(double[] da){
		double sum = 0;
		for(double d:da){
			sum+=d;
		}
		return sum;
	}
	
	public static double trace(double[][] matrix){
		double out = 0;
		for(int i = 0; i<matrix.length;i++){
			out+=matrix[i][i];
		}
		return out;
	}
	public static int trace(int[][] matrix){
		int out = 0;
		for(int i = 0; i<matrix.length;i++){
			out+=matrix[i][i];
		}
		return out;
	}
}
