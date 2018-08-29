package com.seveks.feedmill;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class RequestQueue extends Thread {
    public static final String TAG = RequestQueue.class.getSimpleName();

    public static final int REFRESH_CONNECTION = 0;
    public static final int COMMAND = 1;
    public static final int ERROR = 2;

    private boolean isPaused = false;
    private Handler mBackgroundHandler;
    public Handler mUiHandler;
    private int refreshErrorCounter = 0;
    boolean runningCommand;
    private int timeout = 500;
    public String host;
    public int port;
    @SuppressLint("HandlerLeak")
    @Override
    public void run() {
        Looper.prepare();
        mBackgroundHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case REFRESH_CONNECTION : {
                        if (!isPaused && !runningCommand) {
                            OutputStream outputStream = null;
                            InputStream input = null;
                            Socket socket = new Socket();
                            try {
                                SocketAddress address = new InetSocketAddress(host, port);
                                socket.connect(address, timeout);
                                socket.setSoTimeout(timeout);
                                outputStream = socket.getOutputStream();
                                input = socket.getInputStream();

                                byte[] request = Commands.commandInfo;
                                outputStream.write(request, 0, request.length);
                                outputStream.flush();

                                byte[] response = new byte[64];
                                input.read(response, 0, response.length);

                                byte[] outputStates = Commands.getOutputStates(response[2], response[3]);
                                byte[] inputStates = Commands.getInputStates(response[4], response[5]);

                                if (mUiHandler != null) {
                                    Message uimsg = mUiHandler.obtainMessage(REFRESH_CONNECTION);
                                    Bundle data = new Bundle();
                                    data.putByteArray("outputStates", outputStates);
                                    data.putByteArray("inputStates", inputStates);
                                    uimsg.setData(data);
                                    mUiHandler.sendMessage(uimsg);
                                }
                                refreshErrorCounter=0;

                            } catch (IOException e) {
                                Log.e(TAG, "refresh unsuccessful ("+(++refreshErrorCounter)+")");
                                if (mUiHandler != null && refreshErrorCounter>=5){
                                    mUiHandler.sendEmptyMessage(ERROR);
                                    refreshErrorCounter = 0;
                                }
                            } finally {
                                try {
                                    socket.close();
                                    if (outputStream != null) outputStream.close();
                                    if (input != null) input.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        SystemClock.sleep(500);
                        refreshConnection();
                        break;
                    }
                    case COMMAND:{
                        runningCommand = true;
                        byte[] request = msg.getData().getByteArray("request");
                        int position = msg.getData().getInt("position");
                        int errorCount = msg.getData().getInt("errors", 0);

                        OutputStream outputStream = null;
                        InputStream input = null;
                        Socket socket = new Socket();
                        try {
                            SocketAddress address = new InetSocketAddress(host, port);
                            socket.connect(address, timeout);
                            socket.setSoTimeout(timeout);
                            input = socket.getInputStream();
                            outputStream = socket.getOutputStream();

                            outputStream.write(request, 0, request.length);
                            outputStream.flush();

                            byte[] response = new byte[32];
                            input.read(response, 0, response.length);
                            byte[] outputStates = Commands.getOutputStates(response[2], response[3]);
                            byte[] inputStates = Commands.getInputStates(response[4], response[5]);

                            if (mUiHandler != null){
                                Message uimsg = mUiHandler.obtainMessage(COMMAND);
                                Bundle data = new Bundle();
                                data.putByteArray("outputStates", outputStates);
                                data.putByteArray("inputStates", inputStates);
                                data.putByteArray("response", response);
                                data.putInt("position", position);
                                uimsg.setData(data);
                                mUiHandler.sendMessage(uimsg);
                                Log.d(TAG, "sent message to UiHandler ( position = "+(position+1)+" )");
                            }
                            runningCommand = false;
                        } catch (IOException e) {
                            Log.e(TAG, "Command unsuccessful ("+(++errorCount)+")");
                            if (mUiHandler != null && errorCount>=5) {
                                Message uimsg = mUiHandler.obtainMessage(ERROR);
                                Bundle bundle = new Bundle();
                                bundle.putInt("position", position);
                                uimsg.setData(bundle);
                                mUiHandler.sendMessage(uimsg);
                                runningCommand = false;
                            } else retryCommand(request, position, errorCount);
                        } finally {
                            try {
                                socket.close();
                                if (outputStream != null) outputStream.close();
                                if (input != null) input.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        break;
                    }
                }
            }
        };
        Looper.loop();
    }

    public void setUpConnection(String host, int port){
        this.host = host;
        this.port = port;
        refreshConnection();
    }

    public void refreshConnection(){
        mBackgroundHandler.sendEmptyMessage(REFRESH_CONNECTION);
    }

    public void command(byte[] request, int position){
        Message msg = mBackgroundHandler.obtainMessage(COMMAND);
        Bundle data = new Bundle();
        data.putByteArray("request", request);
        data.putInt("position", position);
        msg.setData(data);
        mBackgroundHandler.sendMessageAtFrontOfQueue(msg);

    }

    private void retryCommand(byte[] request, int position, int errorCount){
        Message msg = mBackgroundHandler.obtainMessage(COMMAND);
        Bundle data = new Bundle();
        data.putByteArray("request", request);
        data.putInt("position", position);
        data.putInt("errors", errorCount);
        msg.setData(data);
        mBackgroundHandler.sendMessage(msg);
    }

    public void pause(){
        isPaused = true;
    }
    public void unpause(){
        isPaused = false;
    }
}
