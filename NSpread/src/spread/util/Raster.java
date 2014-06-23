package spread.util;

import java.util.Arrays;

/**
 * Class representing a Raster object.
 */

public class Raster {
	
	protected double[][] data;
	protected double xll;
	protected double yll;
	protected double cellsize;
	protected int cols;
	protected int rows;

	protected String NDATA;

	public static final String DEFAULT_NODATA = "-9999";

	/**
	 * Creates an empty raster
	 */
	public Raster() {
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param cellsize - the cellsize of the data
	 * @param xll - the lower left x coordinate of the data
	 * @param yll - the lower left y coordinate of the data
	 */
	public Raster(double cellsize, double xll, double yll) {
		this();
		setCellsize(cellsize);
		setXll(xll);
		setYll(yll);
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param cellsize - the cellsize of the data
	 * @param xll - the lower left x coordinate of the data
	 * @param yll - the lower left y coordinate of the data
	 * @param numRows - the number of rows in the raster
	 * @param numCols = the number of columns in the raster
	 */
	public Raster(double cellsize, double xll, double yll, int numRows,
			int numCols) {
		this();
		setCellsize(cellsize);
		setXll(xll);
		setYll(yll);
		setSize(numRows, numCols);
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param data
	 * @param cellsize
	 * @param xll
	 * @param yll
	 */
	
	public Raster(double[][] data, double cellsize, double xll, double yll) {
		this(cellsize, xll, yll);
		setData(data);
	}

	/**
	 * Creates a raster from the given data using the dimensions provided and a cell size of 1.
	 * 
	 * @param minX - the minimum x (horizontal) coordinate
	 * @param minY - the minimum y (vertical) coordinate
	 * @param maxX - the maximum x (horizontal) coordinate
	 * @param maxY - the maximum y (vertical) coordinate
	 */
	
	public Raster(int minX, int minY, int maxX, int maxY) {
		this();
		setCellsize(1);
		setXll(minX);
		setYll(minY);
		setSize(maxY - minY + 1, maxX - minX + 1);
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param data
	 * @param cellsize
	 * @param xll
	 * @param yll
	 */
	public Raster(int[][] data, double cellsize, double xll, double yll) {
		this(cellsize, xll, yll);
		setData(data);
	}

	/**
	 * Converts a column index value to its corresponding x coordinate
	 * @param col - the index of the column value
	 * @return
	 */
	
	public int colToX(int col) {
		return col + (int) xll;
	}
	
	/**
	 * Returns the cell size of the raster
	 * @return
	 */

	public double getCellsize() {
		return cellsize;
	}
	
	/**
	 * Returns the number of columns in the raster
	 * @return
	 */

	public int getCols() {
		return cols;
	}

	/**
	 * Returns the underlying data array - NOTE: this is *NOT* a copy
	 * 
	 * @return - the underlying data array
	 */

	public double[][] getData() {
		return data;
	}
	
	/**
	 * Returns the NoData value being used by the raster
	 * @return
	 */
	
	public String getNDATA() {
		return NDATA;
	}

	/**
	 * Returns the number of rows in the raster
	 * @return
	 */
	
	public int getRows() {
		return rows;
	}
	
	/**
	 * Returns an empty raster using the values of the parameters as currently set.
	 * 
	 * @param data
	 * @param xll
	 * @param yll
	 * @param size
	 * @return
	 */
	
	public static Raster getTempRaster(double[][] data, double xll, double yll,
			double size) {
		return getTempRaster(data, xll, yll, size, DEFAULT_NODATA);
	}
	
	/**
	 * Retrieves an empty raster created using the provided parameters
	 * @param data
	 * @param xll
	 * @param yll
	 * @param size
	 * @param ndata
	 * @return
	 */
	
	public static Raster getTempRaster(double[][] data, double xll, double yll,
			double size, String ndata) {
		Raster a = new Raster();
		a.data = data;
		a.xll = xll;
		a.yll = yll;
		a.cellsize = size;
		a.NDATA = ndata;
		a.rows = data.length;
		a.cols = data[0].length;
		return a;
	}
	
	/**
	 * Retrieves the value at the given pair of row and index values.
	 * @param row
	 * @param column
	 * @return
	 */

	public double getValue(int row, int column) {
		if (row < rows && column < cols && row >= 0 && column >= 0)
			return data[row][column];
		return Double.NaN;
	}
	
	/**
	 * Retrieves the leftmost position of the raster.
	 * @return
	 */

	public double getXll() {
		return xll;
	}

	/**
	 * Returns the value at the given XY coordinate, assuming that x is relative
	 * to Xll and y is relative to Yll
	 * 
	 * @param x
	 *            - the x co-ordinate of the desired value
	 * @param y
	 *            - the y co-ordinate of the desired value
	 * @return - the raster value at the given coordinate pair.
	 */

	public double getXYValue(int x, int y) {
		return getValue(yToRow(y), xToCol(x));
	}

	public double getYll() {
		return yll;
	}

	/**
	 * Sets the parameters of this raster (rows, columns, corner, cellsize,
	 * NDATA etc) to be the same as the other raster. This includes initialising
	 * the data array with NDATAs
	 * 
	 * @param other
	 */
	public void init(Raster other) {
		xll = other.xll;
		yll = other.yll;
		cellsize = other.cellsize;
		NDATA = other.NDATA;
		setSize(other.getRows(), other.getCols());
	}

	/**
	 * Initialises the Raster to Double.NaN (i.e. NDATA)
	 */
	public void initData() {
		initData(Double.NaN);
	}

	/**
	 * Initialises the raster so the entire data array contains 'value'
	 * 
	 * @param value
	 */
	public void initData(double value) {
		data = new double[rows][];
		for (int i = 0; i < rows; i++) {
			data[i] = new double[cols];

			Arrays.fill(data[i], value);
		}
	}
	
	/**
	 * Indicates whether the shape, position and cell size are consistent
	 * with another raster
	 * 
	 * @param other - the comparison raster
	 * @return
	 */

	public boolean isConsistent(Raster other){
		if(this.rows==other.rows &&
		   this.cols==other.cols	&&
		   this.xll==other.xll &&
		   this.yll==other.yll &&
		   this.cellsize==other.cellsize){return true;}
		return false;
	}
	
	/**
	 * Prints the raster to the console.
	 */

	public void print() {
		System.out.println("Rows: " + rows + " cols: " + cols + " cellsize "
				+ cellsize);
		for (double[] row : data) {
			for (double val : row)
				System.out.print(val + " ");
			System.out.println("");
		}

	}
	
	/**
	 * Converts a row index to a y coordinate value.
	 * @param row - the index of the row
	 * @return
	 */

	public int rowToY(int row) {
		return (int) yll - (row + 1 - rows);
	}
	
	/**
	 * Sets the cell size of the raster.
	 * @param cellsize - the cell size of the raster
	 */

	public void setCellsize(double cellsize) {
		this.cellsize = cellsize;
	}
	
	/**
	 * Sets the number of columns of the raster.
	 * @param cols - the number of columns
	 */

	public void setCols(int cols) {
		this.cols = cols;
	}

	/**
	 * Copies the given data into the underlying data array. Also updates the
	 * number of rows and columns.
	 * 
	 * @param data
	 */
	public void setData(double[][] data) {
		rows = data.length;
		cols = data[0].length;
		initData();
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				this.data[i][j] = data[i][j];
	}

	/**
	 * Copies the given data into the underlying data array. Also updates the
	 * number of rows and columns.
	 * 
	 * @param data
	 */
	public void setData(int[][] data) {
		rows = data.length;
		cols = data[0].length;
		initData();
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				this.data[i][j] = data[i][j];
	}
	
	/**
	 * Sets the NoData value.
	 * @param nDATA
	 */

	public void setNDATA(String nDATA) {
		NDATA = nDATA;
	}

	/**
	 * Sets the number of rows of the raster
	 * @param rows
	 */
	
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * Sets the size of the raster, and also initializes the array with NDATA
	 * 
	 * @param nrows
	 * @param columns
	 */
	public void setSize(int nrows, int columns) {
		this.rows = nrows;
		this.cols = columns;
		initData();
	}

	/**
	 * Sets the cell value for a given row and column.
	 * @param row - the row index
	 * @param column - the column index
	 * @param value - the value to which the cell will be set
	 */
	
	public void setValue(int row, int column, double value) {
		if (row < rows && column < cols)
			data[row][column] = value;
	}

	/**
	 * Sets the leftmost position of the raster
	 * @param xll
	 */
	
	public void setXll(double xll) {
		this.xll = xll;
	}
	
	/**
	 * Sets the cell value using an xy coordinate pair.
	 * @param x - the x-coordinate value
	 * @param y - the y-coordinate value
	 * @param val - the value to which the cell will be set
	 */

	public void setXYValue(int x, int y, double val) {
		setValue(yToRow(y), xToCol(x), val);
	}
	
	/**
	 * Sets the bottom-most position of the rater
	 * @param yll
	 */

	public void setYll(double yll) {
		this.yll = yll;
	}

	/**
	 * Converts an x coordinate to a column index value.
	 * 
	 * @param x
	 * @return
	 */
	
	public int xToCol(int x) {
		return x - (int) xll;
	}
	
	/**
	 * Converts a y coordinate to a row index value.
	 * @param y
	 * @return
	 */
	
	public int yToRow(int y) {
		return (rows - (y - (int) yll)) - 1;
	}
}
