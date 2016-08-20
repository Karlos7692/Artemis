package com.elitesportsdevelopers.artemis.model;

import android.support.annotation.NonNull;

/**
 * A wrapper class for primitives so that they can be binded to listeners.
 */

public class ModelValue<Type> {

    private Type mType;
    public ModelValue( Type type )
    {
        mType = type;
    }

    public Type getValue()
    {
        return mType;
    }

    public void setValue( Type type )
    {
        mType = type;
    }

    public boolean isValueNull()
    {
        return mType == null;
    }

    @Override
    public String toString() {
        return mType.toString();
    }
}
