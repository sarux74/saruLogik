import {Component, OnInit, OnDestroy} from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from '../model/logik-view';
import {LogikViewLine} from '../model/logik-view-line';
import {MatDialog} from '@angular/material/dialog';
import {EditService} from './edit.service';
import {GroupService} from '../group/group.service';
import {NewBlockDialogComponent} from './dialog/new-block-dialog/new-block-dialog.component';
import {NewRelationDialogComponent} from './dialog/new-relation-dialog/new-relation-dialog.component';
import {ErrorDialogComponent} from '../dialog/error-dialog/error-dialog.component';
import {Router} from '@angular/router';
import {ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs';
import {SolveService} from '../solve/solve.service';
import {ValueSelectDialogComponent} from '../solve/dialog/value-select-dialog/value-select-dialog.component';

@Component({
    selector: 'app-edit',
    templateUrl: './edit.component.html',
    styleUrls: ['./edit.component.css', '../common.css']
})
export class EditComponent implements OnInit, OnDestroy {

    title = 'Logik-Löser';
    groups: LogikGroup[];
    origLines: LogikViewLine[];
    lines: LogikViewLine[];
    flexPercent: number;
    public selectedLines: LogikViewLine[] = [];
    markedLines: number[] = [];

    key: string;
    private sub: Subscription;

    views = {
        edit: 'Rätsel bearbeiten',
        solve: 'Löser',
        compact: 'Kompakte Ansicht',
        group: 'Gruppen-Ansicht',
        block: 'Blockvergleich',
        multiple: 'Mehrfach-Beziehungen',
        positioner: 'Positionieren'
    };
    selected_view: string;


    constructor(private groupService: GroupService, private editService: EditService, private solveService: SolveService, public dialog: MatDialog, private router: Router,
        private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.groupService.current().subscribe({
            next: this.handleGroups.bind(this)
        });
        this.sub = this.route.params.subscribe({
            next: this.handleProblemKey.bind(this)
            // In a real app: dispatch action to load the details here.
        });
    }

    handleGroups(data: any) {
        this.groups = data;
        this.flexPercent = Math.floor(94 / this.groups.length);
        console.log(this.flexPercent);
        console.log(this.groups);
    }

    handleProblemKey(params: any) {
        const key = 'problem';
        this.key = params[key];
        this.loadView(this.key);
    }

    ngOnDestroy() {
        this.sub.unsubscribe();
    }

    loadView(problemKey: string) {
        this.editService.load(problemKey).subscribe({
            next: this.handleLoadedView.bind(this)
        });
    }

    handleLoadedView(data: LogikView) {
        console.log(data);
        this.origLines = data.lines;
        this.selectedLines = [];
        this.lines = data.lines;
        console.log(this.lines);
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
                this.solveService.updateSelection(this.key, line.lineId, index, result).subscribe(resResult => {
                    this.loadView(this.key);
                });
            }
        });
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

    newBlock(): void {
        const dialogRef = this.dialog.open(NewBlockDialogComponent, {
            width: '400px'
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.editService.newBlock(this.key, result).subscribe(resResult => {
                    this.loadView(this.key);
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
                // Block id und line id?
                this.selectedLines = this.selectedLines.filter(obj => obj !== line);
                console.log('Nachher:');
                console.log(this.selectedLines);
            }
        }, 0);
    }

    isSelected(line: LogikViewLine) {
        return this.selectedLines.indexOf(line) > 0;
    }

    newLine(blockId: number): void {
        const dialogRef = this.dialog.open(NewRelationDialogComponent, {
            width: '400px',
            data: {blockId, groups: this.groups}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.editService.newRelation(this.key, result).subscribe(resResult => {
                    this.loadView(this.key);
                });
            }
        });
    }

    flipBlock(blockId: number) {
        this.editService.flipBlock(this.key, blockId).subscribe(result => {
            this.loadView(this.key);
        });
    }

    isCase() {
        return +this.key > 0;
    }

    newCase() {
        if (this.selectedLines.length === 2) {
            this.editService.newCase(this.key, this.selectedLines[0].lineId, this.selectedLines[1].lineId).subscribe(result => {
                console.log(result);
                const url = this.router.serializeUrl(
                    this.router.createUrlTree(['/solve', {problem: result}])
                );

                window.open(url, '_blank');
            });
        }
    }

    closeCase() {
        this.editService.closeCase(this.key).subscribe(result => {
            window.close();
        });
    }

    mergeMarkedLines(newLines: number[], exceptedLine: number) {
        const mergedLines = [];
        for (const lineId of this.markedLines) {
            if (lineId !== exceptedLine) {
                mergedLines.push(lineId);
            }
        }
        for (const lineId of newLines) {
            let found = false;
            for (const checkLineId of mergedLines) {
                if (checkLineId === lineId) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mergedLines.push(lineId);
            }
        }
        mergedLines.sort();

        this.markedLines = mergedLines;
    }

    openErrorDialog(message: string) {
        const dialogRef = this.dialog.open(ErrorDialogComponent, {
            width: '400px',
            data: message
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
                    this.loadView(this.key);
                });
            }
        });
    }

    changeView() {
        if (this.selected_view) {
            const url = this.router.serializeUrl(
                this.router.createUrlTree(['/view/' + this.selected_view, {problem: this.key}])
            );

            window.open(url, '_blank');
        }
    }

    blockUp() {
        if (this.selectedLines.length !== 1) {
            return;
        }

        this.editService.blockUp(this.key, this.selectedLines[0]).subscribe(result => {
            this.loadView(this.key);
        }, error => {
            console.log(error);
            this.openErrorDialog(error);
        }
        );
    }

    blockDown() {
        if (this.selectedLines.length !== 1) {
            return;
        }

        this.editService.blockDown(this.key, this.selectedLines[0]).subscribe(result => {
            this.loadView(this.key);
        }, error => {
            console.log(error);
            this.openErrorDialog(error);
        }
        );
    }

    refreshView() {
        this.loadView(this.key);
    }
}

