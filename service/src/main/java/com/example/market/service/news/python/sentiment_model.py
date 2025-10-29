import sys, json
from transformers import pipeline

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Missing company argument"}))
        sys.exit(1)

    company = sys.argv[1]
    model_name = "nlptown/bert-base-multilingual-uncased-sentiment"
    nlp = pipeline("sentiment-analysis", model=model_name)

    text = f"Recent financial news about {company} stock performance and market outlook."
    result = nlp(text)[0]
    label = result["label"]

    # Convert star rating (1–5) into 1–5 score + human-readable label
    try:
        stars = int(label[0])
    except ValueError:
        stars = 3

    sentimentScore = stars
    sentimentLabel = (
        "very negative" if stars == 1 else
        "negative" if stars == 2 else
        "neutral" if stars == 3 else
        "positive" if stars == 4 else
        "very positive"
    )

    output = {
        "company": company,
        "sentimentScore": sentimentScore,
        "sentimentLabel": sentimentLabel
    }
    print(json.dumps(output))

if __name__ == "__main__":
    main()