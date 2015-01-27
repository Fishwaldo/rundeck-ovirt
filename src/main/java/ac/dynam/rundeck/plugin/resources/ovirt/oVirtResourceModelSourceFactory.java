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
import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.*;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import java.util.*;

@Plugin(name = "ovirt-source", service = "ResourceModelSource")
public class oVirtResourceModelSourceFactory implements ResourceModelSourceFactory, Describable {
    public static final String PROVIDER_NAME = "ovirt-source";
    private Framework framework;

    public static final String RUNNING_ONLY = "runningOnly";
    public static final String OVIRT_URL = "ovirtURL";
    public static final String OVIRT_USER = "ovirtUser";
    public static final String OVIRT_PASS = "ovirtPass";
    public static final String REFRESH_INTERVAL = "refreshInterval";

    public oVirtResourceModelSourceFactory(final Framework framework) {
        this.framework = framework;
    }

    public ResourceModelSource createResourceModelSource(final Properties properties) throws ConfigurationException {
        final oVirtResourceModelSource ec2ResourceModelSource = new oVirtResourceModelSource(properties);
        ec2ResourceModelSource.validate();
        return ec2ResourceModelSource;
    }

    static Description DESC = DescriptionBuilder.builder()
            .name(PROVIDER_NAME)
            .title("oVirt Resources")
            .description("Produces nodes from oVirt")

            .property(PropertyUtil.string(OVIRT_URL, "oVirt API URL", "oVirt API URL", true, null))
            .property(PropertyUtil.string(OVIRT_USER, "oVirt UserName", "oVirt User Name", true, "admin@internal"))
            .property(
                    PropertyUtil.string(
                    		OVIRT_PASS,
                            "oVirt Password",
                            "Password used to Login to oVirt",
                            false,
                            null,
                            null,
                            null,
                            Collections.singletonMap("displayType", (Object) StringRenderingConstants.DisplayType.PASSWORD)
                    )
            )
            .property(PropertyUtil.integer(REFRESH_INTERVAL, "Refresh Interval",
                    "Minimum time in seconds between API requests to oVirt (default is 30)", false, "30"))
            .property(PropertyUtil.bool(RUNNING_ONLY, "Only Running Instances",
                    "Include Running state instances only. If false, all instances will be returned", false, "true"))

            .build();

    public Description getDescription() {
        return DESC;
    }
}
