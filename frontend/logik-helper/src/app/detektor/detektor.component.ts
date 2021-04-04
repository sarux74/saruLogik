import {Component, OnInit} from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from '../model/logik-view';
import {LogikViewLine} from '../model/logik-view-line';
import {MatDialog} from '@angular/material/dialog';
import {GroupService} from '../group/group.service';
import {DetektorService} from './detektor.service';
import {ValueSelectDialogComponent} from '../solve/dialog/value-select-dialog/value-select-dialog.component';
import {ShowChangesDialogComponent} from '../solve/dialog/show-changes/show-changes.component';
import {ErrorDialogComponent} from '../dialog/error-dialog/error-dialog.component';
import {ActivatedRoute, Router} from '@angular/router';
import {EditService} from '../edit/edit.service';
import {NewRelationDialogComponent} from '../edit/dialog/new-relation-dialog/new-relation-dialog.component';
import {Subscription} from 'rxjs';
import {SolveService} from '../solve/solve.service';

@Component({
    selector: 'app-detektor',
    templateUrl: './detektor.component.html',
    styleUrls: ['./detektor.component.css', '../common.css']
})
export class DetektorComponent implements OnInit {

    title = 'Lügendetektor';
    groups: LogikGroup[];
    origLines: LogikViewLine[];
    lines: LogikViewLine[];
    flexPercent: number;
    public selectedLines: LogikViewLine[] = [];
    markedLines: number[] = [];
    key: string;
    private sub: Subscription;

    constructor(private groupService: GroupService, private detektorService: DetektorService, private editService: EditService, private solveService: SolveService, public dialog: MatDialog,
        private router: Router, private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
            this.flexPercent = Math.floor(94 / this.groups.length);
        });
        this.sub = this.route.params.subscribe({
            next: this.handleProblemKey.bind(this)
            // In a real app: dispatch action to load the details here.
        });
    }

    handleProblemKey(params: any) {
        const key = 'problem';
        this.key = params[key];
        if (!this.key)
            this.key = '0';
        this.loadView();
    }

    loadView() {
        this.editService.load(this.key).subscribe((data: LogikView) => {
            this.origLines = data.lines;
            this.selectedLines = []; 
            this.lines = this.origLines;           
        });
    }

    showHideBlockButton(blockId: LogikViewLine) {
        return blockId.hideSub === false && blockId.type === 'BLOCK';
    }

    showShowBlockButton(blockId: LogikViewLine) {
        return blockId.hideSub === true && blockId.type === 'BLOCK';
    }

    isValueLine(blockId: LogikViewLine) {
        return blockId.type === 'LINE';
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
                this.solveService.updateSelection(this.key, line.lineId, index, result).subscribe(() => {
                    this.loadView();
                });
            }
        });
    }

    selectLine(event, line: LogikViewLine) {
        setTimeout(() => {
            console.log(event);
            if (event.checked) {
                this.selectedLines.push(line);
            } else {
                console.log('Vorher:');
                console.log(this.selectedLines);
                this.selectedLines = this.selectedLines.filter(obj => obj !== line);
                console.log('Nachher:');
                console.log(this.selectedLines);
            }
        }, 0);
    }

    isSelected(line: LogikViewLine) {
        return this.selectedLines.indexOf(line) > 0;
    }

    flipBlock(blockId: number) {
        this.editService.flipBlock(this.key, blockId).subscribe(result => {
            this.loadView();
        });
    }

    showBlock(blockId: number) {
        this.solveService.showBlock(this.key, blockId).subscribe(result => {
            this.loadView();
        });
    }

    hideBlock(blockId: number) {
        this.solveService.hideBlock(this.key, blockId).subscribe(result => {
            this.loadView();
        });
    }

    findNegatives() {
        console.log(this.selectedLines);
        if (this.selectedLines.length !== 1) {
            return;
        }

        this.solveService.findNegatives(this.key, this.selectedLines[0].lineId).subscribe(result => {
            if (result) {
                this.markedLines = result.changedLines;
                const dialogRef = this.dialog.open(ShowChangesDialogComponent, {
                    width: '400px',
                    data: result
                });

                dialogRef.afterClosed().subscribe(() => {
                    this.loadView();
                });
            }
        }, error => {
            console.log(error);
            this.openErrorDialog(error);
        });
    }

    openErrorDialog(message: string) {
        const dialogRef = this.dialog.open(ErrorDialogComponent, {
            width: '400px',
            data: message
        });
    }

    openCombinationView() {
        const url = this.router.serializeUrl(
            this.router.createUrlTree(['/combination'])
        );

        window.open(url, '_blank');
    }

    openCombinationSolver() {
        if (this.selectedLines.length < 1) {
            return;
        }

        const ids = [];
        for (const line of this.selectedLines) {
            ids.push(line.blockId);
        }

        this.detektorService.prepare(this.key, ids).subscribe(result => {

            const url = this.router.serializeUrl(
                this.router.createUrlTree(['/solve', {problem: result}])
            );

            window.open(url, '_blank');
        }, error => {
            console.log(error);
            this.openErrorDialog(error);
        }
        );
    }

    excludeCombination() {
        if (this.selectedLines.length < 1) {
            return;
        }
        const ids = [];
        for (const line of this.selectedLines) {
            ids.push(line.blockId);
        }

        this.detektorService.exclude(this.key, ids).subscribe(result => {
            if (result) {
                this.markedLines = result.changedLines;
                const dialogRef = this.dialog.open(ShowChangesDialogComponent, {
                    width: '400px',
                    data: result
                });

                dialogRef.afterClosed().subscribe(() => {
                    this.loadView();
                });
            }
        }, error => {
            console.log(error);
            this.openErrorDialog(error);
        });
    }

    editRelation(line: LogikViewLine, groupIndex: number) {
        if (line.type !== 'RELATION_UPPER') {
            return;
        }

        const blockId = line.blockId;
        const leftLineId = line.lineId;
        let rightLineId;
        const index = this.lines.indexOf(line);
        if (index) {
            const nextLine = this.lines[index + 1];
            rightLineId = nextLine.rightLineId;
        }

        console.log(leftLineId);
        console.log(rightLineId);
        const dialogRef = this.dialog.open(NewRelationDialogComponent, {
            width: '400px',
            data: {blockId, leftLineId, rightLineId, groups: this.groups, groupIndex}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.editService.newRelation(this.key, result).subscribe(relResult => {
                    console.log(relResult);
                    this.loadView();
                });
            }
        });
    }

}

