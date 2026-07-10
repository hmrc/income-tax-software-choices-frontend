import json, sys, os, glob, openpyxl, re

VENDORS_JSON = "conf/software-vendors.json"
INBOX_DIR    = "scripts/vendors"
FIELD_ORDER  = ["productId", "name", "phone", "email", "website", "accessibilityStatementLink", "filters"]


def ordered_vendor(vendor, product_id):
    result = {"productId": product_id}
    for key in FIELD_ORDER[1:]:
        if key in vendor:
            result[key] = vendor[key]
    for key in vendor:
        if key not in result and key != "productId":
            result[key] = vendor[key]
    return result


def trim_and_report(vendor):
    trimmed = []
    for key, val in vendor.items():
        if key == "filters" and isinstance(val, dict):
            for fk in val:
                fv = val[fk]
                if isinstance(fv, str) and fv != fv.strip():
                    trimmed.append(f"    filters.{fk}: '{fv}' → '{fv.strip()}'")
                    val[fk] = fv.strip()
        elif isinstance(val, str) and val != val.strip():
            trimmed.append(f"    {key}: '{val}' → '{val.strip()}'")
            vendor[key] = val.strip()
    return trimmed


def validate_and_fix_website(vendor):
    website = vendor.get("website", "")
    if website and not website.startswith("https://"):
        clean = website
        for prefix in ("https://", "http://"):
            if clean.startswith(prefix):
                clean = clean[len(prefix):]
                break
        fixed = "https://" + clean
        print(f"  WEBSITE FIXED  : '{website}' → '{fixed}'")
        vendor["website"] = fixed


def print_diff(old, new):
    changes = []
    all_keys = set(old.keys()) | set(new.keys())
    for key in sorted(all_keys):
        if key == "productId":
            continue
        old_val = old.get(key, "<missing>")
        new_val = new.get(key, "<missing>")
        if old_val != new_val:
            if key == "filters":
                old_filters = old_val if isinstance(old_val, dict) else {}
                new_filters = new_val if isinstance(new_val, dict) else {}
                filter_keys = set(old_filters.keys()) | set(new_filters.keys())
                for fk in sorted(filter_keys):
                    ov = old_filters.get(fk, "<missing>")
                    nv = new_filters.get(fk, "<missing>")
                    if ov != nv:
                        changes.append((f"filters.{fk}", str(ov), str(nv)))
            else:
                changes.append((key, str(old_val), str(new_val)))
    return changes


def print_table(changes):
    col1 = max(len("Field"),  max(len(c[0]) for c in changes))
    col2 = max(len("Old"),    max(len(c[1]) for c in changes))
    col3 = max(len("New"),    max(len(c[2]) for c in changes))

    border  = f"  ┌─{'─'*col1}─┬─{'─'*col2}─┬─{'─'*col3}─┐"
    header  = f"  │ {'Field':<{col1}} │ {'Old':<{col2}} │ {'New':<{col3}} │"
    divider = f"  ├─{'─'*col1}─┼─{'─'*col2}─┼─{'─'*col3}─┤"
    footer  = f"  └─{'─'*col1}─┴─{'─'*col2}─┴─{'─'*col3}─┘"

    print(border)
    print(header)
    print(divider)
    for field, old_v, new_v in changes:
        print(f"  │ {field:<{col1}} │ {old_v:<{col2}} │ {new_v:<{col3}} │")
    print(footer)


def process_file(excel_path, data):
    vendors = data["vendors"]
    inserted = 0
    updated  = 0
    skipped  = 0
    errored  = 0
    save_needed = False

    print(f"\n  FILE       : {os.path.basename(excel_path)}")
    print("-" * 60)

    try:
        wb = openpyxl.load_workbook(excel_path, data_only=True)
        if "Json Output" not in wb.sheetnames:
            print(f"  ERROR      : Sheet 'Json Output' not found — skipping")
            print("=" * 60)
            return data, 0, 0, 0, 1, False
        raw = wb["Json Output"]["F1"].value
        if not raw:
            print(f"  ERROR      : Cell F1 is empty — skipping")
            print("=" * 60)
            return data, 0, 0, 0, 1, False
        cleaned = re.sub(r'[\x00-\x1f\x7f]', '', raw)
        if cleaned != raw:
            ctrl_changes = []
            offset = 0
            for i, c in enumerate(raw):
                if ord(c) < 0x20 or ord(c) == 0x7f:
                    start  = max(0, i - 25)
                    end    = min(len(raw), i + 26)
                    before_snip = raw[start:end].replace('\n', '↵').replace('\r', '↵').replace('\t', '→')
                    cs = max(0, start - offset)
                    ce = cs + (end - start - 1)
                    after_snip  = cleaned[cs:ce]
                    ctrl_changes.append((
                        f"\\x{ord(c):02x} (pos {i})",
                        before_snip,
                        after_snip
                    ))
                    offset += 1
            print(f"  CONTROL CHARS REMOVED (auto-fixed):")
            print_table(ctrl_changes)

        # ── Parse JSON from Excel cell ──────────────────────────
        try:
            vendor = json.loads(cleaned)
        except json.JSONDecodeError as e:
            print(f"  ERROR      : Cell F1 contains corrupt or invalid JSON.")
            print(f"               Reason : {e.msg}")
            print(f"               Line   : {e.lineno}, Column: {e.colno}")
            print(f"               Near   : {repr(e.doc[max(0, e.pos-20):e.pos+20])}")
            print("=" * 60)
            return data, 0, 0, 0, 1, False

    except Exception as e:
        print(f"  ERROR      : Failed to read Excel file — {e}")
        print("=" * 60)
        return data, 0, 0, 0, 1, False

    # Trim spaces
    trimmed = trim_and_report(vendor)
    if trimmed:
        print(f"  SPACES TRIMMED (auto-fixed):")
        for t in trimmed:
            print(t)
    else:
        print(f"  VALIDATION : No leading/trailing spaces found")

    # Validate and fix website
    validate_and_fix_website(vendor)

    name = vendor.get("name", "").strip()
    if not name:
        print(f"  ERROR      : \"name\" field is missing or empty — skipping")
        print("=" * 60)
        return data, 0, 0, 0, 1, False

    match = next((i for i, v in enumerate(vendors) if v["name"].strip().lower() == name.lower()), None)

    if match is None:
        new_id = max(v["productId"] for v in vendors if "productId" in v) + 3
        vendors.append(ordered_vendor(vendor, new_id))
        inserted = 1
        save_needed = True
        print(f"  ACTION     : INSERTED (new vendor)")
        print(f"  productId  : {new_id}")
        print(f"  name       : {name}")
    else:
        old_vendor = vendors[match]
        pid = old_vendor["productId"]
        new_vendor = ordered_vendor(vendor, pid)
        changes = print_diff(old_vendor, new_vendor)
        print(f"  productId  : {pid}")
        print(f"  name       : {name}")
        if changes:
            vendors[match] = new_vendor
            updated = 1
            save_needed = True
            print(f"  ACTION     : UPDATED")
            print(f"  Differences found:")
            print_table(changes)
        else:
            skipped = 1
            print(f"  ACTION     : No differences found — skipped")

    print("=" * 60)

    data["vendors"] = vendors
    return data, inserted, updated, skipped, 0, save_needed


# ── MAIN ──────────────────────────────────────────────────
folder = sys.argv[1].strip().strip("'\"") if len(sys.argv) > 1 else INBOX_DIR

if not os.path.isdir(folder):
    print(f"Folder not found: {folder}")
    sys.exit(1)

excel_files = sorted(f for f in glob.glob(os.path.join(folder, "*.xlsx"))
                     if not os.path.basename(f).startswith("~$"))
if not excel_files:
    print(f"No .xlsx files found in: {folder}")
    sys.exit(0)

print("=" * 60)
print(f"  VENDOR PROCESSOR")
print(f"  Inbox      : {folder}")
print(f"  Files found: {len(excel_files)}")
print("=" * 60)

if not os.path.exists(VENDORS_JSON):
    print(f"ERROR: '{VENDORS_JSON}' does not exist. Ensure the file is present before running this script.")
    sys.exit(1)

try:
    with open(VENDORS_JSON, encoding="utf-8") as f:
        data = json.load(f)
except json.JSONDecodeError as e:
    print(f"ERROR: '{VENDORS_JSON}' is corrupt or contains invalid JSON.")
    print(f"       Reason : {e.msg}")
    print(f"       Line   : {e.lineno}, Column: {e.colno}")
    print(f"       Near   : {repr(e.doc[max(0, e.pos-20):e.pos+20])}")
    sys.exit(1)

total_inserted = 0
total_updated  = 0
total_skipped  = 0
total_errored  = 0
any_save       = False

for excel_path in excel_files:
    data, ins, upd, skp, err, save_needed = process_file(excel_path, data)
    total_inserted += ins
    total_updated  += upd
    total_skipped  += skp
    total_errored  += err
    if save_needed:
        any_save = True

if any_save:
    lines = ["    " + json.dumps(v, ensure_ascii=False, separators=(",", ": ")) for v in data["vendors"]]
    with open(VENDORS_JSON, "w", encoding="utf-8") as f:
        f.write("{\n")
        f.write(f'  "lastUpdated": {json.dumps(data["lastUpdated"])},\n')
        f.write('  "vendors": [\n')
        f.write(",\n".join(lines))
        f.write("\n  ]\n}")

print(f"\n{'=' * 60}")
print(f"  SUMMARY")
print(f"  Files processed : {len(excel_files)}")
print(f"  Inserted        : {total_inserted}")
print(f"  Updated         : {total_updated}")
print(f"  Skipped         : {total_skipped}")
print(f"  Errored         : {total_errored}")
print("=" * 60)