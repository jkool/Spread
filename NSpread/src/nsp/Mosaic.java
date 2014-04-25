package nsp;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nsp.util.ControlType;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A Mosaic represents a linked collection of individual patch objects, allowing
 * for processes that affect their properties as an entire collection. The use
 * of an interface allows for different implementations to be written (e.g.
 * raster versus vector operations) in a modular manner.
 * 
 */

public interface Mosaic {

	public void addSpecies(String species);
	
	/**
	 * Clears the Patch information and dimensions from the instance.
	 */

	public void clear();

	/**
	 * Resets all Patches to being unvisited.
	 */

	public void clearVisited();
	
	/**
	 * Assigns whether a collection of occupancies (within Patches) should have their visited status cleared
	 * 
	 * @param patches - The patches to be tagged
	 * @param species - The species of interest
	 */
	
	public void clearVisitedPatches(Collection<Patch> patches, String species);
	
	/**
	 * Assigns whether a collection of occupancies should have their visited status cleared
	 * 
	 * @param patches - The patches to be tagged
	 */
	
	public void clearVisitedOccupancies(Collection<Occupancy> occupancies);

	/**
	 * Generates a copy of the class instance.
	 */

	public Mosaic clone();

	Set<Patch> fill(Collection<Patch> region, String species);
	
	/**
	 * Retrieves the total area covered by the set of patches
	 * @param patches
	 * @return
	 */
	
	public double getArea(Collection<Patch> patches);
	
	/**
	 * Retrieves a region of infested patches, including all enclosed areas even if not infested.
	 * 
	 * @param patch
	 * @param species
	 * @return
	 */
	
	public Set<Patch> getFilledRegion(Patch patch, String species);
	
	/**
	 * Retrieves the infested Patches within the Mosaic
	 * @return a Map of Patch keys and infested Patches within the Mosaic
	 */
	
	public Map<Integer, Patch> getInfested();
	
	/**
	 * Retrieves the number of infested Patches within the Mosaic
	 * @return - the number of infested Patches within the Mosaic
	 */
	
	public int getNumberInfested();
	
	/**
	 * Retrieves the number of infested Patches within the Mosaic
	 * @return - the number of infested Patches within the Mosaic
	 */
	
	public int getNumberInfested(String species);

	/**
	 * Retrieves a map of keys corresponding to Patches occupied by the given species
	 * 
	 * @param species
	 * @return
	 */
	
	public Map<Integer, Occupancy>getOccupied(String species);
	
	/**
	 * Retrieves a map of keys and Patches capable of containing species whether or not they are occupied.
	 * @param species
	 * @return
	 */
	
	public Map<Integer,Occupancy>getOccupancies(String species);
	
	/**
	 * Retrieves a single Patch object using its key.
	 * 
	 * @param id
	 *            - the ID of the Patch object
	 * @return - the Patch object corresponding to the key provided
	 */	

	public Patch getPatch(int id);

	/**
	 * Retrieves a map of cell objects. Integer value corresponds to the unique
	 * index of the cell, defined as row*number of columns + column
	 */

	public Map<Integer, Patch> getPatches();

	/**
	 * Retrieves the set of species occupying the Mosaic
	 * @return
	 */
	
	public List<String> getSpeciesList();

	/**
	 * Retrieves the inner buffered core of a collection of patches.  Exterior strongly-connected elements are
	 * removed (i.e. diagonally connected elements are not trimmed).
	 * 
	 * @param patches - the collection of patches from which the core is to be extracted
	 * @param species - the species that determine which occupancy to use
	 * @param bufferSize - the size of the inner buffer to be removed.
	 * @return
	 */
	
	public Set<Patch> getStrongCore(Collection<Patch> patches, String species, double bufferSize);
	
	/**
	 * Returns patches that are strongly connected to the region (i.e. entire edge)
	 * 
	 * @param patch
	 * @param species
	 * @return
	 */
	
	public Set<Patch> getStrongRegion(Patch patch, String species);
	
	/**
	 * Retrieves the inner buffered core of a collection of patches.  Exterior weakly-connected elements are
	 * removed (i.e. diagonally connected elements are trimmed).
	 * 
	 * @param patches - the collection of patches from which the core is to be extracted
	 * @param species - the species that determine which occupancy to use
	 * @param bufferSize - the size of the inner buffer to be removed.
	 * @return
	 */
	
	public Set<Patch> getWeakCore(Collection<Patch> patches, String species, double bufferSize);
	
	/**
	 * Returns patches that are weakly connected to the region (i.e. single vertex)
	 * 
	 * @param patch
	 * @param species
	 * @return
	 */
	
	public Set<Patch> getWeakRegion(Patch patch, String species);
	
	/**
	 * Infests the mosaic according to a List of coordinate values. Note(!)
	 * infestation *must* happen at the Mosaic level due to dependencies on
	 * implementation-specific variables.
	 */

	public void infest(String species, List<Coordinate> propagules);

	public void removeControl(Collection<Patch> patches, String species, ControlType control);
	
	/**
	 * Sets the age information using a path.
	 */

	public void setAgeMap(String ageMapPath, String species) throws IOException;
	
	/**
	 * Sets occupancies (specified by species) within a collection of patches to be under a
	 * specified form of control.
	 * 
	 * @param patches - The patches to be controlled
	 * @param species - The species to be controlled
	 * @param control - The control label to apply to the occupancies.
	 */

	public void setControl(Collection<Patch> patches, String species, ControlType control);
	
	/**
	 * Sets a copy of the provided Disperser to all patches in the RasterMosaic.
	 * 
	 * @param disperser
	 *            - the Disperser object to be used.
	 */

	public void setDisperser(String species, Disperser disperser);

	/**
	 * Sets the provided Disperser to a single Patch in the RasterMosaic based
	 * on its key.
	 * 
	 * @param disperser
	 *            - the Disperser object to be used.
	 * @param key
	 *            - the key value identifying the Patch to which the Disperser
	 *            should be applied.
	 */

	public void setDisperser(String species, Disperser disperser, Integer key);

	/**
	 * Sets the habitat information using a path.
	 */

	public void setHabitatMap(String habitatMapPath, String species) throws IOException;
	
	/**
	 * Sets the management information using a path.
	 */

	public void setManagementMap(String managementMapPath, String species) throws IOException;

	/**
	 * Sets the presence information using a path.
	 */
	
	public void setMonitored(Collection<Patch> patches, boolean isMonitored);
	
	public void setPresenceMap(String presenceMapPath, String species) throws IOException;

	/**
	 * Sets species being considered using a Set
	 * @param speciesList
	 */
	
	public void setSpeciesList(List<String> speciesList);
	
	/**
	 * Assigns whether a collection of occupancies (within Patches) should be tagged as visited
	 * 
	 * @param patches - The patches to be tagged
	 * @param species - The species of interest
	 */
	
	public void setVisited(Collection<Patch> patches, String species);
	
	/**
	 * Performs teardown functions for the class
	 */

	public void shutdown();
}
