import {Component, OnInit} from '@angular/core';
import {SolveService} from '../solve/solve.service';
import {GroupService} from '../group/group.service';
import {LogikGroup} from '../group/model/logik-group';
import {LogikViewLine} from '../model/logik-view-line';
import {ActivatedRoute} from '@angular/router';
import {ValueSelectDialogComponent} from '../solve/dialog/value-select-dialog/value-select-dialog.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
    selector: 'app-group-view',
    templateUrl: './group-view.component.html',
    styleUrls: ['./group-view.component.css']
})
export class GroupViewComponent implements OnInit {

    groupId: number;
    flexPercent: number;
    groups: LogikGroup[];
    sortedGroups: LogikGroup[];
    lines: LogikViewLine[];
    selectedLines: boolean[] = [];

    key: string;
    private sub: any;

    constructor(private solveService: SolveService, private groupService: GroupService, private route: ActivatedRoute, public dialog: MatDialog) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
            this.flexPercent = Math.floor(98 / this.groups.length);
        });
        this.sub = this.route.params.subscribe(params => {
            const key = 'problem';
            this.key = params[key];
        });
    }

    counter(i: number) {
        return new Array(i);
    }

    printViewValue(line: LogikViewLine, index: number): string {
        if (!line.view || line.view.length <= index) {
            return '';
        }

        const value = line.view[index];
        return (!value) ? '' : value.text;
    }

    loadGroupView() {
        // Resort groups
        const prepareSortedGroups = [];
        for (const group of this.groups) {
            if (group.index === this.groupId) {
                prepareSortedGroups.push(group);
                break;
            }
        }

        for (const group of this.groups) {
            if (group.index !== this.groupId) {
                prepareSortedGroups.push(group);
            }
        }
        this.sortedGroups = prepareSortedGroups;
        this.solveService.loadGroupView(this.key, this.groupId).subscribe(res => {
            this.lines = res;
            const newSelectedLines = [];
            for (const _ of this.lines) {
                newSelectedLines.push(false);
            }
            this.selectedLines = newSelectedLines;
        });
    }
    
    editSelection(line: LogikViewLine, index: number): void {
        console.log(line);
        console.log(index);
        const dialogRef = this.dialog.open(ValueSelectDialogComponent, {
            width: '400px',
            data: {
                group: this.sortedGroups[index],
                selection: line.view[index].selectableValues,
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                const element1Index = line.lineId;
               const group2 = this.sortedGroups[index];
                this.solveService.updateGrid(this.key, element1Index, this.groupId, group2.index, result).subscribe(resResult => {
                    this.loadGroupView();
                });
            }
        });
    }
    
    applyBlockingCandidates() {
        console.log(this.selectedLines);
        const candidates = [];
        for (let i = 0; i < this.selectedLines.length; i++) {
            if (this.selectedLines[i] === true) {
                candidates.push(i);
            }
        }
        console.log(candidates);
        this.solveService.applyBlockingCandidates(this.key, this.groupId, candidates).subscribe(res => {
            if (res) {
                this.loadGroupView();
            }
        });
    }
}
