/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread.impl;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spread.Disperser;
import spread.Mosaic;
import spread.Infestation;
import spread.Patch;

import spread.util.ControlType;
import spread.util.Raster;
import spread.util.RasterReader;

import cern.jet.random.Uniform;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Performs operations on a collection of Patch objects, and saves information
 * relating to the collection as a whole. Operations contained within the class
 * are needed when dependencies arise among the Patch objects (as opposed to
 * process that affect them independently of one another).
 * 
 */

public class RasterMosaic implements Mosaic, Cloneable {

	private Raster ageMap;
	private Raster habitatMap;
	private Raster presenceMap;
	private Raster managementMap;
	private List<String> speciesList = new ArrayList<String>();
	private Map<String, Disperser> dispersers = new TreeMap<String, Disperser>();
	private Long NO_PRESENCE = 0l;
	private Long NULL_HABITAT = 0l;
	private Long NO_MANAGEMENT = 0l;
	private int nrows = 0;
	private int ncols = 0;
	private double cellsize = 0.0d;
	private double llx = 0.0d;
	private double lly = 0.0d;
	private Map<Integer, Patch> patches = new TreeMap<Integer, Patch>();

	public void addDisperser(String species, Disperser disperser) {
		dispersers.put(species, disperser);
	}

	/**
	 * Adds a species to the list of species
	 */

	@Override
	public void addSpecies(String species) {
		speciesList.add(species);
	}

	/**
	 * Used to construct the cells
	 */

	private void buildPatches() {
		// Loop across the raster

		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				int id = i * ncols + j;

				// Generate a new cell

				Patch patch = new Patch();
				patch.setID(id);

				patches.put(id, patch);
			}
		}
	}

	/**
	 * Checks that the dimensions of the provided raster is consistent with the
	 * number of rows and columns being used by the class.
	 * 
	 * @param raster
	 * @return
	 */

	private boolean checkDim(Raster raster) {
		return raster.getRows() == nrows && raster.getCols() == ncols;
	}

	/**
	 * Clears the Patch information and dimensions from the instance.
	 */

	@Override
	public void clear() {
		nrows = 0;
		ncols = 0;
		cellsize = 0.0d;
		llx = 0.0d;
		lly = 0.0d;
		patches = new TreeMap<Integer, Patch>();
	}

	/**
	 * Resets all Patches to being unvisited.
	 */

	@Override
	public void clearVisited() {
		for (Integer key : patches.keySet()) {
			patches.get(key).setVisited(false);
		}
	}

	/**
	 * Assigns whether a collection of infestations (within Patches) should be
	 * tagged as visited
	 * 
	 * @param patches
	 *            - The patches to be tagged
	 * @param species
	 *            - The species of interest
	 */

	public void clearVisited(Collection<Patch> patches, String species) {
		for (Patch p : patches) {
			p.getInfestation(species).setVisited(false);
		}
	}

	/**
	 * Resets a collection of infestations to being unvisited.
	 */

	@Override
	public void clearVisitedInfestations(Collection<Infestation> infestations) {
		for (Infestation o : infestations) {
			o.setVisited(false);
		}
	}

	/**
	 * Resets a collection of patches to being unvisited.
	 * 
	 * @param patches
	 */

	public void clearVisitedPatches(Collection<Patch> patches) {
		for (Patch p : patches) {
			p.setVisited(false);
		}
	}

	/**
	 * Resets a collection of infestations to being unvisited (accessed as
	 * patches with species key).
	 */

	@Override
	public void clearVisitedPatches(Collection<Patch> patches, String species) {
		for (Patch p : patches) {
			p.getInfestation(species).setVisited(false);
		}
	}

	/**
	 * Generates a copy of the class instance.
	 */

	@Override
	public RasterMosaic clone() {
		RasterMosaic rm = new RasterMosaic();
		rm.ageMap = ageMap;
		rm.habitatMap = habitatMap;
		rm.presenceMap = presenceMap;
		rm.NO_PRESENCE = NO_PRESENCE;
		rm.NULL_HABITAT = NULL_HABITAT;
		rm.llx = llx;
		rm.lly = lly;
		rm.cellsize = cellsize;
		rm.nrows = nrows;
		rm.ncols = ncols;

		Map<Integer, Patch> ccells = new TreeMap<Integer, Patch>();

		for (Integer c : patches.keySet()) {
			ccells.put(c, patches.get(c).clone());
		}

		List<String> cspecies = new ArrayList<String>();
		for (String s : speciesList) {
			cspecies.add(s);
		}

		Map<String, Disperser> cdisp = new TreeMap<String, Disperser>();
		for (String s : dispersers.keySet()) {
			cdisp.put(s, dispersers.get(s).clone());
		}

		rm.dispersers = cdisp;
		rm.speciesList = cspecies;
		rm.patches = ccells;

		return rm;
	}

	/**
	 * Fills a collection of patches associated with a given species.
	 */

	@Override
	public Set<Patch> fill(Collection<Patch> region, String species) {
		
		if(region.isEmpty()){return new TreeSet<Patch>();}
		
		List<Patch> plist = new ArrayList<Patch>(region);
		boolean condition = plist.get(0).isInfestedBy(species);

		Set<Patch> s = new TreeSet<Patch>();
		s.addAll(region);
		int[] bounds = getBounds(region);
		Set<Patch> block = getBlock(bounds);
		block.removeAll(region);
		PriorityQueue<Patch> pq = new PriorityQueue<Patch>(block);
		Set<Patch> newtiles = new TreeSet<Patch>();

		while (!pq.isEmpty()) {
			Patch seed = pq.poll();

			if (seed.hasNoData()) {
				continue;
			}

			Set<Patch> tile = getStrongRegion(seed, species, bounds);
			
			boolean anytouch = false;

			for (Patch p : tile) {
				
				int row = p.getID() / ncols;
				int col = p.getID() % ncols;

				// Do any patches touch the edge?

				if (row == bounds[0] || row == bounds[2] || col == bounds[1]
						|| col == bounds[3]) {
					anytouch = true;
					break;
				}
			}

			if (!anytouch) {
				newtiles.addAll(tile);
			}

			// regardless, clear the tile to avoid re-processing

			pq.removeAll(tile);

		}
		
		Set<Patch> del = new HashSet<Patch>();
		
		Iterator<Patch> it = newtiles.iterator();
		while(it.hasNext()){
			Patch k = it.next();
			if(k.isInfestedBy(species)==condition){
				del.add(k);
			}
		}
		newtiles.removeAll(del);
		s.addAll(newtiles);
		return s;
	}

	/**
	 * 
	 */

	@Override
	public Map<Integer, Infestation> getActiveInfestations(String species) {
		Map<Integer, Infestation> Infested = new TreeMap<Integer, Infestation>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if (!p.hasNoData() && p.getInfestation(species).isInfested()) {
				Infested.put(patch_key, p.getInfestation(species));
			}
		}
		return Infested;
	}

	/**
	 * Retrieves the area associated with a collection of patches.
	 */

	@Override
	public double getArea(Collection<Patch> patches) {
		return patches.size() * cellsize * cellsize;
	}

	public Set<Patch> getBlock(int[] bounds) {
		Set<Patch> s = new TreeSet<Patch>();
		for (int j = bounds[0]; j <= bounds[2]; j++) {
			for (int i = bounds[1]; i <= bounds[3]; i++) {
				s.add(getPatch(j * ncols + i));
			}
		}
		return s;
	}

	/**
	 * @param patches
	 *            - the input Collection of Patches
	 * @return the bounding coordinates for a collection of Patches.
	 */

	public int[] getBounds(Collection<Patch> patches) {
		if (patches.size() == 0) {
			return null;
		}
		int[] bnds = new int[4];
		bnds[0] = Integer.MAX_VALUE;
		bnds[1] = Integer.MAX_VALUE;
		bnds[2] = Integer.MIN_VALUE;
		bnds[3] = Integer.MIN_VALUE;
		for (Patch p : patches) {
			int row = p.getID() / ncols;
			int col = p.getID() % ncols;
			bnds[0] = Math.min(bnds[0], row);
			bnds[1] = Math.min(bnds[1], col);
			bnds[2] = Math.max(bnds[2], row);
			bnds[3] = Math.max(bnds[3], col);
		}
		return bnds;
	}

	/**
	 * @return - the cell size of the RasterMosaic.
	 */

	public double getCellsize() {
		return cellsize;
	}

	/**
	 * @return - the controlled cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getControlled(ControlType control) {
		Map<Integer, Patch> controlled = new TreeMap<Integer, Patch>();
		for (Integer key : patches.keySet()) {
			if (patches.get(key).hasControl(control)) {
				controlled.put(key, patches.get(key));
			}
		}
		return controlled;
	}

	/**
	 * @return - the controlled cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	public Map<Integer, Patch> getControlled(String species) {
		Map<Integer, Patch> controlled = new TreeMap<Integer, Patch>();
		for (Integer key : patches.keySet()) {
			if (patches.get(key).getInfestation(species).getControls().size() > 0) {
				controlled.put(key, patches.get(key));
			}
		}
		return controlled;
	}

	/**
	 * @return - the controlled cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getControlled(String species, ControlType control) {
		Map<Integer, Patch> controlled = new TreeMap<Integer, Patch>();
		for (Integer key : patches.keySet()) {
			Patch p = patches.get(key);
			if(p.isInfestedBy(species) && (p.hasControl(control))||p.hasControl(control, species)){
				controlled.put(key, p);
			}
		}
		return controlled;
	}

	@Override
	public Set<Patch> getFilledRegion(Patch patch, String species) {
		Set<Patch> region = getWeakRegion(patch, species);
		return fill(region, species);
	}

	public Set<Patch> getFrozen(Set<Patch> region, String species) {
		Set<Patch> frozen = new TreeSet<Patch>();
		for (Patch p : region) {
			if (p.isInfestedBy(species)
					&& p.getInfestation(species).isManagementFrozen()) {
				frozen.add(p);
			}
		}
		return frozen;
	}

	/**
	 * Retrieves a Map (as in Java Map) of patch ids and Infestations of a given
	 * species type.
	 */

	@Override
	public Map<Integer, Infestation> getInfestations(String species) {
		Map<Integer, Infestation> infestations = new TreeMap<Integer, Infestation>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if (!p.hasNoData() && p.isInfestedBy(species)) {
				infestations.put(patch_key, p.getInfestation(species));
			}
		}
		return infestations;
	}

	/**
	 * @return - the infested cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getInfestedPatches() {
		Map<Integer, Patch> infested = new TreeMap<Integer, Patch>();
		outer: for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			Iterator<String> it = p.getInfestation().keySet().iterator();
			while (it.hasNext()) {
				if (p.getInfestation(it.next()).isInfested()) {
					infested.put(patch_key, patches.get(patch_key));
					continue outer;
				}
			}
		}
		return infested;
	}

	/**
	 * @return - the infested cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	public Map<Integer, Patch> getInfestedPatches(String species) {
		Map<Integer, Patch> infested = new TreeMap<Integer, Patch>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if (p.isInfestedBy(species)
					&& p.getInfestation(species).isInfested()) {
				infested.put(patch_key, patches.get(patch_key));
			}
		}
		return infested;
	}

	/**
	 * @param c
	 *            - a Collection of Patches from which the keys/IDs will be
	 *            retrieved.
	 * @return the key values (IDs/index values) of the Patches.
	 */

	public Set<Integer> getKeys(Collection<Patch> c) {
		Set<Integer> s = new TreeSet<Integer>();
		for (Patch p : c) {
			s.add(p.getID());
		}
		return s;
	}

	/**
	 * Returns the lower left x coordinate of the raster.
	 * 
	 * @return - the lower left x-coordinate.
	 */

	public double getLlx() {
		return llx;
	}

	/**
	 * Returns the lower left y coordinate of the raster.
	 * 
	 * @return - the lower left y-coordinate.
	 */

	public double getLly() {
		return lly;
	}

	/**
	 * Retrieves the monitored cells from the RasterMosaic as a map of keys and
	 * Patches.
	 * 
	 * @return - the monitored cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	public Map<Integer, Patch> getMonitored() {
		Map<Integer, Patch> monitored = new TreeMap<Integer, Patch>();
		for (Integer key : patches.keySet()) {
			if (patches.get(key).isMonitored()) {
				monitored.put(key, patches.get(key));
			}
		}
		return monitored;
	}

	/**
	 * @return the number of columns in the raster
	 */

	public int getNcols() {
		return ncols;
	}

	/**
	 * @return the Patches and associated indices that are marked as having
	 *         NoData.
	 */

	public Map<Integer, Patch> getNoData() {
		Map<Integer, Patch> nodata = new TreeMap<Integer, Patch>();
		for (Integer key : patches.keySet()) {
			if (patches.get(key).hasNoData()) {
				nodata.put(key, patches.get(key));
			}
		}
		return nodata;
	}

	/**
	 * @return - the number of rows in the raster
	 */

	public int getNrows() {
		return nrows;
	}

	/**
	 * @return - the number of infested patches in the RasterMosaic for a given
	 *         species.
	 */

	@Override
	public int getNumberActiveInfestations(String species) {
		return getActiveInfestations(species).size();
	}

	/**
	 * @param species
	 *            - the species of interest.
	 * @return the number of patches under management control for a given
	 *         species.
	 */

	public int getNumberControlled(String species) {
		return getControlled(species).size();
	}

	/**
	 * @return - the number of infested patches in the RasterMosaic for a given
	 *         species.
	 */

	@Override
	public int getNumberInfestations(String species) {
		return getInfestations(species).size();
	}

	/**
	 * @return - the total number of infested patches in the RasterMosaic.
	 */

	@Override
	public int getNumberInfestedPatches() {
		return getInfestedPatches().size();
	}

	/**
	 * @return Retrieves the total number of Patches being monitored in the
	 *         Mosaic.
	 */

	public int getNumberMonitored() {
		return getMonitored().size();
	}

	/**
	 * Returns the number of cells marked as having NoData
	 */

	@Override
	public int getNumberNoData() {
		return getNoData().size();
	}

	/**
	 * @return - the number of infested and undetected patches in the
	 *         RasterMosaic.
	 */

	@Override
	public int getNumberUndetected() {
		return getUndetected().size();
	}

	/**
	 * @return - the number of infested but undetected patches in the
	 *         RasterMosaic for a given species.
	 */

	@Override
	public int getNumberUndetected(String species) {
		return getUndetected(species).size();
	}

	/**
	 * @return - the number of uninfested patches in the RasterMosaic.
	 */

	@Override
	public int getNumberUninfested() {
		return getUninfested().size();
	}

	/**
	 * @return - the number of uninfested patches in the RasterMosaic for a
	 *         given species.
	 */

	@Override
	public int getNumberUninfested(String species) {
		return getUninfested(species).size();
	}

	/**
	 * Retrieves a single Patch object using its key.
	 * 
	 * @param key
	 *            - the ID of the Patch object
	 * @return - the Patch object corresponding to the key provided
	 */

	@Override
	public Patch getPatch(int key) {
		return patches.get(key);
	}

	/**
	 * Retrieves a map of cell objects. Integer value corresponds to the unique
	 * index of the cell, defined as row*number of columns + column
	 */

	@Override
	public Map<Integer, Patch> getPatches() {
		return patches;
	}

	/**
	 * @param indices
	 *            - the index values of the Patches to be retrieved.
	 * @return a Map (as in Java Map) of patch IDs and associated Patches based
	 *         on a Collection of patch id values.
	 * 
	 */

	public Map<Integer, Patch> getPatches(Collection<Integer> indices) {
		Map<Integer, Patch> map = new TreeMap<Integer, Patch>();
		for (int i : indices) {
			Patch p = patches.get(i);
			map.put(p.getID(), p);
		}
		return map;
	}

	/**
	 * @param patches
	 *            - the Collection of patches.
	 * @return the total area of a Collection of patches.
	 */

	public double getRegionArea(Collection<Patch> patches) {
		return patches.size() * cellsize * cellsize;
	}

	/**
	 * Retrieves the List of species that can potentially exist within the
	 * Mosaic.
	 */

	@Override
	public List<String> getSpeciesList() {
		return speciesList;
	}

	/**
	 * @param p
	 *            - The patch to be examined for linkages.
	 * @return the set of patches that are strongly connected to the provided
	 *         patch i.e. sharing an edge (in cardinal directions).
	 */

	public Set<Patch> getStrongAdjacent(Patch p) {
		return getStrongAdjacent(p, new int[] { 0, 0, nrows - 1, ncols - 1 });
	}

	/**
	 * @param p
	 *            - The patch to be examined for linkages.
	 * @param bnds
	 *            - the bounding coordinates (minrow,mincol,maxrow,maxcol)
	 * @return the set of patches that are strongly connected to the provided
	 *         patch i.e. sharing an edge (in cardinal directions) within a
	 *         bounded area.
	 */

	public Set<Patch> getStrongAdjacent(Patch p, int[] bnds) {
		Set<Patch> list = new TreeSet<Patch>();
		int id = p.getID();
		int column = id % ncols;
		int row = id / ncols;

		// Top
		if (row - 1 >= bnds[0]) {
			list.add(getPatch(id - ncols));
		}

		// Left
		if (column - 1 >= bnds[1]) {
			list.add(getPatch(id - 1));
		}

		// Bottom
		if (row + 1 <= bnds[2]) {
			list.add(getPatch(id + ncols));
		}

		// Right
		if (column + 1 <= bnds[3]) {
			list.add(getPatch(id + 1));
		}

		return list;
	}

	/**
	 * Retrieves an internal core of values that are strongly connected to one
	 * another - i.e. sharing an edge (in cardinal directions).
	 */

	@Override
	public Set<Patch> getStrongContainmentCore(Collection<Patch> patches,
			double bufferSize) {
		return nibbleStrong(patches, (int) (bufferSize / cellsize));
	}
	
	/**
	 * Retrieves an internal core of values that are strongly connected to one
	 * another - i.e. sharing an edge (in cardinal directions).
	 */

	@Override
	public Set<Patch> getStrongCore(Collection<Patch> patches, String species,
			double bufferSize) {
		return nibbleStrong(patches, species, (int) (bufferSize / cellsize));
	}

	/**
	 * Retrieves a region (Patch collection) of strongly connected Patches
	 * associated with a given Patch.
	 */

	@Override
	public Set<Patch> getStrongRegion(Patch p, String species) {
		return getStrongRegion(p, species, new int[] { 0, 0, nrows - 1,
				ncols - 1 });
	}
	
	/**
	 * Retrieves a region (Patch collection) of strongly connected Patches
	 * associated with a given Patch.
	 */

	@Override
	public Set<Patch> getStrongContainment(Patch p) {
		return getStrongContainment(p, new int[] { 0, 0, nrows - 1,
				ncols - 1 });
	}

	/**
	 * Retrieves a region (Patch collection) of strongly connected Patches
	 * associated with a given Patch.
	 */

	@Override
	public Set<Patch> getStrongRegion(Patch p, Set<String> species,
			boolean condition) {
		return getStrongRegion(p, species, condition, new int[] { 0, 0,
				nrows - 1, ncols - 1 });
	}

	/**
	 * @param p
	 *            - the starting Patch.
	 * @param speciesSet
	 *            - the Set of species of interest.
	 * @param condition
	 *            - indicates whether the region should consist of infested or
	 *            non-infested Patches.
	 * @param bnds
	 *            - a box that bounds the search area.
	 * @return the region (Patch collection) of strongly connected Patches.
	 *         associated with a given Patch, bounded by an extent box.
	 */

	public Set<Patch> getStrongRegion(Patch p, Set<String> speciesSet,
			boolean condition, int[] bnds) {
		Set<Patch> s = new TreeSet<Patch>();

		if (p.isInfested() == condition) {
			s.add(p);
		}

		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getStrongAdjacent(p, bnds));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();

			if (pc.hasNoData()) {
				continue;
			}

			clearSet.add(pc);
			outer: for (String species : speciesSet) {
				if (!pc.isVisited() && pc.isInfestedBy(species) == condition
						&& !pc.hasNoData()) {
					s.add(pc);
					pq.addAll(getStrongAdjacent(pc, bnds));
					break outer;
				}
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}
	
	/**
	 * @param p
	 *            - the starting Patch.
	 * @param species
	 *            - the species of interest.
	 * @param bnds
	 *            - a box that bounds the search area.
	 * @return the region (Patch collection) of strongly connected Patches.
	 *         associated with a given Patch, bounded by an extent box.
	 */

	public Set<Patch> getStrongContainment(Patch p, int[] bnds) {
		Set<Patch> s = new TreeSet<Patch>();

		s.add(p);
		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getStrongAdjacent(p, bnds));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();

			if (pc.hasNoData()) {
				continue;
			}

			clearSet.add(pc);
			if (!pc.isVisited()
					&& ((pc.hasControl(ControlType.CONTAINMENT)||pc.hasControl(ControlType.CONTAINMENT_CORE)))
					&& !pc.hasNoData()) {
				s.add(pc);
				pq.addAll(getStrongAdjacent(pc, bnds));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}

	/**
	 * @param p
	 *            - the starting Patch.
	 * @param species
	 *            - the species of interest.
	 * @param bnds
	 *            - a box that bounds the search area.
	 * @return the region (Patch collection) of strongly connected Patches.
	 *         associated with a given Patch, bounded by an extent box.
	 */

	public Set<Patch> getStrongRegion(Patch p, String species, int[] bnds) {
		Set<Patch> s = new TreeSet<Patch>();

		s.add(p);
		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getStrongAdjacent(p, bnds));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();

			if (pc.hasNoData()) {
				continue;
			}

			clearSet.add(pc);
			if (!pc.isVisited()
					&& pc.isInfestedBy(species) == p.isInfestedBy(species)
					&& !pc.hasNoData()) {
				s.add(pc);
				pq.addAll(getStrongAdjacent(pc, bnds));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}

	/**
	 * Retrieves infested but undetected cells (any Infested) from the
	 * RasterMosaic as a map of keys and Patches.
	 * 
	 * @return - the undetected cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getUndetected() {
		Map<Integer, Patch> undetected = new TreeMap<Integer, Patch>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if (p.isInfested() && !p.isMonitored()) {
				undetected.put(patch_key, patches.get(patch_key));
			}
		}
		return undetected;
	}

	/**
	 * Retrieves infested but undetected cells (occupancy type specified by key)
	 * from the RasterMosaic as a map of keys and Patches.
	 * 
	 * @return - the undetected cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getUndetected(String species) {
		Map<Integer, Patch> undetected = new TreeMap<Integer, Patch>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);

			if (p.isInfestedBy(species) && !p.isMonitored()) {
				undetected.put(patch_key, patches.get(patch_key));
			}
		}
		return undetected;
	}

	/**
	 * Retrieves uninfested cells (any Infested) from the RasterMosaic as a map
	 * of keys and Patches.
	 * 
	 * @return - the undetected cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getUninfested() {
		Map<Integer, Patch> uninfested = new TreeMap<Integer, Patch>();
		outer: for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			Iterator<String> it = p.getInfestation().keySet().iterator();
			while (it.hasNext()) {
				if (p.isInfestedBy(it.next())) {
					continue outer;
				}
			}
			uninfested.put(patch_key, p);
		}

		return uninfested;
	}

	/**
	 * Retrieves infested but undetected cells (occupancy type specified by key)
	 * from the RasterMosaic as a map of keys and Patches.
	 * 
	 * @return - the undetected cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getUninfested(String species) {
		Map<Integer, Patch> uninfested = new TreeMap<Integer, Patch>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if (!p.isInfestedBy(species)) {
				uninfested.put(patch_key, patches.get(patch_key));
			}
		}
		return uninfested;
	}

	/**
	 * @param p
	 *            - the starting Patch
	 * @return cells adjacent to the designated patch including diagonals
	 */

	public Set<Patch> getWeakAdjacent(Patch p) {
		return getWeakAdjacent(p, new int[] { 0, 0, nrows - 1, ncols - 1 });
	}

	/**
	 * @param p
	 *            - the starting Patch
	 * @return cells adjacent to the designated patch including diagonals
	 */

	public Set<Patch> getWeakAdjacent(Patch p, int[] bnds) {
		Set<Patch> set = new TreeSet<Patch>();
		int id = p.getID();
		int column = id % ncols;
		int row = id / ncols;

		// Upper left
		if (row - 1 >= bnds[0] && column - 1 >= bnds[1]) {
			set.add(getPatch(id - ncols - 1));
		}

		// Top
		if (row - 1 >= bnds[0]) {
			set.add(getPatch(id - ncols));
		}

		// Top right
		if (row - 1 >= bnds[0] && column + 1 <= bnds[3]) {
			set.add(getPatch(id - ncols + 1));
		}

		// Left
		if (column - 1 >= bnds[1]) {
			set.add(getPatch(id - 1));
		}

		// Right
		if (column + 1 <= bnds[3]) {
			set.add(getPatch(id + 1));
		}

		// Bottom left
		if (row + 1 <= bnds[2] && column - 1 >= bnds[1]) {
			set.add(getPatch(id + ncols - 1));
		}

		// Bottom
		if (row + 1 <= bnds[2]) {
			set.add(getPatch(id + ncols));
		}

		// Upper left
		if (row + 1 <= bnds[2] && column + 1 <= bnds[3]) {
			set.add(getPatch(id + ncols + 1));
		}
		return set;
	}

	/**
	 * Retrieves an internal core of values that are weakly connected to one
	 * another - i.e. sharing a point intersection (diagonal links allowed).
	 */

	@Override
	public Set<Patch> getWeakCore(Collection<Patch> patches, String species,
			double bufferSize) {
		return nibbleWeak(patches, species, (int) (bufferSize / cellsize));
	}

	/**
	 * Retrieves a region (Patch collection) of weakly connected Patches
	 * associated with a given Patch.
	 */

	@Override
	public Set<Patch> getWeakContainment(Patch p) {
		return getWeakContainment(p,
				new int[] { 0, 0, nrows - 1, ncols - 1 });
	}
	
	/**
	 * Retrieves a region (Patch collection) of weakly connected Patches
	 * associated with a given Patch.
	 */

	@Override
	public Set<Patch> getWeakRegion(Patch p, String species) {
		return getWeakRegion(p, species,
				new int[] { 0, 0, nrows - 1, ncols - 1 });
	}

	/**
	 * Retrieves a region (Patch collection) of weakly connected Patches
	 * associated with a given Patch.
	 */

	@Override
	public Set<Patch> getWeakRegion(Patch p, Set<String> species,
			boolean condition) {
		return getWeakRegion(p, species, condition, new int[] { 0, 0,
				nrows - 1, ncols - 1 });
	}

	/**
	 * @param p
	 *            - the starting Patch
	 * @param speciesSet
	 *            - the species of interest
	 * @param bnds
	 *            -the extent box (mincol,minrow,maxcol,maxrow)
	 * @return a region (Patch collection) of strongly connected Patches
	 *         associated with a given Patch, bounded by an extent box.
	 */

	public Set<Patch> getWeakRegion(Patch p, Set<String> speciesSet,
			boolean condition, int[] bnds) {
		Set<Patch> s = new TreeSet<Patch>();

		s.add(p);
		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getWeakAdjacent(p, bnds));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();

			if (pc.hasNoData()) {
				continue;
			}

			clearSet.add(pc);
			outer: for (String species : speciesSet) {
				if (!pc.isVisited() && pc.isInfestedBy(species) == condition
						&& !pc.hasNoData()) {
					s.add(pc);
					pq.addAll(getWeakAdjacent(pc, bnds));
					break outer;
				}
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}
	
	/**
	 * @param p
	 *            - the starting Patch
	 * @param species
	 *            - the species of interest
	 * @param bnds
	 *            -the extent box (mincol,minrow,maxcol,maxrow)
	 * @return a region (Patch collection) of strongly connected Patches
	 *         associated with a given Patch, bounded by an extent box.
	 */

	public Set<Patch> getWeakContainment(Patch p, int[] bnds) {
		Set<Patch> s = new TreeSet<Patch>();

		s.add(p);
		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getWeakAdjacent(p, bnds));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();

			if (pc.hasNoData()) {
				continue;
			}

			clearSet.add(pc);
			if (!pc.isVisited()
					&& ((pc.hasControl(ControlType.CONTAINMENT)||pc.hasControl(ControlType.CONTAINMENT_CORE)))
					&& !pc.hasNoData()) {
				s.add(pc);
				pq.addAll(getWeakAdjacent(pc, bnds));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}

	/**
	 * @param p
	 *            - the starting Patch
	 * @param species
	 *            - the species of interest
	 * @param bnds
	 *            -the extent box (mincol,minrow,maxcol,maxrow)
	 * @return a region (Patch collection) of strongly connected Patches
	 *         associated with a given Patch, bounded by an extent box.
	 */

	public Set<Patch> getWeakRegion(Patch p, String species, int[] bnds) {
		Set<Patch> s = new TreeSet<Patch>();

		s.add(p);
		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getWeakAdjacent(p, bnds));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();

			if (pc.hasNoData()) {
				continue;
			}

			clearSet.add(pc);
			if (!pc.isVisited()
					&& pc.isInfestedBy(species) == p.isInfestedBy(species)
					&& !pc.hasNoData()) {
				s.add(pc);
				pq.addAll(getWeakAdjacent(pc, bnds));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}

	/**
	 * Infests the mosaic according to a List of coordinate values. Note(!)
	 * infestation *must* happen at the Mosaic level due to dependencies on
	 * implementation-specific variables such as raster parameters - e.g. cell
	 * size and index-based searching.
	 */

	@Override
	public void infest(String species, List<Coordinate> propagules) {
		for (Coordinate c : propagules) {
			int key_x = (int) ((c.x - llx) / cellsize);
			int key_y = nrows - (1 + (int) ((c.y - lly) / cellsize));

			// Handle out of bounds propagules
			if (key_x < 0 || key_x >= ncols || key_y < 0 || key_y >= nrows) {
				continue;
			}

			// Retrieve cells by index
			int key = key_y * ncols + key_x;

			// If the cells are viable habitat, and it is not already infested,
			// then set as infested and start the counter.

			Patch patch = patches.get(key);

			// If the patch has No Data, continue

			if (patch.hasNoData()) {
				continue;
			}

			// If the patch is under control, continue (cannot receive
			// propagules). We handle here as opposed to during the
			// dispersal process because otherwise we'd need to assume
			// a raster framework there to do the intersections rather
			// than working with dispersal coordinates.

			if (patch.hasControl(ControlType.GROUND_CONTROL, species)
					|| patch.hasControl(ControlType.CONTAINMENT)
					|| patch.hasControl(ControlType.CONTAINMENT_CORE_CONTROL,
							species)) {
				continue;
			}

			if (!patch.isInfestedBy(species)
					&& Uniform.staticNextDouble() < patch
							.getHabitatSuitability(species)) {
				patch.addInfestation(species);
				Disperser d = dispersers.get(species);
				d.setPosition(c);
				patch.getInfestation(species).setDisperser(d);
				patch.getInfestation(species).setInfested(true);
				patch.getInfestation(species).setAgeOfInfestation(0);
			}
		}
	}

	/**
	 * Checks whether a String is a number
	 * 
	 * @param str
	 * @return
	 */

	private boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

	public Set<Patch> nibbleStrong(Collection<Patch> region) {
		Set<Patch> output = new TreeSet<Patch>(region);
		Iterator<Patch> it = region.iterator();
		outer: while (it.hasNext()) {
			Patch p = it.next();
			Set<Patch> adjacent = getStrongAdjacent(p);
			for (Patch inner : adjacent) {
				if (inner.hasNoData()) {
					continue;
				}
				if (!region.containsAll(adjacent)) {
					output.remove(p);
					continue outer;
				}
			}
		}
		return output;
	}
	
	public Set<Patch> nibbleStrong(Collection<Patch> region, String species) {
		Set<Patch> output = new TreeSet<Patch>(region);
		Iterator<Patch> it = region.iterator();
		outer: while (it.hasNext()) {
			Patch p = it.next();
			Set<Patch> adjacent = getStrongAdjacent(p);
			for (Patch inner : adjacent) {
				if (inner.hasNoData()) {
					continue;
				}
				if (!region.containsAll(adjacent)) {
					output.remove(p);
					continue outer;
				}
			}
		}
		return output;
	}
	
	/**
	 * Eliminates strongly-connected (sharing an edge in cardinal directions)
	 * exterior Patches from a region (Collection of Patches)
	 * 
	 * @param region
	 *            - the region to be nibbled
	 * @param species
	 *            - the species of interest
	 * @param depth
	 *            - the depth to which exterior cells should be removed.
	 * @return the Patches remaining after exterior Patches are removed to the
	 *         specified depth.
	 */

	public Set<Patch> nibbleStrong(Collection<Patch> region, int depth) {

		if (depth <= 0) {
			return new TreeSet<Patch>(region);
		}
		if (depth == 1) {
			return nibbleStrong(region);
		}

		// Set<Patch> full = new TreeSet<Patch>(region);
		Set<Patch> output = nibbleStrong(region);

		for (int i = 1; i < depth; i++) {
			if (output.isEmpty()) {
				return output;
			}

			output = nibbleStrong(output);
		}

		return output;
	}


	/**
	 * Eliminates strongly-connected (sharing an edge in cardinal directions)
	 * exterior Patches from a region (Collection of Patches)
	 * 
	 * @param region
	 *            - the region to be nibbled
	 * @param species
	 *            - the species of interest
	 * @param depth
	 *            - the depth to which exterior cells should be removed.
	 * @return the Patches remaining after exterior Patches are removed to the
	 *         specified depth.
	 */

	public Set<Patch> nibbleStrong(Collection<Patch> region, String species,
			int depth) {

		if (depth <= 0) {
			return new TreeSet<Patch>(region);
		}
		if (depth == 1) {
			return nibbleStrong(region, species);
		}

		// Set<Patch> full = new TreeSet<Patch>(region);
		Set<Patch> output = nibbleStrong(region, species);

		for (int i = 1; i < depth; i++) {
			if (output.isEmpty()) {
				return output;
			}

			output = nibbleStrong(output, species);
		}

		return output;
	}

	/**
	 * Eliminates strongly-connected (sharing an edge in cardinal directions)
	 * exterior cells from a region (Collection of Patches)
	 * 
	 * @param region
	 *            - the region to be nibbled.
	 * @param edgeCells
	 *            - a Collection of cells containing cells on the edge.
	 * @param species
	 *            - the species of interest.
	 * @return the Patches remaining after exterior Patches are removed to the
	 *         specified depth.
	 */

	public Set<Patch> nibbleStrong(Set<Patch> region, Set<Patch> edgeCells,
			String species) {
		Set<Patch> output = new TreeSet<Patch>(region);
		Iterator<Patch> it = region.iterator();
		outer: while (it.hasNext()) {
			Patch p = it.next();
			Set<Patch> adjacent = getStrongAdjacent(p);
			for (Patch inner : adjacent) {
				if (inner.hasNoData()) {
					continue;
				}
				if (edgeCells.contains(inner)) {
					output.remove(p);
					continue outer;
				}
			}
		}
		return output;
	}

	/**
	 * Eliminates weakly-connected (point-touching - diagonal connection)
	 * exterior cells from a region (Collection of Patches)
	 * 
	 * @param region
	 *            - the region to be nibbled.
	 * @param species
	 *            - the species of interest.
	 * @return the Patches remaining after exterior Patches are removed.
	 */

	public Set<Patch> nibbleWeak(Collection<Patch> region, String species) {
		Set<Patch> output = new TreeSet<Patch>(region);
		Iterator<Patch> it = region.iterator();
		outer: while (it.hasNext()) {
			Patch p = it.next();
			Set<Patch> adjacent = getWeakAdjacent(p);
			for (Patch inner : adjacent) {
				if (inner.hasNoData()) {
					continue;
				}
				if (p.getInfestation(species).isInfested() != inner
						.getInfestation(species).isInfested()) {
					output.remove(p);
					continue outer;
				}
			}
		}
		return output;
	}

	/**
	 * Eliminates weakly-connected (point-touching - diagonal connection)
	 * exterior cells from a region (Collection of Patches)
	 * 
	 * @param region
	 *            - the region to be nibbled.
	 * @param species
	 *            - the species of interest.
	 * @return the Patches remaining after exterior Patches are removed to the
	 *         specified depth.
	 */

	public Set<Patch> nibbleWeak(Collection<Patch> region, String species,
			int depth) {
		Set<Patch> output = new TreeSet<Patch>(region);
		for (int i = 0; i < depth; i++) {
			if (output.isEmpty()) {
				return output;
			}
			output = nibbleWeak(output, species);
		}
		return output;
	}

	/**
	 * Removes a ControlType associated with a given species from a Collection
	 * of Patches.
	 */

	@Override
	public void removeControl(Collection<Patch> patches, ControlType control) {
		for (Patch p : patches) {
			p.removeControl(control);
		}
	}

	/**
	 * Removes a ControlType associated with a given species from a Collection
	 * of Patches.
	 */

	@Override
	public void removeControl(Collection<Patch> patches, ControlType control,
			String species) {
		for (Patch p : patches) {
			if (p.isInfestedBy(species)) {
				p.getInfestation(species).removeControl(control);
			}
		}
	}

	/**
	 * @param p
	 *            - the starting Patch
	 * @param species
	 *            - the species of interest.
	 * @return a Set of Patches infested by the given species type linked to the
	 *         provided Patch.
	 */

	public Set<Patch> searchInfestation(Patch p, String species) {
		Set<Patch> s = new TreeSet<Patch>();

		if (p.hasNoData()) {
			return null;
		}

		if (!p.isInfestedBy(species)) {
			return s;
		}
		s.add(p);
		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getWeakAdjacent(p));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();
			clearSet.add(pc);
			if (!pc.isVisited() && !pc.hasNoData() && pc.isInfestedBy(species)) {
				s.add(pc);
				pq.addAll(getWeakAdjacent(pc));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}

	/**
	 * Sets the map paths indicating the ages of infestation associated with
	 * given species.
	 * 
	 * @param species
	 * @param ageMapPath
	 * @throws IOException
	 */

	public void setAgeMap(Collection<String> species, String ageMapPath)
			throws IOException {
		Iterator<String> it = species.iterator();
		while (it.hasNext()) {
			setAgeMap(it.next(), ageMapPath);
		}
	}

	/**
	 * Sets the age information using a path to a ESRI ASCII raster object
	 */

	@Override
	public void setAgeMap(String ageMapPath, String species) throws IOException {

		if (isNumeric(ageMapPath)) {
			long num = (long) Double.parseDouble(ageMapPath);
			for (Integer key : patches.keySet()) {
				if (patches.get(key).getInfestation(species).isInfested()) {
					patches.get(key).getInfestation(species)
							.setAgeOfInfestation(num);
				}
			}
		}

		Raster tmpAgeMap = RasterReader.readRaster(ageMapPath);

		// If the cell list is empty, use the raster as a template

		if (patches.isEmpty()) {
			setup(tmpAgeMap);
		}

		// Check that the dimensions are consistent. If not, throw an error.

		if (!checkDim(tmpAgeMap)) {
			throw new IllegalArgumentException(
					"Mosaic has been populated, but dimensions of "
							+ ageMapPath + "(" + tmpAgeMap.getRows() + ","
							+ tmpAgeMap.getCols() + ") are inconsistent ("
							+ nrows + "," + ncols + ").");
		}

		ageMap = tmpAgeMap;

		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				Patch p = patches.get(i * ncols + j);

				if (Double.isNaN(ageMap.getValue(i, j))
						|| (habitatMap != null && Double.isNaN(habitatMap
								.getValue(i, j)))
						|| (presenceMap != null && Double.isNaN(presenceMap
								.getValue(i, j)))) {
					p.setNoData(true);
				}

				if (p.isInfestedBy(species)) {
					p.getInfestation(species).setAgeOfInfestation(
							(long) ageMap.getValue(i, j));
				}
			}
		}
	}

	/**
	 * Set the cell size of the mosaic.
	 * 
	 * @param cellsize
	 */

	public void setCellsize(double cellsize) {
		this.cellsize = cellsize;
	}

	/**
	 * Applies a ControlType to species occupying a Collection of Patches.
	 */

	@Override
	public void setControl(Collection<Patch> patches, ControlType control,
			String species) {
		for (Patch p : patches) {
			if (p.isInfestedBy(species)) {
				p.getInfestation(species).addControl(control);
			}
		}
	}

	/**
	 * Applies a ControlType to species occupying a Collection of Patches.
	 */

	@Override
	public void setControl(Collection<Patch> patches, ControlType control) {
		for (Patch p : patches) {

			p.addControl(control);
		}
	}

	/**
	 * Sets a copy of the provided Disperser to all patches in the RasterMosaic.
	 * We make multiple clones otherwise source positioning would have to be
	 * handled at the level of the Patch.
	 * 
	 * @param d
	 *            - the Disperser object to be used.
	 */

	@Override
	public void setDisperser(String species, Disperser d) {

		dispersers.put(species, d);

		for (Integer key : patches.keySet()) {

			// We clone because Dispersers must be individual copies because
			// each has a unique position. As specified elsewhere, Dispersers
			// must have a unique position since cells could potentially contain
			// many Dispersers at different positions within the cell.

			Disperser dc = d.clone();

			// Calculate the centroid of the cell. Since the cells are raster
			// cells, this information can be built from their key.

			setDisperser(species, dc, key);
		}
	}

	/**
	 * Sets the provided Disperser to a single Patch in the RasterMosaic based
	 * on its key. Note: the Disperser is *not* cloned. Instead, the reference
	 * is passed.
	 * 
	 * @param d
	 *            - the Disperser object to be used.
	 * @param key
	 *            - the key value identifying the Patch to which the Disperser
	 *            should be applied.
	 */

	@Override
	public void setDisperser(String species, Disperser d, Integer key) {

		if (patches.get(key).hasNoData()) {
			return;
		}

		double yi = Math.floor(key / ncols);

		double y = cellsize * (nrows - yi - 1) + lly + (cellsize / 2);
		double x = cellsize * (key % ncols) + llx + (cellsize / 2);
		d.setPosition(new Coordinate(x, y));

		if (patches.get(key).isInfestedBy(species)) {
			patches.get(key).getInfestation(species).setDisperser(d);
		}
	}

	/**
	 * Sets the habitat information using a path to a ESRI ASCII raster object
	 */

	@Override
	public void setHabitatMap(String habitatMapPath, String species)
			throws IOException {

		// Convenience option for setting the value of all locations as true

		if (habitatMapPath.equalsIgnoreCase("ALL")) {
			for (Integer key : patches.keySet()) {
				patches.get(key).setHabitatSuitability(species, 1d);
			}
			return;
		}

		// Convenience option for setting the value of all locations as false

		if (habitatMapPath.equalsIgnoreCase("NONE")) {
			for (Integer key : patches.keySet()) {
				patches.get(key).setHabitatSuitability(species, 0d);
			}
			return;
		}

		Raster tmpHabitatMap = RasterReader.readRaster(habitatMapPath);

		// If the cell list is empty, use the raster as a template

		if (patches.isEmpty()) {
			setup(tmpHabitatMap);
		}

		// Check that the dimensions are consistent. If not, throw an error.

		if (!checkDim(tmpHabitatMap)) {
			throw new IllegalArgumentException(
					"Mosaic has been populated, but dimensions of "
							+ habitatMapPath + "(" + tmpHabitatMap.getRows()
							+ "," + tmpHabitatMap.getCols()
							+ ") are inconsistent (" + nrows + "," + ncols
							+ ").");
		}
		habitatMap = tmpHabitatMap;

		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				Patch p = patches.get(i * ncols + j);
				if (Double.isNaN(habitatMap.getValue(i, j))
						|| Double.isNaN(ageMap.getValue(i, j))
						|| Double.isNaN(presenceMap.getValue(i, j))) {
					p.setNoData(true);
				} else {
					p.setHabitatSuitability(species, habitatMap.getValue(i, j));
				}
			}
		}
	}

	/**
	 * Explicitly sets the lower left x-coordinate of the RasterMosaic
	 * 
	 * @param llx
	 *            - lower left x-coordinate
	 */

	public void setLlx(double llx) {
		this.llx = llx;
	}

	/**
	 * Explicitly sets the lower left y-coordinate of the RasterMosaic
	 * 
	 * @param lly
	 *            - lower left y-coordinate
	 */

	public void setLly(double lly) {
		this.lly = lly;
	}

	/**
	 * Sets the map paths indicating the management types associated with given
	 * species.
	 * 
	 * @param species
	 * @param managementMapPath
	 * @throws IOException
	 */

	@Override
	public void setManagementMap(String managementMapPath, String species)
			throws IOException {

		// Convenience option for setting the value of all locations as true

		if (managementMapPath.equalsIgnoreCase("ALL")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getInfestation(species)
						.addControl(ControlType.GROUND_CONTROL);
				patches.get(key).getInfestation(species)
						.addControl(ControlType.CONTAINMENT);
				patches.get(key).getInfestation(species)
						.addControl(ControlType.CONTAINMENT_CORE);
				patches.get(key).setMonitored(true);
				patches.get(key).getInfestation(species).freezeManagement(true);
			}
			return;
		}

		if (managementMapPath.equalsIgnoreCase("GROUND")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getInfestation(species)
						.addControl(ControlType.GROUND_CONTROL);
				patches.get(key).setMonitored(true);
				patches.get(key).getInfestation(species).freezeManagement(true);
			}
			return;
		}

		if (managementMapPath.equalsIgnoreCase("CONTAINMENT")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getInfestation(species)
						.addControl(ControlType.CONTAINMENT);
				patches.get(key).setMonitored(true);
			}
			return;
		}

		if (managementMapPath.equalsIgnoreCase("CORE")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getInfestation(species)
						.addControl(ControlType.CONTAINMENT_CORE);
				patches.get(key).setMonitored(true);
			}
			return;
		}
		// Convenience option for setting the value of all locations as false

		if (managementMapPath.equalsIgnoreCase("NONE")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				if (patches.get(key).isInfestedBy(species)) {
					patches.get(key).getInfestation(species).clearControls();
				}
			}
			return;
		}

		Raster tmpManagementMap = RasterReader.readRaster(managementMapPath);

		// If the cell list is empty, use the raster as a template

		if (patches.isEmpty()) {
			setup(tmpManagementMap);
		}

		// Check that the dimensions are consistent. If not, throw an error.

		if (!checkDim(tmpManagementMap)) {
			throw new IllegalArgumentException(
					"Mosaic has been populated, but dimensions of "
							+ managementMapPath + "("
							+ tmpManagementMap.getRows() + ","
							+ tmpManagementMap.getCols()
							+ ") are inconsistent (" + nrows + "," + ncols
							+ ").");
		}

		managementMap = tmpManagementMap;

		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				Patch p = patches.get(i * ncols + j);

				double val = managementMap.getValue(i, j);

				// Handle NoData elements

				if (Double.isNaN(val)
						|| (presenceMap != null && Double.isNaN(presenceMap
								.getValue(i, j)))
						|| (ageMap != null && Double.isNaN(ageMap
								.getValue(i, j)))
						|| (habitatMap != null && Double.isNaN(habitatMap
								.getValue(i, j)))) {
					p.setNoData(true);
				}

				// Handle values greater than 3

				if (val > 3 && (long) val != NO_MANAGEMENT) {
					System.out
							.println("Management class "
									+ (long) val
									+ " encountered in "
									+ managementMapPath
									+ ", but the value must be 1-3, equal to NO_MANAGEMENT (default is 0) or NO DATA.");
					System.exit(-1);
				}

				if (managementMap.getValue(i, j) != NO_MANAGEMENT) {
					p.setMonitored(true);
					switch ((int) val) {
					case 1: {

						if (!p.isInfestedBy(species)) {
							break;
							// p.addInfestation(species);
						}

						p.getInfestation(species).addControl(
								ControlType.GROUND_CONTROL);
						p.getInfestation(species).freezeManagement(true);
						break;
					}
					case 2: {
						p.addControl(ControlType.CONTAINMENT);
						break;
					}
					case 3: {
						p.addControl(ControlType.CONTAINMENT_CORE);
						break;
					}
					}
				}
			}
		}
	}

	/**
	 * Sets the monitoring status of a Collection of Patches.
	 */

	@Override
	public void setMonitored(Collection<Patch> patches, boolean isMonitored) {
		for (Patch p : patches) {
			p.setMonitored(isMonitored);
		}
	}

	/**
	 * Explicitly sets the number of columns in the RasterMosaic
	 * 
	 * @param ncols
	 *            - the number of columns in the RasterMosaic
	 */

	public void setNcols(int ncols) {
		this.ncols = ncols;
	}

	/**
	 * Explicitly sets the number of rows in the RasterMosaic
	 * 
	 * @param nrows
	 *            - the number of rows in the RasterMosaic
	 */

	public void setNrows(int nrows) {
		this.nrows = nrows;
	}

	/**
	 * Sets the presence information using a path to a ESRI ASCII raster object
	 */

	@Override
	public void setPresenceMap(String presenceMapPath, String species)
			throws IOException {

		// Convenience option for setting the value of all locations as true

		if (presenceMapPath.equalsIgnoreCase("ALL")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getInfestation(species).setInfested(true);
			}
			return;
		}

		// Convenience option for setting the value of all locations as false

		if (presenceMapPath.equalsIgnoreCase("NONE")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).clearInfestation(species);
			}
			return;
		}

		Raster tmpPresenceMap = RasterReader.readRaster(presenceMapPath);

		// If the cell list is empty, use the raster as a template

		if (patches.isEmpty()) {
			setup(tmpPresenceMap);
		}

		// Check that the dimensions are consistent. If not, throw an error.

		if (!checkDim(tmpPresenceMap)) {
			throw new IllegalArgumentException(
					"Mosaic has been populated, but dimensions of "
							+ presenceMapPath + "(" + tmpPresenceMap.getRows()
							+ "," + tmpPresenceMap.getCols()
							+ ") are inconsistent (" + nrows + "," + ncols
							+ ").");
		}

		presenceMap = tmpPresenceMap;

		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				Patch p = patches.get(i * ncols + j);

				// Handle NoData elements

				if (Double.isNaN(presenceMap.getValue(i, j))
						|| (ageMap != null && Double.isNaN(ageMap
								.getValue(i, j)))
						|| (habitatMap != null && Double.isNaN(habitatMap
								.getValue(i, j)))) {
					p.setNoData(true);

					// Otherwise, set as Infested, adding Infestation if needed.

				}

				if (presenceMap.getValue(i, j) != NO_PRESENCE) {
					if (!p.isInfestedBy(species)) {
						p.addInfestation(species);
					}
					p.getInfestation(species).setInfested(true);
				}
			}
		}
	}

	@Override
	public void setSpeciesList(List<String> speciesList) {
		this.speciesList = speciesList;
	}

	/**
	 * Sets up the array of cells based on the parameters provided
	 * 
	 * @param nrows
	 *            - number of rows
	 * @param ncols
	 *            - number of columns
	 * @param cellsize
	 *            - cell size (side length of a square)
	 * @param llx
	 *            - lower left corner x coordinate
	 * @param lly
	 *            - lower left corner y coordinate
	 */

	public void setup(int nrows, int ncols, double cellsize, double llx,
			double lly) {
		this.nrows = nrows;
		this.ncols = ncols;
		this.cellsize = cellsize;
		this.llx = llx;
		this.lly = lly;

		buildPatches();
	}

	/**
	 * Sets up the array of cells based on a Raster template
	 * 
	 * @param template
	 *            - Raster providing the template of the cells
	 * @throws IOException
	 */

	public void setup(Raster template) throws IOException {

		// Store number of rows and columns and cellsize for re-use

		nrows = template.getRows();
		ncols = template.getCols();
		cellsize = template.getCellsize();
		llx = template.getXll();
		lly = template.getYll();

		buildPatches();
	}

	public void setVisited(Collection<Patch> patches) {
		for (Patch p : patches) {
			p.setVisited(true);
		}
	}

	/**
	 * Assigns whether a collection of infestations (within Patches) should be
	 * tagged as visited.
	 * 
	 * @param patches
	 *            - The patches to be tagged
	 * @param species
	 *            - The species of interest
	 */

	@Override
	public void setVisited(Collection<Patch> patches, String species) {
		for (Patch p : patches) {
			p.getInfestation(species).setVisited(true);
		}
	}

	/**
	 * Performs teardown functions for the class.
	 */

	@Override
	public void shutdown() {
		patches = null;
	}

	/**
	 * Updates the list of species used by the Mosaic.
	 */

	public void updateSpeciesList() {
		Set<String> speciesList = new TreeSet<String>();
		for (int key : patches.keySet()) {
			speciesList.addAll(patches.get(key).getInfestation().keySet());
		}
	}
	
	public void updateInfestationStages(Map<String, long[]> thresholds) {
		for (String species:thresholds.keySet()) {
			for(Patch p:patches.values()){
				if(p.isInfestedBy(species)&&p.getInfestation(species).getAgeOfInfestation()>0){
					int stage = Arrays.binarySearch(thresholds.get(species), p.getInfestation(species).getAgeOfInfestation());
					stage = stage<0?-(stage+1)+1:stage+1;
					p.setStageOfInfestation(species,stage);
				}
			}
		}
	}

}