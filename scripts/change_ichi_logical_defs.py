import sys
import java.util.ArrayList as ArrayList
from edu.stanford.smi.protegex.owl.model import OWLIntersectionClass, OWLNamedClass
from edu.stanford.smi.protegex.owl.model.impl import DefaultOWLIntersectionClass

hi = kb.getOWLNamedClass("http://who.int/icd#HealthIntervention");
t = kb.getOWLNamedClass("http://who.int/icd#HealthIntervention");
#t = kb.getOWLNamedClass("http://who.int/icd#3_512ebf36_bf6b_4d70_85da_cf2637062af4");
#t = kb.getOWLNamedClass("http://who.int/icd#38289_512ebf36_bf6b_4d70_85da_cf2637062af4");
#t = kb.getOWLNamedClass("http://who.int/icd#45306_512ebf36_bf6b_4d70_85da_cf2637062af4");
#t = kb.getOWLNamedClass("http://who.int/icd#48289_512ebf36_bf6b_4d70_85da_cf2637062af4");
print "Retrieving Health Intervention subclasses..."
classes = t.getSubclasses(False);
#classes = t.getSubclasses(True);
print str(classes.size()) + " classes retrieved"

#working with buckets instead of full set
arr = ArrayList(classes)

bucketSize = 200;
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
#  print c.toString() + " (" + c.getBrowserText() + "): ";
#  sys.stdout.flush();

  supers = c.getSuperclasses();

  #namedSupers = ArrayList();
  isHealthIntSuper = False;
  isAnotherSuper = False;
  for s in supers :
    if isinstance(s, OWLNamedClass) :
      #namedSupers.add(s);
	  if s == hi :
	    isHealthIntSuper = True;
	  else :
	    isAnotherSuper = True;

  for s in supers:
    if isinstance(s, OWLIntersectionClass):
      fillers = s.getOperands();
      #print c.toString() + " (" + c.getBrowserText() + "): ";
      #print ". " + s.toString() + ". Fillers: " + fillers.toString();
      super = None;
      copyRestr = kb.createOWLIntersectionClass()
      for f in fillers:
        if isinstance(f, OWLNamedClass):
          super = f;
        else:
          clone = kb.createOWLSomeValuesFrom(f.getOnProperty(), f.getSomeValuesFrom())
          copyRestr.addOperand(clone);
      if super is not None:
        print c.toString() + ". Superclass: " + s.toString() + ". Fillers: " + fillers.toString()
        print ".  . REPLACING " + s.getBrowserText() + " WITH:   " + super.getBrowserText() + "  AND " + copyRestr.getBrowserText();
        if (isAnotherSuper == False) :
          c.addSuperclass(super);
        c.addSuperclass(copyRestr);
        c.removeSuperclass(s);
  sys.stdout.flush();
print "Done!"