# OSGi-R6-Scheduler
OSGi R6 Annotation based Scheduler

This above scheduler is developed using OSGi based R6 annotations and using OOTB Scheduler service

* Add below dependency as part of Core module Dependency to get ACS commons as dependency
```
<dependency>
     <groupId>com.adobe.acs</groupId>
     <artifactId>acs-aem-commons-bundle</artifactId>
</dependency>
```

* Add below code as part UI APPS Pom.xml under Plugins and embed the ACS commons so that ACS dependency will be deployed as part for maven build process
```
 <plugins>
    <!-- ====================================================================== -->
    <!-- V A U L T   P A C K A G E   P L U G I N S                              -->
    <!-- ====================================================================== -->
    <plugin>
        <groupId>org.apache.jackrabbit</groupId>
        <artifactId>filevault-package-maven-plugin</artifactId>
        <extensions>true</extensions>
        <configuration>
            <allowIndexDefinitions>true</allowIndexDefinitions>
            <group>example-commons</group>
            <subPackages>
                <subPackage>
                    <groupId>com.adobe.cq</groupId>
                    <artifactId>core.wcm.components.all</artifactId>
                    <filter>true</filter>
                </subPackage>
                <subPackage>
                    <groupId>com.adobe.acs</groupId>
                    <artifactId>acs-aem-commons-content</artifactId>
                    <filter>true</filter>
                </subPackage>
            </subPackages>
        </configuration>
    </plugin>
    <plugin>
        <groupId>com.day.jcr.vault</groupId>
        <artifactId>content-package-maven-plugin</artifactId>
        <extensions>true</extensions>
        <configuration>
            <verbose>true</verbose>
            <failOnError>true</failOnError>
        </configuration>
    </plugin>
    <plugin>
        <groupId>org.apache.sling</groupId>
        <artifactId>htl-maven-plugin</artifactId>
    </plugin>
</plugins>
```

* We are using ACS commons dispatcher as part of our dependency 
We can use Dispatcher API developed from ACS commons to clear the cache
This scheduler runs only in Author instance, as we know dispatcher api gets all the required Flush agent configurations only in Author instance
 

 
