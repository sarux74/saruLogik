import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Inject} from '@angular/core';
import {LogikGroup} from '../model/logik-group';
import {LogikElement} from '../model/logik-element';
import {ChangeDetectionStrategy, Component} from '@angular/core';


@Component({
    selector: 'group-edit-dialog',
    templateUrl: './group-edit-dialog.component.html',
    styleUrls: ['./group-edit-dialog.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GroupEditDialogComponent {

    copyGroupId: number;
    seriesFrom: number;
    seriesTo: number;
    seriesStep: number;
    sortAfterSave = false;
    addSize = 1;

    constructor(
        public dialogRef: MatDialogRef<GroupEditDialogComponent>,
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
        if (this.sortAfterSave) {
            result = this.sort(this.data.newGroup);
        }
        this.dialogRef.close(result);
    }

    copyGroup() {
        const copiedGroup = [];
        for (const element of this.data.groups[this.copyGroupId].elements) {
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

        const numberLength = (seriesTo >= 1000) ? 4 : (seriesTo >= 100) ? 3 : seriesTo >= 10 ? 2 : 1;
        let counter = seriesFrom;
        let index = 0;
        const copiedGroup = [];
        while (counter <= seriesTo) {
            const newElement = new LogikElement();
            newElement.index = index;
            let shortName = counter + '';
            while (shortName.length < numberLength) {
                shortName = '0' + shortName;
            }

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
