from RealtimeSTT import AudioToTextRecorder
import requests

def handle_text(text):
    print("Transcript:", text)
    # Example: send to an AI endpoint (pseudo-code)
    response = requests.post("https://api.copilot.microsoft.com/query", json={"input": text})
    print("AI Response:", response.json())

if __name__ == '__main__':
    # Initialize RealtimeSTT
    stt = AudioToTextRecorder(model="medium", language="en")
    # Start transcription with callback
    while True:
        stt.text(on_transcription_finished=handle_text)