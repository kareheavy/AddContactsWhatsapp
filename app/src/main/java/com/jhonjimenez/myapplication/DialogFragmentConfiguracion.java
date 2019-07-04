package com.jhonjimenez.myapplication;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.jhonjimenez.myapplication.Utils.CONSECUTIVO;
import static com.jhonjimenez.myapplication.Utils.NOMBRE_BASE;
import static com.jhonjimenez.myapplication.Utils.PREFERENCES;

public class DialogFragmentConfiguracion extends DialogFragment {

    private TextView textViewError;
    private EditText editTextConsecutivo;
    private EditText editTextNombre;
    private Button buttonCancelar, buttonAceptar;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_configuracion, null);

        builder.setView(view);

        getComponetsUI(view);

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String consecutivo = prefs.getString(CONSECUTIVO, "0");
        String nombreBase = prefs.getString(NOMBRE_BASE, "Cliente");

        editTextConsecutivo.setText(consecutivo);
        editTextNombre.setText(nombreBase);

        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int error = 0;

                if (editTextConsecutivo.getText().toString().equals("")) {
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("Todos los campos son obligatorios");
                    error = 1;
                }

                if (editTextNombre.getText().toString().equals("")) {
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("Todos los campos son obligatorios");
                    error = 1;
                }

                if (error == 0) {
                    SharedPreferences prefs = getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(CONSECUTIVO, editTextConsecutivo.getText().toString());
                    editor.putString(NOMBRE_BASE, editTextNombre.getText().toString());
                    editor.apply();
                    dismiss();
                }

            }
        });

        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Dialog d = builder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return d;
    }

    private void getComponetsUI(View view) {
        textViewError = view.findViewById(R.id.dialogCantidadReferencia_textViewError);
        editTextConsecutivo = view.findViewById(R.id.dialogCantidadReferencia_editTextConsecutivo);
        editTextNombre = view.findViewById(R.id.dialogCantidadReferencia_editTextNombre);
        buttonAceptar = view.findViewById(R.id.dialogCantidadReferencia_buttonAceptar);
        buttonCancelar = view.findViewById(R.id.dialogCantidadReferencia_buttonCancelar);

        editTextNombre.setFilters(new InputFilter[]{Utils.EditTextFilters.filterAlphaNumeric25});
    }
}
