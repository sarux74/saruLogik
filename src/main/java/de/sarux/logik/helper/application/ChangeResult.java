package de.sarux.logik.helper.application;

import lombok.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Value
public class ChangeResult {
    private Set<Integer> changedLines = new HashSet<>();
    private List<String> removeMessages = new ArrayList<>();
    private List<String> foundMessages = new ArrayList<>();



}
