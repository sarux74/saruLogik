package de.sarux.logik.helper.application;

import lombok.Data;

import java.util.List;

@Data
public class ValueView {
    private String text;
    private List<Integer> selectableValues;

    public ValueView(String text) {
        this.text = text;
    }

    public ValueView(String text, List<Integer> selectableValues) {
        this.text = text;
        this.selectableValues = selectableValues;
    }


}
