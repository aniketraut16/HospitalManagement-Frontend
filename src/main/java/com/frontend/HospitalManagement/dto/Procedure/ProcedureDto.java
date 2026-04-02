package com.frontend.HospitalManagement.dto.Procedure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcedureDto {

    private Integer code;
    private String name;
    private double cost;

    @JsonProperty("_links")
    @JsonIgnore
    private Map<String, Object> links;
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    @Override
    public String toString() {
        return "ProcedureDto{name='" + name + "', cost=" + cost + "}";
    }
}