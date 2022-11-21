package at.ac.tuwien.nt.abe.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.nt.abe.R;
import at.ac.tuwien.nt.abe.ResultDetailsActivity;
import at.ac.tuwien.nt.abe.model.Converters;
import at.ac.tuwien.nt.abe.model.CruspError;
import at.ac.tuwien.nt.abe.model.MeasurementResult;

import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT_ID;

public class ResultsAdapter extends RecyclerView.Adapter {
    private List<MeasurementResult> resultList;

    public ResultsAdapter() {
        this.resultList = new ArrayList<>();
    }

    // is called whenever a new instance of our ViewHolder class is created.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(i, viewGroup, false);
        return new ResultViewHolder(view);
    }

    // is called when the data is shown in the UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(resultList != null) {
            ((ResultViewHolder)viewHolder).bindData(resultList.get(i));
        }

    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public void setResultList(List<MeasurementResult> resultList) {
        this.resultList = resultList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.result_item;
    }


    private class ResultViewHolder extends RecyclerView.ViewHolder {
        private TextView rate;
        private TextView date;
        private TextView time;
        private TextView type;
        private Button detailsButton;

        ResultViewHolder(@NonNull View itemView) {
            super(itemView);

            rate = itemView.findViewById(R.id.rate);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            type = itemView.findViewById(R.id.type);
            detailsButton = itemView.findViewById(R.id.detailsButton);
        }

        void bindData(final MeasurementResult result) {
            if(result.getErrorType() == CruspError.NO_ERROR) {
                rate.setText(((float) Math.round(result.getAvailableBandwidth() * 100)) / 100 + " Mbps");
            } else {
                rate.setText(result.getErrorType().toString().replace('_', '\n'));
            }

            LocalDateTime localDateTime = Converters.toLocalDateTime(result.getStartTime());
            date.setText(localDateTime.toLocalDate().toString());
            time.setText(localDateTime.withNano(0).format(DateTimeFormatter.ISO_LOCAL_TIME)); // to cut nano seconds
            type.setText(result.isDownlink() ? "DL" : "UL");

            detailsButton.setOnClickListener((view) -> {
                Intent intent = new Intent(view.getContext(), ResultDetailsActivity.class);
                intent.putExtra(KEY_MEASUREMENT_RESULT_ID, result.getUid());
                view.getContext().startActivity(intent);
            });
        }
    }
}
