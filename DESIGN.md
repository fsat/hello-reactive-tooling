# Hello-World for Reactive Deployment Tool

The following project is created to test Reactive Deployment tool from Play Scala and Lagom Scala's perspective.

The project seeks to assert the following functionality:

* Deploying Play Scala and Lagom Scala application into Kubernetes.
* Ensuring Play Scala can lookup Lagom Scala application using `ServiceLocator` provided by the [reactive-lib](https://github.com/typesafehub/reactive-lib) project.
* Ensuring Lagom Scala can establish a fully-formed Akka cluster using `akka-cluster-bootstrap` provided by the [reactive-lib](https://github.com/typesafehub/reactive-lib) project.
* Ensuring Lagom Scala can lookup another Lagom application using `LagomServiceLocator` provided by the [reactive-lib](https://github.com/typesafehub/reactive-lib) project.

## Setup

The project is split into 3 parts:

* `frontend`: this is the Play Scala frontend.
* `clustered-api` and `clustered-impl`: this is the Lagom Scala application which will form an Akka cluster, and message to the actor within the application will be sharded using [Akka Cluster Sharding](https://doc.akka.io/docs/akka/current/cluster-sharding.html).
* `simple-api` and `simple-impl`: this is simple, hello-world like Lagom Scala application.

## Ingress access

Only `frontend` will be ingressed, and both Lagom applications will not be exposed (i.e. not accessible outside of Kubernetes).

## `frontend` endpoints

The `frontend` application will provide these endpoints.

### `/`

* The `/` endpoint simply returns the word `Hello`.

### `/simple/:text`

```
+--------------+          +----------------+
|              + -------> |                |
|  frontend    |          |  simple-impl   |
|              | <------- +                |
+--------------+          +----------------+
```

* `frontend` will locate `simple-impl` service, and then will invoke the located endpoint which returns a simple text.
* The returned text from `simple-impl` service is then returned to `frontend`.
* `frontend` returns this text to the caller.

### `/clustered/:text`

```
+--------------+          +-----------------+
|              + -------> |                 |
|  frontend    |          |  clustered-impl |
|              | <------- +                 |
+--------------+          +-----------------+
```

* `frontend` will locate `clustered-impl` service, and then will invoke the located endpoint which returns a simple text.
* The returned text from `clustered-impl` service is then returned to `frontend`.
* `frontend` returns this text to the caller.

### `/forward/:text`

```
+--------------+          +-----------------+          +----------------+
|              + -------> |                 + -------> |                |
|  frontend    |          |  clustered-impl |          |  simple-impl   |
|              | <------- +                 | <------- +                |
+--------------+          +-----------------+          +----------------+
```

* `frontend` will locate `clustered-impl` service, and then will invoke the located endpoint.
* The located endpoint within `clustered-impl` will then invoke `simple-impl` using the client API provided by `simple-api`.
* The returned text from `simple-impl` is then returned to `clustered-impl`.
* `clustered-impl` returns this text to `frontend`.
* `frontend` returns this text to the caller.

### `/srv/:name`

* `frontend` will perform DNS SRV lookup for a given `:name`.
* The `:name` can be the name of the service registered in Kubernetes, or the SRV name registered in Kube-DNS.
* If found, the list of addresses will be returned, else `404` is returned.

### `/srv/:name/:endpoint`

* `frontend` will perform DNS SRV lookup for a given `:name` of a service, and the `:endpoint` name.
* If found, the list of addresses will be returned, else `404` is returned.
