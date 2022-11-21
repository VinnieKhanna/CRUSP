package at.ac.tuwien.nt.abe;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.ReceivedPacketDetails;
import at.ac.tuwien.nt.abe.model.SequenceDetails;

import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT;

public class PacketDeltaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet_delta);
        Toolbar toolbar = findViewById(R.id.packet_delta_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        MeasurementResult result = (MeasurementResult) Objects.requireNonNull(getIntent().getExtras()).getSerializable(KEY_MEASUREMENT_RESULT);

        if(result != null) {
            initDeltaChart(result);
        }
    }

    private void initDeltaChart(MeasurementResult result) {
        Thread thread = new Thread(() -> {
            BarChart barChart = findViewById(R.id.barchart);
            BarData data = new BarData();

            if (result.getSequenceCollection() != null) {
                for (SequenceDetails sequenceDetails : result.getSequenceCollection()) {
                    List<BarEntry> dataList = new ArrayList<>();
                    for (ReceivedPacketDetails packet : sequenceDetails.getPackets()) {
                        dataList.add(new BarEntry((float)(packet.getDeltaToStartTime()/1000), packet.getRecvBytes()));
                    }

                    BarDataSet barDataSet = new BarDataSet(dataList, "Packet distribution");
                    int color = getColor(R.color.Primary);  // TODO: change to API level
                    barDataSet.setColor(color);
                    barDataSet.setBarBorderColor(color);
                    barDataSet.setBarBorderWidth(0.5f);
                    barDataSet.setDrawValues(false);
                    data.addDataSet(barDataSet);
                    break; //stop after first loop in sequence details
                }
            }

            barChart.getDescription().setText("");

            barChart.setData(data);
            barChart.setVisibility(View.VISIBLE);
        });

        thread.start();
    }
}
