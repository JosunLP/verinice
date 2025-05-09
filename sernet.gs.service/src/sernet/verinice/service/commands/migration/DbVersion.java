/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.migration;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.VersionConstants;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.service.model.LoadModel;

/**
 * Version check.
 * 
 * Compares client version to server and to compatible DB version.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class DbVersion extends GenericCommand {

    private static final Logger log = Logger.getLogger(DbVersion.class);

    private double clientVersion;

    public DbVersion(double clientVersion) {
        this.clientVersion = clientVersion;
    }

    /**
     * Update DB version to compatible DB version.
     * 
     * @param dbVersion
     * @throws CommandException
     */
    public void updateDBVersion(double dbVersion) throws CommandException {
        final double maxPercent = 100.0;
        // round
        dbVersion = Math.round(dbVersion * maxPercent) / maxPercent;
        if (log.isDebugEnabled()) {
            log.debug("updateDBVersion, current version is: " + dbVersion);
        }
        executeUpdateCommand(dbVersion);
    }

    private void executeUpdateCommand(double dbVersion) throws CommandException {

        if (dbVersion < 0.94D) {
            DbMigration migration = new MigrateDbTo0_94();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 0.95D) {
            DbMigration migration = new MigrateDbTo0_95();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 0.96D) {
            // schema update must have been done by SchemaCreator.java, before
            // Hibernate session was started:
            log.debug("Database schema was not correctly updated to V 0.96.");
            throw new CommandException("Datenbank konnte nicht auf V0.96 upgedated werden.");
        }
        if (dbVersion < 0.97D) {
            DbMigration migration = new MigrateDbTo0_97();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 0.98D) {
            DbMigration migration = new MigrateDbTo0_98();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 0.99D) {
            DbMigration migration = new MigrateDbTo0_99();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.00D) {
            DbMigration migration = new MigrateDbTo1_00D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.01D) {
            DbMigration migration = new MigrateDbTo1_01D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.02D) {
            DbMigration migration = new MigrateDbTo1_02D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.03D) {
            DbMigration migration = new MigrateDbTo1_03D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.04D) {
            DbMigration migration = new MigrateDbTo1_04D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.05D) {
            DbMigration migration = new MigrateDbTo1_05D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.07D) {
            DbMigration migration = new MigrateDbTo1_07D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.08D) {
            DbMigration migration = new MigrateDbTo1_08D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.09D) {
            DbMigration migration = new MigrateDbTo1_09D();
            getCommandService().executeCommand(migration);
        }
        if (dbVersion < 1.10D) {
            DbMigration migration = new MigrateDbTo1_10D();
            getCommandService().executeCommand(migration);
        }
    }

    public void execute() {
        final double tolerableDiff = 0.01;
        if (Math.abs(clientVersion - VersionConstants.COMPATIBLE_CLIENT_VERSION) >= tolerableDiff) {
            throw new RuntimeCommandException("Inkompatible Client Version. "
                    + "Server akzeptiert nur V " + VersionConstants.COMPATIBLE_CLIENT_VERSION
                    + ". Vorhandene Client Version: " + clientVersion);
        }

        LoadModel<BSIModel> command = new LoadModel<>(BSIModel.class);
        double dbVersion;
        try {
            command = getCommandService().executeCommand(command);
            if (command.getModel() == null) {
                Logger.getLogger(this.getClass()).debug(
                        "Not migrating database: could not determine current database version or no database created yet.");
                return;
            }
            // set current db version as determined from database:
            dbVersion = command.getModel().getDbVersion();
            updateDBVersion(dbVersion);
        } catch (CommandException e) {
            log.error("Exception while updating database.", e);
            throw new RuntimeCommandException(
                    "Fehler beim Migrieren der Datenbank auf aktuelle Version.", e);
        }
    }
}
