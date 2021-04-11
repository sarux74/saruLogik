import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PositionerService {
    private positionerUrl: string;

    constructor(private http: HttpClient) {
        this.positionerUrl = 'http://localhost:8080/positioner/';
    }

    public initPositionerView(problemKey: string, positionGroupId: number, positionedGroupId: number): Observable<boolean> {
        const data = {positionGroupId, positionedGroupId};
        return this.http.put<boolean>(this.buildPositionerUrl(problemKey, '/init'), data);
    }

    // TODO: Datatype of result
    public loadPositionerView(problemKey: string): Observable<any> {
        return this.http.get<any>(this.buildPositionerUrl(problemKey, '/view'));
    }

    public updateSelection(problemKey: string, lineId: number, groupId: number, selection: number[]) {
        // TODO: Names from LogikView
        const data = {lineId, groupId, selection};
        return this.http.put<boolean>(this.buildPositionerUrl(problemKey, '/selection'), data);
    }

    public addLine(problemKey: string, lineId: number, direction: number) {
        const data = {lineId, direction};
        return this.http.put<boolean>(this.buildPositionerUrl(problemKey, '/add'), data);
    }

    public removeLine(problemKey: string, lineId: number) {
        const data = {lineId};
        return this.http.put<boolean>(this.buildPositionerUrl(problemKey, '/remove'), data);
    }

    public overtake(problemKey: string) {
        return this.http.put<boolean>(this.buildPositionerUrl(problemKey, '/overtake'), {});
    }

    public buildPositionerUrl(problemKey: string, path: string): string {
        return this.positionerUrl + 'problems/' + problemKey + path;
    }

}
