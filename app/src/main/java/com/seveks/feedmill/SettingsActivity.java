package com.seveks.feedmill;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.seveks.feedmill.Fragments.ControllersSettingsFragment;
import com.seveks.feedmill.Fragments.InsOutsSettingsFragment;
import com.seveks.feedmill.Fragments.MainSettingsFragment;

public class SettingsActivity extends AppCompatActivity {


    public static final String  PREF_NAME = "settings",
                                PRESET_FRAGMENT = "preset_fragment",
                                CONTROLLERS_FRAGMENT = "preset_fragment",
                                INSOUTS_FRAGMENT = "preset_fragment",
                                PREF_SELECTED_PRESET = "selected_preset",
                                PREF_SELECTED_CONTROLLER = "PREF_SELECTED_CONTROLLER",
                                DIALOG_ADD_ITEM = "addItemDialogFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment mainSettingsFragment = new MainSettingsFragment();
        ft.setCustomAnimations(0, 0);
        ft.add(R.id.fragments_container, mainSettingsFragment, PRESET_FRAGMENT);
        ft.commit();

    }

    public void openControllerSettings(int presetId, String presetName){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment controllerSettingsFragment = ControllersSettingsFragment.newInstance(presetId, presetName);
        ft.addToBackStack(CONTROLLERS_FRAGMENT);
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.fragments_container, controllerSettingsFragment, CONTROLLERS_FRAGMENT);
        ft.commit();
    }

    public void openInsOutsSettings(int controllerId, String controllerIp, int presetID, String controllerName){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment insoutsSettingsFragment = InsOutsSettingsFragment.newInstance(controllerId, controllerIp, presetID, controllerName);
        ft.addToBackStack(INSOUTS_FRAGMENT);
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.fragments_container, insoutsSettingsFragment, INSOUTS_FRAGMENT);
        ft.commit();
    }
}
