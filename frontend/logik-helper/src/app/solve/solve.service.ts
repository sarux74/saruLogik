import { Injectable } from '@angular/core';
import {LogikView} from './model/logik-view';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
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

      public load(): Observable<LogikView> {
        return this.http.get<LogikView>(this.solveUrl + 'lines');
      }

       public updateSelection(lineIndex: number, groupIndex: number, selection: number[]) {
         const data = {lineId: lineIndex, groupId: groupIndex, selection: selection};
         console.log(data);
          return this.http.put<boolean>(this.solveUrl + 'selection', data);
       }

       public newBlock(result: any) : Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/new', result);
       }

       public flipBlock(blockId: number) : Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/flip', blockId);
       }
       public showBlock(blockId: number) : Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/show', blockId);
       }
       public hideBlock(blockId: number) : Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/hide', blockId);
       }

       public findNegatives(lineId: number) : Observable<ChangeResult> {
              return this.http.put<ChangeResult>(this.solveUrl + 'negative', lineId);
       }

       public findPositives(lineIds: number[]) : Observable<ChangeResult> {
              return this.http.put<ChangeResult>(this.solveUrl + 'positive', lineIds);
       }

       public newRelation(result: any) : Observable<boolean> {
         return this.http.put<boolean>(this.solveUrl + 'relation/new', result);
       }

       public loadGroupView(groupId: number) : Observable<LogikViewLine[]> {
         const opts = { params: new HttpParams({fromString: "groupId=" + groupId}) };

        return this.http.get<LogikViewLine[]>('http://localhost:8080/view/group', opts);
      }

      public loadComparableBlocks() : Observable<IdNamePair[]> {
          return this.http.get<IdNamePair[]>('http://localhost:8080/view/block/comparable');
      }

      public loadBlockCompareView(block1Id: number, block2Id: number) : Observable<any> {
         const opts = { params: new HttpParams({fromString: "blockId1=" + block1Id + "&blockId2=" + block2Id}) };
         return this.http.get<any>('http://localhost:8080/view/blockcompare', opts);
      }

      public mergeLines(line1: LogikViewLine, line2: LogikViewLine) : Observable<boolean> {
         return this.http.put<boolean>(this.solveUrl + 'merge', {line1Id: line1.lineId, line2Id: line2.lineId});
       }

       public blockUp(selectedLine: LogikViewLine) : Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/lineup', {blockId: selectedLine.blockId, lineId: selectedLine.lineId});
       }

       public blockDown(selectedLine: LogikViewLine) : Observable<boolean> {
        return this.http.put<boolean>(this.solveUrl + 'block/linedown', {blockId: selectedLine.blockId, lineId: selectedLine.lineId});
       }

        public applyBlockingCandidates(groupId: number, selectedLines: number[]) : Observable<boolean> {
         return this.http.put<boolean>(this.solveUrl + 'blocking_candidates', {groupId: groupId, selectedLines: selectedLines});
        }

}
