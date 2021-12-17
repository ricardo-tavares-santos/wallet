import { Wallet } from './transaction.model';

describe('Transaction', () => {
  it('should create an instance', () => {
    expect(new Wallet()).toBeTruthy();
  });
});
