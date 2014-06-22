package nsp.impl;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import nsp.Disperser;
import nsp.Mosaic;
import nsp.Occupant;
import nsp.Patch;
import nsp.util.ControlType;
import nsp.util.Raster;
import nsp.util.RasterReader;

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
	private Long NO_PRESENCE = 0l;
	private Long NULL_HABITAT = 0l;
	private Long NO_MANAGEMENT = 0l;
	private int nrows = 0;
	private int ncols = 0;
	private double cellsize = 0.0d;
	private double llx = 0.0d;
	private double lly = 0.0d;
	private Map<Integer, Patch> patches = new TreeMap<Integer, Patch>();

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
				for (String species : speciesList) {
					patch.addOccupant(species);
				}

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
	 * Assigns whether a collection of occupancies (within Patches) should be
	 * tagged as visited
	 * 
	 * @param patches
	 *            - The patches to be tagged
	 * @param species
	 *            - The species of interest
	 * @param visited
	 *            - indicates whether the Occupancies should be marked as
	 *            visited or not visited
	 */

	public void clearVisited(Collection<Patch> patches, String species) {
		for (Patch p : patches) {
			p.getOccupant(species).setVisited(false);
		}
	}

	/**
	 * Resets a collection of Occupancies to being unvisited.
	 */

	@Override
	public void clearVisitedOccupancies(Collection<Occupant> occupancies) {
		for (Occupant o : occupancies) {
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
	 * Resets a collection of Occupancies to being unvisited (accessed as
	 * patches with species key).
	 */

	@Override
	public void clearVisitedPatches(Collection<Patch> patches, String species) {
		for (Patch p : patches) {
			p.getOccupant(species).setVisited(false);
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

		rm.speciesList = cspecies;

		rm.patches = ccells;

		return rm;
	}

	/**
	 * Fills a collection of patches associated with a given species.
	 */

	@Override
	public Set<Patch> fill(Collection<Patch> region, String species) {

		Set<Patch> s = new TreeSet<Patch>();
		s.addAll(region);
		int[] bounds = getBounds(region);
		Set<Patch> block = getBlock(bounds);
		block.removeAll(region);
		PriorityQueue<Patch> pq = new PriorityQueue<Patch>(block);

		outer: while (!pq.isEmpty()) {
			Patch seed = pq.poll();

			if (seed.hasNoData()) {
				continue;
			}

			Set<Patch> tile = getStrongRegion(seed, species, bounds);
			for (Patch p : tile) {
				int row = p.getID() / ncols;
				int col = p.getID() % ncols;
				if (row == bounds[0] || row == bounds[2] || col == bounds[1]
						|| col == bounds[3]) {
					pq.removeAll(tile);
					continue outer;
				}
			}
			s.addAll(tile);
		}
		return s;
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
	 * Retrieves the bounding coordinates for a collection of Patches.
	 * 
	 * @param patches
	 * @return
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


	// Getters and Setters ////////////////////////////////////////////////////

	/**
	 * Retrieves the cell size of the RasterMosaic.
	 * 
	 * @return - the cell size of the RasterMosaic.
	 */

	public double getCellsize() {
		return cellsize;
	}
	
	/**
	 * Retrieves the controlled cells from the RasterMosaic as a map of keys and
	 * Patches.
	 * 
	 * @return - the controlled cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	public Map<Integer, Patch> getControlled(String species) {
		Map<Integer, Patch> controlled = new TreeMap<Integer, Patch>();
		for (Integer key : patches.keySet()) {
			if (patches.get(key).getOccupant(species).getControls().size() > 0) {
				controlled.put(key, patches.get(key));
			}
		}
		return controlled;
	}

	@Override
	public Set<Patch> getFilledRegion(Patch patch, String species) {
		Set<Patch> region = getWeakRegion(patch, species);
		return fill(region, species);
	}

	/**
	 * Retrieves the infested cells (any occupied) from the RasterMosaic as a
	 * map of keys and Patches.
	 * 
	 * @return - the infested cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	@Override
	public Map<Integer, Patch> getInfested() {
		Map<Integer, Patch> infested = new TreeMap<Integer, Patch>();
		outer: for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			Iterator<String> it = p.getOccupants().keySet().iterator();
			if (p.getOccupant(it.next()).isInfested()) {
				infested.put(patch_key, patches.get(patch_key));
				continue outer;
			}
		}
		return infested;
	}

	/**
	 * Retrieves the infested cells (occupancy type specified by key) from the
	 * RasterMosaic as a map of keys and Patches.
	 * 
	 * @return - the infested cells from the RasterMosaic as a map of keys and
	 *         Patches.
	 */

	public Map<Integer, Patch> getInfested(String species) {
		Map<Integer, Patch> infested = new TreeMap<Integer, Patch>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if(p.hasOccupant(species)&&p.getOccupant(species).isInfested()){
				infested.put(patch_key, patches.get(patch_key));
			}
		}
		return infested;
	}

	public Set<Integer> getKeys(Collection<Patch> c) {
		Set<Integer> s = new TreeSet<Integer>();
		for (Patch p : c) {
			s.add(p.getID());
		}
		return s;
	}

	public double getLlx() {
		return llx;
	}

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
	 * Returns the number of columns in the raster
	 * @return
	 */

	public int getNcols() {
		return ncols;
	}
	
	/**
	 * Returns the number of rows in the raster
	 * @return
	 */

	public int getNrows() {
		return nrows;
	}
	
	/**
	 * Returns the number of patches under management control for a given species
	 * 
	 * @param species
	 * @return
	 */

	public int getNumberControlled(String species) {
		return getControlled(species).size();
	}

	/**
	 * Retrieves the total number of infested patches in the RasterMosaic.
	 * 
	 * @return - the total number of infested patches in the RasterMosaic.
	 */

	@Override
	public int getNumberInfested() {
		return getInfested().size();
	}
	
	/**
	 * Retrieves the number of infested patches in the RasterMosaic for a given species.
	 * 
	 * @return - the number of infested patches in the RasterMosaic for a given species.
	 */	

	@Override
	public int getNumberInfested(String species) {
		return getInfested(species).size();
	}
	
	/**
	 * Retrieves the total number of Patches being monitored in the Mosaic.
	 * @return
	 */

	public int getNumberMonitored() {
		return getMonitored().size();
	}
	
	/**
	 * Retrieves a Map (as in Java Map) of patch ids and Occupants of a given species type.
	 * Akin to retrieving 'available spaces'
	 */

	@Override
	public Map<Integer, Occupant> getOccupants(String species) {
		Map<Integer, Occupant> occupancies = new TreeMap<Integer, Occupant>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if (!p.hasNoData()) {
				occupancies.put(patch_key, p.getOccupant(species));
			}
		}
		return occupancies;
	}
	
	/**
	 * Retrieves a Map (as in Java Map) of patch ids and Occupants of a given species type that
	 * are actually inhabited.  Akin to retrieving 'actually occupied'.  Although there *is* a
	 * difference between this and getOccupants - they could probably be mreged as the code currently
	 * guarantees that only spaces containing an occupant exist.
	 * 
	 */

	@Override
	public Map<Integer, Occupant> getOccupied(String species) {
		Map<Integer, Occupant> occupied = new TreeMap<Integer, Occupant>();
		for (Integer patch_key : patches.keySet()) {
			Patch p = patches.get(patch_key);
			if (!p.hasNoData() && p.getOccupant(species).isInfested()) {
				occupied.put(patch_key, p.getOccupant(species));
			}
		}
		return occupied;
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
	 * Retrieves a Map (as in Java Map) of patch ids and associated Patches based
	 * on a Collection of patch id values.
	 *  
	 * @param indices
	 * @return
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
	 * Retrieves the total area of a Collection of patches.
	 * 
	 * @param patches - the Collection of patches.
	 * @return
	 */
	
	public double getRegionArea(Collection<Patch> patches) {
		return patches.size() * cellsize * cellsize;
	}
	
	/**
	 * Retrieves the List of species that can potentially exist within the Mosaic.
	 */

	@Override
	public List<String> getSpeciesList() {
		return speciesList;
	}

	/**
	 * Retrieves a set of patches that are strongly connected to the provided patch
	 * i.e. sharing an edge (in cardinal directions).
	 * 
	 * @param p - The patch to be examined for linkages.
	 * @return
	 */
	
	public Set<Patch> getStrongAdjacent(Patch p) {
		return getStrongAdjacent(p, new int[] { 0, 0, nrows - 1, ncols - 1 });
	}
	
	/**
	 * Retrieves a set of patches that are strongly connected to the provided patch
	 * i.e. sharing an edge (in cardinal directions) within a bounded area.
	 * 
	 * @param p  - The patch to be examined for linkages.
	 * @param bnds - the bounding coordinates (minrow,mincol,maxrow,maxcol)
	 * @return
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
		if (row + 1 < bnds[2]) {
			list.add(getPatch(id + ncols));
		}

		// Right
		if (column + 1 < bnds[3]) {
			list.add(getPatch(id + 1));
		}

		return list;
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
	 * associated with a given Patch, bounded by an extent box.
	 * 
	 * @param p - the 
	 * @param species
	 * @param bnds
	 * @return
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
					&& pc.getOccupant(species).isInfested() == p.getOccupant(
							species).isInfested() && !pc.hasNoData()) {
				s.add(pc);
				pq.addAll(getStrongAdjacent(pc, bnds));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}

	public Set<Patch> getWeakAdjacent(Patch p) {
		return getWeakAdjacent(p, new int[] { 0, 0, nrows - 1, ncols - 1 });
	}

	/**
	 * Retrieves cells adjacent to the designated patch including diagonals
	 * 
	 * @param p
	 * @return
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
	 * Retrieves a region (Patch collection) of strongly connected Patches
	 * associated with a given Patch.
	 */
	
	@Override
	public Set<Patch> getWeakRegion(Patch p, String species) {
		Set<Patch> s = new TreeSet<Patch>();

		s.add(p);
		p.setVisited(true);

		PriorityQueue<Patch> pq = new PriorityQueue<Patch>();
		Set<Patch> clearSet = new HashSet<Patch>();

		pq.addAll(getWeakAdjacent(p));

		while (!pq.isEmpty()) {
			Patch pc = pq.poll();

			if (pc.hasNoData()) {
				continue;
			}

			clearSet.add(pc);
			if (!pc.isVisited()
					&& pc.getOccupant(species).isInfested() == p.getOccupant(
							species).isInfested() && !pc.hasNoData()) {
				s.add(pc);
				pq.addAll(getWeakAdjacent(pc));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}
	
	/**
	 * Retrieves a region (Patch collection) of strongly connected Patches
	 * associated with a given Patch, bounded by an extent box.
	 * @param p
	 * @param species
	 * @param bnds -the extent box (mincol,minrow,maxcol,maxrow)
	 * @return
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
					&& pc.getOccupant(species).isInfested() == p.getOccupant(
							species).isInfested() && !pc.hasNoData()) {
				s.add(pc);
				pq.addAll(getStrongAdjacent(pc, bnds));
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

			if (patch.hasNoData()) {
				continue;
			}

			if (Uniform.staticNextDouble() < patch.getOccupant(species)
					.getHabitatSuitability()
					&& !patch.getOccupant(species).isInfested()) {
				patch.getOccupant(species).setInfested(true);
				patch.getOccupant(species).setAgeOfInfestation(0);
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
				if (p.getOccupant(species).isInfested() != inner.getOccupant(
						species).isInfested()) {
					output.remove(p);
					continue outer;
				}
			}
		}
		return output;
	}

	/**
	 * Eliminates strongly-connected (sharing an edge in cardinal directions) exterior 
	 * cells from a region (Collection of Patches)
	 * @param region
	 * @param species
	 * @param depth - the depth to which exterior cells should be removed.
	 * @return
	 */
	
	public Set<Patch> nibbleStrong(Collection<Patch> region, String species,
			int depth) {

		if (depth <= 0) {
			return new TreeSet<Patch>(region);
		}
		if (depth == 1) {
			return nibbleStrong(region, species);
		}

		Set<Patch> full = new TreeSet<Patch>(region);
		Set<Patch> output = nibbleStrong(region, species);

		for (int i = 1; i < depth; i++) {
			if (output.isEmpty()) {
				return output;
			}

			Set<Patch> edges = new TreeSet<Patch>(region);
			edges.removeAll(output);
			output = nibbleStrong(full, edges, species);

		}
		return output;
	}
	
	/**
	 * Eliminates strongly-connected (sharing an edge in cardinal directions) exterior 
	 * cells from a region (Collection of Patches)
	 * @param region
	 * @param edgeCells
	 * @param species
	 * @return
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
	 * Eliminates weakly-connected (point-touching - diagonal connection) exterior 
	 * cells from a region (Collection of Patches)
	 * @param region
	 * @param species
	 * @return
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
				if (p.getOccupant(species).isInfested() != inner.getOccupant(
						species).isInfested()) {
					output.remove(p);
					continue outer;
				}
			}
		}
		return output;
	}

	
	/**
	 * Eliminates weakly-connected (point-touching - diagonal connection) exterior 
	 * cells from a region (Collection of Patches)
	 * @param region
	 * @param species
	 * @param depth
	 * @return
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
	 * Removes a ControlType associated with a given species from a Collection of Patches.
	 */
	
	@Override
	public void removeControl(Collection<Patch> patches, String species,
			ControlType control) {
		for (Patch p : patches) {
			p.getOccupant(species).removeControl(control);
		}
	}

	/**
	 * Searches for Patches infested by a given species type linked to
	 * the given Patch.
	 * @param p
	 * @param species
	 * @return
	 */
	
	public Set<Patch> searchInfestation(Patch p, String species) {
		Set<Patch> s = new TreeSet<Patch>();

		if (p.hasNoData()) {
			return null;
		}

		if (!p.getOccupant(species).isInfested()) {
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
			if (!pc.isVisited() && !pc.hasNoData()
					&& pc.getOccupant(species).isInfested()) {
				s.add(pc);
				pq.addAll(getWeakAdjacent(pc));
			}
			pc.setVisited(true);
		}

		clearVisitedPatches(clearSet);
		return s;
	}

	/**
	 * Sets the map paths indicating the ages of infestation associated with given species.
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
				if (patches.get(key).getOccupant(species).isInfested()) {
					patches.get(key).getOccupant(species)
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
					if (p.hasOccupant(species)) {
						p.removeOccupant(species);
					}
				}

				else if (p.getOccupant(species).isInfested()) {
					p.getOccupant(species).setAgeOfInfestation(
							(long) ageMap.getValue(i, j));
				}
			}
		}
	}
	
	/**
	 * Set the cell size of the mosaic.
	 * @param cellsize
	 */

	public void setCellsize(double cellsize) {
		this.cellsize = cellsize;
	}
	
	/**
	 * Applies a ControlType to species occupying a Collection of Patches.
	 */

	@Override
	public void setControl(Collection<Patch> patches, String species,
			ControlType control) {
		for (Patch p : patches) {
			p.getOccupant(species).addControl(control);
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
		patches.get(key).getOccupant(species).setDisperser(d);
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
				patches.get(key).getOccupant(species).setHabitatSuitability(1d);
			}
			return;
		}

		// Convenience option for setting the value of all locations as false

		if (habitatMapPath.equalsIgnoreCase("NONE")) {
			for (Integer key : patches.keySet()) {
				patches.get(key).getOccupant(species).setHabitatSuitability(0d);
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
					if (p.hasOccupant(species)) {
						p.removeOccupant(species);
					}
				} else {
					p.getOccupant(species).setHabitatSuitability(
							habitatMap.getValue(i, j));
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
	 * Sets the map paths indicating the management types associated with given species.
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
				patches.get(key).getOccupant(species)
						.addControl(ControlType.GROUND_CONTROL);
				patches.get(key).getOccupant(species)
						.addControl(ControlType.CONTAINMENT);
				patches.get(key).getOccupant(species)
						.addControl(ControlType.CONTAINMENT_CORE);
			}
			return;
		}

		if (managementMapPath.equalsIgnoreCase("GROUND")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getOccupant(species)
						.addControl(ControlType.GROUND_CONTROL);
			}
			return;
		}

		if (managementMapPath.equalsIgnoreCase("CONTAINMENT")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getOccupant(species)
						.addControl(ControlType.CONTAINMENT);
			}
			return;
		}

		if (managementMapPath.equalsIgnoreCase("CORE")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getOccupant(species)
						.addControl(ControlType.CONTAINMENT_CORE);
			}
			return;
		}
		// Convenience option for setting the value of all locations as false

		if (managementMapPath.equalsIgnoreCase("NONE")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getOccupant(species).getControls().clear();
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
				if (Double.isNaN(val)
						|| (presenceMap != null && Double.isNaN(presenceMap
								.getValue(i, j)))
						|| (ageMap != null && Double.isNaN(ageMap
								.getValue(i, j)))
						|| (habitatMap != null && Double.isNaN(habitatMap
								.getValue(i, j)))) {
					p.setNoData(true);
					if (p.hasOccupant(species)) {
						p.removeOccupant(species);
					}
				}

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
						p.getOccupant(species).addControl(
								ControlType.GROUND_CONTROL);
						break;
					}
					case 2: {
						p.getOccupant(species).addControl(
								ControlType.CONTAINMENT);
						break;
					}
					case 3: {
						p.getOccupant(species).addControl(
								ControlType.CONTAINMENT_CORE);
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
				patches.get(key).getOccupant(species).setInfested(true);
			}
			return;
		}

		// Convenience option for setting the value of all locations as false

		if (presenceMapPath.equalsIgnoreCase("NONE")) {
			for (Integer key : patches.keySet()) {
				if (patches.get(key).hasNoData()) {
					continue;
				}
				patches.get(key).getOccupant(species).setInfested(false);
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
				if (Double.isNaN(presenceMap.getValue(i, j))
						|| (ageMap != null && Double.isNaN(ageMap
								.getValue(i, j)))
						|| (habitatMap != null && Double.isNaN(habitatMap
								.getValue(i, j)))) {
					p.setNoData(true);
					if (p.hasOccupant(species)) {
						p.removeOccupant(species);
					}
				} else if (presenceMap.getValue(i, j) != NO_PRESENCE) {
					p.getOccupant(species).setInfested(true);
				} else {
					p.getOccupant(species).setInfested(false);
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
	 * Assigns whether a collection of occupancies (within Patches) should be
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
			p.getOccupant(species).setVisited(true);
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
			speciesList.addAll(patches.get(key).getOccupants().keySet());
		}
	}

}