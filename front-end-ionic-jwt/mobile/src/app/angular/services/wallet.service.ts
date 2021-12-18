import { Transaction } from './../models/transaction.model';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Wallet } from '../models/wallet.model';
import { environment } from 'src/environments/environment';

const baseUrl = `${environment.serverURL}`;

@Injectable({
  providedIn: 'root'
})
export class WalletService {

  constructor(private http: HttpClient) { }

  createDeposit(data: Transaction): Observable<any> {
    return this.http.post(`${baseUrl}/deposit`, data);
  }

  createWithdraw(data: Transaction): Observable<any> {
    return this.http.post(`${baseUrl}/withdraw`, data);
  }

  createBet(data: Transaction): Observable<any> {
    return this.http.post(`${baseUrl}/bet`, data);
  }

  createWin(data: Transaction): Observable<any> {
    data.amount = Number(data.amount) + Number(data.amount);
    return this.http.post(`${baseUrl}/win`, data);
  }

  getWallet(playerId: any): Observable<Wallet> {
    return this.http.get(`${baseUrl}/balance/${Number(playerId)}`);
  }

  getTransactions(playerId: any, sortBy: any, pageNo: any, pageSize: any): Observable<Transaction[]> {
    let params = new HttpParams();
    sortBy ? params = params.append('sortBy', sortBy): ""
    pageNo ? params = params.append('pageNo', Number(pageNo)): ""
    pageSize ? params = params.append('pageSize', Number(pageSize)): ""
    return this.http.get<Transaction[]>(`${baseUrl}/transactions/${Number(playerId)}`, {params: params});
  }

  getIdempotencyToken(): Observable<any> {
    return this.http.get(`${baseUrl}/getIdempotencyToken`);
  }
}