from flask import Flask, request
import urllib2, json
application = Flask(__name__)

API_ACCESS_KEY = "key=AIzaSyDH7MFAWAq9tFKTaGQYjVp5trBD2ZEORT8"
SENDER_ID = 508839522244
FCM_URL = 'https://gcm-http.googleapis.com/gcm/send'

@application.route('/')
def playerIndex():
    print "sanity check"
    return 'PepRally Push Notification Server'

@application.route('/send', methods=['GET', 'POST'])
def test():
    if request.method == 'POST':
        print "POST REQUEST RECEIVED"
        data = request.get_json()
        # print data
        send_fcm_notification(data)
    else:
        print "NOT POSTING"
    return json.dumps({'test_key': 'test_value'})

def send_fcm_notification(jsonData):
    post_msg = {
        'data': jsonData,
        'registration_ids': [jsonData['receiver_id']]
    }

    # post_header = {
    #     'Authorization:': API_ACCESS_KEY,
    #     'Content-Type:': 'application/json'
    # }
    data = json.dumps(post_msg)
    handler = urllib2.HTTPHandler()
    opener = urllib2.build_opener(handler)
    request = urllib2.Request(FCM_URL, data=data)
    request.add_header('Content-Type', 'application/json')
    request.add_header('Authorization', API_ACCESS_KEY)
    try:
        urllib2.urlopen(request)
    except urllib2.HTTPError,e:
        print e;


if __name__ == "__main__":
    application.run()
