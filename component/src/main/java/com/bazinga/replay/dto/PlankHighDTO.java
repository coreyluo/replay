package com.bazinga.replay.dto;


import lombok.Data;

@Data
public class PlankHighDTO {

    private Integer plankHigh;

    private Integer unPlank;

    public PlankHighDTO(Integer plankHigh, Integer unPlank) {
        this.plankHigh = plankHigh;
        this.unPlank = unPlank;
    }
}
