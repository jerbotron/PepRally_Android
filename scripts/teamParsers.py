import re

regex = re.compile('[^a-zA-Z]')

teamNames = {
"baseball": "Baseball",
"mbball": "Basketball",
"football": "Football",
"mgolf": "Golf",
"mswim": "Swimming and Diving",
"mten": "Tennis",
"xc_tf": "Track and Field",
"wbball": "Basketball",
"wgolf": "Golf",
"wrow": "Rowing",
"wsoc": "Soccer",
"softball": "Softball",
"wswim": "Swimming and Diving",
"wten": "Tennis",
"wvball": "Volleyball"
}

teamSizes = {
"baseball": 27,
"mbball": 14,
"football": 120,
"mgolf": 8,
"mswim": 32,
"mten": 12,
"xc_tf": 101,
"wbball": 14,
"wgolf": 7,
"wrow": 56,
"wsoc": 24,
"softball": 19,
"wswim": 27,
"wten": 7,
"wvball": 17    
}

printRoster = True

def parseTeamData(table, team, data, addToDb):
    if team == "baseball":
        parse_baseball(table, team, data, addToDb)
    elif team == "mbball":
        parse_mbball(table, team, data, addToDb)
    elif team == "football":
        parse_football(table, team, data, addToDb)
    elif team == "mgolf":
        parse_mgolf(table, team, data, addToDb)
    elif team == "mswim":
        parse_mswim(table, team, data, addToDb)
    elif team == "mten":
        parse_mten(table, team, data, addToDb)
    elif team == "xc_tf":
        parse_xc_tf(table, team, data, addToDb)
    elif team == "wbball":
        parse_wbball(table, team, data, addToDb)
    elif team == "wgolf":
        parse_wgolf(table, team, data, addToDb)
    elif team == "wrow":
        parse_wrow(table, team, data, addToDb)
    elif team == "wsoc":
        parse_wsoc(table, team, data, addToDb)
    elif team == "softball":
        parse_softball(table, team, data, addToDb)
    elif team == "wswim":
        parse_wswim(table, team, data, addToDb)
    elif team == "wten":
        parse_wten(table, team, data, addToDb)
    elif team == "wvball":
        parse_wvball(table, team, data, addToDb)

def parse_wvball(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['Number'] = number
            item['Index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub('',lastname).lower() + "_" + regex.sub('',firstname).lower() + "_" + str(item['Number']) + ".jpg"
        if "roster_dgrd_academic_year" in line:
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_rp_position_long" in line:
            p = line.find("roster_dgrd_rp_position_long")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_long") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_wten(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count + teamSizes['mten']
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            if height: item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_wswim(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count + teamSizes['wswim']
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            if height: item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_softball(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['Number'] = number
            item['Index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + str(item['Number']) + ".jpg"
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            c = line.find("roster_dgrd_rp_custom1")
            e = line.find("</td>", c)
            custom = line[c + len("roster_dgrd_rp_custom1") + 2:e]
            item['BT'] = custom
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_wsoc(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['Number'] = number
            item['Index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            if item['Number'] == 0:
                item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + "00.jpg"
            else:
                item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + str(item['Number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_wrow(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            if height:
                item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            if position:
                item['Position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            if hometown:
                item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_wgolf(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count + teamSizes['mgolf']
            count += 1
        if "roster_dgrd_academic_year" in line:
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_wbball(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'F',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            try:
                number = int(line[n + len("roster_dgrd_no") + 2:e])
            except ValueError:
                number = -1
            item['Number'] = number
            item['Index'] = count + teamSizes['mbball']
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname.lower()) + "_" + regex.sub("",firstname).lower() + "_" + str(item['Number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_player_hometown" in line:
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['Hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'F',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_xc_tf(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'M',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname.replace(",","")
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'M',
                    'HasUserProfile': False}
            if count >= 53:
                item['MF'] = 'F'
    print "Team size = " + str(count)

def parse_mten(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'M',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            if height: item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'M',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_mswim(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'M',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[j+7:k].split()[1]
            lastname = line[j+7:k].split()[0]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'M',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_mgolf(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'M',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['Index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'M',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_football(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'M',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['Number'] = number
            item['Index'] = count
            count+=1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + str(item['Number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            w = line.find("roster_dgrd_rp_weight")
            e = line.find("</td>", w)
            weight = line[w + len("roster_dgrd_rp_weight") + 2:e]
            item['Weight'] = weight
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_rp_custom1" in line:
            c = line.find("roster_dgrd_rp_custom1")
            e = line.find("</td>", c)
            custom = line[c + len("roster_dgrd_rp_custom1") + 2:e]
            item['Exp'] = custom
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['Hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'M',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_mbball(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'M',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['Number'] = number
            item['Index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + str(item['Number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            w = line.find("roster_dgrd_rp_weight")
            e = line.find("</td>", w)
            weight = line[w + len("roster_dgrd_rp_weight") + 2:e]
            item['Weight'] = weight
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_player_hometown" in line:
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['Hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'M',
                    'HasUserProfile': False}
    print "Team size = " + str(count)

def parse_baseball(table, team, data, addToDb):
    item = {'Team': teamNames[team],
            'MF': 'M',
            'HasUserProfile': False}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['Number'] = number
            item['Index'] = count
            count+=1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['LastName'] = lastname
            item['FirstName'] = firstname
            item['ImageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + str(item['Number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['Position'] = position
            c = line.find("roster_dgrd_rp_custom1")
            e = line.find("</td>", c)
            custom = line[c + len("roster_dgrd_rp_custom1") + 2:e]
            item['BT'] = custom
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['Height'] = height
            w = line.find("roster_dgrd_rp_weight")
            e = line.find("</td>", w)
            weight = line[w + len("roster_dgrd_rp_weight") + 2:e]
            item['Weight'] = weight
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['Year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['Hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'Team': teamNames[team],
                    'MF': 'M',
                    'HasUserProfile': False}
    print "Team size = " + str(count)
