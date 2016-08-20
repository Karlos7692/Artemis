package com.elitesportsdevelopers.artemis;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.elitesportsdevelopers.artemis.model.ModelValue;
import com.elitesportsdevelopers.artemis.model.ModelValueOnEditorActionListener;
import com.elitesportsdevelopers.artemis.model.Session;
import com.elitesportsdevelopers.artemis.utils.Utility;

import java.util.concurrent.TimeUnit;

/**
 * The purpose of this class is to build a session from the control panel inputs.
 * This control panel is used for training.
 */
public class SessionControlPanelFragment extends Fragment {

    static final String TAG = SessionControlPanelFragment.class.getSimpleName();

    private static final int DEFAULT_SHOTS = 6;
    private static final float MAX_DRAW_VAR_SEEK_RATIO = 0.3f;
    private static final long DEFAULT_NOCK_INTERVAL_SECONDS = 5;
    private static final long DEFAULT_DRAW_INTERVAL_SECONDS = 15;
    private static final long DEFAULT_SHOT_INTERVAL_SECONDS = 1;
    private static final long DEFAULT_REST_INTERVAL_SECONDS = 10;
    private static final long DEFAULT_DRAW_VAR_SECONDS = 0;

    private static final String EMPTY = "";

    /** Model for control panel */
    private ModelValue<Integer> mShots;
    private ModelValue<Long> mNockInterval;
    private ModelValue<Long> mDrawInterval;
    private ModelValue<Long> mDrawVar;
    private ModelValue<Long> mShotInterval;
    private ModelValue<Long> mRestInterval;

    /** Views for the control panel **/
    private EditText mShotsEditText;
    private EditText mNockTimeEditText;
    private EditText mDrawTimeEditText;
    private EditText mShotTimeEditText;
    private EditText mRestEditText;
    private TextView mDrawVarTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null )
        {
            return;
        }

        initializeModelFromDefaults();
    }

    private void initializeModelFromDefaults( )
    {
        final Context context = getContext();

        final SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences( getContext() );

        mShots = new ModelValue<>( sharedPreferences.getInt(
                context.getString( R.string.session_default_nshots_key), DEFAULT_SHOTS ) );

        mNockInterval = new ModelValue<>( sharedPreferences.getLong(
                context.getString( R.string.session_default_knock_interval_key ),
                DEFAULT_NOCK_INTERVAL_SECONDS));

        mDrawInterval = new ModelValue<>(sharedPreferences.getLong(
                context.getString( R.string.session_default_draw_interval_key ),
                DEFAULT_DRAW_INTERVAL_SECONDS) );

        mDrawVar = new ModelValue<>( sharedPreferences.getLong(
                context.getString( R.string.session_default_draw_var_key ),
                DEFAULT_DRAW_VAR_SECONDS ) );

        mShotInterval = new ModelValue<>(sharedPreferences.getLong(
                context.getString( R.string.session_default_shot_interval ),
                DEFAULT_SHOT_INTERVAL_SECONDS) );

        mRestInterval = new ModelValue<>(sharedPreferences.getLong(
                context.getString( R.string.session_default_relax_interval_key ),
                DEFAULT_REST_INTERVAL_SECONDS) );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View root = inflater.inflate(R.layout.fragment_control_panel, container, false );

        if ( root != null )
        {
            // Shots
            mShotsEditText = (EditText) root.findViewById( R.id.control_paned_n_shots_edit_text );
            mShotsEditText.setOnEditorActionListener( new IntegerOnActionListener( mShots ) );
            setupTouchListeners( mShotsEditText );

            // Bind the time intervals to the edit texts by reference.
            mNockTimeEditText = (EditText) root.findViewById( R.id.control_panel_nock_time_edit_text );
            mNockTimeEditText.setOnEditorActionListener(new LongOnActionListener( mNockInterval ) );
            setupTouchListeners( mNockTimeEditText );

            mDrawTimeEditText = (EditText) root.findViewById( R.id.control_panel_draw_time_edit_text );
            mDrawTimeEditText.setOnEditorActionListener( new LongOnActionListener( mDrawInterval ) );
            setupTouchListeners( mDrawTimeEditText );

            mShotTimeEditText = (EditText) root.findViewById( R.id.control_panel_shot_time_edit_text );
            mShotTimeEditText.setOnEditorActionListener( new LongOnActionListener( mShotInterval ) );
            setupTouchListeners( mShotTimeEditText );

            mRestEditText = (EditText) root.findViewById( R.id.control_panel_rest_time_edit_text );
            mRestEditText.setOnEditorActionListener( new LongOnActionListener( mRestInterval ) );
            setupTouchListeners( mRestEditText );

            // Draw Variability
            mDrawVarTextView = (TextView) root.findViewById( R.id.control_panel_max_var_text_view );
            final SeekBar mDrawVarSlider = (SeekBar) root
                    .findViewById(R.id.control_panel_draw_var_seek_bar);

            mDrawVarSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser ) {
                    final long drawVar = Utility.calculateDrawVariability( value,
                            MAX_DRAW_VAR_SEEK_RATIO );
                    mDrawVar.setValue( drawVar );
                    mDrawVarTextView.setText( Utility.formatMaxDrawVarInterval( getContext(), drawVar ) );
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Utility.hideSoftKeyboard( getActivity() );
                    updateAllValues();
                    updateAllUIComponents();

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            // Set the values for all UI components
            updateAllUIComponents();

            setupTouchListeners( root );

        }
        return root;
    }

    private Bundle getStateSnapshot()
    {
        final Bundle bundle = new Bundle();
        bundle.putInt( Session.SHOTS_KEY, mShots.getValue() );
        bundle.putLong( Session.NOCK_INTERVAL_KEY, mNockInterval.getValue() );
        bundle.putLong( Session.DRAW_INTERVAL_KEY, mDrawInterval.getValue() );
        bundle.putLong(Session.DRAW_VAR_KEY, mDrawVar.getValue() );
        bundle.putLong( Session.SHOT_INTERVAL_KEY, mShotInterval.getValue() );
        bundle.putLong( Session.REST_INTERVAL_KEY, mRestInterval.getValue() );
        return bundle;
    }

    private void unpackStateSnapshot( final Bundle bundle )
    {
        mShots.setValue( bundle.getInt( Session.SHOTS_KEY ) );
        mNockInterval.setValue( bundle.getLong(Session.NOCK_INTERVAL_KEY) );
        mDrawInterval.setValue( bundle.getLong(Session.DRAW_INTERVAL_KEY) );
        mDrawVar.setValue( bundle.getLong(Session.DRAW_VAR_KEY ) );
        mShotInterval.setValue( bundle.getLong(Session.SHOT_INTERVAL_KEY) );
        mRestInterval.setValue( bundle.getLong(Session.REST_INTERVAL_KEY) );
    }

    // Saving and restoration
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll( getStateSnapshot() );
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if ( savedInstanceState != null ) {
            unpackStateSnapshot(savedInstanceState);
        }
    }

    Session newSession()
    {
        return new Session( mShots.getValue(),
                TimeUnit.SECONDS.toMillis( mNockInterval.getValue() ),
                TimeUnit.SECONDS.toMillis( mDrawInterval.getValue() ),
                TimeUnit.SECONDS.toMillis( mDrawVar.getValue() ),
                TimeUnit.SECONDS.toMillis( mShotInterval.getValue() ),
                TimeUnit.SECONDS.toMillis( mRestInterval.getValue() ) );
    }


    private class LongOnActionListener extends ModelValueOnEditorActionListener<Long>
    {
        private LongOnActionListener(@NonNull ModelValue<Long> value ) {
            super( value );
        }

        @Override
        public Long getDefaultValue() {
            return 0L;
        }

        @Override
        public Long parseValue( final String text ) {
            return Long.parseLong( text );
        }
    }

    private void updateAllValues()
    {
        // Note: Draw variability is always updated at every change on the seek bar.
        // We will not update the value here.

        mShots.setValue( parseIntValue( getText( mShotsEditText ) ) );
        mNockInterval.setValue( parseLongValue( getText( mNockTimeEditText ) ) );
        mDrawInterval.setValue( parseLongValue( getText( mDrawTimeEditText ) ) );
        mShotInterval.setValue( parseLongValue( getText( mShotTimeEditText ) ) );
        mRestInterval.setValue( parseLongValue( getText( mRestEditText ) ) );
    }

    private void updateAllUIComponents()
    {
        // Draw Var is updated on change

        mShotsEditText.setText( mShots == null || mShots.isValueNull()
                ? String.valueOf( DEFAULT_SHOTS ) : mShots.toString() );
        mNockTimeEditText.setText( mNockInterval == null || mNockInterval.isValueNull()
                ? String.valueOf( DEFAULT_NOCK_INTERVAL_SECONDS ) : mNockInterval.toString() );
        mDrawTimeEditText.setText( mDrawInterval == null || mDrawInterval.isValueNull()
                ? String.valueOf( DEFAULT_DRAW_INTERVAL_SECONDS ) : mDrawInterval.toString() );
        mShotTimeEditText.setText( mShotInterval == null || mShotInterval.isValueNull()
                ? String.valueOf( DEFAULT_SHOT_INTERVAL_SECONDS ) : mShotInterval.toString() );
        mRestEditText.setText( mRestInterval == null || mRestInterval.isValueNull()
                ? String.valueOf( DEFAULT_NOCK_INTERVAL_SECONDS ) : mRestInterval.toString() );
    }


    // TODO Move to Utility
    private long parseLongValue( final String text )
    {
        return text.equals( EMPTY ) ? 0 : Long.parseLong( text );
    }

    private int parseIntValue( final String text )
    {
        return text.equals( EMPTY ) ? 0 : Integer.parseInt( text );
    }

    private String getText( final EditText editText )
    {
        return editText.getText().toString();
    }

    private class IntegerOnActionListener extends ModelValueOnEditorActionListener<Integer>
    {
        private IntegerOnActionListener( @NonNull ModelValue<Integer> value ) {
            super( value );
        }

        @Override
        public Integer getDefaultValue() {
            return 0;
        }

        @Override
        public Integer parseValue( final String text ) {
            return Integer.parseInt( text );
        }
    }

    private void setupTouchListeners( final View view )
    {

        if ( !(view instanceof EditText ) )
        {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Utility.hideSoftKeyboard( getActivity() );
                    updateAllValues();
                    updateAllUIComponents();
                    return false;
                }
            });
            return;
        }

        view.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                updateAllValues();
                updateAllUIComponents();
                return false;
            }
        });
    }
}
