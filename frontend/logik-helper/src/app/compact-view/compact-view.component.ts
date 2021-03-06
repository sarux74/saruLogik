import {Component, OnInit} from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from '../model/logik-view';
import {LogikViewLine} from '../model/logik-view-line';
import {MatDialog} from '@angular/material/dialog';
import {SolveService} from '../solve/solve.service';
import {GroupService} from '../group/group.service';
import {ActivatedRoute} from '@angular/router';

@Component({
    selector: 'app-compact-view',
    templateUrl: './compact-view.component.html',
    styleUrls: ['./compact-view.component.css']
})
export class CompactViewComponent implements OnInit {

    title = 'Logik-Löser';
    groups: LogikGroup[];
    lines: LogikViewLine[];
    flexPercent: number;

    key: string;
    private sub: any;

    constructor(private groupService: GroupService, private solveService: SolveService, public dialog: MatDialog,
                private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.groupService.current().subscribe(data => {
            this.groups = data;
            this.flexPercent = Math.floor(98 / this.groups.length);
        });
        this.sub = this.route.params.subscribe(params => {
            const key = 'problem';
            this.key = params[key];
            this.loadView(-1);
        });

    }

    loadView(index: number) {
        this.solveService.load(this.key).subscribe(data => {
            this.lines = this.filter(data, index);
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

    filter(view: LogikView, index: number): LogikViewLine[] {
        const compact = [];
        const indices = [];
        for (const line of view.lines) {
            if (line.type === 'LINE' || line.type === 'SUBLINE') {
                if (indices.indexOf(line.lineId) === -1) {
                    compact.push(line);
                    indices.push(line.lineId);
                }
            }
        }

        if (index === -1) {
            compact.sort((a, b) => (a.lineId > b.lineId) ? 1 : ((b.lineId > a.lineId) ? -1 : 0));
        } else {
            compact.sort((a, b) => {
                const sizeDiff = a.view[index].selectableValues.length - b.view[index].selectableValues.length;
                return (sizeDiff !== 0) ? sizeDiff : a.view[index].text.localeCompare(b.view[index].text);
            });
        }
        return compact;
    }
}
