package com.elitesportsdevelopers.artemis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Karl on 17/08/2016.
 */

public class MainActivity extends AppCompatActivity {

    private TextView mTrainingClickable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );
        if ( savedInstanceState != null )
        {
            return;
        }

        final Context context = this;
        mTrainingClickable = (TextView) findViewById( R.id.training_clickable_text_view );
        mTrainingClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent trainingIntent = new Intent( context, SessionActivity.class );
                context.startActivity( trainingIntent );
            }
        });
    }
}
