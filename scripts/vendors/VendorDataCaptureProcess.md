# Vendor Data Capture Process

## Overview

The vendor data form is a spreadsheet that allows the customer (HMRC vendor management team) to capture the relevant features of a software product.

The customer will complete the spreadsheet form for each software product to be added to the Software Choices service and share that spreadsheet with the development team.

The development team will use `process_vendor.py` every Friday to extract, validate and apply the JSON data to [software-vendors.json](../../conf/software-vendors.json), and in the case of a new product, update [VendorDetails.md](vendor_data/VendorDetails.md) to include a link to the product details page for that vendor.

**IMPORTANT**: If part of an update to an existing product includes that product being renamed, this script will not work as expected, without the name being first manually updated in both [software-vendors.json](../../conf/software-vendors.json) & [VendorDetails.md](vendor_data/VendorDetails.md) before the script is run. It was not possible to make allowances for this, as some vendors have multiple products, with similar contact details, but different names. Therefore, matching on anything other than the name would cause issues with these products. Fortunately, a vendor renaming their product is comparatively rare, and involves some admin for the vendor!

Note: If a new vendor product is to be added, the script automatically assigns a new `productId` that is 3 larger than the last entry.


## Development team process

Pre-requisites:

1 — Install Python 3 (Minimum version 3.10)

Check if already installed by running the below command in Terminal:

`python3 --version`

If not installed, install via Homebrew:

`brew install python3`

2 — Create and Activate the Virtual Environment
```terminaloutput
# Creates the environment
python3 -m venv .venv            
```
```terminaloutput
# Activate the environment
source .venv/bin/activate 
```

3 — Install the Python Dependencies
```terminaloutput
python -m pip install --upgrade pip
pip install -r scripts/vendors/requirements.txt
```

### Step 1 — Create a branch based on the task number
```terminaloutput
git checkout main

git pull

git checkout -b <branch name>  # replace with your actual task number
```

###  Step 2 — Copy the vendor .xlsx file(s) into the vendors folder

Copy the completed spreadsheets received from the vendor management team into `scripts/vendors/vendor_data/`. The script deletes all successfully processed `.xlsx` files from your local folder after a run, to prevent issues occurring in any future runs of the script. Skipped or errored files are kept for analysis.

### Step 3 — Run the vendor processor script

**From the root of the project**, run the following command to process the vendor data and update the software vendor data file, `software-vendors.json`:

```terminaloutput
python3 scripts/vendors/process_vendor.py
```

The script will automatically:
- Read all `.xlsx` files in `scripts/vendors/vendor_data/`
- Extract the JSON from the Json Output sheet
- Remove any invalid control characters (e.g. accidental newlines)
- Trim leading/trailing whitespace from all fields
- Update website URLs with an `http://` protocol to ones with an `https://` protocol
- Insert new vendors or update existing ones in `conf/software-vendors.json`
- Print a diff table of all changes made
- Delete all `.xlsx` files in `scripts/vendors/vendor_data/` relating to updated or inserted records
- Update `scripts/vendors/VendorDetails.md` with product details page of any newly added vendors

The Virtual Environment can now be deactivated with the below command. It can be reactivated by running the activation command in the Prerequisites (Step 2)

```terminaloutput
deactivate
```

## Template Update process

The spreadsheet form contains a series of hidden columns on both the Data and Json Output tabs.
The Data tab will need to have any new filters added as new rows and the appropriate filter name associated with that row. Provided the formula are copied to the correct row then the new filter should be reflected in the output.

It is recommended that the person updating the spreadsheet form should have a good understanding of how the form works and how the filters are applied to ensure that the correct data is being captured and outputted. It is also recommended that the person updating the spreadsheet form should have a good understanding of Excel!

The passwords to unprotect the spreadsheet tabs can be obtained from a current member of the development team.

The vendor data capture spreadsheet are versioned for the benefit of the customer [here](spreadsheet_form_versions). Please copy the latest one and create a new version to avoid confusion.