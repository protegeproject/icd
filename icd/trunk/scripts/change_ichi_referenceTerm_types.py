t = kb.getOWLNamedClass("http://who.int/icd#HealthIntervention");
print "Retrieving Health Intervention subclasses..."
classes = t.getSubclasses(False);
#classes = t.getSubclasses(True);
print str(classes.size()) + " classes retrieved"

pT = kb.getRDFProperty("http://who.int/icd#hasTarget");
pA = kb.getRDFProperty("http://who.int/icd#hasAction");
pM = kb.getRDFProperty("http://who.int/icd#hasMeans");
pAT = kb.getRDFProperty("http://who.int/icd#additionalTarget");

sevRefTerm = kb.getOWLNamedClass("http://who.int/icd#SeverityReferenceTerm");
ccRefTerm = kb.getOWLNamedClass("http://who.int/icd#CausingConditionReferenceTerm");
mcRefTerm = kb.getOWLNamedClass("http://who.int/icd#ManifestingConditionReferenceTerm");
awRefTerm = kb.getOWLNamedClass("http://who.int/icd#AssociatedWithReferenceTerm");

tRefTerm = kb.getOWLNamedClass("http://who.int/icd#TargetReferenceTerm");
aRefTerm = kb.getOWLNamedClass("http://who.int/icd#ActionReferenceTerm");
mRefTerm = kb.getOWLNamedClass("http://who.int/icd#MeansReferenceTerm");
atRefTerm = kb.getOWLNamedClass("http://who.int/icd#AdditionalTargetReferenceTerm");

import sys
import java.util.ArrayList as ArrayList

for c in [t]:
#for c in classes:
  print c.toString() + ": ";
  print ".  hasTarget: " + c.getPropertyValues(pT).toString();
  print ".  hasAction: " + c.getPropertyValues(pA).toString();
  print ".  hasMeans: " + c.getPropertyValues(pM).toString();
  print ".  additionalTarget: " + c.getPropertyValues(pAT).toString();
  print
  sys.stdout.flush();

#working with buckets instead of full set
arr = ArrayList(classes)

bucketSize = 5000;
n= classes.size()
groups = range(1, n / bucketSize);
print groups;
grIndex = 0;

#repeat manually for all group numbers (then do the last iteration below):

print "Group: " + str(groups[grIndex]);
subset = ArrayList()
for i in range(bucketSize * (groups[grIndex] - 1), bucketSize * groups[grIndex]) :
  subset.add(arr.get(i));
grIndex = grIndex + 1;


#last:
#print "(Last) Group: " + str(groups[grIndex-1] + 1);
#subset = ArrayList()
#for i in range(bucketSize * (groups[grIndex-1]), classes.size()) :
#  subset.add(arr.get(i));


#for c in [t]:
#for c in classes:
print "Processing " + str(subset.size()) + " classes";
for c in subset:
  refTerms = c.getPropertyValues(pT);
  if refTerms.size() > 0 :
      print c.toString() + " (" + c.getBrowserText() + "): ";
      sys.stdout.flush();
      print ".   hasTarget:";
      for refTerm in refTerms:
        s = ".    .  " + refTerm.toString();
        if sevRefTerm.equals(refTerm.getRDFType()):
#           refTerm.addRDFType(tRefTerm);
#           refTerm.removeRDFType(sevRefTerm);
          s = s + "-->" + refTerm.toString();
        print s;
        sys.stdout.flush();

  refTerms = c.getPropertyValues(pA);
  if refTerms.size() > 0 :
      print c.toString() + " (" + c.getBrowserText() + "): ";
      sys.stdout.flush();
      print ".   hasAction:";
      for refTerm in refTerms:
        s = ".    .  " + refTerm.toString();
        if ccRefTerm.equals(refTerm.getRDFType()):
#           refTerm.addRDFType(aRefTerm);
#           refTerm.removeRDFType(ccRefTerm);
          s = s + "-->" + refTerm.toString();
        print s;
        sys.stdout.flush();

  refTerms = c.getPropertyValues(pM);
  if refTerms.size() > 0 :
      print c.toString() + " (" + c.getBrowserText() + "): ";
      sys.stdout.flush();
      print ".   hasMeans:";
      for refTerm in refTerms:
        s = ".    .  " + refTerm.toString();
        if mcRefTerm.equals(refTerm.getRDFType()):
#           refTerm.addRDFType(mRefTerm);
#           refTerm.removeRDFType(mcRefTerm);
          s = s + "-->" + refTerm.toString();
        print s;
        sys.stdout.flush();

  refTerms = c.getPropertyValues(pAT);
  if refTerms.size() > 0 :
      print c.toString() + " (" + c.getBrowserText() + "): ";
      sys.stdout.flush();
      print ".   additionalTarget:";
      for refTerm in refTerms:
        s = ".    .  " + refTerm.toString();
        if awRefTerm.equals(refTerm.getRDFType()):
#           refTerm.addRDFType(atRefTerm);
#           refTerm.removeRDFType(awRefTerm);
          s = s + "-->" + refTerm.toString();
        print s;
        sys.stdout.flush();
print "Done!"