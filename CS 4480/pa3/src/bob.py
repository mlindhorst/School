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
import select

# Prvoide Bob's public key together with a signed
# message digest of Bob's public key.

# Receive a secure message from Alice. Use a
# crypto library to implement the inverse of Alice
# Check for message integrity. Print message.

# Begin by reading in keys from file.

print ("Reading keys from file:")
print ("Ka+")
f = open('kap.pem', 'r')
key = f.read()
keyap = RSA.importKey(key)     # Ka+
print (key)
f.close()
print ("Kb+")
f = open('kbp.pem', 'r')
key = f.read()
keybp = RSA.importKey(key)     # Kb+
print (key)
f.close()
print ("Kb-")
f = open('kbn.pem', 'r')
key = f.read()
keybn = RSA.importKey(key)     # Kb-
print (key)
f.close()
print ("Kc-")
f = open('kcn.pem', 'r')
key = f.read()
keycn = RSA.importKey(key)     # Kc-
print (key)
f.close()


# Set up socket.
bobsocket = socket(AF_INET,SOCK_STREAM)
bobsocket.bind((gethostname(), 51234))
bobsocket.listen(1)

input = [bobsocket]

while 1:
    inputready,outputready,exceptready = select.select(input,[],[])
    
    for s in inputready:
        
        # Find new Alice, send Bob's public key along with digest.
        if s == bobsocket:
            alicesocket, aliceaddress = bobsocket.accept()
            input.append(alicesocket)
            print ("\nAlice found.")
            # UNCOMMENT: may need to cast kb+ and signature as a strings.
            # Print in hex with .encode()
            h = SHA.new(keybp.exportKey())
            signer = cryptsig.new(keycn)
            signature = signer.sign(h)
            msg = signature + keybp.exportKey()
            alicesocket.send(msg)
          
        # Decrypt message and print.    
        else:
            message = s.recv(1024)
            if len(message) > 0:
                print ("Received encrypted message from Alice:")
                print(message)
                iv = message[:8]
                dpriv = message[8:136]
                dsymm = message[136:]
                # Decrypt wtih his private key.
                dsize = SHA.digest_size
                sentinel = Random.new().read(128+dsize) 
                cipher = PKCS1_v1_5.new(keybn)
                symmkey = cipher.decrypt(dpriv, sentinel)
                print ("Decrypted symmetric key:")
                print (symmkey)
                # Decrypt with the symmetric key.
                print ("Received IV:")
                print (iv)
                cipher = DES.new(symmkey, DES.MODE_CFB, iv)
                clearhash = cipher.decrypt(dsymm)
                # Verify hash.
                clear = clearhash[128:]
                dig = clearhash[:128]
                hsh = SHA.new(clear)
                verifier = cryptsig.new(keyap)
                if not verifier.verify(hsh, dig):
                    print("Verification failed.")
                else:
                    print ("Verification succeeded.")
                    print ("Message received from Alice:")
                    print (clear)
    
    