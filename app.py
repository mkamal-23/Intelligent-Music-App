from flask import Flask, request, jsonify
from firebase_config import init_firebase
from model import fetch_all_histories, build_cooccurrence_matrix, recommend

app = Flask(__name__)
db = init_firebase()

@app.route("/recommend", methods=["POST"])
def get_recommendations():
    data = request.json
    user_id = data.get("userId")

    if not user_id:
        return jsonify({"error": "userId required"}), 400

    # Us user ki history fetch karo
    history_ref = db.collection("Users").document(user_id)\
                    .collection("history").stream()
    
    user_history = []
    for record in history_ref:
        rec = record.to_dict()
        if rec.get("duration", 0) >= 30000:  # 30 sec filter
            user_history.append(rec["songId"])

    if not user_history:
        return jsonify({"recommendations": []}), 200

    # Sabki history se co-occurrence matrix banao
    all_histories = fetch_all_histories(db)
    cooccurrence = build_cooccurrence_matrix(all_histories)

    # Recommend karo
    result = recommend(user_history, cooccurrence, top_n=10)

    return jsonify({"recommendations": result})

if __name__ == "__main__":
    app.run(debug=True)
