import {Component, OnInit} from '@angular/core';
import {LogikGroup} from '../../../group/model/logik-group';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Inject} from '@angular/core';

@Component({
    selector: 'value-select-dialog',
    templateUrl: './value-select-dialog.component.html',
})
export class ValueSelectDialog {

    selected: boolean[] = [];

    constructor(
        public dialogRef: MatDialogRef<ValueSelectDialog>,
        @Inject(MAT_DIALOG_DATA) public data: any) {

        for (let i = 0; i < data.group.elements.length; i++) {
            this.selected.push(data.selection.indexOf(i) !== -1);
        }
    }

    convert(group: LogikGroup, selected: boolean[]): number[] {
        const result = [];
        for (let i = 0; i < group.elements.length; i++) {
            if (selected[i]) {
                result.push(i);
            }
        }
        return result;
    }

    counter(i: number) {
        return new Array(i);
    }

    unselect() {
        const emptySelection = [];
        for (let i = 0; i < this.selected.length; i++)
            emptySelection.push(false);
        this.selected = emptySelection;
    }
}
