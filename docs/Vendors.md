# Vendor Data Capture process

## Overview

The vendor data capture template is a spreadsheet that allows the customer (HMRC vendor management team) to capture the relevant features of a software product.

The customer will complete a template for each software product to be added to the Software Choices service and share that spreadsheet with the development team.

The development team will use the automated `process_vendor.py` script to extract, validate and apply the JSON data to [software-vendors.json](../conf/software-vendors.json).

Note: If a new vendor product is to be added, the script automatically assigns a new `productId` that is 3 larger than the last entry.


## Development team process

Pre-requisites:

1 — Install Python 3

Check if already installed by running the below command in Terminal
     python3 --version

If not installed, install via Homebrew:
     brew install python3

2 — Install the required Python library openpyxl

Check if already installed:
     python3 -c "import openpyxl"

If not installed:
     pip3 install openpyxl

3 — Verify both are working

python3 --version       # should print e.g. Python 3.11.x
pip3 show openpyxl      # should show openpyxl version info

### Step 1 — Create a branch based on the task number

git checkout main
git pull
git checkout -b <branch name>  # replace with your actual task number

###  Step 2 — Copy the vendor .xlsx file(s) into the vendors folder

Copy the completed template(s) received from the vendor management team into scripts/vendors/ and follow the below steps.

Make sure you're in the project root directory first: cd <project root>

Then copy the .xlsx file(s) into the vendors folder: cp ~/<Your Folder>/*.xlsx scripts/vendors/

This folder is gitignored — the .xlsx files will not be committed.

### Step 3 — Run the vendor processor script

From the root of the project run the below command to process the vendor data and update the software-vendors.json file.

python3 scripts/process_vendor.py scripts/vendors

The script will automatically:
Read all .xlsx files in scripts/vendors/
Extract the JSON from the Json Output sheet
Remove any invalid control characters (e.g. accidental newlines)
Trim leading/trailing whitespace from all fields
Fix http:// website URLs to https://
Insert new vendors or update existing ones in conf/software-vendors.json
Print a diff table of all changes made

## Template Update process

The template spreadsheet contains a series of hidden columns on both the Data and Json Output tabs.
The Data tab will need to have any new filters added as new rows and the appropriate filter name associated with that row. Provided the formula are copied to the correct row then the new filter should be reflected in the output.

It is recommended that the person updating the template should have a good understanding of how the template works and how the filters are applied to ensure that the correct data is being captured and outputted. It is also recommended that the person updating the template should have a good understanding of Excel!

The passwords to unprotect the template tabs can be obtained from a current member of the development team.

The vendor data capture templates are versioned for the benefit of the customer. Please copy the latest one and create a new version to avoid confusion.