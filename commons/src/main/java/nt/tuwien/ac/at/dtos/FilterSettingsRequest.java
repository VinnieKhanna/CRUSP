package nt.tuwien.ac.at.dtos;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sorted",
        "filtered"
})
public class FilterSettingsRequest {
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
