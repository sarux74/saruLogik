import {Component} from '@angular/core';
import {ActivatedRoute, ActivationEnd, Router} from '@angular/router';
import {ProblemService} from './problem.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css', './common.css']
})
export class AppComponent {
    title = 'Logik-Helper';
    problemType = 0;

    views = [
        {key: 'groups', name: 'Gruppen bearbeiten'},
        {key: 'edit', name: 'Rätsel bearbeiten'},
        {key: 'solve', name: 'Löser'},
        {key: 'compact', name: 'Kompakte Ansicht'},
        {key: 'group', name: 'Gruppen-Ansicht'},
        {key: 'block', name: 'Blockvergleich'},
        {key: 'multiple', name: 'Mehrfach-Beziehungen'},
        {key: 'positioner', name: 'Positionieren'},
        {key: 'detektor', name: 'Lügendetektor'},
    ];
    selected_view: string = 'groups';
    key: string = '0';

    constructor(private problemService: ProblemService, private router: Router, private route: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.router.events.subscribe(event => {
            if (event instanceof ActivationEnd) {
                const code = event.snapshot.params['problem'];
                if (code) {
                    this.key = code;
                    let viewName = window.location.href;
                    viewName = viewName.substring(viewName.lastIndexOf('/') + 1);
                    viewName = viewName.substring(0, viewName.indexOf(';'));
                    if (viewName)
                        this.selected_view = viewName;
                }
            }
        });
    }

    newProblem() {
        this.problemType = 0;
        this.problemService.newProblem().subscribe(result => {
            this.selected_view = 'groups';
            this.changeView();
        });
    }


    loadProblem(event: any): void {
        console.log(event);
        this.problemService.loadProblem(event).subscribe(result => {
            console.log(result);
            this.problemType = result;
            this.selected_view = 'edit';
            this.changeView();
        });
    }

    changeView() {
        if (this.selected_view) {
            this.router.navigate(['/' + this.selected_view, {problem: this.key}]);
        }
    }
}
