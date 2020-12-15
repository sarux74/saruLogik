import { Injectable } from '@angular/core';
import { LogikGroup } from './model/logik-group';
import { HttpClient, HttpHeaders } from '@angular/common/http';
  import { Observable } from 'rxjs';

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

    public newProblem(): Observable<boolean> {
      return this.http.put<boolean>("http://localhost:8080/problem/new", null);
    }

    public initProblem(): Observable<boolean> {
        return this.http.put<boolean>("http://localhost:8080/solve/new", null);
    }

    public initDetektor(): Observable<boolean> {
        return this.http.put<boolean>("http://localhost:8080/detektor/new", null);
    }

  public loadProblem(input: any): Observable<number> {
    const formData = new FormData();
    formData.append('file', input.target.files[0]);
    return this.http.post<any>("http://localhost:8080/problem/load", formData);
  }
}
