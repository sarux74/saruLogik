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
        return this.http.get<LogikView>(this.solveUrl + 'problems/' + problemKey + '/view');
    }

    public updateSelection(problemKey: string, lineIndex: number, groupIndex: number, selection: number[]) {
        const data = {lineId: lineIndex, groupId: groupIndex, selection: selection};
        console.log(data);
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/selection', data);
    }

    public newBlock(problemKey: string, result: any): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/block/new', result);
    }

    public flipBlock(problemKey: string, blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/block/flip', blockId);
    }
    public showBlock(problemKey: string, blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/block/show', blockId);
    }
    public hideBlock(problemKey: string, blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/block/hide', blockId);
    }

    public findNegatives(problemKey: string, lineId: number): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.solveUrl + 'problems/' + problemKey + '/negative', lineId);
    }

    public findPositives(problemKey: string, lineIds: number[]): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.solveUrl + 'problems/' + problemKey + '/positive', lineIds);
    }

    public newRelation(problemKey: string, result: any): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/relation/new', result);
    }

    public newCase(problemKey: string, lineId1: number, lineId2: number): Observable<string> {
        const data = {'line1Id': lineId1, 'line2Id': lineId2};
        return this.http.put<string>(this.solveUrl + 'problems/' + problemKey + '/case/new', data);
    }

    public closeCase(problemKey: string): Observable<string> {
        return this.http.put<string>(this.solveUrl + 'problems/' + problemKey + '/case/close', null);
    }

    public loadGroupView(problemKey: string, groupId: number): Observable<LogikViewLine[]> {
        const opts = {params: new HttpParams({fromString: "groupId=" + groupId})};

        return this.http.get<LogikViewLine[]>(this.solveUrl + 'problems/' + problemKey + '/view/group', opts);
    }

    public loadComparableBlocks(problemKey: string): Observable<IdNamePair[]> {
        return this.http.get<IdNamePair[]>(this.solveUrl + 'problems/' + problemKey + '/view/block/comparable');
    }

    public loadBlockCompareView(problemKey: string, block1Id: number, block2Id: number): Observable<any> {
        const opts = {params: new HttpParams({fromString: "blockId1=" + block1Id + "&blockId2=" + block2Id})};
        return this.http.get<any>(this.solveUrl + 'problems/' + problemKey + '/view/blockcompare', opts);
    }

    public mergeLines(problemKey: string, line1: LogikViewLine, line2: LogikViewLine): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/merge', {line1Id: line1.lineId, line2Id: line2.lineId});
    }

    public blockUp(problemKey: string, selectedLine: LogikViewLine): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/block/lineup', {blockId: selectedLine.blockId, lineId: selectedLine.lineId});
    }

    public blockDown(problemKey: string, selectedLine: LogikViewLine): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/block/linedown', {blockId: selectedLine.blockId, lineId: selectedLine.lineId});
    }

    public applyBlockingCandidates(problemKey: string, groupId: number, selectedLines: number[]): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/blocking_candidates', {groupId: groupId, selectedLines: selectedLines});
    }

    public refresh(problemKey: string): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'problems/' + problemKey + '/view/refresh', {});
    }

}
