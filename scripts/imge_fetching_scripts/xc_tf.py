import urllib, urllib2, re

regex = re.compile('[^a-zA-Z]')

root_page = "http://texassports.com"

js = urllib2.urlopen("http://texassports.com/roster.aspx?path=xc_tf")
team = "xc_tf"

for line in js:
	if "roster_dgrd_full_name" in line:
		i = line.find("href")
		j = line.find(team)
		x = line.find(team)
		y = line.find("<i>")
		z = line.find("</a>")
		firstname = line[x+len(team)+2:y]
		lastname = line[y+7:z]
		player_url = root_page + line[i+6:j+len(team)]
		player_page = urllib2.urlopen(player_url)
		player_data = player_page.readlines()
		i = 0
		while 1:
			try:
				if "ctl00_cplhMainContent_ctl01_imgPrimary" in player_data[i]:
					player_card_line = player_data[i]
					break
			except IndexError:
				i = -1
				break
			i+=1
		if i == -1: continue
		i = player_card_line.find("src")
		j = player_card_line.find("alt")
		k = player_card_line.find("style")
		player_card_url = root_page + player_card_line[i+5:j-2].replace("amp;", "")
		if "_" in player_card_line[j+5:k-1]:
			firstName = player_card_line[j+5:k-1].split("_")[0]
			lastName = player_card_line[j+5:k-1].split("_")[1]
		else:
			firstName = player_card_line[j+5:k-1].split()[0]
			lastName = player_card_line[j+5:k-1].split()[1]
		fileName = regex.sub("",lastname).lower() + "_" + regex.sub("",firstname).lower() + ".jpg"
		print player_card_url + " " + fileName
		try:
			urllib.urlretrieve(player_card_url, fileName)
		except IOError:
			print "no image found for " + fileName

print "done"