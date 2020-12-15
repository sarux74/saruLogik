import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import {LogikViewLine} from './solve/model/logik-view-line';

@Injectable({
  providedIn: 'root'
})
export class ViewService {
private viewUrl: string;

      constructor(private http: HttpClient) {
        this.viewUrl = 'http://localhost:8080/view';
      }

      public loadMultipleRelationView() : Observable<LogikViewLine[]> {
               const opts = { params: new HttpParams() };

          return this.http.get<LogikViewLine[]>('http://localhost:8080/view/multiple', opts);
      }
}
