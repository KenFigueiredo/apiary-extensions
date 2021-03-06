# Hive Metastore Events

##  Overview
Hive Metastore Events contains a set of modules responsible for retrieving and processing Hive Metastore events.

Currently, the following modules are defined:
 - [apiary-gluesync-listener](apiary-gluesync-listener) - listens to events from the Hive Metastore and will push metadata updates to an AWS Glue catalog.
 - [apiary-metastore-auth](apiary-metastore-auth) - ReadOnlyAuth pre-event listener which will handle authorization using a configurable database whitelist.
 - [apiary-ranger-metastore-plugin](apiary-ranger-metastore-plugin) - RangerAuth pre-event listener which handles authorization and auditing using Ranger.

# Legal
This project is available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

Copyright 2019 Expedia, Inc.
