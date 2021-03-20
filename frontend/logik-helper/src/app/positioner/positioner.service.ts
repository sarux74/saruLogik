import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PositionerService {
  private positionerUrl: string;

  constructor(private http: HttpClient) {
    this.positionerUrl = 'http://localhost:8080/positioner/';
  }

  public initPositionerView(problemKey: string, positionGroupId: number, positionedGroupId: number) : Observable<boolean> {
    return this.http.put<boolean>(this.positionerUrl + 'problems/' + problemKey + '/init', {positionGroupId: positionGroupId, positionedGroupId: positionedGroupId });
  }

  // TODO: Datatype of result
  public loadPositionerView(problemKey: string): Observable<any> {
    return this.http.get<any>(this.positionerUrl + 'problems/' + problemKey +  '/view');
  }

  public updateSelection(problemKey: string, lineIndex: number, elementIndex: number, selection: number[]) {
    // TODO: Names from LogikView
    const data = {lineId: lineIndex, groupId: elementIndex, selection: selection};
    console.log(data);
    return this.http.put<boolean>(this.positionerUrl + 'problems/' + problemKey + '/selection', data);
  }

  public addLine(problemKey: string, lineIndex: number, direction: number) {
    return this.http.put<boolean>(this.positionerUrl + 'problems/' + problemKey + '/add', {lineId:lineIndex, direction: direction});
  }

  public removeLine(problemKey: string, lineIndex: number) {
  console.log(lineIndex);
    return this.http.put<boolean>(this.positionerUrl + 'problems/' + problemKey + '/remove', {lineId:lineIndex});
  }

  public overtake(problemKey: string) {
    return this.http.put<boolean>(this.positionerUrl + 'problems/' + problemKey + '/overtake', {});
  }

}
