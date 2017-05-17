package com.tapc.android.helper;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RandomValueGenerator 
{
	Random _generator = new Random();
	
	private int _minvalue;
	private int _maxvalue;
	private int _timeperiod;
	
	private int _nextRandomValue = 0;
	
	private int getRange()
	{
		return this._maxvalue - this._minvalue;
	}
	
	public RandomValueGenerator(int minRandomValue, int maxRandomValue, int periodicInterval)
	{
		this._minvalue = Math.max(minRandomValue, 0);
		this._maxvalue = Math.max(maxRandomValue, this._minvalue);
		this._timeperiod = periodicInterval;
		
		new Timer().schedule(new TimerTask(){

			@Override
			public void run() {
				computeNextRandomNUmber();		
			}}, this._timeperiod, this._timeperiod);
	}
	
	private void computeNextRandomNUmber()
	{
		int val = _generator.nextInt();
		
		if(val < 0)
			val = val * -1;		
		
		_nextRandomValue = val % getRange() + this._minvalue;
	}
	
	public int nextValue()
	{
		return _nextRandomValue;
	}
}
