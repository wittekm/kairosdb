KairosDb Release Notes
======================

1.3.0 - June 27, 2020
------------------------
* Initial release as gruop:artifact `io.inscopemetrics.kairosdb:kairosdb`.
* Forked from [ddimensia/kairosdb](https://github.com/ddimensia/kairosdb).
* Dependency changes from original (different transitive closures under Maven vs Ivy):
  * Replaces: `slf4j-api-1.7.25.jar` instead of `slf4j-api-1.7.2.jar`
  * Adds: `dom4j-1.6.1.jar` (Maven transitive of `org.agileclick.genorm:genormous:jar:1.6.4`)
  * Adds: `jffi-1.2.10.jar` (Maven transitive of `com.datastax.cassandra:cassandra-driver-core:jar:3.3.2`)
* Packaging changes from original:
  * Jar: 
    * Adds:
      * `LICENSE`
      * `META-INF/maven/io.inscopemetrics.kairosdb/kairosdb/pom.properties`
      * `META-INF/maven/io.inscopemetrics.kairosdb/kairosdb/pom.xml`
  * Docker
      * Configuration is in `/opt/kairosdb/config` instead of `/opt/kairosdb/conf`
      * `logback.xml` is in `/opt/kairosdb/config` instead of `/opt/kairosdb/conf/logging`
      * Includes `kairosdb.conf` (HOCON) instead of `kairosdb.properties` (Java Properties) 
      * No longer ships with `check-log-errors.sh`, `create_hbase_table.sh`, `kairosdb-service.sh` or `kairosdb.bat` in `bin`
  * Tar.gz:
      * Top level directory is no longer `kairosdb`
      * Configuration is in `./config` instead of `./kairosdb/conf`
      * `logback.xml` is in `./config` instead of `./kairosdb/conf/logging`
      * No longer ships with `check-log-errors.sh`, `create_hbase_table.sh`, `kairosdb-service.sh` or `kairosdb.bat` in `bin`
   * RPM:
      * Packaged in the same way as other Inscope Metrics services.
* Other changes:
  * Log files are written to `logs` and not `log`

&copy; Inscope Metrics, 2020
