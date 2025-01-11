#! /bin/python3

from hashlib import sha256
import base64
import sys

if len(sys.argv) < 2:
    print("""\
This script will print out your Tox and Database passwords for TRIfA Android

Usage:  ./calc_tox_and_db_password_hash.py <your password>
""")
    exit(0)

password_clear_text = sys.argv[1]

pass_str_as_bytes = bytes(password_clear_text, 'utf8')
h = sha256()
h.update(pass_str_as_bytes)
d = h.digest()
db_pass_hash_str = base64.b64encode(d)

print(" db passhash:" + db_pass_hash_str.decode("utf-8"))

password_str_as_bytes_for_tox = db_pass_hash_str

h2 = sha256()
h2.update(password_str_as_bytes_for_tox)
d2 = h2.digest()
tox_pass_hash_str = base64.b64encode(d2)

print("tox passhash:" + tox_pass_hash_str.decode("utf-8"))
