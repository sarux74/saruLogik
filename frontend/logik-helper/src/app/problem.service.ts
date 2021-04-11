import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class ProblemService {
    private viewUrl: string;

    constructor(private http: HttpClient) {
        this.viewUrl = 'http://localhost:8080/problem';
    }

     public newProblem(): Observable<boolean> {
        return this.http.put<boolean>(this.viewUrl + '/new', null);
    }

    public loadProblem(input: any): Observable<number> {
        const formData = new FormData();
        formData.append('file', input.target.files[0]);
        console.log(formData);
        return this.http.post<any>(this.viewUrl + '/load', formData);
    }

}
