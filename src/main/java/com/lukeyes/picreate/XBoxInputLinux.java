package com.lukeyes.picreate;

import net.java.games.input.AbstractComponent;

public class XBoxInputLinux extends XBoxInput {


    @Override
    protected void setupComponents() {
        AbstractComponent c;
        int i = 0;

        while( i < mInputComponents.length )
        {
            c = (AbstractComponent)mInputComponents[i];

            if( c.isAnalog() )
            {
                if(c.getName().equalsIgnoreCase("rx"))
                {
                    mXRotation = c;
                }
                else if(c.getName().equalsIgnoreCase("ry"))
                {
                    mYRotation = c;
                }
                else if(c.getName().equalsIgnoreCase("y"))
                {
                    mYAxis = c;
                }
            }
            else
            {
                if(c.getName().equalsIgnoreCase("Start"))
                {
                    mStartButton = c;
                }
                else if(c.getName().equalsIgnoreCase("A"))
                {
                    mAButton = c;
                }
                else if( c.getName().equalsIgnoreCase("B"))
                {
                    mBButton = c;
                }
            }

            i++;
        }
    }
}
