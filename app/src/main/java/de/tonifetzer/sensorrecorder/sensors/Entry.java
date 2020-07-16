package de.tonifetzer.sensorrecorder.sensors;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;

public class Entry implements Serializable {

    private String sensorName;
    private int sensorId;
    private Long ts;
    private float x;
    private float y;
    private float z;
    private int prediction;
    public Entry(long ts, float x, float y, float z, int sId) {
        this.ts = ts;
        this.sensorId = sId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getSensorId() {
        return sensorId;
    }

    public long getDelta(long start) {
        return this.ts - start;
    }

    public Long getTs() {
        return ts;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getPrediction() {
        return prediction;
    }

    public String getSensorName() {
        return this.sensorName;
    }
}

