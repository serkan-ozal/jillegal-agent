What is Jillegal-Agent?
==============

**Jillegal-Agent** is a dynamic and operating system aware (Windows, Unix, MAC, Solaris) **Java Instrumentation API** service framework. Without any VM argument at startup, you can easily access `java.lang.instrument.Instrumentation` object at runtime to make your instrumentations. At background, Jillegal-Agent, connects current Java process and loads itself as agent library with using **Java Attach API** by `com.sun.tools.attach.VirtualMachine` instance. Note that operating system awareness is needed for using **Java Attach API** to connect current Java process.

Usage
=======

Before getting instrumentation object, you must call

~~~~~ java
JillegalAgent.init();
~~~~~

Then you can get`java.lang.instrument.Instrumentation` object by calling

~~~~~ java
JillegalAgent.getInstrumentation();
~~~~~

If you have got an error like this

~~~~~ java
...

com.sun.tools.attach.AttachNotSupportedException: Unable to open socket file: target process not responding or HotSpot VM not loaded

...
~~~~~

You must explicitly enable attach listener flag by `-XX:+StartAttachListener` as VM argument.
This error is caused by a bug of JVM on **MacOS** operating system.

Installation
=======

In your `pom.xml`, you must add repository and dependency for **Jillegal-Agent**. 
You can change `jillegal.agent.version` to any existing **Jillegal-Agent** library version.
Latest version is `2.0`.

~~~~~ xml
...
<properties>
    ...
    <jillegal.agent.version>2.0</jillegal.agent.version>
    ...
</properties>
...
<dependencies>
    ...
	<dependency>
		<groupId>tr.com.serkanozal</groupId>
		<artifactId>jillegal-agent</artifactId>
		<version>${jillegal.agent.version}</version>
	</dependency>
	...
</dependencies>
...
<repositories>
	...
	<repository>
		<id>serkanozal-maven-repository</id>
		<url>https://github.com/serkan-ozal/maven-repository/raw/master/</url>
	</repository>
	...
</repositories>
...
~~~~~
