import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Inject} from '@angular/core';
import {LogikGroup} from '../model/logik-group';
import {LogikElement} from '../model/logik-element';
import {MatInputModule} from '@angular/material/input';
import {ScrollingModule} from '@angular/cdk/scrolling';
import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';


@Component({
    selector: 'group-edit-dialog',
    templateUrl: './group-edit-dialog.component.html',
    styleUrls: ['./group-edit-dialog.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GroupEditDialog {

    copyGroupId: number;
    seriesFrom: number;
    seriesTo: number;
    seriesStep: number;
    sortAfterSave = false;
    addSize = 1;

    constructor(
        public dialogRef: MatDialogRef<GroupEditDialog>,
        @Inject(MAT_DIALOG_DATA) public data: {newGroup: LogikGroup, groups: LogikGroup[]}) {}

    newElement(index: number) {
        for (let i = 0; i < this.addSize; i++) {
            const newElement = new LogikElement();
            newElement.index = this.data.newGroup.elements.length;
            this.data.newGroup.elements = [...this.data.newGroup.elements, (newElement)];
        }
    }

    removeLastElement() {
        this.data.newGroup.elements.pop();
    }

    onNoClick(): void {
        this.dialogRef.close();
    }

    sort(group: LogikGroup): LogikGroup {
        group.elements.sort((a, b) => (a.shortName.localeCompare(b.shortName)));
        for (let i = 0; i < group.elements.length; i++) {
            group.elements[i].index = i;
        }
        return group;
    }

    submit() {
        let result = this.data.newGroup;
        if (this.sortAfterSave)
            result = this.sort(this.data.newGroup);
        this.dialogRef.close(result);
    }

    copyGroup() {
        const copiedGroup = [];
        for (let element of this.data.groups[this.copyGroupId].elements) {
            const newElement = new LogikElement();
            newElement.index = element.index;
            newElement.shortName = element.shortName;
            newElement.name = element.name;
            copiedGroup.push(newElement);
            this.data.newGroup.elements = copiedGroup;
        }
    }

    seriesGroup() {
        const seriesFrom = +this.seriesFrom;
        const seriesTo = +this.seriesTo;
        const seriesStep = +this.seriesStep;

        let numberLength = (seriesTo >= 1000) ? 4 : (seriesTo >= 100) ? 3 : seriesTo >= 10 ? 2 : 1;
        let counter = seriesFrom;
        let index = 0;
        const copiedGroup = [];
        while (counter <= seriesTo) {
            const newElement = new LogikElement();
            newElement.index = index;
            let shortName = counter + "";
            while (shortName.length < numberLength) shortName = "0" + shortName;

            newElement.shortName = shortName;
            newElement.name = '' + counter;
            copiedGroup.push(newElement);
            this.data.newGroup.elements = copiedGroup;

            index++;
            counter = counter + seriesStep;
            console.log(counter);
        }
    }
}
