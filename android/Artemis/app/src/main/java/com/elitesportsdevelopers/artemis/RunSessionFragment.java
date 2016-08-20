package com.elitesportsdevelopers.artemis;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elitesportsdevelopers.artemis.model.Session;
import com.elitesportsdevelopers.artemis.model.SessionTimerHandler;
import com.elitesportsdevelopers.artemis.utils.Utility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Karl on 10/08/2016.
 */
// TODO Change Name
public class RunSessionFragment extends Fragment {

    private static final String SESSION_KEY = "session";
    public static final String TIMER_STATE_KEY = "timer state";

    public interface SessionFinishedHandler
    {
        public void sessionFinished();
    }

    public static final String TAG = RunSessionFragment.class.getSimpleName();

    @Retention( RetentionPolicy.SOURCE )
    @IntDef( { TIMER_IDLE, TIMER_RUNNING, TIMER_PAUSED, TIMER_STOPPED} )
    @interface TIMER_STATE {}
    public static final int TIMER_IDLE = 0;
    public static final int TIMER_RUNNING = 1;
    public static final int TIMER_PAUSED = 2;
    public static final int TIMER_STOPPED = 3;

    private static final String SESSION_ARG = "session argument";

    // TODO when we create the audio files.


    private SessionFinishedHandler mSessionFinishedHandler;
    private Session mSession;
    private SessionTimerHandler mTimerHandler;
    private @TIMER_STATE int mCurrentState = TIMER_IDLE;


    // Views
    private TextView mShotTextView;
    private TextView mStateTextView;
    private TextView mTimeTextView;

    public static RunSessionFragment newInstance( final Session session )
    {
        final Bundle args = new Bundle();
        args.putParcelable( SESSION_ARG, session );
        final RunSessionFragment runSessionfragment = new RunSessionFragment();
        runSessionfragment.setArguments(args);
        return runSessionfragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try
        {
            mSessionFinishedHandler = (SessionFinishedHandler) context;
        } catch ( ClassCastException e )
        {
            throw new IllegalStateException( e );
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null )
        {
            return;
        }

        final Bundle fragmentArgs = getArguments();
        if ( fragmentArgs != null )
        {
            mSession = (Session) fragmentArgs.get( SESSION_ARG );
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View root = inflater.inflate(R.layout.fragment_run_session, container, false);

        if ( root != null ) {
            mShotTextView = (TextView) root
                    .findViewById( R.id.session_run_shot_counter_text_view );
            mShotTextView.setText( "Shot: " + (mSession.getCurrentShot()+1) + " of " + mSession.getTotalShots() + " shots.");

            mStateTextView = (TextView) root
                    .findViewById( R.id.session_run_state_text_view );

            mTimeTextView = (TextView) root
                    .findViewById( R.id.session_run_time_text_view );

            mTimerHandler = new SessionHandler( mSession );
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable( SESSION_KEY, mSession );
        outState.putInt( TIMER_STATE_KEY, mCurrentState );
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if ( savedInstanceState != null )
        {
            mSession =  savedInstanceState.getParcelable( SESSION_KEY );
            mTimerHandler = new SessionHandler( mSession );
            mCurrentState = intToTimerState( savedInstanceState.getInt( TIMER_STATE_KEY ) );
            if ( mCurrentState == TIMER_RUNNING )
            {
                startTimer();
            }
            return;
        }

        // No saved state was restored, the fragment was loaded from the control panel.
        startTimer();
    }

    private @TIMER_STATE int intToTimerState( int state )
    {
        if ( state < TIMER_IDLE || state > TIMER_STOPPED )
        {
            throw new IllegalStateException( "Invalid state: " + state );
        }
        return state;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( mTimerHandler != null )
        {
            mTimerHandler.onStop();
            mTimerHandler = null;
        }
    }

    public @TIMER_STATE int getTimerState()
    {
        return mCurrentState;
    }

    /** Controls for the timer */
    public void startTimer() {
        if ( mCurrentState != TIMER_IDLE && mCurrentState != TIMER_PAUSED )
        {
            throw new IllegalStateException( "Oops: We cannot start the session if its not idle, or paused.");
        }
        mTimerHandler.onStart();
        mCurrentState = TIMER_RUNNING;
    }

    public void resetSession()
    {
        if ( mCurrentState != TIMER_STOPPED )
        {
            throw new IllegalStateException( "Oops: We cannot reset the session if we are not stopped!" );
        }
        mCurrentState = TIMER_IDLE;
        mSession = new Session( mSession.getTotalShots(), mSession.getNnockInterval(),
                mSession.getDrawInterval(), mSession.getDrawVariability(),
                mSession.getShootInterval(), mSession.getRestInterval() );
        mTimerHandler = new SessionHandler( mSession );
        mShotTextView.setText( "Shot: " + (mSession.getCurrentShot()+1) + " of " + mSession.getTotalShots() + " shots.");
        mStateTextView.setText( Utility.getStateFriendlyName( getContext(), Session.NOCKING_STATE) );
        mTimeTextView.setText( Utility.formatTime( mSession.getNnockInterval() ) );
    }

    public void pauseTimer()
    {
        if ( mCurrentState != TIMER_RUNNING )
        {
            throw new IllegalStateException( "Oops: We cannot pause the session if its not running!" );
        }
        mCurrentState = TIMER_PAUSED;
        mTimerHandler.onPause();
    }

    public void stopTimer()
    {
        if ( mCurrentState != TIMER_PAUSED )
        {
            throw new IllegalStateException( "Oops: We cannot stop the session if we are not paused." );
        }

        mCurrentState = TIMER_STOPPED;
        mTimerHandler.onStop();
    }


    private class SessionHandler extends SessionTimerHandler
    {

        private MediaPlayer mAudioPlayer;

        SessionHandler( Session session ) {
            super( session );
        }

        @Override
        public void beforeStateStart( final @Session.SHOT_STATE int state ) {
            final Context context = getContext();
            final int audioId = Utility.getStateAudioId( context, state );
            mAudioPlayer = MediaPlayer.create( context, audioId );
            mAudioPlayer.start();
        }

        @Override
        public void takeNextShot( int shotNumber ) {
            mShotTextView.setText( "Shot: " + (shotNumber+1) + " of " + mSession.getTotalShots() + " shots." );
        }

        @Override
        public void updateViews( @Session.SHOT_STATE int state, long millisToGo,
                                 final long stateDuration ) {

            mStateTextView.setText(
                    Utility.getStateFriendlyName( getContext(), getSession().getState() ) );

            mTimeTextView.setText( Utility.formatTime( millisToGo ) );
        }

        @Override
        public void onStateFinished() {
            if ( mAudioPlayer == null )
            {
                return;
            }

            if ( mAudioPlayer.isPlaying() ) {
                mAudioPlayer.stop();

            }

            mAudioPlayer.reset();
            mAudioPlayer.release();
        }

        @Override
        public void onFinished() {
            mStateTextView.setText( "Finished!" );
            mCurrentState = TIMER_STOPPED;
            mTimeTextView.setText( Utility.formatTime( 0 ) );
            mSessionFinishedHandler.sessionFinished();
        }
    }
}
