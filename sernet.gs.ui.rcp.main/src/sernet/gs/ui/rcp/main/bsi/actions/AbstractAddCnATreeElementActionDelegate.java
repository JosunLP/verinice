package sernet.gs.ui.rcp.main.bsi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;

import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.RightEnabledUserInteraction;

public abstract class AbstractAddCnATreeElementActionDelegate
        implements IObjectActionDelegate, RightEnabledUserInteraction {

    /*
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.
     * IAction, org.eclipse.jface.viewers.ISelection)
     */
    public final void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(checkRights());
        // Realizes that the action to create a new element is greyed out,
        // when there is no right to do so.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (sel instanceof CnATreeElement) {
            boolean b = CnAElementHome.getInstance().isNewChildAllowed((CnATreeElement) sel);
            // Only change state when it is enabled, since we do not want to
            // trash the enablement settings of plugin.xml
            if (action.isEnabled()) {
                action.setEnabled(b);
            }
        }
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.ADDBSIELEMENT;
    }

}
