
"""
startICDExport

Export selected ICD content in tab-delimited format

Usage Examples:
topNodes = ["http://who.int/icd#XII"]
startICDExport(topNodes, "/Users/tu/Dropbox/WHO/iCAT/spreadsheet/chapter12.txt")
startICDExport(topNodes, "C:\My Dropbox\WHO\iCAT\spreadsheet\chapter17.txt")

Required declarations:

"""
from time import *
import string
from sets import Set

seenClsSet = Set()
fsnIndex=11
typeIndex=12
sortLabelIndex=13
OriginalParentsIndex=14
icd10CodeIndex=15
definitionIndex=16
detailedDefinitionIndex=definitionIndex+1
synonymIndex = detailedDefinitionIndex+1
inclusionIndex=synonymIndex+1
exclusionIndex=inclusionIndex+1
includeLateralityIndex=exclusionIndex+1
bodySystemIndex=includeLateralityIndex+1
bodyPartIndex=bodySystemIndex+1
bodyPartSNMDCTIndex=bodyPartIndex+1
pathohistologyIndex=bodyPartSNMDCTIndex+1
pathohistologySNMDCTIndex=pathohistologyIndex+1
tempModIndex=pathohistologySNMDCTIndex+1
tempModDefIndex=tempModIndex+1
severityModIndex=tempModDefIndex+1
severityModDefIndex=severityModIndex+1
notesIndex=severityModDefIndex+1
classNameIndex=notesIndex+1

pathoPhysioProp=kb.getRDFProperty("http://who.int/icd#morphologicallyAbnormalStructure")
lateralityProp = kb.getRDFProperty("http://who.int/icd#laterality")
bodyPartProp=kb.getRDFProperty("http://who.int/icd#bodyPart")
bodySystemProp=kb.getRDFProperty("http://who.int/icd#bodySystem")
typeProp=kb.getRDFProperty("http://who.int/icd#hasType")
sortLabelProp=kb.getRDFProperty("http://who.int/icd#sortingLabel")
titleProp=kb.getRDFProperty("http://who.int/icd#icdTitle")
codeProp=kb.getRDFProperty("http://who.int/icd#icdCode")
labelProp=kb.getRDFProperty("http://who.int/icd#label")
definitionProp=kb.getRDFProperty("http://who.int/icd#definition")
linViewProp=kb.getRDFProperty("http://who.int/icd#linearizationView")
parentsProp=kb.getRDFProperty("http://who.int/icd#linearizationParent")
linProp =kb.getRDFProperty("http://who.int/icd#linearization")
bpShortTermId=kb.getRDFProperty("http://bioportal.bioontology.org#shortTermId")
exclusionProp=kb.getRDFProperty("http://who.int/icd#exclusion")
inclusionProp=kb.getRDFProperty("http://who.int/icd#inclusion")
synonymProp=kb.getRDFProperty("http://who.int/icd#synonym")


idRow=["Retired\t", "ICD category\t","\t","\t","\t","\t","\t","\t","\t","\t","\t","Fully specified title\t", "Sort label\t",
       "Type\t", "Original Parent\t", "ICD code\t", "Textual Definition\t", "Detailed Definition\t", "Synonyms\t", "Legacy Inclusion Terms(For information only)\t", "Legacy Exclusion Terms (For information only)\t","Does laterality apply?\t","Body System\t","Body Part\t",
       "Body Part SNOMEDCT Code\t", "Histopathology\t", "Histopathology SNOMEDCT Code\t","Temporal Modifier\t", 
       "Temporal Modifier Definition\t", "Severity Term\t", "Severity Term Definition\t",  "Authoring Note to be imported into iCAT\t", 
       "Class name (Internal)"]  


def exportICD(icdCls, subclassLevel, output):
    row=["\t","\t","\t","\t","\t","\t","\t","\t","\t","\t",
         "\t","\t","\t","\t","\t","\t","\t","\t","\t","\t",
         "\t","\t","\t","\t","\t","\t","\t","\t","\t","\t",
         "\t", "\t", "\t"]
    row[subclassLevel]=makeEntry(filterLFTAB(getItemName(icdCls, titleProp, labelProp, 1)))
    row[icd10CodeIndex]=makeEntry(getItemName(icdCls, codeProp, 0, 0))
    row[classNameIndex]=icdCls.getName()+"\t"
    if icdCls.getName() not in seenClsSet:
        row[sortLabelIndex]=makeEntry(getItemName(icdCls, sortLabelProp, 0, 0))
        row[typeIndex] = makeEntry(getItemName(icdCls, typeProp, labelProp, 0))
        definition =makeEntry(filterLFTAB(getItemName(icdCls, definitionProp, labelProp, 1)))
        words = string.split(definition)
        if len(words) > 100:
            row[detailedDefinitionIndex] = definition
        else:
            row[definitionIndex] = definition
        row[synonymIndex] = makeEntry(getMultipleItemNames(icdCls, synonymProp, labelProp, 1))
        row[bodySystemIndex] = makeEntry(getMultipleItemNames(icdCls, bodySystemProp, labelProp, 1))
        row[bodyPartIndex] = makeEntry(getMultipleItemNames(icdCls, bodyPartProp, labelProp, 0))
        row[bodyPartSNMDCTIndex] = makeEntry(getMultipleItemNames(icdCls, bodyPartProp, bpShortTermId, 0))
        row[pathohistologyIndex] = makeEntry(getMultipleItemNames(icdCls, pathoPhysioProp, labelProp, 0))
        row[pathohistologySNMDCTIndex] = makeEntry(getMultipleItemNames(icdCls, pathoPhysioProp, bpShortTermId, 0))
        row[exclusionIndex]=makeEntry(getMultipleItemNames(icdCls, exclusionProp, labelProp, 1))
        row[inclusionIndex]=makeEntry(getMultipleItemNames(icdCls, inclusionProp, labelProp, 1))
        if (icdCls.getSuperclassCount()> 1):
            row[OriginalParentsIndex] =getAllParents(icdCls)
    filter(lambda x: output.write(x), row)
    output.write("\n")
    seenClsSet.add(icdCls.getName())
    subClasses=icdCls.getNamedSubclasses(0)
    pairs = [(getItemName(x, sortLabelProp, 0, 0), x) for x in subClasses]
    pairs.sort()
    sortedSubclasses = [x[1] for x in pairs] 
    for cls in sortedSubclasses:
        exportICD(cls, subclassLevel+1, output)

def makeEntry(item):
    return item + "\t"

    #===========================================================================
    # Get the value of the itemProp property. If labelProp is not null, find 
    # the labelProp value of the itemProp value. If the labelProp does not have
    # a value, then use the localName of the RDF resource, adding an alert flag
    # if the alert parameter is not null. 
    #===========================================================================
def getItemName(icdCls, itemProp, labelProp, alert):
    item=icdCls.getPropertyValue(itemProp)
    if item != None:
        # check to see if item is an instance
        if (labelProp):
            itemName = item.getPropertyValue(labelProp)
            if itemName == None:
                if alert:
                    itemName = item.getLocalName()+" has no label"
                else:
                    itemName = item.getLocalName()
        else:
            itemName=item
        return itemName
    return ""



#   Similar to getItemName, except that itemProp property may have multiple values. Separate values by
#   the "|" separator.

def getMultipleItemNames(icdCls, itemProp, labelProp, alert):
    items=icdCls.getPropertyValues(itemProp)
    itemNames=""
    if items != None:
        first=1
        for item in items:
            if (labelProp):
                itemName = item.getPropertyValue(labelProp)
                if itemName == None:
                    if alert:
                        itemName = item.getLocalName()+" has no label"
                    else:
                        itemName = item.getLocalName()
            if first:
                itemNames=itemName
                first=0
            else:
                itemNames=itemNames+" || "+itemName
        return filterLFTAB(itemNames)
    return ""


def getLinearizationParent(icdCls, linear):
    icdClsLinears= icdCls.getPropertyValues(linProp)
    for eachL in icdClsLinears:
        linearView=eachL.getPropertyValue(linViewProp)
        labels=linearView.getLabels()
        for label in labels:
            if (label == linear):
                linParent = eachL.getPropertyValue(linParentProp)
                if linParent:
                    return getItemName(linParent, titleProp, labelProp, 1)
                else:
                    return None

def getAllParents(icdCls):
    supers=icdCls.getNamedSuperclasses(0)
    first=1
    for sup in supers:
        title = getItemName(sup, titleProp, labelProp, 1)
        if first:
            linParent=title
            first=0
        else:
            linParent=linParent+" || "+title
    return filterLFTAB(linParent)+"\t"


def startICDExport(clsNameList, fileName):
    seenClsSet.clear()
    output = open(fileName, 'w')
    output.write("Export timestamp:\t"+strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime())+"\n")
    filter(lambda x: output.write(x), idRow)
    output.write("\n")
    try:
        for clsName in clsNameList:
            cls = kb.getOWLNamedClass(clsName)
            if cls != None:
                exportICD(cls, 1, output)
    finally:
        output.close()
        seenClsSet.clear()


def checkLFTAB(c):
    #Filter LF and TAB
    if ((c != chr(10)) and (c != chr(9))):
        return c

def filterLFTAB(str):
    return filter(checkLFTAB, str).strip()


