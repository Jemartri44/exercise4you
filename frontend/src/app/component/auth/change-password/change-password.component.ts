import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';
import { CommonModule } from '@angular/common';
import { ChangePasswordRequest } from '../../../services/auth/changePasswordRequest';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ ReactiveFormsModule, CommonModule ],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent {

  changePasswordError:string="";
  changePasswordState: string='';
  expired: boolean = false;

  constructor(private formBuilder: FormBuilder, private router:Router, private authService:AuthService ) {  };
  
  changePasswordForm = this.formBuilder.group({
    password: ['', [Validators.required, Validators.maxLength(128), Validators.minLength(8)]],
    repeatPassword: ['', [Validators.required, Validators.maxLength(128), Validators.minLength(8)]]
  });

  changePassword(){
    if(!this.changePasswordForm.valid || this.password.value != this.repeatPassword.value){
      this.changePasswordError = "Por favor, compruebe que ambas contraseñas coinciden y son válidas";
      return;
    }
    this.changePasswordState = 'LOADING';
    let request = {token: this.router.url.split('=')[1], password: this.changePasswordForm.value.password} as ChangePasswordRequest;
    this.authService.changePassword(request).subscribe({
      next: (userData) => {console.log(userData);},
      error: (errorData) => {
        this.changePasswordState = 'ERROR';
        console.error(errorData);
        this.changePasswordError = errorData.message;
      },
      complete: () => {
        console.info("Contraseña cambiada");
        this.changePasswordState = '';
        this.router.navigateByUrl('/login?changed-password=true');
      }
    });
  }
  
  get password(){
    return this.changePasswordForm.controls.password;
  }

  get repeatPassword(){
    return this.changePasswordForm.controls.repeatPassword;
  }
}

