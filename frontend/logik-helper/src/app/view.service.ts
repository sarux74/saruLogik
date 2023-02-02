import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {LogikViewLine} from './model/logik-view-line';

@Injectable({
    providedIn: 'root'
})
export class ViewService {
    private viewUrl: string;

    constructor(private http: HttpClient) {
        this.viewUrl = 'http://localhost:8080/problems';
    }

    public loadMultipleRelationView(key: string): Observable<LogikViewLine[]> {
        const opts = {params: new HttpParams()};

        return this.http.get<LogikViewLine[]>(this.viewUrl + '/' +  key + '/view/multiple', opts);
    }
}
