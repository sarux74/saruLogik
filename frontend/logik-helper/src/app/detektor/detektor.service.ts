import {Injectable} from '@angular/core';
import {LogikView} from '../solve/model/logik-view';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ChangeResult} from '../solve/change-result';
import {LogikViewLine} from '../solve/model/logik-view-line';
import {IdNamePair} from '../block-compare-view/id-name-pair';
import {CombinationView} from '../combination-view/combination-view';


@Injectable({
    providedIn: 'root'
})
export class DetektorService {

    private solveUrl: string;

    constructor(private http: HttpClient) {
        this.solveUrl = 'http://localhost:8080/detektor/';
    }

    public load(): Observable<LogikView> {
        return this.http.get<LogikView>(this.solveUrl + 'lines');
    }

    public updateSelection(lineId: number, groupId: number, selection: number[]) {
        const data = {lineId, groupId, selection};
        console.log(data);
        return this.http.put<boolean>(this.solveUrl + 'selection', data);
    }

    public newBlock(result: any): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/new', result);
    }

    public newBlockPair(result: any): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'blockpair/new', result);
    }
    public flipBlock(blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/flip', blockId);
    }
    public showBlock(blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/show', blockId);
    }
    public hideBlock(blockId: number): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/hide', blockId);
    }

    public findNegatives(lineId: number): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.solveUrl + 'negative', lineId);
    }

    public findPositives(lineIds: number[]): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.solveUrl + 'positive', lineIds);
    }

    public newRelation(result: any): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'relation/new', result);
    }

    public loadGroupView(groupId: number): Observable<LogikViewLine[]> {
        const opts = {params: new HttpParams({fromString: 'groupId=' + groupId})};

        return this.http.get<LogikViewLine[]>(this.solveUrl + 'view/group', opts);
    }

    public loadComparableBlocks(): Observable<IdNamePair[]> {
        return this.http.get<IdNamePair[]>(this.solveUrl + 'view/block/comparable');
    }

    public loadBlockCompareView(block1Id: number, block2Id: number): Observable<any> {
        const opts = {params: new HttpParams({fromString: 'blockId1=' + block1Id + '&blockId2=' + block2Id})};
        return this.http.get<any>(this.solveUrl + 'view/blockcompare', opts);
    }

    public mergeLines(line1: LogikViewLine, line2: LogikViewLine): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'merge', {line1Id: line1.lineId, line2Id: line2.lineId});
    }

    public blockUp(selectedLine: LogikViewLine): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/lineup', {blockId: selectedLine.blockId, lineId: selectedLine.lineId});
    }

    public blockDown(selectedLine: LogikViewLine): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/linedown', {blockId: selectedLine.blockId, lineId: selectedLine.lineId});
    }

    public applyBlockingCandidates(groupId: number, selectedLines: number[]): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'blocking_candidates', {groupId, selectedLines});
    }


    public combinationView(): Observable<CombinationView> {
        return this.http.get<CombinationView>(this.solveUrl + 'view/combination');
    }

    public prepare(lineIds: number[]): Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'prepare', lineIds);
    }

    public exclude(lineIds: number[]): Observable<ChangeResult> {
        return this.http.put<ChangeResult>(this.solveUrl + 'exclude', lineIds);
    }
}
