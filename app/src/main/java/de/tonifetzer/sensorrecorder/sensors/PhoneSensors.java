package de.tonifetzer.sensorrecorder.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import co.nstant.in.cbor.CborException;

/**
 * all available sensors
 * and what to do within one class
 *
 */
public class PhoneSensors extends MySensor implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor acc;
   	private Sensor lin_acc;
    private Sensor gyro;


	/** ctor */
    public PhoneSensors(final Activity act){

		// fetch the sensor manager from the activity
        sensorManager = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);

		// try to get each sensor
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lin_acc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		// dump sensor-vendor info to file
		dumpVendors(act);

	}

	private final char NL = '\n';

	/** Write Vendors to file */
	private void dumpVendors(final Activity act) {

		final DataFolder folder = new DataFolder(act, "sensorOutFiles");
		final File file = new File(folder.getFolder(), "vendors.txt");

		try {

			final FileOutputStream fos = new FileOutputStream(file);
			final StringBuilder sb = new StringBuilder();

			// constructor smartphone details
			sb.append("[Device]").append(NL);
			sb.append("\tModel: ").append(Build.MODEL).append(NL);
			sb.append("\tAndroid: ").append(Build.VERSION.RELEASE).append(NL);
			sb.append(NL);

			// construct sensor details
			dumpSensor(sb, SensorType.ACCELEROMETER, acc);
			dumpSensor(sb, SensorType.LINEAR_ACCELERATION, lin_acc);
			dumpSensor(sb, SensorType.GYROSCOPE, gyro);
			// write
			fos.write(sb.toString().getBytes());
			fos.close();

		}catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	/** dump all details of the given sensor into the provided stringbuilder */
	private void dumpSensor(final StringBuilder sb, final SensorType type, final Sensor sensor) {
		sb.append("[Sensor]").append(NL);
		sb.append("\tour_id: ").append(type.id()).append(NL);
		sb.append("\ttype: ").append(type).append(NL);

		if (sensor != null) {
			sb.append("\tVendor: ").append(sensor.getVendor()).append(NL);
			sb.append("\tName: ").append(sensor.getName()).append(NL);
			sb.append("\tVersion: ").append(sensor.getVersion()).append(NL);
			sb.append("\tMinDelay: ").append(sensor.getMinDelay()).append(NL);
			//sb.append("\tMaxDelay: ").append(sensor.getMaxDelay()).append(NL);
			sb.append("\tMaxRange: ").append(sensor.getMaximumRange()).append(NL);
			sb.append("\tPower: ").append(sensor.getPower()).append(NL);
			//sb.append("ReportingMode: ").append(sensor.getReportingMode()).append(NL);
			sb.append("\tResolution: ").append(sensor.getResolution()).append(NL);
			sb.append("\tType: ").append(sensor.getType()).append(NL);
		} else {
			sb.append("\tnot available!\n");
		}
		sb.append(NL);
	}

    @Override
    public void onSensorChanged(SensorEvent event) {

		long now = System.currentTimeMillis();
		Log.d("PhoneSensors", "onSensorChanged: " + event.sensor.getType());

		if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			// inform listeners
			if (listener != null){
				try {
					listener.onData(new Entry(now, event.values[0], event.values[1], event.values[2],
							Sensor.TYPE_GYROSCOPE));
			} catch (CborException e) {
					e.printStackTrace();
				}
			}

		}

		else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			// inform listeners
			if (listener != null){
				try {
					listener.onData(new Entry(now, event.values[0], event.values[1], event.values[2],
							Sensor.TYPE_LINEAR_ACCELERATION));
				} catch (CborException e) {
					e.printStackTrace();
				}
			}

		}

		else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// inform listeners
			if (listener != null){
				try {
					listener.onData(new Entry(now, event.values[0],	event.values[1], event.values[2],
							Sensor.TYPE_ACCELEROMETER));
				} catch (CborException e) {
					e.printStackTrace();
				}
			}

		}

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nothing to-do here
    }

    @Override
    public void onResume(final Activity act) {

		// attach as listener to each of the available sensors
        registerIfPresent(acc, SensorManager.SENSOR_DELAY_FASTEST);
       	registerIfPresent(gyro, SensorManager.SENSOR_DELAY_FASTEST);
        registerIfPresent(lin_acc, SensorManager.SENSOR_DELAY_FASTEST);

    }

	private void registerIfPresent(final Sensor sens, final int delay) {
		if (sens != null) {
			sensorManager.registerListener(this, sens, delay);
			Log.d("PhoneSensors", "added sensor " + sens.toString());
		} else {
			Log.d("PhoneSensors", "sensor not present. skipping");
		}
	}

    @Override
    public void onPause(final Activity act) {

		// detach from all events
		sensorManager.unregisterListener(this);

    }

}
