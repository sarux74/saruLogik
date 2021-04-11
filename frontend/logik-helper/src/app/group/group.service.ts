import {Injectable} from '@angular/core';
import {LogikGroup} from './model/logik-group';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class GroupService {
    private groupsUrl: string;

    constructor(private http: HttpClient) {
        this.groupsUrl = 'http://localhost:8080/groups';
    }

    public current(): Observable<LogikGroup[]> {
        return this.http.get<LogikGroup[]>(this.groupsUrl);
    }
    public updateGroup(group: LogikGroup): Observable<boolean> {
        return this.http.put<boolean>(this.groupsUrl, group);
    }

}
