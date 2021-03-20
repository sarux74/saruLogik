import {Injectable} from '@angular/core';
import {LogikView} from './model/logik-view';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ChangeResult} from './change-result';
import {LogikViewLine} from './model/logik-view-line';
import {IdNamePair} from '../block-compare-view/id-name-pair';

@Injectable({
    providedIn: 'root'
})
export class SolveService {

    private solveUrl: string;

    constructor(private http: HttpClient) {
        this.solveUrl = 'http://localhost:8080/solve/';
    }

    public load(problemKey: string): Observable<LogikView> {
        return this.http.get<LogikView>(this.buildProblemUrl(problemKey, '/view'));
    }

    public updateSelection(problemKey: string, lineIndex: number, groupIndex: number, selection: number[]) {
        const data = {lineId: lineIndex, groupId: groupIndex, selection};
        console.log(data);
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/selection'), data);
    }

    public newBlock(problemKey: string, result: any): Observable<boolean> {
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/block/new'), result);
    }

    public flipBlock(problemKey: string, blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/block/flip'), blockId);
    }
    public showBlock(problemKey: string, blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/block/show'), blockId);
    }
    public hideBlock(problemKey: string, blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/block/hide'), blockId);
    }

    public findNegatives(problemKey: string, lineId: number): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.buildProblemUrl(problemKey, '/negative'), lineId);
    }

    public findPositives(problemKey: string, lineIds: number[]): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.buildProblemUrl(problemKey, '/positive'), lineIds);
    }

    public newRelation(problemKey: string, result: any): Observable<boolean> {
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/relation/new'), result);
    }

    public newCase(problemKey: string, line1Id: number, line2Id: number): Observable<string> {
        const data = {line1Id, line2Id};
        return this.http.put<string>(this.buildProblemUrl(problemKey, '/case/new'), data);
    }

    public closeCase(problemKey: string): Observable<string> {
        return this.http.put<string>(this.buildProblemUrl(problemKey, '/case/close'), null);
    }

    public loadGroupView(problemKey: string, groupId: number): Observable<LogikViewLine[]> {
        const opts = {params: new HttpParams({fromString: 'groupId=' + groupId})};

        return this.http.get<LogikViewLine[]>(this.buildProblemUrl(problemKey, '/view/group'), opts);
    }

    public loadComparableBlocks(problemKey: string): Observable<IdNamePair[]> {
        return this.http.get<IdNamePair[]>(this.buildProblemUrl(problemKey, '/view/block/comparable'));
    }

    public loadBlockCompareView(problemKey: string, block1Id: number, block2Id: number): Observable<any> {
        const opts = {params: new HttpParams({fromString: 'blockId1=' + block1Id + '&blockId2=' + block2Id})};
        return this.http.get<any>(this.buildProblemUrl(problemKey, '/view/blockcompare'), opts);
    }

    public mergeLines(problemKey: string, line1: LogikViewLine, line2: LogikViewLine): Observable<boolean> {
        const data = {line1Id: line1.lineId, line2Id: line2.lineId};
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/merge'), data);
    }

    public blockUp(problemKey: string, selectedLine: LogikViewLine): Observable<boolean> {
        const data = {blockId: selectedLine.blockId, lineId: selectedLine.lineId};
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/block/lineup'), data);
    }

    public blockDown(problemKey: string, selectedLine: LogikViewLine): Observable<boolean> {
        const data = {blockId: selectedLine.blockId, lineId: selectedLine.lineId};
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/block/linedown'), data);
    }

    public applyBlockingCandidates(problemKey: string, groupId: number, selectedLines: number[]): Observable<boolean> {
        const data = {groupId, selectedLines};
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/blocking_candidates'), data);
    }

    public refresh(problemKey: string): Observable<boolean> {
        return this.http.put<boolean>(this.buildProblemUrl(problemKey, '/view/refresh'), {});
    }

    public buildProblemUrl(problemKey: string, path: string): string {
        return this.solveUrl + 'problems/' + problemKey + path;
    }

}
