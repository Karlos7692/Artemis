package com.elitesportsdevelopers.artemis.model;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Karl on 10/08/2016.
 */

public abstract class ModelValueOnEditorActionListener<Type> implements TextView.OnEditorActionListener {

    private ModelValue<Type> mType;

    public ModelValueOnEditorActionListener( @NonNull final ModelValue<Type> type )
    {
        mType = type;
    }

    public abstract Type getDefaultValue();

    public abstract Type parseValue( final String text );

    @Override
    public boolean onEditorAction( TextView textView, int actionId, KeyEvent keyEvent ) {
        final boolean editableIsDone = isEditTextDone( actionId, keyEvent );

        if (  editableIsDone && textView.getText().toString().equals( "" ) )
        {
            final Type defaultValue = getDefaultValue();
            mType.setValue( defaultValue );
            textView.setText( defaultValue.toString() );
            return false;
        }

        if ( editableIsDone )
        {
            mType.setValue( parseValue( textView.getText().toString() ) );
            return false;
        }

        return false;
    }

    private boolean isEditTextDone( final int actionId, final KeyEvent event )
    {
        return actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_NEXT
                || actionId == EditorInfo.IME_ACTION_PREVIOUS
                || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK;
    }
}
