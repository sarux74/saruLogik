/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem.view.grid;

import de.sarux.logik.helper.problem.LogikElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author sarux
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class LogikElementRelation {

    private LogikElement leftElement;
    private LogikElement rightElement;
}
