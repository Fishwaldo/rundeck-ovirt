oVirt/RHEV-m Nodes Plugin
========================

Version: 1.0

This is a Resource Model Source plugin for [RunDeck][] 1.5+ that provides
oVirt/RHEV-m Instances as nodes for the RunDeck server.

[RunDeck]: http://rundeck.org

NOTE: For Rundeck 1.4, you will need to use plugin [version 1.2][].

[version 1.0]: http://stash.my-ho.st/stash/projects/DYN/repos/rundeck-ovirt/browse

Installation
------------

Download from http://stash.my-ho.st/stash/projects/DYN/repos/rundeck-ovirt/browse

Put the `rundeck-ovirt-1.0.jar` into your `$RDECK_BASE/libext` dir.

Usage
-----

You can configure the Resource Model Sources for a project either via the
RunDeck GUI, under the "Admin" page, or you can modify the `project.properties`
file to configure the sources.

See: [Resource Model Source Configuration](http://rundeck.org/1.5/manual/plugins.html#resource-model-source-configuration)

The provider name is: `oVirt Resources`

Here are the configuration properties:

* `oVirt API URL`: The URL to the oVirt API - Usually https//<ovirthost>:443/api
* `oVirt UserName`: The Username to login to oVirt with - Defaults to admin@internal
* `oVirt Password` - The password for the above account
* `refreshInterval`: Time in seconds used as minimum interval between calls to the AWS API. (default 30)
* `runningOnly`: if "true", automatically filter the * instances by "instance-state-name=running"

Mapping EC2 Instances to Rundeck Nodes
=================

Rundeck node definitions specify mainly the pertinent data for connecting to and organizing the Nodes.  EC2 Instances have metadata that can be mapped onto the fields used for Rundeck Nodes.

Rundeck nodes have the following metadata fields:

* `nodename` - unique identifier
* `hostname` - IP address/hostname to connect to the node. Only works if the Guest Agent is running on the VM, otherwise tries looking up the NodeName in DNS. If your nodename and DNS search path are equal the Hostname, it will return the hostname
* `username` - SSH username to connect to the node - Defaults to root
* `description` - textual description
* `osName` - OS name - Taken from oVirt
* `osArch` - OS architecture - Taken from oVirt
* `tags` - Any Tags assigned on the oVirt Admin Console to the VM
* `HighAvailability` - If the Host is set with the HA flag
* `Host Type` - Desktop or Server as assigned by the oVirt Admin Gui
* `oVirt VM` - True if the node came from the oVirt plugin

