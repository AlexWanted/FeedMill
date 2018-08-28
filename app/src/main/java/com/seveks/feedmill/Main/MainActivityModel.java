package com.seveks.feedmill.Main;

import android.os.AsyncTask;
import android.util.Log;

import com.seveks.feedmill.Commands;
import com.seveks.feedmill.Constants;
import com.seveks.feedmill.Queue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MainActivityModel {

    MainActivityPresenter presenter;

    MainActivityModel(MainActivityPresenter presenter) {
        this.presenter = presenter;
    }

    interface OnResponseListener {
        void onResponse(byte[] outputStates, byte[] inputStates);
        void onError(boolean shouldUpdate);
    }

    public void performCommand(CommandTask task) {
        presenter.queue.enqueue(new Queue.Node(task));
    }

    static class CommandTask extends AsyncTask<Void, Void, Void> {
        String host;
        int port, errorsCount;
        byte[] request, outputStates, inputStates, response;
        boolean update = true;
        OnResponseListener callback;

        static class Builder {
            private OnResponseListener callback;
            private String host;
            private int port, errors;
            private byte[] request;

            public Builder setCallback(final OnResponseListener callback) {
                this.callback = callback;
                return this;
            }

            public Builder setHost(final String host) {
                this.host = host;
                return this;
            }

            public Builder setPort(final int port) {
                this.port = port;
                return this;
            }

            public Builder setRequest(final byte[] request) {
                this.request = request;
                return this;
            }

            public Builder setErrors(final int errors) {
                this.errors = errors;
                return this;
            }

            public CommandTask create() {
                return new CommandTask(this);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        CommandTask(final Builder builder) {
            this.host = builder.host;
            this.port = builder.port;
            this.request = builder.request;
            this.callback = builder.callback;
            this.errorsCount = builder.errors;
        }

        /*CommandTask(OnResponseListener callback, String host, int port, byte[] command) {
            this.host = host;
            this.port = port;
            this.request = command;
            this.callback = callback;
        }

        CommandTask(OnResponseListener callback, String host, int port, byte[] command, int output) {
            this.host = host;
            this.port = port;
            this.request = command;
            this.outputNumber = output;
            this.callback = callback;
        }

        CommandTask(OnResponseListener callback, String host, int port, byte[] command, int output, int errors) {
            this.host = host;
            this.port = port;
            this.request = command;
            this.errorsCount = errors;
            this.outputNumber = output;
            this.callback = callback;
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket = new Socket();
            OutputStream outputStream = null;
            InputStream input = null;

            try {
                SocketAddress address = new InetSocketAddress(host, port);

                socket.connect(address, Constants.timeout);
                socket.setSoTimeout(Constants.timeout);

                outputStream = socket.getOutputStream();
                input = socket.getInputStream();

                outputStream.write(request, 0, request.length);
                outputStream.flush();

                response = new byte[32];
                input.read(response, 0, response.length);
                outputStates = Commands.getOutputStates(response[2], response[3]);
                inputStates = Commands.getOutputStates(response[4], response[5]);
            } catch (IOException e) {
                /*if(errorsCount < 4) {
                } else {
                    if(request != Commands.commandTurnOffAll && request != Commands.commandTurnOnAll) {
                        insOutsList.get(outputNumber).setLoading(false);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                outputAdapter.notifyItemChanged(outputNumber);
                            }
                        });
                    } else {
                        for (final Outputs outputs : insOutsList) {
                            insOutsList.get(insOutsList.indexOf(outputs)).setLoading(false);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    outputAdapter.notifyItemChanged(insOutsList.indexOf(outputs));
                                }
                            });
                        }
                    }

                }*/
                update = false;
                Log.e("ERROR", e.getMessage() + " On Command");
            } finally {
                try {
                    socket.close();
                    if (outputStream != null) outputStream.close();
                    if (input != null) input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (update) {
                callback.onResponse(outputStates, inputStates);
                /*String first = String.format("%8s", Integer.toBinaryString(response[7] & 0xFF)).replace(" ", "0")
                        +String.format("%8s", Integer.toBinaryString(response[6] & 0xFF)).replace(" ", "0");
                String second = String.format("%8s", Integer.toBinaryString(response[9] & 0xFF)).replace(" ", "0")
                        +String.format("%8s", Integer.toBinaryString(response[8] & 0xFF)).replace(" ", "0");
                String third = String.format("%8s", Integer.toBinaryString(response[11] & 0xFF)).replace(" ", "0")
                        +String.format("%8s", Integer.toBinaryString(response[10] & 0xFF)).replace(" ", "0");

                firstCounter.setText(String.valueOf(Integer.parseInt(new StringBuilder(first).reverse().toString(), 2)/64));
                secondCounter.setText(String.valueOf(Integer.parseInt(new StringBuilder(second).reverse().toString(), 2)/64));
                thirdCounter.setText(String.valueOf(Integer.parseInt(new StringBuilder(third).reverse().toString(), 2)/64));
                actionBar.setTitle(Html.fromHtml("<font color='#00E676'>"+ controller.getIp() + ":" + port+"</font>"));
                if(controller.getName().length() > 0)
                    actionBar.setSubtitle(Html.fromHtml("<font color='#00E676'>" + controller.getName() + "</font>"));
                else
                    actionBar.setSubtitle(Html.fromHtml("<font color='#00E676'>Поделючено</font>"));
                for (int i = 0; i < 16; i++) {
                    if (getByteByBoolean(insOutsList.get(i).getOutputState()) != outputStates[i]
                            || getByteByBoolean(insOutsList.get(i).getInputState()) != inputStates[i]
                            || insOutsList.get(i).isLoading()) {
                        insOutsList.get(i).setLoading(false);
                        insOutsList.get(i).setOutputState(getBooleanByByte(outputStates[i]));
                        insOutsList.get(i).setInputState(getBooleanByByte(inputStates[i]));
                        outputAdapter.notifyItemChanged(i);
                        inputAdapter.notifyItemChanged(i);
                    }
                }*/
            } else {
                if(request[2] == 50 && errorsCount < 4) {
                    MainActivityModel.CommandTask commandTask = new CommandTask.Builder()
                                                                    .setHost(host)
                                                                    .setPort(port)
                                                                    .setRequest(request)
                                                                    .setErrors(++errorsCount)
                                                                    .setCallback(callback)
                                                                    .create();
                    //queue.enqueue(new Queue.Node(connectionTask));
                } else callback.onError(true);

/*
                actionBar.setTitle(Html.fromHtml("<font color='#FF1744'>" + controller.getIp() + ":" + port + "</font>"));
                if (controller.getName().length() > 0)
                    actionBar.setSubtitle(Html.fromHtml("<font color='#FF1744'>" + controller.getName() + "</font>"));
                else
                    actionBar.setSubtitle(Html.fromHtml("<font color='#FF1744'>Подключение...</font>"));
*/
            }
        }
    }
}
