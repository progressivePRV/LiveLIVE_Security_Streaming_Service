package com.example.liveliverecorder;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import fm.liveswitch.AudioStream;
import fm.liveswitch.Channel;
import fm.liveswitch.ChannelClaim;
import fm.liveswitch.VideoStream;
import fm.liveswitch.android.Camera2Source;
import fm.liveswitch.android.LayoutManager;

public class Helper {

    private static final String TAG = "okay_Helper" ;
    fm.liveswitch.Client client = null;
    String userId = "";
    String channelId = "";
    ChannelClaim[] claims = null;
    String token = "";
    LocalMedia localMedia = null;
    Context ctx =  null;
    RemoteMedia remoteMedia;
    AecContext aecContext = null;
    RelativeLayout relativeLayout =  null;
    LayoutManager layoutManager = null;
    InteractWithActivity interact;
    Activity mainActivity;
    boolean isCameraFrontInUse = true;
//    private fm.liveswitch.AudioStream AudioStream;
//    private fm.liveswitch.VideoStream VideoStream;
    Channel channel;

    fm.liveswitch.SfuDownstreamConnection SFU_down_connection;
    fm.liveswitch.SfuUpstreamConnection SFU_up_connection;

    public Helper(Activity activity, Context ctx, RelativeLayout relativeLayout) {
        this.ctx = ctx;
        this.interact = (InteractWithActivity) activity;
        this.mainActivity = activity;
        this.aecContext =  new AecContext();
        this.relativeLayout = relativeLayout;

    }

    void SetUserId(String uid){
        this.userId = uid;
    }

    void SetChannelId(String channelId){
        this.channelId = channelId;
        claims = new ChannelClaim[]{new ChannelClaim(channelId)};
    }


    String GetClientToken(){
        Log.d(TAG, "GetClientToken: called");
        String t_applicationId = "c4193#662-92#71-42#d7-b5#43-a84b#abd00#3d4";
        String applicationId = t_applicationId.replaceAll("#","");
//        String userId = "something@gmail.com";
        String t_deviceid = fm.liveswitch.Guid.newGuid().toString();
        Log.d(TAG, "GetClienttoken: t_device_id=>"+t_deviceid);
        String deviceId = t_deviceid.replaceAll("-", "");
        String t_sharedSecret = "66e-f317eb4674d3a8-5536279f2f5bf5cf4-efc474212e4b469d-7b4505ee1e7-752";
        String sharedSecret = t_sharedSecret.replaceAll("-","");
//        String channelId = "Trial_v1";
        String gatwayUrl = "https://cloud.liveswitch.io/";

        this.client = new fm.liveswitch.Client(gatwayUrl, applicationId, userId, deviceId, null, null);

        this.token = fm.liveswitch.Token.generateClientRegisterToken(
                applicationId,
                client.getUserId(),
                client.getDeviceId(),
                client.getId(),
                client.getRoles(),
                claims,
                sharedSecret
        );
        Log.d(TAG, "GetClientToken: returning Token");
        return this.token;
    }

    boolean CheckIfNothingIsNull() throws Exception {
        Log.d(TAG, "CheckIfNothingIsNull: Called");
//        Log.d(TAG, "CheckIfNothingIsNull: checking if channel Id is null=>"+channelId.isEmpty());
        if (channelId.isEmpty()){
            throw new Exception("Channel Id can't be empty");
        }
        if (userId.isEmpty()){
            throw new Exception("User Id can't be null");
        }
        if (client==null){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        if (token == null){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        return true;
    }

    void RegisterTheClient() throws Exception {
        Log.d(TAG, "RegisterTheClient: called");
        if (CheckIfNothingIsNull()){
            client.register(token).then((Channel[] channels) -> {
                Log.d(TAG, "RegisterTheClient: (registration successful) connected to channel =>"+channels[0].getId());
                this.channel = channels[0];
                if (channels.length>1){
                    Log.d(TAG, "RegisterTheClient: problem more than one channel received");
                }
                Log.d(TAG, "RegisterTheClient: taking first channel as channel for connection");
                interact.ClientRegistered();
//                System.out.println("connected to channel: " + channels[0].getId());
            }).fail((Exception ex) -> {
                Log.d(TAG, "RegisterTheClient: client registration failed msg=>"+ex.getMessage());
//                System.out.println("registration failed");
            });
        }
    }

    void UnRegisterTheClient() throws Exception {
        Log.d(TAG, "UnRegisterTheClient: called");
        if (client==null){
            throw new Exception(" is not called yet, can't proceed further");
        }
        client.unregister().then((Object result) -> {
            Log.d(TAG, "UnRegisterTheClient: successful");
//            System.out.println("unregistration succeeded");
        }).fail((Exception ex) -> {
            Log.d(TAG, "UnRegisterTheClient: failed msg=>"+ex.getMessage());
//            System.out.println("unregistration failed");
        });
    }

    void JoinChannel() throws Exception {
        Log.d(TAG, "JoinChannel: called");
        if (this.channelId.isEmpty()){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        if (this.client==null){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        if (this.token == null){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        client.join(this.channelId, this.token).then((Channel channel) -> {
//            System.out.println("successfully joined channel");
            Log.d(TAG, "JoinChannel: successfully joined channel");
            Log.d(TAG, "JoinChannel: remote upstream connection from channel=>"+channel.getId()+", remote upstream connections=>"+channel.getRemoteUpstreamConnectionInfos().length);
            interact.ChannelJoined();
        }).fail((Exception ex) -> {
            Log.d(TAG, "JoinChannel: failed to join channel msg=>"+ex.getMessage());
//            System.out.println("failed to join channel");
        });
    }

    void LeaveAChannel() throws Exception {
        Log.d(TAG, "LeaveAChannel: called");
        if (channelId.isEmpty()){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        if (client==null){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        if (token == null){
            throw new Exception("'GetClientToken()' is not called yet, can't proceed further");
        }
        client.leave(channelId).then((Channel channel) -> {
            Log.d(TAG, "LeaveAChannel: left the channel");
//            System.out.println("left the channel");
            // now un register the user/device
            try {
                UnRegisterTheClient();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "LeaveAChannel: exception unregistering the user=>"+e.getMessage());
            }
        }).fail((Exception ex) -> {
            Log.d(TAG, "LeaveAChannel: failed to leave the channel msg=>"+ex.getMessage());
//            System.out.println("failed to leave the channel");
        });
    }

    void InitializeLocalMedia() throws Exception {
        Log.d(TAG, "InitializeLocalMedia: called");
        if (ctx == null ){
            throw new Exception("Initialize the Helper object with Context, i.e. new Helper(context,RelativeLayout)");
        }
        this.localMedia =  new LocalMedia(ctx,false,false,aecContext);
    }

    void StartLocalMediaCapture() throws Exception {
        Log.d(TAG, "StartLocalMediaCapture: called");
        if (this.localMedia ==  null){
            throw new Exception("Initialize the LocalMedia first, i.e. call InitializeLocalMedia() function");
        }
        if (this.relativeLayout ==  null){
            throw new Exception("RelativeLayout can't be null, pass RelativeLayout in constructor, i.e.  new Helper(context,RelativeLayout)");
        }
        this.localMedia.start().then((fm.liveswitch.LocalMedia lm) -> {
//            System.out.println("media capture started");
            Log.d(TAG, "StartLocalMediaCapture: successful");
            interact.StartedLocalMediaCapture();
        }).fail((Exception ex) -> {
            Log.d(TAG, "StartLocalMediaCapture: failed msg=>"+ex.getMessage());
//            System.out.println(ex.getMessage());
        });
        mainActivity.runOnUiThread(()->{
            this.layoutManager =  new LayoutManager(this.relativeLayout);
            layoutManager.setLocalView(localMedia.getView());
//            layoutManager.layout();
        });
    }

    void stopLocalMediaCapture() throws Exception {
        Log.d(TAG, "stopLocalMediaCapture: called");
        if (this.localMedia == null) {
            throw new Exception("Initialize the LocalMedia first, i.e. call InitializeLocalMedia() function");
        }
        this.localMedia.stop().then((fm.liveswitch.LocalMedia lm) -> {
//            System.out.println("media capture stopped");
            Log.d(TAG, "stopLocalMediaCapture: successful");
            mainActivity.runOnUiThread(()->{
                layoutManager.unsetLocalView();
            });
            this.localMedia.destroy();
            this.localMedia = null;
            // after this leave the channel
            try {
                LeaveAChannel();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "stopLocalMediaCapture: exception while leaving the channel e =>"+e.getMessage());
            }

        }).fail((Exception ex) -> {
//            System.out.println(ex.getMessage());
            Log.d(TAG, "stopLocalMediaCapture: failed msg=>" + ex.getMessage());
        });

    }


    void CreateSFU_UpStreamConnection(){
        Log.d(TAG, "CreateSFU_UpStreamConnection: called");
        fm.liveswitch.AudioStream audioStream = new AudioStream(localMedia, null);
        fm.liveswitch.VideoStream videoStream = new VideoStream(localMedia, null);
        SFU_up_connection = channel.createSfuUpstreamConnection(audioStream, videoStream);
//        connection.setIceServers(...);
//        connection.DisableAutomaticIceServers = false;
        SFU_up_connection.open().then((Object result) -> {
            Log.d(TAG, "CreateSFU_UpStreamConnection: successful");
//            System.out.println("upstream connection established");
            interact.CreatedSFU_UpStreamConnection();
        }).fail((Exception ex) -> {
            System.out.println("an error occurred");
            Log.d(TAG, "CreateSFU_UpStreamConnection: failed msg=>" + ex.getMessage());
        });
    }

    void CreateSFU_DownStreamConnection(){
        Log.d(TAG, "CreateSFU_DownStreamConnection: called");
        Log.d(TAG, "CreateSFU_DownStreamConnection: called, listening for remote up stream connection, i.e. waiting for another device to send the stream.");
        channel.addOnRemoteUpstreamConnectionOpen((fm.liveswitch.ConnectionInfo remoteConnectionInfo) -> {
            Log.d(TAG, "CreateSFU_DownStreamConnection: got a remote connection");
            Log.d(TAG, "CreateSFU_DownStreamConnection: counts streams=>"+channel.getRemoteUpstreamConnectionInfos().length);
            // as layout manager is not set
            Log.d(TAG, "CreateSFU_DownStreamConnection: setting layout manager");
            remoteMedia = new RemoteMedia(this.ctx,false,false,this.aecContext);
            mainActivity.runOnUiThread(()->{
                this.layoutManager =  new LayoutManager(this.relativeLayout);
                layoutManager.addRemoteView(remoteMedia.getId(), remoteMedia.getView());
            });
            Log.d(TAG, "CreateSFU_DownStreamConnection: done adding remote view in mainActivity");
//            layoutManager.addRemoteView(remoteMedia.getId(), remoteMedia.getView());
//    ...
            fm.liveswitch.AudioStream audioStream = new AudioStream(null, remoteMedia);
            fm.liveswitch.VideoStream videoStream = new VideoStream(null, remoteMedia);
            SFU_down_connection = channel.createSfuDownstreamConnection(remoteConnectionInfo, audioStream, videoStream);
//            connection.setIceServers(...);
//            connection.DisableAutomaticIceServers = false;
            SFU_down_connection.open().then((Object result) -> {
                Log.d(TAG, "CreateSFU_DownStreamConnection:  successful");
//                System.out.println("downstream connection established");
                interact.CreatedSFU_DownStreamConnection();
            }).fail((Exception ex) -> {
                Log.d(TAG, "CreateSFU_DownStreamConnection:  failed msg=>" + ex.getMessage());
//                System.out.println("an error occurred");
            });
            /////////// tear down
            SFU_down_connection.addOnStateChange((fm.liveswitch.ManagedConnection c) -> {
                if (c.getState() == fm.liveswitch.ConnectionState.Closing || c.getState() == fm.liveswitch.ConnectionState.Failing) {
                    mainActivity.runOnUiThread(()->{
                        layoutManager.removeRemoteView(remoteMedia.getId());
                    });
                    Log.d(TAG, "CreateSFU_DownStreamConnection: SFU_down_connection.addOnStateChange called");
                }
            });

        });
    }

    void CloseSFUConnections(){
        Log.d(TAG, "CloseSFUConnections:  called");
        if (SFU_down_connection!=null){
            SFU_down_connection.close().then((Object result) -> {
                Log.d(TAG, "CloseSFUConnections: SFU_down_connection.close() successful");
                mainActivity.runOnUiThread(()->{
                    layoutManager.removeRemoteViews();
                });
//                System.out.println("connection closed");
            }).fail((Exception ex) -> {
                Log.d(TAG, "CloseSFUConnections: failed to close SFU_down_connection, msg=>"+ex.getMessage());
//                System.out.println("an error occurred");
            });
        }
        if (SFU_up_connection!=null) {
            // stopping the preview
            SFU_up_connection.close().then((Object result) -> {
                Log.d(TAG, "CloseSFUConnections: SFU_up_connection.close() successful");
                try {
                    stopLocalMediaCapture();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "CloseSFUConnections: e=>"+e.getMessage());
                }
//                System.out.println("connection closed");
            }).fail((Exception ex) -> {
                Log.d(TAG, "CloseSFUConnections: failed to close SFU_up_connection, msg=>"+ex.getMessage());
//                System.out.println("an error occurred");
            });
        }
    }

    void FlipTheCamera(){
        if (localMedia != null && localMedia.getVideoSource() != null)
        {
            localMedia.changeVideoSourceInput(isCameraFrontInUse ?
                    ((Camera2Source) localMedia.getVideoSource()).getBackInput() :
                    ((Camera2Source) localMedia.getVideoSource()).getFrontInput());

            isCameraFrontInUse = !isCameraFrontInUse;
        }
    }

    interface InteractWithActivity{
        void ClientRegistered();
        void ChannelJoined();
//        void InitializedTheLocalMedia();
        void StartedLocalMediaCapture();
        void CreatedSFU_UpStreamConnection();
        void CreatedSFU_DownStreamConnection();
    }
}
