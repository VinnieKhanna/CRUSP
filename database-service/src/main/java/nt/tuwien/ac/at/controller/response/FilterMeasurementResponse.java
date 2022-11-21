package nt.tuwien.ac.at.controller.response;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "page",
        "pageSize",
        "count",
        "data"
})
public class FilterMeasurementResponse implements Serializable {

    @NotNull
    @JsonProperty("page")
    public int page;

    @NotNull
    @JsonProperty("pageSize")
    public int pageSize;

    @NotNull
    @JsonProperty("count")
    public long count;

    @NotNull
    @JsonProperty("data")
    public List<MeasurementResponse> data;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 2232066586788867277L;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}