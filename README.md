# Mapp Intelligence - Server-side Java tracking library

[Site](https://mapp.com/) |
[Docs](https://documentation.mapp.com/latest/en/java-library-7239408.html) |
[Support](https://support.webtrekk.com/) |
[Changelog](https://documentation.mapp.com/latest/en/java-changelog-10537432.html)

Server-side tracking is crucial for companies needing to measure mission-critical information on their website, such 
as order information or other website goals.

The Java library of Mapp Intelligence helps customers to setup server-side tracking when using Mapp Intelligence as 
their analysis tool.

The basis for the data collection on your server is implementing the respective library. The Mapp Intelligence tracking 
library provides scripts to track user behavior and technical information, such as user agents, among others. In 
addition to the standard data collection, the tracking library offers many options to customize tracking based on 
specific use cases. Typical use cases are product, order and shipment tracking or the tracking of application processes.

# Development

## Requirements

| Software         | Version     |
|------------------|------------:|
| `docker`         |     `19.0+` |
| `docker-compose` |     `1.24+` |
| `make`           |             |

## Build

Build test and compile *Mapp Intelligence - Server-side Java tracking library* with Java 8 inside a docker container.

```bash
$ make build
```

## Test

Test *Mapp Intelligence - Server-side Java tracking library* with Java 8 - Java 16 inside a docker container.

```bash
$ make test-all
```

Test *Mapp Intelligence - Server-side Java tracking library* with the current latest Java version inside a docker container.

```bash
$ make test-latest
```

## Demo

Start a demo shop example on https://localhost:8443.

```bash
$ make test
```