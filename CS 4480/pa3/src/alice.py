'''
Created on Apr 23, 2014

@author: Melynda Lindhorst
'''

from Crypto.PublicKey import RSA
from Crypto.Hash import SHA
from Crypto.Signature import PKCS1_v1_5 as cryptsig
from Crypto.Cipher import PKCS1_v1_5
from Crypto.Cipher import DES
from Crypto import Random
from Crypto.Util import Counter
from socket import *
import sys
import time

# Obtain Bob's public key and verify.

# Use a crypto library for integrity protection,
# encryption, and signing of a text message
# together with a symmetric key.

# End Alice (client).

# Begin by taking in arguments for IP address and port to connect to.
bobaddress = 0
bobport = 0

if len(sys.argv) <= 1:
    print ("Bob's address not given. Assuming it's localhost and default port (51234).")
    bobaddress = gethostname()
    bobport = 51234
else:
    boblocation = sys.argv[1]
    bobarray = boblocation.split(':')
    
    if len(bobarray) == 2:
        bobaddress = bobarray[0]
        bobport = bobarray[1]
    else:
        print ("An error occured parsing the IP and port. Now terminating the program.")
        sys.exit(0)
        
# Read keys in from file.
print ("Reading keys from file:")
print ("Ka+")
f = open('kap.pem', 'r')
key = f.read()
keyap = RSA.importKey(key)     # Ka+
print (key)
f.close()
print ("Ka-")
f = open('kan.pem', 'r')
key = f.read()
keyan = RSA.importKey(key)     # Ka-
print (key)
f.close()
print ("Kc+")
f = open('kcp.pem', 'r')
key = f.read()
keycp = RSA.importKey(key)     # Kc+
print (key)
f.close()

# Set up socket and connect to Bob.
alicesocket = socket(AF_INET,SOCK_STREAM)
alicesocket.connect((bobaddress, bobport))

# Wait for Bob's public key.
bobmsg = alicesocket.recv(1024)
bobsig = bobmsg[:128]
bobkey = bobmsg[128:]
h = SHA.new(bobkey)
verifier = cryptsig.new(keycp)

# Verify Bob's public key.
if not verifier.verify(h, bobsig):
    print ("The signature could not be verified. Now terminating the program.")
    sys.exit(0)
else:
    # Encrypt and send message to Bob.
    keybp = RSA.importKey(bobkey)
    print ("Verification succeeded.")
    print ("Bob's public key:")
    print (keybp.exportKey())
    f = open('tobob.txt', 'r')
    tobob = f.read()
    print ("Encrypting message.")
    print ("Message to encrypt: ")
    print (tobob)
    
    # Hash message with Alice's private key.
    h = SHA.new(tobob)
    signer = cryptsig.new(keyan)
    signature = signer.sign(h)
    # Bind the hash to a cleartext version of the message.
    ch = signature + tobob
    # Encrypt tuple with 3DES and symmetric key.
    key = Random.new().read(8)
    iv = Random.get_random_bytes(8)
    print ("Generated IV:")
    print (iv)
    cipher = DES.new(key, DES.MODE_CFB, iv)
    des = cipher.encrypt(ch)
    print ("After 3DES Encryption:\n")
    print (des)
    # Encrypt the symmetric key using RSA and Bob's public key.
    cipher = PKCS1_v1_5.new(keybp)
    ciphertext = cipher.encrypt(key)
    print ("Encrypted symmetric key.\n")
    # Tuple message with original encryption and send.
    sendbob = iv + ciphertext + des
    print ("Final encrypted message to be sent:\n")
    print (sendbob)
    alicesocket.send(sendbob) 
    print ("Alice sent the encrypted message. Now terminating program.")
    time.sleep(3)
    sys.exit(0)
    
    