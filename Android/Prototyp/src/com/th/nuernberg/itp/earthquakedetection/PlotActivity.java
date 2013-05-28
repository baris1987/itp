package com.th.nuernberg.itp.earthquakedetection;
/*
 * Copyright 2012 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
 
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.*;
 
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
 
// Monitor the phone's orientation sensor and plot the resulting azimuth pitch and roll values.
// See: http://developer.android.com/reference/android/hardware/SensorEvent.html
public class PlotActivity extends Fragment implements SensorEventListener
{
 
    /**
     * A simple formatter to convert bar indexes into sensor names.
     */
    private class APRIndexFormat extends Format {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            Number num = (Number) obj;
 
            // using num.intValue() will floor the value, so we add 0.5 to round instead:
            int roundNum = (int) (num.floatValue() + 0.5f);
            switch(roundNum) {
                case 0:
                    toAppendTo.append("Azimuth");
                    break;
                case 1:
                    toAppendTo.append("Pitch");
                    break;
                case 2:
                    toAppendTo.append("Roll");
                    break;
                default:
                    toAppendTo.append("Unknown");
            }
            return toAppendTo;
        }
 
        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;  // We don't use this so just return null for now.
        }
    }
 
    private static final int HISTORY_SIZE = 30;            // number of points to plot in history
    private SensorManager sensorMgr = null;
    private Sensor orSensor = null;
 
    private XYPlot aprHistoryPlot = null;
    
    private SimpleXYSeries aprLevelsSeries = null;
    private SimpleXYSeries azimuthHistorySeries = null;
    private SimpleXYSeries pitchHistorySeries = null;
    private SimpleXYSeries rollHistorySeries = null;
    
    private static PlotActivity plotActivity;
    private static View rootView;
 
    public PlotActivity()
    {
    	setRetainInstance(true);
    }
    
    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	if(rootView != null)
		{
			((FrameLayout) rootView.getParent()).removeView(rootView);
			return rootView;
		}
    	
    	View rootView = inflater.inflate(R.layout.activity_plot, container, false);
    	
        // setup the APR Levels plot:
       
        aprLevelsSeries = new SimpleXYSeries("APR Levels");
        aprLevelsSeries.useImplicitXVals();
 
        // setup the APR History plot:
        aprHistoryPlot = (XYPlot) rootView.findViewById(R.id.aprHistoryPlot);
 
        azimuthHistorySeries = new SimpleXYSeries("Azimuth");
        azimuthHistorySeries.useImplicitXVals();
        pitchHistorySeries = new SimpleXYSeries("Pitch");
        pitchHistorySeries.useImplicitXVals();
        rollHistorySeries = new SimpleXYSeries("Roll");
        rollHistorySeries.useImplicitXVals();
 
        aprHistoryPlot.setRangeBoundaries(-50, 50, BoundaryMode.FIXED);
        aprHistoryPlot.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
        aprHistoryPlot.addSeries(azimuthHistorySeries, new LineAndPointFormatter(Color.rgb(100, 100, 200), Color.BLACK, null));
        aprHistoryPlot.addSeries(pitchHistorySeries, new LineAndPointFormatter(Color.rgb(100, 200, 100), Color.BLACK, null));
        aprHistoryPlot.addSeries(rollHistorySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null));
        aprHistoryPlot.setDomainStepValue(5);
        aprHistoryPlot.setTicksPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("Sample Index");
        aprHistoryPlot.getDomainLabelWidget().pack();
        aprHistoryPlot.setRangeLabel("Angle (Degs)");
        aprHistoryPlot.getRangeLabelWidget().pack();
 
        // setup checkboxes:
        final PlotStatistics levelStats = new PlotStatistics(1000, false);
        final PlotStatistics histStats = new PlotStatistics(1000, false);
 
        // register for orientation sensor events:
        sensorMgr = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        orSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 
        // if we can't access the orientation sensor then exit:
        if (orSensor == null) {
            System.out.println("Failed to attach to orSensor.");
            cleanup();
        }
 
        sensorMgr.registerListener(this, orSensor, SensorManager.SENSOR_DELAY_UI);
        
        PlotActivity.rootView = rootView;
        return rootView;
    }
 
    private void cleanup() {
        // aunregister with the orientation sensor before exiting:
        sensorMgr.unregisterListener(this);
        //finish();
    }
 
    // Called whenever a new orSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
 
        // update instantaneous data:
        Number[] series1Numbers = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};
        aprLevelsSeries.setModel(Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
 
        // get rid the oldest sample in history:
        if (rollHistorySeries.size() > HISTORY_SIZE) {
            rollHistorySeries.removeFirst();
            pitchHistorySeries.removeFirst();
            azimuthHistorySeries.removeFirst();
        }
 
        // add the latest history sample:
        azimuthHistorySeries.addLast(null, sensorEvent.values[0]);
        pitchHistorySeries.addLast(null, sensorEvent.values[1]);
        rollHistorySeries.addLast(null, sensorEvent.values[2]);
 
        // redraw the Plots:
        aprHistoryPlot.redraw();
    }
 
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }
    
    public static void setPlotActivity(PlotActivity plotActivity)
    {
    	PlotActivity.plotActivity = plotActivity;
    }
    
    public static PlotActivity getPlotActivity()
    {
    	return PlotActivity.plotActivity;
    }
}