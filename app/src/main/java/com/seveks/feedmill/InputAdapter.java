package com.seveks.feedmill;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seveks.feedmill.DataBase.DBHelper;
import com.seveks.feedmill.Fragments.NameDialogFragment;
import java.util.List;

public class InputAdapter extends RecyclerView.Adapter<InputAdapter.InputViewHolder> {

    private List<Outputs> inputsList;

    Drawable[] drawables;
    Context context;

    public InputAdapter(Context context, List<Outputs> inputsList) {
        this.inputsList = inputsList;
        this.context = context;
        drawables = new Drawable[2];
        drawables[0] = ContextCompat.getDrawable(context, R.drawable.round_indicator_on);
        drawables[1] = ContextCompat.getDrawable(context, R.drawable.round_indicator_off);
    }

    @Override
    public InputAdapter.InputViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.controller_input, parent, false);
        return new InputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InputAdapter.InputViewHolder holder, int position) {
        Outputs currentOutput = inputsList.get(position);
        holder.description.setText(currentOutput.getInputDescription());
        holder.name.setText(String.valueOf(currentOutput.getNumber()+"."));
        holder.inputIndicator.setBackground(currentOutput.getInputState() ? drawables[0] : drawables[1]);
    }

    @Override
    public int getItemCount() {
        return inputsList.size();
    }

    public class InputViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView name, description;
        View inputIndicator;

        public InputViewHolder(View view) {
            super(view);
            final NameDialogFragment.OnDoneListener onDoneListener = new NameDialogFragment.OnDoneListener() {
                @Override
                public void onDone(String string) {
                    DBHelper dbHelper = new DBHelper(context);
                    if(string.length() == 0) string = String.valueOf(getAdapterPosition() + 1);
                    dbHelper.editIn(inputsList.get(getAdapterPosition()).getOutId(), string);
                    inputsList.get(getAdapterPosition()).setInputDescription(string);
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
                            NameDialogFragment.CHANGE_IN_DESCRIPTION,
                            description.getText().toString());
                    changeDescription.show(((AppCompatActivity)context).getSupportFragmentManager(), SettingsActivity.DIALOG_ADD_ITEM);
                    return true;
                }
            };

            cardView = view.findViewById(R.id.card_view);
            cardView.setOnLongClickListener(longListener);
            name = view.findViewById(R.id.name);
            description = view.findViewById(R.id.description);
            inputIndicator = view.findViewById(R.id.input_switch);
            inputIndicator.setClickable(false);
        }
    }
}
