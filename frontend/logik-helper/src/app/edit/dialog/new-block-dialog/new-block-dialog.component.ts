import {Component, OnInit} from '@angular/core';
import {GroupService} from '../../../group/group.service';
import {LogikGroup} from '../../../group/model/logik-group';
import {BlockOption} from './block-option';

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
    options: BlockOption[] = [];
    
    constructor(private groupService: GroupService) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
        });
    }

    addOption() {
        if (this.options.length == 0) {
            const option1 = new BlockOption();
            option1.name = 'wahr';
            this.options.push(option1);

            const option2 = new BlockOption();
            option2.name = 'falsch';
            this.options.push(option2);
        } else {
            this.options.push(new BlockOption());
        }
    }
    
    removeLastOption() {
        this.options.splice(-1,1)
    }
}
