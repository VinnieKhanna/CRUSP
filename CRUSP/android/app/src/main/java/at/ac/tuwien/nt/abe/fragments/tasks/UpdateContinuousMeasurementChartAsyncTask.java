package at.ac.tuwien.nt.abe.fragments.tasks;

import android.os.AsyncTask;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.nt.abe.R;
import at.ac.tuwien.nt.abe.fragments.MeasurementFragment;
import at.ac.tuwien.nt.abe.fragments.MeasurementType;
import at.ac.tuwien.nt.abe.model.MeasurementResult;

public class UpdateContinuousMeasurementChartAsyncTask extends AsyncTask<Void, Void, LineData> {
    private final List<MeasurementResult> measurementResults;
    private final MeasurementType measurementType;
    private MeasurementFragment fragment;
    private int color;

    public UpdateContinuousMeasurementChartAsyncTask(MeasurementFragment fragment, List<MeasurementResult> measurementResults, MeasurementType measurementType, int color) {
        this.fragment = fragment;
        this.measurementResults = measurementResults;
        this.measurementType = measurementType;
        this.color = color;
    }

    @Override
    protected LineData doInBackground(Void... empty) {
        LineData data = new LineData();

        if( !measurementResults.isEmpty() ) {
            List<MeasurementResult> lastTenResults = measurementResults.subList(0, measurementResults.size() > 10 ? 10 : measurementResults.size());
            Collections.reverse(lastTenResults);

            ArrayList<Entry> dataList = new ArrayList<>();
            for (int i = 0; i < lastTenResults.size(); i++) {
                float yValue =  lastTenResults.get(i).getAvailableBandwidth();
                dataList.add(new Entry(i, yValue));
            }

            LineDataSet lineDataSet = new LineDataSet(dataList, "Data Rate in MBit/s");
            lineDataSet.setColor(color);
            lineDataSet.setCircleColor(color);
            lineDataSet.setCircleRadius(2f);
            lineDataSet.setDrawValues(false);
            data.addDataSet(lineDataSet);
        }

        return data;
    }

    @Override
    protected void onPostExecute(LineData data) {
        super.onPostExecute(data);

        View view = fragment.getView();
        if(view != null && !measurementType.equals(MeasurementType.SINGLE)) {
            LineChart lineChart = view.findViewById(R.id.cont_linechart);

            lineChart.getDescription().setText("");

            lineChart.setData(data);
            lineChart.invalidate();
            lineChart.setVisibility(View.VISIBLE);
        }
    }
}
