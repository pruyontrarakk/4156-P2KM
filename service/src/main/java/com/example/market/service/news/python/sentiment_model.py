import sys, json
from transformers import pipeline

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Missing text argument"}))
        sys.exit(1)

    # raw text from Java (may be long)
    text = sys.argv[1]

    # limit to avoid extremely long transformer input
    text = text[:500]

    model_name = "nlptown/bert-base-multilingual-uncased-sentiment"
    nlp = pipeline("sentiment-analysis", model=model_name)
    result = nlp(text)[0]

    label = result["label"]
    stars = int(label[0]) if label[0].isdigit() else 3

    sentimentLabel = (
        "very negative" if stars == 1 else
        "negative" if stars == 2 else
        "neutral" if stars == 3 else
        "positive" if stars == 4 else
        "very positive"
    )

    print(json.dumps({
        "sentimentScore": stars,
        "sentimentLabel": sentimentLabel
    }))

if __name__ == "__main__":
    main()