#!/usr/bin/env python3
"""Appwrite backend setup — RPI Student App (rpi_main database + collections + seed).
Usage: APPWRITE_KEY=... python3 appwrite_setup.py
Idempotent: existing collections/attributes/docs → skip (409).
"""
import json
import os
import sys
import time
import urllib.error
import urllib.request

ENDPOINT = os.environ.get("APPWRITE_ENDPOINT", "https://sgp.cloud.appwrite.io/v1")
PROJECT = os.environ.get("APPWRITE_PROJECT", "6b091cf5002c71f31ffb")
KEY = os.environ["APPWRITE_KEY"]

DB = "rpi_main"

def call(method: str, path: str, payload: dict | None = None) -> tuple[int, dict]:
    url = f"{ENDPOINT}{path}"
    req = urllib.request.Request(url, method=method)
    req.add_header("X-Appwrite-Project", PROJECT)
    req.add_header("X-Appwrite-Key", KEY)
    req.add_header("Content-Type", "application/json")
    data = json.dumps(payload).encode() if payload is not None else None
    try:
        with urllib.request.urlopen(req, data) as r:
            body = r.read().decode() or "{}"
            return r.status, json.loads(body)
    except urllib.error.HTTPError as e:
        body = e.read().decode() or "{}"
        try:
            return e.code, json.loads(body)
        except Exception:
            return e.code, {"message": body[:300]}

def ok(s: int) -> bool:
    return 200 <= s < 300

# ---------- verify project ----------
s, proj = call("GET", f"/projects/{PROJECT}")
if not ok(s):
    print(f"FATAL: project {PROJECT} not reachable (HTTP {s}): {proj.get('message')}")
    sys.exit(1)
print(f"project OK: {proj.get('name')}")

# ---------- android platform ----------
s, _ = call("POST", f"/projects/{PROJECT}/platforms", {
    "platformId": "android-app",
    "type": "android",
    "label": "RPI Student App (Android)",
    "key": "com.rpi.student",
})
print(f"android platform: HTTP {s} (409 = already there)")

# ---------- database ----------
s, _ = call("POST", "/databases", {"databaseId": DB, "name": "RPI Main", "enabled": True})
print(f"database {DB}: HTTP {s}")

READ_USERS = 'read("users")'
CREATE_USERS = 'create("users")'
UPDATE_USERS = 'update("users")'
DELETE_USERS = 'delete("users")'
READ_ANY = 'read("any")'

# (id, name, permissions, documentSecurity, attributes, indexes)
# attr: (type, key, size/None, required, default/None)
COLLECTIONS = [
    ("profiles", "Profiles", [READ_ANY, CREATE_USERS, UPDATE_USERS, DELETE_USERS], True, [
        ("string", "userId", 64, True, None),
        ("string", "name", 128, True, None),
        ("string", "email", 256, True, None),
        ("string", "roll", 32, False, None),
        ("string", "registration", 32, False, None),
        ("string", "session", 32, False, None),
        ("string", "technology", 64, False, None),
        ("integer", "semester", None, False, None),
        ("string", "shift", 16, False, None),
        ("string", "section", 8, False, None),
        ("string", "busRoute", 64, False, None),
        ("boolean", "messMember", None, False, False),
        ("string", "badge", 16, False, "none"),
        ("string", "status", 16, False, "pending"),
    ], [("userId_unique", "unique", ["userId"])]),

    ("groups", "Groups", [READ_USERS], False, [
        ("string", "groupId", 64, True, None),
        ("string", "name", 128, True, None),
        ("string", "kind", 16, True, None),
        ("boolean", "nonLeaveable", None, False, True),
        ("boolean", "autoJoin", None, False, True),
        ("string", "description", 512, False, None),
        ("integer", "memberCount", None, False, 0),
        ("integer", "sortOrder", None, False, 0),
    ], [("groupId_unique", "unique", ["groupId"])]),

    ("hot_messages", "Hot Messages", [READ_USERS, CREATE_USERS], False, [
        ("string", "groupId", 64, True, None),
        ("string", "userId", 64, True, None),
        ("string", "userName", 128, True, None),
        ("string", "body", 2000, True, None),
    ], [("by_group", "key", ["groupId"])]),

    ("notices", "Notices", [READ_USERS], False, [
        ("string", "title", 200, True, None),
        ("string", "body", 10000, True, None),
        ("string", "technology", 64, False, "All"),
        ("string", "authorName", 128, False, "Admin"),
        ("integer", "semester", None, False, None),
    ], []),

    ("subjects", "Subjects", [READ_USERS], False, [
        ("string", "code", 32, True, None),
        ("string", "name", 256, True, None),
        ("string", "technology", 64, True, None),
        ("integer", "regulation", None, True, None),
        ("integer", "semester", None, True, None),
        ("double", "credit", None, True, None),
        ("boolean", "hasTheory", None, False, False),
        ("integer", "theoryFull", None, False, None),
        ("boolean", "hasPractical", None, False, False),
        ("integer", "practicalFull", None, False, None),
        ("integer", "totalFull", None, False, None),
        ("integer", "periodsTheory", None, False, None),
        ("integer", "periodsPractical", None, False, None),
        ("string", "source", 256, False, "btebresultszone"),
    ], [("by_tech_reg_sem", "key", ["technology", "regulation", "semester"])]),

    ("routines", "Routines", [READ_USERS], False, [
        ("string", "technology", 64, True, None),
        ("integer", "semester", None, True, None),
        ("string", "shift", 16, True, None),
        ("string", "section", 8, True, None),
        ("integer", "day", None, True, None),
        ("string", "startTime", 8, True, None),
        ("string", "endTime", 8, True, None),
        ("string", "subjectCode", 32, True, None),
        ("string", "subjectName", 256, True, None),
        ("string", "room", 32, False, None),
        ("string", "teacher", 128, False, None),
    ], [("by_class", "key", ["technology", "semester", "shift", "section", "day"])]),

    ("results", "Results", [READ_USERS, CREATE_USERS, UPDATE_USERS], False, [
        ("string", "userId", 64, True, None),
        ("string", "technology", 64, True, None),
        ("integer", "semester", None, True, None),
        ("string", "examName", 128, True, None),
        ("string", "subjectCode", 32, True, None),
        ("string", "subjectName", 256, True, None),
        ("double", "credit", None, True, None),
        ("integer", "marksTheory", None, False, None),
        ("integer", "marksPractical", None, False, None),
        ("string", "grade", 8, True, None),
        ("double", "gradePoint", None, True, None),
        ("double", "gpa", None, True, None),
    ], [("by_user", "key", ["userId"])]),

    ("exam_routines", "Exam Routines", [READ_USERS], False, [
        ("string", "technology", 64, True, None),
        ("integer", "semester", None, True, None),
        ("string", "shift", 16, False, None),
        ("string", "examDate", 32, True, None),
        ("string", "time", 32, True, None),
        ("string", "subjectCode", 32, True, None),
        ("string", "subjectName", 256, True, None),
        ("string", "room", 32, False, None),
    ], []),

    ("bus_routes", "Bus Routes", [READ_USERS], False, [
        ("string", "routeName", 128, True, None),
        ("string", "shift", 16, True, None),
        ("string", "stopName", 128, True, None),
        ("integer", "stopOrder", None, True, None),
        ("string", "time", 8, True, None),
        ("string", "notice", 512, False, None),
    ], []),

    ("mess_entries", "Mess Entries", [READ_USERS, CREATE_USERS, UPDATE_USERS], False, [
        ("string", "userId", 64, True, None),
        ("string", "messName", 128, False, None),
        ("string", "date", 16, True, None),
        ("integer", "meals", None, True, None),
        ("boolean", "isBazar", None, False, False),
        ("double", "amount", None, False, 0),
        ("string", "note", 256, False, None),
    ], [("by_date", "key", ["date"])]),

    ("mess_deposits", "Mess Deposits", [READ_USERS, CREATE_USERS, UPDATE_USERS], False, [
        ("string", "userId", 64, True, None),
        ("string", "messName", 128, False, None),
        ("string", "date", 16, True, None),
        ("double", "amount", None, True, None),
        ("string", "note", 256, False, None),
    ], []),

    ("pomo_sessions", "Pomodoro Sessions", [READ_USERS, CREATE_USERS], False, [
        ("string", "userId", 64, True, None),
        ("string", "preset", 32, True, None),
        ("string", "subject", 128, False, None),
        ("integer", "minutes", None, True, None),
        ("boolean", "completed", None, False, True),
    ], [("by_user", "key", ["userId"])]),

    ("app_config", "App Config", [READ_USERS], False, [
        ("string", "key", 64, True, None),
        ("string", "value", 10000, True, None),
    ], [("key_unique", "unique", ["key"])]),

    ("change_requests", "Change Requests", [READ_USERS, CREATE_USERS], False, [
        ("string", "userId", 64, True, None),
        ("string", "field", 64, True, None),
        ("string", "oldValue", 1000, False, None),
        ("string", "newValue", 1000, True, None),
        ("string", "status", 16, False, "pending"),
    ], []),

    ("audit_log", "Audit Log", [], False, [
        ("string", "actor", 64, True, None),
        ("string", "action", 128, True, None),
        ("string", "detail", 4000, False, None),
    ], []),
]

def attr_exists(col: str, key: str) -> bool:
    s, attrs = call("GET", f"/databases/{DB}/collections/{col}/attributes")
    if ok(s):
        for a in attrs.get("attributes", []):
            if a.get("key") == key:
                return True
    return False

def create_collection(col, name, perms, doc_sec, attrs, indexes):
    s, r = call("POST", f"/databases/{DB}/collections", {
        "collectionId": col,
        "name": name,
        "permissions": perms,
        "documentSecurity": doc_sec,
    })
    if not ok(s) and s != 409:
        print(f"  ✗ {col}: create HTTP {s}: {r.get('message')}")
        return
    print(f"  collection {col}: {'created' if ok(s) else 'exists'}")

    for (typ, key, size, required, default) in attrs:
        if attr_exists(col, key):
            continue
        if typ == "string":
            payload = {"key": key, "size": size, "required": required}
            if default is not None:
                payload["default"] = default
            path = f"/databases/{DB}/collections/{col}/attributes/string"
        elif typ == "integer":
            payload = {"key": key, "required": required}
            if default is not None:
                payload["default"] = default
            path = f"/databases/{DB}/collections/{col}/attributes/integer"
        elif typ == "double":
            # Appwrite 2.x: route = attributes/float (type stays "double")
            payload = {"key": key, "required": required}
            if default is not None:
                payload["default"] = float(default)
            path = f"/databases/{DB}/collections/{col}/attributes/float"
        else:  # boolean
            payload = {"key": key, "required": required}
            if default is not None:
                payload["default"] = default
            path = f"/databases/{DB}/collections/{col}/attributes/boolean"
        s2, r2 = call("POST", path, payload)
        if not ok(s2) and s2 != 409:
            print(f"    ✗ attr {key}: HTTP {s2}: {r2.get('message')}")

    # wait for attributes to become available, then indexes
    for (iname, itype, iattrs) in indexes:
        for attempt in range(12):
            s3, r3 = call("POST", f"/databases/{DB}/collections/{col}/indexes", {
                "key": iname, "type": itype, "attributes": iattrs,
            })
            if ok(s3) or s3 == 409:
                break
            # attribute still processing → wait and retry
            if "attribute" in str(r3.get("message", "")).lower() or s3 == 400:
                time.sleep(2)
            else:
                break
        print(f"    index {iname}: HTTP {s3}")

for c in COLLECTIONS:
    create_collection(*c)

# ---------- seed: 4 auto-join groups ----------
GROUPS_SEED = [
    ("g-all", "All in One — RPI", "ALL", "পুরো পলিটেকনিকের কমন গ্রুপ — সবার জন্য", 1),
    ("g-family", "Family — নিজের টেকনোলজি", "FAMILY", "নিজের technology-র সিনিয়র-জুনিয়র পরিবার", 2),
    ("g-own", "নিজের ক্লাস গ্রুপ", "OWN", "নিজের semester/shift/section ক্লাসমেট গ্রুপ", 3),
    ("g-bus", "BUS — রুট গ্রুপ", "BUS", "নিজের বাস রুটের গ্রুপ — রুটিন/দেরি নোটিস", 4),
]
print("seed groups:")
for gid, name, kind, desc, order in GROUPS_SEED:
    s, r = call("POST", f"/databases/{DB}/collections/groups/documents", {
        "documentId": gid,
        "data": {
            "groupId": gid, "name": name, "kind": kind,
            "nonLeaveable": True, "autoJoin": True,
            "description": desc, "memberCount": 0, "sortOrder": order,
        },
        "permissions": ['read("users")', 'update("users")', 'delete("users")'],
    })
    print(f"  {gid}: HTTP {s} (409 = exists)")

# ---------- seed: app_config ----------
CONFIG_SEED = [
    ("app_name", "RPI Student"),
    ("org_name", "Rangpur Polytechnic Institute"),
    ("media_share_enabled", "false"),
    ("min_supported_version_code", "1"),
]
print("seed app_config:")
for k, v in CONFIG_SEED:
    s, r = call("POST", f"/databases/{DB}/collections/app_config/documents", {
        "documentId": k.replace("_", "-"),
        "data": {"key": k, "value": v},
    })
    print(f"  {k}: HTTP {s} (409 = exists)")

print("DONE — Appwrite backend ready")
