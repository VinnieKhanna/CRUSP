package at.ac.tuwien.nt.abe;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.ac.tuwien.nt.abe.util.BurstCalculator;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.SequenceDetails;

import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT;

public class BurstRateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burst_rate);

        Toolbar toolbar = findViewById(R.id.burst_rate_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        MeasurementResult result = (MeasurementResult) Objects.requireNonNull(getIntent().getExtras()).getSerializable(KEY_MEASUREMENT_RESULT);

        if(result != null) {
            BurstCalculator calculator = new BurstCalculator();

            List<BurstCalculator.Burst> allBursts = new ArrayList<>();

            for(SequenceDetails details: result.getSequenceCollection()) {
                List<BurstCalculator.Burst> bursts = calculator.findBursts(details);
                allBursts.addAll(bursts);
                double rate = calculator.calculateSophisticatedRate(bursts);
                Log.i(BurstRateActivity.class.getName(), "Rate by bursts: " + rate + " MBit/s");
            }

            initBurstChart(allBursts);
        }
    }

    private void initBurstChart(List<BurstCalculator.Burst> bursts) {
        Thread thread = new Thread(() -> {
            CombinedChart chart = findViewById(R.id.combinedchart);
            LineData lineData = new LineData();
            BarData barData = new BarData();

            List<Entry> rateList = new ArrayList<>();
            List<BarEntry> packetCountList = new ArrayList<>();

            for (int i = 0; i < bursts.size(); i++) {
                BurstCalculator.Burst burst = bursts.get(i);
                rateList.add(new Entry(i + 1, (float) burst.getRate()));
                packetCountList.add(new BarEntry(i + 1, (float) burst.packetCount()));
            }

            LineDataSet rateDataSet = new LineDataSet(rateList, "burst rate in MBit/s");
            int color = getColor(R.color.Primary_Variant2);  // TODO: change to API level
            rateDataSet.setColor(color);
            rateDataSet.setCircleColor(color);
            rateDataSet.setCircleRadius(3f);
            rateDataSet.setLineWidth(2f);
            rateDataSet.setDrawValues(true);
            rateDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData.addDataSet(rateDataSet);

            BarDataSet packageCountDataSet = new BarDataSet(packetCountList, "packet count");
            packageCountDataSet.setBarBorderWidth(0.9f);
            int color2 = getColor(R.color.Secondary);  // TODO: change to API level
            packageCountDataSet.setColor(color2);
            packageCountDataSet.setDrawValues(true);
            packageCountDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            barData.addDataSet(packageCountDataSet);

            CombinedData data = new CombinedData();
            data.setData(lineData);
            data.setData(barData);

            // draw bars behind lines
            chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                    CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
            });

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setDrawGridLines(true);
            rightAxis.setGranularity(1.0f);
            rightAxis.setGranularityEnabled(true);
            rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            chart.getDescription().setText("");

            chart.setData(data);
            chart.setVisibility(View.VISIBLE);
        });

        thread.start();
    }
}
