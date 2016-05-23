package net.steppschuh.datalogger.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import net.steppschuh.datalogger.data.Data;
import net.steppschuh.datalogger.data.DataBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorDataManager {

    private SensorManager sensorManager;

    private Map<Integer, DataBatch> sensorDataBatches;
    private Map<Integer, SensorEventListener> sensorEventListeners;

    public SensorDataManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        initializeSensorEventListeners();
        initializeSensorDataBatches();
    }

    private void initializeSensorEventListeners() {
        sensorDataBatches = new HashMap<>();
    }

    private void initializeSensorDataBatches() {
        sensorDataBatches = new HashMap<>();
        List<Sensor> availableSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : availableSensors) {
            sensorDataBatches.put(sensor.getType(), getDataBatch(sensor.getType()));
        }
    }

    private void registerSensorEventListener(int sensorType) {
        registerSensorEventListener(sensorManager.getDefaultSensor(sensorType));
    }

    private void registerSensorEventListener(Sensor sensor) {
        sensorManager.registerListener(getSensorEventListener(sensor.getType()), sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAllSensorEventListeners() {
        List<Sensor> availableSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : availableSensors) {
            unregisterSensorEventListener(sensor.getType());
        }
    }

    private void unregisterSensorEventListener(int sensorType) {
        SensorEventListener sensorEventListener = sensorEventListeners.get(sensorType);
        if (sensorEventListener != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private SensorEventListener getSensorEventListener(int sensorType) {
        SensorEventListener sensorEventListener = sensorEventListeners.get(sensorType);
        if (sensorEventListener == null) {
            sensorEventListener = createSensorEventListener(sensorType);
            sensorEventListeners.put(sensorType, sensorEventListener);
        }
        return sensorEventListener;
    }

    private SensorEventListener createSensorEventListener(final int sensorType) {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] values = new float[event.values.length];
                System.arraycopy(event.values, 0, values, 0, event.values.length);
                Data data = new Data(event.sensor.getName(), values);
                getDataBatch(sensorType).addData(data);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    private DataBatch getDataBatch(int sensorType) {
        DataBatch dataBatch = sensorDataBatches.get(sensorType);
        if (dataBatch == null) {
            dataBatch = createDataBatch(sensorType);
            sensorDataBatches.put(sensorType, dataBatch);
        }
        return dataBatch;
    }

    private DataBatch createDataBatch(int sensorType) {
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if (sensor == null) {
            return null;
        }
        String sensorName = sensor.getName();
        return new DataBatch(sensorName);
    }

    /**
     * Getter & Setter
     */
    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public Map<Integer, DataBatch> getSensorDataBatches() {
        return sensorDataBatches;
    }

    public void setSensorDataBatches(Map<Integer, DataBatch> sensorDataBatches) {
        this.sensorDataBatches = sensorDataBatches;
    }

    public Map<Integer, SensorEventListener> getSensorEventListeners() {
        return sensorEventListeners;
    }

    public void setSensorEventListeners(Map<Integer, SensorEventListener> sensorEventListeners) {
        this.sensorEventListeners = sensorEventListeners;
    }
}
