import {Component, OnInit} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Inject} from '@angular/core';
import {FormControl} from '@angular/forms';

@Component({
    selector: 'app-new-relation-dialog',
    templateUrl: './new-relation-dialog.component.html',
    styleUrls: ['./new-relation-dialog.component.css']
})
export class NewRelationDialogComponent implements OnInit {
    groupFrom = new FormControl();
    relationType = new FormControl('NONE');
    relationHint = new FormControl();
    subLine = new FormControl();
    groupTo = new FormControl();

    relationTypes = [
        {id: 'NONE', name: 'keine'},
        {id: 'PREVIOUS', name: 'vor/früher/jünger/weniger'},
        {id: 'NEXT', name: 'nach/später/älter/mehr'},
        {id: 'PLUS_MINUS', name: 'mehr oder weniger'},
        {id: 'EQUAL', name: 'gleich'},
        {id: 'NOT_PREVIOUS', name: 'nicht vor/früher'},
        {id: 'NOT_NEXT', name: 'nicht nach/später'},
        {id: 'NOT_PLUS_MINUS', name: 'nicht mehr oder weniger'},
        {id: 'NOT_EQUAL', name: 'ungleich'}
    ];

    constructor(public dialogRef: MatDialogRef<NewRelationDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: any) {}

    ngOnInit(): void {
    }

    copySelection(event: any) {
        this.groupTo.setValue(event.value);
    }

    buildResult(): any {
        return {
            blockId: this.data.blockId, leftLineId: this.data.leftLineId, rightLineId: this.data.rightLineId,
            groupFrom: this.groupFrom.value, groupTo: this.groupTo.value, relationType: this.relationType.value,
            relationHint: this.relationHint.value, subLine: this.subLine.value
        };
    }

}
