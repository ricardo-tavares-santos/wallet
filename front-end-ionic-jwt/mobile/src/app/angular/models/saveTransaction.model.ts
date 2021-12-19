import { Transaction } from "./transaction.model";

export class SaveTransaction {
  idempotency_Key?: any;
  data: Transaction;
}
