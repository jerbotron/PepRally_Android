import urllib, urllib2, boto3, json
from teamParsers import *

def createTable():
    table = dynamodb.create_table(
        TableName='PlayerProfiles',
        KeySchema=[
            {
                'AttributeName': 'Team',
                'KeyType': 'HASH'   #Partition key
            },
            {
                'AttributeName': 'Index',
                'KeyType': 'RANGE'  #Sort key
            }
        ],
        AttributeDefinitions=[
            {
                'AttributeName': 'Team',
                'AttributeType': 'S'
            },
            {
                'AttributeName': 'Index',
                'AttributeType': 'N'
            }
        ],
        ProvisionedThroughput={
            'ReadCapacityUnits': 10,
            'WriteCapacityUnits': 10
        
}    )
    return table

dynamodb = boto3.resource('dynamodb', region_name='us-east-1', endpoint_url="https://dynamodb.us-east-1.amazonaws.com")

# table = createTable()
table = dynamodb.Table("PlayerProfiles_UTAustin")

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

for team, url in rosters_urls.iteritems():
    print "Team = " + team
    try:
        parseTeamData(table, team, urllib2.urlopen(url).read(), False)
    except urllib2.HTTPError, e:
        print e.code
        print e.msg
        break

# parseTeamData(table, "football", urllib2.urlopen(rosters_urls["football"]).read(), False)

print("Table status:", table.table_status)