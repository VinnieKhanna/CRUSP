package nt.tuwien.ac.at.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nt.tuwien.ac.at.model.MeasurementResult;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterMeasurementDto {
    private List<MeasurementResult> results;
    private Long count;
}
