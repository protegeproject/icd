# 
#  owl:Class
# http://who.int/icd#DefinitionSection
# http://who.int/icd#TermSection
# http://who.int/icd#LinearizationSection
# http://who.int/icd#ICHIPostcoordinationSection
# http://who.int/icd#ICHISection
# 
# Remove:	
# http://who.int/icd#ValueMetaClass
# http://who.int/icd#ICHIAxesSection
# 
# Add:
# http://who.int/icd#ICHIPostcoordinationSection
# http://who.int/icd#ICHISection
# 
# hi.removePropertyValue(type, valMetaclass);
# hi.removePropertyValue(type, ichiAxisSection);
# 
# 
# defSection = kb.getRDFSNamedClass("http://who.int/icd#DefinitionSection");
# termSection = kb.getRDFSNamedClass("http://who.int/icd#TermSection");
# linSection = kb.getRDFSNamedClass("http://who.int/icd#LinearizationSection");
#
#--------------------------------

hi = kb.getRDFSNamedClass("http://who.int/icd#HealthIntervention");
type = kb.getRDFProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
pcSection = kb.getRDFSNamedClass("http://who.int/icd#ICHIPostcoordinationSection");

ichiSection = kb.getRDFSNamedClass("http://who.int/icd#ICHISection");
valMetaclass = kb.getRDFSNamedClass("http://who.int/icd#ValueMetaClass");
ichiAxisSection = kb.getRDFSNamedClass("http://who.int/icd#ICHIAxesSection");

icdCode = kb.getRDFProperty("http://who.int/icd#icdCode");

hisc1 = hi.getDirectSubclasses();

hisc2 = [];
for c in hisc1:
	hisc2.extend(c.getDirectSubclasses());

hisc3 = [];
for c in hisc2:
	hisc3.extend(c.getDirectSubclasses());
	
#####

hi.addPropertyValue(type, ichiSection);

#####

for c in hisc1:
	c.removePropertyValue(type, valMetaclass);
	c.removePropertyValue(type, ichiAxisSection);
	c.addPropertyValue(type, ichiSection);
	c.addPropertyValue(type, pcSection);

for c in hisc2:
	c.removePropertyValue(type, valMetaclass);
	c.removePropertyValue(type, ichiAxisSection);
	c.addPropertyValue(type, ichiSection);
	c.addPropertyValue(type, pcSection);

cnt = 0;
for c in hisc3:
	if c.getPropertyValue(icdCode) is None:
		c.removePropertyValue(type, valMetaclass);
		c.removePropertyValue(type, ichiAxisSection);
		c.addPropertyValue(type, ichiSection);
		c.addPropertyValue(type, pcSection);
		cnt += 1;
print(str(cnt) + " level 3 Health Intervention groupings classes fixed");


# --------------------------------

#Incision of ventricle of brain
#iCAT-demo:
#newClasses = ["http://who.int/icd#31_c6f4e614_7cc9_4bae_9398_1ca4a9d4818e","http://who.int/icd#13_c6f4e614_7cc9_4bae_9398_1ca4a9d4818e"];
#iCAT:
newClasses = ["http://who.int/icd#582_100d7a13_7aa9_4d1e_8239_5b04c2ccdc9a"];
cnt = 0;
for clsName in newClasses:
    newCls = kb.getRDFSNamedClass(clsName);
    if (newCls is not None) and newCls.getPropertyValue(icdCode) is None:
        newCls.removePropertyValue(type, valMetaclass);
        newCls.removePropertyValue(type, ichiAxisSection);
        newCls.addPropertyValue(type, ichiSection);
        newCls.addPropertyValue(type, pcSection);
        cnt += 1;
print(str(cnt) + " new classes fixed");

