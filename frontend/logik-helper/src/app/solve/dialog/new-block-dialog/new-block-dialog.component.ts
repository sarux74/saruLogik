import {Component, OnInit} from '@angular/core';
import {GroupService} from '../../../group/group.service';
import {LogikGroup} from '../../../group/model/logik-group';

@Component({
    selector: 'app-new-block-dialog',
    templateUrl: './new-block-dialog.component.html',
    styleUrls: ['./new-block-dialog.component.css']
})
export class NewBlockDialogComponent implements OnInit {

    blockName: string;
    noDuplicates: boolean;

    groupId: number;
    groups: LogikGroup[];
    excludeSameShortNames = false;

    constructor(private groupService: GroupService) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
        });
    }
}
