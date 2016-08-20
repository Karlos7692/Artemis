package com.elitesportsdevelopers.artemis;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;

import com.elitesportsdevelopers.artemis.model.Session;
import com.elitesportsdevelopers.artemis.utils.Utility;

public class SessionActivity extends AppCompatActivity implements RunSessionFragment.SessionFinishedHandler {

    // Activity Views
    private Button mStartResetButton;
    private Button mPauseStopButton;

    // TODO consider changing the name of this button. mAdditionalSettingsButton?.
    private ImageButton mAdditionalServicesButton;
    private View mAdditionalServicesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        if ( savedInstanceState != null )
        {
            return;
        }

        final Activity currentActivity = this;

        final Fragment controlPanel = new SessionControlPanelFragment();
        getSupportFragmentManager().beginTransaction()
                .add( R.id.session_container, controlPanel, SessionControlPanelFragment.TAG )
                .commit();

        final int disabledGreyColor = Utility.getColor( this, R.color.disabledGrey);
        final int pauseStopColor = Utility.getColor( this, R.color.pauseStopColor );
        final int startResetColor = Utility.getColor(  this, R.color.startResetColor );

        mStartResetButton =  (Button) findViewById(R.id.session_start_button);
        mStartResetButton.setText( "Start" );
        mPauseStopButton = (Button) findViewById( R.id.session_pause_stop_button );
        mPauseStopButton.setText( "Pause" );
        mPauseStopButton.setEnabled( false );

        mStartResetButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final FragmentManager manager = getSupportFragmentManager();

                final Fragment sessionContainer = manager.findFragmentById( R.id.session_container );

                // Base Case: control panel is present we want to start the session.
                if ( sessionContainer instanceof SessionControlPanelFragment )
                {
                    final SessionControlPanelFragment controlPanel =
                            (SessionControlPanelFragment) sessionContainer;

                    loadRunSessionFragment( manager, controlPanel.newSession() );
                    mStartResetButton.setEnabled( false );
                    mStartResetButton.setTextColor( disabledGreyColor );
                    mPauseStopButton.setEnabled( true );
                    mPauseStopButton.setTextColor( pauseStopColor );
                    mPauseStopButton.setText( "Pause" );
                    return;
                }

                // Session Timer is in progress.
                final RunSessionFragment runSessionFragment = (RunSessionFragment)
                        manager.findFragmentByTag( RunSessionFragment.TAG );

                // Reset Timer
                if ( runSessionFragment.getTimerState() == RunSessionFragment.TIMER_STOPPED )
                {
                    runSessionFragment.resetSession();
                    mStartResetButton.setText( "Start" );
                    mStartResetButton.setTextColor( startResetColor );
                    mPauseStopButton.setText( "Pause" );
                    mPauseStopButton.setEnabled( false );
                    mPauseStopButton.setTextColor( disabledGreyColor );
                    return;
                }

                // Start timer from idle state
                if ( runSessionFragment.getTimerState() == RunSessionFragment.TIMER_IDLE )
                {
                    runSessionFragment.startTimer();
                    mPauseStopButton.setEnabled( true );
                    mPauseStopButton.setTextColor( pauseStopColor );
                    mPauseStopButton.setText( "Pause" );
                    mStartResetButton.setEnabled( false );
                    mStartResetButton.setTextColor( disabledGreyColor );
                    return;
                }

                if ( runSessionFragment.getTimerState() == RunSessionFragment.TIMER_PAUSED )
                {
                    runSessionFragment.startTimer();
                    mStartResetButton.setEnabled( false );
                    mStartResetButton.setTextColor( disabledGreyColor );
                    mPauseStopButton.setText( "Pause" );
                }
            }
        });

        mPauseStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final FragmentManager manager = getSupportFragmentManager();
                final RunSessionFragment sessionRunner = (RunSessionFragment) manager
                        .findFragmentByTag( RunSessionFragment.TAG );

                // Case 1: Timer is running
                if ( sessionRunner.getTimerState() == RunSessionFragment.TIMER_RUNNING )
                {
                    sessionRunner.pauseTimer();
                    mStartResetButton.setText( "Start" );
                    mPauseStopButton.setText( "Stop" );
                    mStartResetButton.setEnabled( true );
                    mStartResetButton.setTextColor( startResetColor );
                    return;
                }

                // Case 2: Stop the timer from paused state
                if ( sessionRunner.getTimerState() == RunSessionFragment.TIMER_PAUSED )
                {
                    sessionRunner.stopTimer();
                    mStartResetButton.setText( "Reset" );
                    mPauseStopButton.setEnabled( false );
                    mPauseStopButton.setTextColor( disabledGreyColor );
                }
            }
        });

        mAdditionalServicesLayout = findViewById( R.id.additional_services );
        setupAdditionalServices( this, mAdditionalServicesLayout );

        //TODO Fix animation for button, fade from menu to down
        mAdditionalServicesButton = (ImageButton) findViewById( R.id.session_additional_services_button );
        mAdditionalServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( mAdditionalServicesLayout.getVisibility() == View.GONE )
                {
                    mAdditionalServicesLayout.setVisibility( View.VISIBLE );
                    final Drawable cancelLook = Utility.getDrawable( currentActivity,
                            R.drawable.additional_services_cancel_button );
                    mAdditionalServicesButton.setBackgroundDrawable( cancelLook );
                }
                else
                {
                    mAdditionalServicesLayout.setVisibility( View.GONE );
                    final Drawable menuLook = Utility.getDrawable( currentActivity,
                            R.drawable.additional_services_button );

                    mAdditionalServicesButton.setBackgroundDrawable( menuLook );
                }

            }

        });
    }

    private void loadRunSessionFragment( final FragmentManager manager, final Session session )
    {
        final RunSessionFragment runSessionFragment = RunSessionFragment
                .newInstance( session );

        manager.beginTransaction()
                .addToBackStack( null )
                .replace(R.id.session_container, runSessionFragment, RunSessionFragment.TAG )
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void sessionFinished() {
        mStartResetButton.setText( "Reset" );
        mStartResetButton.setTextColor( Utility.getColor( this, R.color.startResetColor ) );
        mStartResetButton.setEnabled( true );
        mPauseStopButton.setText( "Stop" );
        mPauseStopButton.setEnabled( false );
        mPauseStopButton.setTextColor( Utility.getColor( this, R.color.disabledGrey ) );
    }

    @Override
    public void onBackPressed() {
        final RunSessionFragment runSession = (RunSessionFragment) getSupportFragmentManager()
                .findFragmentByTag(RunSessionFragment.TAG);

        if ( runSession != null )
        {
            // Manually reset everything.
            mStartResetButton.setEnabled( true );
            mStartResetButton.setText( "Start" );
            mStartResetButton.setTextColor( Utility.getColor( this, R.color.startResetColor ) );
            mPauseStopButton.setEnabled( false );
            mPauseStopButton.setText( "Pause" );
            mPauseStopButton.setTextColor( Utility.getColor( this, R.color.disabledGrey ) );
        }
        super.onBackPressed();
    }

    // TODO Change to fragment
    private void setupAdditionalServices( final Context context, final View view )
    {
        if ( view instanceof ViewGroup )
        {
            final ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    mAdditionalServicesLayout.setVisibility( View.GONE );

                    final Drawable menuLook = Utility.getDrawable(
                            context,
                            R.drawable.additional_services_button
                    );

                    mAdditionalServicesButton.setBackgroundDrawable( menuLook );
                    return false;
                }
            });
            for ( int i=0; i < viewGroup.getChildCount(); i++ )
            {
                setupAdditionalServices( context, viewGroup.getChildAt( i ) );
            }
        }
    }
}
