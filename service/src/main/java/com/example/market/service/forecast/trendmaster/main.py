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

from_date='2025-01-01',
to_date='2025-12-01'
pd_from = pd.to_datetime(from_date)
pd_to = pd.to_datetime(to_date)
data_loader = DataLoader() # TODO: NEED

# Create joblib file from json
stock_data_json = "stock_daily.json"
with open(stock_data_json, 'r') as fh:
    result_dict = json.load(fh)

df_data = {"close": [], "date": []}
for bar in result_dict['bars']:
    # check if bar is within timestamp
    ts = pd.to_datetime(bar["timestamp"])
    if pd_from <= ts <= pd_to:
        df_data["close"].append(bar["close"])
        df_data["date"].append(bar["timestamp"])
df = pd.DataFrame(df_data)
df['date'] = pd.to_datetime(df['date'])
df.set_index('date', inplace=True)
filename = "STOCK_data.joblib"
joblib.dump(df, filename)

# Initialize model, trainer, and train the model
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu') # NEED
model = TransAm()
model.load_state_dict(torch.load("service/src/main/java/com/example/market/service/forecast/trendmaster/model_state.pt"))

# Initialize inferencer and make predictions
inferencer = Inferencer(model, device, data_loader) #TODO
predictions = inferencer.predict(
    symbol='STOCK',
    from_date=from_date,
    to_date=to_date,
    input_window=30,
future_steps=20
)
#print(predictions.to_json(), flush=True)
predictions['Date'] = predictions['Date'].dt.strftime('%Y-%m-%d')
json_str = json.dumps(predictions.to_json(date_format='iso'), indent=4)
print(json_str)
