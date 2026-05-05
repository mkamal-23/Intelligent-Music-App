import firebase_admin
from firebase_admin import credentials, firestore
import os
import json

def init_firebase():
    cred_json = os.environ.get("GOOGLE_APPLICATION_CREDENTIALS_JSON")
    cred_dict = json.loads(cred_json)
    cred = credentials.Certificate(cred_dict)
    firebase_admin.initialize_app(cred)
    return firestore.client()
