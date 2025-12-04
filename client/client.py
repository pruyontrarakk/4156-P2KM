#!/usr/bin/env python3
import os
import sys
import uuid
import requests
from textwrap import dedent

DEFAULT_HORIZON = 10

def get_base_url() -> str:
    base = os.getenv("SERVICE_BASE_URL")
    if not base:
        print("ERROR: Please set SERVICE_BASE_URL, e.g.")
        print("  export SERVICE_BASE_URL=http://localhost:8080")
        sys.exit(1)
    # strip trailing slash
    return base.rstrip("/")

def get_client_id() -> str:
    env_id = os.getenv("CLIENT_ID")
    if env_id and env_id.strip():
        return env_id.strip()
    # fallback: random id
    return f"client-{uuid.uuid4().hex[:8]}"

def print_intro(base_url: str, client_id: str):
    print(dedent(f"""
    ======================================================
      Sentiment-Adjusted Stock Prediction Client
    ======================================================
      Service URL : {base_url}
      Client ID   : {client_id}
    """).strip())

def ask_symbol_and_horizon():
    symbol = input("Enter stock symbol (e.g. AAPL, TSLA): ").strip().upper()
    if not symbol:
        print("Symbol cannot be empty.")
        sys.exit(1)

    horizon_str = input(f"Enter prediction horizon in days [{DEFAULT_HORIZON}]: ").strip()
    if not horizon_str:
        horizon = DEFAULT_HORIZON
    else:
        try:
            horizon = int(horizon_str)
            if horizon <= 0:
                raise ValueError()
        except ValueError:
            print("Horizon must be a positive integer.")
            sys.exit(1)

    return symbol, horizon

def call_endpoint(base_url: str, path: str, params: dict, client_id: str):
    url = f"{base_url}{path}"
    headers = {
        "X-Client-Id": client_id
    }
    resp = requests.get(url, params=params, headers=headers, timeout=30)
    try:
        data = resp.json()
    except Exception:
        print("ERROR: Service did not return valid JSON.")
        print("Status:", resp.status_code)
        print("Body:", resp.text[:500])
        sys.exit(1)

    if resp.status_code != 200:
        print("Service returned error status:", resp.status_code)
        print("Response JSON:", data)
        sys.exit(1)

    return data

def run_client():
    base_url = get_base_url()
    client_id = get_client_id()

    print_intro(base_url, client_id)
    symbol, horizon = ask_symbol_and_horizon()

    print(f"\nRequesting sentiment-adjusted prediction for {symbol} (horizon={horizon})...\n")

    combined = call_endpoint(
        base_url,
        "/market/combined-prediction",
        {"symbol": symbol, "horizon": horizon},
        client_id=client_id,
    )

    # Optional: you can also fetch raw daily + sentiment if you like:
    # daily = call_endpoint(base_url, "/market/daily", {"symbol": symbol}, client_id)
    # sentiment = call_endpoint(base_url, "/market/sentiment", {"symbol": symbol}, client_id)

    sentiment = combined.get("sentiment", {})
    original = combined.get("originalPredictions", {})
    adjusted = combined.get("adjustedPredictions", {})

    print(f"Sentiment for {symbol}: {sentiment.get('label')} (score={sentiment.get('score')})\n")

    # Join dates from adjusted/original predictions
    all_dates = sorted(set(list(original.keys()) + list(adjusted.keys())))

    print("Date        | Original Close | Adjusted (sentiment-aware)")
    print("----------- | -------------- | --------------------------")
    for d in all_dates:
        orig = original.get(d, "")
        adj = adjusted.get(d, "")
        print(f"{d} | {orig:>14} | {adj:>26}")

    print("\nDone.\n")
    print("You can run multiple clients simultaneously by opening new shells,")
    print("setting different CLIENT_ID values, and pointing them at the same SERVICE_BASE_URL.")

if __name__ == "__main__":
    run_client()
