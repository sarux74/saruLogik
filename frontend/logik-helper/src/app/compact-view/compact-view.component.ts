import { Component, OnInit } from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from '../solve/model/logik-view';
import {LogikViewLine} from '../solve/model/logik-view-line';
import {ValueView} from '../solve/model/value-view';
import {MatDialog} from '@angular/material/dialog';
import { SolveService } from '../solve/solve.service';
import { GroupService } from '../group/group.service';

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

    constructor(private groupService: GroupService,private solveService: SolveService, public dialog: MatDialog) { }

    ngOnInit(): void {
      this.groupService.current().subscribe(data => {
        this.groups = data;
        this.flexPercent = Math.floor(98 / this.groups.length);
      });
      this.loadView(-1);
    }

    loadView(index: number) {
      this.solveService.load().subscribe(data => {
        this.lines = this.filter(data, index);
       });
    }

    counter(i: number) {
      return new Array(i);
    }

    printViewValue(line: LogikViewLine,  index: number) : string {
      if (!line.view || line.view.length <=  index)
        return '';

      const value = line.view[index];
      if(!value) return '';
      else return value.text;
    }

    filter(view: LogikView, index: number): LogikViewLine[] {
      const compact = [];
      const indices = [];
      for(let line of view.lines) {
        if(line.type === 'LINE' || line.type === 'SUBLINE') {
          if(indices.indexOf(line.lineId) == -1) {
            compact.push(line);
            indices.push(line.lineId);
          }
        }
      }

      if(index == -1)
        compact.sort((a,b) => (a.lineId > b.lineId) ? 1 : ((b.lineId > a.lineId) ? -1 : 0));
      else
        compact.sort((a, b) => {
          const sizeDiff = a.view[index].selectableValues.length - b.view[index].selectableValues.length;
          if(sizeDiff != 0) return sizeDiff;
          else return a.view[index].text.localeCompare(b.view[index].text);
        });
      return compact;
    }
}
