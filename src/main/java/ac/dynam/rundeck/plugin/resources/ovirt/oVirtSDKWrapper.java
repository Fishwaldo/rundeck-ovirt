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


import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.ovirt.engine.sdk.Api;
import org.ovirt.engine.sdk.ApiBuilder;
import org.ovirt.engine.sdk.decorators.VM;
import org.ovirt.engine.sdk.entities.Action;
import org.ovirt.engine.sdk.exceptions.ServerException;
import org.ovirt.engine.sdk.exceptions.UnsecuredConnectionAttemptError;

public class oVirtSDKWrapper {
    private Api api;
    private String message;
    static Logger logger = Logger.getLogger(oVirtSDKWrapper.class);

    public void login(String baseUrl, String userName, String password) {
        try {
            // true for filter, ie enable regular users to login
            // this.api = new Api(baseUrl, userName, password, true);
        	if (!isLoggedin()) 
        	{
        		this.api = new ApiBuilder()
        			.url(baseUrl)
        		     .user(userName)
        		     .password(password)
        		     .debug(false)
        		     .noHostVerification(true)
        		     .build();
        		logger.debug("Processing....");
        		if (isLoggedin()) {
        			logger.info("Successfully Logged into oVirt: " + baseUrl);
        		} else {
        			logger.warn("Can't Log into oVirt: " + baseUrl);
        		}
        	}
   
        } catch (ClientProtocolException e) {
            this.message = "Protocol Exception: " + e.getMessage();
        } catch (ServerException e) {
            this.message = "Server Exception: " + e.getMessage();
        } catch (UnsecuredConnectionAttemptError e) {
            this.message = "Unsecured Connection Exception: " + e.getMessage();
        } catch (IOException e) {
            this.message = "IOException Exception: " + e.getMessage();
        }
    } 

    public boolean isLoggedin() {
        return this.api != null;
    }

    public String getMessage() {
        return this.message;
    }

    public List<VM> getVms() {
        try {
            return this.api.getVMs().list();
        } catch (ClientProtocolException e) {
            this.message = "Protocol Exception: " + e.getMessage();
        } catch (ServerException e) {
            this.message = "Server Exception: " + e.getMessage();
        } catch (IOException e) {
            this.message = "IOException Exception: " + e.getMessage();
        }
        return null;
    }

    public VM getVmById(String vmid) {
        try {
            return this.api.getVMs().get(UUID.fromString(vmid));
        } catch (ClientProtocolException e) {
            this.message = "Protocol Exception: " + e.getMessage();
        } catch (ServerException e) {
            this.message = "Server Exception: " + e.getMessage();
        } catch (IOException e) {
            this.message = "IOException Exception: " + e.getMessage();
        }
        return null;
    }

    public Action startVm(String vmid) {
        try {
            VM vm = this.api.getVMs().get(UUID.fromString(vmid));
            return vm.start(new Action());
        } catch (ClientProtocolException e) {
            this.message = "Protocol Exception: " + e.getMessage();
        } catch (ServerException e) {
            this.message = "Server Exception: " + e.getReason() + ": " + e.getDetail();
        } catch (IOException e) {
            this.message = "IOException Exception: " + e.getMessage();
        }
        return null;
    }

    public Action stopVm(String vmid) {
        try {
            VM vm = this.api.getVMs().get(UUID.fromString(vmid));
            return vm.stop(new Action());
        } catch (ClientProtocolException e) {
            this.message = "Protocol Exception: " + e.getMessage();
        } catch (ServerException e) {
            this.message = "Server Exception: " + e.getReason() + ": " + e.getDetail();
        } catch (IOException e) {
            this.message = "IOException Exception: " + e.getMessage();
        }
        return null;
    }

    public Action ticketVm(String vmid) {
        try {
            VM vm = this.api.getVMs().get(UUID.fromString(vmid));
            return vm.ticket(new Action());
        } catch (ClientProtocolException e) {
            this.message = "Protocol Exception: " + e.getMessage();
        } catch (ServerException e) {
            this.message = "Server Exception: " + e.getReason() + ": " + e.getDetail();
        } catch (IOException e) {
            this.message = "IOException Exception: " + e.getMessage();
        }
        return null;
    }
}

