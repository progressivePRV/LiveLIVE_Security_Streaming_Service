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

## Use cases

Public and Home Safety 
Infant Monitoring 
Patient Monitoring 
Exam Monitoring

## Features

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

## Using Microsoft Azure Face Service

• We will be using the Face API provided by
Microsoft Cognitive Services for verifying a user
• User will save an image while registering to the
app
• Whenever a user tries to login to the app, they
will need to click a photo via the app.
• This photo will be compared with the saved user
photo using Microsoft face API.
• The Face API determines if two photos belong to
the same person.
• Face – Detect endpoint : generate face IDs
https://{endpoint}/face/v1.0/detect
• Face – Verify endpoint : determine if the two face
ids belong to the same person.
https://{endpoint}/face/v1.0/verify
• If the faces are identical, our app will grant
access to the user

## Using Wowza Streaming Cloud API

• In order to live stream and view content we will be using
Wowza Streaming Could REST API.
• GoCoder SDK : encoder required to build a live streaming
app in Android. Will help broadcast video content.
• Create live stream: create live stream for video
broadcasting.
POST: https://apisandbox.cloud.wowza.com/api/v1.5/live_streams
• Start live stream: start broadcasting video content on
created live stream
PUT: https://apisandbox.cloud.wowza.com/api/v1.5/live_streams/{id}/start
• Stop live stream: endpoint will stop start broadcasting video
content on created live stream
PUT: https://apisandbox.cloud.wowza.com/api/v1.5/live_streams/{id}/stop
• Delete live stream : endpoint will delete the created
livestream.
DELETE: https://apisandbox.cloud.wowza.com/api/v1.5/live_streams/{id}
• Get playback URL/player from live stream to display live
stream to users. Play the live streaming using
WOWZPlayerView. 

## Permissions

CAMERA
INTERNET
RECORD_AUDIO
MODIFY_AUDIO_SETTINGS
FLASHLIGHT


## How can this app make money ?

• The recent COVID pandemic has led to inadequate patient surveillance and has caused an increase in
crimes against patients in hospitals. Our app can provide a solution to this by allowing authorized hospital
staff and family members to monitor patients remotely.
• In a time of work from home culture, new parents can experience a lack of properly monitoring of their
infant in the house. This app will allow these parents to monitoring their infants remotely while working and
attending meetings. It can also be used as a nanny cam if parent are out for work
.
• COVID has caused people to increase their online shopping on a daily basis. Goods being delivered and left
at the doorstep has led to increase in theft. Homeowners can monitor their doors for such activities using our
app. It can also be used for pets and other property surveillance.
• Taking all this into consideration, we can say that this app will provide a one stop solution for many new
security challenges people face in this pandemic. It will help people adapt faster and effectively to this NEW
NORMAL, making it a popular app, as the security of our loved ones and our property/valuables is a primary
concern for everyone.

