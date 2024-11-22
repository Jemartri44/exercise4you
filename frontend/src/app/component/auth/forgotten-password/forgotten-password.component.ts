import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgotten-password',
  standalone: true,
  imports: [ ReactiveFormsModule, CommonModule ],
  templateUrl: './forgotten-password.component.html',
  styleUrl: './forgotten-password.component.css'
})
export class ForgottenPasswordComponent {

  forgottenPasswordError:string="";
  forgottenPasswordState: string='';
  expired: boolean = false;

  constructor(private formBuilder: FormBuilder, private router:Router, private authService:AuthService ) {   }
  
  forgottenPasswordForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]]
  });

  forgottenPasswordEmail(){
    if(!this.forgottenPasswordForm.valid){
      this.forgottenPasswordError = "Introduzca un correo electrónico válido";
      return;
    }
    this.forgottenPasswordState = 'LOADING';
    this.authService.forgottenPasswordEmail(this.forgottenPasswordForm.value as string).subscribe({
      next: (userData) => {console.log(userData);},
      error: (errorData) => {
        this.forgottenPasswordState = 'ERROR';
        console.error(errorData);
        this.forgottenPasswordError = errorData.message;
      },
      complete: () => {
        console.info("Correo para reestablecer contraseña reenviado");
        this.forgottenPasswordState = '';
        this.router.navigateByUrl('/login?change-password=true');
      }
    });
  }
  
  get email(){
    return this.forgottenPasswordForm.controls.email;
  }
}

