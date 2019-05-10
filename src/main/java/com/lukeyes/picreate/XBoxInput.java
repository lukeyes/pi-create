package com.lukeyes.picreate;

import net.java.games.input.AbstractComponent;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;


public abstract class XBoxInput {

	static XBoxInput create() {

		String osName = System.getProperty("os.name");
		if("Linux".equals(osName)) {
			return new XBoxInputLinux();
		}

		return new XBoxInputWindows();
	}
	
	private void makeController(Controller c) 
	{
		mController = c;
		Controller[] subControllers = c.getControllers();
		if (subControllers.length != 0 ) 
			return;
		
		{
			mInputComponents = c.getComponents();
			
			System.out.println("Component count = "+mInputComponents.length);
		}
	}
	
	public boolean setup()
	{
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] ca = ce.getControllers();
		for(int i =0; i<ca.length; i++)
		{
			if(ca[i].getType() != Controller.Type.GAMEPAD )
			{
				continue;
			}				
				
			makeController(ca[i]);
			break;
		}
		
		if( mInputComponents == null )
			return false;
		
		AbstractComponent c = null;

		setupComponents();

		return true;
	}

	protected abstract void setupComponents();

	public boolean left()
	{
		if( mXRotation == null )
			return false;		

		float data = mXRotation.getPollData();
		
		if( mXRotation.getDeadZone() > Math.abs(data))
			return false;			
		
		return (data < -0.5);
	}
	
	public boolean right()
	{
		if( mXRotation == null )
			return false;		

		float data = mXRotation.getPollData();
		
		if( mXRotation.getDeadZone() > Math.abs(data))
			return false;		
			
		return (data > 0.5);
	}
	
	public boolean forward()
	{
		if( mYAxis == null )
			return false;		

		float data = mYAxis.getPollData();
		
		if( mYAxis.getDeadZone() > Math.abs(data))
			return false;		
		
		// dead zone
		return (data < -0.5);		
	}
	
	public float leftStick()
	{
		if( mYAxis == null )
			return 0;		

		float data = mYAxis.getPollData();
		
		if( mYAxis.getDeadZone() > Math.abs(data))
			return 0;
		
		return data;			
	}
	
	public float rightStick()
	{
		if(mYRotation == null)
		{
			return  0;
		}
		
		float data = mYRotation.getPollData();
		
		if( mYRotation.getDeadZone() > Math.abs(data))
			return 0;
		
		return data;	
	}
	
	public boolean backward()
	{
		if( mYAxis == null )
			return false;		

		float data = mYAxis.getPollData();
		
		if( mYAxis.getDeadZone() > Math.abs(data))
			return false;					
			
		// dead zone
		return (data > 0.5);		
	}
	
	boolean isPressed(AbstractComponent aC )
	{
		if( aC.isAnalog() )
			return false;
		
		float data = aC.getPollData();
		
		// dead zone
		return (data == 1.0);		
	}
	
	public boolean isStartPressed()
	{
		return isPressed(mStartButton);		
	}
	
	public boolean isAPressed()
	{
		return isPressed(mAButton);
	}
	
	public boolean isBPressed()
	{
		return isPressed(mBButton);
	}
	
	public boolean poll()
	{
		return mController.poll();
	}
	
	AbstractComponent mYAxis;
	AbstractComponent mXRotation;
	AbstractComponent mYRotation;
	AbstractComponent mStartButton;
	AbstractComponent mAButton;
	AbstractComponent mBButton;
	
	Component[] mInputComponents;
	Controller mController;

}
