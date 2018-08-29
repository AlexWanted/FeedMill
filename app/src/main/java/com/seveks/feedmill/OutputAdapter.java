package com.seveks.feedmill;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.Fragments.NameDialogFragment;
import java.util.List;

public class OutputAdapter extends RecyclerView.Adapter<OutputAdapter.OutputViewHolder> {

    private List<Outputs> outputsList;
    private ClickListener clickListener;
    private Context context;
    public OutputAdapter(Context context, List<Outputs> outputsList) {
        this.outputsList = outputsList;
        this.context = context;
    }

    public void setOnClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public OutputAdapter.OutputViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.controller_output, parent, false);
        return new OutputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OutputAdapter.OutputViewHolder holder, int position) {
        Outputs currentOutput = outputsList.get(position);
        holder.description.setText(Html.fromHtml("<font color='#000000'><b>"+currentOutput.getNumber()+".</b></font> "
                +currentOutput.getOutputDescription()));
        holder.outputSwitch.setChecked(currentOutput.getOutputState());
        int animDuration = 150;
        if (currentOutput.isLoading()){
            holder.progressBar.animate().alpha(1).setDuration(animDuration).start();
            holder.foregroundTint.animate().alpha(1).setDuration(animDuration).start();
        } else {
            holder.progressBar.animate().alpha(0).setDuration(animDuration).start();
            holder.foregroundTint.animate().alpha(0).setDuration(animDuration).start();
        }
    }

    @Override
    public int getItemCount() {
        return outputsList.size();
    }

    public class OutputViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView description;
        SwitchCompat outputSwitch;
        View progressBar, foregroundTint;
        public OutputViewHolder(View view) {
            super(view);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(clickListener!=null) clickListener.outputClicked(view, getAdapterPosition());
                }
            };

            final NameDialogFragment.OnDoneListener onDoneListener = new NameDialogFragment.OnDoneListener() {
                @Override
                public void onDone(String string) {
                    DBHelper dbHelper = new DBHelper(context);
                    dbHelper.editOut(outputsList.get(getAdapterPosition()).getOutId(), string);
                    outputsList.get(getAdapterPosition()).setOutputDescription(string);
                    notifyItemChanged(getAdapterPosition());
                    dbHelper.close();
                }

                @Override
                public void onDismissed() { }
            };
            View.OnLongClickListener longListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    DialogFragment changeDescription = NameDialogFragment.newInstance(onDoneListener,
                            NameDialogFragment.CHANGE_OUT_DESCRIPTION,
                            description.getText().toString());
                    changeDescription.show(((AppCompatActivity)context).getSupportFragmentManager(), SettingsActivity.DIALOG_ADD_ITEM);
                    return true;
                }
            };


            cardView = view.findViewById(R.id.card_view);
            cardView.setOnClickListener(listener);
            cardView.setOnLongClickListener(longListener);
            description = view.findViewById(R.id.description);
            outputSwitch = view.findViewById(R.id.output_switch);
            outputSwitch.setOnClickListener(listener);
            outputSwitch.setOnLongClickListener(longListener);
            outputSwitch.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return event.getActionMasked() == MotionEvent.ACTION_MOVE;
                }
            });
            outputSwitch.setClickable(false);
            progressBar = view.findViewById(R.id.progress);
            foregroundTint = view.findViewById(R.id.foreground_tint);
        }
    }

    public interface ClickListener {
        void outputClicked(View view, int position);
    }
}
