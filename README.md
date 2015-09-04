# DIAL - Discovery and Launch Protocol

DIAL—for DIscovery And Launch—is a simple protocol that second-screen devices can use to discover and launch apps on first-screen devices. 
  2nd screen devices: Tablet, Phone etc.
  1st screen devices: TV, Set-top box, Blu-ray etc.

For example, suppose you discover a video on your mobile app and want to play it on your connected TV.

Without DIAL
1. Launch the apps menu on your TV with the normal remote control
2. Navigate to the TV app
3. Launch the TV app
4. Navigate to the pairing screen on TV app
5. Launch and navigate to the pairing screen on Mobile app
6. Input 9-digit pin on Mobile app. 
7. Tap the Play on TV button on the Mobile app

With DIAL
1. Launch the Mobile app
2. Tap the Play on TV button on the mobile app

To know more about DIAL: http://www.dial-multiscreen.org/details-for-developers

# DialServer
Dial Server app implemented for first screen devices (like TV) to be able to launch client' content on browser while connected with any Dial Client app on same network.

Currently implemented with YouTube app as Dial Client. So with the help of DialServer, any one can see it's mobile videos on STV.

How it works:
1. Install DialServer app on TV android OS (or any other android device for testing)
2. Launch DialServer app.
3. Launch YouTube app(Dial Client) on another device with in same network.
4. Press on cast button of Dial Client app.
5. Dial server will launch Browser with YouTube.


