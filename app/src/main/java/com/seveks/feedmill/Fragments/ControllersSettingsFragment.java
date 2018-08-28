package com.seveks.feedmill.Fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.seveks.feedmill.DataBase.Controller;
import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.SettingsActivity;

import java.util.ArrayList;

import com.seveks.feedmill.R;

public class ControllersSettingsFragment extends Fragment implements NameDialogFragment.OnDoneListener{

    public static final String BUNDLE_PRESET_ID = "bundle_preset_id";
    public static final String BUNDLE_PRESET_NAME = "bundle_preset_name";
    ArrayList<Controller> dataset;
    ControllersAdapter adapter;
    int presetId;
    String presetName;
    Toolbar toolbar;
    boolean touching = false, contextMenuOpen = false, isDialogOpen = false;
    View lastOpenedContextMenu;

    public static ControllersSettingsFragment newInstance(int presetId, String presetName) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_PRESET_ID, presetId);
        args.putString(BUNDLE_PRESET_NAME, presetName);
        ControllersSettingsFragment fragment = new ControllersSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presetId = getArguments().getInt(BUNDLE_PRESET_ID);
        presetName = getArguments().getString(BUNDLE_PRESET_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controllers_settings, container, false);
        Log.d("TAG","drawing fragment");
        setHasOptionsMenu(true);

        toolbar = view.findViewById(R.id.controllers_toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setTitle(presetName);
        toolbar.setSubtitle("Настройки шаблона");
        toolbar.setNavigationIcon(R.drawable.ic_edit);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        RecyclerView recyclerView = view.findViewById(R.id.controllersRecycler);
        DBHelper dbHelper = new DBHelper(getContext());
        dataset = dbHelper.getControllers(presetId);
        adapter = new ControllersAdapter(dataset);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem addController = menu.add(0,0,0, "Добавить контроллер");
        addController.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        addController.setIcon(R.drawable.ic_add);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0){
            DialogFragment changeIpDialogFragment = ChangeIpDialog.newInstance(new ChangeIpDialog.DismissListener() {
                @Override
                public void accepted(String ip, int port, String name) {
                    DBHelper dbHelper = new DBHelper(getContext());
                    if (dbHelper.getControllerByIp(presetId, ip) == null) {
                        dbHelper.insertController(presetId, ip, name);
                        dataset.clear();
                        dataset.addAll(dbHelper.getControllers(presetId));
                        adapter.notifyDataSetChanged();
                        dbHelper.close();
                    } else {
                        Toast.makeText(getContext(), "В этой настройке уже есть контроллер с таким IP", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void dismissed() {

                }
            }, false);
            changeIpDialogFragment.show(this.getChildFragmentManager(), SettingsActivity.DIALOG_ADD_ITEM);
        }
        if (item.getItemId() == android.R.id.home)
            openDialog();
        return false;
    }

    private void openDialog() {
        if(!isDialogOpen) {
            DialogFragment addPresetDialogFragment = NameDialogFragment.newInstance(this, NameDialogFragment.CHANGE_PRESET_NAME, presetName);
            addPresetDialogFragment.show(this.getChildFragmentManager(), SettingsActivity.DIALOG_ADD_ITEM);
            isDialogOpen = true;
        }
    }

    @Override
    public void onDone(String string) {
        DBHelper dbHelper = new DBHelper(getContext());
        dbHelper.editPreset(presetId, string);
        dbHelper.close();
        presetName = string;
        toolbar.setTitle(presetName);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public void onDismissed() {
        isDialogOpen = false;
    }


    public class ControllersAdapter extends RecyclerView.Adapter<ControllersAdapter.ControllerViewHolder>{

        ArrayList<Controller> dataset;

        public class ControllerViewHolder extends RecyclerView.ViewHolder{

            LinearLayout controllerCard;
            View btnsContainer;
            TextView controllerIpText, controllerNameText;
            ImageButton editBtn, removeBtn;

            public ControllerViewHolder(View itemView) {
                super(itemView);
                controllerCard = itemView.findViewById(R.id.controllerCard);
                controllerIpText = itemView.findViewById(R.id.controllerIpText);
                controllerNameText = itemView.findViewById(R.id.controllerNameText);
                btnsContainer = itemView.findViewById(R.id.btnsContainer);
                editBtn = itemView.findViewById(R.id.edit_btn);
                removeBtn = itemView.findViewById(R.id.remove_btn);
            }
        }

        public ControllersAdapter(ArrayList<Controller> dataset){
            this.dataset = dataset;
        }

        @NonNull
        @Override
        public ControllerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.controller_recycler_item,viewGroup,false);
            return new ControllerViewHolder(view);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(final ControllerViewHolder holder, final int i) {
            final String controllerIp = dataset.get(i).getIp();
            final int controllerId = dataset.get(i).getId();
            final String controllerName = dataset.get(i).getName();
            holder.controllerIpText.setText(controllerIp);
            holder.controllerNameText.setText(controllerName);

            holder.controllerCard.setOnTouchListener(new View.OnTouchListener() {
                float mDownX;
                float mRawDawnX;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN : {
                            if (touching){
                                return false;
                            }
                            touching = true;
                            mDownX = event.getX();
                            mRawDawnX = event.getRawX();
                            break;
                        }
                        case MotionEvent.ACTION_MOVE : {

                            float x = event.getX()+v.getTranslationX();
                            float deltaX = x - mDownX;

                            View btns_container = holder.btnsContainer;

                            if (deltaX <= 0 && deltaX >= -btns_container.getWidth() ){
                                v.setTranslationX(deltaX);
                            }

                            break;
                        }
                        case MotionEvent.ACTION_CANCEL :
                        case MotionEvent.ACTION_UP : {

                            float deltaX = event.getRawX()-mRawDawnX;
                            float absDeltaX = Math.abs(deltaX);
                            if (absDeltaX>20) {
                                if (deltaX < 0) {
                                    if (contextMenuOpen) {
                                        lastOpenedContextMenu.animate().translationX(0).setDuration(200).start();
                                    }
                                    v.animate().translationX(-holder.btnsContainer.getWidth()).setDuration(200).start();
                                    contextMenuOpen = true;
                                    lastOpenedContextMenu = v;
                                } else {
                                    v.animate().translationX(-1).setDuration(200).start();
                                    if (lastOpenedContextMenu == v) contextMenuOpen = false;
                                }
                            } else {
                                v.animate().translationX(0).setDuration(50).start();
                            }
                            Log.d("TAG",String.valueOf(deltaX));
                            touching = false;
                            break;
                        }
                    }
                    return true;
                }
            });
            holder.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SettingsActivity)getActivity()).openInsOutsSettings(controllerId, controllerIp, presetId, controllerName);
                }
            });
            holder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Подтверждение")
                            .setMessage("Вы точно уверены, что хотите удалить этот контроллер?")
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DBHelper dbHelper = new DBHelper(getContext());
                                    //dataset.clear();
                                    dbHelper.removeController(controllerId);
                                    dataset.remove(i);
                                    adapter.notifyDataSetChanged();
                                    lastOpenedContextMenu.animate().translationX(0).setDuration(200).start();
                                    SharedPreferences prefs = getContext().getSharedPreferences(SettingsActivity.PREF_NAME, getContext().MODE_PRIVATE);
                                    if(prefs.getInt(SettingsActivity.PREF_SELECTED_CONTROLLER, -1) == controllerId) {
                                        prefs.edit().remove(SettingsActivity.PREF_SELECTED_CONTROLLER).apply();
                                    }
                                    dbHelper.close();
                                    lastOpenedContextMenu.animate().translationX(0).setDuration(200).start();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    lastOpenedContextMenu.animate().translationX(0).setDuration(200).start();
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (dataset != null) return dataset.size();
            return 0;
        }
    }
}
