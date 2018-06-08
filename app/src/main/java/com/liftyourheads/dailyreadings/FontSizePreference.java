package com.liftyourheads.dailyreadings;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import java.util.prefs.Preferences;

public class FontSizePreference extends SpinnerPreference {
    private final LayoutInflater mLayoutInflater;

    public FontSizePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontSizePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLayoutInflater = LayoutInflater.from(getContext());
    }

    @Override
    protected View createDropDownView(int position, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
    }

    @Override
    protected void bindDropDownView(int position, View view) {
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        float textSize = Utils.getDimension(getContext(),
                Utils.Theme.resolveTextSize(mEntryValues[position]),
                R.attr.contentTextSize);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setText(mEntries[position]);
    }
}