const express = require("express");
const mongo = require('mongodb');
const jwt = require('jsonwebtoken');
const { requestBody, validationResult, body, header, param, query } = require('express-validator');
const bcrypt = require('bcryptjs');
const { response, request } = require("express");
const axios = require('axios');


const MongoClient = mongo.MongoClient;
const uri = "mongodb+srv://rojatkaraditi:AprApr_2606@test.z8ya6.mongodb.net/LiveLIVE_DB?retryWrites=true&w=majority";
var client;
var collection;
var channelsCollection;
const tokenSecret = "wFq9+ssDbT#e2H9^";
var decoded={};
var token;
var loggedInUser;
var channel;

var connectToDb = function(req,res,next){
    client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true});
    client.connect(err => {
      if(err){
          closeConnection();
          return res.status(400).json({"error":"Could not connect to database: "+err});
      }
      collection = client.db("LiveLIVE_DB").collection("users");
      channelsCollection = client.db("LiveLIVE_DB").collection("channels");
      console.log("connected to database");
    next();
    });
};

var closeConnection = function(){
    client.close();
}

var verifyToken = function(req,res,next){
    var headerValue = req.header("Authorization");
    if(!headerValue){
        //closeConnection();
        return res.status(400).json({"error":"Authorization header needs to be provided for using API"});
    }

    var authData = headerValue.split(' ');

    if(authData && authData.length==2 && authData[0]==='Bearer'){
        token = authData[1];
        try {
            decoded = jwt.verify(token, tokenSecret);
            next();
          } catch(err) {
            //closeConnection();
            return res.status(400).json({"error":err});
          }
    }
    else {
        //closeConnection();
        return res.status(400).json({"error":"Appropriate authentication information needs to be provided"})
    }

};

var isAuthorisedUser = function(request,response,next){
    if(decoded){
                var query = {"_id":new mongo.ObjectID(decoded._id)};
                        collection.find(query).toArray((err,res)=>{
                            if(err){
                                closeConnection();
                                return response.status(400).json({"error":err});
                            }
                            if(res.length<=0){
                                closeConnection();
                                return response.status(400).json({"error":'Unauthorized user'});
                            }
                            loggedInUser  = res[0];
                            next();
                        });
    }else{
        closeConnection();
        return response.status(400).json({"error":"Appropriate authentication information needs to be provided"});
    }
};

var isAuthorisedAdmin = function(request,response,next){
    if(decoded){
                var query = {"_id":new mongo.ObjectID(decoded._id)};
                        channelsCollection.find(query).toArray((err,res)=>{
                            if(err){
                                closeConnection();
                                return response.status(400).json({"error":err});
                            }
                            if(res.length<=0){
                                closeConnection();
                                return response.status(400).json({"error":'Unauthorized user'});
                            }
                            channel  = res[0];
                            next();
                        });
    }else{
        closeConnection();
        return response.status(400).json({"error":"Appropriate authentication information needs to be provided"});
    }
};

const route = express.Router();

route.use("/admin",verifyToken);
route.use("/user",verifyToken);
route.use(connectToDb);
route.use("/admin",isAuthorisedAdmin);
route.use("/user",isAuthorisedUser);

route.post("/admins",[
    body("username","username cannot be empty").notEmpty().trim().escape(),
    body("username","username should have atleast 6 and at max 30 characters").isLength({min:6,max:30}),
    body("password","password cannot be empty").notEmpty().trim(),
    body("password","password should have atleast 6 and at max 20 characters").isLength({min:6,max:20})
],(request,response)=>{
    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }
    try{
        let pwd = request.body.password;
        var hash = bcrypt.hashSync(pwd,10);
        var newUser = request.body;
        newUser.password=hash;
        newUser.channelId = null;
        newUser.channelName = null;
        newUser.users=[];
        newUser.isBroadcasting = false;

        channelsCollection.insertOne(newUser,(err,res)=>{
            var result={};
            if(err){
                closeConnection();
                return response.status(400).json({"error":err});
            }
            else{
                if(res.ops.length>0){
                    result = res.ops[0];
                    delete result.password;
                    closeConnection();
                    return response.status(200).json(result);
                }
                else{
                    closeConnection();
                    return response.status(400).json({"error":"user could not be created"});
                }
                
            }
        });
    }
    catch(error){
        closeConnection();
        return response.status(400).json({"error":error});
    }
}); 

route.put('/admin/broadcasting',[
    body('isBroadcasting','isBroadcasting boolean needs to be specified').notEmpty().isBoolean()
],(request,response)=>{

    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }

    if(!channel.channelId){
        closeConnection();
        return response.status(400).json({"error":'admin has no channel to start/stop broadcasting','errorCode':121});
    }

    if(channel.isBroadcasting==true && request.body.isBroadcasting==true){
        closeConnection();
        return response.status(400).json({"error":'channel is already broadcasting. cannot start broadcasting again','errorCode':131});
    }

    if(channel.isBroadcasting==false && request.body.isBroadcasting==false){
        closeConnection();
        return response.status(400).json({"error":'channel is not broadcasting. cannot stop broadcasting again','errorCode':131});
    }

    var updatedData={
        "isBroadcasting":request.body.isBroadcasting
    };

    var query={"_id":new mongo.ObjectID(channel._id)};
    var newQuery = {$set : updatedData}; 

    channelsCollection.updateOne(query,newQuery,(userErr,userRes)=>{
        if(userErr){
            closeConnection();
            return response.status(400).json({"error":userErr,'errorCode':101});
        }

        closeConnection();
        return response.status(200).json({"result":'broadcasting updated'});
        
    });
})

route.get("/login/users",[
    header("Authorization","Authorization header required to login").notEmpty().trim()
],(request,response)=>{

    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }
    
    try{
        var data = request.header('Authorization');
        //console.log(data);
        var authData = data.split(' ');

        if(authData && authData.length==2 && authData[0]==='Basic'){
            let buff = new Buffer(authData[1], 'base64');
            let loginInfo = buff.toString('ascii').split(":");
            var result ={};

            if(loginInfo!=undefined && loginInfo!=null && loginInfo.length==2){
                var query = {"email":loginInfo[0]};
                collection.find(query).toArray((err,res)=>{
                    var responseCode = 400;
                    if(err){
                        result = {"error":err,'errorCode':101};
                    }
                    else if(res.length<=0){
                        result={"error":"no such user present",'errorCode':102};
                    }
                    else{
                        var user = res[0];
                        if(bcrypt.compareSync(loginInfo[1],user.password)){
                            result=user;
                            delete result.password;
                            user={'_id' : user._id};
                            user.exp = Math.floor(Date.now() / 1000) + (60 * 60);
                            var token = jwt.sign(user, tokenSecret);
                            result.token=token;
                            responseCode=200;
                        }
                        else{
                            result={"error":"Username or password is incorrect",'errorCode':110};
                        }
                    }
                    closeConnection();
                    return response.status(responseCode).json(result);

                });
            }
            else{
                closeConnection();
                return response.status(400).json({"error":"credentials not provided for login",'errorCode':111});
            }
        }
        else{
            closeConnection();
            return response.status(400).json({"error":"Desired authentication type and value required for login",'errorCode':112})
        }
    }
    catch(error){
        closeConnection();
        return response.status(400).json({"error":error.toString(),'errorCode':113});
    }

});

route.get("/login/admins",[
    header("Authorization","Authorization header required to login").notEmpty().trim()
],(request,response)=>{

    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }
    
    try{
        var data = request.header('Authorization');
        //console.log(data);
        var authData = data.split(' ');

        if(authData && authData.length==2 && authData[0]==='Basic'){
            let buff = new Buffer(authData[1], 'base64');
            let loginInfo = buff.toString('ascii').split(":");
            var result ={};

            if(loginInfo!=undefined && loginInfo!=null && loginInfo.length==2){
                var query = {"username":loginInfo[0]};
                channelsCollection.find(query).toArray((err,res)=>{
                    var responseCode = 400;
                    if(err){
                        result = {"error":err,'errorCode':101};
                    }
                    else if(res.length<=0){
                        result={"error":"no such user present",'errorCode':102};
                    }
                    else{
                        var user = res[0];
                        if(bcrypt.compareSync(loginInfo[1],user.password)){
                            result=user;
                            delete result.password;
                            user={'_id' : user._id};
                            user.exp = Math.floor(Date.now() / 1000) + (24 * 60 * 60);
                            var token = jwt.sign(user, tokenSecret);
                            result.token=token;
                            responseCode=200;
                        }
                        else{
                            result={"error":"Username or password is incorrect",'errorCode':110};
                        }
                    }
                    closeConnection();
                    return response.status(responseCode).json(result);

                });
            }
            else{
                closeConnection();
                return response.status(400).json({"error":"credentials not provided for login",'errorCode':111});
            }
        }
        else{
            closeConnection();
            return response.status(400).json({"error":"Desired authentication type and value required for login",'errorCode':112})
        }
    }
    catch(error){
        closeConnection();
        return response.status(400).json({"error":error.toString(),'errorCode':113});
    }

});

route.post("/admin/users",[
    body("name","name cannot be empty").notEmpty().trim().escape(),
    body("age","age cannot be empty").notEmpty(),
    body("age","please enter a valid age above 10 years").isInt({gt:10}),
    body("email","email cannot be empty").notEmpty().trim().escape(),
    body("email","invalid email format").isEmail(),
    body("password","password cannot be empty").notEmpty().trim(),
    body("password","password should have atleast 6 and at max 20 characters").isLength({min:6,max:20})
],(request,response)=>{
    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }
    try{
        let pwd = request.body.password;
        var hash = bcrypt.hashSync(pwd,10);
        var newUser = request.body;
        newUser.password=hash;

        collection.insertOne(newUser,(err,res)=>{
            var result={};
            if(err){
                closeConnection();
                return response.status(400).json({"error":err});
            }
            else{
                if(res.ops.length>0){
                    result = res.ops[0];
                    delete result.password;
                    closeConnection();
                    return response.status(200).json(result);
                }
                else{
                    closeConnection();
                    return response.status(400).json({"error":"user could not be created"});
                }
                
            }
        });
    }
    catch(error){
        closeConnection();
        return response.status(400).json({"error":error});
    }
}); 

route.post('/admin/channels',[
    body('channelName','channel name cannot be empty').notEmpty().trim().escape(),
    body('channelId','channel id cannot be empty').notEmpty().trim().escape(),
    body('users','users required for creating a channel').notEmpty(),
    body('users','users should be an array with minimum lenght 1 and maximum length 4').isArray({min:1,max:4}),
    body('users.*','each user element should be an email id').isEmail()
],(request,response)=>{
    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }

    //add validation to see if users array has unique email ids
    if(hasDuplicates(request.body.users)){
        closeConnection();
        return response.status(400).json({"error":'users array cannot have duplicate values','errorCode':120});
    }

    if(channel.channelId){
        closeConnection();
        return response.status(400).json({"error":'admin already has created a channel','errorCode':121});
    }

    var query = {"email":{
        $in:request.body.users
    }};

    collection.find(query).toArray((userErr,userRes)=>{
        if(userErr){
            closeConnection();
            return response.status(400).json({"error":userErr,'errorCode':101});
        }
        if(userRes.length!=request.body.users.length){
            //add code to identify which emails are present
            closeConnection();
            return response.status(400).json({"error":'all users are not present in database','errorCode':102});
        }

        var updatedData ={
            channelId : request.body.channelId,
            channelName : request.body.channelName,
            users : request.body.users
        }

        var channelQuery={"_id":new mongo.ObjectID(channel._id)};
        var newQuery = {$set : updatedData};

        channelsCollection.updateOne(channelQuery,newQuery,(channelErr,channelRes)=>{
            if(channelErr){
                closeConnection();
                return response.status(400).json({"error":channelErr,'errorCode':101});
            }

            closeConnection();
            return response.status(200).json({"result":'channel added'});

        });

    });

});

route.put('/admin/channels',[
    body('users','users should be an array with minimum lenght 1 and maximum length 4').optional().isArray({min:1,max:4}),
    body('users.*','each user element should be an email id').isEmail()
],(request,response)=>{
    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }

    //add validation to see if users array has unique email ids
    if(hasDuplicates(request.body.users)){
        closeConnection();
        return response.status(400).json({"error":'users array cannot have duplicate values','errorCode':120});
    }

    if(!channel.channelId){
        closeConnection();
        return response.status(400).json({"error":'admin has no channel to update','errorCode':121});
    }

    var query = {"email":{
        $in:request.body.users?request.body.users:[]
    }};

    collection.find(query).toArray((userErr,userRes)=>{
        if(userErr){
            closeConnection();
            return response.status(400).json({"error":userErr,'errorCode':101});
        }
        if(request.body.users && userRes.length!=request.body.users.length){
            //add code to identify which emails are present
            closeConnection();
            return response.status(400).json({"error":'all users are not present in database','errorCode':102});
        }

        var updatedData={};

        if(request.body.channelName){
            updatedData.channelName = request.body.channelName;
        }
        if(request.body.users){
            updatedData.users = request.body.users;
        }

        if(!updatedData.users && !updatedData.channelName){
            closeConnection();
            return response.status(400).json({"error":'no data to update','errorCode':122});
        }

        var channelQuery={"_id":new mongo.ObjectID(channel._id)};
        var newQuery = {$set : updatedData};

        channelsCollection.updateOne(channelQuery,newQuery,(channelErr,channelRes)=>{
            if(channelErr){
                closeConnection();
                return response.status(400).json({"error":channelErr,'errorCode':101});
            }

            closeConnection();
            return response.status(200).json({"result":'channel updated'});

        });

    });

});

route.delete('/admin/channels',(request,response)=>{

    if(!channel.channelId){
        closeConnection();
        return response.status(400).json({"error":'admin has no channel to delete','errorCode':121});
    }

        var updatedData={
            channelId : null,
            channelName : null,
            users : []
        };

        var channelQuery={"_id":new mongo.ObjectID(channel._id)};
        var newQuery = {$set : updatedData};

        channelsCollection.updateOne(channelQuery,newQuery,(channelErr,channelRes)=>{
            if(channelErr){
                closeConnection();
                return response.status(400).json({"error":channelErr,'errorCode':101});
            }

            closeConnection();
            return response.status(200).json({"result":'channel deleted'});

        });
});

route.get('/admin/channels',(request,response)=>{
    var adminChannel = channel;
    delete adminChannel.username;
    delete adminChannel.password;

    closeConnection();
    return response.status(200).json({adminChannel});
});

route.get('/appConfig',[
    header('apiKey','apiKey required for this endpoint').notEmpty().trim().escape()
],async(request,response)=>{
    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }

    try{
        const res = await axios.get('https://api.liveswitch.io/ApplicationConfigs',{
            headers:{
                'X-API-Key':'40-af-57-0d-2d-f2-ca-fc-0b-af-0f-f7-3f-73-97-0d'
            }
        });

        if(!res){
            closeConnection();
            return response.status(400).json({"error":'could not get response from liveSwitch','errorCode':123});
        }
        closeConnection();
        var data = res.data;
        //data.channelID = Date.now() + Math.random()
        data.channelID = new Date().valueOf();
        return response.status(200).json(data);
        
    }
    catch(e){
        closeConnection();
        return response.status(400).json({"error":e.toString(),'errorCode':124});
    }
});

route.get('/user/channels',(request,response)=>{
    var query = {
        users:loggedInUser.email
    }

    if(request.query.channelName){
        var rule = {"$regex": ".*"+request.query.channelName+".*", "$options": "i"}
        query.channelName = rule;
    }

    channelsCollection.find(query).project({_id:1,channelName:1,channelId:1}).toArray((err,res)=>{
        if(err){
            closeConnection();
            return response.status(400).json({"error":err,'errorCode':101});
        }
        if(res.length<=0){
            closeConnection();
            return response.status(400).json({"error":'no channels found for the users','errorCode':102});
        }
        closeConnection();
        return response.status(200).json(res);
    })
}); 

route.get('/admin/profile',(request,response)=>{
     var adminUser ={
         username:channel.username,
         _id:channel._id
     }

     closeConnection();
     return response.status(200).json(adminUser);
});

route.get('/user/profile',(request,response)=>{
    var userProfile = loggedInUser;
    delete userProfile.password;

    closeConnection();
    return response.status(200).json(userProfile);
});

route.post('/admin/verifyFace',[
    body('url',"url required for image verification").notEmpty().trim(),
    body('url',"url should be a url").isURL()
],async(request,response)=>{

    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }

    try{
        const res1 = await axios.post('https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect',
        {
            'url':request.body.url
        },
        {
            headers:{
                'Ocp-Apim-Subscription-Key':'c173784504ce4312b8502df2c6b1cd25',
                'Content-Type':'application/json'
            }
        });

        if(!res1 || !res1.data){
            closeConnection();
            return response.status(400).json({"error":'images could not be processed. Please try again','errorCode':123});
        }
        if(!Array.isArray(res1.data) || res1.data.length!=1){
            closeConnection();
            return response.status(400).json({"error":'images either contain no faces or more than one face. Please try again','errorCode':123});
        }
        if(!res1.data[0].faceId){
            closeConnection();
            return response.status(400).json({"error":'images could not be processed. Please try again','errorCode':123});
        }

        closeConnection();
        return response.status(200).json({"result":'image has a valid face'});

    }
    catch(e){
        closeConnection();
        //console.log(e);
        return response.status(400).json({"error":e.toString(),'errorCode':124});
    }
})



route.post('/user/verifyFace',[
    body('url1',"url 1 required for image verification").notEmpty().trim(),
    body('url1',"url 1 should be a url").isURL(),
    body('url2',"url 2 required for image verification").notEmpty().trim(),
    body('url2',"url 2 should be a url").isURL(),
],async(request,response)=>{

    //url mandatory now. Write code to componse firebase URL here.
    //var url1 = "https://firebasestorage.googleapis.com/v0/b/faceverification-8f16a.appspot.com/o/image_1.jpg?alt=media&token=ea40b220-be43-40f0-84bf-facdf9826907";
    //var url2 = "https://firebasestorage.googleapis.com/v0/b/faceverification-8f16a.appspot.com/o/image_2.jpg?alt=media&token=b5ac1888-398f-4b61-bc34-26eab88538d4";

    const err = validationResult(request);
    if(!err.isEmpty()){
        closeConnection();
        return response.status(400).json({"error":err});
    }

    try{
        const res1 = await axios.post('https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect',
        {
            'url':request.body.url1
            //'url':url1
        },
        {
            headers:{
                'Ocp-Apim-Subscription-Key':'c173784504ce4312b8502df2c6b1cd25',
                'Content-Type':'application/json'
            }
        });

        const res2 = await axios.post('https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect',
        {
            'url':request.body.url2
            //'url':url2
        },
        {
            headers:{
                'Ocp-Apim-Subscription-Key':'c173784504ce4312b8502df2c6b1cd25',
                'Content-Type':'application/json'
            }
        });

        //console.log(res1.data);

        if(!res1 || !res2 || !res1.data || !res2.data ){
            closeConnection();
            return response.status(400).json({"error":'images could not be processed. Please try again','errorCode':123});
        }
        if(!Array.isArray(res1.data) || !Array.isArray(res2.data) || res1.data.length!=1 || res2.data.length!=1){
            closeConnection();
            return response.status(400).json({"error":'images either contain no faces or more than one face. Please try again','errorCode':123});
        }
        if(!res1.data[0].faceId || !res2.data[0].faceId){
            closeConnection();
            return response.status(400).json({"error":'images could not be processed. Please try again','errorCode':123});
        }

        const verifyRes = await axios.post('https://westcentralus.api.cognitive.microsoft.com/face/v1.0/verify',
        {
            "faceId1":res1.data[0].faceId,
            "faceId2":res2.data[0].faceId
        },
        {
            headers:{
                'Ocp-Apim-Subscription-Key':'c173784504ce4312b8502df2c6b1cd25',
                'Content-Type':'application/json'
            }
        });

        //console.log(verifyRes.data);

        if(!verifyRes || !verifyRes.data || !verifyRes.data.confidence){
            closeConnection();
            return response.status(400).json({"error":'images could not be verified. Please try again','errorCode':123});
        }

        if(verifyRes.data.isIdentical){
            if(verifyRes.data.confidence >= 0.75){
                closeConnection();
                return response.status(200).json({"isFaceSame":true});
            }
        }
        closeConnection();
        return response.status(200).json({"isFaceSame":false});

    }
    catch(e){
        closeConnection();
        //console.log(e);
        return response.status(400).json({"error":e.toString(),'errorCode':124});
    }
})

function hasDuplicates(array) {
    return (new Set(array)).size !== array.length;
}

module.exports = route; 