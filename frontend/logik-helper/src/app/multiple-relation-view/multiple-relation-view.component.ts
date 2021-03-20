import {Component, OnInit, OnDestroy} from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikViewLine} from '../solve/model/logik-view-line';
import {ErrorDialogComponent} from '../dialog/error-dialog/error-dialog.component';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {ViewService} from '../view.service';
import {GroupService} from '../group/group.service';
import {SolveService} from '../solve/solve.service';
import {ValueSelectDialogComponent} from '../solve/dialog/value-select-dialog/value-select-dialog.component';
import {ActivatedRoute} from '@angular/router';

@Component({
    selector: 'app-multiple-relation-view',
    templateUrl: './multiple-relation-view.component.html',
    styleUrls: ['./multiple-relation-view.component.css']
})
export class MultipleRelationViewComponent implements OnInit, OnDestroy {


    title = 'Mehrfache Beziehungen';
    groups: LogikGroup[];
    lines: LogikViewLine[];
    flexPercent: number;
    selectedLine: LogikViewLine;

    key: string;
    private sub: any;

    constructor(private groupService: GroupService, private viewService: ViewService, private solveService: SolveService,
                public dialog: MatDialog, private router: Router, private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
            this.flexPercent = Math.floor(94 / this.groups.length);
            console.log(this.flexPercent);
        });
        this.sub = this.route.params.subscribe(params => {
            const key = 'problem';
            this.key = params[key];
            this.loadView(this.key);

            // In a real app: dispatch action to load the details here.
        });
    }

    ngOnDestroy() {
        this.sub.unsubscribe();
    }

    loadView(problemKey: string) {
        this.viewService.loadMultipleRelationView(problemKey).subscribe((data: LogikViewLine[]) => {
            this.lines = data;
            console.log(this.lines);
        });
    }

    isValueLine(blockId: LogikViewLine) {
        return blockId.type === 'LINE';
    }

    counter(i: number) {
        return new Array(i);
    }

    printViewValue(line: LogikViewLine, index: number): string {
        if (!line.view || line.view.length <= index){
            return '';
        }

        const value = line.view[index];
        return (!value) ? '' : value.text;
    }

    editSelection(line: LogikViewLine, index: number): void {
        const dialogRef = this.dialog.open(ValueSelectDialogComponent, {
            width: '400px',
            data: {
                group: this.groups[index],
                selection: line.view[index].selectableValues,
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.solveService.updateSelection(this.key, line.lineId, index, result).subscribe(_ => {
                    this.loadView(this.key);
                });
            }
        });
    }

    selectLine(event, line: LogikViewLine) {
        console.log(event);
        if (event.checked) {
            this.selectedLine = line;
        }        else {
            this.selectedLine = null;
        }
    }

    openErrorDialog(message: string) {
        const dialogRef = this.dialog.open(ErrorDialogComponent, {
            width: '400px',
            data: message
        });
    }


}
