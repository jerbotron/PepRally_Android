import urllib, urllib2, re

root_page = "http://texassports.com"

regex = re.compile('[^a-zA-Z]')

js = urllib2.urlopen("http://www.texassports.com/roster.aspx?path=football")
team = "football"
count = 0

for line in js:
	if "roster_dgrd_full_name" in line:
		i = line.find("href")
		j = line.find(team)
		k = line.find("<i>")
		e = line.find("</a>")
		firstname = line[j+len(team)+2:k].strip()
		lastname = line[k+7:e]
		player_url = root_page + line[i+6:j+len(team)]
		player_page = urllib2.urlopen(player_url)
		player_data = player_page.readlines()
		i = 0
		while 1:
			if "player_card_image" in player_data[i]:
				player_card_line = player_data[i+2]
			if "player_number_wrapper" in player_data[i]:
				player_number = player_data[i+1]
				break
			i+=1
		i = player_card_line.find("src")
		j = player_card_line.find("alt")
		k = player_card_line.find("/>")
		if (i == -1 and j == -1): 
			continue
		player_card_url = root_page + player_card_line[i+5:j-2]
		fileName = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + "_" + player_number.strip() + ".jpg"
		print player_card_url + " " + fileName
		try:
			urllib.urlretrieve(player_card_url, fileName)
		except:
			print "no image found for " + fileName
		count += 1

print "done, count = " + str(count)
