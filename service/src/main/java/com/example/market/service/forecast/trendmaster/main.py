from trendmaster import (
    DataLoader,
    TransAm,
    Trainer,
    Inferencer
)
import torch
import json
import matplotlib
import joblib
import os
import pandas as pd

matplotlib.use('Agg')
import matplotlib.pyplot as plt
plt.ioff()

# Dates for prediction window
from_date = '2025-01-01'
to_date = '2025-12-01'
pd_from = pd.to_datetime(from_date)
pd_to = pd.to_datetime(to_date)

# Data loader (TrendMaster utility)
data_loader = DataLoader()  # TODO: adjust if your version needs args

# Create joblib file from json
stock_data_json = "stock_daily.json"
with open(stock_data_json, 'r') as fh:
    result_dict = json.load(fh)

df_data = {"close": [], "date": []}
for bar in result_dict['bars']:
    ts = pd.to_datetime(bar["timestamp"])
    if pd_from <= ts <= pd_to:
        df_data["close"].append(bar["close"])
        df_data["date"].append(bar["timestamp"])

df = pd.DataFrame(df_data)
df['date'] = pd.to_datetime(df['date'])
df.set_index('date', inplace=True)
filename = "STOCK_data.joblib"
joblib.dump(df, filename)

# Initialize model and load weights relative to this script's directory
script_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(script_dir, "model_state.pt")

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
model = TransAm()
model.load_state_dict(torch.load(model_path, map_location="cpu"))

# Initialize inferencer and make predictions
inferencer = Inferencer(model, device, data_loader)
predictions = inferencer.predict(
    symbol='STOCK',
    from_date=from_date,
    to_date=to_date,
    input_window=30,
future_steps=10
)

# Format and print JSON for Java to consume
predictions['Date'] = predictions['Date'].dt.strftime('%Y-%m-%d')
json_str = json.dumps(predictions.to_json(date_format='iso'), indent=4)
print(json_str)

