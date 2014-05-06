import sqlite3 as lite
import time
import urllib
import re

proxies={"http":"http://118.26.57.14:80"}
f=open("gutenberg.txt")
rawInput=f.read()
splitByLine=rawInput.split('\n')

numbers=[line.split()[0] for line in splitByLine]

titles=[" ".join(line.split(" by ")[0].split()[1:]).replace('\'','\'\'') for line in splitByLine]

authors=[]
for line in splitByLine:
    if(len(line.split(" by ")) !=2):
        authors.append("Unknown")
    else:
        authors.append(" ".join(line.split(" by ")[1].split()[:-1]))

lengths=[]
for n,booknum in enumerate(numbers):
    #time.sleep(2)
    f=urllib.urlopen("http://onlinebooks.library.upenn.edu/webbin/gutbook/lookup?num="+str(booknum))
    text=f.read()
    lengthMatch=re.search("Text \([0-9]+\.?[0-9]+[MKmk]\)",text)
    if(lengthMatch==None):
        lengths.append(1000000)
        continue
    lengthText=lengthMatch.group(0).split()[1]
    lengthMagnitude=lengthText[-2]
    length=float(lengthText[1:-2])
    if(lengthMagnitude=='K'):
        length*=1000
    if(lengthMagnitude=='M'):
        length*=1000000
    lengths.append(length)
    print lengths[n],
    f.close()

urls=[]
for booknum in numbers:
    url="http://mirror.csclub.uwaterloo.ca/gutenberg"
    for digit in str(booknum)[:-1]:
        url+='/'+digit
    url+='/'+str(booknum)+'/'+str(booknum)+".txt"
    urls.append(url)
    print url
#urls=["http://www.gutenberg.org/cache/epub/"+str(booknum)+"/pg"+str(booknum)+".txt" for booknum in numbers]
con=None
con=lite.connect("booksManager.db")
cur=con.cursor()
cur.execute('SELECT SQLITE_VERSION()')
data=cur.fetchone()
print "version: "+str(data)

cur.execute("DROP TABLE Books")
 
cur.execute("CREATE TABLE Books( KEY_ID INTEGER PRIMARY KEY, KEY_TITLE VARCHAR, KEY_AUTHOR VARCHAR, KEY_URL VARCHAR, KEY_DOWNLOADED BIT, KEY_FILESIZE INTEGER, KEY_TEXT TEXT )")
for n in range(100):
    command="INSERT INTO Books VALUES("+str(n)+",'"+str(titles[n])+"','"+str(authors[n])+"','"+str(urls[n])+"',"+"0,"+str(lengths[n])+",'empty_text')"
    #command="INSERT INTO Books VALUES(1,'Declaration of Independence', 'Various', 'FillerURL', 0, '"+ open("usdeclar.txt").read().replace("'","''") +"')"
    print "Command: "+command
    cur.execute(command)
con.commit()
con.close()
