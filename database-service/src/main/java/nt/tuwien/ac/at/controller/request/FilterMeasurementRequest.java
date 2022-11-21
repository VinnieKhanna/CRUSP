package nt.tuwien.ac.at.controller.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import nt.tuwien.ac.at.dtos.Filtered;
import nt.tuwien.ac.at.dtos.Sorted;

import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "page",
        "pageSize",
        "sorted",
        "filtered"
})
public class FilterMeasurementRequest implements Serializable {

    @NotNull
    @JsonProperty("page")
    public int page;

    @NotNull
    @JsonProperty("pageSize")
    public int pageSize;

    @NotNull
    @JsonProperty("sorted")
    public List<Sorted> sorted = null;

    @NotNull
    @JsonProperty("filtered")
    public List<Filtered> filtered = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 3232066586788867278L;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}