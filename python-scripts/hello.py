from flask import Flask, request

app = Flask(__name__)

@app.route('/hello', methods=['GET'])
def hello():
    name = request.args.get('name', 'World')
    return f"Hello, {name} from Python!"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)