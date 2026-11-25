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
2. [Vendor Data](#Vendor-Data)
3. [Persistence](#Persistence)

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

The vendor data capture process is documented [here](scripts/vendors/VendorDataCaptureProcess.md)
Detailed vendor information [here](scripts/vendors/vendor_data/VendorDetails.md)

## Test-only software commitments overview

Start the service with `sbt 'run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'` and open
[software commitments for April 2027](http://localhost:9591/find-making-tax-digital-income-tax-software/test-only/software-vendors).
The page reuses `SoftwareChoicesService` to show all listed products, grouped feature coverage and a single product commitments table.
Search by product name, select an income or feature, or show only products with outstanding commitments.
Development counts link to the matching products. Summary figures always cover the complete dataset.
Download all products or the current filtered results as CSV. Both exports include all feature columns,
product names and outstanding April 2027 commitments, without product IDs, websites or last-updated columns.
CSV filenames include the download date; UTF-8 encoding supports spreadsheet applications.
Values that could be interpreted as spreadsheet formulas are prefixed with an apostrophe for safety.

The business definition is that features marked "In development" were committed for April 2027.
The "any feature" product total includes submission support and other listing features; the income product total
only includes income support and related items. Products developing tax-return submission alone are not in the income total.
The data does not contain delivery dates or historical progress, so the page does not determine whether commitments are overdue.
Missing feature information is shown separately from an explicit "N/A" ("Not included").
Restart the service after changing the vendor JSON. The route is only available through the test-only router.

# Persistence

Data is stored in mongodb

### License.
 
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
