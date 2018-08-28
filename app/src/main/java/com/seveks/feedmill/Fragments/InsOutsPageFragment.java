package com.seveks.feedmill.Fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.Outputs;
import com.seveks.feedmill.SettingsActivity;
import java.util.ArrayList;
import com.seveks.feedmill.R;

public class InsOutsPageFragment extends Fragment{

    private static final String TAG = InsOutsPageFragment.class.getSimpleName();
    private static final String BUNDLE_IN_OR_OUT = "bundle_in_or_out";

    //mInOrOut - переменная отвечающая за то что показано на странице (true — выходы; false — входы );
    private boolean mInOrOut, isDialogOpen = false;
    private ArrayList<Outputs> mDataset;
    View view;

    public static InsOutsPageFragment newInstance(boolean inOrOut, ArrayList<Outputs> dataset) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_IN_OR_OUT, inOrOut);
        InsOutsPageFragment fragment = new InsOutsPageFragment();
        fragment.setArguments(args);
        fragment.mDataset = dataset;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInOrOut = getArguments().getBoolean(BUNDLE_IN_OR_OUT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_in_or_out_page, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.inoutsRecycler);
        InsOutsAdapter adapter = new InsOutsAdapter(mDataset);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    private class InsOutsAdapter extends RecyclerView.Adapter<InsOutsAdapter.InOutViewHolder> {

        ArrayList<Outputs> dataset;

        public InsOutsAdapter(ArrayList<Outputs> dataset){
            this.dataset = dataset;
        }

        @NonNull
        @Override
        public InOutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new InOutViewHolder(v);
        }

        @Override
        public void onBindViewHolder(InOutViewHolder holder, final int position) {
            final String title, subtitle;
            title = String.valueOf(dataset.get(position).getNumber());
            if (mInOrOut) subtitle = dataset.get(position).getOutputDescription();
            else subtitle = dataset.get(position).getInputDescription();

            holder.title.setText(title);
            holder.subtitle.setText(subtitle);
            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isDialogOpen) {
                        DialogFragment changeDescription = NameDialogFragment.newInstance(new NameDialogFragment.OnDoneListener() {
                            @Override
                            public void onDone(String string) {
                                DBHelper dbHelper = new DBHelper(getContext());
                                if(mInOrOut) {
                                    dbHelper.editOut(dataset.get(position).getOutId(), string);
                                    dataset.get(position).setOutputDescription(string);
                                } else {
                                    dbHelper.editIn(dataset.get(position).getInId(), string);
                                    dataset.get(position).setInputDescription(string);
                                }
                                notifyItemChanged(position);
                                dbHelper.close();
                            }

                            @Override
                            public void onDismissed() {
                                isDialogOpen = false;
                            }
                        },
                                mInOrOut ? NameDialogFragment.CHANGE_OUT_DESCRIPTION : NameDialogFragment.CHANGE_IN_DESCRIPTION,
                                subtitle);
                        changeDescription.show(getChildFragmentManager(), SettingsActivity.DIALOG_ADD_ITEM);
                        isDialogOpen = true;
                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return dataset.size();
        }

        class InOutViewHolder extends RecyclerView.ViewHolder{
            TextView title, subtitle;
            View parent;
            public InOutViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
                parent = itemView;
                title.setTextSize(20);
                title.setTypeface(Typeface.DEFAULT_BOLD);
                subtitle.setTextSize(16);

            }
        }
    }
}
