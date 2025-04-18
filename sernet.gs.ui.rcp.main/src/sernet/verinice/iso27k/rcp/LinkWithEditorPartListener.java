/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * LinkWithEditorPartListener links an view which implements
 * {@link ILinkedWithEditorView} to the content of an editor.
 * 
 * Use this interface together with
 * {@sernet.verinice.iso27k.rcp.ILinkedWithEditorView} as follows:
 * 
 * public class MyView extends ViewPart implements ILinkedWithEditorView { ...
 * IPartListener2 linkWithEditorPartListener = new
 * LinkWithEditorPartListener(this); ... public void createPartControl(Composite
 * parent) { ...
 * getSite().getPage().addPartListener(linkWithEditorPartListener); ... } ...
 * public void editorActivated(IEditorPart editor) { // update view with content
 * from editor ... } ... }
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkWithEditorPartListener implements IPartListener2 {
    private static final Logger LOG = Logger.getLogger(LinkWithEditorPartListener.class);
    /**
     * 
     */
    private final ILinkedWithEditorView view;

    /**
     * Creates a new LinkWithEditorPartListener
     * 
     * @param view
     *            An RCP view
     */
    public LinkWithEditorPartListener(ILinkedWithEditorView view) {
        this.view = view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partActivated(IWorkbenchPartReference ref) {
        if (ref.getPart(true) instanceof IEditorPart) {
            if(this.view.getViewSite().getPage() == null) {
                LOG.info("partActivated ---> page is null "+this.view.getViewSite());
                return;
            }
            
            IEditorPart editor = this.view.getViewSite().getPage().getActiveEditor();
            Optional.ofNullable(editor).ifPresent(view::editorActivated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partBroughtToTop(IWorkbenchPartReference ref) {
        refreshLinkedState(ref);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partOpened(IWorkbenchPartReference ref) {
        refreshLinkedState(ref);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partVisible(IWorkbenchPartReference ref) {
        refreshLinkedState(ref);
    }

    private void refreshLinkedState(IWorkbenchPartReference ref) {
        // This refreshes the linked state on startup and delayed opening
        if (ref.getId().equals(this.view.getViewSite().getId())) {
            if(this.view.getViewSite().getPage() == null) {
                LOG.info("refreshLinkedState ---> page is null "+this.view.getViewSite());
                return;
            }
            
            IEditorPart editor = view.getViewSite().getPage().getActiveEditor();
            if (editor != null) {
                if (ref.getPart(true) == this.view) {
                    view.editorActivated(editor);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partClosed(IWorkbenchPartReference ref) {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partDeactivated(IWorkbenchPartReference ref) {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partHidden(IWorkbenchPartReference ref) {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.
     * IWorkbenchPartReference)
     */
    @Override
    public void partInputChanged(IWorkbenchPartReference ref) {
        // nothing to do
    }

}