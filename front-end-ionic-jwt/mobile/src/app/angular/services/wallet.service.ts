import { SaveTransaction } from './../models/saveTransaction.model';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Wallet } from '../models/wallet.model';
import { environment } from 'src/environments/environment';
import { GetTransaction } from '../models/getTransaction.model';

const baseUrl = `${environment.serverURL}`;

@Injectable({
  providedIn: 'root'
})
export class WalletService {

  constructor(private http: HttpClient) { }

  createDeposit(data: SaveTransaction): Observable<any> {
    return this.http.post(`${baseUrl}/deposit`, data);
  }

  createWithdraw(data: SaveTransaction): Observable<any> {
    return this.http.post(`${baseUrl}/withdraw`, data);
  }

  createBet(data: SaveTransaction): Observable<any> {
    return this.http.post(`${baseUrl}/bet`, data);
  }

  createWin(data: SaveTransaction): Observable<any> {
    data.data.amount = Number(data.data.amount) + Number(data.data.amount);
    return this.http.post(`${baseUrl}/win`, data);
  }

  getWallet(playerId: any): Observable<Wallet> {
    return this.http.get(`${baseUrl}/balance/${Number(playerId)}`);
  }

  getTransactions(playerId: any, sortBy: any, pageNo: any, pageSize: any): Observable<GetTransaction> {
    let params = new HttpParams();
    sortBy ? params = params.append('sortBy', sortBy): ""
    pageNo ? params = params.append('pageNo', Number(pageNo)): ""
    pageSize ? params = params.append('pageSize', Number(pageSize)): ""
    return this.http.get<GetTransaction>(`${baseUrl}/transactions/${Number(playerId)}`, {params: params});
  }

  getIdempotencyToken(): Observable<any> {
    return this.http.get(`${baseUrl}/getIdempotencyToken`);
  }
}