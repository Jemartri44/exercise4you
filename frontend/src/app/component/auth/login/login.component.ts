import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';
import { LoginRequest } from '../../../services/auth/loginRequest';
import { HttpClientModule } from '@angular/common/http';

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

  constructor(private formBuilder: FormBuilder, private router:Router, private authService:AuthService, private route: ActivatedRoute ) {  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.expired = params['expired'];
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
    this.authService.login(this.loginForm.value as LoginRequest).subscribe({
      next: (userData) => {console.log(userData);},
      error: (errorData) => {
        this.loginState = 'ERROR';
        console.error(errorData);
        this.loginError = errorData.message;
      },
      complete: () => {
        this.loginState = '';
        console.info("Login completo");
        this.router.navigate(['/']);
        this.loginForm.reset();
      }
    });
  }

  get email(){
    return this.loginForm.controls.email;
  }

  get password(){
    return this.loginForm.controls.password;
  }
}
