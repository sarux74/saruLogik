/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem.view.group;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author sarux
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateGridInput {

    int group1Id;
    int element1Id;
    int group2Id;
    List<Integer> selection;
}
