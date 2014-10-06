package edu.stanford.bmir.protegex.icd.lucene;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.query.api.QueryApi;
import edu.stanford.smi.protege.query.api.QueryConfiguration;
import edu.stanford.smi.protege.query.indexer.BrowserTextChanged;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;


/**
 * Updates the Lucene index when a new ICD category is being created.
 *
 * @author ttania
 *
 */
public class UpdateLuceneIndexProjectPlugin extends ProjectPluginAdapter {

    private FrameListener kbListener;

    @SuppressWarnings("deprecation")
    @Override
    public void afterLoad(Project p) {
        if (p.isMultiUserClient()) {
            return;
        }
        KnowledgeBase kb = p.getKnowledgeBase();
        if (! (kb instanceof OWLModel) ){
            return;
        }
        OWLModel owlModel = (OWLModel) kb;

        if (owlModel.getRDFProperty(ICDContentModelConstants.ICD_TITLE_PROP) == null ||
                owlModel.getRDFProperty(ICDContentModelConstants.SORTING_LABEL_PROP) == null ||
                owlModel.getRDFProperty(ICDContentModelConstants.LABEL_PROP) == null) {
            return; //most likely not a ICD project
        }

        //check if Lucene is there for this project
        //TODO: we need a nicer way of checking if lucene is there
        QueryConfiguration qConf = new QueryApi(owlModel).install();
        if (qConf == null) {
            return;
        }

        kbListener = getKbListener();
        owlModel.addFrameListener(kbListener);
    }

    private FrameListener getKbListener() {
        if (kbListener == null) {
            kbListener = new FrameAdapter() {
                @Override
                public void ownSlotValueChanged(FrameEvent event) {

                    Frame frame = event.getFrame();
                    if (frame == null) {
                        return;
                    }

                    OWLModel owlModel = (OWLModel) frame.getKnowledgeBase();

                    final RDFProperty icdTitleProp = owlModel.getRDFProperty(ICDContentModelConstants.ICD_TITLE_PROP);
                    final RDFProperty sortingLabelProp = owlModel.getRDFProperty(ICDContentModelConstants.SORTING_LABEL_PROP);
                    final RDFProperty labelProp = owlModel.getRDFProperty(ICDContentModelConstants.LABEL_PROP);

                    Frame changedFrame = null;
                    try {
                        Slot slot = event.getSlot();
                        if (slot.equals(sortingLabelProp) || slot.equals(icdTitleProp)) {
                            BrowserTextChanged.browserTextChanged(changedFrame = event.getFrame());
                        } else if (slot.equals(labelProp)) {
                            Collection<Reference> refs = event.getFrame().getReferences(1);
                            for (Reference ref : refs) {
                                if (ref.getSlot().equals(icdTitleProp)) {
                                    changedFrame = ref.getFrame();
                                    Cls defMetaCls = owlModel.getCls(ICDContentModelConstants.ICD_DEFINITION_METACLASS);
                                    if (((Instance)changedFrame).hasDirectType(defMetaCls)) {
                                        BrowserTextChanged.browserTextChanged(changedFrame);
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        Log.getLogger().log(Level.WARNING, "Error at updating the Lucene index for: " + changedFrame);
                    }
                }
            };
        }
        return kbListener;
    }

    @Override
    public void beforeClose(Project p) {
        if (kbListener != null) {
            try {
                p.getKnowledgeBase().removeFrameListener(kbListener);
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Could not remove frame listener for updating the Lucene indices.");
            }
        }
    }

}
