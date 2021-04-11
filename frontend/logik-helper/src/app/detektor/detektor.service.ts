import {Injectable} from '@angular/core';
import {LogikView} from '../model/logik-view';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ChangeResult} from '../solve/change-result';
import {LogikViewLine} from '../model/logik-view-line';
import {IdNamePair} from '../block-compare-view/id-name-pair';
import {CombinationView} from '../combination-view/combination-view';


@Injectable({
    providedIn: 'root'
})
export class DetektorService {

    private solveUrl: string;

    constructor(private http: HttpClient) {
        this.solveUrl = 'http://localhost:8080/';
    }

    public combinationView(problemKey: string): Observable<CombinationView> {
        return this.http.get<CombinationView>(this.buildProblemUrl(problemKey, '/detektor/view/combination'));
    }

    public prepare(problemKey: string, lineIds: number[]): Observable<boolean> {
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/detektor/prepare'), lineIds);
    }

    public exclude(problemKey: string, lineIds: number[]): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.buildProblemUrl(problemKey, '/detektor/exclude'), lineIds);
    }
    
     public buildProblemUrl(problemKey: string, path: string): string {
        const url = this.solveUrl + '/problems/' + problemKey + path
        console.log(url);
        return url;
    }
}
