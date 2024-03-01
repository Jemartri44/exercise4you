import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { LoginRequest } from '../../services/auth/loginRequest';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule, CommonModule, HttpClientModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  loginError:string="";
  
  loginForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    password: ['', [Validators.required, Validators.maxLength(128), Validators.minLength(8)]]
  });
  constructor(private formBuilder: FormBuilder, private router:Router, private authService:AuthService) {  }

  login(){
    if(this.loginForm.valid){
      this.authService.login(this.loginForm.value as LoginRequest).subscribe({
        next: (userData) => {console.log(userData);},
        error: (errorData) => {
          console.error(errorData);
          this.loginError = errorData;
        },
        complete: () => {
          console.info("Login completo");
          this.router.navigate(['/']);
          this.loginForm.reset();
        }
      });
      
    }
    else{
      this.loginError="Por favor, ingrese un email y contraseña válidos";
    }
  }

  get email(){
    return this.loginForm.controls.email;
  }

  get password(){
    return this.loginForm.controls.password;
  }
}
