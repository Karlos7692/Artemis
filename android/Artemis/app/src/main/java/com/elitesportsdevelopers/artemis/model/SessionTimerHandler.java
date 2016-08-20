package com.elitesportsdevelopers.artemis.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.CountDownTimer;

/**
 * Created by Karl on 10/08/2016.
 */

public abstract class SessionTimerHandler {

    private final Session mSession;

    private SessionStateCountDownTimer mTimer;

    public SessionTimerHandler( final Session session ) {
        mSession = session;
        mTimer = mSession.getCurrentShot() < mSession.getTotalShots()
                ? new SessionStateCountDownTimer( 0, this )
                : new SessionStateCountDownTimer( mSession.next().getStateDuration(), this );
    }

    public void beforeStateStart( final @Session.SHOT_STATE int state )
    {

    }

    public void onStart() {
        if ( mTimer.getMillisToGo() == mSession.getStateDuration() )
        {
            beforeStateStart( mSession.getState() );
        }
        mTimer.start();
    }

    public void onPause() {
        if ( mTimer != null )
        {
            mTimer.cancel();
            mTimer = new SessionStateCountDownTimer( mTimer.getMillisToGo(), this );
        }
    }

    public void onStop() {
        if ( mTimer != null )
        {
            mTimer.cancel();
        }
    }

    public Session getSession() {
        return mSession;
    }

    public abstract void takeNextShot( final int shotNumber );

    public abstract void updateViews( final @Session.SHOT_STATE int state, final long millisToGo,
                                      final long stateDuration );

    public void onStateFinished()
    {

    }

    public abstract void onFinished();

    private void notifyOnStateFinished()
    {
        // Allow children to clean up their resources.
        onStateFinished();

        if ( !mSession.isFinished() )
        {
            mTimer = new SessionStateCountDownTimer( mSession.next().getStateDuration(), this );
            if ( mSession.getState() == Session.NOCKING_STATE )
            {
                takeNextShot( mSession.getCurrentShot() );
            }
            onStart();
            return;
        }

        onFinished();
    }

    private static class SessionStateCountDownTimer extends CountDownTimer
    {
        private static final long MILLISECOND_COUNTDOWN = 1;
        private long mMillisToGo;

        private final SessionTimerHandler mHandler;

        private SessionStateCountDownTimer( final long duration, final SessionTimerHandler handler ) {
            super( duration, MILLISECOND_COUNTDOWN );
            mMillisToGo = duration;
            mHandler = handler;
        }

        @Override
        public void onTick( long millisLeft ) {
            mMillisToGo = millisLeft;
            final Session currentSession = mHandler.getSession();
            mHandler.updateViews( currentSession.getState(), millisLeft, currentSession
                    .getStateDuration() );
        }

        @Override
        public void onFinish() {
            mHandler.notifyOnStateFinished();
        }

        private long getMillisToGo()
        {
            return mMillisToGo;
        }
    }
}
