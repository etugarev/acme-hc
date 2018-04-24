# ACME Health Check

This project contains sling health check which opens a new session for each evaluated page. 

Was tested with AEM 6.3.2.0.

## Structure

acme 
- umbrella maven project, contains project settings 

parent 
- dependency management 

acme-core 
- bundle - java code 
- package - additional settings, rep:policy files, service users, health check console integration 

## Building

This project uses Maven for building. Common commands:

From the root directory, run ``mvn -PautoInstallPackage clean install`` to build the bundle and content package and install to a CQ instance.

From the bundle directory, run ``mvn -PautoInstallBundle clean install`` to build *just* the bundle and install to a CQ instance.


## Specifying CRX Host/Port

The CRX host and port can be specified on the command line with:
mvn -Dcrx.host=otherhost -Dcrx.port=5502 <goals>

Default port is 4502 (configured in root pom.xml)

## Logging

This project creates additional logger log/acme.log in crx-quickstart folder to trace output.

    24.04.2018 15:07:14.647 *TRACE* [ForkJoinPool-9-worker-7] acme.core.util.Templates Acquiring session com.adobe.granite.repository.impl.CRX3SessionImpl@736d80d9
    24.04.2018 15:07:14.649 *TRACE* [ForkJoinPool-9-worker-7] acme.core.services.hc.PagesHealthCheck Using session com.adobe.granite.repository.impl.CRX3SessionImpl@736d80d9
    24.04.2018 15:07:14.652 *DEBUG* [HealthCheck ACME Pages] acme.core.services.hc.PagesHealthCheck Total checks: 478, Completed: 463

Error messages can be observed in log/error.log file

    24.04.2018 15:10:41.999 *WARN* [ForkJoinPool-10-worker-2] org.apache.jackrabbit.oak.jcr.delegate.SessionDelegate Attempted to perform getProperty while thread ForkJoinPool-10-worker-0 was concurrently writing to this session. Blocked until the other thread finished using this session. Please review your code to avoid concurrent use of a session.

## Health Check configuration

[Click here](http://localhost:4502/system/console/configMgr) and then find "
ACME Pages Health Check Service Configuration"


## Launching Health Check

After package is installed [click here.](http://localhost:4502/libs/granite/operations/content/healthreports/healthreport.html/system/sling/monitoring/mbeans/org/apache/sling/healthcheck/HealthCheck/acmePagesHealthCheck)


