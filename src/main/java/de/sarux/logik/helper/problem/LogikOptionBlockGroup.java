package de.sarux.logik.helper.problem;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property="@id")
public class LogikOptionBlockGroup {

    private int blockGroupId;
    @Getter
    private final List<GeneralLogikBlock> optionBlocks = new ArrayList<>();
    @Getter
    private String name;
    
    @JsonIgnore
    @Setter
    private boolean hide;

     public LogikOptionBlockGroup(int blockGroupId, String name) {
        this.blockGroupId = blockGroupId;
        this.name = name;
    }

    void append(GeneralLogikBlock optionBlock) {
        this.optionBlocks.add(optionBlock);
    }
}
