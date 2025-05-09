/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.HUITypeFactory;

/**
 * This class is a crucial helper to make accessing certain beans (the work
 * objects) possible that once were singletons.
 * 
 * <p>
 * Note: Direct access to this class' methods is only for infrastructure classes
 * (e.g. generic superclasses etc.).
 * </p>
 * 
 * <p>
 * The problem that this class solves is the following: In earlier versions of
 * verinice the application was either run as server or client. Depending on the
 * startup certain classes where instantiated as singletons (e.g.
 * HUITypeFactory). The rest of the application frequently accessed those
 * singletons.
 * </p>
 * 
 * <p>
 * As of now the verinice server and client application can run inside the same
 * virtual machine. The effect was that the above mentioned singletons where
 * then always configured for the client since it was started last (former
 * objects were replaced).
 * </p>
 * 
 * <p>
 * Since the server and client functionality never share a common thread the
 * idea was to use a thread local variable to let the application either access
 * the beans that have been configured for the client or the ones that were
 * configured for the server.
 * </p>
 * 
 * <p>
 * This class wraps the access to the thread local variable and allows static
 * methods like {@link HUITypeFactory#getInstance()} to retrieve the correct
 * instance for the server or the client.
 * </p>
 * 
 * <p>
 * To make this work correctly a little bit of housekeeping is neccessary. Again
 * this class makes the housekeeping easy: Everytime code is executed on a new
 * (or foreign) thread you need to initialize the thread local variable. To do
 * this retrieve the so-called state of the <code>VeriniceContext</code> by
 * calling {@link #getState()} when you are still on a thread where the thread
 * local variable is already initialized. A suitable place for this is the
 * constructor of a worker class. When the processing in the new thread begins
 * put the state into the context by calling {@link #setState(State)}.
 * </p>
 * 
 * <p>
 * The context/thread local variable is initialized by the activator class of
 * the verinice client and by the <code>ServerInitializer</code> class of the
 * verinice server.
 * </p>
 * 
 * <p>
 * Note: The introduction of the thread-local variable is more a hack than a
 * solution. However doing it right would involve massive changes to the
 * applications design (many many classes would need to be managed by the Spring
 * IoC container).
 * </p>
 */
public final class VeriniceContext {

    private static final Logger LOG = Logger.getLogger(VeriniceContext.class);

    public static final String SYNC_SERVICE = "syncService";

    /** Key for accessing the <code>HUITypeFactory</code> instance. */
    public static final String HUI_TYPE_FACTORY = "huiTypeFactory";

    public static final String HITRO_UTIL = "hitroUtil";

    public static final String GS_SCRAPER_UTIL = "gsScraperUtil";

    public static final String COMMAND_SERVICE = "commandService";

    public static final String AUTH_SERVICE = "authService";

    public static final String TASK_SERVICE = "taskService";

    public static final String PROCESS_SERVICE_ISA = "processServiceIsa";

    public static final String ISA_CONTROL_FLOW_SERVICE = "isaControlFlowService";

    public static final String ISA_QM_SERVICE = "isaQmService";

    public static final String INDIVIDUAL_SERVICE = "individualService";

    public static final String GSM_SERVICE = "gsmService";

    public static final String RIGHTS_SERVICE = "rightsService";

    public static final String RIGHTS_SERVER_HANDLER = "rightsServerHandler";

    public static final String REMIND_SERVICE = "remindService";

    public static final String JBPM_PROCESS_ENGINE = "processEngine";

    public static final String VALIDATION_SERVICE = "validationService";

    public static final String COMMAND_CACHE_SERVICE = "commandCacheClient";

    public static final String ACCOUNT_SERVICE = "accountService";

    public static final String REPORT_DEPOSIT_SERVICE = "reportdepositService";

    public static final String REPORT_HQL_SERVICE = "reportHQLService";

    public static final String CONFIGURATION_SERVICE = "configurationService";

    public static final String SEARCH_SERVICE = "searchService";

    public static final String OBJECT_MODEL_SERVICE = "objectModelService";

    public static final String UPDATE_NEWS_SERVICE = "updateNewsService";

    public static final String RISK_ANALYSIS_SERVICE = "riskAnalysisService";

    public static final String ITBP_RISK_SERVICE = "itbpRiskService";

    public static final String LICENSE_SERVICE = "licenseManagementService";

    public static final String ENCRYPTION_SERVICE = "encryptionService";

    public static final String GRAPH_SERVICE = "graphService";

    public static final String COUNT_SERVICE = "countService";

    public static final String SESSION_FACTORY = "sessionFactory";

    private static String serverUrl = null;

    private ThreadLocal<Map<String, Object>> threadLocal = new InheritableThreadLocal<>();

    private static VeriniceContext instance;

    private VeriniceContext() {
    }

    /**
     * Transparently retrieves and initializes the thread local variable.
     * 
     * <p>
     * The initialization of the thread local variable is needed because the
     * initial value is <code>null</code> when a new thread accesses it.
     * </p>
     * 
     * @return
     */
    private static VeriniceContext instance() {
        synchronized (VeriniceContext.class) {
            if (instance == null) {
                instance = new VeriniceContext();
            }
        }

        synchronized (instance) {
            if (instance.threadLocal.get() == null) {
                instance.threadLocal.set(new HashMap<String, Object>());
            }
        }

        return instance;
    }

    /**
     * Retrieves a bean from the context. Use one of the public constants as
     * keys.
     * 
     * <p>
     * It is assumed that the requested object exists in the context since it
     * should behave like a singleton for the current thread. If this contract
     * is violated a <code>IllegalStateException</code> is thrown.
     * 
     * @param id
     * @return
     */
    public static Object get(String id) {
        Map<String, Object> map = instance().threadLocal.get();

        Object o = map.get(id);

        if (o == null) {
            // The object must exist. Since it does not this means the thread
            // local variable was not properly initialized.
            // If this happens in the verinice client code you need to call
            // Activator.inheritVeriniceContext() on the thread first.

            String msg = "Requested object '" + id
                    + "' was not available. The context was not properly configured for this thread yet.";
            IllegalStateException e = new IllegalStateException(msg);
            LOG.error(msg, e);
            throw e;
        }

        return o;
    }

    public static boolean exists(String id) {
        Map<String, Object> map = instance().threadLocal.get();
        return map.get(id) != null;
    }

    /**
     * Puts a bean into the context. Use one of the public constants as keys.
     * 
     * <p>
     * It is assumed that the object does not replace an existing one since it
     * should behave like a thread-local singleton. If this contract is violated
     * a <code>IllegalStateException</code> is thrown.
     * </p>
     * 
     * <p>
     * <code>null</code> values as objects are also not tolerated ...
     * </p>
     * 
     * @param id
     * @return
     */
    public static void put(String id, Object o) {
        if (o == null) {
            String msg = "Object for id '" + id + "' must not be null!";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Map<String, Object> map = instance().threadLocal.get();

        if (map.put(id, o) != null) {
            String msg = "Requested object '" + id
                    + "' was displaced. This is not allowed because it breaks the singleton contract.";
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Retrieves a snapshot (= copy) of the objects currently in the context.
     * 
     * <p>
     * Call this method to prepare the initialization of the context for a new
     * or foreign thread.
     * </p>
     * 
     * @return
     */
    public static State getState() {
        State s = new State();
        s.setMap(instance().threadLocal.get());

        LOG.debug("retrieving state in thread: " + Thread.currentThread().getName());

        return s;
    }

    /**
     * Puts the given work objects into the context.
     * 
     * <p>
     * Use this method when processing of a new or foreign thread starts and you
     * want to make sure that code that needs the work objects from this thread
     * can access them.
     * </p>
     * 
     * <p>
     * Note: This method allows violating the singleton contract of the
     * {@link #put(String, Object)} and {@link #get(String)} methods. However we
     * can't do much about this because we have to allow that the state can be
     * set multiple times on the same thread. This is needed because threads are
     * usually not under the applications control and instead are provided by
     * some container's thread pool.
     * </p>
     * 
     * @param s
     */
    public static void setState(State s) {
        instance().threadLocal.set(s.getMap());
    }

    public static String getServerUrl() {
        return serverUrl;
    }

    public static void setServerUrl(String url) {
        serverUrl = url;
    }

    /**
     * Abstraction of the state of a {@link VeriniceContext}.
     * 
     * <p>
     * The class can be created and modified by arbitrary code since the methods
     * are public. However doing so is strongly discouraged under normal
     * circumstances. The methods are only public to allow a creation through
     * the Spring configuration. This is what is considered a 'non-normal'
     * circumstance.
     * </p>
     */
    public static class State {
        private Map<String, Object> map;

        public void setMap(Map<String, Object> map) {
            this.map = new HashMap<>(map);
        }

        public Map<String, Object> getMap() {
            return new HashMap<>(map);
        }
    }

}
