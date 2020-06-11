package io.github.muntashirakon.AppManager.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import io.github.muntashirakon.AppManager.R;

public class EditPrefItemFragment extends DialogFragment {
    public static final String TAG = "EditPrefItemDialogFragment";
    public static final String ARG_PREF_ITEM = "ARG_PREF_ITEM";
    public static final String ARG_MODE = "ARG_MODE";

    public static final int MODE_EDIT = 1;  // Key name is disabled
    public static final int MODE_CREATE = 2;  // Key name is not disabled
    public static final int MODE_DELETE = 3;

    private static final int TYPE_BOOLEAN = 0;
    private static final int TYPE_FLOAT   = 1;
    private static final int TYPE_INTEGER = 2;
    private static final int TYPE_LONG    = 3;
    private static final int TYPE_STRING  = 4;

    public InterfaceCommunicator interfaceCommunicator;

    public interface InterfaceCommunicator {
        void sendInfo(int mode, PrefItem prefItem);
    }

    public static class PrefItem implements Parcelable {
        public String keyName;
        public Object keyValue;

        public PrefItem(){}

        protected PrefItem(@NonNull Parcel in) {
            keyName = in.readString();
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(keyName);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PrefItem> CREATOR = new Creator<PrefItem>() {
            @Override
            public PrefItem createFromParcel(Parcel in) {
                return new PrefItem(in);
            }

            @Override
            public PrefItem[] newArray(int size) {
                return new PrefItem[size];
            }
        };
    }

    private LinearLayout[] mLayoutTypes = new LinearLayout[5];
    private TextView[] mValues = new TextView[5];
    private int currentType;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() == null) return super.onCreateDialog(savedInstanceState);
        if (getArguments() == null) return super.onCreateDialog(savedInstanceState);

        PrefItem prefItem = getArguments().getParcelable(ARG_PREF_ITEM);
        int mode = getArguments().getInt(ARG_MODE);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_edit_pref_item, null);
        Spinner spinner = view.findViewById(R.id.type_selector_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.shared_pref_types, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (LinearLayout layout: mLayoutTypes) layout.setVisibility(View.GONE);
                mLayoutTypes[position].setVisibility(View.VISIBLE);
                currentType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        // Set layouts
        mLayoutTypes[TYPE_BOOLEAN] = view.findViewById(R.id.layout_bool);
        mLayoutTypes[TYPE_FLOAT] = view.findViewById(R.id.layout_float);
        mLayoutTypes[TYPE_INTEGER] = view.findViewById(R.id.layout_int);
        mLayoutTypes[TYPE_LONG] = view.findViewById(R.id.layout_long);
        mLayoutTypes[TYPE_STRING] = view.findViewById(R.id.layout_string);
        // Set views
        mValues[TYPE_BOOLEAN] = view.findViewById(R.id.input_bool);
        mValues[TYPE_FLOAT] = view.findViewById(R.id.input_float);
        mValues[TYPE_INTEGER] = view.findViewById(R.id.input_int);
        mValues[TYPE_LONG] = view.findViewById(R.id.input_long);
        mValues[TYPE_STRING] = view.findViewById(R.id.input_string);
        // Key name
        EditText editKeyName = view.findViewById(R.id.key_name);
        if (prefItem != null) {
            String keyName = prefItem.keyName;
            Object keyValue = prefItem.keyValue;
            editKeyName.setText(keyName);
            if (mode == MODE_EDIT) editKeyName.setEnabled(false);
            // Key value
            if (keyValue instanceof Boolean) {
                currentType = TYPE_BOOLEAN;
                mLayoutTypes[TYPE_BOOLEAN].setVisibility(View.VISIBLE);
                ((Switch) mValues[TYPE_BOOLEAN]).setChecked((Boolean) keyValue);
                spinner.setSelection(TYPE_BOOLEAN);
            } else if (keyValue instanceof Float) {
                currentType = TYPE_FLOAT;
                mLayoutTypes[TYPE_FLOAT].setVisibility(View.VISIBLE);
                mValues[TYPE_FLOAT].setText(keyValue.toString());
                spinner.setSelection(TYPE_FLOAT);
            } else if (keyValue instanceof Integer) {
                currentType = TYPE_INTEGER;
                mLayoutTypes[TYPE_INTEGER].setVisibility(View.VISIBLE);
                mValues[TYPE_INTEGER].setText(keyValue.toString());
                spinner.setSelection(TYPE_INTEGER);
            } else if (keyValue instanceof Long) {
                currentType = TYPE_LONG;
                mLayoutTypes[TYPE_LONG].setVisibility(View.VISIBLE);
                mValues[TYPE_LONG].setText(keyValue.toString());
                spinner.setSelection(TYPE_LONG);
            } else if (keyValue instanceof String) {
                currentType = TYPE_STRING;
                mLayoutTypes[TYPE_LONG].setVisibility(View.VISIBLE);
                mValues[TYPE_STRING].setText((String) keyValue);
                spinner.setSelection(TYPE_STRING);
            }
        }
        interfaceCommunicator = (InterfaceCommunicator) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        builder.setView(view)
                .setPositiveButton(mode == MODE_CREATE ? R.string.add_item : R.string.done, (dialog, which) -> {
                    PrefItem newPrefItem;
                    if (prefItem != null) newPrefItem = prefItem;
                    else {
                        newPrefItem = new PrefItem();
                        newPrefItem.keyName = editKeyName.getText().toString();
                    }
                    if (newPrefItem.keyName == null) {
                        Toast.makeText(getActivity(), R.string.key_name_cannot_be_null, Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        switch (currentType) {
                            case TYPE_BOOLEAN:
                                newPrefItem.keyValue = ((Switch) mValues[currentType]).isChecked();
                                break;
                            case TYPE_FLOAT:
                                newPrefItem.keyValue = Float.valueOf(mValues[currentType].getText().toString());
                                break;
                            case TYPE_INTEGER:
                                newPrefItem.keyValue = Integer.valueOf(mValues[currentType].getText().toString());
                                break;
                            case TYPE_LONG:
                                newPrefItem.keyValue = Long.valueOf(mValues[currentType].getText().toString());
                                break;
                            case TYPE_STRING:
                                newPrefItem.keyValue = mValues[currentType].getText().toString();
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.error_evaluating_input, Toast.LENGTH_LONG).show();
                        return;
                    }
                    interfaceCommunicator.sendInfo(mode, newPrefItem);
                })
                .setNegativeButton(android.R.string.cancel,  (dialog, which) -> {
                    if (getDialog() != null) getDialog().cancel();
                });
        if (mode == MODE_EDIT) builder.setNeutralButton(R.string.delete,
                (dialog, which) -> interfaceCommunicator.sendInfo(MODE_DELETE, prefItem));
        return builder.create();
    }
}
