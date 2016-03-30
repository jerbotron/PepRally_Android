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
"baseball": 34,
"mbball": 14,
"football": 98,
"mgolf": 8,
"mswim": 32,
"mten": 11,
"xc_tf": 101,
"wbball": 15,
"wgolf": 9,
"wrow": 56,
"wsoc": 26,
"softball": 19,
"wswim": 28,
"wten": 7,
"wvball": 16    
}

printRoster = True

def parseTeamData(table, team, data, addToDb=True):
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
    print "Done parsing all teams"

def parse_wvball(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
            item['index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = regex.sub('',lastname).lower() + "_" + regex.sub('',firstname).lower() + "_" + str(item['number']) + ".jpg"
        if "roster_dgrd_academic_year" in line:
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_rp_position_long" in line:
            p = line.find("roster_dgrd_rp_position_long")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_long") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_wten(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
            item['index'] = count + teamSizes['mten']
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_wswim(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
            item['index'] = count + teamSizes['wswim']
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            if height: item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_softball(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
            item['index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.replace(" ","_").lower() + "_" + firstname.replace(" ","_").lower() + "_" + str(item['number']) + ".jpg"
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            c = line.find("roster_dgrd_rp_custom1")
            e = line.find("</td>", c)
            custom = line[c + len("roster_dgrd_rp_custom1") + 2:e]
            item['bt'] = custom
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_wsoc(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
            item['index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['lastname'] = lastname
            item['firstname'] = firstname
            if item['number'] == 0:
                item['imageURL'] = lastname.replace(" ","_").lower() + "_" + firstname.replace(" ","_").lower() + "_" + "00.jpg"
            else:
                item['imageURL'] = lastname.replace(" ","_").lower() + "_" + firstname.replace(" ","_").lower() + "_" + str(item['number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_wrow(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
            item['index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            if height:
                item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            if position:
                item['position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            if hometown:
                item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_wgolf(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
            item['index'] = count + teamSizes['mgolf']
            count += 1
        if "roster_dgrd_academic_year" in line:
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_player_hometown" in line:
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_wbball(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'F'}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
            item['index'] = count + teamSizes['mbball']
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k].replace(" ","").replace(","," ").replace(".","")
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.replace(" ","_").lower() + "_" + firstname.replace(" ","_").lower() + "_" + str(item['number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_player_hometown" in line:
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'F'}
    print "Team size = " + str(count)

def parse_xc_tf(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'M'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j-1]
            lastname = line[j+7:k]
            item['lastname'] = lastname.replace(",","")
            item['firstname'] = firstname
            item['imageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
            item['index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'M'}
            if count >= 53:
                item['MF'] = 'F'
    print "Team size = " + str(count)

def parse_mten(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'M'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
            item['index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'M'}
    print "Team size = " + str(count)

def parse_mswim(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'M'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[j+7:k].split()[1])
            lastname = regex.sub("", line[j+7:k].split()[0])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
            item['index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'M'}
    print "Team size = " + str(count)

def parse_mgolf(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'M'}
    count = 0
    for line in data:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
            item['index'] = count
            count += 1
        if "roster_dgrd_height" in line:
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_player_hometown" in line:
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'M'}
    print "Team size = " + str(count)

def parse_football(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'M'}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
            item['index'] = count
            count+=1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + str(item['number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            w = line.find("roster_dgrd_rp_weight")
            e = line.find("</td>", w)
            weight = line[w + len("roster_dgrd_rp_weight") + 2:e]
            item['weight'] = weight
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_rp_custom1" in line:
            c = line.find("roster_dgrd_rp_custom1")
            e = line.find("</td>", c)
            custom = line[c + len("roster_dgrd_rp_custom1") + 2:e]
            item['exp'] = custom
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'M'}
    print "Team size = " + str(count)

def parse_mbball(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'M'}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
            item['index'] = count
            count += 1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k].replace(" ","").replace(","," ").replace(".","")
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.replace(" ","_").lower() + "_" + firstname.replace(" ","_").lower() + "_" + str(item['number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            w = line.find("roster_dgrd_rp_weight")
            e = line.find("</td>", w)
            weight = line[w + len("roster_dgrd_rp_weight") + 2:e]
            item['weight'] = weight
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_player_hometown" in line:
            h = line.find("roster_dgrd_player_hometown")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_player_hometown") + 2:e]
            hs = line.find("roster_dgrd_player_highschool")
            e = line.find("</td>", hs)
            highschool = line[hs + len("roster_dgrd_player_highschool") + 2:e]
            item['hometown'] = hometown + " / " + highschool
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'M'}
    print "Team size = " + str(count)

def parse_baseball(table, team, data, addToDb=True):
    item = {'team': teamNames[team],
            'MF': 'M'}
    count = 0
    for line in data:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
            item['index'] = count
            count+=1
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k]
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.replace(" ","_").lower() + "_" + firstname.replace(" ","_").lower() + "_" + str(item['number']) + ".jpg"
        if "roster_dgrd_rp_position_short" in line:
            p = line.find("roster_dgrd_rp_position_short")
            e = line.find("</td>", p)
            position = line[p + len("roster_dgrd_rp_position_short") + 2:e]
            item['position'] = position
            c = line.find("roster_dgrd_rp_custom1")
            e = line.find("</td>", c)
            custom = line[c + len("roster_dgrd_rp_custom1") + 2:e]
            item['bt'] = custom
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            item['height'] = height
            w = line.find("roster_dgrd_rp_weight")
            e = line.find("</td>", w)
            weight = line[w + len("roster_dgrd_rp_weight") + 2:e]
            item['weight'] = weight
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            if (printRoster): print item
            if (addToDb): table.put_item(Item=item)
            item = {'team': teamNames[team],
                    'MF': 'M'}
    print "Team size = " + str(count)
