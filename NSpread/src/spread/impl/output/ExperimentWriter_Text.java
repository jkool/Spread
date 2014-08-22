/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread.impl.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import spread.Experiment;
import spread.ExperimentWriter;
import spread.Mosaic;
import spread.MosaicWriter;
import spread.Infestation;
import spread.Process;

import spread.impl.RasterMosaic;
import spread.impl.process.Process_Costing;
import spread.util.RasterWriter;
import spread.util.Stats;

/**
 * Used to write Experiment-level output to output files, including number of
 * infested cells, Kappa statistics and end-of-run Raster output.
 */

public class ExperimentWriter_Text implements ExperimentWriter {

	private Mosaic reference = null;
	private double[] distances;
	private double[] rates;
	private Number replicate = Double.NaN;
	private String outputFolder;
	private String outputFile;
	private String outputFrequency = "frequency.txt";
	private Map<String, BufferedWriter> bw_map = new TreeMap<String, BufferedWriter>();;
	private boolean writeTableHeader = true;
	private boolean writeRasterHeader = false;
	private Stats stats = new Stats();
	private int id = -1;
	private MosaicWriter mw = new MosaicWriter_Raster();
	private MosaicWriter ms = new MosaicWriter_Raster_Stage();
	private MosaicWriter mm = new MosaicWriter_Raster_WasMonitored();
	private boolean writeCoverMaps = true;
	private boolean writeFrequencyMap = true;
	private boolean writeStageMaps = true;
	private boolean writeMonitoredMaps = true;
	private Map<String, Map<Integer, Long>> tally = new TreeMap<String, Map<Integer, Long>>();
	private int n_expts = 0;
	private long nodata = -9999l;

	/**
	 * Close down the output resources.
	 */
	@Override
	public void close() {

		Iterator<String> it = bw_map.keySet().iterator();
		while (it.hasNext()) {
			try {
				String next = it.next();
				BufferedWriter bw = bw_map.get(next);
				bw.flush();
				bw.close();

				if (writeFrequencyMap) {
					writeFrequencyMap(outputFolder + "/" + next + "_"
							+ outputFrequency);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		bw_map.clear();
	}

	/**
	 * Sets up and opens the output files for writing
	 * 
	 * @throws IOException
	 */

	@Override
	public void open(Set<String> speciesList) throws IOException {
		Iterator<String> it = speciesList.iterator();

		while (it.hasNext()) {
			String species = it.next();
			bw_map.put(species, new BufferedWriter(new FileWriter(outputFolder
					+ "/" + species + "_" + outputFile)));

			if (writeTableHeader) {
				StringBuilder sb = new StringBuilder();
				sb.append("OID,Distance,Rate,Replicate,");

				// sb.append("N_infested,K_no,K_Allocation,K_quantity,K_histo,K_standard,Chance_agreement,Quantity_agreement,Allocation_agreement, Allocation_disagreement,Quantity_disagreement");
				sb.append("N_infested,Cost,Labour,K_no,K_Allocation,K_quantity,K_histo,K_standard,Chance_agreement,Quantity_agreement,Allocation_agreement, Allocation_disagreement,Quantity_disagreement");

				if (stats.isBinary()) {
					sb.append(",Pierce_Skill,Figure_of_merit");
				}

				sb.append("\n");
				bw_map.get(species).write(sb.toString());
			}
		}

		mw.setFolder(outputFolder);
		ms.setFolder(outputFolder);
		mm.setFolder(outputFolder);
	}

	/**
	 * Writes an experiment to output - i.e. a single line in the output table
	 * and/or raster output.
	 * 
	 * @param exp
	 *            - The experiment whose attributes are to be written to output.
	 */

	@Override
	public void write(Experiment exp) {

		Mosaic mosaic = exp.getMosaic();
		List<Process> plist = exp.getProcesses();
		int idx = -1;
		for (int i = 0; i < plist.size(); i++) {
			if (plist.get(i) instanceof Process_Costing) {
				idx = i;
				break;
			}
		}

		Process_Costing pcst = (Process_Costing) plist.get(idx);

		List<String> speciesList = mosaic.getSpeciesList();

		for (int i = 0; i < speciesList.size(); i++) {

			String species = speciesList.get(i);

			StringBuilder sb = new StringBuilder();
			sb.append(id + ",");
			sb.append(distances[i] + ",");
			sb.append(rates[i] + ",");
			sb.append(replicate.toString() + ",");

			if (reference == null) {
				throw new IllegalArgumentException(
						"Reference mosaic is null, but is required for generating statistics");
			}

			// calculate the confusion matrix
			int[][] cf = stats.makeConfusionMatrix(
					reference.getPatches(),
					mosaic.getPatches(), species);

			// generate statistics

			stats.pontiusStats(cf);
			sb.append(stats.getNInfested() + ",");
			sb.append(pcst.getCostTotal() + ",");
			sb.append(pcst.getLabourTotal() + ",");
			sb.append(stats.getKno() + ",");
			sb.append(stats.getKallocation() + ",");
			sb.append(stats.getKquantity() + ",");
			sb.append(stats.getKhisto() + ",");
			sb.append(stats.getKstandard() + ",");
			sb.append(stats.getChanceAgreement() + ",");
			sb.append(stats.getQuantityAgreement() + ",");
			sb.append(stats.getAllocationAgreement() + ",");
			sb.append(stats.getAllocationDisagreement() + ",");
			sb.append(stats.getQuantityDisagreement());

			// PierceSkill and FigureOfMerit are only applicable for binary
			// data.

			if (stats.isBinary()) {
				sb.append("," + stats.getPierceSkill() + ",");
				sb.append(stats.getFigureOfMerit());
			}

			sb.append("\n");

			try {
				bw_map.get(species).write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (writeCoverMaps) {
				if (writeRasterHeader) {
					MosaicWriter_Raster mwr = (MosaicWriter_Raster) mw;
					mwr.setWriteHeader(true);
				}
				mw.setName("cover_" + id + "_" + speciesList.get(i) + "_"
						+ distances[i] + "_" + rates[i] + "_" + replicate);
				mw.write(mosaic, species);
			}

			if (writeStageMaps) {
				if (writeRasterHeader) {
					MosaicWriter_Raster_Stage msr = (MosaicWriter_Raster_Stage) ms;
					msr.setWriteHeader(true);
				}
				ms.setName("stage_" + id + "_" + speciesList.get(i) + "_"
						+ distances[i] + "_" + rates[i] + "_" + replicate);
				ms.write(mosaic, species);
			}

			if (writeMonitoredMaps) {
				if (writeRasterHeader) {
					MosaicWriter_Raster mmr = (MosaicWriter_Raster) mm;
					mmr.setWriteHeader(true);
				}
				mm.setName("monitored_" + id + "_" + speciesList.get(i) + "_"
						+ distances[i] + "_" + rates[i] + "_" + replicate);
				mm.write(mosaic, species);
			}

			if (writeFrequencyMap) {
				tally.put(species, new TreeMap<Integer, Long>());
				Map<Integer, Infestation> occupancies = mosaic
						.getInfestations(species);
				for (Integer key : occupancies.keySet()) {
					if (occupancies.get(key).hasNoData()) {
						tally.get(species).put(key, nodata);
					} else {
						if (occupancies.get(key).isInfested()) {
							if (!tally.get(species).containsKey(key)) {
								tally.get(species).put(key, 1l);
							} else {
								tally.get(species).put(key,
										tally.get(species).get(key) + 1);
							}
						}
					}
				}
			}

		}
		n_expts++;
	}

	/**
	 * Sets whether the frequency map should be written.
	 * 
	 * @param writeFrequencyMap
	 *            - boolean indicating whether the frequency map should be
	 *            written.
	 */

	public void writeFrequencyMap(boolean writeFrequencyMap) {
		this.writeFrequencyMap = writeFrequencyMap;
	}

	/**
	 * Performs required actions to write the infestation frequency map to a
	 * raster file
	 * 
	 * @param outputPath
	 *            - the path location of the output file
	 */

	public void writeFrequencyMap(String outputPath) {
		RasterMosaic rm = (RasterMosaic) reference;
		RasterWriter rw = new RasterWriter();
		double[][] data = new double[rm.getNrows()][rm.getNcols()];
		List<String> speciesList = new ArrayList<String>(tally.keySet());
		for (int i = 0; i < speciesList.size(); i++) {
			for (int key : tally.get(speciesList.get(i)).keySet()) {
				int row = key / rm.getNcols();
				int col = key % rm.getNcols();
				if (tally.get(speciesList.get(i)).get(key) == nodata) {
					data[row][col] = nodata;
				} else {
					data[row][col] = (double) tally.get(speciesList.get(i))
							.get(key) / (double) n_expts;
				}
			}

			rw.setWriteHeader(writeRasterHeader);
			try {
				rw.writeRaster(outputFolder + "/" + speciesList.get(i) + "_"
						+ outputFrequency, data, rm.getLlx(), rm.getLly(),
						rm.getCellsize(), "-9999");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Getters and setters

	public String getOutputFile() {
		return outputFile;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setDistances(double[] distances) {
		this.distances = distances;
	}

	public void setID(Integer id) {
		this.id = id;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setRates(double[] rates) {
		this.rates = rates;
	}

	public void setReferenceMosaic(Mosaic reference) {
		this.reference = reference;
	}

	public void setReplicate(Number replicate) {
		this.replicate = replicate;
	}

	public void setWriteRasterHeader(boolean writeRasterHeader) {
		this.writeRasterHeader = writeRasterHeader;
	}

	public void setWriteTableHeader(boolean writeTableHeader) {
		this.writeTableHeader = writeTableHeader;
	}
}
