"""
curl -H "Content-Type: application/json" -d '{"receiver_id":"eo7NkHsfdYo:APA91bHG2hVMxyh98A0lPX5zEbLclSY9HvjhQlQbFKLaaKkeTB6OwjgMZhpIMt5e-ge4HZUtOjOw0TgFQD88Mk1lxFVmyK2qOeUyl9Ues1SKRVbQddpYL_MLhYed7jMhe6B_vtBRFPg-","receiver_nickname":"test", "sender_nickname":"jeremy", "post_text":"hello"}' ec2-107-21-196-112.compute-1.amazonaws.com/send 

tail -f /var/log/apache2/error.log

ssh -i ~/Projects/ssh_keys/peprally_ec2_keypair.pem ubuntu@ec2-107-21-196-112.compute-1.amazonaws.com

// re-route port 80 to port 8080 so the public web can have access to the server
// listening at port 8080
iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to 8080
"""

import json

data = {}

data['key'] = "value"

json_data = json.dumps(data)

print data
print json_data