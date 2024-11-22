import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';
import { LoginResponse } from '../../../services/auth/loginResponse';
import { HttpClientModule } from '@angular/common/http';
import { LoginRequest } from '../../../services/auth/loginRequest';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule, CommonModule, HttpClientModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit{

  loginError:string="";
  loginState: string='';
  expired: boolean = false;
  registered: boolean = false;
  verified: boolean = false;
  forgottenPassword: boolean = false;
  changedPassword: boolean = false;

  showPassword: boolean = false;

  constructor(private formBuilder: FormBuilder, private router:Router, private authService:AuthService, private route: ActivatedRoute ) {  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.expired = params['expired'];
      this.registered = params['registered'];
      this.verified = params['verified'];
      this.forgottenPassword = params['change-password'];
      this.changedPassword = params['changed-password'];
    });
    if(this.expired){
      this.loginError = "La sesión ha expirado. Por favor, inicie sesión nuevamente.";
    }
  }

  loginForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    password: ['', [Validators.required, Validators.maxLength(128), Validators.minLength(8)]]
  });

  login(){
    if(!this.loginForm.valid){
      this.loginError = "Introduzca un correo electrónico y una contraseña válidos";
      return;
    }
    this.loginState = 'LOADING';
    let alertShown = false;
    this.authService.login(this.loginForm.value as LoginRequest).subscribe({
      next: (loginData) => {
        loginData = loginData as LoginResponse;
        alertShown = loginData.alertShown;
      },
      error: (errorData) => {
        this.loginState = 'ERROR';
        console.error(errorData);
        this.loginError = errorData.message;
      },
      complete: () => {
        this.loginState = '';
        console.info("Login completo");
        this.router.navigate(['/']);
        if(!alertShown){
          this.router.navigateByUrl('/pacientes?alertShown=false')
        }else{
          this.router.navigateByUrl('/pacientes')
        }
        this.loginForm.reset();
      }
    });
  }

  togglePasswordVisibility(){
    this.showPassword = !this.showPassword;
  }

  get email(){
    return this.loginForm.controls.email;
  }

  get password(){
    return this.loginForm.controls.password;
  }
}
