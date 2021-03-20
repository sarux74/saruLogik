import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {LogikGroup} from '../group/model/logik-group';
import {LogikViewLine} from '../solve/model/logik-view-line';
import {GroupService} from '../group/group.service';
import {PositionerService} from './positioner.service';
import {ErrorDialogComponent} from '../dialog/error-dialog/error-dialog.component';
import {Router} from '@angular/router';
import {ActivatedRoute} from '@angular/router';
import {ValueSelectDialogComponent} from '../solve/dialog/value-select-dialog/value-select-dialog.component';

@Component({
    selector: 'app-positioner',
    templateUrl: './positioner.component.html',
    styleUrls: ['./positioner.component.css']
})
export class PositionerComponent implements OnInit {
    groups: LogikGroup[];
    groups1: LogikGroup[];
    groups2: LogikGroup[];

    group1Id = -1;
    group2Id = -1;

    headers: string[];
    lines: LogikViewLine[];

    key: string;
    private sub: any;

    flexPercent: number;

    constructor(private groupService: GroupService, private positionerService: PositionerService, public dialog: MatDialog,
                private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
            this.groups1 = data;
            this.groups2 = data;
        });

        this.sub = this.route.params.subscribe(params => {
            const key = 'problem';
            this.key = params[key];
        });
    }


    counter(i: number) {
        return new Array(i);
    }

    updatePositionierer(index: number) {
        console.log('Aufruf update');
        console.log(index);
        if (index === 1) {
            const group2Prep = [];
            for (const id of this.groups) {
                if (id.index !== this.group1Id) {
                    group2Prep.push(id);
                }
            }
            this.groups2 = group2Prep;
        } else if (index === 2) {
            const group1Prep = [];
            for (const id of this.groups) {
                if (id.index !== this.group2Id) {
                    group1Prep.push(id);
                }
            }
            this.groups1 = group1Prep;
        }
        console.log(this.group1Id);
        console.log(this.group2Id);
        if (this.group1Id > -1 && this.group2Id > -1) {
            this.initPositionerView();
        }
    }

    initPositionerView() {
        console.log('Load mit ' + this.group1Id + ' und ' + this.group2Id);
        if (this.group1Id > -1 && this.group2Id > -1 && this.group1Id !== this.group2Id) {
            this.positionerService.initPositionerView(this.key, this.group1Id, this.group2Id).subscribe(data => {
                this.loadPositionerView();
            },
                error => {
                    console.log(error);
                }
            );
        }
    }

    loadPositionerView() {
        this.positionerService.loadPositionerView(this.key).subscribe(data => {
            console.log(data);
            this.headers = data.headers;
            this.lines = data.lines;
            this.flexPercent = Math.floor(94 / this.headers.length);
            console.log(this.flexPercent);
        },
            error => {
                console.log(error);
            }
        );
    }

    printViewValue(valueList: any[], index: number): string {
        if (!valueList || valueList.length <= index) {
            return '';
        }

        const value = valueList[index];
        return (!value) ? '' : value.text;
    }

    editSelection(lineId: number, valueList: any[], index: number): void {
        const dialogRef = this.dialog.open(ValueSelectDialogComponent, {
            width: '400px',
            data: {
                group: this.groups[this.group2Id],
                selection: valueList[index].selectableValues,
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.positionerService.updateSelection(this.key, lineId, index, result).subscribe(_ => {
                    this.loadPositionerView();
                },
                    error => {
                        console.log(error);
                    });
            }
        });
    }

    addLine(lineId: number) {
        this.positionerService.addLine(this.key, lineId, 0).subscribe(result => {
            console.log(result);
            this.loadPositionerView();
        },
            error => {
                console.log(error);
            }
        );
    }

    removeLine(lineId: number) {
        this.positionerService.removeLine(this.key, lineId).subscribe(result => {
            console.log(result);
            this.loadPositionerView();
        },
            error => {
                console.log(error);
            }
        );
    }

    copyToLeft(lineId: number) {
        this.positionerService.addLine(this.key, lineId, -1).subscribe(result => {
            console.log(result);
            this.loadPositionerView();
        },
            error => {
                console.log(error);
            }
        );
    }

    copyToRight(lineId: number) {
        this.positionerService.addLine(this.key, lineId, 1).subscribe(result => {
            console.log(result);
            this.loadPositionerView();
        },
            error => {
                console.log(error);
            }
        );
    }

    overtake() {
        this.positionerService.overtake(this.key).subscribe(result => {
            window.close();
        },
            error => {
                console.log(error);
            }
        );
    }
}
