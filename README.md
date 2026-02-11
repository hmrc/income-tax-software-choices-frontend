[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/hmrc/income-tax-software-choices-frontend.svg)](https://travis-ci.org/hmrc/income-tax-software-choices-frontend)
[![Download](https://api.bintray.com/packages/hmrc/releases/income-tax-software-choices-frontend/images/download.svg)](https://bintray.com/hmrc/releases/income-tax-software-choices-frontend/_latestVersion)

# Income Tax Software Choices Frontend

This is a Scala/Play frontend web UI that provides screens for an individual to discover possible software choices which
will enable them to submit electronic tax returns as part of Making Tax Digital

1. [Quick start](#Quick-start)
    - [Prerequisites](#Prerequisites)
    - [How to start](#How-to-start)
    - [How to use](#How-to-use)
    - [How to test](#How-to-test)
2. [Persistence](#Persistence)
2. [Vendor Data](#Vendor-Data)

# Quick start

## Prerequisites

* [sbt](http://www.scala-sbt.org/)
* MongoDB (*[See Persistence](#Persistence)*)
* HMRC Service manager (*[Install Service-Manager](https://github.com/hmrc/service-manager/wiki/Install#install-service-manager)*)

## How to start

**Run the service with `ITSA_SOFTWARE_CHOICES_ALL`:**  
```
./scripts/start
```

## How to use

There is only one flow: Choose software

See Route files for more information.

### Local

* Login via: Not needed.
* Entry page: [http://localhost:9591/find-making-tax-digital-income-tax-software/](http://localhost:9591/find-making-tax-digital-income-tax-software/)
* Feature switches: [http://localhost:9591/find-making-tax-digital-income-tax-software/test-only/feature-switch](http://localhost:9591/find-making-tax-digital-income-tax-software/test-only/feature-switch)

### QA - Real vendor details

*Requires HMRC VPN*

* Login via: N/A
* Entry page : [https://www.qa.tax.service.gov.uk/find-making-tax-digital-income-tax-software/](https://www.qa.tax.service.gov.uk/find-making-tax-digital-income-tax-software/)
* Feature switches: [https://www.qa.tax.service.gov.uk/find-making-tax-digital-income-tax-software/test-only/feature-switch](https://www.qa.tax.service.gov.uk/find-making-tax-digital-income-tax-software/test-only/feature-switch)

### Staging - Test vendor details

*Requires HMRC VPN*

* Login via: N/A
* Entry page : [https://www.staging.tax.service.gov.uk/find-making-tax-digital-income-tax-software/](https://www.staging.tax.service.gov.uk/find-making-tax-digital-income-tax-software/)
* Feature switches: [https://www.staging.tax.service.gov.uk/find-making-tax-digital-income-tax-software/test-only/feature-switch](https://www.staging.tax.service.gov.uk/find-making-tax-digital-income-tax-software/test-only/feature-switch)

### Notes on behaviour

This service is under development.  Please keep this section up to date.

## How to test

* Run unit tests: `sbt clean test`
* Run integration tests: `sbt clean it:test`
* Run performance tests: **to be provided** in the repo [income-tax-subscription-performance-tests](https://github.com/hmrc/income-tax-subscription-performance-tests)
* Run acceptance tests: **to be provided** in the repo [income-tax-subscription-acceptance-tests](https://github.com/hmrc/income-tax-subscription-acceptance-tests)

# Vendor Data

The vendor data capture process and the template are document [here](docs/Vendors.md)

# Persistence

Data is stored in mongodb

### License.
 
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
