"""
curl -H "Content-Type: application/json" -d '{"receiver_id":"c5jMkAqog3M:APA91bHHI-r2O1EQoRNafJ8QlZ_fON30kasU7q-OI89xtvlp3EUJ1cE1n_bHXs6AHR9oLfM1SjVbzAwiy-Vb8bySKpCw-C8Wjan1ROb_dk4ZCwYnN4-22hpuZR7Bm685sK0FeVCNQeb3","receiver_username":"test", "sender_username":"jeremy", "post_text":"hello"}' http://ec2-52-90-150-67.compute-1.amazonaws.com/send

curl -H "Content-Type: application/json" -d '{"MyKey":"hello"}' http://ec2-52-90-150-67.compute-1.amazonaws.com/send

tail -f /var/log/apache2/error.log

ssh -i ~/Projects/ssh_keys/peprally_ec2_keypair.pem ubuntu@ec2-107-21-196-112.compute-1.amazonaws.com

// re-route port 80 to port 8080 so the public web can have access to the server listening at port 8080
iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to 8080
"""

import json

data = {}

data['key'] = "value"

json_data = json.dumps(data)

print data
print json_data