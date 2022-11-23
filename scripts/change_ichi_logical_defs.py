import sys
from edu.stanford.smi.protegex.owl.model import OWLIntersectionClass, OWLNamedClass
from edu.stanford.smi.protegex.owl.model.impl import DefaultOWLIntersectionClass

t = kb.getOWLNamedClass("http://who.int/icd#HealthIntervention");
print "Retrieving Health Intervention subclasses..."
classes = t.getSubclasses(False);
#classes = t.getSubclasses(True);
print str(classes.size()) + " classes retrieved"

#for c in [t]:
for c in classes:
  print c.toString() + " (" + c.getBrowserText() + "): ";
  sys.stdout.flush();

  supers = c.getSuperclasses();
  for s in supers:
    if isinstance(s, OWLIntersectionClass):
      print s;
      fillers = s.getOperands();
      print "fillers: " + fillers.toString();
      super = None;
      copyS = DefaultOWLIntersectionClass();  #(kb, None);
      print copyS;
      for f in fillers:
        if isinstance(f, OWLNamedClass):
          super = f;
        else:
          copyS.addOperand(f);
      if super is not None:
        print "Replace " + s.toString() + " with " + super + " AND ..." ;#+ copyS.toString();
        #c.addSuperclass(super);
        #c.addSuperclass(copyS);
        #c.removeSuperclass(s);
  sys.stdout.flush();
