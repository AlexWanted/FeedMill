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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.DataBase.Preset;
import com.seveks.feedmill.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

import com.seveks.feedmill.R;

public class MainSettingsFragment extends Fragment implements NameDialogFragment.OnDoneListener {

    ArrayList<Preset> dataset;
    PresetsAdapter adapter;
    SharedPreferences shPr;
    boolean touching = false, contextMenuOpen = false, isDialogOpen = false;
    View lastOpenedContextMenu;
    int currentPresetPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_settings, container, false);
        Log.d("TAG","drawing fragment");


        if (getContext() != null)
        shPr = getContext().getSharedPreferences(SettingsActivity.PREF_NAME, getContext().MODE_PRIVATE);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Настройки");
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        RecyclerView recyclerView = view.findViewById(R.id.presetsRecycler);
        DBHelper dbHelper = new DBHelper(getContext());
        dataset = dbHelper.getPresets();
        adapter = new PresetsAdapter(dataset);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem addPreset = menu.add(0,0,0, "Добавить настройку");
        addPreset.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        addPreset.setIcon(R.drawable.ic_add);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0){
            if(!isDialogOpen) {
                DialogFragment addPresetDialogFragment = NameDialogFragment.newInstance(this, NameDialogFragment.ADD_PRESET);
                addPresetDialogFragment.show(this.getChildFragmentManager(), SettingsActivity.DIALOG_ADD_ITEM);
                isDialogOpen = true;
            }
        }
        return false;
    }

    @Override
    public void onDone(String string) {
        DBHelper dbHelper = new DBHelper(getContext());
        dbHelper.insertPreset(string);
        dataset.clear();
        dataset.addAll(dbHelper.getPresets());
        adapter.notifyDataSetChanged();
        dbHelper.close();
    }

    @Override
    public void onDismissed() {
        isDialogOpen = false;
    }


    public class PresetsAdapter extends RecyclerView.Adapter<PresetsAdapter.PresetViewHolder>{

        List<Preset> dataset;

        public class PresetViewHolder extends RecyclerView.ViewHolder{

            LinearLayout presetCard;
            View btnsContainer;
            TextView presetText;
            RadioButton presetRadioBtn;
            ImageButton editPresetBtn, removePresetBtn;

            public PresetViewHolder(View itemView) {
                super(itemView);
                presetCard = itemView.findViewById(R.id.presetCard);
                btnsContainer = itemView.findViewById(R.id.btnsContainer);
                presetText = itemView.findViewById(R.id.presetText);
                presetRadioBtn = itemView.findViewById(R.id.presetRadioBtn);
                editPresetBtn = itemView.findViewById(R.id.edit_btn);
                removePresetBtn = itemView.findViewById(R.id.remove_btn);
            }
        }

        public PresetsAdapter(ArrayList<Preset> dataset){
            this.dataset = dataset;
        }

        @NonNull
        @Override
        public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.preset_recycler_item,viewGroup,false);
            return new PresetViewHolder(view);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull final PresetViewHolder holder, final int i) {
            final String presetName = dataset.get(i).getPresetName();
            final int presetId = dataset.get(i).getPresetId();
            final int selectedPresetId = shPr.getInt(SettingsActivity.PREF_SELECTED_PRESET, -1);
            holder.presetText.setText(presetName);
            if (presetId == selectedPresetId) {
                currentPresetPosition = i;
                holder.presetRadioBtn.setChecked(true);
            } else holder.presetRadioBtn.setChecked(false);
            holder.presetRadioBtn.setClickable(false);
            holder.presetCard.setOnTouchListener(new View.OnTouchListener() {
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
                            if (absDeltaX>10) {
                                if (deltaX < 0) {
                                    if (contextMenuOpen) {
                                        closeLastOpenContextMenu();
                                    }
                                    v.animate().translationX(-holder.btnsContainer.getWidth()).setDuration(200).start();
                                    contextMenuOpen = true;
                                    lastOpenedContextMenu = v;
                                } else {
                                    v.animate().translationX(0).setDuration(200).start();
                                    if (lastOpenedContextMenu == v) contextMenuOpen = false;
                                }
                            } else {
                                if (!holder.presetRadioBtn.isChecked()){
                                    shPr.edit().putInt(SettingsActivity.PREF_SELECTED_PRESET,
                                            dataset.get(i).getPresetId()).apply();
                                    shPr.edit().remove(SettingsActivity.PREF_SELECTED_CONTROLLER).apply();
                                    adapter.notifyItemChanged(currentPresetPosition);
                                    adapter.notifyItemChanged(i);
                                    currentPresetPosition = i;
                                    //adapter.notifyDataSetChanged();
                                }
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
            holder.editPresetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SettingsActivity)getActivity()).openControllerSettings(presetId, presetName);
                }
            });
            holder.removePresetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DBHelper dbHelper = new DBHelper(getContext());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Подтверждение")
                            .setMessage("Вы точно уверены, что хотите удалить эту настройку?")
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper.removePreset(presetId);
                                    int currentPresetId = dataset.get(i).getPresetId();
                                    dataset.remove(i);
                                    adapter.notifyItemRemoved(i);
                                    if(selectedPresetId == currentPresetId) {
                                        shPr.edit().putInt(SettingsActivity.PREF_SELECTED_PRESET,
                                                dataset.get(0).getPresetId()).apply();
                                        shPr.edit().remove(SettingsActivity.PREF_SELECTED_CONTROLLER).apply();
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    if(dbHelper.getPresets().size() == 1)
                        Toast.makeText(getContext(), "Вы не можете удалить единственную настройку", Toast.LENGTH_LONG).show();
                    else builder.create().show();
                    closeLastOpenContextMenu();
                    dbHelper.close();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (dataset != null) return dataset.size();
            return 0;
        }
    }
    public void closeLastOpenContextMenu(){
        if (lastOpenedContextMenu != null) lastOpenedContextMenu.animate().translationX(0).setDuration(200).start();
    }
}
