import re

regex = re.compile('[^a-zA-Z]')

sportNames = {
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
"wvball": "Volleyball"
}

def parse_xc_tf(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'M'}
    for line in js:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
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
            print item
            # table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'M'}

def parse_mten(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'M'}
    for line in js:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
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
            print item
            # table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'M'}

def parse_mswim(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'M'}
    for line in js:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
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
            print item
            # table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'M'}

def parse_mgolf(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'M'}
    for line in js:
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = regex.sub("", line[i+len(team)+2:j])
            lastname = regex.sub("", line[j+7:k])
            item['lastname'] = lastname
            item['firstname'] = firstname
            item['imageURL'] = lastname.lower() + "_" + firstname.lower() + ".jpg"
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
            print item
            # table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'M'}

def parse_football(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'M'}
    for line in js:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
        if "roster_dgrd_full_name" in line:
            i = line.find(team)
            j = line.find("<i>")
            k = line.find("</a>")
            firstname = line[i+len(team)+2:j].strip()
            lastname = line[j+7:k].replace(" ","").replace(","," ").replace(".","")
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
            print item
            table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'M'}

def parse_mbball(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'M'}
    for line in js:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
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
            print item
            table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'M'}

def parse_baseball(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'M'}
    for line in js:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
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
            print item
            table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'M'}

def parse_wsoc(table, js, team):
    item = {'team': sportNames[team],
            'MF': 'F'}
    for line in js:
        if "roster_dgrd_no" in line:
            n = line.find("roster_dgrd_no")
            e = line.find(" ", n)
            number = int(line[n + len("roster_dgrd_no") + 2:e])
            item['number'] = number
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
            h = line.find("roster_dgrd_height")
            e = line.find("</td>", h)
            height = line[h + len("roster_dgrd_height") + 8:e - 7]
            y = line.find("roster_dgrd_academic_year")
            e = line.find(" ", y)
            year = line[y + len("roster_dgrd_academic_year") + 2:e]
            item['position'] = position
            item['height'] = height
            item['year'] = year
        if "roster_dgrd_hometownhighschool" in line:
            h = line.find("roster_dgrd_hometownhighschool")
            e = line.find("</td>", h)
            hometown = line[h + len("roster_dgrd_hometownhighschool") + 2:e]
            item['hometown'] = hometown
            print item
            table.put_item(Item=item)
            item = {'team': sportNames[team],
                    'MF': 'F'}
