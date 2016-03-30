import urllib, urllib2, boto3, json
from teamParsers import *

def createTable():
    table = dynamodb.create_table(
        TableName='PlayerProfiles',
        KeySchema=[
            {
                'AttributeName': 'team',
                'KeyType': 'HASH'   #Partition key
            },
            {
                'AttributeName': 'index',
                'KeyType': 'RANGE'  #Sort key
            }
        ],
        AttributeDefinitions=[
            {
                'AttributeName': 'team',
                'AttributeType': 'S'
            },
            {
                'AttributeName': 'index',
                'AttributeType': 'N'
            }
        ],
        ProvisionedThroughput={
            'ReadCapacityUnits': 10,
            'WriteCapacityUnits': 10
        }
    )
    return table

dynamodb = boto3.resource('dynamodb', region_name='us-east-1', endpoint_url="https://dynamodb.us-east-1.amazonaws.com")

# table = createTable()
table = dynamodb.Table("PlayerProfiles")

rosters_urls = {
"baseball": "http://texassports.com/roster.aspx?path=baseball",
"mbball": "http://www.texassports.com/roster.aspx?path=mbball",
"football": "http://www.texassports.com/roster.aspx?path=football",
"mgolf": "http://www.texassports.com/roster.aspx?path=mgolf",
"mswim": "http://www.texassports.com/roster.aspx?path=mswim",
"mten": "http://www.texassports.com/roster.aspx?path=mten",
"xc_tf": "http://www.texassports.com/roster.aspx?path=xc_tf",
"wbball": "http://www.texassports.com/roster.aspx?path=wbball",
"wgolf": "http://www.texassports.com/roster.aspx?path=wgolf",
"wrow": "http://www.texassports.com/roster.aspx?path=wrow",
"wsoc": "http://www.texassports.com/roster.aspx?path=wsoc",
"softball": "http://www.texassports.com/roster.aspx?path=softball",
"wswim": "http://www.texassports.com/roster.aspx?path=wswim",
"wten": "http://www.texassports.com/roster.aspx?path=wten",
"wvball": "http://www.texassports.com/roster.aspx?path=wvball"
}

# for team, url in rosters_urls.iteritems():
#     print "Team = " + team
#     data = urllib2.urlopen(url)
#     parseTeamData(table, team, data)

print("Table status:", table.table_status)

# parse_baseball(table, js, team)
# parse_mbball(table, js, team)
parse_football(table, 'football', urllib2.urlopen(rosters_urls['football']))
# parse_mgolf(table, js, team)
# parse_mswim(table, js, team)
# parse_mten(table, js, team)
# parse_xc_tf(table, 'xc_tf', urllib2.urlopen(rosters_urls['xc_tf']))
# parse_wbball(table, js, team)
# parse_wgolf(table, js, team)
# parse_wrow(table, 'wrow', urllib2.urlopen(rosters_urls['wrow']))
# parse_wsoc(table, js, team)
# parse_softball(table, js, team)
# parse_wswim(table, js, team)
# parse_wten(table, js, team)
# parse_wvball(table, 'wvball', urllib2.urlopen(rosters_urls['wvball']))
