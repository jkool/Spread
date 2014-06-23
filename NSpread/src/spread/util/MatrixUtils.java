package spread.util;

/**
 * A collection of methods for processing vectors and matrices of primitive values.
 */

public class MatrixUtils {

	/**
	 * Returns the absolute value for an array of doubles
	 * @param da
	 * @return
	 */
	
	public static double[] abs(double[] da){
		double[] out = da.clone();
		for(int i = 0; i < out.length; i++){
			out[i] = Math.abs(out[i]);
		}
		return out;
	}
	
	/**
	 * Adds two vectors of doubles
	 * @param a
	 * @param b
	 * @return
	 */
	
	public static double[] add(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]+b[i];
		}
		return out;
	}
	
	/**
	 * Divides two vectors of doubles, array-wise.
	 * @param a
	 * @param b
	 * @return
	 */
	
	public static double[] arrayDivide(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]/b[i];
		}
		return out;
	}

	/**
	 * Multiplies two vectors of doubles, array-wise.
	 * @param a
	 * @param b
	 * @return
	 */
	
	public static double[] arrayMultiply(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]*b[i];
		}
		return out;
	}
	
	/**
	 * Sums a matrix along columns (across rows), assuming row,column orientation
	 * @param na
	 * @return
	 */
	
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
	
	/**
	 * Converts a vector of values into a diagonal matrix.
	 * @param ia
	 * @return
	 */
	
	public static int[] diag(int[][] ia){
		int[] out = new int[ia.length];
		for(int i = 0; i < out.length; i++){
			out[i] = ia[i][i];
		}
		return out;
	}
	
	/**
	 * Returns the dot product of two vectors.
	 * @param a
	 * @param b
	 * @return
	 */

	public static double dot(double[] a,double[]b){
		double out = 0;
		for(int i = 0; i < a.length; i++){
			out += a[i]*b[i];
		}
		return out;
	}
	
	/**
	 * Extracts a single column from a matrix
	 * @param ia - the original matrix
	 * @param col - the index of the column to be extracted
	 * @return
	 */
	
	public static int[] getCol(int[][] ia, int col){
		int[] out = new int[ia.length];
		for(int i = 0; i < out.length; i++){
			out[i] = ia[i][col];
		}
		return out;
	}
	
	/**
	 * Extracts a single row from a matrix
	 * @param ia - the original matrix
	 * @param row - the index of the row to be extracted
	 * @return
	 */
	
	public static int[] getRow(int[][] ia, int row){
		return ia[row].clone();
	}
	
	/**
	 * Converts a vector of ints to doubles.
	 * @param ia
	 * @return
	 */
	
	public static double[] int2double(int[] ia){
		double[] out = new double[ia.length];
		for(int i=0; i < ia.length; i++){
			out[i] = ia[i];
		}
		return out;
	}
	
	/**
	 * Returns the minimum value in vector of doubles.
	 * @param da
	 * @return
	 */
	
	public static double min(double[] da){
		if(da.length==0){return Double.NaN;}
		double out = Double.POSITIVE_INFINITY;
		for(int i = 0; i < da.length; i++){
			out=Math.min(out, da[i]);
		}
		return out;
	}
	
	/**
	 * Returns the minimum value between array of doubles and a given value.
	 * @param da
	 * @param val
	 * @return
	 */
	
	public static double min(double[] da, double val){
		double out = val;
		for(int i = 0; i< da.length; i++){
			out=Math.min(out, da[i]);
		}
		return out;
	}
	
	/**
	 * Returns an array containing the minimum of the array value or the given value
	 * @param da - the array of input values
	 * @param val - the minimum comparison value
	 * @return
	 */
	
	public static double[] minOf(double[] da, double val){
		double[] out = new double[da.length];
		for(int i = 0; i < out.length; i++){
			out[i] = Math.min(da[i], val);
		}
		return out;
	}

	/**
	 * Returns an array containing the pair-wise minimum among two arrays
	 * @param a - the first array of input values
	 * @param b - the second array of input values
	 * @return
	 */
	
	public static double[] minOf(double[] a,double[] b){
		double[] out = new double[a.length];
		for(int i = 0; i < out.length; i++){
			out[i] = Math.min(a[i], b[i]);
		}
		return out;
	}

	/**
	 * Sums a matrix along rows (across columns), assuming row,column orientation
	 * @param na
	 * @return
	 */
	
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
	
	/**
	 * Divides an array of doubles by a scalar value
	 * @param a - the vector of doubles
	 * @param b - the scalar divisor
	 * @return
	 */
	
	public static double[] scalarDivide(double[] a,double b ){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]/b;
		}
		return out;
	}
	
	/**
	 * Multiplies an array of doubles by a scalar value
	 * @param a - the vector of doubles
	 * @param b - the scalar multiplier
	 * @return
	 */	
	
	public static double[] scalarMultiply(double[] a,double b ){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]*b;
		}
		return out;
	}
	
	/**
	 * Returns the array-wise difference between two vectors.
	 * @param a
	 * @param b
	 * @return
	 */
	
	public static double[] subtract(double[] a,double[]b){
		double[] out = new double[a.length];
		for(int i = 0; i < a.length; i++){
			out[i] = a[i]-b[i];
		}
		return out;
	}
	
	/**
	 * Returns the sum of an array of doubles
	 * @param da - an array of doubles
	 * @return
	 */
	
	public static double sum(double[] da){
		double sum = 0;
		for(double d:da){
			sum+=d;
		}
		return sum;
	}
	
	/**
	 * Returns the sum of an array of ints
	 * @param ia - an array of ints
	 * @return
	 */
	
	public static int sum(int[] ia){
		int sum = 0;
		for(int i:ia){
			sum+=i;
		}
		return sum;
	}
	
	/**
	 * Returns the trace of a matrix of doubles (sum of elements on the diagonal)
	 * @param matrix
	 * @return
	 */
	
	public static double trace(double[][] matrix){
		double out = 0;
		for(int i = 0; i<matrix.length;i++){
			out+=matrix[i][i];
		}
		return out;
	}
	
	/**
	 * Returns the trace of a matrix of ints (sum of elements on the diagonal)
	 * @param matrix
	 * @return
	 */
	
	public static int trace(int[][] matrix){
		int out = 0;
		for(int i = 0; i<matrix.length;i++){
			out+=matrix[i][i];
		}
		return out;
	}
}
