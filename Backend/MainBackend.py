from flask import Flask, request
import os

app = Flask(__name__)

app_root = os.path.dirname(os.path.abspath(__file__))


@app.route('/upload', methods=['POST'])
def upload():
    upload_to = os.path.join(app_root, "images/predict/test")
    if not os.path.isdir(upload_to): os.mkdir(upload_to)

    file = request.files['photo']
    filename = file.filename
    dest = "/".join([upload_to, filename])
    file.save(dest)

    return "{\"serverResponse\":\"Healthy\"}"

if __name__ == '__main__':
    app.run()
