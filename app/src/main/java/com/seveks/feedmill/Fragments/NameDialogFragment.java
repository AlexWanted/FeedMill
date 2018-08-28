package com.seveks.feedmill.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.seveks.feedmill.R;

public class NameDialogFragment extends DialogFragment {

    public static final int ADD_PRESET = 0, CHANGE_IN_DESCRIPTION = 1, CHANGE_OUT_DESCRIPTION = 2, CHANGE_PRESET_NAME = 3;
    public static final String BUNDLE_ITEM_TO_ADD = "item_to_add";

    public interface OnDoneListener {
        void onDone(String string);
        void onDismissed();
    }

    OnDoneListener listener;
    int itemToAdd;
    String name;

    public static NameDialogFragment newInstance(OnDoneListener listener, int itemToAdd) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_ITEM_TO_ADD, itemToAdd);
        NameDialogFragment fragment = new NameDialogFragment();
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    public static NameDialogFragment newInstance(OnDoneListener listener, int itemToAdd, String name) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_ITEM_TO_ADD, itemToAdd);
        NameDialogFragment fragment = new NameDialogFragment();
        fragment.setArguments(args);
        fragment.listener = listener;
        fragment.name = name;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null) {
            itemToAdd = getArguments().getInt(BUNDLE_ITEM_TO_ADD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_name, container, false);
        TextView title = view.findViewById(R.id.title);
        final EditText itemEdit = view.findViewById(R.id.itemEdit);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        switch (itemToAdd){
            case ADD_PRESET : {
                title.setText("Добавление шаблона");
                itemEdit.setHint("Введите название");
                break;
            }
            case CHANGE_IN_DESCRIPTION : {
                title.setText("Описание входа");
                itemEdit.setHint("Введите описание");
                //itemEdit.setText(name);
                break;
            }
            case CHANGE_OUT_DESCRIPTION : {
                title.setText("Описание выхода");
                itemEdit.setHint("Введите описание");
                //itemEdit.setText(name);
                break;
            }
            case CHANGE_PRESET_NAME : {
                title.setText("Изменение шаблона");
                itemEdit.setHint("Введите название");
                itemEdit.setText(name);
                break;
            }
        }
        itemEdit.setSelection(itemEdit.getText().length());
        itemEdit.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        view.findViewById(R.id.doneBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDone(itemEdit.getText().toString());
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.onDismissed();
    }
}
