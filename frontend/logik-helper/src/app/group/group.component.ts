import {Component, OnInit} from '@angular/core';
import {GroupService} from './group.service';
import {LogikGroup} from './model/logik-group';
import {Router} from '@angular/router';
import {GroupEditDialogComponent} from './dialog/group-edit-dialog.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
    selector: 'app-group',
    templateUrl: './group.component.html',
    styleUrls: ['./group.component.css']
})
export class GroupComponent implements OnInit {

    title = 'Logik-Helper';
    groups: LogikGroup[];
    problemType = 0;
    constructor(private groupService: GroupService, public dialog: MatDialog, private router: Router) {}

    ngOnInit(): void {
        this.load();
    }

    load() {
        this.groupService.current().subscribe(data => {
            this.groups = data;
            console.log(this.groups);
        });
    }

    newProblem() {
        this.problemType = 0;
        this.groupService.newProblem().subscribe(result => this.load());
    }

    newGroup() {
        const newGroup = new LogikGroup();
        newGroup.elements = [];
        newGroup.index = this.groups.length;
        const dialogRef = this.dialog.open(GroupEditDialogComponent, {
            width: '400px',
            data: {newGroup, groups: this.groups}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.groupService.updateGroup(result).subscribe(res => this.groups.push(result));
            }
        });
    }

    editGroup(index: number) {
        const dialogRef = this.dialog.open(GroupEditDialogComponent, {
            width: '400px',
            data: {newGroup: this.groups[index], groups: this.groups}
        });

        dialogRef.afterClosed().subscribe(result => {
            // No sort, may destroy references: this.sort(result);
            if (result) {
                this.groupService.updateGroup(result).subscribe(res => console.log('Erfolg'));
            }
        });
    }

    loadProblem(event: any): void {
        this.groupService.loadProblem(event).subscribe(result => {
            this.problemType = result; this.load();
        });
    }

    startSolver() {
        if (this.problemType === 0) {
            this.groupService.initProblem().subscribe(result => {
                console.log('Problem initialisiert!');
                this.problemType = 1;
                this.openSolver();
            });
        } else {
            this.openSolver();
        }
    }

    openSolver() {
        const url = this.router.serializeUrl(
            this.router.createUrlTree(['/solve', {problem: 0}])
        );

        window.open(url, '_blank');
    }

    startDetektor() {
        if (this.problemType === 0) {
            this.groupService.initDetektor().subscribe(result => {
                console.log('Detektor initialisiert!');
                this.problemType = 2;
                this.openDetektor();
            });
        } else {
            this.openDetektor();
        }
    }

    openDetektor() {
        const url = this.router.serializeUrl(
            this.router.createUrlTree(['/detektor'])
        );

        window.open(url, '_blank');
    }
}

