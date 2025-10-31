from trendmaster import (
    DataLoader,
    TransAm,
    Trainer,
    Inferencer
)
import torch
import json
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
plt.ioff()

from_date='2023-02-27',
to_date='2023-12-31'

data_loader = DataLoader() # TODO: NEED

# Initialize model, trainer, and train the model
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu') # NEED
model = TransAm()
model.load_state_dict(torch.load("src/main/java/com/example/market/service/forecast/trendmaster/model_state.pt"))

# Initialize inferencer and make predictions
inferencer = Inferencer(model, device, data_loader) #TODO
predictions = inferencer.predict(
    symbol='amazon',
    from_date=from_date,
    to_date=to_date,
    input_window=30,
    future_steps=10#hardcoded for iteration 1
)
#print(predictions.to_json(), flush=True)
predictions['Date'] = predictions['Date'].dt.strftime('%Y-%m-%d')
json_str = json.dumps(predictions.to_json(date_format='iso'), indent=4)
print(json_str)