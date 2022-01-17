## Reliable Data Transfer Protocol over Multiple Interfaces

## Programmers
* Ümit KANOĞLU
* Can DOLAŞ

## Starting
* programming language; java
* update the project folder path in *.bat files
* FileRequestHandler.java (https://github.com/makcay/cs447-547) is modified for ports 5000, 5001 and 5002 so that packet loss and timeout

# run in order for successful download; run_server_5000.bat, run_server_5001.bat, startClient_5000_5001.bat
* Server port 5000 and 5001 have different packets loss

# run in order for failed download; run_server_5001.bat, run_server_5002.bat, startClient_5001_5002.bat
* Server port 5001 and 5002 have same packets loss

## Features
# The features are coded in rdtClient.java
* Parallel download with two clients
* Transfer more data on the faster connection
* Checksum for each packets
* Timeout mechanism
* Speed over each connection
* Percentage completed
* Elapsed time
* Remaining time
* Packet loss count, rate
* Round-trip times
* Check file checksum fails
* Compute the md5sum
* Go back to the first screen to ask the user to enter another file to download
