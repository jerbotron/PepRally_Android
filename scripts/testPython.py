import json, urllib, urllib2#, boto3
from teamParsers import *

def createTable():
    table = dynamodb.create_table(
        TableName='PlayerProfiles',
        KeySchema=[
            {
                'AttributeName': 'team',
                'KeyType': 'HASH'  #Partition key
            },
            {
                'AttributeName': 'number',
                'KeyType': 'RANGE'  #Sort key
            }
        ],
        AttributeDefinitions=[
            {
                'AttributeName': 'team',
                'AttributeType': 'S'
            },
            {
                'AttributeName': 'number',
                'AttributeType': 'N'
            },
        ],
        ProvisionedThroughput={
            'ReadCapacityUnits': 10,
            'WriteCapacityUnits': 10
        }
    )
    return table

# dynamodb = boto3.resource('dynamodb', region_name='us-east-1', endpoint_url="https://dynamodb.us-east-1.amazonaws.com")

# table = createTable()
# table = dynamodb.Table("PlayerProfiles")

rosters_urls = {
"baseball": "http://texassports.com/roster.aspx?path=baseball",
"mbball": "http://www.texassports.com/roster.aspx?path=mbball",
"football": "http://www.texassports.com/roster.aspx?path=football",
"mgolf": "http://www.texassports.com/roster.aspx?path=mgolf",
"mswim": "http://www.texassports.com/roster.aspx?path=mswim",
"mten": "http://www.texassports.com/roster.aspx?path=mten",
"xc_tf": "http://www.texassports.com/roster.aspx?path=xc_tf",
"wbball": "http://www.texassports.cobm/roster.aspx?path=wbball",
"wgolf": "http://www.texassports.com/roster.aspx?path=wgolf",
"wrow": "http://www.texassports.com/index.aspx?path=wrow",
"wsoc": "http://www.texassports.com/roster.aspx?path=wsoc",
"softball": "http://www.texassports.com/roster.aspx?path=softball",
"wswim": "http://www.texassports.com/roster.aspx?path=wswim",
"wvball": "http://www.texassports.com/roster.aspx?path=wvball"
}

js = urllib2.urlopen(rosters_urls["xc_tf"])
team = "xc_tf"

print "Team = " + team
table = None

# parse_wsoc(table, js, team)
# parse_baseball(table, js, team)
# parse_basketball(table, js, team)
# parse_football(table, js, team)
# parse_mgolf(table, js, team);
# parse_mswim(table, js, team);
# parse_mten(table, js, team);
parse_xc_tf(table, js, team);

# print("Table status:", table.table_status)
