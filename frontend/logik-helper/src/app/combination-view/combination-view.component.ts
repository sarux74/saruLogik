import {Component, OnInit} from '@angular/core';
import {DetektorService} from '../detektor/detektor.service';
import {CombinationView} from './combination-view';

@Component({
    selector: 'app-combination-view',
    templateUrl: './combination-view.component.html',
    styleUrls: ['./combination-view.component.css']
})
export class CombinationViewComponent implements OnInit {

    combinationView: CombinationView;

    constructor(private detektorService: DetektorService) {}

    ngOnInit(): void {
        this.detektorService.combinationView().subscribe(data => {
            console.log(data);
            this.combinationView = data;
        });
    }


    counter(i: number) {
        return new Array(i);
    }

}
