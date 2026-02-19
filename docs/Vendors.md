# Vendor Data Capture process

## Overview

The vendor data capture template is a spreadsheet that allows the customer (HMRC vendor management team) to capture the relevant features of a software product.

The customer will complete a template for each software product to be added to the Software Choices service and share that spreadsheet with the development team.

The development team will extract the json data (that the service uses) from the spreadsheet and apply it to the [software-vendors.json](conf/software-vendors.json) file.

Note: If a new vendor product is to be added to the service then a new row should be created at the end of the software-vendor file and a new product Id should be allocated that is 3 larger than the last entry.

## Development team process
The spreadsheet contains a hidden tab called "Json Output". This tab should be unhidden and the contents of the appropriate field (currently F1) should be copied and either appended to the bottom of the [software-vendors.json](conf/software-vendors.json) file, or used to replace an existing entry for a software product.

## Template Update process

The template spreadsheet contains a series of hidden columns on both the Data and Json Output tabs.
The Data tab will need to have any new filters added as new rows and the appropriate filter name associated with that row. Provided the formula are copied to the correct row then the new filter should be reflected in the output.

It is recommended that the person updating the template should have a good understanding of how the template works and how the filters are applied to ensure that the correct data is being captured and outputted. It is also recommended that the person updating the template should have a good understanding of Excel!

The passwords to unprotect the template tabs can be obtained from a current member of the development team.

The vendor data capture templates are versioned for the benefit of the customer. Please copy the latest one and create a new version to avoid confusion.