/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.output.OutputWriter_RasterCoverTest;
import test.process.Process_CostingTest;
import test.process.Process_DispersalTest;
import test.process.Process_GrowthTest;
import test.process.Process_InfestationTest;
import test.process.Process_MonitorTest;
import test.util.StatsTest;

@RunWith(Suite.class)
@SuiteClasses({ RasterMosaicTest.class, Process_CostingTest.class, 
	    Process_GrowthTest.class, Process_DispersalTest.class, 
	    Process_InfestationTest.class, Process_MonitorTest.class, 
	    OutputWriter_RasterCoverTest.class,	ExperimentTest.class, StatsTest.class })
public class AllTests {

}
