package com.esp.library.exceedersesp.controllers.fieldstype.viewholders;

import com.esp.library.R;
import com.esp.library.utilities.customcontrols.BodyText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;


public class CurrencyEditTextTypeViewHolder extends RecyclerView.ViewHolder {

    public TextInputLayout tilFieldLabel;
    public com.esp.library.utilities.customcontrols.BodyEditText etValue, etCurrency;
    public BodyText tValue;
    public BodyText tValueLabel;
    public BodyText tCurrencyLabel;
    public Button btnClickArea;
   /* public BodyText tCurrencyValueLabel;
    public BodyText tCurrencyValue;*/
   // public MaterialSpinner msCurrency;



    public CurrencyEditTextTypeViewHolder(View itemView) {
        super(itemView);


        tilFieldLabel = itemView.findViewById(R.id.tilFieldLabel);
        etValue = itemView.findViewById(R.id.etValue);
        etCurrency = itemView.findViewById(R.id.etCurrency);
        tCurrencyLabel = itemView.findViewById(R.id.tCurrencyLabel);
        btnClickArea = itemView.findViewById(R.id.btnClickArea);
  /*      tCurrencyValueLabel = itemView.findViewById(R.id.tCurrencyValueLabel);
        tCurrencyValue = itemView.findViewById(R.id.tCurrencyValue);*/
        tValue = itemView.findViewById(R.id.tValue);
        tValueLabel = itemView.findViewById(R.id.tValueLabel);
       // msCurrency = itemView.findViewById(R.id.msCurrency);

    }
}
