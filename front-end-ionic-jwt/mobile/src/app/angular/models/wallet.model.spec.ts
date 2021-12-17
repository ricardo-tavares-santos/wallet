import { Wallet } from './wallet.model';

describe('Wallet', () => {
  it('should create an instance', () => {
    expect(new Wallet()).toBeTruthy();
  });
});
