import urllib, urllib2, boto3, json
from teamParsers import *

dynamodb = boto3.resource('dynamodb', region_name='us-east-1', endpoint_url="https://dynamodb.us-east-1.amazonaws.com")

table = dynamodb.Table("UserPosts")

testPost = {
  "CognitoId": "us-east-1:68edf112-e7ab-4451-b989-f6e69980071d",
  "CommentsCount": 0,
  "CommentsJson": "[]",
  "FacebookId": "10154007652377280",
  "Firstname": "Jeremy",
  "FistbumpedUsers": {"_"},
  "FistbumpsCount": 0,
  "PostId": "jeezy_1471719492",
  "Username": "jeezy",
  "TextContent": "test post",
  "TimestampSeconds": 1471816192
}

for i in range(0, 100):
    item = testPost
    item['TextContent'] = "test post " + str(i)
    item['TimestampSeconds'] = 1471816191 + i
    table.put_item(Item=item)
    print "test post #" + str(i)

print("Table status:", table.table_status)