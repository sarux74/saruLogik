import { Component, OnInit } from '@angular/core';
import {LogikGroup} from '../group/model/logik-group';
import {LogikView} from '../solve/model/logik-view';
import {LogikViewLine} from '../solve/model/logik-view-line';
import {ValueView} from '../solve/model/value-view';
import {MatDialog} from '@angular/material/dialog';
import { GroupService } from '../group/group.service';
import { DetektorService } from './detektor.service';
import { ValueSelectDialog} from '../solve/dialog/value-select-dialog/value-select-dialog.component';
import { NewBlockDialog} from '../solve/dialog/new-block-dialog/new-block-dialog.component';
import { NewRelationDialog} from '../solve/dialog/new-relation-dialog/new-relation-dialog.component';
import { ShowChangesDialog} from '../solve/dialog/show-changes/show-changes.component';
import { ErrorDialog} from '../dialog/error-dialog/error-dialog.component';
import { Router } from '@angular/router';
import {CombinationView} from '../combination-view/combination-view';


@Component({
  selector: 'app-detektor',
  templateUrl: './detektor.component.html',
  styleUrls: ['./detektor.component.css']
})
export class DetektorComponent implements OnInit {

  title = 'Lügendetektor';
  groups: LogikGroup[];
  origLines: LogikViewLine[];
  lines: LogikViewLine[];
  showEditButtons = false;
  flexPercent: number;
  public selectedLines: LogikViewLine[] = [];
  markedLines: number[] = [];
  constructor(private groupService: GroupService,private detektorService: DetektorService, public dialog: MatDialog, private router: Router) { }

  ngOnInit(): void {
    this.groupService.current().subscribe(data => {
      this.groups = data;
      this.flexPercent = Math.floor(94 / this.groups.length);
      console.log(this.flexPercent);
    });
    this.loadView();
  }

  loadView() {
    this.detektorService.load().subscribe((data: LogikView) => {
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
        this.detektorService.updateSelection(line.lineId, index, result).subscribe(result => {
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
          this.detektorService.newBlock(result).subscribe(result => {
            console.log(result);
            this.loadView();
          })
        });
    }

    newBlockPair() : void {
          const dialogRef = this.dialog.open(NewBlockDialog, {
            width: '400px'
          });

          dialogRef.afterClosed().subscribe(result => {
            if(result)
              this.detektorService.newBlockPair(result).subscribe(result => {
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
          this.detektorService.newRelation(result).subscribe(result => {
            console.log(result);
            this.loadView();
          })
      });
  }

 flipBlock(blockId: number) {
   this.detektorService.flipBlock(blockId).subscribe(result => {
       this.loadView();
    })
 }

 showBlock(blockId: number) {
    this.detektorService.showBlock(blockId).subscribe(result => {
      this.loadView();
    })
 }

 hideBlock(blockId: number) {
    this.detektorService.hideBlock(blockId).subscribe(result => {
      this.loadView();
    })
  }

  findNegatives() {
    console.log(this.selectedLines);
    if(this.selectedLines.length != 1)
      return;
    this.detektorService.findNegatives(this.selectedLines[0].lineId).subscribe(result => {
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
        this.openErrorDialog(error);
      });
  }

  openErrorDialog(message: string) {
     const dialogRef = this.dialog.open(ErrorDialog, {
       width: '400px',
       data: message
     });
   }

  editRelation(line: LogikViewLine) {
    if(line.type !== 'RELATION_UPPER')
      return;

     const blockId = line.blockId;
     let leftLineId = line.lineId;
     let rightLineId;
     const index = this.lines.indexOf(line);
     if(index) {
      const nextLine = this.lines[index + 1];
      rightLineId = nextLine.lineId;
     }

    const dialogRef = this.dialog.open(NewRelationDialog, {
      width: '400px',
       data: {blockId:blockId, leftLineId: leftLineId, rightLineId: rightLineId, groups: this.groups}
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result)
        this.detektorService.newRelation(result).subscribe(result => {
            console.log(result);
            this.loadView();
        })
    });
  }

  openCombinationView() {
    const url = this.router.serializeUrl(
        this.router.createUrlTree(['/view/combination'])
      );

      window.open(url, '_blank');
  }

  openCombinationSolver() {
  console.log(this.selectedLines);
    if(this.selectedLines.length < 2)
        return;
     const ids = [];
    for(const line of this.selectedLines)
      ids.push(line.blockId);

      this.detektorService.prepare(ids).subscribe(result => {

    const url = this.router.serializeUrl(
        this.router.createUrlTree(['/solve', {'problem' : 0}])
      );

      window.open(url, '_blank');
    }, error => {
              console.log(error);
            this.openErrorDialog(error);
            }
          );
  }

  excludeCombination() {
    console.log(this.selectedLines);
        if(this.selectedLines.length < 2)
            return;
         const ids = [];
        for(const line of this.selectedLines)
          ids.push(line.blockId);

          this.detektorService.exclude(ids).subscribe(result => {
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
                    this.openErrorDialog(error);
                  });
   }

}

