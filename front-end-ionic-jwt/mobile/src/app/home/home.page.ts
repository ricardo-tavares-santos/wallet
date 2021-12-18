import {Component, OnInit} from '@angular/core'; 
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../auth.service';
import {JwtHelperService} from '@auth0/angular-jwt';
import {environment} from '../../environments/environment';
import { Wallet } from '../angular/models/wallet.model';
import { Transaction } from '../angular/models/transaction.model';
import { WalletService } from '../angular/services/wallet.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.scss']
})
export class HomePage implements OnInit {

  admin!: boolean;
  player!: any | null;
  playerId!: any | null;
  
  //message: string | null = null;

  filterWin = true;
  filterBet = true;
  filterDeposit = true;
  filterWithdraw = true;

  transactions?: Transaction[];
  transactionsFull?: Transaction[];
  title = '';

  addBet = false;
  addDeposit = false;
  addWithdraw = false;

  wallet: Wallet = {
    cashBalance: 0,
    bonusBalance: 0,
  };
  transaction: Transaction = {
    playerId: 0,
    transactionId: '',
    amount: 0,
    typeTransaction: '',
  };

  totalWallet: number = 0;

  constructor(private readonly authService: AuthService,
              jwtHelper: JwtHelperService,
              private readonly httpClient: HttpClient,
              private walletService: WalletService,) {
    this.authService.authUserObservable.subscribe(jwt => {
      if (jwt) {
        const decoded = jwtHelper.decodeToken(jwt);
        this.player = decoded.player.name;
        this.admin = decoded.player.admin;
        this.playerId = decoded.player.id;
      } else {
        this.player = null;
      }
    });
  }

  ngOnInit(): void {
    this.httpClient.get(`${environment.serverURL}/secret`, {responseType: 'text'}).subscribe(
      //text => this.message = text,
      err => console.log(err)
    );
    this.retrieveTransactions();
  }

  logout(): void {
    this.authService.logout();
  }

  // ------------------ List ------------------

  checkValues(event: String){
    if (event==='WIN') {
      this.filterWin===true?this.filterWin=false:this.filterWin=true
    } else if (event==='BET') {
      this.filterBet===true?this.filterBet=false:this.filterBet=true
    } else if (event==='DEPOSIT') {
      this.filterDeposit===true?this.filterDeposit=false:this.filterDeposit=true
    } else if (event==='WITHDRAW') {
      this.filterWithdraw===true?this.filterWithdraw=false:this.filterWithdraw=true
    }
    this.refreshList();
  }

  orderBy: any = "dateTransaction";
  pageNo: any;
  pageSize: any = "3";
  public optionsFn(): void { 
    this.retrieveTransactions();
  }
  public setMore(): void { 
    this.pageSize = Number(this.pageSize) + 3;
    this.retrieveTransactions();
  }

  retrieveTransactions(): void {
    let playerId = "0";
    if (!this.admin) {
      playerId = this.playerId;
    }
    this.walletService.getTransactions(playerId, this.orderBy, this.pageNo, this.pageSize)
      .subscribe(
        data => {
          if (data !== null && data.length > 0) {
            this.transactions = data.filter(item => {
              if(this.filterWin && item.typeTransaction === 'WIN') return true;
              if(this.filterBet && item.typeTransaction === 'BET') return true;
              if(this.filterDeposit && item.typeTransaction === 'DEPOSIT') return true;
              if(this.filterWithdraw && item.typeTransaction === 'WITHDRAW') return true;
            });
            this.transactionsFull = data;
          }
        },
        error => {
          console.log(error);
        });

    this.walletService.getWallet(playerId)
      .subscribe(
        data => {
            if (data !== null) {
              this.wallet = data;
              this.totalWallet = Number(data.cashBalance) + Number(data.bonusBalance);
            }
          },
          error => {
            console.log(error);
          });

  }

  refreshList(): void {
    this.retrieveTransactions();
    this.addBet = false;
    this.addDeposit = false;
    this.addWithdraw = false;
    this.transaction= {
      playerId: this.playerId,
      transactionId: '',
      amount: 0,
      typeTransaction: '',
    };
  }

  setAddDeposit(): void {
    this.refreshList();
    this.addDeposit = true;
  }
  setAddWithdraw(): void {
    this.refreshList();
    this.addWithdraw = true;
  }
  setAddBet(): void {
    this.refreshList();
    this.addBet = true;
  }

  // ------------------ Save ------------------

  saveWithdraw(): void {    
    this.transaction.playerId = this.playerId;
    this.transaction.typeTransaction = 'WITHDRAW';
    this.transaction.transactionId = this.createTransactionId();
    this.walletService.getIdempotencyToken().subscribe(
      responseToken => {
        console.log(responseToken);
        this.transaction.idempotency_Key = responseToken.idempotency_Key;
        this.walletService.createWithdraw(this.transaction).subscribe(
          response => {
            console.log(response);
            this.refreshList();
          },
          error => {
            console.log(error);
          });
      },
      error => {
        console.log(error);
      });
  }

  saveDeposit(): void {    
    this.transaction.playerId = this.playerId;
    this.transaction.typeTransaction = 'DEPOSIT';
    this.transaction.transactionId = this.createTransactionId();
    this.walletService.getIdempotencyToken().subscribe(
      responseToken => {
        console.log(responseToken);
        this.transaction.idempotency_Key = responseToken.idempotency_Key;
        this.walletService.createDeposit(this.transaction).subscribe(
          response => {
            console.log(response);
            this.refreshList();
          },
          error => {
            console.log(error);
          });
      },
      error => {
        console.log(error);
      });
  }

  saveBet(): void {   
    this.transaction.playerId = this.playerId; 
    this.transaction.typeTransaction = 'BET';
    this.transaction.transactionId = this.createTransactionId();
    this.walletService.getIdempotencyToken().subscribe(
      responseToken => {
        console.log(responseToken);
        this.transaction.idempotency_Key = responseToken.idempotency_Key;
        this.walletService.createBet(this.transaction).subscribe(
          response => {
            console.log(response);
            this.refreshList();
          },
          error => {
            console.log(error);
          });
      },
      error => {
        console.log(error);
      });
  }

  saveWinAdmin(transactionRow: Transaction): void {   
    transactionRow.typeTransaction = 'WIN';
    this.walletService.getIdempotencyToken().subscribe(
      responseToken => {
        console.log(responseToken);
        transactionRow.idempotency_Key = responseToken.idempotency_Key;
        this.walletService.createWin(transactionRow).subscribe(
          response => {
            console.log(response);
            this.refreshList();
          },
          error => {
            console.log(error);
          });
      },
      error => {
        console.log(error);
      });
  }

  saveWinOnline(): void {   
    this.transaction.playerId = this.playerId; 
    this.transaction.typeTransaction = 'BET';
    this.transaction.transactionId = this.createTransactionId();
    this.walletService.getIdempotencyToken().subscribe(
      responseToken => {
        console.log(responseToken);
        this.transaction.idempotency_Key = responseToken.idempotency_Key;
        this.walletService.createBet(this.transaction).subscribe(
          responseWallet => {
            console.log(responseWallet);
            this.transaction.typeTransaction = 'WIN';
            this.walletService.createWin(this.transaction).subscribe(
              response => {
                console.log(response);
                this.refreshList();
              },
              error => {
                console.log(error);
              });
    
          },
          error => {
            console.log(error);
          });
      },
      error => {
        console.log(error);
      });
  }

  createTransactionId(): string {    
    return this.player.toLowerCase().replace(/ /g,"-")+"-"+this.makeid(3)+"-"+this.makeid(7)+"-"+this.makeid(3);
  }
  

  makeid(length) {
    var result           = '';
    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for ( var i = 0; i < length; i++ ) {
      result += characters.charAt(Math.floor(Math.random() * 
      charactersLength));
    }
    return result;
  }

  alreadyWon(transactionRow: Transaction): boolean {
    if (this.transactionsFull.filter(item => item.transactionId === transactionRow.transactionId)?.length>1) {
      return true;
    }
    return false;
  }

}
