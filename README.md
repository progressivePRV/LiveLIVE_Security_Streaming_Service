# LiveLIVE_Security_Streaming_Service

## About the Application

LiveLive is a security broadcasting and streaming tool for people which is used in many use cases and can attract many potential users. The main functionalities of the app is explained as follows

• App will allow authorized users to have video
security surveillance of their desired
properties/valuables.

• Creates a live streaming session and assigns
authorized users to the live stream viewing list.

• Broadcasts the video feed in real time.

• Livestreams the broadcasted video in real time.

• Only registered users with their login
credentials and their face are allowed to view
the livestream.

• Allows user to enter into any livestream he or
she is authorized to

## Features

The main features of this aim includes

• The live streaming app will create and broadcast a live stream and assign
authorized users to the live stream (only authorized users can view the live
stream).
• It can also stop the streaming and delete it.
• This app will be installed on a device doing the security surveillance

• The viewing app will implement multi-factor authentication using password
and face verification.
• While unlocking the app, the face presented to the app screen should
match the user profile picture uploaded while creating an account.

• In the viewing app, users can view a list of live video streams they are
authorized to monitor.
• They can pick any of the streams and start viewing the content the link is
streaming for surveillance

Public and Home Safety , Infant Monitoring, Patient Monitoring, Exam Monitoring are the main use cases for this app

## Desgin and Implementation

## Using Microsoft Azure Face Service

For Face recognition which is one of our authentication,

• We will be using the Face API provided by
Microsoft Cognitive Services for verifying a user
• User will save an image while registering to the
app <br/>
• Whenever a user tries to login to the app, they
will need to click a photo via the app. <br/>
• This photo will be compared with the saved user
photo using Microsoft face API. <br/>
• The Face API determines if two photos belong to
the same person. <br/>
• Face – Detect endpoint : generate face IDs
https://{endpoint}/face/v1.0/detect <br/>
• Face – Verify endpoint : determine if the two face
ids belong to the same person. 
https://{endpoint}/face/v1.0/verify <br/>
• If the faces are identical, our app will grant
access to the user. <br/>

## Using Live Switch SDK and API

• In order to live stream and view content we will be using
Live Switch SDK and their API.
• Live Switch SDK : This sdk is useful to create a live stream channel where all the users can join
POST: https://api.liveswitch.io/ApplicationConfigs with the created API key
It returns the application ID, gateway URL and the shared secret ID as response.
Using these we can create a livestream.
• when the user wants to view the livestream then clicking on the app checks if the user is registered or not using the authentication steps explained before and shows the list of channels that the user is authorized to see.

## Permissions

The android permissions that we need are

CAMERA
INTERNET
RECORD_AUDIO
MODIFY_AUDIO_SETTINGS
FLASHLIGHT


## Conclusion

The main app of building this app was

• The recent COVID pandemic has led to inadequate patient surveillance and has caused an increase in
crimes against patients in hospitals. Our app can provide a solution to this by allowing authorized hospital
staff and family members to monitor patients remotely.

• In a time of work from home culture, new parents can experience a lack of properly monitoring of their
infant in the house. This app will allow these parents to monitoring their infants remotely while working and
attending meetings. It can also be used as a nanny cam if parent are out for work

• COVID has caused people to increase their online shopping on a daily basis. Goods being delivered and left
at the doorstep has led to increase in theft. Homeowners can monitor their doors for such activities using our
app. It can also be used for pets and other property surveillance.

• Taking all this into consideration, we can say that this app will provide a one stop solution for many new
security challenges people face in this pandemic. It will help people adapt faster and effectively to this NEW
NORMAL, making it a popular app, as the security of our loved ones and our property/valuables is a primary
concern for everyone.
