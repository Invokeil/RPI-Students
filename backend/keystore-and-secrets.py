#!/usr/bin/env python3
"""Generate release keystore + upload GitHub Actions secrets (Invokeil/RPI-Students).
Usage: GITHUB_PAT=... python3 keystore_and_secrets.py
Keystore + passwords saved to download/ for owner backup (NEVER committed).
"""
import base64
import os
import secrets as pysecrets
import subprocess
import sys
import time
import urllib.request
import json

PAT = os.environ["GITHUB_PAT"]
REPO = "Invokeil/RPI-Students"
API = f"https://api.github.com/repos/{REPO}"

OUT_DIR = "/home/z/my-project/download"
KS_PATH = os.path.join(OUT_DIR, "rpi-students-release.keystore")
INFO_PATH = os.path.join(OUT_DIR, "keystore-backup-info.txt")

ALIAS = "rpi-app"
KS_PASSWORD = pysecrets.token_urlsafe(18)
KEY_PASSWORD = pysecrets.token_urlsafe(18)

def gh(method: str, path: str, payload: dict | None = None) -> tuple[int, dict]:
    url = path if path.startswith("http") else f"{API}{path}"
    req = urllib.request.Request(url, method=method)
    req.add_header("Authorization", f"Bearer {PAT}")
    req.add_header("Accept", "application/vnd.github+json")
    data = json.dumps(payload).encode() if payload is not None else None
    if data:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, data) as r:
            body = r.read().decode() or "{}"
            return r.status, json.loads(body)
    except urllib.error.HTTPError as e:
        body = e.read().decode() or "{}"
        try:
            return e.code, json.loads(body)
        except Exception:
            return e.code, {"message": body}

# 1) generate keystore
print("[1/4] generating keystore (PKCS12, validity 10000 days)...")
subprocess.run([
    "keytool", "-genkeypair", "-v",
    "-keystore", KS_PATH,
    "-storetype", "PKCS12",
    "-alias", ALIAS,
    "-keyalg", "RSA", "-keysize", "2048",
    "-validity", "10000",
    "-storepass", KS_PASSWORD,
    "-keypass", KEY_PASSWORD,
    "-dname", "CN=RPI Student App, OU=Students, O=Invokeil, L=Rangpur, ST=Rangpur, C=BD",
], check=True, capture_output=True)

# 2) keystore → base64
print("[2/4] encoding base64...")
ks_b64 = base64.b64encode(open(KS_PATH, "rb").read()).decode()

# 3) repo public key for secrets encryption
print("[3/4] fetching repo secrets public key...")
status, pk = gh("GET", "/actions/secrets/public-key")
if status != 200:
    print("FAILED to get public key:", pk)
    sys.exit(1)

from nacl import encoding, public as nacl_public

pk_obj = nacl_public.PublicKey(pk["key"].encode(), encoding.Base64Encoder())
sealed = nacl_public.SealedBox(pk_obj)

def put_secret(name: str, value: str):
    enc = sealed.encrypt(value.encode())
    payload = {
        "encrypted_value": base64.b64encode(enc).decode(),
        "key_id": pk["key_id"],
    }
    s, _ = gh("PUT", f"/actions/secrets/{name}", payload)
    print(f"    secret {name}: HTTP {s}")
    return s == 201 or s == 204

print("[4/4] uploading secrets...")
ok = all([
    put_secret("KEYSTORE_BASE64", ks_b64),
    put_secret("KEYSTORE_PASSWORD", KS_PASSWORD),
    put_secret("KEY_ALIAS", ALIAS),
    put_secret("KEY_PASSWORD", KEY_PASSWORD),
])

# 4) owner backup info
info = f"""RPI Student App — release signing backup (KEEP SAFE, এই ফাইল delete করবে না)
Generated: {time.strftime('%Y-%m-%d %H:%M:%S')}
Repo: {REPO}
Keystore file: {KS_PATH} (PKCS12)
Store password: {KS_PASSWORD}
Key alias: {ALIAS}
Key password: {KEY_PASSWORD}

⚠ সাইনিং কী হারালে পুরনো ইউজার OTA update ইনস্টল করতে পারবে না।
⚠ এই ফাইল আর keystore কখনো GitHub repo-তে commit করবে না।
"""
with open(INFO_PATH, "w") as f:
    f.write(info)
os.chmod(KS_PATH, 0o600)
os.chmod(INFO_PATH, 0o600)

print("ALL OK" if ok else "PARTIAL — check secret statuses above")
print(f"keystore: {KS_PATH}")
print(f"backup info: {INFO_PATH}")
