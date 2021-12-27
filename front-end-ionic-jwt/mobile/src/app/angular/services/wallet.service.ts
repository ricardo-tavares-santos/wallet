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

  createDeposit(send: SaveTransaction): Observable<any> {
    return this.http.post(`${baseUrl}/deposit`, send.data);
  }

  createWithdraw(send: SaveTransaction): Observable<any> {
    return this.http.post(`${baseUrl}/withdraw`, send.data);
  }

  createBet(send: SaveTransaction): Observable<any> {
    return this.http.post(`${baseUrl}/bet`, send.data);
  }

  createWin(send: SaveTransaction): Observable<any> {
    send.data.amount = Number(send.data.amount) + Number(send.data.amount);
    return this.http.post(`${baseUrl}/win`, send.data);
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