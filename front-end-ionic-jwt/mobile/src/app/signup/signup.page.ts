import {Component, ViewChild} from '@angular/core';
import {LoadingController, NavController, ToastController} from '@ionic/angular';
import {AuthService} from '../auth.service';
import {NgModel} from '@angular/forms';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.page.html',
  styleUrls: ['./signup.page.scss'],
})
export class SignupPage {

  @ViewChild('email')
  emailModel!: NgModel;

  constructor(private readonly navCtrl: NavController,
              private readonly authService: AuthService,
              private readonly loadingCtrl: LoadingController,
              private readonly toastCtrl: ToastController) {
  }

  async signup(value: { name: string, email: string, admin: boolean, password: string }): Promise<void> {
    const loading = await this.loadingCtrl.create({
      spinner: 'bubbles',
      message: 'Signing up ...'
    });

    loading.present();

    this.authService
      .signup(value)
      .pipe(finalize(() => loading.dismiss()))
      .subscribe(
        jwt => this.showSuccesToast(jwt),
        err => this.handleError(err));
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async handleError(error: any): Promise<void> {
    const message = 'Unexpected error occurred: ' + error;

    const toast = await this.toastCtrl.create({
      message,
      duration: 5000,
      position: 'bottom'
    });

    toast.present();
  }

  private async showSuccesToast(jwt: string): Promise<void> {
    if (jwt !== 'EXISTS') {
      const toast = await this.toastCtrl.create({
        message: 'Sign up successful',
        duration: 3000,
        position: 'bottom'
      });

      toast.present();
      this.navCtrl.navigateRoot(['login'], {replaceUrl: true});
    } else {
      const toast = await this.toastCtrl.create({
        message: 'Email already registered',
        duration: 3000,
        position: 'bottom'
      });

      toast.present();

      this.emailModel.control.setErrors({emailTaken: true});
    }
  }


}
