# Hello-World for Reactive Deployment Tool

This project is created to test Reactive Deployment Tool.

## What's this about?

This project consists of a simple Play application which has [SBT Reactive App](https://github.com/lightbend/sbt-reactive-app) plugin enabled. Using this plugin and the combination of [Reactive CLI](https://github.com/typesafehub/reactive-cli), deployment to a target runtime can be done in a seamless and timely manner.

At the point of writing the target runtime supported is Kubernetes, although the tool might be extended to support DC/OS and other target runtime.

## Pre-requisite

* [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) installed and running with insecure registry enabled.
* [Docker](https://docs.docker.com/engine/installation/) command line tools installed and enabled.
* [OpenJDK 8](http://openjdk.java.net/install/) or [Oracle Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [SBT](http://www.scala-sbt.org/download.html)

### Install Reactive CLI

Pick the installation command appropriate for your operating system.

_At this point in time on Linux and MacOS are supported. We haven't forgotten about windows support._

The following steps is taken from the [Tooling Notes](https://docs.google.com/a/lightbend.com/document/d/1OpvzJmLJodZtb6L4HRyQZNpFwALMEkzIxLB2YkfD_1o/edit?usp=sharing) document which I assume will be hosted in the actual documentation site later on.

#### Debian-based distros

```bash
# Setup Repository
$ wget -qO - https://downloads.lightbend.com/rp/keys/bintray-debian | sudo apt-key add - && echo "deb https://dl.bintray.com/lightbend/deb $(lsb_release -cs) main" | sudo dd status=none of=/etc/apt/sources.list.d/lightbend.list && sudo apt-get update

# Install CLI
$ sudo apt-get install reactive-cli
```

#### RPM-based distros

```bash
# Setup Repository
$ wget -qO - https://bintray.com/lightbend/rpm/rpm | sudo dd status=none of=/etc/yum.repos.d/bintray-lightbend-rpm.repo

# Install CLI
$ sudo yum install reactive-cli
```

#### MacOS

```bash
# Setup Repository
$ brew tap lightbend/tools

# Install CLI
$ brew install lightbend/tools/reactive-cli
```

## Deploying to Minikube

Setup Minikube Docker environment variables.

```bash
$ eval $(minikube docker-env)
```

Clone this project from Github.

Go into the recently cloned project directory.

```bash
$ cd hello-reactive-tooling
```

Publish the project as Docker images into Minikube.

```bash
$ sbt frontend/docker:publishLocal simple-impl/docker:publishLocal clustered-impl/docker:publishLocal
```

Deploy the `simple-impl` to minikube using the following command. Note for the `simple-impl` we don't generate the Ingress resource as we don't wish to expose the endpoint outside of Kubernetes.

```bash
$ rp generate-kubernetes-deployment hello-reactive-tooling/simple-impl:0.0.1 --generate-services --generate-pod-controllers --env JAVA_OPTS="-Dplay.http.secret.key=simple" | kubectl apply -f -
```

Similarly, deploy the `clustered-impl`. We also won't expose `clustered-impl` outside of Kubernetes. We also scale the `clustered-impl` to `3` instances to test the cluster functionality.

```bash
$ rp generate-kubernetes-deployment hello-reactive-tooling/clustered-impl:0.0.1 --generate-services --generate-pod-controllers --pod-controller-replicas 3 --env JAVA_OPTS="-Dplay.http.secret.key=clustered" --external-service simple-service=_lagom-http-api._tcp.simple-service.default.svc.cluster.local | kubectl apply -f -
```


Deploy the `frontend` to minikube using the following command.

```bash
$ rp generate-kubernetes-deployment hello-reactive-tooling/frontend:0.0.1 --env JAVA_OPTS="-Dplay.http.secret.key=hereiam -Dplay.filters.hosts.allowed.0=$(minikube ip)" | kubectl apply -f -
```



## Accessing the application

Run the following command to access the deployed `frontend`.

```bash
$ curl -vLk "https://$(minikube ip)/"
```

## Outstanding issues

* Reactive Lib should configure the settings so user can simply specify `RP_PLAY_APPLICATION_SECRET` or `APPLICATION_SECRET` from CLI's `--env` switch.
* Play's [Allowed Host Filter](https://www.playframework.com/documentation/2.6.x/AllowedHostsFilter) need to be configured to allow ingress access. _Not sure how to do this_ since the ingress address must be known by the application during startup, and the value of allowed address must be updated if the ingress address is changed.
