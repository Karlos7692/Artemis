package com.elitesportsdevelopers.artemis.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Karl on 15/08/2016.
 */

public class ControlPanelEditText extends EditText {

    private OnEditorActionListener mOnEditorActionListener;

    public ControlPanelEditText(Context context)
    {
        super(context);
    }

    public ControlPanelEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ControlPanelEditText(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi( api = Build.VERSION_CODES.LOLLIPOP )
    public ControlPanelEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setOnEditorActionListener( final OnEditorActionListener l )
    {
        mOnEditorActionListener = l;
        super.setOnEditorActionListener( l );
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        // TODO Check out how dispatch key listener works rather than storing a reference to the listener
        if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK )
        {
            return mOnEditorActionListener.onEditorAction( this, keyCode, event );
        }
        return super.onKeyPreIme(keyCode, event);
    }



}
