package com.lukeyes.picreate;

import net.java.games.input.AbstractComponent;

public class XBoxInputWindows extends XBoxInput {

    @Override
    protected void setupComponents() {
        AbstractComponent c;
        int i = 0;

        while( i < mInputComponents.length )
        {
            c = (AbstractComponent)mInputComponents[i];

            if( c.isAnalog() )
            {
                if(c.getName().equalsIgnoreCase("X Rotation"))
                {
                    mXRotation = c;
                }
                else if(c.getName().equalsIgnoreCase("Y Rotation"))
                {
                    mYRotation = c;
                }
                else if(c.getName().equalsIgnoreCase("Y Axis"))
                {
                    mYAxis = c;
                }
            }
            else
            {
                if(c.getName().equalsIgnoreCase("Button 7"))
                {
                    mStartButton = c;
                }
                else if(c.getName().equalsIgnoreCase("Button 1"))
                {
                    mAButton = c;
                }
                else if( c.getName().equalsIgnoreCase("Button 2"))
                {
                    mBButton = c;
                }
            }

            i++;
        }
    }
}
