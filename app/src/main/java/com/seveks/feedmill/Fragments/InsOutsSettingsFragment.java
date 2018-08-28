package com.seveks.feedmill.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.seveks.feedmill.DataBase.Controller;
import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.Outputs;

import java.util.ArrayList;

import com.seveks.feedmill.R;

public class InsOutsSettingsFragment extends Fragment implements ChangeIpDialog.DismissListener {

    private static final String TAG = InsOutsSettingsFragment.class.getSimpleName();
    public static final String BUNDLE_CONTROLLER_ID = "bundle_controller_id";
    public static final String BUNDLE_CONTROLLER_IP = "bundle_controller_ip";
    public static final String BUNDLE_CONTROLLER_NAME = "bundle_controller_name";
    public static final String BUNDLE_PRESET_ID = "bundle_preset_id";
    ArrayList<Outputs> dataset;
    int presetID;
    Controller controller;
    TabLayout tabLayout;
    ViewPager viewPager;
    InsOutsPagerAdapter pagerAdapter;
    Toolbar toolbar;

    public static InsOutsSettingsFragment newInstance(int controllerId, String controllerIp, int presetID, String controllerName) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_CONTROLLER_ID, controllerId);
        args.putString(BUNDLE_CONTROLLER_IP, controllerIp);
        args.putString(BUNDLE_CONTROLLER_NAME, controllerName);
        args.putInt(BUNDLE_PRESET_ID, presetID);
        InsOutsSettingsFragment fragment = new InsOutsSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int controllerId = getArguments().getInt(BUNDLE_CONTROLLER_ID);
        presetID = getArguments().getInt(BUNDLE_PRESET_ID);
        controller = new DBHelper(getContext()).getControllerById(controllerId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insouts_settings, container, false);
        setHasOptionsMenu(true);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setTitle(controller.getName());
        toolbar.setSubtitle(controller.getIp());
        toolbar.setNavigationIcon(R.drawable.ic_edit);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DBHelper dbHelper = new DBHelper(getContext());
        dataset = dbHelper.getInsOuts(controller.getId());
        dbHelper.close();

        pagerAdapter = new InsOutsPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager, true);
        pagerAdapter.notifyDataSetChanged();
    }

    private void openDialog() {
        ChangeIpDialog changeIpDialogFragment = ChangeIpDialog.newInstance(this, controller.getIp(), controller.getName());
        changeIpDialogFragment.show(getChildFragmentManager(), "dialog_change_ip");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            openDialog();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void accepted(String ip, int port, String name) {
        DBHelper dbHelper = new DBHelper(getContext());
        if ( (!ip.equals(controller.getIp()) && dbHelper.getControllerByIp(presetID, ip) == null) ||
             (ip.equals(controller.getIp())  && !name.equals(controller.getName())) ||
             (!ip.equals(controller.getIp()) && dbHelper.getControllerByIp(presetID, ip) == null && !name.equals(controller.getName())) ){
            dbHelper.editController(controller.getId(), ip, name);
            controller.setIp(ip);
            controller.setName(name);
            toolbar.setTitle(name);
            toolbar.setSubtitle(ip);
        } else Toast.makeText(getContext(), "В этой настройке уже есть контроллер с таким IP", Toast.LENGTH_LONG).show();
        dbHelper.close();

    }

    @Override
    public void dismissed() {

    }

    class InsOutsPagerAdapter extends FragmentPagerAdapter {

        public InsOutsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position==0 ? "Выходы" : "Входы";
        }

        @Override
        public Fragment getItem(int position) {
            return InsOutsPageFragment.newInstance(position==0, dataset);
        }
    }
}
