from PIL import Image
import numpy as np
import re
import base64
import cStringIO
import sys
import socket
from thread import *

HOST=''
PORT=4303
WIDTH = 1
HEIGHT = 30
BLACK = 0
WHITE = 255
START = "101100100".replace('0','00').replace('1','11').replace('0000','00000').replace('1111','11111')    #     0             1            2             3             4             5             6             7            8               9       
NUMBERS = [i.replace('0','00').replace('1','11').replace('0000','00000').replace('1111','11111') for i in ["1010101001", "1010101100", "1010100101", "1011001010", "1010110100", "1011010100", "1010010101", "1010010110", "1010011010", "1011010010"]]
CARD_END3 = "101010011001".replace('0','00').replace('1','11').replace('0000','00000').replace('1111','11111') #Trebor and Mom
CARD_END2 = "101011001001".replace('0','00').replace('1','11').replace('0000','00000').replace('1111','11111') #My card
CARD_END1 = "" #Dad's card, not yet recorded
BOOK_END  = "101001001011".replace('0','00').replace('1','11').replace('0000','00000').replace('1111','11111')
ENDS = [BOOK_END, CARD_END1, CARD_END2, CARD_END3]

class Barcode:
    def __init__(self, num, end):
        self.num = num
        self.end = end
        self.encodedNum = self.encodeNumber()
        self.len = len(self.encodedNum)
        self.im = Image.new("L", (WIDTH * self.len, HEIGHT), WHITE)
        self.a = np.array(self.im)
        self.draw()

    def encodeNumber(self):
        return START+''.join([NUMBERS[int(i)] for i in self.num])+ENDS[self.end]
    
    def draw(self):
        for i in range(self.a.shape[1]/WIDTH):
            if(self.encodedNum[i]=="1"):
                for j in range(WIDTH):
                    self.fillColumn(WIDTH*i+j)

    def fillColumn(self, column):
        x = np.full((HEIGHT), BLACK)
        self.a[:,column] = x

    def getImage(self):
        return Image.fromarray(self.a, "L")

def server_mode(line):
    buffered = cStringIO.StringIO()
    m = re.search("([A-Z]*)(\d{10})(B|(?:E\d))", line)
    if m:
        number = m.group(2)
        if m.group(3)=='B':
            ending = 0
        else:
            ending = int(m.group(3)[1:])
        img = Barcode(number, ending).getImage()
        img.show()
        img.save(buffered, format="BMP")
    return base64.b64encode(buffered.getvalue())

def clientthread(conn,addr):
    while 1:
        data=conn.recv(1024)
        reply=server_mode(data)
        if not(data):
            break
        conn.sendall(reply)
        print(addr[0]+": "+data)
    print "closed"
    conn.close()

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print 'Socket Created'

try:
    s.bind((HOST, PORT))
except socket.error as msg:
    print 'Bind failed. Error Code: '+str(msg[0])+' Message: '+msg[1]
    sys.exit()
print 'Socket bind complete'

s.listen(5)
print 'Socket now listening'

while 1:
    conn, addr = s.accept()
    print 'Connected with '+addr[0]+':'+str(addr[1])
    start_new_thread(clientthread,(conn,addr))
s.close()
