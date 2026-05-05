import pandas as pd
import numpy as np
from sklearn.preprocessing import LabelEncoder
from collections import defaultdict

MIN_DURATION_MS = 30000  # 30 seconds se kam suna toh ignore

def fetch_all_histories(db):
    """Firestore se sabka history fetch karo"""
    users_ref = db.collection("Users").stream()
    all_data = []

    for user in users_ref:
        history_ref = db.collection("Users").document(user.id)\
                        .collection("history").stream()
        songs = []
        for record in history_ref:
            data = record.to_dict()
            if data.get("duration", 0) >= MIN_DURATION_MS:
                songs.append(data["songId"])
        
        if len(songs) > 1:
            all_data.append(songs)
    
    return all_data

def build_cooccurrence_matrix(all_histories):
    """
    Co-occurrence matrix banao:
    Agar user ne A ke baad B suna → A aur B saath occur hue
    """
    cooccurrence = defaultdict(lambda: defaultdict(int))

    for history in all_histories:
        for i in range(len(history)):
            for j in range(i + 1, min(i + 5, len(history))):  # agle 4 songs tak
                song_a = history[i]
                song_b = history[j]
                cooccurrence[song_a][song_b] += 1
                cooccurrence[song_b][song_a] += 1

    return cooccurrence

def recommend(user_history, cooccurrence, top_n=10):
    """
    User ki recent history ke basis par recommend karo
    """
    if not user_history:
        return []

    # Recent 5 songs zyada important hain
    recent_songs = user_history[-5:]
    already_listened = set(user_history)

    score = defaultdict(float)

    for i, song in enumerate(recent_songs):
        weight = (i + 1) / len(recent_songs)  # recent song ko zyada weight
        if song in cooccurrence:
            for related_song, count in cooccurrence[song].items():
                if related_song not in already_listened:
                    score[related_song] += count * weight

    # Top N recommendations
    recommended = sorted(score.items(), key=lambda x: x[1], reverse=True)
    return [song_id for song_id, _ in recommended[:top_n]]
