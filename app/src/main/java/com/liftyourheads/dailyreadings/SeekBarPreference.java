package com.liftyourheads.dailyreadings;

import android.content.Context;
import android.preference.Preference;
//import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;


public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    public static int MAX_VALUE = 100;
    public static int INTERVAL  = 1;

    private int oldValue;

    public SeekBarPreference(Context context) {
        super(context);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);

        setWidgetLayoutResource(R.layout.preference_spinner);

        SeekBar seekbar = new SeekBar(getContext());
        seekbar.setMax(MAX_VALUE);
        seekbar.setProgress(this.oldValue);
        seekbar.setOnSeekBarChangeListener(this);

        return null;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
