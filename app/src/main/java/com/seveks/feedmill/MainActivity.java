package com.seveks.feedmill;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.icu.util.Output;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.seveks.feedmill.DataBase.Controller;
import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.Fragments.ChangeIpDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity implements OutputAdapter.ClickListener, ChangeIpDialog.DismissListener {

    public static final String PREF_PORT = "PREF_PORT";
    Queue queue;
    public int port = 2001, timeout = 500;
    Controller controller;
    ArrayList<Outputs> insOutsList;
    OutputAdapter outputAdapter;
    InputAdapter inputAdapter;
    ControllersAdapter controllersAdapter;
    ActionBar actionBar;
    TextView firstCounter, secondCounter, thirdCounter;
    boolean isDialogOpened = false, firstRun = false;
    SharedPreferences prefs;
    ArrayList<Controller> controllersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstCounter = findViewById(R.id.firstCounter);
        secondCounter = findViewById(R.id.secondCounter);
        thirdCounter = findViewById(R.id.thirdCounter);
        DBHelper dbHelper = new DBHelper(this);
        prefs = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE);

        if(getSupportActionBar() != null) {
            actionBar = getSupportActionBar();
            actionBar.setElevation(0);
        }

        queue = new Queue();

        if(!prefs.contains(SettingsActivity.PREF_SELECTED_PRESET)){
            int id = dbHelper.insertPreset("Настройка 1");
            dbHelper.close();
            prefs.edit().putInt(SettingsActivity.PREF_SELECTED_PRESET, id).apply();
        }

        if(hasCurrentController()) {
            initRecycler();
        } else {
            firstRun = true;
            openIpDialog();
        }
        dbHelper.close();
    }

    private void initRecycler() {
        DBHelper dbHelper = new DBHelper(this);
        int controllerId = prefs.getInt(SettingsActivity.PREF_SELECTED_CONTROLLER, -1);
        controller = dbHelper.getControllerById(controllerId);
        port = prefs.getInt(PREF_PORT, 2001);
        actionBar.setTitle(controller.getIp() + ":" + port);
        actionBar.setSubtitle(controller.getName());
        insOutsList = dbHelper.getInsOuts(controller.getId());
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        RecyclerView outputsRecycler = findViewById(R.id.outputs_recycler);
        outputsRecycler.setLayoutManager(mLayoutManager);
        outputAdapter = new OutputAdapter(this, insOutsList);
        outputAdapter.setOnClickListener(this);
        outputsRecycler.setItemAnimator(null);
        outputsRecycler.setAdapter(outputAdapter);

        inputAdapter = new InputAdapter(this, insOutsList);
        GridLayoutManager inputLayoutManager = new GridLayoutManager(this, 8) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        RecyclerView inputsRecycler = findViewById(R.id.inputs_recycler);
        inputsRecycler.setLayoutManager(inputLayoutManager);
        inputsRecycler.setItemAnimator(null);
        inputsRecycler.setAdapter(inputAdapter);


        updateControllersRecycler(dbHelper);
        controllersAdapter = new ControllersAdapter(controllersList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView controllersRecycler = findViewById(R.id.controllers_recycler);
        controllersRecycler.setLayoutManager(layoutManager);
        controllersRecycler.setItemAnimator(null);
        controllersRecycler.setAdapter(controllersAdapter);

        queue.enqueue(new Queue.Node(new UpdateTask(controller.getIp(), port, true)));
        dbHelper.close();
    }

    private void updateControllersRecycler(DBHelper dbHelper) {
        if (controllersList != null) controllersList.clear();
        else controllersList = new ArrayList<>();
        controllersList.addAll(dbHelper.getControllers(prefs.getInt(SettingsActivity.PREF_SELECTED_PRESET, -1)));
        if (controllersAdapter != null) controllersAdapter.notifyDataSetChanged();
        if(controllersList.size() > 0) {
            findViewById(R.id.controllers_container).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.controllers_container).setVisibility(View.GONE);
        }
    }

    private boolean hasCurrentController() {
        return prefs.contains(SettingsActivity.PREF_SELECTED_CONTROLLER);
    }

    public void openIpDialog() {
        if(!isDialogOpened) {
            ChangeIpDialog changeIpDialogFragment = ChangeIpDialog.newInstance(MainActivity.this, true);
            changeIpDialogFragment.show(getSupportFragmentManager(), "dialog_change_ip");
            isDialogOpened = true;
        }
    }

    @Override
    public void accepted(String ip, int port, String name) {
        this.port = port;
        DBHelper dbHelper = new DBHelper(this);
        int presetId = prefs.getInt(SettingsActivity.PREF_SELECTED_PRESET, -1);
        controller = dbHelper.getControllerByIp(presetId, ip);
        if (controller == null) {
            int controllerId = dbHelper.insertController(presetId, ip, name);
            controller = dbHelper.getControllerById(controllerId);
        }
        if(!controller.getName().equals(name)) {
            dbHelper.editController(controller.getId(), controller.getIp(), name);
            controller.setName(name);
        }
        prefs.edit().putInt(PREF_PORT, port).apply();
        prefs.edit().putInt(SettingsActivity.PREF_SELECTED_CONTROLLER, controller.getId()).apply();
        if(firstRun) {
            initRecycler();
            firstRun = false;
            queue.resumeQueue();
        } else {
            insOutsList.clear();
            insOutsList.addAll(dbHelper.getInsOuts(controller.getId()));
            inputAdapter.notifyDataSetChanged();
            outputAdapter.notifyDataSetChanged();
            updateControllersRecycler(dbHelper);
        }

        actionBar.setTitle(controller.getIp() + ":" + port);
        actionBar.setSubtitle(controller.getName());

        dbHelper.close();
    }

    @Override
    public void dismissed() {
        isDialogOpened = false;
        if(firstRun) openIpDialog();
    }

    public void toSettingsActivityForResult() {
        startActivityForResult(new Intent(this, SettingsActivity.class), 1);
        queue.pauseQueue();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            DBHelper dbHelper = new DBHelper(this);
            if(!hasCurrentController()) {
                firstRun = true;
                openIpDialog();
            } else {
                updateControllersRecycler(dbHelper);
                queue.resumeQueue();
            }
            actionBar.setTitle(controller.getIp() + ":" + port);
            actionBar.setSubtitle(controller.getName());
            insOutsList.clear();
            insOutsList.addAll(dbHelper.getInsOuts(controller.getId()));
            inputAdapter.notifyDataSetChanged();
            outputAdapter.notifyDataSetChanged();
            dbHelper.close();
        }
    }

    @Override
    public void outputClicked(View view, int position) {
        if(!insOutsList.get(position).isLoading()) {
            CommandTask connectionTask = new CommandTask(
                    controller.getIp(), port, Commands.getCommandToggle(
                    (byte) (insOutsList.get(position).getNumber() - 1),
                    insOutsList.get(position).getOutputState()), position);
            insOutsList.get(position).setLoading(true);
            outputAdapter.notifyItemChanged(position);
            queue.enqueue(new Queue.Node(connectionTask));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem settings = menu.add(0, 0, 0, "Настройки");
        MenuItem turnOffAll = menu.add(0, 1, 0, "Отключить все");
        MenuItem turnOnAll = menu.add(0, 2, 0, "Включить все");
        MenuItem changeIP = menu.add(0, 3, 0, "Сменить IP");
        MenuItem exit = menu.add(0, 4, 0, "Выйти");
        settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        turnOffAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        turnOnAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        changeIP.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        exit.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                toSettingsActivityForResult();
                break;
            case 1: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Подтверждение")
                        .setMessage("Вы точно уверены, что хотите отключить все выходы?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CommandTask turnOffAllTask = new CommandTask(
                                        controller.getIp(), port, Commands.commandTurnOffAll);
                                for (int i = 0; i < insOutsList.size(); i++) {
                                    insOutsList.get(i).setLoading(true);
                                    outputAdapter.notifyItemChanged(i);
                                }
                                queue.enqueue(new Queue.Node(turnOffAllTask));
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                break;
            }
            case 2: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Подтверждение")
                        .setMessage("Вы точно уверены, что хотите включить все выходы?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CommandTask turnOffAllTask = new CommandTask(
                                        controller.getIp(), port, Commands.commandTurnOnAll);
                                for (int i = 0; i < insOutsList.size(); i++) {
                                    insOutsList.get(i).setLoading(true);
                                    outputAdapter.notifyItemChanged(i);
                                }
                                queue.enqueue(new Queue.Node(turnOffAllTask));
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                break;
            }
            case 3:
                openIpDialog();
                break;
            case 4:
                finish();
                break;
        }
        return false;
    }

    private class CommandTask extends AsyncTask<Void, Void, Void> {
        String host;
        int port, errorsCount, outputNumber;
        byte[] request, outputStates, inputStates, response;
        boolean update = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        CommandTask(String host, int port, byte[] command) {
            this.host = host;
            this.port = port;
            this.request = command;
        }

        CommandTask(String host, int port, byte[] command, int output) {
            this.host = host;
            this.port = port;
            this.request = command;
            this.outputNumber = output;
        }

        CommandTask(String host, int port, byte[] command, int output, int errors) {
            this.host = host;
            this.port = port;
            this.request = command;
            this.errorsCount = errors;
            this.outputNumber = output;
            if(errorsCount > 4) CommandTask.this.cancel(false);
            Log.d("ERRORS", String.valueOf(errorsCount));
        }

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket = new Socket();
            OutputStream outputStream = null;
            InputStream input = null;

            try {
                SocketAddress address = new InetSocketAddress(host, port);

                socket.connect(address, timeout);
                socket.setSoTimeout(timeout);

                outputStream = socket.getOutputStream();
                input = socket.getInputStream();

                outputStream.write(request, 0, request.length);
                outputStream.flush();

                response = new byte[32];
                input.read(response, 0, response.length);
                outputStates = Commands.getOutputStates(response[2], response[3]);
                inputStates = Commands.getOutputStates(response[4], response[5]);
            } catch (IOException e) {
                if(errorsCount < 4) {
                    CommandTask connectionTask = new CommandTask(host, port, request, outputNumber, ++errorsCount);
                    queue.enqueue(new Queue.Node(connectionTask));
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

                }
                update = false;
                Log.d("ERROR", e.getMessage() + " On Command");
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
                String first = String.format("%8s", Integer.toBinaryString(response[7] & 0xFF)).replace(" ", "0")
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
                }
            } else {
                actionBar.setTitle(Html.fromHtml("<font color='#FF1744'>" + controller.getIp() + ":" + port + "</font>"));
                if (controller.getName().length() > 0)
                    actionBar.setSubtitle(Html.fromHtml("<font color='#FF1744'>" + controller.getName() + "</font>"));
                else
                    actionBar.setSubtitle(Html.fromHtml("<font color='#FF1744'>Подключение...</font>"));
            }
        }
    }

    private class UpdateTask extends AsyncTask<Void, Void, Void> {
        String host;
        int port;
        byte[] request, outputStates, inputStates, response;
        boolean update = true, firstRun;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        UpdateTask(String host, int port, boolean firstRun) {
            this.host = host;
            this.port = port;
            this.firstRun = firstRun;
        }

        UpdateTask(String host, int port) {
            this.host = host;
            this.port = port;
            this.firstRun = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket = new Socket();
            OutputStream outputStream = null;
            InputStream input = null;
            try {
                socket.setSoTimeout(timeout);
                SocketAddress address = new InetSocketAddress(host, port);
                socket.connect(address, timeout);

                outputStream = socket.getOutputStream();
                input = socket.getInputStream();

                request = Commands.commandInfo;
                outputStream.write(request, 0, request.length);
                outputStream.flush();

                response = new byte[64];
                input.read(response, 0, response.length);
                outputStates = Commands.getOutputStates(response[2], response[3]);
                inputStates = Commands.getOutputStates(response[4], response[5]);
            } catch (IOException e) {
                update = false;
                Log.d("ERROR", e.getMessage() + " On Update, " + update);
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
                actionBar.setTitle(Html.fromHtml("<font color='#00E676'>"+ controller.getIp() + ":" + port+"</font>"));
                actionBar.setSubtitle(Html.fromHtml("<font color='#00E676'>" + controller.getName() + "</font>"));
                String first = String.format("%8s", Integer.toBinaryString(response[7] & 0xFF)).replace(" ", "0")
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
                    if (firstRun || getByteByBoolean(insOutsList.get(i).getOutputState()) != outputStates[i]
                            || getByteByBoolean(insOutsList.get(i).getInputState()) != inputStates[i]) {
                        insOutsList.get(i).setOutputState(getBooleanByByte(outputStates[i]));
                        insOutsList.get(i).setInputState(getBooleanByByte(inputStates[i]));
                        outputAdapter.notifyItemChanged(i);
                        inputAdapter.notifyItemChanged(i);
                        Log.d("Update", "Update " + i);
                    }
                }
            } else {
                actionBar.setTitle(Html.fromHtml("<font color='#FF1744'>" + controller.getIp() + ":" + port + "</font>"));
                if (controller.getName().length() > 0)
                    actionBar.setSubtitle(Html.fromHtml("<font color='#FF1744'>" + controller.getName() + "</font>"));
                else
                    actionBar.setSubtitle(Html.fromHtml("<font color='#FF1744'>Подключение...</font>"));
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(new Queue.Node(new UpdateTask(controller.getIp(), MainActivity.this.port)));
                    //queue.run();
                }
            }, timeout);
        }
    }

    public boolean getBooleanByByte(byte i) {
        return i == 1;
    }

    public byte getByteByBoolean(boolean i) {
        return i ? (byte) 1 : 0;
    }

    public class ControllersAdapter extends RecyclerView.Adapter<ControllersAdapter.ControllersViewHolder> {

        private List<Controller> controllersList;

        public ControllersAdapter(List<Controller> controllersList) {
            this.controllersList = controllersList;
        }

        @Override
        public ControllersAdapter.ControllersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.controller_button, parent, false);
            return new ControllersAdapter.ControllersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ControllersAdapter.ControllersViewHolder holder, final int position) {
            final Controller currentController = controllersList.get(position);
            final String[] splitIP = currentController.getIp().split("\\.");
            String controllerName = !currentController.getName().equals("") ?
                    (currentController.getName().length() > 8 ?
                            currentController.getName().substring(0, 8)+"...\n" :
                            currentController.getName()+"\n") : "";
            holder.controller.setText(controllerName+splitIP[2]+"."+splitIP[3]);
            holder.controller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBHelper dbHelper = new DBHelper(MainActivity.this);
                    prefs.edit().putInt(SettingsActivity.PREF_SELECTED_CONTROLLER, currentController.getId()).apply();
                    insOutsList.clear();
                    insOutsList.addAll(dbHelper.getInsOuts(currentController.getId()));
                    inputAdapter.notifyDataSetChanged();
                    outputAdapter.notifyDataSetChanged();
                    actionBar.setTitle(currentController.getIp() + ":" + port);
                    actionBar.setSubtitle(currentController.getName());
                    controller = currentController;
                    dbHelper.close();
                }
            });
        }

        @Override
        public int getItemCount() {
            return controllersList.size();
        }

        public class ControllersViewHolder extends RecyclerView.ViewHolder {
            Button controller;
            public ControllersViewHolder(View view) {
                super(view);
                controller = view.findViewById(R.id.controller_button);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
