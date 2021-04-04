import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs';
import {DetektorService} from '../detektor/detektor.service';
import {CombinationView} from './combination-view';

@Component({
    selector: 'app-combination-view',
    templateUrl: './combination-view.component.html',
    styleUrls: ['./combination-view.component.css']
})
export class CombinationViewComponent implements OnInit {

    combinationView: CombinationView;

    key: string;
    private sub: Subscription;

    constructor(private detektorService: DetektorService, private route: ActivatedRoute) {}

    ngOnInit(): void {
        this.sub = this.route.params.subscribe({
            next: this.handleProblemKey.bind(this)
            // In a real app: dispatch action to load the details here.
        });

    }

    handleProblemKey(params: any) {
        const key = 'problem';
        this.key = params[key];
        if (!this.key)
            this.key = '0';
        this.detektorService.combinationView(this.key).subscribe(data => {
            console.log(data);
            this.combinationView = data;
        });
    }

    counter(i: number) {
        return new Array(i);
    }

}
