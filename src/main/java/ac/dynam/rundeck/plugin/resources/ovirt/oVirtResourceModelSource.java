/*
 * Copyright 2014 Dynamx (Singapore) Pte Ltd
 * 
 * Based on the RunDeck EC2 Plugin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ac.dynam.rundeck.plugin.resources.ovirt;


import com.dtolabs.rundeck.core.common.*;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import org.apache.log4j.Logger;

import java.util.*;

public class oVirtResourceModelSource implements ResourceModelSource {
    static Logger logger = Logger.getLogger(oVirtResourceModelSource.class);
    private String ovirtURL;
    private String ovirtUser;
    private String ovirtPass;
    long refreshInterval = 30000;
    long lastRefresh = 0;
    boolean runningOnly = true;

    oVirtSDKWrapper ovirtserver = new oVirtSDKWrapper();

    
    INodeSet iNodeSet;
    InstanceToNodeMapper mapper;

    public oVirtResourceModelSource(final Properties configuration) {
        this.ovirtURL = configuration.getProperty(oVirtResourceModelSourceFactory.OVIRT_URL);
        this.ovirtUser = configuration.getProperty(oVirtResourceModelSourceFactory.OVIRT_USER);
        this.ovirtPass = configuration.getProperty(oVirtResourceModelSourceFactory.OVIRT_PASS);
        
        int refreshSecs = 30;
        final String refreshStr = configuration.getProperty(oVirtResourceModelSourceFactory.REFRESH_INTERVAL);
        if (null != refreshStr && !"".equals(refreshStr)) {
            try {
                refreshSecs = Integer.parseInt(refreshStr);
            } catch (NumberFormatException e) {
                logger.warn(oVirtResourceModelSourceFactory.REFRESH_INTERVAL + " value is not valid: " + refreshStr);
            }
        }
        refreshInterval = refreshSecs * 1000;
        if (configuration.containsKey(oVirtResourceModelSourceFactory.RUNNING_ONLY)) {
            runningOnly = Boolean.parseBoolean(configuration.getProperty(
                oVirtResourceModelSourceFactory.RUNNING_ONLY));
        }
    
        
        
        initialize();
    }

    private void initialize() {
        mapper = new InstanceToNodeMapper(ovirtserver);
        mapper.setRunningStateOnly(runningOnly);
    }


    public synchronized INodeSet getNodes() throws ResourceModelSourceException {
        if (!needsRefresh()) {
            if (null != iNodeSet) {
                logger.info("Returning " + iNodeSet.getNodeNames().size() + " cached nodes from oVirt");
            }
            return iNodeSet;
        }
        iNodeSet = mapper.performQuery();
        lastRefresh = System.currentTimeMillis();
        if (null != iNodeSet) {
            logger.info("Read " + iNodeSet.getNodeNames().size() + " nodes from oVirt");
        }
        return iNodeSet;
    }

    /**
     * Returns true if the last refresh time was longer ago than the refresh interval
     */
    private boolean needsRefresh() {
        return refreshInterval < 0 || (System.currentTimeMillis() - lastRefresh > refreshInterval);
    }

    public void validate() throws ConfigurationException {
        if (null == ovirtURL || null == ovirtUser || null == ovirtPass) {
            throw new ConfigurationException("URL, Username and Password are Required: " + ovirtURL + " " + ovirtUser + " " + ovirtPass);
        }
       	this.ovirtserver.login(ovirtURL, ovirtUser, ovirtPass);
       	if (!this.ovirtserver.isLoggedin())
       		throw new ConfigurationException(this.ovirtserver.getMessage());
    }
}
