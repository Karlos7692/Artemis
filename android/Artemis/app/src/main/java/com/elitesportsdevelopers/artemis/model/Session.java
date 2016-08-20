package com.elitesportsdevelopers.artemis.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import com.elitesportsdevelopers.artemis.utils.Utility;

import java.lang.annotation.RetentionPolicy;

/**
 * Created by Karl on 4/08/2016.
 */

public class Session implements Parcelable {

    public static final String SHOTS_KEY = "shots";
    public static final String NOCK_INTERVAL_KEY = "nock";
    public static final String DRAW_INTERVAL_KEY = "draw";
    public static final String SHOT_INTERVAL_KEY = "shot";
    public static final String REST_INTERVAL_KEY = "rest";
    public static final String DRAW_VAR_KEY = "draw";

    @Retention( RetentionPolicy.SOURCE )
    @IntDef( {REST_STATE, NOCKING_STATE, DRAW_STATE, SHOOT_STATE } )
    public @interface SHOT_STATE {}
    public static final int REST_STATE = 0;
    public static final int NOCKING_STATE = 1;
    public static final int DRAW_STATE = 2;
    public static final int SHOOT_STATE = 3;
    private static final int N_STATES = 4;


    /** The index of the current shot. */
    private int mCurrentShot = 0;

    @SHOT_STATE
    private int mCurrentState = REST_STATE;

    /** Number of shots in this session. **/
    private final int mTotalShots;

    /** The time taken for knocking */
    private final long mKnockInterval;

    /** The time taken between knocking and releasing */
    private final long mDrawInterval;

    /** The time taken to take the shot **/
    private final long mShotInterval;

    /** The time taken between shooting and knocking. */
    private final long mRestInterval;

    /** Additional variability for draw **/
    private final long mDrawVariability;

    public static Parcelable.Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel parcel) {
            return new Session( parcel );
        }

        @Override
        public Session[] newArray( int size ) {
            return new Session[size];
        }
    };

    public Session( final int nShots, final long knockInterval, final long drawInterval,
                     final long drawVariability, final long shotInterval, final long restInterval )
    {
        mTotalShots = nShots;
        mKnockInterval = knockInterval;
        mDrawInterval = drawInterval;
        mDrawVariability = drawVariability;
        mShotInterval = shotInterval;
        mRestInterval = restInterval;;
    }

    // Parcelable constructor.
    private Session( final Parcel in )
    {
        mTotalShots = in.readInt();
        mKnockInterval = in.readLong();
        mDrawInterval = in.readLong();
        mDrawVariability = in.readLong();
        mShotInterval= in.readLong();
        mRestInterval = in.readLong();

        // State variables
        mCurrentShot = in.readInt();
        mCurrentState = intToState( in.readInt() );
    }

    // TODO better way to manage is finished, maybe another state indicating that the session is finished.
    public boolean isFinished()
    {
        return mCurrentShot == mTotalShots && mCurrentState == SHOOT_STATE;
    }

    public Session next()
    {
        if ( ( mCurrentState = nextShotState( mCurrentState ) ) == SHOOT_STATE ) { shoot(); }
        return this;
    }

    public long getStateDuration() {
        switch ( mCurrentState ) {
            case NOCKING_STATE:
                return getNnockInterval();
            case DRAW_STATE:
                return getDrawInterval() + Utility.getRandomSeconds( getDrawVariability() );
            case REST_STATE:
                return getRestInterval();
            case SHOOT_STATE:
                return getShootInterval();
            default:
                throw new IllegalStateException( "Oops, we should only be in the following states "
                    + "RELAX_STATE(0) to SHOT_STATE(3) but found state " + mCurrentState );
        }
    }

    private void shoot()
    {
        if ( mCurrentShot == mTotalShots)
        {
            throw new IllegalStateException( "Oops: We cannot shoot more shots than the number of" +
                    "shots in this session." );
        }
        ++mCurrentShot;
    }

    public @SHOT_STATE int getState()
    {
        return mCurrentState;
    }

    public int getCurrentShot()
    {
        return mCurrentShot;
    }

    public int getTotalShots()
    {
        return mTotalShots;
    }

    public long getNnockInterval()
    {
        return mKnockInterval;
    }

    public long getDrawInterval()
    {
        return mDrawInterval;
    }

    public long getRestInterval()
    {
        return mRestInterval;
    }

    public long getDrawVariability()
    {
        return mDrawVariability;
    }

    public long getShootInterval()
    {
        return mShotInterval;
    }

    /**
     * @param currentShotState the current shot state of the session.
     * @return the next state.
     */
    public static @SHOT_STATE int nextShotState( @SHOT_STATE int currentShotState )
    {
        return intToState( stateToInt( currentShotState ) + 1 % N_STATES );
    }

    public static @Session.SHOT_STATE int intToState( final int state )
    {
        return  REST_STATE <= state && state <= SHOOT_STATE ? state : REST_STATE;
    }

    public static int stateToInt( @Session.SHOT_STATE int state )
    {
        return state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int mode ) {
        parcel.writeInt(mTotalShots);
        parcel.writeLong( mKnockInterval );
        parcel.writeLong( mDrawInterval );
        parcel.writeLong( mDrawVariability );
        parcel.writeLong( mShotInterval );
        parcel.writeLong( mRestInterval );

        // State variables
        parcel.writeInt( mCurrentShot );
        parcel.writeInt( mCurrentState );
    }

}
