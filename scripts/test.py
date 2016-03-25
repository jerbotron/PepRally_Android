import re

regex = re.compile('[^a-zA-Z]')

a = "DOnta"

print regex.sub('', a)
