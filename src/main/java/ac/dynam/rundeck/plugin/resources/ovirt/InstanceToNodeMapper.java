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


import org.ovirt.engine.sdk.decorators.VM;
import org.ovirt.engine.sdk.decorators.VMTags;
import org.ovirt.engine.sdk.exceptions.ServerException;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.common.NodeSetImpl;

import ac.dynam.rundeck.plugin.resources.ovirt.oVirtSDKWrapper;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


/**
 * InstanceToNodeMapper produces Rundeck node definitions from ovirt Instances
 * @author Justin Hammond
 * @author Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 */
class InstanceToNodeMapper {
	static final Logger logger = Logger.getLogger(InstanceToNodeMapper.class);
	final oVirtSDKWrapper ovirtSDK;
	private boolean runningStateOnly = true;

	/**
	 * Create with the credentials and mapping definition
	 */
	InstanceToNodeMapper(final oVirtSDKWrapper ovirtSDK) {
		this.ovirtSDK = ovirtSDK;
	}

	/**
	 * Perform the query and return the set of instances
	 *
	 */
	public INodeSet performQuery() {
		final NodeSetImpl nodeSet = new NodeSetImpl();


		final List<VM> instances = this.ovirtSDK.getVms();


		mapInstances(nodeSet, instances);
		return nodeSet;
	}



	private void mapInstances(final NodeSetImpl nodeSet, final List<VM> instances) {
		for (final VM inst : instances) {
			logger.debug(this.runningStateOnly + inst.getStatus().getState());
			if (this.runningStateOnly == true) {
				String status = inst.getStatus().getState();
				if (status != null && !status.equals("up"))
					continue;
				if (status == null) 
					logger.warn("VM Status is Undefined: " + inst.getName());
			}
			logger.debug(inst.getName());
			final INodeEntry iNodeEntry;
			try {
				iNodeEntry = InstanceToNodeMapper.instanceToNode(inst);
				if (null != iNodeEntry) {
					nodeSet.putNode(iNodeEntry);
				}
			} catch (GeneratorException e) {
				logger.error(e);
			}
		}
	}


	/**
	 * Convert an oVirt Instance to a RunDeck INodeEntry based on the mapping input
	 */
	@SuppressWarnings("unchecked")
	static INodeEntry instanceToNode(final VM inst) throws GeneratorException {
		final NodeEntryImpl node = new NodeEntryImpl();

		node.setNodename(inst.getName());
		node.setOsArch(inst.getCpu().getArchitecture());
		node.setOsName(inst.getOs().getType());
		node.setDescription(inst.getDescription());
		node.setUsername("root");
		InetAddress address = null;
		if (inst.getGuestInfo() != null) {
			try {
				address = InetAddress.getByName(inst.getGuestInfo().getFqdn());
				logger.debug("Host " + node.getNodename() + " Guest FQDN " + inst.getGuestInfo().getFqdn() + " Address: " + address.getHostName());
				if (address.getHostName() == "localhost") 
					throw new UnknownHostException();
			} catch (UnknownHostException e) {
				/* try the first IP instead then */
				logger.warn("Host " + node.getNodename() + " address " + inst.getGuestInfo().getFqdn() + " does not resolve. Trying IP addresses instead");
				for (int i = 0; i < inst.getGuestInfo().getIps().getIPs().size(); i++) { 
					logger.debug("Host " + node.getNodename() + " Trying " + inst.getGuestInfo().getIps().getIPs().get(i).getAddress());
					try {
						address = InetAddress.getByName(inst.getGuestInfo().getIps().getIPs().get(i).getAddress());
						if (address != null) {
							if (address.isLinkLocalAddress() || address.isMulticastAddress() ) {
								logger.warn("Host " + node.getNodename() + " ip address is not valid: " + inst.getGuestInfo().getIps().getIPs().get(i).getAddress());
								continue;
							}
							logger.debug("Host " + node.getNodename() + " ip address " + address.getHostAddress() + " will be used instead");
							break;
						}
					} catch (UnknownHostException e1) {
						logger.warn("Host " + node.getNodename() + " IP Address " + inst.getGuestInfo().getIps().getIPs().get(i).getAddress() + " is invalid");
					}
				}
			}
		}
		if (address == null) {
			/* try resolving based on name */
			try {
				address = InetAddress.getByName(node.getNodename());
			} catch (UnknownHostException e) {
				logger.warn("Unable to Find IP address for Host " + node.getNodename());
				return null;
			}
		}


		if (address != null)
			node.setHostname(address.getCanonicalHostName());


		if (inst.getTags() != null) {
			VMTags tags = inst.getTags();
			final HashSet<String> tagset = new HashSet<String>();
			try {
				for (int j = 0; j < tags.list().size(); j++) {
					tagset.add(tags.list().get(j).getName());
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (null == node.getTags()) {
                node.setTags(tagset);
            } else {
                final HashSet<String> orig = new HashSet<String>(node.getTags());
                orig.addAll(tagset);
                node.setTags(orig);
            }
		}

		if (inst.getHighAvailability().getEnabled())
			node.setAttribute("HighAvailability", "true");
		if (inst.getType() != null)
			node.setAttribute("Host Type", inst.getType());
		node.setAttribute("oVirt VM", "true");
		node.setAttribute("oVirt Host", inst.getHost().getName());
		
		return node;
	}


	/**
	 * Return true if runningStateOnly
	 */
	public boolean isRunningStateOnly() {
		return runningStateOnly;
	}

	/**
	 * If true, the an automatic "running" state filter will be applied
	 */
	public void setRunningStateOnly(final boolean runningStateOnly) {
		this.runningStateOnly = runningStateOnly;
	}

	public static class GeneratorException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6384545528587691313L;

		public GeneratorException() {
		}

		public GeneratorException(final String message) {
			super(message);
		}

		public GeneratorException(final String message, final Throwable cause) {
			super(message, cause);
		}

		public GeneratorException(final Throwable cause) {
			super(cause);
		}
	}

}
