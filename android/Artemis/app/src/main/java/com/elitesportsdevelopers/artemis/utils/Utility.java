package com.elitesportsdevelopers.artemis.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.elitesportsdevelopers.artemis.R;
import com.elitesportsdevelopers.artemis.SessionActivity;
import com.elitesportsdevelopers.artemis.model.Session;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Karl on 4/08/2016.
 */

public class Utility {

    private static final long MINIMUM_NOCK_INTERVAL = 3000L;
    private static final long MINIMUM_DRAW_INTERVAL = 3000L;
    private static final long MINIMUM_SHOT_INTERVAL = 3000L;
    private static final long MINIMUM_REST_INTERVAL = 3000L;

    public static long getRandomSeconds( final long secondsInterval )
    {
        return Math.round( Math.random() * secondsInterval );
    }

    public static String formatMaxDrawVarInterval( final Context context,
                                                  final long maxDrawVarSeconds )
    {
        if ( maxDrawVarSeconds != 0 )
        {
            return String.format(
                    context.getString( R.string.draw_var_interval_non_zero_var_format ),
                    maxDrawVarSeconds
            );
        }
        return context.getString( R.string.draw_var_interval_zero_var );
    }

    public static long calculateDrawVariability( final int seekValue, final float seekRatio )
    {
        return Math.round( seekValue * seekRatio );
    }

    public static String getStateFriendlyName( final Context context, final @Session.SHOT_STATE int state )
    {
        switch ( state )
        {
            case Session.NOCKING_STATE:
                return context.getString( R.string.nocking_state_friendly_name );
            case Session.DRAW_STATE:
                return context.getString( R.string.draw_state_friendly_name );
            case Session.SHOOT_STATE:
                return context.getString( R.string.shoot_state_friendly_name );
            case Session.REST_STATE:
                return context.getString( R.string.rest_state_friendly_name );
            default:
                throw new IllegalStateException( "Oops: " + state + " is not a supported state." );
        }
    }

    public static long getStateMinDuration( final @Session.SHOT_STATE int state )
    {
        switch ( state )
        {
            case Session.NOCKING_STATE:
                return MINIMUM_NOCK_INTERVAL;
            case Session.DRAW_STATE:
                return MINIMUM_DRAW_INTERVAL;
            case Session.SHOOT_STATE:
                return MINIMUM_SHOT_INTERVAL;
            case Session.REST_STATE:
                return MINIMUM_REST_INTERVAL;
            default:
                throw new IllegalStateException( "Oops: " + state + " is not a supported state." );
        }
    }

    public static int getStateAudioId( final Context context, final @Session.SHOT_STATE int state )
    {
        switch ( state )
        {
            case Session.NOCKING_STATE:
                return R.raw.nock;
            case Session.DRAW_STATE:
                return R.raw.nock;
            case Session.SHOOT_STATE:
                return R.raw.nock;
            case Session.REST_STATE:
                return R.raw.nock;
            default:
                throw new IllegalStateException( "Oops: " + state + " is not a supported state." );
        }
    }
    // TODO Basic formatting to get the app going.
    public static String formatTime( final long millis )
    {
        return String.format(Locale.UK, "%d.%d", TimeUnit.MILLISECONDS.toSeconds( millis ),
                millis % 1000 );
    }

    // TODO Replace the above funtion with the completed version of this function.
    public static String formatTimerText( final Context context, final long millis )
    {
        return String.format(
                context.getString( R.string.timer_text_format ),
                millis
        );
    }

    public static int getColor( final Context context, final int id )
    {
        if ( Build.VERSION.SDK_INT >= 23 )
        {
            return ContextCompat.getColor( context, id );
        }

        return context.getResources().getColor( id );
    }

    public static Drawable getDrawable( final Context context, final int id )
    {
        final int version = Build.VERSION.SDK_INT;
        if ( Build.VERSION.SDK_INT >= 22 )
        {
            return context.getDrawable( id );
        }
        return context.getResources().getDrawable( id );
    }

    public static void hideSoftKeyboard( final Activity activity )
    {
        final InputMethodManager inputService = (InputMethodManager) activity
                .getSystemService(SessionActivity.INPUT_METHOD_SERVICE);
        final View currentFocus = activity.getCurrentFocus();
        if ( currentFocus != null )
        {
            inputService.hideSoftInputFromWindow( currentFocus.getWindowToken(), 0 );
        }
    }


}
