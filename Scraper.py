from bs4 import BeautifulSoup
import os
import os.path
import requests


def pullbooks():
    URL="https://www.gutenberg.org/browse/scores/top#books-last30"
    page = requests.get(URL)
    soup = BeautifulSoup(page.content, "html.parser")
    for link in soup.find_all('a'):
      if(link.get('href').find("ebooks")!=-1):
        bookEnum = link.get('href')
        if(bookEnum.find("bookshelf")!=-1 or bookEnum.find("offline")!=-1):
          continue
        bookID = bookEnum[8:]
        bookURL="https://www.gutenberg.org/files/" + bookID + "/" + bookID + ".txt"
        if(bookURL == "https://www.gutenberg.org/files//.txt"):
          continue
        bookPage = requests.get(bookURL)
        bookName = link.string
        trunk = len(bookName) - bookName.rfind("by") + 1
        bookName = bookName[:-trunk]
        if(bookPage.text.find("Error 404")):
          bookURL="https://www.gutenberg.org/files/" + bookID + "/" + bookID + "-0.txt"
          bookPage = requests.get(bookURL)
        soup = BeautifulSoup(bookPage.content, "html.parser")
        writebooks(soup, bookName)

def writebooks(soupedPage, bookName):
  '{}.txt'.format(bookName)
  if not os.path.exists("../books"):
    os.makedirs("../books")
  if not os.path.exists('../books/{}.txt'.format(bookName)): 
    f = open('../books/{}.txt'.format(bookName), "w+")
    f.write(soupedPage.get_text())
    f.close()

pullbooks()  