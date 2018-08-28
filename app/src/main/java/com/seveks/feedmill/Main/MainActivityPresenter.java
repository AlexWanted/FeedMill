package com.seveks.feedmill.Main;

import com.seveks.feedmill.Commands;
import com.seveks.feedmill.DataBase.Controller;
import com.seveks.feedmill.MainActivity;
import com.seveks.feedmill.Queue;

import java.util.ArrayList;

public class MainActivityPresenter implements MainActivityModel.OnResponseListener{

    private MainActivity activity;
    private MainActivityModel model;
    Controller controller;
    Queue queue;

    public MainActivityPresenter(Controller controller) {
        this.controller = controller;
        this.queue = new Queue();
        this.model = new MainActivityModel(this);
    }

    public void attachActivity(MainActivity activity) {
        this.activity = activity;
    }

    public void detachActivity() {
        this.activity = null;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void performCommand(String ip, int port, byte[] command) {
        model.performCommand(new MainActivityModel.CommandTask.Builder()
        .setHost(ip)
        .setPort(port)
        .setRequest(command)
        .setCallback(this)
        .create());
    }

    public void performCommand(String ip, int port, byte[] command, int position) {

    }

    @Override
    public void onResponse(byte[] outputStates, byte[] inputStates) {
        //UpdateViews
    }

    @Override
    public void onError(boolean shouldUpdate) {
        MainActivityModel.CommandTask update = new MainActivityModel.CommandTask.Builder()
                                                .setHost(controller.getIp())
                                                .setPort(activity.port)
                                                .setRequest(Commands.commandInfo)
                                                .setCallback(MainActivityPresenter.this)
                                                .create();
        queue.enqueue(new Queue.Node(update));
    }
}
