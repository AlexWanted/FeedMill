package com.seveks.feedmill.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.seveks.feedmill.DataBase.Controller;
import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.MainActivity;
import com.seveks.feedmill.R;
import com.seveks.feedmill.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class ChangeIpDialog extends DialogFragment {


    private DismissListener listener;
    private String ip = null, controllerName = null, previousText = "";
    private boolean showPort;
    private EditText[] fields;
    private EditText name;
    int presetID;
    DBHelper dbHelper;
    View view;
    public static ChangeIpDialog newInstance(DismissListener listener, boolean showPort) {
        ChangeIpDialog fragment = new ChangeIpDialog();
        fragment.listener = listener;
        fragment.showPort = showPort;
        return fragment;
    }


    public static ChangeIpDialog newInstance(DismissListener listener, String ip, String name) {
        ChangeIpDialog fragment = new ChangeIpDialog();
        fragment.listener = listener;
        fragment.ip = ip;
        fragment.showPort = false;
        fragment.controllerName = name;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_change_ip, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        presetID = getContext().getSharedPreferences(SettingsActivity.PREF_NAME, getContext().MODE_PRIVATE).getInt(SettingsActivity.PREF_SELECTED_PRESET, -1);
        dbHelper = new DBHelper(getContext());
        fields = new EditText[]{
                view.findViewById(R.id.one),
                view.findViewById(R.id.two),
                view.findViewById(R.id.three),
                view.findViewById(R.id.four),
                view.findViewById(R.id.port)
        };
        name = view.findViewById(R.id.name);

        for (int i = 0; i < fields.length; i++) {
            fields[i].addTextChangedListener(createTextWatcher(i));
            fields[i].setOnKeyListener(createKeyListener(i));
        }

        view.findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasAllDigits = !fields[0].getText().toString().equals("") &&
                        !fields[1].getText().toString().equals("") &&
                        !fields[2].getText().toString().equals("") &&
                        !fields[3].getText().toString().equals("");
                if ((hasAllDigits && !showPort)
                        | (hasAllDigits && showPort && !fields[4].getText().toString().equals(""))) {
                    dismiss();
                    String portText = fields[4].getText().toString();
                    listener.accepted(fields[0].getText() + "." +
                                    fields[1].getText() + "." +
                                    fields[2].getText() + "." +
                                    fields[3].getText(),
                                    Integer.parseInt(portText.equals("") ? "0" : portText),
                                    name.getText().toString());
                } else {
                    Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_LONG).show();
                }

            }
        });

        if(!showPort) {
            fields[4].setVisibility(View.GONE);
            view.findViewById(R.id.colon).setVisibility(View.GONE);
        } else {
            int currentPort = getContext().getSharedPreferences(SettingsActivity.PREF_NAME, getContext().MODE_PRIVATE).getInt(MainActivity.PREF_PORT, 2001);
            fields[4].setText(String.valueOf(currentPort));
            /*DBHelper dbHelper = new DBHelper(getContext());
            ArrayList<Controller> controllersList = dbHelper.getControllers(presetID);
            if(controllersList.size() > 0) {
                ControllersAdapter adapter = new ControllersAdapter(controllersList);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                RecyclerView controllersRecycler = view.findViewById(R.id.controllers_recycler);
                controllersRecycler.setLayoutManager(layoutManager);
                controllersRecycler.setItemAnimator(null);
                controllersRecycler.setAdapter(adapter);
            } else {
                view.findViewById(R.id.controllers_recycler).setVisibility(View.GONE);
            }*/
        }
        if(ip != null ) {
            String[] splitIP = ip.split("[.]");
            fields[0].setText(splitIP[0]);
            fields[1].setText(splitIP[1]);
            fields[2].setText(splitIP[2]);
            fields[3].setText(splitIP[3]);
            fields[3].requestFocus();
            fields[3].setSelection(fields[3].getText().length());
            name.setText(controllerName);
        } else fields[2].requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.dismissed();
    }

    public interface DismissListener {
        void accepted(String ip, int port, String name);
        void dismissed();
    }

    private View.OnKeyListener createKeyListener(final int position) {
        return new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DEL && position > 0)
                        if(fields[position].getText().length() == 0) {
                            fields[position - 1].setSelection(fields[position - 1].getText().length());
                            fields[position - 1].requestFocus();
                            return true;
                        }
                    if(keyCode != KeyEvent.KEYCODE_DEL && position < 4)
                        if(fields[position].getText().length() == 3) {
                            fields[position + 1].setSelection(fields[position + 1].getText().length());
                            fields[position + 1].requestFocus();
                            return true;
                        }
                }
                return false;
            }
        };
    }

    private TextWatcher createTextWatcher(final int position) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 3) {
                    if( position < 4) {
                        fields[position + 1].requestFocus();
                        fields[position + 1].setSelection(fields[position + 1].getText().length());
                    }
                } else if (s.length() == 0) {
                    if( position > 0) {
                        fields[position - 1].requestFocus();
                        fields[position - 1].setSelection(fields[position - 1].getText().length());
                    }
                }

                if(!s.toString().equals("")) {
                    if(position < 4 && Integer.parseInt(s.toString()) > 255) {
                        fields[position].setText("255");
                        fields[position].setSelection(fields[position].getText().length());
                    } else if(Integer.parseInt(s.toString()) > 65535) {
                        fields[position].setText("65535");
                        fields[position].setSelection(fields[position].getText().length());
                    }
                }

                String currentIP = fields[0].getText() + "." +
                        fields[1].getText() + "." +
                        fields[2].getText() + "." +
                        fields[3].getText();
                if (dbHelper.getControllerByIp(presetID, currentIP) == null) {
                    if (((TextView) view.findViewById(R.id.name)).getText().toString().equals(previousText))
                        ((TextView) view.findViewById(R.id.name)).setText("");
                    Log.d("NAME", "NO");
                } else {
                    previousText = dbHelper.getControllerByIp(presetID, currentIP).getName();
                    Log.d("NAME", previousText);
                    ((TextView) view.findViewById(R.id.name)).setText(previousText);
                }

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }


}
