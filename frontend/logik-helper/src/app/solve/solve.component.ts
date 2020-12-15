import { Component, OnInit } from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from './model/logik-view';
import {LogikViewLine} from './model/logik-view-line';
import {ValueView} from './model/value-view';
import {MatDialog} from '@angular/material/dialog';
import { SolveService } from './solve.service';
import { GroupService } from '../group/group.service';
import { ValueSelectDialog} from './dialog/value-select-dialog/value-select-dialog.component';
import { NewBlockDialog} from './dialog/new-block-dialog/new-block-dialog.component';
import { NewRelationDialog} from './dialog/new-relation-dialog/new-relation-dialog.component';
import { ShowChangesDialog} from './dialog/show-changes/show-changes.component';
import { ErrorDialog} from '../dialog/error-dialog/error-dialog.component';
import { Router } from '@angular/router';



@Component({
  selector: 'app-solve',
  templateUrl: './solve.component.html',
  styleUrls: ['./solve.component.css']
})
export class SolveComponent implements OnInit {

  title = 'Logik-Löser';
  groups: LogikGroup[];
  origLines: LogikViewLine[];
  lines: LogikViewLine[];
  showEditButtons = false;
  flexPercent: number;
  public selectedLines: LogikViewLine[] = [];
  markedLines: number[] = [];
  constructor(private groupService: GroupService,private solveService: SolveService, public dialog: MatDialog, private router: Router) { }

  ngOnInit(): void {
    this.groupService.current().subscribe(data => {
      this.groups = data;
      this.flexPercent = Math.floor(94 / this.groups.length);
      console.log(this.flexPercent);
    });
    this.loadView();
  }

  loadView() {
    this.solveService.load().subscribe((data: LogikView) => {
      this.origLines = data.lines;
      this.selectedLines = [];
      this.toggleEdit();
      console.log(this.lines);
     });
  }

  toggleEdit() {
    if(this.showEditButtons)
      this.lines = this.origLines;
    else {
      const copyLines = [];
      for(let line of this.origLines) {
        if(line.type !== 'ADD_LINE' && line.type !== 'ADD_BLOCK') {
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

  newBlock() : void {
      const dialogRef = this.dialog.open(NewBlockDialog, {
        width: '400px'
      });

      dialogRef.afterClosed().subscribe(result => {
        if(result)
          this.solveService.newBlock(result).subscribe(result => {
            console.log(result);
            this.loadView();
          })
        });
    }

  selectLine(event, line: LogikViewLine) {
  setTimeout(() => {
    console.log(event);
    if(event.checked)
     this.selectedLines.push(line);
    else {
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

  newLine(blockId: number):void {
      const dialogRef = this.dialog.open(NewRelationDialog, {
        width: '400px',
         data: {blockId:blockId, groups: this.groups}
      });

      dialogRef.afterClosed().subscribe(result => {
        if(result)
          this.solveService.newRelation(result).subscribe(result => {
            console.log(result);
            this.loadView();
          })
      });
  }

 flipBlock(blockId: number) {
   this.solveService.flipBlock(blockId).subscribe(result => {
       this.loadView();
    })
 }

 showBlock(blockId: number) {
    this.solveService.showBlock(blockId).subscribe(result => {
      this.loadView();
    })
 }

 hideBlock(blockId: number) {
    this.solveService.hideBlock(blockId).subscribe(result => {
      this.loadView();
    })
  }

  findNegatives() {
    console.log(this.selectedLines);
    if(this.selectedLines.length != 1)
      return;
    this.solveService.findNegatives(this.selectedLines[0].lineId).subscribe(result => {
      if(result) {
        this.markedLines = result.changedLines;
        const dialogRef = this.dialog.open(ShowChangesDialog, {
              width: '400px',
               data: result
            });

            dialogRef.afterClosed().subscribe(result => {
                    this.loadView();
             });
        }
      }, error => {
        console.log(error);
        this.openErrorDialog(error.error.message);
        this.loadView();
      });
  }

  findPositives() {
    if(this.selectedLines.length == 0)
      return;

    const ids = [];
    for(const line of this.selectedLines)
      ids.push(line.lineId);

    this.solveService.findPositives(ids).subscribe(result => {
      console.log(result);
      if(result) {
        this.markedLines = result.changedLines;
        const dialogRef = this.dialog.open(ShowChangesDialog, {
                width: '400px',
                 data: result
              });

              dialogRef.afterClosed().subscribe(result => {
                      this.loadView();
               });
          }
      }, error => {
             console.log(error);
             this.openErrorDialog(error.error.message);
           }
       );
  }

  openErrorDialog(message: string) {
     const dialogRef = this.dialog.open(ErrorDialog, {
       width: '400px',
       data: message
     });
   }

  editRelation(line: LogikViewLine, groupIndex: number) {
    if(line.type !== 'RELATION_UPPER')
      return;

     const blockId = line.blockId;
     let leftLineId = line.lineId;
     let rightLineId;
     const index = this.lines.indexOf(line);
     if(index) {
      const nextLine = this.lines[index + 1];
      rightLineId = nextLine.rightLineId;
     }

console.log(leftLineId);
console.log(rightLineId);
    const dialogRef = this.dialog.open(NewRelationDialog, {
      width: '400px',
       data: {blockId:blockId, leftLineId: leftLineId, rightLineId: rightLineId, groups: this.groups, groupIndex: groupIndex}
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result)
        this.solveService.newRelation(result).subscribe(result => {
            console.log(result);
            this.loadView();
        })
    });
  }

  openCompactView() {
    const url = this.router.serializeUrl(
        this.router.createUrlTree(['/view/compact'])
      );

      window.open(url, '_blank');
  }

  openGroupView() {
      const url = this.router.serializeUrl(
          this.router.createUrlTree(['/view/group'])
        );

        window.open(url, '_blank');
    }

    openBlockCompareView() {
      const url = this.router.serializeUrl(
          this.router.createUrlTree(['/view/block'])
        );

        window.open(url, '_blank');
    }

    openMultipleRelationView() {
      const url = this.router.serializeUrl(
          this.router.createUrlTree(['/view/multiple'])
        );

        window.open(url, '_blank');
    }

    blockUp() {
     if(this.selectedLines.length != 1)
          return;

      this.solveService.blockUp(this.selectedLines[0]).subscribe(result => {
        this.loadView();
      }, error => {
         console.log(error);
       this.openErrorDialog(error);
       }
     );
    }

    blockDown() {
      if(this.selectedLines.length != 1)
        return;

      this.solveService.blockDown(this.selectedLines[0]).subscribe(result => {
        this.loadView();
      }, error => {
         console.log(error);
         this.openErrorDialog(error);
       }
     );
    }
}

