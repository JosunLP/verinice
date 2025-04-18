/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.io.File;
import java.util.Calendar;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.RightEnabledUserInteraction;

public class AddFileActionDelegate implements IObjectActionDelegate, RightEnabledUserInteraction {

    private IWorkbenchPart targetPart;

    /*
     * @see
     * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
     * action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    /*
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        try {
            Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider()
                    .getSelection()).getFirstElement();
            if (sel instanceof CnATreeElement) {
                CnATreeElement element = (CnATreeElement) sel;
                FileDialog fd = new FileDialog(targetPart.getSite().getShell());
                fd.setText(Messages.AddFileActionDelegate_0);
                fd.setFilterPath(System.getProperty(IVeriniceConstants.USER_HOME)); // $NON-NLS-1$
                String selected = fd.open();
                if (selected != null && selected.length() > 0) {
                    File file = new File(selected);
                    if (file.isDirectory()) {
                        return;
                    }

                    Attachment attachment = new Attachment();
                    attachment.setCnATreeElement(element);
                    attachment.setCnAElementTitel(element.getTitle());
                    attachment.setTitel(file.getName());
                    attachment.setDate(Calendar.getInstance().getTime());
                    attachment.setFilePath(selected);
                    attachment.setFileSize(String.valueOf(file.length()));

                    attachment.addListener(() -> {
                        IViewPart page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                .getActivePage().findView(FileView.ID);
                        if (page != null) {
                            ((FileView) page).loadFiles();
                        }

                    });

                    EditorFactory.getInstance().openEditor(attachment);
                }
            }
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.AddFileActionDelegate_2);
        }

    }

    /*
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(checkRights() && isCnATreeElementEditable(selection));
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.ADDFILE;
    }

    private boolean isCnATreeElementEditable(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object element = structuredSelection.getFirstElement();
            if (element instanceof CnATreeElement) {
                return CnAElementHome.getInstance().isNewChildAllowed((CnATreeElement) element);
            }
        }
        return false;
    }

}
