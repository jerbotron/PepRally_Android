import urllib, urllib2

root_page = "http://texassports.com"

js = urllib2.urlopen("http://texassports.com/roster.aspx?path=wsoc")
team = "wsoc"

count = 0
for line in js:
	if "roster_dgrd_full_name" in line:
		i = line.find("href")
		j = line.find(team)
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
		player_card_url = root_page + player_card_line[i+5:j-2]
		firstName = player_card_line[j+5:k-1].split()[0]
		lastName = player_card_line[j+5:k-1].split()[1]
		fileName = lastName.lower() + "_" + firstName.lower() + "_" + player_number.strip() + ".jpg"
		print player_card_url + " " + fileName
		count+=1
		try:
			urllib.urlretrieve(player_card_url, fileName)
		except IOError:
			print "no image found for " + fileName

print "done, count = " + str(count)