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
mRefTerm = kb.getOWLNamedClass("http://who.int/icd#MeanReferenceTerm");
atRefTerm = kb.getOWLNamedClass("http://who.int/icd#AdditionalTargetReferenceTerm");

import sys
for c in [t]:
#for c in classes:
  print c.toString() + ": ";
  print ".  hasTarget: " + c.getPropertyValues(pT).toString();
  print ".  hasAction: " + c.getPropertyValues(pA).toString();
  print ".  hasMeans: " + c.getPropertyValues(pM).toString();
  print ".  additionalTarget: " + c.getPropertyValues(pAT).toString();
  print
  sys.stdout.flush();

#for c in [t]:
for c in classes:
  print c.toString() + " (" + c.getBrowserText() + "): ";
  sys.stdout.flush();

  refTerms = c.getPropertyValues(pT);
  if refTerms.size() > 0 :
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
      print ".   additionalTarget:";
      for refTerm in refTerms:
        s = ".    .  " + refTerm.toString();
        if awRefTerm.equals(refTerm.getRDFType()):
#           refTerm.addRDFType(atRefTerm);
#           refTerm.removeRDFType(awRefTerm);
          s = s + "-->" + refTerm.toString();
        print s;
        sys.stdout.flush();