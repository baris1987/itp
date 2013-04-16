package com.th.nuernberg.itp.earthquakedetection;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChartActivity extends Fragment implements SensorEventListener {

	private GraphicalView view;
	LinearLayout chartViewHolder;
	private TextView tvX, tvY, tvZ, tvAbs;
	private XYSeries[] xySeries;
	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int x = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_chart, container,
				false);
		dataset = new XYMultipleSeriesDataset();
		renderer = new XYMultipleSeriesRenderer();
		renderer = getRenderer();
		xySeries = getXYSeries();
		getXYSeriesRenderer();
		initLayout(rootView);
		mSensorManager = (SensorManager) this.getActivity().getSystemService(
				Context.SENSOR_SERVICE);

		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		return rootView;
	}

	@Override
	public void onResume() {
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}

	@Override
	public void onPause() {
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	private void initLayout(View rootView) {
		chartViewHolder = (LinearLayout) rootView.findViewById(R.id.chart);
		view = ChartFactory.getTimeChartView(this.getActivity(), dataset,
				renderer, "");
		view.refreshDrawableState();
		view.repaint();
		chartViewHolder.addView(view);

		tvX = (TextView) rootView.findViewById(R.id.tv_acc_x);
		tvY = (TextView) rootView.findViewById(R.id.tv_acc_y);
		tvZ = (TextView) rootView.findViewById(R.id.tv_acc_z);
		tvAbs = (TextView) rootView.findViewById(R.id.tv_acc_abs);
	}

	private XYMultipleSeriesRenderer getRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setMarginsColor(Color.BLACK);
		renderer.setClickEnabled(false);
		renderer.setExternalZoomEnabled(false);
		renderer.setPanEnabled(false, false);
		renderer.setYAxisMin(0, 0);
		renderer.setYAxisMax(16, 0);
		renderer.setYAxisAlign(Align.LEFT, 0);
		renderer.setAxesColor(Color.WHITE);

		return renderer;
	}

	private void getXYSeriesRenderer() {
		// X axis
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.RED);
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);
		// Y axis
		r = new XYSeriesRenderer();
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		r.setColor(Color.GREEN);
		renderer.addSeriesRenderer(r);
		// Z axis
		r = new XYSeriesRenderer();
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		r.setColor(Color.BLUE);
		renderer.addSeriesRenderer(r);
		// Abs axis
		r = new XYSeriesRenderer();
		r.setPointStyle(PointStyle.POINT);
		r.setColor(Color.YELLOW);
		r.setLineWidth(3);
		renderer.addSeriesRenderer(r);
	}

	private XYSeries[] getXYSeries() {
		XYSeries[] series = new XYSeries[4];
		series[0] = new XYSeries("X");
		series[1] = new XYSeries("Y");
		series[2] = new XYSeries("Z");
		series[3] = new XYSeries("Abs");
		dataset.addSeries(series[0]);
		dataset.addSeries(series[1]);
		dataset.addSeries(series[2]);
		dataset.addSeries(series[3]);
		return series;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

		float abs = Math.abs(event.values[0]) + Math.abs(event.values[1])
				+ Math.abs(event.values[2]);
		tvX.setText(String.format( "%.2f", event.values[0]));
		tvY.setText(String.format( "%.2f", event.values[1]));
		tvZ.setText(String.format( "%.2f", event.values[2]));
		tvAbs.setText(String.format( "%.2f", abs));
		
		if (xySeries[0].getItemCount() > 20) {
			xySeries[0].remove(0);
			xySeries[1].remove(0);
			xySeries[2].remove(0);
			xySeries[3].remove(0);
		}
		xySeries[0].add(x, event.values[0]);
		xySeries[1].add(x, event.values[1]);
		xySeries[2].add(x, event.values[2]);
		xySeries[3].add(x, abs);
		x++;
		view.repaint();

	}

}
