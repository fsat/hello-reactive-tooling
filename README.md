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

_These steps are temporary until we have Reactive Deployment Tool released and published_

### Build Reactive CLI

Clone the project from [Reactive CLI](https://github.com/typesafehub/reactive-cli) and build the native executable.

_TEMPORARY: checkout commit 8211e6b6fb908552cfab4eee1bb577c6cc9112ed until [47](https://github.com/lightbend/reactive-cli/pull/47) is merged_

```bash
$ sbt cli/nativeLink
```

Copy the native executable into a path outside of the project:

```bash
$ mkdir -p ~/tmp/reactive-cli/bin
$ cp cli/target/scala-2.11/*out ~/tmp/reactive-cli/bin/rp
```

Add the executable into `PATH` environment variable:

```bash
$ export PATH=${PATH}:~/tmp/reactive-cli/bin
```

### Libcurl (MacOS only)

_Only follow this step for MacOS. This will ensure correct version of `libcurl` with correct TLS support is used._

Install `libcurl` with correct TLS support.

```bash
$ brew install curl --with-openssl
```

Export the environment variables to ensure correct version of `libcurl` is being used.

```bash
$ export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/local/opt/curl/lib"
$ export DYLD_LIBRARY_PATH="${DYLD_LIBRARY_PATH}:/usr/local/opt/curl/lib"
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
