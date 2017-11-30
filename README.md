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

## Setup - temporary

_These steps are temporary until we have the public release version of Reactive Deployment Tool released and published_

### Publish Akka Management locally

Clone the project from [SBT Reactive App](https://github.com/lightbend/reactive-lib) and execute the following script to publish locally.

```bash
$ sh publish-akka-management.sh
```

### Ensure correct libcurl is used (MacOS only)

_Only follow this step for MacOS. This will ensure correct version of `libcurl` with correct TLS support is used._

Install `libcurl` with correct TLS support.

```bash
$ brew install curl --with-openssl
```

Export the environment variables to ensure correct version of `libcurl` is being used.

```bash
$ export DYLD_LIBRARY_PATH="${DYLD_LIBRARY_PATH}:/usr/local/opt/curl/lib"
```

## Setup

Here are the setup steps.

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
$ sbt frontend/docker:publishLocal
```

This will publish the project as a docker image called `frontend` tagged with `0.0.1`.

```bash
$ docker images | grep frontend | grep 0.0.1
REPOSITORY                                             TAG                 IMAGE ID            CREATED             SIZE
hello-reactive-tooling/frontend                        0.0.1               7fea8f5b2a06        18 minutes ago      166MB
```

Deploy the image to minikube using the following command.

```bash
$ rp generate-deployment hello-reactive-tooling/frontend:0.0.1 --target kubernetes --kubernetes-version 1.6 --env JAVA_OPTS="-Dplay.http.secret.key=hereiam -Dplay.filters.hosts.allowed.0=$(minikube ip)" | kubectl apply -f -
```

In the command above, the `--kubernetes-version` is set to `1.6`. Please update accordingly to `1.7` or `1.8` to match the version of Kubernetes running on your installed Minikube.

## Accessing the application

Run the following command to access the deployed `frontend`.

```bash
$ curl -vk $(minikube service --url --https nginx-ingress | tail -n 1)
```

## Outstanding issues

* Reactive Lib should configure the settings so user can simply specify `RP_PLAY_APPLICATION_SECRET` or `APPLICATION_SECRET` from CLI's `--env` switch.
* Play's [Allowed Host Filter](https://www.playframework.com/documentation/2.6.x/AllowedHostsFilter) need to be configured to allow ingress access. _Not sure how to do this_ since the ingress address must be known by the application during startup, and the value of allowed address must be updated if the ingress address is changed.
