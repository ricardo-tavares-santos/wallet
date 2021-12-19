import { Transaction } from "./transaction.model";

export class GetTransaction {
  pageNoTotal?: any;
  pageSizeTotal?: any;
  data: Transaction[];
}
