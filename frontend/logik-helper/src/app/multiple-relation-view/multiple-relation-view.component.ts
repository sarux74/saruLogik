import { Component, OnInit } from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from '../solve/model/logik-view';
import {LogikViewLine} from '../solve/model/logik-view-line';
import { ErrorDialog} from '../dialog/error-dialog/error-dialog.component';
import { Router } from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {ViewService} from '../view.service';
import { GroupService } from '../group/group.service';
import { SolveService } from '../solve/solve.service';
import { ValueSelectDialog} from '../solve/dialog/value-select-dialog/value-select-dialog.component';

@Component({
  selector: 'app-multiple-relation-view',
  templateUrl: './multiple-relation-view.component.html',
  styleUrls: ['./multiple-relation-view.component.css']
})
export class MultipleRelationViewComponent implements OnInit {


  title = 'Mehrfache Beziehungen';
  groups: LogikGroup[];
  lines: LogikViewLine[];
  flexPercent: number;
  selectedLine: LogikViewLine;
  constructor(private groupService: GroupService,private viewService: ViewService,private solveService: SolveService, public dialog: MatDialog, private router: Router) { }

  ngOnInit(): void {
    this.groupService.current().subscribe(data => {
      this.groups = data;
      this.flexPercent = Math.floor(94 / this.groups.length);
      console.log(this.flexPercent);
    });
    this.loadView();
  }

  loadView() {
    this.viewService.loadMultipleRelationView().subscribe((data: LogikViewLine[]) => {
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

  printViewValue(line: LogikViewLine,  index: number) : string {
    if (!line.view || line.view.length <=  index)
      return '';

    const value = line.view[index];
    if(!value) return '';
    else return value.text;
  }

  editSelection(line: LogikViewLine,  index: number) : void {
    const dialogRef = this.dialog.open(ValueSelectDialog, {
      width: '400px',
      data: {
        group: this.groups[index],
        selection: line.view[index].selectableValues,
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result)
        this.solveService.updateSelection(line.lineId, index, result).subscribe(result => {
          console.log(result);
          this.loadView();
        })
      });
  }

  selectLine(event, line: LogikViewLine) {
    console.log(event);
    if(event.checked)
     this.selectedLine = line;
    else
      this.selectedLine = null;
  }

  openErrorDialog(message: string) {
     const dialogRef = this.dialog.open(ErrorDialog, {
       width: '400px',
       data: message
     });
   }


}
