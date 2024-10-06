from fastapi import FastAPI

app = FastAPI()

@app.get("/predict")
async def predict(input_text: str):
    return {"output": "This is a dummy response"}
