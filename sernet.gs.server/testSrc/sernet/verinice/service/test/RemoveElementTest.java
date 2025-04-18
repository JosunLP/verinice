/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.bp.LoadBpModel;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.LoadConfiguration;
import sernet.verinice.service.commands.LoadElementById;
import sernet.verinice.service.commands.LoadElementByTypeId;
import sernet.verinice.service.commands.LoadElementsByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.model.LoadModel;
import sernet.verinice.service.sync.VeriniceArchive;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class RemoveElementTest extends CommandServiceProvider {

    private ITVerbund itVerbund;

    private Organization organization;

    private ItNetwork itNetwork;

    private static final String VNA_FILE = "RemoveElementTest.vna";

    @Before
    public void setUp() throws CommandException {
        LoadModel<BSIModel> loadBSIModel = new LoadModel<>(BSIModel.class);
        BSIModel bSIModel = commandService.executeCommand(loadBSIModel).getModel();
        itVerbund = createElement(ITVerbund.class, bSIModel);

        LoadModel<ISO27KModel> loadISO27Model = new LoadModel<>(ISO27KModel.class);
        ISO27KModel iSO27Model = commandService.executeCommand(loadISO27Model).getModel();
        organization = createElement(Organization.class, iSO27Model);

        LoadBpModel loadBpModel = new LoadBpModel();
        BpModel bpModel = commandService.executeCommand(loadBpModel).getModel();
        itNetwork = createElement(ItNetwork.class, bpModel);
    }

    @Test
    public void removeITVerbund() throws CommandException {
        executeRemoveCommand(itVerbund);
        assertElementIsDeleted(itVerbund);
    }

    @Test
    public void removeOrganization() throws CommandException {
        executeRemoveCommand(organization);
        assertElementIsDeleted(organization);
    }

    @Test
    public void removeKategorieFromVerbund() throws CommandException {
        GebaeudeKategorie gebaeudeKategorie = createElement(GebaeudeKategorie.class, itVerbund);
        executeRemoveCommand(gebaeudeKategorie);
        assertElementIsDeleted(gebaeudeKategorie);
    }

    @Test
    public void removeElementFromKategorie() throws CommandException {
        GebaeudeKategorie gebaeudeKategorie = createElement(GebaeudeKategorie.class, itVerbund);
        Gebaeude gebaeude = createElement(Gebaeude.class, gebaeudeKategorie);
        executeRemoveCommand(gebaeude);
        assertElementIsDeleted(gebaeude);
    }

    @Test
    public void removeParentKategorie() throws CommandException {
        GebaeudeKategorie gebaeudeKategorie = createElement(GebaeudeKategorie.class, itVerbund);
        Gebaeude gebaeude = createElement(Gebaeude.class, gebaeudeKategorie);
        executeRemoveCommand(gebaeudeKategorie);
        assertElementIsDeleted(gebaeude);
    }

    @Test
    public void removeParentElementFromGroup() throws CommandException {
        for (Entry<String, Class<? extends CnATreeElement>> entry : GROUP_TYPE_MAP.entrySet()) {
            CnATreeElement element = createElement(entry.getValue(), organization);

            int documents = new SecureRandom().nextInt(20);
            List<Document> documentList = new ArrayList<Document>(0);
            for (int i = documents; i > 0; i--) {
                createElement(Document.class, element);
            }
            RemoveElement removeCommand = executeRemoveCommand(element);

            for (Document doc : documentList) {
                assertElementIsDeleted(doc);
            }
            assertElementIsDeleted(element);
        }
    }

    @Test
    public void removePersonWithAccount() throws CommandException {
        PersonenKategorie personenKategorie = createElement(PersonenKategorie.class, itVerbund);
        Person person = createElement(Person.class, personenKategorie);

        Configuration configuration = commandService.executeCommand(new CreateConfiguration(person))
                .getConfiguration();
        executeRemoveCommand(person);
        assertElementIsDeleted(person);

        configuration = commandService.executeCommand(new LoadConfiguration(person))
                .getConfiguration();
        assertNull(configuration);
    }

    @Test
    public void removeIsoPersonWithAccount() throws CommandException {
        PersonGroup personGroup = createElement(PersonGroup.class, organization);
        PersonIso person = createElement(PersonIso.class, personGroup);

        Configuration configuration = commandService.executeCommand(new CreateConfiguration(person))
                .getConfiguration();
        executeRemoveCommand(person);
        assertElementIsDeleted(person);

        configuration = commandService.executeCommand(new LoadConfiguration(person))
                .getConfiguration();
        assertNull(configuration);
    }

    @Test
    public void removeBpPersonWithAccount() throws CommandException {
        BpPersonGroup bpPersonGroup = createElement(BpPersonGroup.class, itNetwork);
        BpPerson person = createElement(BpPerson.class, bpPersonGroup);

        Configuration configuration = commandService.executeCommand(new CreateConfiguration(person))
                .getConfiguration();
        executeRemoveCommand(person);
        assertElementIsDeleted(person);

        configuration = commandService.executeCommand(new LoadConfiguration(person))
                .getConfiguration();
        assertNull(configuration);
    }

    @Test
    public void removeElements() throws CommandException {
        RemoveElement removeCommand = new RemoveElement(itVerbund, organization);
        commandService.executeCommand(removeCommand);

        assertElementIsDeleted(itVerbund);
        assertElementIsDeleted(organization);
    }

    @Test
    public void removeVerbundFromImportedBSIVerbund() throws CommandException {
        LoadModel<BSIModel> loadBSIModel = new LoadModel<>(BSIModel.class);
        BSIModel bSIModel = commandService.executeCommand(loadBSIModel).getModel();

        ImportBsiGroup importBsiGroup = createElement(ImportBsiGroup.class, bSIModel);
        ITVerbund itVerbund = createElement(ITVerbund.class, importBsiGroup);

        executeRemoveCommand(itVerbund);
        assertElementIsDeleted(itVerbund);
    }

    /**
     * {@link ImportBsiGroup} and {@link ImportIsoGroup} are not deleted when
     * they imported from a {@link VeriniceArchive}.
     */
    @Test
    public void removeVNAImportedBSIVerbund()
            throws IOException, CommandException, SyncParameterException {
        String path = getClass().getResource(VNA_FILE).getPath();
        SyncParameter syncParameter = new SyncParameter(true, true, true, false,
                SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        byte[] it_network_vna = FileUtils.readFileToByteArray(new File(path));

        SyncCommand syncCommand = new SyncCommand(syncParameter, it_network_vna);
        syncCommand = commandService.executeCommand(syncCommand);
        Set<String> importedElementUUIDs = syncCommand.getImportedElementUUIDs();
        LoadElementsByUuid<CnATreeElement> elementLoader = new LoadElementsByUuid<>(
                new ArrayList<>(importedElementUUIDs), new RetrieveInfo());
        elementLoader = commandService.executeCommand(elementLoader);
        Set<CnATreeElement> elements = elementLoader.getElements();

        for (CnATreeElement element : elements) {
            if (element instanceof ITVerbund) {
                element = loadElement(element.getSourceId(), element.getExtId());
                executeRemoveCommand((ITVerbund) element);
                assertElementIsDeleted(element);
            } else if (element instanceof Organization) {
                element = loadElement(element.getSourceId(), element.getExtId());
                executeRemoveCommand((Organization) element);
                assertElementIsDeleted(element);
            }
        }

    }

    private void assertElementIsDeleted(CnATreeElement element) throws CommandException {
        LoadElementById<CnATreeElement> command = new LoadElementById<CnATreeElement>(
                element.getDbId());
        command = commandService.executeCommand(command);
        CnATreeElement loadByUUid = command.getElement();
        assertNull("element " + element.getUuid() + " was not deleted.", loadByUUid);
    }

    private <T extends CnATreeElement> T createElement(Class<T> type, CnATreeElement element)
            throws CommandException {
        CreateElement<T> createElementCommand = new CreateElement<T>(element, type,
                RemoveElementTest.class.getSimpleName() + " [" + UUID.randomUUID() + "]");
        createElementCommand = commandService.executeCommand(createElementCommand);

        return createElementCommand.getNewElement();
    }

    private <T extends CnATreeElement> RemoveElement executeRemoveCommand(T element)
            throws CommandException {
        RemoveElement removeCommand = new RemoveElement(element);
        return commandService.executeCommand(removeCommand);
    }

    private void removeAllElementsByType(String type) throws CommandException {
        LoadElementByTypeId loadElementByTypeId = new LoadElementByTypeId(type);
        loadElementByTypeId = commandService.executeCommand(loadElementByTypeId);

        for (CnATreeElement element : loadElementByTypeId.getElementList()) {
            executeRemoveCommand(element);
        }
    }

    @After
    public void tearDown() throws CommandException {
        executeRemoveCommand(itVerbund);
        executeRemoveCommand(organization);
        executeRemoveCommand(itNetwork);

        // clean up the parents of imported cnatreeelements
        removeAllElementsByType(ImportBsiGroup.TYPE_ID);
        removeAllElementsByType(ImportIsoGroup.TYPE_ID);

        this.itVerbund = null;
        this.organization = null;
        this.itNetwork = null;
    }
}
