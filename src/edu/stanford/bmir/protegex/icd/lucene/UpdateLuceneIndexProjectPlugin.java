package edu.stanford.bmir.protegex.icd.lucene;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.query.indexer.BrowserTextChanged;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;


public class UpdateLuceneIndexProjectPlugin extends ProjectPluginAdapter {

    private FrameListener kbListener;
    private ICDContentModel cm;

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
        cm = new ICDContentModel(owlModel);
        if (cm.getIcdTitleProperty() == null) { //most likely not a ICD project
            return;
        }

        kbListener = getKbListener(owlModel);
        owlModel.addFrameListener(kbListener);
    }

    private FrameListener getKbListener(final OWLModel owlModel) {
        if (kbListener == null) {
            kbListener = new FrameAdapter() {
                @Override
                public void ownSlotValueChanged(FrameEvent event) {
                    Slot slot = event.getSlot();
                    if (slot.equals(cm.getIcdTitleProperty()) || slot.equals(cm.getSortingLabelProperty())) {
                        BrowserTextChanged.browserTextChanged(event.getFrame());
                    }
                };
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
