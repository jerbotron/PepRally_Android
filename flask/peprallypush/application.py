from flask import Flask, request
import urllib2, json
application = Flask(__name__)

API_ACCESS_KEY = "key=AIzaSyDH7MFAWAq9tFKTaGQYjVp5trBD2ZEORT8"
SENDER_ID = 508839522244
FCM_URL = 'https://gcm-http.googleapis.com/gcm/send'
test_token = ["eo7NkHsfdYo:APA91bHG2hVMxyh98A0lPX5zEbLclSY9HvjhQlQbFKLaaKkeTB6OwjgMZhpIMt5e-ge4HZUtOjOw0TgFQD88Mk1lxFVmyK2qOeUyl9Ues1SKRVbQddpYL_MLhYed7jMhe6B_vtBRFPg-"]


@application.route('/')
def index():
    return 'PepRally Push Notification Server'

@application.route('/send', methods=['GET', 'POST'])
def test():
    if request.method == 'POST':
        print "posting something"
        send_fcm_notification("PepRally", "new notification is here", test_token)
    else:
        print "not posting"
    return 'testing\n'

def send_fcm_notification(title, body, reg_tokens):
    post_msg = {
        'data': {
            'title': title,
            'body': body
        },
        'registration_ids': reg_tokens
    }

    post_header = {
        'Authorization:': API_ACCESS_KEY,
        'Content-Type:': 'application/json'
    }
    data = json.dumps(post_msg)
    handler = urllib2.HTTPHandler()
    opener = urllib2.build_opener(handler)
    request = urllib2.Request(FCM_URL, data=data)
    request.add_header('Content-Type', 'application/json')
    request.add_header('Authorization', 'key=AIzaSyDH7MFAWAq9tFKTaGQYjVp5trBD2ZEORT8')
    try:
        urllib2.urlopen(request)
    except urllib2.HTTPError,e:
        print e;


if __name__ == "__main__":
    application.run()