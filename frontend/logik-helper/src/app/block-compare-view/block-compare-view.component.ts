import {Component, OnInit} from '@angular/core';
import {SolveService} from '../solve/solve.service';
import {GroupService} from '../group/group.service';
import {LogikGroup} from '../group/model/logik-group';
import {LogikViewLine} from '../solve/model/logik-view-line';
import {IdNamePair} from './id-name-pair';
import {ErrorDialog} from '../dialog/error-dialog/error-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';

@Component({
    selector: 'app-block-compare-view',
    templateUrl: './block-compare-view.component.html',
    styleUrls: ['./block-compare-view.component.css']
})
export class BlockCompareViewComponent implements OnInit {

    groups: LogikGroup[];
    lines: LogikViewLine[];
    flexPercent: number;
    selectedLine1: LogikViewLine;
    selectedLine2: LogikViewLine;
    blocks: IdNamePair[];
    blocks1: IdNamePair[];
    blocks2: IdNamePair[];
    block1LineIds: number[];
    block2LineIds: number[];
    block1Id = -1;
    block2Id = -1;
    proposed: boolean[][];

    key: string;
    private sub: any;

    constructor(private groupService: GroupService, private solveService: SolveService, public dialog: MatDialog, private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
            this.flexPercent = Math.floor(94 / this.groups.length);
            console.log(this.flexPercent);
        });

        this.sub = this.route.params.subscribe(params => {
            this.key = params['problem']; // (+) converts string 'id' to a number
            this.solveService.loadComparableBlocks(this.key).subscribe(data => {
                this.blocks = data;
                this.blocks1 = data;
                this.blocks2 = data;
            })

            // In a real app: dispatch action to load the details here.
        });

    }

    selectBlock2List() {
        const block2Prep = [];
        for (let id of this.blocks) {
            if (id.id !== this.block1Id)
                block2Prep.push(id);
        }
        this.blocks2 = block2Prep;
    }

    updateBlockCompareView(index: number) {
        console.log('Aufruf update');
        if (index === 1) {
            const block2Prep = [];
            for (let id of this.blocks) {
                if (id.id !== this.block1Id)
                    block2Prep.push(id);
            }
            this.blocks2 = block2Prep;
        } else if (index === 2) {
            const block1Prep = [];
            for (let id of this.blocks) {
                if (id.id !== this.block2Id)
                    block1Prep.push(id);
            }
            this.blocks1 = block1Prep;
        }
        this.loadBlockCompareView();
    }

    loadBlockCompareView() {
        console.log('Load mit ' + this.block1Id + ' und ' + this.block2Id);
        if (this.block1Id > -1 && this.block2Id > -1 && this.block1Id !== this.block2Id) {
            this.solveService.loadBlockCompareView(this.key, this.block1Id, this.block2Id).subscribe(data => {
                console.log(data);
                this.lines = data.viewLines;
                this.block1LineIds = data.block1LineIds;
                this.block2LineIds = data.block2LineIds;
                this.proposed = data.proposed;
            }, error => {console.log(error);});
        }
    }

    counter(i: number) {
        return new Array(i);
    }

    printViewValue(line: LogikViewLine, index: number): string {
        if (!line.view || line.view.length <= index)
            return '';

        const value = line.view[index];
        if (!value) return '';
        else return value.text;
    }

    selectLine(event, line: LogikViewLine) {
        if (line.blockId == this.block1Id)
            this.selectedLine1 = line;
        else this.selectedLine2 = line;
    }


    isValueLine(blockId: LogikViewLine) {
        return blockId.type === 'LINE';
    }

    merge() {
        this.solveService.mergeLines(this.key, this.selectedLine1, this.selectedLine2).subscribe(data => {
            this.loadBlockCompareView();
        }, error => {
            console.log(error);
            this.openErrorDialog(error);
        }
        );
    }

    openErrorDialog(message: string) {
        const dialogRef = this.dialog.open(ErrorDialog, {
            width: '400px',
            data: message
        });
    }
}
