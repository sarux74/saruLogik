import { Component, OnInit } from '@angular/core';
import {SolveService} from '../solve/solve.service';
import {GroupService} from '../group/group.service';
import {LogikGroup} from '../group/model/logik-group';
import {LogikViewLine} from '../solve/model/logik-view-line';

@Component({
  selector: 'app-group-view',
  templateUrl: './group-view.component.html',
  styleUrls: ['./group-view.component.css']
})
export class GroupViewComponent implements OnInit {

  groupId: number;
  flexPercent: number;
  groups: LogikGroup[];
  sortedGroups: LogikGroup[];
  lines: LogikViewLine[];
  selectedLines: boolean[] = [];

  constructor(private solveService: SolveService, private groupService: GroupService) { }

  ngOnInit(): void {
    this.groupService.current().subscribe(data => {
        this.groups = data;
        this.flexPercent = Math.floor(98 / this.groups.length);
        console.log(this.flexPercent);
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

  loadGroupView() {
    // Resort groups
    const prepareSortedGroups = [];
    for(let group of this.groups) {
      if(group.index == this.groupId) {
        prepareSortedGroups.push(group);
        break;
      }
    }

    for(let group of this.groups) {
      if(group.index != this.groupId) {
        prepareSortedGroups.push(group);
      }
    }
    this.sortedGroups = prepareSortedGroups;
    this.solveService.loadGroupView(this.groupId).subscribe(res => {
      console.log(res);
      this.lines = res;
      const newSelectedLines = [];
      for(let i =0; i<this.lines.length; i++) {
        newSelectedLines.push(false);
      }
      this.selectedLines = newSelectedLines;
    });
  }

  applyBlockingCandidates() {
    console.log(this.selectedLines);
    const candidates = [];
    for(let i=0; i<this.selectedLines.length; i++) {
      if(this.selectedLines[i] === true)
      candidates.push(i);
    }
    console.log(candidates);
    this.solveService.applyBlockingCandidates(this.groupId, candidates).subscribe(res => {if(res) this.loadGroupView(); });
  }
}
