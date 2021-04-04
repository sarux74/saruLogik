import {Component, OnInit, OnDestroy} from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from '../model/logik-view';
import {LogikViewLine} from '../model/logik-view-line';
import {MatDialog} from '@angular/material/dialog';
import {SolveService} from './solve.service';
import {GroupService} from '../group/group.service';
import {ValueSelectDialogComponent} from './dialog/value-select-dialog/value-select-dialog.component';
import {ShowChangesDialogComponent} from './dialog/show-changes/show-changes.component';
import {ErrorDialogComponent} from '../dialog/error-dialog/error-dialog.component';
import {Router} from '@angular/router';
import {ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs';

@Component({
    selector: 'app-solve',
    templateUrl: './solve.component.html',
    styleUrls: ['./solve.component.css', '../common.css']
})
export class SolveComponent implements OnInit, OnDestroy {

    title = 'Logik-Löser';
    groups: LogikGroup[];
    origLines: LogikViewLine[];
    lines: LogikViewLine[];
    showEditButtons = false;
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


    constructor(private groupService: GroupService, private solveService: SolveService, public dialog: MatDialog, private router: Router,
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
        this.solveService.load(problemKey).subscribe({
            next: this.handleLoadedView.bind(this)
        });
    }

    handleLoadedView(data: LogikView) {
        this.origLines = data.lines;
        this.selectedLines = [];
        this.toggleEdit();
        console.log(this.lines);
    }

    toggleEdit() {
        if (this.showEditButtons) {
            this.lines = this.origLines;
        } else {
            const copyLines = [];
            for (const line of this.origLines) {
                if (line.type !== 'ADD_LINE' && line.type !== 'ADD_BLOCK') {
                    copyLines.push(line);
                }
            }
            this.lines = copyLines;
        }
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
                this.solveService.updateSelection(this.key, line.lineId, index, result).subscribe(resResult => {
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

    flipBlock(blockId: number) {
        this.solveService.flipBlock(this.key, blockId).subscribe(result => {
            this.loadView(this.key);
        });
    }

    showBlock(blockId: number) {
        this.solveService.showBlock(this.key, blockId).subscribe(result => {
            this.loadView(this.key);
        });
    }

    hideBlock(blockId: number) {
        this.solveService.hideBlock(this.key, blockId).subscribe(result => {
            this.loadView(this.key);
        });
    }

    isCase() {
        return +this.key > 0;
    }

    newCase() {
        if (this.selectedLines.length === 2) {
            this.solveService.newCase(this.key, this.selectedLines[0].lineId, this.selectedLines[1].lineId).subscribe(result => {
                console.log(result);
                const url = this.router.serializeUrl(
                    this.router.createUrlTree(['/solve', {problem: result}])
                );

                window.open(url, '_blank');
            });
        }
    }

    closeCase() {
        this.solveService.closeCase(this.key).subscribe(result => {
            window.close();
        });
    }

    findNegatives() {
        console.log(this.selectedLines);
        if (this.selectedLines.length !== 1) {
            return;
        }

        this.solveService.findNegatives(this.key, this.selectedLines[0].lineId).subscribe(result => {
            if (result) {
                this.mergeMarkedLines(result.changedLines, this.selectedLines[0].lineId);
                // this.markedLines = result.changedLines;
                const dialogRef = this.dialog.open(ShowChangesDialogComponent, {
                    width: '400px',
                    data: result
                });

                dialogRef.afterClosed().subscribe(_ => {
                    this.loadView(this.key);
                });
            }
        }, error => {
            console.log(error);
            this.openErrorDialog(error.error.message);
            this.loadView(this.key);
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

    findPositives() {
        if (this.selectedLines.length === 0) {
            return;
        }

        const ids = [];
        for (const line of this.selectedLines) {
            ids.push(line.lineId);
        }

        this.solveService.findPositives(this.key, ids).subscribe(result => {
            console.log(result);
            if (result) {
                this.mergeMarkedLines(result.changedLines, this.selectedLines[0].lineId);
                const dialogRef = this.dialog.open(ShowChangesDialogComponent, {
                    width: '400px',
                    data: result
                });

                dialogRef.afterClosed().subscribe(resResult => {
                    this.loadView(this.key);
                });
            }
        }, error => {
            console.log(error);
            this.openErrorDialog(error.error.message);
        }
        );
    }

    openErrorDialog(message: string) {
        const dialogRef = this.dialog.open(ErrorDialogComponent, {
            width: '400px',
            data: message
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

    refreshView() {
        this.solveService.refresh(this.key).subscribe(result => {
            this.loadView(this.key);
        });
    }
}

