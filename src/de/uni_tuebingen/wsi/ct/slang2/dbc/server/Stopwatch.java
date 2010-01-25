package de.uni_tuebingen.wsi.ct.slang2.dbc.server;

public class Stopwatch
{
	private long startTime = 0;
	private long stopTime = 0;
	private boolean running = false;

	public void resetWatch( )
	{
		this.stopTime = 0;
		this.startTime = 0;
		this.running = false;
	}

	public long getElapsedTimeSecs( )
	{
		long elapsed;
		if (this.running)
		{
			elapsed = ((System.currentTimeMillis() - this.startTime) / 1000);
		}
		else
		{
			elapsed = ((this.stopTime - this.startTime) / 1000);
		}
		return elapsed;
	}

	public long getElapsedTimeMinutes( )
	{
		long elapsed;
		if (this.running)
		{
			elapsed = ( ( (System.currentTimeMillis() - this.startTime) / 1000 ) / 60 );
		}
		else
		{
			elapsed = ( ( (this.stopTime - this.startTime) / 1000 ) / 60 );
		}
		return elapsed;
	}
	
	public void start( )
	{
		this.startTime = System.currentTimeMillis();
		this.running = true;
	}

	public void stop( )
	{
		this.stopTime = System.currentTimeMillis();
		this.running = false;
	}

	public long getElapsedTimeMilliSecs( )
	{
		long elapsed;
		if (this.running)
		{
			elapsed = ((System.currentTimeMillis() - this.startTime) );
		}
		else
		{
			elapsed = ((this.stopTime - this.startTime) );
		}
		return elapsed;
	}
}
