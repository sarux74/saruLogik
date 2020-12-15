import { Component, OnInit } from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import { Inject } from '@angular/core';

@Component({
  selector: 'app-show-changes',
  templateUrl: './show-changes.component.html',
  styleUrls: ['./show-changes.component.css']
})
export class ShowChangesDialog implements OnInit {

  constructor(public dialogRef: MatDialogRef<ShowChangesDialog>,
                    @Inject(MAT_DIALOG_DATA) public data: any) { }


  ngOnInit(): void {
  }

}
