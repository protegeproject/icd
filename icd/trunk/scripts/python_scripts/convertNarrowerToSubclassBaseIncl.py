# 
# This script converts narrower terms to subclasses, and adds them
# as base inclusions from subclasses

from edu.stanford.smi.protege.server.framestore import RemoteClientFrameStore;

pNarrower = cm.getNarrowerProperty();
pBaseIndex = cm.getBaseIndexProperty();
pIndexBaseInclusion = cm.getIndexBaseInclusionProperty();
pSubclassBaseInclusion = cm.getSubclassBaseInclusionProperty();
pReferencedCategory = cm.getReferencedCategoryProperty();
pLabel = cm.getLabelProperty();
pIsIncluded = cm.getIsIncludedInLinearizationProperty();

session = RemoteClientFrameStore.getCurrentSession(kb);

tam = ["http://who.int/icd#Target", "http://who.int/icd#Action", "http://who.int/icd#Means"];

cnt = 0;
for tamCName in tam:
	tamc = kb.getRDFSNamedClass(tamCName);
	tamcs = [tamc];
	tamcs.extend(tamc.getSubclasses());
	for tamCls in tamcs:
		tamClsName = tamCls.getName();
		title = cm.getTitleLabel(tamCls);
		#indTerms = cm.getTerms(tamCls, pBaseIndex);
		nTerms = cm.getTerms(tamCls, pNarrower);
		bInclTerms = cm.getTerms(tamCls, pIndexBaseInclusion);
		if nTerms:
			print title, " (narrower): ", nTerms;
			print title, "(inclusions): ", bInclTerms;
		found = False;
		for nTerm in nTerms:
			nLabel = nTerm.getPropertyValue(pLabel);
			print "   ", nLabel;
			session.setDelegate("WHO");
			#---start transaction for subclass creation
			kb.beginTransaction("Create class with name: " + nLabel + ", parents: " + title + " (as part of converting a narrower term into a subclass)");
			newTamCls = cm.createICDCategory(None, [tamClsName]);
			print "cm.createICDCategory(None, [", tamClsName,"])";
			#create and add title term:
			titleTerm = cm.createTitleTerm();
			cm.fillTerm(titleTerm, None, nLabel, None);
			cm.addTitleTermToClass(newTamCls, titleTerm);
			#set isPartOf flag to false
			linSpecs = cm.getLinearizationSpecifications(newTamCls);
			for linSpec in linSpecs:
				linSpec.setPropertyValue(pIsIncluded, False);
			kb.commitTransaction();
			#---end transaction for subclass creation
			
			#---start transaction for narrower term conversion
			kb.beginTransaction("Converted narrower term '" + nLabel + "' to subclass (and subclass base inclusion)", tamClsName);
			#Optional: add subclass inclusion (by checking inclusion flag, if needed)
			addAsInclusion = True;	#by default
			if nTerm in bInclTerms:
				print nLabel, " is an inclusion";
				addAsInclusion = True;
			
			if addAsInclusion:
				subclsBInclTerm = cm.createSubclassBaseInclusionTerm();
				subclsBInclTerm.setPropertyValue(pReferencedCategory, newTamCls);
				tamCls.addPropertyValue(pSubclassBaseInclusion, subclsBInclTerm);
			#delete narrower term
			nTerm.delete();
			kb.commitTransaction();
			#---end transaction for narrower term conversion
			
			session.setDelegate(None);
			cnt += 1;
			found = True;
# 			break
# 		if found:
# 			break
print(str(cnt) + " narrower terms converted to subclass base inclusions");

