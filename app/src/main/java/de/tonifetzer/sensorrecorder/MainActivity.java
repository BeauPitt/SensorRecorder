package de.tonifetzer.sensorrecorder;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;

import java.util.concurrent.TimeUnit;

import co.nstant.in.cbor.CborException;
import de.tonifetzer.sensorrecorder.sensors.Entry;
import de.tonifetzer.sensorrecorder.sensors.Logger;
import de.tonifetzer.sensorrecorder.sensors.MySensor;
import de.tonifetzer.sensorrecorder.sensors.PhoneSensors;
import de.tonifetzer.sensorrecorder.sensors.SensorType;

public class MainActivity extends AppCompatActivity {

    private static final int HISTORY_SIZE = 1500;

    //ui in main layout
    private View mainView;
    private ToggleButton recButton;
    private Button plotButton;
    private TextView txtViewTimeCounter;
    private TextView txtViewFilename;
    private TextView txtViewFilesize;
    private boolean isMainView = true;

    private PhoneSensors phoneSensors;
    private final Logger dataLogger = new Logger(this);

    private int loadCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this is super ugly. normally we would use fragments to switch ui
        mainView = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(mainView);

        //this is a small hack to get a static context
        MainActivity.context = getApplicationContext();

        //init the sensors of the phone
        phoneSensors = new PhoneSensors(this);

        //find ui elements in main
        recButton = findViewById(R.id.toggleButton);
//        txtViewTimeCounter = findViewById(R.id.textViewTimeCounter);
        txtViewFilename = findViewById(R.id.textViewFilename);
        txtViewFilesize = findViewById(R.id.textViewFilesize);


        //set the listener
        phoneSensors.setListener(new MySensor.SensorListener(){
            @Override public void onData(final String csv) {}
            @Override public void onData(final SensorType id, final String csv) throws CborException {
            }
            @Override
            public void onData(final Entry entry) throws CborException {
                addDataToFile(entry);
            }
        });

        recButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    buttonView.setBackgroundColor(Color.parseColor("#FFCC0000"));
                    startRecording();
                }
                else{
                    buttonView.setBackgroundColor(Color.parseColor("#FF669900"));
                    try {
                        stopRecording();
                    } catch (CborException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startRecording() {
        phoneSensors.onResume(this);
        loadCounter = 0;
        dataLogger.start();
        String path =  dataLogger.getFile().getAbsolutePath();
        txtViewFilename.setText(path.substring(path.length()-17));
    }

    private void stopRecording() throws CborException {
        phoneSensors.onPause(this);
        dataLogger.stop();
    }

    private void addDataToFile(Entry entry) throws CborException {

        dataLogger.addEntry(entry);

        runOnUiThread(new Runnable() {
            @Override public void run() {

                // dump buffer stats every x entries
                if (++loadCounter % 250 == 0) {

                    //set filesize and buffer dump
                    final int kbPerMin = (int) (dataLogger.getTotalSize() / 1024 * 1000 * 60 / (System.currentTimeMillis() - dataLogger.getStartTS()));
                    txtViewFilesize.setText( (dataLogger.getCurrentSize() / 1024) + "kb, " + kbPerMin + "kb/min");

                    //set time (of course, this is not perfectly accurate, however for this purpose its okay)
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - dataLogger.getStartTS());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - dataLogger.getStartTS());
                }

            }
        });

    }

    //This is also part of the hack to get a static context
    private static Context context;

    public static Context getAppContext() {
        return MainActivity.context;
    }
}
