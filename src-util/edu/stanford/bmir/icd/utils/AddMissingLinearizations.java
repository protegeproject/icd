package edu.stanford.bmir.icd.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * This script adds some of the ICD-10 Historic linearization specifications 
 * and splits the Primary Care linearizations into High-Resource and 
 * Low-Resource settings, for all ICD Categories.
 * 
 * @author csnyulas
 */
public class AddMissingLinearizations {
	private static final String EXCEL_FILE_ICD10 = "resources/xls/icd10.xls";
	
	private static final String[] icd11LinearizationNames = new String[] {"Primary_Care_High_RS", "Primary_Care_Low_RS"};
	private static final String[] recyclableIcd11LinearizationNames = new String[] {"PrimaryCare"};
	private static final LinearizationConfiguration[] icd11LinearizationConfigurations = new LinearizationConfiguration[] {
			LinearizationConfiguration.UNMODIFIED,
			LinearizationConfiguration.USE_PREVIOUS };
	private static final String[] icd10LinearizationNames = new String[] {"Historic_Linearization_ICD10", "Historic_Linearization_ICD10_CM"};
	private static final String[] recyclableIcd10LinearizationNames = new String[] {};

	private static File xlFileICD10 = new File(EXCEL_FILE_ICD10);

	private static Map<String, CategoryInfo> categoryInfoMap;

    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
	private static RDFSNamedClass linearizationMetaClass;

    private static OWLNamedClass linearizationSpecificationClass;
    private static OWLNamedClass historicLinearizationSpecificationClass;
    private static RDFProperty linearizationProp;
    private static RDFProperty icd10LinearizationProp;
    private static RDFProperty linearizationViewProp;
    private static RDFProperty isIncludedInLinearizationProp;
    private static RDFProperty isGroupingProp;
    private static RDFProperty linearizationSortingLabelProp;
    private static RDFProperty linearizationParentProp;

    private static ArrayList<RDFResource> linearizationViewsForIcd11Linearizations;
    private static ArrayList<RDFResource> recyclableLinearizationViewsForIcd11Linearizations;
    private static ArrayList<RDFResource> linearizationViewsForIcd10Linearizations;
    private static ArrayList<RDFResource> recyclableLinearizationViewsForIcd10Linearizations;

    private static int cntProblemsWithLinParent = 0;
    private static int cntWrongLinParentRetired = 0;
    private static int cntPossiblyWrongLinParent = 0;
    private static int cntWrongLinParent = 0;
    private static int cntWrongLinParentMultiParent = 0;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: AddMissingLinearizations pprjFileName xlFileICD10");
            return;
        }

        Collection<?> errors = new ArrayList<Object>();
        Project prj = Project.loadProjectFromFile(args[0], errors);
        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }
		xlFileICD10 = new File(args[1]);

		Log.getLogger().info("\n===== Starting import from Excel " + new Date());
		Log.getLogger().info("=== PPRJ File: " + args[0]);
		Log.getLogger().info("=== Excel file no mortality: " + xlFileICD10);

		categoryInfoMap = readExcelFile(xlFileICD10);
		System.out.println(categoryInfoMap);

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            System.out.println("Failed");
            return;
        }
        icdContentModel = new ICDContentModel(owlModel);

        linearizationMetaClass = icdContentModel.getLinearizationMetaClass();
        linearizationSpecificationClass = (OWLNamedClass) icdContentModel.getLinearizationSpecificationClass();
        historicLinearizationSpecificationClass = (OWLNamedClass) icdContentModel.getLinearizationHistoricSpecificationClass();

        linearizationProp = icdContentModel.getLinearizationProperty();
        icd10LinearizationProp = icdContentModel.getLinearizationICD10Property();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
        isIncludedInLinearizationProp = icdContentModel.getIsIncludedInLinearizationProperty();
        isGroupingProp = icdContentModel.getIsGroupingProperty();
        linearizationSortingLabelProp = icdContentModel.getLinearizationSortingLabelProperty();
        linearizationParentProp = icdContentModel.getLinearizationParentProperty();
        
        linearizationViewsForIcd11Linearizations = new ArrayList<RDFResource>();
        for (String icd11LinName : icd11LinearizationNames) {
        	linearizationViewsForIcd11Linearizations.add(owlModel.getOWLIndividual(ICDContentModelConstants.NS + icd11LinName));
        }
        recyclableLinearizationViewsForIcd11Linearizations = new ArrayList<RDFResource>();
        for (String icd11LinName : recyclableIcd11LinearizationNames) {
        	recyclableLinearizationViewsForIcd11Linearizations.add(owlModel.getOWLIndividual(ICDContentModelConstants.NS + icd11LinName));
        }
		linearizationViewsForIcd10Linearizations = new ArrayList<RDFResource>();
		for (String icd10LinName : icd10LinearizationNames) {
			linearizationViewsForIcd10Linearizations.add(owlModel.getOWLIndividual(ICDContentModelConstants.NS + icd10LinName));
		}
		recyclableLinearizationViewsForIcd10Linearizations = new ArrayList<RDFResource>();
		for (String icd10LinName : recyclableIcd10LinearizationNames) {
			recyclableLinearizationViewsForIcd10Linearizations.add(owlModel.getOWLIndividual(ICDContentModelConstants.NS + icd10LinName));
		}

        fixLinearizations();
    }

    private static void fixLinearizations() {
        long t0 = System.currentTimeMillis();

        Log.getLogger().setLevel(Level.FINE);

        owlModel.setGenerateEventsEnabled(false);
        RDFSNamedClass icdCatCls = icdContentModel.getICDCategoryClass();

        Collection<RDFResource> missingIcdCategoryLinViews = getMissingLinearizations(icdCatCls);
        Collection<RDFResource> missingIcdCategoryHistLinViews = getMissingICD10Linearizations(icdCatCls);

        //checking whether whether changes are necessary or not
        boolean proceed = true;
        if (missingIcdCategoryLinViews.isEmpty() && missingIcdCategoryHistLinViews.isEmpty()) {
        	System.out.println("There is no linarization view missing from 'ICD Categories', " +
        			"which means that probably there is no point in running this time-consuming script!");
        	try {
        		System.out.println("Do you wish to run the script on all the ICD categories, nevertheless? [Y/N] ");
				int ch = System.in.read();
				if (ch != 'Y' && ch != 'y') {
					proceed = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        else {
        	System.out.println("Adding the following linearizations:");
        	for (RDFResource linView : missingIcdCategoryLinViews) {
				System.out.println("    " + linView.getBrowserText());
			}
        	System.out.println("Adding the following ICD10 linearizations:");
        	for (RDFResource linView : missingIcdCategoryHistLinViews) {
        		System.out.println("    " + linView.getBrowserText());
        	}
        }

        //applying the fix
        if (proceed) {
	        fixLinearization(icdCatCls);
	        System.out.println("Retrieving all categories...");
	        Collection<RDFSNamedClass> subclses = icdCatCls.getSubclasses(true);
	        System.out.println("Done! (All categories have been retrieved in : " + (System.currentTimeMillis() - t0) /1000 + " secs)");
	        int n = subclses.size();
	        int i = 0;
	        for (RDFSNamedClass subcls :subclses) {
				if (++i % 500 == 0) {
					System.out.println("" + (i) + "/" + n + " (" + i*100/n + "%)");
				}
	        	//if (i <= 0) continue;	//activate for DEBUG mode
	        	//if (i > 500) break;	//activate for DEBUG mode
	        	
	            if (subcls instanceof RDFSNamedClass) {
	                fixLinearization(subcls);
	            }
	        }
	        Log.getLogger().info("A TOTAL of " + cntProblemsWithLinParent + " classes had some kind of problem with their linearization parents.");
	        Log.getLogger().info("There were " + cntWrongLinParentRetired + " RETIRED classes with wrong linearization parents.");
	        Log.getLogger().info("There were " + cntPossiblyWrongLinParent + " classes with POSSIBLY wrong linearization parents (i.e. indirect parent).");
	        Log.getLogger().info("There were " + cntWrongLinParent + " SINGLE PARENT classes with wrong linearization parents.");
	        Log.getLogger().info("There were " + cntWrongLinParentMultiParent + " MULTIPLE PARENT classes with wrong linearization parents.");
	        Log.getLogger().info("Done");
        }
        else {
        	System.out.println("There was nothing to be done");
        	Log.getLogger().info("Script aborted on user request");
        }

        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");
    }


    private static void fixLinearization(RDFSNamedClass c) {
        //ICD-11 linearizations
        Collection<RDFResource> missingLinViews = getMissingLinearizations(c);

        List<RDFResource> recyclableLinSpecs = getLinearizationSpecificationsForViews(
        		c, linearizationProp, recyclableLinearizationViewsForIcd11Linearizations);
        int recycledLinSpecCntr = 0;
        int recyclableCount = recyclableLinSpecs.size();
        int confCntr = 0;
    	for (RDFResource linViewInstance : linearizationViewsForIcd11Linearizations) {
    		LinearizationConfiguration conf = icd11LinearizationConfigurations[confCntr++];
    		if (missingLinViews.contains(linViewInstance)) {
	    		RDFResource linSpec;
	    		if (recycledLinSpecCntr < recyclableCount) {
	    			linSpec = recyclableLinSpecs.get( recycledLinSpecCntr ++ );
	    		}
	    		else {
	    			linSpec = linearizationSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
	            	c.addPropertyValue(linearizationProp, linSpec);
	    		}
	            linSpec.setPropertyValue(linearizationViewProp, linViewInstance);
	
	            //initialize all properties
	            if (conf == null || conf == LinearizationConfiguration.DEFAULT || 
	            		conf == LinearizationConfiguration.UNMODIFIED) {
	            	//do nothing
	            }
	            else if (conf == LinearizationConfiguration.USE_PREVIOUS) {
	            	RDFResource prevLinSpec = getLinearizationSpecificationsForView(
	            			c, linearizationProp, linearizationViewsForIcd11Linearizations.get(confCntr - 2));	//-2 because counter is already pointing to next element
	            	LinearizationConfiguration prevConf = new LinearizationConfiguration(prevLinSpec);
					initializeLinearizationSpecification(linSpec, prevConf);
	            }
	            else {
	            	initializeLinearizationSpecification(linSpec, conf);
	            }
	
	            if (Log.getLogger().isLoggable(Level.FINER)) {
	                Log.getLogger().finer("Added " + linViewInstance.getBrowserText() + " to " + c.getBrowserText());
	            }
    		}
		}
    	
        //ICD-10 linearizations
        missingLinViews = getMissingICD10Linearizations(c);

        recyclableLinSpecs = getLinearizationSpecificationsForViews(
        		c, icd10LinearizationProp, recyclableLinearizationViewsForIcd10Linearizations);
        recycledLinSpecCntr = 0;
        recyclableCount = recyclableIcd10LinearizationNames.length;
        
        //prepare linearization configuration
        LinearizationConfiguration conf = LinearizationConfiguration.NOT_INCLUDED_IN_LINEARIZATION;
        CategoryInfo catInfo = categoryInfoMap.get(c.getName());
        if (catInfo != null) {
        	String catCode = catInfo.getCategoryCode();
        	boolean isGrouping = catCode.contains("-") || catCode.matches("\\D*");
			conf = new LinearizationConfiguration(true, isGrouping, catCode, icdContentModel.getICDCategory(catInfo.getParentCode()));
        }
        
    	for (RDFResource linViewInstance : missingLinViews) {
    		RDFResource linSpec;
    		if (recycledLinSpecCntr < recyclableCount) {
    			linSpec = recyclableLinSpecs.get( recycledLinSpecCntr ++ );
    		}
    		else {
    			linSpec = historicLinearizationSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
            	c.addPropertyValue(icd10LinearizationProp, linSpec);
    		}
            linSpec.setPropertyValue(linearizationViewProp, linViewInstance);
            
            //initialize all properties
            initializeLinearizationSpecification(linSpec, conf);

            if (Log.getLogger().isLoggable(Level.FINER)) {
                Log.getLogger().finer("Added " + linViewInstance.getBrowserText() + " to " + c.getBrowserText());
            }
		}

    }

	private static void initializeLinearizationSpecification( 
			RDFResource linSpec, LinearizationConfiguration conf) {
		
		if (conf.getIsIncluded() != null) {
			linSpec.setPropertyValue(isIncludedInLinearizationProp, conf.getIsIncluded());
		}
		if (conf.getIsGrouping() != null) {
			linSpec.setPropertyValue(isGroupingProp, conf.getIsGrouping());
		}
		if (conf.getSortingLabel() != null) {
			linSpec.setPropertyValue(linearizationSortingLabelProp, conf.getSortingLabel());
		}
		if (conf.getParent() != null) {
			linSpec.setPropertyValue(linearizationParentProp, conf.getParent());
		}
	}
    
    
    private static Collection<RDFResource> getMissingLinearizations(RDFSNamedClass c) {
    	return getMissingLinearizations(c, linearizationProp, linearizationViewsForIcd11Linearizations);
    }
    
    private static Collection<RDFResource> getMissingICD10Linearizations(RDFSNamedClass c) {
    	return getMissingLinearizations(c, icd10LinearizationProp, linearizationViewsForIcd10Linearizations);
    }
    
    private static RDFResource getLinearizationSpecificationsForView(RDFSNamedClass c, RDFProperty linProperty, RDFResource linViewInstance) {
    	return getLinearizationSpecificationsForViews(c, linProperty, Collections.singleton(linViewInstance)).get(0);
    }
    
    private static List<RDFResource> getLinearizationSpecificationsForViews(RDFSNamedClass c, RDFProperty linProperty, Collection<RDFResource> linViewInstances) {
    	ArrayList<RDFResource> res = new ArrayList<RDFResource>();
    	
    	if ( c.getRDFTypes().contains(linearizationMetaClass) ) {
    		Collection<RDFResource> linearizationSpecs = c.getPropertyValues(linProperty);
    		
    		for (RDFResource linView : linViewInstances) {
    			for (RDFResource linSpec : linearizationSpecs) {
    				RDFResource currLinView = (RDFResource) linSpec.getPropertyValue(linearizationViewProp);
    				if (currLinView.equals(linView)) {
    					res.add(linSpec);
    					break;
    				}
    			}
    		}
    	}
    	
    	return res;
    }
    
    
    private static Collection<RDFResource> getMissingLinearizations(RDFSNamedClass c, RDFProperty linProperty, Collection<RDFResource> linViewInstances) {
    	ArrayList<RDFResource> res = new ArrayList<RDFResource>();

    	if ( c.getRDFTypes().contains(linearizationMetaClass) ) {
    	    int wrongLinParentRetired = 0;
    	    int possiblyWrongLinParent = 0;
    	    int wrongLinParent = 0;
    	    int wrongLinParentMultiParent = 0;

    		res.addAll(linViewInstances);

            //Collection<RDFResource> linearizationSpecs = icdContentModel.getLinearizationSpecifications(c);
            Collection<RDFResource> linearizationSpecs = c.getPropertyValues(linProperty);

//            RDFSNamedClass singleParent = getSingleParent(c);

            for (RDFResource linSpec : linearizationSpecs) {
            	RDFResource linView = (RDFResource) linSpec.getPropertyValue(linearizationViewProp);
            	//remove linearization parent if necessary
//            	RDFResource linParent = (RDFResource) linSpec.getPropertyValue(linearizationParentProp);
//            	if (singleParent != null) {
//            		if (singleParent.equals(linParent)) {
//            			linSpec.removePropertyValue(linearizationParentProp, linParent);
//            		}
//            		else {
//            			//if we have a linearization parent that is not a direct superclass
//            			if (linParent != null && !c.getSuperclasses(false).contains(linParent)) {
//            				Collection<RDFSNamedClass> allSuperclasses = c.getSuperclasses(true);
//            				if (getBrowserTexts(allSuperclasses).toUpperCase().contains("RETIRED")) {
//            					wrongLinParentRetired ++;
//            					if (Log.getLogger().isLoggable(Level.FINER)) {
//            					    Log.getLogger().log(Level.FINER, "POSSIBLE ERROR IN RETIRED CLASS: The retired class " + c.getBrowserText() +
//            							" has a linearization parent set for linearization " + linView.getBrowserText() + ", other then its direct parent, namely: " + linParent.getBrowserText());
//            					}
//            				}
//            				else {
//	            				if (allSuperclasses.contains(linParent)) {
//	            					possiblyWrongLinParent ++;
//	            					Log.getLogger().log(Level.INFO, "POSSIBLE ERROR IN THE MODEL: The linearization parent of " + c.getBrowserText() +
//	            							" for linearization " + linView.getBrowserText() + " does not refer to a parent, but to a higher order ancestor: " + linParent.getBrowserText());
//	            				}
//	            				else {
//	            					wrongLinParent ++;
//	            					Log.getLogger().log(Level.WARNING, "ERROR IN THE MODEL: The linearization parent of " + c.getBrowserText() +
//	            							" for linearization " + linView.getBrowserText() + " does not refer to an ancestor (superclass), but to:" + linParent.getBrowserText());
//	            				}
//            				}
//            			}
//            		}
//            	}
//            	else {
//            		//check if linearization parent is in the ancestors of one of the multiple parents
//    				Collection<RDFSNamedClass> allSuperclasses = c.getSuperclasses(true);
//    				if (linParent != null && !allSuperclasses.contains(linParent)) {
//        				wrongLinParentMultiParent ++;
//    					Log.getLogger().log(Level.WARNING, "ERROR IN THE MODEL: The linearization parent of " + c.getBrowserText() +
//    							" for linearization " + linView.getBrowserText() + " does not refer to any of the ancestors (superclasses), but to:" + linParent.getBrowserText());
//    				}
//            	}

            	//remove this linearization view from the result
    			res.remove(linView);
    		}

        	//update wrong linearization parent counters
        	if (wrongLinParentRetired + possiblyWrongLinParent + wrongLinParent + wrongLinParentMultiParent > 0) {
        		cntProblemsWithLinParent ++;
        		cntWrongLinParentRetired += (wrongLinParentRetired == 0 ? 0 : 1);
        		cntPossiblyWrongLinParent += (possiblyWrongLinParent == 0 ? 0 : 1);
        		cntWrongLinParent += (wrongLinParent == 0 ? 0 : 1);
        		cntWrongLinParentMultiParent += (wrongLinParentMultiParent == 0 ? 0 : 1);
        	}
    	}

        return res;
    }


//	private static String getBrowserTexts( Collection<RDFSNamedClass> classes) {
//		String s = "[";
//		boolean first = true;
//		for (RDFSNamedClass c : classes) {
//			if (first) {
//				//add nothing, just switch the flag
//				first = false;
//			}
//			else {
//				s += ", ";
//			}
//			s += c.getBrowserText();
//		}
//		s += "]";
//		return s;
//	}
//
//	/**
//	 * Checks whether the class <code>c</code> has exactly one superclass, and in case
//	 * it does it returns that superclass. If the class has more than one superclass
//	 * the method returns null.
//	 *
//	 * @param c a class
//	 * @return
//	 */
//	private static RDFSNamedClass getSingleParent(RDFSNamedClass c) {
//		RDFSNamedClass singleParent = null;
//		Collection<?> superclasses = c.getSuperclasses(false);
//		if (superclasses != null && superclasses.size() > 0) {
//			boolean lookingForFirst = true;
//			for (Object superclass : superclasses) {
//				if (superclass instanceof RDFSNamedClass) {
//					if (lookingForFirst) {
//						//found the first valid parent
//						singleParent = (RDFSNamedClass) superclass;
//					}
//					else {
//						//this is one of the multiple parents: reset singleParent to null
//						singleParent = null;
//					}
//					lookingForFirst = false;
//				}
//			}
//		}
//		return singleParent;
//	}


	private static Map<String, CategoryInfo> readExcelFile(File excelFile) {
		LinkedHashMap<String, CategoryInfo> result = new LinkedHashMap<String, CategoryInfo>();
		
		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			Sheet sh = wb.getSheet(0);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)
				
				//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
				{
					String catCode = sh.getCell(0,r).getContents();
					String parentCode = sh.getCell(1,r).getContents();
					String catURI = ICDContentModelConstants.NS + catCode.trim(); 

					CategoryInfo categoryInfo = new CategoryInfo(catCode, parentCode);
					result.put(catURI, categoryInfo);
				}
			}
			System.out.println("Done with reading excel file!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	private static class CategoryInfo {
		private String catCode;
		private String parentCode;
		
		public CategoryInfo() {
		}
		
		public CategoryInfo(String category, String parent) {
			this();
			setCategoryCode(category);
			setParentCode(parent);
		}
		
		public String getCategoryCode() {
			return catCode;
		}
		public void setCategoryCode(String categoryCode) {
			this.catCode = categoryCode;
		}
		
		public String getParentCode() {
			return parentCode;
		}
		public void setParentCode(String parentCode) {
			this.parentCode = parentCode;
		}
		
		@Override
		public String toString() {
			String res = "";
			res += getCategoryCode();
			res += " -> ";
			res += getParentCode();
			return res;
		}
	}

	static class LinearizationConfiguration {
		private Boolean isIncluded;
		private Boolean isGrouping;
		private String sortingLabel;
		private RDFResource parent;
		
		public static final LinearizationConfiguration DEFAULT = new LinearizationConfiguration() {
			public String toString() { return "DEFAULT"; }
		};
		public static final LinearizationConfiguration UNMODIFIED = new LinearizationConfiguration() {
			public String toString() { return "UNMODIFIED"; }
		};
		public static final LinearizationConfiguration USE_PREVIOUS = new LinearizationConfiguration() {
			public String toString() { return "USE_PREVIOUS"; }
		};
		
		public static final LinearizationConfiguration INCLUDED_IN_LINEARIZATION = new LinearizationConfiguration(true);
		public static final LinearizationConfiguration INCLUDED_IN_LINEARIZATION_GROUPING = new LinearizationConfiguration(true, true);
		public static final LinearizationConfiguration INCLUDED_IN_LINEARIZATION_NON_GROUPING = new LinearizationConfiguration(true, false);
		
		public static final LinearizationConfiguration NOT_INCLUDED_IN_LINEARIZATION = new LinearizationConfiguration(false);
		public static final LinearizationConfiguration NOT_INCLUDED_IN_LINEARIZATION_NON_GROUPING = new LinearizationConfiguration(false, false);

		
		public LinearizationConfiguration() {
			isIncluded = isGrouping = null;
			sortingLabel = null;
			parent = null;
		}
		
		public LinearizationConfiguration(Boolean isIncluded) {
			this.isIncluded = isIncluded;
		}
		
		public LinearizationConfiguration(Boolean isIncluded, Boolean isGrouping) {
			this.isIncluded = isIncluded;
			this.isGrouping = isGrouping;
		}
		
		public LinearizationConfiguration(Boolean isIncluded, Boolean isGrouping, String linearizationSortingLabel) {
			this.isIncluded = isIncluded;
			this.isGrouping = isGrouping;
			this.sortingLabel = linearizationSortingLabel;
		}
		
		public LinearizationConfiguration(Boolean isIncluded, Boolean isGrouping, String linearizationSortingLabel, RDFResource parent) {
			this.isIncluded = isIncluded;
			this.isGrouping = isGrouping;
			this.sortingLabel = linearizationSortingLabel;
			this.parent = parent;
		}
		
		public LinearizationConfiguration(RDFResource linearizationSpecification) {
			setIsIncluded((Boolean) linearizationSpecification.getPropertyValue(isIncludedInLinearizationProp));
			setIsGrouping((Boolean) linearizationSpecification.getPropertyValue(isGroupingProp));
			setSortingLabel((String) linearizationSpecification.getPropertyValue(linearizationSortingLabelProp));
			setParent((RDFResource) linearizationSpecification.getPropertyValue(linearizationParentProp));
		}
		
		public Boolean getIsIncluded() {
			return isIncluded;
		}
		public void setIsIncluded(Boolean isIncluded) {
			this.isIncluded = isIncluded;
		}

		public Boolean getIsGrouping() {
			return isGrouping;
		}
		public void setIsGrouping(Boolean isGrouping) {
			this.isGrouping = isGrouping;
		}

		public String getSortingLabel() {
			return sortingLabel;
		}
		public void setSortingLabel(String sortingLabel) {
			this.sortingLabel = sortingLabel;
		}

		public RDFResource getParent() {
			return parent;
		}
		public void setParent(RDFResource parent) {
			this.parent = parent;
		}
		

		@Override
		public String toString() {
			return "[" + isIncluded + " - " + isGrouping + " - " + sortingLabel + " - " + parent + "]";
		}
	}
}
