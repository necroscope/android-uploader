package com.nightscout.core.dexcom.records;

import com.nightscout.core.dexcom.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class EGVRecord extends GenericTimestampRecord {
    public final static int RECORD_SIZE = 12;
    private int bGValue;
    private TrendArrow trend;
    private NoiseMode noiseMode;

    public EGVRecord(byte[] packet) {
        // system_time (UInt), display_time (UInt), glucose (UShort), trend_arrow (Byte), crc (UShort))
        super(packet);
        if (packet.length != RECORD_SIZE) {
            throw new InvalidRecordLengthException("Unexpected record size: " + packet.length +
                    ". Expected size: " + RECORD_SIZE + ". Unparsed record: " + Utils.bytesToHex(packet));
        }
        int eGValue = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN).getShort(8);
        bGValue = eGValue & Constants.EGV_VALUE_MASK;
        int trendAndNoise = ByteBuffer.wrap(packet).get(10);
        int trendValue = trendAndNoise & Constants.EGV_TREND_ARROW_MASK;
        byte noiseValue = (byte) ((trendAndNoise & 0xF) >> 4);
        trend = TrendArrow.values()[trendValue];
        noiseMode = NoiseMode.values()[noiseValue];
    }

    public EGVRecord(int bGValue, TrendArrow trend, Date displayTime, Date systemTime, NoiseMode noise){
        super(displayTime, systemTime);
        this.bGValue = bGValue;
        this.trend = trend;
        this.noiseMode = noise;
    }

    public int getBGValue() {
        return bGValue;
    }

    public TrendArrow getTrend() {
        return trend;
    }

    public NoiseMode getNoiseMode(){
        return noiseMode;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("sgv", getBGValue());
            obj.put("date", getDisplayTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public boolean isSpecialValue(){
        for (SpecialValue specialValue : SpecialValue.values()){
            if (specialValue.getValue() == bGValue){
                return true;
            }
        }
        return false;
    }
}
